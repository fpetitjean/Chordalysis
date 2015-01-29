/*******************************************************************************
 * Copyright (C) 2015 Francois Petitjean
 * 
 * This file is part of Chordalysis.
 * 
 * Chordalysis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * Chordalysis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Chordalysis.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import stats.MyPriorityQueue;

/**
 * This class represents a chordal graph, i.e., a triangulated graph
 */
public class ChordalGraph extends SimpleGraph<Integer, DefaultEdge> implements UndirectedGraph<Integer, DefaultEdge> {

	public SimpleGraph<BitSet, CliqueGraphEdge> cg;
	CliqueGraphEdge[][] eligibleEdges = null;
	ArrayList<Integer> peo = null;

	/**
	 * Used to store the cliques and separators computed with BFS
	 */
	ArrayList<BitSet> cliques = null, separators = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1106115079865694550L;

	public ChordalGraph() {
		super(DefaultEdge.class);
		cg = new SimpleGraph<BitSet, CliqueGraphEdge>(CliqueGraphEdgeFactory.getInstance());
	}

	public boolean isEdgeRemovable(Integer vertex1, Integer vertex2) {
		if (containsEdge(vertex1, vertex2) || containsEdge(vertex2, vertex1)) {
			int numberCliquesContainingBoth = 0;
			for (BitSet clique : cg.vertexSet()) {
				if (clique.get(vertex1) && clique.get(vertex2)) {
					numberCliquesContainingBoth++;
					if (numberCliquesContainingBoth > 1) {
						break;
					}
				}
			}

			// keeping chordality requires the two vertices to be part
			// of one clique only
			return (numberCliquesContainingBoth == 1);
		} else {
			return false;
		}
	}

	public void initStructures() {
		if (areStructuresInitialised()) return;
		int nbVertices = vertexSet().size();
		eligibleEdges = new CliqueGraphEdge[nbVertices][nbVertices];
		for (int i = 0; i < nbVertices - 1; i++) {
			BitSet Ci = new BitSet(nbVertices);
			Ci.set(i);
			for (int j = i + 1; j < nbVertices; j++) {
				BitSet Cj = new BitSet();
				Cj.set(j);
				eligibleEdges[i][j] = new CliqueGraphEdge(Ci, Cj);
				eligibleEdges[j][i] = eligibleEdges[i][j];
			}
		}

		for (Integer v : vertexSet()) {
			BitSet clique = new BitSet();
			clique.set(v);
			cg.addVertex(clique);
		}

	}

	private boolean areStructuresInitialised() {
		return (eligibleEdges != null);
	}

	public boolean isEdgeAddable(Integer vertex1, Integer vertex2) {
		if (!areStructuresInitialised()) {
			initStructures();
			// System.out.println("structure initialised");
		}
		if (containsEdge(vertex1, vertex2) || containsEdge(vertex2, vertex1)) {
			return false;
		} else {
			return (eligibleEdges[vertex1][vertex2] != null);
		}
	}

	@Override
	public boolean addVertex(Integer v) {
		if (areStructuresInitialised()) {
			eligibleEdges = null;
		}
		return super.addVertex(v);
	}

	@Override
	public DefaultEdge addEdge(Integer vertex1, Integer vertex2) {
		if (isEdgeAddable(vertex1, vertex2)) {
			return addSecuredEdge(vertex1, vertex2);
		} else {
			System.err.println("Can't add edge (" + vertex1 + "," + vertex2 + "), wouldn't give a chordal graph");
			return null;
		}
	}
	
	/**
	 * To use with extreme caution, and only if the structure is not modified afterwards
	 * @param vertex1
	 * @param vertex2
	 * @return
	 */
	public DefaultEdge addEdgeEvenIfNotChordal(Integer sourceVertex, Integer targetVertex) {
		separators = null;
		cliques = null;
		return super.addEdge(sourceVertex, targetVertex);
	}

	@Override
	public DefaultEdge removeEdge(Integer vertex1, Integer vertex2) {
		if (isEdgeRemovable(vertex1, vertex2)) {
			return removeSecuredEdge(vertex1, vertex2);
		} else {
			return null;
		}
	}

	public DefaultEdge addSecuredEdge(Integer a, Integer b) {
		return addSecuredEdge(a, b, null, false);
	}

	public DefaultEdge addSecuredEdge(Integer a, Integer b, MyPriorityQueue pq) {
		return addSecuredEdge(a, b, pq, false);
	}

