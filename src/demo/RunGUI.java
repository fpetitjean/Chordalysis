/*******************************************************************************
 * Copyright (C) 2014 Francois Petitjean
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

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import explorer.ChordalysisModellingSMT;
import model.DecomposableModel;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class RunGUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file", "csv");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
		}
		CSVLoader loader = new CSVLoader();
		File csvFile = chooser.getSelectedFile();
		if (!csvFile.exists()) {
			System.out.println("The file doesn't exist");
			return;
		}

		double pValue = Double.valueOf(JOptionPane.showInputDialog("Desired p-value ]0,1[",0.05));
		if (pValue <= 0 || 1 <= pValue) {
			System.out.println("The p-value should be between 0 and 1 excluded. ");
			return;
		}
		try {
			loader.setFile(csvFile);
			loader.setNominalAttributes("first-last");
			Instances instances = loader.getDataSet();
			String[] variablesNames = new String[instances.numAttributes()];
			for (int i = 0; i < variablesNames.length; i++) {
				variablesNames[i] = instances.attribute(i).name();
			}
			
			ChordalysisModellingSMT modeller = new ChordalysisModellingSMT(pValue);
			modeller.buildModel(instances);
			DecomposableModel bestModel = modeller.getModel();
			System.out.println("The model selected is:");
			System.out.println(bestModel.toString(variablesNames));
			bestModel.display(variablesNames);
		} catch (IOException e) {
			System.out.println("I/O error while loading csv file");
			e.printStackTrace();
		}

	}

}
