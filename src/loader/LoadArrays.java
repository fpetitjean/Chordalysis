package loader;

import java.util.BitSet;

import core.explorer.ChordalysisModeller;
import core.lattice.Lattice;
import core.model.DecomposableModel;

public final class LoadArrays {

  /**
   * Initialising method for the R package
   * @param nValuesForAttribute number of values per attribute
   * @param data (first dim is line no, second is variable number, content is value no for variable k in [0,nValuesForAttribute[k])
   */
  public static ChordalysisModeller.Data makeModelData(int[]nValuesForAttribute, int[][] data) {

    // Access the size:
    int nbInstances   = data.length;
    int nbVariables   = nValuesForAttribute.length;

    // Create the array for the model
    int[] variables = new int[nbVariables];

    // Create the bitset for the lattice
    BitSet[][] convData = new BitSet[nbVariables][];


    // --- 1 ---
    // For each variables...
    for (int a = 0; a < nbVariables; a++) {
      // --- For the model:
      variables[a] = a;
      // --- For the lattice:
      convData[a] = new BitSet[nValuesForAttribute[a]];
      for (int v = 0; v < convData[a].length; v++) { convData[a][v]=new BitSet(nbInstances); }
    }

    // --- 2 ---
    // For each instances (only for the lattice)...
    for (int i = 0; i < nbInstances; i++) {
      for (int a = 0; a < nbVariables; a++) {
        int[] sample = data[i];
        convData[a][sample[a]].set(i);
      }
    }

    // --- 3 ---
    // Create the data:
    return new ChordalysisModeller.Data(
        new DecomposableModel(variables, nValuesForAttribute),
        new Lattice(nbVariables, nbInstances, nValuesForAttribute, convData) );
  }
}
