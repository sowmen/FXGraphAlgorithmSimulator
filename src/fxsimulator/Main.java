/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author sowme
 */
public class Main extends Application {
    public static Stage primaryStage;
    public static Scene scene;
    public static Parent root;
    public static FXMLLoader loader;
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        loader = new FXMLLoader(getClass().getResource("Panel1FXML.fxml"));
        root = loader.load();
        
        scene = new Scene(root);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
