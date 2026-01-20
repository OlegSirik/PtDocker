# Font Resources for PDF Generation

This directory contains font files used for PDF generation with Cyrillic character support.

## Required Font

Place `LiberationSans-Regular.ttf` in this directory to enable proper Cyrillic character support in generated PDFs.

### Download Liberation Sans

You can download Liberation Sans font from:
- https://github.com/liberationfonts/liberation-fonts/releases

Or use the font already available in most Linux distributions:
- `/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf` (Debian/Ubuntu)
- `/usr/share/fonts/liberation-sans/LiberationSans-Regular.ttf` (RedHat/CentOS)

### Alternative: Use System Fonts

If the font file is not present, PDFBox will automatically fall back to using the system's Liberation Sans font. The warnings in the logs are informational and do not prevent PDF generation.

## Font License

Liberation Sans is licensed under the SIL Open Font License, which allows free use in both open source and commercial applications.
