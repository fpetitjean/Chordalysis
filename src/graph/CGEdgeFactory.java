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
package graph;

import java.util.TreeSet;

import org.jgrapht.EdgeFactory;

import tools.SortedSets;

public class CGEdgeFactory implements EdgeFactory<TreeSet<Integer>, UniqueTreeSet<Integer>> {
	static CGEdgeFactory singleton = new CGEdgeFactory();
	@Override
	public UniqueTreeSet<Integer> createEdge(TreeSet<Integer> arg0,TreeSet<Integer> arg1) {
		return new UniqueTreeSet<Integer>(SortedSets.intersection(arg0, arg1));
	}
	
	public static CGEdgeFactory getInstance(){
		return singleton;
	}

}
