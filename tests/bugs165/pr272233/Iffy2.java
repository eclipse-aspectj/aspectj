import java.util.*;

import org.aspectj.lang.annotation.*;

@Aspect
class Iffy2 {

  // Match getCollectionArray(), getIntegerCollectionArray()
  @Before("execution(!void *(..))")
  public void nonVoid() { }

  // Do not match anything, because void[] is an illegal type
  @Before("execution(void[] *(..))")
  public void voidArray() {
    // This does not compile in Java
    // void[] voids = new void[5];
  }

  // Match getCollectionArray() and myVoid(), getIntegerCollectionArray(), because void[] is an illegal type which
  // cannot be resolved/matched. The negation of an unmatched type, however, matches any type, similar to how
  // !my.UnknownType would also match all other types.
  @Before("execution(!void[][] *(..))")
  public void nonVoidArray() { }

  // Match getCollectionArray(), getIntegerCollectionArray()
  @Before("execution(*..Collection[] *(..))")
  public void wildcardRawCollectionArray() { }

  // Match getCollectionArray()
  @Before("execution(java.util.Collection<?>[] *(..))")
  public void exactGenericCollectionArray() { }

  // Match getCollectionArray()
  @Before("execution(*..Collection<?>[] *(..))")
  public void wildcardGenericCollectionArray() { }

  // Do not match anything
  @Before("execution(*..Collection<String>[] *(..))")
  public void wildcardGenericCollectionArrayOfString() { }

  // Match getIntegerCollectionArray()
  @Before("execution(*..Collection<Integer>[] *(..))")
  public void wildcardGenericCollectionArrayOfInteger() { }

  // Do not match anything. The fact that primitive type int is illegal as a generic type parameter, is not mentioned
  // in any warning.
  @Before("execution(*..Collection<int>[] *(..))")
  public void wildcardGenericCollectionArrayOfPrimitiveInt() { }

  public void myVoid() { }

  public Collection<?>[] getCollectionArray() {
    return null;
  }

  public Collection<Integer>[] getIntegerCollectionArray() {
    return null;
  }

}
