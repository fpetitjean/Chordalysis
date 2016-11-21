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
package core.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.lattice.Lattice;
import core.lattice.LatticeNode;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

@SuppressWarnings("deprecation")
public class Inference {

    private DirectedAcyclicGraph<Integer, DefaultEdge> bn;
    private BayesNet jbn;
    private Map<Integer, BayesNode> jnodes;
    private Map<Integer, String> nodeNames;
    private Map<String, Integer> nodeIDFromName;
    private Map<BayesNode, Integer> nodesNumber;
    private IBayesInferer inferer;
    Map<BayesNode, String> evidence;

    public Inference(DecomposableModel model, String[] variableNames, String[][] outcomes) {

	try {
	    this.bn = model.getBayesianNetwork();
	} catch (CycleFoundException e) {
	    e.printStackTrace();
	}

	this.jnodes = new HashMap<Integer, BayesNode>();
	this.nodesNumber = new HashMap<BayesNode, Integer>();
	this.nodeNames = new HashMap<Integer, String>();
	this.nodeIDFromName = new HashMap<String, Integer>();
	this.jbn = new BayesNet();
	for (Integer nodeID : bn.vertexSet()) {
	    String name = variableNames[nodeID];
	    BayesNode node = jbn.createNode(name);
	    node.addOutcomes(outcomes[nodeID]);

	    nodeNames.put(nodeID, name);
	    nodeIDFromName.put(name, nodeID);
	    nodesNumber.put(node, nodeID);
	    jnodes.put(nodeID, node);
	}

	for (Integer nodeID : bn.vertexSet()) {
	    BayesNode node = jnodes.get(nodeID);
	    ArrayList<BayesNode> parents = new ArrayList<BayesNode>();
	    for (DefaultEdge e : bn.edgesOf(nodeID)) {
		if (bn.getEdgeTarget(e) == nodeID) {
		    BayesNode oneParent = jnodes.get(bn.getEdgeSource(e));
		    parents.add(oneParent);
		}
	    }
	    if (!parents.isEmpty()) {
		node.setParents(parents);
	    }
	}
    }

    public void setProbabilities(Lattice lattice) {

	for (Integer nodeID : jnodes.keySet()) {
	    BayesNode n = jnodes.get(nodeID);
	    // System.out.println("setting CPT for "+n.getName());
	    List<BayesNode> parents = n.getParents();
	    List<BayesNode> parentsAndChild = new ArrayList<BayesNode>(parents);
	    parentsAndChild.add(n);

	    int nbParents = parents.size();
	    // System.out.println(nbParents+" parents");
	    // System.out.print("numbers for jayes =[");
	    // for (int i = 0; i < parentsAndChild.size(); i++) {
	    // BayesNode nodeTmp = parentsAndChild.get(i);
	    // System.out.print(nodesNumber.get(nodeTmp)+",");
	    // }
	    // System.out.println("]");

	    BitSet numbers = new BitSet();
	    numbers.set(nodeID);

	    int[] sizes = new int[nbParents];
	    int nbRowsInCPT = 1;
	    for (int i = 0; i < parents.size(); i++) {
		BayesNode parent = parents.get(i);
		numbers.set(nodesNumber.get(parent));
		sizes[i] = parents.get(i).getOutcomeCount();
		nbRowsInCPT *= sizes[i];
	    }

	    LatticeNode latticeNode = lattice.getNode(numbers);
	    Map<Integer, Integer> fromNodeIDToPositionInSortedTable = new HashMap<Integer, Integer>();

	    Integer[] variablesNumbers = new Integer[numbers.cardinality()];
	    int current = 0;
	    for (int i = numbers.nextSetBit(0); i >= 0; i = numbers.nextSetBit(i + 1)) {
		variablesNumbers[current] = i;
		current++;
	    }
	    for (int i = 0; i < variablesNumbers.length; i++) {
		fromNodeIDToPositionInSortedTable.put(variablesNumbers[i], i);
	    }

	    int[] counts = new int[nbRowsInCPT * n.getOutcomeCount()];
	    int[] indexes4lattice = new int[parentsAndChild.size()];
	    int[] indexes4Jayes = new int[parentsAndChild.size()];
	    // System.out.println(counts.length +" cases");
	    // System.out.println("numbers for lattice "+Arrays.toString(variablesNumbers));

	    for (int c = 0; c < counts.length; c++) {
		// System.out.println("case "+c);
		int index = c;
		// find indexes
		for (int i = indexes4Jayes.length - 1; i > 0; i--) {
		    BayesNode associatedNode = parentsAndChild.get(i);
		    int dim = associatedNode.getOutcomeCount();
		    indexes4Jayes[i] = index % dim;
		    index /= dim;
		}
		indexes4Jayes[0] = index;

		// System.out.println("indexes jayes = "+Arrays.toString(indexes4Jayes));

		for (int i = 0; i < indexes4Jayes.length; i++) {
		    BayesNode nodeInPositionI = parentsAndChild.get(i);
		    // System.out.println(nodeInPositionI);
		    // System.out.println(fromNodeIDToPositionInSortedTable);
		    int nodeInPositionIID = nodesNumber.get(nodeInPositionI);
		    int indexInSortedTable = fromNodeIDToPositionInSortedTable
			    .get(nodeInPositionIID);
		    indexes4lattice[indexInSortedTable] = indexes4Jayes[i];
		}

		// System.out.println("indexes lattice = "+Arrays.toString(indexes4lattice));

		int count = latticeNode.getMatrixCell(indexes4lattice);
		counts[c] = count;
	    }
	    // System.out.println(Arrays.toString(counts));
	    // System.out.println("total="+sumAllCounts);

	    double mTerm = 0.5;
	    double[] probas1D = new double[n.getOutcomeCount() * nbRowsInCPT];
	    for (int s = 0; s < probas1D.length; s += n.getOutcomeCount()) {

		double sumOfCounts = 0.0;
		for (int j = 0; j < n.getOutcomeCount(); j++) {
		    sumOfCounts += counts[s + j] + mTerm;
		}

		for (int j = 0; j < n.getOutcomeCount(); j++) {
		    probas1D[s + j] = (counts[s + j] + mTerm) / sumOfCounts;
		}
	    }
	    // System.out.println(Arrays.toString(probas1D));
	    n.setProbabilities(probas1D);

	}

	System.out.println("Compiling network for inference...");
	inferer = new JunctionTreeAlgorithm();
	inferer.setNetwork(jbn);
	evidence = new HashMap<BayesNode, String>();
	System.out.println("Compiled.");
    }

