
import java.util.Observer;
import java.util.Observable;

/** @testcase PR#740 pointcut references with incorrect args should prompt compiler errors */
aspect A {
    pointcut none() : call(static void main(String[]));
    pointcut one(Runnable r) : target(r) && call(void run());
    pointcut two(Observer seer, Observable seen) : 
        target(seer) 
        && args(seen, Object)
        && execution(void update(Observable, Object));
    pointcut three(Observer seer, Observable seen, Object arg) : 
        target(seer) 
        && args(seen, arg)
        && execution(void update(Observable, Object));

    // cases should not have errors
    pointcut none0type() : none();
    pointcut one1type() : one(Runnable);
    pointcut two2type() : two(Observer, Observable);
    pointcut three3type() : three(Observer, Observable, Object);
    pointcut one1arg(Runnable r) : one(r);
    pointcut two2arg(Observer seer, Observable seen) : two(seer, seen);
    pointcut three3arg(Observer seer, Observable seen, Object o) : 
        two(seer, seen, o);

    // cases should prompt CE
    pointcut none1type() : none(Object);                              // CE 29
    pointcut none1name(Object o) : none(o);                           // CE 30
    pointcut none2type() : none(Object, Object);                      // CE 31
    pointcut none2name(Object o, Object p) : none(o,p);               // CE 32
    pointcut one0() : one();                                          // CE 33
    pointcut one2type() : one(Runnable, Object);                      // CE 34
    pointcut one2name(Runnable o, Object p) : one(o,p);               // CE 35

    pointcut two0() : two(Object);                                    // CE 37
    pointcut two1type() : two(Object);                                // CE 38
    pointcut two1name(Object o) : two(o);                             // CE 39
    pointcut two3type() : two(Observer, Observable, Object);          // CE 40
    pointcut two3name(Observer seer, Observable seen, Object object) :
        two(seer, seen, object);                                      // CE 42

    pointcut three0() : three(Object);                                // CE 44
    pointcut three1type() : three(Object);                            // CE 45
    pointcut three1name(Object o) : three(o);                         // CE 46
    pointcut three2type() : three(Observer, Observable);              // CE 47
    pointcut three2name(Observer seer, Observable seen) : 
        three(seer, seen);                                            // CE 49
    pointcut three4type() : three(Observer, Observable, Object, Object); // CE 50
    pointcut three4name(Observer seer, Observable seen, 
                        Object object, Object two) : 
        three(seer, seen, object, two);                              // CE 53
}
