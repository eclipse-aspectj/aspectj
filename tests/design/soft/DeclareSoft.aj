public class DeclareSoft {
    
    public void throwException() throws Exception {
        throw new Exception("This should be softened");
    }
    
    public void throwRuntimeException() {
        throw new RuntimeException("Under enh 42743 this should not be softened");
    }
    
    public static void main(String[] args) throws Exception {
        DeclareSoft ds = new DeclareSoft();
        try {
            ds.throwException();
        } catch (org.aspectj.lang.SoftException se) {}
        try {
            ds.throwRuntimeException();
        } catch(org.aspectj.lang.SoftException se) {
            throw new RuntimeException("Runtime exception was innappropriately softened");
        } catch (RuntimeException ex) {}
    }
    
}

aspect Softener {
    
    declare soft: Exception : execution(* DeclareSoft.throw*(..));
    
}