    public void addEvidence(int nodeID, String outcome) {
	addEvidence(jnodes.get(nodeID), outcome);
    }

    public void addEvidence(String nodeName, String outcome) {
	addEvidence(jnodes.get(nodeIDFromName.get(nodeName)), outcome);
    }

    protected void addEvidence(BayesNode node, String outcome) {
	evidence.put(node, outcome);
    }

    public void recordEvidence() {
	inferer.setEvidence(evidence);
    }

    public void clearEvidences() {
	evidence = new HashMap<BayesNode, String>();
	inferer.setEvidence(evidence);
    }

    public double[] getBelief(int nodeID) {
	return getBelief(jnodes.get(nodeID));
    }

    public double[] getBelief(BayesNode n) {
	return inferer.getBeliefs(n);
    }

    public double[] getBelief(String nodeName) {
	Integer nodeID = nodeIDFromName.get(nodeName);
	if (nodeID == null) {
	    System.err.println("Cannot find a node named '" + nodeName + "'.");
	    return null;
	}
	BayesNode node = jnodes.get(nodeID);
	if (node == null) {
	    System.err.println("Cannot find a node named '" + nodeName + "'.");
	    return null;
	}

	return getBelief(node);
    }

    public void exportDSC(File file, Lattice lattice) throws FileNotFoundException {
	PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
	out.println("belief network \"net\"");
	for (Integer nodeID : jnodes.keySet()) {
	    BayesNode n = jnodes.get(nodeID);
	    out.print("node " + n.getName() + " {\n" + "\ttype : discrete [ "
		    + n.getOutcomeCount() + " ] = { ");
	    out.print("\""+n.getOutcomeName(0)+"\"");
	    for (int i = 1; i < n.getOutcomeCount(); i++) {
		out.print(",\"" + n.getOutcomeName(i) + "\"");
	    }
	    out.println("},\n}");
	    // System.out.println("setting CPT for "+n.getName());
	    List<BayesNode> parents = n.getParents();
	    List<BayesNode> parentsAndChild = new ArrayList<BayesNode>(parents);
	    parentsAndChild.add(n);
	}

	for (Integer nodeID : jnodes.keySet()) {
	    BayesNode n = jnodes.get(nodeID);
	    List<BayesNode> parents = n.getParents();
	    List<BayesNode> parentsAndChild = new ArrayList<BayesNode>(parents);
	    parentsAndChild.add(n);

	    out.print("probability ( " + n.getName());
	    if (!parents.isEmpty()) {
		out.print(" | " + parents.get(0).getName());
		for (int p = 1; p < parents.size(); p++) {
		    out.print(" , " + parents.get(p).getName());
		}
	    }
	    out.println(" ) {");

	    int nbParents = parents.size();

	    BitSet numbers = new BitSet();
	    numbers.set(nodeID);

	    int[] sizes = new int[nbParents];
	    int nbRowsInCPT = 1;
	    for (int i = 0; i < parents.size(); i++) {
		BayesNode parent = parents.get(i);
		numbers.set(nodesNumber.get(parent));
		sizes[i] = parents.get(i).getOutcomeCount();
		nbRowsInCPT *= sizes[i];
	    }

	    LatticeNode latticeNode = lattice.getNode(numbers);
	    Map<Integer, Integer> fromNodeIDToPositionInSortedTable = new HashMap<Integer, Integer>();

	    Integer[] variablesNumbers = new Integer[numbers.cardinality()];
	    int current = 0;
	    for (int i = numbers.nextSetBit(0); i >= 0; i = numbers.nextSetBit(i + 1)) {
		variablesNumbers[current] = i;
		current++;
	    }
	    for (int i = 0; i < variablesNumbers.length; i++) {
		fromNodeIDToPositionInSortedTable.put(variablesNumbers[i], i);
	    }

	    int[] counts = new int[nbRowsInCPT * n.getOutcomeCount()];
	    int[] indexes4lattice = new int[parentsAndChild.size()];
	    int[] indexes4Jayes = new int[parentsAndChild.size()];
	    // System.out.println(counts.length +" cases");
	    // System.out.println("numbers for lattice "+Arrays.toString(variablesNumbers));

	    for (int c = 0; c < counts.length; c++) {
		// System.out.println("case "+c);
		int index = c;
		// find indexes
		for (int i = indexes4Jayes.length - 1; i > 0; i--) {
		    BayesNode associatedNode = parentsAndChild.get(i);
		    int dim = associatedNode.getOutcomeCount();
		    indexes4Jayes[i] = index % dim;
		    index /= dim;
		}
		indexes4Jayes[0] = index;

		for (int i = 0; i < indexes4Jayes.length; i++) {
		    BayesNode nodeInPositionI = parentsAndChild.get(i);
		    // System.out.println(nodeInPositionI);
		    // System.out.println(fromNodeIDToPositionInSortedTable);
		    int nodeInPositionIID = nodesNumber.get(nodeInPositionI);
		    int indexInSortedTable = fromNodeIDToPositionInSortedTable
			    .get(nodeInPositionIID);
		    indexes4lattice[indexInSortedTable] = indexes4Jayes[i];
		}

		// System.out.println("indexes lattice = "+Arrays.toString(indexes4lattice));

		int count = latticeNode.getMatrixCell(indexes4lattice);
		counts[c] = count;
	    }
	    // System.out.println(Arrays.toString(counts));
	    // System.out.println("total="+sumAllCounts);

	    double mTerm = 0.5;
	    if (parents.isEmpty()) {
		double sumOfCounts = 0.0;
		for (int j = 0; j < n.getOutcomeCount(); j++) {
		    sumOfCounts += counts[j] + mTerm;
		}
		double p = (counts[0] + mTerm) / sumOfCounts;
		out.print("\t " + p);
		for (int j = 1; j < n.getOutcomeCount(); j++) {
		    p = (counts[j] + mTerm) / sumOfCounts;
		    out.print(", " + p);
		}
		out.println(";");
	    } else {
		int[] indexes4Parents = new int[parents.size()];
		for (int r = 0; r < nbRowsInCPT; r++) {
		    int index = r;
		    // find indexes
		    for (int i = indexes4Parents.length - 1; i > 0; i--) {
			BayesNode associatedNode = parents.get(i);
			int dim = associatedNode.getOutcomeCount();
			indexes4Parents[i] = index % dim;
			index /= dim;
		    }
		    indexes4Parents[0] = index;
		    out.print("\t(" + indexes4Parents[0]);
		    for (int p = 1; p < indexes4Parents.length; p++) {
			out.print("," + indexes4Parents[p]);
		    }
		    out.print("): ");

		    double sumOfCounts = 0.0;
		    for (int j = 0; j < n.getOutcomeCount(); j++) {
			sumOfCounts += counts[r * n.getOutcomeCount() + j] + mTerm;
		    }

		    double p = (counts[r * n.getOutcomeCount() + 0] + mTerm) / sumOfCounts;
		    out.print(p);
		    for (int j = 1; j < n.getOutcomeCount(); j++) {
			p = (counts[r * n.getOutcomeCount() + j] + mTerm) / sumOfCounts;
			out.print(", " + p);
		    }
		    out.println(";");

		}
	    }
	    out.println("}");
	}
	out.flush();
	out.close();
    }

}
