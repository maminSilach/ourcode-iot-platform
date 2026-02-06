package com.example.apiorchestrator.utils;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;

public final class JwtUtils {

    private JwtUtils() {
        throw new UnsupportedOperationException();
    }

    public static String getBearerAuthorizationHeader(String url) throws URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();

        URI authorizationURI = new URIBuilder(url).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.put("grant_type", singletonList("client_credentials"));
        formData.put("client_id", singletonList("device-client"));
        formData.put("client_secret", singletonList("EExb34BXy7xXRqTRN4wzPECl7D1Nbpe6"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(authorizationURI, request, String.class);

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return "Bearer " + jsonParser.parseMap(response.getBody())
                .get("access_token")
                .toString();
    }
}
