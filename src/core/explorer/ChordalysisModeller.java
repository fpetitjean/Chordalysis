/*******************************************************************************
 * Copyright (C) 2016 Francois Petitjean
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
package core.explorer;

import java.util.ArrayList;

import core.lattice.Lattice;
import core.model.DecomposableModel;
import core.model.GraphAction;
import core.stats.MyPriorityQueue;
import core.stats.scorer.GraphActionScorer;

/**
 * Main abstract class defined for all Chordalysis explorers
 */
public abstract class ChordalysisModeller{

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Fields
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Loaded from data:
  protected DecomposableModel bestModel;
  protected Lattice           lattice;
  protected MyPriorityQueue   pq;
  // Others
  protected ArrayList<GraphAction>  operationsPerformed;
  protected GraphActionScorer       scorer;
  protected int                     maxNSteps;

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Constructors
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /** Prevent call of the default constructor. */
  @SuppressWarnings("unused")   // No warning for 'unused', as we do not want to use this one
  private ChordalysisModeller(){
    super();
    // Throw an exception if this ever *is* called
    throw new AssertionError("Instantiating utility class.");
  }

  /** Must be call with data by subclasses. */
  protected ChordalysisModeller(Data data){
    super();
    // From data:
    this.bestModel            = data.bestModel;
    this.lattice              = data.lattice;
    // Other:
    this.maxNSteps            = Integer.MAX_VALUE;
    this.operationsPerformed  = new ArrayList<GraphAction>();
    // Warning: null pointers
    this.scorer               = null; // Must be init by subclasses
    this.pq                   = null;
  }

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Getters
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /** @return The Decomposable model that has been built. */
  public DecomposableModel getModel() { return bestModel; }

  /** @return The lattice. */
  public Lattice getLattice() { return lattice; }

  /** @Return The number of instances, querid from the lattice. */
  public int getNbInstances() { return lattice.getNbInstances(); }

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Setters
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  public void setMaxNSteps(int nSteps){ this.maxNSteps = nSteps; }

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Abstract methods
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  public abstract void explore();

  protected abstract GraphActionScorer initScorer();

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Methods
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  /** Launch the modelling. */
  public void buildModel(){
    buildModelNoExplore();
    explore();
  }

  /** Finish building the model, but do not explore it. */
  public void buildModelNoExplore(){
    // --- 1 ---
    int vl      = this.lattice.getNbVariables();
    this.scorer = initScorer();
    this.pq     = new MyPriorityQueue(vl, bestModel, scorer);
    // --- 2 ---
    for (int i = 0; i < vl; i++) {
      for (int j = i + 1; j < vl; j++) {
        pq.enableEdge(i, j);
      }
    }
  }

  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
  // Internal class
  // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

  public static class Data {
    // --- --- --- Fields
    private DecomposableModel bestModel;
    private Lattice           lattice;

    // --- --- --- Contructor
    public Data(DecomposableModel bm, Lattice l){
      this.bestModel  = bm;
      this.lattice    = l;
    }
  }










}
