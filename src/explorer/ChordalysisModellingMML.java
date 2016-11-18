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
package explorer;

import model.ScoredGraphAction;
import stats.MessageLengthFactorialComputer;
import stats.scorer.GraphActionScorer;
import stats.scorer.GraphActionScorerMML;

/**
 * This class searches a statistically significant decomposable model to explain
 * a dataset. See paper
 * "A statistically efficient and scalable method for log-linear analysis of high-dimensional data, ICDM 2014"
 * See paper
 * "Scaling log-linear analysis to datasets with thousands of variables, SDM 2015"
 * 
 * @see http://www.francois-petitjean.com/Research/
 */
public class ChordalysisModellingMML extends ChordalysisModeller {

	MessageLengthFactorialComputer computer;
	@Override
	protected GraphActionScorer initScorer() {
		this.computer = new MessageLengthFactorialComputer(this.lattice);
		return new GraphActionScorerMML(computer);
	}


	protected double getMMLGraphStructure(int nEdges) {
		int nVariables = lattice.getNbVariables();
		int maxNEdges = (int) (nVariables * (nVariables - 1) / 2.0);
		double MML = 0.0;
		MML += computer.getLogFromTable(1 + maxNEdges);
		MML += computer.getLogFactorials()[maxNEdges];
		MML -= computer.getLogFactorials()[nEdges];
		MML -= computer.getLogFactorials()[maxNEdges - nEdges];
		return MML;
	}

	@Override
	public void explore() {
		pq.processStoredModifications();
		int nVariables = lattice.getNbVariables();
		int maxNEdges = (int) (nVariables * (nVariables - 1) / 2.0);
		int nEdgesReferenceModel = 0;

		double MMLRef = bestModel.getMessageLength(computer);
		// correction for graph structure
		double MMLGraphStructureRef = computer.getLogFromTable(1 + maxNEdges);
		double fullMMLRef = MMLRef + MMLGraphStructureRef;
		int step=0;
		while (!pq.isEmpty()&& step<maxNSteps) {
			ScoredGraphAction todo = pq.poll();
			double MMLCandidate = MMLRef + todo.getScore();
			double MMLGraphStructureCandidate = getMMLGraphStructure(nEdgesReferenceModel + 1);
			double fullMMLCandidate = MMLCandidate + MMLGraphStructureCandidate;
			if (fullMMLCandidate >= fullMMLRef) {
				break;
			}
			operationsPerformed.add(todo);
			bestModel.performAction(todo, bestModel, pq);
			nEdgesReferenceModel++;
			MMLGraphStructureRef = MMLGraphStructureCandidate;
			MMLRef = MMLCandidate;
			fullMMLRef = fullMMLCandidate;
			step++;
		}
	}

}
