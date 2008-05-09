/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
