/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author sowme
 */
public class Panel1Controller implements Initializable {

    public static boolean directed = false, undirected = false, weighted = false, unweighted = false;

    @FXML
    public Button panel1Next, panel2Back;
    @FXML
    private RadioButton dButton, udButton, wButton, uwButton;
    @FXML
    private AnchorPane panel1;

    static CanvasController cref;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        dButton.setSelected(directed);
        wButton.setSelected(weighted);
        udButton.setSelected(undirected);
        uwButton.setSelected(unweighted);

        // Thread for button control
        panel1Next.setDisable(true);
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.out.println(directed + " " + weighted);
                    if ((directed == true || undirected == true) && (weighted == true || unweighted == true)) {
                        System.out.println("In thread " + directed);
                        panel1Next.setDisable(false);
                        panel1Next.setStyle("-fx-background-color : #487eb0;");
                        break;
                    }
                }
                System.out.println("Exiting thread");
            }
        };
        t.start();

        // Button Action listeners
        dButton.setOnAction(e -> {
            directed = true;
            undirected = false;
            System.out.println("dButton");
        });
        udButton.setOnAction(e -> {
            directed = false;
            undirected = true;
            System.out.println("udButton");
        });
        wButton.setOnAction(e -> {
            weighted = true;
            unweighted = false;
            System.out.println("wButton");
        });
        uwButton.setOnAction(e -> {
            weighted = false;
            unweighted = true;
            System.out.println("uwButton");
        });
        panel1Next.setOnAction(e -> {
            FadeOut();
        });

    }

    void FadeOut() {
        FadeTransition ft = new FadeTransition();
        ft.setDuration(Duration.millis(1000));
        ft.setNode(panel1);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            loadNextScene();
        });
        ft.play();
        System.out.println("Here");
    }

    void loadNextScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Canvas.fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root);
            cref = loader.getController();

            System.out.println("Controller ref: " + cref);
            newScene.getStylesheets().add(getClass().getResource("Styling.css").toExternalForm());
            FXSimulatorMain.primaryStage.setScene(newScene);
        } catch (IOException ex) {
            Logger.getLogger(Panel1Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
