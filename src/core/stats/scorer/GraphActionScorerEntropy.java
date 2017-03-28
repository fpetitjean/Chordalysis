/*******************************************************************************
 * Copyright (C) 2014 Francois Petitjean
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

public class GraphActionScorerEntropy extends GraphActionScorer {
	int nbInstances;
	EntropyComputer entropyComputer;
	int maxK;
	boolean normaliseByNumberParameters;
	
	public GraphActionScorerEntropy(EntropyComputer entropyComputer, int maxK){
		this.entropyComputer = entropyComputer;
		this.nbInstances = entropyComputer.getNbInstances();
		this.maxK = maxK;
		this.normaliseByNumberParameters = false;
	}
	public GraphActionScorerEntropy(EntropyComputer entropyComputer, int maxK,boolean normalizeByNumberParameters){
		this.entropyComputer = entropyComputer;
		this.nbInstances = entropyComputer.getNbInstances();
		this.maxK = maxK;
		this.normaliseByNumberParameters = normalizeByNumberParameters;
	}

	@Override
	public ScoredGraphAction scoreEdge(DecomposableModel model, GraphAction action) {
		Double score;
		int treeWidthIfAdding = model.treeWidthIfAdding(action.getV1(), action.getV2());
		if(treeWidthIfAdding>maxK){
			score=Double.POSITIVE_INFINITY;
		}else{
			if(normaliseByNumberParameters){
				long dfDiff = model.nbParametersDiffIfAdding(action.getV1(),action.getV2());
				score = dfDiff/model.entropyDiffIfAdding(action.getV1(),action.getV2(), entropyComputer);
			}else{
				score = 1.0/model.entropyDiffIfAdding(action.getV1(),action.getV2(), entropyComputer);
			}
		}
		ScoredGraphAction scoredAction = new ScoredGraphAction(action.getType(),action.getV1(), action.getV2(), score);
		return scoredAction;
		
	}

}
