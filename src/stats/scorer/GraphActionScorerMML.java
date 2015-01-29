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
package stats.scorer;

import model.DecomposableModel;
import model.GraphAction;
import model.ScoredGraphAction;
import stats.MessageLengthFactorialComputer;

public class GraphActionScorerMML extends GraphActionScorer {
	int nbInstances;
	MessageLengthFactorialComputer computer;
	public GraphActionScorerMML(int nbInstances,MessageLengthFactorialComputer computer){
		this.nbInstances = nbInstances;
		this.computer = computer;
	}

	@Override
	public ScoredGraphAction scoreEdge(DecomposableModel model, GraphAction action) {
		
		double diffLength =model.messageLengthDiffIfAdding(action.getV1(),action.getV2(), computer, false);
		ScoredGraphAction scoredAction = new ScoredGraphAction(action.getType(),action.getV1(), action.getV2(), diffLength);
		return scoredAction;
		
	}

}
