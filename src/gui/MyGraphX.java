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
package gui;

import java.util.HashMap;

import com.mxgraph.view.mxGraph;

/**
 * The graphical representation of a graph
 */
public class MyGraphX extends mxGraph {

	private HashMap<Integer, Object> vertexToCellMap = new HashMap<Integer, Object>();
	

	public MyGraphX() {
		super();
	}

	public void addVertex(int id,String vertex) {

		getModel().beginUpdate();

		try {
			vertexToCellMap.put(id, insertVertex(getDefaultParent(), null, vertex, 100, 100, 100, 30));
		} finally {
			getModel().endUpdate();
		}
	}

	public void addEdge(int id1,int id2){
		getModel().beginUpdate();
		try {
			insertEdge(defaultParent,null,"", vertexToCellMap.get(id1), vertexToCellMap.get(id2),"startArrow=none;endArrow=none;");
			
		} finally {
			
			getModel().endUpdate();
		}
	}

}
