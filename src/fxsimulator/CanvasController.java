package fxsimulator;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.Stack;
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

    @FXML private JFXButton canvasBackButton, clearButton, resetButton, bfsButton, dfsButton, dijkstraButton;
    @FXML private JFXToggleButton addNodeButton, addEdgeButton;
    @FXML private Pane viewer;
    @FXML private Group canvasGroup;
    @FXML private Line edgeLine;
    @FXML private Label sourceText = new Label("Source"), weight;
    @FXML private Pane border;
    @FXML private Arrow arrow;

    int nNode = 0, time = 500;
    NodeFX selectedNode = null;
    List<NodeFX> circles = new ArrayList<NodeFX>();
    boolean addNode = true, addEdge = false, calculate = false,
            calculated = false;
    List<Label> distances = new ArrayList<Label>();
    private boolean weighted = false, unweighted = true,
                    directed = true, undirected = false,
                    bfs = true, dfs =true, dijkstra =true;
    Algorithm algo = new Algorithm();

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
                            } else if(unweighted){
                                weight.setText("1");
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
                        selectedNode = null;
                        return;
                    }
                    
                    FillTransition ft = new FillTransition(Duration.millis(300),circle, Color.BLACK, Color.RED);
                    ft.play();
                    circle.isSelected = true;
                    selectedNode = circle;
                    
                    /**
                     * WHAT TO DO WHEN SELECTED ON ACTIVE ALGORITHM
                     */
                    
                    if(calculate && ! calculated){
                        if(bfs){
                            algo.BFS(circle.node);
                        } else if(dfs){
                            algo.DFS(circle.node);
                        } else if(dijkstra){
                            algo.Dijkstra(circle.node);
                        }
                        
                        calculated = true;
                    } else if(calculate && calculated) {
                        
                        for(NodeFX n : circles){
                            n.isSelected = false;
                            FillTransition ft1 = new FillTransition(Duration.millis(300),n);
                            ft1.setToValue(Color.BLACK);
                            ft1.play();
                        }
                        List<Node> path = algo.getShortestPathTo(circle.node);
                        for(Node n : path){
                            FillTransition ft1 = new FillTransition(Duration.millis(300),n.circle);
                            ft1.setToValue(Color.BLUE);
                            ft1.play();
                        }
                    }
                    
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
        addNodeButton.setDisable(false);
        clearButton.setDisable(true);
        algo = new Algorithm();
        
        bfsButton.setDisable(true);
        dfsButton.setDisable(true);
        dijkstraButton.setDisable(true);
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
        };
        canvasGroup.getChildren().remove(sourceText);
        for (Label x : distances) {
            x.setText("Distance : INFINITY");
            canvasGroup.getChildren().remove(x);
        }
        distances = new ArrayList<Label>();
        addNodeButton.setDisable(false);
        AddNodeHandle(null);
    }

    @FXML
    public void AddEdgeHandle(ActionEvent event) {
        addNode = false;
        addEdge = true;
        calculate = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(true);
        
        if(unweighted){
            bfsButton.setDisable(false);
            dfsButton.setDisable(false);
        }
        if(weighted)
            dijkstraButton.setDisable(false);
    }

    @FXML
    public void AddNodeHandle(ActionEvent event) {
        addNode = true;
        addEdge = false;
        calculate = false;
        addNodeButton.setSelected(true);
        addEdgeButton.setSelected(false);
        selectedNode = null;
        
        if(unweighted){
            bfsButton.setDisable(false);
            dfsButton.setDisable(false);
        }
        if(weighted)
            dijkstraButton.setDisable(false);
    }
    
    @FXML public void BFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);;
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        bfs = true;
        dfs = false; dijkstra = false;
    }
    @FXML public void DFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);;
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        dfs = true;
        bfs = false; dijkstra = false;
    }
    @FXML public void DijkstraHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);;
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        bfs = false;
        dfs = false; dijkstra = true;
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        viewer.prefHeightProperty().bind(border.heightProperty());
        viewer.prefWidthProperty().bind(border.widthProperty());
        AddNodeHandle(null);
        addEdgeButton.setDisable(true);
        clearButton.setDisable(true);

        if(weighted){
            bfsButton.setDisable(true); 
            dfsButton.setDisable(true); 
        }
        if(unweighted){
            dijkstraButton.setDisable(true);
        }
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
    
    public class Algorithm {
        public boolean init = false;
        
        //<editor-fold defaultstate="collapsed" desc="Dijkstra">
        public void Dijkstra(Node source){
            
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
                        FillTransition ft = new FillTransition(Duration.millis(time), v.circle);
                        if(v.circle.getFill()==Color.BLACK)
                            ft.setToValue(Color.FORESTGREEN);
                        st.getChildren().add(ft);
                        //</editor-fold>
                        
                        if(u.minDistance + e.weight < v.minDistance)
                        {
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
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
                    FillTransition ft1 = new FillTransition(Duration.millis(time),n);
                    ft1.setToValue(Color.BLACK);
                    ft1.play();
                }
                FillTransition ft1 = new FillTransition(Duration.millis(time),source.circle);
                ft1.setToValue(Color.RED);
                ft1.play(); 
            });
            st.onFinishedProperty();
            st.play();
            //</editor-fold>
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="BFS">
        public void BFS(Node source) {
            
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
            source.visited = true;
            LinkedList<Node> q = new LinkedList<Node>();
            q.push(source);
            while(!q.isEmpty())
            {
                Node u = q.poll();
                System.out.println(u.name);
                for(Edge e : u.adjacents){
                    if(e != null){
                        Node v = e.target;
                        if(!v.visited){
                            
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft = new FillTransition(Duration.millis(time), v.circle);
                            if(v.circle.getFill()==Color.BLACK)
                                ft.setToValue(Color.FORESTGREEN);
                            st.getChildren().add(ft);
                            //</editor-fold>
                            
                            v.minDistance = u.minDistance + 1;
                            v.visited = true;
                            q.push(v);
                            v.previous = u;
                            
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.setOnFinished(ev ->{
                                    v.circle.distance.setText("distance : " +v.minDistance);
                            });
                            ft1.onFinishedProperty();
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        }
                    }
                }
            }
            
            //<editor-fold defaultstate="collapsed" desc="Animation Control">
            st.setOnFinished( ev ->{
                for(NodeFX n: circles){
                    FillTransition ft1 = new FillTransition(Duration.millis(time),n);
                    ft1.setToValue(Color.BLACK);
                    ft1.play();
                }
                FillTransition ft1 = new FillTransition(Duration.millis(time),source.circle);
                ft1.setToValue(Color.RED);
                ft1.play(); 
            });
            st.onFinishedProperty();
            st.play();
            //</editor-fold>
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="DFS">
        public void DFS(Node source) {
             
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
            source.visited = true;
            Stack<Node> q = new Stack<Node>();
            q.push(source);
            while(!q.isEmpty())
            {
                Node u = q.peek();
                q.pop();
                System.out.println(u.name);
                for(Edge e : u.adjacents){
                    if(e != null){
                        Node v = e.target;
                        if(!v.visited){
                            
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft = new FillTransition(Duration.millis(time), v.circle);
                            if(v.circle.getFill()==Color.BLACK)
                                ft.setToValue(Color.FORESTGREEN);
                            st.getChildren().add(ft);
                            //</editor-fold>
                            
                            v.minDistance = u.minDistance + 1;
                            v.visited = true;
                            q.push(v);
                            v.previous = u;
                            
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.setOnFinished(ev ->{
                                    v.circle.distance.setText("distance : " +v.minDistance);
                            });
                            ft1.onFinishedProperty();
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        }
                    }
                }
            }
            
            //<editor-fold defaultstate="collapsed" desc="Animation Control">
            st.setOnFinished( ev ->{
                for(NodeFX n: circles){
                    FillTransition ft1 = new FillTransition(Duration.millis(time),n);
                    ft1.setToValue(Color.BLACK);
                    ft1.play();
                }
                FillTransition ft1 = new FillTransition(Duration.millis(time),source.circle);
                ft1.setToValue(Color.RED);
                ft1.play(); 
            });
            st.onFinishedProperty();
            st.play();
            //</editor-fold>
        }
        //</editor-fold>
        public List<Node> getShortestPathTo(Node target) {
            List<Node> path = new ArrayList<Node>();
            for(Node i = target; i!=null; i = i.previous)
                path.add(i);
            Collections.reverse(path);
            return path;
        }
    }
    
}
