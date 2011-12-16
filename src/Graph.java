import java.util.Collections;
import java.util.ArrayList;

public class Graph {
	/**
	 * We assume graph is undirected, so adjMatrix[i][j] == adjMatrix[j][i]
	 */
	private int[][] adjMatrix;

	public String toString() {
		String ans = new String();
		for (int i = 0; i < size(); i++) {
			for (int j = i + 1; j < size(); j++) {
				if (adjMatrix[i][j] == 1) {
					ans += i + "->" + j;
				}
			}
		}
		return ans;
	}

	public class Edge {
		int from, to;

		public Edge() {
		}

		public String toString() {
			return from + "->" + to;
		}

		public Edge(int from, int to) {
			this.from = from;
			this.to = to;
		}

		public void setFrom(int f) {
			from = f;
		}

		public Integer from() {
			return from;
		}

		public void setTo(int t) {
			to = t;
		}

		public Integer to() {
			return to();
		}
	}

	public Graph(int n) {
		adjMatrix = new int[n][n];
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

	private boolean dfsCycle(ArrayList<Edge> result, int[] used, int parent,
			int v) {
		used[v] = 1;
		for (int i = 0; i < size(); i++) {
			if (i == parent)
				continue;
			if (adjMatrix[v][i] == 0)
				continue;
			if (used[i] == 0) {
				result.add(new Edge(v, i));
				if (dfsCycle(result, used, v, i)) {
					// if we got an answer, it has already been restored
					return true;
				} else {
					// otherwise, current branch doesn't lead to a predecessor,
					// so there's no cycle
					result.remove(result.size() - 1);
				}
			}
			if (used[i] == 1) {
				result.add(new Edge(v, i));
				// found a cycle, restoring the answer
				ArrayList<Edge> cycle = new ArrayList<Edge>();
				for (int j = 0; j < result.size(); j++) {
					if (result.get(j).from() == i) {
						cycle.addAll(result.subList(j, result.size()));
						result.clear();
						result.addAll(cycle);
						return true;
					}
				}
				// the algorithm should never get here, but anyway…
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
			for (Edge e : cycle)
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

		// 1. searching for one-edge segments
		for (int i = 0; i < size(); i++) {
			for (int j = i + 1; j < size(); j++) {
				if (adjMatrix[i][j] == 1 && !laide[i][j] && laidv[i]
						&& laidv[j]) {
					Graph t = new Graph(size());
					t.addEdge(i, j);
					ans.add(t);
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
		if (size() == 1) { // Singletone graph is the only acyclic biconnected graph
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
}
