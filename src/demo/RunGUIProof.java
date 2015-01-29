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

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.DecomposableModel;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import explorer.ChordalysisModelling;

public class RunGUIProof {
	
	static String introductionMessage = "Welcome to Chordalysis: log-linear analysis for high-dimensional data. \n"
			+ "\nHere are the steps that you're going to follow: \n"
			+ "   1. Choice of the data file\n" 
			+ "   2. Choice of the desired significance level\n" 
			+ "   3. Choice of the output file\n" 
			+ "   4. Automatic analysis of your dataset!\n"
			+ "\nDo not hesitate to contact me at petitjean@tiny-clues.eu for any enquiry. ";
	static String noFileMessage = "The file doesn't exist. ";
	static String noFileSelectedMessage = "You didn't select any file. ";
	static String incorrectPValueMessage = "The p-value threshold has to be between 0 and 1. ";
	static String agreeCitation = "Chordalysis is provided as free software. "
			+ "Do you agree to reference its source below "
			+ "if it helped you find something useful? " 
			+ "\n\n\t- F. Petitjean, G.I. Webb and A. Nicholson, Scaling log-linear analysis to high-dimensional data, ICDM 2013"
			+ "\n\t- F. Petitjean and G.I. Webb, Scaling log-linear analysis to datasets with thousands of variables, SDM 2015";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JOptionPane.showMessageDialog(null, introductionMessage, "Chordalysis", JOptionPane.INFORMATION_MESSAGE);
		
		int result = JOptionPane.showOptionDialog(null, new JTextArea(agreeCitation), "Reference", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null, null, null);
		if(result==JOptionPane.NO_OPTION || result==JOptionPane.CLOSED_OPTION){
			JOptionPane.showMessageDialog(null, "Chordalysis will now stop, because you do not want to reference its source. ", "Chordalysis", JOptionPane.WARNING_MESSAGE);
			System.exit(0);
		}

		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file", "csv");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		File csvFile = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			csvFile = chooser.getSelectedFile();
			System.out.println("You chose to open: " + csvFile);
		}else{
			JOptionPane.showMessageDialog(null, noFileSelectedMessage, "Chordalysis", JOptionPane.ERROR_MESSAGE);
			return;
		}
		CSVLoader loader = new CSVLoader();
		if (!csvFile.exists()) {
			JOptionPane.showMessageDialog(null, noFileMessage, "Chordalysis", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		double pValue = -1;
		while(pValue <= 0 || 1 <= pValue) {
			pValue = Double.valueOf(JOptionPane.showInputDialog("Desired p-value (between 0 and 1)", 0.05));
			if (pValue <= 0 || 1 <= pValue) {
				JOptionPane.showMessageDialog(null, incorrectPValueMessage, "Chordalysis", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		filter = new FileNameExtensionFilter("PNG or DOT or CSV file or DNE file", "png","dot","csv","dne");
		chooser = new JFileChooser();
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("Where to save the graph?");
		chooser.setSelectedFile(new File(csvFile.getAbsolutePath()+".png"));
		returnVal = chooser.showSaveDialog(null);
		File graphFile = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			graphFile = chooser.getSelectedFile();
			System.out.println("You chose to save the graph to: " + graphFile.getAbsolutePath());
		}else{
			JOptionPane.showMessageDialog(null, noFileSelectedMessage, "Chordalysis", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		try {
			loader.setFile(csvFile);
			
			returnVal = JOptionPane.showConfirmDialog(null, "Are all of your attribute nominal?","Chordalysis", JOptionPane.YES_NO_OPTION);
			if(returnVal==JOptionPane.YES_OPTION){
				loader.setNominalAttributes("first-last");
			}
			
			Instances instances = loader.getDataSet();
			
			String cols = "";
			for (int i = 0; i < instances.numAttributes(); i++) {
				Attribute att = instances.attribute(i);
				if (!att.isNominal()) {
					cols += (i+1) + ",";
				}
			}
			if (!cols.isEmpty()) {
				cols = cols.substring(0, cols.length() - 1);
				String message = "Some atributes are not nominal (number "+cols+"), please wait during discretization. ";
				JOptionPane.showMessageDialog(null, message, "Chordalysis", JOptionPane.INFORMATION_MESSAGE);
				Discretize discretizer = new Discretize(cols);
				discretizer.setUseEqualFrequency(true);
				discretizer.setBins(3);
				discretizer.setIgnoreClass(true);
				discretizer.setInputFormat(instances);
				instances = Filter.useFilter(instances, discretizer);
				JOptionPane.showMessageDialog(null, "Discretization is now finished.","Chordalysis", JOptionPane.INFORMATION_MESSAGE);
			}

			String[] variablesNames = new String[instances.numAttributes()];
			String [][]outcomes = new String[instances.numAttributes()][];
			for (int i = 0; i < variablesNames.length; i++) {
				variablesNames[i] = instances.attribute(i).name();
				outcomes[i] = new String[instances.attribute(i).numValues()];
				for (int j = 0; j < outcomes[i].length; j++) {
					outcomes[i][j]=instances.attribute(i).value(j);
				}
			}

			ChordalysisModelling modeller = new ChordalysisModelling(pValue);
			modeller.buildModel(instances);
			DecomposableModel bestModel = modeller.getModel();
			JOptionPane.showMessageDialog(null,new JTextArea("Chordalysis has now finished analysing your data. "
					+ "\nIf you found something useful, please reference Chordalysis as"
					+ "\n\t- F. Petitjean, G.I. Webb and A. Nicholson, Scaling log-linear analysis to high-dimensional data, ICDM 2013"
					+ "\n\t- F. Petitjean and G.I. Webb, Scaling log-linear analysis to datasets with thousands of variables, SDM 2015"
					+"\n\nYou can find the output file at: '"+graphFile.getAbsolutePath()+"'"),"Citation",JOptionPane.INFORMATION_MESSAGE);
			System.out.println("The model selected is:");
			System.out.println(bestModel.toString(variablesNames));
			if(graphFile.getName().endsWith("dot")){
				bestModel.exportDOT(graphFile, variablesNames);
			}else if(graphFile.getName().endsWith("png")){
				ImageIO.write(bestModel.getImage(variablesNames),"png", graphFile);
			}else if(graphFile.getName().endsWith("dne")){
				bestModel.exportBNNetica(graphFile, variablesNames,outcomes);
				bestModel.exportDOT(new File(graphFile.getAbsolutePath()+".dot"), variablesNames);
				ImageIO.write(bestModel.getImage(variablesNames),"png", new File(graphFile.getAbsolutePath()+".png"));
				bestModel.saveAssociations(variablesNames, new File(graphFile.getAbsolutePath()+".csv"));
			}else{
				bestModel.saveAssociations(variablesNames, graphFile);
			}
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "The file '" + csvFile.getAbsolutePath() + "'\ncannot be read properly.", "Error while reading file", JOptionPane.ERROR_MESSAGE);
			System.out.println("I/O error while loading csv file");
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error:" + e.getMessage(), "Chordalysis" ,JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}

}
