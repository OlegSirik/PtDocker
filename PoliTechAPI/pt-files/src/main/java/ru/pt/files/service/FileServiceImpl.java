package ru.pt.files.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;
import ru.pt.api.dto.auth.Tenant;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.file.FileDownload;
import ru.pt.api.dto.file.FileModel;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.file.StorageProperty;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.file.FileStorage;
import ru.pt.auth.repository.TenantRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.model.VariableContext;
import ru.pt.files.entity.FileEntity;
import ru.pt.files.repository.FileRepository;
import java.io.FileInputStream;
import lombok.RequiredArgsConstructor;

import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.TenantConfig;
import ru.pt.api.service.auth.AuthorizationService;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {


    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final SecurityContextHelper securityContextHelper;
    private final TextDocumentView textDocumentView;
    private final List<FileStorage> storages;
    private final TenantConfig tenantConfig;
    private final AuthorizationService authorizationService;


    private static final String FONT_CLASSPATH = "fonts/calibri.ttf";

    /**
     * Load a TrueType font that supports Cyrillic characters (Liberation Sans, compatible with Helvetica).
     * Tries classpath first, then filesystem fallbacks.
     *
     * @param doc The PDF document
     * @return PDFont instance or null if no suitable font found
     */
    private PDFont loadCyrillicFont(PDDocument doc) {
        // 1. Classpath resource (preferred)
        try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream(FONT_CLASSPATH)) {
            if (fontStream != null) {
                logger.debug("Loading font from classpath: {}", FONT_CLASSPATH);
                return PDType0Font.load(doc, fontStream, false);
            }
        } catch (IOException e) {
            logger.trace("Could not load font from classpath: {}", e.getMessage());
        }

        // 2. Alternative: getClass().getResource (for some classloader setups)
        try (InputStream fontStream = getClass().getResourceAsStream("/" + FONT_CLASSPATH)) {
            if (fontStream != null) {
                logger.debug("Loading font from classpath (absolute): /{}", FONT_CLASSPATH);
                return PDType0Font.load(doc, fontStream, false);
            }
        } catch (IOException e) {
            logger.trace("Could not load font from classpath (absolute): {}", e.getMessage());
        }

        // 3. Filesystem fallbacks (Linux)
        String[] fsPaths = {
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
            "/usr/share/fonts/liberation-sans/LiberationSans-Regular.ttf",
            "/usr/share/fonts/liberation/LiberationSans-Regular.ttf"
        };
        for (String path : fsPaths) {
            try {
                File f = new File(path);
                if (f.exists() && f.canRead()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        logger.debug("Loading font from file system: {}", path);
                        return PDType0Font.load(doc, fis, false);
                    }
                }
            } catch (Exception e) {
                logger.trace("Could not load font from {}: {}", path, e.getMessage());
            }
        }

        logger.info("No custom Cyrillic font found, PDFBox will use built-in fallback fonts");
        return null;
    }

    /**
     * Get current authenticated user from security context
     * @return AuthenticatedUser representing the current user
     * @throws ru.pt.api.dto.exception.UnauthorizedException if user is not authenticated
     */
    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new UnauthorizedException("Unable to get current user from context"));
    }

    /**
     * Get current tenant ID from authenticated user
     * @return Long representing the current tenant ID
     * @throws ru.pt.api.dto.exception.UnauthorizedException if user is not authenticated
     */
    protected Long getCurrentTenantId() {
        return getCurrentUser().getTenantId();
    }

    @Transactional
    @Override
    public Long uploadFile(String tenantCode, InputStream file, String filename, String contentType, Long size) {
        /* Файлы сохраняются в теннате, поэтому проверяем что это доступный тенант для юзера */
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.FILE, null, getCurrentTenantId(), AuthZ.Action.MANAGE);
        Tenant tenant = tenantConfig.getTenant(tenantCode);

        Map<String, String> storageConfig = tenant.storageConfig();
        if (storageConfig.containsKey(StorageProperty.MAX_SIZE.getValue())) {
            Long maxSize = Long.parseLong(storageConfig.get(StorageProperty.MAX_SIZE.getValue()));
            if (size > maxSize) {
                throw new UnprocessableEntityException("File size is too large");
            }
        }

        FileEntity entity = new FileEntity();
        entity.setTid(getCurrentTenantId());
        entity.setPublicId(UUID.randomUUID().toString());
        entity.setFilename(filename);
        entity.setContentType(contentType);
        entity.setSize(size);
        var saved = fileRepository.save(entity);

        FileStorage storage = resolve(tenant);
        storage.store(entity.getTid(), entity.getPublicId(), tenant.storageConfig(), file);

        return saved.getId();
    }

    @Override
    public FileDownload downloadFile(Long id) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        Tenant tenant = tenantConfig.getTenantById(entity.getTid());

        /* Файлы загружаются из тенната, поэтому проверяем что это доступный тенант для юзера */
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.FILE, null, tenant.id(), AuthZ.Action.VIEW);
        
        FileStorage storage = resolve(tenant);

        try (InputStream inputStream = storage.load(entity.getPublicId(), tenant.storageConfig())) {
           
            String filename = entity.getFilename() != null ? entity.getFilename() : "download";
            String contentType = entity.getContentType() != null ? entity.getContentType() : "application/octet-stream";
            return new FileDownload(inputStream, filename, contentType, entity.getSize());
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to download file", e);
        }
    }


    @Transactional
    @Override
    public void delete(Long id) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        // ToDo checj Auth
        Tenant tenant = tenantConfig.getTenantById( entity.getTid());
        authorizationService.check(getCurrentUser(), AuthZ.ResourceType.FILE, null, tenant.id(), AuthZ.Action.MANAGE);

        FileStorage storage = resolve(tenant);   
        storage.delete(entity.getPublicId(), tenant.storageConfig());
        fileRepository.delete(entity);
    }

    @Override
    public byte[] process(Long id, VariableContext keyValues) {
        
        logger.debug("Processing PDF file with id: {}", id);
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (entity.getFileBody() == null) {
            throw new IllegalArgumentException("File body is empty");
        }

        try (PDDocument doc = Loader.loadPDF( entity.getFileBody())) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form != null) {
                // Configure font for Cyrillic support
                PDFont cyrillicFont = loadCyrillicFont(doc);
                if (cyrillicFont != null) {
                    try {
                        // Set default appearance with the font that supports Cyrillic
                        PDResources resources = form.getDefaultResources();
                        if (resources == null) {
                            resources = new PDResources();
                        }

                        // Добавляем шрифт в ресурсы и получаем его имя
                        String fontName = resources.add(cyrillicFont).getName();
                        // Alias Helv (Helvetica) — многие PDF-формы используют его по умолчанию
                        resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"), cyrillicFont);
                        form.setDefaultResources(resources);

                        // Устанавливаем appearance для всех текстовых полей (включая вложенные)
                        for (PDField field : form.getFieldTree()) {
                            if (field instanceof PDTextField) {
                                ((PDTextField) field).setDefaultAppearance("/" + fontName + " 12 Tf 0 g");
                            }
                        }
                        
                        form.setDefaultAppearance("/" + fontName + " 12 Tf 0 g");
                        
                        logger.debug("Configured Liberation Sans font for Cyrillic support");
                    } catch (Exception e) {
                        logger.warn("Could not configure custom font: {}", e.getMessage());
                    }
                }
                
                // Fill form fields (включая вложенные через getFieldTree)
                for (PDField pdfield : form.getFieldTree()) {
                    //String fName = pdfield.getPartialName();
                    String fName = pdfield.getValueAsString();
                    String fieldValue = textDocumentView.get(keyValues, fName);

                    logger.debug("Field name: {}, value: {}", fName, fieldValue);

                    if (fieldValue != null) {
                        String fValue = fieldValue.toString();
                        try {
                            pdfield.setValue(fValue);
                            logger.trace("Set field '{}' to value: {}", fName, fValue);
 
                        } catch (Exception ex) {
                            logger.warn("Failed to set field '{}': {}", fName, ex.getMessage());
                        }
                    }
                }

                form.flatten();
                logger.debug("Form flattened successfully");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            logger.debug("PDF processed successfully, size: {} bytes", out.size());
            return out.toByteArray();
        } catch (IOException ex) {
            logger.error("Failed to process PDF: {}", ex.getMessage(), ex);
            throw new InternalServerErrorException("Failed to process PDF", ex);
        }
    }

    @Override
    public byte[] getFile(Integer fileId, VariableContext keyValues) {

        if (fileId == null) {
            throw new NotFoundException("File ID is not found");
        }

        keyValues.calcEmptyMagic();
        return process(fileId.longValue(), keyValues);
    }

    private FileStorage resolve(Tenant tenant) {

        FileStorageType fsType = tenant.storageType();
        logger.debug("Resolving storage for tenant: {}, type: {}", tenant.code(), fsType);
        for (FileStorage storage : storages) {
            if (storage.supports(fsType)) {
                return storage;
            }
        }
        throw new NotFoundException("Storage not found for tenant: " + tenant.code());
    }
}