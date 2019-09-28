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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings("unused")
final class Dijkstra {
	
	private Dijkstra() {
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> void findShortestPathToAllOtherNodes(Node<T> startingPoint) {
		startingPoint.setCost(0);
		
		Set<Node<T>> settledNodes = new HashSet<>();
		Set<Node<T>> unsettledNodes = new HashSet<>();
		unsettledNodes.add(startingPoint);
		
		while (unsettledNodes.size() != 0) {
			Node<T> currentNode = getLowestDistanceNode(unsettledNodes);
			unsettledNodes.remove(currentNode);
			for (Entry<Node<T>, Integer> adjacencyPair : currentNode.getToNodes().entrySet()) {
				Node<T> adjacentNode = adjacencyPair.getKey();
				
				if (!settledNodes.contains(adjacentNode)) {
					Integer edgeWeight = adjacencyPair.getValue();
					calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
					unsettledNodes.add(adjacentNode);
				}
			}
			settledNodes.add(currentNode);
		}
	}
	
	private static <T> void calculateMinimumDistance(Node<T> evaluationNode, Integer edgeWeigh, Node<T> sourceNode) {
		if (sourceNode.getCost() + edgeWeigh < evaluationNode.getCost()) {
			evaluationNode.setCost(sourceNode.getCost() + edgeWeigh);
			LinkedList<Node<T>> shortestPath = new LinkedList<>(sourceNode.getLeastExpensivePath());
			shortestPath.add(sourceNode);
			evaluationNode.setLeastExpensivePath(shortestPath);
		}
	}
	
	private static <T> Node<T> getLowestDistanceNode(Set<Node<T>> unsettledNodes) {
		Node<T> lowestDistanceNode = null;
		int lowestDistance = Integer.MAX_VALUE;
		for (Node<T> node : unsettledNodes) {
			if (node.getCost() < lowestDistance) {
				lowestDistance = node.getCost();
				lowestDistanceNode = node;
			}
		}
		return lowestDistanceNode;
	}
}