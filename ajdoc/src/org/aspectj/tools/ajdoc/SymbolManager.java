/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;


import java.util.*;

import org.aspectj.asm.*;




public class SymbolManager {

//    public static File mapFilenameToSymbolFile(String filename) {
//        return mapFilenameToNewExtensionFile(filename, SYMBOL_FILE_EXTENSION);
//    }
//
//    public static File mapFilenameToSourceLinesFile(String filename) {
//        return mapFilenameToNewExtensionFile(filename, SOURCE_LINES_FILE_EXTENSION);
//    }
//
//    public static File getSourceToOutputFile(String dirname) {
//        return new File(dirname, ".ajsline");
//    }
//
//    public static File getOutputToSourceFile(String dirname) {
//        return new File(dirname, ".ajoline");
//    }

//
//    private static File mapFilenameToNewExtensionFile(String filename, String ext) {
//        int lastDot = filename.lastIndexOf('.');
//        String basename = filename;
//        if (lastDot != -1) {
//            basename = basename.substring(0, lastDot);
//        }
//
//        return new File(basename+ext);
//    }

    private static SymbolManager INSTANCE = new SymbolManager();

    public static SymbolManager getDefault() {
        return INSTANCE;
    }

    /**
     * @param   filePath    the full path to the preprocessed source file
     * @param   lineNumber  line number in the preprocessed source file
     * @return  the <CODE>SourceLine</CODE> corresponding to the original file/line
     * @see SourceLine
     */
    public SourceLine mapToSourceLine(String filePath, int lineNumber) {
    	System.err.println("> mapping: " + filePath);
    	return null;
//        Map map = lookupOutputToSource(filePath);
//
//        if (map == null) return null;
//
//        return (SourceLine)map.get(new SourceLine(filePath, lineNumber));
    }


    /**
     * @param   filePath    the full path to the original source file
     * @param   lineNumber  line number in the original source file
     * @return  the <CODE>SourceLine</CODE> corresponding to the preprocessed file/line
     * @see SourceLine
     */
    public SourceLine mapToOutputLine(String filePath, int lineNumber) {
    	return null;
//        Map map = lookupSourceToOutput(filePath);
//
//        if (map == null) return null;
//
//        return (SourceLine)map.get(new SourceLine(filePath, lineNumber));
    }



    /****
    public int mapToOutputLine(String filename, int line) {
        Vector sourceLines = lookupSourceLines(filename);

        // do linear search here
        if (sourceLines == null) return -1;

        for(int outputLine = 0; outputLine < sourceLines.size(); outputLine++) {
            SourceLine sl = (SourceLine)sourceLines.elementAt(outputLine);

            if (sl == null) continue;
            if (sl.line == line) {
                String outputRoot = new File(filename).getName();
                String sourceRoot = new File(sl.filename).getName();
                if (outputRoot.equals(sourceRoot)) return outputLine + 1;
            }
        }

        return -1;
    }
    ****/

 
	/**
	 * TODO: only works for one class
	 */
    public Declaration[] getDeclarations(String filename) {
    	IProgramElement file = (IProgramElement)AsmManager.getDefault().getHierarchy().findElementForSourceFile(filename);
		IProgramElement node = (IProgramElement)file.getChildren().get(0);
		 
//		Declaration[] decls = new Declaration[node.getChildren().size()+1];
		final List nodes = new ArrayList();
		HierarchyWalker walker = new HierarchyWalker() {
			public void preProcess(IProgramElement node) {
				IProgramElement p = (IProgramElement)node;
				nodes.add(buildDecl(p));
			}
		};

		file.walk(walker);
		
		return (Declaration[])nodes.toArray(new Declaration[nodes.size()]);
//        return lookupDeclarations(filename);
    }

