
public aspect TraceMains {


    before() : execution(static void main(String[])) {
        String m = thisJoinPointStaticPart.getSignature().toString();
        System.out.println("before " + m);
    }
    after() returning: execution(static void main(String[])) {
        String m = thisJoinPointStaticPart.getSignature().toString();
        System.out.println("after " + m);
    }
    
}