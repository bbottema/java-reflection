package org.bbottema.javareflection.util.graph;

import javax.annotation.Nonnull;
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
	public static <T> List<List<Node<T>>> findAllPathsAscending(Node<T> startingPoint, Node<T> destination) {
		List<List<Node<T>>> allPaths = new ArrayList<>();
		findAllPossiblePaths(startingPoint, destination, new ArrayDeque<Node<T>>(), allPaths);
		Collections.sort(allPaths, NodePathComparator.<T>INSTANCE());
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
}
