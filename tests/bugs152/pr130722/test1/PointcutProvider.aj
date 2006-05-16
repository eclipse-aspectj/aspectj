package test1;

import test.Test;
public aspect PointcutProvider {

        public pointcut test(): execution(* Test.*(..));
}


