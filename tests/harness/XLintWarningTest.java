
        
public aspect XLintWarningTest {        
    
    before() : staticinitialization(UnknownType) { // CW 5 - XLint:invalidAbsoluteTypeName
        System.err.println("never");
    }
}



