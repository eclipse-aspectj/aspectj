public class Statics {

    public static Integer I = new Integer(1 +0);
    public static byte    b = (byte)      1 +0 ;
    public static short   s = (short)     1 +0 ;
    public static int     i = (int)       1 +0 ;
    public static long    l = (long)      1 +0 ;
    public static float   f = (float)     1 +0 ;
    public static double  d = (double)    1 +0 ;
    public static char    c = (char)     '1'+0 ;

    public static class NestedStaticClass {

        public static Integer I = new Integer(2 +0);
        public static byte    b = (byte)      2 +0 ;
        public static short   s = (short)     2 +0 ;
        public static int     i = (int)       2 +0 ;
        public static long    l = (long)      2 +0 ;
        public static float   f = (float)     2 +0 ;
        public static double  d = (double)    2 +0 ;
        public static char    c = (char)     '2'+0 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +0);
            public static byte    b = (byte)      3 +0 ;
            public static short   s = (short)     3 +0 ;
            public static int     i = (int)       3 +0 ;
            public static long    l = (long)      3 +0 ;
            public static float   f = (float)     3 +0 ;
            public static double  d = (double)    3 +0 ;
            public static char    c = (char)     '3'+0 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +0);
            public static byte    b = (byte)      4 +0 ;
            public static short   s = (short)     4 +0 ;
            public static int     i = (int)       4 +0 ;
            public static long    l = (long)      4 +0 ;
            public static float   f = (float)     4 +0 ;
            public static double  d = (double)    4 +0 ;
            public static char    c = (char)     '4'+0 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +0);
            public static byte    b = (byte)      5 +0 ;
            public static short   s = (short)     5 +0 ;
            public static int     i = (int)       5 +0 ;
            public static long    l = (long)      5 +0 ;
            public static float   f = (float)     5 +0 ;
            public static double  d = (double)    5 +0 ;
            public static char    c = (char)     '5'+0 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +0);
            public byte    b = (byte)      6 +0 ;
            public short   s = (short)     6 +0 ;
            public int     i = (int)       6 +0 ;
            public long    l = (long)      6 +0 ;
            public float   f = (float)     6 +0 ;
            public double  d = (double)    6 +0 ;
            public char    c = (char)     '6'+0 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +0);
            public byte    b = (byte)      7 +0 ;
            public short   s = (short)     7 +0 ;
            public int     i = (int)       7 +0 ;
            public long    l = (long)      7 +0 ;
            public float   f = (float)     7 +0 ;
            public double  d = (double)    7 +0 ;
            public char    c = (char)     '7'+0 ;
        }
    }

    public static interface NestedStaticInterface {

        public static Integer I = new Integer(2 +10);
        public static byte    b = (byte)      2 +10 ;
        public static short   s = (short)     2 +10 ;
        public static int     i = (int)       2 +10 ;
        public static long    l = (long)      2 +10 ;
        public static float   f = (float)     2 +10 ;
        public static double  d = (double)    2 +10 ;
        public static char    c = (char)     '2'+10 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +10);
            public static byte    b = (byte)      3 +10 ;
            public static short   s = (short)     3 +10 ;
            public static int     i = (int)       3 +10 ;
            public static long    l = (long)      3 +10 ;
            public static float   f = (float)     3 +10 ;
            public static double  d = (double)    3 +10 ;
            public static char    c = (char)     '3'+10 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +10);
            public static byte    b = (byte)      4 +10 ;
            public static short   s = (short)     4 +10 ;
            public static int     i = (int)       4 +10 ;
            public static long    l = (long)      4 +10 ;
            public static float   f = (float)     4 +10 ;
            public static double  d = (double)    4 +10 ;
            public static char    c = (char)     '4'+10 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +10);
            public static byte    b = (byte)      5 +10 ;
            public static short   s = (short)     5 +10 ;
            public static int     i = (int)       5 +10 ;
            public static long    l = (long)      5 +10 ;
            public static float   f = (float)     5 +10 ;
            public static double  d = (double)    5 +10 ;
            public static char    c = (char)     '5'+10 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +10);
            public byte    b = (byte)      6 +10 ;
            public short   s = (short)     6 +10 ;
            public int     i = (int)       6 +10 ;
            public long    l = (long)      6 +10 ;
            public float   f = (float)     6 +10 ;
            public double  d = (double)    6 +10 ;
            public char    c = (char)     '6'+10 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +10);
            public byte    b = (byte)      7 +10 ;
            public short   s = (short)     7 +10 ;
            public int     i = (int)       7 +10 ;
            public long    l = (long)      7 +10 ;
            public float   f = (float)     7 +10 ;
            public double  d = (double)    7 +10 ;
            public char    c = (char)     '7'+10 ;
        }
    }

    public interface NestedInterface {

        public static Integer I = new Integer(2 +20);
        public static byte    b = (byte)      2 +20 ;
        public static short   s = (short)     2 +20 ;
        public static int     i = (int)       2 +20 ;
        public static long    l = (long)      2 +20 ;
        public static float   f = (float)     2 +20 ;
        public static double  d = (double)    2 +20 ;
        public static char    c = (char)     '2'+20 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +20);
            public static byte    b = (byte)      3 +20 ;
            public static short   s = (short)     3 +20 ;
            public static int     i = (int)       3 +20 ;
            public static long    l = (long)      3 +20 ;
            public static float   f = (float)     3 +20 ;
            public static double  d = (double)    3 +20 ;
            public static char    c = (char)     '3'+20 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +20);
            public static byte    b = (byte)      4 +20 ;
            public static short   s = (short)     4 +20 ;
            public static int     i = (int)       4 +20 ;
            public static long    l = (long)      4 +20 ;
            public static float   f = (float)     4 +20 ;
            public static double  d = (double)    4 +20 ;
            public static char    c = (char)     '4'+20 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +20);
            public static byte    b = (byte)      5 +20 ;
            public static short   s = (short)     5 +20 ;
            public static int     i = (int)       5 +20 ;
            public static long    l = (long)      5 +20 ;
            public static float   f = (float)     5 +20 ;
            public static double  d = (double)    5 +20 ;
            public static char    c = (char)     '5'+20 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +20);
            public byte    b = (byte)      6 +20 ;
            public short   s = (short)     6 +20 ;
            public int     i = (int)       6 +20 ;
            public long    l = (long)      6 +20 ;
            public float   f = (float)     6 +20 ;
            public double  d = (double)    6 +20 ;
            public char    c = (char)     '6'+20 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +20);
            public byte    b = (byte)      7 +20 ;
            public short   s = (short)     7 +20 ;
            public int     i = (int)       7 +20 ;
            public long    l = (long)      7 +20 ;
            public float   f = (float)     7 +20 ;
            public double  d = (double)    7 +20 ;
            public char    c = (char)     '7'+20 ;
        }
    }    
}