	private Declaration buildDecl(IProgramElement node) {
		System.err.println("> getting decs: " + node); 
			
		String modifiers = "";
		for (Iterator modIt = node.getModifiers().iterator(); modIt.hasNext(); ) {
			modifiers += modIt.next() + " ";
		}
//		Declaration dec = new Declaration(
//			node.getSourceLocation().getLine(),
//			node.getSourceLocation().getEndLine(),
//			node.getSourceLocation().getColumn(),
//			-1,
//			modifiers,
//			node.getName(),
//			node.getFullSignature(),
//			"",
//			node.getDeclaringType(),
//			node.getKind(),
//			node.getSourceLocation().getSourceFile().getAbsolutePath(),
//			node.getFormalComment(),
//			node.getPackageName()
//		);
//		return dec;
		return null;
	}
    
    
    

//    // In the unusual case that there are multiple declarations on a single line
//    // This will return a random one
//    public Declaration getDeclarationAtLine(String filename, int line) {
//        return getDeclarationAtPoint(filename, line, -1);
//    }

    public Declaration getDeclarationAtPoint(String filename, int line, int column) {

        Declaration[] declarations = lookupDeclarations(filename);
        //System.out.println("getting "+filename+", "+line+":"+column);
        //System.out.println("decs: "+declarations);
        return getDeclarationAtPoint(declarations, line, column);
    }

    public Declaration getDeclarationAtPoint(Declaration[] declarations, int line, int column) {
        //!!! when we care about the performance of this method
        //!!! these should be guaranteed to be sorted and a binary search used here
        //!!! for now we use the simple (and reliable) linear search
        if (declarations == null) return null;

        for(int i=0; i<declarations.length; i++) {
            Declaration dec = declarations[i];
            if (dec.getBeginLine() == line) { // && dec.getEndLine() >= line) {
                if (column == -1) return dec;
                if (dec.getBeginColumn() == column) { // && dec.getEndColumn() >= column) {
                    return dec;
                }
            }
            Declaration[] enclosedDecs = dec.getDeclarations();
            if (enclosedDecs.length == 0) continue;

            Declaration dec1 = getDeclarationAtPoint(enclosedDecs, line, column);
            if (dec1 != null) return dec1;
        }

        //??? what should be returned for no declaration found
        return null;
    }

//    private Hashtable symbolFileEntryCache = new Hashtable();

    private Declaration[] lookupDeclarations(String filename) {
		System.err.println("> looking up: " + filename);
    	return null;
//        CorrFileEntry entry = lookup(filename,  mapFilenameToSymbolFile(filename),
//                                     symbolFileEntryCache);
//        return (Declaration[])entry.data;
    }

//    private Hashtable sourceToOutputCache = new Hashtable();
//    private Hashtable outputToSourceCache = new Hashtable();

//    private Map lookupSourceToOutput(String filename) {
//        CorrFileEntry entry = lookup(filename,
//                      getSourceToOutputFile(new File(filename).getParent()),
//                      sourceToOutputCache);
//        return (Map)entry.data;
//    }

//    private Map lookupOutputToSource(String filename) {
//        CorrFileEntry entry = lookup(filename,
//                      getOutputToSourceFile(new File(filename).getParent()),
//                      outputToSourceCache);
//        return (Map)entry.data;
//    }

    /* generic code for dealing with correlation files, serialization, and caching */
//    private static class CorrFileEntry {
//        public long lastModified;
//        public Object data;
//
//        public CorrFileEntry(long lastModified, Object data) {
//            this.lastModified = lastModified;
//            this.data = data;
//        }
//    }

//    private CorrFileEntry lookup(String filename, File file, Hashtable cache) {
//        CorrFileEntry entry = (CorrFileEntry)cache.get(filename);
//        if (entry != null && entry.lastModified == file.lastModified()) {
//            return entry;
//        }
//
//        entry = createCorrFileEntry(file);
//        cache.put(filename, entry);
//        return entry;
//    }

//    private CorrFileEntry createCorrFileEntry(File file) {
//        if (!file.exists()) {
//            return new CorrFileEntry(0l, null);
//        }
//
//        try {
//            long lastModified = file.lastModified();
//            ObjectInputStream stream =
//                new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
//            Object data = stream.readObject();
//            stream.close();
//            return new CorrFileEntry(lastModified, data);
//        } catch (IOException ioe) {
//            //System.err.println("ERROR!!!");
//            //ioe.printStackTrace();
//            return new CorrFileEntry(0l, null);
//        } catch (ClassNotFoundException cce) {
//            //System.err.println("ERROR!!!");
//            //cce.printStackTrace();
//            return new CorrFileEntry(0l, null);
//        }
//    }


