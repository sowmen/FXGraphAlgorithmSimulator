package fxsimulator;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleButton;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.StrokeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.controlsfx.control.HiddenSidesPane;

public class CanvasController implements Initializable, ChangeListener {

    @FXML
    private HiddenSidesPane hiddenPane;
    @FXML
    private AnchorPane anchorRoot;
    @FXML
    private JFXButton canvasBackButton, clearButton, resetButton, playPauseButton;
    @FXML
    private JFXToggleButton addNodeButton, addEdgeButton, bfsButton, dfsButton, topSortButton, dijkstraButton,
            articulationPointButton, mstButton;
    @FXML
    private ToggleGroup algoToggleGroup;
    @FXML
    private Pane viewer;
    @FXML
    private Group canvasGroup;
    @FXML
    private Line edgeLine;
    @FXML
    private Label sourceText = new Label("Source"), weight;
    @FXML
    private Pane border;
    @FXML
    private Arrow arrow;
    @FXML
    private JFXNodesList nodeList;
    @FXML
    private JFXSlider slider = new JFXSlider();
    @FXML
    private ImageView playPauseImage, openHidden;

    boolean menuBool = false;
    ContextMenu globalMenu;

    int nNode = 0, time = 500;
    NodeFX selectedNode = null;
    List<NodeFX> circles = new ArrayList<>();
    List<Edge> mstEdges = new ArrayList<>(), realEdges = new ArrayList<>();
    List<Shape> edges = new ArrayList<>();
    boolean addNode = true, addEdge = false, calculate = false,
            calculated = false, playing = false, paused = false, pinned = false;
    List<Label> distances = new ArrayList<Label>(), visitTime = new ArrayList<>(), lowTime = new ArrayList<Label>();
    private boolean weighted = Panel1Controller.weighted, unweighted = Panel1Controller.unweighted,
            directed = Panel1Controller.directed, undirected = Panel1Controller.undirected,
            bfs = true, dfs = true, dijkstra = true, articulationPoint = true, mst = true, topSortBool = true;
    Algorithm algo = new Algorithm();

    public SequentialTransition st;

    public AnchorPane hiddenRoot = new AnchorPane();

    public static TextArea textFlow = new TextArea();
    public ScrollPane textContainer = new ScrollPane();

    public String topSort = "";
    public int x = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("In intit");
        hiddenPane.setContent(canvasGroup);

        ResetHandle(null);
        viewer.prefHeightProperty().bind(border.heightProperty());
        viewer.prefWidthProperty().bind(border.widthProperty());
//        AddNodeHandle(null);
        addEdgeButton.setDisable(true);
        addNodeButton.setDisable(true);
        clearButton.setDisable(true);

        if (weighted) {
            bfsButton.setDisable(true);
            dfsButton.setDisable(true);
            articulationPointButton.setDisable(true);
        }
        if (unweighted) {
            dijkstraButton.setDisable(true);
        }
        if (directed) {
            articulationPointButton.setDisable(true);
        }