	public BitSet getSeparator(Integer a, Integer b) {
		if (eligibleEdges[a][b] == null) {
			return null;
		}
		return (BitSet) eligibleEdges[a][b].separator.clone();
	}

	public DefaultEdge removeSecuredEdge(Integer v1, Integer v2) {
		System.err.println("removing an edge in the chordal graph is not implemented yet");

		return null;
	}

	/**
	 * @return the list of the maximal cliques of the graph
	 */
	public List<BitSet> getCliques() {
		if (!areStructuresInitialised()) {
			initStructures();
		}
		return new ArrayList<BitSet>(cg.vertexSet());
	}

	@SuppressWarnings("unchecked")
	private void computeCliquesAndSeparatorsBFS() {
		cliques = new ArrayList<BitSet>();
		separators = new ArrayList<BitSet>();

		SimpleGraph<Integer, DefaultEdge> gNum = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		SimpleGraph<Integer, DefaultEdge> gElim = (SimpleGraph<Integer, DefaultEdge>) super.clone();
		TreeMap<Integer, Integer> labels = new TreeMap<Integer, Integer>(); // store
																			// the
		// labels of all
		// the nodes
		for (Integer vertex : this.vertexSet()) {
			labels.put(vertex, new Integer(0));
		}
		int lambda = Integer.MAX_VALUE;
		Integer xiplus1 = null;
		while (!labels.isEmpty()) {

			// System.out.println("labels = "+labels.toString());
			// look for the node with the biggest label
			int maxNumber = -1;
			Integer xi = null;

			for (Entry<Integer, Integer> entry : labels.entrySet()) {
				if (entry.getValue() > maxNumber) {
					xi = entry.getKey();
					maxNumber = entry.getValue();
				}
			}
			// System.out.println("picked xi = " + xi.toString());
			gNum.addVertex(xi);// add vertex
			// add the edges of the vertex from the original graph
			Set<DefaultEdge> gEdges = this.edgesOf(xi);
			for (DefaultEdge edge : gEdges) {
				Integer source = this.getEdgeSource(edge);
				Integer target = this.getEdgeTarget(edge);
				if (source.equals(xi)) {
					if (gNum.containsVertex(target)) {
						gNum.addEdge(source, target);
					}
				} else {
					if (gNum.containsVertex(source)) {
						gNum.addEdge(source, target);
					}

				}
			}
			if (xiplus1 != null && maxNumber <= lambda) {
				Set<DefaultEdge> currentEdges = gNum.edgesOf(xi);
				BitSet currentNeighbours = new BitSet();
				for (DefaultEdge edge : currentEdges) {
					Integer source = gNum.getEdgeSource(edge);
					Integer target = gNum.getEdgeTarget(edge);
					if (source.equals(xi)) {
						currentNeighbours.set(target);
					} else {
						currentNeighbours.set(source);
					}
				}
				if (!currentNeighbours.isEmpty()) {
					separators.add(currentNeighbours);
				}
				// System.out.println("prevGNum = " + prevGNum.toString());
				Set<DefaultEdge> previousEdges = gNum.edgesOf(xiplus1);
				BitSet previousNeighbours = new BitSet();
				for (DefaultEdge edge : previousEdges) {
					Integer source = gNum.getEdgeSource(edge);
					Integer target = gNum.getEdgeTarget(edge);
					if (source.equals(xiplus1)) {
						previousNeighbours.set(target);
					} else {
						previousNeighbours.set(source);
					}
				}
				previousNeighbours.set(xiplus1);
				previousNeighbours.clear(xi);
				// System.out.println("new clique : "+previousNeighbours);
				cliques.add(previousNeighbours);

			}
			lambda = maxNumber;

			Set<DefaultEdge> edges = gElim.edgesOf(xi);
			for (DefaultEdge edge : edges) {
				Integer source = gElim.getEdgeSource(edge);
				Integer target = gElim.getEdgeTarget(edge);
				if (source.equals(xi)) {
					Integer number = labels.get(target);
					labels.put(target, number + 1);
				} else {
					Integer number = labels.get(source);
					labels.put(source, number + 1);
				}

			}
			gElim.removeVertex(xi);
			labels.remove(xi);
			xiplus1 = xi;
		}

		Set<DefaultEdge> lastEdges = this.edgesOf(xiplus1);
		BitSet lastNeighbours = new BitSet();
		for (DefaultEdge edge : lastEdges) {
			Integer source = this.getEdgeSource(edge);
			Integer target = this.getEdgeTarget(edge);
			if (source.equals(xiplus1)) {
				lastNeighbours.set(target);
			} else {
				lastNeighbours.set(source);
			}
		}
		lastNeighbours.set(xiplus1);
		// System.out.println(xiplus1);

		cliques.add(lastNeighbours);

	}

