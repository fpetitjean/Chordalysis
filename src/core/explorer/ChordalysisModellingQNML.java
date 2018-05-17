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

package core.explorer;

import core.model.ScoredGraphAction;
import core.stats.EntropyComputer;
import core.stats.RegretComputer;
import core.stats.scorer.GraphActionScorer;
import core.stats.scorer.GraphActionScorerQNML;

/**
 * Created by Joan Capdevila on 11/07/17.
 */
public class ChordalysisModellingQNML extends ChordalysisModeller {
    boolean normaliseByBIC;

    /**
     * Constructor
     * @param data the data
     */

    public ChordalysisModellingQNML(Data data) {
        super(data);
    }


    @Override
    protected GraphActionScorer initScorer() {
        RegretComputer regretComputer = new RegretComputer(this.lattice);
        EntropyComputer entropyComputer = new EntropyComputer(this.lattice);
        return new GraphActionScorerQNML(entropyComputer, regretComputer);
    }

    @Override
    public void explore() {
        pq.processStoredModifications();
        int step = 0;
        while (!pq.isEmpty() && step < maxNSteps) {

            ScoredGraphAction todo = pq.poll();
            //System.out.println(todo.toString());

            if (todo.getScore() == Double.POSITIVE_INFINITY || todo.getScore() >= 0) {
                break;
            }

            operationsPerformed.add(todo);
            bestModel.performAction(todo, bestModel, pq);

            step++;
        }
    }
}
