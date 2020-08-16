package org.aspectj.apache.bcel.generic;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.CodeException;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeParamAnnos;

/**
 * Template class for building up a method. This is done by defining exception handlers, adding thrown exceptions, local variables
 * and attributes, whereas the 'LocalVariableTable' and 'LineNumberTable' attributes will be set automatically for the code. Use
 * stripAttributes() if you don't like this.
 * 
 * While generating code it may be necessary to insert NOP operations. You can use the `removeNOPs' method to get rid off them. The
 * resulting method object can be obtained via the `getMethod()' method.
 * 
 * @version $Id: MethodGen.java,v 1.17 2011/05/19 23:23:46 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author <A HREF="http://www.vmeng.com/beard">Patrick C. Beard</A> [setMaxStack()]
 * @see InstructionList
 * @see Method
 */
public class MethodGen extends FieldGenOrMethodGen {
	private String classname;
	private Type[] parameterTypes;
	private String[] parameterNames;
	private int maxLocals;
	private int maxStack;
	private InstructionList il;

	// Indicates whether to produce code attributes for LineNumberTable and LocalVariableTable, like javac -O
	private boolean stripAttributes;

	private int highestLineNumber = 0;

	private List<LocalVariableGen> localVariablesList = new ArrayList<>();
	private List<LineNumberGen> lineNumbersList = new ArrayList<>();
	private ArrayList<CodeExceptionGen> exceptionsList = new ArrayList<>();
	private ArrayList<String> exceptionsThrown = new ArrayList<>();
	private List<Attribute> codeAttributesList = new ArrayList<>();
	private List<AnnotationGen>[] param_annotations; // Array of lists containing AnnotationGen objects
	private boolean hasParameterAnnotations = false;
	private boolean haveUnpackedParameterAnnotations = false;

	/**
	 * Declare method. If the method is non-static the constructor automatically declares a local variable `$this' in slot 0. The
	 * actual code is contained in the `il' parameter, which may further manipulated by the user. But he must take care not to
	 * remove any instruction (handles) that are still referenced from this object.
	 * 
	 * For example one may not add a local variable and later remove the instructions it refers to without causing havoc. It is safe
	 * however if you remove that local variable, too.
	 * 
	 * @param access_flags access qualifiers
	 * @param return_type method type
	 * @param arg_types argument types
	 * @param arg_names argument names (if this is null, default names will be provided for them)
	 * @param method_name name of method
	 * @param class_name class name containing this method (may be null, if you don't care)
	 * @param il instruction list associated with this method, may be null only for abstract or native methods
	 * @param cp constant pool
	 */
	public MethodGen(int access_flags, Type return_type, Type[] arg_types, String[] arg_names, String method_name,
			String class_name, InstructionList il, ConstantPool cp) {

		this.modifiers = access_flags;
		this.type = return_type;
		this.parameterTypes = arg_types;
		this.parameterNames = arg_names;
		this.name = method_name;
		this.classname = class_name;
		this.il = il;
		this.cp = cp;

		// OPTIMIZE this code messes with the local variables - do we need it?
		// boolean abstract_ = isAbstract() || isNative();
		// InstructionHandle start = null;
		// InstructionHandle end = null;
		//
		// if (!abstract_) {
		// start = il.getStart();
		// end = il.getEnd();
		//
		// /* Add local variables, namely the implicit `this' and the arguments
		// */
		// // if(!isStatic() && (class_name != null)) { // Instance method -> `this' is local var 0
		// // addLocalVariable("this", new ObjectType(class_name), start, end);
		// // }
		// }

		// if(arg_types != null) {
		// int size = arg_types.length;
		//
		// for(int i=0; i < size; i++) {
		// if(Type.VOID == arg_types[i]) {
		// throw new ClassGenException("'void' is an illegal argument type for a method");
		// }
		// }
		//	
		// if(arg_names != null) { // Names for variables provided?
		// if(size != arg_names.length)
		// throw new ClassGenException("Mismatch in argument array lengths: " +
		// size + " vs. " + arg_names.length);
		// } else { // Give them dummy names
		// // arg_names = new String[size];
		// //
		// // for(int i=0; i < size; i++)
		// // arg_names[i] = "arg" + i;
		// //
		// // setArgumentNames(arg_names);
		// }

		// if(!abstract_) {
		// for(int i=0; i < size; i++) {
		// // addLocalVariable(arg_names[i], arg_types[i], start, end);
		// }
		// }
		// }
	}

