package p1.p2;
public class P1P2Statics {

    public static Integer I = new Integer(1 +2);
    public static byte    b = (byte)      1 +2 ;
    public static short   s = (short)     1 +2 ;
    public static int     i = (int)       1 +2 ;
    public static long    l = (long)      1 +2 ;
    public static float   f = (float)     1 +2 ;
    public static double  d = (double)    1 +2 ;
    public static char    c = (char)     '1'+2 ;

    public static class NestedStaticClass {

        public static Integer I = new Integer(2 +2);
        public static byte    b = (byte)      2 +2 ;
        public static short   s = (short)     2 +2 ;
        public static int     i = (int)       2 +2 ;
        public static long    l = (long)      2 +2 ;
        public static float   f = (float)     2 +2 ;
        public static double  d = (double)    2 +2 ;
        public static char    c = (char)     '2'+2 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +2);
            public static byte    b = (byte)      3 +2 ;
            public static short   s = (short)     3 +2 ;
            public static int     i = (int)       3 +2 ;
            public static long    l = (long)      3 +2 ;
            public static float   f = (float)     3 +2 ;
            public static double  d = (double)    3 +2 ;
            public static char    c = (char)     '3'+2 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +2);
            public static byte    b = (byte)      4 +2 ;
            public static short   s = (short)     4 +2 ;
            public static int     i = (int)       4 +2 ;
            public static long    l = (long)      4 +2 ;
            public static float   f = (float)     4 +2 ;
            public static double  d = (double)    4 +2 ;
            public static char    c = (char)     '4'+2 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +2);
            public static byte    b = (byte)      5 +2 ;
            public static short   s = (short)     5 +2 ;
            public static int     i = (int)       5 +2 ;
            public static long    l = (long)      5 +2 ;
            public static float   f = (float)     5 +2 ;
            public static double  d = (double)    5 +2 ;
            public static char    c = (char)     '5'+2 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +2);
            public byte    b = (byte)      6 +2 ;
            public short   s = (short)     6 +2 ;
            public int     i = (int)       6 +2 ;
            public long    l = (long)      6 +2 ;
            public float   f = (float)     6 +2 ;
            public double  d = (double)    6 +2 ;
            public char    c = (char)     '6'+2 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +2);
            public byte    b = (byte)      7 +2 ;
            public short   s = (short)     7 +2 ;
            public int     i = (int)       7 +2 ;
            public long    l = (long)      7 +2 ;
            public float   f = (float)     7 +2 ;
            public double  d = (double)    7 +2 ;
            public char    c = (char)     '7'+2 ;
        }
    }

    public static interface NestedStaticInterface {

        public static Integer I = new Integer(2 +12);
        public static byte    b = (byte)      2 +12 ;
        public static short   s = (short)     2 +12 ;
        public static int     i = (int)       2 +12 ;
        public static long    l = (long)      2 +12 ;
        public static float   f = (float)     2 +12 ;
        public static double  d = (double)    2 +12 ;
        public static char    c = (char)     '2'+12 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +12);
            public static byte    b = (byte)      3 +12 ;
            public static short   s = (short)     3 +12 ;
            public static int     i = (int)       3 +12 ;
            public static long    l = (long)      3 +12 ;
            public static float   f = (float)     3 +12 ;
            public static double  d = (double)    3 +12 ;
            public static char    c = (char)     '3'+12 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +12);
            public static byte    b = (byte)      4 +12 ;
            public static short   s = (short)     4 +12 ;
            public static int     i = (int)       4 +12 ;
            public static long    l = (long)      4 +12 ;
            public static float   f = (float)     4 +12 ;
            public static double  d = (double)    4 +12 ;
            public static char    c = (char)     '4'+12 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +12);
            public static byte    b = (byte)      5 +12 ;
            public static short   s = (short)     5 +12 ;
            public static int     i = (int)       5 +12 ;
            public static long    l = (long)      5 +12 ;
            public static float   f = (float)     5 +12 ;
            public static double  d = (double)    5 +12 ;
            public static char    c = (char)     '5'+12 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +12);
            public byte    b = (byte)      6 +12 ;
            public short   s = (short)     6 +12 ;
            public int     i = (int)       6 +12 ;
            public long    l = (long)      6 +12 ;
            public float   f = (float)     6 +12 ;
            public double  d = (double)    6 +12 ;
            public char    c = (char)     '6'+12 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +12);
            public byte    b = (byte)      7 +12 ;
            public short   s = (short)     7 +12 ;
            public int     i = (int)       7 +12 ;
            public long    l = (long)      7 +12 ;
            public float   f = (float)     7 +12 ;
            public double  d = (double)    7 +12 ;
            public char    c = (char)     '7'+12 ;
        }
    }

    public interface NestedInterface {

        public static Integer I = new Integer(2 +22);
        public static byte    b = (byte)      2 +22 ;
        public static short   s = (short)     2 +22 ;
        public static int     i = (int)       2 +22 ;
        public static long    l = (long)      2 +22 ;
        public static float   f = (float)     2 +22 ;
        public static double  d = (double)    2 +22 ;
        public static char    c = (char)     '2'+22 ;

        public static class InnerStaticClass {
            public static Integer I = new Integer(3 +22);
            public static byte    b = (byte)      3 +22 ;
            public static short   s = (short)     3 +22 ;
            public static int     i = (int)       3 +22 ;
            public static long    l = (long)      3 +22 ;
            public static float   f = (float)     3 +22 ;
            public static double  d = (double)    3 +22 ;
            public static char    c = (char)     '3'+22 ;
        }

        public static interface InnerStaticInterface {
            public static Integer I = new Integer(4 +22);
            public static byte    b = (byte)      4 +22 ;
            public static short   s = (short)     4 +22 ;
            public static int     i = (int)       4 +22 ;
            public static long    l = (long)      4 +22 ;
            public static float   f = (float)     4 +22 ;
            public static double  d = (double)    4 +22 ;
            public static char    c = (char)     '4'+22 ;
        }

        public interface InnerInterface {
            public static Integer I = new Integer(5 +22);
            public static byte    b = (byte)      5 +22 ;
            public static short   s = (short)     5 +22 ;
            public static int     i = (int)       5 +22 ;
            public static long    l = (long)      5 +22 ;
            public static float   f = (float)     5 +22 ;
            public static double  d = (double)    5 +22 ;
            public static char    c = (char)     '5'+22 ;
        }

        public static interface InnerStaticInterfaceNoStatics {
            public Integer I = new Integer(6 +22);
            public byte    b = (byte)      6 +22 ;
            public short   s = (short)     6 +22 ;
            public int     i = (int)       6 +22 ;
            public long    l = (long)      6 +22 ;
            public float   f = (float)     6 +22 ;
            public double  d = (double)    6 +22 ;
            public char    c = (char)     '6'+22 ;
        }

        public interface InnerInterfaceNoStatics {
            public Integer I = new Integer(7 +22);
            public byte    b = (byte)      7 +22 ;
            public short   s = (short)     7 +22 ;
            public int     i = (int)       7 +22 ;
            public long    l = (long)      7 +22 ;
            public float   f = (float)     7 +22 ;
            public double  d = (double)    7 +22 ;
            public char    c = (char)     '7'+22 ;
        }
    }    
}
