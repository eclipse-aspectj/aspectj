import org.aspectj.testing.*;

public class Gets {
    public static void main(String[] args) {
        new Gets().go();
    }

    public int nums0 =  5;
    public int nums1 =  5;
    public int nums2 =  5;
    public int nums3 =  5;
    public int nums4 = 20;
    public int nums5 =  9;
    public int nums6 = 11;
    public int nums7 =  9;
    public int nums8 = 11;
    public int nums9 =  5;

    void go() {
        nums0 =  10;
        nums1 +=  5;
        nums2 -= -5;
        nums3 *=  2;
        nums4 /=  2;
        nums5++;
        nums6--;
        ++nums7;
        --nums8;
        nums9 = nums0;

        { int i = nums0; }
        { int i = nums1; }
        { int i = nums2; }
        { int i = nums3; }
        { int i = nums4; }
        { int i = nums5; }
        { int i = nums6; }
        { int i = nums7; }
        { int i = nums8; }
        { int i = nums9; }
        
         Tester.check(GetAspect.bstar,
                      "Advice wasn't run for GetAspect.star");
         Tester.check(GetAspect.b0,
                      "Advice wasn't run for GetAspect.nums0");
         Tester.check(GetAspect.b1,
                      "Advice wasn't run for GetAspect.nums1");
         Tester.check(GetAspect.b2,
                      "Advice wasn't run for GetAspect.nums2");
         Tester.check(GetAspect.b3,
                      "Advice wasn't run for GetAspect.nums3");
         Tester.check(GetAspect.b4,
                      "Advice wasn't run for GetAspect.nums4");
         Tester.check(GetAspect.b5,
                      "Advice wasn't run for GetAspect.nums5");
         Tester.check(GetAspect.b6,
                      "Advice wasn't run for GetAspect.nums6");
         Tester.check(GetAspect.b7,
                      "Advice wasn't run for GetAspect.nums7");
         Tester.check(GetAspect.b8,
                      "Advice wasn't run for GetAspect.nums8");
         Tester.check(GetAspect.b9,
                      "Advice wasn't run for GetAspect.nums9");

//           Tester.check(GetAspectOfEach.bstar,
//                        "Advice wasn't run for GetAspectOfEach.star");
//           Tester.check(GetAspectOfEach.b0,
//                        "Advice wasn't run for GetAspectOfEach.nums0");
//           Tester.check(GetAspectOfEach.b1,
//                        "Advice wasn't run for GetAspectOfEach.nums1");
//           Tester.check(GetAspectOfEach.b2,
//                        "Advice wasn't run for GetAspectOfEach.nums2");
//           Tester.check(GetAspectOfEach.b3,
//                        "Advice wasn't run for GetAspectOfEach.nums3");
//           Tester.check(GetAspectOfEach.b4,
//                        "Advice wasn't run for GetAspectOfEach.nums4");
//           Tester.check(GetAspectOfEach.b5,
//                        "Advice wasn't run for GetAspectOfEach.nums5");
//           Tester.check(GetAspectOfEach.b6,
//                        "Advice wasn't run for GetAspectOfEach.nums6");
//           Tester.check(GetAspectOfEach.b7,
//                        "Advice wasn't run for GetAspectOfEach.nums7");
//           Tester.check(GetAspectOfEach.b8,
//                        "Advice wasn't run for GetAspectOfEach.nums8");
//           Tester.check(GetAspectOfEach.b9,
//                        "Advice wasn't run for GetAspectOfEach.nums9");         

//           Tester.check(GetAspectWithBrackets.bstar,
//                        "Advice wasn't run for GetAspectWithBrackets.star");
//           Tester.check(GetAspectWithBrackets.b0,
//                        "Advice wasn't run for GetAspectWithBrackets.nums0");
//           Tester.check(GetAspectWithBrackets.b1,
//                        "Advice wasn't run for GetAspectWithBrackets.nums1");
//           Tester.check(GetAspectWithBrackets.b2,
//                        "Advice wasn't run for GetAspectWithBrackets.nums2");
//           Tester.check(GetAspectWithBrackets.b3,
//                        "Advice wasn't run for GetAspectWithBrackets.nums3");
//           Tester.check(GetAspectWithBrackets.b4,
//                        "Advice wasn't run for GetAspectWithBrackets.nums4");
//           Tester.check(GetAspectWithBrackets.b5,
//                        "Advice wasn't run for GetAspectWithBrackets.nums5");
//           Tester.check(GetAspectWithBrackets.b6,
//                        "Advice wasn't run for GetAspectWithBrackets.nums6");
//           Tester.check(GetAspectWithBrackets.b7,
//                        "Advice wasn't run for GetAspectWithBrackets.nums7");
//           Tester.check(GetAspectWithBrackets.b8,
//                        "Advice wasn't run for GetAspectWithBrackets.nums8");
//           Tester.check(GetAspectWithBrackets.b9,
//                        "Advice wasn't run for GetAspectWithBrackets.nums9");         

//           Tester.check(SetAspect.bstar,
//                        "Advice wasn't run for SetAspect.star");
//           Tester.check(SetAspect.b0,
//                        "Advice wasn't run for SetAspect.nums0");
//           Tester.check(SetAspect.b1,
//                        "Advice wasn't run for SetAspect.nums1");
//           Tester.check(SetAspect.b2,
//                        "Advice wasn't run for SetAspect.nums2");
//           Tester.check(SetAspect.b3,
//                        "Advice wasn't run for SetAspect.nums3");
//           Tester.check(SetAspect.b4,
//                        "Advice wasn't run for SetAspect.nums4");
//           Tester.check(SetAspect.b5,
//                        "Advice wasn't run for SetAspect.nums5");
//           Tester.check(SetAspect.b6,
//                        "Advice wasn't run for SetAspect.nums6");
//           Tester.check(SetAspect.b7,
//                        "Advice wasn't run for SetAspect.nums7");
//           Tester.check(SetAspect.b8,
//                        "Advice wasn't run for SetAspect.nums8");
//           Tester.check(SetAspect.b9,
//                        "Advice wasn't run for SetAspect.nums9");

         Tester.check(SetAspect2.bstar,
                      "Advice wasn't run for SetAspect2.star");
         Tester.check(SetAspect2.b0,
                      "Advice wasn't run for SetAspect2.nums0");
         Tester.check(SetAspect2.b1,
                      "Advice wasn't run for SetAspect2.nums1");
         Tester.check(SetAspect2.b2,
                      "Advice wasn't run for SetAspect2.nums2");
         Tester.check(SetAspect2.b3,
                      "Advice wasn't run for SetAspect2.nums3");
         Tester.check(SetAspect2.b4,
                      "Advice wasn't run for SetAspect2.nums4");
         Tester.check(SetAspect2.b5,
                      "Advice wasn't run for SetAspect2.nums5");
         Tester.check(SetAspect2.b6,
                      "Advice wasn't run for SetAspect2.nums6");
         Tester.check(SetAspect2.b7,
                      "Advice wasn't run for SetAspect2.nums7");
         Tester.check(SetAspect2.b8,
                      "Advice wasn't run for SetAspect2.nums8");
         Tester.check(SetAspect2.b9,
                      "Advice wasn't run for SetAspect2.nums9");

//           Tester.check(SetAspect3.bstar,
//                        "Advice wasn't run for SetAspect3.star");
//           Tester.check(SetAspect3.b0,
//                        "Advice wasn't run for SetAspect3.nums0");
//           Tester.check(SetAspect3.b1,
//                        "Advice wasn't run for SetAspect3.nums1");
//           Tester.check(SetAspect3.b2,
//                        "Advice wasn't run for SetAspect3.nums2");
//           Tester.check(SetAspect3.b3,
//                        "Advice wasn't run for SetAspect3.nums3");
//           Tester.check(SetAspect3.b4,
//                        "Advice wasn't run for SetAspect3.nums4");
//           Tester.check(SetAspect3.b5,
//                        "Advice wasn't run for SetAspect3.nums5");
//           Tester.check(SetAspect3.b6,
//                        "Advice wasn't run for SetAspect3.nums6");
//           Tester.check(SetAspect3.b7,
//                        "Advice wasn't run for SetAspect3.nums7");
//           Tester.check(SetAspect3.b8,
//                        "Advice wasn't run for SetAspect3.nums8");
//           Tester.check(SetAspect3.b9,
//                        "Advice wasn't run for SetAspect3.nums9");

//           Tester.check(SetAspect4.bstar,
//                        "Advice wasn't run for SetAspect4.star");
//           Tester.check(SetAspect4.b0,
//                        "Advice wasn't run for SetAspect4.nums0");
//           Tester.check(SetAspect4.b1,
//                        "Advice wasn't run for SetAspect4.nums1");
//           Tester.check(SetAspect4.b2,
//                        "Advice wasn't run for SetAspect4.nums2");
//           Tester.check(SetAspect4.b3,
//                        "Advice wasn't run for SetAspect4.nums3");
//           Tester.check(SetAspect4.b4,
//                        "Advice wasn't run for SetAspect4.nums4");
//           Tester.check(SetAspect4.b5,
//                        "Advice wasn't run for SetAspect4.nums5");
//           Tester.check(SetAspect4.b6,
//                        "Advice wasn't run for SetAspect4.nums6");
//           Tester.check(SetAspect4.b7,
//                        "Advice wasn't run for SetAspect4.nums7");
//           Tester.check(SetAspect4.b8,
//                        "Advice wasn't run for SetAspect4.nums8");
//           Tester.check(SetAspect4.b9,
//                        "Advice wasn't run for SetAspect4.nums9");

//           Tester.check(GetAspectOfEachWithBrackets.bstar,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.star");
//           Tester.check(GetAspectOfEachWithBrackets.b0,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums0");
//           Tester.check(GetAspectOfEachWithBrackets.b1,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums1");
//           Tester.check(GetAspectOfEachWithBrackets.b2,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums2");
//           Tester.check(GetAspectOfEachWithBrackets.b3,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums3");
//           Tester.check(GetAspectOfEachWithBrackets.b4,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums4");
//           Tester.check(GetAspectOfEachWithBrackets.b5,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums5");
//           Tester.check(GetAspectOfEachWithBrackets.b6,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums6");
//           Tester.check(GetAspectOfEachWithBrackets.b7,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums7");
//           Tester.check(GetAspectOfEachWithBrackets.b8,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums8");
//           Tester.check(GetAspectOfEachWithBrackets.b9,
//                        "Advice wasn't run for GetAspectOfEachWithBrackets.nums9");         

//           Tester.check(SetAspectOfEach.bstar,
//                        "Advice wasn't run for SetAspectOfEach.star");
//           Tester.check(SetAspectOfEach.b0,
//                        "Advice wasn't run for SetAspectOfEach.nums0");
//           Tester.check(SetAspectOfEach.b1,
//                        "Advice wasn't run for SetAspectOfEach.nums1");
//           Tester.check(SetAspectOfEach.b2,
//                        "Advice wasn't run for SetAspectOfEach.nums2");
//           Tester.check(SetAspectOfEach.b3,
//                        "Advice wasn't run for SetAspectOfEach.nums3");
//           Tester.check(SetAspectOfEach.b4,
//                        "Advice wasn't run for SetAspectOfEach.nums4");
//           Tester.check(SetAspectOfEach.b5,
//                        "Advice wasn't run for SetAspectOfEach.nums5");
//           Tester.check(SetAspectOfEach.b6,
//                        "Advice wasn't run for SetAspectOfEach.nums6");
//           Tester.check(SetAspectOfEach.b7,
//                        "Advice wasn't run for SetAspectOfEach.nums7");
//           Tester.check(SetAspectOfEach.b8,
//                        "Advice wasn't run for SetAspectOfEach.nums8");
//           Tester.check(SetAspectOfEach.b9,
//                        "Advice wasn't run for SetAspectOfEach.nums9");

//           Tester.check(SetAspectOfEach2.bstar,
//                        "Advice wasn't run for SetAspectOfEach2.star");
//           Tester.check(SetAspectOfEach2.b0,
//                        "Advice wasn't run for SetAspectOfEach2.nums0");
//           Tester.check(SetAspectOfEach2.b1,
//                        "Advice wasn't run for SetAspectOfEach2.nums1");
//           Tester.check(SetAspectOfEach2.b2,
//                        "Advice wasn't run for SetAspectOfEach2.nums2");
//           Tester.check(SetAspectOfEach2.b3,
//                        "Advice wasn't run for SetAspectOfEach2.nums3");
//           Tester.check(SetAspectOfEach2.b4,
//                        "Advice wasn't run for SetAspectOfEach2.nums4");
//           Tester.check(SetAspectOfEach2.b5,
//                        "Advice wasn't run for SetAspectOfEach2.nums5");
//           Tester.check(SetAspectOfEach2.b6,
//                        "Advice wasn't run for SetAspectOfEach2.nums6");
//           Tester.check(SetAspectOfEach2.b7,
//                        "Advice wasn't run for SetAspectOfEach2.nums7");
//           Tester.check(SetAspectOfEach2.b8,
//                        "Advice wasn't run for SetAspectOfEach2.nums8");
//           Tester.check(SetAspectOfEach2.b9,
//                        "Advice wasn't run for SetAspectOfEach2.nums9");

//           Tester.check(SetAspectOfEach3.bstar,
//                        "Advice wasn't run for SetAspectOfEach3.star");
//           Tester.check(SetAspectOfEach3.b0,
//                        "Advice wasn't run for SetAspectOfEach3.nums0");
//           Tester.check(SetAspectOfEach3.b1,
//                        "Advice wasn't run for SetAspectOfEach3.nums1");
//           Tester.check(SetAspectOfEach3.b2,
//                        "Advice wasn't run for SetAspectOfEach3.nums2");
//           Tester.check(SetAspectOfEach3.b3,
//                        "Advice wasn't run for SetAspectOfEach3.nums3");
//           Tester.check(SetAspectOfEach3.b4,
//                        "Advice wasn't run for SetAspectOfEach3.nums4");
//           Tester.check(SetAspectOfEach3.b5,
//                        "Advice wasn't run for SetAspectOfEach3.nums5");
//           Tester.check(SetAspectOfEach3.b6,
//                        "Advice wasn't run for SetAspectOfEach3.nums6");
//           Tester.check(SetAspectOfEach3.b7,
//                        "Advice wasn't run for SetAspectOfEach3.nums7");
//           Tester.check(SetAspectOfEach3.b8,
//                        "Advice wasn't run for SetAspectOfEach3.nums8");
//           Tester.check(SetAspectOfEach3.b9,
//                        "Advice wasn't run for SetAspectOfEach3.nums9");

//           Tester.check(SetAspectOfEach4.bstar,
//                        "Advice wasn't run for SetAspectOfEach4.star");
//           Tester.check(SetAspectOfEach4.b0,
//                        "Advice wasn't run for SetAspectOfEach4.nums0");
//           Tester.check(SetAspectOfEach4.b1,
//                        "Advice wasn't run for SetAspectOfEach4.nums1");
//           Tester.check(SetAspectOfEach4.b2,
//                        "Advice wasn't run for SetAspectOfEach4.nums2");
//           Tester.check(SetAspectOfEach4.b3,
//                        "Advice wasn't run for SetAspectOfEach4.nums3");
//           Tester.check(SetAspectOfEach4.b4,
//                        "Advice wasn't run for SetAspectOfEach4.nums4");
//           Tester.check(SetAspectOfEach4.b5,
//                        "Advice wasn't run for SetAspectOfEach4.nums5");
//           Tester.check(SetAspectOfEach4.b6,
//                        "Advice wasn't run for SetAspectOfEach4.nums6");
//           Tester.check(SetAspectOfEach4.b7,
//                        "Advice wasn't run for SetAspectOfEach4.nums7");
//           Tester.check(SetAspectOfEach4.b8,
//                        "Advice wasn't run for SetAspectOfEach4.nums8");
//           Tester.check(SetAspectOfEach4.b9,
//                        "Advice wasn't run for SetAspectOfEach4.nums9");
     }
 }

