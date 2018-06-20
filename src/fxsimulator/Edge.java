/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

/**
 *
 * @author sowme
 */
public class Edge {
    public final Node target;
    public final double weight;
    
    public Edge(Node argTarget) {
        target = argTarget; weight = 0;
    }
    public Edge(Node argTarget, double argWeight) {
        target = argTarget; weight = argWeight;                
    }
}
