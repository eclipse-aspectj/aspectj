public \u0063l\u0061ss Driver {

  public static void main(String[] args) { test(); }

  public static void test() {

    // integer literals
    int  dec = 5;
    long longDec  = 5;
    long longDecL = 5L;

    int  hex = 0xAbcdE;
    long longHex  = 0xAbcdE;
    long longHexL = 0xAbcdEL;

    int  oct = 0762;
    long longOct  = 0762;
    long longOctL = 0762L;

    // boolean literals
    boolean btrue  = true;
    boolean bfalse = false;

    // float literals
    float f1 = 1e1f, f2 = 2.f, f3 = .3f, f4 = 3.14f, f5 = 6.023e+23f;

    // character literals
    char 
      // c1 = '\u2352', 
      c2 = '\u0063'; // 'c'
      //      c3 = '\u0007';

    // string literals
    String \u0063 = "c";  // String c = "c";
    String s1 = "";
    String s2 = "\u0063"; // the string "c";
    //    String s3 = "\u3333"; // uncommenting this will break weaver

    // string literals with escapes
   String bs = "\b";
   String ht = "\t";
   String lf = "\n";
   String cr = "\r";
   String dq = "\"";
   String sq = "\'";
   String backslash = "\\";
   String oes = "\u0000"; // octal escape smallest
   String oeb = "\u00ff"; // octal escape biggest
   String ctrlg = "";   // this turns into "\u0007" by the time it is parsed.
   String random = "\u3333"; 
  }
}

