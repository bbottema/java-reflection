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