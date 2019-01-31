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

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.testing.util.IntRange;
import org.aspectj.testing.util.ObjectChecker;

/**
 * This checks if a run status passes, as follows:
 * <li>state: fail unless completed and not aborted</li>
 * <li>messages: fail if any of type ABORT, FAIL - permit ERROR, WARNING, DEBUG...
 *     (which permits expected compiler errors and warnings) </li>
 * <li>thrown: if type required, fail unless type thrown; 
 *             if type permitted, fail if wrong type thrown</li>
 * <li>result: fail unless no ObjectChecker or it validates
 *              and the result is not IRunStatus.FAIL.</li>
 * <li>otherwise delegates to any subclass doPassed()<li>
 * Client setup the expected and permitted exception classes
 * and the result object checker, and may also subclass to
 * query the IRunStatus more carefully.
 * <p>
 * Note that IRunStatus states can be out of sync with messages,
 * e.g., as underlying components signal ABORT without using abort(...).
 */
public class RunValidator implements IRunValidator {
    /** expect normal completion with any non-null result object,
     *  except that Integer Objects must have value 0 */
    public static final IRunValidator NORMAL
            = new RunValidator(ObjectChecker.ANY_ZERO);
    /** expect normal completion with any non-null result object */
    public static final IRunValidator ORIGINAL_NORMAL
            = new RunValidator(ObjectChecker.ANY);

    /** expect normal completion and Integer result object with value 0 */
    public static final IRunValidator ZERO_STATUS
            = new RunValidator(IntRange.ZERO);

    /** expect finished(IRunStatus.PASS) and no thrown, fail, etc. */
    public static final IRunValidator PASS 
            = new RunValidator(new ObjectChecker() {
                public boolean isValid(Object o) {
                    return (o == IRunStatus.PASS);
                }
            });
            
    /** expect finished(IRunStatus.FAIL) */
    public static final IRunValidator FAIL 
            = new RunValidator(new ObjectChecker() {
                public boolean isValid(Object o) {
                    return (o == IRunStatus.FAIL);
                }
            });

    /** range of status values required for passing */
    private ObjectChecker resultChecker;
    
    // XXX replace two exc. classes with one, plus boolean for how to interpret?
    /** if non-null, passed() permits any thrown assignable to this class */
    private Class permittedExceptionsClass;

    /** if non-null, passed() requires some thrown assignable to this class */
    private Class requiredExceptionsClass;
    
    /** Create result validator that expects a certain int status */
    public RunValidator(ObjectChecker resultChecker) {
       this(resultChecker, null, null);
    }   
     
    /** 
     * Create result validator that passes only when completed abruptly by
     * a Throwable assignable to the specified class.
     * @throws illegalArgumentException if requiredExceptionsClass is not Throwable
     */
    public RunValidator(Class requiredExceptionsClass) { 
        this(null, null, requiredExceptionsClass);
    }

    /**
     * Create a result handler than knows how to evaluate {@link #passed()}.
     * You cannot specify both permitted and required exception class,
     * and any exception class specified must be assignable to throwable.
     * 
     * @param resultChecker  {@link #passed()} will return false if
     * the int status is not accepted by this int validator - if null,
     * any int status result is accepted.
     * @param fastFailErrorClass an Error subclass with a (String) constructor to use to 
     *                            construct and throw Error from fail(String).  If null, then fail(String)
     *                            returns normally.
     * @param permittedExceptionsClass if not null and any exceptions thrown are
     * assignable to this class, {@link #passed()} will not return
     * false as it normally does when exceptions are thrown.
     * @param requiredExceptionsClass if not null,  {@link #passed()} will return false
     * unless some exception was thrown that is assignable to this class.
     * @throws illegalArgumentException if any exception class is not Throwable
     *          or if fast fail class is illegal (can't make String constructor)
     */
    protected RunValidator(
        ObjectChecker resultChecker,
        Class permittedExceptionsClass,
        Class requiredExceptionsClass) {
        init(resultChecker,permittedExceptionsClass, requiredExceptionsClass);
    }

    /** same as init with existing values */
    protected void reset() {
        init(resultChecker, permittedExceptionsClass,
         requiredExceptionsClass);
    }
    
    /** subclasses may use this to re-initialize this for re-use */
    protected void init(
        ObjectChecker resultChecker,
        Class permittedExceptionsClass,
        Class requiredExceptionsClass) {
        this.permittedExceptionsClass = permittedExceptionsClass;
        this.requiredExceptionsClass = requiredExceptionsClass;
        
        if (null != resultChecker) {
            this.resultChecker = resultChecker;
        } else {
            this.resultChecker = IntRange.ANY;
        }

        if (null != permittedExceptionsClass) {
           if (!Throwable.class.isAssignableFrom(permittedExceptionsClass)) {
                String e = "permitted not throwable: " + permittedExceptionsClass;
                throw new IllegalArgumentException(e);
           } 
        }
        if (null != requiredExceptionsClass) {
           if (!Throwable.class.isAssignableFrom(requiredExceptionsClass)) {
                String e = "required not throwable: " + requiredExceptionsClass;
                throw new IllegalArgumentException(e);
           } 
        }
        if ((null != permittedExceptionsClass) 
            && (null != requiredExceptionsClass) ) {
            String e = "define at most one of required or permitted exceptions";
            throw new IllegalArgumentException(e);
        }
    }
    
    /** @return true if this result passes per this validator */
    public final boolean runPassed(IRunStatus result) {
        if (null == result) {
            throw new IllegalArgumentException("null result");
        }
        // After the result has completed, the result is stored.
        if (!result.isCompleted()) {
            return false;
        }
        if (result.aborted()) {
            return false;
        }
        if (null != result.getAbortRequest()) {
            return false;
        }
        Object resultObject = result.getResult();
        if (!resultChecker.isValid(resultObject)) {
            return false;
        }
        if (resultObject == IRunStatus.FAIL) {
             return false;
        }
        // need MessageHandler.getMessage(...)
        if (result.hasAnyMessage(IMessage.FAIL, IMessageHolder.ORGREATER)) {
            return false;
        }
        Throwable thrown = result.getThrown();
        if (null == thrown) {
            if (null != requiredExceptionsClass) {
                return false;
            }
        } else {
            Class c = thrown.getClass();
            // at most one of the ExceptionsClass set
            if (null != requiredExceptionsClass) {
                if (!requiredExceptionsClass.isAssignableFrom(c)) {
                  return false;
                }
            } else if (null != permittedExceptionsClass) {
                if (!permittedExceptionsClass.isAssignableFrom(c)) {
                    return false;
                }
            } else {
                return false;
            }            
        }
        return dopassed();
    }

    /** subclasses implement subclass-specific behavior for passed() here */
    protected boolean dopassed() {
        return true;
    }
}
