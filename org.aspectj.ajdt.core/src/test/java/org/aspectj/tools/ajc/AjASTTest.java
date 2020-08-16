/********************************************************************
 * Copyright (c) 2006, 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aspectj.org.eclipse.jdt.core.SourceRange;
import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTNode;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AbstractBooleanTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AfterAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AfterReturningAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AfterThrowingAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AjAST;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.AjTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AndPointcut;
import org.aspectj.org.eclipse.jdt.core.dom.AnyTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AnyWithAnnotationTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.AroundAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.AspectDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.BeforeAdviceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.Block;
import org.aspectj.org.eclipse.jdt.core.dom.CflowPointcut;
import org.aspectj.org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareAtTypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareErrorDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareParentsDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclarePrecedenceDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareSoftDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DeclareWarningDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.DefaultPointcut;
import org.aspectj.org.eclipse.jdt.core.dom.DefaultTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.EllipsisTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.HasMemberTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.IExtendedModifier;
import org.aspectj.org.eclipse.jdt.core.dom.IdentifierTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.Javadoc;
import org.aspectj.org.eclipse.jdt.core.dom.NoTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.NotPointcut;
import org.aspectj.org.eclipse.jdt.core.dom.NotTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.OrPointcut;
import org.aspectj.org.eclipse.jdt.core.dom.PerCflow;
import org.aspectj.org.eclipse.jdt.core.dom.PerObject;
import org.aspectj.org.eclipse.jdt.core.dom.PerTypeWithin;
import org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.PrimitiveType;
import org.aspectj.org.eclipse.jdt.core.dom.ReferencePointcut;
import org.aspectj.org.eclipse.jdt.core.dom.SignaturePattern;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
import org.aspectj.org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.aspectj.org.eclipse.jdt.core.dom.SimpleType;
import org.aspectj.org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.StringLiteral;
import org.aspectj.org.eclipse.jdt.core.dom.Type;
import org.aspectj.org.eclipse.jdt.core.dom.TypeCategoryTypePattern;
import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.core.dom.TypePattern;

/**
 * For each AspectJ ASTNode there is a test for:
 *
 * - that a new instance can be created via ajast.newXXX() - that the property descriptors have been set correctly - that the
 * get/set methods for the different properties work as expected - that the clone0 method sets the correct properties - that the
 * internalStructuralPropertiesForType(int) and internalGetSetXXXProperty(..) methods have been implemented correctly
 *
 * These are all that is required for an ASTNode, except an implementation of the accept0() method which is tested in
 * ASTVisitorTest.
 */
public class AjASTTest extends AjASTTestCase {

	// -------------- DefaultPointcut tests ---------------

	public void testNewDefaultPointcut() {
		AjAST ajast = createAjAST();
		DefaultPointcut p = ajast.newDefaultPointcut();
		assertNotNull("a new DefaultPointcut should have been created", p);
	}

	public void testGetAndSetDetail() {
		AjAST ajast = createAjAST();
		DefaultPointcut p = ajast.newDefaultPointcut();
		assertEquals("The default detail should be the empty string", "", p.getDetail());
		p.setDetail("some detail");
		assertEquals("The detail should now be 'some detail'", "some detail", p.getDetail());
	}

	// -------------- ReferencePointcut tests ---------------

	public void testNewReferencePointcut() {
		AjAST ajast = createAjAST();
		ReferencePointcut p = ajast.newReferencePointcut();
		assertNotNull("a new ReferencePointcut should have been created", p);
	}

	/**
	 * ReferencePointcut's have a name property - tests the getting and setting of this property
	 */
	public void testGetAndSetRefPointcutName() {
		AjAST ajast = createAjAST();
		ReferencePointcut p = ajast.newReferencePointcut();
		assertEquals("the default reference pointcut name should be MISSING", "MISSING", p.getName().getFullyQualifiedName());
		p.setName(ajast.newSimpleName("refPointcutName"));
		assertEquals("the pointcut name should now be set to 'refPointcutName'", "refPointcutName", p.getName()
				.getFullyQualifiedName());
	}

	public void testNewAndPointcut() {
		AjAST ajast = createAjAST();
		AndPointcut p = ajast.newAndPointcut();
		assertNotNull("a new AndPointcut should have been created", p);
	}

	// -------------- AndPointcut tests ---------------

	/**
	 * AndPointcut's have a left property - test the getting and setting of this property
	 */
	public void testGetAndSetLeftOfAndPointcut() {
		AjAST ajast = createAjAST();
		AndPointcut ap = ajast.newAndPointcut();
		assertNull("by default the left side of an AndPointcut should be null", ap.getLeft());
		ReferencePointcut p = ajast.newReferencePointcut();
		ap.setLeft(p);
		assertEquals("the left side of the AndPointcut should now be the" + " ReferencePointcut", p, ap.getLeft());
	}

	/**
	 * AndPointcut's have a right property - test the getting and setting of this property
	 */
	public void testGetAndSetRightOfAndPointcut() {
		AjAST ajast = createAjAST();
		AndPointcut ap = ajast.newAndPointcut();
		assertNull("by default the right side of an AndPointcut should be null", ap.getRight());
		ReferencePointcut p = ajast.newReferencePointcut();
		ap.setRight(p);
		assertEquals("the right side of the AndPointcut should now be the" + " ReferencePointcut", p, ap.getRight());
	}

	// -------------- CflowPointcut tests ---------------

	public void testNewCflowPointcut() {
		AjAST ajast = createAjAST();
		CflowPointcut p = ajast.newCflowPointcut();
		assertNotNull("a new CflowPointcut should have been created", p);
	}

	/**
	 * CflowPointcut's have a body property - test the getting and setting of this property
	 */
	public void testGetAndSetBodyOfCflowPointcut() {
		AjAST ajast = createAjAST();
		CflowPointcut p = ajast.newCflowPointcut();
		assertNull("by default a CflowPointcut should have a null body", p.getBody());
		ReferencePointcut rp = ajast.newReferencePointcut();
		p.setBody(rp);
		assertEquals("the body of the CflowPointcut should now be a " + "ReferencePointcut", rp, p.getBody());
	}

	// -------------- NotPointcut tests ---------------

	public void testNewNotPointcut() {
		AjAST ajast = createAjAST();
		NotPointcut p = ajast.newNotPointcut();
		assertNotNull("a new NotPointcut should have been created", p);
	}

	/**
	 * NotPointcut's have a body property - test the getting and setting of this property
	 */
	public void testGetAndSetBodyOfNotPointcut() {
		AjAST ajast = createAjAST();
		NotPointcut p = ajast.newNotPointcut();
		assertNull("by default a NotPointcut should have a null body", p.getBody());
		ReferencePointcut rp = ajast.newReferencePointcut();
		p.setBody(rp);
		assertEquals("the body of the NotPointcut should now be a " + "ReferencePointcut", rp, p.getBody());
	}

	// -------------- OrPointcut tests ---------------

	public void testNewOrPointcut() {
		AjAST ajast = createAjAST();
		OrPointcut p = ajast.newOrPointcut();
		assertNotNull("a new OrPointcut should have been created", p);
	}

	/**
	 * OrPointcut's have a left property - test the getting and setting of this property
	 */
	public void testGetAndSetLeftOfOrPointcut() {
		AjAST ajast = createAjAST();
		OrPointcut op = ajast.newOrPointcut();
		assertNull("by default the left side of an OrPointcut should be null", op.getLeft());
		ReferencePointcut p = ajast.newReferencePointcut();
		op.setLeft(p);
		assertEquals("the left side of the OrPointcut should now be the" + " ReferencePointcut", p, op.getLeft());
	}

	/**
	 * OrPointcut's have a right property - test the getting and setting of this property
	 */
	public void testGetAndSetRightOfOrPointcut() {
		AjAST ajast = createAjAST();
		OrPointcut op = ajast.newOrPointcut();
		assertNull("by default the right side of an OrPointcut should be null", op.getRight());
		ReferencePointcut p = ajast.newReferencePointcut();
		op.setRight(p);
		assertEquals("the right side of the OrPointcut should now be the" + " ReferencePointcut", p, op.getRight());
	}

	// -------------- PerCflow tests ---------------

	public void testNewPerCflow() {
		AjAST ajast = createAjAST();
		PerCflow p = ajast.newPerCflow();
		assertNotNull("a new PerCflow should have been created", p);
	}

	/**
	 * PerCflow's have a body property - test the getting and setting of this property
	 */
	public void testGetAndSetBodyOfPerCflow() {
		AjAST ajast = createAjAST();
		PerCflow p = ajast.newPerCflow();
		assertNull("by default a PerCflow should have a null body", p.getBody());
		ReferencePointcut rp = ajast.newReferencePointcut();
		p.setBody(rp);
		assertEquals("the body of the PerCflow should now be a " + "ReferencePointcut", rp, p.getBody());
	}

	// -------------- PerObject tests ---------------

	public void testNewPerObject() {
		AjAST ajast = createAjAST();
		PerObject p = ajast.newPerObject();
		assertNotNull("a new PerObject should have been created", p);
	}

