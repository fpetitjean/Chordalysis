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
package core.tools;

import org.apache.commons.math3.special.Gamma;

public class ChiSquared {
  public static double pValue(double chiSquared, long nbDegreesOfFreedom, double epsilon) {
    if (chiSquared <= 0.0) {
      return 1.0;
    } else {
      return 1.0 - Gamma.regularizedGammaP(nbDegreesOfFreedom / 2.0, chiSquared / 2.0, epsilon, Integer.MAX_VALUE);
    }
  }

  public static double pValue(double chiSquared, long nbDegreesOfFreedom) {
    return pValue(chiSquared, nbDegreesOfFreedom, 1e-20);
  }

}
