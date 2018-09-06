package org.bbottema.javareflection.util.graph;

import java.util.Comparator;
import java.util.List;

class NodePathComparator<T> implements Comparator<List<Node<T>>> {
	
	private static final NodePathComparator<?> INSTANCE = new NodePathComparator<>();
	
	@SuppressWarnings("unchecked")
	static <T> NodePathComparator<T> INSTANCE() {
		return (NodePathComparator<T>) INSTANCE;
	}
	
	private NodePathComparator() {
	}
	
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
