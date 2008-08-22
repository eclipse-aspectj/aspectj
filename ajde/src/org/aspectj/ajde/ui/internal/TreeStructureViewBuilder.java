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


package org.aspectj.ajde.ui.internal;

import java.util.*;

import org.aspectj.ajde.ui.*;
import org.aspectj.asm.*;
//import org.aspectj.asm.internal.*;
//import org.aspectj.asm.internal.ProgramElement;

/**
 * @author Mik Kersten
 */
public class TreeStructureViewBuilder {

	private StructureViewNodeFactory nodeFactory;

	public TreeStructureViewBuilder(StructureViewNodeFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	/**
	 * @todo	get rid of instanceof tests
	 */
	public void buildView(StructureView view, IHierarchy model) {
//		StructureViewProperties properties = view.getViewProperties();
		IProgramElement modelRoot = null;
//		boolean noStructure = false;
		if (isFileView(view)) {
			FileStructureView fileView = (FileStructureView)view;
			if (fileView.getSourceFile() == null) {	
				modelRoot = IHierarchy.NO_STRUCTURE;
//				noStructure = true;
			} else {
				modelRoot = model.findElementForSourceFile(fileView.getSourceFile());
			}
		} else {
			modelRoot = model.getRoot();
		}
		
		IStructureViewNode viewRoot = null;
		if (!isFileView(view)) {
			StructureViewProperties.Hierarchy hierarchy 
				= ((GlobalStructureView)view).getGlobalViewProperties().getHierarchy();
			if (hierarchy.equals(StructureViewProperties.Hierarchy.CROSSCUTTING) 
				|| hierarchy.equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
				viewRoot = buildCustomTree((GlobalStructureView)view, model);		
			}		
		} 
		if (viewRoot == null) {
			viewRoot = createViewNode(modelRoot, view.getViewProperties());//modelRoot;
		}  
		
		if (view.getViewProperties().getSorting() == StructureViewProperties.Sorting.ALPHABETICAL
			|| (!isFileView(view) && 
			 ((GlobalStructureView)view).getGlobalViewProperties().getHierarchy().equals(StructureViewProperties.Hierarchy.DECLARATION))) {
			sortView(viewRoot, ALPHABETICAL_COMPARATOR);
		} else {
			sortView(viewRoot, DECLARATIONAL_COMPARATOR);
		}  
		
		addPackageNode(view, viewRoot);
		view.setRootNode(viewRoot);
	}

	private void addPackageNode(StructureView view, IStructureViewNode viewRoot) {
		if (isFileView(view)) {
//			IProgramElement fileNode = viewRoot.getStructureNode();
//			IProgramElement parentNode = fileNode.getParent();
//			
//			if (parentNode.getKind() == IProgramElement.Kind.PACKAGE) {
//				String name = parentNode.getName();
//				IProgramElement packageNode = new ProgramElement(name, IProgramElement.Kind.PACKAGE, null);
//				packageNode.setSourceLocation(fileNode.getSourceLocation());
//				StructureViewNode packageViewNode = createViewNode(
//					packageNode, 
//					view.getViewProperties()
//				);
//				viewRoot.getChildren().add(0, packageViewNode);
//			};
		}
	}
	
	private IStructureViewNode createViewNode(IProgramElement node, StructureViewProperties properties) {
		if (node == null) return null;
		List children = new ArrayList();
//		IProgramElement pNode = node;
//		if (node.getRelations() != null) {
//			for (Iterator it = node.getRelations().iterator(); it.hasNext(); ) {
//				IProgramElement IProgramElement = (IProgramElement)it.next();
//				if (acceptNode(IProgramElement, properties)) {
//					children.add(createViewNode(IProgramElement, properties));
//				}
//			}	
//		}
		if (node.isRunnable() && node.getParent() != null) {
			IProgramElement parent = node.getParent();
			if (parent.getKind().equals(IProgramElement.Kind.CLASS)
				|| parent.getKind().equals(IProgramElement.Kind.ASPECT)) {
				parent.setRunnable(true);	
				node.setRunnable(false);
			}
		}
		if (node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				IProgramElement IProgramElement = (IProgramElement)it.next();
				if (acceptNode(IProgramElement, properties)) {
					children.add(createViewNode(IProgramElement, properties));
				}
			}	
		}

		IStructureViewNode viewNode = nodeFactory.createNode(node, children);//new TreeViewNode(root, null, children);
		return viewNode;	
	}
	
