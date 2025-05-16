package com.sigma.huella_api;

import com.sigma.lib.Base64ToPngConverter;
import com.sigma.lib.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/huellas")
public class HuellaApiController {
    private static final String UPLOAD_DIR = "uploads/";
    private Base64ToPngConverter base64Converter = new Base64ToPngConverter();

    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestParam("fingerprint") String base64,
            @RequestParam("userId") String userId) {
        
        try {
            String pngFilePath = this.base64Converter.convertToPngFile(base64, UPLOAD_DIR, userId);
             return ResponseEntity.ok(new ApiResponse(true, "Huella del usuario " + userId + " guardada exitosamente."));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "Error al procesar huellas" ));
        }
    }
}