        //Set back button action
        canvasBackButton.setOnAction(e -> {
            try {
                ResetHandle(null);
                Parent root = FXMLLoader.load(getClass().getResource("Panel1FXML.fxml"));

                Scene scene = new Scene(root);
                FXSimulator.primaryStage.setScene(scene);
            } catch (IOException ex) {
                Logger.getLogger(CanvasController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        //Setup Slider
        slider = new JFXSlider(10, 1000, 500);
        slider.setPrefWidth(150);
        slider.setPrefHeight(80);
        slider.setSnapToTicks(true);
        slider.setMinorTickCount(100);
        slider.setIndicatorPosition(JFXSlider.IndicatorPosition.RIGHT);
        slider.setBlendMode(BlendMode.MULTIPLY);
        slider.setCursor(Cursor.CLOSED_HAND);
        nodeList.addAnimatedNode(slider);
        nodeList.setSpacing(50D);
        nodeList.setRotate(270D);
        slider.toFront();
        nodeList.toFront();
        slider.valueProperty().addListener(this);

        hiddenRoot.setPrefWidth(220);
        hiddenRoot.setPrefHeight(581);

        hiddenRoot.setCursor(Cursor.DEFAULT);

        //Set Label "Detail"
        Label detailLabel = new Label("Detail");
        detailLabel.setPrefSize(hiddenRoot.getPrefWidth() - 20, 38);
        detailLabel.setAlignment(Pos.CENTER);
        detailLabel.setFont(new Font("Roboto", 20));
        detailLabel.setPadding(new Insets(7, 40, 3, -10));
        detailLabel.setStyle("-fx-background-color: #dcdde1;");
        detailLabel.setLayoutX(35);

        //Set TextFlow pane properties
        textFlow.setPrefSize(hiddenRoot.getPrefWidth(), hiddenRoot.getPrefHeight() - 2);
//        textFlow.prefHeightProperty().bind(hiddenRoot.heightProperty());
        textFlow.setStyle("-fx-background-color: #dfe6e9;");
        textFlow.setLayoutY(39);
        textContainer.setLayoutY(textFlow.getLayoutY());
        textFlow.setPadding(new Insets(5, 0, 0, 5));
        textFlow.setEditable(false);
        textContainer.setContent(textFlow);

        //Set Pin/Unpin Button
        JFXButton pinUnpin = new JFXButton();
        pinUnpin.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        ImageView imgPin = new ImageView(new Image(getClass().getResourceAsStream("/pinned.png")));
        imgPin.setFitHeight(20);
        imgPin.setFitWidth(20);
        ImageView imgUnpin = new ImageView(new Image(getClass().getResourceAsStream("/unpinned.png")));
        imgUnpin.setFitHeight(20);
        imgUnpin.setFitWidth(20);
        pinUnpin.setGraphic(imgPin);

        pinUnpin.setPrefSize(20, 39);
        pinUnpin.setButtonType(JFXButton.ButtonType.FLAT);
        pinUnpin.setStyle("-fx-background-color: #dcdde1;");
        pinUnpin.setOnMouseClicked(e -> {
            if (pinned) {
                pinUnpin.setGraphic(imgPin);
                hiddenPane.setPinnedSide(null);
                pinned = false;
            } else {
                pinUnpin.setGraphic(imgUnpin);
                hiddenPane.setPinnedSide(Side.RIGHT);
                pinned = true;
            }
        });

        //Add Label and TextFlow to hiddenPane
        hiddenRoot.getChildren().addAll(pinUnpin, detailLabel, textContainer);
        hiddenPane.setRight(hiddenRoot);
        hiddenRoot.setOnMouseEntered(e -> {
            hiddenPane.setPinnedSide(Side.RIGHT);
            openHidden.setVisible(false);
            e.consume();
        });
        hiddenRoot.setOnMouseExited(e -> {
            if (!pinned) {
                hiddenPane.setPinnedSide(null);
                openHidden.setVisible(true);
            }
            e.consume();
        });
        hiddenPane.setTriggerDistance(60);

    }

    /**
     * Change Listener for change in speed slider values.
     *
     * @param observable
     * @param oldValue
     * @param newValue
     */
    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        int temp = (int) slider.getValue();

        if (temp > 500) {
            int diff = temp - 500;
            temp = 500;
            temp -= diff;
            temp += 10;
        } else if (temp < 500) {
            int diff = 500 - temp;
            temp = 500;
            temp += diff;
            temp -= 10;
        }
        time = temp;
        System.out.println(time);
    }

    /**
     * Handles events for mouse clicks on the canvas. Adds a new node on the
     * drawing canvas where mouse is clicked.
     *
     * @param ev
     */
    @FXML
    public void handle(MouseEvent ev) {
        if (addNode) {
            if (nNode == 1) {
                addNodeButton.setDisable(false);
            }
            if (nNode == 2) {
                addEdgeButton.setDisable(false);
                AddNodeHandle(null);
            }

            if (!ev.getSource().equals(canvasGroup)) {
                if (ev.getEventType() == MouseEvent.MOUSE_RELEASED && ev.getButton() == MouseButton.PRIMARY) {
                    if (menuBool == true) {
                        System.out.println("here" + ev.getEventType());
                        menuBool = false;
                        return;
                    }
                    nNode++;
                    NodeFX circle = new NodeFX(ev.getX(), ev.getY(), 1.2, String.valueOf(nNode));
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

    /**
     * Checks if an edge already exists between two nodes before adding a new
     * edge.
     *
     * @param u = selected node
     * @param v = second selected node
     * @return True if edge already exists. Else false.
     */
    boolean edgeExists(NodeFX u, NodeFX v) {
        for (Edge e : realEdges) {
            if (e.source == u.node && e.target == v.node) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an edge between two selected nodes. Handles events for mouse clicks
     * on a node.
     */
    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            NodeFX circle = (NodeFX) mouseEvent.getSource();
            if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED && mouseEvent.getButton() == MouseButton.PRIMARY) {

                if (!circle.isSelected) {
                    if (selectedNode != null) {
                        if (addEdge && !edgeExists(selectedNode, circle)) {
                            weight = new Label();
                            System.out.println("Adding Edge");
                            //Adds the edge between two selected nodes
                            if (undirected) {
                                edgeLine = new Line(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(edgeLine);
                                edgeLine.setId("line");
                            } else if (directed) {
                                arrow = new Arrow(selectedNode.point.x, selectedNode.point.y, circle.point.x, circle.point.y);
                                canvasGroup.getChildren().add(arrow);
                                arrow.setId("arrow");
                            }

                            //Adds weight between two selected nodes
                            if (weighted) {
                                weight.setLayoutX(((selectedNode.point.x) + (circle.point.x)) / 2);
                                weight.setLayoutY(((selectedNode.point.y) + (circle.point.y)) / 2);

                                TextInputDialog dialog = new TextInputDialog("0");
                                dialog.setTitle(null);
                                dialog.setHeaderText("Enter Weight of the Edge :");
                                dialog.setContentText(null);

                                Optional<String> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    weight.setText(result.get());
                                } else {
                                    weight.setText("0");
                                }
                                canvasGroup.getChildren().add(weight);
                            } else if (unweighted) {
                                weight.setText("1");
                            }
                            Shape line_arrow = null;
                            Edge temp = null;
                            if (undirected) {
                                temp = new Edge(selectedNode.node, circle.node, Integer.valueOf(weight.getText()), edgeLine, weight);
                                if (weighted) {
                                    mstEdges.add(temp);
                                }

                                selectedNode.node.adjacents.add(new Edge(selectedNode.node, circle.node, Double.valueOf(weight.getText()), edgeLine, weight));
                                circle.node.adjacents.add(new Edge(circle.node, selectedNode.node, Double.valueOf(weight.getText()), edgeLine, weight));
                                edges.add(edgeLine);
                                realEdges.add(selectedNode.node.adjacents.get(selectedNode.node.adjacents.size() - 1));
                                realEdges.add(circle.node.adjacents.get(circle.node.adjacents.size() - 1));
                                line_arrow = edgeLine;

                            } else if (directed) {
                                temp = new Edge(selectedNode.node, circle.node, Double.valueOf(weight.getText()), arrow, weight);
                                selectedNode.node.adjacents.add(temp);
//                                circle.node.revAdjacents.add(new Edge(circle.node, selectedNode.node, Integer.valueOf(weight.getText()), arrow));
                                edges.add(arrow);
                                line_arrow = arrow;
                                realEdges.add(temp);
                            }

                            RightClickMenu rt = new RightClickMenu(temp);
                            ContextMenu menu = rt.getMenu();
                            if (weighted) {
                                rt.changeId.setText("Change Weight");
                            } else if (unweighted) {
                                rt.changeId.setDisable(true);
                            }
                            final Shape la = line_arrow;
                            line_arrow.setOnContextMenuRequested(e -> {
                                System.out.println("In Edge Menu :" + menuBool);

                                if (menuBool == true) {
                                    globalMenu.hide();
                                    menuBool = false;
                                }
                                if (addEdge || addNode) {
                                    globalMenu = menu;
                                    menu.show(la, e.getScreenX(), e.getScreenY());
                                    menuBool = true;
                                }
                            });
                            menu.setOnAction(e -> {
                                menuBool = false;
                            });
                        }
                        if (addNode || (calculate && !calculated) || addEdge) {
                            selectedNode.isSelected = false;
                            FillTransition ft1 = new FillTransition(Duration.millis(300), selectedNode, Color.RED, Color.BLACK);
                            ft1.play();
                        }
                        selectedNode = null;
                        return;
                    }

                    FillTransition ft = new FillTransition(Duration.millis(300), circle, Color.BLACK, Color.RED);
                    ft.play();
                    circle.isSelected = true;
                    selectedNode = circle;

                    // WHAT TO DO WHEN SELECTED ON ACTIVE ALGORITHM
                    if (calculate && !calculated) {
                        if (bfs) {
                            algo.newBFS(circle.node);
                        } else if (dfs) {
                            algo.newDFS(circle.node);
                        } else if (dijkstra) {
                            algo.newDijkstra(circle.node);
                        }

                        calculated = true;
                    } else if (calculate && calculated && !articulationPoint & !mst) {

                        for (NodeFX n : circles) {
                            n.isSelected = false;
                            FillTransition ft1 = new FillTransition(Duration.millis(300), n);
                            ft1.setToValue(Color.BLACK);
                            ft1.play();
                        }
                        List<Node> path = algo.getShortestPathTo(circle.node);
                        for (Node n : path) {
                            FillTransition ft1 = new FillTransition(Duration.millis(300), n.circle);
                            ft1.setToValue(Color.BLUE);
                            ft1.play();
                        }
                    }
                } else {
                    circle.isSelected = false;
                    FillTransition ft1 = new FillTransition(Duration.millis(300), circle, Color.RED, Color.BLACK);
                    ft1.play();
                    selectedNode = null;
                }

            }
        }

    };

    /**
     * Get a random node to start Articulation Point.
     *
     * @return A node from the current node list.
     */
    private Node getRandomStart() {
        return circles.get(0).node;
    }

    /**
     * Event handler for the Play/Pause button.
     *
     * @param event
     */
    @FXML
    public void PlayPauseHandle(ActionEvent event) {
        System.out.println("IN PLAYPAUSE");
        System.out.println(playing + " " + paused);
        if (playing) {
            Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
            playPauseImage.setImage(image);
            System.out.println("Pausing");
            st.pause();
            paused = true;
            playing = false;
            return;
        } else if (paused) {
            Image image = new Image(getClass().getResourceAsStream("/pause_black_48x48.png"));
            playPauseImage.setImage(image);
            st.play();
            playing = true;
            paused = false;
            return;
        }
    }

    /**
     * Event handler for the Reset button. Clears all the lists and empties the
     * canvas.
     *
     * @param event
     */
    @FXML
    public void ResetHandle(ActionEvent event) {
        ClearHandle(null);
        nNode = 0;
        canvasGroup.getChildren().clear();
        canvasGroup.getChildren().addAll(viewer);
        selectedNode = null;
        circles = new ArrayList<NodeFX>();
        distances = new ArrayList<Label>();
        visitTime = new ArrayList<Label>();
        lowTime = new ArrayList<Label>();
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
        Image image = new Image(getClass().getResourceAsStream("/pause_black_48x48.png"));
        playPauseImage.setImage(image);
        hiddenPane.setPinnedSide(null);

        bfsButton.setDisable(true);
        topSortButton.setDisable(true);
        dfsButton.setDisable(true);
        dijkstraButton.setDisable(true);
        articulationPointButton.setDisable(true);
        mstButton.setDisable(true);
        playing = false;
        paused = false;
    }

    /**
     * Event handler for the Clear button. Re-initiates the distance and node
     * values and labels.
     *
     * @param event
     */
    @FXML
    public void ClearHandle(ActionEvent event) {
        menuBool = false;
        selectedNode = null;
        calculated = false;
        topSort = "";
        System.out.println("IN CLEAR:" + circles.size());
        for (NodeFX n : circles) {
            n.isSelected = false;
            n.node.visited = false;
            n.node.previous = null;
            n.node.minDistance = Double.POSITIVE_INFINITY;

            FillTransition ft1 = new FillTransition(Duration.millis(300), n);
            ft1.setToValue(Color.BLACK);
            ft1.play();
        }
        for (Shape x : edges) {
            if (undirected) {
                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), x);
                ftEdge.setToValue(Color.BLACK);
                ftEdge.play();
            } else if (directed) {
                FillTransition ftEdge = new FillTransition(Duration.millis(time), x);
                ftEdge.setToValue(Color.BLACK);
                ftEdge.play();
            }
        }
        canvasGroup.getChildren().remove(sourceText);
        for (Label x : distances) {
            x.setText("Distance : INFINITY");
            canvasGroup.getChildren().remove(x);
        }
        for (Label x : visitTime) {
            x.setText("Visit : 0");
            canvasGroup.getChildren().remove(x);
        }
        for (Label x : lowTime) {
            x.setText("Low Value : NULL");
            canvasGroup.getChildren().remove(x);
        }
        textFlow.clear();

        Image image = new Image(getClass().getResourceAsStream("/pause_black_48x48.png"));
        playPauseImage.setImage(image);

        distances = new ArrayList<Label>();
        visitTime = new ArrayList<Label>();
        lowTime = new ArrayList<Label>();
        addNodeButton.setDisable(false);
        addEdgeButton.setDisable(false);
        AddNodeHandle(null);
        bfs = false;
        dfs = false;
        articulationPoint = false;
        dijkstra = false;
        mst = false;
        topSortBool = false;
        playing = false;
        paused = false;
    }

    /**
     * Event handler for the Add Edge button.
     *
     * @param event
     */
    @FXML
    public void AddEdgeHandle(ActionEvent event) {
        addNode = false;
        addEdge = true;
        calculate = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(true);

        if (unweighted) {
            bfsButton.setDisable(false);
            bfsButton.setSelected(false);
            dfsButton.setDisable(false);
            dfsButton.setSelected(false);
            topSortButton.setDisable(false);
            topSortButton.setSelected(false);
            if (undirected) {
                articulationPointButton.setDisable(false);
                articulationPointButton.setSelected(false);
            }
        }
        if (weighted) {
            dijkstraButton.setDisable(false);
            dijkstraButton.setSelected(false);
            if (undirected) {
                mstButton.setDisable(false);
                mstButton.setSelected(false);
            }
        }
    }

    @FXML
    public void AddNodeHandle(ActionEvent event) {
        addNode = true;
        addEdge = false;
        calculate = false;
        addNodeButton.setSelected(true);
        addEdgeButton.setSelected(false);
        selectedNode = null;

        if (unweighted) {
            bfsButton.setDisable(false);
            bfsButton.setSelected(false);
            dfsButton.setDisable(false);
            dfsButton.setSelected(false);
            topSortButton.setDisable(false);
            topSortButton.setSelected(false);
            if (undirected) {
                articulationPointButton.setDisable(false);
                articulationPointButton.setSelected(false);
            }
        }
        if (weighted) {
            dijkstraButton.setDisable(false);
            dijkstraButton.setSelected(false);
            if (undirected) {
                mstButton.setDisable(false);
                mstButton.setSelected(false);
            }
        }
    }

    @FXML
    public void BFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        bfs = true;
        dfs = false;
        dijkstra = false;
        mst = false;
        articulationPoint = false;
    }

    @FXML
    public void DFSHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        dfs = true;
        bfs = false;
        dijkstra = false;
        mst = false;
        articulationPoint = false;
    }

    @FXML
    public void TopSortHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        dfs = false;
        bfs = false;
        dijkstra = false;
        mst = false;
        articulationPoint = false;
        topSortBool = true;
    }

