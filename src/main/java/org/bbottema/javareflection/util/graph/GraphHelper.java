package org.bbottema.javareflection.util.graph;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GraphHelper {
	
	private GraphHelper() {
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> boolean isPathPossible(Node<T> startingPoint, Node<T> destination) {
		return findPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), new ArrayList<List<Node<T>>>(), true, 4);
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> List<List<Node<T>>> findAllPathsAscending(Node<T> startingPoint, Node<T> destination) {
		List<List<Node<T>>> allPaths = new ArrayList<>();
		findPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), allPaths, false, 4);
		Collections.sort(allPaths, NodePathComparator.<T>INSTANCE());
		return allPaths;
	}
	
	@SuppressWarnings({"StatementWithEmptyBody"})
	private static <T> boolean findPossiblePaths(Node<T> currentNode, Node<T> destination, Deque<Node<T>> currentPath,
												 List<List<Node<T>>> possiblePathsSoFar,
												 boolean returnOnFirstPathFound,
												 int cutOffEdgeCount) {
		boolean foundAPatch = false;
		
		if (!currentPath.contains(currentNode)) {
			currentPath.addLast(currentNode);
			
			if (currentNode.equals(destination)) {
				if (!currentPath.isEmpty()) {
					possiblePathsSoFar.add(new ArrayList<>(currentPath).subList(1, currentPath.size()));
					foundAPatch = true;
				} else {
					// startingPoint same as destination
				}
			} else if (currentPath.size() <= cutOffEdgeCount) {
				for (Node<T> nextNode : currentNode.getToNodes().keySet()) {
					boolean	currentPathValid = findPossiblePaths(nextNode, destination, currentPath, possiblePathsSoFar, returnOnFirstPathFound, cutOffEdgeCount);
					foundAPatch = foundAPatch || currentPathValid;
					if (foundAPatch && returnOnFirstPathFound) {
						break;
					}
				}
			}
			currentPath.removeLast();
		} else {
			// cyclic path
		}
		return foundAPatch;
	}
	
	
	@NotNull
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
}
