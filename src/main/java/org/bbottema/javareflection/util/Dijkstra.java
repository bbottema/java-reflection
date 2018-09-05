package org.bbottema.javareflection.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Collections.reverseOrder;

public class Dijkstra {
	
	@Data
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	@ToString(onlyExplicitlyIncluded = true)
	public static class Node {
		@Nonnull
		@EqualsAndHashCode.Include
		@ToString.Include
		private Class<?> type;
		private LinkedList<Node> shortestPath = new LinkedList<>();
		private Integer distance = Integer.MAX_VALUE;
		private Map<Node, Integer> toTypes = new HashMap<>();
	}
	
	@SuppressWarnings("WeakerAccess")
	public static List<List<Node>> findAllPathsAscending(Node startingPoint, Node destination) {
		List<List<Node>> allPaths = new ArrayList<>();
		findAllPossiblePaths(startingPoint, destination, new ArrayDeque<Node>(), allPaths);
		Collections.sort(allPaths, NodesComparator.INSTANCE);
		return allPaths;
	}
	
	@SuppressWarnings({"StatementWithEmptyBody"})
	private static void findAllPossiblePaths(Node currentNode, Node destination, Deque<Node> currentPath, List<List<Node>> possiblePathsSoFar) {
		if (!currentPath.contains(currentNode)) {
			currentPath.addLast(currentNode);
			
			if (currentNode.equals(destination)) {
				if (!currentPath.isEmpty()) {
					possiblePathsSoFar.add(new ArrayList<>(currentPath));
				} else {
					// startingPoint same as destination
				}
			} else {
				for (Node nextNode : currentNode.getToTypes().keySet()) {
					findAllPossiblePaths(nextNode, destination, currentPath, possiblePathsSoFar);
				}
			}
			currentPath.removeLast();
		} else {
			// cyclic path
		}
	}
	
	@Nonnull
	public static Set<Node> findReachableNodes(final Node fromNode) {
		Set<Node> reachableNodes = new HashSet<>();
		findReachableNodes(fromNode, reachableNodes);
		reachableNodes.remove(fromNode); // in case of cyclic paths, remove fromNode
		return reachableNodes;
	}
	
	private static void findReachableNodes(final Node currentNode, final Set<Node> reachableNodesSoFar) {
		for (Node reachableNode : currentNode.getToTypes().keySet()) {
			if (reachableNodesSoFar.add(reachableNode)) {
				findReachableNodes(reachableNode, reachableNodesSoFar);
			}
		}
	}
	
	public static void findShortestPathToAllOtherNodesUsingDijkstra(Node startingPoint) {
		startingPoint.setDistance(0);
		
		Set<Node> settledNodes = new HashSet<>();
		Set<Node> unsettledNodes = new HashSet<>();
		unsettledNodes.add(startingPoint);
		
		while (unsettledNodes.size() != 0) {
			Node currentNode = getLowestDistanceNode(unsettledNodes);
			unsettledNodes.remove(currentNode);
			for (Entry<Node, Integer> adjacencyPair : currentNode.getToTypes().entrySet()) {
				Node adjacentNode = adjacencyPair.getKey();
				
				if (!settledNodes.contains(adjacentNode)) {
					Integer edgeWeight = adjacencyPair.getValue();
					calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
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
	
	private static class NodesComparator implements Comparator<List<Node>> {
		
		static final NodesComparator INSTANCE = new NodesComparator();
		
		@Override
		public int compare(List<Node> nodes1, List<Node> nodes2) {
			return sumNodes(nodes1).compareTo(sumNodes(nodes2));
		}
		
		/**
		 * Since given list of nodes represents a connected path, each subsequent nodes will exist in each previous node' toNodes collection.
		 * <p>
		 * We need to calculate the distance this way, since Dijkstra's algorithm mutates the distance property for a single shortes path to a
		 * starting node.
		 */
		private Integer sumNodes(List<Node> nodes) {
			Node currentFromNode = null;
			int nodesCost = 0;
			for (Node nodeInPath : nodes) {
				if (currentFromNode == null) {
					currentFromNode = nodeInPath;
				} else {
					nodesCost += currentFromNode.getToTypes().get(nodeInPath);
				}
			}
			return nodesCost;
		}
	}
}