interface i {
    i() {}          // ERR: interfaces can't have constructors
}

class c {
    c(); //ERR: constructors must have bodies 

    abstract c(int i) { } //ERR: constructors can't be abstract
}
