
/** @testcase PR#31724 omnibus declare-warning test using default initializers/constructors*/
public class DeclareWarningEmpty {  // CE 3        





}

aspect A {
    declare warning: staticinitialization(DeclareWarningEmpty)   
        : "staticinitialization(DeclareWarningEmpty)";
    declare warning: initialization(DeclareWarningEmpty.new(..))   
        : "initialization(DeclareWarningEmpty)";
}
