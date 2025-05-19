package com.sigma.huella_api;
import com.sigma.lib.Base64ToPngConverter;
import com.sigma.lib.ApiResponse;

import com.sigma.lib.Base64ToPngConverter;
import com.sigma.lib.ApiResponse;
import com.sigma.lib.ApiClient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.machinezoo.sourceafis.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/api/huellas")
@CrossOrigin(origins = "*")
public class ValidarHuellasController {

    private String TEMP_DIR = "temp/";
    private String HUELAS_DIR = "uploads/";
    private Base64ToPngConverter base64Converter = new Base64ToPngConverter();
    private ApiClient apiClient = new ApiClient("https://demo.getsigmacare.com/develop");

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse> validarHuella(
            @RequestParam("fingerprint") String base64,
            @RequestParam("userId") String userId) {
        
        try {
            String tempFileName = "temp_" + userId + "_" + UUID.randomUUID() + ".png";
            String tempFilePath = this.base64Converter.convertToPngFile(base64, TEMP_DIR, tempFileName);
            Path[] huellaRegistradaPath = obtenerHuellasUsuario(userId);
            if (huellaRegistradaPath.length == 0) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No existe huella registrada para este usuario"));
            }

            boolean coinciden = compararMultiplesHuellas(tempFilePath, huellaRegistradaPath);
            Files.deleteIfExists(Paths.get(tempFilePath));
            String result = coinciden ? "Huellas coinciden" : "Huellas no coinciden";
            if(coinciden) {
                String token = apiClient.getTokenBasedInUserName(userId);
                if (token != null) {
                    return ResponseEntity.ok(new ApiResponse(coinciden, token));
                }
            }
            
            return ResponseEntity.ok(new ApiResponse(coinciden, result));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Error al procesar huellas"));
        }
    }

    
    // Función privada para obtener todas las huellas de un usuario
    private Path[] obtenerHuellasUsuario(String userId) throws IOException {
        Path userDir = Paths.get(HUELAS_DIR + userId);
        
        if (!Files.exists(userDir) || !Files.isDirectory(userDir)) {
            return new Path[0];
        }
        
        try (Stream<Path> paths = Files.list(userDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                .toArray(Path[]::new);
        }
    }

    private boolean compararMultiplesHuellas(String pathHuellaCandidata, Path[] huellasRegistradas) {
        try {
            byte[] candidataBytes = Files.readAllBytes(Paths.get(pathHuellaCandidata));
            FingerprintImage candidataImage = new FingerprintImage(candidataBytes);
            FingerprintTemplate candidataTemplate = new FingerprintTemplate(candidataImage);
            
            FingerprintMatcher matcher = new FingerprintMatcher(candidataTemplate);
            double threshold = 40;
            
            for (Path huellaPath : huellasRegistradas) {
                byte[] registradaBytes = Files.readAllBytes(huellaPath);
                FingerprintImage registradaImage = new FingerprintImage(registradaBytes);
                FingerprintTemplate registradaTemplate = new FingerprintTemplate(registradaImage);
                
                double similarity = matcher.match(registradaTemplate);
                if (similarity >= threshold) {
                    return true;
                }
            }
            
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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