
package foo;

import java.io.*;

public class PlainJava {
	public int i;
	 
	public int getI() { 
		
		new FileFilter() {
			public boolean accept(File f) {
				boolean accept = !(f.isDirectory() || f.getName().endsWith(".class")) ;
				return accept;
			}  
		};
		
		return i; 
	}
} 