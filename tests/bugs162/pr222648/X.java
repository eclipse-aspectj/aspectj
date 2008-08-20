
abstract class FooBase<A> { }

// Existence of this line causes the exception
abstract class Foo <CC extends Resource, DD extends DataInterface<CC>> extends FooBase<DD> { }

interface DataInterface<CC> {
  public CC getContent(); // ERR
}

interface Marker<CC> extends DataInterface<CC> { }

interface Resource { }

aspect DataAspect {
  // Intertype declaration onto Marker that shares the variable
  public C Marker<C>.getContent() { // ERR
    return null;
  }
}

/*
X.java:7 [error] can't override CC DataInterface<CC>.getContent() with CC Marker.getContent() return types don't match
public CC getContent();
          ^^^^^^^^^

X.java:16 [error] can't override CC DataInterface<CC>.getContent() with CC Marker.getContent() return types don't match
public C Marker<C>.getContent() {
                   ^^^^^^^^^

1. Two errors because both source locations reported (stupid code)


when failing:
parent: CC DataInterface<CC>.getContent()
child : CC Marker.getContent()
Both return types are type variable reference types (different ones though)
parent: TypeVar CC extends Resource
child : CC

So parent discovered the wrong type variable (the wrong CC).  
parent is a ResolvedMemberImpl
it is considered an 'existingmember' of the type DataInterface<CC>

*/

