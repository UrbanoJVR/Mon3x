package com.urbanojvr.monex.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonexDesktopApp extends Application {

    private BackendProcess backendProcess;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "monex-backend-bootstrap");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void start(Stage stage) {
        ProgressIndicator spinner = new ProgressIndicator();
        Label status = new Label("Arrancando Monex…");
        VBox loading = new VBox(16, spinner, status);
        loading.setStyle("-fx-alignment: center; -fx-padding: 24;");

        StackPane root = new StackPane(loading);
        Scene scene = new Scene(root, 1280, 800);
        stage.setTitle("Monex");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> shutdown());
        stage.show();

        CompletableFuture
                .supplyAsync(this::startBackend, executor)
                .thenAccept(url -> Platform.runLater(() -> showUi(root, url, status)))
                .exceptionally(error -> {
                    Platform.runLater(() -> status.setText("Error al arrancar el backend: " + rootCause(error).getMessage()));
                    return null;
                });
    }

    private String startBackend() {
        try {
            int port = PortFinder.findFreePort();
            backendProcess = new BackendProcess(port);
            backendProcess.start();
            HealthWaiter.waitUntilReady(port);
            return "http://127.0.0.1:" + port + "/";
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void showUi(StackPane root, String url, Label status) {
        WebView webView = new WebView();
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                status.setText("No se pudo cargar la interfaz en " + url);
            }
        });
        webView.getEngine().load(url);
        root.getChildren().setAll(webView);
    }

    private void shutdown() {
        if (backendProcess != null) {
            backendProcess.stop();
        }
        executor.shutdownNow();
        Platform.exit();
    }

    @Override
    public void stop() {
        shutdown();
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
