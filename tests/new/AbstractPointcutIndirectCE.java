
/** @testcase PR#619 indirect use outside aspect of undefined abstract pointcut */
abstract aspect AbstractPointcutIndirectCE { 
    abstract pointcut abstractPointcut();
    pointcut pc() : abstractPointcut();
} 

aspect AbstractPointcutUser { 
    before () 
        : AbstractPointcutIndirectCE.pc() { } // CE
}

