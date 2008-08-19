// priviligedness of aspect contributes to the error
//public aspect GenericClassInAdvice { // comment out this line and comment the
// following to be able to compile...
privileged aspect GenericClassInAdvice {

 Object around(final SomeInterface src, final SomeInterface dst) : call(!void *.*(..)) && this(src) && target(dst) {

  // the parameterized constructor contributes to the error  
//  final PayloadClass<Object> payloadClass = new PayloadClass/*<Object>*/() {
// comment out this line and comment the following to be able to compile...
  final PayloadClass<Object> payloadClass = new PayloadClass<Object>() {

   public void run() {
    // this triggers a compiler error in combination with:
    // * privilegedness of the aspect "privileged aspect ..."
    // * parameterized constructor "new PayloadClass<Object>() {...}'
    // * the existence of a payload field in PayloadClass
    Object payload = proceed(src,dst); // comment this line and the following or rename 'payload' to 'pl' to be able to compile...
    this.setPayload(payload);
   }

  };

  payloadClass.run();

  return payloadClass.getPayload();
 }
}