	/**
	 * @return the list of separators for the clique graph
	 */
	public List<BitSet> getSeparatorsBFS() {
		if (separators == null) {
			computeCliquesAndSeparatorsBFS();
		}
		return separators;
	}

	public List<BitSet> getCliquesBFS() {
		if (separators == null) {
			computeCliquesAndSeparatorsBFS();
		}
		return cliques;
	}

	/**
	 * @return the size of the biggest clique for the graph associated to this
	 *         model
	 */
	public int getTreeWidth() {
		int maxCliqueSize = 0;
		Set<BitSet> cliques = cg.vertexSet();

		for (BitSet clique : cliques) {
			if (clique.cardinality() > maxCliqueSize) {
				maxCliqueSize = clique.cardinality();
			}
		}
		return maxCliqueSize;
	}

	public void printAvailableEdges() {
		System.out.print("\t\t");
		for (int j = 0; j < eligibleEdges.length; j++) {
			System.out.print(j + "\t");
		}
		System.out.println();
		for (int i = 0; i < eligibleEdges.length; i++) {
			System.out.print("\t" + i + "\t");
			for (int j = 0; j < eligibleEdges.length; j++) {
				if (eligibleEdges[i][j] == null) {
					System.out.print("\t");
				} else {
					System.out.print(eligibleEdges[i][j].separator.toString() + "\t");
				}
			}
			System.out.println();
		}

	}

	public String toString() {
		String str = super.toString();
		str += "\ncliques:\n";
		for (BitSet clique : cg.vertexSet()) {
			str += "\t" + clique.toString() + "\n";
		}
		str += "\nedges:\n";
		for (CliqueGraphEdge edge : cg.edgeSet()) {
			str += "\t" + edge.toString() + "\n";
		}
		return str;
	}

