
class Foo<A> extends FooBase implements Marker{}//Marker<A> { }

interface Marker<A> { }

aspect AspectDoWhatEver {
    void Marker.doWhatEver()  { // do nothing
    }
}

abstract class FooBase
{
    abstract void doWhatEver();
}