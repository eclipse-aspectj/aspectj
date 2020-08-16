/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initital version
 * 				 Matthew Webster - moved tests
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.util.List;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.AjAST;
import org.aspectj.org.eclipse.jdt.core.dom.AjTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AspectDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareParentsDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DefaultTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.PerTypeWithin;
import org.aspectj.org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.TypePattern;


public class AjAST5Test extends AjASTTestCase {

	public void testInternalAspectDeclaration() {
		AjAST ajast = createAjAST();
		AspectDeclaration d = ajast.newAspectDeclaration();
		List props = AspectDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("perClause")) {
					assertNull("AspectDeclaration's " + element.getId() + " property" +
									"should be null since we haven't set it yet",
							d.getStructuralProperty(element));
				}
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				assertNotNull("AspectDeclaration's " + element.getId() + " property" +
								"should not be null since it is a boolean",
						d.getStructuralProperty(element));
			}
		}
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("perClause")) {
					PerTypeWithin ptw = ajast.newPerTypeWithin();
					d.setStructuralProperty(element, ptw);
					assertEquals("AspectDeclaration's perClause property should" +
							" now be a perTypeWithin", ptw, d.getStructuralProperty(element));
				} else if (element.getId().equals("javadoc")) {
					// do nothing since makes no sense to have javadoc
				}
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				if (element.getId().equals("privileged")) {
					Boolean b = Boolean.TRUE;
					d.setStructuralProperty(element, b);
					assertEquals("AspectDeclaration's isPrivileged property should" +
							" now be a boolean", b, d.getStructuralProperty(element));
				}
			}
		}
	}
	
	public void testInternalAjTypeDeclaration() {
		AjAST ajast = createAjAST();
		AjTypeDeclaration d = ajast.newAjTypeDeclaration();
		List props = AjTypeDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				if (element.getId().equals("aspect")) {
					assertNotNull("AjTypeDeclaration's " + element.getId() + " property" +
									" should not be null since it is a boolean",
							d.getStructuralProperty(element));
				}
			}
		}
		for (Object o : props) {
			if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				if (element.getId().equals("aspect")) {
					Boolean b = Boolean.TRUE;
					d.setStructuralProperty(element, b);
					assertEquals("AjTypeDeclaration's aspect property should" +
							" now be a SignaturePattern", b, d.getStructuralProperty(element));
				}
			}
		}
	}
	
	public void testInternalDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		List props = DeclareParentsDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				assertNull("DeclareParentsDeclaration's " + element.getId() + " property" +
								"should be null since we haven't set it yet",
						d.getStructuralProperty(element));
			} else if (o instanceof ChildListPropertyDescriptor) {
				ChildListPropertyDescriptor element = (ChildListPropertyDescriptor) o;
				assertNotNull("DeclareParentsDeclaration's " + element.getId() + " property" +
								"should not be null since it is a list",
						d.getStructuralProperty(element));
				assertEquals("should only be able to put TypePattern's into the list",
						TypePattern.class, element.getElementType());
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				assertNotNull("DeclareParentsDeclaration's " + element.getId() + " property" +
								"should not be null since it is a boolean",
						d.getStructuralProperty(element));
			} else {
				fail("unknown PropertyDescriptor associated with DeclareParentsDeclaration: " + o);
			}
		}
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("childTypePattern")) {
					DefaultTypePattern dtp = ajast.newDefaultTypePattern();
					d.setStructuralProperty(element, dtp);
					assertEquals("DeclareParentsDeclaration's typePattern property should" +
							" now be a DefaultTypePattern", dtp, d.getStructuralProperty(element));
				} else if (element.getId().equals("javadoc")) {
					// do nothing since makes no sense to have javadoc
				} else {
					fail("unknown property for DeclareParentsDeclaration");
				}
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				if (element.getId().equals("isExtends")) {
					Boolean b = Boolean.TRUE;
					d.setStructuralProperty(element, b);
					assertEquals("DeclareParentsDeclaration's isExtends property should" +
							" now be a boolean", b, d.getStructuralProperty(element));
				}
			}
		}
	}

}
