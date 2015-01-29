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
package tools;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class SortedSets {
	public static <T extends Comparable<T>> NavigableSet<T> intersection(NavigableSet<T> set1, NavigableSet<T> set2) {
		NavigableSet<T> res = new TreeSet<T>();

		Iterator<T> it1 = set1.descendingIterator();
		Iterator<T> it2 = set2.descendingIterator();

		T first1 = null, first2 = null;
		if (it1.hasNext()) {
			first1 = it1.next();
		} else {
			return res;
		}
		if (it2.hasNext()) {
			first2 = it2.next();
		} else {
			return res;
		}

		while (it1.hasNext() && it2.hasNext()) {
			int test = first1.compareTo(first2);
			if (test < 0) {
				first2 = it2.next();
			} else if (test > 0) {
				first1 = it1.next();
			} else {
				res.add(first1);
				first1 = it1.next();
				first2 = it2.next();
			}
		}

		return res;
	}

	public static TreeSet<Integer> intersection(TreeSet<Integer> set1, TreeSet<Integer> set2) {
		
		final int maxResLength = (set1.size() < set2.size()) ? set1.size() : set2.size();
		if (maxResLength == 0) {
			if (set1.size() == 0) {
				return set1;
			} else {
				return set2;
			}
		}
		TreeSet<Integer> res = new TreeSet<Integer>();

		Iterator<Integer> it1 = set1.descendingIterator();
		Iterator<Integer> it2 = set2.descendingIterator();

		Integer first1 = null, first2 = null;
		if (it1.hasNext()) {
			first1 = it1.next();
		} else {
			return res;
		}
		if (it2.hasNext()) {
			first2 = it2.next();
		} else {
			return res;
		}
		boolean keepTesting = true;
		while (keepTesting) {
			int test = first1.compareTo(first2);
			if (test < 0) {
				if (it2.hasNext()) {
					first2 = it2.next();
				} else {
					keepTesting = false;
				}
			} else if (test > 0) {
				if (it1.hasNext()) {
					first1 = it1.next();
				} else {
					keepTesting = false;
				}
			} else {
				res.add(first1);
				if(it1.hasNext() && it2.hasNext()){
					first1 = it1.next();
					first2 = it2.next();
				}else{
					keepTesting = false;
				}
			}
		}

		return res;

	}
	
	

	public static final int[] intersection(final int[] set1, final int[] set2) {
		final int maxResLength = (set1.length < set2.length) ? set1.length : set2.length;
		if (maxResLength == 0) {
			if (set1.length == 0) {
				return set1;
			} else {
				return set2;
			}
		}

		int[] res = new int[maxResLength];

		int i1 = 0;
		int i2 = 0;
		int iRes = 0;

		while (i1 < set1.length && i2 < set2.length) {
			if (set1[i1] < set2[i2]) {
				i1++;
			} else if (set1[i1] == set2[i2]) {
				res[iRes] = set1[i1];
				iRes++;
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		res = Arrays.copyOf(res, iRes);

		return res;
	}

	public static final int intersectionSize(final int[] set1, final int[] set2) {
		final int maxResLength = (set1.length < set2.length) ? set1.length : set2.length;
		if (maxResLength == 0) {
			return 0;
		}

		int res = 0;

		int i1 = 0;
		int i2 = 0;

		while (i1 < set1.length && i2 < set2.length) {
			if (set1[i1] < set2[i2]) {
				i1++;
			} else if (set1[i1] == set2[i2]) {
				res++;
				i1++;
				i2++;
			} else {
				i2++;
			}
		}

		return res;
	}

	public static <T extends Comparable<T>> int intersectionSize(NavigableSet<T> set1, NavigableSet<T> set2) {
		int res = 0;

		Iterator<T> it1 = set1.descendingIterator();
		Iterator<T> it2 = set2.descendingIterator();

		T first1 = null, first2 = null;
		if (it1.hasNext()) {
			first1 = it1.next();
		} else {
			return res;
		}
		if (it2.hasNext()) {
			first2 = it2.next();
		} else {
			return res;
		}
		boolean keepTesting = true;
		while (keepTesting) {
			int test = first1.compareTo(first2);
			if (test < 0) {
				if (it2.hasNext()) {
					first2 = it2.next();
				} else {
					keepTesting = false;
				}
			} else if (test > 0) {
				if (it1.hasNext()) {
					first1 = it1.next();
				} else {
					keepTesting = false;
				}
			} else {
				res++;
				if(it1.hasNext() && it2.hasNext()){
					first1 = it1.next();
					first2 = it2.next();
				}else{
					keepTesting = false;
				}
			}
		}

		return res;
	}

	public static <T extends Comparable<T>> int unionSize(NavigableSet<T> set1, NavigableSet<T> set2) {
		int res = 0;

		Iterator<T> it1 = set1.descendingIterator();
		Iterator<T> it2 = set2.descendingIterator();

		T first1 = null, first2 = null;
		if (it1.hasNext()) {
			first1 = it1.next();
		} else {
			set2.size();
		}
		if (it2.hasNext()) {
			first2 = it2.next();
		} else {
			set1.size();
		}

		while (it1.hasNext() && it2.hasNext()) {
			final int test = first1.compareTo(first2);
			if (test < 0) {
				first2 = it2.next();
			} else if (test > 0) {
				first1 = it1.next();
			} else {
				first1 = it1.next();
				first2 = it2.next();
			}
			res++;
		}

		while (it1.hasNext()) {
			res++;
			it1.next();
		}
		while (it2.hasNext()) {
			res++;
			it2.next();
		}

		return res;
	}

	@SafeVarargs
	public static <T extends Comparable<T>> TreeSet<T> union(TreeSet<T>... sets) {
		// not optimized
		TreeSet<T> res = new TreeSet<T>();
		for (TreeSet<T> set : sets) {
			res.addAll(set);
		}
		return res;
	}

}
