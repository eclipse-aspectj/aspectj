
package figures;

aspect SimpleTracing {

    pointcut tracePoints(): call(void Point.setX(int)) ||
                            call(void Point.setY(int));

    before(): tracePoints() {
        System.out.println("Entering:" + thisJoinPoint);
    }
}

