public class PR72157 {
    public PR72157() throws Exception {
        throw new Exception();
    }

    public static void main(String[] args) {
        new SCE2();
    }
}

class SCE2 extends PR72157 {
    public SCE2() {  
        super();    // CE L13?
    }
    
}

class Foo {
    
    public Foo() throws Exception {
        throw new Exception();
    }
    
}

class Goo {
    public Goo() {
        new Foo();
    }
}

aspect SCEAspect {
    declare soft: Exception: within(SCE2);
    declare soft: Exception: within(Goo);
}