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

/**
 * @author Mik Kersten
 */
public class SymbolManager {

    private static SymbolManager INSTANCE = new SymbolManager();

    public static SymbolManager getDefault() {
        return INSTANCE;
    }

 
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
				if (accept(node)) nodes.add(buildDecl(p));
			}
		};

		file.walk(walker);
		
		return (Declaration[])nodes.toArray(new Declaration[nodes.size()]);
    }
    
    /**
     * Rejects anonymous kinds by checking if their name is an integer
     */
	private boolean accept(IProgramElement node) {
		if (node.getKind().isType()) {
			boolean isAnonymous = StructureUtil.isAnonymous(node);
			return !node.getParent().getKind().equals(IProgramElement.Kind.METHOD)
				&& !isAnonymous;
		} else {
			return !node.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE);
		}
//			&& !(node.getKind().isType() &&
//				node.getParent().getKind().equals(IProgramElement.Kind.METHOD));
	}

	private Declaration buildDecl(IProgramElement node) {
		
		String signature = "";
		String accessibility = node.getAccessibility().toString();
		if (!accessibility.equals("package")) signature = accessibility.toString() + " ";
		
		String modifiers = "";
		if (!node.getAccessibility().equals(IProgramElement.Accessibility.PACKAGE)) modifiers += node.getAccessibility() + " ";
		for (Iterator modIt = node.getModifiers().iterator(); modIt.hasNext(); ) {
			modifiers += modIt.next() + " ";
		}
	
		if (node.getKind().equals(IProgramElement.Kind.METHOD) || 
			node.getKind().equals(IProgramElement.Kind.FIELD)) {
			signature += node.getCorrespondingType() + " ";
		}

		if (node.getKind().equals(IProgramElement.Kind.CLASS) || 
				node.getKind().equals(IProgramElement.Kind.METHOD)) {
				signature += "class ";
		} else if (node.getKind().equals(IProgramElement.Kind.INTERFACE) || 
				node.getKind().equals(IProgramElement.Kind.METHOD)) {
			signature += "interface ";
		} 
		
		signature += node.toSignatureString();
		  
		String name = node.getName();
		if (node.getKind().isType()) {
			name = genPartiallyQualifiedName(node, node.getName());
		}
		
		String declaringType = node.getParent().getName();
//		if (!node.getKind().isType()) {
//			declaringType = node.getParent().getName(); 
//		}
		
		Declaration dec = new Declaration(
			node.getSourceLocation().getLine(),
			node.getSourceLocation().getEndLine(),
			node.getSourceLocation().getColumn(),
			-1,
			modifiers,
			name,
			signature,
			"", // crosscut designator
			node.getDeclaringType(),
			node.getKind().toString(),
			node.getSourceLocation().getSourceFile().getAbsolutePath(),
			node.getFormalComment(),
			node.getPackageName(),
			node
		);
		return dec;
	}
    

//    // In the unusual case that there are multiple declarations on a single line
//    // This will return a random one
//    public Declaration getDeclarationAtLine(String filename, int line) {
//        return getDeclarationAtPoint(filename, line, -1);
//    }

    private String genPartiallyQualifiedName(IProgramElement node, String name) {
//    	if (node.getParent() != null) System.err.println("%%% " + node.getParent());
		if (node.getParent() != null && node.getParent().getKind().isType()) {
			name = node.getParent().getName() + '.' + name;
			genPartiallyQualifiedName(node.getParent(), name);
		}
		return name;
	}

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
