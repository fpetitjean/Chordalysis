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
package stats;

import static org.apache.commons.math3.util.FastMath.log;

import java.util.BitSet;
import java.util.HashMap;

import lattice.Lattice;
import lattice.LatticeNode;

/**
 * This class aims at computing multiple entropies between different sets of
 * variables. This class uses different memoizations and memorizations
 * techniques to retrieve results very quickly.
 */
public class EntropyComputer {
	public static long nbCellsEverParsed;
	HashMap<BitSet, Double> lookup;
	double[] partialEntropy;

	Lattice lattice;
	int nbInstances;

	/**
	 * Constructor
	 * 
	 * @param nbInstances
	 *            number of lines in the database
	 * @param lattice
	 *            associated lattice
	 *            @deprecated
	 */
	public EntropyComputer(int nbInstances, Lattice lattice) {
		this.lookup = new HashMap<BitSet, Double>();
		this.lattice = lattice;
		this.nbInstances = nbInstances;
		lookup.put(new BitSet(lattice.getNbVariables()), 0.0);
		this.partialEntropy = new double[this.nbInstances + 1];
		double lnN = log(nbInstances);
		partialEntropy[0] = 0.0;
		for (int i = 1; i < partialEntropy.length; i++) {
			partialEntropy[i] = i * (log(i) - lnN);
		}
		nbCellsEverParsed = 0;
	}
	
	/**
	 * Constructor
	 * 
	 * @param lattice
	 *            associated lattice
	 */
	public EntropyComputer(Lattice lattice) {
		this.lookup = new HashMap<BitSet, Double>();
		this.lattice = lattice;
		this.nbInstances = this.lattice.getNbInstances();
		lookup.put(new BitSet(lattice.getNbVariables()), 0.0);
		this.partialEntropy = new double[this.nbInstances + 1];
		double lnN = log(nbInstances);
		partialEntropy[0] = 0.0;
		for (int i = 1; i < partialEntropy.length; i++) {
			partialEntropy[i] = i * (log(i) - lnN);
		}
		nbCellsEverParsed = 0;
	}

	/**
	 * Computes the partial entropy for a lattice node (set of variables)
	 * 
	 * @param clique
	 *            the lattice node represented by a set of integers
	 * @return the entropy
	 */
	public Double computeEntropy(BitSet clique) {
		Double computedEntropy = lookup.get(clique);
		if (computedEntropy != null) {
//			System.out.println("cached entropy for clique "+clique+":"+clique.hashCode());
			return computedEntropy;
		}
//		System.out.println("Getting entropy for clique "+clique+":"+clique.hashCode());
//		System.out.println("computing entropy for clique "+clique);

		double entropy = 0.0;
		LatticeNode node = lattice.getNode(clique);
		int nbCells = node.getNbCells();
		nbCellsEverParsed += nbCells;
//		 System.out.println("matrix:"+Arrays.toString(matrix));
		for (int i = 0; i < nbCells; i++) {
			int O = node.getMatrixCell(i);
			entropy += partialEntropy[O];
		}
		entropy /= nbInstances;
		entropy *= -1.0;
//		System.out.println("caching "+clique+"("+clique.hashCode()+"):"+entropy);
		lookup.put(clique, entropy);
		return entropy;
	}

	/**
	 * 
	 * @return the number of lines in the database
	 */
	public int getNbInstances() {
		return nbInstances;
	}

	/**
	 * @return the number of variables in the dataset
	 */
	public int getNbVariables() {
		return lattice.getNbVariables();
	}
	
	public int getSizeLookup(){
		return lookup.size();
	}

}
