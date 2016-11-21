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

package core.stats.scorer;

import core.stats.EntropyComputer;
import core.tools.ChiSquared;
import core.model.DecomposableModel;
import core.model.GraphAction;
import core.model.PValueScoredGraphAction;
import core.model.ScoredGraphAction;

public class GraphActionScorerPValue extends GraphActionScorer {

  EntropyComputer entropyComputer;
  @Deprecated
  public GraphActionScorerPValue(int nbInstances,EntropyComputer entropyComputer){
    this.entropyComputer = entropyComputer;
    this.nbInstances = nbInstances;
  }

  public GraphActionScorerPValue(EntropyComputer entropyComputer){
    this.entropyComputer = entropyComputer;
    this.nbInstances = this.entropyComputer.getNbInstances();
  }


  @Override
  public ScoredGraphAction scoreEdge(DecomposableModel model, GraphAction action) {

    Double diffEntropy;
    long dfDiff;
    //		System.out.println(model);
    //		System.out.println(action.getV1());
    //		System.out.println(action.getV2());
    diffEntropy = model.entropyDiffIfAdding(action.getV1(),action.getV2(), entropyComputer);
    dfDiff = model.nbParametersDiffIfAdding(action.getV1(),action.getV2());

    if (diffEntropy == null) {
      return new PValueScoredGraphAction(action.getType(),action.getV1(), action.getV2(), 1.0, dfDiff, Double.NaN);
    }
    double gDiff = 2.0 * this.nbInstances * (diffEntropy);
    double pValue = ChiSquared.pValue(gDiff, dfDiff);

    PValueScoredGraphAction scoredAction = new PValueScoredGraphAction(action.getType(),action.getV1(), action.getV2(), pValue, dfDiff, gDiff);
    return scoredAction;

  }

}
