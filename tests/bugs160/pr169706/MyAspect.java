public aspect MyAspect {

    // this throws an exception
    before(MyAnnotation myAnnotation) : 
        //call(* *..*.*(..)) &&
        call(@MyAnnotation * *(..)) &&
        @annotation(myAnnotation) {

    }

    // this, however, works fine
//     before() : 
 //        call(@MyAnnotation * *(..)) {
  //   
   //  }
}
