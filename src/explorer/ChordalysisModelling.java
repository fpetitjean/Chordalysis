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
 * This class searches a statistically significant decomposable model to explain a dataset using Prioritized Chordalysis. 
 * See paper "Scaling log-linear analysis to high-dimensional data, ICDM 2013"
 * See paper "Scaling log-linear analysis to datasets with thousands of variables, SDM 2015"
 * @see http://www.tiny-clues.eu/Research/
 */
public class ChordalysisModelling{

	int nbInstances;
	double pValueThreshold;
	DecomposableModel bestModel;
	EntropyComputer entropyComputer;
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
	 * 
	 * @param pValueThreshold
	 *            minimum p-value for statistical consistency (commonly 0.05)
	 */
	public ChordalysisModelling(double pValueThreshold) {
		this.pValueThreshold = pValueThreshold;
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
		return nbInstances;
	}
	
	public void buildModelNoExplore(Instances dataset) {
		this.nbInstances = dataset.numInstances();
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
		this.entropyComputer = new EntropyComputer(dataset.numInstances(), this.lattice);
		this.scorer = new GraphActionScorerPValue(nbInstances, entropyComputer);
		this.bestModel = new DecomposableModel(variables, nbValuesForAttribute);
		this.pq = new MyPriorityQueue(variables.length, bestModel, scorer);
		for (int i = 0; i < variables.length; i++) {
			for (int j = i + 1; j < variables.length; j++) {
				pq.enableEdge(i, j);
			}
		}
		

	}
	
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
	
	public void buildModelNoExplore(Instances dataset,ArffReader loader) throws IOException {
		this.dataset = dataset;
		int[] variables = new int[dataset.numAttributes()];
		int[] nbValuesForAttribute = new int[variables.length];
		for (int i = 0; i < variables.length; i++) {
			variables[i] = i;
			nbValuesForAttribute[i] = dataset.attribute(i).numValues();
		}
		this.lattice = new Lattice(dataset,loader);
		this.nbInstances = this.lattice.getNbInstances();
		
		
		this.entropyComputer = new EntropyComputer(nbInstances, this.lattice);
		this.scorer = new GraphActionScorerPValue(nbInstances, entropyComputer);
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

	public void explore() {
		pq.processStoredModifications();
		int step=0;
		while (!pq.isEmpty() && step<maxNSteps) {
			int nbTests = pq.size();
			double correctedPValueThreshold = (pValueThreshold / Math.pow(2, step)) / nbTests;

			ScoredGraphAction todo = pq.poll();
			if (todo.getScore()>= correctedPValueThreshold) {
				break;
			}
			operationsPerformed.add(todo);
			bestModel.performAction(todo, bestModel, pq);
			step++;
		}
	}

	public Lattice getLattice() {
		return lattice;
	}

}
