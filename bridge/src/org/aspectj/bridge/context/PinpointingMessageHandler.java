/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.bridge.context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.IMessage.Kind;

/**
 * @author colyer
 * Facade for an IMessageHandler
 * Extends message with details of exactly what the compiler / weaver was doing at the 
 * time. Use the -Xdev:Pinpoint option to turn this facility on.
 */
public class PinpointingMessageHandler implements IMessageHandler {

	private IMessageHandler delegate;
	
	public PinpointingMessageHandler(IMessageHandler delegate) {
		this.delegate = delegate;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IMessageHandler#handleMessage(org.aspectj.bridge.IMessage)
	 */
	public boolean handleMessage(IMessage message) throws AbortException {
		if (!isIgnoring(message.getKind())) {
			MessageIssued ex = new MessageIssued();
			ex.fillInStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			StringBuffer sb = new StringBuffer();
			sb.append(CompilationAndWeavingContext.getCurrentContext());
			sb.append(sw.toString());
			IMessage pinpointedMessage = new PinpointedMessage(message,sb.toString());
			return delegate.handleMessage(pinpointedMessage);
		} else {
			return delegate.handleMessage(message);
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IMessageHandler#isIgnoring(org.aspectj.bridge.IMessage.Kind)
	 */
	public boolean isIgnoring(Kind kind) {
		return delegate.isIgnoring(kind);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IMessageHandler#dontIgnore(org.aspectj.bridge.IMessage.Kind)
	 */
	public void dontIgnore(Kind kind) {
		delegate.dontIgnore(kind);
	}


	/* (non-Javadoc)
	 * @see org.aspectj.bridge.IMessageHandler#ignore(org.aspectj.bridge.IMessage.Kind)
	 */
	public void ignore(Kind kind) {
		delegate.ignore(kind);
	}
	
	private static class PinpointedMessage implements IMessage {

		private IMessage delegate;
		private String message;
		
		public PinpointedMessage(IMessage delegate, String pinpoint) {
			this.delegate = delegate;
			this.message = delegate.getMessage() + "\n" + pinpoint;
		}
		
		public String getMessage() { return this.message; }
		public Kind getKind() { return delegate.getKind();}
		public boolean isError() { return delegate.isError(); }
		public boolean isWarning() { return delegate.isWarning();}
		public boolean isDebug() { return delegate.isDebug();}
		public boolean isInfo() { return delegate.isInfo();}
		public boolean isAbort() { return delegate.isAbort();}
		public boolean isTaskTag() { return delegate.isTaskTag();}
		public boolean isFailed() { return delegate.isFailed();}
		public boolean getDeclared() { return delegate.getDeclared(); }
		public int getID() { return delegate.getID();}
		public int getSourceStart() { return delegate.getSourceStart();}
		public int getSourceEnd() { return delegate.getSourceEnd();}
		public Throwable getThrown() { return delegate.getThrown();}
		public ISourceLocation getSourceLocation() { return delegate.getSourceLocation();}
		public String getDetails() { return delegate.getDetails();}
		public List getExtraSourceLocations() { return delegate.getExtraSourceLocations();}
	}
	
   private static class MessageIssued extends RuntimeException {
 	private static final long serialVersionUID = 1L;

		public String getMessage() {
    		return "message issued...";
    	}
    }

}
