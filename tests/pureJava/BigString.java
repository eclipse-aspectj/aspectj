// This is a GENERATED file.  Use the scheme program at the end to
// regenerate.  Note that in jdk 1.3, Float.MIN_VALUE does not have a
// proper constant value.  If that ever gets fixed, look for "NOT
// CORRECT IN 1.3" and uncomment that line.

import org.aspectj.testing.Tester;

public strictfp class BigString {
  public static void main(String[] args) {
    byteTest();
    shortTest();
    charTest();
    intTest();
    longTest();
    floatTest();
    doubleTest();
    booleanTest();
    StringTest();
  }

  // --------
  // byte tests
  static String byteOp(String x, byte y) { return x + y; }
  static String byteAssign(String x, byte y) { x += y; return x; }
  static void byteTest() {
    Tester.checkEqual(byteOp("" + Byte.MIN_VALUE, Byte.MIN_VALUE), ("" + Byte.MIN_VALUE) + Byte.MIN_VALUE, "byteOp(\"\" + Byte.MIN_VALUE, Byte.MIN_VALUE)");
    Tester.checkEqual(byteAssign("" + Byte.MIN_VALUE, Byte.MIN_VALUE), ("" + Byte.MIN_VALUE) + Byte.MIN_VALUE, "byteOp(\"\" + Byte.MIN_VALUE, Byte.MIN_VALUE)");
    Tester.checkEqual(byteOp("" + Byte.MIN_VALUE, (byte) -1), ("" + Byte.MIN_VALUE) + (byte) -1, "byteOp(\"\" + Byte.MIN_VALUE, (byte) -1)");
    Tester.checkEqual(byteAssign("" + Byte.MIN_VALUE, (byte) -1), ("" + Byte.MIN_VALUE) + (byte) -1, "byteOp(\"\" + Byte.MIN_VALUE, (byte) -1)");
    Tester.checkEqual(byteOp("" + Byte.MIN_VALUE, (byte) 0), ("" + Byte.MIN_VALUE) + (byte) 0, "byteOp(\"\" + Byte.MIN_VALUE, (byte) 0)");
    Tester.checkEqual(byteAssign("" + Byte.MIN_VALUE, (byte) 0), ("" + Byte.MIN_VALUE) + (byte) 0, "byteOp(\"\" + Byte.MIN_VALUE, (byte) 0)");
    Tester.checkEqual(byteOp("" + Byte.MIN_VALUE, (byte) 1), ("" + Byte.MIN_VALUE) + (byte) 1, "byteOp(\"\" + Byte.MIN_VALUE, (byte) 1)");
    Tester.checkEqual(byteAssign("" + Byte.MIN_VALUE, (byte) 1), ("" + Byte.MIN_VALUE) + (byte) 1, "byteOp(\"\" + Byte.MIN_VALUE, (byte) 1)");
    Tester.checkEqual(byteOp("" + Byte.MIN_VALUE, Byte.MAX_VALUE), ("" + Byte.MIN_VALUE) + Byte.MAX_VALUE, "byteOp(\"\" + Byte.MIN_VALUE, Byte.MAX_VALUE)");
    Tester.checkEqual(byteAssign("" + Byte.MIN_VALUE, Byte.MAX_VALUE), ("" + Byte.MIN_VALUE) + Byte.MAX_VALUE, "byteOp(\"\" + Byte.MIN_VALUE, Byte.MAX_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) -1, Byte.MIN_VALUE), ("" + (byte) -1) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) -1, Byte.MIN_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) -1, Byte.MIN_VALUE), ("" + (byte) -1) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) -1, Byte.MIN_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) -1, (byte) -1), ("" + (byte) -1) + (byte) -1, "byteOp(\"\" + (byte) -1, (byte) -1)");
    Tester.checkEqual(byteAssign("" + (byte) -1, (byte) -1), ("" + (byte) -1) + (byte) -1, "byteOp(\"\" + (byte) -1, (byte) -1)");
    Tester.checkEqual(byteOp("" + (byte) -1, (byte) 0), ("" + (byte) -1) + (byte) 0, "byteOp(\"\" + (byte) -1, (byte) 0)");
    Tester.checkEqual(byteAssign("" + (byte) -1, (byte) 0), ("" + (byte) -1) + (byte) 0, "byteOp(\"\" + (byte) -1, (byte) 0)");
    Tester.checkEqual(byteOp("" + (byte) -1, (byte) 1), ("" + (byte) -1) + (byte) 1, "byteOp(\"\" + (byte) -1, (byte) 1)");
    Tester.checkEqual(byteAssign("" + (byte) -1, (byte) 1), ("" + (byte) -1) + (byte) 1, "byteOp(\"\" + (byte) -1, (byte) 1)");
    Tester.checkEqual(byteOp("" + (byte) -1, Byte.MAX_VALUE), ("" + (byte) -1) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) -1, Byte.MAX_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) -1, Byte.MAX_VALUE), ("" + (byte) -1) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) -1, Byte.MAX_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) 0, Byte.MIN_VALUE), ("" + (byte) 0) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) 0, Byte.MIN_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) 0, Byte.MIN_VALUE), ("" + (byte) 0) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) 0, Byte.MIN_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) 0, (byte) -1), ("" + (byte) 0) + (byte) -1, "byteOp(\"\" + (byte) 0, (byte) -1)");
    Tester.checkEqual(byteAssign("" + (byte) 0, (byte) -1), ("" + (byte) 0) + (byte) -1, "byteOp(\"\" + (byte) 0, (byte) -1)");
    Tester.checkEqual(byteOp("" + (byte) 0, (byte) 0), ("" + (byte) 0) + (byte) 0, "byteOp(\"\" + (byte) 0, (byte) 0)");
    Tester.checkEqual(byteAssign("" + (byte) 0, (byte) 0), ("" + (byte) 0) + (byte) 0, "byteOp(\"\" + (byte) 0, (byte) 0)");
    Tester.checkEqual(byteOp("" + (byte) 0, (byte) 1), ("" + (byte) 0) + (byte) 1, "byteOp(\"\" + (byte) 0, (byte) 1)");
    Tester.checkEqual(byteAssign("" + (byte) 0, (byte) 1), ("" + (byte) 0) + (byte) 1, "byteOp(\"\" + (byte) 0, (byte) 1)");
    Tester.checkEqual(byteOp("" + (byte) 0, Byte.MAX_VALUE), ("" + (byte) 0) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) 0, Byte.MAX_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) 0, Byte.MAX_VALUE), ("" + (byte) 0) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) 0, Byte.MAX_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) 1, Byte.MIN_VALUE), ("" + (byte) 1) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) 1, Byte.MIN_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) 1, Byte.MIN_VALUE), ("" + (byte) 1) + Byte.MIN_VALUE, "byteOp(\"\" + (byte) 1, Byte.MIN_VALUE)");
    Tester.checkEqual(byteOp("" + (byte) 1, (byte) -1), ("" + (byte) 1) + (byte) -1, "byteOp(\"\" + (byte) 1, (byte) -1)");
    Tester.checkEqual(byteAssign("" + (byte) 1, (byte) -1), ("" + (byte) 1) + (byte) -1, "byteOp(\"\" + (byte) 1, (byte) -1)");
    Tester.checkEqual(byteOp("" + (byte) 1, (byte) 0), ("" + (byte) 1) + (byte) 0, "byteOp(\"\" + (byte) 1, (byte) 0)");
    Tester.checkEqual(byteAssign("" + (byte) 1, (byte) 0), ("" + (byte) 1) + (byte) 0, "byteOp(\"\" + (byte) 1, (byte) 0)");
    Tester.checkEqual(byteOp("" + (byte) 1, (byte) 1), ("" + (byte) 1) + (byte) 1, "byteOp(\"\" + (byte) 1, (byte) 1)");
    Tester.checkEqual(byteAssign("" + (byte) 1, (byte) 1), ("" + (byte) 1) + (byte) 1, "byteOp(\"\" + (byte) 1, (byte) 1)");
    Tester.checkEqual(byteOp("" + (byte) 1, Byte.MAX_VALUE), ("" + (byte) 1) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) 1, Byte.MAX_VALUE)");
    Tester.checkEqual(byteAssign("" + (byte) 1, Byte.MAX_VALUE), ("" + (byte) 1) + Byte.MAX_VALUE, "byteOp(\"\" + (byte) 1, Byte.MAX_VALUE)");
    Tester.checkEqual(byteOp("" + Byte.MAX_VALUE, Byte.MIN_VALUE), ("" + Byte.MAX_VALUE) + Byte.MIN_VALUE, "byteOp(\"\" + Byte.MAX_VALUE, Byte.MIN_VALUE)");
    Tester.checkEqual(byteAssign("" + Byte.MAX_VALUE, Byte.MIN_VALUE), ("" + Byte.MAX_VALUE) + Byte.MIN_VALUE, "byteOp(\"\" + Byte.MAX_VALUE, Byte.MIN_VALUE)");
    Tester.checkEqual(byteOp("" + Byte.MAX_VALUE, (byte) -1), ("" + Byte.MAX_VALUE) + (byte) -1, "byteOp(\"\" + Byte.MAX_VALUE, (byte) -1)");
    Tester.checkEqual(byteAssign("" + Byte.MAX_VALUE, (byte) -1), ("" + Byte.MAX_VALUE) + (byte) -1, "byteOp(\"\" + Byte.MAX_VALUE, (byte) -1)");
    Tester.checkEqual(byteOp("" + Byte.MAX_VALUE, (byte) 0), ("" + Byte.MAX_VALUE) + (byte) 0, "byteOp(\"\" + Byte.MAX_VALUE, (byte) 0)");
    Tester.checkEqual(byteAssign("" + Byte.MAX_VALUE, (byte) 0), ("" + Byte.MAX_VALUE) + (byte) 0, "byteOp(\"\" + Byte.MAX_VALUE, (byte) 0)");
    Tester.checkEqual(byteOp("" + Byte.MAX_VALUE, (byte) 1), ("" + Byte.MAX_VALUE) + (byte) 1, "byteOp(\"\" + Byte.MAX_VALUE, (byte) 1)");
    Tester.checkEqual(byteAssign("" + Byte.MAX_VALUE, (byte) 1), ("" + Byte.MAX_VALUE) + (byte) 1, "byteOp(\"\" + Byte.MAX_VALUE, (byte) 1)");
    Tester.checkEqual(byteOp("" + Byte.MAX_VALUE, Byte.MAX_VALUE), ("" + Byte.MAX_VALUE) + Byte.MAX_VALUE, "byteOp(\"\" + Byte.MAX_VALUE, Byte.MAX_VALUE)");
    Tester.checkEqual(byteAssign("" + Byte.MAX_VALUE, Byte.MAX_VALUE), ("" + Byte.MAX_VALUE) + Byte.MAX_VALUE, "byteOp(\"\" + Byte.MAX_VALUE, Byte.MAX_VALUE)");
  }
  static void byteSwitch() {
    switch(-1) {
      case (((("" + Byte.MIN_VALUE) + Byte.MIN_VALUE) == "") ? 0 : 0):
      case (((("" + Byte.MIN_VALUE) + (byte) -1) == "") ? 1 : 1):
      case (((("" + Byte.MIN_VALUE) + (byte) 0) == "") ? 2 : 2):
      case (((("" + Byte.MIN_VALUE) + (byte) 1) == "") ? 3 : 3):
      case (((("" + Byte.MIN_VALUE) + Byte.MAX_VALUE) == "") ? 4 : 4):
      case (((("" + (byte) -1) + Byte.MIN_VALUE) == "") ? 5 : 5):
      case (((("" + (byte) -1) + (byte) -1) == "") ? 6 : 6):
      case (((("" + (byte) -1) + (byte) 0) == "") ? 7 : 7):
      case (((("" + (byte) -1) + (byte) 1) == "") ? 8 : 8):
      case (((("" + (byte) -1) + Byte.MAX_VALUE) == "") ? 9 : 9):
      case (((("" + (byte) 0) + Byte.MIN_VALUE) == "") ? 10 : 10):
      case (((("" + (byte) 0) + (byte) -1) == "") ? 11 : 11):
      case (((("" + (byte) 0) + (byte) 0) == "") ? 12 : 12):
      case (((("" + (byte) 0) + (byte) 1) == "") ? 13 : 13):
      case (((("" + (byte) 0) + Byte.MAX_VALUE) == "") ? 14 : 14):
      case (((("" + (byte) 1) + Byte.MIN_VALUE) == "") ? 15 : 15):
      case (((("" + (byte) 1) + (byte) -1) == "") ? 16 : 16):
      case (((("" + (byte) 1) + (byte) 0) == "") ? 17 : 17):
      case (((("" + (byte) 1) + (byte) 1) == "") ? 18 : 18):
      case (((("" + (byte) 1) + Byte.MAX_VALUE) == "") ? 19 : 19):
      case (((("" + Byte.MAX_VALUE) + Byte.MIN_VALUE) == "") ? 20 : 20):
      case (((("" + Byte.MAX_VALUE) + (byte) -1) == "") ? 21 : 21):
      case (((("" + Byte.MAX_VALUE) + (byte) 0) == "") ? 22 : 22):
      case (((("" + Byte.MAX_VALUE) + (byte) 1) == "") ? 23 : 23):
      case (((("" + Byte.MAX_VALUE) + Byte.MAX_VALUE) == "") ? 24 : 24):
      default:
    }
  }

  // --------
  // short tests
  static String shortOp(String x, short y) { return x + y; }
  static String shortAssign(String x, short y) { x += y; return x; }
  static void shortTest() {
    Tester.checkEqual(shortOp("" + Short.MIN_VALUE, Short.MIN_VALUE), ("" + Short.MIN_VALUE) + Short.MIN_VALUE, "shortOp(\"\" + Short.MIN_VALUE, Short.MIN_VALUE)");
    Tester.checkEqual(shortAssign("" + Short.MIN_VALUE, Short.MIN_VALUE), ("" + Short.MIN_VALUE) + Short.MIN_VALUE, "shortOp(\"\" + Short.MIN_VALUE, Short.MIN_VALUE)");
    Tester.checkEqual(shortOp("" + Short.MIN_VALUE, (short) -1), ("" + Short.MIN_VALUE) + (short) -1, "shortOp(\"\" + Short.MIN_VALUE, (short) -1)");
    Tester.checkEqual(shortAssign("" + Short.MIN_VALUE, (short) -1), ("" + Short.MIN_VALUE) + (short) -1, "shortOp(\"\" + Short.MIN_VALUE, (short) -1)");
    Tester.checkEqual(shortOp("" + Short.MIN_VALUE, (short) 0), ("" + Short.MIN_VALUE) + (short) 0, "shortOp(\"\" + Short.MIN_VALUE, (short) 0)");
    Tester.checkEqual(shortAssign("" + Short.MIN_VALUE, (short) 0), ("" + Short.MIN_VALUE) + (short) 0, "shortOp(\"\" + Short.MIN_VALUE, (short) 0)");
    Tester.checkEqual(shortOp("" + Short.MIN_VALUE, (short) 1), ("" + Short.MIN_VALUE) + (short) 1, "shortOp(\"\" + Short.MIN_VALUE, (short) 1)");
    Tester.checkEqual(shortAssign("" + Short.MIN_VALUE, (short) 1), ("" + Short.MIN_VALUE) + (short) 1, "shortOp(\"\" + Short.MIN_VALUE, (short) 1)");
    Tester.checkEqual(shortOp("" + Short.MIN_VALUE, Short.MAX_VALUE), ("" + Short.MIN_VALUE) + Short.MAX_VALUE, "shortOp(\"\" + Short.MIN_VALUE, Short.MAX_VALUE)");
    Tester.checkEqual(shortAssign("" + Short.MIN_VALUE, Short.MAX_VALUE), ("" + Short.MIN_VALUE) + Short.MAX_VALUE, "shortOp(\"\" + Short.MIN_VALUE, Short.MAX_VALUE)");
    Tester.checkEqual(shortOp("" + (short) -1, Short.MIN_VALUE), ("" + (short) -1) + Short.MIN_VALUE, "shortOp(\"\" + (short) -1, Short.MIN_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) -1, Short.MIN_VALUE), ("" + (short) -1) + Short.MIN_VALUE, "shortOp(\"\" + (short) -1, Short.MIN_VALUE)");
    Tester.checkEqual(shortOp("" + (short) -1, (short) -1), ("" + (short) -1) + (short) -1, "shortOp(\"\" + (short) -1, (short) -1)");
    Tester.checkEqual(shortAssign("" + (short) -1, (short) -1), ("" + (short) -1) + (short) -1, "shortOp(\"\" + (short) -1, (short) -1)");
    Tester.checkEqual(shortOp("" + (short) -1, (short) 0), ("" + (short) -1) + (short) 0, "shortOp(\"\" + (short) -1, (short) 0)");
    Tester.checkEqual(shortAssign("" + (short) -1, (short) 0), ("" + (short) -1) + (short) 0, "shortOp(\"\" + (short) -1, (short) 0)");
    Tester.checkEqual(shortOp("" + (short) -1, (short) 1), ("" + (short) -1) + (short) 1, "shortOp(\"\" + (short) -1, (short) 1)");
    Tester.checkEqual(shortAssign("" + (short) -1, (short) 1), ("" + (short) -1) + (short) 1, "shortOp(\"\" + (short) -1, (short) 1)");
    Tester.checkEqual(shortOp("" + (short) -1, Short.MAX_VALUE), ("" + (short) -1) + Short.MAX_VALUE, "shortOp(\"\" + (short) -1, Short.MAX_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) -1, Short.MAX_VALUE), ("" + (short) -1) + Short.MAX_VALUE, "shortOp(\"\" + (short) -1, Short.MAX_VALUE)");
    Tester.checkEqual(shortOp("" + (short) 0, Short.MIN_VALUE), ("" + (short) 0) + Short.MIN_VALUE, "shortOp(\"\" + (short) 0, Short.MIN_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) 0, Short.MIN_VALUE), ("" + (short) 0) + Short.MIN_VALUE, "shortOp(\"\" + (short) 0, Short.MIN_VALUE)");
    Tester.checkEqual(shortOp("" + (short) 0, (short) -1), ("" + (short) 0) + (short) -1, "shortOp(\"\" + (short) 0, (short) -1)");
    Tester.checkEqual(shortAssign("" + (short) 0, (short) -1), ("" + (short) 0) + (short) -1, "shortOp(\"\" + (short) 0, (short) -1)");
    Tester.checkEqual(shortOp("" + (short) 0, (short) 0), ("" + (short) 0) + (short) 0, "shortOp(\"\" + (short) 0, (short) 0)");
    Tester.checkEqual(shortAssign("" + (short) 0, (short) 0), ("" + (short) 0) + (short) 0, "shortOp(\"\" + (short) 0, (short) 0)");
    Tester.checkEqual(shortOp("" + (short) 0, (short) 1), ("" + (short) 0) + (short) 1, "shortOp(\"\" + (short) 0, (short) 1)");
    Tester.checkEqual(shortAssign("" + (short) 0, (short) 1), ("" + (short) 0) + (short) 1, "shortOp(\"\" + (short) 0, (short) 1)");
    Tester.checkEqual(shortOp("" + (short) 0, Short.MAX_VALUE), ("" + (short) 0) + Short.MAX_VALUE, "shortOp(\"\" + (short) 0, Short.MAX_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) 0, Short.MAX_VALUE), ("" + (short) 0) + Short.MAX_VALUE, "shortOp(\"\" + (short) 0, Short.MAX_VALUE)");
    Tester.checkEqual(shortOp("" + (short) 1, Short.MIN_VALUE), ("" + (short) 1) + Short.MIN_VALUE, "shortOp(\"\" + (short) 1, Short.MIN_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) 1, Short.MIN_VALUE), ("" + (short) 1) + Short.MIN_VALUE, "shortOp(\"\" + (short) 1, Short.MIN_VALUE)");
    Tester.checkEqual(shortOp("" + (short) 1, (short) -1), ("" + (short) 1) + (short) -1, "shortOp(\"\" + (short) 1, (short) -1)");
    Tester.checkEqual(shortAssign("" + (short) 1, (short) -1), ("" + (short) 1) + (short) -1, "shortOp(\"\" + (short) 1, (short) -1)");
    Tester.checkEqual(shortOp("" + (short) 1, (short) 0), ("" + (short) 1) + (short) 0, "shortOp(\"\" + (short) 1, (short) 0)");
    Tester.checkEqual(shortAssign("" + (short) 1, (short) 0), ("" + (short) 1) + (short) 0, "shortOp(\"\" + (short) 1, (short) 0)");
    Tester.checkEqual(shortOp("" + (short) 1, (short) 1), ("" + (short) 1) + (short) 1, "shortOp(\"\" + (short) 1, (short) 1)");
    Tester.checkEqual(shortAssign("" + (short) 1, (short) 1), ("" + (short) 1) + (short) 1, "shortOp(\"\" + (short) 1, (short) 1)");
    Tester.checkEqual(shortOp("" + (short) 1, Short.MAX_VALUE), ("" + (short) 1) + Short.MAX_VALUE, "shortOp(\"\" + (short) 1, Short.MAX_VALUE)");
    Tester.checkEqual(shortAssign("" + (short) 1, Short.MAX_VALUE), ("" + (short) 1) + Short.MAX_VALUE, "shortOp(\"\" + (short) 1, Short.MAX_VALUE)");
    Tester.checkEqual(shortOp("" + Short.MAX_VALUE, Short.MIN_VALUE), ("" + Short.MAX_VALUE) + Short.MIN_VALUE, "shortOp(\"\" + Short.MAX_VALUE, Short.MIN_VALUE)");
    Tester.checkEqual(shortAssign("" + Short.MAX_VALUE, Short.MIN_VALUE), ("" + Short.MAX_VALUE) + Short.MIN_VALUE, "shortOp(\"\" + Short.MAX_VALUE, Short.MIN_VALUE)");
    Tester.checkEqual(shortOp("" + Short.MAX_VALUE, (short) -1), ("" + Short.MAX_VALUE) + (short) -1, "shortOp(\"\" + Short.MAX_VALUE, (short) -1)");
    Tester.checkEqual(shortAssign("" + Short.MAX_VALUE, (short) -1), ("" + Short.MAX_VALUE) + (short) -1, "shortOp(\"\" + Short.MAX_VALUE, (short) -1)");
    Tester.checkEqual(shortOp("" + Short.MAX_VALUE, (short) 0), ("" + Short.MAX_VALUE) + (short) 0, "shortOp(\"\" + Short.MAX_VALUE, (short) 0)");
    Tester.checkEqual(shortAssign("" + Short.MAX_VALUE, (short) 0), ("" + Short.MAX_VALUE) + (short) 0, "shortOp(\"\" + Short.MAX_VALUE, (short) 0)");
    Tester.checkEqual(shortOp("" + Short.MAX_VALUE, (short) 1), ("" + Short.MAX_VALUE) + (short) 1, "shortOp(\"\" + Short.MAX_VALUE, (short) 1)");
    Tester.checkEqual(shortAssign("" + Short.MAX_VALUE, (short) 1), ("" + Short.MAX_VALUE) + (short) 1, "shortOp(\"\" + Short.MAX_VALUE, (short) 1)");
    Tester.checkEqual(shortOp("" + Short.MAX_VALUE, Short.MAX_VALUE), ("" + Short.MAX_VALUE) + Short.MAX_VALUE, "shortOp(\"\" + Short.MAX_VALUE, Short.MAX_VALUE)");
    Tester.checkEqual(shortAssign("" + Short.MAX_VALUE, Short.MAX_VALUE), ("" + Short.MAX_VALUE) + Short.MAX_VALUE, "shortOp(\"\" + Short.MAX_VALUE, Short.MAX_VALUE)");
  }
  static void shortSwitch() {
    switch(-1) {
      case (((("" + Short.MIN_VALUE) + Short.MIN_VALUE) == "") ? 0 : 0):
      case (((("" + Short.MIN_VALUE) + (short) -1) == "") ? 1 : 1):
      case (((("" + Short.MIN_VALUE) + (short) 0) == "") ? 2 : 2):
      case (((("" + Short.MIN_VALUE) + (short) 1) == "") ? 3 : 3):
      case (((("" + Short.MIN_VALUE) + Short.MAX_VALUE) == "") ? 4 : 4):
      case (((("" + (short) -1) + Short.MIN_VALUE) == "") ? 5 : 5):
      case (((("" + (short) -1) + (short) -1) == "") ? 6 : 6):
      case (((("" + (short) -1) + (short) 0) == "") ? 7 : 7):
      case (((("" + (short) -1) + (short) 1) == "") ? 8 : 8):
      case (((("" + (short) -1) + Short.MAX_VALUE) == "") ? 9 : 9):
      case (((("" + (short) 0) + Short.MIN_VALUE) == "") ? 10 : 10):
      case (((("" + (short) 0) + (short) -1) == "") ? 11 : 11):
      case (((("" + (short) 0) + (short) 0) == "") ? 12 : 12):
      case (((("" + (short) 0) + (short) 1) == "") ? 13 : 13):
      case (((("" + (short) 0) + Short.MAX_VALUE) == "") ? 14 : 14):
      case (((("" + (short) 1) + Short.MIN_VALUE) == "") ? 15 : 15):
      case (((("" + (short) 1) + (short) -1) == "") ? 16 : 16):
      case (((("" + (short) 1) + (short) 0) == "") ? 17 : 17):
      case (((("" + (short) 1) + (short) 1) == "") ? 18 : 18):
      case (((("" + (short) 1) + Short.MAX_VALUE) == "") ? 19 : 19):
      case (((("" + Short.MAX_VALUE) + Short.MIN_VALUE) == "") ? 20 : 20):
      case (((("" + Short.MAX_VALUE) + (short) -1) == "") ? 21 : 21):
      case (((("" + Short.MAX_VALUE) + (short) 0) == "") ? 22 : 22):
      case (((("" + Short.MAX_VALUE) + (short) 1) == "") ? 23 : 23):
      case (((("" + Short.MAX_VALUE) + Short.MAX_VALUE) == "") ? 24 : 24):
      default:
    }
  }

  // --------
  // char tests
  static String charOp(String x, char y) { return x + y; }
  static String charAssign(String x, char y) { x += y; return x; }
  static void charTest() {
    Tester.checkEqual(charOp("" + (char) 0, (char) 0), ("" + (char) 0) + (char) 0, "charOp(\"\" + (char) 0, (char) 0)");
    Tester.checkEqual(charAssign("" + (char) 0, (char) 0), ("" + (char) 0) + (char) 0, "charOp(\"\" + (char) 0, (char) 0)");
    Tester.checkEqual(charOp("" + (char) 0, (char) 1), ("" + (char) 0) + (char) 1, "charOp(\"\" + (char) 0, (char) 1)");
    Tester.checkEqual(charAssign("" + (char) 0, (char) 1), ("" + (char) 0) + (char) 1, "charOp(\"\" + (char) 0, (char) 1)");
    Tester.checkEqual(charOp("" + (char) 0, Character.MAX_VALUE), ("" + (char) 0) + Character.MAX_VALUE, "charOp(\"\" + (char) 0, Character.MAX_VALUE)");
    Tester.checkEqual(charAssign("" + (char) 0, Character.MAX_VALUE), ("" + (char) 0) + Character.MAX_VALUE, "charOp(\"\" + (char) 0, Character.MAX_VALUE)");
    Tester.checkEqual(charOp("" + (char) 1, (char) 0), ("" + (char) 1) + (char) 0, "charOp(\"\" + (char) 1, (char) 0)");
    Tester.checkEqual(charAssign("" + (char) 1, (char) 0), ("" + (char) 1) + (char) 0, "charOp(\"\" + (char) 1, (char) 0)");
    Tester.checkEqual(charOp("" + (char) 1, (char) 1), ("" + (char) 1) + (char) 1, "charOp(\"\" + (char) 1, (char) 1)");
    Tester.checkEqual(charAssign("" + (char) 1, (char) 1), ("" + (char) 1) + (char) 1, "charOp(\"\" + (char) 1, (char) 1)");
    Tester.checkEqual(charOp("" + (char) 1, Character.MAX_VALUE), ("" + (char) 1) + Character.MAX_VALUE, "charOp(\"\" + (char) 1, Character.MAX_VALUE)");
    Tester.checkEqual(charAssign("" + (char) 1, Character.MAX_VALUE), ("" + (char) 1) + Character.MAX_VALUE, "charOp(\"\" + (char) 1, Character.MAX_VALUE)");
    Tester.checkEqual(charOp("" + Character.MAX_VALUE, (char) 0), ("" + Character.MAX_VALUE) + (char) 0, "charOp(\"\" + Character.MAX_VALUE, (char) 0)");
    Tester.checkEqual(charAssign("" + Character.MAX_VALUE, (char) 0), ("" + Character.MAX_VALUE) + (char) 0, "charOp(\"\" + Character.MAX_VALUE, (char) 0)");
    Tester.checkEqual(charOp("" + Character.MAX_VALUE, (char) 1), ("" + Character.MAX_VALUE) + (char) 1, "charOp(\"\" + Character.MAX_VALUE, (char) 1)");
    Tester.checkEqual(charAssign("" + Character.MAX_VALUE, (char) 1), ("" + Character.MAX_VALUE) + (char) 1, "charOp(\"\" + Character.MAX_VALUE, (char) 1)");
    Tester.checkEqual(charOp("" + Character.MAX_VALUE, Character.MAX_VALUE), ("" + Character.MAX_VALUE) + Character.MAX_VALUE, "charOp(\"\" + Character.MAX_VALUE, Character.MAX_VALUE)");
    Tester.checkEqual(charAssign("" + Character.MAX_VALUE, Character.MAX_VALUE), ("" + Character.MAX_VALUE) + Character.MAX_VALUE, "charOp(\"\" + Character.MAX_VALUE, Character.MAX_VALUE)");
  }
  static void charSwitch() {
    switch(-1) {
      case (((("" + (char) 0) + (char) 0) == "") ? 0 : 0):
      case (((("" + (char) 0) + (char) 1) == "") ? 1 : 1):
      case (((("" + (char) 0) + Character.MAX_VALUE) == "") ? 2 : 2):
      case (((("" + (char) 1) + (char) 0) == "") ? 3 : 3):
      case (((("" + (char) 1) + (char) 1) == "") ? 4 : 4):
      case (((("" + (char) 1) + Character.MAX_VALUE) == "") ? 5 : 5):
      case (((("" + Character.MAX_VALUE) + (char) 0) == "") ? 6 : 6):
      case (((("" + Character.MAX_VALUE) + (char) 1) == "") ? 7 : 7):
      case (((("" + Character.MAX_VALUE) + Character.MAX_VALUE) == "") ? 8 : 8):
      default:
    }
  }

  // --------
  // int tests
  static String intOp(String x, int y) { return x + y; }
  static String intAssign(String x, int y) { x += y; return x; }
  static void intTest() {
    Tester.checkEqual(intOp("" + Integer.MIN_VALUE, Integer.MIN_VALUE), ("" + Integer.MIN_VALUE) + Integer.MIN_VALUE, "intOp(\"\" + Integer.MIN_VALUE, Integer.MIN_VALUE)");
    Tester.checkEqual(intAssign("" + Integer.MIN_VALUE, Integer.MIN_VALUE), ("" + Integer.MIN_VALUE) + Integer.MIN_VALUE, "intOp(\"\" + Integer.MIN_VALUE, Integer.MIN_VALUE)");
    Tester.checkEqual(intOp("" + Integer.MIN_VALUE, -1), ("" + Integer.MIN_VALUE) + -1, "intOp(\"\" + Integer.MIN_VALUE, -1)");
    Tester.checkEqual(intAssign("" + Integer.MIN_VALUE, -1), ("" + Integer.MIN_VALUE) + -1, "intOp(\"\" + Integer.MIN_VALUE, -1)");
    Tester.checkEqual(intOp("" + Integer.MIN_VALUE, 0), ("" + Integer.MIN_VALUE) + 0, "intOp(\"\" + Integer.MIN_VALUE, 0)");
    Tester.checkEqual(intAssign("" + Integer.MIN_VALUE, 0), ("" + Integer.MIN_VALUE) + 0, "intOp(\"\" + Integer.MIN_VALUE, 0)");
    Tester.checkEqual(intOp("" + Integer.MIN_VALUE, 1), ("" + Integer.MIN_VALUE) + 1, "intOp(\"\" + Integer.MIN_VALUE, 1)");
    Tester.checkEqual(intAssign("" + Integer.MIN_VALUE, 1), ("" + Integer.MIN_VALUE) + 1, "intOp(\"\" + Integer.MIN_VALUE, 1)");
    Tester.checkEqual(intOp("" + Integer.MIN_VALUE, Integer.MAX_VALUE), ("" + Integer.MIN_VALUE) + Integer.MAX_VALUE, "intOp(\"\" + Integer.MIN_VALUE, Integer.MAX_VALUE)");
    Tester.checkEqual(intAssign("" + Integer.MIN_VALUE, Integer.MAX_VALUE), ("" + Integer.MIN_VALUE) + Integer.MAX_VALUE, "intOp(\"\" + Integer.MIN_VALUE, Integer.MAX_VALUE)");
    Tester.checkEqual(intOp("" + -1, Integer.MIN_VALUE), ("" + -1) + Integer.MIN_VALUE, "intOp(\"\" + -1, Integer.MIN_VALUE)");
    Tester.checkEqual(intAssign("" + -1, Integer.MIN_VALUE), ("" + -1) + Integer.MIN_VALUE, "intOp(\"\" + -1, Integer.MIN_VALUE)");
    Tester.checkEqual(intOp("" + -1, -1), ("" + -1) + -1, "intOp(\"\" + -1, -1)");
    Tester.checkEqual(intAssign("" + -1, -1), ("" + -1) + -1, "intOp(\"\" + -1, -1)");
    Tester.checkEqual(intOp("" + -1, 0), ("" + -1) + 0, "intOp(\"\" + -1, 0)");
    Tester.checkEqual(intAssign("" + -1, 0), ("" + -1) + 0, "intOp(\"\" + -1, 0)");
    Tester.checkEqual(intOp("" + -1, 1), ("" + -1) + 1, "intOp(\"\" + -1, 1)");
    Tester.checkEqual(intAssign("" + -1, 1), ("" + -1) + 1, "intOp(\"\" + -1, 1)");
    Tester.checkEqual(intOp("" + -1, Integer.MAX_VALUE), ("" + -1) + Integer.MAX_VALUE, "intOp(\"\" + -1, Integer.MAX_VALUE)");
    Tester.checkEqual(intAssign("" + -1, Integer.MAX_VALUE), ("" + -1) + Integer.MAX_VALUE, "intOp(\"\" + -1, Integer.MAX_VALUE)");
    Tester.checkEqual(intOp("" + 0, Integer.MIN_VALUE), ("" + 0) + Integer.MIN_VALUE, "intOp(\"\" + 0, Integer.MIN_VALUE)");
    Tester.checkEqual(intAssign("" + 0, Integer.MIN_VALUE), ("" + 0) + Integer.MIN_VALUE, "intOp(\"\" + 0, Integer.MIN_VALUE)");
    Tester.checkEqual(intOp("" + 0, -1), ("" + 0) + -1, "intOp(\"\" + 0, -1)");
    Tester.checkEqual(intAssign("" + 0, -1), ("" + 0) + -1, "intOp(\"\" + 0, -1)");
    Tester.checkEqual(intOp("" + 0, 0), ("" + 0) + 0, "intOp(\"\" + 0, 0)");
    Tester.checkEqual(intAssign("" + 0, 0), ("" + 0) + 0, "intOp(\"\" + 0, 0)");
    Tester.checkEqual(intOp("" + 0, 1), ("" + 0) + 1, "intOp(\"\" + 0, 1)");
    Tester.checkEqual(intAssign("" + 0, 1), ("" + 0) + 1, "intOp(\"\" + 0, 1)");
    Tester.checkEqual(intOp("" + 0, Integer.MAX_VALUE), ("" + 0) + Integer.MAX_VALUE, "intOp(\"\" + 0, Integer.MAX_VALUE)");
    Tester.checkEqual(intAssign("" + 0, Integer.MAX_VALUE), ("" + 0) + Integer.MAX_VALUE, "intOp(\"\" + 0, Integer.MAX_VALUE)");
    Tester.checkEqual(intOp("" + 1, Integer.MIN_VALUE), ("" + 1) + Integer.MIN_VALUE, "intOp(\"\" + 1, Integer.MIN_VALUE)");
    Tester.checkEqual(intAssign("" + 1, Integer.MIN_VALUE), ("" + 1) + Integer.MIN_VALUE, "intOp(\"\" + 1, Integer.MIN_VALUE)");
    Tester.checkEqual(intOp("" + 1, -1), ("" + 1) + -1, "intOp(\"\" + 1, -1)");
    Tester.checkEqual(intAssign("" + 1, -1), ("" + 1) + -1, "intOp(\"\" + 1, -1)");
    Tester.checkEqual(intOp("" + 1, 0), ("" + 1) + 0, "intOp(\"\" + 1, 0)");
    Tester.checkEqual(intAssign("" + 1, 0), ("" + 1) + 0, "intOp(\"\" + 1, 0)");
    Tester.checkEqual(intOp("" + 1, 1), ("" + 1) + 1, "intOp(\"\" + 1, 1)");
    Tester.checkEqual(intAssign("" + 1, 1), ("" + 1) + 1, "intOp(\"\" + 1, 1)");
    Tester.checkEqual(intOp("" + 1, Integer.MAX_VALUE), ("" + 1) + Integer.MAX_VALUE, "intOp(\"\" + 1, Integer.MAX_VALUE)");
    Tester.checkEqual(intAssign("" + 1, Integer.MAX_VALUE), ("" + 1) + Integer.MAX_VALUE, "intOp(\"\" + 1, Integer.MAX_VALUE)");
    Tester.checkEqual(intOp("" + Integer.MAX_VALUE, Integer.MIN_VALUE), ("" + Integer.MAX_VALUE) + Integer.MIN_VALUE, "intOp(\"\" + Integer.MAX_VALUE, Integer.MIN_VALUE)");
    Tester.checkEqual(intAssign("" + Integer.MAX_VALUE, Integer.MIN_VALUE), ("" + Integer.MAX_VALUE) + Integer.MIN_VALUE, "intOp(\"\" + Integer.MAX_VALUE, Integer.MIN_VALUE)");
    Tester.checkEqual(intOp("" + Integer.MAX_VALUE, -1), ("" + Integer.MAX_VALUE) + -1, "intOp(\"\" + Integer.MAX_VALUE, -1)");
    Tester.checkEqual(intAssign("" + Integer.MAX_VALUE, -1), ("" + Integer.MAX_VALUE) + -1, "intOp(\"\" + Integer.MAX_VALUE, -1)");
    Tester.checkEqual(intOp("" + Integer.MAX_VALUE, 0), ("" + Integer.MAX_VALUE) + 0, "intOp(\"\" + Integer.MAX_VALUE, 0)");
    Tester.checkEqual(intAssign("" + Integer.MAX_VALUE, 0), ("" + Integer.MAX_VALUE) + 0, "intOp(\"\" + Integer.MAX_VALUE, 0)");
    Tester.checkEqual(intOp("" + Integer.MAX_VALUE, 1), ("" + Integer.MAX_VALUE) + 1, "intOp(\"\" + Integer.MAX_VALUE, 1)");
    Tester.checkEqual(intAssign("" + Integer.MAX_VALUE, 1), ("" + Integer.MAX_VALUE) + 1, "intOp(\"\" + Integer.MAX_VALUE, 1)");
    Tester.checkEqual(intOp("" + Integer.MAX_VALUE, Integer.MAX_VALUE), ("" + Integer.MAX_VALUE) + Integer.MAX_VALUE, "intOp(\"\" + Integer.MAX_VALUE, Integer.MAX_VALUE)");
    Tester.checkEqual(intAssign("" + Integer.MAX_VALUE, Integer.MAX_VALUE), ("" + Integer.MAX_VALUE) + Integer.MAX_VALUE, "intOp(\"\" + Integer.MAX_VALUE, Integer.MAX_VALUE)");
  }
  static void intSwitch() {
    switch(-1) {
      case (((("" + Integer.MIN_VALUE) + Integer.MIN_VALUE) == "") ? 0 : 0):
      case (((("" + Integer.MIN_VALUE) + -1) == "") ? 1 : 1):
      case (((("" + Integer.MIN_VALUE) + 0) == "") ? 2 : 2):
      case (((("" + Integer.MIN_VALUE) + 1) == "") ? 3 : 3):
      case (((("" + Integer.MIN_VALUE) + Integer.MAX_VALUE) == "") ? 4 : 4):
      case (((("" + -1) + Integer.MIN_VALUE) == "") ? 5 : 5):
      case (((("" + -1) + -1) == "") ? 6 : 6):
      case (((("" + -1) + 0) == "") ? 7 : 7):
      case (((("" + -1) + 1) == "") ? 8 : 8):
      case (((("" + -1) + Integer.MAX_VALUE) == "") ? 9 : 9):
      case (((("" + 0) + Integer.MIN_VALUE) == "") ? 10 : 10):
      case (((("" + 0) + -1) == "") ? 11 : 11):
      case (((("" + 0) + 0) == "") ? 12 : 12):
      case (((("" + 0) + 1) == "") ? 13 : 13):
      case (((("" + 0) + Integer.MAX_VALUE) == "") ? 14 : 14):
      case (((("" + 1) + Integer.MIN_VALUE) == "") ? 15 : 15):
      case (((("" + 1) + -1) == "") ? 16 : 16):
      case (((("" + 1) + 0) == "") ? 17 : 17):
      case (((("" + 1) + 1) == "") ? 18 : 18):
      case (((("" + 1) + Integer.MAX_VALUE) == "") ? 19 : 19):
      case (((("" + Integer.MAX_VALUE) + Integer.MIN_VALUE) == "") ? 20 : 20):
      case (((("" + Integer.MAX_VALUE) + -1) == "") ? 21 : 21):
      case (((("" + Integer.MAX_VALUE) + 0) == "") ? 22 : 22):
      case (((("" + Integer.MAX_VALUE) + 1) == "") ? 23 : 23):
      case (((("" + Integer.MAX_VALUE) + Integer.MAX_VALUE) == "") ? 24 : 24):
      default:
    }
  }

  // --------
  // long tests
  static String longOp(String x, long y) { return x + y; }
  static String longAssign(String x, long y) { x += y; return x; }
  static void longTest() {
    Tester.checkEqual(longOp("" + Long.MIN_VALUE, Long.MIN_VALUE), ("" + Long.MIN_VALUE) + Long.MIN_VALUE, "longOp(\"\" + Long.MIN_VALUE, Long.MIN_VALUE)");
    Tester.checkEqual(longAssign("" + Long.MIN_VALUE, Long.MIN_VALUE), ("" + Long.MIN_VALUE) + Long.MIN_VALUE, "longOp(\"\" + Long.MIN_VALUE, Long.MIN_VALUE)");
    Tester.checkEqual(longOp("" + Long.MIN_VALUE, -1L), ("" + Long.MIN_VALUE) + -1L, "longOp(\"\" + Long.MIN_VALUE, -1L)");
    Tester.checkEqual(longAssign("" + Long.MIN_VALUE, -1L), ("" + Long.MIN_VALUE) + -1L, "longOp(\"\" + Long.MIN_VALUE, -1L)");
    Tester.checkEqual(longOp("" + Long.MIN_VALUE, 0L), ("" + Long.MIN_VALUE) + 0L, "longOp(\"\" + Long.MIN_VALUE, 0L)");
    Tester.checkEqual(longAssign("" + Long.MIN_VALUE, 0L), ("" + Long.MIN_VALUE) + 0L, "longOp(\"\" + Long.MIN_VALUE, 0L)");
    Tester.checkEqual(longOp("" + Long.MIN_VALUE, 1L), ("" + Long.MIN_VALUE) + 1L, "longOp(\"\" + Long.MIN_VALUE, 1L)");
    Tester.checkEqual(longAssign("" + Long.MIN_VALUE, 1L), ("" + Long.MIN_VALUE) + 1L, "longOp(\"\" + Long.MIN_VALUE, 1L)");
    Tester.checkEqual(longOp("" + Long.MIN_VALUE, Long.MAX_VALUE), ("" + Long.MIN_VALUE) + Long.MAX_VALUE, "longOp(\"\" + Long.MIN_VALUE, Long.MAX_VALUE)");
    Tester.checkEqual(longAssign("" + Long.MIN_VALUE, Long.MAX_VALUE), ("" + Long.MIN_VALUE) + Long.MAX_VALUE, "longOp(\"\" + Long.MIN_VALUE, Long.MAX_VALUE)");
    Tester.checkEqual(longOp("" + -1L, Long.MIN_VALUE), ("" + -1L) + Long.MIN_VALUE, "longOp(\"\" + -1L, Long.MIN_VALUE)");
    Tester.checkEqual(longAssign("" + -1L, Long.MIN_VALUE), ("" + -1L) + Long.MIN_VALUE, "longOp(\"\" + -1L, Long.MIN_VALUE)");
    Tester.checkEqual(longOp("" + -1L, -1L), ("" + -1L) + -1L, "longOp(\"\" + -1L, -1L)");
    Tester.checkEqual(longAssign("" + -1L, -1L), ("" + -1L) + -1L, "longOp(\"\" + -1L, -1L)");
    Tester.checkEqual(longOp("" + -1L, 0L), ("" + -1L) + 0L, "longOp(\"\" + -1L, 0L)");
    Tester.checkEqual(longAssign("" + -1L, 0L), ("" + -1L) + 0L, "longOp(\"\" + -1L, 0L)");
    Tester.checkEqual(longOp("" + -1L, 1L), ("" + -1L) + 1L, "longOp(\"\" + -1L, 1L)");
    Tester.checkEqual(longAssign("" + -1L, 1L), ("" + -1L) + 1L, "longOp(\"\" + -1L, 1L)");
    Tester.checkEqual(longOp("" + -1L, Long.MAX_VALUE), ("" + -1L) + Long.MAX_VALUE, "longOp(\"\" + -1L, Long.MAX_VALUE)");
    Tester.checkEqual(longAssign("" + -1L, Long.MAX_VALUE), ("" + -1L) + Long.MAX_VALUE, "longOp(\"\" + -1L, Long.MAX_VALUE)");
    Tester.checkEqual(longOp("" + 0L, Long.MIN_VALUE), ("" + 0L) + Long.MIN_VALUE, "longOp(\"\" + 0L, Long.MIN_VALUE)");
    Tester.checkEqual(longAssign("" + 0L, Long.MIN_VALUE), ("" + 0L) + Long.MIN_VALUE, "longOp(\"\" + 0L, Long.MIN_VALUE)");
    Tester.checkEqual(longOp("" + 0L, -1L), ("" + 0L) + -1L, "longOp(\"\" + 0L, -1L)");
    Tester.checkEqual(longAssign("" + 0L, -1L), ("" + 0L) + -1L, "longOp(\"\" + 0L, -1L)");
    Tester.checkEqual(longOp("" + 0L, 0L), ("" + 0L) + 0L, "longOp(\"\" + 0L, 0L)");
    Tester.checkEqual(longAssign("" + 0L, 0L), ("" + 0L) + 0L, "longOp(\"\" + 0L, 0L)");
    Tester.checkEqual(longOp("" + 0L, 1L), ("" + 0L) + 1L, "longOp(\"\" + 0L, 1L)");
    Tester.checkEqual(longAssign("" + 0L, 1L), ("" + 0L) + 1L, "longOp(\"\" + 0L, 1L)");
    Tester.checkEqual(longOp("" + 0L, Long.MAX_VALUE), ("" + 0L) + Long.MAX_VALUE, "longOp(\"\" + 0L, Long.MAX_VALUE)");
    Tester.checkEqual(longAssign("" + 0L, Long.MAX_VALUE), ("" + 0L) + Long.MAX_VALUE, "longOp(\"\" + 0L, Long.MAX_VALUE)");
    Tester.checkEqual(longOp("" + 1L, Long.MIN_VALUE), ("" + 1L) + Long.MIN_VALUE, "longOp(\"\" + 1L, Long.MIN_VALUE)");
    Tester.checkEqual(longAssign("" + 1L, Long.MIN_VALUE), ("" + 1L) + Long.MIN_VALUE, "longOp(\"\" + 1L, Long.MIN_VALUE)");
    Tester.checkEqual(longOp("" + 1L, -1L), ("" + 1L) + -1L, "longOp(\"\" + 1L, -1L)");
    Tester.checkEqual(longAssign("" + 1L, -1L), ("" + 1L) + -1L, "longOp(\"\" + 1L, -1L)");
    Tester.checkEqual(longOp("" + 1L, 0L), ("" + 1L) + 0L, "longOp(\"\" + 1L, 0L)");
    Tester.checkEqual(longAssign("" + 1L, 0L), ("" + 1L) + 0L, "longOp(\"\" + 1L, 0L)");
    Tester.checkEqual(longOp("" + 1L, 1L), ("" + 1L) + 1L, "longOp(\"\" + 1L, 1L)");
    Tester.checkEqual(longAssign("" + 1L, 1L), ("" + 1L) + 1L, "longOp(\"\" + 1L, 1L)");
    Tester.checkEqual(longOp("" + 1L, Long.MAX_VALUE), ("" + 1L) + Long.MAX_VALUE, "longOp(\"\" + 1L, Long.MAX_VALUE)");
    Tester.checkEqual(longAssign("" + 1L, Long.MAX_VALUE), ("" + 1L) + Long.MAX_VALUE, "longOp(\"\" + 1L, Long.MAX_VALUE)");
    Tester.checkEqual(longOp("" + Long.MAX_VALUE, Long.MIN_VALUE), ("" + Long.MAX_VALUE) + Long.MIN_VALUE, "longOp(\"\" + Long.MAX_VALUE, Long.MIN_VALUE)");
    Tester.checkEqual(longAssign("" + Long.MAX_VALUE, Long.MIN_VALUE), ("" + Long.MAX_VALUE) + Long.MIN_VALUE, "longOp(\"\" + Long.MAX_VALUE, Long.MIN_VALUE)");
    Tester.checkEqual(longOp("" + Long.MAX_VALUE, -1L), ("" + Long.MAX_VALUE) + -1L, "longOp(\"\" + Long.MAX_VALUE, -1L)");
    Tester.checkEqual(longAssign("" + Long.MAX_VALUE, -1L), ("" + Long.MAX_VALUE) + -1L, "longOp(\"\" + Long.MAX_VALUE, -1L)");
    Tester.checkEqual(longOp("" + Long.MAX_VALUE, 0L), ("" + Long.MAX_VALUE) + 0L, "longOp(\"\" + Long.MAX_VALUE, 0L)");
    Tester.checkEqual(longAssign("" + Long.MAX_VALUE, 0L), ("" + Long.MAX_VALUE) + 0L, "longOp(\"\" + Long.MAX_VALUE, 0L)");
    Tester.checkEqual(longOp("" + Long.MAX_VALUE, 1L), ("" + Long.MAX_VALUE) + 1L, "longOp(\"\" + Long.MAX_VALUE, 1L)");
    Tester.checkEqual(longAssign("" + Long.MAX_VALUE, 1L), ("" + Long.MAX_VALUE) + 1L, "longOp(\"\" + Long.MAX_VALUE, 1L)");
    Tester.checkEqual(longOp("" + Long.MAX_VALUE, Long.MAX_VALUE), ("" + Long.MAX_VALUE) + Long.MAX_VALUE, "longOp(\"\" + Long.MAX_VALUE, Long.MAX_VALUE)");
    Tester.checkEqual(longAssign("" + Long.MAX_VALUE, Long.MAX_VALUE), ("" + Long.MAX_VALUE) + Long.MAX_VALUE, "longOp(\"\" + Long.MAX_VALUE, Long.MAX_VALUE)");
  }
  static void longSwitch() {
    switch(-1) {
      case (((("" + Long.MIN_VALUE) + Long.MIN_VALUE) == "") ? 0 : 0):
      case (((("" + Long.MIN_VALUE) + -1L) == "") ? 1 : 1):
      case (((("" + Long.MIN_VALUE) + 0L) == "") ? 2 : 2):
      case (((("" + Long.MIN_VALUE) + 1L) == "") ? 3 : 3):
      case (((("" + Long.MIN_VALUE) + Long.MAX_VALUE) == "") ? 4 : 4):
      case (((("" + -1L) + Long.MIN_VALUE) == "") ? 5 : 5):
      case (((("" + -1L) + -1L) == "") ? 6 : 6):
      case (((("" + -1L) + 0L) == "") ? 7 : 7):
      case (((("" + -1L) + 1L) == "") ? 8 : 8):
      case (((("" + -1L) + Long.MAX_VALUE) == "") ? 9 : 9):
      case (((("" + 0L) + Long.MIN_VALUE) == "") ? 10 : 10):
      case (((("" + 0L) + -1L) == "") ? 11 : 11):
      case (((("" + 0L) + 0L) == "") ? 12 : 12):
      case (((("" + 0L) + 1L) == "") ? 13 : 13):
      case (((("" + 0L) + Long.MAX_VALUE) == "") ? 14 : 14):
      case (((("" + 1L) + Long.MIN_VALUE) == "") ? 15 : 15):
      case (((("" + 1L) + -1L) == "") ? 16 : 16):
      case (((("" + 1L) + 0L) == "") ? 17 : 17):
      case (((("" + 1L) + 1L) == "") ? 18 : 18):
      case (((("" + 1L) + Long.MAX_VALUE) == "") ? 19 : 19):
      case (((("" + Long.MAX_VALUE) + Long.MIN_VALUE) == "") ? 20 : 20):
      case (((("" + Long.MAX_VALUE) + -1L) == "") ? 21 : 21):
      case (((("" + Long.MAX_VALUE) + 0L) == "") ? 22 : 22):
      case (((("" + Long.MAX_VALUE) + 1L) == "") ? 23 : 23):
      case (((("" + Long.MAX_VALUE) + Long.MAX_VALUE) == "") ? 24 : 24):
      default:
    }
  }

  // --------
  // float tests
  static String floatOp(String x, float y) { return x + y; }
  static String floatAssign(String x, float y) { x += y; return x; }
  static void floatTest() {
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), ("" + Float.NEGATIVE_INFINITY) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), ("" + Float.NEGATIVE_INFINITY) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, -1.0f), ("" + Float.NEGATIVE_INFINITY) + -1.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, -1.0f)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, -1.0f), ("" + Float.NEGATIVE_INFINITY) + -1.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, -1.0f)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, -0.0f), ("" + Float.NEGATIVE_INFINITY) + -0.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, -0.0f)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, -0.0f), ("" + Float.NEGATIVE_INFINITY) + -0.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, -0.0f)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, 0.0f), ("" + Float.NEGATIVE_INFINITY) + 0.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, 0.0f)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, 0.0f), ("" + Float.NEGATIVE_INFINITY) + 0.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, 0.0f)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, Float.MIN_VALUE), ("" + Float.NEGATIVE_INFINITY) + Float.MIN_VALUE, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, Float.MIN_VALUE), ("" + Float.NEGATIVE_INFINITY) + Float.MIN_VALUE, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, 1.0f), ("" + Float.NEGATIVE_INFINITY) + 1.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, 1.0f)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, 1.0f), ("" + Float.NEGATIVE_INFINITY) + 1.0f, "floatOp(\"\" + Float.NEGATIVE_INFINITY, 1.0f)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, Float.MAX_VALUE), ("" + Float.NEGATIVE_INFINITY) + Float.MAX_VALUE, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, Float.MAX_VALUE), ("" + Float.NEGATIVE_INFINITY) + Float.MAX_VALUE, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), ("" + Float.NEGATIVE_INFINITY) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), ("" + Float.NEGATIVE_INFINITY) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.NEGATIVE_INFINITY, Float.NaN), ("" + Float.NEGATIVE_INFINITY) + Float.NaN, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.NaN)");
    Tester.checkEqual(floatAssign("" + Float.NEGATIVE_INFINITY, Float.NaN), ("" + Float.NEGATIVE_INFINITY) + Float.NaN, "floatOp(\"\" + Float.NEGATIVE_INFINITY, Float.NaN)");
    Tester.checkEqual(floatOp("" + -1.0f, Float.NEGATIVE_INFINITY), ("" + -1.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + -1.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + -1.0f, Float.NEGATIVE_INFINITY), ("" + -1.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + -1.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + -1.0f, -1.0f), ("" + -1.0f) + -1.0f, "floatOp(\"\" + -1.0f, -1.0f)");
    Tester.checkEqual(floatAssign("" + -1.0f, -1.0f), ("" + -1.0f) + -1.0f, "floatOp(\"\" + -1.0f, -1.0f)");
    Tester.checkEqual(floatOp("" + -1.0f, -0.0f), ("" + -1.0f) + -0.0f, "floatOp(\"\" + -1.0f, -0.0f)");
    Tester.checkEqual(floatAssign("" + -1.0f, -0.0f), ("" + -1.0f) + -0.0f, "floatOp(\"\" + -1.0f, -0.0f)");
    Tester.checkEqual(floatOp("" + -1.0f, 0.0f), ("" + -1.0f) + 0.0f, "floatOp(\"\" + -1.0f, 0.0f)");
    Tester.checkEqual(floatAssign("" + -1.0f, 0.0f), ("" + -1.0f) + 0.0f, "floatOp(\"\" + -1.0f, 0.0f)");
    Tester.checkEqual(floatOp("" + -1.0f, Float.MIN_VALUE), ("" + -1.0f) + Float.MIN_VALUE, "floatOp(\"\" + -1.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + -1.0f, Float.MIN_VALUE), ("" + -1.0f) + Float.MIN_VALUE, "floatOp(\"\" + -1.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + -1.0f, 1.0f), ("" + -1.0f) + 1.0f, "floatOp(\"\" + -1.0f, 1.0f)");
    Tester.checkEqual(floatAssign("" + -1.0f, 1.0f), ("" + -1.0f) + 1.0f, "floatOp(\"\" + -1.0f, 1.0f)");
    Tester.checkEqual(floatOp("" + -1.0f, Float.MAX_VALUE), ("" + -1.0f) + Float.MAX_VALUE, "floatOp(\"\" + -1.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + -1.0f, Float.MAX_VALUE), ("" + -1.0f) + Float.MAX_VALUE, "floatOp(\"\" + -1.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + -1.0f, Float.POSITIVE_INFINITY), ("" + -1.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + -1.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + -1.0f, Float.POSITIVE_INFINITY), ("" + -1.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + -1.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + -1.0f, Float.NaN), ("" + -1.0f) + Float.NaN, "floatOp(\"\" + -1.0f, Float.NaN)");
    Tester.checkEqual(floatAssign("" + -1.0f, Float.NaN), ("" + -1.0f) + Float.NaN, "floatOp(\"\" + -1.0f, Float.NaN)");
    Tester.checkEqual(floatOp("" + -0.0f, Float.NEGATIVE_INFINITY), ("" + -0.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + -0.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + -0.0f, Float.NEGATIVE_INFINITY), ("" + -0.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + -0.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + -0.0f, -1.0f), ("" + -0.0f) + -1.0f, "floatOp(\"\" + -0.0f, -1.0f)");
    Tester.checkEqual(floatAssign("" + -0.0f, -1.0f), ("" + -0.0f) + -1.0f, "floatOp(\"\" + -0.0f, -1.0f)");
    Tester.checkEqual(floatOp("" + -0.0f, -0.0f), ("" + -0.0f) + -0.0f, "floatOp(\"\" + -0.0f, -0.0f)");
    Tester.checkEqual(floatAssign("" + -0.0f, -0.0f), ("" + -0.0f) + -0.0f, "floatOp(\"\" + -0.0f, -0.0f)");
    Tester.checkEqual(floatOp("" + -0.0f, 0.0f), ("" + -0.0f) + 0.0f, "floatOp(\"\" + -0.0f, 0.0f)");
    Tester.checkEqual(floatAssign("" + -0.0f, 0.0f), ("" + -0.0f) + 0.0f, "floatOp(\"\" + -0.0f, 0.0f)");
    Tester.checkEqual(floatOp("" + -0.0f, Float.MIN_VALUE), ("" + -0.0f) + Float.MIN_VALUE, "floatOp(\"\" + -0.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + -0.0f, Float.MIN_VALUE), ("" + -0.0f) + Float.MIN_VALUE, "floatOp(\"\" + -0.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + -0.0f, 1.0f), ("" + -0.0f) + 1.0f, "floatOp(\"\" + -0.0f, 1.0f)");
    Tester.checkEqual(floatAssign("" + -0.0f, 1.0f), ("" + -0.0f) + 1.0f, "floatOp(\"\" + -0.0f, 1.0f)");
    Tester.checkEqual(floatOp("" + -0.0f, Float.MAX_VALUE), ("" + -0.0f) + Float.MAX_VALUE, "floatOp(\"\" + -0.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + -0.0f, Float.MAX_VALUE), ("" + -0.0f) + Float.MAX_VALUE, "floatOp(\"\" + -0.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + -0.0f, Float.POSITIVE_INFINITY), ("" + -0.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + -0.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + -0.0f, Float.POSITIVE_INFINITY), ("" + -0.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + -0.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + -0.0f, Float.NaN), ("" + -0.0f) + Float.NaN, "floatOp(\"\" + -0.0f, Float.NaN)");
    Tester.checkEqual(floatAssign("" + -0.0f, Float.NaN), ("" + -0.0f) + Float.NaN, "floatOp(\"\" + -0.0f, Float.NaN)");
    Tester.checkEqual(floatOp("" + 0.0f, Float.NEGATIVE_INFINITY), ("" + 0.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + 0.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + 0.0f, Float.NEGATIVE_INFINITY), ("" + 0.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + 0.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + 0.0f, -1.0f), ("" + 0.0f) + -1.0f, "floatOp(\"\" + 0.0f, -1.0f)");
    Tester.checkEqual(floatAssign("" + 0.0f, -1.0f), ("" + 0.0f) + -1.0f, "floatOp(\"\" + 0.0f, -1.0f)");
    Tester.checkEqual(floatOp("" + 0.0f, -0.0f), ("" + 0.0f) + -0.0f, "floatOp(\"\" + 0.0f, -0.0f)");
    Tester.checkEqual(floatAssign("" + 0.0f, -0.0f), ("" + 0.0f) + -0.0f, "floatOp(\"\" + 0.0f, -0.0f)");
    Tester.checkEqual(floatOp("" + 0.0f, 0.0f), ("" + 0.0f) + 0.0f, "floatOp(\"\" + 0.0f, 0.0f)");
    Tester.checkEqual(floatAssign("" + 0.0f, 0.0f), ("" + 0.0f) + 0.0f, "floatOp(\"\" + 0.0f, 0.0f)");
    Tester.checkEqual(floatOp("" + 0.0f, Float.MIN_VALUE), ("" + 0.0f) + Float.MIN_VALUE, "floatOp(\"\" + 0.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + 0.0f, Float.MIN_VALUE), ("" + 0.0f) + Float.MIN_VALUE, "floatOp(\"\" + 0.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + 0.0f, 1.0f), ("" + 0.0f) + 1.0f, "floatOp(\"\" + 0.0f, 1.0f)");
    Tester.checkEqual(floatAssign("" + 0.0f, 1.0f), ("" + 0.0f) + 1.0f, "floatOp(\"\" + 0.0f, 1.0f)");
    Tester.checkEqual(floatOp("" + 0.0f, Float.MAX_VALUE), ("" + 0.0f) + Float.MAX_VALUE, "floatOp(\"\" + 0.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + 0.0f, Float.MAX_VALUE), ("" + 0.0f) + Float.MAX_VALUE, "floatOp(\"\" + 0.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + 0.0f, Float.POSITIVE_INFINITY), ("" + 0.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + 0.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + 0.0f, Float.POSITIVE_INFINITY), ("" + 0.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + 0.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + 0.0f, Float.NaN), ("" + 0.0f) + Float.NaN, "floatOp(\"\" + 0.0f, Float.NaN)");
    Tester.checkEqual(floatAssign("" + 0.0f, Float.NaN), ("" + 0.0f) + Float.NaN, "floatOp(\"\" + 0.0f, Float.NaN)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, Float.NEGATIVE_INFINITY), ("" + Float.MIN_VALUE) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.MIN_VALUE, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, Float.NEGATIVE_INFINITY), ("" + Float.MIN_VALUE) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.MIN_VALUE, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, -1.0f), ("" + Float.MIN_VALUE) + -1.0f, "floatOp(\"\" + Float.MIN_VALUE, -1.0f)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, -1.0f), ("" + Float.MIN_VALUE) + -1.0f, "floatOp(\"\" + Float.MIN_VALUE, -1.0f)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, -0.0f), ("" + Float.MIN_VALUE) + -0.0f, "floatOp(\"\" + Float.MIN_VALUE, -0.0f)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, -0.0f), ("" + Float.MIN_VALUE) + -0.0f, "floatOp(\"\" + Float.MIN_VALUE, -0.0f)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, 0.0f), ("" + Float.MIN_VALUE) + 0.0f, "floatOp(\"\" + Float.MIN_VALUE, 0.0f)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, 0.0f), ("" + Float.MIN_VALUE) + 0.0f, "floatOp(\"\" + Float.MIN_VALUE, 0.0f)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, Float.MIN_VALUE), ("" + Float.MIN_VALUE) + Float.MIN_VALUE, "floatOp(\"\" + Float.MIN_VALUE, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, Float.MIN_VALUE), ("" + Float.MIN_VALUE) + Float.MIN_VALUE, "floatOp(\"\" + Float.MIN_VALUE, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, 1.0f), ("" + Float.MIN_VALUE) + 1.0f, "floatOp(\"\" + Float.MIN_VALUE, 1.0f)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, 1.0f), ("" + Float.MIN_VALUE) + 1.0f, "floatOp(\"\" + Float.MIN_VALUE, 1.0f)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, Float.MAX_VALUE), ("" + Float.MIN_VALUE) + Float.MAX_VALUE, "floatOp(\"\" + Float.MIN_VALUE, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, Float.MAX_VALUE), ("" + Float.MIN_VALUE) + Float.MAX_VALUE, "floatOp(\"\" + Float.MIN_VALUE, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, Float.POSITIVE_INFINITY), ("" + Float.MIN_VALUE) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.MIN_VALUE, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, Float.POSITIVE_INFINITY), ("" + Float.MIN_VALUE) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.MIN_VALUE, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.MIN_VALUE, Float.NaN), ("" + Float.MIN_VALUE) + Float.NaN, "floatOp(\"\" + Float.MIN_VALUE, Float.NaN)");
    Tester.checkEqual(floatAssign("" + Float.MIN_VALUE, Float.NaN), ("" + Float.MIN_VALUE) + Float.NaN, "floatOp(\"\" + Float.MIN_VALUE, Float.NaN)");
    Tester.checkEqual(floatOp("" + 1.0f, Float.NEGATIVE_INFINITY), ("" + 1.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + 1.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + 1.0f, Float.NEGATIVE_INFINITY), ("" + 1.0f) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + 1.0f, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + 1.0f, -1.0f), ("" + 1.0f) + -1.0f, "floatOp(\"\" + 1.0f, -1.0f)");
    Tester.checkEqual(floatAssign("" + 1.0f, -1.0f), ("" + 1.0f) + -1.0f, "floatOp(\"\" + 1.0f, -1.0f)");
    Tester.checkEqual(floatOp("" + 1.0f, -0.0f), ("" + 1.0f) + -0.0f, "floatOp(\"\" + 1.0f, -0.0f)");
    Tester.checkEqual(floatAssign("" + 1.0f, -0.0f), ("" + 1.0f) + -0.0f, "floatOp(\"\" + 1.0f, -0.0f)");
    Tester.checkEqual(floatOp("" + 1.0f, 0.0f), ("" + 1.0f) + 0.0f, "floatOp(\"\" + 1.0f, 0.0f)");
    Tester.checkEqual(floatAssign("" + 1.0f, 0.0f), ("" + 1.0f) + 0.0f, "floatOp(\"\" + 1.0f, 0.0f)");
    Tester.checkEqual(floatOp("" + 1.0f, Float.MIN_VALUE), ("" + 1.0f) + Float.MIN_VALUE, "floatOp(\"\" + 1.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + 1.0f, Float.MIN_VALUE), ("" + 1.0f) + Float.MIN_VALUE, "floatOp(\"\" + 1.0f, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + 1.0f, 1.0f), ("" + 1.0f) + 1.0f, "floatOp(\"\" + 1.0f, 1.0f)");
    Tester.checkEqual(floatAssign("" + 1.0f, 1.0f), ("" + 1.0f) + 1.0f, "floatOp(\"\" + 1.0f, 1.0f)");
    Tester.checkEqual(floatOp("" + 1.0f, Float.MAX_VALUE), ("" + 1.0f) + Float.MAX_VALUE, "floatOp(\"\" + 1.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + 1.0f, Float.MAX_VALUE), ("" + 1.0f) + Float.MAX_VALUE, "floatOp(\"\" + 1.0f, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + 1.0f, Float.POSITIVE_INFINITY), ("" + 1.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + 1.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + 1.0f, Float.POSITIVE_INFINITY), ("" + 1.0f) + Float.POSITIVE_INFINITY, "floatOp(\"\" + 1.0f, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + 1.0f, Float.NaN), ("" + 1.0f) + Float.NaN, "floatOp(\"\" + 1.0f, Float.NaN)");
    Tester.checkEqual(floatAssign("" + 1.0f, Float.NaN), ("" + 1.0f) + Float.NaN, "floatOp(\"\" + 1.0f, Float.NaN)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, Float.NEGATIVE_INFINITY), ("" + Float.MAX_VALUE) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.MAX_VALUE, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, Float.NEGATIVE_INFINITY), ("" + Float.MAX_VALUE) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.MAX_VALUE, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, -1.0f), ("" + Float.MAX_VALUE) + -1.0f, "floatOp(\"\" + Float.MAX_VALUE, -1.0f)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, -1.0f), ("" + Float.MAX_VALUE) + -1.0f, "floatOp(\"\" + Float.MAX_VALUE, -1.0f)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, -0.0f), ("" + Float.MAX_VALUE) + -0.0f, "floatOp(\"\" + Float.MAX_VALUE, -0.0f)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, -0.0f), ("" + Float.MAX_VALUE) + -0.0f, "floatOp(\"\" + Float.MAX_VALUE, -0.0f)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, 0.0f), ("" + Float.MAX_VALUE) + 0.0f, "floatOp(\"\" + Float.MAX_VALUE, 0.0f)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, 0.0f), ("" + Float.MAX_VALUE) + 0.0f, "floatOp(\"\" + Float.MAX_VALUE, 0.0f)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, Float.MIN_VALUE), ("" + Float.MAX_VALUE) + Float.MIN_VALUE, "floatOp(\"\" + Float.MAX_VALUE, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, Float.MIN_VALUE), ("" + Float.MAX_VALUE) + Float.MIN_VALUE, "floatOp(\"\" + Float.MAX_VALUE, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, 1.0f), ("" + Float.MAX_VALUE) + 1.0f, "floatOp(\"\" + Float.MAX_VALUE, 1.0f)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, 1.0f), ("" + Float.MAX_VALUE) + 1.0f, "floatOp(\"\" + Float.MAX_VALUE, 1.0f)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, Float.MAX_VALUE), ("" + Float.MAX_VALUE) + Float.MAX_VALUE, "floatOp(\"\" + Float.MAX_VALUE, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, Float.MAX_VALUE), ("" + Float.MAX_VALUE) + Float.MAX_VALUE, "floatOp(\"\" + Float.MAX_VALUE, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, Float.POSITIVE_INFINITY), ("" + Float.MAX_VALUE) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.MAX_VALUE, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, Float.POSITIVE_INFINITY), ("" + Float.MAX_VALUE) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.MAX_VALUE, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.MAX_VALUE, Float.NaN), ("" + Float.MAX_VALUE) + Float.NaN, "floatOp(\"\" + Float.MAX_VALUE, Float.NaN)");
    Tester.checkEqual(floatAssign("" + Float.MAX_VALUE, Float.NaN), ("" + Float.MAX_VALUE) + Float.NaN, "floatOp(\"\" + Float.MAX_VALUE, Float.NaN)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), ("" + Float.POSITIVE_INFINITY) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), ("" + Float.POSITIVE_INFINITY) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, -1.0f), ("" + Float.POSITIVE_INFINITY) + -1.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, -1.0f)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, -1.0f), ("" + Float.POSITIVE_INFINITY) + -1.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, -1.0f)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, -0.0f), ("" + Float.POSITIVE_INFINITY) + -0.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, -0.0f)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, -0.0f), ("" + Float.POSITIVE_INFINITY) + -0.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, -0.0f)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, 0.0f), ("" + Float.POSITIVE_INFINITY) + 0.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, 0.0f)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, 0.0f), ("" + Float.POSITIVE_INFINITY) + 0.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, 0.0f)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, Float.MIN_VALUE), ("" + Float.POSITIVE_INFINITY) + Float.MIN_VALUE, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, Float.MIN_VALUE), ("" + Float.POSITIVE_INFINITY) + Float.MIN_VALUE, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, 1.0f), ("" + Float.POSITIVE_INFINITY) + 1.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, 1.0f)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, 1.0f), ("" + Float.POSITIVE_INFINITY) + 1.0f, "floatOp(\"\" + Float.POSITIVE_INFINITY, 1.0f)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, Float.MAX_VALUE), ("" + Float.POSITIVE_INFINITY) + Float.MAX_VALUE, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, Float.MAX_VALUE), ("" + Float.POSITIVE_INFINITY) + Float.MAX_VALUE, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), ("" + Float.POSITIVE_INFINITY) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), ("" + Float.POSITIVE_INFINITY) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.POSITIVE_INFINITY, Float.NaN), ("" + Float.POSITIVE_INFINITY) + Float.NaN, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.NaN)");
    Tester.checkEqual(floatAssign("" + Float.POSITIVE_INFINITY, Float.NaN), ("" + Float.POSITIVE_INFINITY) + Float.NaN, "floatOp(\"\" + Float.POSITIVE_INFINITY, Float.NaN)");
    Tester.checkEqual(floatOp("" + Float.NaN, Float.NEGATIVE_INFINITY), ("" + Float.NaN) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.NaN, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.NaN, Float.NEGATIVE_INFINITY), ("" + Float.NaN) + Float.NEGATIVE_INFINITY, "floatOp(\"\" + Float.NaN, Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.NaN, -1.0f), ("" + Float.NaN) + -1.0f, "floatOp(\"\" + Float.NaN, -1.0f)");
    Tester.checkEqual(floatAssign("" + Float.NaN, -1.0f), ("" + Float.NaN) + -1.0f, "floatOp(\"\" + Float.NaN, -1.0f)");
    Tester.checkEqual(floatOp("" + Float.NaN, -0.0f), ("" + Float.NaN) + -0.0f, "floatOp(\"\" + Float.NaN, -0.0f)");
    Tester.checkEqual(floatAssign("" + Float.NaN, -0.0f), ("" + Float.NaN) + -0.0f, "floatOp(\"\" + Float.NaN, -0.0f)");
    Tester.checkEqual(floatOp("" + Float.NaN, 0.0f), ("" + Float.NaN) + 0.0f, "floatOp(\"\" + Float.NaN, 0.0f)");
    Tester.checkEqual(floatAssign("" + Float.NaN, 0.0f), ("" + Float.NaN) + 0.0f, "floatOp(\"\" + Float.NaN, 0.0f)");
    Tester.checkEqual(floatOp("" + Float.NaN, Float.MIN_VALUE), ("" + Float.NaN) + Float.MIN_VALUE, "floatOp(\"\" + Float.NaN, Float.MIN_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.NaN, Float.MIN_VALUE), ("" + Float.NaN) + Float.MIN_VALUE, "floatOp(\"\" + Float.NaN, Float.MIN_VALUE)");
    Tester.checkEqual(floatOp("" + Float.NaN, 1.0f), ("" + Float.NaN) + 1.0f, "floatOp(\"\" + Float.NaN, 1.0f)");
    Tester.checkEqual(floatAssign("" + Float.NaN, 1.0f), ("" + Float.NaN) + 1.0f, "floatOp(\"\" + Float.NaN, 1.0f)");
    Tester.checkEqual(floatOp("" + Float.NaN, Float.MAX_VALUE), ("" + Float.NaN) + Float.MAX_VALUE, "floatOp(\"\" + Float.NaN, Float.MAX_VALUE)");
    Tester.checkEqual(floatAssign("" + Float.NaN, Float.MAX_VALUE), ("" + Float.NaN) + Float.MAX_VALUE, "floatOp(\"\" + Float.NaN, Float.MAX_VALUE)");
    Tester.checkEqual(floatOp("" + Float.NaN, Float.POSITIVE_INFINITY), ("" + Float.NaN) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.NaN, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAssign("" + Float.NaN, Float.POSITIVE_INFINITY), ("" + Float.NaN) + Float.POSITIVE_INFINITY, "floatOp(\"\" + Float.NaN, Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatOp("" + Float.NaN, Float.NaN), ("" + Float.NaN) + Float.NaN, "floatOp(\"\" + Float.NaN, Float.NaN)");
    Tester.checkEqual(floatAssign("" + Float.NaN, Float.NaN), ("" + Float.NaN) + Float.NaN, "floatOp(\"\" + Float.NaN, Float.NaN)");
  }
  static void floatSwitch() {
    switch(-1) {
      case (((("" + Float.NEGATIVE_INFINITY) + Float.NEGATIVE_INFINITY) == "") ? 0 : 0):
      case (((("" + Float.NEGATIVE_INFINITY) + -1.0f) == "") ? 1 : 1):
      case (((("" + Float.NEGATIVE_INFINITY) + -0.0f) == "") ? 2 : 2):
      case (((("" + Float.NEGATIVE_INFINITY) + 0.0f) == "") ? 3 : 3):
      case (((("" + Float.NEGATIVE_INFINITY) + Float.MIN_VALUE) == "") ? 4 : 4):
      case (((("" + Float.NEGATIVE_INFINITY) + 1.0f) == "") ? 5 : 5):
      case (((("" + Float.NEGATIVE_INFINITY) + Float.MAX_VALUE) == "") ? 6 : 6):
      case (((("" + Float.NEGATIVE_INFINITY) + Float.POSITIVE_INFINITY) == "") ? 7 : 7):
      case (((("" + Float.NEGATIVE_INFINITY) + Float.NaN) == "") ? 8 : 8):
      case (((("" + -1.0f) + Float.NEGATIVE_INFINITY) == "") ? 9 : 9):
      case (((("" + -1.0f) + -1.0f) == "") ? 10 : 10):
      case (((("" + -1.0f) + -0.0f) == "") ? 11 : 11):
      case (((("" + -1.0f) + 0.0f) == "") ? 12 : 12):
      case (((("" + -1.0f) + Float.MIN_VALUE) == "") ? 13 : 13):
      case (((("" + -1.0f) + 1.0f) == "") ? 14 : 14):
      case (((("" + -1.0f) + Float.MAX_VALUE) == "") ? 15 : 15):
      case (((("" + -1.0f) + Float.POSITIVE_INFINITY) == "") ? 16 : 16):
      case (((("" + -1.0f) + Float.NaN) == "") ? 17 : 17):
      case (((("" + -0.0f) + Float.NEGATIVE_INFINITY) == "") ? 18 : 18):
      case (((("" + -0.0f) + -1.0f) == "") ? 19 : 19):
      case (((("" + -0.0f) + -0.0f) == "") ? 20 : 20):
      case (((("" + -0.0f) + 0.0f) == "") ? 21 : 21):
      case (((("" + -0.0f) + Float.MIN_VALUE) == "") ? 22 : 22):
      case (((("" + -0.0f) + 1.0f) == "") ? 23 : 23):
      case (((("" + -0.0f) + Float.MAX_VALUE) == "") ? 24 : 24):
      case (((("" + -0.0f) + Float.POSITIVE_INFINITY) == "") ? 25 : 25):
      case (((("" + -0.0f) + Float.NaN) == "") ? 26 : 26):
      case (((("" + 0.0f) + Float.NEGATIVE_INFINITY) == "") ? 27 : 27):
      case (((("" + 0.0f) + -1.0f) == "") ? 28 : 28):
      case (((("" + 0.0f) + -0.0f) == "") ? 29 : 29):
      case (((("" + 0.0f) + 0.0f) == "") ? 30 : 30):
      case (((("" + 0.0f) + Float.MIN_VALUE) == "") ? 31 : 31):
      case (((("" + 0.0f) + 1.0f) == "") ? 32 : 32):
      case (((("" + 0.0f) + Float.MAX_VALUE) == "") ? 33 : 33):
      case (((("" + 0.0f) + Float.POSITIVE_INFINITY) == "") ? 34 : 34):
      case (((("" + 0.0f) + Float.NaN) == "") ? 35 : 35):
      case (((("" + Float.MIN_VALUE) + Float.NEGATIVE_INFINITY) == "") ? 36 : 36):
      case (((("" + Float.MIN_VALUE) + -1.0f) == "") ? 37 : 37):
      case (((("" + Float.MIN_VALUE) + -0.0f) == "") ? 38 : 38):
      case (((("" + Float.MIN_VALUE) + 0.0f) == "") ? 39 : 39):
      case (((("" + Float.MIN_VALUE) + Float.MIN_VALUE) == "") ? 40 : 40):
      case (((("" + Float.MIN_VALUE) + 1.0f) == "") ? 41 : 41):
      case (((("" + Float.MIN_VALUE) + Float.MAX_VALUE) == "") ? 42 : 42):
      case (((("" + Float.MIN_VALUE) + Float.POSITIVE_INFINITY) == "") ? 43 : 43):
      case (((("" + Float.MIN_VALUE) + Float.NaN) == "") ? 44 : 44):
      case (((("" + 1.0f) + Float.NEGATIVE_INFINITY) == "") ? 45 : 45):
      case (((("" + 1.0f) + -1.0f) == "") ? 46 : 46):
      case (((("" + 1.0f) + -0.0f) == "") ? 47 : 47):
      case (((("" + 1.0f) + 0.0f) == "") ? 48 : 48):
      case (((("" + 1.0f) + Float.MIN_VALUE) == "") ? 49 : 49):
      case (((("" + 1.0f) + 1.0f) == "") ? 50 : 50):
      case (((("" + 1.0f) + Float.MAX_VALUE) == "") ? 51 : 51):
      case (((("" + 1.0f) + Float.POSITIVE_INFINITY) == "") ? 52 : 52):
      case (((("" + 1.0f) + Float.NaN) == "") ? 53 : 53):
      case (((("" + Float.MAX_VALUE) + Float.NEGATIVE_INFINITY) == "") ? 54 : 54):
      case (((("" + Float.MAX_VALUE) + -1.0f) == "") ? 55 : 55):
      case (((("" + Float.MAX_VALUE) + -0.0f) == "") ? 56 : 56):
      case (((("" + Float.MAX_VALUE) + 0.0f) == "") ? 57 : 57):
      case (((("" + Float.MAX_VALUE) + Float.MIN_VALUE) == "") ? 58 : 58):
      case (((("" + Float.MAX_VALUE) + 1.0f) == "") ? 59 : 59):
      case (((("" + Float.MAX_VALUE) + Float.MAX_VALUE) == "") ? 60 : 60):
      case (((("" + Float.MAX_VALUE) + Float.POSITIVE_INFINITY) == "") ? 61 : 61):
      case (((("" + Float.MAX_VALUE) + Float.NaN) == "") ? 62 : 62):
      case (((("" + Float.POSITIVE_INFINITY) + Float.NEGATIVE_INFINITY) == "") ? 63 : 63):
      case (((("" + Float.POSITIVE_INFINITY) + -1.0f) == "") ? 64 : 64):
      case (((("" + Float.POSITIVE_INFINITY) + -0.0f) == "") ? 65 : 65):
      case (((("" + Float.POSITIVE_INFINITY) + 0.0f) == "") ? 66 : 66):
      case (((("" + Float.POSITIVE_INFINITY) + Float.MIN_VALUE) == "") ? 67 : 67):
      case (((("" + Float.POSITIVE_INFINITY) + 1.0f) == "") ? 68 : 68):
      case (((("" + Float.POSITIVE_INFINITY) + Float.MAX_VALUE) == "") ? 69 : 69):
      case (((("" + Float.POSITIVE_INFINITY) + Float.POSITIVE_INFINITY) == "") ? 70 : 70):
      case (((("" + Float.POSITIVE_INFINITY) + Float.NaN) == "") ? 71 : 71):
      case (((("" + Float.NaN) + Float.NEGATIVE_INFINITY) == "") ? 72 : 72):
      case (((("" + Float.NaN) + -1.0f) == "") ? 73 : 73):
      case (((("" + Float.NaN) + -0.0f) == "") ? 74 : 74):
      case (((("" + Float.NaN) + 0.0f) == "") ? 75 : 75):
      case (((("" + Float.NaN) + Float.MIN_VALUE) == "") ? 76 : 76):
      case (((("" + Float.NaN) + 1.0f) == "") ? 77 : 77):
      case (((("" + Float.NaN) + Float.MAX_VALUE) == "") ? 78 : 78):
      case (((("" + Float.NaN) + Float.POSITIVE_INFINITY) == "") ? 79 : 79):
      case (((("" + Float.NaN) + Float.NaN) == "") ? 80 : 80):
      default:
    }
  }

  // --------
  // double tests
  static String doubleOp(String x, double y) { return x + y; }
  static String doubleAssign(String x, double y) { x += y; return x; }
  static void doubleTest() {
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), ("" + Double.NEGATIVE_INFINITY) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), ("" + Double.NEGATIVE_INFINITY) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, -1.0), ("" + Double.NEGATIVE_INFINITY) + -1.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, -1.0)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, -1.0), ("" + Double.NEGATIVE_INFINITY) + -1.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, -1.0)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, -0.0), ("" + Double.NEGATIVE_INFINITY) + -0.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, -0.0)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, -0.0), ("" + Double.NEGATIVE_INFINITY) + -0.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, -0.0)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, 0.0), ("" + Double.NEGATIVE_INFINITY) + 0.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, 0.0)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, 0.0), ("" + Double.NEGATIVE_INFINITY) + 0.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, 0.0)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, 1.0), ("" + Double.NEGATIVE_INFINITY) + 1.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, 1.0)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, 1.0), ("" + Double.NEGATIVE_INFINITY) + 1.0, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, 1.0)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, Double.MAX_VALUE), ("" + Double.NEGATIVE_INFINITY) + Double.MAX_VALUE, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, Double.MAX_VALUE), ("" + Double.NEGATIVE_INFINITY) + Double.MAX_VALUE, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), ("" + Double.NEGATIVE_INFINITY) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), ("" + Double.NEGATIVE_INFINITY) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.NEGATIVE_INFINITY, Double.NaN), ("" + Double.NEGATIVE_INFINITY) + Double.NaN, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + Double.NEGATIVE_INFINITY, Double.NaN), ("" + Double.NEGATIVE_INFINITY) + Double.NaN, "doubleOp(\"\" + Double.NEGATIVE_INFINITY, Double.NaN)");
    Tester.checkEqual(doubleOp("" + -1.0, Double.NEGATIVE_INFINITY), ("" + -1.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + -1.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + -1.0, Double.NEGATIVE_INFINITY), ("" + -1.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + -1.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + -1.0, -1.0), ("" + -1.0) + -1.0, "doubleOp(\"\" + -1.0, -1.0)");
    Tester.checkEqual(doubleAssign("" + -1.0, -1.0), ("" + -1.0) + -1.0, "doubleOp(\"\" + -1.0, -1.0)");
    Tester.checkEqual(doubleOp("" + -1.0, -0.0), ("" + -1.0) + -0.0, "doubleOp(\"\" + -1.0, -0.0)");
    Tester.checkEqual(doubleAssign("" + -1.0, -0.0), ("" + -1.0) + -0.0, "doubleOp(\"\" + -1.0, -0.0)");
    Tester.checkEqual(doubleOp("" + -1.0, 0.0), ("" + -1.0) + 0.0, "doubleOp(\"\" + -1.0, 0.0)");
    Tester.checkEqual(doubleAssign("" + -1.0, 0.0), ("" + -1.0) + 0.0, "doubleOp(\"\" + -1.0, 0.0)");
    Tester.checkEqual(doubleOp("" + -1.0, 1.0), ("" + -1.0) + 1.0, "doubleOp(\"\" + -1.0, 1.0)");
    Tester.checkEqual(doubleAssign("" + -1.0, 1.0), ("" + -1.0) + 1.0, "doubleOp(\"\" + -1.0, 1.0)");
    Tester.checkEqual(doubleOp("" + -1.0, Double.MAX_VALUE), ("" + -1.0) + Double.MAX_VALUE, "doubleOp(\"\" + -1.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + -1.0, Double.MAX_VALUE), ("" + -1.0) + Double.MAX_VALUE, "doubleOp(\"\" + -1.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + -1.0, Double.POSITIVE_INFINITY), ("" + -1.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + -1.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + -1.0, Double.POSITIVE_INFINITY), ("" + -1.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + -1.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + -1.0, Double.NaN), ("" + -1.0) + Double.NaN, "doubleOp(\"\" + -1.0, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + -1.0, Double.NaN), ("" + -1.0) + Double.NaN, "doubleOp(\"\" + -1.0, Double.NaN)");
    Tester.checkEqual(doubleOp("" + -0.0, Double.NEGATIVE_INFINITY), ("" + -0.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + -0.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + -0.0, Double.NEGATIVE_INFINITY), ("" + -0.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + -0.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + -0.0, -1.0), ("" + -0.0) + -1.0, "doubleOp(\"\" + -0.0, -1.0)");
    Tester.checkEqual(doubleAssign("" + -0.0, -1.0), ("" + -0.0) + -1.0, "doubleOp(\"\" + -0.0, -1.0)");
    Tester.checkEqual(doubleOp("" + -0.0, -0.0), ("" + -0.0) + -0.0, "doubleOp(\"\" + -0.0, -0.0)");
    Tester.checkEqual(doubleAssign("" + -0.0, -0.0), ("" + -0.0) + -0.0, "doubleOp(\"\" + -0.0, -0.0)");
    Tester.checkEqual(doubleOp("" + -0.0, 0.0), ("" + -0.0) + 0.0, "doubleOp(\"\" + -0.0, 0.0)");
    Tester.checkEqual(doubleAssign("" + -0.0, 0.0), ("" + -0.0) + 0.0, "doubleOp(\"\" + -0.0, 0.0)");
    Tester.checkEqual(doubleOp("" + -0.0, 1.0), ("" + -0.0) + 1.0, "doubleOp(\"\" + -0.0, 1.0)");
    Tester.checkEqual(doubleAssign("" + -0.0, 1.0), ("" + -0.0) + 1.0, "doubleOp(\"\" + -0.0, 1.0)");
    Tester.checkEqual(doubleOp("" + -0.0, Double.MAX_VALUE), ("" + -0.0) + Double.MAX_VALUE, "doubleOp(\"\" + -0.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + -0.0, Double.MAX_VALUE), ("" + -0.0) + Double.MAX_VALUE, "doubleOp(\"\" + -0.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + -0.0, Double.POSITIVE_INFINITY), ("" + -0.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + -0.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + -0.0, Double.POSITIVE_INFINITY), ("" + -0.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + -0.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + -0.0, Double.NaN), ("" + -0.0) + Double.NaN, "doubleOp(\"\" + -0.0, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + -0.0, Double.NaN), ("" + -0.0) + Double.NaN, "doubleOp(\"\" + -0.0, Double.NaN)");
    Tester.checkEqual(doubleOp("" + 0.0, Double.NEGATIVE_INFINITY), ("" + 0.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + 0.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + 0.0, Double.NEGATIVE_INFINITY), ("" + 0.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + 0.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + 0.0, -1.0), ("" + 0.0) + -1.0, "doubleOp(\"\" + 0.0, -1.0)");
    Tester.checkEqual(doubleAssign("" + 0.0, -1.0), ("" + 0.0) + -1.0, "doubleOp(\"\" + 0.0, -1.0)");
    Tester.checkEqual(doubleOp("" + 0.0, -0.0), ("" + 0.0) + -0.0, "doubleOp(\"\" + 0.0, -0.0)");
    Tester.checkEqual(doubleAssign("" + 0.0, -0.0), ("" + 0.0) + -0.0, "doubleOp(\"\" + 0.0, -0.0)");
    Tester.checkEqual(doubleOp("" + 0.0, 0.0), ("" + 0.0) + 0.0, "doubleOp(\"\" + 0.0, 0.0)");
    Tester.checkEqual(doubleAssign("" + 0.0, 0.0), ("" + 0.0) + 0.0, "doubleOp(\"\" + 0.0, 0.0)");
    Tester.checkEqual(doubleOp("" + 0.0, 1.0), ("" + 0.0) + 1.0, "doubleOp(\"\" + 0.0, 1.0)");
    Tester.checkEqual(doubleAssign("" + 0.0, 1.0), ("" + 0.0) + 1.0, "doubleOp(\"\" + 0.0, 1.0)");
    Tester.checkEqual(doubleOp("" + 0.0, Double.MAX_VALUE), ("" + 0.0) + Double.MAX_VALUE, "doubleOp(\"\" + 0.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + 0.0, Double.MAX_VALUE), ("" + 0.0) + Double.MAX_VALUE, "doubleOp(\"\" + 0.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + 0.0, Double.POSITIVE_INFINITY), ("" + 0.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + 0.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + 0.0, Double.POSITIVE_INFINITY), ("" + 0.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + 0.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + 0.0, Double.NaN), ("" + 0.0) + Double.NaN, "doubleOp(\"\" + 0.0, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + 0.0, Double.NaN), ("" + 0.0) + Double.NaN, "doubleOp(\"\" + 0.0, Double.NaN)");
    Tester.checkEqual(doubleOp("" + 1.0, Double.NEGATIVE_INFINITY), ("" + 1.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + 1.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + 1.0, Double.NEGATIVE_INFINITY), ("" + 1.0) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + 1.0, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + 1.0, -1.0), ("" + 1.0) + -1.0, "doubleOp(\"\" + 1.0, -1.0)");
    Tester.checkEqual(doubleAssign("" + 1.0, -1.0), ("" + 1.0) + -1.0, "doubleOp(\"\" + 1.0, -1.0)");
    Tester.checkEqual(doubleOp("" + 1.0, -0.0), ("" + 1.0) + -0.0, "doubleOp(\"\" + 1.0, -0.0)");
    Tester.checkEqual(doubleAssign("" + 1.0, -0.0), ("" + 1.0) + -0.0, "doubleOp(\"\" + 1.0, -0.0)");
    Tester.checkEqual(doubleOp("" + 1.0, 0.0), ("" + 1.0) + 0.0, "doubleOp(\"\" + 1.0, 0.0)");
    Tester.checkEqual(doubleAssign("" + 1.0, 0.0), ("" + 1.0) + 0.0, "doubleOp(\"\" + 1.0, 0.0)");
    Tester.checkEqual(doubleOp("" + 1.0, 1.0), ("" + 1.0) + 1.0, "doubleOp(\"\" + 1.0, 1.0)");
    Tester.checkEqual(doubleAssign("" + 1.0, 1.0), ("" + 1.0) + 1.0, "doubleOp(\"\" + 1.0, 1.0)");
    Tester.checkEqual(doubleOp("" + 1.0, Double.MAX_VALUE), ("" + 1.0) + Double.MAX_VALUE, "doubleOp(\"\" + 1.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + 1.0, Double.MAX_VALUE), ("" + 1.0) + Double.MAX_VALUE, "doubleOp(\"\" + 1.0, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + 1.0, Double.POSITIVE_INFINITY), ("" + 1.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + 1.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + 1.0, Double.POSITIVE_INFINITY), ("" + 1.0) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + 1.0, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + 1.0, Double.NaN), ("" + 1.0) + Double.NaN, "doubleOp(\"\" + 1.0, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + 1.0, Double.NaN), ("" + 1.0) + Double.NaN, "doubleOp(\"\" + 1.0, Double.NaN)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, Double.NEGATIVE_INFINITY), ("" + Double.MAX_VALUE) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.MAX_VALUE, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, Double.NEGATIVE_INFINITY), ("" + Double.MAX_VALUE) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.MAX_VALUE, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, -1.0), ("" + Double.MAX_VALUE) + -1.0, "doubleOp(\"\" + Double.MAX_VALUE, -1.0)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, -1.0), ("" + Double.MAX_VALUE) + -1.0, "doubleOp(\"\" + Double.MAX_VALUE, -1.0)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, -0.0), ("" + Double.MAX_VALUE) + -0.0, "doubleOp(\"\" + Double.MAX_VALUE, -0.0)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, -0.0), ("" + Double.MAX_VALUE) + -0.0, "doubleOp(\"\" + Double.MAX_VALUE, -0.0)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, 0.0), ("" + Double.MAX_VALUE) + 0.0, "doubleOp(\"\" + Double.MAX_VALUE, 0.0)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, 0.0), ("" + Double.MAX_VALUE) + 0.0, "doubleOp(\"\" + Double.MAX_VALUE, 0.0)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, 1.0), ("" + Double.MAX_VALUE) + 1.0, "doubleOp(\"\" + Double.MAX_VALUE, 1.0)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, 1.0), ("" + Double.MAX_VALUE) + 1.0, "doubleOp(\"\" + Double.MAX_VALUE, 1.0)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, Double.MAX_VALUE), ("" + Double.MAX_VALUE) + Double.MAX_VALUE, "doubleOp(\"\" + Double.MAX_VALUE, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, Double.MAX_VALUE), ("" + Double.MAX_VALUE) + Double.MAX_VALUE, "doubleOp(\"\" + Double.MAX_VALUE, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, Double.POSITIVE_INFINITY), ("" + Double.MAX_VALUE) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.MAX_VALUE, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, Double.POSITIVE_INFINITY), ("" + Double.MAX_VALUE) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.MAX_VALUE, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.MAX_VALUE, Double.NaN), ("" + Double.MAX_VALUE) + Double.NaN, "doubleOp(\"\" + Double.MAX_VALUE, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + Double.MAX_VALUE, Double.NaN), ("" + Double.MAX_VALUE) + Double.NaN, "doubleOp(\"\" + Double.MAX_VALUE, Double.NaN)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), ("" + Double.POSITIVE_INFINITY) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), ("" + Double.POSITIVE_INFINITY) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, -1.0), ("" + Double.POSITIVE_INFINITY) + -1.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, -1.0)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, -1.0), ("" + Double.POSITIVE_INFINITY) + -1.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, -1.0)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, -0.0), ("" + Double.POSITIVE_INFINITY) + -0.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, -0.0)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, -0.0), ("" + Double.POSITIVE_INFINITY) + -0.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, -0.0)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, 0.0), ("" + Double.POSITIVE_INFINITY) + 0.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, 0.0)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, 0.0), ("" + Double.POSITIVE_INFINITY) + 0.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, 0.0)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, 1.0), ("" + Double.POSITIVE_INFINITY) + 1.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, 1.0)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, 1.0), ("" + Double.POSITIVE_INFINITY) + 1.0, "doubleOp(\"\" + Double.POSITIVE_INFINITY, 1.0)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, Double.MAX_VALUE), ("" + Double.POSITIVE_INFINITY) + Double.MAX_VALUE, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, Double.MAX_VALUE), ("" + Double.POSITIVE_INFINITY) + Double.MAX_VALUE, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), ("" + Double.POSITIVE_INFINITY) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), ("" + Double.POSITIVE_INFINITY) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.POSITIVE_INFINITY, Double.NaN), ("" + Double.POSITIVE_INFINITY) + Double.NaN, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + Double.POSITIVE_INFINITY, Double.NaN), ("" + Double.POSITIVE_INFINITY) + Double.NaN, "doubleOp(\"\" + Double.POSITIVE_INFINITY, Double.NaN)");
    Tester.checkEqual(doubleOp("" + Double.NaN, Double.NEGATIVE_INFINITY), ("" + Double.NaN) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.NaN, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, Double.NEGATIVE_INFINITY), ("" + Double.NaN) + Double.NEGATIVE_INFINITY, "doubleOp(\"\" + Double.NaN, Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.NaN, -1.0), ("" + Double.NaN) + -1.0, "doubleOp(\"\" + Double.NaN, -1.0)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, -1.0), ("" + Double.NaN) + -1.0, "doubleOp(\"\" + Double.NaN, -1.0)");
    Tester.checkEqual(doubleOp("" + Double.NaN, -0.0), ("" + Double.NaN) + -0.0, "doubleOp(\"\" + Double.NaN, -0.0)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, -0.0), ("" + Double.NaN) + -0.0, "doubleOp(\"\" + Double.NaN, -0.0)");
    Tester.checkEqual(doubleOp("" + Double.NaN, 0.0), ("" + Double.NaN) + 0.0, "doubleOp(\"\" + Double.NaN, 0.0)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, 0.0), ("" + Double.NaN) + 0.0, "doubleOp(\"\" + Double.NaN, 0.0)");
    Tester.checkEqual(doubleOp("" + Double.NaN, 1.0), ("" + Double.NaN) + 1.0, "doubleOp(\"\" + Double.NaN, 1.0)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, 1.0), ("" + Double.NaN) + 1.0, "doubleOp(\"\" + Double.NaN, 1.0)");
    Tester.checkEqual(doubleOp("" + Double.NaN, Double.MAX_VALUE), ("" + Double.NaN) + Double.MAX_VALUE, "doubleOp(\"\" + Double.NaN, Double.MAX_VALUE)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, Double.MAX_VALUE), ("" + Double.NaN) + Double.MAX_VALUE, "doubleOp(\"\" + Double.NaN, Double.MAX_VALUE)");
    Tester.checkEqual(doubleOp("" + Double.NaN, Double.POSITIVE_INFINITY), ("" + Double.NaN) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.NaN, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, Double.POSITIVE_INFINITY), ("" + Double.NaN) + Double.POSITIVE_INFINITY, "doubleOp(\"\" + Double.NaN, Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleOp("" + Double.NaN, Double.NaN), ("" + Double.NaN) + Double.NaN, "doubleOp(\"\" + Double.NaN, Double.NaN)");
    Tester.checkEqual(doubleAssign("" + Double.NaN, Double.NaN), ("" + Double.NaN) + Double.NaN, "doubleOp(\"\" + Double.NaN, Double.NaN)");
  }
  static void doubleSwitch() {
    switch(-1) {
      case (((("" + Double.NEGATIVE_INFINITY) + Double.NEGATIVE_INFINITY) == "") ? 0 : 0):
      case (((("" + Double.NEGATIVE_INFINITY) + -1.0) == "") ? 1 : 1):
      case (((("" + Double.NEGATIVE_INFINITY) + -0.0) == "") ? 2 : 2):
      case (((("" + Double.NEGATIVE_INFINITY) + 0.0) == "") ? 3 : 3):
      case (((("" + Double.NEGATIVE_INFINITY) + 1.0) == "") ? 4 : 4):
      case (((("" + Double.NEGATIVE_INFINITY) + Double.MAX_VALUE) == "") ? 5 : 5):
      case (((("" + Double.NEGATIVE_INFINITY) + Double.POSITIVE_INFINITY) == "") ? 6 : 6):
      case (((("" + Double.NEGATIVE_INFINITY) + Double.NaN) == "") ? 7 : 7):
      case (((("" + -1.0) + Double.NEGATIVE_INFINITY) == "") ? 8 : 8):
      case (((("" + -1.0) + -1.0) == "") ? 9 : 9):
      case (((("" + -1.0) + -0.0) == "") ? 10 : 10):
      case (((("" + -1.0) + 0.0) == "") ? 11 : 11):
      case (((("" + -1.0) + 1.0) == "") ? 12 : 12):
      case (((("" + -1.0) + Double.MAX_VALUE) == "") ? 13 : 13):
      case (((("" + -1.0) + Double.POSITIVE_INFINITY) == "") ? 14 : 14):
      case (((("" + -1.0) + Double.NaN) == "") ? 15 : 15):
      case (((("" + -0.0) + Double.NEGATIVE_INFINITY) == "") ? 16 : 16):
      case (((("" + -0.0) + -1.0) == "") ? 17 : 17):
      case (((("" + -0.0) + -0.0) == "") ? 18 : 18):
      case (((("" + -0.0) + 0.0) == "") ? 19 : 19):
      case (((("" + -0.0) + 1.0) == "") ? 20 : 20):
      case (((("" + -0.0) + Double.MAX_VALUE) == "") ? 21 : 21):
      case (((("" + -0.0) + Double.POSITIVE_INFINITY) == "") ? 22 : 22):
      case (((("" + -0.0) + Double.NaN) == "") ? 23 : 23):
      case (((("" + 0.0) + Double.NEGATIVE_INFINITY) == "") ? 24 : 24):
      case (((("" + 0.0) + -1.0) == "") ? 25 : 25):
      case (((("" + 0.0) + -0.0) == "") ? 26 : 26):
      case (((("" + 0.0) + 0.0) == "") ? 27 : 27):
      case (((("" + 0.0) + 1.0) == "") ? 28 : 28):
      case (((("" + 0.0) + Double.MAX_VALUE) == "") ? 29 : 29):
      case (((("" + 0.0) + Double.POSITIVE_INFINITY) == "") ? 30 : 30):
      case (((("" + 0.0) + Double.NaN) == "") ? 31 : 31):
      case (((("" + 1.0) + Double.NEGATIVE_INFINITY) == "") ? 32 : 32):
      case (((("" + 1.0) + -1.0) == "") ? 33 : 33):
      case (((("" + 1.0) + -0.0) == "") ? 34 : 34):
      case (((("" + 1.0) + 0.0) == "") ? 35 : 35):
      case (((("" + 1.0) + 1.0) == "") ? 36 : 36):
      case (((("" + 1.0) + Double.MAX_VALUE) == "") ? 37 : 37):
      case (((("" + 1.0) + Double.POSITIVE_INFINITY) == "") ? 38 : 38):
      case (((("" + 1.0) + Double.NaN) == "") ? 39 : 39):
      case (((("" + Double.MAX_VALUE) + Double.NEGATIVE_INFINITY) == "") ? 40 : 40):
      case (((("" + Double.MAX_VALUE) + -1.0) == "") ? 41 : 41):
      case (((("" + Double.MAX_VALUE) + -0.0) == "") ? 42 : 42):
      case (((("" + Double.MAX_VALUE) + 0.0) == "") ? 43 : 43):
      case (((("" + Double.MAX_VALUE) + 1.0) == "") ? 44 : 44):
      case (((("" + Double.MAX_VALUE) + Double.MAX_VALUE) == "") ? 45 : 45):
      case (((("" + Double.MAX_VALUE) + Double.POSITIVE_INFINITY) == "") ? 46 : 46):
      case (((("" + Double.MAX_VALUE) + Double.NaN) == "") ? 47 : 47):
      case (((("" + Double.POSITIVE_INFINITY) + Double.NEGATIVE_INFINITY) == "") ? 48 : 48):
      case (((("" + Double.POSITIVE_INFINITY) + -1.0) == "") ? 49 : 49):
      case (((("" + Double.POSITIVE_INFINITY) + -0.0) == "") ? 50 : 50):
      case (((("" + Double.POSITIVE_INFINITY) + 0.0) == "") ? 51 : 51):
      case (((("" + Double.POSITIVE_INFINITY) + 1.0) == "") ? 52 : 52):
      case (((("" + Double.POSITIVE_INFINITY) + Double.MAX_VALUE) == "") ? 53 : 53):
      case (((("" + Double.POSITIVE_INFINITY) + Double.POSITIVE_INFINITY) == "") ? 54 : 54):
      case (((("" + Double.POSITIVE_INFINITY) + Double.NaN) == "") ? 55 : 55):
      case (((("" + Double.NaN) + Double.NEGATIVE_INFINITY) == "") ? 56 : 56):
      case (((("" + Double.NaN) + -1.0) == "") ? 57 : 57):
      case (((("" + Double.NaN) + -0.0) == "") ? 58 : 58):
      case (((("" + Double.NaN) + 0.0) == "") ? 59 : 59):
      case (((("" + Double.NaN) + 1.0) == "") ? 60 : 60):
      case (((("" + Double.NaN) + Double.MAX_VALUE) == "") ? 61 : 61):
      case (((("" + Double.NaN) + Double.POSITIVE_INFINITY) == "") ? 62 : 62):
      case (((("" + Double.NaN) + Double.NaN) == "") ? 63 : 63):
      default:
    }
  }

  // --------
  // boolean tests
  static String booleanOp(String x, boolean y) { return x + y; }
  static String booleanAssign(String x, boolean y) { x += y; return x; }
  static void booleanTest() {
    Tester.checkEqual(booleanOp("" + true, true), ("" + true) + true, "booleanOp(\"\" + true, true)");
    Tester.checkEqual(booleanAssign("" + true, true), ("" + true) + true, "booleanOp(\"\" + true, true)");
    Tester.checkEqual(booleanOp("" + true, false), ("" + true) + false, "booleanOp(\"\" + true, false)");
    Tester.checkEqual(booleanAssign("" + true, false), ("" + true) + false, "booleanOp(\"\" + true, false)");
    Tester.checkEqual(booleanOp("" + false, true), ("" + false) + true, "booleanOp(\"\" + false, true)");
    Tester.checkEqual(booleanAssign("" + false, true), ("" + false) + true, "booleanOp(\"\" + false, true)");
    Tester.checkEqual(booleanOp("" + false, false), ("" + false) + false, "booleanOp(\"\" + false, false)");
    Tester.checkEqual(booleanAssign("" + false, false), ("" + false) + false, "booleanOp(\"\" + false, false)");
  }
  static void booleanSwitch() {
    switch(-1) {
      case (((("" + true) + true) == "") ? 0 : 0):
      case (((("" + true) + false) == "") ? 1 : 1):
      case (((("" + false) + true) == "") ? 2 : 2):
      case (((("" + false) + false) == "") ? 3 : 3):
      default:
    }
  }

  // --------
  // String tests
  static String StringOp(String x, String y) { return x + y; }
  static String StringAssign(String x, String y) { x += y; return x; }
  static void StringTest() {
    Tester.checkEqual(StringOp("" + "hello", "hello"), ("" + "hello") + "hello", "StringOp(\"\" + \"hello\", \"hello\")");
    Tester.checkEqual(StringAssign("" + "hello", "hello"), ("" + "hello") + "hello", "StringOp(\"\" + \"hello\", \"hello\")");
    Tester.checkEqual(StringOp("" + "hello", ""), ("" + "hello") + "", "StringOp(\"\" + \"hello\", \"\")");
    Tester.checkEqual(StringAssign("" + "hello", ""), ("" + "hello") + "", "StringOp(\"\" + \"hello\", \"\")");
    Tester.checkEqual(StringOp("" + "", "hello"), ("" + "") + "hello", "StringOp(\"\" + \"\", \"hello\")");
    Tester.checkEqual(StringAssign("" + "", "hello"), ("" + "") + "hello", "StringOp(\"\" + \"\", \"hello\")");
    Tester.checkEqual(StringOp("" + "", ""), ("" + "") + "", "StringOp(\"\" + \"\", \"\")");
    Tester.checkEqual(StringAssign("" + "", ""), ("" + "") + "", "StringOp(\"\" + \"\", \"\")");
  }
  static void StringSwitch() {
    switch(-1) {
      case (((("" + "hello") + "hello") == "") ? 0 : 0):
      case (((("" + "hello") + "") == "") ? 1 : 1):
      case (((("" + "") + "hello") == "") ? 2 : 2):
      case (((("" + "") + "") == "") ? 3 : 3):
      default:
    }
  }
}
  /*

(define table
  '((byte ("Byte.MIN_VALUE" "(byte) -1" "(byte) 0" "(byte) 1" "Byte.MAX_VALUE"))
    (short ("Short.MIN_VALUE" "(short) -1" "(short) 0" "(short) 1" "Short.MAX_VALUE") )
    (char ("(char) 0" "(char) 1" "Character.MAX_VALUE") )
    (int ("Integer.MIN_VALUE" "-1" "0" "1" "Integer.MAX_VALUE") )
    (long ("Long.MIN_VALUE" "-1L" "0L" "1L" "Long.MAX_VALUE") )
    (float ("Float.NEGATIVE_INFINITY" "-1.0f" "-0.0f" "0.0f" "Float.MIN_VALUE" "1.0f" "Float.MAX_VALUE" "Float.POSITIVE_INFINITY" "Float.NaN") )
    (double ("Double.NEGATIVE_INFINITY" "-1.0" "-0.0" "0.0"
	     ;; "Double.MIN_VALUE"  NOT CORRECT IN 1.3
	     "1.0" "Double.MAX_VALUE" "Double.POSITIVE_INFINITY" "Double.NaN") )
    (boolean (true false) )
    ;;("Object" ("null" "new int[3]"))
    ("String" ("\"hello\"" "\"\""))))

(define gen
  (lambda ()
    (printf "import org.aspectj.testing.Tester;~n~n")
    (printf "public strictfp class BigString {~n")
    (printf "  public static void main(String[] args) {~n")
    (for-each (lambda (elem)
		(display (format "    ~aTest();~n" (car elem))))
      table)
    (printf "  }~n")
    (display (apply string-append (map tyGen table)))
    (printf "}~n")))

;; elems == ((exprfun callfun op args) ...)

(define tyGen
  (lambda (ls)
    (let ((type (car ls))
	  (vals (cadr ls)))
      (let ((args (cross vals vals)))
	(string-append
	  (format "~n  // --------~n")
	  (format "  // ~a tests~n" type)
	  (genOpFun type)
	  (genAssignFun type)
	  (genTester type args)
	  (genSwitch type args))))))

(define genTester
  (lambda (type argss)
    (string-append (format "  static void ~aTest() {~n" type)
      (apply string-append
	(map (lambda (args)
	       (apply genCall type args))
	  argss))
      (format "  }~n"))))

(define genSwitch
  (lambda (type argss)
    (string-append (format "  static void ~aSwitch() {~n    switch(-1) {~n" type)
      (apply string-append
	(map (lambda (args num) (genCase type num args))
	  argss (iota argss)))
      (format "      default:~n    }~n  }~n"))))

(define genCase
  (lambda (type num args)
    (format "      case (((~a) == \"\") ? ~a : ~a):~n"
      (apply exprfun args)
      num num)))

(define exprfun
  (lambda (a b) (format "(\"\" + ~a) + ~a" a b)))

(define genOpFun
  (lambda (type)
    (format "  static String ~aOp(String x, ~a y) { return x + y; }~n"
      type type)))

(define genAssignFun
  (lambda (type)
    (format "  static String ~aAssign(String x, ~a y) { x += y; return x; }~n"
      type type)))

(define genCall
  (lambda (type val0 val1)
    (string-append
      (format "    Tester.checkEqual(~aOp(\"\" + ~a, ~a), ~a, \"~aOp(\\\"\\\" + ~a, ~a)\");~n"
	type val0 val1 (exprfun val0 val1) type
	(escape-quotes val0) (escape-quotes val1))
      (format "    Tester.checkEqual(~aAssign(\"\" + ~a, ~a), ~a, \"~aOp(\\\"\\\" + ~a, ~a)\");~n"
	type val0 val1 (exprfun val0 val1) type
	(escape-quotes val0) (escape-quotes val1)))))

(define escape-quotes
  (lambda (str)
    (if (string? str)
	(list->string
      (let f ((ls (string->list str)))
	(if (null? ls) '()
	    (if (eq? (car ls) #\")
		(cons #\\ (cons #\" (f (cdr ls))))
		(cons (car ls) (f (cdr ls)))))))
	str)))

(define cross
  (lambda (a b)
    (apply append
      (map (lambda (x)
	   (map (lambda (y)
		  (list x y))
	     b))
	a))))

(define iota
  (lambda (ls)
    (let f ((ls ls) (i 0))
      (if (null? ls) '()
	  (cons i (f (cdr ls) (+ i 1)))))))

*/