	public int getHighestlinenumber() {
		return highestLineNumber;
	}

	/**
	 * Instantiate from existing method.
	 * 
	 * @param m method
	 * @param class_name class name containing this method
	 * @param cp constant pool
	 */

	public MethodGen(Method m, String class_name, ConstantPool cp) {
		this(m, class_name, cp, false);
	}

	// OPTIMIZE should always use tags and never anything else!
	public MethodGen(Method m, String class_name, ConstantPool cp, boolean useTags) {
		this(m.getModifiers(),
		// OPTIMIZE implementation of getReturnType() and getArgumentTypes() on Method seems weak
				m.getReturnType(), m.getArgumentTypes(), null /* may be overridden anyway */, m.getName(), class_name, ((m
						.getModifiers() & (Constants.ACC_ABSTRACT | Constants.ACC_NATIVE)) == 0) ? new InstructionList(m.getCode()
						.getCode()) : null, cp);

		Attribute[] attributes = m.getAttributes();
		for (Attribute attribute : attributes) {
			Attribute a = attribute;

			if (a instanceof Code) {
				Code code = (Code) a;
				setMaxStack(code.getMaxStack());
				setMaxLocals(code.getMaxLocals());

				CodeException[] ces = code.getExceptionTable();

				InstructionHandle[] arrayOfInstructions = il.getInstructionsAsArray();

				// process the exception table
				// -
				if (ces != null) {
					for (CodeException ce : ces) {
						int type = ce.getCatchType();
						ObjectType catchType = null;

						if (type > 0) {
							String cen = m.getConstantPool().getConstantString_CONSTANTClass(type);
							catchType = new ObjectType(cen);
						}

						int end_pc = ce.getEndPC();
						int length = m.getCode().getCode().length;

						InstructionHandle end;

						if (length == end_pc) { // May happen, because end_pc is exclusive
							end = il.getEnd();
						} else {
							end = il.findHandle(end_pc, arrayOfInstructions);// il.findHandle(end_pc);
							end = end.getPrev(); // Make it inclusive
						}

						addExceptionHandler(il.findHandle(ce.getStartPC(), arrayOfInstructions), end, il.findHandle(ce
								.getHandlerPC(), arrayOfInstructions), catchType);
					}
				}

				Attribute[] codeAttrs = code.getAttributes();
				for (Attribute codeAttr : codeAttrs) {
					a = codeAttr;

					if (a instanceof LineNumberTable) {
						LineNumber[] ln = ((LineNumberTable) a).getLineNumberTable();
						if (useTags) {
							// abracadabra, lets create tags rather than linenumbergens.
							for (LineNumber l : ln) {
								int lnum = l.getLineNumber();
								if (lnum > highestLineNumber) {
									highestLineNumber = lnum;
								}
								LineNumberTag lt = new LineNumberTag(lnum);
								il.findHandle(l.getStartPC(), arrayOfInstructions, true).addTargeter(lt);
							}
						} else {
							for (LineNumber l : ln) {
								addLineNumber(il.findHandle(l.getStartPC(), arrayOfInstructions, true), l.getLineNumber());
							}
						}
					} else if (a instanceof LocalVariableTable) {

						// Lets have a go at creating Tags directly
						if (useTags) {
							LocalVariable[] lv = ((LocalVariableTable) a).getLocalVariableTable();

							for (LocalVariable l : lv) {
								Type t = Type.getType(l.getSignature());
								LocalVariableTag lvt = new LocalVariableTag(t, l.getSignature(), l.getName(), l.getIndex(), l
										.getStartPC());
								InstructionHandle start = il.findHandle(l.getStartPC(), arrayOfInstructions, true);
								byte b = t.getType();
								if (b != Constants.T_ADDRESS) {
									int increment = t.getSize();
									if (l.getIndex() + increment > maxLocals) {
										maxLocals = l.getIndex() + increment;
									}
								}
								int end = l.getStartPC() + l.getLength();
								do {
									start.addTargeter(lvt);
									start = start.getNext();
								} while (start != null && start.getPosition() < end);
							}
						} else {

							LocalVariable[] lv = ((LocalVariableTable) a).getLocalVariableTable();

							removeLocalVariables();

							for (LocalVariable l : lv) {
								InstructionHandle start = il.findHandle(l.getStartPC(), arrayOfInstructions);
								InstructionHandle end = il.findHandle(l.getStartPC() + l.getLength(), arrayOfInstructions);
								// AMC, this actually gives us the first instruction AFTER the range,
								// so move back one... (findHandle can't cope with mid-instruction indices)
								if (end != null) {
									end = end.getPrev();
								}
								// Repair malformed handles
								if (null == start) {
									start = il.getStart();
								}
								if (null == end) {
									end = il.getEnd();
								}

								addLocalVariable(l.getName(), Type.getType(l.getSignature()), l.getIndex(), start, end);
							}
						}
					} else {
						addCodeAttribute(a);
					}
				}
			} else if (a instanceof ExceptionTable) {
				String[] names = ((ExceptionTable) a).getExceptionNames();
				for (String s : names) {
					addException(s);
				}
			} else if (a instanceof RuntimeAnnos) {
				RuntimeAnnos runtimeAnnotations = (RuntimeAnnos) a;
				List<AnnotationGen> l = runtimeAnnotations.getAnnotations();
				annotationList.addAll(l);
				// for (Iterator<AnnotationGen> it = l.iterator(); it.hasNext();) {
				// AnnotationGen element = it.next();
				// addAnnotation(new AnnotationGen(element, cp, false));
				// }
			} else {
				addAttribute(a);
			}
		}
	}

