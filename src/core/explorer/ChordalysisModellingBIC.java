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

import core.model.GraphAction;
import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.LoglikeComputer;
import core.stats.scorer.GraphActionScorer;
import core.stats.scorer.GraphActionScorerBIC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class searches a decomposable model to explain a dataset using
 * Prioritized Chordalysis. The function optimized is the Bayesian information Criteria (BIC).
 * See paper "Scaling log-linear analysis to datasets with thousands
 * of variables," SDM 2015
 *
 */

public class ChordalysisModellingBIC extends ChordalysisModeller {

	int maxK;
	boolean normaliseByBIC;

	/**
	 * Constructor
	 * @param data the data
	 * @param maxK the size of the maximum clique to be created
	 */
	public ChordalysisModellingBIC(Data data, int maxK) {
		super(data);
		this.maxK = maxK;
	}


	@Override
	protected GraphActionScorer initScorer() {
		EntropyComputer entropyComputer = new EntropyComputer(this.lattice);
		return new GraphActionScorerBIC(entropyComputer, maxK);
	}

	@Override
	public void explore() {
		pq.processStoredModifications();
		int step = 0;
		while (!pq.isEmpty() && step < maxNSteps) {

			ScoredGraphAction todo = pq.poll();
			if (todo.getScore() == Double.POSITIVE_INFINITY || todo.getScore() >= 0) {
				break;
			}

			operationsPerformed.add(todo);
			bestModel.performAction(todo, bestModel, pq);
			step++;

		}
	}
}
