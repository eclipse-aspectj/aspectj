
/** @testcase inter-type declaration of void field */
public class VoidFieldDeclarationCE {
}

aspect A {
    public void VoidFieldDeclarationCE.bug; // CE 46 invalid field type
}
