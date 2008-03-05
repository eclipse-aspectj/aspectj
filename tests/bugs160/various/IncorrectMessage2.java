
class Foo<A> extends FooBase implements Marker<A> { }

interface Marker<A> { }

aspect AspectDoWhatEver {
    void Marker<A>.doWhatEver()  { // do nothing
    }
}

abstract class FooBase
{
    abstract void doWhatEver();
}