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

import model.ScoredGraphAction;
import stats.EntropyComputer;
import stats.scorer.GraphActionScorer;
import stats.scorer.GraphActionScorerPValue;

/**
 * This class searches a statistically significant decomposable model to explain a dataset using Prioritized Chordalysis. 
 * See paper "Scaling log-linear analysis to high-dimensional data," ICDM 2013
 * See paper "Scaling log-linear analysis to datasets with thousands of variables," SDM 2015
 * See paper "A multiple test correction for streams and cascades of statistical hypothesis tests," KDD 2016
 * @see http://www.francois-petitjean.com/Research/
 */
public class ChordalysisModellingBudget extends ChordalysisModeller{

	double pValueThreshold;
	double budgetShare = 0.01;
	

	/**
	 * Default constructor
	 * 
	 * @param pValueThreshold
	 *            minimum p-value for statistical consistency (commonly 0.05)
	 *            @param budgetShare share of the statistical budget to consume at each step (>0 and <=1; 0.01 seems like a reasonable value for most datasets)
	 */
	public ChordalysisModellingBudget(double pValueThreshold,double budgetShare) {
		super();
		this.pValueThreshold = pValueThreshold;
		this.budgetShare = budgetShare;
		if(budgetShare<=0 || budgetShare>1){
			throw new RuntimeException("budgetShare has to be within ]0,1]");
		}
	}

	@Override
	protected GraphActionScorer initScorer() {
		EntropyComputer entropyComputer = new EntropyComputer(this.lattice);
		return new GraphActionScorerPValue(entropyComputer);
	}

	@Override
	public void explore() {
	    pq.processStoredModifications();
	    double remainingBudget = pValueThreshold;
	    int step=0;
		while (!pq.isEmpty()&& step<maxNSteps) {
			int nbTests = pq.size();
			double budgetToUse = budgetShare * remainingBudget;
			double correctedPValueThreshold = budgetToUse / nbTests;
			remainingBudget -= budgetToUse;

			// System.out.println(pq);
			ScoredGraphAction todo = pq.poll();
		
			if (todo.getScore()> correctedPValueThreshold) {
				break;
			}
			operationsPerformed.add(todo);
			bestModel.performAction(todo, bestModel, pq);
			step++;
		}
	}

}
