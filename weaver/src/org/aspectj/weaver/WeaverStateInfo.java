/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.bcel.BcelTypeMunger;

public class WeaverStateInfo {
	private List/*Entry*/ typeMungers;
	private boolean oldStyle;
	
	public WeaverStateInfo() {
		this(new ArrayList(), false);
	}
	
	private WeaverStateInfo(List typeMungers, boolean oldStyle) {
		this.typeMungers = typeMungers;
		this.oldStyle = oldStyle;
	}
	
	private static final int UNTOUCHED=0, WOVEN=2, EXTENDED=3;
	
	public static final WeaverStateInfo read(DataInputStream s, ISourceContext context) throws IOException {
		byte b = s.readByte();
		
		switch(b) {
			case UNTOUCHED:
				throw new RuntimeException("unexpected UNWOVEN");
			case WOVEN: 
				return new WeaverStateInfo(Collections.EMPTY_LIST, true);
			case EXTENDED:
				int n = s.readShort();
				List l = new ArrayList();
				for (int i=0; i < n; i++) {
					TypeX aspectType = TypeX.read(s);
					ResolvedTypeMunger typeMunger = 
						ResolvedTypeMunger.read(s, context);
					l.add(new Entry(aspectType, typeMunger));
				}
				return new WeaverStateInfo(l, false);
		} 
		throw new RuntimeException("bad WeaverState.Kind: " + b);
	}
	
	private static class Entry {
		public TypeX aspectType;
		public ResolvedTypeMunger typeMunger;
		public Entry(TypeX aspectType, ResolvedTypeMunger typeMunger) {
			this.aspectType = aspectType;
			this.typeMunger = typeMunger;
		}
		
		public String toString() {
			return "<" + aspectType + ", " + typeMunger + ">";
		}
	} 

	public void write(DataOutputStream s) throws IOException {
		if (oldStyle) throw new RuntimeException("shouldn't be writing this");
		
		s.writeByte(EXTENDED);
		int n = typeMungers.size();
		s.writeShort(n);
		for (int i=0; i < n; i++) {
			Entry e = (Entry)typeMungers.get(i);
			e.aspectType.write(s);
			e.typeMunger.write(s);
		}
	}

	public void addConcreteMunger(ConcreteTypeMunger munger) {
		typeMungers.add(new Entry(munger.getAspectType(), munger.getMunger()));
	}
	
	public String toString() {
		return "WeaverStateInfo(" + typeMungers + ", " + oldStyle + ")";
	}
	

	public List getTypeMungers(ResolvedTypeX onType) {
		World world = onType.getWorld();
		List ret = new ArrayList();
		for (Iterator i = typeMungers.iterator(); i.hasNext();) {
			Entry entry = (Entry) i.next();
			ResolvedTypeX aspectType = world.resolve(entry.aspectType, true);
			if (aspectType == ResolvedTypeX.MISSING) {
				world.showMessage(IMessage.ERROR, "aspect " + entry.aspectType + 
					" is needed when using type " + onType,
					onType.getSourceLocation(), null);
				continue;
			}
			
			ret.add(new BcelTypeMunger(entry.typeMunger, aspectType));
		}
		return ret;
	}

	public boolean isOldStyle() {
		return oldStyle;
	}
}