	/**
	 * PerObject's have a body property - test the getting and setting of this property
	 */
	public void testGetAndSetBodyOfPerObject() {
		AjAST ajast = createAjAST();
		PerObject p = ajast.newPerObject();
		assertNull("by default a PerObject should have a null body", p.getBody());
		ReferencePointcut rp = ajast.newReferencePointcut();
		p.setBody(rp);
		assertEquals("the body of the PerObject should now be a " + "ReferencePointcut", rp, p.getBody());
	}

	// -------------- PerTypeWithin tests ---------------

	public void testNewPerTypeWithin() {
		AjAST ajast = createAjAST();
		PerTypeWithin p = ajast.newPerTypeWithin();
		assertNotNull("a new PerTypeWithin should have been created", p);
	}

	// -------------- DefaultTypePattern tests ---------------

	public void testNewDefaultTypePattern() {
		AjAST ajast = createAjAST();
		DefaultTypePattern p = ajast.newDefaultTypePattern();
		assertNotNull("a new DefaultTypePattern should have been created", p);
	}

	public void testGetAndSetDetailInDefaultTypePattern() {
		AjAST ajast = createAjAST();
		DefaultTypePattern p = ajast.newDefaultTypePattern();
		assertEquals("The default detail should be the empty string", "", p.getDetail());
		p.setDetail("some detail");
		assertEquals("The detail should now be 'some detail'", "some detail", p.getDetail());
	}

	public void testPropertyDescriptorsForDefaultTypePattern() {
		AjAST ajast = createAjAST();
		// DefaultTypePattern d =
		ajast.newDefaultTypePattern();
		List props = DefaultTypePattern.propertyDescriptors(AST.JLS3);
		assertEquals("there should be no properties for the DefaultTypePattern", 0, props.size());
	}

	public void testCloneDefaultTypePattern() {
		AjAST ajast = createAjAST();
		DefaultTypePattern d = ajast.newDefaultTypePattern();
		d.setDetail("new detail");
		DefaultTypePattern copy = (DefaultTypePattern) ASTNode.copySubtree(ajast, d);
		assertEquals("the copy should have detail 'new detail'", "new detail", copy.getDetail());
	}

	// -------------- SignaturePattern tests ---------------

	public void testNewSignaturePattern() {
		AjAST ajast = createAjAST();
		SignaturePattern p = ajast.newSignaturePattern();
		assertNotNull("a new SignaturePattern should have been created", p);
	}

	public void testGetAndSetDetailInSignaturePattern() {
		AjAST ajast = createAjAST();
		SignaturePattern p = ajast.newSignaturePattern();
		assertEquals("The default detail should be the empty string", "", p.getDetail());
		p.setDetail("some detail");
		assertEquals("The detail should now be 'some detail'", "some detail", p.getDetail());
	}

	public void testPropertyDescriptorsForSignaturePattern() {
		AjAST ajast = createAjAST();
		// SignaturePattern p =
		ajast.newSignaturePattern();
		List props = SignaturePattern.propertyDescriptors(AST.JLS3);
		assertEquals("there should be no properties for the DefaultTypePattern", 0, props.size());
	}

	public void testCloneDefaultSignaturePattern() {
		AjAST ajast = createAjAST();
		SignaturePattern p = ajast.newSignaturePattern();
		p.setDetail("new detail");
		SignaturePattern copy = (SignaturePattern) ASTNode.copySubtree(ajast, p);
		assertEquals("the copy should have detail 'new detail'", "new detail", copy.getDetail());
	}

	// -------------- PointcutDeclaration tests ---------------

	public void testNewPointcutDeclaration() {
		AjAST ajast = createAjAST();
		PointcutDeclaration pd = ajast.newPointcutDeclaration();
		assertNotNull("a new PointcutDeclaration should have been created", pd);
	}

	/**
	 * PointcutDeclarations's have a name property - test the getting and setting of this property
	 */
	public void testGetAndSetPointcutName() {
		AjAST ajast = createAjAST();
		PointcutDeclaration pd = ajast.newPointcutDeclaration();
		assertEquals("the default pointcut name should be MISSING", "MISSING", pd.getName().getFullyQualifiedName());
		pd.setName(ajast.newSimpleName("p"));
		assertEquals("the pointcut name should now be set to 'p'", "p", pd.getName().getFullyQualifiedName());
	}

	/**
	 * PointcutDeclarations's have a designator property - test the getting and setting of this property
	 */
	public void testGetAndSetPointcutDesignator() {
		AjAST ajast = createAjAST();
		PointcutDeclaration pd = ajast.newPointcutDeclaration();
		assertNull("by default the pointcut designator is null", pd.getDesignator());
		ReferencePointcut rp = ajast.newReferencePointcut();
		pd.setDesignator(rp);
		assertEquals("should have set the pointcut designator to be " + "the ReferencePointcut", rp, pd.getDesignator());
	}

	public void testGetAndSetPointcutArguments() {
		AjAST ajast = createAjAST();
		PointcutDeclaration pd = ajast.newPointcutDeclaration();
		assertEquals("by default the number of arguments is zero", pd.parameters().size(), 0);
		List l = pd.parameters();
		assertEquals("there shouldn't be any arguments associated with" + "the pointcut yet", 0, l.size());
		SingleVariableDeclaration p1 = ajast.newSingleVariableDeclaration();
		l.add(p1);
		assertEquals("there should be one parameter associated with" + "the pointcut", 1, pd.parameters().size());
		assertEquals("there should be a SingleVariableDeclaration associated with" + "the pointcut", p1, pd.parameters().get(0));
	}

