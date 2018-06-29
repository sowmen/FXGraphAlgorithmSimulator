/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import javafx.scene.shape.Shape;

/**
 *
 * @author sowme
 */
public class Edge {
    public final Node source, target;
    public final double weight;
    public Shape line;
    public Edge(Node argSource, Node argTarget) {
        source = argSource; target = argTarget; weight = 0;
    }
    public Edge(Node argSource, Node argTarget, double argWeight, Shape argline) {
        source = argSource; target = argTarget; weight = argWeight; line = argline;           
    }
}
