
/** @testcase PR#619 direct use outside aspect of undefined abstract pointcut */
abstract aspect AbstractPointcutAccessCE { 
    abstract pointcut abstractPointcut();
} 

aspect AbstractPointcutUser { 
    before () 
        : AbstractPointcutAccessCE.abstractPointcut() { } // CE
}

