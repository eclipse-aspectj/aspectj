import java.util.*;

public aspect ParameterizedMethodMatching {

  pointcut findMax() : execution(static<T> T *(List<T>));
    // matches findMax
    // does not match e.g. Object foo(List<Object> foos) {...}
    
  pointcut findMax2() : execution(static<X> X * List<X>));
    // matches findMax  
    
  pointcut findMax3() : execution(static<T> T+ *(List<T>));
    // CE  
    
  pointcut listargs(): args(List<?>);
    // always matches findMax
    
  pointcut listNumberargs() : args(List<Number>);
    // may match findMax (RTT)
  
}