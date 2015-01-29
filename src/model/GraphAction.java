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
package model;

/**
 * This class represent an action of addition/deletion of an edge to/from the graph
 *
 */
public class GraphAction implements Comparable<GraphAction> {
	public enum ActionType {
		REMOVE, ADD
	}

	protected ActionType type;
	protected Couple<Integer> edge;

	public GraphAction(ActionType type, int v1, int v2) {
		this.type = type;
		this.edge = new Couple<Integer>(v1, v2);
	}
	
	public GraphAction(ActionType type, Couple<Integer>edge) {
		this.type = type;
		this.edge = edge;
	}

	@Override
	public int compareTo(GraphAction o) {
		if(type==ActionType.REMOVE){
			if(o.type==ActionType.REMOVE){
				return edge.compareTo(o.edge);
			}else{
				return -1;
			}
		}else{
			if(o.type==ActionType.ADD){
				return edge.compareTo(o.edge);
			}else{
				return 1;
			}
		}
	}
	
	public int getV1(){
		return edge.getV1();
	}
	
	public int getV2(){
		return edge.getV2();
	}
	
	public String toString(){
		return type.name()+"\t"+edge.toString();
	}
	
	public ActionType getType(){
		return type;
	}


}