aspect GetAspect {
    static boolean bstar = false;
    after(): get(int Gets.*) {
        bstar = true;
    }    
    static boolean b0 = false;
    after(): get(int Gets.nums0) {
        b0 = true;
    }
    static boolean b1 = false;
    after(): get(int Gets.nums1) {
        b1 = true;
    }
    static boolean b2 = false;
    after(): get(int Gets.nums2) {
        b2 = true;
    }
    static boolean b3 = false;
    after(): get(int Gets.nums3) {
        b3 = true;
    }
    static boolean b4 = false;
    after(): get(int Gets.nums4) {
        b4 = true;
    }
    static boolean b5 = false;
    after(): get(int Gets.nums5) {
        b5 = true;
    }
    static boolean b6 = false;
    after(): get(int Gets.nums6) {
        b6 = true;
    }
    static boolean b7 = false;
    after(): get(int Gets.nums7) {
        b7 = true;
    }
    static boolean b8 = false;
    after(): get(int Gets.nums8) {
        b8 = true;
    }
    static boolean b9 = false;
    after(): get(int Gets.nums9) {
        b9 = true;
    } 
}

//  aspect GetAspectWithBrackets {
//      static boolean bstar = false;
//      after(int n): get(int Gets.*)[n] {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int n): get(int Gets.nums0)[n] {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int n): get(int Gets.nums1)[n] {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int n): get(int Gets.nums2)[n] {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int n): get(int Gets.nums3)[n] {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int n): get(int Gets.nums4)[n] {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int n): get(int Gets.nums5)[n] {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int n): get(int Gets.nums6)[n] {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int n): get(int Gets.nums7)[n] {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int n): get(int Gets.nums8)[n] {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int n): get(int Gets.nums9)[n] {
//          b9 = true;
//      } 
//  }

