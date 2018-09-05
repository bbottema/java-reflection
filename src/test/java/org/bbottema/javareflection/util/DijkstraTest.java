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
		Node<Class<?>> nodeA = new Node<Class<?>>(byte.class);
		Node<Class<?>> nodeB = new Node<Class<?>>(Integer.class);
		Node<Class<?>> nodeC = new Node<Class<?>>(String.class);
		Node<Class<?>> nodeD = new Node<Class<?>>(double.class);
		Node<Class<?>> nodeE = new Node<Class<?>>(Double.class);
		Node<Class<?>> nodeF = new Node<Class<?>>(boolean.class);
		// define edges from source
		nodeA.getToNodes().put(nodeB, 10);
		nodeA.getToNodes().put(nodeC, 15);
		nodeB.getToNodes().put(nodeE, 10);
		nodeC.getToNodes().put(nodeA, 10); // cyclic path
		// define other edges
		nodeD.getToNodes().put(nodeA, 15); // to A but not reachable
		nodeF.getToNodes().put(nodeE, 10);
		nodeF.getToNodes().put(nodeD, 10); // cyclic path
		
		assertThat(Dijkstra.findReachableNodes(nodeA)).containsExactlyInAnyOrder(nodeB, nodeC, nodeE);
	}
	
	@Test
	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	public void testFindShortestPathToAllOtherNodes() {
		// define nodes
		Node<Class<?>> nodeA = new Node<Class<?>>(byte.class);
		Node<Class<?>> nodeB = new Node<Class<?>>(Integer.class);
		Node<Class<?>> nodeC = new Node<Class<?>>(String.class);
		Node<Class<?>> nodeD = new Node<Class<?>>(double.class);
		Node<Class<?>> nodeE = new Node<Class<?>>(Double.class);
		Node<Class<?>> nodeF = new Node<Class<?>>(boolean.class);
		// define edges
		nodeA.getToNodes().put(nodeB, 10);
		nodeA.getToNodes().put(nodeC, 15);
		nodeB.getToNodes().put(nodeD, 12);
		nodeB.getToNodes().put(nodeF, 15);
		nodeC.getToNodes().put(nodeE, 10);
		nodeD.getToNodes().put(nodeE, 2);
		nodeD.getToNodes().put(nodeF, 1);
		nodeF.getToNodes().put(nodeE, 5);
		
		@SuppressWarnings("UnnecessaryLocalVariable")
		Node startingPoint = nodeA;
		Set<Node<Class<?>>> destinationNodes = new HashSet<>();
		destinationNodes.add(nodeB);
		destinationNodes.add(nodeC);
		destinationNodes.add(nodeD);
		destinationNodes.add(nodeE);
		destinationNodes.add(nodeF);
		
		Dijkstra.findShortestPathToAllOtherNodesUsingDijkstra(startingPoint);
		
		Map<Class<?>, List<Node<Class<?>>>> expectedShortestPaths = new HashMap<>();
		expectedShortestPaths.put(Integer.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(String.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(double.class, Arrays.asList(nodeA, nodeB));
		expectedShortestPaths.put(Double.class, Arrays.asList(nodeA, nodeB, nodeD));
		expectedShortestPaths.put(boolean.class, Arrays.asList(nodeA, nodeB, nodeD));
		
		for (Node<Class<?>> node : destinationNodes) {
			assertEquals(node.getLeastExpensivePath(), expectedShortestPaths.get(node.getType()));
		}
	}
	
	@Test
	public void testFindAllPathsAscending() {
		Node<Class<?>> nodeA = new Node<Class<?>>(byte.class);
		Node<Class<?>> nodeB = new Node<Class<?>>(Integer.class);
		Node<Class<?>> nodeC = new Node<Class<?>>(String.class);
		Node<Class<?>> nodeD = new Node<Class<?>>(double.class);
		Node<Class<?>> nodeE = new Node<Class<?>>(Double.class);
		Node<Class<?>> nodeF = new Node<Class<?>>(boolean.class);
		// define edges from source
		nodeA.getToNodes().put(nodeE, 10);
		nodeA.getToNodes().put(nodeB, 10);
		nodeA.getToNodes().put(nodeC, 15);
		nodeB.getToNodes().put(nodeE, 10);
		nodeC.getToNodes().put(nodeA, 10); // cyclic path
		nodeC.getToNodes().put(nodeE, 10); // cyclic path
		// define other edges
		nodeD.getToNodes().put(nodeA, 15); // to A but not reachable
		nodeF.getToNodes().put(nodeE, 10);
		nodeF.getToNodes().put(nodeD, 10); // cyclic path
		
		assertThat(Dijkstra.findAllPathsAscending(nodeA, nodeD)).isEmpty();
		assertThat(Dijkstra.findAllPathsAscending(nodeC, nodeF)).isEmpty();
		assertThat(Dijkstra.findAllPathsAscending(nodeA, nodeE)).containsExactly(
				Arrays.asList(nodeA, nodeE),
				Arrays.asList(nodeA, nodeB, nodeE),
				Arrays.asList(nodeA, nodeC, nodeE)
		);
	}
}
