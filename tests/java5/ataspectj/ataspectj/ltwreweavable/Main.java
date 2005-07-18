/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.ltwreweavable;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Main implements Advisable {

	private static List joinPoints = new ArrayList();

	public void test1 () {

	}

	public void test2 () {

	}

	public void addJoinPoint (String name) {
		joinPoints.add(name);
	}

	public static void main (String[] args) {
		joinPoints = new ArrayList();
		new Main().test1();
		new Main().test2();
		if (joinPoints.size() != 2) {
			throw new RuntimeException("size="  + joinPoints.size());
		}
	}
}
