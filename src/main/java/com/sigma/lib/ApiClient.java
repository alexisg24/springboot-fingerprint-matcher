package com.sigma.lib;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.net.URI;
import io.github.cdimascio.dotenv.Dotenv;

@Component
public class ApiClient {

    private final String apiBaseUrl;
    private final RestTemplate restTemplate;
    private final Dotenv dotenv = Dotenv.configure().load();

    public ApiClient(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    public String getTokenBasedInUserName(String userName) {
    try {
        System.out.println("key: " + this.dotenv.get("secretKey"));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", userName);
        formData.add("secret_token",  this.dotenv.get("secretKey"));

        URI uri = UriComponentsBuilder.fromHttpUrl(this.apiBaseUrl)
                .path("/api/fingerprintapitoken.jsp")
                .build()
                .toUri();

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(uri, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            return this.sanitizeResponseToken(response.getBody());
        } else {
            throw new RuntimeException("Error en la respuesta del servidor: " + response.getStatusCode());
        }
    } catch (Exception e) {
        throw new RuntimeException("Error al realizar la petición: " + e.getMessage(), e);
    }
}

    private String sanitizeResponseToken(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\n", "");
    }
}
