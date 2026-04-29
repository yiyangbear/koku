package com.example.koku.app;

import com.example.koku.ui.GameSelectView;
import com.example.koku.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new GameSelectView(gameDefinition -> showGame(stage, gameDefinition)), 1480, 978);
        stage.setScene(scene);

        stage.setTitle("Koku / 观子");
        stage.setMinWidth(1480);
        stage.setMinHeight(978);
        stage.setMaxWidth(1480);
        stage.setMaxHeight(978);
        stage.setResizable(false);
        stage.show();
    }

    private void showSelect(Stage stage) {
        stage.getScene().setRoot(new GameSelectView(gameDefinition -> showGame(stage, gameDefinition)));
    }

    private void showGame(Stage stage, com.example.koku.game.GameDefinition gameDefinition) {
        stage.getScene().setRoot(new MainView(gameDefinition, () -> showSelect(stage)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
