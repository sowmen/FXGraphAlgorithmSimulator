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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author sowme
 */
public class Panel1Controller implements Initializable {
    
    public static boolean directed = false, undirected = false, weighted = false, unweighted = false;
    
    @FXML public Button panel1Next, panel2Back, drawGraph, textInput;    
    @FXML private RadioButton dButton, udButton, wButton, uwButton;
    @FXML private AnchorPane panel1, panel2;
    
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        dButton.setSelected(directed);
        wButton.setSelected(weighted);
        udButton.setSelected(undirected);
        uwButton.setSelected(unweighted);
        
        panel2.setVisible(true);
        panel1.setVisible(false);
        
        // Thread for button control
        panel1Next.setDisable(true);
        Thread t = new Thread () {
            @Override
            public void run() {
                while(true) {
                    System.out.println(directed + " " + weighted);
                    if((directed==true || undirected==true) && (weighted==true || unweighted==true)) {
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
            directed = true; undirected = false;
            System.out.println("dButton");
        });
        udButton.setOnAction(e -> {
            directed = false; undirected = true;
            System.out.println("udButton");
        });
        wButton.setOnAction(e -> {
            weighted = true; unweighted = false;
            System.out.println("wButton");
        });
        uwButton.setOnAction(e -> {
            weighted = false; unweighted = true;
            System.out.println("uwButton");
        });
        panel1Next.setOnAction(e -> {
            Main.loader = new FXMLLoader(getClass().getResource("Canvas.fxml")); 
            try {
                Main.root = Main.loader.load();
            } catch (IOException ex) {
                
            }
            Main.scene = new Scene(Main.root);
            Main.primaryStage.setScene(Main.scene);
        });
        
    }    
    
}
