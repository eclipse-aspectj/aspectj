/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 *  Contributors
 *  Andy Clement
 * ******************************************************************/
package test;

public class AnnoValues {
	public void none() {}
	@A3 public void defaultMethod() {}
	@A3(Color.GREEN) public void greenMethod() {}
	@A3(Color.RED) public void redMethod() {}
	@A3(Color.BLUE) public void blueMethod() {}
}
