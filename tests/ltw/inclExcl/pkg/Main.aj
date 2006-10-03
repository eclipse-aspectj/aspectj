package pkg;

import java.io.File;

public class Main {
    public static void main(String argz[]) {
        foo();
    }

    public static void foo() {
        (new pkg.sub.Foo()).foo();
        
        File dumpDir = new File("_ajdump"); 
        lsLR(dumpDir);
        
        // the LTW harness should clean up _ajdump files!
        cleanup(dumpDir);
    }
    
    public static void lsLR(File dir) {
        String[] files = dir.list();
        if (files == null) return;
        for (int i=0; i<files.length; i++) {
        	File f = new File(dir, files[i]);
        	if (f.isFile()) {
        		System.err.println(files[i]);
        	} else {
        		lsLR(f);
        	}
        }    	
    }
    
    public static void cleanup(File dir) {
        String[] files = dir.list();
        if (files == null) return;
        for (int i=0; i<files.length; i++) {
        	File f = new File(dir, files[i]);
        	if (f.isFile()) {
        		f.delete();
        	} else {
        		cleanup(f);
        	}
        }    	
		dir.delete();
    }
}
