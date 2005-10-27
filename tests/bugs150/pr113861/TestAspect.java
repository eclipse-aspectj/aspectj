package com;
import java.util.*;

public privileged aspect TestAspect {
  pointcut gettingMember(Test t) :
             target(t) &&
             get(!public Set<Integer> com.*.*) &&
             !within(TestAspect);

  Set<Integer> around(Test t) : gettingMember(t)  {
    Set s =  proceed(t);
    return s;
  }
}
