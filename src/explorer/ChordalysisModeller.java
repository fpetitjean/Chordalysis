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
package explorer;

import java.io.IOException;
import java.util.ArrayList;

import lattice.Lattice;
import model.DecomposableModel;
import model.GraphAction;
import model.ScoredGraphAction;
import stats.EntropyComputer;
import stats.MyPriorityQueue;
import stats.scorer.GraphActionScorer;
import stats.scorer.GraphActionScorerPValue;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/**
 * Main abstract class defined for all Chordalysis explorers
 */
public abstract class ChordalysisModeller{

	DecomposableModel bestModel;
	protected Lattice lattice;
	Instances dataset;
	ArrayList<GraphAction> operationsPerformed;
	MyPriorityQueue pq;
	GraphActionScorer scorer;
	
	boolean hasMissingValues = true;
	public void setHasMissingValues(boolean hasMissingValues){
	    this.hasMissingValues = hasMissingValues;
	}
	
	int maxNSteps = Integer.MAX_VALUE;
	public void setMaxNSteps(int nSteps){
		this.maxNSteps = nSteps;
		System.out.println(maxNSteps);
	}

	/**
	 * Default constructor
	 */
	public ChordalysisModeller() {
		operationsPerformed = new ArrayList<GraphAction>();
	}

	/**
	 * Launch the modelling
	 * 
	 * @param dataset
	 *            the dataset from which the analysis is performed on
	 */
	public void buildModel(Instances dataset) {
		buildModelNoExplore(dataset);
		this.explore();
	}
	
	public int getNbInstances() {
		return lattice.getNbInstances();
	}
	
	public void buildModelNoExplore(Instances dataset) {
		this.dataset = dataset;
		int[] variables = new int[dataset.numAttributes()];
		int[] nbValuesForAttribute = new int[variables.length];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = i;
			if(hasMissingValues){
			    nbValuesForAttribute[i] = dataset.attribute(i).numValues()+1;
			}else{
			    nbValuesForAttribute[i] = dataset.attribute(i).numValues();
			}
		}
		this.lattice = new Lattice(dataset,hasMissingValues);
		this.scorer = initScorer();
		this.bestModel = new DecomposableModel(variables, nbValuesForAttribute);
		this.pq = new MyPriorityQueue(variables.length, bestModel, scorer);
		for (int i = 0; i < variables.length; i++) {
			for (int j = i + 1; j < variables.length; j++) {
				pq.enableEdge(i, j);
			}
		}
		

	}
	
	protected abstract GraphActionScorer initScorer();

	/**
	 * Launch the modelling
	 * 
	 * @param dataset the structure of the dataset which the analysis is performed
	 * @param 
	 * @throws IOException 
	 * 
	 */
	public void buildModel(Instances dataset,ArffReader loader) throws IOException {
		buildModelNoExplore(dataset, loader);
		this.explore();
	}
	
	public void buildModelNoExplore(Instances structure,ArffReader loader) throws IOException {
		this.dataset = structure;
		int[] variables = new int[structure.numAttributes()];
		int[] nbValuesForAttribute = new int[variables.length];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = i;
			nbValuesForAttribute[i] = structure.attribute(i).numValues();
		}
		this.lattice = new Lattice(structure,loader);
		
		this.scorer = initScorer();
		this.bestModel = new DecomposableModel(variables, nbValuesForAttribute);
		this.pq = new MyPriorityQueue(variables.length, bestModel, scorer);
		for (int i = 0; i < variables.length; i++) {
			for (int j = i + 1; j < variables.length; j++) {
				pq.enableEdge(i, j);
			}
		}
		

	}

	/**
	 * @return the Decomposable model that has been built
	 */
	public DecomposableModel getModel() {
		return bestModel;
	}
	
	public abstract void explore();
	
	public Lattice getLattice() {
		return lattice;
	}

}
