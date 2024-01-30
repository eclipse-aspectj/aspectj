package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */
 
import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.Vector;  

/**
 * Defines the abstract Composite design pattern.<p> 
 * 
 * It maintains the mapping between composites and their children, defines the
 * Component, Composite, and Leaf roles, and implements facilities to
 * implements methods that work on the whole aggregate structure.
 *
 * <p><i>This is the AspectJ version.</i><p> 
 *
 * Each concrete subaspect does the following things: <UL>
 * <LI> Defines which classes are Components and Leafs
 * <LI> (optional) Defines methods that operate on the whole aggregate
 *      structure (using visitors)
 * </UL>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 */

public abstract aspect CompositeProtocol {   
    
    /**
     * Defines the Component role. The role is public to allow clients to 
     * handle objects of that type.
     */
    
    public interface Component {}                                       

    /**
     * Defines the Composite role. Composites are Components that can have
     * children. This role is only used within the pattern context, thus it
     * is protected.
     */

    protected interface Composite extends Component {}
    
    /**
     * Defines the Leaf role. Leafs are Components that can not have
     * children. This role is only used within the pattern context, thus it
     * is protected.
     */

    protected interface Leaf      extends Component {}
    
    /**
     * stores the mapping between components and their children
     */    

    private WeakHashMap perComponentChildren = new WeakHashMap();

    /** 
     * Returns a vector of the children of the argument component
     */

    private Vector getChildren(Component s) {
        Vector children = (Vector)perComponentChildren.get(s);
        if ( children == null ) {
            children = new Vector();
            perComponentChildren.put(s, children);
        }
        return children;
    }
    
    /**
     * Client-accessible method to add a new child to a composite
     *
     * @param composite the composite to add a new child to
     * @param component the new child to add
     */
    
    public void    addChild(Composite composite, Component component) { 
        getChildren(composite).add(component);    
    }

    /**
     * Client-accessible method to remove a child from a composite
     *
     * @param composite the composite to remove a child from
     * @param component the child to remove
     */

    public void removeChild(Composite composite, Component component) { 
        getChildren(composite).remove(component); 
    }
    
    /**
     * Client-accessible method to get an Enumeration of all children of 
     * a composite
     *
     * @param composite the composite to add a new child to
     * @param component the new child to add
     */

    public Enumeration getAllChildren(Component c)  { 
        return getChildren(c).elements(); 
    }



    /** 
     * Defines an interface for visitors that operate on the composite 
     * structure. These visitors are implemented by concrete sub-aspects
     * and used in the <code>recurseOperation(Component, Visitor)</code>
     * method. This construct is needed to allow for method forwarding:
     * A composite that receives a method forwards the request to all its
     * children.
     */
   
    protected interface Visitor { 
        
        /**
         * Generic method that performs an unspecified operation on compoennts
         *
         * @param c the component to perform the operation on
         */
    
        public void doOperation(Component c);
    }

    /** 
     * Implements the method-forwarding logic: If a method is to be applied 
     * to the aggregate structure, each composite forwards it to its children
     *
     * @param c the current component
     * @param v the visitor representing the operation to be performed
     */

    public void recurseOperation(Component c, Visitor v) {                    // This implements the logic that Composites forward 
        for (Enumeration enum = getAllChildren(c); enum.hasMoreElements(); ) {  // method calls to their children
            Component child = (Component) enum.nextElement();
            v.doOperation(child);
        }
    }
    
    
    
    /** 
     * Defines an interface for visitors that operate on the composite 
     * structure. These visitors are implemented by comcrete sub-aspects
     * and used in the <code>recurseOperation(Component, Visitor)<>/code>
     * method. This construct is needed to allow for method forwarding:
     * A composite that receives a method forwards the request to all its
     * children.  <p>
     *
     * This version allows for a return value of Object type. For some odd
     * reason AJDT complains if this type is declared protected (as it should
     * be). Note that Visitor above works fine as protected. 
     */

    public interface FunctionVisitor {
 
        /**
         * Generic method that performs an unspecified operation on components
         *
         * @param c the component to perform the operation on
         */
 
        public Object doFunction(Component c);
    }
    
    /** 
     * Implements the method-forwarding logic: If a method is to be applied 
     * to the aggregate structure, each composite forwards it to its children.
     * This version allows for a return value of Object type, effectively
     * enableing collecting aggregate results on the composite structure.
     *
     * @param c the current component
     * @param fv the visitor representing the operation to be performed
     */

    public Enumeration recurseFunction(Component c, FunctionVisitor fv) {
        Vector results = new Vector();
        for (Enumeration enum = getAllChildren(c); enum.hasMoreElements(); ) {  // method calls to their children
            Component child = (Component) enum.nextElement();
            results.add(fv.doFunction(child));
        }
        return results.elements();
    } 
} 

