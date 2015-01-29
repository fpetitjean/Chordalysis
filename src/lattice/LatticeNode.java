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
package lattice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.TreeSet;

/**
 * Represents one node of the Lattice {@link Lattice}
 */
public class LatticeNode implements Comparable<LatticeNode> {

	/**
	 * children of the node
	 */
	TreeSet<LatticeNode> children;

	/**
	 * table storing the number of the variables corresponding to the node
	 */
	int[] variablesNumbers;

	public int[] getVariablesNumbers() {
		return variablesNumbers;
	}

	/**
	 * Storage of the records: one Bitset set per cell in the matrix. (one
	 * dimensional storage of numVariables-dimensional matrix)
	 */
	BitSet[] records;

	/**
	 * Stores the number of values by attribute (indexed by the number of the
	 * attribute)
	 */
	int[] dimensionsForVariables;

	/**
	 * number of variables in the dataset
	 */
	int nbVariables;

	int nbCells;

	/**
	 * tells if the entropy has been computed or not
	 */
	boolean entropyComputed = false;

	Lattice lattice;

	/**
	 * Constructor of a node
	 * 
	 * @param lattice
	 *            the lattice this node belongs to
	 * @param variablesNumbers
	 *            the numbers of the variables corresponding to this node
	 * @param dimensionsForVariables
	 *            the number of values for every variable composing this node
	 * @param records
	 *            the bitsets for all the possible combination of values for the
	 *            variables of this node
	 * @param parents
	 *            the parents of this node
	 */
	public LatticeNode(Lattice lattice, int[] variablesNumbers,
			int[] dimensionsForVariables, BitSet[] records,
			LatticeNode... parents) {
		this.lattice = lattice;
		this.nbVariables = this.lattice.getNbVariables();
		this.variablesNumbers = variablesNumbers;
		this.dimensionsForVariables = dimensionsForVariables;
		this.children = new TreeSet<LatticeNode>();
		nbCells = 1;
		for (int i = 0; i < variablesNumbers.length; i++) {
			nbCells *= dimensionsForVariables[variablesNumbers[i]];
		}
		this.records = records;

		for (LatticeNode parent : parents) {
			parent.addChild(this);
		}

	}

	/**
	 * Just to be used for the top node
	 * 
	 * @param variablesNumbers
	 * @param dimensionsForVariables
	 */
	public LatticeNode(Lattice lattice, int[] dimensionsForVariables) {
		this.lattice = lattice;
		this.variablesNumbers = null;
		this.dimensionsForVariables = dimensionsForVariables;
		this.children = new TreeSet<LatticeNode>();
		records = null;
	}

	/**
	 * Computes the intersections of the TIDset for every cell of the matrix, as
	 * well as the count (size of the intersection) that will be used for the
	 * actual computation of the statistics
	 */
	protected void computeRecords() {
		if (records == null) {
			records = new BitSet[nbCells];
			if(getLevel()==1)System.out.println(getLevel());
			for (int i = 0; i < nbCells; i++) {
				int[] indexes = getIndexes(i);
				// nb variables always strictly greater than 1
				BitSet set = (BitSet) lattice.getSetForVariable(
						variablesNumbers[0], indexes[0]).clone();
				set.and(lattice.getSetForVariable(variablesNumbers[1],
						indexes[1]));
				records[i] = set;

			}
		}

	}

	/**
	 * @return the matrix containing how many records correspond to every
	 *         possible combination of values for this node. This
	 *         {@link #nbVariables}-dimensional matrix is represented in
	 *         one-dimension.
	 */
	public int[] getMatrix() {
		int[] matrix = new int[nbCells];
		if (getLevel() <= 1) {
			computeRecords();
			for (int i = 0; i < matrix.length; i++) {
				matrix[i] = records[i].cardinality();
			}
		} else {
			for (int i = 0; i < nbCells; i++) {
				int[] indexes = getIndexes(i);
				// nb variables always strictly greater than 1
				BitSet set = (BitSet) lattice.getSetForVariable(
						variablesNumbers[0], indexes[0]).clone();
				int j;
				for (j = 1; j < variablesNumbers.length; j++) {
					set.and(lattice.getSetForVariable(variablesNumbers[j],
							indexes[j]));
				}
				matrix[i] = set.cardinality();
			}
		}
		return matrix;
	}

