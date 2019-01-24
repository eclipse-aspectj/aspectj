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

package org.aspectj.weaver.patterns;

import java.io.IOException;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.VersionedDataInputStream;

public class NamePattern extends PatternNode {
	char[] pattern;
	int starCount = 0;
	private int hashcode = -1;

	public static final NamePattern ELLIPSIS = new NamePattern("");
	public static final NamePattern ANY = new NamePattern("*");

	public NamePattern(String name) {
		this(name.toCharArray());
	}

	public NamePattern(char[] pattern) {
		this.pattern = pattern;

		for (int i = 0, len = pattern.length; i < len; i++) {
			if (pattern[i] == '*') {
				starCount++;
			}
		}
		hashcode = new String(pattern).hashCode();
	}

	public boolean matches(char[] a2) {
		char[] a1 = pattern;
		int len1 = a1.length;
		int len2 = a2.length;
		if (starCount == 0) {
			if (len1 != len2) {
				return false;
			}
			for (int i = 0; i < len1; i++) {
				if (a1[i] != a2[i]) {
					return false;
				}
			}
			return true;
		} else if (starCount == 1) {
			// just '*' matches anything
			if (len1 == 1) {
				return true;
			}
			if (len1 > len2 + 1) {
				return false;
			}
			int i2 = 0;
			for (int i1 = 0; i1 < len1; i1++) {
				char c1 = a1[i1];
				if (c1 == '*') {
					i2 = len2 - (len1 - (i1 + 1));
				} else if (c1 != a2[i2++]) {
					return false;
				}
			}
			return true;
		} else {
			// System.err.print("match(\"" + pattern + "\", \"" + target + "\") -> ");
			boolean b = outOfStar(a1, a2, 0, 0, len1 - starCount, len2, starCount);
			// System.err.println(b);
			return b;
		}
	}

	private static boolean outOfStar(final char[] pattern, final char[] target, int pi, int ti, int pLeft, int tLeft,
			final int starsLeft) {
		if (pLeft > tLeft) {
			return false;
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length && pi < pattern.length)
			if (tLeft == 0) {
				return true;
			}
			if (pLeft == 0) {
				return (starsLeft > 0);
			}
			if (pattern[pi] == '*') {
				return inStar(pattern, target, pi + 1, ti, pLeft, tLeft, starsLeft - 1);
			}
			if (target[ti] != pattern[pi]) {
				return false;
			}
			pi++;
			ti++;
			pLeft--;
			tLeft--;
		}
	}

	private static boolean inStar(final char[] pattern, final char[] target, int pi, int ti, final int pLeft, int tLeft,
			int starsLeft) {
		// invariant: pLeft > 0, so we know we'll run out of stars and find a real char in pattern
		char patternChar = pattern[pi];
		while (patternChar == '*') {
			starsLeft--;
			patternChar = pattern[++pi];
		}
		while (true) {
			// invariant: if (tLeft > 0) then (ti < target.length)
			if (pLeft > tLeft) {
				return false;
			}
			if (target[ti] == patternChar) {
				if (outOfStar(pattern, target, pi + 1, ti + 1, pLeft - 1, tLeft - 1, starsLeft)) {
					return true;
				}
			}
			ti++;
			tLeft--;
		}
	}

	public boolean matches(String other) {
		if (starCount == 1 && pattern.length == 1) {
			// optimize for wildcard
			return true;
		}
		return matches(other.toCharArray());
	}

	@Override
	public String toString() {
		return new String(pattern);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NamePattern) {
			NamePattern otherPat = (NamePattern) other;
			if (otherPat.starCount != this.starCount) {
				return false;
			}
			if (otherPat.pattern.length != this.pattern.length) {
				return false;
			}
			for (int i = 0; i < this.pattern.length; i++) {
				if (this.pattern[i] != otherPat.pattern[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public void write(CompressingDataOutputStream out) throws IOException {
		out.writeUTF(new String(pattern));
	}

	public static NamePattern read(VersionedDataInputStream in) throws IOException {
		String s = in.readUTF();
		if (s.length() == 0) {
			return ELLIPSIS;
		}
		return new NamePattern(s);
	}

	/**
	 * Method maybeGetSimpleName.
	 * 
	 * @return String
	 */
	public String maybeGetSimpleName() {
		if (starCount == 0 && pattern.length > 0) {
			return new String(pattern);
		}
		return null;
	}

	/**
	 * Method isAny.
	 * 
	 * @return boolean
	 */
	public boolean isAny() {
		return starCount == 1 && pattern.length == 1;
	}

	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
