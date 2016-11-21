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
package loader;

// Java
import java.io.IOException;
import java.util.BitSet;

// Chordalysis
import core.explorer.ChordalysisModeller;
import core.lattice.Lattice;
import core.model.DecomposableModel;

// Weka
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public final class LoadWekaInstances {

  /** Make a lattice over the given variables of the dataset.*/
  public static ChordalysisModeller.Data makeModelData(Instances dataset) {
    return makeModelData(dataset, true);
  }

  /** Make a lattice over the given variables of the dataset.*/
  public static ChordalysisModeller.Data makeModelData(Instances dataset, boolean hasMissingValues) {

    // Access the size:
    int nbInstances = dataset.numInstances();
    int nbVariables = dataset.numAttributes();

    // Create the array for the model
    int[] variables = new int[nbVariables];

    // Create the bitset for the lattice
    int[] nbValuesForAttribute            = new int[nbVariables];       // Also used by the model
    BitSet[][] presence                   = new BitSet[nbVariables][];

    // --- 1 ---
    // For each attribute...
    for (int a = 0; a < nbVariables; a++) {
      // --- For the model:
      variables[a] = a;

      // --- For the lattice:
      nbValuesForAttribute[a] = dataset.attribute(a).numValues();
      // --- --- Handle the case of missing values:  +1 for missing
      if (hasMissingValues) { nbValuesForAttribute[a]++; }
      // --- --- Build the bitset
      presence[a] = new BitSet[nbValuesForAttribute[a]];
      for (int v = 0; v < presence[a].length; v++) { presence[a][v] = new BitSet(); }
    }

    // --- 2 ---
    // For each instance (only for the lattice)...
    for (int i = 0; i < nbInstances; i++) {
      Instance row = dataset.instance(i);
      for (int a = 0; a < nbVariables; a++) {
        int indexOfValue;
        if (row.isMissing(a)) {
          if (hasMissingValues) {
            // missing at the end
            indexOfValue = dataset.attribute(a).numValues();
          } else {
            indexOfValue = (int) dataset.meanOrMode(a);
          }
        } else {
          String value = row.stringValue(a);
          indexOfValue = row.attribute(a).indexOfValue(value);
        }
        presence[a][indexOfValue].set(i);
      }
    }

    // --- 3 ---
    // Create the data:
    return new ChordalysisModeller.Data(
        new DecomposableModel(variables, nbValuesForAttribute),
        new Lattice(nbVariables, nbInstances, nbValuesForAttribute, presence) );
  }





  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // With ARFF support
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---


  /** Make a lattice over the given variables of the dataset.*/
  public static ChordalysisModeller.Data makeModelData(Instances structure, ArffReader loader) throws IOException {
    return makeModelData(structure, loader, true);
  }

  /** Make a lattice over the given variables of the dataset.*/
  public static ChordalysisModeller.Data makeModelData(Instances structure, ArffReader loader, boolean hasMissingValues)
    throws IOException {

    // Access the size:
    int nbInstances = 0;
    int nbVariables = structure.numAttributes();

    // Create the array for the model
    int[] variables = new int[nbVariables];

    // Create the bitset for the lattice
    int[] nbValuesForAttribute            = new int[nbVariables];       // Also used by the model
    BitSet[][] presence                   = new BitSet[nbVariables][];

    // --- 1 ---
    // For each attribute:
    for (int a = 0; a < nbVariables; a++) {
      // --- For the model:
      variables[a] = a;

      // --- For the lattice:
      nbValuesForAttribute[a] = structure.attribute(a).numValues();
      // --- --- Handle the case of missing values:  +1 for missing
      if (hasMissingValues) { nbValuesForAttribute[a]++; }
      // --- --- Build the bitset
      presence[a] = new BitSet[nbValuesForAttribute[a]];
      for (int v = 0; v < presence[a].length; v++) { presence[a][v] = new BitSet(); }
    }

    // --- 2 ---
    // For each instance (only for the lattice)...
    Instance row;
    while ((row = loader.readInstance(structure)) != null) {
      boolean skipRow = false;
      for (int a = 0; a < nbVariables; a++) {
        int indexOfValue;
        if (row.isMissing(a)) {
          if (hasMissingValues) {
            indexOfValue = structure.attribute(a).numValues() ;
          } else {
            System.err.println("Found missing while I was told I wouldn't; ignoring whole row");
            skipRow = true;
            break;
          }
        } else {
          String value = row.stringValue(a);
          indexOfValue = row.attribute(a).indexOfValue(value);
        }
        presence[a][indexOfValue].set(nbInstances);
      }
      if (!skipRow) { nbInstances++; }
    }

    // --- 3 ---
    // Create the data:
    return new ChordalysisModeller.Data(
        new DecomposableModel(variables, nbValuesForAttribute),
        new Lattice(nbVariables, nbInstances, nbValuesForAttribute, presence) );
  }

}
