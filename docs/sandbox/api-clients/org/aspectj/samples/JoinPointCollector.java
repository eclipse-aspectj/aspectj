/* @author Mik Kersten */

// START-SAMPLE api-ajde-modelWalker Walk model to collect join point information for advised methods and constructors
package org.aspectj.samples;

import java.util.*;
import org.aspectj.tools.ajc.Main;


import org.aspectj.asm.*;

/**
 * Collects join point information for all advised methods and constructors.  
 * 
 * @author Mik Kersten
 */
public class JoinPointCollector extends Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] newArgs = new String[args.length +1];
        newArgs[0] = "-emacssym";
        for (int i = 0; i < args.length; i++) {
            newArgs[i+1] = args[i]; 
        }
        new JoinPointCollector().runMain(newArgs, false);
    }
 
    public void runMain(String[] args, boolean useSystemExit) {
        super.runMain(args, useSystemExit);
        
        ModelWalker walker = new ModelWalker() {
            public void preProcess(StructureNode node) {
                ProgramElementNode p = (ProgramElementNode)node;
                
                // first check if it is a method or constructor
                if (p.getProgramElementKind().equals(ProgramElementNode.Kind.METHOD)) {

                    // now check if it is advsied
                    for (Iterator it = p.getRelations().iterator(); it.hasNext(); ) {
                    
                        RelationNode relationNode = (RelationNode)it.next();
                        Relation relation = relationNode.getRelation();
                        if (relation == AdviceAssociation.METHOD_RELATION) {
                            System.out.println("method: " + p.toString() + ", advised by: " + relationNode.getChildren());
                        } 
                    }
                }
                    
                // code around the fact that constructor advice relationship is on the type
                if (p.getProgramElementKind().equals(ProgramElementNode.Kind.CONSTRUCTOR)) {
                    for (Iterator it = ((ProgramElementNode)p.getParent()).getRelations().iterator(); it.hasNext(); ) {
                        RelationNode relationNode = (RelationNode)it.next();
                        Relation relation = relationNode.getRelation();
                        if (relation == AdviceAssociation.CONSTRUCTOR_RELATION) {
                            System.out.println("constructor: " + p.toString() + ", advised by: " + relationNode.getChildren());
                        } 
                    }
                }
            }
        };

        StructureModelManager.getDefault().getStructureModel().getRoot().walk(walker);
    }
}
//END-SAMPLE api-ajde-modelWalker 

