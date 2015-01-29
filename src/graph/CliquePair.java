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

public class CliquePair {
	public TreeSet<Integer> c1;
	public TreeSet<Integer> c2;
	public CliquePair(TreeSet<Integer>c1,TreeSet<Integer>c2){
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public String toString(){
		return "("+c1.toString()+" , "+c2.toString()+")";
	}
}