	/**
	 * Returns a shallow copy of this graph instance. Neither edges nor vertices
	 * are cloned.
	 * 
	 * @return a shallow copy of this set.
	 * @throws RuntimeException
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		ChordalGraph copy = (ChordalGraph) super.clone();
		copy.cg = (SimpleGraph<BitSet, CliqueGraphEdge>) this.cg.clone();
		return copy;
	}

	@SuppressWarnings("unchecked")
	public SimpleGraph<BitSet, UniqueTreeSet<Integer>> getCliqueGraph() {
		return (SimpleGraph<BitSet, UniqueTreeSet<Integer>>) cg.clone();
	}

	/**
	 * This function add the given edge without checking if this edge will
	 * maintain the graph chordal. The user has to be sure it will, or the
	 * results of the class won't be valid anymore.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public DefaultEdge addSecuredEdge(Integer a, Integer b, MyPriorityQueue pq, boolean verbose) {
		int nbVertices = vertexSet().size();
		// get Ca and Cb
		BitSet Ca = eligibleEdges[a][b].c1;
		BitSet Cb = eligibleEdges[a][b].c2;
		if (!Ca.get(a)) {
			BitSet tmp = Ca;
			Ca = Cb;
			Cb = tmp;
		}
		if (verbose) System.out.println(a + "--" + Ca);
		if (verbose) System.out.println(b + "--" + Cb);
		BitSet Sab = eligibleEdges[a][b].separator;
		TreeSet<Integer> SabSet = new TreeSet<Integer>();
		for (int i = Sab.nextSetBit(0); i >= 0; i = Sab.nextSetBit(i + 1)) {
			SabSet.add(i);
		}

		if (verbose) System.out.println("\tSab=" + Sab);

		// new clique
		BitSet Cab = new BitSet(nbVertices);
		Cab.or(Sab);
		Cab.set(a);
		Cab.set(b);
		Set<CliqueGraphEdge> cgEdges = cg.edgeSet(); // edges before
														// modification

		SimpleGraph<Integer, DefaultEdge> gMinusSabBeforeAB = (SimpleGraph<Integer, DefaultEdge>) super.clone();
		gMinusSabBeforeAB.removeAllVertices(SabSet);
		ConnectivityInspector<Integer, DefaultEdge> inspectorgMinusSabBeforeAB = new ConnectivityInspector<Integer, DefaultEdge>(gMinusSabBeforeAB);
		TreeSet<Integer> connectedToA = new TreeSet<Integer>(inspectorgMinusSabBeforeAB.connectedSetOf(a));
		TreeSet<Integer> connectedToB = new TreeSet<Integer>(inspectorgMinusSabBeforeAB.connectedSetOf(b));

		DefaultEdge addedE = super.addEdge(a, b);
		disableEdge(a, b, pq, verbose);
		SimpleGraph<Integer, DefaultEdge> gpp = (SimpleGraph<Integer, DefaultEdge>) super.clone();
		gpp.removeAllVertices(SabSet);
		ConnectivityInspector<Integer, DefaultEdge> inspectorGpp = new ConnectivityInspector<Integer, DefaultEdge>(gpp);

		// step 3.3.2
		if (verbose) System.out.println("\tdisabling edges for vertices linked to " + a + " and " + b);
		if (verbose) System.out.println("neighbours of " + a + ":" + connectedToA);
		if (verbose) System.out.println("neighbours of " + b + ":" + connectedToB);
		for (Integer vx : connectedToA) {
			for (Integer vy : connectedToB) {
				disableEdge(vx, vy, pq, verbose);
			}
		}

		// Cab is the new clique between Ca and Cb in the
		// clique graph
		if (verbose) System.out.println("\tremove edge (" + Ca + "," + Cb + ")");
		cg.removeEdge(Ca, Cb);
		cg.removeEdge(Cb, Ca);

		if (verbose) System.out.println("\tadd vertex Cab=" + Cab);
		cg.addVertex(Cab);
		if (verbose) System.out.println("\tadd edge (Ca,Cab)=(" + Ca + "," + Cab + ")");
		cg.addEdge(Ca, Cab);
		if (verbose) System.out.println("\tadd edge (Cb,Cab)=(" + Cb + "," + Cab + ")");
		cg.addEdge(Cb, Cab);
		if (verbose) System.out.println("\tdisabling edges for vertices for which Sab is identical");
		// considering every edge of the clique graph for removal (step 2)
		removeInvalidEdgesCG(Sab, cgEdges, inspectorGpp, pq, verbose);

		// Neighbors of Ca
		BitSet SabUniona = (BitSet) Sab.clone();
		SabUniona.set(a);
		addCGEdgesAroundCa(Ca, Cab, SabUniona, verbose);
		addCGEdgesAroundCaFullCheckVb(b, Cab, SabUniona, verbose);

		// Neighbours of Cb
		BitSet SabUnionb = (BitSet) Sab.clone();
		SabUnionb.set(b);
		addCGEdgesAroundCa(Cb, Cab, SabUnionb, verbose);
		addCGEdgesAroundCaFullCheckVb(a, Cab, SabUnionb, verbose);

//		BitSet toTestForNonConnected = (BitSet) Cab.clone();

		if (containsAll(Cab, Ca)) {
			if (verbose) System.out.println("\tremove vertex Ca=" + Ca);
			cg.removeVertex(Ca);

		}

		if (containsAll(Cab, Cb)) {
			if (verbose) System.out.println("\tremove vertex Cb=" + Cb);
			cg.removeVertex(Cb);

		}

		// update final eligibility
		updateEligibilityAroundCab(Cab, pq, verbose);
		// updateEligibilityForNonConectedComponents(true);
		// updateEligibilityForNonConectedComponentsSmart(Cab,true);
		if (containsAll(Cab, Ca)) {
			updateEligibilityForNonConectedComponentsSmart(Ca, pq, verbose);
		}
		if (containsAll(Cab, Cb)) {
			updateEligibilityForNonConectedComponentsSmart(Cb, pq, verbose);
		}

		if (verbose) printAvailableEdges();
		if (pq != null) pq.processStoredModifications();
		return addedE;

	}

	private void updateEligibilityForNonConectedComponentsSmart(BitSet Cab, MyPriorityQueue pq, boolean verbose) {
		ConnectivityInspector<Integer, DefaultEdge> insp = new ConnectivityInspector<Integer, DefaultEdge>(this);
		if (!insp.isGraphConnected()) {
			if (verbose) System.out.println("\tnon-connected components");
			for (int v = Cab.nextSetBit(0); v >= 0; v = Cab.nextSetBit(v + 1)) {
				for (int j = 0; j < eligibleEdges.length; j++) {
					if (!insp.pathExists(v, j)) {
						BitSet Ci = null;
						BitSet Cj = null;

						for (BitSet c : cg.vertexSet()) {
							if (Ci == null && c.get(v)) {
								Ci = c;
							} else if (Cj == null && c.get(j)) {
								Cj = c;
							}
						}
						enableEdge(v, j, new CliqueGraphEdge(Ci, Cj), pq, verbose);
					}
				}

			}
		}
	}

	private void updateEligibilityAroundCab(BitSet Cab, MyPriorityQueue pq, boolean verbose) {
		Set<CliqueGraphEdge> edgesAroundCab = cg.edgesOf(Cab);
		if (verbose) System.out.println("\tnew edges around Cab");
		for (CliqueGraphEdge edgeAroundCab : edgesAroundCab) {
			BitSet Sp = edgeAroundCab.separator;
			BitSet Cp = edgeAroundCab.c1;
			if (Cp.equals(Cab)) {
				Cp = edgeAroundCab.c2;
			}
			BitSet CpMinusSp = (BitSet) Cp.clone();
			CpMinusSp.andNot(Sp);

			BitSet CabMinusSp = (BitSet) Cab.clone();
			CabMinusSp.andNot(Sp);

			for (int vx = CpMinusSp.nextSetBit(0); vx >= 0; vx = CpMinusSp.nextSetBit(vx + 1)) {
				for (int vxp = CabMinusSp.nextSetBit(0); vxp >= 0; vxp = CabMinusSp.nextSetBit(vxp + 1)) {
					enableEdge(vx, vxp, edgeAroundCab, pq, verbose);
				}
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void addCGEdgesAroundCaFullCheckVb(Integer b, BitSet Cab, BitSet SabUniona, boolean verbose) {
		for (BitSet Cp : cg.vertexSet()) {

			BitSet Sp = (BitSet) Cp.clone();
			Sp.and(Cab);
			if (!Sp.isEmpty() && Sp.equals(SabUniona)) {
				// check if Sab union a separates b from C'-Cab
				BitSet CpMinusCab = (BitSet) Cp.clone();
				CpMinusCab.andNot(Sp);
				if (CpMinusCab.isEmpty()) {
					continue;
				}

				// copy graph for test
				SimpleGraph<Integer, DefaultEdge> graphCopy = (SimpleGraph<Integer, DefaultEdge>) super.clone();
				for (int v = SabUniona.nextSetBit(0); v >= 0; v = SabUniona.nextSetBit(v + 1)) {
					graphCopy.removeVertex(v);
				}

				// look at the connected components
				ConnectivityInspector<Integer, DefaultEdge> inspector = new ConnectivityInspector<Integer, DefaultEdge>(graphCopy);
				// first component
				Set<Integer> componentOfB = inspector.connectedSetOf(b);
				// second component

				Integer oneVertex = CpMinusCab.nextSetBit(0);
				Set<Integer> componentOfCpMinusCab = inspector.connectedSetOf(oneVertex);

				if (!componentOfB.equals(componentOfCpMinusCab)) {
					if (!cg.containsEdge(Cp, Cab) && !cg.containsEdge(Cab, Cp)) {
						if (verbose) System.out.println("\tadd edge (Cab,C') = (" + Cab + "," + Cp + ")");
						CliqueGraphEdge addedEdge = cg.addEdge(Cp, Cab);

						if (addedEdge.separator.isEmpty()) {
							System.err.println("shouldn't have added this edge");
							System.exit(0);
						}
					}
				}
			}
		}
	}

	private void addCGEdgesAroundCa(BitSet Ca, BitSet Cab, BitSet SabUniona, boolean verbose) {
		// ~ considering the addition of edges involving Cab
		// Neighbours of Ca

		for (CliqueGraphEdge CpCa : cg.edgesOf(Ca)) {
			BitSet Cp = CpCa.c1;
			if (Cp.equals(Ca)) {
				Cp = CpCa.c2;
			}
			if (Cp.equals(Cab)) {
				continue;
			}
			if (verbose) System.out.println("\tCp=" + Cp + " is a neighbour of Ca=" + Ca);
			BitSet Sp = CpCa.separator;
			if (verbose) System.out.println("\tS' = Cp inter Ca=" + Sp);
			if (Sp.isEmpty()) {
				continue;
			}

			if (containsAll(SabUniona, Sp)) {
				if (!cg.containsEdge(Cp, Cab) && !cg.containsEdge(Cab, Cp)) {
					if (verbose) System.out.println("\tadd edge (C',Cab)a=(" + Cp + "," + Cab + ")");
					CliqueGraphEdge addedEdge = cg.addEdge(Cp, Cab);

					if (addedEdge.separator.isEmpty()) {
						System.err.println("shouldn't have added this edge");
						System.exit(0);
					}
				}
			}
		}

	}

	private void removeInvalidEdgesCG(BitSet Sab, Set<CliqueGraphEdge> cgEdgesBeforeNewNode, ConnectivityInspector<Integer, DefaultEdge> inspectorGpp, MyPriorityQueue pq, boolean verbose) {
		ArrayList<CliqueGraphEdge> toRemove = new ArrayList<CliqueGraphEdge>();
		for (CliqueGraphEdge cEdge : cgEdgesBeforeNewNode) {
			BitSet c1 = cEdge.c1;
			BitSet c2 = cEdge.c2;
			BitSet S12 = cEdge.separator;

			// if S12 != Sab, the edge is kept
			if (S12.equals(Sab)) {
				if (verbose) System.out.println("\ttrying to remove edge between " + c1 + " and " + c2);

				BitSet c1mS12 = (BitSet) c1.clone();
				c1mS12.andNot(S12);// equivalent removeAll
				BitSet c2mS12 = (BitSet) c2.clone();
				c2mS12.andNot(S12);
				
				

				// test1
				Integer vFromC1mS12 = c1mS12.nextSetBit(0);
				Integer vFromC2mS12 = c2mS12.nextSetBit(0);
				if (inspectorGpp.pathExists(vFromC1mS12, vFromC2mS12)) {
//					if(c1mS12.cardinality()>1 && c2mS12.cardinality()>1){
//						System.out.println("alert\t"+c1mS12+"\t"+c2mS12);
//					}
					if (verbose) System.out.println("\tremove edge S12=" + S12);
					toRemove.add(cEdge);

					for (int vx = c1mS12.nextSetBit(0); vx >= 0; vx = c1mS12.nextSetBit(vx + 1)) {
						for (int vy = c2mS12.nextSetBit(0); vy >= 0; vy = c2mS12.nextSetBit(vy + 1)) {
							disableEdge(vx, vy, pq, verbose);
						}
					}

				}
			}
		}
		cg.removeAllEdges(toRemove);

	}

	private void disableEdge(Integer a, Integer b, MyPriorityQueue pq, boolean verbose) {
		eligibleEdges[a][b] = null;
		eligibleEdges[b][a] = null;
		if (pq != null) pq.disableEdge(a, b);
		if (verbose) System.out.println("\t(" + a + "," + b + ") not accessible any more:" + eligibleEdges[a][b]);
	}

	private void enableEdge(Integer a, Integer b, CliqueGraphEdge edge, MyPriorityQueue pq, boolean verbose) {
		if (verbose && eligibleEdges[a][b] != null && eligibleEdges[a][b].equals(edge)) {
			System.err.println("\tdidn't need to update (" + a + "," + b + "):" + eligibleEdges[a][b]);
		}
		if (eligibleEdges[a][b] == null) {
			eligibleEdges[a][b] = edge;
			eligibleEdges[b][a] = edge;
			if (pq != null) pq.enableEdge(a, b);
		} else {
			if (!eligibleEdges[a][b].equals(edge)) {
				eligibleEdges[a][b] = edge;
				eligibleEdges[b][a] = edge;
				if (!eligibleEdges[a][b].separator.equals(edge.separator)) {
					if (pq != null) pq.updateEdge(a, b);
				}
			}
		}
		if (verbose) System.out.println("\t(" + a + "," + b + ") now accessible:" + eligibleEdges[a][b]);

	}

	private static final boolean containsAll(BitSet s1, BitSet s2) {
		BitSet intersection = (BitSet) s1.clone();
		intersection.and(s2);
		return intersection.equals(s2);
	}

	/**
	 * @return one a Bayesian Network for this model (representing the same
	 *         joint distribution)
	 * @throws CycleFoundException
	 */
	public DirectedAcyclicGraph<Integer, DefaultEdge> getBayesianNetwork() throws CycleFoundException {
	
		List<Integer> peo = getEliminationOrdering();
		DirectedAcyclicGraph<Integer, DefaultEdge> bn = new DirectedAcyclicGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for (int i = 0; i < peo.size(); i++) {
			Integer nodeI = peo.get(i);
			bn.addVertex(nodeI);
			for (int j = 0; j < i; j++) {
				Integer nodeJ = peo.get(j);
				if (this.containsEdge(nodeJ, nodeI) || this.containsEdge(nodeI, nodeJ)) {
					bn.addDagEdge(nodeI, nodeJ);
				}
			}
		}
	
		// verifying that it's correct
		SimpleGraph<Integer, DefaultEdge> moralized = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		for (Integer v : vertexSet()) {
			moralized.addVertex(v);
		}
		for (Integer v : bn.vertexSet()) {
			TreeSet<Integer> parents = new TreeSet<Integer>();
			Set<DefaultEdge> edges = bn.edgesOf(v);
			for (DefaultEdge e : edges) {
				Integer child = bn.getEdgeTarget(e);
				if (child.equals(v)) {
					parents.add(bn.getEdgeSource(e));
				}
			}
	
			for (Integer p : parents) {
				moralized.addEdge(v, p);
			}
	
			ArrayList<Integer> listOfParents = new ArrayList<Integer>(parents);
			for (int p1 = 1; p1 < listOfParents.size(); p1++) {
				for (int p2 = 0; p2 < p1; p2++) {
					Integer parent1 = listOfParents.get(p1);
					Integer parent2 = listOfParents.get(p2);
					if (!containsEdge(parent1, parent2) && !containsEdge(parent2, parent1)) {
						System.err.println("shouldn't add (" + parent1 + "," + parent2 + ")");
					}
					moralized.addEdge(parent1, parent2);
				}
			}
		}
	
		// now checking that _this_ is the same as moralized
		if (vertexSet().size() != moralized.vertexSet().size() || !vertexSet().containsAll(moralized.vertexSet())) {
			System.err.println("different vertices!");
			System.err.println("original: " + vertexSet());
			System.err.println("bn: " + bn.vertexSet());
		}
		if (edgeSet().size() != moralized.edgeSet().size()) {
			System.err.println("not the same number of edges!");
			System.err.println("original: " + edgeSet().size());
			System.err.println("moralized bn: " + moralized.edgeSet().size());
		}
	
		for (DefaultEdge e : edgeSet()) {
			Integer source = getEdgeSource(e);
			Integer target = getEdgeTarget(e);
			if (!moralized.containsEdge(source, target) && !moralized.containsEdge(target, source)) {
				System.err.println(e + " missing in moralized graph");
			}
		}
	
		return bn;
	}

