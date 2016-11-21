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
package core.graph;

import java.util.BitSet;

import org.jgrapht.EdgeFactory;

public class CliqueGraphEdgeFactory implements EdgeFactory<BitSet, CliqueGraphEdge> {
  static CliqueGraphEdgeFactory singleton = new CliqueGraphEdgeFactory();
  @Override
  public CliqueGraphEdge createEdge(BitSet arg0,BitSet arg1) {
    return new CliqueGraphEdge(arg0, arg1);
  }

  public static CliqueGraphEdgeFactory getInstance(){
    return singleton;
  }

}
