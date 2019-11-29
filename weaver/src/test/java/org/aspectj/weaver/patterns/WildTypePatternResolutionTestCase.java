/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.BoundedReferenceType;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeFactory;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelWorld;

import junit.framework.TestCase;

// TODO write test cases for instanceof matching

public class WildTypePatternResolutionTestCase extends TestCase {

	private World world;
	private Bindings bindings;
	private SimpleScope scope;
	private ResolvedType javaUtilList;
	private ResolvedType javaLangString;
	private ResolvedType javaUtilListOfString;
	private ResolvedType javaUtilListOfDouble;
	private ResolvedType javaUtilListOfSomething;

	/**
	 * Foo where Foo exists and is generic Parser creates WildTypePattern namePatterns={Foo} resolveBindings resolves Foo to RT(Foo
	 * - raw) return ExactTypePattern(LFoo;)
	 */
	public void testSimpleFoo() {
		TypePattern rtp = resolveWildTypePattern("List", false);

		assertTrue("resolves to exact type", rtp instanceof ExactTypePattern);
		UnresolvedType exactType = rtp.getExactType();
		assertTrue(exactType.isRawType());
		assertEquals("Ljava/util/List;", exactType.getSignature());

		ResolvedType rt = exactType.resolve(world);
		assertEquals("Ljava/util/List;", rt.getSignature());
		assertTrue(rt.isRawType());

		ExactTypePattern etp = (ExactTypePattern) writeAndRead(rtp);
		exactType = etp.getExactType();

		assertEquals("Ljava/util/List;", exactType.getSignature());

		rt = exactType.resolve(world);
		assertEquals("Ljava/util/List;", rt.getSignature());
		assertTrue(rt.isRawType());

		assertTrue("matches List", etp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertTrue("matches generic List", etp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertTrue("matches parameterized list", etp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertTrue("does not match String", etp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
	}

	/**
	 * Foo<String> where Foo exists and String meets the bounds Parser creates WildTypePattern namePatterns = {Foo},
	 * typeParameters=WTP{String} resolveBindings resolves typeParameters to ExactTypePattern(String) resolves Foo to RT(Foo)
	 * returns ExactTypePattern(PFoo<String>; - parameterized)
	 */
	public void testParameterized() {
		TypePattern rtp = resolveWildTypePattern("List<String>", false);

		assertTrue("resolves to exact type", rtp instanceof ExactTypePattern);
		UnresolvedType exactType = rtp.getExactType();
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", exactType.getSignature());

		ResolvedType rt = exactType.resolve(world);
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", rt.getSignature());
		assertTrue(rt.isParameterizedType());

		ExactTypePattern etp = (ExactTypePattern) writeAndRead(rtp);
		exactType = etp.getExactType();

		assertEquals("Pjava/util/List<Ljava/lang/String;>;", rt.getSignature());
		assertTrue(rt.isParameterizedType());

		rt = exactType.resolve(world);
		assertEquals("Pjava/util/List<Ljava/lang/String;>;", rt.getSignature());
		assertTrue(rt.isParameterizedType());

		assertFalse("does not match List", etp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", etp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertTrue("matches parameterized list", etp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", etp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", etp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());

	}

	/**
	 * Foo<Str*> where Foo exists and takes one bound Parser creates WildTypePattern namePatterns = {Foo}, typeParameters=WTP{Str*}
	 * resolveBindings resolves typeParameters to WTP{Str*} resolves Foo to RT(Foo) returns WildTypePattern(name = Foo,
	 * typeParameters = WTP{Str*} isGeneric=false)
	 */
	public void testParameterizedWildCard() {
		TypePattern rtp = resolveWildTypePattern("List<Str*>", false);

		assertTrue("resolves to WildTypePattern", rtp instanceof WildTypePattern);
		assertTrue("one type parameter", rtp.typeParameters.size() == 1);
		assertTrue("missing", ResolvedType.isMissing(rtp.getExactType()));

		WildTypePattern wtp = (WildTypePattern) writeAndRead(rtp);
		assertTrue("one type parameter", wtp.typeParameters.size() == 1);
		assertTrue("missing", ResolvedType.isMissing(wtp.getExactType()));
		assertEquals("Str*", wtp.getTypeParameters().getTypePatterns()[0].toString());

		assertFalse("does not match List", wtp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", wtp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertTrue("matches parameterized list", wtp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", wtp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", wtp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
	}

	/**
	 * Fo*<String> Parser creates WildTypePattern namePatterns = {Fo*}, typeParameters=WTP{String} resolveBindings resolves
	 * typeParameters to ETP{String} returns WildTypePattern(name = Fo*, typeParameters = ETP{String} isGeneric=false)
	 */
	public void testWildcardParameterized() {
		TypePattern rtp = resolveWildTypePattern("Li*<String>", false);

		assertTrue("resolves to WildTypePattern", rtp instanceof WildTypePattern);
		assertTrue("one type parameter", rtp.typeParameters.size() == 1);
		assertEquals("Ljava/lang/String;", rtp.typeParameters.getTypePatterns()[0].getExactType().getSignature());

		WildTypePattern wtp = (WildTypePattern) writeAndRead(rtp);
		assertTrue("one type parameter", wtp.typeParameters.size() == 1);
		assertEquals("Ljava/lang/String;", wtp.typeParameters.getTypePatterns()[0].getExactType().getSignature());

		assertFalse("does not match List", wtp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", wtp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertTrue("matches parameterized list", wtp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", wtp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", wtp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
	}

	/**
	 * Foo<?>
	 */
	public void testSomething() {
		TypePattern rtp = resolveWildTypePattern("List<?>", false);

		assertTrue("resolves to exact type", rtp instanceof ExactTypePattern);
		UnresolvedType exactType = rtp.getExactType();
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<*>;", exactType.getSignature());

		ExactTypePattern etp = (ExactTypePattern) writeAndRead(rtp);
		exactType = etp.getExactType();
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<*>;", exactType.getSignature());

		assertFalse("does not match List", etp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", etp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list", etp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", etp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", etp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());

		assertTrue("matches list of something", etp.matches(javaUtilListOfSomething, TypePattern.STATIC).alwaysTrue());
	}

	/**
	 * Foo<? extends Number>
	 */
	public void testSomethingExtends() {
		TypePattern rtp = resolveWildTypePattern("List<? extends Number>", false);

		assertTrue("resolves to exact type", rtp instanceof ExactTypePattern);
		UnresolvedType exactType = rtp.getExactType();
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<+Ljava/lang/Number;>;", exactType.getSignature());
		assertTrue("got a bounded reference type", exactType.getTypeParameters()[0] instanceof BoundedReferenceType);

		ExactTypePattern etp = (ExactTypePattern) writeAndRead(rtp);
		exactType = etp.getExactType();
		exactType = exactType.resolve(world);
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<+Ljava/lang/Number;>;", exactType.getSignature());
		assertTrue("got a bounded reference type", exactType.getTypeParameters()[0] instanceof BoundedReferenceType);

		assertFalse("does not match List", etp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", etp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list", etp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", etp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", etp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
		assertFalse("does not match list of something", etp.matches(javaUtilListOfSomething, TypePattern.STATIC).alwaysTrue());

		ResolvedType listOfNumber = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Number").resolve(world) }, world);

		ResolvedType listOfDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Double").resolve(world) }, world);

		assertFalse("does not match list of number", etp.matches(listOfNumber, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match list of double", etp.matches(listOfDouble, TypePattern.STATIC).alwaysTrue());

		ResolvedType extendsNumber = TypeFactory.createTypeFromSignature("+Ljava/lang/Number;").resolve(world);
		ResolvedType listOfExtendsNumber = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { extendsNumber }, world);

		assertTrue("matches list of ? extends number", etp.matches(listOfExtendsNumber, TypePattern.STATIC).alwaysTrue());

	}

	/**
	 * Foo<? extends Number+>
	 */
	public void testSomethingExtendsPattern() {
		TypePattern rtp = resolveWildTypePattern("List<? extends Number+>", false);

		assertTrue("resolves to wild type pattern", rtp instanceof WildTypePattern);
		assertEquals("one type parameter", 1, rtp.getTypeParameters().size());
		TypePattern tp = rtp.getTypeParameters().getTypePatterns()[0];
		assertTrue("parameter is wild", tp instanceof WildTypePattern);
		WildTypePattern tpwtp = (WildTypePattern) tp;
		assertEquals("?", tpwtp.getNamePatterns()[0].maybeGetSimpleName());
		assertEquals("java.lang.Number+", tpwtp.upperBound.toString());

		WildTypePattern wtp = (WildTypePattern) writeAndRead(rtp);
		assertEquals("one type parameter", 1, wtp.getTypeParameters().size());
		tp = rtp.getTypeParameters().getTypePatterns()[0];
		assertTrue("parameter is wild", tp instanceof WildTypePattern);
		tpwtp = (WildTypePattern) tp;
		assertEquals("?", tpwtp.getNamePatterns()[0].maybeGetSimpleName());
		assertEquals("java.lang.Number+", tpwtp.upperBound.toString());

		assertFalse("does not match List", wtp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", wtp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list", wtp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", wtp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", wtp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
		assertFalse("does not match list of something", wtp.matches(javaUtilListOfSomething, TypePattern.STATIC).alwaysTrue());

		ResolvedType listOfNumber = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Number").resolve(world) }, world);

		ResolvedType listOfDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Double").resolve(world) }, world);

		assertFalse("does not match list of number", wtp.matches(listOfNumber, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match list of double", wtp.matches(listOfDouble, TypePattern.STATIC).alwaysTrue());

		ResolvedType extendsNumber = TypeFactory.createTypeFromSignature("+Ljava/lang/Number;").resolve(world);
		ResolvedType listOfExtendsNumber = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { extendsNumber }, world);

		assertTrue("matches list of ? extends number", wtp.matches(listOfExtendsNumber, TypePattern.STATIC).alwaysTrue());

		ResolvedType extendsDouble = TypeFactory.createTypeFromSignature("+Ljava/lang/Double;").resolve(world);
		ResolvedType listOfExtendsDouble = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { extendsDouble }, world);

		assertTrue("matches list of ? extends double", wtp.matches(listOfExtendsDouble, TypePattern.STATIC).alwaysTrue());

	}

	/**
	 * Foo<? extends Num*>
	 */
	public void testSomethingExtendsPatternv2() {
		TypePattern rtp = resolveWildTypePattern("List<? extends Num*>", false);

		assertTrue("resolves to wild type pattern", rtp instanceof WildTypePattern);
		assertEquals("one type parameter", 1, rtp.getTypeParameters().size());
		TypePattern tp = rtp.getTypeParameters().getTypePatterns()[0];
		assertTrue("parameter is wild", tp instanceof WildTypePattern);
		WildTypePattern tpwtp = (WildTypePattern) tp;
		assertEquals("?", tpwtp.getNamePatterns()[0].maybeGetSimpleName());
		assertEquals("Num*", tpwtp.upperBound.toString());

		WildTypePattern wtp = (WildTypePattern) writeAndRead(rtp);
		assertEquals("one type parameter", 1, wtp.getTypeParameters().size());
		tp = rtp.getTypeParameters().getTypePatterns()[0];
		assertTrue("parameter is wild", tp instanceof WildTypePattern);
		tpwtp = (WildTypePattern) tp;
		assertEquals("?", tpwtp.getNamePatterns()[0].maybeGetSimpleName());
		assertEquals("Num*", tpwtp.upperBound.toString());

		assertFalse("does not match List", wtp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", wtp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list", wtp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", wtp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", wtp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
		assertFalse("does not match list of something", wtp.matches(javaUtilListOfSomething, TypePattern.STATIC).alwaysTrue());

		ResolvedType listOfNumber = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Number").resolve(world) }, world);

		ResolvedType listOfDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Double").resolve(world) }, world);

		assertFalse("does not match list of number", wtp.matches(listOfNumber, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match list of double", wtp.matches(listOfDouble, TypePattern.STATIC).alwaysTrue());

		ResolvedType extendsNumber = TypeFactory.createTypeFromSignature("+Ljava/lang/Number;").resolve(world);
		ResolvedType listOfExtendsNumber = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { extendsNumber }, world);

		assertTrue("matches list of ? extends number", wtp.matches(listOfExtendsNumber, TypePattern.STATIC).alwaysTrue());

		ResolvedType extendsDouble = TypeFactory.createTypeFromSignature("+Ljava/lang/Double;").resolve(world);
		ResolvedType listOfExtendsDouble = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { extendsDouble }, world);

		assertFalse("does not match list of ? extends double", wtp.matches(listOfExtendsDouble, TypePattern.STATIC).alwaysTrue());
	}

	/**
	 * Foo<? super Number>
	 * 
	 */
	public void testSomethingSuper() {
		TypePattern rtp = resolveWildTypePattern("List<? super Double>", false);

		assertTrue("resolves to exact type", rtp instanceof ExactTypePattern);
		UnresolvedType exactType = rtp.getExactType();
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<-Ljava/lang/Double;>;", exactType.getSignature());
		assertTrue("got a bounded reference type", exactType.getTypeParameters()[0] instanceof BoundedReferenceType);

		ExactTypePattern etp = (ExactTypePattern) writeAndRead(rtp);
		exactType = etp.getExactType();
		exactType = exactType.resolve(world);
		assertTrue(exactType.isParameterizedType());
		assertEquals("Pjava/util/List<-Ljava/lang/Double;>;", exactType.getSignature());
		assertTrue("got a bounded reference type", exactType.getTypeParameters()[0] instanceof BoundedReferenceType);

		assertFalse("does not match List", etp.matches(javaUtilList, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match generic List", etp.matches(javaUtilList.getGenericType(), TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list", etp.matches(javaUtilListOfString, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match parameterized list of double", etp.matches(javaUtilListOfDouble, TypePattern.STATIC)
				.alwaysTrue());
		assertTrue("does not match String", etp.matches(javaLangString, TypePattern.STATIC).alwaysFalse());
		assertFalse("does not match list of something", etp.matches(javaUtilListOfSomething, TypePattern.STATIC).alwaysTrue());

		ResolvedType listOfNumber = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Number").resolve(world) }, world);

		ResolvedType listOfDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Double").resolve(world) }, world);

		assertFalse("does not match list of number", etp.matches(listOfNumber, TypePattern.STATIC).alwaysTrue());
		assertFalse("does not match list of double", etp.matches(listOfDouble, TypePattern.STATIC).alwaysTrue());

		ResolvedType superDouble = TypeFactory.createTypeFromSignature("-Ljava/lang/Double;").resolve(world);
		ResolvedType listOfSuperDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { superDouble },
				world);

		assertTrue("matches list of ? super double", etp.matches(listOfSuperDouble, TypePattern.STATIC).alwaysTrue());
	}

	private TypePattern resolveWildTypePattern(String source, boolean requireExact) {
		WildTypePattern wtp = makeWildTypePattern(source);
		return wtp.resolveBindings(scope, bindings, false, requireExact);
	}

	private WildTypePattern makeWildTypePattern(String source) {
		PatternParser parser = new PatternParser(source);
		return (WildTypePattern) parser.parseTypePattern();
	}

	private TypePattern writeAndRead(TypePattern etp) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ConstantPoolSimulator cps = new ConstantPoolSimulator();
			CompressingDataOutputStream dos = new CompressingDataOutputStream(baos, cps);
			etp.write(dos);
			dos.flush();
			dos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			VersionedDataInputStream in = new VersionedDataInputStream(bais, cps);
			in.setVersion(new WeaverVersionInfo());
			TypePattern ret = TypePattern.read(in, null);
			return ret;
		} catch (IOException ioEx) {
			fail(ioEx + " thrown during serialization");
		}
		return null;
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.world = new BcelWorld();
		this.world.setBehaveInJava5Way(true);
		this.bindings = new Bindings(0);
		this.scope = new SimpleScope(world, new FormalBinding[] {});
		this.scope.setImportedPrefixes(new String[] { "java.io.", "java.util.", "java.lang." });
		this.javaLangString = UnresolvedType.forName("java.lang.String").resolve(world);
		this.javaUtilList = UnresolvedType.forName("java.util.List").resolve(world);
		this.javaUtilListOfString = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { javaLangString },
				world);
		this.javaUtilListOfDouble = TypeFactory.createParameterizedType(javaUtilList, new UnresolvedType[] { UnresolvedType
				.forName("java.lang.Double").resolve(world) }, world);
		this.javaUtilListOfSomething = TypeFactory.createParameterizedType(javaUtilList,
				new UnresolvedType[] { UnresolvedType.SOMETHING.resolve(world) }, world);
	}
}
