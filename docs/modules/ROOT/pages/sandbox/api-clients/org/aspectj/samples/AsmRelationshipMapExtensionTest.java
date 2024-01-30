package org.aspectj.samples;

import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.AjdeTestCase;
import org.aspectj.asm.*;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.ResolvedTypeX;

/**
 * @author Mik Kersten
 */
public class AsmRelationshipMapExtensionTest extends AjdeTestCase {

    public void testDeclares() {
        System.out.println("----------------------------------");
        System.out.println("Parents declared by declare parents statements: ");
        HierarchyWalker walker = new HierarchyWalker() {
            public void preProcess(IProgramElement node) {
                if (node.getKind().equals(IProgramElement.Kind.DECLARE_PARENTS)) {
                    System.out.println(node);

                    List relations = AsmManager.getDefault().getRelationshipMap().get(node);
                    if (relations != null) {
                        for (Iterator it = relations.iterator(); it.hasNext();) {
                            IRelationship relationship = (IRelationship) it.next();
                            List targets = relationship.getTargets();
                            for (Iterator iter = targets.iterator(); iter.hasNext();) {
                                IProgramElement currElt = AsmManager
                                        .getDefault().getHierarchy()
                                        .getElement((String) iter.next());
                                System.out.println("--> " + relationship.getName() + ": " + currElt);
                            }
                        }
                    }
                }
            }
        };
        AsmManager.getDefault().getHierarchy().getRoot().walk(walker);
    }

    protected void setUp() throws Exception {
        AsmRelationshipProvider.setDefault(new DeclareInfoProvider());
        super.setUp("examples");
 
        assertTrue("build success",
                doSynchronousBuild("../examples/coverage/coverage.lst"));
    }
}

class DeclareInfoProvider extends AsmRelationshipProvider {
    
    public void addDeclareParentsRelationship(ISourceLocation decp,
            ResolvedTypeX targetType, List newParents) {
        super.addDeclareParentsRelationship(decp, targetType, newParents);
        for (Iterator it = newParents.iterator(); it.hasNext();) {
            ResolvedTypeX superType = (ResolvedTypeX) it.next();
            
    		String sourceHandle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(
                    decp.getSourceFile(),decp.getLine(),decp.getColumn(), decp.getOffset());
    		IProgramElement ipe = AsmManager.getDefault().getHierarchy().findElementForHandle(sourceHandle);
  
    		String superHandle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(
    		        superType.getSourceLocation().getSourceFile(),
    		        superType.getSourceLocation().getLine(),
    		        superType.getSourceLocation().getColumn(),
    		        superType.getSourceLocation().getOffset()
            );
    				
    		if (sourceHandle != null && superHandle != null) {
    			IRelationship foreward = AsmManager.getDefault().getRelationshipMap().get(
    			        sourceHandle, 
    			        IRelationship.Kind.DECLARE, 
    			        "super types declared",
    			        false,
    			        true);
    			foreward.addTarget(superHandle);
    				
    			IRelationship back = AsmManager.getDefault().getRelationshipMap().get(
    			        superHandle, IRelationship.Kind.DECLARE, 
    			        "declared as super type by",
    			        false,
    			        true);
    			back.addTarget(sourceHandle);
    		}
        }
    }
}