package org.bbottema.javareflection.util;

import org.bbottema.javareflection.util.Dijkstra.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class DijkstraTest {
	
	@Test
	public void testFindReachableNodes() {
		// define nodes
		Node nodeA = new Node(byte.class);
		Node nodeB = new Node(Integer.class);
		Node nodeC = new Node(String.class);
		Node nodeD = new Node(double.class);
		Node nodeE = new Node(Double.class);
		Node nodeF = new Node(boolean.class);
		// define edges from source
		nodeA.getToTypes().put(nodeB, 10);
		nodeA.getToTypes().put(nodeC, 15);
		nodeB.getToTypes().put(nodeE, 10);
		nodeC.getToTypes().put(nodeA, 10); // cyclic path
		// define other edges
		nodeD.getToTypes().put(nodeA, 15); // to A but not reachable
		nodeF.getToTypes().put(nodeE, 10);
		nodeF.getToTypes().put(nodeD, 10); // cyclic path
		
		assertThat(Dijkstra.findReachableNodes(nodeA)).containsExactlyInAnyOrder(nodeB, nodeC, nodeE);
	}
	
	@Test
	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	public void testFindShortestPathToAllOtherNodes() {
		// define nodes
		Node nodeA = new Node(byte.class);
		Node nodeB = new Node(Integer.class);
		Node nodeC = new Node(String.class);
		Node nodeD = new Node(double.class);
		Node nodeE = new Node(Double.class);
		Node nodeF = new Node(boolean.class);
		// define edges
		nodeA.getToTypes().put(nodeB, 10);
		nodeA.getToTypes().put(nodeC, 15);
		nodeB.getToTypes().put(nodeD, 12);
		nodeB.getToTypes().put(nodeF, 15);
		nodeC.getToTypes().put(nodeE, 10);
		nodeD.getToTypes().put(nodeE, 2);
		nodeD.getToTypes().put(nodeF, 1);
		nodeF.getToTypes().put(nodeE, 5);
		
		@SuppressWarnings("UnnecessaryLocalVariable")
		Node startingPoint = nodeA;
		Set<Node> destinationNodes = new HashSet<>();
		destinationNodes.add(nodeB);
		destinationNodes.add(nodeC);
		destinationNodes.add(nodeD);
		destinationNodes.add(nodeE);
		destinationNodes.add(nodeF);
		
		Dijkstra.findShortestPathToAllOtherNodesUsingDijkstra(startingPoint);
		
		Map<Class<?>, List<Node>> expectedShortestPaths = new HashMap<>();
		expectedShortestPaths.put(Integer.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(String.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(double.class, Arrays.asList(nodeA, nodeB));
		expectedShortestPaths.put(Double.class, Arrays.asList(nodeA, nodeB, nodeD));
		expectedShortestPaths.put(boolean.class, Arrays.asList(nodeA, nodeB, nodeD));
		
		for (Node node : destinationNodes) {
			assertEquals(node.getShortestPath(), expectedShortestPaths.get(node.getType()));
		}
	}
	
	@Test
	public void testFindAllPathsAscending() {
		Node nodeA = new Node(byte.class);
		Node nodeB = new Node(Integer.class);
		Node nodeC = new Node(String.class);
		Node nodeD = new Node(double.class);
		Node nodeE = new Node(Double.class);
		Node nodeF = new Node(boolean.class);
		// define edges from source
		nodeA.getToTypes().put(nodeE, 10);
		nodeA.getToTypes().put(nodeB, 10);
		nodeA.getToTypes().put(nodeC, 15);
		nodeB.getToTypes().put(nodeE, 10);
		nodeC.getToTypes().put(nodeA, 10); // cyclic path
		nodeC.getToTypes().put(nodeE, 10); // cyclic path
		// define other edges
		nodeD.getToTypes().put(nodeA, 15); // to A but not reachable
		nodeF.getToTypes().put(nodeE, 10);
		nodeF.getToTypes().put(nodeD, 10); // cyclic path
		
		assertThat(Dijkstra.findAllPathsAscending(nodeA, nodeD)).isEmpty();
		assertThat(Dijkstra.findAllPathsAscending(nodeC, nodeF)).isEmpty();
		assertThat(Dijkstra.findAllPathsAscending(nodeA, nodeE)).containsExactly(
				Arrays.asList(nodeA, nodeE),
				Arrays.asList(nodeA, nodeB, nodeE),
				Arrays.asList(nodeA, nodeC, nodeE)
		);
	}
}