	public void testPropertyDescriptorsForPointcutDeclaration() {
		AjAST ajast = createAjAST();
		// PointcutDeclaration d =
		ajast.newPointcutDeclaration();
		List props = PointcutDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundModifiers = false;
		boolean foundName = false;
		boolean foundParamList = false;
		boolean foundDesignator = false;
		for (Object o : props) {
			if ((o instanceof ChildPropertyDescriptor)) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("javadoc")) {
					foundJavadoc = true;
				} else if (id.equals("designator")) {
					foundDesignator = true;
				} else if (id.equals("name")) {
					foundName = true;
				} else {
					fail("unknown PropertyDescriptor associated with PointcutDeclaration: " + element.getId());
				}
			} else if (o instanceof ChildListPropertyDescriptor) {
				ChildListPropertyDescriptor element = (ChildListPropertyDescriptor) o;
				if (element.getId().equals("parameters")) {
					foundParamList = true;
				} else if (element.getId().equals("modifiers")) {
					foundModifiers = true;
				}
			} else {
				fail("unknown PropertyDescriptor associated with PointcutDeclaration: " + o);
			}
		}
		assertTrue("PointcutDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("PointcutDeclaration should have a designator PropertyDescriptor", foundDesignator);
		assertTrue("PointcutDeclaration should have an name PropertyDescriptor", foundName);
		assertTrue("PointcutDeclaration should have a parameters PropertyDescriptor", foundParamList);
		assertTrue("PointcutDeclaration should have a modifiers PropertyDescriptor", foundModifiers);
	}

	public void testClonePointcutDeclaration() {
		AjAST ajast = createAjAST();
		PointcutDeclaration d = ajast.newPointcutDeclaration();
		d.setName(ajast.newSimpleName("pointcut_name"));
		d.parameters().add(ajast.newSingleVariableDeclaration());
		PointcutDeclaration copy = (PointcutDeclaration) ASTNode.copySubtree(ajast, d);
		assertEquals("there should be one parameter associated with" + "the pointcut copy", 1, copy.parameters().size());
		assertEquals("the PointcutDeclaration clone should have the name ", "pointcut_name", copy.getName().toString());
	}

	public void testInternalPointcutDeclaration() {
		AjAST ajast = createAjAST();
		PointcutDeclaration d = ajast.newPointcutDeclaration();
		List props = PointcutDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("name")) {
					assertNotNull("PointcutDeclaration's " + element.getId() + " property"
							+ " should not be null since it is lazily created", d.getStructuralProperty(element));
				} else {
					assertNull("PointcutDeclaration's " + element.getId() + " property"
							+ " should be null since we haven't set it yet", d.getStructuralProperty(element));
				}
			} else if (o instanceof ChildListPropertyDescriptor) {
				ChildListPropertyDescriptor element = (ChildListPropertyDescriptor) o;
				assertNotNull("PointcutDeclaration's " + element.getId() + " property" + "should not be null since it is a list", d
						.getStructuralProperty(element));
				boolean isIExtendedModifier = element.getElementType().equals(IExtendedModifier.class);
				boolean isSingleVariable = element.getElementType().equals(SingleVariableDeclaration.class);
				assertTrue("should only be able to put SingleVariableDeclaration's"
						+ " (which itself has node type IExtendedModifier) into the list", isIExtendedModifier || isSingleVariable);
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				assertNotNull(
						"PointcutDeclaration's " + element.getId() + " property" + "should not be null since it is a boolean", d
								.getStructuralProperty(element));
			} else {
				fail("unknown PropertyDescriptor associated with PointcutDeclaration: " + o);
			}
		}
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("designator")) {
					ReferencePointcut rp = ajast.newReferencePointcut();
					d.setStructuralProperty(element, rp);
					assertEquals("PointcutDeclaration's designator property should" + " now be a ReferencePointcut", rp, d
							.getStructuralProperty(element));
				} else if (element.getId().equals("javadoc")) {
					// do nothing since makes no sense to have javadoc
				} else if (element.getId().equals("name")) {
					SimpleName sn = ajast.newSimpleName("p");
					d.setStructuralProperty(element, sn);
					assertEquals("PointcutDeclaration's name property should" + " now be a SimpleName", sn, d
							.getStructuralProperty(element));
				}
			}
		}
	}

	// -------------- AspectDeclaration tests ---------------

	public void testNewAspectDeclaration() {
		AjAST ajast = createAjAST();
		AspectDeclaration ad = ajast.newAspectDeclaration();
		assertNotNull("a new AspectDeclaration should have been created", ad);
	}

	public void testPropertyDescriptorsForAspectDeclaration() {
		AjAST ajast = createAjAST();
		// AspectDeclaration d =
		ajast.newAspectDeclaration();
		List props = AspectDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPerClause = false;
		boolean foundIsPrivileged = false;
		boolean foundIsAspect = false;
		for (Object o : props) {
			if ((o instanceof ChildPropertyDescriptor)) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("javadoc")) {
					foundJavadoc = true;
				} else if (id.equals("perClause")) {
					foundPerClause = true;
				}
			} else if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("privileged")) {
					foundIsPrivileged = true;
				} else if (id.equals("aspect")) {
					foundIsAspect = true;
				}
			}
		}
		assertTrue("AspectDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("AspectDeclaration should have a perClause PropertyDescriptor", foundPerClause);
		assertTrue("AspectDeclaration should have a privileged PropertyDescriptor", foundIsPrivileged);
		assertTrue("AspectDeclaration should have inherited an aspect PropertyDescriptor", foundIsAspect);
	}

	public void testCloneAspectDeclaration() {
		AjAST ajast = createAjAST();
		AspectDeclaration d = ajast.newAspectDeclaration();
		d.setPerClause(ajast.newPerTypeWithin());
		d.setPrivileged(true);
		AspectDeclaration copy = (AspectDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the AspectDeclaration clone should have a perClause set", copy.getPerClause());
		assertTrue("the AspectDeclaration clone should be a 'privileged'", copy.isPrivileged());
	}

	/**
	 * AsepctDeclarations's have a perClause property - test the getting and setting of this property
	 */
	public void testSetPerClauseInAspectDeclaration() {
		AjAST ajast = createAjAST();
		AspectDeclaration ad = ajast.newAspectDeclaration();
		assertNull("by default the perClause should be null", ad.getPerClause());
		PerCflow pcf = ajast.newPerCflow();
		ad.setPerClause(pcf);
		assertEquals("should have set the perClause to be a PerCflow", pcf, ad.getPerClause());
	}

	/**
	 * AsepctDeclarations's have a isPrivileged property - test the getting and setting of this property
	 */
	public void testSetPrivilegedInAspectDeclaration() {
		AjAST ajast = createAjAST();
		AspectDeclaration ad = ajast.newAspectDeclaration();
		assertFalse("by default the aspect should not be privileged", ad.isPrivileged());
		ad.setPrivileged(true);
		assertTrue("the aspect should now privileged", ad.isPrivileged());
	}

	// -------------- AfterAdviceDeclaration tests ---------------

	public void testNewAfterAdviceDeclaration() {
		AjAST ajast = createAjAST();
		AfterAdviceDeclaration ad = ajast.newAfterAdviceDeclaration();
		assertNotNull("a new AfterAdviceDeclaration should have been created", ad);
	}

	// -------------- BeforeAdviceDeclaration tests ---------------

	public void testNewBeforeAdviceDeclaration() {
		AjAST ajast = createAjAST();
		BeforeAdviceDeclaration bd = ajast.newBeforeAdviceDeclaration();
		assertNotNull("a new BeforeAdviceDeclaration should have been created", bd);
	}

	/**
	 * AdviceDeclarations's have a pointcut property - test the getting and setting of this property
	 */
	public void testGetAndSetPointcutInAdviceDeclaration() {
		AjAST ajast = createAjAST();
		BeforeAdviceDeclaration bd = ajast.newBeforeAdviceDeclaration();
		assertNull("by default there should be no pointcut associated with" + " the advice", bd.getPointcut());
		AndPointcut p = ajast.newAndPointcut();
		bd.setPointcut(p);
		assertEquals("there should now be an AndPointcut associated with" + " the advice", p, bd.getPointcut());
	}

	/**
	 * AdviceDeclarations's have a body property - test the getting and setting of this property
	 */
	public void testGetAndSetBodyInAdviceDeclaration() {
		AjAST ajast = createAjAST();
		BeforeAdviceDeclaration bd = ajast.newBeforeAdviceDeclaration();
		assertNull("by default there should be no body associated with" + " the advice", bd.getBody());
		Block b = ajast.newBlock();
		bd.setBody(b);
		assertEquals("there should now be a body associated with" + " the advice", b, bd.getBody());
	}

	// -------------- AfterReturningAdviceDeclaration tests ---------------

	public void testNewAfterReturningAdviceDeclaration() {
		AjAST ajast = createAjAST();
		AfterReturningAdviceDeclaration d = ajast.newAfterReturningAdviceDeclaration();
		assertNotNull("should have created an AfterReturningAdviceDeclaration", d);
	}

	/**
	 * AfterReturningAdviceDeclarations's have a returning property - test the getting and setting of this property
	 */
	public void testGetAndSetReturning() {
		AjAST ajast = createAjAST();
		AfterReturningAdviceDeclaration d = ajast.newAfterReturningAdviceDeclaration();
		assertNull("by default there should be no returning property associated with" + " the AfterReturningAdviceDeclaration", d
				.getReturning());
		SingleVariableDeclaration s = ajast.newSingleVariableDeclaration();
		d.setReturning(s);
		assertEquals("there should now be a returning property associated with" + " the AfterReturningAdviceDeclaration", s, d
				.getReturning());
	}

	// -------------- AfterThrowingAdviceDeclaration tests ---------------

	public void testNewAfterThrowingAdviceDeclaration() {
		AjAST ajast = createAjAST();
		AfterThrowingAdviceDeclaration d = ajast.newAfterThrowingAdviceDeclaration();
		assertNotNull("should have created an AfterThrowingAdviceDeclaration", d);
	}

	/**
	 * AfterThrowingAdviceDeclaration's have a throwing property - test the getting and setting of this property
	 */
	public void testGetAndSetThrowing() {
		AjAST ajast = createAjAST();
		AfterThrowingAdviceDeclaration d = ajast.newAfterThrowingAdviceDeclaration();
		assertNull("by default there should be no throwing property associated with" + " the AfterThrowingAdviceDeclaration", d
				.getThrowing());
		SingleVariableDeclaration s = ajast.newSingleVariableDeclaration();
		d.setThrowing(s);
		assertEquals("there should now be a throwing property associated with" + " the AfterThrowingAdviceDeclaration", s, d
				.getThrowing());
	}

	// -------------- AroundAdviceDeclaration tests ---------------

	public void testNewAroundAdviceDeclaration() {
		AjAST ajast = createAjAST();
		AroundAdviceDeclaration d = ajast.newAroundAdviceDeclaration();
		assertNotNull("should have created an AroundAdviceDeclaration", d);
	}

	/**
	 * AroundAdviceDeclaration's have a return type property - test the getting and setting of this property (different
	 * implementation for JLS2 and JLS3)
	 */
	public void testGetAndSetReturnTypeJLS2() {
		AjAST ajast = createAjAST(AST.JLS2);
		AroundAdviceDeclaration d = ajast.newAroundAdviceDeclaration();
		Type t = d.getReturnType();
		assertTrue("by default the return type associated with the" + " AroundAdviceDeclaration should be a PrimitiveType",
				t instanceof PrimitiveType);
		assertEquals("by default there should be the PrimitiveType.VOID return "
				+ "type associated with the AroundAdviceDeclaration", PrimitiveType.VOID.toString(), ((PrimitiveType) t).toString());
		SimpleType s = ajast.newSimpleType(ajast.newSimpleName("name"));
		d.setReturnType(s);
		assertEquals("there should now be a SimpleType return type associated with" + " the AroundAdviceDeclaration", s, d
				.getReturnType());
	}

	/**
	 * AroundAdviceDeclaration's have a return type property - test the getting and setting of this property (different
	 * implementation for JLS2 and JLS3)
	 */
	public void testGetAndSetReturnTypeJLS3() {
		AjAST ajast = createAjAST();
		AroundAdviceDeclaration d = ajast.newAroundAdviceDeclaration();
		Type t = d.getReturnType2();
		assertTrue("by default the return type associated with the" + " AroundAdviceDeclaration should be a PrimitiveType",
				t instanceof PrimitiveType);
		assertEquals("by default there should be the PrimitiveType.VOID return "
				+ "type associated with the AroundAdviceDeclaration", PrimitiveType.VOID.toString(), ((PrimitiveType) t).toString());
		SimpleType s = ajast.newSimpleType(ajast.newSimpleName("name"));
		d.setReturnType2(s);
		assertEquals("there should now be a SimpleType return type associated with" + " the AroundAdviceDeclaration", s, d
				.getReturnType2());
	}

	// -------------- InterTypeFieldDeclaration tests ---------------

	public void testNewITDFieldDeclaration() {
		AjAST ajast = createAjAST();
		InterTypeFieldDeclaration d = ajast.newInterTypeFieldDeclaration();
		assertNotNull("should have created an InterTypeFieldDeclaration", d);
	}

	// -------------- InterTypeMethodDeclaration tests ---------------

	public void testNewITDMethodDeclaration() {
		AjAST ajast = createAjAST();
		InterTypeMethodDeclaration d = ajast.newInterTypeMethodDeclaration();
		assertNotNull("should have created an InterTypeMethodDeclaration", d);
	}

	// -------------- AjTypeDeclaration tests ---------------

	public void testNewAjTypeDeclaration() {
		AjAST ajast = createAjAST();
		AjTypeDeclaration d = ajast.newAjTypeDeclaration();
		assertNotNull("should have created an AjTypeDeclaration", d);
	}

	/**
	 * AjTypeDeclaration's have an isAspect property - test the getting and setting of this property
	 */
	public void testGetAndSetIsAspect() {
		AjAST ajast = createAjAST();
		AjTypeDeclaration d = ajast.newAjTypeDeclaration();
		assertFalse("by default an AjTypeDeclaration should be a class", d.isAspect());
		d.setAspect(true);
		assertTrue("AjTypeDeclaration should now be an aspect", d.isAspect());
		d.setAspect(false);
		assertFalse("AjTypeDeclaration should now be a class", d.isAspect());
	}

	public void testPropertyDescriptorsForAjTypeDeclaration() {
		AjAST ajast = createAjAST();
		// AjTypeDeclaration d =
		ajast.newAjTypeDeclaration();
		List props = AjTypeDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundAspect = false;
		for (Object o : props) {
			if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("aspect")) {
					foundAspect = true;
				}
			}
		}
		assertTrue("AjTypeDeclaration should have an aspect PropertyDescriptor", foundAspect);
	}

	public void testCloneAjTypeDeclaration() {
		AjAST ajast = createAjAST();
		AjTypeDeclaration d = ajast.newAjTypeDeclaration();
		d.setAspect(true);
		AjTypeDeclaration copy = (AjTypeDeclaration) ASTNode.copySubtree(ajast, d);
		assertTrue("the AjTypeDeclaration clone should be an aspect", copy.isAspect());
	}

	// test for bug 125809 - make sure the property descriptors
	// associated with the AspectDeclaration aren't consequently
	// associated with the AjTypeDeclaration
	public void testPropertyDescriptorsForAjTypeDeclaration2() {
		AjAST ajast = createAjAST();
		// AspectDeclaration ad =
		ajast.newAspectDeclaration();
		// List aspectProps = AspectDeclaration.propertyDescriptors(AST.JLS3);
		// AjTypeDeclaration d =
		ajast.newAjTypeDeclaration();
		List props = AjTypeDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundPrivileged = false;
		boolean foundPerClause = false;
		// boolean foundAspect = false;
		for (Object o : props) {
			if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("privileged")) {
					foundPrivileged = true;
				}
			} else if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				if (element.getId().equals("perClause")) {
					foundPerClause = true;
				}
			}
		}
		assertFalse("AjTypeDeclaration should not have a privileged PropertyDescriptor", foundPrivileged);
		assertFalse("AjTypeDeclaration should not have a perClause PropertyDescriptor", foundPerClause);
	}

	// test for bug 125809 - make sure the property descriptors
	// associated with the AjTypeDeclaration aren't consequently
	// associated with the TypeDeclaration
	public void testPropertyDescriptorsForAjTypeDeclaration3() {
		AjAST ajast = createAjAST();
		// AjTypeDeclaration d =
		ajast.newAjTypeDeclaration();
		// List ajProps =
		AjTypeDeclaration.propertyDescriptors(AST.JLS3);
		// TypeDeclaration td =
		ajast.newTypeDeclaration();
		List props = TypeDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundAspect = false;
		for (Object o : props) {
			if (o instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor element = (SimplePropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("aspect")) {
					foundAspect = true;
				}
			}
		}
		assertFalse("TypeDeclaration should not have an aspect PropertyDescriptor", foundAspect);
	}

	// -------------- DeclareAtFieldDeclaration tests ---------------

	public void testNewDeclareAtFieldDeclaration() {
		AjAST ajast = createAjAST();
		DeclareAtFieldDeclaration d = ajast.newDeclareAtFieldDeclaration();
		assertNotNull("should have created a DeclareAtFieldDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareAtField() {
		AjAST ajast = createAjAST();
		// DeclareAtFieldDeclaration d =
		ajast.newDeclareAtFieldDeclaration();
		List props = DeclareAtFieldDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPattern = false;
		boolean foundAnnotationName = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pattern")) {
				foundPattern = true;
			} else if (id.equals("annotationName")) {
				foundAnnotationName = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareAtFieldDeclaration: " + element.getId());
			}
		}
		assertTrue("DeclareAtFieldDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareAtFieldDeclaration should have a attern PropertyDescriptor", foundPattern);
		assertTrue("DeclareAtFieldDeclaration should have a annotationName PropertyDescriptor", foundAnnotationName);
	}

	public void testGetAndSetPatternNodeInDeclareAtField() {
		AjAST ajast = createAjAST();
		DeclareAtFieldDeclaration d = ajast.newDeclareAtFieldDeclaration();
		assertNull("by default there should be no typePattern associated with" + " the declare @field annotation", d
				.getPatternNode());
		SignaturePattern p = ajast.newSignaturePattern();
		d.setPatternNode(p);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare @field annotation", p, d
				.getPatternNode());
	}

	public void testGetAndSetAnnNameInDeclareAtField() {
		AjAST ajast = createAjAST();
		DeclareAtFieldDeclaration d = ajast.newDeclareAtFieldDeclaration();
		assertEquals("the default annotation name should be MISSING", "MISSING", d.getAnnotationName().getFullyQualifiedName());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		assertEquals("the annotation name should now be set to 'MyAnnotation'", "MyAnnotation", d.getAnnotationName()
				.getFullyQualifiedName());
	}

	public void testCloneDeclareAtField() {
		AjAST ajast = createAjAST();
		DeclareAtFieldDeclaration d = ajast.newDeclareAtFieldDeclaration();
		d.setPatternNode(ajast.newSignaturePattern());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		DeclareAtFieldDeclaration copy = (DeclareAtFieldDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareAtFieldDeclaration clone should have a TypePattern set", copy.getPatternNode());
		assertNotNull("the DeclareAtFieldDeclaration clone should have a Annotation name set", copy.getAnnotationName());
	}

	public void testInternalDeclareAtField() {
		AjAST ajast = createAjAST();
		DeclareAtFieldDeclaration d = ajast.newDeclareAtFieldDeclaration();
		List props = DeclareAtFieldDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			if (element.getId().equals("annotationName")) {
				assertNotNull("DeclareAtFieldDeclaration's " + element.getId() + " property"
						+ " should not be null since it is lazily created", d.getStructuralProperty(element));
			} else {
				assertNull("DeclareAtFieldDeclaration's " + element.getId() + " property"
						+ " should be null since we haven't set it yet", d.getStructuralProperty(element));
			}
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pattern")) {
				SignaturePattern p = ajast.newSignaturePattern();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareAtFieldDeclaration's pattern property should" + " now be a SignaturePattern", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("annotationName")) {
				SimpleName s = ajast.newSimpleName("MyAnnotation");
				d.setStructuralProperty(element, s);
				assertEquals("DeclareAtFieldDeclaration's annotationName property should" + " now be a SimpleName", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareAtFieldDeclaration");
			}
		}
	}

	// -------------- DeclareAtMethodDeclaration tests ---------------

	public void testNewDeclareAtMethodDeclaration() {
		AjAST ajast = createAjAST();
		DeclareAtMethodDeclaration d = ajast.newDeclareAtMethodDeclaration();
		assertNotNull("should have created a DeclareAtMethodDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareAtMethod() {
		AjAST ajast = createAjAST();
		// DeclareAtMethodDeclaration d =
		ajast.newDeclareAtMethodDeclaration();
		List props = DeclareAtMethodDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPattern = false;
		boolean foundAnnotationName = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pattern")) {
				foundPattern = true;
			} else if (id.equals("annotationName")) {
				foundAnnotationName = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareAtMethodDeclaration: " + element.getId());
			}
		}
		assertTrue("DeclareAtMethodDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareAtMethodDeclaration should have a attern PropertyDescriptor", foundPattern);
		assertTrue("DeclareAtMethodDeclaration should have a annotationName PropertyDescriptor", foundAnnotationName);
	}

	public void testGetAndSetPatternNodeInDeclareAtMethod() {
		AjAST ajast = createAjAST();
		DeclareAtMethodDeclaration d = ajast.newDeclareAtMethodDeclaration();
		assertNull("by default there should be no typePattern associated with" + " the declare @method annotation", d
				.getPatternNode());
		SignaturePattern p = ajast.newSignaturePattern();
		d.setPatternNode(p);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare @method annotation", p, d
				.getPatternNode());
	}

	public void testGetAndSetAnnNameInDeclareAtMethod() {
		AjAST ajast = createAjAST();
		DeclareAtMethodDeclaration d = ajast.newDeclareAtMethodDeclaration();
		assertEquals("the default annotation name should be MISSING", "MISSING", d.getAnnotationName().getFullyQualifiedName());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		assertEquals("the annotation name should now be set to 'MyAnnotation'", "MyAnnotation", d.getAnnotationName()
				.getFullyQualifiedName());
	}

	public void testCloneDeclareAtMethod() {
		AjAST ajast = createAjAST();
		DeclareAtMethodDeclaration d = ajast.newDeclareAtMethodDeclaration();
		d.setPatternNode(ajast.newSignaturePattern());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		DeclareAtMethodDeclaration copy = (DeclareAtMethodDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareAtMethodDeclaration clone should have a TypePattern set", copy.getPatternNode());
		assertNotNull("the DeclareAtMethodDeclaration clone should have a Annotation name set", copy.getAnnotationName());
	}

	public void testInternalDeclareAtMethod() {
		AjAST ajast = createAjAST();
		DeclareAtMethodDeclaration d = ajast.newDeclareAtMethodDeclaration();
		List props = DeclareAtMethodDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			if (element.getId().equals("annotationName")) {
				assertNotNull("DeclareAtMethodDeclaration's " + element.getId() + " property"
						+ " should not be null since it is lazily created", d.getStructuralProperty(element));
			} else {
				assertNull("DeclareAtMethodDeclaration's " + element.getId() + " property"
						+ " should be null since we haven't set it yet", d.getStructuralProperty(element));
			}
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pattern")) {
				SignaturePattern p = ajast.newSignaturePattern();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareAtMethodDeclaration's pattern property should" + " now be a SignaturePattern", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("annotationName")) {
				SimpleName s = ajast.newSimpleName("MyAnnotation");
				d.setStructuralProperty(element, s);
				assertEquals("DeclareAtMethodDeclaration's annotationName property should" + " now be a SimpleName", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareAtMethodDeclaration");
			}
		}
	}

	// -------------- DeclareAtConstructorDeclaration tests ---------------

	public void testNewDeclareAtConstructorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareAtConstructorDeclaration d = ajast.newDeclareAtConstructorDeclaration();
		assertNotNull("should have created a DeclareAtConstructorDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareAtConstructor() {
		AjAST ajast = createAjAST();
		// DeclareAtConstructorDeclaration d =
		ajast.newDeclareAtConstructorDeclaration();
		List props = DeclareAtConstructorDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPattern = false;
		boolean foundAnnotationName = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pattern")) {
				foundPattern = true;
			} else if (id.equals("annotationName")) {
				foundAnnotationName = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareAtConstructorDeclaration: " + element.getId());
			}
		}
		assertTrue("DeclareAtConstructorDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareAtConstructorDeclaration should have a attern PropertyDescriptor", foundPattern);
		assertTrue("DeclareAtConstructorDeclaration should have a annotationName PropertyDescriptor", foundAnnotationName);
	}

	public void testGetAndSetPatternNodeInDeclareAtConstructor() {
		AjAST ajast = createAjAST();
		DeclareAtConstructorDeclaration d = ajast.newDeclareAtConstructorDeclaration();
		assertNull("by default there should be no typePattern associated with" + " the declare @constructor annotation", d
				.getPatternNode());
		SignaturePattern p = ajast.newSignaturePattern();
		d.setPatternNode(p);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare @constructor annotation", p, d
				.getPatternNode());
	}

	public void testGetAndSetAnnNameInDeclareAtConstructor() {
		AjAST ajast = createAjAST();
		DeclareAtConstructorDeclaration d = ajast.newDeclareAtConstructorDeclaration();
		assertEquals("the default annotation name should be MISSING", "MISSING", d.getAnnotationName().getFullyQualifiedName());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		assertEquals("the annotation name should now be set to 'MyAnnotation'", "MyAnnotation", d.getAnnotationName()
				.getFullyQualifiedName());
	}

	public void testCloneDeclareAtConstructor() {
		AjAST ajast = createAjAST();
		DeclareAtConstructorDeclaration d = ajast.newDeclareAtConstructorDeclaration();
		d.setPatternNode(ajast.newSignaturePattern());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		DeclareAtConstructorDeclaration copy = (DeclareAtConstructorDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareAtConstructorDeclaration clone should have a SignaturePattern set", copy.getPatternNode());
		assertNotNull("the DeclareAtConstructorDeclaration clone should have a Annotation name set", copy.getAnnotationName());
	}

	public void testInternalDeclareAtConstructor() {
		AjAST ajast = createAjAST();
		DeclareAtConstructorDeclaration d = ajast.newDeclareAtConstructorDeclaration();
		List props = DeclareAtConstructorDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			if (element.getId().equals("annotationName")) {
				assertNotNull("DeclareAtConstructorDeclaration's " + element.getId() + " property"
						+ " should not be null since it is lazily created", d.getStructuralProperty(element));
			} else {
				assertNull("DeclareAtConstructorDeclaration's " + element.getId() + " property"
						+ " should be null since we haven't set it yet", d.getStructuralProperty(element));
			}
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pattern")) {
				SignaturePattern p = ajast.newSignaturePattern();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareAtConstructorDeclaration's pattern property should" + " now be a SignaturePattern", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("annotationName")) {
				SimpleName s = ajast.newSimpleName("MyAnnotation");
				d.setStructuralProperty(element, s);
				assertEquals("DeclareAtConstructorDeclaration's annotationName property should" + " now be a SimpleName", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareAtConstructorDeclaration");
			}
		}
	}

	// -------------- DeclareAtTypeDeclaration tests ---------------

	public void testNewDeclareAtTypeDeclaration() {
		AjAST ajast = createAjAST();
		DeclareAtTypeDeclaration d = ajast.newDeclareAtTypeDeclaration();
		assertNotNull("should have created a DeclareAtTypeDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareAtType() {
		AjAST ajast = createAjAST();
		// DeclareAtTypeDeclaration d =
		ajast.newDeclareAtTypeDeclaration();
		List props = DeclareAtTypeDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPattern = false;
		boolean foundAnnotationName = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pattern")) {
				foundPattern = true;
			} else if (id.equals("annotationName")) {
				foundAnnotationName = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareAtTypeDeclaration: " + element.getId());
			}
		}
		assertTrue("DeclareAtTypeDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareAtTypeDeclaration should have a attern PropertyDescriptor", foundPattern);
		assertTrue("DeclareAtTypeDeclaration should have a annotationName PropertyDescriptor", foundAnnotationName);
	}

	public void testGetAndSetPatternNodeInDeclareAtType() {
		AjAST ajast = createAjAST();
		DeclareAtTypeDeclaration d = ajast.newDeclareAtTypeDeclaration();
		assertNull("by default there should be no typePattern associated with" + " the declare @type annotation", d
				.getPatternNode());
		DefaultTypePattern dtp = ajast.newDefaultTypePattern();
		d.setPatternNode(dtp);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare @type annotation", dtp, d
				.getPatternNode());
	}

	public void testGetAndSetAnnNameInDeclareAtType() {
		AjAST ajast = createAjAST();
		DeclareAtTypeDeclaration d = ajast.newDeclareAtTypeDeclaration();
		assertEquals("the default annotation name should be MISSING", "MISSING", d.getAnnotationName().getFullyQualifiedName());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		assertEquals("the annotation name should now be set to 'MyAnnotation'", "MyAnnotation", d.getAnnotationName()
				.getFullyQualifiedName());
	}

	public void testCloneDeclareAtType() {
		AjAST ajast = createAjAST();
		DeclareAtTypeDeclaration d = ajast.newDeclareAtTypeDeclaration();
		d.setPatternNode(ajast.newDefaultTypePattern());
		d.setAnnotationName(ajast.newSimpleName("MyAnnotation"));
		DeclareAtTypeDeclaration copy = (DeclareAtTypeDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareAtTypeDeclaration clone should have a TypePattern set", copy.getPatternNode());
		assertNotNull("the DeclareAtTypeDeclaration clone should have a Annotation name set", copy.getAnnotationName());
	}

	public void testInternalDeclareAtType() {
		AjAST ajast = createAjAST();
		DeclareAtTypeDeclaration d = ajast.newDeclareAtTypeDeclaration();
		List props = DeclareAtTypeDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			if (element.getId().equals("annotationName")) {
				assertNotNull("DeclareAtTypeDeclaration's " + element.getId() + " property"
						+ " should not be null since it is lazily created", d.getStructuralProperty(element));
			} else {
				assertNull("DeclareAtTypeDeclaration's " + element.getId() + " property"
						+ " should be null since we haven't set it yet", d.getStructuralProperty(element));
			}
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pattern")) {
				DefaultTypePattern p = ajast.newDefaultTypePattern();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareAtTypeDeclaration's pattern property should" + " now be a DefaultTypePattern", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("annotationName")) {
				SimpleName s = ajast.newSimpleName("MyAnnotation");
				d.setStructuralProperty(element, s);
				assertEquals("DeclareAtTypeDeclaration's annotationName property should" + " now be a SimpleName", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareAtTypeDeclaration");
			}
		}
	}

	// -------------- DeclareErrorDeclaration tests ---------------

	public void testNewDeclareErrorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareErrorDeclaration d = ajast.newDeclareErrorDeclaration();
		assertNotNull("should have created a DeclareErrorDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareErrorDeclaration() {
		AjAST ajast = createAjAST();
		// DeclareErrorDeclaration d =
		ajast.newDeclareErrorDeclaration();
		List props = DeclareErrorDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPointcut = false;
		boolean foundMessage = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pointcut")) {
				foundPointcut = true;
			} else if (id.equals("message")) {
				foundMessage = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareErrorDeclaration");
			}
		}
		assertTrue("DeclareErrorDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareErrorDeclaration should have a pointcut PropertyDescriptor", foundPointcut);
		assertTrue("DeclareErrorDeclaration should have a message PropertyDescriptor", foundMessage);
	}

	public void testGetAndSetPointcutInErrorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareErrorDeclaration d = ajast.newDeclareErrorDeclaration();
		assertNull("by default there should be no pointcut associated with" + " the declare error", d.getPointcut());
		AndPointcut p = ajast.newAndPointcut();
		d.setPointcut(p);
		assertEquals("there should now be an AndPointcut associated with" + " the declare error", p, d.getPointcut());
	}

	public void testGetAndSetMessageInErrorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareErrorDeclaration d = ajast.newDeclareErrorDeclaration();
		assertNull("by default there should be no message associated with" + " the declare error", d.getMessage());
		StringLiteral s = ajast.newStringLiteral();
		d.setMessage(s);
		assertEquals("there should now be a StringLiteral message associated with" + " the declare error", s, d.getMessage());
	}

	public void testCloneDeclareErrorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareErrorDeclaration d = ajast.newDeclareErrorDeclaration();
		d.setPointcut(ajast.newAndPointcut());
		d.setMessage(ajast.newStringLiteral());
		DeclareErrorDeclaration copy = (DeclareErrorDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareErrorDeclaration clone should have a pointcut set", copy.getPointcut());
		assertNotNull("the DeclareErrorDeclaration clone should have a message set", copy.getMessage());
	}

	public void testInternalDeclareErrorDeclaration() {
		AjAST ajast = createAjAST();
		DeclareErrorDeclaration d = ajast.newDeclareErrorDeclaration();
		List props = DeclareErrorDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			assertNull("DeclareErrorDeclaration's " + element.getId() + " property" + "should be null since we haven't set it yet",
					d.getStructuralProperty(element));
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pointcut")) {
				AndPointcut p = ajast.newAndPointcut();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareErrorDeclaration's pointcut property should" + " now be an AndPointcut", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("message")) {
				StringLiteral s = ajast.newStringLiteral();
				d.setStructuralProperty(element, s);
				assertEquals("DeclareErrorDeclaration's message property should" + " now be an AndPointcut", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareErrorDeclaration");
			}
		}
	}

	// -------------- DeclareParentsDeclaration tests ---------------

	public void testNewDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		assertNotNull("should have created a DeclareParentsDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		// DeclareParentsDeclaration d =
		ajast.newDeclareParentsDeclaration();
		List props = DeclareParentsDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundTypePattern = false;
		boolean foundIsExtends = false;
		boolean foundTypePatternList = false;
		for (Object o : props) {
			if ((o instanceof ChildPropertyDescriptor)) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				String id = element.getId();
				if (id.equals("javadoc")) {
					foundJavadoc = true;
				} else if (id.equals("childTypePattern")) {
					foundTypePattern = true;
				} else {
					fail("unknown PropertyDescriptor associated with DeclareParentsDeclaration: " + element.getId());
				}
			} else if ((o instanceof ChildListPropertyDescriptor)
					&& ((ChildListPropertyDescriptor) o).getId().equals("typePatternsList")) {
				foundTypePatternList = true;
			} else if ((o instanceof SimplePropertyDescriptor) && ((SimplePropertyDescriptor) o).getId().equals("isExtends")) {
				foundIsExtends = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareErrorDeclaration: " + o);
			}
		}
		assertTrue("DeclareParentsDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareParentsDeclaration should have a typePattern PropertyDescriptor", foundTypePattern);
		assertTrue("DeclareParentsDeclaration should have an isExtends PropertyDescriptor", foundIsExtends);
		assertTrue("DeclareParentsDeclaration should have a typePatternList PropertyDescriptor", foundTypePatternList);
	}

	public void testGetAndSetTypePatternInDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		assertNull("by default there should be no TypePattern associated with" + " the declare parents", d.getChildTypePattern());
		DefaultTypePattern dtp = ajast.newDefaultTypePattern();
		d.setChildTypePattern(dtp);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare parents", dtp, d
				.getChildTypePattern());
	}

	public void testGetAndSetIsExtendsInDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		assertFalse("by default the declare parents should not be 'extends'", d.isExtends());
		d.setExtends(true);
		assertTrue("the declare parents should now be 'extends'", d.isExtends());
	}

	public void testTypePatternsInDeclareParents() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		List l = d.parentTypePatterns();
		assertEquals("there shouldn't be any type patterns associated with" + "the declare parents yet", 0, l.size());
		DefaultTypePattern dtp = ajast.newDefaultTypePattern();
		l.add(dtp);
		assertEquals("there should be one type patterns associated with" + "the declare parents", 1, l.size());
		assertEquals("there should be a DefaultTypePattern associated with" + "the declare parents", dtp, l.get(0));

	}

	public void testCloneDeclareParentsDeclaration() {
		AjAST ajast = createAjAST();
		DeclareParentsDeclaration d = ajast.newDeclareParentsDeclaration();
		d.setChildTypePattern(ajast.newDefaultTypePattern());
		d.setExtends(true);
		d.parentTypePatterns().add(ajast.newDefaultTypePattern());
		DeclareParentsDeclaration copy = (DeclareParentsDeclaration) ASTNode.copySubtree(ajast, d);
		assertEquals("there should be one type patterns associated with" + "the declare parents copy", 1, copy.parentTypePatterns()
				.size());
		assertNotNull("the DeclareParentsDeclaration clone should have a typePattern set", copy.getChildTypePattern());
		assertTrue("the DeclareParentsDeclaration clone should be an 'extends'", copy.isExtends());
	}

	// -------------- DeclarePrecedenceDeclaration tests ---------------

	public void testNewDeclarePrecedenceDeclaration() {
		AjAST ajast = createAjAST();
		DeclarePrecedenceDeclaration d = ajast.newDeclarePrecedenceDeclaration();
		assertNotNull("should have created a DeclarePrecedenceDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclarePrecedence() {
		AjAST ajast = createAjAST();
		// DeclarePrecedenceDeclaration d =
		ajast.newDeclarePrecedenceDeclaration();
		List props = DeclarePrecedenceDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundTypePatterns = false;
		for (Object o : props) {
			if ((o instanceof ChildPropertyDescriptor) && ((ChildPropertyDescriptor) o).getId().equals("javadoc")) {
				foundJavadoc = true;
			} else if ((o instanceof ChildListPropertyDescriptor)
					&& ((ChildListPropertyDescriptor) o).getId().equals("parentTypePatterns")) {
				foundTypePatterns = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareErrorDeclaration: " + o);
			}
		}
		assertTrue("DeclareErrorDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareErrorDeclaration should have a pointcut PropertyDescriptor", foundTypePatterns);
	}

	public void testTypePatternsInDeclarePrecedence() {
		AjAST ajast = createAjAST();
		DeclarePrecedenceDeclaration d = ajast.newDeclarePrecedenceDeclaration();
		List l = d.typePatterns();
		assertEquals("there shouldn't be any type patterns associated with" + "the declare precedence yet", 0, l.size());
		DefaultTypePattern dtp = ajast.newDefaultTypePattern();
		l.add(dtp);
		assertEquals("there should be one type patterns associated with" + "the declare precedence", 1, l.size());
		assertEquals("there should be a DefaultTypePattern associated with" + "the declare precedence", dtp, l.get(0));

	}

	public void testCloneDeclarePrecedenceDeclaration() {
		AjAST ajast = createAjAST();
		DeclarePrecedenceDeclaration d = ajast.newDeclarePrecedenceDeclaration();
		d.typePatterns().add(ajast.newDefaultTypePattern());
		DeclarePrecedenceDeclaration copy = (DeclarePrecedenceDeclaration) ASTNode.copySubtree(ajast, d);
		assertEquals("there should be one type patterns associated with" + "the declare precedence copy", 1, copy.typePatterns()
				.size());
	}

	public void testInternalDeclarePrecedenceDeclaration() {
		AjAST ajast = createAjAST();
		DeclarePrecedenceDeclaration d = ajast.newDeclarePrecedenceDeclaration();
		List props = DeclarePrecedenceDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			if (o instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
				assertNull("DeclareErrorDeclaration's " + element.getId() + " property"
						+ "should be null since we haven't set it yet", d.getStructuralProperty(element));
			} else if (o instanceof ChildListPropertyDescriptor) {
				ChildListPropertyDescriptor element = (ChildListPropertyDescriptor) o;
				assertNotNull("DeclareErrorDeclaration's " + element.getId() + " property"
						+ "should not be null since it is a list", d.getStructuralProperty(element));
				assertEquals("should only be able to put TypePattern's into the list", TypePattern.class, element.getElementType());
			} else {
				fail("unknown PropertyDescriptor associated with DeclareErrorDeclaration: " + o);
			}
		}
	}

	// -------------- DeclareSoftDeclaration tests ---------------

	public void testNewDeclareSoftDeclaration() {
		AjAST ajast = createAjAST();
		DeclareSoftDeclaration d = ajast.newDeclareSoftDeclaration();
		assertNotNull("should have created a DeclareSoftDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareSoftDeclaration() {
		AjAST ajast = createAjAST();
		// DeclareSoftDeclaration d =
		ajast.newDeclareSoftDeclaration();
		List props = DeclareSoftDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPointcut = false;
		boolean foundTypePattern = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pointcut")) {
				foundPointcut = true;
			} else if (id.equals("typePattern")) {
				foundTypePattern = true;
			} else {
				fail("unknown PropertyDescriptor associated with " + "DeclareSoftDeclaration: " + element.getId());
			}
		}
		assertTrue("DeclareSoftDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareSoftDeclaration should have a pointcut PropertyDescriptor", foundPointcut);
		assertTrue("DeclareSoftDeclaration should have a type PropertyDescriptor", foundTypePattern);
	}

	public void testGetAndSetPointcutInSoftDeclaration() {
		AjAST ajast = createAjAST();
		DeclareSoftDeclaration d = ajast.newDeclareSoftDeclaration();
		assertNull("by default there should be no pointcut associated with" + " the declare soft", d.getPointcut());
		AndPointcut p = ajast.newAndPointcut();
		d.setPointcut(p);
		assertEquals("there should now be an AndPointcut associated with" + " the declare soft", p, d.getPointcut());
	}

	public void testGetAndSetTypePatternInSoftDeclaration() {
		AjAST ajast = createAjAST();
		DeclareSoftDeclaration d = ajast.newDeclareSoftDeclaration();
		assertNull("by default there should be no TypePattern associated with" + " the declare soft", d.getTypePattern());
		DefaultTypePattern dtp = ajast.newDefaultTypePattern();
		d.setTypePattern(dtp);
		assertEquals("there should now be a DefaultTypePattern associated with" + " the declare soft", dtp, d.getTypePattern());
	}

	public void testCloneDeclareSoftDeclaration() {
		AjAST ajast = createAjAST();
		DeclareSoftDeclaration d = ajast.newDeclareSoftDeclaration();
		d.setPointcut(ajast.newAndPointcut());
		d.setTypePattern(ajast.newDefaultTypePattern());
		DeclareSoftDeclaration copy = (DeclareSoftDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareSoftDeclaration clone should have a pointcut set", copy.getPointcut());
		assertNotNull("the DeclareSoftDeclaration clone should have a typePattern set", copy.getTypePattern());
	}

	public void testInternalDeclareSoftDeclaration() {
		AjAST ajast = createAjAST();
		DeclareSoftDeclaration d = ajast.newDeclareSoftDeclaration();
		List props = DeclareSoftDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			assertNull("DeclareSoftDeclaration's " + element.getId() + " property" + "should be null since we haven't set it yet",
					d.getStructuralProperty(element));
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pointcut")) {
				AndPointcut p = ajast.newAndPointcut();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareSoftDeclaration's pointcut property should" + " now be an AndPointcut", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("typePattern")) {
				DefaultTypePattern dtp = ajast.newDefaultTypePattern();
				d.setStructuralProperty(element, dtp);
				assertEquals("DeclareSoftDeclaration's typePattern property should" + " now be an DefaultTypePattern", dtp, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareSoftDeclaration: " + element.getId());
			}
		}
	}

	// -------------- DeclareWarningDeclaration tests ---------------

	public void testNewDeclareWarningDeclaration() {
		AjAST ajast = createAjAST();
		DeclareWarningDeclaration d = ajast.newDeclareWarningDeclaration();
		assertNotNull("should have created a DeclareWarningDeclaration", d);
	}

	public void testPropertyDescriptorsForDeclareWarningDeclaration() {
		AjAST ajast = createAjAST();
		// DeclareWarningDeclaration d =
		ajast.newDeclareWarningDeclaration();
		List props = DeclareWarningDeclaration.propertyDescriptors(AST.JLS3);
		boolean foundJavadoc = false;
		boolean foundPointcut = false;
		boolean foundMessage = false;
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			String id = element.getId();
			if (id.equals("javadoc")) {
				foundJavadoc = true;
			} else if (id.equals("pointcut")) {
				foundPointcut = true;
			} else if (id.equals("message")) {
				foundMessage = true;
			} else {
				fail("unknown PropertyDescriptor associated with DeclareWarningDeclaration");
			}
		}
		assertTrue("DeclareWarningDeclaration should have a javadoc PropertyDescriptor", foundJavadoc);
		assertTrue("DeclareWarningDeclaration should have a pointcut PropertyDescriptor", foundPointcut);
		assertTrue("DeclareWarningDeclaration should have a message PropertyDescriptor", foundMessage);
	}

	public void testGetAndSetPointcutInWarningDeclaration() {
		AjAST ajast = createAjAST();
		DeclareWarningDeclaration d = ajast.newDeclareWarningDeclaration();
		assertNull("by default there should be no pointcut associated with" + " the declare warning", d.getPointcut());
		AndPointcut p = ajast.newAndPointcut();
		d.setPointcut(p);
		assertEquals("there should now be an AndPointcut associated with" + " the declare warning", p, d.getPointcut());
	}

	public void testGetAndSetMessageInWarningDeclaration() {
		AjAST ajast = createAjAST();
		DeclareWarningDeclaration d = ajast.newDeclareWarningDeclaration();
		assertNull("by default there should be no message associated with" + " the declare warning", d.getMessage());
		StringLiteral s = ajast.newStringLiteral();
		d.setMessage(s);
		assertEquals("there should now be a StringLiteral message associated with" + " the declare warning", s, d.getMessage());
	}

	public void testCloneDeclareWarningDeclaration() {
		AjAST ajast = createAjAST();
		DeclareWarningDeclaration d = ajast.newDeclareWarningDeclaration();
		d.setPointcut(ajast.newAndPointcut());
		d.setMessage(ajast.newStringLiteral());
		DeclareWarningDeclaration copy = (DeclareWarningDeclaration) ASTNode.copySubtree(ajast, d);
		assertNotNull("the DeclareWarningDeclaration clone should have a pointcut set", copy.getPointcut());
		assertNotNull("the DeclareWarningDeclaration clone should have a message set", copy.getMessage());
	}

	public void testInternalDeclareWarningDeclaration() {
		AjAST ajast = createAjAST();
		DeclareWarningDeclaration d = ajast.newDeclareWarningDeclaration();
		List props = DeclareWarningDeclaration.propertyDescriptors(AST.JLS3);
		for (Object o : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) o;
			assertNull("DeclareWarningDeclaration's " + element.getId() + " property"
					+ "should be null since we haven't set it yet", d.getStructuralProperty(element));
		}
		for (Object prop : props) {
			ChildPropertyDescriptor element = (ChildPropertyDescriptor) prop;
			if (element.getId().equals("pointcut")) {
				AndPointcut p = ajast.newAndPointcut();
				d.setStructuralProperty(element, p);
				assertEquals("DeclareWarningDeclaration's pointcut property should" + " now be an AndPointcut", p, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("message")) {
				StringLiteral s = ajast.newStringLiteral();
				d.setStructuralProperty(element, s);
				assertEquals("DeclareWarningDeclaration's message property should" + " now be an AndPointcut", s, d
						.getStructuralProperty(element));
			} else if (element.getId().equals("javadoc")) {
				// do nothing since makes no sense to have javadoc
			} else {
				fail("unknown property for DeclareWarningDeclaration");
			}
		}
	}

	// --------- testing that the source ranges have been set correctly ---------

	public void testDeclareAnnotationType() {
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @type: C : @MyAnnotation;}", 43, 33);
	}

	public void testDeclareAnnotationMethod() {
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @method:public * C.*(..) : @MyAnnotation;}", 43, 49);
	}

	public void testDeclareAnnotationField() {
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @field: * C+.* : @MyAnnotation;}", 43, 39);
	}

	public void testDeclareAnnotationConstructor() {
		checkJLS3("@interface MyAnnotation{}class C{}aspect A{declare @constructor: C+.new(..) : @MyAnnotation;}", 43, 49);
	}

	public void testDeclareParents() {
		checkJLS3("class A{}class B{}aspect C {declare parents : A extends B;}", 28, 29);
	}


	/*
	 *
	 *
	 * START: Test TypePattern nodes introduced in Bugzilla 329268.
	 *
	 *
	 */

	public void testDeclareParentsTypePatternNodeSource() {
		checkTypePatternSourceRangesJLS3("class A{}class B{}aspect C {declare parents : A extends B;}", new int[][] {{46, 1} , {56, 1 }});
	}

	public void testDeclareParentsAnySource() {
		checkTypePatternSourceRangesJLS3("class A{}class B{}aspect C {declare parents : * extends B;}", new int[][] {{46, 1} , {56, 1 }});
	}

	public void testDeclareParentsAndSource() {

		checkTypePatternSourceRangesJLS3(
				"class A{}class B{}class D{}class E{}aspect C {declare parents : A && B && D extends E;}",
				new int[][] { { 64, 11 },// A && B && D,
						{ 64, 1 }, // A
						{ 69, 6 }, // B && D
						{ 69, 1 }, // B
						{ 74, 1 }, // D
						{ 84, 1 } // E
				});
	}

	public void testDeclareParentsNotSource() {

		checkTypePatternSourceRangesJLS3(
				"class A{}class B{}class D{}class E{}aspect C {declare parents : A && !B extends E;}",
				new int[][] { { 64, 7 },// A && !B
						{ 64, 1 }, // A
						{ 70, 1 }, // !B: the source location for a negated pattern is the start of the negated pattern excluding "!". Is this a bug?
						{ 70, 1 }, // B
						{ 80, 1 } // E
				});
	}

	public void testDeclareParentsOrSource() {
		checkTypePatternSourceRangesJLS3(
				"class A{}class B{}class D{}class E{}aspect C {declare parents : A || B || D extends E;}",
				new int[][] { { 64, 11 },// A || B || D,
						{ 64, 1 }, // A
						{ 69, 6 }, // B || D
						{ 69, 1 }, // B
						{ 74, 1 }, // D
						{ 84, 1 } // E
				});
	}

	public void testDeclareParentsAnyWithAnnotationSource() {
		checkTypePatternSourceRangesJLS3(
				"@interface AnnotationT {}class E{}aspect C {declare parents : (@AnnotationT *) extends E;}",
				new int[][] { { 62, 16 },// (@AnnotationT *)
						{ 87, 1 } // E
				});

	}

	public void testDeclareParentsTypeCategorySource() {
		checkTypePatternSourceRangesJLS3(
				"class A{}class E{}aspect C {declare parents : A && is(ClassType) extends E;}",
				new int[][] { { 46, 18 },// A && !is(InnerType)
						{ 46, 1 }, // A
						{ 51, 13}, // is(InnerType)
						{ 73, 1 } // E
				});
	}

	public void testDeclareParentsTypeCategoryNotSource() {
		checkTypePatternSourceRangesJLS3(
				"class A{}class E{}aspect C {declare parents : A && !is(InnerType) extends E;}",
				new int[][] { { 46, 19 },// A && !is(InnerType)
						{ 46, 1 }, // A
						{ 52, 13}, // !is(InnerType): the source location for a negated pattern is the start of the negated pattern excluding "!". Is this a bug?
						{ 52, 13}, // is(InnerType)
						{ 74, 1 } // E
				});
	}

	// // TODO: commenting out as there isn't proper support for hasmethod(..)
	// yet. Uncomment and fix when hasmethod is supported
	// public void testDeclareParentsHasMember() {
	// // This is wrong. Call checkTypePatternJLS3 instead to check the source
	// ranges for hasMethod...
	// checkJLS3("class A{}class B{ public void fooB() {}}class D{}aspect C {declare parents : A && hasmethod(void foo*(..)) extends D;}",
	// 37, 34);
	// }

	public void testDeclareParentsTypeCategoryInner() {
		checkCategoryTypePatternJLS3(
				"class A{class B{}}class E{}aspect C {declare parents : B && is(InnerType) extends E;}",
				TypeCategoryTypePattern.INNER, "is(InnerType)");
	}

	public void testDeclareParentsTypeCategoryInterface() {
		checkCategoryTypePatternJLS3(
				"interface B{}interface E{}aspect C {declare parents : B && is(InterfaceType) extends E;}",
				TypeCategoryTypePattern.INTERFACE, "is(InterfaceType)");
	}

	public void testDeclareParentsTypeCategoryClass() {
		checkCategoryTypePatternJLS3(
				"class B{}class E{}aspect C {declare parents : B && is(ClassType) extends E;}",
				TypeCategoryTypePattern.CLASS, "is(ClassType)");
	}

	public void testDeclareParentsTypeCategoryAnnotation() {
		checkCategoryTypePatternJLS3(
				"@interface B{}class E{}aspect C {declare parents : B && is(AnnotationType) extends E;}",
				TypeCategoryTypePattern.ANNOTATION, "is(AnnotationType)");
	}

	public void testDeclareParentsTypeCategoryAnonymous() {
		checkCategoryTypePatternJLS3(
				"class A{B annonymousB = new B() {};}class B{}class E{}aspect C {declare parents : B && is(AnonymousType) extends E;}",
				TypeCategoryTypePattern.ANONYMOUS, "is(AnonymousType)");
	}

	public void testDeclareParentsTypeCategoryEnum() {
		checkCategoryTypePatternJLS3(
				"class B{}class E{}aspect C {declare parents : B && !is(EnumType) extends E;}",
				TypeCategoryTypePattern.ENUM, "is(EnumType)");
	}

	/*
	 *
	 *
	 * END: Test TypePattern nodes introduced in Bugzilla 329268.
	 *
	 *
	 */


	public void testDeclareWarning() {
		checkJLS3("aspect A {pointcut a();declare warning: a(): \"error\";}", 23, 30);
	}

	public void testDeclareError() {
		checkJLS3("aspect A {pointcut a();declare error: a(): \"error\";}", 23, 28);
	}

	public void testDeclareSoft() {
		checkJLS3("aspect A {pointcut a();declare soft: Exception+: a();}", 23, 29);
	}

	public void testDeclarePrecedence() {
		checkJLS3("aspect A{}aspect B{declare precedence: B,A;}", 19, 23);
	}

	// --------- tests for bugs ----------

	public void testJavadocCommentForDeclareExists_pr150467() {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource("aspect X {/** I have a doc comment */declare parents : Y implements Z;}".toCharArray());
		//parser.setSource("aspect X {/** I have a doc comment */public  void foo() {}}".toCharArray());
		parser.setCompilerOptions(Collections.emptyMap());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		//Javadoc javadoc = ((MethodDeclaration) ((TypeDeclaration) cu.types().get(0)).bodyDeclarations().get(0)).getJavadoc();
		Javadoc javadoc = ((DeclareParentsDeclaration) ((TypeDeclaration) cu.types().get(0)).bodyDeclarations().get(0)).getJavadoc();
		assertNull("expected the doc comment node to be null but it wasn't", javadoc);
		assertEquals("expected there to be one comment but found " + cu.getCommentList().size(), 1, cu.getCommentList().size());
	}


	protected void assertExpression(String expectedExpression, TypePattern node) {
		assertTrue("Expected: " + expectedExpression + ". Actual: " + node.getTypePatternExpression(), node.getTypePatternExpression().equals(expectedExpression));

	}

	protected void assertNodeType(Class<?> expected, TypePattern node) {
		assertTrue("Expected " + expected.toString() + ". Actual: " + node.getClass().toString(), node.getClass().equals(expected));
	}
}



