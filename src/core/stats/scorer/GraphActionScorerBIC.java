/*******************************************************************************
 * Copyright (C) 2014 Joan Capdevila Pujol
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

public class GraphActionScorerBIC extends GraphActionScorer {
	int nbInstances;
	double lognbInstances;
	EntropyComputer entropyComputer;
	int maxK;

	public GraphActionScorerBIC(EntropyComputer entropyComputer, int maxK){
		this.entropyComputer = entropyComputer;
		this.nbInstances = entropyComputer.getNbInstances();
		this.lognbInstances = Math.log(this.nbInstances);
		this.maxK = maxK;
	}


	@Override
	public ScoredGraphAction scoreEdge(DecomposableModel model, GraphAction action) {
		Double score;
		int treeWidthIfAdding = model.treeWidthIfAdding(action.getV1(), action.getV2());
		if(treeWidthIfAdding>maxK){
			score = Double.POSITIVE_INFINITY;
		}else{
			double df = model.nbParametersDiffIfAdding(action.getV1(),action.getV2());
			double dentropy = model.entropyDiffIfAdding(action.getV1(), action.getV2(), entropyComputer);
			double dbic = -2*nbInstances*dentropy+df*lognbInstances;
			score = dbic;

		}
		ScoredGraphAction scoredAction = new ScoredGraphAction(action.getType(),action.getV1(), action.getV2(), score);
		return scoredAction;
		
	}

}
