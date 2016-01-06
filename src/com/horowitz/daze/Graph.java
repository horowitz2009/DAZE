package com.horowitz.daze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Graph<T> {

	public static interface Visitor<T> {
		boolean visit(T vertex) throws Exception;

		boolean canBeVisited(T neighbor);
	}

	// Alternatively, use a Multimap:
	// http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multimap.html
	private Map<T, List<T>> edges = new HashMap<T, List<T>>();
	Set<T> _explored;// TODO visibility and so

	public void addEdge(T src, T dest) {
		List<T> srcNeighbors = this.edges.get(src);
		if (srcNeighbors == null) {
			this.edges.put(src, srcNeighbors = new ArrayList<T>());
		}
		srcNeighbors.add(dest);
	}

	public Iterable<T> getNeighbors(T vertex) {
		List<T> neighbors = this.edges.get(vertex);
		if (neighbors == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(neighbors);
		}
	}

	public void addExplored(T vertex) {
		_explored.add(vertex);
	}

	public void preOrderTraversal(T vertex, Visitor<T> visitor) throws Exception {
		_explored = new HashSet<T>();
		preOrderTraversal(vertex, visitor, _explored);
	}

	private void preOrderTraversal(T vertex, Visitor<T> visitor, Set<T> visited) throws Exception {
		boolean fine = visitor.visit(vertex);
		visited.add(vertex);
		if (fine) {
			for (T neighbor : this.getNeighbors(vertex)) {
				// if neighbor has not been visited then recurse
				if (canBeVisited(neighbor, visitor)) {
					preOrderTraversal(neighbor, visitor, visited);
				}
			}
		}
	}

	public void breadthFirstTraversal(T vertex, Visitor<T> visitor) throws Exception {
		_explored = new HashSet<T>();
		Queue<T> queue = new LinkedList<T>();

		queue.add(vertex); // Adds to end of queue
		_explored.add(vertex);

		while (!queue.isEmpty()) {
			// removes from front of queue
			vertex = queue.remove();
			boolean fine = visitor.visit(vertex);
			if (fine) {
				// Visit child first before grandchild
				Iterable<T> neighbors = this.getNeighbors(vertex);
				// just try all neighbors (unknown, green, red???)
				// sort them, so green go first
				for (T neighbor : neighbors) {
					if (canBeVisited(neighbor, visitor)) {// if not yet visited or not an obstacle
						queue.add(neighbor);
						_explored.add(neighbor);// mark the new node visited, but it is still not actually visited
					}
				}
			}
		}
	}

	public boolean canBeVisited(T vertex, Visitor<T> visitor) {
		// if vertex is unknown, not yet visited and not an obstacle or gate
		return !_explored.contains(vertex) || (_explored.contains(vertex) && visitor.canBeVisited(vertex));
	}
	
}