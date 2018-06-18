
package fxsimulator;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import jfxtras.labs.scene.layout.ScalableContentPane;

public class CanvasController implements Initializable {

    @FXML private Button canvasBackButton, clearButton, resetButton, bfsButton, dfsButton;
    @FXML ScalableContentPane canvas;
    
    public static ArrayList<Node> graph = new ArrayList<>();
    private int counter = 0;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        canvas.setOnMousePressed((MouseEvent e) -> {
            e.consume();
            System.out.println(e.getX() + " " + e.getY());
            Node temp = new Node(Integer.toString(counter), e.getX(), e.getY());
            counter++;
            graph.add(temp);
            canvas.getChildren().add(temp.getNode());
        });
//        canvas.setOnMousePressed(e -> {graph.remove(graph.size()-1);});
    }    
    
}
