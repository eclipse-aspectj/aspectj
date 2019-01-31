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


package org.aspectj.testing.run;

import java.util.Enumeration;
import java.util.Hashtable;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;

/**
 * Run IRun, setting status and invoking listeners
 * for simple and nested runs.
 * <p>
 * This manages baseline IRun status reporting: 
 * any throwables are caught and reported, and
 * the status is started before running and 
 * (if not already completed) completed after.
 * <p>
 * This runs any IRunListeners specified directly in the 
 * run*(..., IRunListener) methods
 * as well as any specified indirectly by registering listeners per-type
 * in {@link registerListener(Class, IRunListener)} 
 * <p>
 * For correct handling of nested runs, this sets up
 * status parent/child relationships.
 * It uses the child result object supplied directly in the
 * runChild(..., IRunStatus childStatus,..) methods,
 * or (if that is null) one obtained from the child IRun itself, 
 * or (if that is null) a generic IRunStatus.  
 * <p>
 * For IRunIterator, this uses IteratorWrapper to wrap the 
 * iterator as an IRun.  Runner and IteratorWrapper coordinate
 * to handle fast-fail (aborting further iteration when an IRun fails).
 * The IRunIterator itself may specify fast-fail by returning true
 * from {@link IRunIterator#abortOnFailure()}, or clients can 
 * register IRunIterator by Object or type for fast-failure using
 * {@link registerFastFailIterator(IRunIterator)} or 
 * {@link registerFastFailIterator(Class)}.
 * This also ensures that 
 * {@link IRunIterator#iterationCompleted()} is
 * called after the iteration process has completed.
 */
public class Runner {
    // XXX need to consider wiring in a logger - sigh

    private static final IMessage FAIL_NORUN
        = MessageUtil.fail("Null IRun parameter to Runner.run(IRun..)");
//    private static final IMessage FAIL_NORUN_ITERATOR
//        = MessageUtil.fail("Null IRunterator parameter to Runner.run(IRunterator...)");
    
    public Runner() {
    }
    
    /**
     * Do the run, setting run status, invoking
     * listener, and aborting as necessary.  
     * If the run is null, the status is
     * updated, but the listener is never run.  
     * If the listener is null, then the runner does a lookup
     * for the listeners of this run type.
     * Any exceptions thrown by the listener(s) are added
     * to the status messages and processing continues. 
     * unless the status is aborted.
     * The status will be completed when this method completes.
     * @param run the IRun to run - if null, issue a FAIL message
     * to that effect in the result.
     * @throws IllegalArgumentException if status is null
     * @return boolean result returned from IRun 
     * or false if IRun did not complete normally
     * or status.runResult() if aborted.
     */
    /* XXX later permit null status
     * If the status is null, this tries to complete
     * the run without a status.  It ignores exceptions
     * from the listeners, but does not catch any from the run.
     */
    public boolean run(IRun run, IRunStatus status, 
                             IRunListener listener) {
        return run(run, status, listener, (Class) null);
    }  
                                                 
    public boolean run(IRun run, IRunStatus status, 
                             IRunListener listener, Class exceptionClass) {                                
        if (!precheck(run, status)) {
            return false;
        }
        RunListeners listeners = getListeners(run, listener);
        return runPrivate(run, status, listeners, exceptionClass);
                             
    }

    /**
     * Run child of parent, handling interceptor registration, etc.
     * @throws IllegalArgumentException if parent or child status is null
     */
    public boolean runChild(IRun child, 
                                 IRunStatus parentStatus, 
                                 IRunStatus childStatus,
                                 IRunListener listener) {
        return runChild(child, parentStatus, childStatus, listener, null);
    }
    
