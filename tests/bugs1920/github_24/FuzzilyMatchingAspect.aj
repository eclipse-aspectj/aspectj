public aspect FuzzilyMatchingAspect {

  pointcut returnRefTypeSimpleOrArray() : execution(public MaybeMissing* MaybeMissing*.*());
  pointcut return1DimRefTypeArray() : execution(public MaybeMissing*[] MaybeMissing*.*());
  pointcut return2DimRefTypeArray() : execution(public MaybeMissing*[][] MaybeMissing*.*());

  // Return type 'MaybeMissing*' also matches array types due to the '*' at the end.
  // Therefore, explicitly exclude array pointcuts in order to only match the method returning the simple type.
  after() : returnRefTypeSimpleOrArray() && !return1DimRefTypeArray() && !return2DimRefTypeArray() {
    System.out.println(thisJoinPoint);
  }

  after() : return1DimRefTypeArray() {
    System.out.println(thisJoinPoint);
  }

  after() : return2DimRefTypeArray() {
    System.out.println(thisJoinPoint);
  }

  pointcut returnPrimitiveTypeSimpleOrArray() : execution(public in* MaybeMissing*.*());
  pointcut return1DimPrimitiveTypeArray() : execution(public in*[] MaybeMissing*.*());
  pointcut return2DimPrimitiveTypeArray() : execution(public in*[][] MaybeMissing*.*());

  // Return type 'in*' also matches array types due to the '*' at the end.
  // Therefore, explicitly exclude array pointcuts in order to only match the method returning the simple type.
  after() : returnPrimitiveTypeSimpleOrArray() && !return1DimPrimitiveTypeArray() && !return2DimPrimitiveTypeArray() {
    System.out.println(thisJoinPoint);
  }

  after() : return1DimPrimitiveTypeArray() {
    System.out.println(thisJoinPoint);
  }

  after() : return2DimPrimitiveTypeArray() {
    System.out.println(thisJoinPoint);
  }

}
