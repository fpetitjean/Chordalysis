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
 * 
 * @author petitjean
 *
 */
public class PValueScoredGraphAction extends ScoredGraphAction {
	protected long nDF;
	protected double entropy;

	public PValueScoredGraphAction(ActionType type, int v1, int v2, double score, long nDF, double entropy) {
		super(type, v1, v2, score);
		this.nDF = nDF;
		this.entropy = entropy;
	}

	public PValueScoredGraphAction(ActionType type, Couple<Integer> edge, double score, long nDF, double entropy) {
		super(type, edge, score);
		this.nDF = nDF;
		this.entropy = entropy;
	}

	@Override
	public int compareTo(GraphAction o) {
		if (o instanceof PValueScoredGraphAction) {
			PValueScoredGraphAction os = (PValueScoredGraphAction) o;
			int res = Double.compare(this.score, os.score);
			if (res != 0) return res;
			res = Long.compare(this.nDF, os.nDF);
			if (res != 0) return res;
			res = -1 * (Double.compare(this.entropy, os.entropy));
			if (res != 0) return res;
		}
		return super.compareTo(o);
	}
	
	public String toString(){
		String res = super.toString();
		res+="p="+score+"\t";
		res+="df="+nDF+"\t";
		res+="H="+entropy;
		return res;
	}
	
	public double getEntropy(){
		return entropy;
	}
	
	public long getNDF(){
		return nDF;
	}
	
}
