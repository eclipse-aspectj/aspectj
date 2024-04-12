/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement IBM     initial implementation 30-May-2004
 * ******************************************************************/

package org.aspectj.bridge;

public class WeaveMessage extends Message {

	// Kinds of weaving message we can produce

	public static WeaveMessageKind WEAVEMESSAGE_DECLAREPARENTSIMPLEMENTS = new WeaveMessageKind(1,
			"Extending interface set for type '%1' (%2) to include '%3' (%4)");

	public static WeaveMessageKind WEAVEMESSAGE_ITD = new WeaveMessageKind(2, "Type '%1' (%2) has intertyped %3 from '%4' (%5)");

	// %7 is information like "[with runtime test]"
	public static WeaveMessageKind WEAVEMESSAGE_ADVISES = new WeaveMessageKind(3,
			"Join point '%1' in Type '%2' (%3) advised by %4 advice from '%5' (%6)%7");

	public static WeaveMessageKind WEAVEMESSAGE_DECLAREPARENTSEXTENDS = new WeaveMessageKind(4,
			"Setting superclass of type '%1' (%2) to '%3' (%4)");

	public static WeaveMessageKind WEAVEMESSAGE_SOFTENS = new WeaveMessageKind(5,
			"Softening exceptions in type '%1' (%2) as defined by aspect '%3' (%4)");

	public static WeaveMessageKind WEAVEMESSAGE_ANNOTATES = new WeaveMessageKind(6,
			"'%1' (%2) is annotated with %3 %4 annotation from '%5' (%6)");

	public static WeaveMessageKind WEAVEMESSAGE_MIXIN = new WeaveMessageKind(7, "Mixing interface '%1' (%2) into type '%3' (%4)");

	public static WeaveMessageKind WEAVEMESSAGE_REMOVES_ANNOTATION = new WeaveMessageKind(6,
			"'%1' (%2) has had %3 %4 annotation removed by '%5' (%6)");

	private String affectedTypeName;
	private String aspectName;

	// private ctor - use the static factory method
	private WeaveMessage(
		String message,
		String affectedTypeName, String aspectName,
		ISourceLocation affectedTypeLocation, ISourceLocation aspectLocation
	) {
		super(message, null, IMessage.WEAVEINFO, affectedTypeLocation, null, new ISourceLocation[] { aspectLocation });
		this.affectedTypeName = affectedTypeName;
		this.aspectName = aspectName;
	}

	/**
	 * Static helper method for constructing weaving messages.
	 *
	 * @param kind what kind of message (e.g. declare parents)
	 * @param inserts inserts for the message (inserts are marked %n in the message)
	 * @return new weaving message
	 */
	public static WeaveMessage constructWeavingMessage(WeaveMessageKind kind, String[] inserts) {
		StringBuilder str = new StringBuilder(kind.getMessage());
		int pos = -1;
		while ((pos = new String(str).indexOf("%")) != -1) {
			int n = Character.getNumericValue(str.charAt(pos + 1));
			str.replace(pos, pos + 2, inserts[n - 1]);
		}
		return new WeaveMessage(str.toString(), null, null, null, null);
	}

	/**
	 * Static helper method for constructing weaving messages.
	 *
	 * @param kind what kind of message (e.g. declare parents)
	 * @param inserts inserts for the message (inserts are marked %n in the message)
	 * @param affectedTypeName the type which is being advised/declaredUpon
	 * @param aspectName the aspect that defined the advice or declares
   * @param affectedTypeLocation the source location of the advised/declaredUpon type
   * @param aspectLocation the source location of the declaring/defining advice/declare
	 * @return new weaving message
	 */
	public static WeaveMessage constructWeavingMessage(
		WeaveMessageKind kind, String[] inserts,
		String affectedTypeName, String aspectName,
		ISourceLocation affectedTypeLocation, ISourceLocation aspectLocation
	) {
		StringBuilder str = new StringBuilder(kind.getMessage());
		int pos = -1;
		while ((pos = new String(str).indexOf("%")) != -1) {
			int n = Character.getNumericValue(str.charAt(pos + 1));
			str.replace(pos, pos + 2, inserts[n - 1]);
		}
		return new WeaveMessage(str.toString(), affectedTypeName, aspectName, affectedTypeLocation, aspectLocation);
	}

	/**
	 * @return Returns the aspectname.
	 */
	public String getAspectName() {
		return aspectName;
	}

	/**
	 * @return Returns the affectedtypename.
	 */
	public String getAffectedTypeName() {
		return affectedTypeName;
	}

	public static class WeaveMessageKind {

		// private int id;
		private String message;

		public WeaveMessageKind(int id, String message) {
			// this.id = id;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
}