//  aspect SetAspect {
//      static boolean bstar = false;
//      after(int d, int n): set(int Gets.*)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "nums should be 10, not " + d + " for SetAspect");
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int d, int n): set(int Gets.nums0)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num0 should be 10, not " + d + " for SetAspect");
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int d, int n): set(int Gets.nums1)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num1 should be 10, not " + d + " for SetAspect");
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int d, int n): set(int Gets.nums2)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num2 should be 10, not " + d + " for SetAspect");
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int d, int n): set(int Gets.nums3)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num3 should be 10, not " + d + " for SetAspect");
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int d, int n): set(int Gets.nums4)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num4 should be 10, not " + d + " for SetAspect");
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int d, int n): set(int Gets.nums5)[d][n] && withincode(void go())//   {
//          Tester.check(n == 10, "num5 should be 10, not " + d + " for SetAspect");
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int d, int n): set(int Gets.nums6)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num6 should be 10, not " + d + " for SetAspect");
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int d, int n): set(int Gets.nums7)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num7 should be 10, not " + d + " for SetAspect");
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int d, int n): set(int Gets.nums8)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num8 should be 10, not " + d + " for SetAspect");
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int d, int n): set(int Gets.nums9)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num9 should be 10, not " + d + " for SetAspect");
//          b9 = true;
//      }

