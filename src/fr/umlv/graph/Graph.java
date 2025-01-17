package fr.umlv.graph;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class Graph {
	private final ArrayList<ArrayList<Edge>> adjacenyList;
	final static int INFINI = 256;
	private final static int SOURCE = 0;
	private final int TARGET;

	/**
	 * The Constructor of the Class Graph.java.
	 * return A Graph with the number of vertices given in parameters. <br>
	 * The source is the vertex 0 and the target is the last vertex.
	 * @param vertexCount the number of vertices in the graph.
	 */
	public Graph(int vertexCount) {
		TARGET = vertexCount - 1;
		adjacenyList = new ArrayList<>(vertexCount);
		
		for (int v = 0; v < vertexCount; v++)
			adjacenyList.add(new ArrayList<>());
	}
	
	/**
	 * The method to have the number of vertex in the graph.
	 * @return the number of vertices in the graph.
	 */
	public int vertices() {
		return adjacenyList.size();
	}
	
	/**
	 * This method add an Edge in the list in the graph.
	 * @param edge the edge you want to add in the graph.
	 */
	public void addEdge(Edge edge) {
		adjacenyList.get(edge.from).add(edge);
		adjacenyList.get(edge.to).add(edge);
	}
	
	/**
	 * 
	 * @param vertex the vertex you want to have all the vertices.
	 * @return the list of the Edge for the vertex given in argument.
	 */
	public Iterable<Edge> adjacent(int vertex) {
		return adjacenyList.get(vertex);
	}
	
	/**
	 * 
	 * @return List of all the edges in the graph.
	 */
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
	 * Convert a 2D tab of interest and create a graph with the tab <br>
	 * and fill the edges with the good interest
	 * @param itr take a 2D tab of interest
	 * @return the graph created with the 2D tab of interest
	 */
	public static Graph toGraph (int [][] itr){
		int i, j;
		int line, col;
		int state;
		
		line = itr.length;
		col = itr[0].length;
		Graph g = new Graph (line * col + 2); // on veut un sommet par pixel donc on calcule le nombre de pixel avec itr et on ajoute les deux sommets s et t
		for (i = 1; i <= line; i++){
			g.addEdge(new Edge(SOURCE, i, INFINI , 0));
			g.addEdge(new Edge (g.TARGET - i, g.TARGET, itr[line - i][(col - 1)], 0));
		}
		
		for (i = 1; i <= line; i++){
			for (j = 1; j <= col; j++){
				state = i + ((j-1) * line);
				// Si je ne suis pas sur le bord droit de l'image alors je peux alors l'arc de (i,j) -> (i,j+1)
				if (col != j)
					g.addEdge(new Edge(state, state + line, itr[i-1][j-1], 0));
				// Si je ne suis pas sur le bord gauche ni sur le bord bas de l'image alors je peux alors l'arc de (i,j) -> (i-1,j-1)
				if (1 != j && 1 != i)
					g.addEdge(new Edge( state, state - line - 1, INFINI, 0));
				
				// Si je ne suis pas sur le bord gauche de l'image alors je peux alors l'arc de (i,j) -> (i,j-1)
				if (1 != j)
					g.addEdge(new Edge( state,state - line, INFINI, 0));
				
				// Si je ne suis pas sur le bord bas de l'image alors je peux alors l'arc de (i,j) -> (i+1,j-1)
				if (line != i && 1 != j)
					g.addEdge(new Edge( state, state - line + 1, INFINI, 0));
			}
		}
		
		return g;		
	}
	
	/**
	 * @param source One vertex.
	 * @param other The other vertex.
	 * @return Returns the edge which links source and other.<br>
	 * Returns <i>null</i> if the edge doesn't exist.
	 */
	private Edge findEdge (int source, int other) {		
		for (Edge edge : adjacent(source)) {
			if ((edge.to == other && edge.getPlusValueEdge(source) != 0) || (edge.from == other && edge.getPlusValueEdge(other) != 0)) {
				return edge;
			}
		}
		
		return null;
	}
	
	/**
	 * Finds a path from the source to the target, by taking the largest edge available.
	 * @return The list of edges of the path.
	 */
	private LinkedList<Edge> findPath () {
		LinkedList<Edge> path = new LinkedList<>();
		int vertMax = vertices();
		boolean visited[] = new boolean [vertMax]; // Init: false
		int father[] = new int[vertMax];
		int next;
		int oth;
		
		Queue<Integer> queue = new PriorityQueue<>();
		int i;
		
		// Init
		for (i = 1 ; i < vertMax ; i++) {
			father[i] = -1;
		}
		
		// Source
		queue.add(SOURCE);
		father[SOURCE] = SOURCE;
		
		while (father[TARGET] == -1 && false == queue.isEmpty()) {
			next = queue.poll();
			visited[next] = true;
			
			for (Edge edge : adjacent(next)) {
				oth = edge.other(next);
				if (edge.getPlusValueEdge(next) != 0 && !visited[oth]) { // Not visited and edge not full
					queue.add(oth);
					father[oth] = next;
				}
			}
		}
		
		if (father[TARGET] != -1) {
			next = TARGET;
			while (next != SOURCE) {
				path.addFirst(findEdge(father[next], next));
				next = father[next];
			}
			
			return path;
		}
		return null; // No path found.
	}
	
	/**
	 * Returns the next non infinite edge, which capacity is not infinite.
	 * @param adjacent : an ArrayList of adjacent edges.
	 * @return A finite Edge.
	 */

	private Edge nextFiniteEdge (int current) {
		return adjacenyList.get(current).stream().filter(e -> e.capacity != INFINI && e.from == current).findFirst().get();
	}
	
	void fillGraph () {
		LinkedList<Edge> path;		
		while (null != (path = findPath())) { // While there is a positive path
			int min = INFINI;
			int last = path.get(0).from;
			Iterator<Edge> it = path.iterator();
			
			// Get the minimum value of the path
			while (it.hasNext()) {
				Edge edge = it.next();
				int plusValue = edge.getPlusValueEdge(last);
				last = edge.other(last);
				
				if (min > plusValue) {
					min = plusValue;
				}
			}
			
			// Change the value of each of the edges of the path.
			last = path.get(0).from;
			
			it = path.iterator();
			while (it.hasNext()) {
				Edge edge = it.next();
				
				Edge.modEdge(last, edge, min);
				
				last = edge.other(last);
			}
		}
	}
	
	/**
	 * Returns a minimal cut of this graph.
	 * @return The list of Edges cut
	 */
	public ArrayList<Edge> minimalCut () {
		ArrayList<Edge> cut = new ArrayList <>();
		boolean visited[] = new boolean[vertices()];
		Queue<Integer> queue = new PriorityQueue<>();
		int verticetmp;
		
		fillGraph();

		// When there is no path found, the minimal cut can be found.
		queue.add(SOURCE);
		visited[SOURCE] = true;
		while (!queue.isEmpty()) {
			verticetmp = queue.poll();
			for (Edge edge : adjacenyList.get(verticetmp)) {
				if (!edge.isMaxed() && edge.from == verticetmp && !visited[edge.to]) {
				/* From a given vertex and an edge, add the next vertex if :
				 * the edge is not full,
				 * the next vertex is not visited
				 * the source of the edge is the current vertex 
				 */
					visited[edge.to] = true;
					queue.add(edge.to);
				}
			}
		}
		
		for (int i = 1 ; i <= adjacenyList.get(SOURCE).size() ; i++) {
			Edge e;
			int current = i;
			
			while (!(e = nextFiniteEdge(current)).isMaxed() || visited[e.to]) { // Arc de S (visité) à T (non visité) plein : arc à ajouter
				current = e.to;				
			}
			
			cut.add(e);
		}
		return cut;
	}

	/**
	 * Create a file of the graph.
	 * @param path the path of the file.
	 * @throws IOException throw an IOException
	 */
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
