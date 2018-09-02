package org.bbottema.javareflection.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Dijkstra {
	
	@Setter
	@Getter
	@RequiredArgsConstructor
	public static class Node {
		@Nonnull
		private String name;
		private LinkedList<Node> shortestPath = new LinkedList<>();
		private Integer distance = Integer.MAX_VALUE;
		private Map<Node, Integer> adjacentNodes = new HashMap<>();
	}
	
	public static void calculateShortestPathFromSource(Node source) {
		source.setDistance(0);
		
		Set<Node> settledNodes = new HashSet<>();
		Set<Node> unsettledNodes = new HashSet<>();
		unsettledNodes.add(source);
		
		while (unsettledNodes.size() != 0) {
			Node currentNode = getLowestDistanceNode(unsettledNodes);
			unsettledNodes.remove(currentNode);
			for (Entry<Node, Integer> adjacencyPair : currentNode.getAdjacentNodes().entrySet()) {
				Node adjacentNode = adjacencyPair.getKey();
				
				if (!settledNodes.contains(adjacentNode)) {
					Integer edgeWeigh = adjacencyPair.getValue();
					calculateMinimumDistance(adjacentNode, edgeWeigh, currentNode);
					unsettledNodes.add(adjacentNode);
				}
			}
			settledNodes.add(currentNode);
		}
	}
	
	private static void calculateMinimumDistance(Node evaluationNode, Integer edgeWeigh, Node sourceNode) {
		if (sourceNode.getDistance() + edgeWeigh < evaluationNode.getDistance()) {
			evaluationNode.setDistance(sourceNode.getDistance() + edgeWeigh);
			LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
			shortestPath.add(sourceNode);
			evaluationNode.setShortestPath(shortestPath);
		}
	}
	
	private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
		Node lowestDistanceNode = null;
		int lowestDistance = Integer.MAX_VALUE;
		for (Node node : unsettledNodes) {
			if (node.getDistance() < lowestDistance) {
				lowestDistance = node.getDistance();
				lowestDistanceNode = node;
			}
		}
		return lowestDistanceNode;
	}
}
