
public class IncompatibleAfterReturningTypeCE {
    public static void main(String[] args) {
        System.setProperty("foo", ""+"".length());
    }
}

class C {
    Integer getInteger() { 
        return null;
    }
}

/** @testcase PR#42668 after returning type incompatible with join point return type */
aspect A {

    after () returning (Boolean b) : execution(Integer C.getInteger()) { } // CE 20 incompatible return type from join point    

    after () returning (byte b) : call(int String.length()) {} // CE 22 incompatible return type 

}