//  }

aspect SetAspect2 {
    static boolean bstar = false;
    after(): set(int Gets.*) {
        bstar = true;
    }    
    static boolean b0 = false;
    after(): set(int Gets.nums0) {
        b0 = true;
    }
    static boolean b1 = false;
    after(): set(int Gets.nums1) {
        b1 = true;
    }
    static boolean b2 = false;
    after(): set(int Gets.nums2) {
        b2 = true;
    }
    static boolean b3 = false;
    after(): set(int Gets.nums3) {
        b3 = true;
    }
    static boolean b4 = false;
    after(): set(int Gets.nums4) {
        b4 = true;
    }
    static boolean b5 = false;
    after(): set(int Gets.nums5) {
        b5 = true;
    }
    static boolean b6 = false;
    after(): set(int Gets.nums6) {
        b6 = true;
    }
    static boolean b7 = false;
    after(): set(int Gets.nums7) {
        b7 = true;
    }
    static boolean b8 = false;
    after(): set(int Gets.nums8) {
        b8 = true;
    }
    static boolean b9 = false;
    after(): set(int Gets.nums9) {
        b9 = true;
    }

}

//  aspect SetAspect3 {
//      static boolean bstar = false;
//      after(int d): set(int Gets.*)[d][] {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int d): set(int Gets.nums0)[d][] {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int d): set(int Gets.nums1)[d][] {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int d): set(int Gets.nums2)[d][] {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int d): set(int Gets.nums3)[d][] {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int d): set(int Gets.nums4)[d][] {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int d): set(int Gets.nums5)[d][] {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int d): set(int Gets.nums6)[d][] {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int d): set(int Gets.nums7)[d][] {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int d): set(int Gets.nums8)[d][] {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int d): set(int Gets.nums9)[d][] {
//          b9 = true;
//      }

