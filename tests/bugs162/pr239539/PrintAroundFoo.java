package foo;

import bar.PrintAround;

public aspect PrintAroundFoo extends PrintAround {

       pointcut method() : call (void Main(String[]));

}