	/**
	 * Returns the i<up>th</up> cell of the matrix.
	 * 
	 * @see #getMatrix()
	 * @param i
	 * @return the number of records corresponding to the given combination of
	 *         values.
	 */
	public int getMatrixCell(int i) {
		if (getLevel() <= 1) {
			computeRecords();//TODO not necessary for storing one level?
			return records[i].cardinality();
		} else {
			int[] indexes = getIndexes(i);
			// nb variables always strictly greater than 1
			BitSet set = (BitSet) lattice.getSetForVariable(
					variablesNumbers[0], indexes[0]).clone();
			int j;
			for (j = 1; j < variablesNumbers.length; j++) {
				set.and(lattice.getSetForVariable(variablesNumbers[j],
						indexes[j]));
			}
			return set.cardinality();
		}
	}

	/**
	 * Returns the number of records for the given combination of values.
	 * 
	 * @see #getMatrix()
	 * @param indexes
	 *            the combination of values ({@link #nbVariables}-dimensional
	 *            table)
	 * @return the number of records for the given combination of values.
	 */
	public int getMatrixCell(int[] indexes) {
		if (getLevel() <= 1) {
			computeRecords();
			return records[getIndex(indexes)].cardinality();
		} else {
			// nb variables always strictly greater than 1
			BitSet set = (BitSet) lattice.getSetForVariable(
					variablesNumbers[0], indexes[0]).clone();
			int j;
			for (j = 1; j < variablesNumbers.length; j++) {
				set.and(lattice.getSetForVariable(variablesNumbers[j],
						indexes[j]));
			}
			return set.cardinality();
		}
	}

	/**
	 * Get the child of the current node, given the variable added to it.
	 * 
	 * @param variableNumber
	 *            the number of the variable added to it.
	 * @return the corresponding node
	 */
	public LatticeNode getChild(int variableNumber, Lattice lattice) {
		// constructing the variables numbers corresponding to the search
		int[] childVariablesNumbers = new int[variablesNumbers.length + 1];
		int currentIndex = 0;
		while (currentIndex < variablesNumbers.length
				&& variablesNumbers[currentIndex] < variableNumber) {
			childVariablesNumbers[currentIndex] = variablesNumbers[currentIndex];
			currentIndex++;
		}
		childVariablesNumbers[currentIndex] = variableNumber;
		currentIndex++;
		while (currentIndex < childVariablesNumbers.length) {
			childVariablesNumbers[currentIndex] = variablesNumbers[currentIndex - 1];
			currentIndex++;
		}

		// looking for the corresponding child
		LatticeNode foundNode = null;
		for (LatticeNode child : children) {
			int i;
			for (i = 0; i < childVariablesNumbers.length; i++) {
				if (childVariablesNumbers[i] < child.variablesNumbers[i]) {
					break;
				}
				if (childVariablesNumbers[i] > child.variablesNumbers[i]) {
					break;
				}
			}
			if (i == childVariablesNumbers.length) {
				foundNode = child;
				break;
			}

		}
		// if (foundNode == null)
		// System.out.println(Arrays.toString(childVariablesNumbers) +
		// " not found among children " + children);

		if (foundNode == null) { // node not yet computed, thus we compute it
			// get all the parents using all (the root)
			ArrayList<LatticeNode> childParents = new ArrayList<LatticeNode>();
			for (int i = 0; i < childVariablesNumbers.length; i++) {
				// one parent per variable of the child
				// parent skipping the i
				int[] parentVariableNumbers = new int[childVariablesNumbers.length - 1];
				for (int j = 0; j < childVariablesNumbers.length; j++) {
					if (j < i) {
						parentVariableNumbers[j] = childVariablesNumbers[j];
					} else if (j > i) {
						parentVariableNumbers[j - 1] = childVariablesNumbers[j];
					}
				}
				// System.out.println("potential parent for "+Arrays.toString(childVariablesNumbers)+": "+Arrays.toString(parentVariableNumbers));
				// System.out.println(lattice.getNode(parentVariableNumbers));
				childParents.add(lattice.getNode(parentVariableNumbers));// should
																			// always
																			// exist
			}
			foundNode = new LatticeNode(lattice, childVariablesNumbers,
					dimensionsForVariables, null,
					childParents.toArray(new LatticeNode[] {}));

		}
		return foundNode;

	}

