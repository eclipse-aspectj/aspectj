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

package org.aspectj.runtime.reflect;

import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

class JoinPointImpl implements ProceedingJoinPoint {
	static class StaticPartImpl implements JoinPoint.StaticPart {
		String kind;
		Signature signature;
		SourceLocation sourceLocation;
		private int id;

		public StaticPartImpl(int id, String kind, Signature signature, SourceLocation sourceLocation) {
			this.kind = kind;
			this.signature = signature;
			this.sourceLocation = sourceLocation;
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public String getKind() {
			return kind;
		}

		public Signature getSignature() {
			return signature;
		}

		public SourceLocation getSourceLocation() {
			return sourceLocation;
		}

		String toString(StringMaker sm) {
			StringBuffer buf = new StringBuffer();
			buf.append(sm.makeKindName(getKind()));
			buf.append("(");
			buf.append(((SignatureImpl) getSignature()).toString(sm));
			buf.append(")");
			return buf.toString();
		}

		public final String toString() {
			return toString(StringMaker.middleStringMaker);
		}

		public final String toShortString() {
			return toString(StringMaker.shortStringMaker);
		}

		public final String toLongString() {
			return toString(StringMaker.longStringMaker);
		}
	}

	static class EnclosingStaticPartImpl extends StaticPartImpl implements EnclosingStaticPart {
		public EnclosingStaticPartImpl(int count, String kind, Signature signature, SourceLocation sourceLocation) {
			super(count, kind, signature, sourceLocation);
		}
	}

	Object _this;
	Object target;
	Object[] args;
	org.aspectj.lang.JoinPoint.StaticPart staticPart;

	public JoinPointImpl(org.aspectj.lang.JoinPoint.StaticPart staticPart, Object _this, Object target, Object[] args) {
		this.staticPart = staticPart;
		this._this = _this;
		this.target = target;
		this.args = args;
	}

	public Object getThis() {
		return _this;
	}

	public Object getTarget() {
		return target;
	}

	public Object[] getArgs() {
		if (args == null) {
			args = new Object[0];
		}
		Object[] argsCopy = new Object[args.length];
		System.arraycopy(args, 0, argsCopy, 0, args.length);
		return argsCopy;
	}

	public org.aspectj.lang.JoinPoint.StaticPart getStaticPart() {
		return staticPart;
	}

	public String getKind() {
		return staticPart.getKind();
	}

	public Signature getSignature() {
		return staticPart.getSignature();
	}

	public SourceLocation getSourceLocation() {
		return staticPart.getSourceLocation();
	}

	public final String toString() {
		return staticPart.toString();
	}

	public final String toShortString() {
		return staticPart.toShortString();
	}

	public final String toLongString() {
		return staticPart.toLongString();
	}

	// To proceed we need a closure to proceed on. Generated code
	// will either be using arc or arcs but not both. arcs being non-null
	// indicates it is in use (even if an empty stack)
	private AroundClosure arc = null;	
	private Stack<AroundClosure> arcs = null;

	public void set$AroundClosure(AroundClosure arc) {
		this.arc = arc;
	}

 	public void stack$AroundClosure(AroundClosure arc) {
		// If input parameter arc is null this is the 'unlink' call from AroundClosure
		if (arcs == null) {
			arcs = new Stack<>();
		}
		if (arc==null) {
			this.arcs.pop();
		} else {
			this.arcs.push(arc);
		}
 	}

	public Object proceed() throws Throwable {
		// when called from a before advice, but be a no-op
		if (arcs == null) {
			if (arc == null) {
				return null;
			} else {
				return arc.run(arc.getState());
			}
		} else {
			return arcs.peek().run(arcs.peek().getState());			
		}
	}

	public Object proceed(Object[] adviceBindings) throws Throwable {
		// when called from a before advice, but be a no-op
		AroundClosure ac = null;
		if (arcs == null) {
			ac = arc;
		} else {
			ac = arcs.peek();
		}
		
		if (ac == null) {
			return null;
		} else {
			// Based on the bit flags in the AroundClosure we can determine what to
			// expect in the adviceBindings array. We may or may not be expecting
			// the first value to be a new this or a new target... (see pr126167)
			int flags = ac.getFlags();
			boolean unset = (flags & 0x100000) != 0;
			boolean thisTargetTheSame = (flags & 0x010000) != 0;
			boolean hasThis = (flags & 0x001000) != 0;
			boolean bindsThis = (flags & 0x000100) != 0;
			boolean hasTarget = (flags & 0x000010) != 0;
			boolean bindsTarget = (flags & 0x000001) != 0;
	
			// state is always consistent with caller?,callee?,formals...,jp
			Object[] state = ac.getState();
	
			// these next two numbers can differ because some join points have a this and
			// target that are the same (eg. call) - and yet you can bind this and target
			// separately.
	
			// In the state array, [0] may be this, [1] may be target
	
			int firstArgumentIndexIntoAdviceBindings = 0;
			int firstArgumentIndexIntoState = 0;
			firstArgumentIndexIntoState += (hasThis ? 1 : 0);
			firstArgumentIndexIntoState += (hasTarget && !thisTargetTheSame ? 1 : 0);
			if (hasThis) {
				if (bindsThis) {
					// replace [0] (this)
					firstArgumentIndexIntoAdviceBindings = 1;
					state[0] = adviceBindings[0];
				} else {
					// leave state[0] alone, its OK
				}
			}
			if (hasTarget) {
				if (bindsTarget) {
					if (thisTargetTheSame) {
						// this and target are the same so replace state[0]
						firstArgumentIndexIntoAdviceBindings = 1 + (bindsThis ? 1 : 0);
						state[0] = adviceBindings[(bindsThis ? 1 : 0)];
					} else {
						// need to replace the target, and it is different to this, whether
						// that means replacing state[0] or state[1] depends on whether
						// the join point has a this 
						
						// This previous variant doesn't seem to cope with only binding target at a joinpoint
						// which has both this and target. It forces you to supply this even if you didn't bind
						// it.
	//						firstArgumentIndexIntoAdviceBindings = (hasThis ? 1 : 0) + 1;
	//						state[hasThis ? 1 : 0] = adviceBindings[hasThis ? 1 : 0];
						
						int targetPositionInAdviceBindings = (hasThis && bindsThis) ? 1 : 0;
						firstArgumentIndexIntoAdviceBindings = ((hasThis&&bindsThis)?1:0)+((hasTarget&&bindsTarget&&!thisTargetTheSame)?1:0);
						state[hasThis ? 1 : 0] = adviceBindings[targetPositionInAdviceBindings];
					}
				} else {
					// leave state[0]/state[1] alone, they are OK
				}
			}
	
			// copy the rest across
			for (int i = firstArgumentIndexIntoAdviceBindings; i < adviceBindings.length; i++) {
				state[firstArgumentIndexIntoState + (i - firstArgumentIndexIntoAdviceBindings)] = adviceBindings[i];
			}
	
			// old code that did this, didnt allow this/target overriding
			// for (int i = state.length-2; i >= 0; i--) {
			// int formalIndex = (adviceBindings.length - 1) - (state.length-2) + i;
			// if (formalIndex >= 0 && formalIndex < adviceBindings.length) {
			// state[i] = adviceBindings[formalIndex];
			// }
			// }
			return ac.run(state);
		}
	}

}
