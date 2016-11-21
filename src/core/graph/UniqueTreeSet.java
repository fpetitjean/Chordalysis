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

import java.util.Collection;
import java.util.TreeSet;

public class UniqueTreeSet<L> extends TreeSet<L> {

  private static final long serialVersionUID = 8083106570463160149L;

  public UniqueTreeSet(){
    super();
  }

  public UniqueTreeSet(Collection<L> collection) {
    super(collection);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    return false;
  }
}
