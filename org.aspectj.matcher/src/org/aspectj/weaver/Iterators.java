/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public final class Iterators {

	/**
	 * Private constructor, nobody should ever make one of these
	 */
	private Iterators() {
	}

	/**
	 * A getter represents a mapping function from Object to Iterator
	 */
	public interface Getter<A, B> {
		Iterator<B> get(A target);
	}

	/**
	 * A filter represents a mapping function from Iterator to Iterator
	 */
	public interface Filter<T> {
		Iterator<T> filter(Iterator<T> in);
	}

	/**
	 * Create a new filter F that, when wrapped around another iterator I, creates a new iterator I' that will return only those
	 * values of I that have not yet been returned by I', discarding duplicates.
	 */
	public static <T> Filter<T> dupFilter() {
		return new Filter<T>() {
			final Set<T> seen = new HashSet<T>(); // should have weak ptrs?

			public Iterator<T> filter(final Iterator<T> in) {
				return new Iterator<T>() {
					boolean fresh = false;
					T peek;

					public boolean hasNext() {
						if (fresh) {
							return true;
						}
						while (true) {
							if (!in.hasNext()) {
								return false;
							}
							peek = in.next();
							if (!seen.contains(peek)) {
								fresh = true;
								return true;
							} else {
								peek = null; // garbage collection
							}
						}
					}

					public T next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						T ret = peek;
						seen.add(peek);
						peek = null;
						fresh = false;
						return ret;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Creates an iterator that will return the elements of a specified array, in order. Like Arrays.asList(o).iterator(), without
	 * all that pesky safety.
	 */

	public static <T> Iterator<T> array(final T[] o) {
		return new Iterator<T>() {
			int i = 0;
			int len = (o == null) ? 0 : o.length;

			public boolean hasNext() {
				return i < len;
			}

			public T next() {
				if (i < len) {
					return o[i++];
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public static class ResolvedTypeArrayIterator implements Iterator<ResolvedType> {
		private ResolvedType[] array;
		private int index;
		private int len;
		private boolean wantGenerics;
		private List<String> alreadySeen; // type signatures

		public ResolvedTypeArrayIterator(ResolvedType[] array, List<String> alreadySeen, boolean wantGenerics) {
			assert array != null;
			this.array = array;
			this.wantGenerics = wantGenerics;
			this.len = array.length;
			this.index = 0;
			this.alreadySeen = alreadySeen;
			moveToNextNewOne();
		}

		private void moveToNextNewOne() {
			while (index < len) {
				ResolvedType interfaceType = array[index];
				if (!wantGenerics && interfaceType.isParameterizedOrGenericType()) {
					interfaceType = interfaceType.getRawType();
				}
				String signature = interfaceType.getSignature();
				if (!alreadySeen.contains(signature)) {
					break;
				}
				index++;
			}
		}

		public boolean hasNext() {
			return index < len;
		}

		public ResolvedType next() {
			if (index < len) {
				ResolvedType oo = array[index++];
				if (!wantGenerics && (oo.isParameterizedType() || oo.isGenericType())) {
					oo = oo.getRawType();
				}
				alreadySeen.add(oo.getSignature());
				moveToNextNewOne();
				return oo;
			} else {
				throw new NoSuchElementException();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static Iterator<ResolvedType> array(final ResolvedType[] o, final boolean genericsAware) {
		return new Iterator<ResolvedType>() {
			int i = 0;
			int len = (o == null) ? 0 : o.length;

			public boolean hasNext() {
				return i < len;
			}

			public ResolvedType next() {
				if (i < len) {
					ResolvedType oo = o[i++];
					if (!genericsAware && (oo.isParameterizedType() || oo.isGenericType())) {
						return oo.getRawType();
					}
					return oo;
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * creates an iterator I based on a base iterator A and a getter G. I returns, in order, forall (i in A), G(i).
	 */
	public static <A, B> Iterator<B> mapOver(final Iterator<A> a, final Getter<A, B> g) {
		return new Iterator<B>() {
			Iterator<B> delegate = new Iterator<B>() {
				public boolean hasNext() {
					if (!a.hasNext()) {
						return false;
					}
					A o = a.next();
					delegate = append1(g.get(o), this);
					return delegate.hasNext();
				}

				public B next() {
					if (!hasNext()) {
						throw new UnsupportedOperationException();
					}
					return delegate.next();
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}
			};

			public boolean hasNext() {
				return delegate.hasNext();
			}

			public B next() {
				return delegate.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * creates an iterator I based on a base iterator A and a getter G. I returns, in order, forall (i in I) i :: forall (i' in
	 * g(i)) recur(i', g)
	 */
	public static <A> Iterator<A> recur(final A a, final Getter<A, A> g) {
		return new Iterator<A>() {
			Iterator<A> delegate = one(a);

			public boolean hasNext() {
				return delegate.hasNext();
			}

			public A next() {
				A next = delegate.next();
				delegate = append(g.get(next), delegate);
				return next;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * creates an iterator I based on base iterators A and B. Returns the elements returned by A followed by those returned by B. If
	 * B is empty, simply returns A, and if A is empty, simply returns B. Do NOT USE if b.hasNext() is not idempotent.
	 */
	public static <T> Iterator<T> append(final Iterator<T> a, final Iterator<T> b) {
		if (!b.hasNext()) {
			return a;
		}
		return append1(a, b);
	}

	/**
	 * creates an iterator I based on base iterators A and B. Returns the elements returned by A followed by those returned by B. If
	 * A is empty, simply returns B. Guaranteed not to call B.hasNext() until A is empty.
	 */
	public static <T> Iterator<T> append1(final Iterator<T> a, final Iterator<T> b) {
		if (!a.hasNext()) {
			return b;
		}
		return new Iterator<T>() {
			public boolean hasNext() {
				return a.hasNext() || b.hasNext();
			}

			public T next() {
				if (a.hasNext()) {
					return a.next();
				}
				if (b.hasNext()) {
					return b.next();
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * creates an iterator I based on a base iterator A and an object O. Returns the elements returned by A, followed by O.
	 */
	public static <T> Iterator<T> snoc(final Iterator<T> first, final T last) {
		return new Iterator<T>() {
			T last1 = last;

			public boolean hasNext() {
				return first.hasNext() || last1 != null;
			}

			public T next() {
				if (first.hasNext()) {
					return first.next();
				} else if (last1 == null) {
					throw new NoSuchElementException();
				}
				T ret = last1;
				last1 = null;
				return ret;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * creates an iterator I based on an object O. Returns O, once.
	 */
	public static <T> Iterator<T> one(final T it) {
		return new Iterator<T>() {
			boolean avail = true;

			public boolean hasNext() {
				return avail;
			}

			public T next() {
				if (!avail) {
					throw new NoSuchElementException();
				}
				avail = false;
				return it;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
