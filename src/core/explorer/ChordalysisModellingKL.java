/*******************************************************************************
 * Copyright (C) 2017 Francois Petitjean
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

import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.scorer.GraphActionScorer;
import core.stats.scorer.GraphActionScorerEntropy;

/**
 * This class searches a decomposable model to explain a dataset using
 * Prioritized Chordalysis. The function optimized is the Kullbach Leibler
 * divergence. See paper "Scaling log-linear analysis to datasets with thousands
 * of variables," SDM 2015
 * 
 * @see http://www.francois-petitjean.com/Research/
 */
public class ChordalysisModellingKL extends ChordalysisModeller {

	int maxK;

	public ChordalysisModellingKL(Data data, int maxK) {
		super(data);
		this.maxK = maxK;
	}

	@Override
	protected GraphActionScorer initScorer() {
		EntropyComputer entropyComputer = new EntropyComputer(this.lattice);
		return new GraphActionScorerEntropy(entropyComputer, maxK);
	}

	@Override
	public void explore() {
		pq.processStoredModifications();
		int step = 0;
		while (!pq.isEmpty() && step < maxNSteps) {
			ScoredGraphAction todo = pq.poll();

			if (todo.getScore() == Double.POSITIVE_INFINITY) {
				break;
			}
			operationsPerformed.add(todo);
			bestModel.performAction(todo, bestModel, pq);
			step++;
		}
	}

}
