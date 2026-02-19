package ru.pt.files.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.file.FileModel;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.file.FileService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.model.VariableContext;
import ru.pt.files.entity.FileEntity;
import ru.pt.files.repository.FileRepository;
import java.io.FileInputStream;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final SecurityContextHelper securityContextHelper;
    private final TextDocumentView textDocumentView;

    public FileServiceImpl(FileRepository fileRepository, SecurityContextHelper securityContextHelper, TextDocumentView textDocumentView) {
        this.fileRepository = fileRepository;
        this.securityContextHelper = securityContextHelper; 
        this.textDocumentView = textDocumentView;
    }

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

    // pt_files will contain only id and fileDate. Other columns should be removed
    // ToDo remove this method
    @Transactional
    @Override
    public FileModel createMeta(String fileType, String fileDesc, String productCode, Integer packageCode) {
        FileEntity e = new FileEntity();
        e.setFileType(fileType);
        e.setFileDesc(fileDesc);
        e.setProductCode(productCode);
        e.setPackageCode(packageCode);
        e.setDeleted(false);
        e.setTid(getCurrentTenantId());
        var saved = fileRepository.save(e);
        var model = new FileModel();
        model.setId(saved.getId());
        model.setFileDescription(saved.getFileDesc());
        model.setDeleted(saved.isDeleted());
        model.setFileType(model.getFileType());
        model.setProductCode(saved.getProductCode());
        model.setPackageCode(saved.getPackageCode());
        return model;
    }

    @Transactional
    @Override
    public void uploadBody(Long id, byte[] file) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        entity.setFileBody(file);
        fileRepository.save(entity);
    }

    @Transactional
    @Override
    public Long uploadFile(Long tid, byte[] file) {
        FileEntity entity = new FileEntity();
        entity.setTid(tid);
        entity.setFileBody(file);
        entity.setDeleted(false);
        entity.setTid(getCurrentTenantId());
        var saved = fileRepository.save(entity);
        return saved.getId();
    }

    @Override
    public List<Map<String, Object>> list(String productCode) {
        List<Object[]> rows = fileRepository.listSummaries(getCurrentTenantId(), productCode);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r[0]);
            m.put("fileType", r[1]);
            m.put("fileDescription", r[2]);
            m.put("productCode", r[3]);
            m.put("packageCode", r[4]);
            result.add(m);
        }
        return result;
    }

    @Override
    public byte[] download(Long id) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (entity.getFileBody() == null) {
            throw new UnprocessableEntityException("File body is empty");
        }
        return entity.getFileBody();
    }

    @Transactional
    @Override
    public void softDelete(Long id) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        entity.setDeleted(true);
        fileRepository.save(entity);
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


    /*     @Override
    public byte[] process_old(Long id, VariableContext keyValues) {
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (entity.getFileBody() == null) {
            throw new IllegalArgumentException("File body is empty");
        }

        try (PDDocument doc = Loader.loadPDF( entity.getFileBody())) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form != null) {
                //form.getFields().forEach(System.out::println);

                for (Map.Entry<String, Object> e : keyValues.entrySet()) {
                    //String key = e.getKey();
                    //String value = e.getValue();

                    PDField field = form.getField(e.getKey());
                    if (field != null) {
                        System.out.println(e.getKey() + " " + e.getValue());
                        try {
                            field.setValue(e.getValue().toString());
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
                form.flatten();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new InternalServerErrorException("Failed to process PDF", ex);
        }
    }
    */

    @Override
    public byte[] getFile(Integer fileId, VariableContext keyValues) {

        //String productCode = JsonPath.parse(keyValues.get("product")).read("$.code");
        //String packageCode = keyValues.get("packageCode");
        
        //String fileId = keyValues.get("fileId");
        // TODO проверить что версия продукта сохраняется в keyValues !!!!!!!!
        if (fileId == null) {
            throw new NotFoundException("File ID is not found");
        }

//        FileEntity entity = fileRepository.findActiveByFileTypeAndProductCodeAndPackageCode(fileType, productCode, Integer.parseInt(packageCode))
//                .orElseThrow(() -> new IllegalArgumentException("File not found"));
//        return process(entity.getId(), keyValues);
        return process(fileId.longValue(), keyValues);
    }

}