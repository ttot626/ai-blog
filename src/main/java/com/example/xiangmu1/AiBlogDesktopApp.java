package com.example.xiangmu1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 桌面版：独立窗口，不打开系统浏览器。
 */
public class AiBlogDesktopApp extends Application {

    private ConfigurableApplicationContext springContext;
    private Label loadingLabel;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(Xiangmu1Application.class)
                .properties("app.auto-open-browser=false")
                .run();
    }

    @Override
    public void start(Stage stage) {
        loadingLabel = new Label("AI Blog 正在启动，请稍候…");
        loadingLabel.setStyle("-fx-font-size: 16px;");

        VBox box = new VBox(loadingLabel);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #f4f6fb;");

        Scene scene = new Scene(box, 1100, 750);
        stage.setTitle("AI Blog");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();

        waitForServer(scene);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    private void waitForServer(Scene scene) {
        Thread waiter = new Thread(() -> {
            for (int i = 0; i < 120; i++) {
                if (isServerUp()) {
                    Platform.runLater(() -> {
                        WebView webView = new WebView();
                        webView.getEngine().load("http://localhost:8080");
                        scene.setRoot(webView);
                    });
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            Platform.runLater(() ->
                    loadingLabel.setText("启动超时，请检查 MySQL 是否已启动并已执行 init.sql"));
        }, "server-waiter");
        waiter.setDaemon(true);
        waiter.start();
    }

    private boolean isServerUp() {
        try {
            var conn = java.net.URI.create("http://localhost:8080/hello").toURL().openConnection();
            conn.setConnectTimeout(800);
            conn.setReadTimeout(800);
            conn.getInputStream().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
