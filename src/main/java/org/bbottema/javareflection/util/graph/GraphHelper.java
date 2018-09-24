package org.bbottema.javareflection.util.graph;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@UtilityClass
public final class GraphHelper {
	
	@SuppressWarnings("WeakerAccess")
	public static <T> boolean isPathPossible(Node<T> startingPoint, Node<T> destination) {
		return findPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), new ArrayList<List<Node<T>>>(), true, 4);
	}
	
	@SuppressWarnings("WeakerAccess")
	public static <T> List<List<Node<T>>> findAllPathsAscending(Node<T> startingPoint, Node<T> destination) {
		List<List<Node<T>>> allPaths = new ArrayList<>();
		findPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), allPaths, false, 4);
		Collections.sort(allPaths, NodePathComparator.<T>INSTANCE()); // NodePathComparator needs the startingPoints included in the path
		removeStartingPoints(allPaths);
		return allPaths;
	}
	
	private static <T> void removeStartingPoints(List<List<Node<T>>> allPaths) {
		for (List<Node<T>> path : allPaths) {
			path.remove(0);
		}
	}
	
	@SuppressWarnings({"StatementWithEmptyBody"})
	private static <T> boolean findPossiblePaths(Node<T> currentNode, Node<T> destination, Deque<Node<T>> currentPath,
												 List<List<Node<T>>> possiblePathsSoFar, boolean returnOnFirstPathFound, int cutOffEdgeCount) {
		boolean foundAPath = false;
		
		if (!currentPath.contains(currentNode)) {
			currentPath.addLast(currentNode);
			
			if (currentNode.equals(destination)) {
				if (!currentPath.isEmpty()) {
					possiblePathsSoFar.add(new ArrayList<>(currentPath));
					foundAPath = true;
				} else {
					// startingPoint same as destination
				}
			} else if (currentPath.size() <= cutOffEdgeCount) {
				for (Node<T> nextNode : currentNode.getToNodes().keySet()) {
					foundAPath |= findPossiblePaths(nextNode, destination, currentPath, possiblePathsSoFar, returnOnFirstPathFound, cutOffEdgeCount);
					if (foundAPath && returnOnFirstPathFound) {
						break;
					}
				}
			}
			currentPath.removeLast();
		} else {
			// cyclic path
		}
		
		return foundAPath;
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