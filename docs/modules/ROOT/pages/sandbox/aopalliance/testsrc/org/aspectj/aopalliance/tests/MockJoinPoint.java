/*
 * Created on 07-May-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.aspectj.aopalliance.tests;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;


class MockJoinPoint implements JoinPoint {
		
		private Object me;
		private Signature sig;
		private Object[] args;
		
		public MockJoinPoint(Object me, Signature sig, Object[] args) {
			this.me = me;
			this.sig = sig;
			this.args = args;
		}
		
		public Object[] getArgs() {
			return args;
		}
		public String getKind() {
			return null;
		}
		public Signature getSignature() {
			return sig;
		}
		public SourceLocation getSourceLocation() {
			return null;
		}
		public StaticPart getStaticPart() {
			return null;
		}
		public Object getTarget() {
			return null;
		}
		public Object getThis() {
			return me;
		}
		public String toLongString() {
			return null;
		}
		public String toShortString() {
			return null;
		}
}