package com.sigma.huella_api;
import com.sigma.lib.Base64ToPngConverter;
import com.sigma.lib.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.machinezoo.sourceafis.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/huellas")
public class ValidarHuellasController {

    private String TEMP_DIR = "temp/";
    private String HUELAS_DIR = "uploads/";
    private Base64ToPngConverter base64Converter = new Base64ToPngConverter();

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse> validarHuella(
            @RequestParam("fingerprint") String base64,
            @RequestParam("userId") String userId) {
        
        try {
            String tempFileName = "temp_" + userId + "_" + UUID.randomUUID() + ".png";
            String tempFilePath = this.base64Converter.convertToPngFile(base64, TEMP_DIR, tempFileName);
            Path huellaRegistradaPath = Paths.get(HUELAS_DIR + userId + ".png");
            if (!Files.exists(huellaRegistradaPath)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No existe huella registrada para este usuario"));
            }

            // 4. Comparar huellas
            boolean coinciden = compararHuellas(tempFilePath, huellaRegistradaPath.toString());
            Files.deleteIfExists(Paths.get(tempFilePath));
            String result = coinciden ? "Huellas coinciden" : "Huellas no coinciden";
            return ResponseEntity.ok(new ApiResponse(coinciden, result));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Error al procesar huellas" ));
        }
    }

    private boolean compararHuellas(String pathHuellaTemp, String pathHuellaRegistrada) {
        try {
            var huellaTemp = Files.readAllBytes(Paths.get(pathHuellaTemp));
            var huella = Files.readAllBytes(Paths.get(pathHuellaRegistrada));

            var imageHuellaTemp = new FingerprintImage(huellaTemp);
            var imageHuella = new FingerprintImage(huella);

            var imagenBase = new FingerprintTemplate(imageHuella);
            var imagenCandidata = new FingerprintTemplate(imageHuellaTemp);
            var matcher = new FingerprintMatcher(imagenBase);
            double similarity = matcher.match(imagenCandidata);
            double threshold = 40;
            boolean matches = similarity >= threshold;
            
            return matches;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}