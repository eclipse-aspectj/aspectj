package bar;

import foo.Foo;

aspect Bar {
    public void Foo.doing() {
try {
    System.out.println(i()); // CE L8
    System.out.println(ancientI()); // CE L9
    System.out.println(ancientJ()); // CE L10
    System.out.println(this.clone()); // CE L11
    System.out.println(clone()); // CE L12
}
catch(Throwable t) { }
    }
    before(Foo f) : call(* doStuff(..)) && target(f) {
        f.doing();
    }
}


privileged aspect PBar {
    public void Foo.doingMore() {
try {
    System.out.println(i());
    System.out.println(ancientI());
    System.out.println(ancientJ()); 
 }
catch(Throwable t) { }
    }
    before(Foo f) : call(* doStuff(..)) && target(f) {
        f.doing();
    }
}