    /**
      * @param      methodName  method name without type or parameter list
      * @return     method name with ajc-specific name mangling removed,
      *             unchanged if there's not ajc name mangling present
      */
    public static String translateMethodName(String methodName) {
        int firstDollar = methodName.indexOf('$');

        if (firstDollar == -1) return methodName;

        String baseName = methodName.substring(firstDollar);

        if (methodName.indexOf("ajc") != -1) {
            return "<" + baseName + " advice>";
        } else {
            return baseName;
        }
    }


    /************************************************************************
      The rest of the code in this file is just for testing purposes
     ************************************************************************/

//    private static final void printIndentation(int indent, String prefix) {
//        for(int i=0; i< indent; i++) System.out.print(" ");
//        System.out.print(prefix);
//    }
//
//
//    private static final void printDeclaration(Declaration dec, int indent, String prefix) {
//        printIndentation(indent, prefix);
//        if (dec == null) {
//            System.out.println("null");
//            return;
//        }
//
//        System.out.println(dec.getKind()+": "+dec.getDeclaringType()+": "+
//                                            dec.getModifiers()+": "+dec.getSignature()+": " +
//                                            //dec.getFullSignature()+": "+
//                                            dec.getCrosscutDesignator()+
//                                            ": "+dec.isIntroduced()+": "+dec.getPackageName()+": "+dec.getBeginLine()+":"+dec.getBeginColumn()
//                                            );
//
//        //printIndentation(indent, "\"\"\"");
//        //System.out.println(dec.getFormalComment());
//        /*
//        if (dec.getParentDeclaration() != null) {
//            printDeclaration(dec.getParentDeclaration(), indent+INDENT, "PARENT ");
//        }
//       if (dec.getCrosscutDeclaration() != null) {
//            printDeclaration(dec.getCrosscutDeclaration(), indent+INDENT, "XC ");
//        }
//        */
//        if (prefix.equals("")) {
//            printDeclarations(dec.getTargets(), indent+INDENT, "T> ");
//            printDeclarations(dec.getPointsTo(), indent+INDENT, ">> ");
//            printDeclarations(dec.getPointedToBy(), indent+INDENT, "<< ");
//            printDeclarations(dec.getDeclarations(), indent+INDENT, "");
//        }
//    }

//    private static final void printDeclarations(Declaration[] decs, int indent, String prefix) {
//        for(int i=0; i<decs.length; i++) {
//            printDeclaration(decs[i], indent, prefix);
//        }
//    }

//    private static final int INDENT = 2;

//    static void printLines(String filename, Map baseMap) throws IOException {
//        if (baseMap == null) return;
//
//        String fullName = new File(filename).getCanonicalPath();
//        java.util.TreeMap map = new java.util.TreeMap();
//
//        for (Iterator i = baseMap.entrySet().iterator(); i.hasNext(); ) {
//            Map.Entry entry = (Map.Entry)i.next();
//            SourceLine keyLine = (SourceLine)entry.getKey();
//            if (!keyLine.filename.equals(fullName)) continue;
//
//            map.put(new Integer(keyLine.line), entry.getValue());
//        }
//
//        for (java.util.Iterator j = map.entrySet().iterator(); j.hasNext(); ) {
//            java.util.Map.Entry entry = (java.util.Map.Entry)j.next();
//
//            System.out.println(entry.getKey() + ":\t" + entry.getValue());
//        }
//    }

//    public static void main(String[] args) throws IOException {
//        for(int i=0; i<args.length; i++) {
//            String filename = args[i];
//            System.out.println(filename);
//
//            System.out.println("declaration mappings");
//            System.out.println("kind: declaringType: modifiers: signature: fullSignature: crosscutDesignator: isIntroduced: packageName: parentDeclaration");
//
//            Declaration[] declarations = getSymbolManager().getDeclarations(filename);
//            if (declarations != null) {
//                printDeclarations(declarations, INDENT, "");
//            }
//
//            System.out.println("source to output");
//            printLines(filename, getSymbolManager().lookupSourceToOutput(filename));
//            System.out.println("output to source");
//            printLines(filename, getSymbolManager().lookupOutputToSource(filename));
//        }
//    }
}
