



@NeedsXYZ
public class ClientCode
{

    MyInterface __Interface__ = null;
    
    ClientProxy specialConfigProxy = new ClientProxy();
    
    
    void doIt() {
        System.out.println("hold onto your hat...");
        System.out.println("the answer is:"+ __Interface__.doB(42));   // direct Call is intercepted here
        
        System.out.println("and now, "
                          + specialConfigProxy.additionalValueServiceForTheCustomer()
                          );                                         //   indirect call is intercepted in the proxy
    }
    
    
    
    public static void main(String[] args) {
        
        new ClientCode().doIt();
    
    }
}
