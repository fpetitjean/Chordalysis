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
 * Comparable Couple (used to represent an edge)
 * @author petitjean
 * @param <T> the type of couple
 */
public class Couple<T extends Comparable<T>> implements Comparable<Couple<T>> {
	private T v1, v2;

	public Couple(T v1, T v2) {
		if(v1.compareTo(v2)<0){
			this.v1 = v1;
			this.v2 = v2;
		}else{
			this.v1 = v2;
			this.v2 = v1;
		}
	}
	
	

	@Override
	public int compareTo(Couple<T> o) {
		int res = v1.compareTo(o.v1);
		if (res == 0) {
			res = v2.compareTo(o.v2);
		}
		return res;
	}
	
	public String toString(){
		return "("+v1+"--"+v2+")";
	}



	public T getV1() {
		return v1;
	}



	public T getV2() {
		return v2;
	}

}
