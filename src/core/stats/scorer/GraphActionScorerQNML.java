/*******************************************************************************
 * Copyright (C) 2017 Joan Capdevila Pujol
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

import core.model.DecomposableModel;
import core.model.GraphAction;
import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.RegretComputer;

public class GraphActionScorerQNML extends GraphActionScorer{

    int nbInstances;
    double lognbInstances;
    EntropyComputer entropyComputer;
    RegretComputer regretComputer;
    int maxK;

    public GraphActionScorerQNML(EntropyComputer entropyComputer, RegretComputer regretComputer){
        this.regretComputer = regretComputer;
        this.entropyComputer = entropyComputer;
        this.nbInstances = regretComputer.getNbInstances();
        this.lognbInstances = Math.log(this.nbInstances);
    }


    @Override
    public ScoredGraphAction scoreEdge(DecomposableModel model, GraphAction action) {
        Double score;

        double dregret = model.regretDiffIfAdding(action.getV1(), action.getV2(), regretComputer);

        double dloglike = - nbInstances * model.entropyDiffIfAdding(action.getV1(), action.getV2(), entropyComputer);

        score = dloglike + dregret;

        ScoredGraphAction scoredAction = new ScoredGraphAction(action.getType(), action.getV1(), action.getV2(), score);

        return scoredAction;

    }

    public Object clone(){
        return new GraphActionScorerQNML((EntropyComputer)this.entropyComputer.clone(), (RegretComputer) this.regretComputer.clone());
    }
}
