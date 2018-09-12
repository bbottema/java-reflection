package org.bbottema.javareflection.util.graph;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@SuppressFBWarnings(justification = "Generated code")
public class Node<T> {
	@Nonnull
	@EqualsAndHashCode.Include
	@ToString.Include
	private T type;
	private LinkedList<Node<T>> leastExpensivePath = new LinkedList<>();
	private Integer cost = Integer.MAX_VALUE;
	private Map<Node<T>, Integer> toNodes = new HashMap<>();
}