    /**
     * Run child of parent, handling interceptor registration, etc.
     * If the child run is supposed to throw an exception, then pass
     * the exception class.
     * After this returns, the childStatus is guaranteed to be completed.
     * If an unexpected exception is thrown, an ABORT message 
     * is passed to childStatus.
     * @param parentStatus the IRunStatus for the parent - must not be null
     * @param childStatus the IRunStatus for the child - default will be created if null
     * @param exceptionClass the Class of any expected exception
     * @throws IllegalArgumentException if parent status is null
     */
    public boolean runChild(IRun child, 
                                 IRunStatus parentStatus, 
                                 IRunStatus childStatus,
                                 IRunListener listener,
                                 Class exceptionClass) {
        if (!precheck(child, parentStatus)) {
            return false;
        }
        if (null == childStatus) {
            childStatus = new RunStatus(new MessageHandler(), this);
        }
        installChildStatus(child, parentStatus, childStatus);
        if (!precheck(child, childStatus)) {
            return false;
        }
        RunListeners listeners = getListeners(child, listener);
        if (null != listeners) {
            try {
                listeners.addingChild(parentStatus, childStatus);
            } catch (Throwable t) {
                String m = "RunListenerI.addingChild(..) exception " + listeners;
                parentStatus.handleMessage(MessageUtil.abort(m, t)); // XXX                
            }
        }
        boolean result = false;
        try {
            result = runPrivate(child, childStatus, listeners, exceptionClass);
        } finally {
            if (!childStatus.isCompleted()) {
                childStatus.finish(result ? IRunStatus.PASS : IRunStatus.FAIL);
                childStatus.handleMessage(MessageUtil.debug("XXX parent runner set completion"));
            }
        }
        boolean childResult = childStatus.runResult();
        if (childResult != result) {
            childStatus.handleMessage(MessageUtil.info("childResult != result=" + result));
        }
        return childResult;        
    }

    public IRunStatus makeChildStatus(IRun run, IRunStatus parent, IMessageHolder handler) {
        return installChildStatus(run, parent, new RunStatus(handler, this));
    }
    
    /**
     * Setup the child status before running
     * @param run the IRun of the child process (not null)
     * @param parent the IRunStatus parent of the child status (not null)
     * @param child the IRunStatus to install - if null, a generic one is created
     * @return the IRunStatus child status (as passed in or created if null)
     */
    public IRunStatus installChildStatus(IRun run, IRunStatus parent, IRunStatus child) {
        if (null == parent) {
            throw new IllegalArgumentException("null parent");
        }
        if (null == child) {
            child = new RunStatus(new MessageHandler(), this);
        }
        child.setIdentifier(run); // XXX leak if ditching run...
        parent.addChild(child);
        return child;
    }
                  
    /**
     * Do a run by running all the subrunners provided by the iterator,
     * creating a new child status of status for each.
     * If the iterator is null, the result is
     * updated, but the interceptor is never run.  
     * @param iterator the IRunteratorI for all the IRun to run
     * - if null, abort (if not completed) or message to status.
     * @throws IllegalArgumentException if status is null
     */
    public boolean runIterator(IRunIterator iterator, IRunStatus status,
                             IRunListener listener) {
        LangUtil.throwIaxIfNull(status, "status");                       
        if (status.aborted()) {
            return status.runResult();
        }
        if (null == iterator) {
            if (!status.isCompleted()) {
                status.abort(IRunStatus.ABORT_NORUN);
            } else {
                status.handleMessage(FAIL_NORUN);
            }                       
            return false;
        }
        IRun wrapped = wrap(iterator, listener);
        return run(wrapped, status, listener);
    }

    /**
     * Signal whether to abort on failure for this run and iterator,
     * based on the iterator and any fast-fail registrations.
     * @param iterator the IRunIterator to stop running if this returns true
     * @param run the IRun that failed
     * @return true to halt further iterations
     */
    private boolean abortOnFailure(IRunIterator iterator, IRun run) {
        return ((null == iterator) || iterator.abortOnFailure()); // XxX not complete
    }


    /** 
     * Tell Runner to stop iterating over IRun for an IRunIterator
     * if any IRun.run(IRunStatus) fails.
     * This overrides a false result from IRunIterator.abortOnFailure().
     * @param iterator the IRunIterator to fast-fail - ignored if null.
     * @see IRunIterator#abortOnFailure()
     */
    public void registerFastFailIterator(IRunIterator iterator) { // XXX unimplemented
        throw new UnsupportedOperationException("ignoring " + iterator);
    }

