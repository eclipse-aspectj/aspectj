
public class DeclareParentsNonsenseCE { }
aspect A {
    /** @testcase PR#652 declare parent accepting interface for extension */
    declare parents: DeclareParentsNonsenseCE extends java.io.Serializable;    // CE here
    /** @testcase PR#652 declare parent accepting class for implementation */
    declare parents: DeclareParentsNonsenseCE implements java.util.Observable; // CE here
}
