public class AspectsCantHaveYesArgumentConstructors {
    public static void main(String[] args) {
    }
}

aspect A /*of eachobject(instanceof(CompileError2))*/ {
    //ERROR: only zero argument constructors allowed in an aspect
    public A(String s) {}
}
