strictfp interface StrictInterfaceBadFunction {
    // Has to be error, may not generate strictfp, but has to set strictfp in bytecode
    strictfp float test1();
};
