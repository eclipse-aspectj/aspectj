import java.util.*;

public aspect GenericParameterMatching {

  pointcut takesAMap() : execution(* *(Map<Double,Short>));
    // matches takesAMap, staticTakesAMap
    
  pointcut takesAnyMapType() : execution(* *(Map+<Double,Short));
    // matches takesAMap, staticTakesAMap, takesAHashmap
    
  pointcut collectionOfAnything() : execution(* *(Collection<?>));
    // matches collectionOfAnything
    
  pointcut collectionOfAnyNumber() : execution(* *(Collection<? extends Number));
    // matches collectionOfAnyNumber
    
  pointcut collectionOfTakingDouble() : execution(* *(Collection<? super Double>));
    // matches collectionOfAnythingTakingADouble
    
  pointcut anyCollection() : execution(* *(Collection<*>));
    // matches all 3 collection methods
    
  pointcut anyObjectOrSubtypeCollection() : execution(* *(Collection<? extends Object+>));
    // matches collection of any number
    
  pointcut superTypePattern(): execution(* *(Collection<? super Number+>));
    // matches collection of anything taking a double

  // RTT matching...
  
  pointcut mapargs() : args(Map<Double,Short>);
    // matches takesAMap, staticTakesAMap, takesAHashmap
    
  pointcut hashmapargs() : args(HashMap<Double,Short>);
    // matches takesAHashmap, RT test for takesAMap, staticTakesAmap
    
  pointcut nomapargs(): args(Map<Object,Object>);
    // no matches
    
  pointcut wildargs() : args(Map<? extends Number, Short>);
    // matches takesAMap, staticTakesAMap, takesAHashmap
    
  pointcut nowildargs() : args(Map<? extends String, Short>);
    // no matches  
    
  pointcut wildsuperargs() : args(Map<Double, ? super Short>);
    // matches takesAmap, staticTakesAmap, takesAHashmap
    
  // RTT matching with signature wildcards  

  pointcut collAnythingArgs() : args(Collection<?>);
    // matches all collection methods
    
  pointcut collNumberArgs() : args(Collection<Number>);
    // does NOT match collectionOfAnyNumber (can't insert safely)
    // does NOT match collectionOfAnythingTakingADouble (can't remove safely)
    
  pointcut collNumberArgsWild() : args(Collection<? extends Number>);
    // matches collection of any number
   
   pointcut superDoubleArgs(): args(Collection<? super Number+>);
     // matches coll taking a double
     
   // add max and copy tests here...
}