    @FXML
    public void ArticulationPointHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);;
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        dfs = false;
        bfs = false;
        dijkstra = false;
        articulationPoint = true;
        mst = false;
        algo.newArticulationPoint(getRandomStart());
    }

    @FXML
    public void DijkstraHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        bfs = false;
        dfs = false;
        dijkstra = true;
        mst = false;
        articulationPoint = false;
    }

    @FXML
    public void MSTHandle(ActionEvent event) {
        addNode = false;
        addEdge = false;
        addNodeButton.setSelected(false);
        addEdgeButton.setSelected(false);
        addNodeButton.setDisable(true);
        addEdgeButton.setDisable(true);
        calculate = true;
        clearButton.setDisable(false);
        bfs = false;
        dfs = false;
        dijkstra = false;
        articulationPoint = false;
        mst = true;
        algo.newMST();
    }

    /**
     * Changes the current Name/ID of a node.
     *
     * @param source Node reference of selected node
     */
    public void changeID(NodeFX source) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
        selectedNode = null;

        TextInputDialog dialog = new TextInputDialog(Integer.toString(nNode));
        dialog.setTitle(null);
        dialog.setHeaderText("Enter Node ID :");
        dialog.setContentText(null);

        String res = null;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            res = result.get();
        }

        circles.get(circles.indexOf(source)).id.setText(res);
        circles.get(circles.indexOf(source)).node.name = res;

        System.out.println("AFTER----------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }
    }

    /**
     * Deletes the currently selected node.
     *
     * @param sourceFX
     */
    public void deleteNode(NodeFX sourceFX) {
        selectedNode = null;
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }

        Node source = sourceFX.node;
        circles.remove(sourceFX);

        List<Edge> tempEdges = new ArrayList<>();
        List<Node> tempNodes = new ArrayList<>();
        for (Edge e : source.adjacents) {
            Node u = e.target;
            for (Edge x : u.adjacents) {
                if (x.target == source) {
                    x.target = null;
                    tempNodes.add(u);
                    tempEdges.add(x);
                }
            }
            edges.remove(e.getLine());
            canvasGroup.getChildren().remove(e.getLine());
            mstEdges.remove(e);
        }
        for (Node q : tempNodes) {
            q.adjacents.removeAll(tempEdges);
        }
        List<Edge> tempEdges2 = new ArrayList<>();
        List<Shape> tempArrows = new ArrayList<>();
        List<Node> tempNodes2 = new ArrayList<>();
        for (NodeFX z : circles) {
            for (Edge s : z.node.adjacents) {
                if (s.target == source) {
                    tempEdges2.add(s);
                    tempArrows.add(s.line);
                    tempNodes2.add(z.node);
                    canvasGroup.getChildren().remove(s.line);
                }
            }
        }
        for (Node z : tempNodes2) {
            z.adjacents.removeAll(tempEdges2);
        }
        realEdges.removeAll(tempEdges);
        realEdges.removeAll(tempEdges2);
        canvasGroup.getChildren().remove(sourceFX.id);
        canvasGroup.getChildren().remove(sourceFX);

        System.out.println("AFTER----------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }

    }

    /**
     * Deletes the currently selected Edge.
     *
     * @param sourceEdge
     */
    public void deleteEdge(Edge sourceEdge) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name);
            }
        }

        System.out.println(sourceEdge.source.name + " -- " + sourceEdge.target.name);
        List<Edge> ls1 = new ArrayList<>();
        List<Shape> lshape2 = new ArrayList<>();
        for (Edge e : sourceEdge.source.adjacents) {
            if (e.target == sourceEdge.target) {
                ls1.add(e);
                lshape2.add(e.line);
            }
        }
        for (Edge e : sourceEdge.target.adjacents) {
            if (e.target == sourceEdge.source) {
                ls1.add(e);
                lshape2.add(e.line);
            }
        }
        System.out.println("sdsdsd  " + ls1.size());
        sourceEdge.source.adjacents.removeAll(ls1);
        sourceEdge.target.adjacents.removeAll(ls1);
        realEdges.removeAll(ls1);

        edges.removeAll(lshape2);
        canvasGroup.getChildren().removeAll(lshape2);

        System.out.println("AFTER----------");
        for (NodeFX p : circles) {
            System.out.println(p.node.name + " - ");
            for (Edge q : p.node.adjacents) {
                System.out.println(q.source.name + " " + q.target.name);
            }
        }
    }

    /**
     * Change weight of the currently selected edge. Disabled for unweighted
     * graphs.
     *
     * @param sourceEdge
     */
    public void changeWeight(Edge sourceEdge) {
        System.out.println("Before-------");
        for (NodeFX u : circles) {
            System.out.println(u.node.name + " - ");
            for (Edge v : u.node.adjacents) {
                System.out.println(v.source.name + " " + v.target.name + " weight: " + v.weight);
            }
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle(null);
        dialog.setHeaderText("Enter Weight of the Edge :");
        dialog.setContentText(null);

        String res = null;
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            res = result.get();
        }

        for (Edge e : sourceEdge.source.adjacents) {
            if (e.target == sourceEdge.target) {
                e.weight = Double.valueOf(res);
                e.weightLabel.setText(res);
            }
        }
        for (Edge e : sourceEdge.target.adjacents) {
            if (e.target == sourceEdge.source) {
                e.weight = Double.valueOf(res);
            }
        }
        for (Edge e : mstEdges) {
            if (e.source == sourceEdge.source && e.target == sourceEdge.target) {
                e.weight = Double.valueOf(res);
            }
        }

        System.out.println("AFTER----------");
        for (NodeFX p : circles) {
            System.out.println(p.node.name + " - ");
            for (Edge q : p.node.adjacents) {
                System.out.println(q.source.name + " " + q.target.name + " weigh: " + q.weight);
            }
        }
    }

    /**
     * Shape class for the nodes.
     */
    public class NodeFX extends Circle {

        Node node;
        Point point;
        Label distance = new Label("Dist. : INFINITY");
        Label visitTime = new Label("Visit : 0");
        Label lowTime = new Label("Low : 0");
        Label id;
        boolean isSelected = false;

        public NodeFX(double x, double y, double rad, String name) {
            super(x, y, rad);
            node = new Node(name, this);
            point = new Point((int) x, (int) y);
            id = new Label(name);
            canvasGroup.getChildren().add(id);
            id.setLayoutX(x - 18);
            id.setLayoutY(y - 18);
            this.setOpacity(0.5);
            this.setBlendMode(BlendMode.MULTIPLY);
            this.setId("node");

            RightClickMenu rt = new RightClickMenu(this);
            ContextMenu menu = rt.getMenu();
            globalMenu = menu;
            this.setOnContextMenuRequested(e -> {
                if (addEdge || addNode) {
                    menu.show(this, e.getScreenX(), e.getScreenY());
                    menuBool = true;
                }
            });
            menu.setOnAction(e -> {
                menuBool = false;
            });

            circles.add(this);
            System.out.println("ADDing: " + circles.size());
        }
    }

    /*
     * Algorithm Declarations -----------------------------------------------
     */
    public class Algorithm {

        //<editor-fold defaultstate="collapsed" desc="Dijkstra">    
        public void newDijkstra(Node source) {
            new Dijkstra(source);
        }

        class Dijkstra {

            Dijkstra(Node source) {

                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                SequentialTransition st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);
                //</editor-fold>

                source.minDistance = 0;
                PriorityQueue<Node> pq = new PriorityQueue<Node>();
                pq.add(source);
                while (!pq.isEmpty()) {
                    Node u = pq.poll();
                    System.out.println(u.name);
                    //<editor-fold defaultstate="collapsed" desc="Animation Control">
                    FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
                    ft.setToValue(Color.CHOCOLATE);
                    st.getChildren().add(ft);
                    String str = "";
                    str = str.concat("Popped : Node(" + u.name + "), Current Distance: " + u.minDistance + "\n");
                    final String str2 = str;
                    FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
                    fd.setOnFinished(e -> {
                        textFlow.appendText(str2);
                    });
                    fd.onFinishedProperty();
                    st.getChildren().add(fd);
                    //</editor-fold>
                    System.out.println(u.name);
                    for (Edge e : u.adjacents) {
                        if (e != null) {
                            Node v = e.target;
                            System.out.println("HERE " + v.name);
                            if (u.minDistance + e.weight < v.minDistance) {
                                pq.remove(v);
                                v.minDistance = u.minDistance + e.weight;
                                v.previous = u;
                                pq.add(v);
                                //<editor-fold defaultstate="collapsed" desc="Node visiting animation">
                                //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                                if (undirected) {
                                    StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                } else if (directed) {
                                    FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                }
                                //</editor-fold>
                                FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                                ft1.setToValue(Color.FORESTGREEN);
                                ft1.setOnFinished(ev -> {
                                    v.circle.distance.setText("Dist. : " + v.minDistance);
                                });
                                ft1.onFinishedProperty();
                                st.getChildren().add(ft1);

                                str = "\t";
                                str = str.concat("Pushing : Node(" + v.name + "), (" + u.name + "--" + v.name + ") Distance : " + v.minDistance + "\n");
                                final String str1 = str;
                                FadeTransition fd2 = new FadeTransition(Duration.millis(10), textFlow);
                                fd2.setOnFinished(ev -> {
                                    textFlow.appendText(str1);
                                });
                                fd2.onFinishedProperty();
                                st.getChildren().add(fd2);
                                //</editor-fold>
                            }
                        }
                    }
                    //<editor-fold defaultstate="collapsed" desc="Animation Control">
                    FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
                    ft2.setToValue(Color.BLUEVIOLET);
                    st.getChildren().add(ft2);
                    //</editor-fold>
                }

                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();
                    Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                    playPauseImage.setImage(image);
                    paused = true;
                    playing = false;
                    textFlow.appendText("---Finished--\n");
                });
                st.onFinishedProperty();
                st.play();
                playing = true;
                paused = false;
                //</editor-fold>
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="BFS">
        public void newBFS(Node source) {
            new BFS(source);
        }

        class BFS {

            BFS(Node source) {

                //<editor-fold defaultstate="collapsed" desc="Set labels and distances">
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);
                //</editor-fold>

                source.minDistance = 0;
                source.visited = true;
                LinkedList<Node> q = new LinkedList<Node>();
                q.push(source);
                while (!q.isEmpty()) {
                    Node u = q.removeLast();
                    //<editor-fold defaultstate="collapsed" desc="Node Popped Animation">
                    FillTransition ft = new FillTransition(Duration.millis(time), u.circle);
                    if (u.circle.getFill() == Color.BLACK) {
                        ft.setToValue(Color.CHOCOLATE);
                    }
                    st.getChildren().add(ft);

                    String str = "";
                    str = str.concat("Popped : Node(" + u.name + ")\n");
                    final String str2 = str;
                    FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
                    fd.setOnFinished(e -> {
                        textFlow.appendText(str2);
                    });
                    fd.onFinishedProperty();
                    st.getChildren().add(fd);
                    //</editor-fold>
                    System.out.println(u.name);
                    for (Edge e : u.adjacents) {
                        if (e != null) {
                            Node v = e.target;

                            if (!v.visited) {
                                v.minDistance = u.minDistance + 1;
                                v.visited = true;
                                q.push(v);
                                v.previous = u;

                                //<editor-fold defaultstate="collapsed" desc="Node visiting animation">
                                //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                                if (undirected) {
                                    StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                } else if (directed) {
                                    FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                    ftEdge.setToValue(Color.FORESTGREEN);
                                    st.getChildren().add(ftEdge);
                                }
                                //</editor-fold>
                                FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                                ft1.setToValue(Color.FORESTGREEN);
                                ft1.setOnFinished(ev -> {
                                    v.circle.distance.setText("Dist. : " + v.minDistance);
                                });
                                ft1.onFinishedProperty();
                                st.getChildren().add(ft1);

                                str = "\t";
                                str = str.concat("Pushing : Node(" + v.name + ")\n");
                                final String str1 = str;
                                FadeTransition fd2 = new FadeTransition(Duration.millis(10), textFlow);
                                fd2.setOnFinished(ev -> {
                                    textFlow.appendText(str1);
                                });
                                fd2.onFinishedProperty();
                                st.getChildren().add(fd2);
                                //</editor-fold>
                            }
                        }
                    }
                    //<editor-fold defaultstate="collapsed" desc="Animation Control">
                    FillTransition ft2 = new FillTransition(Duration.millis(time), u.circle);
                    ft2.setToValue(Color.BLUEVIOLET);
                    st.getChildren().add(ft2);
                    //</editor-fold>
                }

                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();
                    Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                    playPauseImage.setImage(image);
                    paused = true;
                    playing = false;
                    textFlow.appendText("---Finished--\n");
                });
                st.onFinishedProperty();
                st.play();
                playing = true;
                paused = false;
                //</editor-fold>

            }

        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="TopSort">
        public void newTopSort(Node source) {
            new TopSort(source);
        }

        class TopSort {

            TopSort(Node source) {

                //<editor-fold defaultstate="collapsed" desc="Animation Setup Distances">
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);
                //</editor-fold>

                source.minDistance = 0;
                source.visited = true;
                source.degColor = 0;
                x = 0;
                CycleDetection(source, 0);
                if (x == 1) {
                    TopsortRecursion(source, 0);
                    System.out.println("Hello World " + topSort);
                    String reverse = new StringBuffer(topSort).reverse().toString();

                    //<editor-fold defaultstate="collapsed" desc="Animation after algorithm is finished">
                    st.setOnFinished(ev -> {
                        for (NodeFX n : circles) {
                            FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                            ft1.setToValue(Color.BLACK);
                            ft1.play();
                        }
                        if (directed) {
                            for (Shape n : edges) {
                                n.setFill(Color.BLACK);
                            }
                        } else if (undirected) {
                            for (Shape n : edges) {
                                n.setStroke(Color.BLACK);
                            }
                        }
                        FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                        ft1.setToValue(Color.RED);
                        ft1.play();
                        Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                        playPauseImage.setImage(image);
                        paused = true;
                        playing = false;
                        textFlow.appendText("---Finished--\n\n");
                        textFlow.appendText("Top Sort: " + reverse);

                    });
                    st.onFinishedProperty();
                    st.play();

                    playing = true;
                    paused = false;
                    //</editor-fold>
                } else {
                    System.out.println("Cycle");
                    
                }

            }

            void CycleDetection(Node source, int level) {
                source.degColor = 1;
                for (Edge e : source.adjacents) {
                    if (e != null) {
                        Node v = e.target;
                        if (v.degColor == 1) {
                            x = 1;
                        } else if (v.degColor == 0) {
                            v.previous = source;
                            TopsortRecursion(v, level + 1);
                        }
                    }
                }
                source.degColor=2;
            }

            public void TopsortRecursion(Node source, int level) {
                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                FillTransition ft = new FillTransition(Duration.millis(time), source.circle);
                if (source.circle.getFill() == Color.BLACK) {
                    ft.setToValue(Color.FORESTGREEN);
                }
                st.getChildren().add(ft);

                String str = "";
                for (int i = 0; i < level; i++) {
                    str = str.concat("\t");
                }
                str = str.concat("DFS(" + source.name + ") Enter\n");
                final String str2 = str;
                FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
                fd.setOnFinished(e -> {
                    textFlow.appendText(str2);
                });
                fd.onFinishedProperty();
                st.getChildren().add(fd);
                //</editor-fold>
                for (Edge e : source.adjacents) {
                    if (e != null) {
                        Node v = e.target;
                        if (!v.visited) {
                            v.minDistance = source.minDistance + 1;
                            v.visited = true;
                            v.previous = source;
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            TopsortRecursion(v, level + 1);
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.onFinishedProperty();
                            ft1.setOnFinished(ev -> {
                                v.circle.distance.setText("Dist. : " + v.minDistance);
                            });
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        }
                    }
                }
                str = "";
                for (int i = 0; i < level; i++) {
                    str = str.concat("\t");
                }
                topSort = topSort.concat(" " + source.name);
                System.out.println(topSort);
                str = str.concat("DFS(" + source.name + ") Exit\n");
                final String str1 = str;
                fd = new FadeTransition(Duration.millis(10), textFlow);
                fd.setOnFinished(e -> {
                    textFlow.appendText(str1);
                });
                fd.onFinishedProperty();
                st.getChildren().add(fd);
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Articulation Point">
        public void newArticulationPoint(Node s) {
            new ArticulationPoint(s);
        }

        class ArticulationPoint {

            int timeCnt = 0;

            ArticulationPoint(Node source) {

                //<editor-fold defaultstate="collapsed" desc="Animation Setup Distances">
                for (NodeFX n : circles) {
                    visitTime.add(n.visitTime);
                    n.visitTime.setLayoutX(n.point.x + 20);
                    n.visitTime.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.visitTime);

                    lowTime.add(n.lowTime);
                    n.lowTime.setLayoutX(n.point.x + 20);
                    n.lowTime.setLayoutY(n.point.y + 13);
                    canvasGroup.getChildren().add(n.lowTime);

                    n.node.isArticulationPoint = false;
                }

                st = new SequentialTransition();
                source.circle.lowTime.setText("Low : " + source.name);
                source.circle.visitTime.setText("Visit : " + source.visitTime);
                //</editor-fold>

                timeCnt = 0;
                RecAP(source);

                for (NodeFX n : circles) {
                    if (n.node.isArticulationPoint) {
                        System.out.println(n.node.name);
                    }
                }

                //<editor-fold defaultstate="collapsed" desc="Animation after algorithm is finished">
                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    for (NodeFX n : circles) {
                        if (n.node.isArticulationPoint) {
                            FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                            ft1.setToValue(Color.CHARTREUSE);
                            ft1.play();
                        }
                    }
                    Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                    playPauseImage.setImage(image);
                    paused = true;
                    playing = false;
                });
                st.onFinishedProperty();
                st.play();
                playing = true;
                //</editor-fold>
            }

            void RecAP(Node s) {
                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                FillTransition ft = new FillTransition(Duration.millis(time), s.circle);
                if (s.circle.getFill() == Color.BLACK) {
                    ft.setToValue(Color.FORESTGREEN);
                }
                ft.setOnFinished(ev -> {
                    s.circle.lowTime.setText("Low : " + s.lowTime);
                    s.circle.visitTime.setText("Visit : " + s.visitTime);
                });
                st.getChildren().add(ft);
                //</editor-fold>
                s.visited = true;
                s.visitTime = timeCnt;
                s.lowTime = timeCnt;

                timeCnt++;
                int childCount = 0;

                for (Edge e : s.adjacents) {
                    if (e != null) {
                        Node v = e.target;
                        if (s.previous == v) {
                            continue;
                        }
                        if (!v.visited) {
                            v.previous = s;
                            childCount++;
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            RecAP(v);

                            s.lowTime = Math.min(s.lowTime, v.lowTime);
                            if (s.visitTime <= v.lowTime && s.previous != null) {
                                s.isArticulationPoint = true;
                            }

                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            ///<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.setOnFinished(ev -> {
                                s.circle.lowTime.setText("Low : " + s.lowTime);
                                s.circle.visitTime.setText("Visit : " + s.visitTime);
                            });
                            ft1.onFinishedProperty();
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        } else {
                            s.lowTime = Math.min(s.lowTime, v.visitTime);
                        }
                    }
                }
                if (childCount > 1 && s.previous == null) {
                    s.isArticulationPoint = true;
                }
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="MST">
        public void newMST() {
            new MST();
        }

        class MST {

            int mstValue = 0;

            Node findParent(Node x) {
                if (x == x.previous) {
                    return x;
                }
                x.previous = findParent(x.previous);
                return x.previous;
            }

            void unionNode(Node x, Node y) {
                Node px = findParent(x);
                Node py = findParent(y);
                if (px == py) {
                    return;
                }
                if (Integer.valueOf(px.name) < Integer.valueOf(py.name)) {
                    px.previous = py;
                } else {
                    py.previous = px;
                }
            }

            public MST() {

                st = new SequentialTransition();
                for (NodeFX x : circles) {
                    x.node.previous = x.node;
                }

                //<editor-fold defaultstate="collapsed" desc="Detail Information">
                String init = "Intially : \n";
                for (NodeFX x : circles) {
                    final String s = "Node : " + x.node.name + " , Parent: " + x.node.previous.name + "\n";
                    FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
                    fd.setOnFinished(e -> {
                        textFlow.appendText(s);
                    });
                    fd.onFinishedProperty();
                    st.getChildren().add(fd);
                }
                final String s = "Start Algorithm :---\n";
                FadeTransition fdss = new FadeTransition(Duration.millis(10), textFlow);
                fdss.setOnFinished(ev -> {
                    textFlow.appendText(s);
                });
                fdss.onFinishedProperty();
                st.getChildren().add(fdss);
                //</editor-fold>
                Collections.sort(mstEdges, new Comparator<Edge>() {
                    public int compare(Edge o1, Edge o2) {
                        if (o1.weight == o2.weight) {
                            return 0;
                        }
                        return o1.weight > o2.weight ? 1 : -1;
                    }
                });

                for (Edge e : mstEdges) {

                    StrokeTransition ft1 = new StrokeTransition(Duration.millis(time), e.line);
                    ft1.setToValue(Color.DARKORANGE);
                    st.getChildren().add(ft1);

                    //<editor-fold defaultstate="collapsed" desc="Detail Information">
                    final String se = "Selected Edge:- (" + e.source.name.trim() + "--" + e.target.name.trim() + ") Weight: " + String.valueOf(e.weight) + " \n";
                    FadeTransition fdx = new FadeTransition(Duration.millis(10), textFlow);
                    fdx.setOnFinished(evx -> {
                        textFlow.appendText(se);
                    });
                    fdx.onFinishedProperty();
                    st.getChildren().add(fdx);

                    final String s1 = "\t-> Node :" + e.source.name.trim() + "  Parent: " + findParent(e.source.previous).name.trim() + "\n";
                    FadeTransition fdx2 = new FadeTransition(Duration.millis(10), textFlow);
                    fdx2.setOnFinished(evx -> {
                        textFlow.appendText(s1);
                    });
                    fdx2.onFinishedProperty();
                    st.getChildren().add(fdx2);

                    final String s2 = "\t-> Node :" + e.target.name.trim() + "  Parent: " + findParent(e.target.previous).name.trim() + "\n";
                    FadeTransition fdx3 = new FadeTransition(Duration.millis(10), textFlow);
                    fdx3.setOnFinished(evx -> {
                        textFlow.appendText(s2);
                    });
                    fdx3.onFinishedProperty();
                    st.getChildren().add(fdx3);
                    //</editor-fold>

                    if (findParent(e.source.previous) != findParent(e.target.previous)) {
                        unionNode(e.source, e.target);
                        mstValue += e.weight;

                        //<editor-fold defaultstate="collapsed" desc="Detail Information">
                        final String sa = "\t---->Unioned\n";
                        final String sa1 = "\t\t->Node :" + e.source.name.trim() + "  Parent: " + findParent(e.source.previous).name.trim() + "\n";
                        final String sa2 = "\t\t->Node :" + e.target.name.trim() + "  Parent: " + findParent(e.target.previous).name.trim() + "\n";
                        FadeTransition fdx4 = new FadeTransition(Duration.millis(10), textFlow);
                        fdx4.setOnFinished(evx -> {
                            textFlow.appendText(sa);
                        });
                        fdx4.onFinishedProperty();
                        st.getChildren().add(fdx4);
                        FadeTransition fdx5 = new FadeTransition(Duration.millis(10), textFlow);
                        fdx5.setOnFinished(evx -> {
                            textFlow.appendText(sa1);
                        });
                        fdx5.onFinishedProperty();
                        st.getChildren().add(fdx5);
                        FadeTransition fdx6 = new FadeTransition(Duration.millis(10), textFlow);
                        fdx6.setOnFinished(evx -> {
                            textFlow.appendText(sa2);
                        });
                        fdx6.onFinishedProperty();
                        st.getChildren().add(fdx6);

                        StrokeTransition ft2 = new StrokeTransition(Duration.millis(time), e.line);
                        ft2.setToValue(Color.DARKGREEN);
                        st.getChildren().add(ft2);

                        FillTransition ft3 = new FillTransition(Duration.millis(time), e.source.circle);
                        ft3.setToValue(Color.AQUA);
                        st.getChildren().add(ft3);

                        ft3 = new FillTransition(Duration.millis(time), e.target.circle);
                        ft3.setToValue(Color.AQUA);
                        st.getChildren().add(ft3);
                        //</editor-fold>
                    } else {
                        //<editor-fold defaultstate="collapsed" desc="Detail Info">
                        final String sa = "\t---->Cycle Detected\n";
                        FadeTransition fdx7 = new FadeTransition(Duration.millis(10), textFlow);
                        fdx7.setOnFinished(evx -> {
                            textFlow.appendText(sa);
                        });
                        fdx7.onFinishedProperty();
                        st.getChildren().add(fdx7);
                        //</editor-fold>
                        StrokeTransition ft2 = new StrokeTransition(Duration.millis(time), e.line);
                        ft2.setToValue(Color.DARKRED);
                        st.getChildren().add(ft2);

                        ft2 = new StrokeTransition(Duration.millis(time), e.line);
                        ft2.setToValue(Color.web("#E0E0E0"));
                        st.getChildren().add(ft2);

                    }
                }

                //<editor-fold defaultstate="collapsed" desc="Animation after algorithm is finished">
                st.setOnFinished(ev -> {
                    Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                    playPauseImage.setImage(image);
                    paused = true;
                    playing = false;
                    textFlow.appendText("Minimum Cost of the Graph " + mstValue);
                });
                st.onFinishedProperty();
                st.play();
                playing = true;
                //</editor-fold>
                System.out.println("" + mstValue);
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="DFS">
        public void newDFS(Node source) {
            new DFS(source);
        }

        class DFS {

            DFS(Node source) {

                //<editor-fold defaultstate="collapsed" desc="Animation Setup Distances">
                for (NodeFX n : circles) {
                    distances.add(n.distance);
                    n.distance.setLayoutX(n.point.x + 20);
                    n.distance.setLayoutY(n.point.y);
                    canvasGroup.getChildren().add(n.distance);
                }
                sourceText.setLayoutX(source.circle.point.x + 20);
                sourceText.setLayoutY(source.circle.point.y + 10);
                canvasGroup.getChildren().add(sourceText);
                st = new SequentialTransition();
                source.circle.distance.setText("Dist. : " + 0);
                //</editor-fold>

                source.minDistance = 0;
                source.visited = true;
                DFSRecursion(source, 0);

                //<editor-fold defaultstate="collapsed" desc="Animation after algorithm is finished">
                st.setOnFinished(ev -> {
                    for (NodeFX n : circles) {
                        FillTransition ft1 = new FillTransition(Duration.millis(time), n);
                        ft1.setToValue(Color.BLACK);
                        ft1.play();
                    }
                    if (directed) {
                        for (Shape n : edges) {
                            n.setFill(Color.BLACK);
                        }
                    } else if (undirected) {
                        for (Shape n : edges) {
                            n.setStroke(Color.BLACK);
                        }
                    }
                    FillTransition ft1 = new FillTransition(Duration.millis(time), source.circle);
                    ft1.setToValue(Color.RED);
                    ft1.play();
                    Image image = new Image(getClass().getResourceAsStream("/play_arrow_black_48x48.png"));
                    playPauseImage.setImage(image);
                    paused = true;
                    playing = false;
                    textFlow.appendText("---Finished--\n");
                });
                st.onFinishedProperty();
                st.play();
                playing = true;
                paused = false;
                //</editor-fold>
            }

            public void DFSRecursion(Node source, int level) {
                //<editor-fold defaultstate="collapsed" desc="Animation Control">
                FillTransition ft = new FillTransition(Duration.millis(time), source.circle);
                if (source.circle.getFill() == Color.BLACK) {
                    ft.setToValue(Color.FORESTGREEN);
                }
                st.getChildren().add(ft);

                String str = "";
                for (int i = 0; i < level; i++) {
                    str = str.concat("\t");
                }
                str = str.concat("DFS(" + source.name + ") Enter\n");
                final String str2 = str;
                FadeTransition fd = new FadeTransition(Duration.millis(10), textFlow);
                fd.setOnFinished(e -> {
                    textFlow.appendText(str2);
                });
                fd.onFinishedProperty();
                st.getChildren().add(fd);
                //</editor-fold>
                for (Edge e : source.adjacents) {
                    if (e != null) {
                        Node v = e.target;
                        if (!v.visited) {
                            v.minDistance = source.minDistance + 1;
                            v.visited = true;
                            v.previous = source;
//                        v.circle.distance.setText("Dist. : " + v.minDistance);
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.FORESTGREEN);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            DFSRecursion(v, level + 1);
                            //<editor-fold defaultstate="collapsed" desc="Animation Control">
                            //<editor-fold defaultstate="collapsed" desc="Change Edge colors">
                            if (undirected) {
                                StrokeTransition ftEdge = new StrokeTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            } else if (directed) {
                                FillTransition ftEdge = new FillTransition(Duration.millis(time), e.line);
                                ftEdge.setToValue(Color.BLUEVIOLET);
                                st.getChildren().add(ftEdge);
                            }
                            //</editor-fold>
                            FillTransition ft1 = new FillTransition(Duration.millis(time), v.circle);
                            ft1.setToValue(Color.BLUEVIOLET);
                            ft1.onFinishedProperty();
                            ft1.setOnFinished(ev -> {
                                v.circle.distance.setText("Dist. : " + v.minDistance);
                            });
                            st.getChildren().add(ft1);
                            //</editor-fold>
                        }
                    }
                }
                str = "";
                for (int i = 0; i < level; i++) {
                    str = str.concat("\t");
                }
                str = str.concat("DFS(" + source.name + ") Exit\n");
                final String str1 = str;
                fd = new FadeTransition(Duration.millis(10), textFlow);
                fd.setOnFinished(e -> {
                    textFlow.appendText(str1);
                });
                fd.onFinishedProperty();
                st.getChildren().add(fd);
            }
        }

        //</editor-fold>
        public List<Node> getShortestPathTo(Node target) {
            List<Node> path = new ArrayList<Node>();
            for (Node i = target; i != null; i = i.previous) {
                path.add(i);
            }
            Collections.reverse(path);
            return path;
        }
    }

}
