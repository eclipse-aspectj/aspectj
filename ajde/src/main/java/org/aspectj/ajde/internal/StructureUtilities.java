/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.ajde.internal;

//import org.aspectj.ajde.compiler.AjdeCompiler;
//import org.aspectj.compiler.structure.*;
//import org.aspectj.compiler.structure.associations.*;

/**
 * Utility class for building a structure model for a given compile. Typical command-line usage: <BR>
 * &nbsp;&nbsp;&gt; {@code java org.aspectj.tools.ajde.StructureManager @&lt;config-file&gt;.lst}
 */
public class StructureUtilities {

//    private static StructureManager structureManager = new StructureManager();

    /**
     * Usage is the same as <CODE>org.aspectj.tools.ajc.Main</CODE>.
     */
//    public static void main(String[] args) throws IOException {
//        StructureNode model = buildStructureModel(args);
//        if (model == null) {

//        } else {
//            dumpStructure(model, "");
//        }
//    }

    /**
     * Compiles and builds a structure model.
     *
     * @return  the node representing the root for the structure model
     */
//    public static StructureNode buildStructureModel(String[] args) {
//        new StructureBuilder().buildStructure(args);
//        return structureManager.getStructureModel();
//    }

    /**
     * Dumps the structure model by walking the
     * corresponding tree.
     *     * @param   node    node to start traversal at, typically the root
     * @param   indent  whitespace accumulator for pretty-printing
     */
//    public static void dumpStructure(StructureNode node, String indent) {
//        if (node == null) return;
//        Syste.println(indent + node); 
//        if (node.getChildren() != null) {
//            for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
//                dumpStructure((StructureNode)it.next(), indent + "  ");
//            }
//        }
//        if (node instanceof ProgramElementNode) {
//            if (((ProgramElementNode)node).getRelations() != null) {
//                for (Iterator it = ((ProgramElementNode)node).getRelations().iterator(); it.hasNext(); ) {
//                    dumpStructure((StructureNode)it.next(), indent + "  ");
//                }
//            }
//        }
//    }
//
//    private static class StructureBuilder extends org.aspectj.tools.ajc.Main {
//        public void buildStructure(String[] args) {
//            compile(args);
//        }
//
//        public JavaCompiler getCompiler() {
//            if (compiler != null) return compiler;
//            if (compiler == null) {
//                if (errorHandler == null) {
//                    compiler = new AjdeCompiler(structureManager);
//                }
//            }
//            return compiler;
//        }
//    }
}