//  }

//  aspect SetAspect4 {
//      static boolean bstar = false;
//      after(int n): set(int Gets.*)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "nums should be 10 for SetAspect4");
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int n): set(int Gets.nums0)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num0 should be 10 for SetAspect4");
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int n): set(int Gets.nums1)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num1 should be 10 for SetAspect4");
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int n): set(int Gets.nums2)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num2 should be 10 for SetAspect4");
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int n): set(int Gets.nums3)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num3 should be 10 for SetAspect4");
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int n): set(int Gets.nums4)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num4 should be 10 for SetAspect4");
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int n): set(int Gets.nums5)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num5 should be 10 for SetAspect4");
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int n): set(int Gets.nums6)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num6 should be 10 for SetAspect4");
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int n): set(int Gets.nums7)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num7 should be 10 for SetAspect4");
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int n): set(int Gets.nums8)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num8 should be 10 for SetAspect4");
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int n): set(int Gets.nums9)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num9 should be 10 for SetAspect4");
//          b9 = true;
//      }

//  }

//  aspect GetAspectOfEach of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(): get(int Gets.*) {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(): get(int Gets.nums0) {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(): get(int Gets.nums1) {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(): get(int Gets.nums2) {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(): get(int Gets.nums3) {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(): get(int Gets.nums4) {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(): get(int Gets.nums5) {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(): get(int Gets.nums6) {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(): get(int Gets.nums7) {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(): get(int Gets.nums8) {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(): get(int Gets.nums9) {
//          b9 = true;
//      }
 