	/**
	 * @todo	get rid of this test, fix polymorphism
	 */
	private boolean isFileView(StructureView view) {
		return view instanceof FileStructureView
			&& !(view instanceof GlobalStructureView);
	}
	
	private boolean acceptGranularity(IProgramElement.Kind kind, StructureViewProperties.Granularity granularity) {
		
		if (granularity == StructureViewProperties.Granularity.DECLARED_ELEMENTS) {
			return true;
		} else if (granularity == StructureViewProperties.Granularity.MEMBER && 
			(kind != IProgramElement.Kind.CODE)) {
			return true;
		} else if (granularity == StructureViewProperties.Granularity.TYPE
			&& (kind == IProgramElement.Kind.PROJECT
				|| kind == IProgramElement.Kind.PACKAGE
				|| kind.isSourceFile()
				|| kind.isType())) {
			return true;			
		} else if (granularity == StructureViewProperties.Granularity.FILE
			&& (kind == IProgramElement.Kind.PROJECT
				|| kind == IProgramElement.Kind.PACKAGE
				|| kind.isSourceFile())) {
			return true;			
		} else if (granularity == StructureViewProperties.Granularity.PACKAGE
			&& (kind == IProgramElement.Kind.PROJECT
				|| kind == IProgramElement.Kind.PACKAGE)) {
			return true;			
		} else {
			return false;
		}
	}
	
	private boolean acceptNode(IProgramElement node, StructureViewProperties properties) {
		if (node instanceof IProgramElement) {
			IProgramElement pNode = (IProgramElement)node;
			if (!acceptGranularity(pNode.getKind(), properties.getGranularity())) {
				return false;
			} else if (pNode.getKind().isMember()) {
				if (properties.getFilteredMemberAccessibility().contains(pNode.getAccessibility())) {
					return false;	
				}
				if (properties.getFilteredMemberKinds().contains(pNode.getKind())) {
					return false;	
				}
				for (Iterator it = pNode.getModifiers().iterator(); it.hasNext(); ) {
					if (properties.getFilteredMemberModifiers().contains(it.next())) {
						return false;	
					}	
				}
			}
//		} else if (node instanceof IRelationship) {
//			IRelationship relation = (IRelationship)node;
//			return properties.getRelations().contains(relation);
		} else {
			return true;
		}
		return true;
	}

