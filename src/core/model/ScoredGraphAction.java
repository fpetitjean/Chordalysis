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
package core.model;

/**
 * This class represent a scored action of addition/deletion of an edge to/from the graph. 
 *
 */
public class ScoredGraphAction extends GraphAction{
  protected double score;
  public ScoredGraphAction(ActionType type, int v1, int v2,double score) {
    super(type,v1,v2);
    this.score = score;
  }

  public ScoredGraphAction(ActionType type, Couple<Integer>edge,double score) {
    super(type,edge);
    this.score = score;
  }

  @Override
  public int compareTo(GraphAction o) {
    if(o instanceof ScoredGraphAction){
      ScoredGraphAction os = (ScoredGraphAction)o;
      int res = Double.compare(this.score, os.score);
      if(res==0){
        res = super.compareTo(os);
      }
      return res;
    }else{
      return super.compareTo(o);
    }
  }

  public double getScore(){
    return score;
  }

  public String toString(){
    return super.toString()+"\t"+score;
    //		return super.toString();
  }


}
