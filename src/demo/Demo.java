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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import core.explorer.ChordalysisModeller;
import core.explorer.ChordalysisModellingSMT;
import core.model.DecomposableModel;
import extra.PrintableModel;
import loader.LoadWekaInstances;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class Demo {

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
    while ((inputLine = in.readLine()) != null){
      out.println(inputLine);
    }
    in.close();
    out.close();
    System.out.println("Dataset written to: "+csvFile.getAbsolutePath());

    loader.setFile(csvFile);
    loader.setNominalAttributes("first-last");
    Instances instances = loader.getDataSet();
    String[] variablesNames = new String[instances.numAttributes()];
    for (int i = 0; i < variablesNames.length; i++) {
      variablesNames[i] = instances.attribute(i).name();
    }
    
    ChordalysisModeller.Data mydata = LoadWekaInstances.makeModelData(instances);
    ChordalysisModellingSMT modeller = new ChordalysisModellingSMT(mydata, 0.05);

    System.out.println("Learning...");
    modeller.buildModel();
    DecomposableModel bestModel = modeller.getModel();
    System.out.println("The model selected is:");
    System.out.println(bestModel.toString(variablesNames));
    
    PrintableModel.display(bestModel, variablesNames);

  }
}
