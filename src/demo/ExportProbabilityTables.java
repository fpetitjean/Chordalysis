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
package demo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import core.explorer.ChordalysisModeller;
import core.explorer.ChordalysisModellingSMT;
import core.lattice.Lattice;
import core.lattice.LatticeNode;
import core.model.DecomposableModel;
import loader.LoadWekaInstances;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class ExportProbabilityTables {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	CSVLoader loader = new CSVLoader();
	System.out.println("Downloading dataset...");
	URL oracle = new URL("http://repository.seasr.org/Datasets/UCI/csv/mushroom.csv");
	File csvFile = File.createTempFile("data-", ".csv");
	BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
	PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(csvFile)));
	String inputLine;
	while ((inputLine = in.readLine()) != null) {
	    out.println(inputLine);
	}
	in.close();
	out.close();
	System.out.println("Dataset written to: " + csvFile.getAbsolutePath());

	loader.setFile(csvFile);
	loader.setNominalAttributes("first-last");
	Instances instances = loader.getDataSet();
	String[] variablesNames = new String[instances.numAttributes()];
	String[][] outcomes = new String[instances.numAttributes()][];
	for (int i = 0; i < variablesNames.length; i++) {
	    variablesNames[i] = instances.attribute(i).name();
	    outcomes[i] = new String[instances.attribute(i).numValues() + 1];// +1
									     // for
									     // missing
	    for (int j = 0; j < outcomes[i].length - 1; j++) {
		outcomes[i][j] = instances.attribute(i).value(j);
	    }
	    outcomes[i][outcomes[i].length - 1] = "missing";
	    System.out.println("Dom(" + variablesNames[i] + ") = " + Arrays.toString(outcomes[i]));

	}

	ChordalysisModeller.Data mydata = LoadWekaInstances.makeModelData(instances);
	ChordalysisModellingSMT modeller = new ChordalysisModellingSMT(mydata, 0.05);

	System.out.println("Learning...");
	modeller.buildModel();
	DecomposableModel bestModel = modeller.getModel();
	// bestModel.display(variablesNames);
	System.out.println("The model selected is:");
	System.out.println(bestModel.toString(variablesNames));

	Lattice lattice = modeller.getLattice();

	List<BitSet> cliques = bestModel.getCliquesBFS();
	for (BitSet clique : cliques) {
	    LatticeNode node = lattice.getNode(clique);
	    int[] variableNumbers = node.getVariablesNumbers();
	    System.out.print("clique: [");
	    for (int i = 0; i < variableNumbers.length; i++) {
		System.out.print(variablesNames[variableNumbers[i]] + " ");
	    }
	    System.out.println("]");

	    int nCombinations = node.getNbCells();
	    for (int combination = 0; combination < nCombinations; combination++) {
		int[] indexes = node.getIndexes(combination);

		System.out.print("\tp(");
		for (int var = 0; var < variableNumbers.length; var++) {
		    System.out.print(variablesNames[variableNumbers[var]] + "=");
		    System.out.print(outcomes[variableNumbers[var]][indexes[var]]);
		    if (var < variableNumbers.length - 1) {
			System.out.print(",");
		    }
		}
		int count = node.getMatrixCell(combination);
		double p = (1.0 + count) / (lattice.getNbInstances() + nCombinations);
		System.out.println(") = " + p);
	    }
	    System.out.println();

	}

	List<BitSet> separators = bestModel.getSeparators();
	for (BitSet clique : separators) {
	    LatticeNode node = lattice.getNode(clique);
	    int[] variableNumbers = node.getVariablesNumbers();
	    System.out.print("separator: [");
	    for (int i = 0; i < variableNumbers.length; i++) {
		System.out.print(variablesNames[variableNumbers[i]] + " ");
	    }
	    System.out.println("]");

	    int nCombinations = node.getNbCells();
	    for (int combination = 0; combination < nCombinations; combination++) {
		int[] indexes = node.getIndexes(combination);

		System.out.print("\tp(");
		for (int var = 0; var < variableNumbers.length; var++) {
		    System.out.print(variablesNames[variableNumbers[var]] + "=");
		    System.out.print(outcomes[variableNumbers[var]][indexes[var]]);
		    if (var < variableNumbers.length - 1) {
			System.out.print(",");
		    }
		}
		int count = node.getMatrixCell(combination);
		double p = (1.0 + count) / (lattice.getNbInstances() + nCombinations);
		System.out.println(") = " + p);
	    }
	    System.out.println();

	}

    }
}
