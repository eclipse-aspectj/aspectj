Mode: vm run
Title: introduce of variables

This tests if variable introductions happen at all the places that
they are supposed to happen in the presence of interfaces,
subinterfaces, classes and inheritance.  It tests rule 2 partly, (the
non-error causing cases only).

It DOES NOT test if variable introductions do not happen at all the
places they should not happen.  (That will be a separate test.)


