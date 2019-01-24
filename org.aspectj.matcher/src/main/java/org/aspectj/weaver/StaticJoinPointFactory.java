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

public class StaticJoinPointFactory {
	// int usedKeys;
	//	
	// List/*String*/ strings = new ArrayList();
	// Map/*String,Integer*/ keysForStrings = new HashMap();
	//	
	// public StaticJoinPointFactory() {
	// super();
	// }
	//
	// static char[] encoding = new char[] {
	// '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',//10
	// 'a', 'b', 'z', //36
	// 'A', 'B', 'Z', //62
	// '%', '$', //64
	// };
	//    
	// static int TWO_WORDS = 64*64-1;
	// static int WORD_MASK = 63;
	//    
	// public void write(String s, StringBuffer result) {
	// int i = getIndex(s);
	// encode(i, result);
	// }
	//    
	// void encode(int i, StringBuffer result) {
	// if (i > TWO_WORDS) {
	// throw new RuntimeException("unimplemented");
	// } else {
	// result.append( encoding[(i >> 6) & WORD_MASK] );
	// result.append( encoding[i & WORD_MASK] );
	// }
	// }
	//    
	// public String read(StringReader reader) {
	// int i = reader.read();
	//    	
	// }

}
