package org.bbottema.javareflection.util.graph;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphHelperTest {
	
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
		
		assertThat(GraphHelper.findAllPathsAscending(nodeA, nodeD)).isEmpty();
		assertThat(GraphHelper.findAllPathsAscending(nodeC, nodeF)).isEmpty();
		assertThat(GraphHelper.findAllPathsAscending(nodeA, nodeE)).containsExactly(
				Arrays.asList(nodeA, nodeE),
				Arrays.asList(nodeA, nodeB, nodeE),
				Arrays.asList(nodeA, nodeC, nodeE)
		);
	}
	
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
		
		assertThat(GraphHelper.findReachableNodes(nodeA)).containsExactlyInAnyOrder(nodeB, nodeC, nodeE);
	}
}