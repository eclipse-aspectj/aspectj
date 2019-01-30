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


package org.aspectj.ajde.ui;

import java.io.ObjectStreamException;
import java.util.*;

import org.aspectj.asm.*;

/** 
 * Nested properties use the typesafe enum pattern.
 * 
 * @author Mik Kersten
 */
public class StructureViewProperties {
	
	/**
	 * @deprecated
	 */
	public static final String SORT_DECLARATIONAL = StructureViewProperties.Sorting.DECLARATIONAL.toString();
	
	/**
	 * @deprecated
	 */ 
	public void setSorting(String sorting) { }
	
    private List relations = new ArrayList();
    private List filteredMemberAccessibility = new ArrayList();
    private List filteredMemberModifiers = new ArrayList();
    private List filteredMemberKinds = new ArrayList();
    private List grouping = new ArrayList();
    private Sorting sorting = Sorting.DECLARATIONAL;
	private Granularity granularity = StructureViewProperties.Granularity.DECLARED_ELEMENTS;

    public List getRelations() {
        return relations;
    }
    
    public void setRelations(List relations) {
    	this.relations = relations;	
    }

    public void addRelation(IRelationship.Kind kind) {
        relations.add(kind);
    }

    public void removeRelation(IRelationship.Kind kind) {
        relations.remove(kind);
    }

    public void setFilteredMemberAccessibility(List memberVisibility) {
        this.filteredMemberAccessibility = memberVisibility;
    }

    public List getFilteredMemberAccessibility() {
        return filteredMemberAccessibility;
    }

    public void addFilteredMemberAccessibility(IProgramElement.Accessibility accessibility) {
 	   	this.filteredMemberAccessibility.add(accessibility);
	}
	
	public void removeFilteredMemberAccessibility(IProgramElement.Accessibility accessibility) {
		this.filteredMemberAccessibility.remove(accessibility);	
	}

    public List getFilteredMemberModifiers() {
        return filteredMemberModifiers;
    }

    public void setFilteredMemberModifiers(List memberModifiers) {
        this.filteredMemberModifiers = memberModifiers;
    }
    
    public void addFilteredMemberModifiers(IProgramElement.Modifiers modifiers) {
 	   	this.filteredMemberModifiers.add(modifiers);
	}
	
	public void removeFilteredMemberModifiers(IProgramElement.Modifiers modifiers) {
		this.filteredMemberModifiers.remove(modifiers);	
	}
    
    public StructureViewProperties.Sorting getSorting() {
        return sorting;
    }

    public void setSorting(StructureViewProperties.Sorting sorting) {
        this.sorting = sorting;
    }
    
	public List getFilteredMemberKinds() {
		return filteredMemberKinds;
	}

	public void setFilteredMemberKinds(List memberKinds) {
		this.filteredMemberKinds = memberKinds;
	}
	
    public void addFilteredMemberKind(IProgramElement.Kind kind) {
 	   	this.filteredMemberKinds.add(kind);
	}
	
	public void removeFilteredMemberKind(IProgramElement.Kind kind) {
		this.filteredMemberKinds.remove(kind);	
	}
	
	public List getGrouping() {
		return grouping;
	}

	public void setGrouping(List grouping) {
		this.grouping = grouping;
	}
	
	
	public void addGrouping(Grouping grouping) {
		this.grouping.add(grouping);
	}

	public void removeGrouping(Grouping grouping) {
		this.grouping.remove(grouping);
	}

	public Granularity getGranularity() {
		return granularity;
	}

	public void setGranularity(Granularity granularity) {
		this.granularity = granularity;
	}
	
	public String getName() {
		return "<unnamed view>";
	}
	
	public String toString() {
		return "\nView Properties:"
			+ "\n-> sorting: " + sorting	
			+ "\n-> grouping: " + grouping
			+ "\n-> filtered member kinds: " + filteredMemberKinds
			+ "\n-> filtered member accessibility: " + filteredMemberAccessibility
			+ "\n-> filtered member modifiers: " + filteredMemberModifiers
			+ "\n-> relations: " + relations;
	}
	
	public static class Hierarchy {
		
		public static final Hierarchy DECLARATION = new Hierarchy("package hierarchy");
		public static final Hierarchy CROSSCUTTING = new Hierarchy("crosscutting structure");
		public static final Hierarchy INHERITANCE = new Hierarchy("type hierarchy");
		public static final Hierarchy[] ALL = { DECLARATION, CROSSCUTTING, INHERITANCE };
		  
		private final String name;
		
		private Hierarchy(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
	
	public static class Grouping {
		
		public static final Grouping KIND = new Grouping("group by kind");
		public static final Grouping VISIBILITY = new Grouping("group by visibility");
		public static final Grouping[] ALL = { KIND, VISIBILITY };
		
		private final String name;
		
		private Grouping(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
 
	public static class Sorting {
	
 		public static final Sorting ALPHABETICAL = new Sorting("sort alphabetically");
 		public static final Sorting DECLARATIONAL = new Sorting("sort declarationally");
 		public static final Sorting[] ALL = { ALPHABETICAL, DECLARATIONAL };
		
		private final String name;
		
		private Sorting(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
	
	public static class Granularity {
	
		public static final Granularity PACKAGE = new Granularity("package");
		public static final Granularity FILE = new Granularity("file");
		public static final Granularity TYPE = new Granularity("type");
		public static final Granularity MEMBER = new Granularity("member");
		public static final Granularity DECLARED_ELEMENTS = new Granularity("declared body elements");
		public static final Granularity[] ALL = { PACKAGE, FILE, TYPE, MEMBER, DECLARED_ELEMENTS };
		
		private final String name;
		
		private Granularity(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
}
