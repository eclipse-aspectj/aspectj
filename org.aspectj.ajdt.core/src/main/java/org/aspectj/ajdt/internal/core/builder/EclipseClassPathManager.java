/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.bcel.ClassPathManager;
import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 * @author colyer
 *
 * Provide a type lookup environment for the weaver, without having to convert
 * the various eclipse paths into their external form.
 */
public class EclipseClassPathManager extends ClassPathManager {
	
	private INameEnvironment nameEnv;
	
	public EclipseClassPathManager(INameEnvironment env) {
		this.nameEnv = env;
	}
	
	// class path manager will be used by weaver across multiple compiles,
	// whereas a name environment may be constructed per-compile.
	public void setNameEnvironment(INameEnvironment env) {
		this.nameEnv = env;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.bcel.ClassPathManager#addPath(java.lang.String, org.aspectj.bridge.IMessageHandler)
	 */
	public void addPath(String name, IMessageHandler handler) {
		throw new UnsupportedOperationException("Can't add paths to an *Eclipse*ClassPathManager.");
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.bcel.ClassPathManager#find(org.aspectj.weaver.UnresolvedType)
	 */
	public ClassFile find(UnresolvedType type) {
		ClassFile cf = null;
		String name = type.getName();
		if (name.endsWith(".class")) {
			name = name.substring(0,name.length() - ".class".length());
		}
		char[][] cname = CharOperation.splitOn('.',name.toCharArray());
		// TODO [j9] passing null client/module here...
		NameEnvironmentAnswer answer = nameEnv.findType(cname);
		if (answer == null || !answer.isBinaryType()) {
			return null;
		} else {
			IBinaryType binType = answer.getBinaryType();
			// XXX - but better than the alternative hacks
			if (binType instanceof ClassFileReader) {
				ClassFileReader cfr = (ClassFileReader) binType;
				cf = new ClassFileReaderBackedClassFile(cfr);
			} else {
				throw new IllegalArgumentException(
						"I'm only geared up to handle ClassFileReaders, and you gave me a " + 
						binType.getClass().getName());
			}
			return cf;
		} 
	}
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.bcel.ClassPathManager#getAllClassFiles()
	 */
	public List getAllClassFiles() {
		throw new UnsupportedOperationException("I don't implement getAllClassFiles()");
		//return Collections.EMPTY_LIST;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("EclipseClassPathManager: ");
		buf.append(nameEnv.toString());
		return buf.toString();
	}
	
	private class ClassFileReaderBackedClassFile extends ClassPathManager.ClassFile {

		private ClassFileReader source;
		private InputStream is;
		
		public ClassFileReaderBackedClassFile(ClassFileReader cfr) {
			source = cfr;
		}
		
		/* (non-Javadoc)
		 * @see org.aspectj.weaver.bcel.ClassPathManager.ClassFile#getInputStream()
		 */
		public InputStream getInputStream() throws IOException {
			is = new ByteArrayInputStream(source.getReferenceBytes());
			return is;
		}
		
		public void close() {
			try {
				if (is!=null) is.close();
			} catch (IOException e) {
				// Should never happen !
				e.printStackTrace();
			}
		} 

		/* (non-Javadoc)
		 * @see org.aspectj.weaver.bcel.ClassPathManager.ClassFile#getPath()
		 */
		public String getPath() {
			return new String(source.getFileName());
		}		
	}
}
