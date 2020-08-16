package org.aspectj.apache.bcel;

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

/**
 * Exception constants.
 *
 * @version $Id: ExceptionConstants.java,v 1.5 2009/09/14 20:29:10 aclement Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase">E. Haase</A>
 */
@SuppressWarnings("rawtypes")
public interface ExceptionConstants {
	/**
	 * The mother of all exceptions
	 */
	Class<Throwable> THROWABLE = Throwable.class;

	/**
	 * Super class of any run-time exception
	 */
	Class<RuntimeException> RUNTIME_EXCEPTION = RuntimeException.class;

	/**
	 * Super class of any linking exception (aka Linkage Error)
	 */
	Class<LinkageError> LINKING_EXCEPTION = LinkageError.class;

	/**
	 * Linking Exceptions
	 */
	Class<ClassCircularityError> CLASS_CIRCULARITY_ERROR = ClassCircularityError.class;
	Class<ClassFormatError> CLASS_FORMAT_ERROR = ClassFormatError.class;
	Class<ExceptionInInitializerError> EXCEPTION_IN_INITIALIZER_ERROR = ExceptionInInitializerError.class;
	Class<IncompatibleClassChangeError> INCOMPATIBLE_CLASS_CHANGE_ERROR = IncompatibleClassChangeError.class;
	Class<AbstractMethodError> ABSTRACT_METHOD_ERROR = AbstractMethodError.class;
	Class<IllegalAccessError> ILLEGAL_ACCESS_ERROR = IllegalAccessError.class;
	Class<InstantiationError> INSTANTIATION_ERROR = InstantiationError.class;
	Class<NoSuchFieldError> NO_SUCH_FIELD_ERROR = NoSuchFieldError.class;
	Class<NoSuchMethodError> NO_SUCH_METHOD_ERROR = NoSuchMethodError.class;
	Class<NoClassDefFoundError> NO_CLASS_DEF_FOUND_ERROR = NoClassDefFoundError.class;
	Class<UnsatisfiedLinkError> UNSATISFIED_LINK_ERROR = UnsatisfiedLinkError.class;
	Class<VerifyError> VERIFY_ERROR = VerifyError.class;

	/* UnsupportedClassVersionError is new in JDK 1.2 */
	// public static final Class UnsupportedClassVersionError = UnsupportedClassVersionError.class;
	/**
	 * Run-Time Exceptions
	 */
	Class<NullPointerException> NULL_POINTER_EXCEPTION = NullPointerException.class;
	Class<ArrayIndexOutOfBoundsException> ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = ArrayIndexOutOfBoundsException.class;
	Class<ArithmeticException> ARITHMETIC_EXCEPTION = ArithmeticException.class;
	Class<NegativeArraySizeException> NEGATIVE_ARRAY_SIZE_EXCEPTION = NegativeArraySizeException.class;
	Class<ClassCastException> CLASS_CAST_EXCEPTION = ClassCastException.class;
	Class<IllegalMonitorStateException> ILLEGAL_MONITOR_STATE = IllegalMonitorStateException.class;

