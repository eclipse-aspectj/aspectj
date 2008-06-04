public aspect SomeAspect {

        void around(final SomeAnnotation someAnnotation) :
                call(@SomeAnnotation void *.*(..)) && @annotation(someAnnotation) {

                System.out.println("@someAspect annotation parameter (call)");
//CASES 1, 3 only
                proceed(someAnnotation);
        }

        void around(final SomeAnnotation someAnnotation) :
                execution(@SomeAnnotation void *.*(..)) &&
@annotation(someAnnotation) {

                System.out.println("@someAspect annotation parameter (execution)"); //CASES 1, 2, 3
                proceed(someAnnotation);
        }

        void around() : call(@SomeAnnotation void *.*(..)) {
                System.out.println("@someAspect annotation no parameter");
//CASES 1, 2, 3
                proceed();
        }

        void around() : call(void *.test*(..)) {
                System.out.println("@someAspect method name"); //CASES 1, 2, 3
                proceed();
        }
}