	public LocalVariableGen addLocalVariable(String name, Type type, int slot, InstructionHandle start, InstructionHandle end) {
		int size = type.getSize();
		if (slot + size > maxLocals) {
			maxLocals = slot + size;
		}
		LocalVariableGen l = new LocalVariableGen(slot, name, type, start, end);
		int i = localVariablesList.indexOf(l);
		if (i >= 0) {
			localVariablesList.set(i, l); // Overwrite if necessary
		} else {
			localVariablesList.add(l);
		}
		return l;
	}

	/**
	 * Adds a local variable to this method and assigns an index automatically.
	 * 
	 * @param name variable name
	 * @param type variable type
	 * @param start from where the variable is valid, if this is null, it is valid from the start
	 * @param end until where the variable is valid, if this is null, it is valid to the end
	 * @return new local variable object
	 * @see LocalVariable
	 */
	public LocalVariableGen addLocalVariable(String name, Type type, InstructionHandle start, InstructionHandle end) {
		return addLocalVariable(name, type, maxLocals, start, end);
	}

	/**
	 * Remove a local variable, its slot will not be reused, if you do not use addLocalVariable with an explicit index argument.
	 */
	public void removeLocalVariable(LocalVariableGen l) {
		localVariablesList.remove(l);
	}

	/**
	 * Remove all local variables.
	 */
	public void removeLocalVariables() {
		localVariablesList.clear();
	}

	/**
	 * Sort local variables by index
	 */
	private static final void sort(LocalVariableGen[] vars, int l, int r) {
		int i = l, j = r;
		int m = vars[(l + r) / 2].getIndex();
		LocalVariableGen h;

		do {
			while (vars[i].getIndex() < m) {
				i++;
			}
			while (m < vars[j].getIndex()) {
				j--;
			}

			if (i <= j) {
				h = vars[i];
				vars[i] = vars[j];
				vars[j] = h; // Swap elements
				i++;
				j--;
			}
		} while (i <= j);

		if (l < j) {
			sort(vars, l, j);
		}
		if (i < r) {
			sort(vars, i, r);
		}
	}

	/*
	 * If the range of the variable has not been set yet, it will be set to be valid from the start to the end of the instruction
	 * list.
	 * 
	 * @return array of declared local variables sorted by index
	 */
	public LocalVariableGen[] getLocalVariables() {
		int size = localVariablesList.size();
		LocalVariableGen[] lg = new LocalVariableGen[size];
		localVariablesList.toArray(lg);

		for (int i = 0; i < size; i++) {
			if (lg[i].getStart() == null) {
				lg[i].setStart(il.getStart());
			}

			if (lg[i].getEnd() == null) {
				lg[i].setEnd(il.getEnd());
			}
		}

		if (size > 1) {
			sort(lg, 0, size - 1);
		}

		return lg;
	}

