import org.aspectj.testing.Tester; 
public class C {

    public void c() { a("C.c()"); }
    public void c(C c) { a("C.c(C)"); }
    public void c(C c, I i) { a("C.c(C,I)"); }
    public void c(C c, I.J ij) { a("C.c(C,I$J)"); }
    public void c(C c, I.J.K ijk) { a("C.c(C,I$J$K)"); }
    public void c(C c, I i, I.J ij) { a("C.c(C,I,I$J)"); }
    public void c(C c, I i, I.J.K ijk) { a("C.c(C,I,I$J$K)"); }
    public void c(C c, I.J ij, I.J.K ijk) { a("C.c(C,I$J,I$J$K)"); }
    public void c(C c, I i, I.J ij, I.J.K ijk) { a("C.c(C,I,I$J,I$J$K)"); }
    public void c(I i) { a("C.c(I)"); }
    public void c(I.J ij) { a("C.c(I$J)"); }
    public void c(I.J.K ijk) { a("C.c(I$J$K)"); }
    public void c(I i, I.J ij) { a("C.c(I,I$J)"); }
    public void c(I i, I.J.K ijk) { a("C.c(I,I$J$K)"); }
    public void c(I.J ij, I.J.K ijk) { a("C.c(I$J,I$J$K)"); }
    public void c(I i, I.J ij, I.J.K ijk) { a("C.c(I,I$J,I$J$K)"); }
    
    public static class I {

        public void i() { a("C$I.i()"); }
        public void i(C c) { a("C$I.i(C)"); }
        public void i(C c, I i) { a("C$I.i(C,I)"); }
        public void i(C c, J j) { a("C$I.i(C,J)"); }
        public void i(C c, J.K jk) { a("C$I.i(C,J$K)"); }
        public void i(C c, I i, J j) { a("C$I.i(C,I,J)"); }
        public void i(C c, I i, J.K jk) { a("C$I.i(C,I,J$K)"); }
        public void i(C c, J j, J.K jk) { a("C$I.i(C,J,J$K)"); }
        public void i(C c, I i, J j, J.K jk) { a("C$I.i(C,I,J,J$K)"); }
        public void i(I i) { a("C$I.i(I)"); }
        public void i(J j) { a("C$I.i(J)"); }
        public void i(J.K jk) { a("C$I.i(J$K)"); }
        public void i(I i, J j) { a("C$I.i(I,J)"); }
        public void i(I i, J.K jk) { a("C$I.i(I,J$K)"); }
        public void i(J j, J.K jk) { a("C$I.i(J,J$K)"); }
        public void i(I i, J j, J.K jk) { a("C$I.i(I,J,J$K)"); }
        
        public static class J {

            public void j() { a("C$I$J.j()"); }
            public void j(C c) { a("C$I$J.j(C)"); }
            public void j(C c, I i) { a("C$I$J.j(C,I)"); }
            public void j(C c, J j) { a("C$I$J.j(C,J)"); }
            public void j(C c, K k) { a("C$I$J.j(C,K)"); }
            public void j(C c, I i, J j) { a("C$I$J.j(C,I,J)"); }
            public void j(C c, I i, K k) { a("C$I$J.j(C,I,K)"); }
            public void j(C c, J j, K k) { a("C$I$J.j(C,J,K)"); }
            public void j(C c, I i, J j, K k) { a("C$I$J.j(C,I,J,K)"); }
            public void j(I i) { a("C$I$J.j(I)"); }
            public void j(J j) { a("C$I$J.j(J)"); }
            public void j(K k) { a("C$I$J.j(K)"); }
            public void j(I i, J j) { a("C$I$J.j(I,J)"); }
            public void j(I i, K k) { a("C$I$J.j(I,K)"); }
            public void j(J j, K k) { a("C$I$J.j(J,K)"); }
            public void j(I i, J j, K k) { a("C$I$J.j(I,J,K)"); }
            
            public static class K {

                public void k() { a("C$I$J$K.k()"); }
                public void k(C c) { a("C$I$J$K.k(C)"); }
                public void k(C c, I i) { a("C$I$J$K.k(C,I)"); }
                public void k(C c, J j) { a("C$I$J$K.k(C,J)"); }
                public void k(C c, K k) { a("C$I$J$K.k(C,K)"); }
                public void k(C c, I i, J j) { a("C$I$J$K.k(C,I,J)"); }
                public void k(C c, I i, K k) { a("C$I$J$K.k(C,I,K)"); }
                public void k(C c, J j, K k) { a("C$I$J$K.k(C,J,K)"); }
                public void k(C c, I i, J j, K k) { a("C$I$J$K.k(C,I,J,K)"); }
                public void k(I i) { a("C$I$J$K.k(I)"); }
                public void k(J j) { a("C$I$J$K.k(J)"); }
                public void k(K k) { a("C$I$J$K.k(K)"); }
                public void k(I i, J j) { a("C$I$J$K.k(I,J)"); }
                public void k(I i, K k) { a("C$I$J$K.k(I,K)"); }
                public void k(J j, K k) { a("C$I$J$K.k(J,K)"); }
                public void k(I i, J j, K k) { a("C$I$J$K.k(I,J,K)"); }

            }
        }
    }

    public static void a(String msg) { Tester.event(msg); }
}
