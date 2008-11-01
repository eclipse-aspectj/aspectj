
public class Service implements IService {

    public void method(long l) throws Exception {
        System.err.println("Original impl of service method, arg " + l);
    }
}
