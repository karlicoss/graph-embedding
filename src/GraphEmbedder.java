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
		Pair<ArrayList<ArrayList<Integer>>, ArrayList<Integer>> embedding = g
				.getEmbedding();
		if (embedding == null) {
			System.out.println("The graph is not planar");
		} else {
			System.out.println("The graph is planar");
			System.out.println("The faces are:");
			for (ArrayList<Integer> face : embedding.first()) {
				if (face != embedding.second()) {
					System.out.println(g.toLabels(face));
				}
			}
			System.out.println("The outer face is:");
			System.out.println(g.toLabels(embedding.second()));
		}
		System.out.println();
	}
}
