

public class ClientProxy extends MyServiceImpl
{
    
    // just for this demo here; this cast allows
    // us to call to the interface (without the cast
    // we'd get a class cast exception)
    MyInterface this_in_disguise = this;
    
    
    @Clientside
    public String additionalValueServiceForTheCustomer() {
        return "if you don't know what to ask, then you " 
             + this_in_disguise.doA(42); // call through to the server side
    }

}
