package lib;

public aspect ConcreteA {
    public interface Marker {}
    
    public String Marker.value = "public";
    private String Marker.pValue = "private";
    
    public static String getPrivateValue(Marker m) { return m.pValue; }

    public static class MyException extends Exception {}

    declare soft: MyException: withincode(new(..));
    
    
    // added this to cover Bugzilla Bug 34820  
    //     ajc -aspectpath fails with NPE for cflow pointcuts 
    int counter = 0;
    before(): cflow(within(client..*)) && call(* println(..)) {
        counter ++;
    }
}