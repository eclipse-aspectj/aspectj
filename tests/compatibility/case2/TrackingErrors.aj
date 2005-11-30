import org.aspectj.lang.*;

import org.aspectj.lang.JoinPoint;

public abstract aspect TrackingErrors {

   protected abstract pointcut errorScope ();

   private pointcut staticContext () : !this(Object);
   private pointcut nonStaticContext (Object obj) : this(obj);
   private pointcut caughtThrowable (Throwable th) : handler(Throwable+)
           && args(th);



   private pointcut excluded () : within(TrackingErrors+);

   before (Throwable th) : caughtThrowable(th) && errorScope() &&
           !excluded() && staticContext() {

           processStaticTrackingErrors(th,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
   }

   before (Throwable th, Object obj) : caughtThrowable(th) && errorScope()
           && !excluded() && nonStaticContext(obj) {

           processNonStaticTrackingErrors(th,obj,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
   }

   protected  void processStaticTrackingErrors (Throwable th,
           JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {}

   protected void processNonStaticTrackingErrors (Throwable th, Object obj,
           JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {}

   protected String getSourceId (JoinPoint.StaticPart ejp) {
                 String typeName =
           ejp.getSignature().getDeclaringTypeName();
                 String name = ejp.getSignature().getName();
           return typeName + "." + name;
   }

   protected String getProbeId (JoinPoint.StaticPart tjp) {
                 String sourceLocation =
           tjp.getSourceLocation().toString();
           return sourceLocation;
   }

}
/*public abstract aspect Complex {
    
    protected abstract pointcut scope ();
    
    private pointcut staticContext () : !this(Object);
    private pointcut nonStaticContext (Object obj) : this(obj);
    private pointcut caughtThrowable (Throwable th) : handler(Throwable+) && args(th);
 
    private pointcut excluded () : within(Complex+);
 
    before (Throwable th) : caughtThrowable(th) && scope() && !excluded() && staticContext() {
       processStaticData(th,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
    }
 
    before (Throwable th, Object obj) : caughtThrowable(th) && scope() && !excluded() && nonStaticContext(obj) {
       processNonStaticData(th,obj,thisJoinPointStaticPart,thisEnclosingJoinPointStaticPart);
    }
    
    private void processStaticData (Throwable th, JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {
    }
 
    private void processNonStaticData (Throwable th, Object obj, JoinPoint.StaticPart tjp, JoinPoint.StaticPart ejp) {
    }
 
    protected String getSourceId (JoinPoint.StaticPart ejp) {
        String typeName = ejp.getSignature().getDeclaringTypeName();
        String name = ejp.getSignature().getName();
        return typeName + "." + name;
    }
 
    protected String getProbeId (JoinPoint.StaticPart tjp) {
        String sourceLocation = String.valueOf(tjp.getSourceLocation().getLine());
        return sourceLocation;
    }
 
}
*/
