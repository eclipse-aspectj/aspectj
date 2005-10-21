import java.io.*;

class SomeClass {
    public void doSomething() { }
}

aspect DoesntCompile {

    declare parents : SomeClass implements Serializable;

    pointcut doSomething(SomeClass someClass) :
            execution(void SomeClass.doSomething()) &&
            this(someClass);

    void around(Serializable myWorld) : doSomething(myWorld) { }
    
}