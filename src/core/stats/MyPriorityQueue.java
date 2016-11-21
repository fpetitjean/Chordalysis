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
package core.stats;

import java.util.ArrayList;
import java.util.PriorityQueue;

import core.model.Couple;
import core.model.DecomposableModel;
import core.model.GraphAction;
import core.model.GraphAction.ActionType;
import core.model.ScoredGraphAction;
import core.stats.scorer.GraphActionScorer;

public class MyPriorityQueue extends PriorityQueue<ScoredGraphAction> {

  private static final long serialVersionUID = 6863099646496386231L;
  DecomposableModel model;
  GraphActionScorer scorer;

  ScoredGraphAction[][] actionsByEdge;

  ArrayList<Couple<Integer>> toDelete;
  ArrayList<Couple<Integer>> toAdd;
  ArrayList<Couple<Integer>> toUpdate;

  public ArrayList<Integer> nbScoredEdgesPerStep = new ArrayList<Integer>();
  public ArrayList<Integer> nbEdgesAvailablePerStep = new ArrayList<Integer>();

  public MyPriorityQueue(int nbVariables, DecomposableModel model, GraphActionScorer scorer) {
    super(nbVariables * nbVariables / 2);
    this.model = model;
    this.scorer = scorer;
    actionsByEdge = new ScoredGraphAction[nbVariables][nbVariables];
    toDelete = new ArrayList<Couple<Integer>>();
    toAdd = new ArrayList<Couple<Integer>>();
    toUpdate = new ArrayList<Couple<Integer>>();
  }

  public void enableEdge(Integer a, Integer b) {
    toAdd.add(new Couple<Integer>(a, b));

  }

  public void updateEdge(Integer a, Integer b) {
    toUpdate.add(new Couple<Integer>(a, b));

  }

  public void disableEdge(Integer a, Integer b) {
    toDelete.add(new Couple<Integer>(a, b));
  }

  public void processStoredModifications() {
    int nbScoredEdges = 0;
    for (Couple<Integer> c : toDelete) {
      int a = c.getV1();
      int b = c.getV2();
      // System.out.println("removing ("+a+","+b+") from the queue");
      this.remove(actionsByEdge[a][b]);
      actionsByEdge[a][b] = null;
      actionsByEdge[b][a] = actionsByEdge[a][b];
    }

    for (Couple<Integer> c : toAdd) {
      int a = c.getV1();
      int b = c.getV2();
      // System.out.println("adding ("+a+","+b+") to the queue");
      ScoredGraphAction scoredEdge = scorer.scoreEdge(model, new GraphAction(ActionType.ADD, a, b));
      actionsByEdge[a][b] = scoredEdge;
      actionsByEdge[b][a] = actionsByEdge[a][b];
      this.offer(scoredEdge);
      nbScoredEdges++;
    }

    for (Couple<Integer> c : toUpdate) {
      int a = c.getV1();
      int b = c.getV2();
      // System.out.println("updating ("+a+","+b+") in the queue");
      this.remove(actionsByEdge[a][b]);
      ScoredGraphAction scoredEdge = scorer.scoreEdge(model, new GraphAction(ActionType.ADD, a, b));
      actionsByEdge[a][b] = scoredEdge;
      actionsByEdge[b][a] = actionsByEdge[a][b];

      this.offer(scoredEdge);
      nbScoredEdges++;
    }
    toDelete.clear();
    toAdd.clear();
    toUpdate.clear();

    nbScoredEdgesPerStep.add(nbScoredEdges);
    nbEdgesAvailablePerStep.add(this.size());

  }

  public String toString() {
    String res = "";
    ArrayList<ScoredGraphAction> polled = new ArrayList<ScoredGraphAction>();
    ScoredGraphAction a;
    while ((a = this.poll()) != null) {
      polled.add(a);
      res += a.toString() + "\n";
    }
    for (ScoredGraphAction action : polled) {
      this.offer(action);
    }
    return res;
  }

}
