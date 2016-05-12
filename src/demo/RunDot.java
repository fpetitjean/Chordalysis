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

import explorer.ChordalysisModellingSMT;
import model.DecomposableModel;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
/**
 * This class launches Chordalysis and export the results as a .dot file for GraphViz
 *
 */
public class RunDot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {			
			System.out.println("Usage:\tjava -Xmx1g -jar Chordalysis.jar dataFile pvalue dotOutputFile");
			System.out.println("Example:\tjava -Xmx1g -jar Chordalysis.jar dataset.csv 0.05 graph.dot");
			System.out.println("\nNote:\t'1g' means that you authorize 1GB of memory. " +
					"\nNote:\tIt should be adjusted depending upon the size of your data set (mostly required to load the data set).");
			return;
		}
		System.out.println();
		CSVLoader loader = new CSVLoader();
		File csvFile = new File(args[0]);
		if (!csvFile.exists()) {
			System.out.println("The file doesn't exist");
			return;
		}else{
			System.out.println("Info:\tUsing the dataset file "+csvFile.getAbsolutePath());
		}
		
		double pValue = Double.valueOf(args[1]);
		if(pValue<=0 || 1<=pValue){
			System.out.println("The p-value should be between 0 and 1 excluded. ");
			return;
		}else{
			System.out.println("Info:\tUsing p="+pValue);
		}
		
		File outPutFile = new File(args[2]);
		String []splitted = outPutFile.getName().split("\\.");
		if(splitted.length<2){
			System.out.println("The image output file should declare a \".dot\" extension");
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
			long start = System.currentTimeMillis();
			
			ChordalysisModellingSMT modeller = new ChordalysisModellingSMT(pValue);
			modeller.buildModel(instances);
			DecomposableModel bestModel = modeller.getModel();

			System.out.println("The model selected is: (selected in " + (System.currentTimeMillis() - start) + "ms)");
			System.out.println(bestModel.toString(variablesNames));
			bestModel.exportDOT(outPutFile, variablesNames);
			System.out.println("DOT file exported - note that the variables with no neighbors won't be included in the graph");
		} catch (IOException e) {
			System.out.println("I/O error while loading csv file");
			e.printStackTrace();
		} 
	}

}
