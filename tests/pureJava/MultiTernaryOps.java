import org.aspectj.testing.*;

public class MultiTernaryOps {
    public static void main(String[] args) {
        new MultiTernaryOps().realMain(args);
    }

    int c = 0;
    int z = 0;
    public void realMain(String[] args) {

        int SIZE = 1000;
        int[] xs = new int[SIZE];
        boolean b = true;
        int e = 123;
        Object o = null;
        Object ot = new t();

        // b?e:e
        c(1); xs[c++] = t(0) ? 1 : 2; e();
        c(1); xs[c++] = f(0) ? 2 : 1; e();

        // b ? (b : e : e) : e
        c(2); xs[c++] = t(0) ? f( 1) ? 2 : 1 : 3; e();
        c(2); xs[c++] = t(0) ? (f( 1) ? 2 : 1) : 3; e();

        // b ? ( b : e : e) : e
        c(1); xs[c++] = f(0) ? x() ? 2 : 3 : 1; e();
        c(1); xs[c++] = f(0) ? (x() ? 2 : 3) : 1; e();
        
        // b ? (b ? (b ? e : e) : e) : e
        c(3); xs[c++] = t(0) ? (t(1) ? (t(2) ? 1 : 2) : 3) : 4; e();
        c(3); xs[c++] = t(0) ?  t(1) ? (t(2) ? 1 : 2) : 3  : 4; e();
        c(3); xs[c++] = t(0) ?  t(1) ?  t(2) ? 1 : 2  : 3  : 4; e();

        c(1); xs[c++] = f(0) ? (x() ? (x() ? 4 : 2) : 3) : 1; e();
        c(1); xs[c++] = f(0) ?  x() ? (x() ? 4 : 2) : 3  : 1; e();
        c(1); xs[c++] = f(0) ?  x() ?  x() ? 4 : 2  : 3  : 1; e();

        c(2); xs[c++] = t(0) ? (f(1) ? (x() ? 2 : 3) : 1) : 4; e();
        c(2); xs[c++] = t(0) ?  f(1) ? (x() ? 2 : 3) : 1  : 4; e();
        c(2); xs[c++] = t(0) ?  f(1) ?  x() ? 2 : 3  : 1  : 4; e();

        c(3); xs[c++] = t(0) ? (t(1) ? (f(2) ? 2 : 1) : 3) : 4; e();
        c(3); xs[c++] = t(0) ?  t(1) ? (f(2) ? 2 : 1) : 3  : 4; e();
        c(3); xs[c++] = t(0) ?  t(1) ?  f(2) ? 2 : 1  : 3  : 4; e();

        // b ? (b ? (b ? (b ? e : e) : e) : e) : e
        c(4); xs[c++] = t(0) ? (t(1) ? (t(2) ? (t(3) ? 1 : 2) : 3) : 4) : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ? (t(2) ? (t(3) ? 1 : 2) : 3) : 4  : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ?  t(2) ? (t(3) ? 1 : 2) : 3  : 4  : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ?  t(2) ?  t(3) ? 1 : 2  : 3  : 4  : 5; e();

        c(1); xs[c++] = f(0) ? (x() ? (x() ? (x() ? 5 : 2) : 3) : 4) : 1; e();
        c(1); xs[c++] = f(0) ?  x() ? (x() ? (x() ? 5 : 2) : 3) : 4  : 1; e();
        c(1); xs[c++] = f(0) ?  x() ?  x() ? (x() ? 5 : 2) : 3  : 4  : 1; e();
        c(1); xs[c++] = f(0) ?  x() ?  x() ?  x() ? 5 : 2  : 3  : 4  : 1; e();

        c(2); xs[c++] = t(0) ? (f(1) ? (x() ? (x() ? 4 : 2) : 3) : 1) : 5; e();
        c(2); xs[c++] = t(0) ?  f(1) ? (x() ? (x() ? 4 : 2) : 3) : 1  : 5; e();
        c(2); xs[c++] = t(0) ?  f(1) ?  x() ? (x() ? 4 : 2) : 3  : 1  : 5; e();
        c(2); xs[c++] = t(0) ?  f(1) ?  x() ?  x() ? 4 : 2  : 3  : 1  : 5; e();

        c(3); xs[c++] = t(0) ? (t(1) ? (f(2) ? (x() ? 3 : 2) : 1) : 4) : 5; e();
        c(3); xs[c++] = t(0) ?  t(1) ? (f(2) ? (x() ? 3 : 2) : 1) : 4  : 5; e();
        c(3); xs[c++] = t(0) ?  t(1) ?  f(2) ? (x() ? 3 : 2) : 1  : 4  : 5; e();
        c(3); xs[c++] = t(0) ?  t(1) ?  f(2) ?  x() ? 3 : 2  : 1  : 4  : 5; e();

        c(4); xs[c++] = t(0) ? (t(1) ? (t(2) ? (f(3) ? 2 : 1) : 3) : 4) : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ? (t(2) ? (f(3) ? 2 : 1) : 3) : 4  : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ?  t(2) ? (f(3) ? 2 : 1) : 3  : 4  : 5; e();
        c(4); xs[c++] = t(0) ?  t(1) ?  t(2) ?  f(3) ? 2 : 1  : 3  : 4  : 5; e();

        // b ? e : (b ? e : e)
        c(1); xs[c++] = t(0) ? 1 : (x() ? 2 : 3); e();
        c(1); xs[c++] = t(0) ? 1 :  x() ? 2 : 3 ; e();

        c(2); xs[c++] = f(0) ? 2 : (t(1) ? 1 : 3); e();
        c(2); xs[c++] = f(0) ? 2 :  t(1) ? 1 : 3 ; e();

        c(2); xs[c++] = f(0) ? 2 : (f(1) ? 3 : 1); e();
        c(2); xs[c++] = f(0) ? 2 :  f(1) ? 3 : 1 ; e();

        // b ? e : (b ? e : (b ? e : e))
        c(1); xs[c++] = t(0) ? 1 : (x() ? 2 : (x() ? 3 : 4)); e();
        c(1); xs[c++] = t(0) ? 1 :  x() ? 2 : (x() ? 3 : 4) ; e();
        c(1); xs[c++] = t(0) ? 1 :  x() ? 2 :  x() ? 3 : 4  ; e();

        c(2); xs[c++] = f(0) ? 2 : (t(1) ? 1 : (x() ? 3 : 4)); e();
        c(2); xs[c++] = f(0) ? 2 :  t(1) ? 1 : (x() ? 3 : 4) ; e();
        c(2); xs[c++] = f(0) ? 2 :  t(1) ? 1 :  x() ? 3 : 4  ; e();

        c(3); xs[c++] = f(0) ? 2 : (f(1) ? 3 : (t(2) ? 1 : 4)); e();
        c(3); xs[c++] = f(0) ? 2 :  f(1) ? 3 : (t(2) ? 1 : 4) ; e();
        c(3); xs[c++] = f(0) ? 2 :  f(1) ? 3 :  t(2) ? 1 : 4  ; e();

        c(3); xs[c++] = f(0) ? 2 : (f(1) ? 3 : (f(2) ? 4 : 1)); e();
        c(3); xs[c++] = f(0) ? 2 :  f(1) ? 3 : (f(2) ? 4 : 1) ; e();
        c(3); xs[c++] = f(0) ? 2 :  f(1) ? 3 :  f(2) ? 4 : 1  ; e();

        // b ? (b ? (b ? e : e) : (b ? e : e)) : e
        c(3); xs[c++] = t(0) ? (t(1) ? (t(2) ? 1 : 2) : (x() ? 3 : 4)) : 5; e();
        c(3); xs[c++] = t(0) ? (t(1) ?  t(2) ? 1 : 2  : (x() ? 3 : 4)) : 5; e();
        c(3); xs[c++] = t(0) ? (t(1) ? (t(2) ? 1 : 2) :  x() ? 3 : 4 ) : 5; e();
        c(3); xs[c++] = t(0) ?  t(1) ? (t(2) ? 1 : 2) :  x() ? 3 : 4   : 5; e();
        c(3); xs[c++] = t(0) ? (t(1) ?  t(2) ? 1 : 2  :  x() ? 3 : 4 ) : 5; e();
        c(3); xs[c++] = t(0) ?  t(1) ?  t(2) ? 1 : 2  :  x() ? 3 : 4   : 5; e();

        // b?e:e
        c(); xs[c++] = ot instanceof t ? 1 : 2; e();
        c(); xs[c++] = o instanceof t ? 2 : 1; e();

        // b ? (b : e : e) : e
        c(); xs[c++] = ot instanceof t ? o instanceof t  ? 2 : 1 : 3; e();
        c(); xs[c++] = ot instanceof t ? (o instanceof t  ? 2 : 1) : 3; e();

        // b ? ( b : e : e) : e
        c(); xs[c++] = o instanceof t ? x() ? 2 : 3 : 1; e();
        c(); xs[c++] = o instanceof t ? (x() ? 2 : 3) : 1; e();

        // b ? (b ? (b ? e : e) : e) : e
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (ot instanceof t ? 1 : 2) : 3) : 4; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (ot instanceof t ? 1 : 2) : 3  : 4; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ? 1 : 2  : 3  : 4; e();

