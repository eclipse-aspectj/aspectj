/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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

	private String affectedtypename;
	private String aspectname;

	// private ctor - use the static factory method
	private WeaveMessage(String message, String affectedtypename, String aspectname) {
		super(message, IMessage.WEAVEINFO, null, null);
		this.affectedtypename = affectedtypename;
		this.aspectname = aspectname;
	}

	/**
	 * Static helper method for constructing weaving messages.
	 * 
	 * @param kind what kind of message (e.g. declare parents)
	 * @param inserts inserts for the message (inserts are marked %n in the message)
	 * @return new weaving message
	 */
	public static WeaveMessage constructWeavingMessage(WeaveMessageKind kind, String[] inserts) {
		StringBuffer str = new StringBuffer(kind.getMessage());
		int pos = -1;
		while ((pos = new String(str).indexOf("%")) != -1) {
			int n = Character.getNumericValue(str.charAt(pos + 1));
			str.replace(pos, pos + 2, inserts[n - 1]);
		}
		return new WeaveMessage(str.toString(), null, null);
	}

	/**
	 * Static helper method for constructing weaving messages.
	 * 
	 * @param kind what kind of message (e.g. declare parents)
	 * @param inserts inserts for the message (inserts are marked %n in the message)
	 * @param affectedtypename the type which is being advised/declaredUpon
	 * @param aspectname the aspect that defined the advice or declares
	 * @return new weaving message
	 */
	public static WeaveMessage constructWeavingMessage(WeaveMessageKind kind, String[] inserts, String affectedtypename,
			String aspectname) {
		StringBuffer str = new StringBuffer(kind.getMessage());
		int pos = -1;
		while ((pos = new String(str).indexOf("%")) != -1) {
			int n = Character.getNumericValue(str.charAt(pos + 1));
			str.replace(pos, pos + 2, inserts[n - 1]);
		}
		return new WeaveMessage(str.toString(), affectedtypename, aspectname);
	}

	/**
	 * @return Returns the aspectname.
	 */
	public String getAspectname() {
		return aspectname;
	}

	/**
	 * @return Returns the affectedtypename.
	 */
	public String getAffectedtypename() {
		return affectedtypename;
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