	private void sortView(IStructureViewNode node, Comparator comparator) {
		if (node == null || node.getChildren() == null) return;
		Collections.sort(node.getChildren(), comparator);
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			IStructureViewNode nextNode = (IStructureViewNode)it.next();
			if (nextNode != null) sortView(nextNode, comparator);	
		}
	}

    private IStructureViewNode buildCustomTree(GlobalStructureView view, IHierarchy model) {
        IProgramElement rootNode = model.getRoot();
        IStructureViewNode treeNode = nodeFactory.createNode(rootNode);

        List rootNodes = new ArrayList();
        getRoots(rootNode, rootNodes, view.getGlobalViewProperties().getHierarchy());
		
        for (Iterator it = rootNodes.iterator(); it.hasNext(); ) {
            if (view.getGlobalViewProperties().getHierarchy().equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
                treeNode.add(getCrosscuttingChildren((IProgramElement)it.next()));
            } else if (view.getGlobalViewProperties().getHierarchy().equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
                treeNode.add(getInheritanceChildren(
                	(IProgramElement)it.next(),
                	view.getViewProperties().getRelations())	
                );
            }
        }
        return treeNode;
    }

    private void getRoots(IProgramElement rootNode, List roots, StructureViewProperties.Hierarchy hierarchy) {
//        if (rootNode != null && rootNode.getChildren() != null) {
//            for (Iterator it = rootNode.getChildren().iterator(); it.hasNext(); ) {
//                IProgramElement node = (IProgramElement)it.next();
//                if (node instanceof IProgramElement) {
//                    if (acceptNodeAsRoot((IProgramElement)node, hierarchy)) {
//                        IProgramElement pNode = (IProgramElement)node;
//                        List relations = pNode.getRelations();
//                        String delimiter = "";
//                        if (hierarchy.equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
//                            delimiter = "uses pointcut";
//                        } else if (hierarchy.equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
//                            delimiter = "inherits";
//                        } 
//                        if (relations != null && relations.toString().indexOf(delimiter) == -1) {
//                            boolean found = false;
//                            for (Iterator it2 = roots.iterator(); it2.hasNext(); ) {
//                                if (((IProgramElement)it2.next()).equals(pNode)) found = true;
//                            }
//                            if (!found) roots.add(pNode);
//                        } 
//                    } 
//                }
//                getRoots(node, roots, hierarchy);
//            }
//        }
    }

    public boolean acceptNodeAsRoot(IProgramElement node, StructureViewProperties.Hierarchy hierarchy) {
        if (hierarchy.equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
            return node.getKind().equals(IProgramElement.Kind.ADVICE)
                || node.getKind().equals(IProgramElement.Kind.POINTCUT);
        } else if (hierarchy.equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
            return node.getKind().equals(IProgramElement.Kind.CLASS);
        } else {	
            return false;
        }
    }

    private IStructureViewNode getInheritanceChildren(IProgramElement node, List associations) {
//    	IStructureViewNode treeNode = nodeFactory.createNode(node);
//        //StructureViewNode treeNode = new StructureViewNodeAdapter(node);
//        List relations = ((IProgramElement)node).getRelations();
        throw new RuntimeException("unimplemented");
//        if (relations != null) {
//            for (Iterator it = relations.iterator(); it.hasNext(); ) {
//				IRelationship relation = (IRelationship)it.next();
//                if (relation.getName().equals("is inherited by")) {
//                    for (Iterator it2 = relation.getTargets().iterator(); it2.hasNext(); ) {
////                        IProgramElement pNode = ((LinkNode)it2.next()).getProgramElementNode();
////                        StructureViewNode newNode = getInheritanceChildren(pNode, associations);
//                        StructureViewNode typeChildren = buildTree(newNode.getStructureNode(), associations);
//                        for (int i = 0; i < typeChildren.getChildren().size(); i++) {
//                            newNode.add((StructureViewNode)typeChildren.getChildren().get(i));
//                        }
//						treeNode.add(newNode);
//                    }
//                }
//            }
//        }
//        return treeNode;
    }

    private IStructureViewNode getCrosscuttingChildren(IProgramElement node) {
        //StructureViewNodeAdapter treeNode = new StructureViewNodeAdapter(node);
//        IStructureViewNode treeNode = nodeFactory.createNode(node);
//        List relations = ((IProgramElement)node).getRelations();
        throw new RuntimeException("unimplemented");
//        if (relations != null) {
//            for (Iterator it = relations.iterator(); it.hasNext(); ) {
//				IRelationship relation = (IRelationship)it.next();
//                if (relation.getName().equals("pointcut used by")) {
//                    for (Iterator it2 = relation.getTargets().iterator(); it2.hasNext(); ) {
//                        IProgramElement pNode = ((LinkNode)it2.next()).getProgramElementNode();
//                        StructureViewNode newNode = getCrosscuttingChildren(pNode);
//                        for (Iterator it3 = pNode.getRelations().iterator(); it3.hasNext(); ) {
//							IRelationship relationNode = (IRelation)it3.next();
//                            if (relationNode.getName().indexOf("pointcut") == -1) {
//                                newNode.add(getRelations(relationNode));
//                            }
//                        }
//                        treeNode.add(newNode);
//                    }
//                } else if (relations.toString().indexOf("uses pointcut") == -1) {
//                    for (Iterator it4 = relations.iterator(); it4.hasNext(); ) {
//                        IRelation relationNode = (IRelationship)it4.next();
//                        if (relationNode.getName().indexOf("pointcut") == -1) {
//                            treeNode.add(getRelations(relationNode));
//                        }
//                    }
//                }
//            }
//        }
//        return treeNode;
    }

//    private IStructureViewNode buildTree(IProgramElement node, List associations) {
//        //StructureViewNode treeNode = new StructureViewNodeAdapter(node);
//        IStructureViewNode treeNode = nodeFactory.createNode(node);
////        if (node instanceof IProgramElement) {
//            List relations = ((IProgramElement)node).getRelations();
//            if (relations != null) {
//                for (Iterator it = relations.iterator(); it.hasNext(); ) {
//					IRelationship relationNode = (IRelationship)it.next();
//                    if (associations.contains(relationNode.toString())) {
//                        treeNode.add(buildTree(relationNode, associations));
//                    }
//                }
//            }
//        }
//        if (node != null) {
//            List children = null;
//            children = node.getChildren();
//            if (children != null) {
//                List childList = new ArrayList();
//                for (Iterator itt = children.iterator(); itt.hasNext(); ) {
//                    IProgramElement child = (IProgramElement)itt.next();
//                    if (child instanceof IProgramElement) {
//                        IProgramElement progNode = (IProgramElement)child;
////                        if (progNode.getKind() != IProgramElement.Kind.CODE) {
//                            childList.add(buildTree(child, associations));
////                        }
//                    } else {
//                        childList.add(buildTree(child, associations));
//                    }
//                }
//                //sortNodes(childList);
//                for (Iterator it = childList.iterator(); it.hasNext(); ) {
//                    treeNode.add((IStructureViewNode)it.next());
//                }
//            }
//
//        }
//        return treeNode;
//    }

