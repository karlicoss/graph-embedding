import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA. User: karlicos Date: 30.09.11 Time: 19:09 To change
 * this template use File | Settings | File Templates.
 */
public class GraphEmbedder {
	public static void main(String[] args) throws IOException {
		new GraphEmbedder().run();
	}

	public Graph readGraph() throws IOException {
		String inf = "input.txt";
		Scanner sc = new Scanner(new File(inf));

		int n = sc.nextInt();
		Graph g = new Graph(n);

		int m = sc.nextInt();
		for (int i = 0; i < m; i++) {
			int f = sc.nextInt();
			int t = sc.nextInt();
			g.addEdge(f, t);
		}
		return g;
	}

	public void run() throws IOException {
		Graph g = readGraph();
		g.getCutPointsDecomposition();
		// 1. Dividing graph in connected components
		ArrayList<Graph> connectedComponentsDecomposition = g
				.getConnectedComponentsDecomposition();
		System.out.println("The graph has "
				+ connectedComponentsDecomposition.size()
				+ " connected components");
		for (int cCi = 0; cCi < connectedComponentsDecomposition.size(); cCi++) {
			Graph curCC = connectedComponentsDecomposition.get(cCi);
			System.out.println("The " + (cCi + 1)
					+ "th connected component's (" + curCC + ") layout:");

			// 2. Dividing graph in edge biconnected components
			Pair<ArrayList<Graph>, ArrayList<Graph.Edge>> bridgesDecomposition = curCC
					.getBridgesDecomposition();
			ArrayList<Graph> edgeBC = bridgesDecomposition.first();
			ArrayList<Graph.Edge> bridges = bridgesDecomposition.second();
			System.out.println("The connected component contains "
					+ edgeBC.size() + " edge biconnected components and "
					+ (bridges.size() == 0 ? "no" : bridges.size())
					+ " bridges");
			if (bridges.size() > 0) {
				System.out
						.println("They should be laid separately and then connected with the bridges(it's trivial)");
				System.out.println("The bridges are:");
				for (Graph.Edge bridge : bridges) {
					System.out.print(bridge + " ");
				}
				System.out.println();
			}

			for (int eBCi = 0; eBCi < edgeBC.size(); eBCi++) {
				Graph curEdgeBC = edgeBC.get(eBCi);
				System.out.println("The " + (eBCi + 1)
						+ "th edge biconnected component's (" + curEdgeBC
						+ " ) layout:");
				// 3. Dividing graph into vertex biconnected components
				Pair<ArrayList<Graph>, ArrayList<Integer>> cutPointsDecomposition = curEdgeBC
						.getCutPointsDecomposition();
				ArrayList<Graph> vertexBC = cutPointsDecomposition.first();
				ArrayList<Integer> cutPoints = cutPointsDecomposition
						.second();
				System.out.println("The edge biconnected component contains "
						+ vertexBC.size()
						+ " vertex biconnected components and "
						+ (cutPoints.size() == 0 ? "no" : cutPoints.size())
						+ " cut points");
				if (cutPoints.size() > 0) {
					System.out
							.println("They should be laid separately and then connected by the cut points(it's trivial)");
					System.out.println("The cut points are:");
					for (int cp : cutPoints) {
						System.out.print(cp + " ");
					}
					System.out.println();
				}

				for (int vBCi = 0; vBCi < vertexBC.size(); vBCi++) {
					Graph curVertexBC = vertexBC.get(vBCi);
					System.out.println("The " + (vBCi + 1)
							+ "th vertex biconnected component's ("
							+ curVertexBC + ") layout:");

					Pair<ArrayList<ArrayList<Integer>>, ArrayList<Integer>> embedding = curVertexBC
							.getEmbedding();
					if (embedding == null) {
						System.out
								.println("The vertex biconnected connected component is NOT planar, so the entire graph too");
					} else {
						System.out
								.println("The vertex biconnected connected component is planar");
						System.out.println("The faces are:");
						for (ArrayList<Integer> face : embedding.first()) {
							if (face != embedding.second()) {
								System.out.println(curVertexBC.toLabels(face));
							}
						}
						System.out.println("The outer face is:");
						System.out.println(curVertexBC.toLabels(embedding
								.second()));
					}
				}
			}
			System.out.println();
		}
	}
}
