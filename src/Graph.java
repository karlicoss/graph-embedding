import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA. User: karlicos Date: 30.09.11 Time: 18:31 To change
 * this template use File | Settings | File Templates.
 */
public class Graph {
	public String toString() {
		String ans = new String();
		ans += "( ";
		for (int i = 0; i < size(); i++) {
			ans += getLabel(i) + " ";
		}
		ans += ") ";
		for (int i = 0; i < size(); i++) {
			for (int j = i + 1; j < size(); j++) {
				if (adjMatrix[i][j] == 1) {
					ans += Integer.toString(getLabel(i)) + "->"
							+ Integer.toString(getLabel(j)) + "; ";
				}
			}
		}
		return ans;
	}

	public ArrayList<Integer> toLabels(ArrayList<Integer> vnumbers) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int v : vnumbers) {
			result.add(label[v]);
		}
		return result;
	}

	public class Edge extends Pair<Integer, Integer> {
		public Edge() {
			super(null, null);
		}

		public String toString() {
			return from() + "->" + to();
		}

		public Edge(Integer from, Integer to) {
			super(from, to);
		}

		public void setFrom(Integer a) {
			setFirst(a);
		}

		public Integer from() {
			return first();
		}

		public void setTo(Integer a) {
			setSecond(a);
		}

		public Integer to() {
			return second();
		}
	}

	/**
	 * We assume graph is undirected, so adjMatrix[i][j] == adjMatrix[j][i]
	 */
	private int[][] adjMatrix;
	private int[] label;

	public Graph(int n) {
		adjMatrix = new int[n][n];
		label = new int[n];
		for (int i = 0; i < n; i++)
			label[i] = i;
	}

	public void setLabel(int v, int l) {
		label[v] = l;
	}

	public int getLabel(int v) {
		return label[v];
	}

	public void addEdge(int f, int t) {
		adjMatrix[f][t] = 1;
		adjMatrix[t][f] = 1;
	}

	public void deleteEdge(int f, int t) {
		adjMatrix[f][t] = 0;
		adjMatrix[t][f] = 0;
	}

	public boolean existsEdge(int f, int t) {
		return adjMatrix[f][t] == 1;
	}

	public int size() {
		return adjMatrix.length;
	}
	
	private boolean dfsCycle(ArrayList<Edge> result, int[] used, int predecessor, int v) {
		used[v] = 1;
		for (int i = 0; i < size(); i++) {
			if (i == predecessor)
				continue;
			if (adjMatrix[v][i] == 0)
				continue;
			if (used[i] == 0) {
				result.add(new Edge(v, i));
				if (dfsCycle(result, used, v, i))
				{
					//if we got an answer, it has already been restored
					return true;
				} else {
					//otherwise, current branch doesn't lead to a predecessor, so there's no cycle
					result.remove(result.size() - 1);
				}
			} if (used[i] == 1) {
				result.add(new Edge(v, i));
				//found a cycle, restoring the answer
				ArrayList<Edge> cycle = new ArrayList<Edge>();
				for (int j = 0; j < result.size(); j++) {
					if (result.get(j).from() == i) {
						cycle.addAll(j, result);
						result = cycle;
						return true;
					}
				}
				//the algorithm should never get here, but anyway…
				return true;
			} 
		}
		used[v] = 2;
		return false;
	} 
	/*
	 * This method assumes graph is connected
	 */
	public ArrayList<Integer> getCycle() {
		ArrayList<Edge> cycle = new ArrayList<Graph.Edge>();
		boolean gotCycle = dfsCycle(cycle, new int[size()], -1, 0);
		if (!gotCycle)
			return null;
		else {
			ArrayList<Integer> answer = new ArrayList<Integer>();
			for (Edge e: cycle)
				answer.add(e.from());
			return answer;
		}
	}

	private void dfsSegments(int[] used, boolean[] laidv, Graph result, int v) {
		used[v] = 1;
		for (int i = 0; i < size(); i++) {
			if (adjMatrix[v][i] == 1) {
				result.addEdge(v, i);
				if (used[i] == 0 && !laidv[i])
					dfsSegments(used, laidv, result, i);
			}
		}
	}

	private ArrayList<Graph> getSegments(boolean[] laidv, boolean[][] laide) {
		ArrayList<Graph> ans = new ArrayList<Graph>();

		int segcount = 0;
		// 1. searching for one-edge segments
		for (int i = 0; i < size(); i++) {
			for (int j = i + 1; j < size(); j++) {
				if (adjMatrix[i][j] == 1 && !laide[i][j] && laidv[i] && laidv[j]) {
					Graph t = new Graph(size());
					t.addEdge(i, j);
					ans.add(t);
					segcount++;
				}
			}
		}
		// 2. Searching for segments containing vertices
		int used[] = new int[size()];
		for (int i = 0; i < size(); i++) {
			if (used[i] == 0 && !laidv[i]) {
				Graph res = new Graph(size());
				dfsSegments(used, laidv, res, i);
				ans.add(res);
				segcount++;
			}
		}

		return ans;
	}

	private void layChain(boolean[][] laide, ArrayList<Integer> chain,
			boolean cyclic) {
		for (int i = 0; i < chain.size() - 1; i++) {
			laide[chain.get(i)][chain.get(i + 1)] = true;
			laide[chain.get(i + 1)][chain.get(i)] = true;
		}
		if (cyclic) {
			laide[chain.get(0)][chain.get(chain.size() - 1)] = true;
			laide[chain.get(chain.size() - 1)][chain.get(0)] = true;
		}
	}

	private boolean containsSegment(final ArrayList<Integer> face,
			final Graph segment, boolean[] laidv) {
		for (int f = 0; f < size(); f++) {
			for (int t = 0; t < size(); t++) {
				if (segment.existsEdge(f, t)) {
					if ((laidv[f] && !face.contains(f))
							|| (laidv[t] && !face.contains(t))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public Pair<ArrayList<ArrayList<Integer>>, ArrayList<Integer>> getEmbedding() {
		if (size() == 1) {
			ArrayList<ArrayList<Integer>> faces = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> face = new ArrayList<Integer>();
			face.add(0);
			faces.add(face);
			faces.add((ArrayList<Integer>) face.clone());
			return new Pair<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>(
					faces, face);
		}

		ArrayList<Integer> c = getCycle();
		if (c.isEmpty())
			return null;
		ArrayList<ArrayList<Integer>> faces = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> outerFace = (ArrayList<Integer>) c.clone();
		faces.add(c);
		faces.add(outerFace);

		boolean[] laidv = new boolean[size()];
		boolean[][] laide = new boolean[size()][size()];
		for (int i : c) {
			laidv[i] = true;
		}
		layChain(laide, c, true);
		while (true) {
			ArrayList<Graph> segments = getSegments(laidv, laide);
			if (segments.size() == 0)
				break;
			int[] count = new int[segments.size()];
			ArrayList<Integer>[] lface = new ArrayList[segments.size()];
			for (int i = 0; i < segments.size(); i++) {
				for (ArrayList<Integer> face : faces) {
					if (containsSegment(face, segments.get(i), laidv)) {
						lface[i] = face;
						count[i]++;
					}
				}
				if (containsSegment(outerFace, segments.get(i), laidv)) {
					lface[i] = outerFace;
					count[i]++;
				}
			}
			int mi = 0; // Searching for the segment which can be embedded in
						// the minimal number of faces
			for (int i = 0; i < segments.size(); i++) {
				if (count[i] < count[mi])
					mi = i;
			}
			if (count[mi] == 0) {
				return null;
			} else {
				// Let's embed the mi'th segment
				// Searchig for a chain and laying it
				ArrayList<Integer> chain = segments.get(mi).getChain(laidv);
				for (int i : chain) {
					laidv[i] = true;
				}
				layChain(laide, chain, false);

				ArrayList<Integer> face = lface[mi], face1 = new ArrayList<Integer>(), face2 = new ArrayList<Integer>();
				int chst = 0, chfn = 0;
				for (int i = 0; i < face.size(); i++) {
					if (face.get(i).equals(chain.get(0))) {
						chst = i;
					}
					if (face.get(i).equals(chain.get(chain.size() - 1))) {
						chfn = i;
					}
				}
				ArrayList<Integer> rchain = (ArrayList<Integer>) chain.clone();
				Collections.reverse(rchain);
				int fsize = face.size();
				if (face != outerFace) {
					if (chst < chfn) {
						face1.addAll(chain);
						for (int i = (chfn + 1) % fsize; i != chst; i = (i + 1)
								% fsize) {
							face1.add(face.get(i));
						}
						face2.addAll(rchain);
						for (int i = (chst + 1) % fsize; i != chfn; i = (i + 1)
								% fsize) {
							face2.add(face.get(i));
						}
					} else {
						face1.addAll(rchain);
						for (int i = (chst + 1) % fsize; i != chfn; i = (i + 1)
								% fsize) {
							face1.add(face.get(i));
						}
						face2.addAll(chain);
						for (int i = (chfn + 1) % fsize; i != chst; i = (i + 1)
								% fsize) {
							face2.add(face.get(i));
						}
					}
					faces.remove(face);
					faces.add(face1);
					faces.add(face2);
				} else {
					ArrayList<Integer> newOuterFace = new ArrayList<Integer>();
					if (chst < chfn) {
						newOuterFace.addAll(chain);
						for (int i = (chfn + 1) % fsize; i != chst; i = (i + 1)
								% fsize) {
							newOuterFace.add(face.get(i));
						}
						face2.addAll(chain);
						for (int i = (chfn - 1 + fsize) % fsize; i != chst; i = (i - 1 + fsize)
								% fsize) {
							face2.add(face.get(i));
						}
					} else {
						newOuterFace.addAll(rchain);
						for (int i = (chst + 1) % fsize; i != chfn; i = (i + 1)
								% fsize) {
							newOuterFace.add(face.get(i));
						}
						face2.addAll(rchain);
						for (int i = (chst - 1 + fsize) % fsize; i != chfn; i = (i - 1 + fsize)
								% fsize) {
							face2.add(face.get(i));
						}
					}
					faces.remove(outerFace);
					faces.add(newOuterFace);
					faces.add(face2);
					outerFace = newOuterFace;
				}
			}
		}
		return new Pair<ArrayList<ArrayList<Integer>>, ArrayList<Integer>>(
				faces, outerFace);
	}

	private void removeCycle(ArrayList<Integer> cycle) {
		for (int i = 0; i < cycle.size(); i++) {
			deleteEdge(cycle.get(i), cycle.get((i + 1) % cycle.size()));
		}
	}

	private void dfsChain(int used[], boolean[] laidv,
			ArrayList<Integer> chain, int v) {
		used[v] = 1;
		chain.add(v);
		for (int i = 0; i < size(); i++) {
			if (adjMatrix[v][i] == 1 && used[i] == 0) {
				if (!laidv[i]) {
					dfsChain(used, laidv, chain, i);
				} else {
					chain.add(i);
				}
				return;
			}
		}
	}

	private ArrayList<Integer> getChain(boolean[] laidv) {
		ArrayList<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < size(); i++) {
			if (laidv[i]) {
				boolean ingraph = false;
				for (int j = 0; j < size(); j++) {
					if (existsEdge(i, j))
						ingraph = true;
				}
				if (ingraph) {
					dfsChain(new int[size()], laidv, result, i);
					break;
				}
			}
		}
		return result;
	}

	private void dfsCCD(boolean[] used, ArrayList<Integer> result, int v) {
		used[v] = true;
		result.add(v);
		for (int i = 0; i < size(); i++) {
			if (adjMatrix[v][i] == 1 && !used[i])
				dfsCCD(used, result, i);
		}
	}

	public ArrayList<Graph> getConnectedComponentsDecomposition() {
		ArrayList<Graph> result = new ArrayList<Graph>();
		boolean[] used = new boolean[size()];
		for (int i = 0; i < size(); i++) {
			if (!used[i]) {
				ArrayList<Integer> ng = new ArrayList<Integer>();
				dfsCCD(used, ng, i);
				Graph g = new Graph(ng.size());
				for (int j = 0; j < ng.size(); j++) {
					g.setLabel(j, getLabel(ng.get(j)));
				}
				for (int f = 0; f < ng.size(); f++) {
					for (int t = 0; t < ng.size(); t++) {
						if (existsEdge(ng.get(f), ng.get(t)))
							g.addEdge(f, t);
					}
				}
				result.add(g);
			}
		}
		return result;
	}

	private void dfsBD(MutableInteger time, int[] enter, int[] ret, int p, int v) {
		time.increment();
		enter[v] = time.getValue();
		ret[v] = enter[v];
		for (int i = 0; i < size(); i++) {
			if (existsEdge(v, i)) {
				if (enter[i] == 0) {
					dfsBD(time, enter, ret, v, i);
					ret[v] = Math.min(ret[v], ret[i]);
				} else {
					if (i != p) {
						ret[v] = Math.min(ret[v], enter[i]);
					}
				}
			}
		}
	}

	private void dfsPaintBD(int[] enter, int[] ret, int[] colors, int curColor,
			MutableInteger maxColor, ArrayList<Edge> bridges, int v) {
		colors[v] = curColor;
		for (int i = 0; i < size(); i++) {
			if (existsEdge(v, i)) {
				if (colors[i] == 0) {
					if (enter[i] == ret[i]) {
						bridges.add(new Edge(v, i));
						maxColor.increment();
						dfsPaintBD(enter, ret, colors, maxColor.getValue(),
								maxColor, bridges, i);
					} else {
						dfsPaintBD(enter, ret, colors, curColor, maxColor,
								bridges, i);
					}
				}
			}
		}
	}

	public Pair<ArrayList<Graph>, ArrayList<Edge>> getBridgesDecomposition() {
		ArrayList<Graph> edgeConnectedComponents = new ArrayList<Graph>();
		ArrayList<Edge> bridges = new ArrayList<Edge>();
		if (size() == 1) {
			edgeConnectedComponents.add(this); // TODO: clone
			return new Pair<ArrayList<Graph>, ArrayList<Edge>>(
					edgeConnectedComponents, bridges);
		}

		int[] enter = new int[size()];
		int[] ret = new int[size()];
		MutableInteger time = new MutableInteger(0);
		for (int i = 0; i < size(); i++) {
			if (enter[i] == 0) {
				dfsBD(time, enter, ret, -1, i);
			}
		}

		int[] colors = new int[size()];
		MutableInteger maxColor = new MutableInteger(0);
		for (int i = 0; i < size(); i++) {
			if (colors[i] == 0) {
				maxColor.increment();
				dfsPaintBD(enter, ret, colors, maxColor.getValue(), maxColor,
						bridges, i);
			}
		}

		// Reassigning bridges' labels
		for (Edge bridge : bridges) {
			bridge.setFrom(getLabel(bridge.from()));
			bridge.setTo(getLabel(bridge.to()));
		}

		int colorsCount = maxColor.getValue();
		ArrayList<Integer>[] comp = new ArrayList[colorsCount];
		for (int i = 0; i < colorsCount; i++)
			comp[i] = new ArrayList<Integer>();
		for (int i = 0; i < size(); i++) {
			comp[colors[i] - 1].add(i);
		}
		for (int i = 0; i < colorsCount; i++) {
			Graph g = new Graph(comp[i].size());
			for (int j = 0; j < comp[i].size(); j++) {
				g.setLabel(j, getLabel(comp[i].get(j)));
			}
			for (int j = 0; j < comp[i].size(); j++) {
				for (int k = 0; k < comp[i].size(); k++) {
					if (existsEdge(comp[i].get(j), comp[i].get(k)))
						g.addEdge(j, k);
				}
			}
			edgeConnectedComponents.add(g);
		}
		return new Pair<ArrayList<Graph>, ArrayList<Edge>>(
				edgeConnectedComponents, bridges);
	}

	private void dfsCPD(int[] used, int[] enter, int[] ret,
			MutableInteger time, int p, int v) {
		time.increment();
		enter[v] = time.getValue();
		ret[v] = enter[v];
		used[v] = 1;
		for (int i = 0; i < size(); i++) {
			if (existsEdge(v, i)) {
				if (i == p)
					continue;
				if (used[i] == 1) {
					ret[v] = Math.min(ret[v], enter[i]);
				} else {
					dfsCPD(used, enter, ret, time, v, i);
					ret[v] = Math.min(ret[v], ret[i]);
				}
			}
		}
	}

	public void dfsPaintCPD(int[] used, int[] enter, int[] ret, int curColor,
			MutableInteger maxColor, int[][] color, boolean[] isCutPoint,
			int p, int v) {
		used[v] = 1;
		int children = 0;// for root
		for (int i = 0; i < size(); i++) {
			if (existsEdge(v, i)) {
				if (i == p)
					continue;
				if (used[i] == 0) {
					children++;
					if (ret[i] >= enter[v]) {
						maxColor.increment();
						color[v][i] = maxColor.getValue();
						if (p != -1)
							isCutPoint[v] = true;
						dfsPaintCPD(used, enter, ret, maxColor.getValue(),
								maxColor, color, isCutPoint, v, i);
					} else {
						color[v][i] = curColor;
						dfsPaintCPD(used, enter, ret, curColor, maxColor,
								color, isCutPoint, v, i);
					}
				} else {
					if (enter[i] <= enter[v]) {
						color[v][i] = curColor;
					}
				}
			}
		}
		if (p == -1 && children > 1)
			isCutPoint[v] = true;
	}

	public Pair<ArrayList<Graph>, ArrayList<Integer>> getCutPointsDecomposition() {
		ArrayList<Graph> vertexConnectedComponent = new ArrayList<Graph>();
		ArrayList<Integer> cutPoints = new ArrayList<Integer>();
		if (size() == 1) {
			vertexConnectedComponent.add(this);// TODO: clone!
			return new Pair<ArrayList<Graph>, ArrayList<Integer>>(
					vertexConnectedComponent, cutPoints);
		}

		int[] used = new int[size()];
		int[] enter = new int[size()];
		int[] ret = new int[size()];

		MutableInteger time = new MutableInteger(0);
		for (int i = 0; i < size(); i++) {
			if (used[i] == 0) {
				dfsCPD(used, enter, ret, time, -1, i);
			}
		}

		MutableInteger maxColor = new MutableInteger(0);
		used = new int[size()];
		int[][] color = new int[size()][size()];
		boolean[] isCutPoint = new boolean[size()];

		for (int i = 0; i < size(); i++) {
			if (used[i] == 0) {
				dfsPaintCPD(used, enter, ret, 0, maxColor, color, isCutPoint,
						-1, i);
			}
		}

		for (int i = 0; i < size(); i++) {
			if (isCutPoint[i])
				cutPoints.add(getLabel(i));
		}

		HashSet<Integer>[] comp = new HashSet[maxColor.getValue()];
		for (int i = 0; i < comp.length; i++)
			comp[i] = new HashSet<Integer>();
		for (int i = 0; i < size(); i++) {
			for (int j = 0; j < size(); j++) {
				if (color[i][j] != 0) {
					comp[color[i][j] - 1].add(i);
					comp[color[i][j] - 1].add(j);
				}
			}
		}
		for (HashSet<Integer> hs : comp) {
			Object[] obj = hs.toArray();
			int[] vertices = new int[obj.length];
			for (int i = 0; i < obj.length; i++)
				vertices[i] = (Integer) obj[i];
			Graph g = new Graph(hs.size());
			for (int i = 0; i < vertices.length; i++) {
				g.setLabel(i, getLabel(vertices[i]));
			}
			for (int i = 0; i < vertices.length; i++) {
				for (int j = 0; j < vertices.length; j++) {
					if (existsEdge(vertices[i], vertices[j]))
						g.addEdge(i, j);
				}
			}
			vertexConnectedComponent.add(g);
		}
		return new Pair<ArrayList<Graph>, ArrayList<Integer>>(
				vertexConnectedComponent, cutPoints);
	}
}
