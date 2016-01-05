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
    void visit(T vertex);
  }

  // Alternatively, use a Multimap:
  // http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multimap.html
  private Map<T, List<T>> edges = new HashMap<T, List<T>>();
  private Set<T> _visited;

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

  public void preOrderTraversal(T vertex, Visitor<T> visitor) {
    preOrderTraversal(vertex, visitor, new HashSet<T>());
  }

  private void preOrderTraversal(T vertex, Visitor<T> visitor, Set<T> visited) {
    visitor.visit(vertex);
    visited.add(vertex);

    for (T neighbor : this.getNeighbors(vertex)) {
      // if neighbor has not been visited then recurse
      if (!visited.contains(neighbor)) {
        preOrderTraversal(neighbor, visitor, visited);
      }
    }
  }

  public void breadthFirstTraversal(T vertex, Visitor<T> visitor) {
    _visited = new HashSet<T>();
    Queue<T> queue = new LinkedList<T>();

    queue.add(vertex); // Adds to end of queue
    _visited.add(vertex);

    while (!queue.isEmpty()) {
      // removes from front of queue
      vertex = queue.remove();
      visitor.visit(vertex);

      // Visit child first before grandchild
      Iterable<T> neighbors = this.getNeighbors(vertex);
      //just try all neighbors (unknown, green, red???)
      //sort them, so green go first
      for (T neighbor : neighbors) {
        if (canBeVisited(neighbor)) {//if not yet visited or not an obstacle 
          queue.add(neighbor);
          _visited.add(neighbor);//mark the new node visited, but it is still not actually visited
        }
      }
    }
  }
  
  public boolean canBeVisited(T vertex) {
    //if vertex is unknown, not yet visited and not an obstacle or gate
    return !_visited.contains(vertex);
  }

}