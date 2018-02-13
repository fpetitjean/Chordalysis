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
package demo;

import java.io.IOException;
import java.util.BitSet;

import org.jgrapht.graph.SimpleGraph;

import core.graph.ChordalGraph;
import core.graph.CliqueGraphEdge;
import core.graph.UniqueTreeSet;

public class DemoJunctionTree {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		//model [ABC][BCD][DE][EFG][FGH][GI]
		//model [012][123][34][456][567][68]
		
		ChordalGraph graph = new ChordalGraph();
		for (int i = 0; i < 9; i++) { //9 vertices
			graph.addVertex(i);	
		}
		graph.initStructures();
		
		//only use addSecuredEdge if you know the final graph is chordal
		
		//ABC
		graph.addSecuredEdge(0, 1);
		graph.addSecuredEdge(0, 2);
		graph.addSecuredEdge(1, 2);
		
		//BCD
		graph.addSecuredEdge(1, 3);
		graph.addSecuredEdge(2, 3);

		//DE
		graph.addSecuredEdge(3, 4);

		//EFG
		graph.addSecuredEdge(4, 5);
		graph.addSecuredEdge(4, 6);
		graph.addSecuredEdge(5, 6);

		//FGH
		graph.addSecuredEdge(5, 7);
		graph.addSecuredEdge(6, 7);

		//GI
		graph.addSecuredEdge(6, 8);
		
		SimpleGraph<BitSet, UniqueTreeSet<Integer>> cliqueGraph = graph.getCliqueGraph();
		SimpleGraph<BitSet, CliqueGraphEdge> junctionTree = graph.getJunctionTree();

		System.out.println(graph);
		System.out.println("nEdges in clique graph = "+cliqueGraph.edgeSet().size());
		System.out.println("nEdges in junction tree = "+junctionTree.edgeSet().size());
		System.out.println("(junction tree eliminates edge between [567] and [68])");
		
		System.out.println(junctionTree);
		
		

	}

	

}
