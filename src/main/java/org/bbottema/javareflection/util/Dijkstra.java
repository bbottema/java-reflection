package org.bbottema.javareflection.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
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

public class Dijkstra {
	
	@Data
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	@ToString(onlyExplicitlyIncluded = true)
	public static class Node<T> {
		@Nonnull
		@EqualsAndHashCode.Include
		@ToString.Include
		private T type;
		private LinkedList<Node<T>> leastExpensivePath = new LinkedList<>();
		private Integer cost = Integer.MAX_VALUE;
		private Map<Node<T>, Integer> toNodes = new HashMap<>();
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> List<List<Node<T>>> findAllPathsAscending(Node<T> startingPoint, Node<T> destination) {
		List<List<Node<T>>> allPaths = new ArrayList<>();
		findAllPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), allPaths);
		Collections.sort(allPaths, new NodesComparator<T>());
		return allPaths;
	}
	
	@SuppressWarnings({"StatementWithEmptyBody"})
	private static <T> void findAllPossiblePaths(Node<T> currentNode, Node<T> destination, Deque<Node<T>> currentPath, List<List<Node<T>>> possiblePathsSoFar) {
		if (!currentPath.contains(currentNode)) {
			currentPath.addLast(currentNode);
			
			if (currentNode.equals(destination)) {
				if (!currentPath.isEmpty()) {
					possiblePathsSoFar.add(new ArrayList<>(currentPath));
				} else {
					// startingPoint same as destination
				}
			} else {
				for (Node<T> nextNode : currentNode.getToNodes().keySet()) {
					findAllPossiblePaths(nextNode, destination, currentPath, possiblePathsSoFar);
				}
			}
			currentPath.removeLast();
		} else {
			// cyclic path
		}
	}
	
	@Nonnull
	public static <T> Set<Node<T>> findReachableNodes(final Node<T> fromNode) {
		Set<Node<T>> reachableNodes = new HashSet<>();
		findReachableNodes(fromNode, reachableNodes);
		reachableNodes.remove(fromNode); // in case of cyclic paths, remove fromNode
		return reachableNodes;
	}
	
	private static <T> void findReachableNodes(final Node<T> currentNode, final Set<Node<T>> reachableNodesSoFar) {
		for (Node<T> reachableNode : currentNode.getToNodes().keySet()) {
			if (reachableNodesSoFar.add(reachableNode)) {
				findReachableNodes(reachableNode, reachableNodesSoFar);
			}
		}
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> void findShortestPathToAllOtherNodesUsingDijkstra(Node<T> startingPoint) {
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
	
	private static class NodesComparator<T> implements Comparator<List<Node<T>>> {
		
		@Override
		public int compare(List<Node<T>> nodes1, List<Node<T>> nodes2) {
			return sumNodes(nodes1).compareTo(sumNodes(nodes2));
		}
		
		/**
		 * Since given list of nodes represents a connected path, each subsequent nodes will exist in each previous node' toNodes collection.
		 * <p>
		 * We need to calculate the distance this way, since Dijkstra's algorithm mutates the distance property for a single shortes path to a
		 * starting node.
		 */
		private Integer sumNodes(List<Node<T>> nodes) {
			Node<T> currentFromNode = null;
			int nodesCost = 0;
			for (Node<T> nodeInPath : nodes) {
				if (currentFromNode == null) {
					currentFromNode = nodeInPath;
				} else {
					nodesCost += currentFromNode.getToNodes().get(nodeInPath);
				}
			}
			return nodesCost;
		}
	}
}