    /** 
     * Tell Runner to stop iterating over IRun for an IRunIterator
     * if any IRun.run(IRunStatus) fails, 
     * if the IRunIterator is assignable to iteratorType.
     * This overrides a false result from IRunIterator.abortOnFailure().
     * @param iteratorType the IRunIterator Class to fast-fail
     *              - ignored if null, must be assignable to IRunIterator
     * @see IRunIterator#abortOnFailure()
     */
    public void registerFastFailIteratorTypes(Class iteratorType) { // XXX unimplemented
        throw new UnsupportedOperationException("ignoring " + iteratorType);
    }
    
    /**
     * Register a run listener for any run assignable to type.
     * @throws IllegalArgumentException if either is null
     */
    public void registerListener(Class type, IRunListener listener) { // XXX unregister
        if (null == type) {
            throw new IllegalArgumentException("null type");
        }
        if (null == listener) {
            throw new IllegalArgumentException("null listener");
        }
        ClassListeners.addListener(type, listener);
    }
    
    /**
     * Wrap this IRunIterator.
     * This wrapper takes care of calling 
     * <code>iterator.iterationCompleted()</code> when done, so
     * after running this, clients should not invoker IRunIterator 
     * methods on this iterator.
     * @return the iterator wrapped as a single IRun
     */
    public IRun wrap(IRunIterator iterator, IRunListener listener) {
        LangUtil.throwIaxIfNull(iterator, "iterator");
        return new IteratorWrapper(this, iterator, listener);
    }                             
        
    /**
     * This gets any listeners registered for the run
     * based on the class of the run (or of the wrapped iterator,
     * if the run is an IteratorWrapper).
     * @return a listener with all registered listener and parm,
     * or null if parm is null and there are no listeners
     */
    protected RunListeners getListeners(IRun run, IRunListener listener) {        
        if (null == run) {
            throw new IllegalArgumentException("null run");
        }
        Class runClass = run.getClass();
        if (runClass == IteratorWrapper.class) {
            IRunIterator it = ((IteratorWrapper) run).iterator;
            if (null != it) {
                runClass = it.getClass();
            }
            // fyi expecting: listener == ((IteratorWrapper) run).listener
        }
        RunListeners listeners = ClassListeners.getListeners(runClass);
        if (null != listener) {
            listeners.addListener(listener);
        }
        return listeners; // XXX implement registration
    }

    /** check status and run before running */    
    private boolean precheck(IRun run, IRunStatus status) {
        if (null == status) {
            throw new IllegalArgumentException("null status");
        }
        // check abort request coming in
        if (status.aborted()) {
            return status.runResult();
        } else if (status.isCompleted()) {
            throw new IllegalStateException("status completed before starting");
        }
        
        if (!status.started()) {
            status.start();
        }
        return true;
    }
        
    /** This assumes precheck has happened and listeners have been obtained */
    private boolean runPrivate(IRun run, IRunStatus status, 
                             RunListeners listeners, Class exceptionClass) {
        IRunListener listener = listeners;                               
        if (null != listener) {
            try {
                listener.runStarting(status);
            } catch (Throwable t) {
                String m = listener + " RunListenerI.runStarting(..) exception";
                IMessage mssg = new Message(m, IMessage.WARNING, t, null); 
                // XXX need IMessage.EXCEPTION - WARNING is ambiguous
                status.handleMessage(mssg);
            }
        }
        // listener can set abort request
        if (status.aborted()) {
            return status.runResult();
        }
        if (null == run) {
            if (!status.isCompleted()) {
                status.abort(IRunStatus.ABORT_NORUN);
            } else {
                status.handleMessage(MessageUtil.FAIL_INCOMPLETE);
            }
            return false;
        } 
        
        boolean result = false;
        try {
            result = run.run(status);
            if (!status.isCompleted()) {                
                status.finish(result?IRunStatus.PASS: IRunStatus.FAIL); 
            }
        } catch (Throwable thrown) {
            if (!status.isCompleted()) {
                status.thrown(thrown);
            } else {
                String m = "run status completed but run threw exception";
                status.handleMessage(MessageUtil.abort(m, thrown));
                result = false;
            }
        } finally {
            if (!status.isCompleted()) {
                // XXX should never get here... - hides errors to set result
                status.finish(result ? IRunStatus.PASS : IRunStatus.FAIL);
                if (!status.isCompleted()) {
                    status.handleMessage(MessageUtil.debug("child set of status failed"));
                }
            }
        }
        
        
        try {
            if ((null != listener) && !status.aborted()) {
                listener.runCompleted(status);
            }
        } catch (Throwable t) {
            String m = listener + " RunListenerI.runCompleted(..) exception";
            status.handleMessage(MessageUtil.abort(m, t));
        }
        return result;
    }