	/**
	 * @return `LocalVariableTable' attribute of all the local variables of this method.
	 */
	public LocalVariableTable getLocalVariableTable(ConstantPool cp) {
		LocalVariableGen[] lg = getLocalVariables();
		int size = lg.length;
		LocalVariable[] lv = new LocalVariable[size];

		for (int i = 0; i < size; i++) {
			lv[i] = lg[i].getLocalVariable(cp);
		}

		return new LocalVariableTable(cp.addUtf8("LocalVariableTable"), 2 + lv.length * 10, lv, cp);
	}

	/**
	 * Give an instruction a line number corresponding to the source code line.
	 * 
	 * @param ih instruction to tag
	 * @return new line number object
	 * @see LineNumber
	 */
	public LineNumberGen addLineNumber(InstructionHandle ih, int src_line) {
		LineNumberGen l = new LineNumberGen(ih, src_line);
		lineNumbersList.add(l);
		return l;
	}

	/**
	 * Remove a line number.
	 */
	public void removeLineNumber(LineNumberGen l) {
		lineNumbersList.remove(l);
	}

	/**
	 * Remove all line numbers.
	 */
	public void removeLineNumbers() {
		lineNumbersList.clear();
	}

	/*
	 * @return array of line numbers
	 */
	public LineNumberGen[] getLineNumbers() {
		LineNumberGen[] lg = new LineNumberGen[lineNumbersList.size()];
		lineNumbersList.toArray(lg);
		return lg;
	}

	/**
	 * @return 'LineNumberTable' attribute for all the local variables of this method.
	 */
	public LineNumberTable getLineNumberTable(ConstantPool cp) {
		int size = lineNumbersList.size();
		LineNumber[] ln = new LineNumber[size];

		for (int i = 0; i < size; i++) {
			ln[i] = lineNumbersList.get(i).getLineNumber();
		}

		return new LineNumberTable(cp.addUtf8("LineNumberTable"), 2 + ln.length * 4, ln, cp);
	}

	/**
	 * Add an exception handler, i.e., specify region where a handler is active and an instruction where the actual handling is
	 * done.
	 * 
	 * @param start_pc Start of region (inclusive)
	 * @param end_pc End of region (inclusive)
	 * @param handler_pc Where handling is done
	 * @param catch_type class type of handled exception or null if any exception is handled
	 * @return new exception handler object
	 */
	public CodeExceptionGen addExceptionHandler(InstructionHandle start_pc, InstructionHandle end_pc, InstructionHandle handler_pc,
			ObjectType catch_type) {
		if ((start_pc == null) || (end_pc == null) || (handler_pc == null)) {
			throw new ClassGenException("Exception handler target is null instruction");
		}

		CodeExceptionGen c = new CodeExceptionGen(start_pc, end_pc, handler_pc, catch_type);
		exceptionsList.add(c);
		return c;
	}

	/**
	 * Remove an exception handler.
	 */
	public void removeExceptionHandler(CodeExceptionGen c) {
		exceptionsList.remove(c);
	}

	/**
	 * Remove all line numbers.
	 */
	public void removeExceptionHandlers() {
		exceptionsList.clear();
	}

	/*
	 * @return array of declared exception handlers
	 */
	public CodeExceptionGen[] getExceptionHandlers() {
		CodeExceptionGen[] cg = new CodeExceptionGen[exceptionsList.size()];
		exceptionsList.toArray(cg);
		return cg;
	}

