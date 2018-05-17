/*******************************************************************************
 * Copyright (C) 2017 Joan Capdevila Pujol
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

import core.lattice.Lattice;
import core.lattice.LatticeNode;
import demo.Run;

import java.util.BitSet;
import java.util.HashMap;

import static org.apache.commons.math3.util.FastMath.log;


public class RegretComputer {

    public static long nbCellsEverParsed;

    HashMap<BitSet, Double> lookup;
    double[] reg;
    double[] logreg;

    Lattice lattice;
    int nbInstances;

    public RegretComputer(Lattice lattice) {
        this.lookup = new HashMap<BitSet, Double>();
        this.lattice = lattice;
        this.nbInstances = this.lattice.getNbInstances();
        lookup.put(new BitSet(lattice.getNbVariables()), 0.0);
        int dim = (int)Math.pow(2.,20.);
        this.reg = new double[dim];
        this.logreg = new double[dim];

        reg[1] = 1.0;
        logreg[1] = 0.0;

        double c = nbInstances*(nbInstances-1.)/2.;
        for(int r = 0; r <= nbInstances; r++){
            reg[2] += c * Math.pow(1.0*r/nbInstances, r) * Math.pow((1.0*nbInstances-r)/nbInstances, nbInstances-r);
        }

        logreg[2] = Math.log(reg[2]);


        for(int k = 3; k < reg.length; k++){
            double a = Math.max(logreg[k-1], logreg[k-2]);
            logreg[k] = a + Math.log(Math.exp(logreg[k-1] - a) + 1.0 * nbInstances/k*Math.exp(logreg[k-2] - a));
        }

    }

    public Double computeRegret(BitSet clique){
        Double computedRegret = lookup.get(clique);
        if (computedRegret != null) {
            return computedRegret;
        }

        LatticeNode node = lattice.getNode(clique);
        int nbCells = node.getNbCells();

        double regret = getLogRegret(nbCells);

        lookup.put(clique, regret);
        return regret;
    }

    protected double getLogRegret(int u){
        if(u==0) throw new RuntimeException("Should not call getRegret with 0 outcomes");
        return logreg[u];
    }

    public int getNbInstances(){
        return nbInstances;
    }

    public Object clone(){
        return new RegretComputer(new Lattice(this.lattice));
    }

}
