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

    /**
     * Load a TrueType font that supports Cyrillic characters
     * @param doc The PDF document
     * @return PDFont instance or null if no suitable font found
     */
    private PDFont loadCyrillicFont(PDDocument doc) {
        // Try multiple font locations in order of preference
        String[] fontPaths = {
            "/fonts/LiberationSans-Regular.ttf"  // Classpath resource
        };
        
        for (String fontPath : fontPaths) {
            try {
                InputStream fontStream = null;
                
                // Try loading from classpath first
                if (fontPath.startsWith("/fonts/")) {
                    fontStream = getClass().getResourceAsStream(fontPath);
                    if (fontStream != null) {
                        logger.debug("Loading font from classpath: {}", fontPath);
                        return PDType0Font.load(doc, fontStream);
                    }
                }
                
                // Try loading from file system
                File fontFile = new File(fontPath);
                if (fontFile.exists() && fontFile.canRead()) {
                    logger.debug("Loading font from file system: {}", fontPath);
                    return PDType0Font.load(doc, new FileInputStream(fontFile));
                }
            } catch (Exception e) {
                logger.trace("Could not load font from {}: {}", fontPath, e.getMessage());
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

/*     
    public byte[] process_123(Long id, VariableContext keyValues) {
        
        logger.debug("Processing PDF file with id: {}", id);
        FileEntity entity = fileRepository.findActiveById(getCurrentTenantId(), id)
                .orElseThrow(() -> new NotFoundException("File not found"));
        if (entity.getFileBody() == null) {
            throw new IllegalArgumentException("File body is empty");
        }

        try (PDDocument doc = Loader.loadPDF( entity.getFileBody())) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form == null) {
                throw new IllegalStateException("PDF has no AcroForm");
            }
        
            form.setNeedAppearances(false);
            // Configure font for Cyrillic support
            PDType0Font font = loadCyrillicFont(doc);
            COSName fontName = null;

            if (form != null) {

                if (font != null) {
                    try {
                        // Set default appearance with the font that supports Cyrillic
                        PDResources resources = form.getDefaultResources();
                        if (resources == null) {
                            resources = new PDResources();
                            form.setDefaultResources(resources);
                        }
        
                        // добавляем шрифт и получаем имя ресурса
                        fontName = resources.add(font);
        
                        // 👇 ВАЖНЕЙШАЯ СТРОКА
                        form.setDefaultAppearance("/" + fontName.getName() + " 10 Tf 0 g");
        
                        logger.debug("Configured Unicode font {} for AcroForm", fontName.getName());
                    } catch (Exception e) {
                        logger.warn("Could not configure custom font: {}", e.getMessage());
                    }
                }
                
                // Fill form fields
                for (PDField pdfield : form.getFieldTree()) {
                    String fName = pdfield.getValueAsString();
                    if (fName == null) {
                        continue;
                    }
                    String fieldValue = textDocumentView.get(keyValues, fName);
                    if (fieldValue != null) {
                        String fValue = fieldValue.toString();
                        try {
                            float fontSize = getFieldFontSize(pdfield);
                            normalizeField(pdfield);
                            applyFieldFont(pdfield, fontName, fontSize);
                            pdfield.setReadOnly(false);
                            pdfield.setValue(fValue);
                            removeBorder(pdfield);
                            logger.trace("Set field '{}' to value: {}", fName, fValue);
 
                        } catch (Exception ex) {
                            logger.warn("Failed to set field '{}': {}", fName, ex.getMessage());
                        }
                    }

                    // Убрать рамку
                    //removeBorder(pdfield);
                }
                form.refreshAppearances();
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
*/
    /* 
    private void clearFieldAppearance(PDField field) {
        if (field instanceof PDTerminalField terminal) {
            for (PDAnnotationWidget widget : terminal.getWidgets()) {
                widget.setDefaultAppearance(null);
                widget.setAppearance(null);
            }
        }
    }
    */
   /* 
    private void normalizeField(PDField field) {
        if (!(field instanceof PDTerminalField terminal)) {
            return;
        }
    
        for (PDAnnotationWidget widget : terminal.getWidgets()) {
            COSDictionary w = widget.getCOSObject();
    
            // ❌ Убираем локальный DA (Helvetica)
            w.removeItem(org.apache.pdfbox.cos.COSName.DA);
    
            // ❌ Убираем старый appearance stream
            w.removeItem(org.apache.pdfbox.cos.COSName.AP);
        }
    
        // ❌ Иногда DA висит и на уровне поля
        field.getCOSObject().removeItem(COSName.DA);
    }
    
    private static void removeBorder(PDField field) {

        if (!(field instanceof PDTerminalField terminal)) {
            return;
        }
    
        for (PDAnnotationWidget widget : terminal.getWidgets()) {
    
            // BorderStyle
            PDBorderStyleDictionary borderStyle = widget.getBorderStyle();
            if (borderStyle != null) {
                borderStyle.setWidth(0);
            }
    
            // Border array (старый способ)
            widget.setBorder(new COSArray());
    
            // Appearance (важно!)
            widget.setAppearance(null);

            PDAppearanceCharacteristicsDictionary appearance = widget.getAppearanceCharacteristics();
            if (appearance == null) {
                appearance = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
            }
            appearance.setBorderColour(new PDColor(new float[] {}, PDDeviceRGB.INSTANCE));
            widget.setAppearanceCharacteristics(appearance);
        }
    }

    private static void applyFieldFont(PDField field, COSName fontName, float fontSize) {
        if (fontName == null || !(field instanceof PDVariableText variableText)) {
            return;
        }

        String defaultAppearance = "/" + fontName.getName() + " " + fontSize + " Tf 0 g";
        variableText.setDefaultAppearance(defaultAppearance);
    }

    private static float getFieldFontSize(PDField field) {
        if (!(field instanceof PDVariableText variableText)) {
            return 10f;
        }

        return extractFontSize(variableText.getDefaultAppearance());
    }

    private static float extractFontSize(String defaultAppearance) {
        if (defaultAppearance == null) {
            return 10f;
        }

        Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s+Tf").matcher(defaultAppearance);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return 10f;
            }
        }
        return 10f;
    }
    */
    //@Override
    public byte[] process_old(Long id, VariableContext keyValues) {
        
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
                PDFont font = loadCyrillicFont(doc);
                if (font != null) {
                    try {
                        // Set default appearance with the font that supports Cyrillic
                        PDResources resources = form.getDefaultResources();
                        if (resources == null) {
                            resources = new PDResources();
                        }
                        resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"), font);
                        form.setDefaultResources(resources);
                        
                        logger.debug("Configured Liberation Sans font for Cyrillic support");
                    } catch (Exception e) {
                        logger.warn("Could not configure custom font: {}", e.getMessage());
                    }
                }
                
                // Fill form fields
                for (PDField pdfield : form.getFields()) {
                    String fName = pdfield.getPartialName();
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
                PDFont font = loadCyrillicFont(doc);
                if (font != null) {
                    try {
                        // Set default appearance with the font that supports Cyrillic
                        PDResources resources = form.getDefaultResources();
                        if (resources == null) {
                            resources = new PDResources();
                        }
                        resources.put(org.apache.pdfbox.cos.COSName.getPDFName("Helv"), font);
                        form.setDefaultResources(resources);
                        
                        logger.debug("Configured Liberation Sans font for Cyrillic support");
                    } catch (Exception e) {
                        logger.warn("Could not configure custom font: {}", e.getMessage());
                    }
                }
                
                // Fill form fields
                for (PDField pdfield : form.getFields()) {
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