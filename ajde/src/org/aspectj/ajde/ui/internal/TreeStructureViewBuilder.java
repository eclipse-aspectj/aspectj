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
 * ******************************************************************/


package org.aspectj.ajde.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.GlobalStructureView;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewNode;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.Relation;
import org.aspectj.asm.RelationNode;
import org.aspectj.asm.StructureModel;
import org.aspectj.asm.StructureNode;

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
	public void buildView(StructureView view, StructureModel model) {
		StructureViewProperties properties = view.getViewProperties();
		StructureNode modelRoot = null;
		boolean noStructure = false;
		if (isFileView(view)) {
			FileStructureView fileView = (FileStructureView)view;
			if (fileView.getSourceFile() == null) {	
				modelRoot = StructureModel.NO_STRUCTURE;
				noStructure = true;
			} else {
				modelRoot = model.findRootNodeForSourceFile(fileView.getSourceFile());
			}
		} else {
			modelRoot = model.getRoot();
		}
	
		StructureViewNode viewRoot = null;
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

	private void addPackageNode(StructureView view, StructureViewNode viewRoot) {
		if (isFileView(view)) {
			ProgramElementNode fileNode = (ProgramElementNode)viewRoot.getStructureNode();
			
			StructureNode parentNode = fileNode.getParent();
			if (parentNode instanceof ProgramElementNode 
				&& ((ProgramElementNode)parentNode).getProgramElementKind().equals(ProgramElementNode.Kind.PACKAGE)) {
				String name = ((ProgramElementNode)parentNode).getName();
				ProgramElementNode packageNode = new ProgramElementNode(name, ProgramElementNode.Kind.PACKAGE, null);
				packageNode.setSourceLocation(fileNode.getSourceLocation());
				StructureViewNode packageViewNode = createViewNode(
					packageNode, 
					view.getViewProperties()
				);
				viewRoot.getChildren().add(0, packageViewNode);
			};
		}
	}
	
	private StructureViewNode createViewNode(StructureNode node, StructureViewProperties properties) {
		if (node == null) return null;
		List children = new ArrayList();
		if (node instanceof ProgramElementNode) {
			ProgramElementNode pNode = (ProgramElementNode)node;
			if (pNode.getRelations() != null) {
				for (Iterator it = pNode.getRelations().iterator(); it.hasNext(); ) {
					StructureNode structureNode = (StructureNode)it.next();
					if (acceptNode(structureNode, properties)) {
						children.add(createViewNode(structureNode, properties));
					}
				}	
			}
			if (pNode.isRunnable() && pNode.getParent() != null) {
				ProgramElementNode parent = (ProgramElementNode)pNode.getParent();
				if (parent.getProgramElementKind().equals(ProgramElementNode.Kind.CLASS)
					|| parent.getProgramElementKind().equals(ProgramElementNode.Kind.ASPECT)) {
					parent.setRunnable(true);	
					pNode.setRunnable(false);
				}
			}
		}
		if (node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				StructureNode structureNode = (StructureNode)it.next();
				if (acceptNode(structureNode, properties)) {
					children.add(createViewNode(structureNode, properties));
				}
			}	
		}
		StructureViewNode viewNode = nodeFactory.createNode(node, children);//new TreeViewNode(root, null, children);
		return viewNode;	
	}
	
	/**
	 * @todo	get rid of this test, fix polymorphism
	 */
	private boolean isFileView(StructureView view) {
		return view instanceof FileStructureView
			&& !(view instanceof GlobalStructureView);
	}
	
	private boolean acceptGranularity(ProgramElementNode.Kind kind, StructureViewProperties.Granularity granularity) {
		
		if (granularity == StructureViewProperties.Granularity.DECLARED_ELEMENTS) {
			return true;
		} else if (granularity == StructureViewProperties.Granularity.MEMBER && 
			(kind != ProgramElementNode.Kind.CODE)) {
			return true;
		} else if (granularity == StructureViewProperties.Granularity.TYPE
			&& (kind == ProgramElementNode.Kind.PROJECT
				|| kind == ProgramElementNode.Kind.PACKAGE
				|| kind.isSourceFileKind()
				|| kind.isTypeKind())) {
			return true;			
		} else if (granularity == StructureViewProperties.Granularity.FILE
			&& (kind == ProgramElementNode.Kind.PROJECT
				|| kind == ProgramElementNode.Kind.PACKAGE
				|| kind.isSourceFileKind())) {
			return true;			
		} else if (granularity == StructureViewProperties.Granularity.PACKAGE
			&& (kind == ProgramElementNode.Kind.PROJECT
				|| kind == ProgramElementNode.Kind.PACKAGE)) {
			return true;			
		} else {
			return false;
		}
	}
	
	private boolean acceptNode(StructureNode node, StructureViewProperties properties) {
		if (node instanceof ProgramElementNode) {
			ProgramElementNode pNode = (ProgramElementNode)node;
			if (!acceptGranularity(pNode.getProgramElementKind(), properties.getGranularity())) {
				return false;
			} else if (pNode.isMemberKind()) {
				if (properties.getFilteredMemberAccessibility().contains(pNode.getAccessibility())) {
					return false;	
				}
				if (properties.getFilteredMemberKinds().contains(pNode.getProgramElementKind())) {
					return false;	
				}
				for (Iterator it = pNode.getModifiers().iterator(); it.hasNext(); ) {
					if (properties.getFilteredMemberModifiers().contains(it.next())) {
						return false;	
					}	
				}
			}
		} else if (node instanceof RelationNode) {
			Relation relation = ((RelationNode)node).getRelation();
			return properties.getRelations().contains(relation);
		} else {
			return true;
		}
		return true;
	}

	private void sortView(StructureViewNode node, Comparator comparator) {
		if (node == null || node.getChildren() == null) return;
		Collections.sort(node.getChildren(), comparator);
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			StructureViewNode nextNode = (StructureViewNode)it.next();
			if (nextNode != null) sortView(nextNode, comparator);	
		}
	}

    private StructureViewNode buildCustomTree(GlobalStructureView view, StructureModel model) {
        StructureNode rootNode = model.getRoot();
        StructureViewNode treeNode = nodeFactory.createNode(rootNode);

        List rootNodes = new ArrayList();
        getRoots(rootNode, rootNodes, view.getGlobalViewProperties().getHierarchy());
		
        for (Iterator it = rootNodes.iterator(); it.hasNext(); ) {
            if (view.getGlobalViewProperties().getHierarchy().equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
                treeNode.add(getCrosscuttingChildren((StructureNode)it.next()));
            } else if (view.getGlobalViewProperties().getHierarchy().equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
                treeNode.add(getInheritanceChildren(
                	(StructureNode)it.next(),
                	view.getViewProperties().getRelations())	
                );
            }
        }
        return treeNode;
    }

    private void getRoots(StructureNode rootNode, List roots, StructureViewProperties.Hierarchy hierarchy) {
        if (rootNode != null && rootNode.getChildren() != null) {
            for (Iterator it = rootNode.getChildren().iterator(); it.hasNext(); ) {
                StructureNode node = (StructureNode)it.next();
                if (node instanceof ProgramElementNode) {
                    if (acceptNodeAsRoot((ProgramElementNode)node, hierarchy)) {
                        ProgramElementNode pNode = (ProgramElementNode)node;
                        List relations = pNode.getRelations();
                        String delimiter = "";
                        if (hierarchy.equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
                            delimiter = "uses pointcut";
                        } else if (hierarchy.equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
                            delimiter = "inherits";
                        } 
                        if (relations != null && relations.toString().indexOf(delimiter) == -1) {
                            boolean found = false;
                            for (Iterator it2 = roots.iterator(); it2.hasNext(); ) {
                                if (((ProgramElementNode)it2.next()).equals(pNode)) found = true;
                            }
                            if (!found) roots.add(pNode);
                        } 
                    } 
                }
                getRoots(node, roots, hierarchy);
            }
        }
    }

    public boolean acceptNodeAsRoot(ProgramElementNode node, StructureViewProperties.Hierarchy hierarchy) {
        if (hierarchy.equals(StructureViewProperties.Hierarchy.CROSSCUTTING)) {
            return node.getProgramElementKind().equals(ProgramElementNode.Kind.ADVICE)
                || node.getProgramElementKind().equals(ProgramElementNode.Kind.POINTCUT);
        } else if (hierarchy.equals(StructureViewProperties.Hierarchy.INHERITANCE)) {
            return node.getProgramElementKind().equals(ProgramElementNode.Kind.CLASS);
        } else {	
            return false;
        }
    }

    private StructureViewNode getInheritanceChildren(StructureNode node, List associations) {
    	StructureViewNode treeNode = nodeFactory.createNode(node);
        //StructureViewNode treeNode = new StructureViewNodeAdapter(node);
        List relations = ((ProgramElementNode)node).getRelations();
        if (relations != null) {
            for (Iterator it = relations.iterator(); it.hasNext(); ) {
                RelationNode relation = (RelationNode)it.next();
                if (relation.getName().equals("is inherited by")) {
                    for (Iterator it2 = relation.getChildren().iterator(); it2.hasNext(); ) {
                        ProgramElementNode pNode = ((LinkNode)it2.next()).getProgramElementNode();
                        StructureViewNode newNode = getInheritanceChildren(pNode, associations);
                        StructureViewNode typeChildren = buildTree(newNode.getStructureNode(), associations);
                        for (int i = 0; i < typeChildren.getChildren().size(); i++) {
                            newNode.add((StructureViewNode)typeChildren.getChildren().get(i));
                        }
						treeNode.add(newNode);
                    }
                }
            }
        }
        return treeNode;
    }

    private StructureViewNode getCrosscuttingChildren(StructureNode node) {
        //StructureViewNodeAdapter treeNode = new StructureViewNodeAdapter(node);
        StructureViewNode treeNode = nodeFactory.createNode(node);
        List relations = ((ProgramElementNode)node).getRelations();
        if (relations != null) {
            for (Iterator it = relations.iterator(); it.hasNext(); ) {
                RelationNode relation = (RelationNode)it.next();
                if (relation.getName().equals("pointcut used by")) {
                    for (Iterator it2 = relation.getChildren().iterator(); it2.hasNext(); ) {
                        ProgramElementNode pNode = ((LinkNode)it2.next()).getProgramElementNode();
                        StructureViewNode newNode = getCrosscuttingChildren(pNode);
                        for (Iterator it3 = pNode.getRelations().iterator(); it3.hasNext(); ) {
                            RelationNode relationNode = (RelationNode)it3.next();
                            if (relationNode.getName().indexOf("pointcut") == -1) {
                                newNode.add(getRelations(relationNode));
                            }
                        }
                        treeNode.add(newNode);
                    }
                } else if (relations.toString().indexOf("uses pointcut") == -1) {
                    for (Iterator it4 = relations.iterator(); it4.hasNext(); ) {
                        RelationNode relationNode = (RelationNode)it4.next();
                        if (relationNode.getName().indexOf("pointcut") == -1) {
                            treeNode.add(getRelations(relationNode));
                        }
                    }
                }
            }
        }
        return treeNode;
    }

    private StructureViewNode buildTree(StructureNode node, List associations) {
        //StructureViewNode treeNode = new StructureViewNodeAdapter(node);
        StructureViewNode treeNode = nodeFactory.createNode(node);
        if (node instanceof ProgramElementNode) {
            List relations = ((ProgramElementNode)node).getRelations();
            if (relations != null) {
                for (Iterator it = relations.iterator(); it.hasNext(); ) {
                    RelationNode relationNode = (RelationNode)it.next();
                    if (associations.contains(relationNode.getRelation().toString())) {
                        treeNode.add(buildTree(relationNode, associations));
                    }
                }
            }
        }
        if (node != null) {
            List children = null;
            children = node.getChildren();
            if (children != null) {
                List childList = new ArrayList();
                for (Iterator itt = children.iterator(); itt.hasNext(); ) {
                    StructureNode child = (StructureNode)itt.next();
                    if (child instanceof ProgramElementNode) {
                        ProgramElementNode progNode = (ProgramElementNode)child;
                        if (!progNode.isCode()) {
                            childList.add(buildTree(child, associations));
                        }
                    } else {
                        childList.add(buildTree(child, associations));
                    }
                }
                //sortNodes(childList);
                for (Iterator it = childList.iterator(); it.hasNext(); ) {
                    treeNode.add((StructureViewNode)it.next());
                }
            }

        }
        return treeNode;
    }

    private StructureViewNode getRelations(RelationNode node) {
        //StructureViewNode treeNode = new StructureViewNode(node);
        StructureViewNode treeNode = nodeFactory.createNode(node);
        for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
            treeNode.add(
            	nodeFactory.createNode((StructureNode)it.next())
            );
        }
        return treeNode;
    }

	/**
	 * For debugging only.
	 */
	private void dumpView(StructureViewNode root, int level) {
		System.out.println(root.getStructureNode());
		for (Iterator it = root.getChildren().iterator(); it.hasNext(); ) {
			dumpView((StructureViewNode)it.next(), level++);	
		}
		for (int i = 0; i < level; i++) {
			System.out.print(' ');
		}		
	}

    private static final Comparator ALPHABETICAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {  
        	StructureNode sv1 = ((StructureViewNode)o1).getStructureNode();
        	StructureNode sv2 = ((StructureViewNode)o2).getStructureNode();        
            if (sv1 instanceof ProgramElementNode && sv2 instanceof ProgramElementNode) {
            	ProgramElementNode p1 = (ProgramElementNode)sv1;
            	ProgramElementNode p2 = (ProgramElementNode)sv2;
				return p1.getName().compareTo(p2.getName());
            } else {
            	return 0;	
           }
        }
    };
    
    private static final Comparator DECLARATIONAL_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {            
        	StructureNode sv1 = ((StructureViewNode)o1).getStructureNode();
        	StructureNode sv2 = ((StructureViewNode)o2).getStructureNode();        
            if (sv1 instanceof ProgramElementNode && sv2 instanceof ProgramElementNode) {
            	ProgramElementNode p1 = (ProgramElementNode)sv1;
            	ProgramElementNode p2 = (ProgramElementNode)sv2;
            	if (p1.getSourceLocation() == null) {
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

