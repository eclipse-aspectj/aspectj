/********************************************************************
 * Copyright (c) 2006, 2010 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: Helen Hawkins   - initial implementation
 * 				 Matthew Webster - initial implementation
 *******************************************************************/
package org.aspectj.tools.ajc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.aspectj.org.eclipse.jdt.core.dom.AST;
import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
import org.aspectj.org.eclipse.jdt.core.dom.AjAST;
import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
import org.aspectj.org.eclipse.jdt.core.SourceRange;

public abstract class AjASTTestCase extends TestCase {

	protected AjAST createAjAST() {
		return createAjAST(AST.JLS3);
	}

	protected AjAST createAjAST(int astlevel) {
		if (astlevel != AST.JLS2 && astlevel != AST.JLS3) {
			fail("need to pass AST.JLS2 or AST.JLS3 as an argument");
		}
		String source = "";
		ASTParser parser = ASTParser.newParser(astlevel);
		parser.setSource(source.toCharArray());
		parser.setCompilerOptions(new HashMap());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		AST ast = cu.getAST();
		assertTrue("the ast should be an instance of AjAST",
				ast instanceof AjAST);
		return (AjAST) ast;
	}

	protected void checkJLS3(String source, ITypePatternTester tester) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(new HashMap());
		parser.setSource(source.toCharArray());
		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
		AjASTVisitor visitor = tester.createVisitor();
		cu2.accept(visitor);
		tester.testCondition(visitor);
	}

	protected void checkJLS3(String source, int start, int length) {
		checkJLS3(source, new SourceRangeTester(start, length));
	}

	/**
	 *
	 * @param source
	 *            to parse and visit
	 * @param expectedSourceRanges
	 *            of TypePattern nodes encountered while visiting the AST
	 */
	protected void checkTypePatternSourceRangesJLS3(String source,
			int[][] expectedSourceRanges) {
		checkJLS3(source,
				new TypePatternSourceRangeTester(expectedSourceRanges));
	}

	/**
	 *
	 * @param source
	 *            to parse and visit
	 * @param expectedCategory
	 *            expected category of a TypeCategoryTypePattern node
	 *            encountered in the AST
	 */
	protected void checkCategoryTypePatternJLS3(String source,
			int expectedCategory, String expectedExpression) {
		checkJLS3(source, new TypeCategoryTester(expectedCategory, expectedExpression));
	}


	protected List<SourceRange> getSourceRanges(int[][] sourceRanges) {
		List<SourceRange> convertedRanges = new ArrayList<>();

		for (int[] sourceRange : sourceRanges) {
			convertedRanges.add(new SourceRange(sourceRange[0],
					sourceRange[1]));
		}
		return convertedRanges;
	}

	/*
	 *
	 *
	 * Testing Classes and Interfaces
	 */

	/**
	 * Tests the results of a visitor when walking the AST
	 *
	 */
	interface ITypePatternTester {

		/**
		 *
		 * @return visitor to walk the AST. Must not be null.
		 */
		AjASTVisitor createVisitor();

		/**
		 * Tests a condition after the visitor has visited the AST. This means
		 * the visitor should contain the results of the visitation.
		 *
		 * @return true if test condition passed. False otherwise
		 */
		void testCondition(AjASTVisitor visitor);
	}

	/**
	 * Tests whether a particular type category type pattern (InnerType,
	 * InterfaceType, ClassType, etc..) is encountered when visiting nodes in an
	 * AST.
	 *
	 */
	class TypeCategoryTester implements ITypePatternTester {

		private int expectedCategory;
		private String expectedExpression;

		public TypeCategoryTester(int expectedCategory,
				String expectedExpression) {
			this.expectedCategory = expectedCategory;
			this.expectedExpression = expectedExpression;
		}

		public AjASTVisitor createVisitor() {
			return new TypeCategoryTypeVisitor();
		}

		public void testCondition(AjASTVisitor visitor) {
			TypeCategoryTypeVisitor tcVisitor = (TypeCategoryTypeVisitor) visitor;
			assertTrue("Expected type category: " + expectedCategory
					+ ". Actual type category: "
					+ tcVisitor.getTypeCategoryNode().getTypeCategory(),
					expectedCategory == tcVisitor.getTypeCategoryNode()
							.getTypeCategory());
			assertTrue("Expected type category expression: "
					+ expectedExpression
					+ ". Actual type category expression: "
					+ tcVisitor.getTypeCategoryNode()
							.getTypePatternExpression(),
					expectedExpression.equals(tcVisitor.getTypeCategoryNode()
							.getTypePatternExpression()));
		}
	}

	/**
	 * Tests the starting location and source length of each TypePattern node
	 * encountered while walking the AST.
	 *
	 */
	class TypePatternSourceRangeTester implements ITypePatternTester {

		private int[][] expectedRawSourceRanges;

		public TypePatternSourceRangeTester(int[][] expectedRawSourceRanges) {
			this.expectedRawSourceRanges = expectedRawSourceRanges;
		}

		public AjASTVisitor createVisitor() {
			return new TypePatternSourceRangeVisitor();
		}

		public void testCondition(AjASTVisitor visitor) {
			TypePatternSourceRangeVisitor sourceRangeVisitor = (TypePatternSourceRangeVisitor) visitor;

			List<SourceRange> actualRanges = sourceRangeVisitor
					.getVisitedSourceRanges();
			List<SourceRange> expectedRanges = getSourceRanges(expectedRawSourceRanges);

			assertTrue("Expected " + expectedRanges.size()
					+ " number of source range entries. Actual: "
					+ actualRanges.size(),
					expectedRanges.size() == actualRanges.size());

			for (int i = 0; i < actualRanges.size(); i++) {
				SourceRange expected = expectedRanges.get(i);
				SourceRange actual = actualRanges.get(i);
				assertTrue(
						"Expected source range: " + expected.toString()
								+ " does not match actual source range: "
								+ actual.toString(), expected.equals(actual));
			}

		}
	}

	/**
	 * Tests whether a particular AST node starts at a given expected location
	 * and has an expected length
	 *
	 */
	class SourceRangeTester implements ITypePatternTester {

		private int expectedStart;
		private int expectedLength;

		public SourceRangeTester(int expectedStart, int expectedLength) {
			this.expectedLength = expectedLength;
			this.expectedStart = expectedStart;
		}

		public AjASTVisitor createVisitor() {
			return new SourceRangeVisitor();
		}

		public void testCondition(AjASTVisitor visitor) {

			int s = ((SourceRangeVisitor) visitor).getStart();
			int l = ((SourceRangeVisitor) visitor).getLength();
			assertTrue("Expected start position: " + expectedStart
					+ ", Actual:" + s, expectedStart == s);
			assertTrue("Expected length: " + expectedLength + ", Actual:" + l,
					expectedLength == l);

		}

	}

}
