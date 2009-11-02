package ex;

aspect SupplyMethodCollectedToService {
    declare @type: @Service * : @MethodCollected;
   
    pointcut collected() :@within(MethodCollected) && execution(* *(..));
 //   pointcut collected2() : execution(* (@MethodCollected *).*(..));
   
    before() : collected() {
        // Should advice both serve() and serve_itd()
    }
//    before() : collected2() { }
}
@interface MethodCollected {}
