/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import com.sun.glass.events.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfxtras.labs.util.event.MouseControlUtil;
import static fxsimulator.CanvasController.graph;
import javafx.scene.Group;
import javafx.scene.text.Text;

/**
 *
 * @author sowme
 */
public class Node {
    private String id;
    private double x,y;
    private final int RAD = 12;
    private Group node;
    
    
    Node(String id, double x, double y) {
        this.id = id;
        this.x = x; this.y = y;
        
        Circle circle = new Circle(x, y, RAD, Color.DARKORCHID);
        circle.setStroke(Color.BLACK);
        Text text = new Text(x,y,id);
        text.setTranslateX(-3.5); text.setTranslateY(4);
        node = new Group(circle,text);
        
        MouseControlUtil.makeDraggable(node);
        
        node.setOnMouseClicked(e -> {
            circle.setFill(Color.YELLOW);
            System.out.println("dgd");
            circle.setOnKeyPressed(ev -> {
                System.out.println("HERE");
                if(ev.getCharacter().equals(KeyEvent.VK_DELETE)){
                    graph.remove(graph.indexOf(circle));
                }
            });
            System.out.println("qwqw");
        });
        node.setOnMouseExited(e -> {
            circle.setFill(Color.DARKORCHID);    
        });
        
        
    }
    
    public Group getNode() {
        return node;
    }
}
