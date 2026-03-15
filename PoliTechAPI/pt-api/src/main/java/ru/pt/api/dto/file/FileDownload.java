package ru.pt.api.dto.file;

import java.io.InputStream;

public record FileDownload(

        InputStream inputStream,
        String filename,
        String contentType,
        long size
) {}
