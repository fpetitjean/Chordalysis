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
package core.lattice;

import java.util.BitSet;

/**
 * Represents the lattice over the variables. This lattice is used to access the
 * high-dimensional matrices for different combinations of the variables of a
 * dataset.
 */
public class Lattice {

  private LatticeNode all;
  private LatticeNode[] singleNodes;
  private int nbVariables;
  private int nbInstances;

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Constructors
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /** Lattice Constructor */
  public Lattice(int nbVariables, int nbInstances, int[] nbValuesForAttribute, BitSet[][] convData){

    // Init fields:
    this.nbVariables = nbVariables;
    this.nbInstances = nbInstances;
    this.singleNodes = new LatticeNode[nbVariables];
    this.all         = new LatticeNode(this, nbValuesForAttribute);

    // Initialise the first nodes of the lattice (i.e., the ones corresponding to single variables
    for (int a = 0; a < nbVariables; a++) {
      int[] variablesNumbers = { a };
      LatticeNode node = new LatticeNode(this, variablesNumbers, nbValuesForAttribute, convData[a], all);
      singleNodes[a] = node;
    }
  }

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Getters
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /** @return the number of variables that are modelled by this lattice.  */
  public int getNbVariables(){return this.nbVariables;}

  public int getNbInstances(){return this.nbInstances;}

  public LatticeNode getAll(){return this.all;}

  public LatticeNode[] getSingleNodes(){return this.singleNodes;}

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Methods
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /**
   * Get a node of the lattice from its integer set representation (e.g.,
   * {0,4,8} for the node representing the correlation of variables 0,4 and 8
   * in the dataset).
   *
   * @param clique
   *            the list of variable
   * @return the node of the lattice
   */
  public LatticeNode getNode(BitSet clique) {
    int[] variables = new int[clique.cardinality()];
    int current =0;
    for (int i = clique.nextSetBit(0); i >= 0; i = clique.nextSetBit(i+1)) {
      variables[current]=i;
      current++;
    }
    return getNode(variables);
  }

  /**
   * Get a node of the lattice from its integer set representation -- sorted
   * array of integers (e.g., [0,4,8] for the node representing the
   * correlation of variables 0,4 and 8 in the dataset).
   *
   * @param variables
   *            the list of variable
   * @return the node of the lattice
   */
  public LatticeNode getNode(int[] variables) {
    LatticeNode node = singleNodes[variables[0]];
    for (int i = 1; i < variables.length; i++) { node = node.getChild(variables[i], this); }
    return node;
  }

  protected BitSet getSetForVariable(int variableIndex, int valueIndex) {
    return singleNodes[variableIndex].getSet(valueIndex);
  }

  /*
     protected BitSet getSetForPairOfVariables(int variableIndex1, int valueIndex1, int variableIndex2, int valueIndex2) {
     LatticeNode pairNode = singleNodes[variableIndex1].getChild( variableIndex2, this);
     return pairNode.getSet(valueIndex1, valueIndex2);
     }

     public Lattice(BitSet[][] presence,int nbInstances) {
// ~ initialise internal structure for counting (TID sets)
this.nbInstances = nbInstances;
this.nbVariables = presence.length;

int[] nbValuesForAttribute = new int[nbVariables];
for (int a = 0; a < nbVariables; a++) {
nbValuesForAttribute[a] = presence[a].length;
}

// initialise the first nodes of the lattice (i.e., the ones
// corresponding to single variables
this.all = new LatticeNode(this, nbValuesForAttribute);
this.singleNodes = new LatticeNode[nbVariables];
for (int a = 0; a < nbVariables; a++) {
int[] variablesNumbers = { a };
LatticeNode node = new LatticeNode(this,
variablesNumbers, nbValuesForAttribute,
presence[a], all);
singleNodes[a] = node;
}
     }
     */

}
