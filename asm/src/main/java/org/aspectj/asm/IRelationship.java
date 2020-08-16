/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/
package org.aspectj.asm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

/**
 * A relationship has a name (e.g. 'declare warning') and some set of affected targets.
 *
 * @author Mik Kersten
 * @author Andy Clement
 */
public interface IRelationship extends Serializable {

	String getName();

	Kind getKind();

	void addTarget(String handle);

	List<String> getTargets();

	String getSourceHandle();

	boolean hasRuntimeTest();

	boolean isAffects();

	class Kind implements Serializable { // typesafe enum

		private static final long serialVersionUID = -2691351740214705220L;

		public static final Kind DECLARE_WARNING = new Kind("declare warning");
		public static final Kind DECLARE_ERROR = new Kind("declare error");
		public static final Kind ADVICE_AROUND = new Kind("around advice");
		public static final Kind ADVICE_AFTERRETURNING = new Kind("after returning advice");
		public static final Kind ADVICE_AFTERTHROWING = new Kind("after throwing advice");
		public static final Kind ADVICE_AFTER = new Kind("after advice");
		public static final Kind ADVICE_BEFORE = new Kind("before advice");
		public static final Kind ADVICE = new Kind("advice");
		public static final Kind DECLARE = new Kind("declare");
		public static final Kind DECLARE_INTER_TYPE = new Kind("inter-type declaration");
		public static final Kind USES_POINTCUT = new Kind("uses pointcut");
		public static final Kind DECLARE_SOFT = new Kind("declare soft");

		public static final Kind[] ALL = { DECLARE_WARNING, DECLARE_ERROR, ADVICE_AROUND, ADVICE_AFTERRETURNING,
				ADVICE_AFTERTHROWING, ADVICE_AFTER, ADVICE_BEFORE, ADVICE, DECLARE, DECLARE_INTER_TYPE, USES_POINTCUT, DECLARE_SOFT };

		private final String name;

		public boolean isDeclareKind() {
			return this == DECLARE_WARNING || this == DECLARE_ERROR || this == DECLARE || this == DECLARE_INTER_TYPE
					|| this == DECLARE_SOFT;
		}

		public String getName() {
			return name;
		}

		/**
		 * Return the Kind of the relationship that is passed in by name.
		 *
		 * @param stringFormOfRelationshipKind the relationship name, eg. 'declare warning', 'declare error', etc.
		 * @return the Kind instance
		 */
		public static Kind getKindFor(String stringFormOfRelationshipKind) {
			for (Kind kind : ALL) {
				if (kind.name.equals(stringFormOfRelationshipKind)) {
					return kind;
				}
			}
			return null;
		}

		private Kind(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;

		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}

}
