Tests that if binary weaving then we create the right bridge methods.

Test One: Using decp to wire together two types - a generic type that has a bunch of methods that use type variables and a second type that is told to implement a parameterization of the generic type with the decp.


