Mode: VM run
Title: Introduce of constructors

This tests if  constructor introductions happen at all the places that
they are supposed to happen in the presence of interfaces,
subinterfaces, classes and inheritance.  

It DOES NOT test if constructor introductions do not happen at all the
places they should not happen.  (That will be a separate test.)  Nor
does it test for the special cases when the constructors with the
signatures used in the introductions already exist. (That should be a
separate test.)
