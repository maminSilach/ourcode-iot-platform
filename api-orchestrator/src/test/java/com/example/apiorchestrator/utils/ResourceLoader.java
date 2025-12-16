package com.example.apiorchestrator.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.StreamUtils.copyToString;

public final class ResourceLoader {

    public static String loadResourceAsString(String resourceName, FileExtension fileExtension) {
        String resourceNameWithExtension = resourceName + fileExtension.getExtension();

        try (InputStream resourceInputStream = new ClassPathResource(resourceNameWithExtension).getInputStream()) {
            return copyToString(resourceInputStream, UTF_8);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
