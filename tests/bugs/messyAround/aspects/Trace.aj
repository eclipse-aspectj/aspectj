package aspects;

import java.io.*;
import java.util.*;
import org.aspectj.runtime.*;
import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;
import java.lang.reflect.*;

/** Trace is an aspect that traces execution through code.  
 * 
 */
public abstract aspect Trace issingleton()
{   
    // our internal instance
    private static Trace _trace;
    
    // Call depth on trace
    private static final ThreadLocal traceDepths = new ThreadLocal();

    // An object to synchronize on
    protected static final Object  lock = new Object();

    private static final String NL = System.getProperty("line.separator");
    
    // Space indentation increment
    private static final int INDENT = 4;

    // Used for indentation
    private static final byte[] SPACES = new byte[100];
    
    private static boolean traceActive = true;

    static
    {
        Arrays.fill(SPACES,(byte)' ');
    }

       
    /** Trace constructor. Since this aspect is a singleton, we can be
     * assured that only a single instance exists.  
     */
    protected Trace() {_trace = this;}
    
    /**
    * This abstract pointcut indicates what classes we should trace.  Typically
    * you will define this using a within() PCD. We leave that up to concrete aspects.
    */
    protected abstract pointcut lexicalScope();
    
    /**
     * Common scope for all traces - includes lexicalScope
     */
    final pointcut scope() : if(_trace != null && _trace.canTraceJoinpoint(thisJoinPoint)) && lexicalScope() && !within(Trace+);

    /**
     * This pointcut designates tracing constructors within lexicalScope()
     */
    protected final pointcut constructorTrace() : scope() && (call( new(..) ) || execution( new(..)));
    /**
     * This pointcut designates tracing method executions within lexicalScope()
     */
    protected final pointcut methodTrace() : scope() && (call(* *(..)) || execution(* *(..)));

    /**
     * This pointcut designates tracing exception handlers within lexicalScope()
     */
    protected final pointcut handlerTrace(Exception e) : scope() &&  args(e) && handler(Exception+);


    /**
     * This pointcut picks out joinpoints within this aspect that implement
     * the actual tracing.  Since parameters and return values are printed
     * out via implicit or explicit call to Object.toString(), there is the possibility
     * of an execution joinpoint on toString() causing the trace logic
     * to be re-entered. This is undesireable because it makes the trace output
     * difficult to read and adds unecessary overhead. <p>
     * This pointcut is used within a cflowbelow pointcut to prevent recursive
     * trace calls.
     */
    private pointcut internalMethods() : 
        execution( void Trace.trace*(..,(JoinPoint||JoinPoint.StaticPart),..) );

    /**
     * For methods, we use around() advice to capture calls/executions.
     */
    Object around() : methodTrace() && !cflowbelow(internalMethods())
    {
        traceEnter(thisJoinPoint); 
        try
        {
            Object result = proceed();
            traceResult(result,thisJoinPoint);
            return result;
        }
        finally
        {
            traceExit(thisJoinPoint);
        }
    }
    
    /**
     * For Constructors, we use around() advice to capture calls/executions.
     */
    Object around() : constructorTrace() && !cflowbelow(internalMethods())
    {
        traceEnter(thisJoinPoint); 
        try
        {
            return proceed();
        }
        finally
        {
            traceExit(thisJoinPoint);
        }
    }
    

    /**
     * Trace Exceptions that may occur with constructors or methods
     */
    after() throwing(Throwable e): (constructorTrace() || methodTrace()) && !cflowbelow(internalMethods())
    {   
        traceThrowable(e,thisJoinPoint);
    }


    /**
     * Trace Exception handlers entry
     */    
    before(Exception e) : handlerTrace(e) && !cflowbelow(internalMethods())
    {
        traceHandlerEntry(e,thisJoinPointStaticPart);
    }
    /**
     * Trace Exception handlers exit
     */
    after(Exception e) : handlerTrace(e) && !cflowbelow(internalMethods())
    {
        traceHandlerExit();
    }


    /**
     * Subaspects can override this method to log the data as needed. The default
     * mechanism is to log to System.out
     * 
     * Clients should be aware that this method is not synchronized.
     */
    protected void log(String data) {System.out.println(data);}
    
    /**
     * Can be overridden by subaspects to filter what constructors/methods should be 
     * traced at runtime.  This method is always called prior to the log()
     * method. The default always returns true.<p> Note that exceptions thrown
     * by constructors/methods are filtered through this method.
     * @param currentlyExecutingClass The Class that is currently executing.
     * @param signature The signature of the member being traced
     * @param traceType The type of trace entry (see AspectJ doc for the available types)
     */
    protected boolean isTraceable(Class currentlyExecutingClass, CodeSignature signature,String traceType) {return true;}

    /**
     * Can be overridden by subaspects to filter what exception handlers should be 
     * traced at runtime.  This method is always called prior to the log()
     * method. The default always returns false.<p>
     * Note that exception handlers are catch(...){} blocks and are filtered
     * independently from constructor/method calls and execution.
     * @param currentlyExecutingClass The Class that is currently executing.
     * @param signature The signature of the member being traced
     */
    protected boolean isTraceable(Class currentlyExecutingClass, CatchClauseSignature signature) {return false;}
    
    /**
     * Retrieves the signature of the joinpoint and asks if it can be traced
     */
    private boolean canTraceJoinpoint(JoinPoint jp)
    {
        if ( !traceActive ) return false;
        final Signature sig = jp.getSignature();
        final Object o = jp.getThis();  // current object
        Class currentType;
        if ( o == null ) // must be a static
            currentType = jp.getStaticPart().getSourceLocation().getWithinType();
        else
            currentType = o.getClass();
            
        // dispatch the correct filter method
        if ( sig instanceof CodeSignature )            
            return isTraceable(currentType,(CodeSignature)sig,jp.getKind());
        else
            return isTraceable(currentType,(CatchClauseSignature)sig);            
    }
    

    /**
     * This method creates a trace entry line based on information in the
     * supplied join point.
     */
    private void traceEnter(JoinPoint thisJoinPoint)
    { 
   
        // Get the indent level (call depth for current thread * 4).
        int depth = getTraceDepth(INDENT);
    
        Class[] parameterTypes = ((CodeSignature)thisJoinPoint.getSignature()).getParameterTypes();
        String[] parameterNames = ((CodeSignature)thisJoinPoint.getSignature()).getParameterNames();
    
        boolean isCall = thisJoinPoint.getKind().endsWith("call");

        StringBuffer enterPhrase = new StringBuffer(100);
        enterPhrase.append(getSpaces(depth));
        if ( isCall )
            enterPhrase.append("Call ");
        else
            enterPhrase.append("Entering ");
        enterPhrase.append(methodSignature(parameterNames,parameterTypes,thisJoinPoint));
        
//        if ( isCall )
//            enterPhrase.append(" From: ").append(thisJoinPoint.getSourceLocation().getWithinType());

        // Prepare the methods parameter list
        String parmStr = null;
        Object[] parameters = thisJoinPoint.getArgs();
        if (parameters.length > 0)
        {
            String spaces = getSpaces(depth + 6);
            StringBuffer parms = new StringBuffer();
            for (int i = 0; i < parameters.length; i++)
            {
                if (parameters[i] != null && parameters[i].getClass().isArray())
                {
                    // arrays can be huge...limit to first 100 elements
                    final int len = Math.min(Array.getLength(parameters[i]),100);
                    if ( len == 0 )
                    {
                        parms.append(spaces);
                        parms.append(parameterNames[i]);
                        parms.append(": 0 length array");
                        parms.append(NL);
                    }
                    else
                    {
                        Object o = null;
                        for ( int x = 0; x < len; x++ )
                        {
                            parms.append(spaces);
                            parms.append(parameterNames[i]);
                            parms.append("[");
                            parms.append(x);
                            parms.append("]:");
                            o = Array.get(parameters[i],x);
                            try{parms.append(" " + (o != null?o:"null"));}  // implicit toString()
                            catch(Throwable t) {parms.append(" " + parameters[i]);}
                            parms.append(NL);                            
                        }
                    }
                }
                else
                {
                    // Not an array.
                    parms.append(spaces);
                    parms.append(parameterNames[i]);
                    parms.append(": ");
                    try
                    {
                        parms.append("" + parameters[i]);
                    }
                    catch (Throwable t ) {parms.append("" + parameters[i].getClass().getName());}
                }

                parmStr = parms.toString();

            }

        }

        if (parmStr != null)
            enterPhrase.append(NL).append(parmStr);
        log(enterPhrase.toString());
    }

    /**
     * This method creates an exception handler trace entry based on a Throwable
     * and information contained in the join point.
     */
    private void traceHandlerEntry(Throwable t, JoinPoint.StaticPart thisJoinPoint)
    {

        int depth = getTraceDepth(INDENT);
        String phrase = getSpaces(depth) +
                        "Exception caught at: " +
                        thisJoinPoint;
        log(printStackTrace(phrase,t));

    }
    /**
     * This method simply adjusts the trace depth - no other information printed.
     */
    private void traceHandlerExit()
    {
        getTraceDepth(-INDENT);
    }
    /**
     * This method creates a stack trace entry based on a Throwable and
     * information contained in the join point.
     */
    private void traceThrowable(Throwable t, JoinPoint thisJoinPoint)
    {

        int depth = getTraceDepth(0);
        String phrase = getSpaces(depth+4) +
                                        "Throwing Exception at: " +
                                        thisJoinPoint;
        log(printStackTrace(phrase,t));
    }


    private String printStackTrace(String phrase, Throwable t)
      {
    try {
      StringWriter sw = new StringWriter(4096);
      PrintWriter  pw = new PrintWriter(sw,true);

      pw.println(phrase);

      pw.println();

      pw.println("Exception Stack Trace:");

      pw.println();

      t.printStackTrace(pw);

      pw.println();

      pw.flush();
      sw.flush();

      pw.close();
      sw.close();
      return sw.toString();
    }
    catch(IOException IOE) {
      log(IOE.toString());
      return IOE.getMessage();
    }
      }

    /**
     * This method creates a trace exit entry based on the join point
     * information.
     */
    private void traceExit(JoinPoint thisJoinPoint)
    {

        int depth = getTraceDepth(-INDENT);

        // Assemble the method's signature.
        Class[] parameterTypes = ((CodeSignature)thisJoinPoint.getSignature()).getParameterTypes();
        String[] parameterNames = ((CodeSignature)thisJoinPoint.getSignature()).getParameterNames();

        boolean isCall = thisJoinPoint.getKind().endsWith("call");

        StringBuffer exitPhrase = new StringBuffer(100);
        exitPhrase.append(getSpaces(depth));
        if ( isCall )
            exitPhrase.append("Return ");
        else
            exitPhrase.append("Exiting ");
        exitPhrase.append(methodSignature(parameterNames,parameterTypes,thisJoinPoint)).append(NL);
        

        log(exitPhrase.toString());

    }

    /**
     * This method creates a trace result entry based on a result and the
     * join point.
     */
    private void traceResult(Object thisResult, JoinPoint thisJoinPoint)
    {

        Class returnType = ((MethodSignature)thisJoinPoint.getSignature()).getReturnType();
        if ( returnType.toString().equals("void") )
            return;

        int depth = getTraceDepth(0);
        if ( thisResult == null )
            thisResult = "null";

        if ( thisResult.getClass().isArray() )
        {
            // arrays can be Oprah-sized - limit to 100 elements
            final int len = Math.min(Array.getLength(thisResult),100);
            StringBuffer buf = new StringBuffer();
            if ( len == 0 )
                buf.append(">>>zero-length array<<<");
            else
            {
                Object o;
                for ( int i = 0; i < len; i++ )
                {
                    o = Array.get(thisResult,i);
                    buf.append("data[").append(i).append("] ");
                    try{buf.append(o != null?o:"null");} // implicit toString() 
                    catch(Throwable t) {buf.append(thisResult);}
                    buf.append(NL);
                }
            }
            thisResult = buf.toString();                
        }
        thisResult = thisResult.toString();
        
        StringBuffer returnPhrase = new StringBuffer(100);
        returnPhrase.append(getSpaces(depth+2)).append(thisJoinPoint);
        returnPhrase.append(" returned >>>>>>> ").append(thisResult);

        log(returnPhrase.toString());
    }

    /**
     * This method returns the current trace line indentation for the
     * thread.
     */
    private int getTraceDepth(int incr)
    {
            int rc = 0;
            Integer depth = (Integer) traceDepths.get();
            if (depth == null)
            {
                if ( incr > 0 )
                {
                    traceDepths.set(new Integer(incr));
                    return incr;
                }
                else return rc;
            }    
            
            rc = depth.intValue();
                            
            if ( incr > 0 )
            {
                depth = new Integer(rc += incr);            
                traceDepths.set(depth);
            }
            else if ( incr < 0 )
            {
                depth = new Integer(rc + incr);            
                traceDepths.set(depth);
            }
            
            return rc;
    }

    /**
     * This method returns a String containing the number of spaces desired to
     * be used as padding for formatting trace log entries.
     */
    private String getSpaces(int num)
    {
        return new String(SPACES,0,Math.min(num,SPACES.length));
    }

    /**
     * Create a method signature
     */
    private String methodSignature(String[] parameterNames,
                    Class[] parameterTypes,
                    JoinPoint thisJoinPoint)
    {
        // Assemble the method's signature.
        StringBuffer signature = new StringBuffer("(");
        for (int i = 0; i < parameterTypes.length; i++)
        {
            signature.append(parameterTypes[i].getName());
            signature.append(" ");
            signature.append(parameterNames[i]);
            if (i < (parameterTypes.length-1))
                signature.append(", ");
        }
        signature.append(")");

        return thisJoinPoint.getSignature().getDeclaringType().getName() + "." +
            thisJoinPoint.getSignature().getName() +
            signature;
    }
    
}