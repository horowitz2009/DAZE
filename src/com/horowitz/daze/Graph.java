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

    List<T> prioritize(List<T> neighbors);
  }

  public Graph(Set<T> explored) {
    super();
    _explored = explored;
  }

  public Graph() {
    super();
    _explored = new HashSet<T>();
  }

  // Alternatively, use a Multimap:
  // http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multimap.html
  private Map<T, List<T>> edges = new HashMap<T, List<T>>();
  private Set<T> _explored;
  private boolean _interrupt = false;

  public void addEdge(T src, T dest) {
    List<T> srcNeighbors = this.edges.get(src);
    if (srcNeighbors == null) {
      this.edges.put(src, srcNeighbors = new ArrayList<T>());
    }
    srcNeighbors.add(dest);
  }

  public List<T> getNeighbors(T vertex) {
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
    preOrderTraversalRecursive(vertex, visitor);
  }

  private void preOrderTraversalRecursive(T vertex, Visitor<T> visitor) throws Exception {
    if (!_interrupt) {
      boolean fine = visitor.visit(vertex);
      _explored.add(vertex);
      if (fine) {
        List<T> neighbors = this.getNeighbors(vertex);
        List<T> prioritizedNeighbors = visitor.prioritize(neighbors);
        for (T neighbor : prioritizedNeighbors) {
          // if neighbor has not been visited then recurse
          if (canBeVisited(neighbor, visitor)) {
            preOrderTraversalRecursive(neighbor, visitor);
          }
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
        List<T> neighbors = this.getNeighbors(vertex);
        // just try all neighbors (unknown, green, red???)
        // sort them, so green go first
        for (T neighbor : neighbors) {
          if (canBeVisited(neighbor, visitor)) {// if not yet visited or not an
                                                // obstacle
            queue.add(neighbor);
            _explored.add(neighbor);// mark the new node visited, but it is
                                    // still not actually visited
          }
        }
      }
    }
  }

  public boolean canBeVisited(T vertex, Visitor<T> visitor) {
    // if vertex is unknown, not yet visited and not an obstacle or gate
    return !_explored.contains(vertex) || (_explored.contains(vertex) && visitor.canBeVisited(vertex));
  }

  public Set<T> getExplored() {
    return _explored;
  }

  public void setExplored(Set<T> explored) {
    _explored = explored;
  }
  
  public void interrupt() {
    _interrupt = true;
  }

}