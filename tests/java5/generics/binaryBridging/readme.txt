Tests that if binary weaving then we create the right bridge methods.

Test One: Using decp to wire together two types - a generic type that has a bunch of methods that use type variables and a second type that is told to implement a parameterization of the generic type with the decp.

Test Two: Now the horrific method in the supertype is overridden by a combination of covariance and parameterization.

Test Three: inspired by a post to the list.  The abstract method in the subclass should have a bridge method generated alongside it.
