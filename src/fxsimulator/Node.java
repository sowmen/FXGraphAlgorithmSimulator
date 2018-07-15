/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxsimulator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sowme
 */
public class Node implements Comparable<Node> {

    public String name;
    public List<Edge> adjacents = new ArrayList<Edge>();
    public List<Edge> revAdjacents = new ArrayList<Edge>();
    public Node previous;
    public CanvasController.NodeFX circle;
    public double minDistance = Double.POSITIVE_INFINITY;
    public boolean visited, isArticulationPoint;
    public int visitTime = 0, lowTime = 0;

    public Node(String argName) {
        name = argName;
        visited = false;
    }

    public Node(String argName, CanvasController.NodeFX c) {
        name = argName;
        circle = c;
        visited = false;
    }

    @Override
    public int compareTo(Node o) {
        return Double.compare(minDistance, o.minDistance);
    }
}
