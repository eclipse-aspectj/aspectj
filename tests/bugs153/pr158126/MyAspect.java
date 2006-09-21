public aspect MyAspect {

        before() :
                call(@MyAnnotation *.new(..)) {
                System.out.println(thisJoinPoint);
        }
}