//    private IStructureViewNode getRelations(IRelationship node) {
//    	return null;
//        //StructureViewNode treeNode = new StructureViewNode(node);
////        IStructureViewNode treeNode = nodeFactory.c(node);
////        for (Iterator it = node.getTargets().iterator(); it.hasNext(); ) {
////            treeNode.add(
////            	nodeFactory.createNode((IProgramElement)it.next())
////            );
////        } 
////        return treeNode;
//    }
//
//	/**
//	 * For debugging only.
//	 */
//	private void dumpView(IStructureViewNode root, int level) {
//		System.out.println(root.getStructureNode());
//		for (Iterator it = root.getChildren().iterator(); it.hasNext(); ) {
//			dumpView((IStructureViewNode)it.next(), level++);	
//		}
//		for (int i = 0; i < level; i++) {
//			System.out.print(' ');
//		}		
//	}

	/**
	 * Does not sort imports alphabetically.
	 */
    private static final Comparator ALPHABETICAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {  
        	IProgramElement sv1 = ((IStructureViewNode)o1).getStructureNode();
        	IProgramElement sv2 = ((IStructureViewNode)o2).getStructureNode();        
			
            if (sv1 instanceof IProgramElement && sv2 instanceof IProgramElement) {
         
            	IProgramElement p1 = (IProgramElement)sv1;
            	IProgramElement p2 = (IProgramElement)sv2;
            	
				if (p2.getKind() == IProgramElement.Kind.IMPORT_REFERENCE) return 1;
				if (p1.getKind() == IProgramElement.Kind.IMPORT_REFERENCE) return -1;
            	
				return p1.getName().compareTo(p2.getName());
            } else {
            	return 0;	
           }
        }
    };
    
    private static final Comparator DECLARATIONAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {            
        	IProgramElement sv1 = ((IStructureViewNode)o1).getStructureNode();
        	IProgramElement sv2 = ((IStructureViewNode)o2).getStructureNode();        
            if (sv1 instanceof IProgramElement && sv2 instanceof IProgramElement) {
            	IProgramElement p1 = (IProgramElement)sv1;
            	IProgramElement p2 = (IProgramElement)sv2;
            	if (p2.getKind() == IProgramElement.Kind.IMPORT_REFERENCE) return 1;
				if (p1.getKind() == IProgramElement.Kind.IMPORT_REFERENCE) return -1;
            	if (p1.getSourceLocation() == null || p2.getSourceLocation() == null) {
            		return 0;
            	} else if (p1.getSourceLocation().getLine() < p2.getSourceLocation().getLine()) {
            		return -1;
            	} else {
            		return 1;
            	}
            } else {
            	return 0;	
           }
        }
    };
}

//    private boolean acceptNode(ProgramElementNode node) {
//    	return true;
//        if (node.getKind().equals("package")) return true;
//
//        if (node.getKind().equals("file")) {
//            if (granularity == ViewProperties.Granularity.PACKAGE) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//
//        if (node.getKind().equals("class") || node.getKind().equals("aspect") || node.getKind().equals("interface")) {
//            if (granularity == ViewProperties.Granularity.FILE || granularity == ViewProperties.Granularity.PACKAGE) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//
//        if (node.isMemberKind()) {
//            if (granularity == ViewProperties.Granularity.MEMBER) {
//                for (Iterator it = modifiers.iterator(); it.hasNext(); ) {
//                    if (node.getModifiers().contains((String)it.next())) return false;
//                }
//                for (Iterator it2 = visibility.iterator(); it2.hasNext(); ) {
//                    if (node.getAccessibility().equals((String)it2.next()))  return false;
//                }
//                if (filteredMemberKinds.contains(node.getKind())) {
//                    return false;
//                } else {
//                    return true;
//                }
//            } else {
//                return false;
//            }
//        }
//
//        if (node.isCode()) return false;
//
//        return false;
//    }