//  }

//  aspect GetAspectOfEachWithBrackets of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(int n): get(int Gets.*)[n] {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int n): get(int Gets.nums0)[n] {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int n): get(int Gets.nums1)[n] {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int n): get(int Gets.nums2)[n] {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int n): get(int Gets.nums3)[n] {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int n): get(int Gets.nums4)[n] {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int n): get(int Gets.nums5)[n] {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int n): get(int Gets.nums6)[n] {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int n): get(int Gets.nums7)[n] {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int n): get(int Gets.nums8)[n] {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int n): get(int Gets.nums9)[n] {
//          b9 = true;
//      } 
//  }

//  aspect SetAspectOfEach of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(int d, int n): set(int Gets.*)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "nums should be 10, not " + d + " for SetAspectOfEach");
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int d, int n): set(int Gets.nums0)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num0 should be 10, not " + d + " for SetAspectOfEach");
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int d, int n): set(int Gets.nums1)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num1 should be 10, not " + d + " for SetAspectOfEach");
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int d, int n): set(int Gets.nums2)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num2 should be 10, not " + d + " for SetAspectOfEach");
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int d, int n): set(int Gets.nums3)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num3 should be 10, not " + d + " for SetAspectOfEach");
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int d, int n): set(int Gets.nums4)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num4 should be 10, not " + d + " for SetAspectOfEach");
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int d, int n): set(int Gets.nums5)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num5 should be 10, not " + d + " for SetAspectOfEach");
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int d, int n): set(int Gets.nums6)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num6 should be 10, not " + d + " for SetAspectOfEach");
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int d, int n): set(int Gets.nums7)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num7 should be 10, not " + d + " for SetAspectOfEach");
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int d, int n): set(int Gets.nums8)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num8 should be 10, not " + d + " for SetAspectOfEach");
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int d, int n): set(int Gets.nums9)[d][n] && withincode(void go()) {
//          Tester.check(n == 10, "num9 should be 10, not " + d + " for SetAspectOfEach");
//          b9 = true;
//      }

//  }

//  aspect SetAspectOfEach2 of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(): set(int Gets.*) {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(): set(int Gets.nums0) {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(): set(int Gets.nums1) {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(): set(int Gets.nums2) {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(): set(int Gets.nums3) {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(): set(int Gets.nums4) {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(): set(int Gets.nums5) {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(): set(int Gets.nums6) {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(): set(int Gets.nums7) {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(): set(int Gets.nums8) {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(): set(int Gets.nums9) {
//          b9 = true;
//      }

//  }

//  aspect SetAspectOfEach3 of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(int d): set(int Gets.*)[d][] {
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int d): set(int Gets.nums0)[d][] {
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int d): set(int Gets.nums1)[d][] {
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int d): set(int Gets.nums2)[d][] {
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int d): set(int Gets.nums3)[d][] {
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int d): set(int Gets.nums4)[d][] {
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int d): set(int Gets.nums5)[d][] {
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int d): set(int Gets.nums6)[d][] {
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int d): set(int Gets.nums7)[d][] {
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int d): set(int Gets.nums8)[d][] {
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int d): set(int Gets.nums9)[d][] {
//          b9 = true;
//      }

//  }

//  aspect SetAspectOfEach4 of eachobject(instanceof(Gets)) {
//      static boolean bstar = false;
//      after(int n): set(int Gets.*)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "nums should be 10 for SetAspectOfEach4");
//          bstar = true;
//      }    
//      static boolean b0 = false;
//      after(int n): set(int Gets.nums0)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num0 should be 10 for SetAspectOfEach4");
//          b0 = true;
//      }
//      static boolean b1 = false;
//      after(int n): set(int Gets.nums1)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num1 should be 10 for SetAspectOfEach4");
//          b1 = true;
//      }
//      static boolean b2 = false;
//      after(int n): set(int Gets.nums2)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num2 should be 10 for SetAspectOfEach4");
//          b2 = true;
//      }
//      static boolean b3 = false;
//      after(int n): set(int Gets.nums3)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num3 should be 10 for SetAspectOfEach4");
//          b3 = true;
//      }
//      static boolean b4 = false;
//      after(int n): set(int Gets.nums4)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num4 should be 10 for SetAspectOfEach4");
//          b4 = true;
//      }
//      static boolean b5 = false;
//      after(int n): set(int Gets.nums5)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num5 should be 10 for SetAspectOfEach4");
//          b5 = true;
//      }
//      static boolean b6 = false;
//      after(int n): set(int Gets.nums6)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num6 should be 10 for SetAspectOfEach4");
//          b6 = true;
//      }
//      static boolean b7 = false;
//      after(int n): set(int Gets.nums7)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num7 should be 10 for SetAspectOfEach4");
//          b7 = true;
//      }
//      static boolean b8 = false;
//      after(int n): set(int Gets.nums8)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num8 should be 10 for SetAspectOfEach4");
//          b8 = true;
//      }
//      static boolean b9 = false;
//      after(int n): set(int Gets.nums9)[][n] && withincode(void go()) {
//          Tester.checkEqual(n, 10, "num9 should be 10 for SetAspectOfEach4");
//          b9 = true;
//      }

//  }
