public class AspectInterfaces {
}

aspect SerAspect implements java.io.Serializable {} //ERR: can't implement Ser
aspect CloneAspect implements Cloneable {} //ERR: can't implement Clone

class C implements java.io.Serializable { }

aspect Ser1Aspect extends C {} //ERR: can't extend a class that implements Ser
