/* *******************************************************************
 * Copyright (c) 2009 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Andy Clement
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.reflect.ReflectionWorld;

import junit.framework.TestCase;

/**
 * @author Andy Clement
 */
public class JoinPointSignatureIteratorTest extends TestCase {

	private World getWorld() {
		return new ReflectionWorld(getClass().getClassLoader());
	}

	/**
	 * Checking the signatures for a dynamic proxy - which is created using erased information and so is completely generics unaware
	 */
	public void testPr268419() {
		World w = getWorld();
		w.setBehaveInJava5Way(true);

		// The proxy class here is not generated, it is simply loaded up. $Proxy1 is in the java5-testsrc folder, but it
		// obeys the rules of a generated proxy in that it extends java.lang.reflect.Proxy
		ResolvedType proxy = UnresolvedType.forName("$Proxy1").resolve(w);
		assertNotNull(proxy);

		// The test hierarchy here (messageservice and genericservice) contains 2 methods. One is generic
		// and one is not. The aim of the test here is that the join point signatures generated for both
		// should be the same because of the use of a proxy.

		List l = proxy.getMethodsWithoutIterator(false, false, false);
		for (Object object : l) {
			ResolvedMember rm = (ResolvedMember) object;
			if (expectedResults.containsKey(rm.toString())) {
				System.out.println("\nChecking: " + rm);
				int i = 0;
				List<String> sigs = expectedResults.get(rm.toString());
				Iterator jpsi = rm.getJoinPointSignatures(w);
				while (jpsi.hasNext()) {
					ResolvedMember sig = (ResolvedMember) jpsi.next();
					assertEquals(sigs.get(i).toString(), sig.toString());
					i++;
				}
				if (i != sigs.size()) {
					fail("Expected " + sigs.size() + " signatures but got " + i);
				}
			} else {
				if (rm.getName().equals("get1") || rm.getName().equals("get2")) {
					fail("\nFound this unchecked get method " + rm);
					// Iterator jpsi = rm.getJoinPointSignatures(w);
					// while (jpsi.hasNext()) {
					// ResolvedMember sig = (ResolvedMember) jpsi.next();
					// System.out.println(sig);
					// }
				}
			}
		}
	}

	public static Map<String, List<String>> expectedResults = new HashMap<>();

	static {
		List<String> sigs = new ArrayList<>();
		sigs.add("java.lang.Object $Proxy1.get1(java.io.Serializable)");
		sigs.add("java.lang.Object MessageService.get1(java.io.Serializable)");
		sigs.add("java.lang.Object GenericService.get1(java.io.Serializable)");
		sigs.add("java.lang.Object GenericService.get1(java.io.Serializable)");
		expectedResults.put("java.lang.Object $Proxy1.get1(java.io.Serializable)", sigs);

		sigs = new ArrayList<>();
		sigs.add("java.lang.Object $Proxy1.get2(java.io.Serializable)");
		sigs.add("java.lang.Object MessageService.get2(java.io.Serializable)");
		sigs.add("java.lang.Object GenericService.get2(java.io.Serializable)");
		sigs.add("java.lang.Object GenericService.get2(java.io.Serializable)");
		expectedResults.put("java.lang.Object $Proxy1.get2(java.io.Serializable)", sigs);

		sigs = new ArrayList<>();
		sigs.add("java.lang.Object $Proxy1.get1(java.lang.Long)");
		expectedResults.put("java.lang.Object $Proxy1.get1(java.lang.Long)", sigs);

		sigs = new ArrayList<>();
		sigs.add("java.lang.Object GenericService.get1(java.io.Serializable)");
		expectedResults.put("java.lang.Object GenericService.get1(java.io.Serializable)", sigs);

		sigs = new ArrayList<>();
		sigs.add("java.lang.Object GenericService.get2(java.io.Serializable)");
		expectedResults.put("java.lang.Object GenericService.get2(java.io.Serializable)", sigs);
	}
}