	/**
	 * Pre-defined exception arrays according to chapters 5.1-5.4 of the Java Virtual Machine Specification
	 */
	Class[] EXCS_CLASS_AND_INTERFACE_RESOLUTION = { NO_CLASS_DEF_FOUND_ERROR, CLASS_FORMAT_ERROR, VERIFY_ERROR,
			ABSTRACT_METHOD_ERROR, EXCEPTION_IN_INITIALIZER_ERROR, ILLEGAL_ACCESS_ERROR }; // Chapter 5.1
	Class[] EXCS_CLASS_AND_INTERFACE_RESOLUTION_MULTIANEWARRAY = { NO_CLASS_DEF_FOUND_ERROR,
			CLASS_FORMAT_ERROR, VERIFY_ERROR, ABSTRACT_METHOD_ERROR, EXCEPTION_IN_INITIALIZER_ERROR, ILLEGAL_ACCESS_ERROR,
			NEGATIVE_ARRAY_SIZE_EXCEPTION, ILLEGAL_ACCESS_ERROR };
	Class[] EXCS_CLASS_AND_INTERFACE_RESOLUTION_ANEWARRAY = { NO_CLASS_DEF_FOUND_ERROR, CLASS_FORMAT_ERROR,
			VERIFY_ERROR, ABSTRACT_METHOD_ERROR, EXCEPTION_IN_INITIALIZER_ERROR, ILLEGAL_ACCESS_ERROR,
			NEGATIVE_ARRAY_SIZE_EXCEPTION }; // Chapter 5.1
	Class[] EXCS_CLASS_AND_INTERFACE_RESOLUTION_CHECKCAST = { NO_CLASS_DEF_FOUND_ERROR, CLASS_FORMAT_ERROR,
			VERIFY_ERROR, ABSTRACT_METHOD_ERROR, EXCEPTION_IN_INITIALIZER_ERROR, ILLEGAL_ACCESS_ERROR, CLASS_CAST_EXCEPTION }; // Chapter
	// 5.1
	Class[] EXCS_CLASS_AND_INTERFACE_RESOLUTION_FOR_ALLOCATIONS = { NO_CLASS_DEF_FOUND_ERROR,
			CLASS_FORMAT_ERROR, VERIFY_ERROR, ABSTRACT_METHOD_ERROR, EXCEPTION_IN_INITIALIZER_ERROR, ILLEGAL_ACCESS_ERROR,
			INSTANTIATION_ERROR, ILLEGAL_ACCESS_ERROR };

	Class[] EXCS_FIELD_AND_METHOD_RESOLUTION = { NO_SUCH_FIELD_ERROR, ILLEGAL_ACCESS_ERROR,
			NO_SUCH_METHOD_ERROR }; // Chapter 5.2

	Class[] EXCS_FIELD_AND_METHOD_RESOLUTION_GETFIELD_PUTFIELD = { NO_SUCH_FIELD_ERROR, ILLEGAL_ACCESS_ERROR,
			NO_SUCH_METHOD_ERROR, INCOMPATIBLE_CLASS_CHANGE_ERROR, NULL_POINTER_EXCEPTION };

	Class[] EXCS_FIELD_AND_METHOD_RESOLUTION_GETSTATIC_PUTSTATIC = { NO_SUCH_FIELD_ERROR, ILLEGAL_ACCESS_ERROR,
			NO_SUCH_METHOD_ERROR, INCOMPATIBLE_CLASS_CHANGE_ERROR };

	Class[] EXCS_INTERFACE_METHOD_RESOLUTION_INVOKEINTERFACE = { INCOMPATIBLE_CLASS_CHANGE_ERROR,
			ILLEGAL_ACCESS_ERROR, ABSTRACT_METHOD_ERROR, UNSATISFIED_LINK_ERROR };
	Class[] EXCS_INTERFACE_METHOD_RESOLUTION_INVOKESPECIAL_INVOKEVIRTUAL = { INCOMPATIBLE_CLASS_CHANGE_ERROR,
			NULL_POINTER_EXCEPTION, ABSTRACT_METHOD_ERROR, UNSATISFIED_LINK_ERROR };
//	public static final Class[] EXCS_INVOKEDYNAMIC = { BOOTSTRAP_METHOD_ERROR};

	Class[] EXCS_INTERFACE_METHOD_RESOLUTION_INVOKESTATIC = { INCOMPATIBLE_CLASS_CHANGE_ERROR,
			UNSATISFIED_LINK_ERROR };

	Class[] EXCS_INTERFACE_METHOD_RESOLUTION = new Class[0]; // Chapter 5.3 (as below)
	Class[] EXCS_STRING_RESOLUTION = new Class[0];
	// Chapter 5.4 (no errors but the ones that _always_ could happen! How stupid.)

	Class[] EXCS_ARRAY_EXCEPTION = { NULL_POINTER_EXCEPTION, ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION };

}