class TypeCategoryTypeVisitor extends AjASTVisitor {

	private TypeCategoryTypePattern typeCategory = null;

	public boolean visit(TypeCategoryTypePattern node) {
		typeCategory = node;
		return false;
	}

	public TypeCategoryTypePattern getTypeCategoryNode() {
		return typeCategory;
	}
}

class TypePatternSourceRangeVisitor extends AjASTVisitor {
	private List<SourceRange> sourceRanges = new ArrayList<>();

	public List<SourceRange> getVisitedSourceRanges() {
		return sourceRanges;
	}

	public boolean visit(AbstractBooleanTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(AnyTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(AnyWithAnnotationTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(EllipsisTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(HasMemberTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(IdentifierTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(NotTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(NoTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}

	public boolean visit(TypeCategoryTypePattern node) {
		sourceRanges.add(new SourceRange(node.getStartPosition(), node
				.getLength()));
		return true;
	}
}


class SourceRangeVisitor extends AjASTVisitor {

	boolean visitTheKids = true;
	boolean visitDocTags;
	int start, length;

	SourceRangeVisitor() {
		this(false);
	}

	SourceRangeVisitor(boolean visitDocTags) {
		super(visitDocTags);
		this.visitDocTags = visitDocTags;
	}

	public boolean isVisitingChildren() {
		return visitTheKids;
	}

	public void setVisitingChildren(boolean visitChildren) {
		visitTheKids = visitChildren;
	}

	public int getStart() {
		return start;
	}

	public int getLength() {
		return length;
	}

	public boolean visit(DeclareAtTypeDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareAtMethodDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareAtConstructorDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareAtFieldDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareWarningDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareErrorDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareParentsDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclarePrecedenceDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}

	public boolean visit(DeclareSoftDeclaration node) {
		start = node.getStartPosition();
		length = node.getLength();
		return isVisitingChildren();
	}
}
