/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import fxsimulator.CanvasController.NodeFX;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author sowmen
 */
public class RightClickMenu {

    ContextMenu menu;
    NodeFX sourceNode;
    Edge sourceEdge;
    MenuItem delete, changeId;

    public RightClickMenu() {
        menu = new ContextMenu();
        delete = new MenuItem("Delete");
        changeId = new MenuItem("Change ID");

        Image openIcon = new Image(getClass().getResourceAsStream("/delete_img.png"));
        ImageView openView = new ImageView(openIcon);
        delete.setGraphic(openView);

        Image textIcon = new Image(getClass().getResourceAsStream("/rename_img.png"));
        ImageView textIconView = new ImageView(textIcon);
        changeId.setGraphic(textIconView);

        menu.getItems().addAll(delete, changeId);
        menu.setOpacity(0.9);
    }

    /**
     * Constructor for the context menu on node
     * @param node 
     */
    public RightClickMenu(NodeFX node) {
        this();
        sourceNode = node;
        delete.setOnAction(e -> {
            Panel1Controller.cref.deleteNode(sourceNode);
        });
        changeId.setOnAction(e -> {
            Panel1Controller.cref.changeID(node);
        });
    }
    
    /**
     * Constructor for the context menu on edge
     * @param edge 
     */
    public RightClickMenu(Edge edge) {
        this();
        sourceEdge = edge;
        delete.setOnAction(e -> {
            Panel1Controller.cref.deleteEdge(sourceEdge);
        });
        changeId.setOnAction(e -> {
            Panel1Controller.cref.changeWeight(sourceEdge);
        });
    }

    public ContextMenu getMenu() {
        return menu;
    }
}