	/**
	 * @return code exceptions for `Code' attribute
	 */
	private CodeException[] getCodeExceptions() {
		int size = exceptionsList.size();
		CodeException[] c_exc = new CodeException[size];

		try {
			for (int i = 0; i < size; i++) {
				CodeExceptionGen c = exceptionsList.get(i);
				c_exc[i] = c.getCodeException(cp);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		return c_exc;
	}

	/**
	 * Add an exception possibly thrown by this method.
	 * 
	 * @param class_name (fully qualified) name of exception
	 */
	public void addException(String class_name) {
		exceptionsThrown.add(class_name);
	}

	/**
	 * Remove an exception.
	 */
	public void removeException(String c) {
		exceptionsThrown.remove(c);
	}

	/**
	 * Remove all exceptions.
	 */
	public void removeExceptions() {
		exceptionsThrown.clear();
	}

	/*
	 * @return array of thrown exceptions
	 */
	public String[] getExceptions() {
		String[] e = new String[exceptionsThrown.size()];
		exceptionsThrown.toArray(e);
		return e;
	}

	/**
	 * @return `Exceptions' attribute of all the exceptions thrown by this method.
	 */
	private ExceptionTable getExceptionTable(ConstantPool cp) {
		int size = exceptionsThrown.size();
		int[] ex = new int[size];

		try {
			for (int i = 0; i < size; i++) {
				ex[i] = cp.addClass(exceptionsThrown.get(i));
			}
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		return new ExceptionTable(cp.addUtf8("Exceptions"), 2 + 2 * size, ex, cp);
	}

	/**
	 * Add an attribute to the code. Currently, the JVM knows about the LineNumberTable, LocalVariableTable and StackMap attributes,
	 * where the former two will be generated automatically and the latter is used for the MIDP only. Other attributes will be
	 * ignored by the JVM but do no harm.
	 * 
	 * @param a attribute to be added
	 */
	public void addCodeAttribute(Attribute a) {
		codeAttributesList.add(a);
	}

	public void addParameterAnnotationsAsAttribute(ConstantPool cp) {
		if (!hasParameterAnnotations) {
			return;
		}
		Attribute[] attrs = Utility.getParameterAnnotationAttributes(cp, param_annotations);
		if (attrs != null) {
			for (Attribute attr : attrs) {
				addAttribute(attr);
			}
		}
	}

	/**
	 * Remove a code attribute.
	 */
	public void removeCodeAttribute(Attribute a) {
		codeAttributesList.remove(a);
	}

	/**
	 * Remove all code attributes.
	 */
	public void removeCodeAttributes() {
		codeAttributesList.clear();
	}

	/**
	 * @return all attributes of this method.
	 */
	public Attribute[] getCodeAttributes() {
		Attribute[] attributes = new Attribute[codeAttributesList.size()];
		codeAttributesList.toArray(attributes);
		return attributes;
	}

	/**
	 * Get method object. Never forget to call setMaxStack() or setMaxStack(max), respectively, before calling this method (the same
	 * applies for max locals).
	 * 
	 * @return method object
	 */
	public Method getMethod() {
		String signature = getSignature();
		int name_index = cp.addUtf8(name);
		int signature_index = cp.addUtf8(signature);

		/*
		 * Also updates positions of instructions, i.e., their indices
		 */
		byte[] byte_code = null;

		if (il != null) {
			try {
				byte_code = il.getByteCode();
			} catch (Exception e) {
				throw new IllegalStateException("Unexpected problem whilst preparing bytecode for " + this.getClassName() + "."
						+ this.getName() + this.getSignature(), e);
			}
		}

		LineNumberTable lnt = null;
		LocalVariableTable lvt = null;
		// J5TODO: LocalVariableTypeTable support!

		/*
		 * Create LocalVariableTable and LineNumberTable attributes (for debuggers, e.g.)
		 */
		if ((localVariablesList.size() > 0) && !stripAttributes) {
			addCodeAttribute(lvt = getLocalVariableTable(cp));
		}

		if ((lineNumbersList.size() > 0) && !stripAttributes) {
			addCodeAttribute(lnt = getLineNumberTable(cp));
		}

		Attribute[] code_attrs = getCodeAttributes();

		/*
		 * Each attribute causes 6 additional header bytes
		 */
		int attrs_len = 0;
		for (Attribute code_attr : code_attrs) {
			attrs_len += (code_attr.getLength() + 6);
		}

		CodeException[] c_exc = getCodeExceptions();
		int exc_len = c_exc.length * 8; // Every entry takes 8 bytes

		Code code = null;

		if ((il != null) && !isAbstract()) {
			// Remove any stale code attribute
			List<Attribute> attributes = getAttributes();
			for (Attribute a : attributes) {
				if (a instanceof Code) {
					removeAttribute(a);
				}
			}

			code = new Code(cp.addUtf8("Code"), 8 + byte_code.length + // prologue byte code
					2 + exc_len + // exceptions
					2 + attrs_len, // attributes
					maxStack, maxLocals, byte_code, c_exc, code_attrs, cp);

			addAttribute(code);
		}

		addAnnotationsAsAttribute(cp);
		addParameterAnnotationsAsAttribute(cp);

		ExceptionTable et = null;

		if (exceptionsThrown.size() > 0) {
			addAttribute(et = getExceptionTable(cp)); // Add `Exceptions' if there are "throws" clauses
		}

		Method m = new Method(modifiers, name_index, signature_index, getAttributesImmutable(), cp);

		// Undo effects of adding attributes
		// OPTIMIZE why redo this? is there a better way to clean up?
		if (lvt != null) {
			removeCodeAttribute(lvt);
		}
		if (lnt != null) {
			removeCodeAttribute(lnt);
		}
		if (code != null) {
			removeAttribute(code);
		}
		if (et != null) {
			removeAttribute(et);
		}
		// J5TODO: Remove the annotation attributes that may have been added
		return m;
	}

	/**
	 * Set maximum number of local variables.
	 */
	public void setMaxLocals(int m) {
		maxLocals = m;
	}

	public int getMaxLocals() {
		return maxLocals;
	}

	/**
	 * Set maximum stack size for this method.
	 */
	public void setMaxStack(int m) {
		maxStack = m;
	}

	public int getMaxStack() {
		return maxStack;
	}

	/**
	 * @return class that contains this method
	 */
	public String getClassName() {
		return classname;
	}

	public void setClassName(String class_name) {
		this.classname = class_name;
	}

	public void setReturnType(Type return_type) {
		setType(return_type);
	}

	public Type getReturnType() {
		return getType();
	}

	public void setArgumentTypes(Type[] arg_types) {
		this.parameterTypes = arg_types;
	}

	public Type[] getArgumentTypes() {
		return this.parameterTypes;
	}// OPTIMIZE dont need clone here? (Type[])arg_types.clone(); }

	public void setArgumentType(int i, Type type) {
		parameterTypes[i] = type;
	}

	public Type getArgumentType(int i) {
		return parameterTypes[i];
	}

	public void setArgumentNames(String[] arg_names) {
		this.parameterNames = arg_names;
	}

	public String[] getArgumentNames() {
		if (parameterNames != null) {
			return parameterNames.clone();
		} else {
			return new String[0];
		}
	}

	public void setArgumentName(int i, String name) {
		parameterNames[i] = name;
	}

	public String getArgumentName(int i) {
		return parameterNames[i];
	}

	public InstructionList getInstructionList() {
		return il;
	}

	public void setInstructionList(InstructionList il) {
		this.il = il;
	}

	@Override
	public String getSignature() {
		return Utility.toMethodSignature(type, parameterTypes);
	}

	/**
	 * Computes max. stack size by performing control flow analysis.
	 */
	public void setMaxStack() {
		if (il != null) {
			maxStack = getMaxStack(cp, il, getExceptionHandlers());
		} else {
			maxStack = 0;
		}
	}

	/**
	 * Compute maximum number of local variables based on the parameter count and bytecode usage of variables.
	 */
	public void setMaxLocals() {
		setMaxLocals(false);
	}
	
	/**
	 * Compute maximum number of local variables.
	 * 
	 * @param respectLocalVariableTable if true and the local variable table indicates more are in use
	 * than the code suggests, respect the higher value from the local variable table data.
	 */
	public void setMaxLocals(boolean respectLocalVariableTable) {
		if (il != null) {
			int max = isStatic() ? 0 : 1;

			if (parameterTypes != null) {
				for (Type parameterType : parameterTypes) {
					max += parameterType.getSize();
				}
			}

			for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
				Instruction ins = ih.getInstruction();

				if ((ins instanceof InstructionLV) || (ins instanceof RET)) {
					int index = ins.getIndex() + ins.getType(cp).getSize();

					if (index > max) {
						max = index;
					}
				}
			}
			if (!respectLocalVariableTable || max > maxLocals) {
				maxLocals = max;
			}
		} else {
			if (!respectLocalVariableTable) {
				maxLocals = 0;
			}
		}
	}

	public void stripAttributes(boolean flag) {
		stripAttributes = flag;
	}

	static final class BranchTarget {
		InstructionHandle target;
		int stackDepth;

		BranchTarget(InstructionHandle target, int stackDepth) {
			this.target = target;
			this.stackDepth = stackDepth;
		}
	}

	static final class BranchStack {
		Stack<BranchTarget> branchTargets = new Stack<>();
		Map<InstructionHandle, BranchTarget> visitedTargets = new Hashtable<>();

		public void push(InstructionHandle target, int stackDepth) {
			if (visited(target)) {
				return;
			}

			branchTargets.push(visit(target, stackDepth));
		}

		public BranchTarget pop() {
			if (!branchTargets.empty()) {
				BranchTarget bt = branchTargets.pop();
				return bt;
			}

			return null;
		}

		private final BranchTarget visit(InstructionHandle target, int stackDepth) {
			BranchTarget bt = new BranchTarget(target, stackDepth);
			visitedTargets.put(target, bt);

			return bt;
		}

		private final boolean visited(InstructionHandle target) {
			return (visitedTargets.get(target) != null);
		}
	}

	/**
	 * Computes stack usage of an instruction list by performing control flow analysis.
	 * 
	 * @return maximum stack depth used by method
	 */
	public static int getMaxStack(ConstantPool cp, InstructionList il, CodeExceptionGen[] et) {
		BranchStack branchTargets = new BranchStack();

		int stackDepth = 0;
		int maxStackDepth = 0;

		/*
		 * Initially, populate the branch stack with the exception handlers, because these aren't (necessarily) branched to
		 * explicitly. In each case, the stack will have depth 1, containing the exception object.
		 */
		for (CodeExceptionGen codeExceptionGen : et) {
			InstructionHandle handlerPos = codeExceptionGen.getHandlerPC();
			if (handlerPos != null) {
				// it must be at least 1 since there is an exception handler
				maxStackDepth = 1;
				branchTargets.push(handlerPos, 1);
			}
		}

		InstructionHandle ih = il.getStart();
		while (ih != null) {
			Instruction instruction = ih.getInstruction();
			short opcode = instruction.opcode;
			int prod = instruction.produceStack(cp);
			int con = instruction.consumeStack(cp);
			int delta = prod - con;

			stackDepth += delta;
			if (stackDepth > maxStackDepth) {
				maxStackDepth = stackDepth;
			}

			// choose the next instruction based on whether current is a branch.
			if (instruction instanceof InstructionBranch) {
				InstructionBranch branch = (InstructionBranch) instruction;
				if (instruction instanceof InstructionSelect) {
					// explore all of the select's targets. the default target is handled below.
					InstructionSelect select = (InstructionSelect) branch;
					InstructionHandle[] targets = select.getTargets();
					for (InstructionHandle target : targets) {
						branchTargets.push(target, stackDepth);
					}
					// nothing to fall through to.
					ih = null;
				} else if (!(branch.isIfInstruction())) {
					// if an instruction that comes back to following PC,
					// push next instruction, with stack depth reduced by 1.
					if (opcode == Constants.JSR || opcode == Constants.JSR_W) {
						branchTargets.push(ih.getNext(), stackDepth - 1);
					}
					ih = null;
				}
				// for all branches, the target of the branch is pushed on the branch stack.
				// conditional branches have a fall through case, selects don't, and
				// jsr/jsr_w return to the next instruction.
				branchTargets.push(branch.getTarget(), stackDepth);
			} else {
				// check for instructions that terminate the method.
				if (opcode == Constants.ATHROW || opcode == Constants.RET
						|| (opcode >= Constants.IRETURN && opcode <= Constants.RETURN)) {
					ih = null;
				}
			}
			// normal case, go to the next instruction.
			if (ih != null) {
				ih = ih.getNext();
			}
			// if we have no more instructions, see if there are any deferred branches to explore.
			if (ih == null) {
				BranchTarget bt = branchTargets.pop();
				if (bt != null) {
					ih = bt.target;
					stackDepth = bt.stackDepth;
				}
			}
		}
		return maxStackDepth;
	}

	/**
	 * Return string representation close to declaration format, `public static void main(String[]) throws IOException', e.g.
	 * 
	 * @return String representation of the method.
	 */
	@Override
	public final String toString() {
		String access = Utility.accessToString(modifiers);
		String signature = Utility.toMethodSignature(type, parameterTypes);

		signature = Utility.methodSignatureToString(signature, name, access, true, getLocalVariableTable(cp));

		StringBuffer buf = new StringBuffer(signature);

		if (exceptionsThrown.size() > 0) {
			for (String s : exceptionsThrown) {
				buf.append("\n\t\tthrows " + s);
			}
		}

		return buf.toString();
	}

	// J5TODO: Should param_annotations be an array of arrays? Rather than an array of lists, this
	// is more likely to suggest to the caller it is readonly (which a List does not).
	/**
	 * Return a list of AnnotationGen objects representing parameter annotations
	 */
	public List<AnnotationGen> getAnnotationsOnParameter(int i) {
		ensureExistingParameterAnnotationsUnpacked();
		if (!hasParameterAnnotations || i > parameterTypes.length) {
			return null;
		}
		return param_annotations[i];
	}

	/**
	 * Goes through the attributes on the method and identifies any that are RuntimeParameterAnnotations, extracting their contents
	 * and storing them as parameter annotations. There are two kinds of parameter annotation - visible and invisible. Once they
	 * have been unpacked, these attributes are deleted. (The annotations will be rebuilt as attributes when someone builds a Method
	 * object out of this MethodGen object).
	 */
	private void ensureExistingParameterAnnotationsUnpacked() {
		if (haveUnpackedParameterAnnotations) {
			return;
		}
		// Find attributes that contain parameter annotation data
		List<Attribute> attrs = getAttributes();
		RuntimeParamAnnos paramAnnVisAttr = null;
		RuntimeParamAnnos paramAnnInvisAttr = null;

		for (Attribute attribute : attrs) {
			if (attribute instanceof RuntimeParamAnnos) {

				if (!hasParameterAnnotations) {
					param_annotations = new List[parameterTypes.length];
					for (int j = 0; j < parameterTypes.length; j++) {
						param_annotations[j] = new ArrayList<>();
					}
				}

				hasParameterAnnotations = true;
				RuntimeParamAnnos rpa = (RuntimeParamAnnos) attribute;
				if (rpa.areVisible()) {
					paramAnnVisAttr = rpa;
				} else {
					paramAnnInvisAttr = rpa;
				}
				for (int j = 0; j < parameterTypes.length; j++) {
					// This returns Annotation[] ...
					AnnotationGen[] annos = rpa.getAnnotationsOnParameter(j);
					// ... which needs transforming into an AnnotationGen[] ...
					// List<AnnotationGen> mutable = makeMutableVersion(immutableArray);
					// ... then add these to any we already know about
					for (AnnotationGen anAnnotation : annos) {
						param_annotations[j].add(anAnnotation);
					}
				}
			}
		}
		if (paramAnnVisAttr != null) {
			removeAttribute(paramAnnVisAttr);
		}
		if (paramAnnInvisAttr != null) {
			removeAttribute(paramAnnInvisAttr);
		}
		haveUnpackedParameterAnnotations = true;
	}

	private List /* AnnotationGen */<AnnotationGen> makeMutableVersion(AnnotationGen[] mutableArray) {
		List<AnnotationGen> result = new ArrayList<>();
		for (AnnotationGen annotationGen : mutableArray) {
			result.add(new AnnotationGen(annotationGen, getConstantPool(), false));
		}
		return result;
	}

	public void addParameterAnnotation(int parameterIndex, AnnotationGen annotation) {
		ensureExistingParameterAnnotationsUnpacked();
		if (!hasParameterAnnotations) {
			param_annotations = new List[parameterTypes.length];
			hasParameterAnnotations = true;
		}
		List<AnnotationGen> existingAnnotations = param_annotations[parameterIndex];
		if (existingAnnotations != null) {
			existingAnnotations.add(annotation);
		} else {
			List<AnnotationGen> l = new ArrayList<>();
			l.add(annotation);
			param_annotations[parameterIndex] = l;
		}
	}
}
