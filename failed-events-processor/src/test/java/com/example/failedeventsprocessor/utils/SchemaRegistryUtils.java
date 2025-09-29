package com.example.failedeventsprocessor.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SchemaRegistryUtils {

    public static void registerSchema(String address, String subject) {
        RestClient restClient = RestClient.create();
        String url = address + "/subjects/" + subject + "/versions";

        restClient
                .post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(escapeJson())
                .retrieve()
                .toEntity(String.class);
    }

    private static String escapeJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("schema", loadSchemaFromFile());

        return requestNode.toString();
    }

    public static String loadSchemaFromFile(){
        ClassPathResource resource = new ClassPathResource("failed-event.avsc");
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}