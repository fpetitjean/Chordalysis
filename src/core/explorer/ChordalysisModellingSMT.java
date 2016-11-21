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

import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.scorer.GraphActionScorer;
import core.stats.scorer.GraphActionScorerPValue;

/**
 * This class searches a statistically significant decomposable model to explain a dataset using Prioritized Chordalysis. 
 * It uses Stepwise Multiple Testing, accepted for publication at KDD 2016. 
 * See paper "A multiple test correction for streams and cascades of statistical hypothesis tests," KDD 2016
 * See paper "Scaling log-linear analysis to high-dimensional data," ICDM 2013
 * See paper "Scaling log-linear analysis to datasets with thousands of variables," SDM 2015
 * @see http://www.francois-petitjean.com/Research/
 */
public class ChordalysisModellingSMT extends ChordalysisModeller{

  double pValueThreshold;
  /**
   * Default constructor
   * 
   * @param pValueThreshold
   *            minimum p-value for statistical consistency (commonly 0.05)
   */
  public ChordalysisModellingSMT(Data data, double pValueThreshold) {
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
    double remainingBudget = pValueThreshold;
    int step=0;
    while (!pq.isEmpty()&& step<maxNSteps) {
      int nTests = pq.size();

      double correctedPValueThreshold = remainingBudget / nTests;

      // System.out.println(pq);
      ScoredGraphAction todo = pq.poll();

      if (todo.getScore()> correctedPValueThreshold) {
        break;
      }
      double usedBudget = todo.getScore()*nTests;
      remainingBudget -= usedBudget;
      operationsPerformed.add(todo);
      bestModel.performAction(todo, bestModel, pq);
      step++;
    }
  }

}
