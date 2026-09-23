package com.urbanojvr.monex.desktop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class BackendProcess {

    private final int port;
    private Process process;

    BackendProcess(int port) {
        this.port = port;
    }

    void start() throws IOException {
        Path jar = resolveBackendJar();
        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.add("-jar");
        command.add(jar.toAbsolutePath().toString());
        command.add("--server.port=" + port);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        process = builder.start();
    }

    void stop() {
        if (process == null || !process.isAlive()) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    private static String javaExecutable() {
        String javaHome = System.getProperty("java.home");
        if (javaHome != null) {
            Path java = Path.of(javaHome, "bin", "java");
            if (Files.isExecutable(java)) {
                return java.toString();
            }
        }
        return "java";
    }

    private static Path resolveBackendJar() throws IOException {
        Path fromClasspath = extractClasspathBackendJar();
        if (fromClasspath != null) {
            return fromClasspath;
        }

        Path[] candidates = {
                Path.of("monex-backend", "target", "monex-backend-0.0.1-SNAPSHOT.jar"),
                Path.of("..", "monex-backend", "target", "monex-backend-0.0.1-SNAPSHOT.jar"),
                Path.of("target", "backend", "monex-backend.jar"),
        };

        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(absolute)) {
                return absolute;
            }
        }

        throw new IOException(
                "No se encontró monex-backend.jar. Ejecuta primero: ./mvnw -pl monex-backend -am package");
    }

    private static Path extractClasspathBackendJar() throws IOException {
        try (var in = BackendProcess.class.getResourceAsStream("/backend/monex-backend.jar")) {
            if (in == null) {
                return null;
            }
            Path temp = Files.createTempFile("monex-backend-", ".jar");
            temp.toFile().deleteOnExit();
            Files.copy(in, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return temp;
        }
    }
}
