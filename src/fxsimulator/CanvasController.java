package fxsimulator;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

public class CanvasController implements Initializable {

    @FXML private JFXButton canvasBackButton, clearButton, resetButton, bfsButton, dfsButton;
    @FXML private JFXToggleButton addNodeButton, addEdgeButton;
    @FXML private Pane viewer;
    @FXML private Group canvasGroup;
    @FXML private Line edgeLine;
    @FXML private Label sourceText = new Label("Source"), weight;
    @FXML private Pane border;
    @FXML private Arrow arrow;

    int nNode = 0;
    NodeFX selectedNode = null;
    List<NodeFX> circles = new ArrayList<NodeFX>();
    boolean addNode = true, addEdge = false, calculate = false,
            calculated = false;
    List<Label> distances = new ArrayList<Label>();
    private boolean weighted = false, unweighted = true,
                    directed = true, undirected = false;


    @FXML
    public void handle(MouseEvent ev) {
        if (addNode) {
            if (nNode == 2) {
                addEdgeButton.setDisable(false);
                AddNodeHandle(null);
            }

            if (!ev.getSource().equals(canvasGroup)) {
                if (ev.getEventType() == MouseEvent.MOUSE_RELEASED && ev.getButton() == MouseButton.PRIMARY) {
                    nNode++;
                    NodeFX circle = new NodeFX(ev.getX(), ev.getY(), 1, String.valueOf(nNode));
                    canvasGroup.getChildren().add(circle);

                    circle.setOnMousePressed(mouseHandler);
                    circle.setOnMouseReleased(mouseHandler);
                    circle.setOnMouseDragged(mouseHandler);
                    circle.setOnMouseExited(mouseHandler);
                    circle.setOnMouseEntered(mouseHandler);
                    
                    ScaleTransition tr = new ScaleTransition(Duration.millis(100), circle);
                    tr.setByX(10f);
                    tr.setByY(10f);
                    tr.setInterpolator(Interpolator.EASE_OUT);
                    tr.play();
                }
            }
        }
    }

    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            if(mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && mouseEvent.getButton() == MouseButton.PRIMARY) {
                NodeFX circle = (NodeFX) mouseEvent.getSource();
                
                if(!circle.isSelected){
                    if(selectedNode != null){
                        if(addEdge){
                            weight = new Label();
                            weight.setText("0");
                            //Adds the edge between two selected nodes
                            if(undirected){
                                edgeLine = new Line(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(edgeLine);
                            } else if(directed){
                                arrow = new Arrow(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(arrow);
                            }
                            //Adds weight between two selected nodes
                            if(weighted){
                                weight.setLayoutX(((selectedNode.point.x)+(circle.point.x))/2);
                                weight.setLayoutY(((selectedNode.point.y)+(circle.point.y))/2);
                                
                                TextInputDialog dialog = new TextInputDialog("0");
                                dialog.setTitle(null);
                                dialog.setHeaderText("Enter Weight of the Edge :");
                                dialog.setContentText(null);
                                
                                Optional<String> result = dialog.showAndWait();
                                if(result.isPresent())
                                    weight.setText(result.get());
                                else weight.setText("0");
                                canvasGroup.getChildren().add(weight);
                            }
                            
                            if(undirected)
                                selectedNode.node.adjacents.add(new Edge(circle.node, Integer.valueOf(weight.getText())));
                            else if(directed){
                                selectedNode.node.adjacents.add(new Edge(circle.node, Integer.valueOf(weight.getText())));
                                circle.node.adjacents.add(new Edge(selectedNode.node, Integer.valueOf(weight.getText())));
                            }
                        }
                        if(addNode || (calculate && !calculated) || addEdge) {
                            selectedNode.isSelected = false;
                            FillTransition ft1 = new FillTransition(Duration.millis(300),selectedNode, Color.RED, Color.BLACK);
                            ft1.play();
                        }
                    }
                    
                    FillTransition ft = new FillTransition(Duration.millis(300),circle, Color.BLACK, Color.RED);
                    ft.play();
                    circle.isSelected = true;
                    selectedNode = circle;
                    
                    /**
                     * ADD WHAT TO DO WHEN SELECTED ON ACTIVE ALGORITHM
                     */
                } else {
                    circle.isSelected = false;
                    FillTransition ft1 = new FillTransition(Duration.millis(300),circle, Color.RED, Color.BLACK);
                    ft1.play();
                    selectedNode = null;
                }
                
            }
        }

    };

    @FXML
    public void ResetHandle(ActionEvent event) {
        nNode = 0;
        canvasGroup.getChildren().clear();
        canvasGroup.getChildren().addAll(viewer);
        selectedNode = null;
        circles = new ArrayList<NodeFX>();
        distances = new ArrayList<Label>();
        addNode = true;
        addEdge = false;
        calculate = false;
        calculated = false;
        addNodeButton.setSelected(true);
        addEdgeButton.setSelected(false);
        addEdgeButton.setDisable(true);
        clearButton.setDisable(true);
    }

