import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;


@Clientside
public aspect ServiceCall
{
    
    public pointcut ServicePoint()
        : call( * (@BussFacade *).*(..) )
          && !@within(Clientside)
          && !@annotation(Clientside)
          && ( !@within(ServiceImplementation)
             || @withincode(Clientside)
             )
          ;
   
    declare @type 
        : hasmethod(* (@BussFacade *).*(..)) : @ServiceImplementation
        ;
        
    public @interface ServiceImplementation {  }
        
        
    private pointcut call_Service(Object businessFacade)
        : ServicePoint()
          && target(businessFacade);
        
    
    protected Object findImpl(Object bussFacade, JoinPoint.StaticPart location) 
    {
        Class dienstID;
        if ( null!=bussFacade )
            dienstID = bussFacade.getClass();
        else {
            Signature sig = location.getSignature();
            dienstID = sig.getDeclaringType();
        }
        Object impl = new MyServiceImpl();  // call ServiceLocator here
        return impl;
    }


    
    Object around(Object bussFacade)
        : call_Service(bussFacade) 
     {
        try {
                Object umgelenkt = findImpl(bussFacade, thisJoinPointStaticPart);
                Object res = proceed(umgelenkt);
                return res;
            }
            catch(Throwable T) {
                System.out.println("oh my");
                throw new RuntimeException(T);
            }
     }    
    
}
