
/** @testcase PR#770 cflow pcd syntax error */
aspect IllegalCflowCE {
    pointcut badCflow() : cflow(*.new(..)); // CE 4
}
