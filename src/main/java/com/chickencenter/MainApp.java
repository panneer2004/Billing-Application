package com.chickencenter;

import com.chickencenter.database.DatabaseInitializer;
import com.chickencenter.util.ToastManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        DatabaseInitializer.initialize();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chickencenter/ui/main.fxml"));
        Parent mainLayout = loader.load();
        
        StackPane globalRoot = new StackPane();
        globalRoot.getChildren().add(mainLayout);
        
        ToastManager.getInstance().init(globalRoot);
        
        Scene scene = new Scene(globalRoot, 1200, 750);
        
        primaryStage.setTitle("JK Chicken Center - Billing System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        com.chickencenter.database.DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
