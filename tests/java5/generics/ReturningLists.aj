import java.util.*;

public aspect ReturningLists {

  pointcut listOfInteger() : execution(List<Integer> *(..));
    // matches returningListOfInteger
  
  pointcut listOfObject() : execution(List<Object> *(..));
    // matches returningListOfObject
  
  pointcut listOfObjects() : execution(List<Object+> *(..));
    // matches returningListOfInteger and returningListofObject
  
  pointcut listOfAnything() : execution(List<*> *(..));
    // matches returningListOfInteger and returningListofObject

  pointcut rawList() : execution(List *(..));
    // matches returningRawList
    
  pointcut wildcardList() : execution(List<?> *(..));
    // matches nothing
    
  pointcut anyListType() : execution(List+<*> *(..));
    // matches returning list of integer, returning list of object,
    // returning subtype of list of integer
        
}