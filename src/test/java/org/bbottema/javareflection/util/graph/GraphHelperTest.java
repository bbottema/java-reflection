/*
 * Copyright (C) ${project.inceptionYear} Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bbottema.javareflection.util.graph;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphHelperTest {
	
	@Test
	public void testFindAllPathsAscending() {
		Node<String> nodeA = new Node<>("A");
		Node<String> nodeB = new Node<>("B");
		Node<String> nodeC = new Node<>("C");
		Node<String> nodeD = new Node<>("D");
		Node<String> nodeE = new Node<>("E");
		Node<String> nodeF = new Node<>("F");
		Node<String> nodeX = new Node<>("X");
		Node<String> nodeY = new Node<>("Y");
		Node<String> nodeZ = new Node<>("Z");
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
		// define low cost edges
		nodeA.getToNodes().put(nodeX, 1);
		nodeX.getToNodes().put(nodeY, 1);
		nodeY.getToNodes().put(nodeZ, 1);
		nodeZ.getToNodes().put(nodeE, 1);
		
		assertThat(GraphHelper.findAllPathsAscending(nodeA, nodeD)).isEmpty();
		assertThat(GraphHelper.findAllPathsAscending(nodeC, nodeF)).isEmpty();
		assertThat(GraphHelper.findAllPathsAscending(nodeA, nodeE)).containsExactly(
				Arrays.asList(nodeX, nodeY, nodeZ, nodeE),
				Arrays.asList(nodeE),
				Arrays.asList(nodeB, nodeE),
				Arrays.asList(nodeC, nodeE)
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