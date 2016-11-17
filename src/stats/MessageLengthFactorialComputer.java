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

import java.util.BitSet;
import java.util.HashMap;

import lattice.Lattice;
import lattice.LatticeNode;

import org.apache.commons.math3.util.FastMath;

/**
 * This class aims at computing the length of the message transmitting the data
 * associated with a clique of variables, given that the frequencies have know
 * by the receiver. See paper
 * "A statistically efficient and scalable method for log-linear analysis of high-dimensional data, ICDM 2014"
 * 
 * @see http://www.tiny-clues.eu/Research/
 * @author Dr Francois Petitjean
 */
public class MessageLengthFactorialComputer {
	public long nbCellsEverParsed;
	HashMap<BitSet, Double> lookup;
	double[] logs;
	double[] logFactorials;

	Lattice lattice;
	int nbInstances;
	double lnN;

	@Deprecated
	public MessageLengthFactorialComputer(int nbInstances, Lattice lattice) {
		this.lookup = new HashMap<BitSet, Double>();
		this.lattice = lattice;
		this.nbInstances = nbInstances;

		int nbFactorials = this.nbInstances + 2;
		int nbVariables = lattice.getNbVariables();
		int nbMaxEdges = nbVariables * (nbVariables - 1) / 2;
		if (nbMaxEdges + 2 > nbFactorials) {
			nbFactorials = nbMaxEdges + 3;
		}

		this.logs = new double[nbFactorials];
		for (int i = 0; i < logs.length; i++) {
			logs[i] = FastMath.log(i);
		}
		lnN = logs[nbInstances];

		this.logFactorials = new double[nbFactorials];
		logFactorials[0] = logs[1];
		logFactorials[1] = logs[1];
		for (int i = 2; i < logFactorials.length; i++) {
			logFactorials[i] = logFactorials[i - 1] + logs[i];
		}
		nbCellsEverParsed = 0;

		lookup.put(new BitSet(lattice.getNbVariables()), 0.0);

	}
	
	public MessageLengthFactorialComputer(Lattice lattice) {
		this.lookup = new HashMap<BitSet, Double>();
		this.lattice = lattice;
		this.nbInstances = this.lattice.getNbInstances();

		int nbFactorials = this.nbInstances + 2;
		int nbVariables = lattice.getNbVariables();
		int nbMaxEdges = nbVariables * (nbVariables - 1) / 2;
		if (nbMaxEdges + 2 > nbFactorials) {
			nbFactorials = nbMaxEdges + 3;
		}

		this.logs = new double[nbFactorials];
		for (int i = 0; i < logs.length; i++) {
			logs[i] = FastMath.log(i);
		}
		lnN = logs[nbInstances];

		this.logFactorials = new double[nbFactorials];
		logFactorials[0] = logs[1];
		logFactorials[1] = logs[1];
		for (int i = 2; i < logFactorials.length; i++) {
			logFactorials[i] = logFactorials[i - 1] + logs[i];
		}
		nbCellsEverParsed = 0;

		lookup.put(new BitSet(lattice.getNbVariables()), 0.0);

	}

	public double computeLengthData(BitSet clique) {
		Double computedLength = lookup.get(clique);
		if (computedLength != null) {
			// System.out.println("sending "+clique+" costs "+computedLength+" nits");
			return computedLength;
		}

		LatticeNode node = lattice.getNode(clique);
		int nbCells = node.getNbCells();
		nbCellsEverParsed += nbCells;

		double length = 0.0;
		// stating the position in data
		length += logFactorials[nbInstances];
		for (int i = 0; i < nbCells; i++) {
			int O = node.getMatrixCell(i);
			length -= logFactorials[O];
		}
		lookup.put(clique, length);
		// System.out.println("sending "+clique+" costs "+length+" nits");
		return length;
	}

	public int getNbInstances() {
		return nbInstances;
	}

	public int getNbVariables() {
		return lattice.getNbVariables();
	}

	public double[] getLogFactorials() {
		return logFactorials;
	}

	public double[] getLogs() {
		return logs;
	}

	public final double getLogFromTable(int i) {
		return logs[i];
	}

}
