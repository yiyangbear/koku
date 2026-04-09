package com.example.koku.app;

import com.example.koku.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    @Override
    public void start(Stage stage) {
        MainView root = new MainView();
        Scene scene = new Scene(root, 1480, 978);

        stage.setTitle("Koku / 观子");
        stage.setMinWidth(1480);
        stage.setMinHeight(978);
        stage.setMaxWidth(1480);
        stage.setMaxHeight(978);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
