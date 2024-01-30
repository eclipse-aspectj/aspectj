package ca.ubc.cs.spl.aspectPatterns.examples.composite.java;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */
 
/**
 * Implements the driver for the Composite design pattern example.<p> 
 *
 * Intent: <i>Compose objects into tree structures to represent part-whole 
 * hierarchies. Composite lets clients treat individual objects and 
 * compositions of objects uniformly.</i><p>
 *
 * Participating classes are <code>Directory</code>s as <i>Composite</i>s,
 * and <code>File</code>s as <i>Leaf</i>s. Both implement the 
 * <i>Component</i> interface.<p>
 *
 * This example creates a simple structure as follows: Composite directory1 
 * has three children: file1, directory2, and file3. directory2 has file2 
 * as a child.
 * 
 * Compact notation: directory1(file1, directory2(file2), file3)
 *
 * <p><i>This is the Java version.</i><p> 
 *
 * Every <i>Component</i> and every <i>Leaf</i> needs to know about the 
 * pattern and their in the pattern.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 * 
 * @see Component
 * @see Directory 
 * @see File
 */
  
public class Main { 

	/**
	 * helper variable to store recursion depth for pretty printing
	 */
	 
	private static int indent = 0;  

    /**
     * Prints a number of spaces according to the current recursion depth
     */
     	
	private static void indent() {
		for (int i=0; i<indent; i++)
			System.out.print(" ");
	}

    /** 
     * Pretty-prints a recursive composite structure 
     *
     * @param comp the component denoting the entry point into the structure
     */
     
	private static void printStructure(FileSystemComponent comp) {   
		indent();
		System.out.println(comp);
		indent +=4;                
		for (int i=0; i<comp.getChildCount(); i++) {
			printStructure(comp.getChild(i));
		}
		indent -= 4;
	}


    /**
     * This example creates a simple structure as follows: Composite directory1 
 	 * has three children: file1, directory2, and file3. directory2 has file2 
 	 * as a child.
     */


	public static void main(String[] args) {
		System.out.println("\n<<< Sample OOP implementation of Composite pattern >>>\n");
		System.out.print  ("Creating Composite structure ...\n");

		Directory directory1 = new Directory("Directory1");
		Directory directory2 = new Directory("Directory2");
		File 	  file1      = new File("File1", 123);
		File      file2      = new File("File2", 4556);
		File      file3      = new File("File3", 16); 
		
		directory1.add(file1);
		directory1.add(directory2);
		directory2.add(file2);
		directory1.add(file3); 

		System.out.println("done."); 
		System.out.println("This is the Structure:");
		
		printStructure(directory1);

		System.out.println("\n<<< Test completed >>>\n");
	}
}