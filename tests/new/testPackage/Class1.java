package packagevisibility.testPackage;

public class Class1 {
    public String doIt(String s) {
        return s + "-class1";
    }
    
    public String doItToClass2(String s) {
        return (new packagevisibility.testPackage.Class2()).doIt(s);
    }
}
