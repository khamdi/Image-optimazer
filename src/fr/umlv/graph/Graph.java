package fr.umlv.graph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class Graph {
	private final ArrayList<ArrayList<Edge>> adjacenyList;
	private final static int INFINI = 256;
	private final static int SOURCE = 0;
	private final static int TARGET = 1;
	
	public Graph(int vertexCount) {
		adjacenyList = new ArrayList<>(vertexCount);
		for (int v = 0; v < vertexCount; v++)
			adjacenyList.add(new ArrayList<>());
	}
	
	public int vertices() {
		return adjacenyList.size();
	}
	
	public void addEdge(Edge edge) {
		adjacenyList.get(edge.from).add(edge);
		adjacenyList.get(edge.to).add(edge);
	}
	
	public Iterable<Edge> adjacent(int vertex) {
		return adjacenyList.get(vertex);
	}
	
	public Iterable<Edge> edges() {
		ArrayList<Edge> list = new ArrayList<>();
		for (int vertex = 0; vertex < vertices(); vertex++) {
			for (Edge edge : adjacent(vertex)) {
				if (edge.to != vertex)
					list.add(edge);
			}
		}
		return list;
	}
	
	/**
	 * Find the next edge to take, by choosing the one with :<br>
	 * <li>the greatest capacity, if it's an edge from <b>current</b><br>
	 * <li>the most used capacity, if it's an edge to <b>current</b><br>
	 * <li>any other possible edge otherwise<br>
	 * <br>
	 * Can return <i>null</i> if there is no edge available.<br>
	 * <br>
	 * @param current : The current vertice
	 * @param visited : The table of the visited vertices, or currently in the constructed path.
	 * @param list : The list of edges adjacent to the current vertice.
	 * @return The next edge of the path
	 */
	private Edge findNextNonFullEdge (int current, boolean visited[], ArrayList<Edge> list) {
		Edge max = null;		// Index of the element that has the max augment value
		int val = 0; 			// Max augment value
		
		visited[current] = true;
		
		for (int i = 1 ; i < list.size() ; i++) {
			Edge e = list.get(i);
			
			if (e.from == current) { // Edge that has current as its origin
				if (!visited[e.to]) { // Destination already visited ?
					int augmentValue;
					if (INFINI != e.capacity &&	((augmentValue = (e.capacity - e.used)) > 0)) { // Edge not full
						if (val < augmentValue) {
							max = e;
							val = augmentValue;
						}
					}
				}
			}
			else { // Other types of edge
				if (!visited[e.from])
					if (val < e.used) { // Is there something to take off ?
						max = e;
						val = e.used;
					}
			}
		}
		
		if (null == max) {
			Optional<Edge> tmp = list	.stream()
										.filter(e -> (e.from == current && !visited[e.to] && INFINI == e.capacity))
										.findFirst();
			
			return tmp.orElse(null);
		}
			
		return max;
	}
	
	/**
	 * Finds a path from the source to the target, by taking the largest edge available.
	 * @return The list of edges of the path.
	 */
	private ArrayList<Edge> findPath () {
		ArrayList<Edge> path = new ArrayList<>();
		int vertMax = vertices() - 2;
		boolean visited[] = new boolean [vertMax];
		Edge nextEdge = null;
		int nextVertice;
		
		// Choose first element
		ArrayList<Edge> list = new ArrayList<>();
		for (Edge edge : adjacent(SOURCE)) { // For each left vertice
			Edge to = adjacenyList.get(edge.to).stream().filter(x -> edge.to == x.from).findFirst().get();
			
			list.add(to);
		}
		
		Edge start = list.stream().reduce(list.get(0), Edge::max);
		if (start.capacity == start.used)
			return null; // No path available
		nextVertice = start.to;
		path.add(start);
		
		for (int i = 0 ; i < vertMax && (null != (nextEdge = findNextNonFullEdge(nextVertice, visited, adjacenyList.get(nextVertice)))) && nextVertice != TARGET ; i++ , path.add(nextEdge));
		//	Find next edge :
		//		if [not null] and [next vertice is not the target],
		// 		then [add next edge in the path] and proceed.
		
		if (null == nextEdge) // No path available
			return null;
		return path;
	}
	
	/**
	 * Modify the used capacity of the <b>edge</b> by adding or subtracting <b>value</b> from it,
	 * whether the <b>current</b> vertice is the source of this edge.<br>
	 * <br>
	 * It is <u>added</u> when the current vertice is the source, <u>subtracted</u> otherwise.
	 * 
	 * @param current : The current vertice
	 * @param mod : The modified Edge
	 * @param value : The value applied
	 */
	private static void modEdge (int current, Edge mod, int value) {
		mod.setUsed(mod.getUsed() + ((mod.from == current) ? value : -value));
	}
	
//	public int[] minimalCut () {
//		ArrayList<Edge> path;
//		while (null != (path = findPath())) { // While there is a positive path
//			int min =
//		}
//	}
	
	public void writeFile(Path path) throws IOException {
		try(BufferedWriter writer = Files.newBufferedWriter(path);
			PrintWriter printer = new PrintWriter(writer)) {
			
			printer.println("digraph G{");
			
			for (Edge e : edges())
				printer.println(e.from + "->" + e.to + "[label=\"" + e.used + "/" + e.capacity + "\"];");
			
			printer.println("}");
		}
	}
}