    //---------------------------------- nested classes
    /** 
     * Wrap an IRunIterator as a IRun, coordinating 
     * fast-fail IRunIterator and Runner
     */ 
    public static class IteratorWrapper implements IRun {
        final Runner runner;
        public final IRunIterator iterator;
        final IRunListener listener;
        
        public IteratorWrapper(Runner runner, IRunIterator iterator, IRunListener listener) {
            LangUtil.throwIaxIfNull(iterator, "iterator");
            LangUtil.throwIaxIfNull(runner, "runner");
            this.runner = runner;
            this.iterator = iterator;
            this.listener = listener;
        }

        /** @return null */
        public IRunStatus makeStatus() { 
            return null; 
        }
        
        /** @return true unless one failed */
        public boolean run(IRunStatus status) {
            boolean result = true;
            try {
                int i = 0;
                int numMessages = status.numMessages(IMessage.FAIL, IMessageHolder.ORGREATER);
                while (iterator.hasNextRun()) {
                    IRun run = iterator.nextRun((IMessageHandler) status, runner);
                    if (null == run) {
                        MessageUtil.debug(status,  "null run " + i + " from " + iterator);
                        continue;
                    }
    
                    int newMessages = status.numMessages(IMessage.FAIL, IMessageHolder.ORGREATER);
                    if (newMessages > numMessages) {
                        numMessages = newMessages;
                        String m = "run " + i + " from " + iterator
                            + " not invoked, due to fail(+) message(s) ";
                        MessageUtil.debug(status,  m);
                        continue;
                    }
                    RunStatus childStatus = null; // let runChild create
                    if (!runner.runChild(run, status, childStatus, listener)) {
                        if (result) {
                            result = false;
                        }
                        if (iterator.abortOnFailure() 
                            || runner.abortOnFailure(iterator, run)) {
                            break;
                        }
                    }
                    i++;
                }
                return result;
            } finally {
                iterator.iterationCompleted();
            }
        }

        /** @return iterator, clipped to 75 char */
        public String toString() {
            String s = "" + iterator;
            if (s.length() > 75) {
                s = s.substring(0, 72) + "...";
            }
            return s;
        }
    }
    
    /** per-class collection of IRun */
    static class ClassListeners extends RunListeners {        
        private static final Hashtable known = new Hashtable();
        
        static RunListeners getListeners(Class c) { // XXX costly and stupid
            Enumeration keys = known.keys();
            RunListeners many = new RunListeners();
            while (keys.hasMoreElements()) {
                Class kc = (Class) keys.nextElement();
                if (kc.isAssignableFrom(c)) {
                    many.addListener((IRunListener) known.get(kc));
                }
            }
            return many;
        }
        
        private static RunListeners makeListeners(Class c) {
            RunListeners result = (ClassListeners) known.get(c);
            if (null == result) {
                result = new ClassListeners(c);
                known.put(c, result);
            }
            return result;
        }
        
        static void addListener(Class c, IRunListener listener) {
            if (null == listener) {
                throw new IllegalArgumentException("null listener");
            }
            if (null == c) {
                c = IRun.class;
            }
            makeListeners(c).addListener(listener);
        }
        
        Class clazz;
        
        ClassListeners(Class clazz) {
            this.clazz = clazz;
        }
        
        public String toString() {
            return clazz.getName() + " ClassListeners: " + super.toString();
        }
    }       
}
