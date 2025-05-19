package com.sigma.lib;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Component
public class Base64ToPngConverter {

    private static final String BASE64_PREFIX = "data:image/png;base64,";

    public void convertMultipleToPngFiles(String[] base64Images, String outputDir, String userId) throws IOException {
        validateInputsForMultiple(base64Images, outputDir, userId);
        Path userDir = prepareOutputDirectory(outputDir).resolve(sanitizeFilename(userId));
        if (Files.exists(userDir)) {
            FileUtils.forceDelete(new File(userDir.toString()));
        }
        Files.createDirectories(userDir);

        for (String base64Image : base64Images) {
            if (StringUtils.isNotBlank(base64Image)) {
                String imageData = extractBase64Data(base64Image);
                byte[] imageBytes = decodeBase64(imageData);
                String randomName = UUID.randomUUID().toString();
                String filePath = userDir.resolve(randomName + ".png").toString();

                writeImageFile(filePath, imageBytes);
            }
        }
    }

    private void validateInputsForMultiple(String[] base64Images, String outputDir, String userId) {
        if (base64Images == null || base64Images.length == 0) {
            throw new IllegalArgumentException("El array de imágenes Base64 no puede estar vacío");
        }
        if (StringUtils.isBlank(outputDir)) {
            throw new IllegalArgumentException("El directorio de salida no puede estar vacío");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("El ID de usuario no puede estar vacío");
        }
    }

    public String convertToPngFile(String base64Image, String outputDir, String fileName) throws IOException {
        validateInputs(base64Image, outputDir, fileName);
        Path uploadPath = prepareOutputDirectory(outputDir);

        String imageData = extractBase64Data(base64Image);
        byte[] imageBytes = decodeBase64(imageData);

        String safeFileName = sanitizeFilename(fileName);
        String filePath = uploadPath.resolve(safeFileName + ".png").toString();

        writeImageFile(filePath, imageBytes);

        return filePath;
    }

    private void validateInputs(String base64Image, String outputDir, String fileName) {
        if (StringUtils.isBlank(base64Image)) {
            throw new IllegalArgumentException("La imagen Base64 no puede estar vacía");
        }
        if (StringUtils.isBlank(outputDir)) {
            throw new IllegalArgumentException("El directorio de salida no puede estar vacío");
        }
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("El nombre de archivo no puede estar vacío");
        }
    }

    private Path prepareOutputDirectory(String outputDir) throws IOException {
        Path uploadPath = Paths.get(outputDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String extractBase64Data(String base64Image) {
        String imageData = base64Image.startsWith(BASE64_PREFIX) 
            ? base64Image.substring(BASE64_PREFIX.length())
            : base64Image;

        imageData = imageData.replaceAll("\\s", "");

        if (StringUtils.isBlank(imageData)) {
            throw new IllegalArgumentException("Datos Base64 no válidos (cadena vacía después de limpieza)");
        }

        return imageData;
    }

    private byte[] decodeBase64(String base64Data) {
        try {
            int padding = base64Data.length() % 4;
            if (padding > 0) {
                base64Data += "===".substring(0, 4 - padding);
            }

            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error decodificando Base64: " + e.getMessage(), e);
        }
    }

    private String sanitizeFilename(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private void writeImageFile(String filePath, byte[] imageBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(imageBytes);
            fos.flush();
        }
    }
}