	@Override
	public int compareTo(LatticeNode o) {
		if (o.variablesNumbers == null) {
			return -1;
		}
		if (variablesNumbers.length < o.variablesNumbers.length) {
			return -1;
		}
		if (variablesNumbers.length > o.variablesNumbers.length) {
			return 1;
		}
		for (int i = 0; i < variablesNumbers.length; i++) {
			if (variablesNumbers[i] < o.variablesNumbers[i]) {
				return -1;
			}
			if (variablesNumbers[i] > o.variablesNumbers[i]) {
				return 1;
			}
		}
		return 0;
	}

	protected BitSet getSet(int... indexes) {
		if (records == null) {
			computeRecords();
		}
		return records[getIndex(indexes)];
	}

	// private int getCount(int... indexes) {
	// return records[getIndex(indexes)].size();
	// }
	//

	/**
	 * @return the number of combinations of values that are possible for the
	 *         {@link #nbVariables} variables. This corresponds to the number of
	 *         cells in the matrix (@see {@link #getMatrix()}).
	 */
	public int getNbCells() {
		return nbCells;
	}

	/**
	 * Computes the index in the one-dimensional array from the coordinates in
	 * the matrix
	 * 
	 * @param indexes
	 *            the list of indexes
	 * @return the index in the one-dimensional array
	 */
	protected final int getIndex(int... indexes) {
		int index = indexes[0];
		for (int i = 1; i < variablesNumbers.length; i++) {
			index *= dimensionsForVariables[variablesNumbers[i]];
			index += indexes[i];
		}
		return index;

	}
	
	/**
	 * Get index from an array of indexes that might be larger than the number of variables
	 * @param indexes
	 * @return
	 */
	public final int getIndexTooBig(int[] indexes) {
		int index = indexes[0];
		for (int i = 1; i < variablesNumbers.length; i++) {
			index *= dimensionsForVariables[variablesNumbers[i]];
			index += indexes[i];
		}
		return index;

	}

	/**
	 * Compute the indexes in the matrix from the index in the one-dimensional
	 * array.
	 * 
	 * @param index
	 *            the index in the one-dimensional array
	 * @return the list of indexes (corresponding to the variables of the node)
	 */
	public final int[] getIndexes(int index) {

		int[] indexes = new int[variablesNumbers.length];
		for (int i = indexes.length - 1; i > 0; i--) {
			int dim = dimensionsForVariables[variablesNumbers[i]];
			indexes[i] = index % dim;
			index /= dim;
		}
		indexes[0] = index;

		return indexes;
	}
	
	
	
	public String toString() {
		return "lattice node:" + Arrays.toString(variablesNumbers);
	}

	/**
	 * Add the given node as a child of the current one (without checking
	 * anything).
	 * 
	 * @param child
	 *            the node to be added as a child
	 */
	protected void addChild(LatticeNode child) {
		this.children.add(child);
	}

	/**
	 * @return the level of the node in the lattice, i.e., the number of
	 *         variables considered by this node.
	 */
	public int getLevel() {
		if (variablesNumbers == null) {
			return 0;
		} else {
			return this.variablesNumbers.length;
		}
	}

}
