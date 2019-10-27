package org.bbottema.javareflection.util.graph;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@SuppressFBWarnings(justification = "Generated code")
public class Node<T> {
	@NonNull
	@EqualsAndHashCode.Include
	@ToString.Include
	private final T type;
	private final Map<Node<T>, Integer> toNodes = new HashMap<>();
	private LinkedList<Node<T>> leastExpensivePath = new LinkedList<>();
	private Integer cost = Integer.MAX_VALUE;
}