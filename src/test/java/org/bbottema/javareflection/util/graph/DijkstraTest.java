package org.bbottema.javareflection.util.graph;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DijkstraTest {
	
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
		Node<Class<?>> startingPoint = nodeA;
		Set<Node<Class<?>>> destinationNodes = new HashSet<>();
		destinationNodes.add(nodeB);
		destinationNodes.add(nodeC);
		destinationNodes.add(nodeD);
		destinationNodes.add(nodeE);
		destinationNodes.add(nodeF);
		
		Dijkstra.findShortestPathToAllOtherNodes(startingPoint);
		
		Map<Class<?>, List<Node<Class<?>>>> expectedShortestPaths = new HashMap<>();
		expectedShortestPaths.put(Integer.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(String.class, Arrays.asList(nodeA));
		expectedShortestPaths.put(double.class, Arrays.asList(nodeA, nodeB));
		expectedShortestPaths.put(Double.class, Arrays.asList(nodeA, nodeB, nodeD));
		expectedShortestPaths.put(boolean.class, Arrays.asList(nodeA, nodeB, nodeD));
		
		for (Node<Class<?>> node : destinationNodes) {
			assertThat(expectedShortestPaths.get(node.getType())).isEqualTo(node.getLeastExpensivePath());
		}
	}
}
