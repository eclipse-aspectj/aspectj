package p1;
public class P1Statics {

    public static Integer I = new Integer(1 +1);
    public static byte    b = (byte)      1 +1 ;
    public static short   s = (short)     1 +1 ;
    public static int     i = (int)       1 +1 ;
    public static long    l = (long)      1 +1 ;
    public static float   f = (float)     1 +1 ;
    public static double  d = (double)    1 +1 ;
    public static char    c = (char)     '1'+1 ;

    public static class NestedStaticClass {

        public static Integer I = new Integer(2 +1);
        public static byte    b = (byte)      2 +1 ;
        public static short   s = (short)     2 +1 ;
        public static int     i = (int)       2 +1 ;
        public static long    l = (long)      2 +1 ;
        public static float   f = (float)     2 +1 ;
        public static double  d = (double)    2 +1 ;
        public static char    c = (char)     '2'+1 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +1);
            public static byte    b = (byte)      3 +1 ;
            public static short   s = (short)     3 +1 ;
            public static int     i = (int)       3 +1 ;
            public static long    l = (long)      3 +1 ;
            public static float   f = (float)     3 +1 ;
            public static double  d = (double)    3 +1 ;
            public static char    c = (char)     '3'+1 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +1);
            public static byte    b = (byte)      4 +1 ;
            public static short   s = (short)     4 +1 ;
            public static int     i = (int)       4 +1 ;
            public static long    l = (long)      4 +1 ;
            public static float   f = (float)     4 +1 ;
            public static double  d = (double)    4 +1 ;
            public static char    c = (char)     '4'+1 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +1);
            public static byte    b = (byte)      5 +1 ;
            public static short   s = (short)     5 +1 ;
            public static int     i = (int)       5 +1 ;
            public static long    l = (long)      5 +1 ;
            public static float   f = (float)     5 +1 ;
            public static double  d = (double)    5 +1 ;
            public static char    c = (char)     '5'+1 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +1);
            public byte    b = (byte)      6 +1 ;
            public short   s = (short)     6 +1 ;
            public int     i = (int)       6 +1 ;
            public long    l = (long)      6 +1 ;
            public float   f = (float)     6 +1 ;
            public double  d = (double)    6 +1 ;
            public char    c = (char)     '6'+1 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +1);
            public byte    b = (byte)      7 +1 ;
            public short   s = (short)     7 +1 ;
            public int     i = (int)       7 +1 ;
            public long    l = (long)      7 +1 ;
            public float   f = (float)     7 +1 ;
            public double  d = (double)    7 +1 ;
            public char    c = (char)     '7'+1 ;
        }
    }

    public static interface NestedStaticInterface {

        public static Integer I = new Integer(2 +11);
        public static byte    b = (byte)      2 +11 ;
        public static short   s = (short)     2 +11 ;
        public static int     i = (int)       2 +11 ;
        public static long    l = (long)      2 +11 ;
        public static float   f = (float)     2 +11 ;
        public static double  d = (double)    2 +11 ;
        public static char    c = (char)     '2'+11 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +11);
            public static byte    b = (byte)      3 +11 ;
            public static short   s = (short)     3 +11 ;
            public static int     i = (int)       3 +11 ;
            public static long    l = (long)      3 +11 ;
            public static float   f = (float)     3 +11 ;
            public static double  d = (double)    3 +11 ;
            public static char    c = (char)     '3'+11 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +11);
            public static byte    b = (byte)      4 +11 ;
            public static short   s = (short)     4 +11 ;
            public static int     i = (int)       4 +11 ;
            public static long    l = (long)      4 +11 ;
            public static float   f = (float)     4 +11 ;
            public static double  d = (double)    4 +11 ;
            public static char    c = (char)     '4'+11 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +11);
            public static byte    b = (byte)      5 +11 ;
            public static short   s = (short)     5 +11 ;
            public static int     i = (int)       5 +11 ;
            public static long    l = (long)      5 +11 ;
            public static float   f = (float)     5 +11 ;
            public static double  d = (double)    5 +11 ;
            public static char    c = (char)     '5'+11 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +11);
            public byte    b = (byte)      6 +11 ;
            public short   s = (short)     6 +11 ;
            public int     i = (int)       6 +11 ;
            public long    l = (long)      6 +11 ;
            public float   f = (float)     6 +11 ;
            public double  d = (double)    6 +11 ;
            public char    c = (char)     '6'+11 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +11);
            public byte    b = (byte)      7 +11 ;
            public short   s = (short)     7 +11 ;
            public int     i = (int)       7 +11 ;
            public long    l = (long)      7 +11 ;
            public float   f = (float)     7 +11 ;
            public double  d = (double)    7 +11 ;
            public char    c = (char)     '7'+11 ;
        }
    }

    public interface NestedInterface {

        public static Integer I = new Integer(2 +21);
        public static byte    b = (byte)      2 +21 ;
        public static short   s = (short)     2 +21 ;
        public static int     i = (int)       2 +21 ;
        public static long    l = (long)      2 +21 ;
        public static float   f = (float)     2 +21 ;
        public static double  d = (double)    2 +21 ;
        public static char    c = (char)     '2'+21 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +21);
            public static byte    b = (byte)      3 +21 ;
            public static short   s = (short)     3 +21 ;
            public static int     i = (int)       3 +21 ;
            public static long    l = (long)      3 +21 ;
            public static float   f = (float)     3 +21 ;
            public static double  d = (double)    3 +21 ;
            public static char    c = (char)     '3'+21 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +21);
            public static byte    b = (byte)      4 +21 ;
            public static short   s = (short)     4 +21 ;
            public static int     i = (int)       4 +21 ;
            public static long    l = (long)      4 +21 ;
            public static float   f = (float)     4 +21 ;
            public static double  d = (double)    4 +21 ;
            public static char    c = (char)     '4'+21 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +21);
            public static byte    b = (byte)      5 +21 ;
            public static short   s = (short)     5 +21 ;
            public static int     i = (int)       5 +21 ;
            public static long    l = (long)      5 +21 ;
            public static float   f = (float)     5 +21 ;
            public static double  d = (double)    5 +21 ;
            public static char    c = (char)     '5'+21 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +21);
            public byte    b = (byte)      6 +21 ;
            public short   s = (short)     6 +21 ;
            public int     i = (int)       6 +21 ;
            public long    l = (long)      6 +21 ;
            public float   f = (float)     6 +21 ;
            public double  d = (double)    6 +21 ;
            public char    c = (char)     '6'+21 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +21);
            public byte    b = (byte)      7 +21 ;
            public short   s = (short)     7 +21 ;
            public int     i = (int)       7 +21 ;
            public long    l = (long)      7 +21 ;
            public float   f = (float)     7 +21 ;
            public double  d = (double)    7 +21 ;
            public char    c = (char)     '7'+21 ;
        }
    }    
}
