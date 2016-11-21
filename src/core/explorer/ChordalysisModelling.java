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
package core.explorer;

import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.scorer.GraphActionScorer;
import core.stats.scorer.GraphActionScorerPValue;

/**
 * This class searches a statistically significant decomposable model to explain a dataset using Prioritized Chordalysis. 
 * See paper "Scaling log-linear analysis to high-dimensional data, ICDM 2013"
 * See paper "Scaling log-linear analysis to datasets with thousands of variables, SDM 2015"
 * @see http://www.francois-petitjean.com/Research/
 * Note that this is superseeded by {@link ChordalysisModellingBudget} with budget {@link ChordalysisModellingBudget#budgetShare}=0.5
 */
@Deprecated
public class ChordalysisModelling extends ChordalysisModeller{

  double pValueThreshold;
  EntropyComputer entropyComputer;

  /**
   * Default constructor
   * 
   * @param pValueThreshold
   *            minimum p-value for statistical consistency (commonly 0.05)
   */
  public ChordalysisModelling(Data data, double pValueThreshold) {
    super(data);
    this.pValueThreshold = pValueThreshold;
  }

  @Override
  protected GraphActionScorer initScorer() {
    EntropyComputer entropyComputer = new EntropyComputer(this.lattice);
    return new GraphActionScorerPValue(entropyComputer);
  }

  @Override
  public void explore() {
    pq.processStoredModifications();
    int step=0;
    while (!pq.isEmpty() && step<maxNSteps) {
      int nbTests = pq.size();
      double correctedPValueThreshold = (pValueThreshold / Math.pow(2, step)) / nbTests;

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