        c(); xs[c++] = o instanceof t ? (x() ? (x() ? 4 : 2) : 3) : 1; e();
        c(); xs[c++] = o instanceof t ?  x() ? (x() ? 4 : 2) : 3  : 1; e();
        c(); xs[c++] = o instanceof t ?  x() ?  x() ? 4 : 2  : 3  : 1; e();

        c(); xs[c++] = ot instanceof t ? (o instanceof t ? (x() ? 2 : 3) : 1) : 4; e();
        c(); xs[c++] = ot instanceof t ?  o instanceof t ? (x() ? 2 : 3) : 1  : 4; e();
        c(); xs[c++] = ot instanceof t ?  o instanceof t ?  x() ? 2 : 3  : 1  : 4; e();

        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (o instanceof t ? 2 : 1) : 3) : 4; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (o instanceof t ? 2 : 1) : 3  : 4; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  o instanceof t ? 2 : 1  : 3  : 4; e();

        // b ? (b ? (b ? (b ? e : e) : e) : e) : e
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (ot instanceof t ? (ot instanceof t ? 1 : 2) : 3) : 4) : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (ot instanceof t ? (ot instanceof t ? 1 : 2) : 3) : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ? (ot instanceof t ? 1 : 2) : 3  : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ?  ot instanceof t ? 1 : 2  : 3  : 4  : 5; e();

        c(); xs[c++] = o instanceof t ? (x() ? (x() ? (x() ? 5 : 2) : 3) : 4) : 1; e();
        c(); xs[c++] = o instanceof t ?  x() ? (x() ? (x() ? 5 : 2) : 3) : 4  : 1; e();
        c(); xs[c++] = o instanceof t ?  x() ?  x() ? (x() ? 5 : 2) : 3  : 4  : 1; e();
        c(); xs[c++] = o instanceof t ?  x() ?  x() ?  x() ? 5 : 2  : 3  : 4  : 1; e();

        c(); xs[c++] = ot instanceof t ? (o instanceof t ? (x() ? (x() ? 4 : 2) : 3) : 1) : 5; e();
        c(); xs[c++] = ot instanceof t ?  o instanceof t ? (x() ? (x() ? 4 : 2) : 3) : 1  : 5; e();
        c(); xs[c++] = ot instanceof t ?  o instanceof t ?  x() ? (x() ? 4 : 2) : 3  : 1  : 5; e();
        c(); xs[c++] = ot instanceof t ?  o instanceof t ?  x() ?  x() ? 4 : 2  : 3  : 1  : 5; e();

        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (o instanceof t ? (x() ? 3 : 2) : 1) : 4) : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (o instanceof t ? (x() ? 3 : 2) : 1) : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  o instanceof t ? (x() ? 3 : 2) : 1  : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  o instanceof t ?  x() ? 3 : 2  : 1  : 4  : 5; e();

        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (ot instanceof t ? (o instanceof t ? 2 : 1) : 3) : 4) : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (ot instanceof t ? (o instanceof t ? 2 : 1) : 3) : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ? (o instanceof t ? 2 : 1) : 3  : 4  : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ?  o instanceof t ? 2 : 1  : 3  : 4  : 5; e();

        // b ? e : (b ? e : e)
        c(); xs[c++] = ot instanceof t ? 1 : (x() ? 2 : 3); e();
        c(); xs[c++] = ot instanceof t ? 1 :  x() ? 2 : 3 ; e();

        c(); xs[c++] = o instanceof t ? 2 : (ot instanceof t ? 1 : 3); e();
        c(); xs[c++] = o instanceof t ? 2 :  ot instanceof t ? 1 : 3 ; e();

        c(); xs[c++] = o instanceof t ? 2 : (o instanceof t ? 3 : 1); e();
        c(); xs[c++] = o instanceof t ? 2 :  o instanceof t ? 3 : 1 ; e();

        // b ? e : (b ? e : (b ? e : e))
        c(); xs[c++] = ot instanceof t ? 1 : (x() ? 2 : (x() ? 3 : 4)); e();
        c(); xs[c++] = ot instanceof t ? 1 :  x() ? 2 : (x() ? 3 : 4) ; e();
        c(); xs[c++] = ot instanceof t ? 1 :  x() ? 2 :  x() ? 3 : 4  ; e();

        c(); xs[c++] = o instanceof t ? 2 : (ot instanceof t ? 1 : (x() ? 3 : 4)); e();
        c(); xs[c++] = o instanceof t ? 2 :  ot instanceof t ? 1 : (x() ? 3 : 4) ; e();
        c(); xs[c++] = o instanceof t ? 2 :  ot instanceof t ? 1 :  x() ? 3 : 4  ; e();

        c(); xs[c++] = o instanceof t ? 2 : (o instanceof t ? 3 : (ot instanceof t ? 1 : 4)); e();
        c(); xs[c++] = o instanceof t ? 2 :  o instanceof t ? 3 : (ot instanceof t ? 1 : 4) ; e();
        c(); xs[c++] = o instanceof t ? 2 :  o instanceof t ? 3 :  ot instanceof t ? 1 : 4  ; e();

        c(); xs[c++] = o instanceof t ? 2 : (o instanceof t ? 3 : (o instanceof t ? 4 : 1)); e();
        c(); xs[c++] = o instanceof t ? 2 :  o instanceof t ? 3 : (o instanceof t ? 4 : 1) ; e();
        c(); xs[c++] = o instanceof t ? 2 :  o instanceof t ? 3 :  o instanceof t ? 4 : 1  ; e();

        // b ? (b ? (b ? e : e) : (b ? e : e)) : e
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (ot instanceof t ? 1 : 2) : (x() ? 3 : 4)) : 5; e();
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ?  ot instanceof t ? 1 : 2  : (x() ? 3 : 4)) : 5; e();
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ? (ot instanceof t ? 1 : 2) :  x() ? 3 : 4 ) : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ? (ot instanceof t ? 1 : 2) :  x() ? 3 : 4   : 5; e();
        c(); xs[c++] = ot instanceof t ? (ot instanceof t ?  ot instanceof t ? 1 : 2  :  x() ? 3 : 4 ) : 5; e();
        c(); xs[c++] = ot instanceof t ?  ot instanceof t ?  ot instanceof t ? 1 : 2  :  x() ? 3 : 4   : 5; e();

        // += b ? e : e
        c(1,0); xs[c++] = z   += t(0) ? 1 : 9; e();
        c(1,2); xs[c++] = z   -= t(0) ? 1 : 9; e();
        c(1,1); xs[c++] = z   *= t(0) ? 1 : 9; e();
        c(1,1); xs[c++] = z   /= t(0) ? 1 : 9; e();
        c(1,1); xs[c++] = z   &= t(0) ? 1 : 9; e();
        c(1,0); xs[c++] = z   |= t(0) ? 1 : 9; e();
        c(1,3); xs[c++] = z   ^= t(0) ? 2 : 9; e();
        c(1,3); xs[c++] = z   %= t(0) ? 2 : 9; e();
        c(1,0); xs[c++] = z  <<= t(0) ? 1 : 9; e();
        c(1,3); xs[c++] = z  >>= t(0) ? 1 : 9; e(); 
        c(1,3); xs[c++] = z >>>= t(0) ? 1 : 0; e();

        c(1,0); xs[c++] = z   += f(0) ? 9 : 1; e();
        c(1,2); xs[c++] = z   -= f(0) ? 9 : 1; e();
        c(1,1); xs[c++] = z   *= f(0) ? 9 : 1; e();
        c(1,1); xs[c++] = z   /= f(0) ? 9 : 1; e();
        c(1,1); xs[c++] = z   &= f(0) ? 9 : 1; e();
        c(1,0); xs[c++] = z   |= f(0) ? 9 : 1; e();
        c(1,3); xs[c++] = z   ^= f(0) ? 9 : 2; e();
        c(1,3); xs[c++] = z   %= f(0) ? 9 : 2; e();
        c(1,0); xs[c++] = z  <<= f(0) ? 9 : 1; e();
        c(1,3); xs[c++] = z  >>= f(0) ? 9 : 1; e(); 
        c(1,3); xs[c++] = z >>>= f(0) ? 0 : 1; e();

        // b ? e + (b ? e : e) : e
        c(2); xs[c++] = t(0) ? 2 + (t(1) ? -1 :  1) : 3; e();
        c(1); xs[c++] = f(0) ? 2 + (x()  ?  3 :  1) : 1; e();
        c(2); xs[c++] = t(0) ? 2 + (f(1) ?  1 : -1) : 3; e();

        // b ? e + (b ? e : e) : (b ? e : e)
        c(2); xs[c++] = t(0) ? 2 + (t(1) ? -1 : 1) : (x() ? 3 : 4); e();
        c(2); xs[c++] = t(0) ? 2 + (t(1) ? -1 : 1) :  x() ? 3 : 4 ; e();

        c(2); xs[c++] = f(0) ? 2 + (x() ? -1 : 1) : (t(1) ? 1 : 4); e();
        c(2); xs[c++] = f(0) ? 2 + (x() ? -1 : 1) :  t(1) ? 1 : 4 ; e();

        c(2); xs[c++] = f(0) ? 2 + (x() ? -1 : 1) : (f(1) ? 4 : 1); e();
        c(2); xs[c++] = f(0) ? 2 + (x() ? -1 : 1) :  f(1) ? 4 : 1 ; e();

        c(2); xs[c++] = t(0) ? 2 + (f(1) ? 1 : -1) : (x() ? 3 : 4); e();
        c(2); xs[c++] = t(0) ? 2 + (f(1) ? 1 : -1) :  x() ? 3 : 4 ; e();

        // b ? z = (b ? e : e) : e;
        // problems
        c(2,-1); xs[c++] = t(0) ? z = (t(1) ? 1 : 2) : 3; e(1);
        c(2,-1); xs[c++] = t(0) ? z =  t(1) ? 1 : 2  : 3; e(1);

        c(1,-1); xs[c++] = f(0) ? z = (x() ? 3 : 2) : 1; e(-1);
        c(1,-1); xs[c++] = f(0) ? z =  x() ? 3 : 2  : 1; e(-1);

        // b ? (z = (b ? e : e)) : (b ? e : e)
        // problems
        c(2,-1); xs[c++] = t(0) ? (z = (t(1) ? 1 : 2)) : (x() ? 3 : 4); e(1);
        c(2,-1); xs[c++] = t(0) ?  z = (t(1) ? 1 : 2)  : (x() ? 3 : 4); e(1);
        c(2,-1); xs[c++] = t(0) ? (z = (t(1) ? 1 : 2)) :  x() ? 3 : 4 ; e(1);
        c(2,-1); xs[c++] = t(0) ? (z =  t(1) ? 1 : 2 ) : (x() ? 3 : 4); e(1);
        c(2,-1); xs[c++] = t(0) ? (z =  t(1) ? 1 : 2 ) :  x() ? 3 : 4 ; e(1);
        c(2,-1); xs[c++] = t(0) ?  z = (t(1) ? 1 : 2 ) :  x() ? 3 : 4 ; e(1);
        c(2,-1); xs[c++] = t(0) ?  z =  t(1) ? 1 : 2   :  x() ? 3 : 4 ; e(1);

        c(2,-1); xs[c++] = f(0) ? (z = (x() ? 3 : 2)) : (t(1) ? 1 : 4); e(-1);
        c(2,-1); xs[c++] = f(0) ?  z = (x() ? 3 : 2)  : (t(1) ? 1 : 4); e(-1);
        c(2,-1); xs[c++] = f(0) ? (z = (x() ? 3 : 2)) :  t(1) ? 1 : 4 ; e(-1);
        c(2,-1); xs[c++] = f(0) ? (z =  x() ? 3 : 2 ) : (t(1) ? 1 : 4); e(-1);
        c(2,-1); xs[c++] = f(0) ? (z =  x() ? 3 : 2 ) :  t(1) ? 1 : 4 ; e(-1);
        c(2,-1); xs[c++] = f(0) ?  z = (x() ? 3 : 2 ) :  t(1) ? 1 : 4 ; e(-1);
        c(2,-1); xs[c++] = f(0) ?  z =  x() ? 3 : 2   :  t(1) ? 1 : 4 ; e(-1);

        c(2,-1); xs[c++] = f(0) ? (z = (x() ? 3:  2)) : (f(1) ? 4 : 1); e(-1);
        c(2,-1); xs[c++] = f(0) ?  z = (x() ? 3 : 2)  : (f(1) ? 4 : 1); e(-1);
        c(2,-1); xs[c++] = f(0) ? (z = (x() ? 3 : 2)) :  f(1) ? 4 : 1 ; e(-1);
        c(2,-1); xs[c++] = f(0) ? (z =  x() ? 3 : 2 ) : (f(1) ? 4 : 1); e(-1);
        c(2,-1); xs[c++] = f(0) ? (z =  x() ? 3 : 2 ) :  f(1) ? 4 : 1 ; e(-1);
        c(2,-1); xs[c++] = f(0) ?  z = (x() ? 3 : 2 ) :  f(1) ? 4 : 1 ; e(-1);
        c(2,-1); xs[c++] = f(0) ?  z =  x() ? 3 : 2   :  f(1) ? 4 : 1 ; e(-1);


        for (int i = 0; i < xs.length; i++) {
            if (xs[i] != 0) {
                Tester.check(xs[i]==1, "xs["+i+"]=" + xs[i] + ", not 1!");
            }
        }

    }

    static class t {}
    
    int cur = 0;
    int expect = -1;
    private void c(int expect, int z) { this.expect = expect; cur = 0; this.z = z; }
    private void c(int expect) { c(expect, 0); }
    private void c() { c(-1); }
    private void e(int z) {
        if (z != -123) {
            Tester.check(this.z == z, c + ": z=" + this.z + ", should be " + z);
        }
        if (expect >= 0) {
            Tester.check(expect == cur, c + ": expect=" + expect + ", should be " + cur);
        }
    }
    private void e() { e(-123); }
    private boolean t(int i) { return b(i, true); }
    private boolean f(int i) { return b(i, false); }
    private boolean x()      { return b(-1, false); }
    private boolean b(int i, boolean b) {
        Tester.check(i >= 0, c + ": this expression shouldn't have been evaluated");
         if (i >= 0) Tester.check(i == cur, c + ": i=" + i + ", should be " + cur);
        cur++;
        return b;
    }


}