	/**
	 * @return a possible elimination ordering on the graph.
	 */
	@SuppressWarnings("unchecked")
	protected final ArrayList<Integer> getEliminationOrdering() {
		if (peo != null) {
			return peo;
		} else {
			peo = new ArrayList<Integer>();
	
			SimpleGraph<Integer, DefaultEdge> gElim = (SimpleGraph<Integer, DefaultEdge>) super.clone();
			TreeMap<Integer, Integer> labels = new TreeMap<Integer, Integer>(); // store
																				// the
			// labels of
			// all
			// the nodes
			for (Integer vertex : this.vertexSet()) {
				labels.put(vertex, new Integer(0));
			}
			while (!labels.isEmpty()) {
	
				// System.out.println("labels = "+labels.toString());
				// look for the node with the biggest label
				int maxNumber = -1;
				Integer xi = null;
	
				for (Entry<Integer, Integer> entry : labels.entrySet()) {
					if (entry.getValue() > maxNumber) {
						xi = entry.getKey();
						maxNumber = entry.getValue();
					}
				}
				peo.add(0, xi);
	
				Set<DefaultEdge> edges = gElim.edgesOf(xi);
				for (DefaultEdge edge : edges) {
					Integer source = gElim.getEdgeSource(edge);
					Integer target = gElim.getEdgeTarget(edge);
					if (source.equals(xi)) {
						Integer number = labels.get(target);
						labels.put(target, number + 1);
					} else {
						Integer number = labels.get(source);
						labels.put(source, number + 1);
					}
	
				}
				gElim.removeVertex(xi);
				labels.remove(xi);
			}
			System.out.println(peo);
			return peo;
		}
	}

}
