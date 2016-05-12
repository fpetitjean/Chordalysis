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

import explorer.ChordalysisModellingSMT;
import model.DecomposableModel;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
/**
 * This class launches Chordalysis with a GUI
 *
 */
public class Run {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {			
			System.out.println("Usage:\tjava -Xmx1g -jar Chordalysis.jar dataFile pvalue imageOutputFile useGUI?");
			System.out.println("Example:\tjava -Xmx1g -jar Chordalysis.jar dataset.csv 0.05 graph.png false");
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
			System.out.println("The image output file should declare an extension among \".jpg\", \".png\" or \".gif\"");
			return;
		}
		String extension = splitted[splitted.length-1];
		if(!extension.equals("jpg") && !extension.equals("png")&&!extension.equals("gif")){
			System.out.println("The format for the graphical representation of the model should be either jpg, png or gif. ");
			return;
		}else{
			System.out.println("Info:\tExporting result as a "+extension+" file");
		}
		
		boolean gui = Boolean.parseBoolean(args[3]);
		
		if(gui){
			System.out.println("Info:\tUsing a graphical user interface");
		}else{
			System.out.println("Info:\tNot using a graphical user interface");
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

			if(gui)bestModel.display(variablesNames);
			System.out.println("The model selected is: (selected in " + (System.currentTimeMillis() - start) + "ms)");
			System.out.println(bestModel.toString(variablesNames));
			ImageIO.write(bestModel.getImage(variablesNames),extension, outPutFile);
		} catch (IOException e) {
			System.out.println("I/O error while loading csv file");
			e.printStackTrace();
		} 
	}

}
