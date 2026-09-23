package com.urbanojvr.monex.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Crea {@code ~/.monex} antes de que Spring Boot abra la base SQLite.
 */
public class DataDirectoryInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Path dataDir = Path.of(System.getProperty("user.home"), ".monex");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de datos: " + dataDir, e);
        }
    }
}
