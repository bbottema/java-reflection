package org.bbottema.javareflection.util;

import org.bbottema.javareflection.util.Dijkstra.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class DijkstraAlgorithmLongRunningUnitTest {

    @Test
    public void whenSPPSolved_thenCorrect() {

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Node nodeF = new Node("F");
	
		nodeA.getAdjacentNodes().put(nodeB, 10);
		nodeA.getAdjacentNodes().put(nodeC, 15);
	
		nodeB.getAdjacentNodes().put(nodeD, 12);
		nodeB.getAdjacentNodes().put(nodeF, 15);
	
		nodeC.getAdjacentNodes().put(nodeE, 10);
	
		nodeD.getAdjacentNodes().put(nodeE, 2);
		nodeD.getAdjacentNodes().put(nodeF, 1);
	
		nodeF.getAdjacentNodes().put(nodeE, 5);
	
	
		Set<Node> nodes = new HashSet<>();
        
        nodes.add(nodeA);
        nodes.add(nodeB);
        nodes.add(nodeC);
        nodes.add(nodeD);
        nodes.add(nodeE);
        nodes.add(nodeF);
    
        Dijkstra.calculateShortestPathFromSource(nodeA);

        List<Node> shortestPathForNodeB = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeC = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeD = Arrays.asList(nodeA, nodeB);
        List<Node> shortestPathForNodeE = Arrays.asList(nodeA, nodeB, nodeD);
        List<Node> shortestPathForNodeF = Arrays.asList(nodeA, nodeB, nodeD);
    
        for (Node node : nodes) {
            switch (node.getName()) {
            case "B":
                assertTrue(node
                  .getShortestPath()
                  .equals(shortestPathForNodeB));
                break;
            case "C":
                assertTrue(node
                  .getShortestPath()
                  .equals(shortestPathForNodeC));
                break;
            case "D":
                assertTrue(node
                  .getShortestPath()
                  .equals(shortestPathForNodeD));
                break;
            case "E":
                assertTrue(node
                  .getShortestPath()
                  .equals(shortestPathForNodeE));
                break;
            case "F":
                assertTrue(node
                  .getShortestPath()
                  .equals(shortestPathForNodeF));
                break;
            }
        }
    }
}
