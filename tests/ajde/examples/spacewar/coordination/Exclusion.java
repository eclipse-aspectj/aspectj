
package coordination;


interface Exclusion {

    boolean testExclusion(String methodName);

    void enterExclusion(String methodName);

    void exitExclusion(String methodName);

    // for debug    !!!
    void printNames();
}