    @FXML
    public void ClearHandle(ActionEvent event) {
        selectedNode = null;
        calculated = false;
        for (NodeFX n : circles) {
            n.isSelected = false;
            n.node.previous = null;
            n.node.minDistance = Double.POSITIVE_INFINITY;

            FillTransition ft1 = new FillTransition(Duration.millis(300), n);
            ft1.setToValue(Color.BLACK);
            ft1.play();
        }
        for (Label x : distances) {
            x.setText("Distance : INFINITY");
            canvasGroup.getChildren().remove(x);
        }
        distances = new ArrayList<Label>();
    }

    @FXML
    public void AddEdgeHandle(ActionEvent event) {
        addNode = false;
        addEdge = true;
        calculate = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(true);
    }

    @FXML
    public void AddNodeHandle(ActionEvent event) {
        addNode = true;
        addEdge = false;
        calculate = false;
        addNodeButton.setSelected(true);
        addEdgeButton.setSelected(false);
        selectedNode = null;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewer.prefHeightProperty().bind(border.heightProperty());
        viewer.prefWidthProperty().bind(border.widthProperty());
        AddNodeHandle(null);
        addEdgeButton.setDisable(true);
        clearButton.setDisable(true);
    }

    public class NodeFX extends Circle {

        Node node;
        Point point;
        Label distance = new Label("Distance : INFINITY");
        boolean isSelected = false;

        public NodeFX(double x, double y, double rad, String name) {
            super(x, y, rad);
            node = new Node(name, this);
            point = new Point((int) x, (int) y);
            Label id = new Label(name);
            canvasGroup.getChildren().add(id);
            id.setLayoutX(x-18);
            id.setLayoutY(y-18);          
            this.setOpacity(0.5);
            this.setBlendMode(BlendMode.MULTIPLY);
            circles.add(this);
        }
    }
    
    
    
    /*
        Algorithm Declarations --------------------------------------------
    */
    //<editor-fold defaultstate="collapsed" desc="Algorithm Classes">
    
    public class Dijkstra {
        public boolean init = false;
        
        public void computePaths(Node source){
            
            //<editor-fold defaultstate="collapsed" desc="Animation Control">
            for(NodeFX n:circles){
                distances.add(n.distance);
                n.distance.setLayoutX(n.point.x+20);
                n.distance.setLayoutY(n.point.y);
                canvasGroup.getChildren().add(n.distance);
            }
            sourceText.setLayoutX(source.circle.point.x+20);
            sourceText.setLayoutY(source.circle.point.y+10);
            canvasGroup.getChildren().add(sourceText);
            SequentialTransition st = new SequentialTransition();
            source.circle.distance.setText("distance : "+0);
            //</editor-fold>
            
            source.minDistance = 0;
            PriorityQueue<Node> pq = new PriorityQueue<Node>();
            pq.add(source);
            while(!pq.isEmpty())
            {
                Node u = pq.poll();
                for(Edge e : u.adjacents){
                    if(e != null){
                        Node v = e.target;
                        
                        //<editor-fold defaultstate="collapsed" desc="Animation Control">
                        FillTransition ft = new FillTransition(Duration.millis(500), v.circle);
                        if(v.circle.getFill()==Color.BLACK)
                            ft.setToValue(Color.FORESTGREEN);
                        st.getChildren().add(ft);
                        //</editor-fold>
                        
                        if(u.minDistance + e.weight < v.minDistance)
                        {
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft1 = new FillTransition(Duration.millis(500), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.setOnFinished(ev ->{
                                    v.circle.distance.setText("distance : "+v.minDistance);
                            });
                            ft1.onFinishedProperty();
                            st.getChildren().add(ft1);
                            //</editor-fold>
                            pq.remove(v);
                            v.minDistance = u.minDistance + e.weight;
                            v.previous = u;
                            pq.add(v);
                        }
                    }
                }
            }
            
            //<editor-fold defaultstate="collapsed" desc="Animation Control">
            st.setOnFinished( ev ->{
                for(NodeFX n: circles){
                    FillTransition ft1 = new FillTransition(Duration.millis(500),n);
                    ft1.setToValue(Color.BLACK);
                    ft1.play();
                }
                FillTransition ft1 = new FillTransition(Duration.millis(500),source.circle);
                ft1.setToValue(Color.RED);
                ft1.play(); 
            });
            st.onFinishedProperty();
            st.play();
            //</editor-fold>
        }
        
        public List<Node> getShortestPathTo(Node target) {
            List<Node> path = new ArrayList<Node>();
            for(Node i = target; i!=null; i = i.previous)
                path.add(i);
            Collections.reverse(path);
            return path;
        }
    }
    
    
    
    //</editor-fold>
}
