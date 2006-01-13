
package foo;

import java.io.*;

public class PlainJava {
	public int i;
	 
	/**
     * Mark that the transaction is "poisoned". I.e., it will have to rollback.
     * If commit() is called, the transaction will roll back instead.
	 */
	public int getI() { 
		assert true;
		
		new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !(f.isDirectory() || f.getName().endsWith(".class")) ;
				return accept;
			}  
		};
		
		return i; 
	}
	
	static private class ClassBar {
		
		static private class Baz { 

			static private class Jazz { 
				void mumble() { }
			}
		}
	}
} 