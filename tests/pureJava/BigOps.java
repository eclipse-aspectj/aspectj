// This is a GENERATED file.  Use the scheme program at the end to
// regenerate.  Note that in jdk 1.3, Float.MIN_VALUE does not have a
// proper constant value.  If that ever gets fixed, look for "NOT
// CORRECT IN 1.3" and uncomment that line.

import org.aspectj.testing.Tester;

public strictfp class BigOps {
  public static void main(String[] args) {
    byteTest();
    shortTest();
    charTest();
    intTest();
    longTest();
    floatTest();
    doubleTest();
    booleanTest();
  }

  // --------
  // byte tests
  static byte bytePlus(byte x) { return (byte) + x; }
  static byte byteMinus(byte x) { return (byte) - x; }
  static byte byteBitNot(byte x) { return (byte) ~ x; }
  static byte byteTimes(byte x, byte y) { return (byte) (x * y); }
  static byte byteDiv(byte x, byte y) { return (byte) (x / y); }
  static byte byteRem(byte x, byte y) { return (byte) (x % y); }
  static byte byteAdd(byte x, byte y) { return (byte) (x + y); }
  static byte byteSub(byte x, byte y) { return (byte) (x - y); }
  static byte byteShl(byte x, byte y) { return (byte) (x << y); }
  static byte byteShr(byte x, byte y) { return (byte) (x >> y); }
  static byte byteUshr(byte x, byte y) { return (byte) (x >>> y); }
  static boolean byteLt(byte x, byte y) { return x < y; }
  static boolean byteGt(byte x, byte y) { return x > y; }
  static boolean byteLe(byte x, byte y) { return x <= y; }
  static boolean byteGe(byte x, byte y) { return x >= y; }
  static boolean byteEq(byte x, byte y) { return x == y; }
  static boolean byteNe(byte x, byte y) { return x != y; }
  static byte byteAnd(byte x, byte y) { return (byte) (x & y); }
  static byte byteXor(byte x, byte y) { return (byte) (x ^ y); }
  static byte byteOr(byte x, byte y) { return (byte) (x | y); }
  static void byteTest() {
    Tester.checkEqual(bytePlus(Byte.MIN_VALUE), (byte) + Byte.MIN_VALUE, "(byte) + Byte.MIN_VALUE");
    Tester.checkEqual(bytePlus((byte) -1), (byte) + (byte) -1, "(byte) + (byte) -1");
    Tester.checkEqual(bytePlus((byte) 0), (byte) + (byte) 0, "(byte) + (byte) 0");
    Tester.checkEqual(bytePlus((byte) 1), (byte) + (byte) 1, "(byte) + (byte) 1");
    Tester.checkEqual(bytePlus(Byte.MAX_VALUE), (byte) + Byte.MAX_VALUE, "(byte) + Byte.MAX_VALUE");
    Tester.checkEqual(byteMinus(Byte.MIN_VALUE), (byte) - Byte.MIN_VALUE, "(byte) - Byte.MIN_VALUE");
    Tester.checkEqual(byteMinus((byte) -1), (byte) - (byte) -1, "(byte) - (byte) -1");
    Tester.checkEqual(byteMinus((byte) 0), (byte) - (byte) 0, "(byte) - (byte) 0");
    Tester.checkEqual(byteMinus((byte) 1), (byte) - (byte) 1, "(byte) - (byte) 1");
    Tester.checkEqual(byteMinus(Byte.MAX_VALUE), (byte) - Byte.MAX_VALUE, "(byte) - Byte.MAX_VALUE");
    Tester.checkEqual(byteBitNot(Byte.MIN_VALUE), (byte) ~ Byte.MIN_VALUE, "(byte) ~ Byte.MIN_VALUE");
    Tester.checkEqual(byteBitNot((byte) -1), (byte) ~ (byte) -1, "(byte) ~ (byte) -1");
    Tester.checkEqual(byteBitNot((byte) 0), (byte) ~ (byte) 0, "(byte) ~ (byte) 0");
    Tester.checkEqual(byteBitNot((byte) 1), (byte) ~ (byte) 1, "(byte) ~ (byte) 1");
    Tester.checkEqual(byteBitNot(Byte.MAX_VALUE), (byte) ~ Byte.MAX_VALUE, "(byte) ~ Byte.MAX_VALUE");
    Tester.checkEqual(byteTimes(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE * Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE * Byte.MIN_VALUE)");
    Tester.checkEqual(byteTimes(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE * (byte) -1), "(byte) (Byte.MIN_VALUE * (byte) -1)");
    Tester.checkEqual(byteTimes(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE * (byte) 0), "(byte) (Byte.MIN_VALUE * (byte) 0)");
    Tester.checkEqual(byteTimes(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE * (byte) 1), "(byte) (Byte.MIN_VALUE * (byte) 1)");
    Tester.checkEqual(byteTimes(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE * Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE * Byte.MAX_VALUE)");
    Tester.checkEqual(byteTimes((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 * Byte.MIN_VALUE), "(byte) ((byte) -1 * Byte.MIN_VALUE)");
    Tester.checkEqual(byteTimes((byte) -1, (byte) -1), (byte) ((byte) -1 * (byte) -1), "(byte) ((byte) -1 * (byte) -1)");
    Tester.checkEqual(byteTimes((byte) -1, (byte) 0), (byte) ((byte) -1 * (byte) 0), "(byte) ((byte) -1 * (byte) 0)");
    Tester.checkEqual(byteTimes((byte) -1, (byte) 1), (byte) ((byte) -1 * (byte) 1), "(byte) ((byte) -1 * (byte) 1)");
    Tester.checkEqual(byteTimes((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 * Byte.MAX_VALUE), "(byte) ((byte) -1 * Byte.MAX_VALUE)");
    Tester.checkEqual(byteTimes((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 * Byte.MIN_VALUE), "(byte) ((byte) 0 * Byte.MIN_VALUE)");
    Tester.checkEqual(byteTimes((byte) 0, (byte) -1), (byte) ((byte) 0 * (byte) -1), "(byte) ((byte) 0 * (byte) -1)");
    Tester.checkEqual(byteTimes((byte) 0, (byte) 0), (byte) ((byte) 0 * (byte) 0), "(byte) ((byte) 0 * (byte) 0)");
    Tester.checkEqual(byteTimes((byte) 0, (byte) 1), (byte) ((byte) 0 * (byte) 1), "(byte) ((byte) 0 * (byte) 1)");
    Tester.checkEqual(byteTimes((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 * Byte.MAX_VALUE), "(byte) ((byte) 0 * Byte.MAX_VALUE)");
    Tester.checkEqual(byteTimes((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 * Byte.MIN_VALUE), "(byte) ((byte) 1 * Byte.MIN_VALUE)");
    Tester.checkEqual(byteTimes((byte) 1, (byte) -1), (byte) ((byte) 1 * (byte) -1), "(byte) ((byte) 1 * (byte) -1)");
    Tester.checkEqual(byteTimes((byte) 1, (byte) 0), (byte) ((byte) 1 * (byte) 0), "(byte) ((byte) 1 * (byte) 0)");
    Tester.checkEqual(byteTimes((byte) 1, (byte) 1), (byte) ((byte) 1 * (byte) 1), "(byte) ((byte) 1 * (byte) 1)");
    Tester.checkEqual(byteTimes((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 * Byte.MAX_VALUE), "(byte) ((byte) 1 * Byte.MAX_VALUE)");
    Tester.checkEqual(byteTimes(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE * Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE * Byte.MIN_VALUE)");
    Tester.checkEqual(byteTimes(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE * (byte) -1), "(byte) (Byte.MAX_VALUE * (byte) -1)");
    Tester.checkEqual(byteTimes(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE * (byte) 0), "(byte) (Byte.MAX_VALUE * (byte) 0)");
    Tester.checkEqual(byteTimes(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE * (byte) 1), "(byte) (Byte.MAX_VALUE * (byte) 1)");
    Tester.checkEqual(byteTimes(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE * Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE * Byte.MAX_VALUE)");
    Tester.checkEqual(byteDiv(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE / Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE / Byte.MIN_VALUE)");
    Tester.checkEqual(byteDiv(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE / (byte) -1), "(byte) (Byte.MIN_VALUE / (byte) -1)");
    Tester.checkEqual(byteDiv(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE / (byte) 1), "(byte) (Byte.MIN_VALUE / (byte) 1)");
    Tester.checkEqual(byteDiv(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE / Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE / Byte.MAX_VALUE)");
    Tester.checkEqual(byteDiv((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 / Byte.MIN_VALUE), "(byte) ((byte) -1 / Byte.MIN_VALUE)");
    Tester.checkEqual(byteDiv((byte) -1, (byte) -1), (byte) ((byte) -1 / (byte) -1), "(byte) ((byte) -1 / (byte) -1)");
    Tester.checkEqual(byteDiv((byte) -1, (byte) 1), (byte) ((byte) -1 / (byte) 1), "(byte) ((byte) -1 / (byte) 1)");
    Tester.checkEqual(byteDiv((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 / Byte.MAX_VALUE), "(byte) ((byte) -1 / Byte.MAX_VALUE)");
    Tester.checkEqual(byteDiv((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 / Byte.MIN_VALUE), "(byte) ((byte) 0 / Byte.MIN_VALUE)");
    Tester.checkEqual(byteDiv((byte) 0, (byte) -1), (byte) ((byte) 0 / (byte) -1), "(byte) ((byte) 0 / (byte) -1)");
    Tester.checkEqual(byteDiv((byte) 0, (byte) 1), (byte) ((byte) 0 / (byte) 1), "(byte) ((byte) 0 / (byte) 1)");
    Tester.checkEqual(byteDiv((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 / Byte.MAX_VALUE), "(byte) ((byte) 0 / Byte.MAX_VALUE)");
    Tester.checkEqual(byteDiv((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 / Byte.MIN_VALUE), "(byte) ((byte) 1 / Byte.MIN_VALUE)");
    Tester.checkEqual(byteDiv((byte) 1, (byte) -1), (byte) ((byte) 1 / (byte) -1), "(byte) ((byte) 1 / (byte) -1)");
    Tester.checkEqual(byteDiv((byte) 1, (byte) 1), (byte) ((byte) 1 / (byte) 1), "(byte) ((byte) 1 / (byte) 1)");
    Tester.checkEqual(byteDiv((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 / Byte.MAX_VALUE), "(byte) ((byte) 1 / Byte.MAX_VALUE)");
    Tester.checkEqual(byteDiv(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE / Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE / Byte.MIN_VALUE)");
    Tester.checkEqual(byteDiv(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE / (byte) -1), "(byte) (Byte.MAX_VALUE / (byte) -1)");
    Tester.checkEqual(byteDiv(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE / (byte) 1), "(byte) (Byte.MAX_VALUE / (byte) 1)");
    Tester.checkEqual(byteDiv(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE / Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE / Byte.MAX_VALUE)");
    Tester.checkEqual(byteRem(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE % Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE % Byte.MIN_VALUE)");
    Tester.checkEqual(byteRem(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE % (byte) -1), "(byte) (Byte.MIN_VALUE % (byte) -1)");
    Tester.checkEqual(byteRem(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE % (byte) 1), "(byte) (Byte.MIN_VALUE % (byte) 1)");
    Tester.checkEqual(byteRem(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE % Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE % Byte.MAX_VALUE)");
    Tester.checkEqual(byteRem((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 % Byte.MIN_VALUE), "(byte) ((byte) -1 % Byte.MIN_VALUE)");
    Tester.checkEqual(byteRem((byte) -1, (byte) -1), (byte) ((byte) -1 % (byte) -1), "(byte) ((byte) -1 % (byte) -1)");
    Tester.checkEqual(byteRem((byte) -1, (byte) 1), (byte) ((byte) -1 % (byte) 1), "(byte) ((byte) -1 % (byte) 1)");
    Tester.checkEqual(byteRem((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 % Byte.MAX_VALUE), "(byte) ((byte) -1 % Byte.MAX_VALUE)");
    Tester.checkEqual(byteRem((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 % Byte.MIN_VALUE), "(byte) ((byte) 0 % Byte.MIN_VALUE)");
    Tester.checkEqual(byteRem((byte) 0, (byte) -1), (byte) ((byte) 0 % (byte) -1), "(byte) ((byte) 0 % (byte) -1)");
    Tester.checkEqual(byteRem((byte) 0, (byte) 1), (byte) ((byte) 0 % (byte) 1), "(byte) ((byte) 0 % (byte) 1)");
    Tester.checkEqual(byteRem((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 % Byte.MAX_VALUE), "(byte) ((byte) 0 % Byte.MAX_VALUE)");
    Tester.checkEqual(byteRem((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 % Byte.MIN_VALUE), "(byte) ((byte) 1 % Byte.MIN_VALUE)");
    Tester.checkEqual(byteRem((byte) 1, (byte) -1), (byte) ((byte) 1 % (byte) -1), "(byte) ((byte) 1 % (byte) -1)");
    Tester.checkEqual(byteRem((byte) 1, (byte) 1), (byte) ((byte) 1 % (byte) 1), "(byte) ((byte) 1 % (byte) 1)");
    Tester.checkEqual(byteRem((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 % Byte.MAX_VALUE), "(byte) ((byte) 1 % Byte.MAX_VALUE)");
    Tester.checkEqual(byteRem(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE % Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE % Byte.MIN_VALUE)");
    Tester.checkEqual(byteRem(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE % (byte) -1), "(byte) (Byte.MAX_VALUE % (byte) -1)");
    Tester.checkEqual(byteRem(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE % (byte) 1), "(byte) (Byte.MAX_VALUE % (byte) 1)");
    Tester.checkEqual(byteRem(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE % Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE % Byte.MAX_VALUE)");
    Tester.checkEqual(byteAdd(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE + Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE + Byte.MIN_VALUE)");
    Tester.checkEqual(byteAdd(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE + (byte) -1), "(byte) (Byte.MIN_VALUE + (byte) -1)");
    Tester.checkEqual(byteAdd(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE + (byte) 0), "(byte) (Byte.MIN_VALUE + (byte) 0)");
    Tester.checkEqual(byteAdd(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE + (byte) 1), "(byte) (Byte.MIN_VALUE + (byte) 1)");
    Tester.checkEqual(byteAdd(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE + Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE + Byte.MAX_VALUE)");
    Tester.checkEqual(byteAdd((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 + Byte.MIN_VALUE), "(byte) ((byte) -1 + Byte.MIN_VALUE)");
    Tester.checkEqual(byteAdd((byte) -1, (byte) -1), (byte) ((byte) -1 + (byte) -1), "(byte) ((byte) -1 + (byte) -1)");
    Tester.checkEqual(byteAdd((byte) -1, (byte) 0), (byte) ((byte) -1 + (byte) 0), "(byte) ((byte) -1 + (byte) 0)");
    Tester.checkEqual(byteAdd((byte) -1, (byte) 1), (byte) ((byte) -1 + (byte) 1), "(byte) ((byte) -1 + (byte) 1)");
    Tester.checkEqual(byteAdd((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 + Byte.MAX_VALUE), "(byte) ((byte) -1 + Byte.MAX_VALUE)");
    Tester.checkEqual(byteAdd((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 + Byte.MIN_VALUE), "(byte) ((byte) 0 + Byte.MIN_VALUE)");
    Tester.checkEqual(byteAdd((byte) 0, (byte) -1), (byte) ((byte) 0 + (byte) -1), "(byte) ((byte) 0 + (byte) -1)");
    Tester.checkEqual(byteAdd((byte) 0, (byte) 0), (byte) ((byte) 0 + (byte) 0), "(byte) ((byte) 0 + (byte) 0)");
    Tester.checkEqual(byteAdd((byte) 0, (byte) 1), (byte) ((byte) 0 + (byte) 1), "(byte) ((byte) 0 + (byte) 1)");
    Tester.checkEqual(byteAdd((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 + Byte.MAX_VALUE), "(byte) ((byte) 0 + Byte.MAX_VALUE)");
    Tester.checkEqual(byteAdd((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 + Byte.MIN_VALUE), "(byte) ((byte) 1 + Byte.MIN_VALUE)");
    Tester.checkEqual(byteAdd((byte) 1, (byte) -1), (byte) ((byte) 1 + (byte) -1), "(byte) ((byte) 1 + (byte) -1)");
    Tester.checkEqual(byteAdd((byte) 1, (byte) 0), (byte) ((byte) 1 + (byte) 0), "(byte) ((byte) 1 + (byte) 0)");
    Tester.checkEqual(byteAdd((byte) 1, (byte) 1), (byte) ((byte) 1 + (byte) 1), "(byte) ((byte) 1 + (byte) 1)");
    Tester.checkEqual(byteAdd((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 + Byte.MAX_VALUE), "(byte) ((byte) 1 + Byte.MAX_VALUE)");
    Tester.checkEqual(byteAdd(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE + Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE + Byte.MIN_VALUE)");
    Tester.checkEqual(byteAdd(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE + (byte) -1), "(byte) (Byte.MAX_VALUE + (byte) -1)");
    Tester.checkEqual(byteAdd(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE + (byte) 0), "(byte) (Byte.MAX_VALUE + (byte) 0)");
    Tester.checkEqual(byteAdd(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE + (byte) 1), "(byte) (Byte.MAX_VALUE + (byte) 1)");
    Tester.checkEqual(byteAdd(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE + Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE + Byte.MAX_VALUE)");
    Tester.checkEqual(byteSub(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE - Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE - Byte.MIN_VALUE)");
    Tester.checkEqual(byteSub(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE - (byte) -1), "(byte) (Byte.MIN_VALUE - (byte) -1)");
    Tester.checkEqual(byteSub(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE - (byte) 0), "(byte) (Byte.MIN_VALUE - (byte) 0)");
    Tester.checkEqual(byteSub(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE - (byte) 1), "(byte) (Byte.MIN_VALUE - (byte) 1)");
    Tester.checkEqual(byteSub(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE - Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE - Byte.MAX_VALUE)");
    Tester.checkEqual(byteSub((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 - Byte.MIN_VALUE), "(byte) ((byte) -1 - Byte.MIN_VALUE)");
    Tester.checkEqual(byteSub((byte) -1, (byte) -1), (byte) ((byte) -1 - (byte) -1), "(byte) ((byte) -1 - (byte) -1)");
    Tester.checkEqual(byteSub((byte) -1, (byte) 0), (byte) ((byte) -1 - (byte) 0), "(byte) ((byte) -1 - (byte) 0)");
    Tester.checkEqual(byteSub((byte) -1, (byte) 1), (byte) ((byte) -1 - (byte) 1), "(byte) ((byte) -1 - (byte) 1)");
    Tester.checkEqual(byteSub((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 - Byte.MAX_VALUE), "(byte) ((byte) -1 - Byte.MAX_VALUE)");
    Tester.checkEqual(byteSub((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 - Byte.MIN_VALUE), "(byte) ((byte) 0 - Byte.MIN_VALUE)");
    Tester.checkEqual(byteSub((byte) 0, (byte) -1), (byte) ((byte) 0 - (byte) -1), "(byte) ((byte) 0 - (byte) -1)");
    Tester.checkEqual(byteSub((byte) 0, (byte) 0), (byte) ((byte) 0 - (byte) 0), "(byte) ((byte) 0 - (byte) 0)");
    Tester.checkEqual(byteSub((byte) 0, (byte) 1), (byte) ((byte) 0 - (byte) 1), "(byte) ((byte) 0 - (byte) 1)");
    Tester.checkEqual(byteSub((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 - Byte.MAX_VALUE), "(byte) ((byte) 0 - Byte.MAX_VALUE)");
    Tester.checkEqual(byteSub((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 - Byte.MIN_VALUE), "(byte) ((byte) 1 - Byte.MIN_VALUE)");
    Tester.checkEqual(byteSub((byte) 1, (byte) -1), (byte) ((byte) 1 - (byte) -1), "(byte) ((byte) 1 - (byte) -1)");
    Tester.checkEqual(byteSub((byte) 1, (byte) 0), (byte) ((byte) 1 - (byte) 0), "(byte) ((byte) 1 - (byte) 0)");
    Tester.checkEqual(byteSub((byte) 1, (byte) 1), (byte) ((byte) 1 - (byte) 1), "(byte) ((byte) 1 - (byte) 1)");
    Tester.checkEqual(byteSub((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 - Byte.MAX_VALUE), "(byte) ((byte) 1 - Byte.MAX_VALUE)");
    Tester.checkEqual(byteSub(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE - Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE - Byte.MIN_VALUE)");
    Tester.checkEqual(byteSub(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE - (byte) -1), "(byte) (Byte.MAX_VALUE - (byte) -1)");
    Tester.checkEqual(byteSub(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE - (byte) 0), "(byte) (Byte.MAX_VALUE - (byte) 0)");
    Tester.checkEqual(byteSub(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE - (byte) 1), "(byte) (Byte.MAX_VALUE - (byte) 1)");
    Tester.checkEqual(byteSub(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE - Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE - Byte.MAX_VALUE)");
    Tester.checkEqual(byteShl(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE << Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE << Byte.MIN_VALUE)");
    Tester.checkEqual(byteShl(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE << (byte) -1), "(byte) (Byte.MIN_VALUE << (byte) -1)");
    Tester.checkEqual(byteShl(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE << (byte) 0), "(byte) (Byte.MIN_VALUE << (byte) 0)");
    Tester.checkEqual(byteShl(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE << (byte) 1), "(byte) (Byte.MIN_VALUE << (byte) 1)");
    Tester.checkEqual(byteShl(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE << Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE << Byte.MAX_VALUE)");
    Tester.checkEqual(byteShl((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 << Byte.MIN_VALUE), "(byte) ((byte) -1 << Byte.MIN_VALUE)");
    Tester.checkEqual(byteShl((byte) -1, (byte) -1), (byte) ((byte) -1 << (byte) -1), "(byte) ((byte) -1 << (byte) -1)");
    Tester.checkEqual(byteShl((byte) -1, (byte) 0), (byte) ((byte) -1 << (byte) 0), "(byte) ((byte) -1 << (byte) 0)");
    Tester.checkEqual(byteShl((byte) -1, (byte) 1), (byte) ((byte) -1 << (byte) 1), "(byte) ((byte) -1 << (byte) 1)");
    Tester.checkEqual(byteShl((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 << Byte.MAX_VALUE), "(byte) ((byte) -1 << Byte.MAX_VALUE)");
    Tester.checkEqual(byteShl((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 << Byte.MIN_VALUE), "(byte) ((byte) 0 << Byte.MIN_VALUE)");
    Tester.checkEqual(byteShl((byte) 0, (byte) -1), (byte) ((byte) 0 << (byte) -1), "(byte) ((byte) 0 << (byte) -1)");
    Tester.checkEqual(byteShl((byte) 0, (byte) 0), (byte) ((byte) 0 << (byte) 0), "(byte) ((byte) 0 << (byte) 0)");
    Tester.checkEqual(byteShl((byte) 0, (byte) 1), (byte) ((byte) 0 << (byte) 1), "(byte) ((byte) 0 << (byte) 1)");
    Tester.checkEqual(byteShl((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 << Byte.MAX_VALUE), "(byte) ((byte) 0 << Byte.MAX_VALUE)");
    Tester.checkEqual(byteShl((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 << Byte.MIN_VALUE), "(byte) ((byte) 1 << Byte.MIN_VALUE)");
    Tester.checkEqual(byteShl((byte) 1, (byte) -1), (byte) ((byte) 1 << (byte) -1), "(byte) ((byte) 1 << (byte) -1)");
    Tester.checkEqual(byteShl((byte) 1, (byte) 0), (byte) ((byte) 1 << (byte) 0), "(byte) ((byte) 1 << (byte) 0)");
    Tester.checkEqual(byteShl((byte) 1, (byte) 1), (byte) ((byte) 1 << (byte) 1), "(byte) ((byte) 1 << (byte) 1)");
    Tester.checkEqual(byteShl((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 << Byte.MAX_VALUE), "(byte) ((byte) 1 << Byte.MAX_VALUE)");
    Tester.checkEqual(byteShl(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE << Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE << Byte.MIN_VALUE)");
    Tester.checkEqual(byteShl(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE << (byte) -1), "(byte) (Byte.MAX_VALUE << (byte) -1)");
    Tester.checkEqual(byteShl(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE << (byte) 0), "(byte) (Byte.MAX_VALUE << (byte) 0)");
    Tester.checkEqual(byteShl(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE << (byte) 1), "(byte) (Byte.MAX_VALUE << (byte) 1)");
    Tester.checkEqual(byteShl(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE << Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE << Byte.MAX_VALUE)");
    Tester.checkEqual(byteShr(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE >> Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE >> Byte.MIN_VALUE)");
    Tester.checkEqual(byteShr(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE >> (byte) -1), "(byte) (Byte.MIN_VALUE >> (byte) -1)");
    Tester.checkEqual(byteShr(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE >> (byte) 0), "(byte) (Byte.MIN_VALUE >> (byte) 0)");
    Tester.checkEqual(byteShr(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE >> (byte) 1), "(byte) (Byte.MIN_VALUE >> (byte) 1)");
    Tester.checkEqual(byteShr(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE >> Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE >> Byte.MAX_VALUE)");
    Tester.checkEqual(byteShr((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 >> Byte.MIN_VALUE), "(byte) ((byte) -1 >> Byte.MIN_VALUE)");
    Tester.checkEqual(byteShr((byte) -1, (byte) -1), (byte) ((byte) -1 >> (byte) -1), "(byte) ((byte) -1 >> (byte) -1)");
    Tester.checkEqual(byteShr((byte) -1, (byte) 0), (byte) ((byte) -1 >> (byte) 0), "(byte) ((byte) -1 >> (byte) 0)");
    Tester.checkEqual(byteShr((byte) -1, (byte) 1), (byte) ((byte) -1 >> (byte) 1), "(byte) ((byte) -1 >> (byte) 1)");
    Tester.checkEqual(byteShr((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 >> Byte.MAX_VALUE), "(byte) ((byte) -1 >> Byte.MAX_VALUE)");
    Tester.checkEqual(byteShr((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 >> Byte.MIN_VALUE), "(byte) ((byte) 0 >> Byte.MIN_VALUE)");
    Tester.checkEqual(byteShr((byte) 0, (byte) -1), (byte) ((byte) 0 >> (byte) -1), "(byte) ((byte) 0 >> (byte) -1)");
    Tester.checkEqual(byteShr((byte) 0, (byte) 0), (byte) ((byte) 0 >> (byte) 0), "(byte) ((byte) 0 >> (byte) 0)");
    Tester.checkEqual(byteShr((byte) 0, (byte) 1), (byte) ((byte) 0 >> (byte) 1), "(byte) ((byte) 0 >> (byte) 1)");
    Tester.checkEqual(byteShr((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 >> Byte.MAX_VALUE), "(byte) ((byte) 0 >> Byte.MAX_VALUE)");
    Tester.checkEqual(byteShr((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 >> Byte.MIN_VALUE), "(byte) ((byte) 1 >> Byte.MIN_VALUE)");
    Tester.checkEqual(byteShr((byte) 1, (byte) -1), (byte) ((byte) 1 >> (byte) -1), "(byte) ((byte) 1 >> (byte) -1)");
    Tester.checkEqual(byteShr((byte) 1, (byte) 0), (byte) ((byte) 1 >> (byte) 0), "(byte) ((byte) 1 >> (byte) 0)");
    Tester.checkEqual(byteShr((byte) 1, (byte) 1), (byte) ((byte) 1 >> (byte) 1), "(byte) ((byte) 1 >> (byte) 1)");
    Tester.checkEqual(byteShr((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 >> Byte.MAX_VALUE), "(byte) ((byte) 1 >> Byte.MAX_VALUE)");
    Tester.checkEqual(byteShr(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE >> Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE >> Byte.MIN_VALUE)");
    Tester.checkEqual(byteShr(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE >> (byte) -1), "(byte) (Byte.MAX_VALUE >> (byte) -1)");
    Tester.checkEqual(byteShr(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE >> (byte) 0), "(byte) (Byte.MAX_VALUE >> (byte) 0)");
    Tester.checkEqual(byteShr(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE >> (byte) 1), "(byte) (Byte.MAX_VALUE >> (byte) 1)");
    Tester.checkEqual(byteShr(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE >> Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE >> Byte.MAX_VALUE)");
    Tester.checkEqual(byteUshr(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE >>> Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE >>> Byte.MIN_VALUE)");
    Tester.checkEqual(byteUshr(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE >>> (byte) -1), "(byte) (Byte.MIN_VALUE >>> (byte) -1)");
    Tester.checkEqual(byteUshr(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE >>> (byte) 0), "(byte) (Byte.MIN_VALUE >>> (byte) 0)");
    Tester.checkEqual(byteUshr(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE >>> (byte) 1), "(byte) (Byte.MIN_VALUE >>> (byte) 1)");
    Tester.checkEqual(byteUshr(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE >>> Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE >>> Byte.MAX_VALUE)");
    Tester.checkEqual(byteUshr((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 >>> Byte.MIN_VALUE), "(byte) ((byte) -1 >>> Byte.MIN_VALUE)");
    Tester.checkEqual(byteUshr((byte) -1, (byte) -1), (byte) ((byte) -1 >>> (byte) -1), "(byte) ((byte) -1 >>> (byte) -1)");
    Tester.checkEqual(byteUshr((byte) -1, (byte) 0), (byte) ((byte) -1 >>> (byte) 0), "(byte) ((byte) -1 >>> (byte) 0)");
    Tester.checkEqual(byteUshr((byte) -1, (byte) 1), (byte) ((byte) -1 >>> (byte) 1), "(byte) ((byte) -1 >>> (byte) 1)");
    Tester.checkEqual(byteUshr((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 >>> Byte.MAX_VALUE), "(byte) ((byte) -1 >>> Byte.MAX_VALUE)");
    Tester.checkEqual(byteUshr((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 >>> Byte.MIN_VALUE), "(byte) ((byte) 0 >>> Byte.MIN_VALUE)");
    Tester.checkEqual(byteUshr((byte) 0, (byte) -1), (byte) ((byte) 0 >>> (byte) -1), "(byte) ((byte) 0 >>> (byte) -1)");
    Tester.checkEqual(byteUshr((byte) 0, (byte) 0), (byte) ((byte) 0 >>> (byte) 0), "(byte) ((byte) 0 >>> (byte) 0)");
    Tester.checkEqual(byteUshr((byte) 0, (byte) 1), (byte) ((byte) 0 >>> (byte) 1), "(byte) ((byte) 0 >>> (byte) 1)");
    Tester.checkEqual(byteUshr((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 >>> Byte.MAX_VALUE), "(byte) ((byte) 0 >>> Byte.MAX_VALUE)");
    Tester.checkEqual(byteUshr((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 >>> Byte.MIN_VALUE), "(byte) ((byte) 1 >>> Byte.MIN_VALUE)");
    Tester.checkEqual(byteUshr((byte) 1, (byte) -1), (byte) ((byte) 1 >>> (byte) -1), "(byte) ((byte) 1 >>> (byte) -1)");
    Tester.checkEqual(byteUshr((byte) 1, (byte) 0), (byte) ((byte) 1 >>> (byte) 0), "(byte) ((byte) 1 >>> (byte) 0)");
    Tester.checkEqual(byteUshr((byte) 1, (byte) 1), (byte) ((byte) 1 >>> (byte) 1), "(byte) ((byte) 1 >>> (byte) 1)");
    Tester.checkEqual(byteUshr((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 >>> Byte.MAX_VALUE), "(byte) ((byte) 1 >>> Byte.MAX_VALUE)");
    Tester.checkEqual(byteUshr(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE >>> Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE >>> Byte.MIN_VALUE)");
    Tester.checkEqual(byteUshr(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE >>> (byte) -1), "(byte) (Byte.MAX_VALUE >>> (byte) -1)");
    Tester.checkEqual(byteUshr(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE >>> (byte) 0), "(byte) (Byte.MAX_VALUE >>> (byte) 0)");
    Tester.checkEqual(byteUshr(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE >>> (byte) 1), "(byte) (Byte.MAX_VALUE >>> (byte) 1)");
    Tester.checkEqual(byteUshr(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE >>> Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE >>> Byte.MAX_VALUE)");
    Tester.checkEqual(byteLt(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE < Byte.MIN_VALUE, "Byte.MIN_VALUE < Byte.MIN_VALUE");
    Tester.checkEqual(byteLt(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE < (byte) -1, "Byte.MIN_VALUE < (byte) -1");
    Tester.checkEqual(byteLt(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE < (byte) 0, "Byte.MIN_VALUE < (byte) 0");
    Tester.checkEqual(byteLt(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE < (byte) 1, "Byte.MIN_VALUE < (byte) 1");
    Tester.checkEqual(byteLt(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE < Byte.MAX_VALUE, "Byte.MIN_VALUE < Byte.MAX_VALUE");
    Tester.checkEqual(byteLt((byte) -1, Byte.MIN_VALUE), (byte) -1 < Byte.MIN_VALUE, "(byte) -1 < Byte.MIN_VALUE");
    Tester.checkEqual(byteLt((byte) -1, (byte) -1), (byte) -1 < (byte) -1, "(byte) -1 < (byte) -1");
    Tester.checkEqual(byteLt((byte) -1, (byte) 0), (byte) -1 < (byte) 0, "(byte) -1 < (byte) 0");
    Tester.checkEqual(byteLt((byte) -1, (byte) 1), (byte) -1 < (byte) 1, "(byte) -1 < (byte) 1");
    Tester.checkEqual(byteLt((byte) -1, Byte.MAX_VALUE), (byte) -1 < Byte.MAX_VALUE, "(byte) -1 < Byte.MAX_VALUE");
    Tester.checkEqual(byteLt((byte) 0, Byte.MIN_VALUE), (byte) 0 < Byte.MIN_VALUE, "(byte) 0 < Byte.MIN_VALUE");
    Tester.checkEqual(byteLt((byte) 0, (byte) -1), (byte) 0 < (byte) -1, "(byte) 0 < (byte) -1");
    Tester.checkEqual(byteLt((byte) 0, (byte) 0), (byte) 0 < (byte) 0, "(byte) 0 < (byte) 0");
    Tester.checkEqual(byteLt((byte) 0, (byte) 1), (byte) 0 < (byte) 1, "(byte) 0 < (byte) 1");
    Tester.checkEqual(byteLt((byte) 0, Byte.MAX_VALUE), (byte) 0 < Byte.MAX_VALUE, "(byte) 0 < Byte.MAX_VALUE");
    Tester.checkEqual(byteLt((byte) 1, Byte.MIN_VALUE), (byte) 1 < Byte.MIN_VALUE, "(byte) 1 < Byte.MIN_VALUE");
    Tester.checkEqual(byteLt((byte) 1, (byte) -1), (byte) 1 < (byte) -1, "(byte) 1 < (byte) -1");
    Tester.checkEqual(byteLt((byte) 1, (byte) 0), (byte) 1 < (byte) 0, "(byte) 1 < (byte) 0");
    Tester.checkEqual(byteLt((byte) 1, (byte) 1), (byte) 1 < (byte) 1, "(byte) 1 < (byte) 1");
    Tester.checkEqual(byteLt((byte) 1, Byte.MAX_VALUE), (byte) 1 < Byte.MAX_VALUE, "(byte) 1 < Byte.MAX_VALUE");
    Tester.checkEqual(byteLt(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE < Byte.MIN_VALUE, "Byte.MAX_VALUE < Byte.MIN_VALUE");
    Tester.checkEqual(byteLt(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE < (byte) -1, "Byte.MAX_VALUE < (byte) -1");
    Tester.checkEqual(byteLt(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE < (byte) 0, "Byte.MAX_VALUE < (byte) 0");
    Tester.checkEqual(byteLt(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE < (byte) 1, "Byte.MAX_VALUE < (byte) 1");
    Tester.checkEqual(byteLt(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE < Byte.MAX_VALUE, "Byte.MAX_VALUE < Byte.MAX_VALUE");
    Tester.checkEqual(byteGt(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE > Byte.MIN_VALUE, "Byte.MIN_VALUE > Byte.MIN_VALUE");
    Tester.checkEqual(byteGt(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE > (byte) -1, "Byte.MIN_VALUE > (byte) -1");
    Tester.checkEqual(byteGt(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE > (byte) 0, "Byte.MIN_VALUE > (byte) 0");
    Tester.checkEqual(byteGt(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE > (byte) 1, "Byte.MIN_VALUE > (byte) 1");
    Tester.checkEqual(byteGt(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE > Byte.MAX_VALUE, "Byte.MIN_VALUE > Byte.MAX_VALUE");
    Tester.checkEqual(byteGt((byte) -1, Byte.MIN_VALUE), (byte) -1 > Byte.MIN_VALUE, "(byte) -1 > Byte.MIN_VALUE");
    Tester.checkEqual(byteGt((byte) -1, (byte) -1), (byte) -1 > (byte) -1, "(byte) -1 > (byte) -1");
    Tester.checkEqual(byteGt((byte) -1, (byte) 0), (byte) -1 > (byte) 0, "(byte) -1 > (byte) 0");
    Tester.checkEqual(byteGt((byte) -1, (byte) 1), (byte) -1 > (byte) 1, "(byte) -1 > (byte) 1");
    Tester.checkEqual(byteGt((byte) -1, Byte.MAX_VALUE), (byte) -1 > Byte.MAX_VALUE, "(byte) -1 > Byte.MAX_VALUE");
    Tester.checkEqual(byteGt((byte) 0, Byte.MIN_VALUE), (byte) 0 > Byte.MIN_VALUE, "(byte) 0 > Byte.MIN_VALUE");
    Tester.checkEqual(byteGt((byte) 0, (byte) -1), (byte) 0 > (byte) -1, "(byte) 0 > (byte) -1");
    Tester.checkEqual(byteGt((byte) 0, (byte) 0), (byte) 0 > (byte) 0, "(byte) 0 > (byte) 0");
    Tester.checkEqual(byteGt((byte) 0, (byte) 1), (byte) 0 > (byte) 1, "(byte) 0 > (byte) 1");
    Tester.checkEqual(byteGt((byte) 0, Byte.MAX_VALUE), (byte) 0 > Byte.MAX_VALUE, "(byte) 0 > Byte.MAX_VALUE");
    Tester.checkEqual(byteGt((byte) 1, Byte.MIN_VALUE), (byte) 1 > Byte.MIN_VALUE, "(byte) 1 > Byte.MIN_VALUE");
    Tester.checkEqual(byteGt((byte) 1, (byte) -1), (byte) 1 > (byte) -1, "(byte) 1 > (byte) -1");
    Tester.checkEqual(byteGt((byte) 1, (byte) 0), (byte) 1 > (byte) 0, "(byte) 1 > (byte) 0");
    Tester.checkEqual(byteGt((byte) 1, (byte) 1), (byte) 1 > (byte) 1, "(byte) 1 > (byte) 1");
    Tester.checkEqual(byteGt((byte) 1, Byte.MAX_VALUE), (byte) 1 > Byte.MAX_VALUE, "(byte) 1 > Byte.MAX_VALUE");
    Tester.checkEqual(byteGt(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE > Byte.MIN_VALUE, "Byte.MAX_VALUE > Byte.MIN_VALUE");
    Tester.checkEqual(byteGt(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE > (byte) -1, "Byte.MAX_VALUE > (byte) -1");
    Tester.checkEqual(byteGt(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE > (byte) 0, "Byte.MAX_VALUE > (byte) 0");
    Tester.checkEqual(byteGt(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE > (byte) 1, "Byte.MAX_VALUE > (byte) 1");
    Tester.checkEqual(byteGt(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE > Byte.MAX_VALUE, "Byte.MAX_VALUE > Byte.MAX_VALUE");
    Tester.checkEqual(byteLe(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE <= Byte.MIN_VALUE, "Byte.MIN_VALUE <= Byte.MIN_VALUE");
    Tester.checkEqual(byteLe(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE <= (byte) -1, "Byte.MIN_VALUE <= (byte) -1");
    Tester.checkEqual(byteLe(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE <= (byte) 0, "Byte.MIN_VALUE <= (byte) 0");
    Tester.checkEqual(byteLe(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE <= (byte) 1, "Byte.MIN_VALUE <= (byte) 1");
    Tester.checkEqual(byteLe(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE <= Byte.MAX_VALUE, "Byte.MIN_VALUE <= Byte.MAX_VALUE");
    Tester.checkEqual(byteLe((byte) -1, Byte.MIN_VALUE), (byte) -1 <= Byte.MIN_VALUE, "(byte) -1 <= Byte.MIN_VALUE");
    Tester.checkEqual(byteLe((byte) -1, (byte) -1), (byte) -1 <= (byte) -1, "(byte) -1 <= (byte) -1");
    Tester.checkEqual(byteLe((byte) -1, (byte) 0), (byte) -1 <= (byte) 0, "(byte) -1 <= (byte) 0");
    Tester.checkEqual(byteLe((byte) -1, (byte) 1), (byte) -1 <= (byte) 1, "(byte) -1 <= (byte) 1");
    Tester.checkEqual(byteLe((byte) -1, Byte.MAX_VALUE), (byte) -1 <= Byte.MAX_VALUE, "(byte) -1 <= Byte.MAX_VALUE");
    Tester.checkEqual(byteLe((byte) 0, Byte.MIN_VALUE), (byte) 0 <= Byte.MIN_VALUE, "(byte) 0 <= Byte.MIN_VALUE");
    Tester.checkEqual(byteLe((byte) 0, (byte) -1), (byte) 0 <= (byte) -1, "(byte) 0 <= (byte) -1");
    Tester.checkEqual(byteLe((byte) 0, (byte) 0), (byte) 0 <= (byte) 0, "(byte) 0 <= (byte) 0");
    Tester.checkEqual(byteLe((byte) 0, (byte) 1), (byte) 0 <= (byte) 1, "(byte) 0 <= (byte) 1");
    Tester.checkEqual(byteLe((byte) 0, Byte.MAX_VALUE), (byte) 0 <= Byte.MAX_VALUE, "(byte) 0 <= Byte.MAX_VALUE");
    Tester.checkEqual(byteLe((byte) 1, Byte.MIN_VALUE), (byte) 1 <= Byte.MIN_VALUE, "(byte) 1 <= Byte.MIN_VALUE");
    Tester.checkEqual(byteLe((byte) 1, (byte) -1), (byte) 1 <= (byte) -1, "(byte) 1 <= (byte) -1");
    Tester.checkEqual(byteLe((byte) 1, (byte) 0), (byte) 1 <= (byte) 0, "(byte) 1 <= (byte) 0");
    Tester.checkEqual(byteLe((byte) 1, (byte) 1), (byte) 1 <= (byte) 1, "(byte) 1 <= (byte) 1");
    Tester.checkEqual(byteLe((byte) 1, Byte.MAX_VALUE), (byte) 1 <= Byte.MAX_VALUE, "(byte) 1 <= Byte.MAX_VALUE");
    Tester.checkEqual(byteLe(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE <= Byte.MIN_VALUE, "Byte.MAX_VALUE <= Byte.MIN_VALUE");
    Tester.checkEqual(byteLe(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE <= (byte) -1, "Byte.MAX_VALUE <= (byte) -1");
    Tester.checkEqual(byteLe(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE <= (byte) 0, "Byte.MAX_VALUE <= (byte) 0");
    Tester.checkEqual(byteLe(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE <= (byte) 1, "Byte.MAX_VALUE <= (byte) 1");
    Tester.checkEqual(byteLe(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE <= Byte.MAX_VALUE, "Byte.MAX_VALUE <= Byte.MAX_VALUE");
    Tester.checkEqual(byteGe(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE >= Byte.MIN_VALUE, "Byte.MIN_VALUE >= Byte.MIN_VALUE");
    Tester.checkEqual(byteGe(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE >= (byte) -1, "Byte.MIN_VALUE >= (byte) -1");
    Tester.checkEqual(byteGe(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE >= (byte) 0, "Byte.MIN_VALUE >= (byte) 0");
    Tester.checkEqual(byteGe(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE >= (byte) 1, "Byte.MIN_VALUE >= (byte) 1");
    Tester.checkEqual(byteGe(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE >= Byte.MAX_VALUE, "Byte.MIN_VALUE >= Byte.MAX_VALUE");
    Tester.checkEqual(byteGe((byte) -1, Byte.MIN_VALUE), (byte) -1 >= Byte.MIN_VALUE, "(byte) -1 >= Byte.MIN_VALUE");
    Tester.checkEqual(byteGe((byte) -1, (byte) -1), (byte) -1 >= (byte) -1, "(byte) -1 >= (byte) -1");
    Tester.checkEqual(byteGe((byte) -1, (byte) 0), (byte) -1 >= (byte) 0, "(byte) -1 >= (byte) 0");
    Tester.checkEqual(byteGe((byte) -1, (byte) 1), (byte) -1 >= (byte) 1, "(byte) -1 >= (byte) 1");
    Tester.checkEqual(byteGe((byte) -1, Byte.MAX_VALUE), (byte) -1 >= Byte.MAX_VALUE, "(byte) -1 >= Byte.MAX_VALUE");
    Tester.checkEqual(byteGe((byte) 0, Byte.MIN_VALUE), (byte) 0 >= Byte.MIN_VALUE, "(byte) 0 >= Byte.MIN_VALUE");
    Tester.checkEqual(byteGe((byte) 0, (byte) -1), (byte) 0 >= (byte) -1, "(byte) 0 >= (byte) -1");
    Tester.checkEqual(byteGe((byte) 0, (byte) 0), (byte) 0 >= (byte) 0, "(byte) 0 >= (byte) 0");
    Tester.checkEqual(byteGe((byte) 0, (byte) 1), (byte) 0 >= (byte) 1, "(byte) 0 >= (byte) 1");
    Tester.checkEqual(byteGe((byte) 0, Byte.MAX_VALUE), (byte) 0 >= Byte.MAX_VALUE, "(byte) 0 >= Byte.MAX_VALUE");
    Tester.checkEqual(byteGe((byte) 1, Byte.MIN_VALUE), (byte) 1 >= Byte.MIN_VALUE, "(byte) 1 >= Byte.MIN_VALUE");
    Tester.checkEqual(byteGe((byte) 1, (byte) -1), (byte) 1 >= (byte) -1, "(byte) 1 >= (byte) -1");
    Tester.checkEqual(byteGe((byte) 1, (byte) 0), (byte) 1 >= (byte) 0, "(byte) 1 >= (byte) 0");
    Tester.checkEqual(byteGe((byte) 1, (byte) 1), (byte) 1 >= (byte) 1, "(byte) 1 >= (byte) 1");
    Tester.checkEqual(byteGe((byte) 1, Byte.MAX_VALUE), (byte) 1 >= Byte.MAX_VALUE, "(byte) 1 >= Byte.MAX_VALUE");
    Tester.checkEqual(byteGe(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE >= Byte.MIN_VALUE, "Byte.MAX_VALUE >= Byte.MIN_VALUE");
    Tester.checkEqual(byteGe(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE >= (byte) -1, "Byte.MAX_VALUE >= (byte) -1");
    Tester.checkEqual(byteGe(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE >= (byte) 0, "Byte.MAX_VALUE >= (byte) 0");
    Tester.checkEqual(byteGe(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE >= (byte) 1, "Byte.MAX_VALUE >= (byte) 1");
    Tester.checkEqual(byteGe(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE >= Byte.MAX_VALUE, "Byte.MAX_VALUE >= Byte.MAX_VALUE");
    Tester.checkEqual(byteEq(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE == Byte.MIN_VALUE, "Byte.MIN_VALUE == Byte.MIN_VALUE");
    Tester.checkEqual(byteEq(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE == (byte) -1, "Byte.MIN_VALUE == (byte) -1");
    Tester.checkEqual(byteEq(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE == (byte) 0, "Byte.MIN_VALUE == (byte) 0");
    Tester.checkEqual(byteEq(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE == (byte) 1, "Byte.MIN_VALUE == (byte) 1");
    Tester.checkEqual(byteEq(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE == Byte.MAX_VALUE, "Byte.MIN_VALUE == Byte.MAX_VALUE");
    Tester.checkEqual(byteEq((byte) -1, Byte.MIN_VALUE), (byte) -1 == Byte.MIN_VALUE, "(byte) -1 == Byte.MIN_VALUE");
    Tester.checkEqual(byteEq((byte) -1, (byte) -1), (byte) -1 == (byte) -1, "(byte) -1 == (byte) -1");
    Tester.checkEqual(byteEq((byte) -1, (byte) 0), (byte) -1 == (byte) 0, "(byte) -1 == (byte) 0");
    Tester.checkEqual(byteEq((byte) -1, (byte) 1), (byte) -1 == (byte) 1, "(byte) -1 == (byte) 1");
    Tester.checkEqual(byteEq((byte) -1, Byte.MAX_VALUE), (byte) -1 == Byte.MAX_VALUE, "(byte) -1 == Byte.MAX_VALUE");
    Tester.checkEqual(byteEq((byte) 0, Byte.MIN_VALUE), (byte) 0 == Byte.MIN_VALUE, "(byte) 0 == Byte.MIN_VALUE");
    Tester.checkEqual(byteEq((byte) 0, (byte) -1), (byte) 0 == (byte) -1, "(byte) 0 == (byte) -1");
    Tester.checkEqual(byteEq((byte) 0, (byte) 0), (byte) 0 == (byte) 0, "(byte) 0 == (byte) 0");
    Tester.checkEqual(byteEq((byte) 0, (byte) 1), (byte) 0 == (byte) 1, "(byte) 0 == (byte) 1");
    Tester.checkEqual(byteEq((byte) 0, Byte.MAX_VALUE), (byte) 0 == Byte.MAX_VALUE, "(byte) 0 == Byte.MAX_VALUE");
    Tester.checkEqual(byteEq((byte) 1, Byte.MIN_VALUE), (byte) 1 == Byte.MIN_VALUE, "(byte) 1 == Byte.MIN_VALUE");
    Tester.checkEqual(byteEq((byte) 1, (byte) -1), (byte) 1 == (byte) -1, "(byte) 1 == (byte) -1");
    Tester.checkEqual(byteEq((byte) 1, (byte) 0), (byte) 1 == (byte) 0, "(byte) 1 == (byte) 0");
    Tester.checkEqual(byteEq((byte) 1, (byte) 1), (byte) 1 == (byte) 1, "(byte) 1 == (byte) 1");
    Tester.checkEqual(byteEq((byte) 1, Byte.MAX_VALUE), (byte) 1 == Byte.MAX_VALUE, "(byte) 1 == Byte.MAX_VALUE");
    Tester.checkEqual(byteEq(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE == Byte.MIN_VALUE, "Byte.MAX_VALUE == Byte.MIN_VALUE");
    Tester.checkEqual(byteEq(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE == (byte) -1, "Byte.MAX_VALUE == (byte) -1");
    Tester.checkEqual(byteEq(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE == (byte) 0, "Byte.MAX_VALUE == (byte) 0");
    Tester.checkEqual(byteEq(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE == (byte) 1, "Byte.MAX_VALUE == (byte) 1");
    Tester.checkEqual(byteEq(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE == Byte.MAX_VALUE, "Byte.MAX_VALUE == Byte.MAX_VALUE");
    Tester.checkEqual(byteNe(Byte.MIN_VALUE, Byte.MIN_VALUE), Byte.MIN_VALUE != Byte.MIN_VALUE, "Byte.MIN_VALUE != Byte.MIN_VALUE");
    Tester.checkEqual(byteNe(Byte.MIN_VALUE, (byte) -1), Byte.MIN_VALUE != (byte) -1, "Byte.MIN_VALUE != (byte) -1");
    Tester.checkEqual(byteNe(Byte.MIN_VALUE, (byte) 0), Byte.MIN_VALUE != (byte) 0, "Byte.MIN_VALUE != (byte) 0");
    Tester.checkEqual(byteNe(Byte.MIN_VALUE, (byte) 1), Byte.MIN_VALUE != (byte) 1, "Byte.MIN_VALUE != (byte) 1");
    Tester.checkEqual(byteNe(Byte.MIN_VALUE, Byte.MAX_VALUE), Byte.MIN_VALUE != Byte.MAX_VALUE, "Byte.MIN_VALUE != Byte.MAX_VALUE");
    Tester.checkEqual(byteNe((byte) -1, Byte.MIN_VALUE), (byte) -1 != Byte.MIN_VALUE, "(byte) -1 != Byte.MIN_VALUE");
    Tester.checkEqual(byteNe((byte) -1, (byte) -1), (byte) -1 != (byte) -1, "(byte) -1 != (byte) -1");
    Tester.checkEqual(byteNe((byte) -1, (byte) 0), (byte) -1 != (byte) 0, "(byte) -1 != (byte) 0");
    Tester.checkEqual(byteNe((byte) -1, (byte) 1), (byte) -1 != (byte) 1, "(byte) -1 != (byte) 1");
    Tester.checkEqual(byteNe((byte) -1, Byte.MAX_VALUE), (byte) -1 != Byte.MAX_VALUE, "(byte) -1 != Byte.MAX_VALUE");
    Tester.checkEqual(byteNe((byte) 0, Byte.MIN_VALUE), (byte) 0 != Byte.MIN_VALUE, "(byte) 0 != Byte.MIN_VALUE");
    Tester.checkEqual(byteNe((byte) 0, (byte) -1), (byte) 0 != (byte) -1, "(byte) 0 != (byte) -1");
    Tester.checkEqual(byteNe((byte) 0, (byte) 0), (byte) 0 != (byte) 0, "(byte) 0 != (byte) 0");
    Tester.checkEqual(byteNe((byte) 0, (byte) 1), (byte) 0 != (byte) 1, "(byte) 0 != (byte) 1");
    Tester.checkEqual(byteNe((byte) 0, Byte.MAX_VALUE), (byte) 0 != Byte.MAX_VALUE, "(byte) 0 != Byte.MAX_VALUE");
    Tester.checkEqual(byteNe((byte) 1, Byte.MIN_VALUE), (byte) 1 != Byte.MIN_VALUE, "(byte) 1 != Byte.MIN_VALUE");
    Tester.checkEqual(byteNe((byte) 1, (byte) -1), (byte) 1 != (byte) -1, "(byte) 1 != (byte) -1");
    Tester.checkEqual(byteNe((byte) 1, (byte) 0), (byte) 1 != (byte) 0, "(byte) 1 != (byte) 0");
    Tester.checkEqual(byteNe((byte) 1, (byte) 1), (byte) 1 != (byte) 1, "(byte) 1 != (byte) 1");
    Tester.checkEqual(byteNe((byte) 1, Byte.MAX_VALUE), (byte) 1 != Byte.MAX_VALUE, "(byte) 1 != Byte.MAX_VALUE");
    Tester.checkEqual(byteNe(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE != Byte.MIN_VALUE, "Byte.MAX_VALUE != Byte.MIN_VALUE");
    Tester.checkEqual(byteNe(Byte.MAX_VALUE, (byte) -1), Byte.MAX_VALUE != (byte) -1, "Byte.MAX_VALUE != (byte) -1");
    Tester.checkEqual(byteNe(Byte.MAX_VALUE, (byte) 0), Byte.MAX_VALUE != (byte) 0, "Byte.MAX_VALUE != (byte) 0");
    Tester.checkEqual(byteNe(Byte.MAX_VALUE, (byte) 1), Byte.MAX_VALUE != (byte) 1, "Byte.MAX_VALUE != (byte) 1");
    Tester.checkEqual(byteNe(Byte.MAX_VALUE, Byte.MAX_VALUE), Byte.MAX_VALUE != Byte.MAX_VALUE, "Byte.MAX_VALUE != Byte.MAX_VALUE");
    Tester.checkEqual(byteAnd(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE & Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE & Byte.MIN_VALUE)");
    Tester.checkEqual(byteAnd(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE & (byte) -1), "(byte) (Byte.MIN_VALUE & (byte) -1)");
    Tester.checkEqual(byteAnd(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE & (byte) 0), "(byte) (Byte.MIN_VALUE & (byte) 0)");
    Tester.checkEqual(byteAnd(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE & (byte) 1), "(byte) (Byte.MIN_VALUE & (byte) 1)");
    Tester.checkEqual(byteAnd(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE & Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE & Byte.MAX_VALUE)");
    Tester.checkEqual(byteAnd((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 & Byte.MIN_VALUE), "(byte) ((byte) -1 & Byte.MIN_VALUE)");
    Tester.checkEqual(byteAnd((byte) -1, (byte) -1), (byte) ((byte) -1 & (byte) -1), "(byte) ((byte) -1 & (byte) -1)");
    Tester.checkEqual(byteAnd((byte) -1, (byte) 0), (byte) ((byte) -1 & (byte) 0), "(byte) ((byte) -1 & (byte) 0)");
    Tester.checkEqual(byteAnd((byte) -1, (byte) 1), (byte) ((byte) -1 & (byte) 1), "(byte) ((byte) -1 & (byte) 1)");
    Tester.checkEqual(byteAnd((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 & Byte.MAX_VALUE), "(byte) ((byte) -1 & Byte.MAX_VALUE)");
    Tester.checkEqual(byteAnd((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 & Byte.MIN_VALUE), "(byte) ((byte) 0 & Byte.MIN_VALUE)");
    Tester.checkEqual(byteAnd((byte) 0, (byte) -1), (byte) ((byte) 0 & (byte) -1), "(byte) ((byte) 0 & (byte) -1)");
    Tester.checkEqual(byteAnd((byte) 0, (byte) 0), (byte) ((byte) 0 & (byte) 0), "(byte) ((byte) 0 & (byte) 0)");
    Tester.checkEqual(byteAnd((byte) 0, (byte) 1), (byte) ((byte) 0 & (byte) 1), "(byte) ((byte) 0 & (byte) 1)");
    Tester.checkEqual(byteAnd((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 & Byte.MAX_VALUE), "(byte) ((byte) 0 & Byte.MAX_VALUE)");
    Tester.checkEqual(byteAnd((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 & Byte.MIN_VALUE), "(byte) ((byte) 1 & Byte.MIN_VALUE)");
    Tester.checkEqual(byteAnd((byte) 1, (byte) -1), (byte) ((byte) 1 & (byte) -1), "(byte) ((byte) 1 & (byte) -1)");
    Tester.checkEqual(byteAnd((byte) 1, (byte) 0), (byte) ((byte) 1 & (byte) 0), "(byte) ((byte) 1 & (byte) 0)");
    Tester.checkEqual(byteAnd((byte) 1, (byte) 1), (byte) ((byte) 1 & (byte) 1), "(byte) ((byte) 1 & (byte) 1)");
    Tester.checkEqual(byteAnd((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 & Byte.MAX_VALUE), "(byte) ((byte) 1 & Byte.MAX_VALUE)");
    Tester.checkEqual(byteAnd(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE & Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE & Byte.MIN_VALUE)");
    Tester.checkEqual(byteAnd(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE & (byte) -1), "(byte) (Byte.MAX_VALUE & (byte) -1)");
    Tester.checkEqual(byteAnd(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE & (byte) 0), "(byte) (Byte.MAX_VALUE & (byte) 0)");
    Tester.checkEqual(byteAnd(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE & (byte) 1), "(byte) (Byte.MAX_VALUE & (byte) 1)");
    Tester.checkEqual(byteAnd(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE & Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE & Byte.MAX_VALUE)");
    Tester.checkEqual(byteXor(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE ^ Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE ^ Byte.MIN_VALUE)");
    Tester.checkEqual(byteXor(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE ^ (byte) -1), "(byte) (Byte.MIN_VALUE ^ (byte) -1)");
    Tester.checkEqual(byteXor(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE ^ (byte) 0), "(byte) (Byte.MIN_VALUE ^ (byte) 0)");
    Tester.checkEqual(byteXor(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE ^ (byte) 1), "(byte) (Byte.MIN_VALUE ^ (byte) 1)");
    Tester.checkEqual(byteXor(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE ^ Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE ^ Byte.MAX_VALUE)");
    Tester.checkEqual(byteXor((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 ^ Byte.MIN_VALUE), "(byte) ((byte) -1 ^ Byte.MIN_VALUE)");
    Tester.checkEqual(byteXor((byte) -1, (byte) -1), (byte) ((byte) -1 ^ (byte) -1), "(byte) ((byte) -1 ^ (byte) -1)");
    Tester.checkEqual(byteXor((byte) -1, (byte) 0), (byte) ((byte) -1 ^ (byte) 0), "(byte) ((byte) -1 ^ (byte) 0)");
    Tester.checkEqual(byteXor((byte) -1, (byte) 1), (byte) ((byte) -1 ^ (byte) 1), "(byte) ((byte) -1 ^ (byte) 1)");
    Tester.checkEqual(byteXor((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 ^ Byte.MAX_VALUE), "(byte) ((byte) -1 ^ Byte.MAX_VALUE)");
    Tester.checkEqual(byteXor((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 ^ Byte.MIN_VALUE), "(byte) ((byte) 0 ^ Byte.MIN_VALUE)");
    Tester.checkEqual(byteXor((byte) 0, (byte) -1), (byte) ((byte) 0 ^ (byte) -1), "(byte) ((byte) 0 ^ (byte) -1)");
    Tester.checkEqual(byteXor((byte) 0, (byte) 0), (byte) ((byte) 0 ^ (byte) 0), "(byte) ((byte) 0 ^ (byte) 0)");
    Tester.checkEqual(byteXor((byte) 0, (byte) 1), (byte) ((byte) 0 ^ (byte) 1), "(byte) ((byte) 0 ^ (byte) 1)");
    Tester.checkEqual(byteXor((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 ^ Byte.MAX_VALUE), "(byte) ((byte) 0 ^ Byte.MAX_VALUE)");
    Tester.checkEqual(byteXor((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 ^ Byte.MIN_VALUE), "(byte) ((byte) 1 ^ Byte.MIN_VALUE)");
    Tester.checkEqual(byteXor((byte) 1, (byte) -1), (byte) ((byte) 1 ^ (byte) -1), "(byte) ((byte) 1 ^ (byte) -1)");
    Tester.checkEqual(byteXor((byte) 1, (byte) 0), (byte) ((byte) 1 ^ (byte) 0), "(byte) ((byte) 1 ^ (byte) 0)");
    Tester.checkEqual(byteXor((byte) 1, (byte) 1), (byte) ((byte) 1 ^ (byte) 1), "(byte) ((byte) 1 ^ (byte) 1)");
    Tester.checkEqual(byteXor((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 ^ Byte.MAX_VALUE), "(byte) ((byte) 1 ^ Byte.MAX_VALUE)");
    Tester.checkEqual(byteXor(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE ^ Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE ^ Byte.MIN_VALUE)");
    Tester.checkEqual(byteXor(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE ^ (byte) -1), "(byte) (Byte.MAX_VALUE ^ (byte) -1)");
    Tester.checkEqual(byteXor(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE ^ (byte) 0), "(byte) (Byte.MAX_VALUE ^ (byte) 0)");
    Tester.checkEqual(byteXor(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE ^ (byte) 1), "(byte) (Byte.MAX_VALUE ^ (byte) 1)");
    Tester.checkEqual(byteXor(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE ^ Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE ^ Byte.MAX_VALUE)");
    Tester.checkEqual(byteOr(Byte.MIN_VALUE, Byte.MIN_VALUE), (byte) (Byte.MIN_VALUE | Byte.MIN_VALUE), "(byte) (Byte.MIN_VALUE | Byte.MIN_VALUE)");
    Tester.checkEqual(byteOr(Byte.MIN_VALUE, (byte) -1), (byte) (Byte.MIN_VALUE | (byte) -1), "(byte) (Byte.MIN_VALUE | (byte) -1)");
    Tester.checkEqual(byteOr(Byte.MIN_VALUE, (byte) 0), (byte) (Byte.MIN_VALUE | (byte) 0), "(byte) (Byte.MIN_VALUE | (byte) 0)");
    Tester.checkEqual(byteOr(Byte.MIN_VALUE, (byte) 1), (byte) (Byte.MIN_VALUE | (byte) 1), "(byte) (Byte.MIN_VALUE | (byte) 1)");
    Tester.checkEqual(byteOr(Byte.MIN_VALUE, Byte.MAX_VALUE), (byte) (Byte.MIN_VALUE | Byte.MAX_VALUE), "(byte) (Byte.MIN_VALUE | Byte.MAX_VALUE)");
    Tester.checkEqual(byteOr((byte) -1, Byte.MIN_VALUE), (byte) ((byte) -1 | Byte.MIN_VALUE), "(byte) ((byte) -1 | Byte.MIN_VALUE)");
    Tester.checkEqual(byteOr((byte) -1, (byte) -1), (byte) ((byte) -1 | (byte) -1), "(byte) ((byte) -1 | (byte) -1)");
    Tester.checkEqual(byteOr((byte) -1, (byte) 0), (byte) ((byte) -1 | (byte) 0), "(byte) ((byte) -1 | (byte) 0)");
    Tester.checkEqual(byteOr((byte) -1, (byte) 1), (byte) ((byte) -1 | (byte) 1), "(byte) ((byte) -1 | (byte) 1)");
    Tester.checkEqual(byteOr((byte) -1, Byte.MAX_VALUE), (byte) ((byte) -1 | Byte.MAX_VALUE), "(byte) ((byte) -1 | Byte.MAX_VALUE)");
    Tester.checkEqual(byteOr((byte) 0, Byte.MIN_VALUE), (byte) ((byte) 0 | Byte.MIN_VALUE), "(byte) ((byte) 0 | Byte.MIN_VALUE)");
    Tester.checkEqual(byteOr((byte) 0, (byte) -1), (byte) ((byte) 0 | (byte) -1), "(byte) ((byte) 0 | (byte) -1)");
    Tester.checkEqual(byteOr((byte) 0, (byte) 0), (byte) ((byte) 0 | (byte) 0), "(byte) ((byte) 0 | (byte) 0)");
    Tester.checkEqual(byteOr((byte) 0, (byte) 1), (byte) ((byte) 0 | (byte) 1), "(byte) ((byte) 0 | (byte) 1)");
    Tester.checkEqual(byteOr((byte) 0, Byte.MAX_VALUE), (byte) ((byte) 0 | Byte.MAX_VALUE), "(byte) ((byte) 0 | Byte.MAX_VALUE)");
    Tester.checkEqual(byteOr((byte) 1, Byte.MIN_VALUE), (byte) ((byte) 1 | Byte.MIN_VALUE), "(byte) ((byte) 1 | Byte.MIN_VALUE)");
    Tester.checkEqual(byteOr((byte) 1, (byte) -1), (byte) ((byte) 1 | (byte) -1), "(byte) ((byte) 1 | (byte) -1)");
    Tester.checkEqual(byteOr((byte) 1, (byte) 0), (byte) ((byte) 1 | (byte) 0), "(byte) ((byte) 1 | (byte) 0)");
    Tester.checkEqual(byteOr((byte) 1, (byte) 1), (byte) ((byte) 1 | (byte) 1), "(byte) ((byte) 1 | (byte) 1)");
    Tester.checkEqual(byteOr((byte) 1, Byte.MAX_VALUE), (byte) ((byte) 1 | Byte.MAX_VALUE), "(byte) ((byte) 1 | Byte.MAX_VALUE)");
    Tester.checkEqual(byteOr(Byte.MAX_VALUE, Byte.MIN_VALUE), (byte) (Byte.MAX_VALUE | Byte.MIN_VALUE), "(byte) (Byte.MAX_VALUE | Byte.MIN_VALUE)");
    Tester.checkEqual(byteOr(Byte.MAX_VALUE, (byte) -1), (byte) (Byte.MAX_VALUE | (byte) -1), "(byte) (Byte.MAX_VALUE | (byte) -1)");
    Tester.checkEqual(byteOr(Byte.MAX_VALUE, (byte) 0), (byte) (Byte.MAX_VALUE | (byte) 0), "(byte) (Byte.MAX_VALUE | (byte) 0)");
    Tester.checkEqual(byteOr(Byte.MAX_VALUE, (byte) 1), (byte) (Byte.MAX_VALUE | (byte) 1), "(byte) (Byte.MAX_VALUE | (byte) 1)");
    Tester.checkEqual(byteOr(Byte.MAX_VALUE, Byte.MAX_VALUE), (byte) (Byte.MAX_VALUE | Byte.MAX_VALUE), "(byte) (Byte.MAX_VALUE | Byte.MAX_VALUE)");
  }
  static void byteSwitch() {
    switch(0) {
      case ((((byte) + Byte.MIN_VALUE) == 0) ? 0 : 0):
      case ((((byte) + (byte) -1) == 0) ? 1 : 1):
      case ((((byte) + (byte) 0) == 0) ? 2 : 2):
      case ((((byte) + (byte) 1) == 0) ? 3 : 3):
      case ((((byte) + Byte.MAX_VALUE) == 0) ? 4 : 4):
      case ((((byte) - Byte.MIN_VALUE) == 0) ? 5 : 5):
      case ((((byte) - (byte) -1) == 0) ? 6 : 6):
      case ((((byte) - (byte) 0) == 0) ? 7 : 7):
      case ((((byte) - (byte) 1) == 0) ? 8 : 8):
      case ((((byte) - Byte.MAX_VALUE) == 0) ? 9 : 9):
      case ((((byte) ~ Byte.MIN_VALUE) == 0) ? 10 : 10):
      case ((((byte) ~ (byte) -1) == 0) ? 11 : 11):
      case ((((byte) ~ (byte) 0) == 0) ? 12 : 12):
      case ((((byte) ~ (byte) 1) == 0) ? 13 : 13):
      case ((((byte) ~ Byte.MAX_VALUE) == 0) ? 14 : 14):
      case ((((byte) (Byte.MIN_VALUE * Byte.MIN_VALUE)) == 0) ? 15 : 15):
      case ((((byte) (Byte.MIN_VALUE * (byte) -1)) == 0) ? 16 : 16):
      case ((((byte) (Byte.MIN_VALUE * (byte) 0)) == 0) ? 17 : 17):
      case ((((byte) (Byte.MIN_VALUE * (byte) 1)) == 0) ? 18 : 18):
      case ((((byte) (Byte.MIN_VALUE * Byte.MAX_VALUE)) == 0) ? 19 : 19):
      case ((((byte) ((byte) -1 * Byte.MIN_VALUE)) == 0) ? 20 : 20):
      case ((((byte) ((byte) -1 * (byte) -1)) == 0) ? 21 : 21):
      case ((((byte) ((byte) -1 * (byte) 0)) == 0) ? 22 : 22):
      case ((((byte) ((byte) -1 * (byte) 1)) == 0) ? 23 : 23):
      case ((((byte) ((byte) -1 * Byte.MAX_VALUE)) == 0) ? 24 : 24):
      case ((((byte) ((byte) 0 * Byte.MIN_VALUE)) == 0) ? 25 : 25):
      case ((((byte) ((byte) 0 * (byte) -1)) == 0) ? 26 : 26):
      case ((((byte) ((byte) 0 * (byte) 0)) == 0) ? 27 : 27):
      case ((((byte) ((byte) 0 * (byte) 1)) == 0) ? 28 : 28):
      case ((((byte) ((byte) 0 * Byte.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((byte) ((byte) 1 * Byte.MIN_VALUE)) == 0) ? 30 : 30):
      case ((((byte) ((byte) 1 * (byte) -1)) == 0) ? 31 : 31):
      case ((((byte) ((byte) 1 * (byte) 0)) == 0) ? 32 : 32):
      case ((((byte) ((byte) 1 * (byte) 1)) == 0) ? 33 : 33):
      case ((((byte) ((byte) 1 * Byte.MAX_VALUE)) == 0) ? 34 : 34):
      case ((((byte) (Byte.MAX_VALUE * Byte.MIN_VALUE)) == 0) ? 35 : 35):
      case ((((byte) (Byte.MAX_VALUE * (byte) -1)) == 0) ? 36 : 36):
      case ((((byte) (Byte.MAX_VALUE * (byte) 0)) == 0) ? 37 : 37):
      case ((((byte) (Byte.MAX_VALUE * (byte) 1)) == 0) ? 38 : 38):
      case ((((byte) (Byte.MAX_VALUE * Byte.MAX_VALUE)) == 0) ? 39 : 39):
      case ((((byte) (Byte.MIN_VALUE / Byte.MIN_VALUE)) == 0) ? 40 : 40):
      case ((((byte) (Byte.MIN_VALUE / (byte) -1)) == 0) ? 41 : 41):
      case ((((byte) (Byte.MIN_VALUE / (byte) 1)) == 0) ? 42 : 42):
      case ((((byte) (Byte.MIN_VALUE / Byte.MAX_VALUE)) == 0) ? 43 : 43):
      case ((((byte) ((byte) -1 / Byte.MIN_VALUE)) == 0) ? 44 : 44):
      case ((((byte) ((byte) -1 / (byte) -1)) == 0) ? 45 : 45):
      case ((((byte) ((byte) -1 / (byte) 1)) == 0) ? 46 : 46):
      case ((((byte) ((byte) -1 / Byte.MAX_VALUE)) == 0) ? 47 : 47):
      case ((((byte) ((byte) 0 / Byte.MIN_VALUE)) == 0) ? 48 : 48):
      case ((((byte) ((byte) 0 / (byte) -1)) == 0) ? 49 : 49):
      case ((((byte) ((byte) 0 / (byte) 1)) == 0) ? 50 : 50):
      case ((((byte) ((byte) 0 / Byte.MAX_VALUE)) == 0) ? 51 : 51):
      case ((((byte) ((byte) 1 / Byte.MIN_VALUE)) == 0) ? 52 : 52):
      case ((((byte) ((byte) 1 / (byte) -1)) == 0) ? 53 : 53):
      case ((((byte) ((byte) 1 / (byte) 1)) == 0) ? 54 : 54):
      case ((((byte) ((byte) 1 / Byte.MAX_VALUE)) == 0) ? 55 : 55):
      case ((((byte) (Byte.MAX_VALUE / Byte.MIN_VALUE)) == 0) ? 56 : 56):
      case ((((byte) (Byte.MAX_VALUE / (byte) -1)) == 0) ? 57 : 57):
      case ((((byte) (Byte.MAX_VALUE / (byte) 1)) == 0) ? 58 : 58):
      case ((((byte) (Byte.MAX_VALUE / Byte.MAX_VALUE)) == 0) ? 59 : 59):
      case ((((byte) (Byte.MIN_VALUE % Byte.MIN_VALUE)) == 0) ? 60 : 60):
      case ((((byte) (Byte.MIN_VALUE % (byte) -1)) == 0) ? 61 : 61):
      case ((((byte) (Byte.MIN_VALUE % (byte) 1)) == 0) ? 62 : 62):
      case ((((byte) (Byte.MIN_VALUE % Byte.MAX_VALUE)) == 0) ? 63 : 63):
      case ((((byte) ((byte) -1 % Byte.MIN_VALUE)) == 0) ? 64 : 64):
      case ((((byte) ((byte) -1 % (byte) -1)) == 0) ? 65 : 65):
      case ((((byte) ((byte) -1 % (byte) 1)) == 0) ? 66 : 66):
      case ((((byte) ((byte) -1 % Byte.MAX_VALUE)) == 0) ? 67 : 67):
      case ((((byte) ((byte) 0 % Byte.MIN_VALUE)) == 0) ? 68 : 68):
      case ((((byte) ((byte) 0 % (byte) -1)) == 0) ? 69 : 69):
      case ((((byte) ((byte) 0 % (byte) 1)) == 0) ? 70 : 70):
      case ((((byte) ((byte) 0 % Byte.MAX_VALUE)) == 0) ? 71 : 71):
      case ((((byte) ((byte) 1 % Byte.MIN_VALUE)) == 0) ? 72 : 72):
      case ((((byte) ((byte) 1 % (byte) -1)) == 0) ? 73 : 73):
      case ((((byte) ((byte) 1 % (byte) 1)) == 0) ? 74 : 74):
      case ((((byte) ((byte) 1 % Byte.MAX_VALUE)) == 0) ? 75 : 75):
      case ((((byte) (Byte.MAX_VALUE % Byte.MIN_VALUE)) == 0) ? 76 : 76):
      case ((((byte) (Byte.MAX_VALUE % (byte) -1)) == 0) ? 77 : 77):
      case ((((byte) (Byte.MAX_VALUE % (byte) 1)) == 0) ? 78 : 78):
      case ((((byte) (Byte.MAX_VALUE % Byte.MAX_VALUE)) == 0) ? 79 : 79):
      case ((((byte) (Byte.MIN_VALUE + Byte.MIN_VALUE)) == 0) ? 80 : 80):
      case ((((byte) (Byte.MIN_VALUE + (byte) -1)) == 0) ? 81 : 81):
      case ((((byte) (Byte.MIN_VALUE + (byte) 0)) == 0) ? 82 : 82):
      case ((((byte) (Byte.MIN_VALUE + (byte) 1)) == 0) ? 83 : 83):
      case ((((byte) (Byte.MIN_VALUE + Byte.MAX_VALUE)) == 0) ? 84 : 84):
      case ((((byte) ((byte) -1 + Byte.MIN_VALUE)) == 0) ? 85 : 85):
      case ((((byte) ((byte) -1 + (byte) -1)) == 0) ? 86 : 86):
      case ((((byte) ((byte) -1 + (byte) 0)) == 0) ? 87 : 87):
      case ((((byte) ((byte) -1 + (byte) 1)) == 0) ? 88 : 88):
      case ((((byte) ((byte) -1 + Byte.MAX_VALUE)) == 0) ? 89 : 89):
      case ((((byte) ((byte) 0 + Byte.MIN_VALUE)) == 0) ? 90 : 90):
      case ((((byte) ((byte) 0 + (byte) -1)) == 0) ? 91 : 91):
      case ((((byte) ((byte) 0 + (byte) 0)) == 0) ? 92 : 92):
      case ((((byte) ((byte) 0 + (byte) 1)) == 0) ? 93 : 93):
      case ((((byte) ((byte) 0 + Byte.MAX_VALUE)) == 0) ? 94 : 94):
      case ((((byte) ((byte) 1 + Byte.MIN_VALUE)) == 0) ? 95 : 95):
      case ((((byte) ((byte) 1 + (byte) -1)) == 0) ? 96 : 96):
      case ((((byte) ((byte) 1 + (byte) 0)) == 0) ? 97 : 97):
      case ((((byte) ((byte) 1 + (byte) 1)) == 0) ? 98 : 98):
      case ((((byte) ((byte) 1 + Byte.MAX_VALUE)) == 0) ? 99 : 99):
      case ((((byte) (Byte.MAX_VALUE + Byte.MIN_VALUE)) == 0) ? 100 : 100):
      case ((((byte) (Byte.MAX_VALUE + (byte) -1)) == 0) ? 101 : 101):
      case ((((byte) (Byte.MAX_VALUE + (byte) 0)) == 0) ? 102 : 102):
      case ((((byte) (Byte.MAX_VALUE + (byte) 1)) == 0) ? 103 : 103):
      case ((((byte) (Byte.MAX_VALUE + Byte.MAX_VALUE)) == 0) ? 104 : 104):
      case ((((byte) (Byte.MIN_VALUE - Byte.MIN_VALUE)) == 0) ? 105 : 105):
      case ((((byte) (Byte.MIN_VALUE - (byte) -1)) == 0) ? 106 : 106):
      case ((((byte) (Byte.MIN_VALUE - (byte) 0)) == 0) ? 107 : 107):
      case ((((byte) (Byte.MIN_VALUE - (byte) 1)) == 0) ? 108 : 108):
      case ((((byte) (Byte.MIN_VALUE - Byte.MAX_VALUE)) == 0) ? 109 : 109):
      case ((((byte) ((byte) -1 - Byte.MIN_VALUE)) == 0) ? 110 : 110):
      case ((((byte) ((byte) -1 - (byte) -1)) == 0) ? 111 : 111):
      case ((((byte) ((byte) -1 - (byte) 0)) == 0) ? 112 : 112):
      case ((((byte) ((byte) -1 - (byte) 1)) == 0) ? 113 : 113):
      case ((((byte) ((byte) -1 - Byte.MAX_VALUE)) == 0) ? 114 : 114):
      case ((((byte) ((byte) 0 - Byte.MIN_VALUE)) == 0) ? 115 : 115):
      case ((((byte) ((byte) 0 - (byte) -1)) == 0) ? 116 : 116):
      case ((((byte) ((byte) 0 - (byte) 0)) == 0) ? 117 : 117):
      case ((((byte) ((byte) 0 - (byte) 1)) == 0) ? 118 : 118):
      case ((((byte) ((byte) 0 - Byte.MAX_VALUE)) == 0) ? 119 : 119):
      case ((((byte) ((byte) 1 - Byte.MIN_VALUE)) == 0) ? 120 : 120):
      case ((((byte) ((byte) 1 - (byte) -1)) == 0) ? 121 : 121):
      case ((((byte) ((byte) 1 - (byte) 0)) == 0) ? 122 : 122):
      case ((((byte) ((byte) 1 - (byte) 1)) == 0) ? 123 : 123):
      case ((((byte) ((byte) 1 - Byte.MAX_VALUE)) == 0) ? 124 : 124):
      case ((((byte) (Byte.MAX_VALUE - Byte.MIN_VALUE)) == 0) ? 125 : 125):
      case ((((byte) (Byte.MAX_VALUE - (byte) -1)) == 0) ? 126 : 126):
      case ((((byte) (Byte.MAX_VALUE - (byte) 0)) == 0) ? 127 : 127):
      case ((((byte) (Byte.MAX_VALUE - (byte) 1)) == 0) ? 128 : 128):
      case ((((byte) (Byte.MAX_VALUE - Byte.MAX_VALUE)) == 0) ? 129 : 129):
      case ((((byte) (Byte.MIN_VALUE << Byte.MIN_VALUE)) == 0) ? 130 : 130):
      case ((((byte) (Byte.MIN_VALUE << (byte) -1)) == 0) ? 131 : 131):
      case ((((byte) (Byte.MIN_VALUE << (byte) 0)) == 0) ? 132 : 132):
      case ((((byte) (Byte.MIN_VALUE << (byte) 1)) == 0) ? 133 : 133):
      case ((((byte) (Byte.MIN_VALUE << Byte.MAX_VALUE)) == 0) ? 134 : 134):
      case ((((byte) ((byte) -1 << Byte.MIN_VALUE)) == 0) ? 135 : 135):
      case ((((byte) ((byte) -1 << (byte) -1)) == 0) ? 136 : 136):
      case ((((byte) ((byte) -1 << (byte) 0)) == 0) ? 137 : 137):
      case ((((byte) ((byte) -1 << (byte) 1)) == 0) ? 138 : 138):
      case ((((byte) ((byte) -1 << Byte.MAX_VALUE)) == 0) ? 139 : 139):
      case ((((byte) ((byte) 0 << Byte.MIN_VALUE)) == 0) ? 140 : 140):
      case ((((byte) ((byte) 0 << (byte) -1)) == 0) ? 141 : 141):
      case ((((byte) ((byte) 0 << (byte) 0)) == 0) ? 142 : 142):
      case ((((byte) ((byte) 0 << (byte) 1)) == 0) ? 143 : 143):
      case ((((byte) ((byte) 0 << Byte.MAX_VALUE)) == 0) ? 144 : 144):
      case ((((byte) ((byte) 1 << Byte.MIN_VALUE)) == 0) ? 145 : 145):
      case ((((byte) ((byte) 1 << (byte) -1)) == 0) ? 146 : 146):
      case ((((byte) ((byte) 1 << (byte) 0)) == 0) ? 147 : 147):
      case ((((byte) ((byte) 1 << (byte) 1)) == 0) ? 148 : 148):
      case ((((byte) ((byte) 1 << Byte.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((byte) (Byte.MAX_VALUE << Byte.MIN_VALUE)) == 0) ? 150 : 150):
      case ((((byte) (Byte.MAX_VALUE << (byte) -1)) == 0) ? 151 : 151):
      case ((((byte) (Byte.MAX_VALUE << (byte) 0)) == 0) ? 152 : 152):
      case ((((byte) (Byte.MAX_VALUE << (byte) 1)) == 0) ? 153 : 153):
      case ((((byte) (Byte.MAX_VALUE << Byte.MAX_VALUE)) == 0) ? 154 : 154):
      case ((((byte) (Byte.MIN_VALUE >> Byte.MIN_VALUE)) == 0) ? 155 : 155):
      case ((((byte) (Byte.MIN_VALUE >> (byte) -1)) == 0) ? 156 : 156):
      case ((((byte) (Byte.MIN_VALUE >> (byte) 0)) == 0) ? 157 : 157):
      case ((((byte) (Byte.MIN_VALUE >> (byte) 1)) == 0) ? 158 : 158):
      case ((((byte) (Byte.MIN_VALUE >> Byte.MAX_VALUE)) == 0) ? 159 : 159):
      case ((((byte) ((byte) -1 >> Byte.MIN_VALUE)) == 0) ? 160 : 160):
      case ((((byte) ((byte) -1 >> (byte) -1)) == 0) ? 161 : 161):
      case ((((byte) ((byte) -1 >> (byte) 0)) == 0) ? 162 : 162):
      case ((((byte) ((byte) -1 >> (byte) 1)) == 0) ? 163 : 163):
      case ((((byte) ((byte) -1 >> Byte.MAX_VALUE)) == 0) ? 164 : 164):
      case ((((byte) ((byte) 0 >> Byte.MIN_VALUE)) == 0) ? 165 : 165):
      case ((((byte) ((byte) 0 >> (byte) -1)) == 0) ? 166 : 166):
      case ((((byte) ((byte) 0 >> (byte) 0)) == 0) ? 167 : 167):
      case ((((byte) ((byte) 0 >> (byte) 1)) == 0) ? 168 : 168):
      case ((((byte) ((byte) 0 >> Byte.MAX_VALUE)) == 0) ? 169 : 169):
      case ((((byte) ((byte) 1 >> Byte.MIN_VALUE)) == 0) ? 170 : 170):
      case ((((byte) ((byte) 1 >> (byte) -1)) == 0) ? 171 : 171):
      case ((((byte) ((byte) 1 >> (byte) 0)) == 0) ? 172 : 172):
      case ((((byte) ((byte) 1 >> (byte) 1)) == 0) ? 173 : 173):
      case ((((byte) ((byte) 1 >> Byte.MAX_VALUE)) == 0) ? 174 : 174):
      case ((((byte) (Byte.MAX_VALUE >> Byte.MIN_VALUE)) == 0) ? 175 : 175):
      case ((((byte) (Byte.MAX_VALUE >> (byte) -1)) == 0) ? 176 : 176):
      case ((((byte) (Byte.MAX_VALUE >> (byte) 0)) == 0) ? 177 : 177):
      case ((((byte) (Byte.MAX_VALUE >> (byte) 1)) == 0) ? 178 : 178):
      case ((((byte) (Byte.MAX_VALUE >> Byte.MAX_VALUE)) == 0) ? 179 : 179):
      case ((((byte) (Byte.MIN_VALUE >>> Byte.MIN_VALUE)) == 0) ? 180 : 180):
      case ((((byte) (Byte.MIN_VALUE >>> (byte) -1)) == 0) ? 181 : 181):
      case ((((byte) (Byte.MIN_VALUE >>> (byte) 0)) == 0) ? 182 : 182):
      case ((((byte) (Byte.MIN_VALUE >>> (byte) 1)) == 0) ? 183 : 183):
      case ((((byte) (Byte.MIN_VALUE >>> Byte.MAX_VALUE)) == 0) ? 184 : 184):
      case ((((byte) ((byte) -1 >>> Byte.MIN_VALUE)) == 0) ? 185 : 185):
      case ((((byte) ((byte) -1 >>> (byte) -1)) == 0) ? 186 : 186):
      case ((((byte) ((byte) -1 >>> (byte) 0)) == 0) ? 187 : 187):
      case ((((byte) ((byte) -1 >>> (byte) 1)) == 0) ? 188 : 188):
      case ((((byte) ((byte) -1 >>> Byte.MAX_VALUE)) == 0) ? 189 : 189):
      case ((((byte) ((byte) 0 >>> Byte.MIN_VALUE)) == 0) ? 190 : 190):
      case ((((byte) ((byte) 0 >>> (byte) -1)) == 0) ? 191 : 191):
      case ((((byte) ((byte) 0 >>> (byte) 0)) == 0) ? 192 : 192):
      case ((((byte) ((byte) 0 >>> (byte) 1)) == 0) ? 193 : 193):
      case ((((byte) ((byte) 0 >>> Byte.MAX_VALUE)) == 0) ? 194 : 194):
      case ((((byte) ((byte) 1 >>> Byte.MIN_VALUE)) == 0) ? 195 : 195):
      case ((((byte) ((byte) 1 >>> (byte) -1)) == 0) ? 196 : 196):
      case ((((byte) ((byte) 1 >>> (byte) 0)) == 0) ? 197 : 197):
      case ((((byte) ((byte) 1 >>> (byte) 1)) == 0) ? 198 : 198):
      case ((((byte) ((byte) 1 >>> Byte.MAX_VALUE)) == 0) ? 199 : 199):
      case ((((byte) (Byte.MAX_VALUE >>> Byte.MIN_VALUE)) == 0) ? 200 : 200):
      case ((((byte) (Byte.MAX_VALUE >>> (byte) -1)) == 0) ? 201 : 201):
      case ((((byte) (Byte.MAX_VALUE >>> (byte) 0)) == 0) ? 202 : 202):
      case ((((byte) (Byte.MAX_VALUE >>> (byte) 1)) == 0) ? 203 : 203):
      case ((((byte) (Byte.MAX_VALUE >>> Byte.MAX_VALUE)) == 0) ? 204 : 204):
      case ((Byte.MIN_VALUE < Byte.MIN_VALUE) ? 205 : 205):
      case ((Byte.MIN_VALUE < (byte) -1) ? 206 : 206):
      case ((Byte.MIN_VALUE < (byte) 0) ? 207 : 207):
      case ((Byte.MIN_VALUE < (byte) 1) ? 208 : 208):
      case ((Byte.MIN_VALUE < Byte.MAX_VALUE) ? 209 : 209):
      case (((byte) -1 < Byte.MIN_VALUE) ? 210 : 210):
      case (((byte) -1 < (byte) -1) ? 211 : 211):
      case (((byte) -1 < (byte) 0) ? 212 : 212):
      case (((byte) -1 < (byte) 1) ? 213 : 213):
      case (((byte) -1 < Byte.MAX_VALUE) ? 214 : 214):
      case (((byte) 0 < Byte.MIN_VALUE) ? 215 : 215):
      case (((byte) 0 < (byte) -1) ? 216 : 216):
      case (((byte) 0 < (byte) 0) ? 217 : 217):
      case (((byte) 0 < (byte) 1) ? 218 : 218):
      case (((byte) 0 < Byte.MAX_VALUE) ? 219 : 219):
      case (((byte) 1 < Byte.MIN_VALUE) ? 220 : 220):
      case (((byte) 1 < (byte) -1) ? 221 : 221):
      case (((byte) 1 < (byte) 0) ? 222 : 222):
      case (((byte) 1 < (byte) 1) ? 223 : 223):
      case (((byte) 1 < Byte.MAX_VALUE) ? 224 : 224):
      case ((Byte.MAX_VALUE < Byte.MIN_VALUE) ? 225 : 225):
      case ((Byte.MAX_VALUE < (byte) -1) ? 226 : 226):
      case ((Byte.MAX_VALUE < (byte) 0) ? 227 : 227):
      case ((Byte.MAX_VALUE < (byte) 1) ? 228 : 228):
      case ((Byte.MAX_VALUE < Byte.MAX_VALUE) ? 229 : 229):
      case ((Byte.MIN_VALUE > Byte.MIN_VALUE) ? 230 : 230):
      case ((Byte.MIN_VALUE > (byte) -1) ? 231 : 231):
      case ((Byte.MIN_VALUE > (byte) 0) ? 232 : 232):
      case ((Byte.MIN_VALUE > (byte) 1) ? 233 : 233):
      case ((Byte.MIN_VALUE > Byte.MAX_VALUE) ? 234 : 234):
      case (((byte) -1 > Byte.MIN_VALUE) ? 235 : 235):
      case (((byte) -1 > (byte) -1) ? 236 : 236):
      case (((byte) -1 > (byte) 0) ? 237 : 237):
      case (((byte) -1 > (byte) 1) ? 238 : 238):
      case (((byte) -1 > Byte.MAX_VALUE) ? 239 : 239):
      case (((byte) 0 > Byte.MIN_VALUE) ? 240 : 240):
      case (((byte) 0 > (byte) -1) ? 241 : 241):
      case (((byte) 0 > (byte) 0) ? 242 : 242):
      case (((byte) 0 > (byte) 1) ? 243 : 243):
      case (((byte) 0 > Byte.MAX_VALUE) ? 244 : 244):
      case (((byte) 1 > Byte.MIN_VALUE) ? 245 : 245):
      case (((byte) 1 > (byte) -1) ? 246 : 246):
      case (((byte) 1 > (byte) 0) ? 247 : 247):
      case (((byte) 1 > (byte) 1) ? 248 : 248):
      case (((byte) 1 > Byte.MAX_VALUE) ? 249 : 249):
      case ((Byte.MAX_VALUE > Byte.MIN_VALUE) ? 250 : 250):
      case ((Byte.MAX_VALUE > (byte) -1) ? 251 : 251):
      case ((Byte.MAX_VALUE > (byte) 0) ? 252 : 252):
      case ((Byte.MAX_VALUE > (byte) 1) ? 253 : 253):
      case ((Byte.MAX_VALUE > Byte.MAX_VALUE) ? 254 : 254):
      case ((Byte.MIN_VALUE <= Byte.MIN_VALUE) ? 255 : 255):
      case ((Byte.MIN_VALUE <= (byte) -1) ? 256 : 256):
      case ((Byte.MIN_VALUE <= (byte) 0) ? 257 : 257):
      case ((Byte.MIN_VALUE <= (byte) 1) ? 258 : 258):
      case ((Byte.MIN_VALUE <= Byte.MAX_VALUE) ? 259 : 259):
      case (((byte) -1 <= Byte.MIN_VALUE) ? 260 : 260):
      case (((byte) -1 <= (byte) -1) ? 261 : 261):
      case (((byte) -1 <= (byte) 0) ? 262 : 262):
      case (((byte) -1 <= (byte) 1) ? 263 : 263):
      case (((byte) -1 <= Byte.MAX_VALUE) ? 264 : 264):
      case (((byte) 0 <= Byte.MIN_VALUE) ? 265 : 265):
      case (((byte) 0 <= (byte) -1) ? 266 : 266):
      case (((byte) 0 <= (byte) 0) ? 267 : 267):
      case (((byte) 0 <= (byte) 1) ? 268 : 268):
      case (((byte) 0 <= Byte.MAX_VALUE) ? 269 : 269):
      case (((byte) 1 <= Byte.MIN_VALUE) ? 270 : 270):
      case (((byte) 1 <= (byte) -1) ? 271 : 271):
      case (((byte) 1 <= (byte) 0) ? 272 : 272):
      case (((byte) 1 <= (byte) 1) ? 273 : 273):
      case (((byte) 1 <= Byte.MAX_VALUE) ? 274 : 274):
      case ((Byte.MAX_VALUE <= Byte.MIN_VALUE) ? 275 : 275):
      case ((Byte.MAX_VALUE <= (byte) -1) ? 276 : 276):
      case ((Byte.MAX_VALUE <= (byte) 0) ? 277 : 277):
      case ((Byte.MAX_VALUE <= (byte) 1) ? 278 : 278):
      case ((Byte.MAX_VALUE <= Byte.MAX_VALUE) ? 279 : 279):
      case ((Byte.MIN_VALUE >= Byte.MIN_VALUE) ? 280 : 280):
      case ((Byte.MIN_VALUE >= (byte) -1) ? 281 : 281):
      case ((Byte.MIN_VALUE >= (byte) 0) ? 282 : 282):
      case ((Byte.MIN_VALUE >= (byte) 1) ? 283 : 283):
      case ((Byte.MIN_VALUE >= Byte.MAX_VALUE) ? 284 : 284):
      case (((byte) -1 >= Byte.MIN_VALUE) ? 285 : 285):
      case (((byte) -1 >= (byte) -1) ? 286 : 286):
      case (((byte) -1 >= (byte) 0) ? 287 : 287):
      case (((byte) -1 >= (byte) 1) ? 288 : 288):
      case (((byte) -1 >= Byte.MAX_VALUE) ? 289 : 289):
      case (((byte) 0 >= Byte.MIN_VALUE) ? 290 : 290):
      case (((byte) 0 >= (byte) -1) ? 291 : 291):
      case (((byte) 0 >= (byte) 0) ? 292 : 292):
      case (((byte) 0 >= (byte) 1) ? 293 : 293):
      case (((byte) 0 >= Byte.MAX_VALUE) ? 294 : 294):
      case (((byte) 1 >= Byte.MIN_VALUE) ? 295 : 295):
      case (((byte) 1 >= (byte) -1) ? 296 : 296):
      case (((byte) 1 >= (byte) 0) ? 297 : 297):
      case (((byte) 1 >= (byte) 1) ? 298 : 298):
      case (((byte) 1 >= Byte.MAX_VALUE) ? 299 : 299):
      case ((Byte.MAX_VALUE >= Byte.MIN_VALUE) ? 300 : 300):
      case ((Byte.MAX_VALUE >= (byte) -1) ? 301 : 301):
      case ((Byte.MAX_VALUE >= (byte) 0) ? 302 : 302):
      case ((Byte.MAX_VALUE >= (byte) 1) ? 303 : 303):
      case ((Byte.MAX_VALUE >= Byte.MAX_VALUE) ? 304 : 304):
      case ((Byte.MIN_VALUE == Byte.MIN_VALUE) ? 305 : 305):
      case ((Byte.MIN_VALUE == (byte) -1) ? 306 : 306):
      case ((Byte.MIN_VALUE == (byte) 0) ? 307 : 307):
      case ((Byte.MIN_VALUE == (byte) 1) ? 308 : 308):
      case ((Byte.MIN_VALUE == Byte.MAX_VALUE) ? 309 : 309):
      case (((byte) -1 == Byte.MIN_VALUE) ? 310 : 310):
      case (((byte) -1 == (byte) -1) ? 311 : 311):
      case (((byte) -1 == (byte) 0) ? 312 : 312):
      case (((byte) -1 == (byte) 1) ? 313 : 313):
      case (((byte) -1 == Byte.MAX_VALUE) ? 314 : 314):
      case (((byte) 0 == Byte.MIN_VALUE) ? 315 : 315):
      case (((byte) 0 == (byte) -1) ? 316 : 316):
      case (((byte) 0 == (byte) 0) ? 317 : 317):
      case (((byte) 0 == (byte) 1) ? 318 : 318):
      case (((byte) 0 == Byte.MAX_VALUE) ? 319 : 319):
      case (((byte) 1 == Byte.MIN_VALUE) ? 320 : 320):
      case (((byte) 1 == (byte) -1) ? 321 : 321):
      case (((byte) 1 == (byte) 0) ? 322 : 322):
      case (((byte) 1 == (byte) 1) ? 323 : 323):
      case (((byte) 1 == Byte.MAX_VALUE) ? 324 : 324):
      case ((Byte.MAX_VALUE == Byte.MIN_VALUE) ? 325 : 325):
      case ((Byte.MAX_VALUE == (byte) -1) ? 326 : 326):
      case ((Byte.MAX_VALUE == (byte) 0) ? 327 : 327):
      case ((Byte.MAX_VALUE == (byte) 1) ? 328 : 328):
      case ((Byte.MAX_VALUE == Byte.MAX_VALUE) ? 329 : 329):
      case ((Byte.MIN_VALUE != Byte.MIN_VALUE) ? 330 : 330):
      case ((Byte.MIN_VALUE != (byte) -1) ? 331 : 331):
      case ((Byte.MIN_VALUE != (byte) 0) ? 332 : 332):
      case ((Byte.MIN_VALUE != (byte) 1) ? 333 : 333):
      case ((Byte.MIN_VALUE != Byte.MAX_VALUE) ? 334 : 334):
      case (((byte) -1 != Byte.MIN_VALUE) ? 335 : 335):
      case (((byte) -1 != (byte) -1) ? 336 : 336):
      case (((byte) -1 != (byte) 0) ? 337 : 337):
      case (((byte) -1 != (byte) 1) ? 338 : 338):
      case (((byte) -1 != Byte.MAX_VALUE) ? 339 : 339):
      case (((byte) 0 != Byte.MIN_VALUE) ? 340 : 340):
      case (((byte) 0 != (byte) -1) ? 341 : 341):
      case (((byte) 0 != (byte) 0) ? 342 : 342):
      case (((byte) 0 != (byte) 1) ? 343 : 343):
      case (((byte) 0 != Byte.MAX_VALUE) ? 344 : 344):
      case (((byte) 1 != Byte.MIN_VALUE) ? 345 : 345):
      case (((byte) 1 != (byte) -1) ? 346 : 346):
      case (((byte) 1 != (byte) 0) ? 347 : 347):
      case (((byte) 1 != (byte) 1) ? 348 : 348):
      case (((byte) 1 != Byte.MAX_VALUE) ? 349 : 349):
      case ((Byte.MAX_VALUE != Byte.MIN_VALUE) ? 350 : 350):
      case ((Byte.MAX_VALUE != (byte) -1) ? 351 : 351):
      case ((Byte.MAX_VALUE != (byte) 0) ? 352 : 352):
      case ((Byte.MAX_VALUE != (byte) 1) ? 353 : 353):
      case ((Byte.MAX_VALUE != Byte.MAX_VALUE) ? 354 : 354):
      case ((((byte) (Byte.MIN_VALUE & Byte.MIN_VALUE)) == 0) ? 355 : 355):
      case ((((byte) (Byte.MIN_VALUE & (byte) -1)) == 0) ? 356 : 356):
      case ((((byte) (Byte.MIN_VALUE & (byte) 0)) == 0) ? 357 : 357):
      case ((((byte) (Byte.MIN_VALUE & (byte) 1)) == 0) ? 358 : 358):
      case ((((byte) (Byte.MIN_VALUE & Byte.MAX_VALUE)) == 0) ? 359 : 359):
      case ((((byte) ((byte) -1 & Byte.MIN_VALUE)) == 0) ? 360 : 360):
      case ((((byte) ((byte) -1 & (byte) -1)) == 0) ? 361 : 361):
      case ((((byte) ((byte) -1 & (byte) 0)) == 0) ? 362 : 362):
      case ((((byte) ((byte) -1 & (byte) 1)) == 0) ? 363 : 363):
      case ((((byte) ((byte) -1 & Byte.MAX_VALUE)) == 0) ? 364 : 364):
      case ((((byte) ((byte) 0 & Byte.MIN_VALUE)) == 0) ? 365 : 365):
      case ((((byte) ((byte) 0 & (byte) -1)) == 0) ? 366 : 366):
      case ((((byte) ((byte) 0 & (byte) 0)) == 0) ? 367 : 367):
      case ((((byte) ((byte) 0 & (byte) 1)) == 0) ? 368 : 368):
      case ((((byte) ((byte) 0 & Byte.MAX_VALUE)) == 0) ? 369 : 369):
      case ((((byte) ((byte) 1 & Byte.MIN_VALUE)) == 0) ? 370 : 370):
      case ((((byte) ((byte) 1 & (byte) -1)) == 0) ? 371 : 371):
      case ((((byte) ((byte) 1 & (byte) 0)) == 0) ? 372 : 372):
      case ((((byte) ((byte) 1 & (byte) 1)) == 0) ? 373 : 373):
      case ((((byte) ((byte) 1 & Byte.MAX_VALUE)) == 0) ? 374 : 374):
      case ((((byte) (Byte.MAX_VALUE & Byte.MIN_VALUE)) == 0) ? 375 : 375):
      case ((((byte) (Byte.MAX_VALUE & (byte) -1)) == 0) ? 376 : 376):
      case ((((byte) (Byte.MAX_VALUE & (byte) 0)) == 0) ? 377 : 377):
      case ((((byte) (Byte.MAX_VALUE & (byte) 1)) == 0) ? 378 : 378):
      case ((((byte) (Byte.MAX_VALUE & Byte.MAX_VALUE)) == 0) ? 379 : 379):
      case ((((byte) (Byte.MIN_VALUE ^ Byte.MIN_VALUE)) == 0) ? 380 : 380):
      case ((((byte) (Byte.MIN_VALUE ^ (byte) -1)) == 0) ? 381 : 381):
      case ((((byte) (Byte.MIN_VALUE ^ (byte) 0)) == 0) ? 382 : 382):
      case ((((byte) (Byte.MIN_VALUE ^ (byte) 1)) == 0) ? 383 : 383):
      case ((((byte) (Byte.MIN_VALUE ^ Byte.MAX_VALUE)) == 0) ? 384 : 384):
      case ((((byte) ((byte) -1 ^ Byte.MIN_VALUE)) == 0) ? 385 : 385):
      case ((((byte) ((byte) -1 ^ (byte) -1)) == 0) ? 386 : 386):
      case ((((byte) ((byte) -1 ^ (byte) 0)) == 0) ? 387 : 387):
      case ((((byte) ((byte) -1 ^ (byte) 1)) == 0) ? 388 : 388):
      case ((((byte) ((byte) -1 ^ Byte.MAX_VALUE)) == 0) ? 389 : 389):
      case ((((byte) ((byte) 0 ^ Byte.MIN_VALUE)) == 0) ? 390 : 390):
      case ((((byte) ((byte) 0 ^ (byte) -1)) == 0) ? 391 : 391):
      case ((((byte) ((byte) 0 ^ (byte) 0)) == 0) ? 392 : 392):
      case ((((byte) ((byte) 0 ^ (byte) 1)) == 0) ? 393 : 393):
      case ((((byte) ((byte) 0 ^ Byte.MAX_VALUE)) == 0) ? 394 : 394):
      case ((((byte) ((byte) 1 ^ Byte.MIN_VALUE)) == 0) ? 395 : 395):
      case ((((byte) ((byte) 1 ^ (byte) -1)) == 0) ? 396 : 396):
      case ((((byte) ((byte) 1 ^ (byte) 0)) == 0) ? 397 : 397):
      case ((((byte) ((byte) 1 ^ (byte) 1)) == 0) ? 398 : 398):
      case ((((byte) ((byte) 1 ^ Byte.MAX_VALUE)) == 0) ? 399 : 399):
      case ((((byte) (Byte.MAX_VALUE ^ Byte.MIN_VALUE)) == 0) ? 400 : 400):
      case ((((byte) (Byte.MAX_VALUE ^ (byte) -1)) == 0) ? 401 : 401):
      case ((((byte) (Byte.MAX_VALUE ^ (byte) 0)) == 0) ? 402 : 402):
      case ((((byte) (Byte.MAX_VALUE ^ (byte) 1)) == 0) ? 403 : 403):
      case ((((byte) (Byte.MAX_VALUE ^ Byte.MAX_VALUE)) == 0) ? 404 : 404):
      case ((((byte) (Byte.MIN_VALUE | Byte.MIN_VALUE)) == 0) ? 405 : 405):
      case ((((byte) (Byte.MIN_VALUE | (byte) -1)) == 0) ? 406 : 406):
      case ((((byte) (Byte.MIN_VALUE | (byte) 0)) == 0) ? 407 : 407):
      case ((((byte) (Byte.MIN_VALUE | (byte) 1)) == 0) ? 408 : 408):
      case ((((byte) (Byte.MIN_VALUE | Byte.MAX_VALUE)) == 0) ? 409 : 409):
      case ((((byte) ((byte) -1 | Byte.MIN_VALUE)) == 0) ? 410 : 410):
      case ((((byte) ((byte) -1 | (byte) -1)) == 0) ? 411 : 411):
      case ((((byte) ((byte) -1 | (byte) 0)) == 0) ? 412 : 412):
      case ((((byte) ((byte) -1 | (byte) 1)) == 0) ? 413 : 413):
      case ((((byte) ((byte) -1 | Byte.MAX_VALUE)) == 0) ? 414 : 414):
      case ((((byte) ((byte) 0 | Byte.MIN_VALUE)) == 0) ? 415 : 415):
      case ((((byte) ((byte) 0 | (byte) -1)) == 0) ? 416 : 416):
      case ((((byte) ((byte) 0 | (byte) 0)) == 0) ? 417 : 417):
      case ((((byte) ((byte) 0 | (byte) 1)) == 0) ? 418 : 418):
      case ((((byte) ((byte) 0 | Byte.MAX_VALUE)) == 0) ? 419 : 419):
      case ((((byte) ((byte) 1 | Byte.MIN_VALUE)) == 0) ? 420 : 420):
      case ((((byte) ((byte) 1 | (byte) -1)) == 0) ? 421 : 421):
      case ((((byte) ((byte) 1 | (byte) 0)) == 0) ? 422 : 422):
      case ((((byte) ((byte) 1 | (byte) 1)) == 0) ? 423 : 423):
      case ((((byte) ((byte) 1 | Byte.MAX_VALUE)) == 0) ? 424 : 424):
      case ((((byte) (Byte.MAX_VALUE | Byte.MIN_VALUE)) == 0) ? 425 : 425):
      case ((((byte) (Byte.MAX_VALUE | (byte) -1)) == 0) ? 426 : 426):
      case ((((byte) (Byte.MAX_VALUE | (byte) 0)) == 0) ? 427 : 427):
      case ((((byte) (Byte.MAX_VALUE | (byte) 1)) == 0) ? 428 : 428):
      case ((((byte) (Byte.MAX_VALUE | Byte.MAX_VALUE)) == 0) ? 429 : 429):
      default:
    }
  }

  // --------
  // short tests
  static short shortPlus(short x) { return (short) + x; }
  static short shortMinus(short x) { return (short) - x; }
  static short shortBitNot(short x) { return (short) ~ x; }
  static short shortTimes(short x, short y) { return (short) (x * y); }
  static short shortDiv(short x, short y) { return (short) (x / y); }
  static short shortRem(short x, short y) { return (short) (x % y); }
  static short shortAdd(short x, short y) { return (short) (x + y); }
  static short shortSub(short x, short y) { return (short) (x - y); }
  static short shortShl(short x, short y) { return (short) (x << y); }
  static short shortShr(short x, short y) { return (short) (x >> y); }
  static short shortUshr(short x, short y) { return (short) (x >>> y); }
  static boolean shortLt(short x, short y) { return x < y; }
  static boolean shortGt(short x, short y) { return x > y; }
  static boolean shortLe(short x, short y) { return x <= y; }
  static boolean shortGe(short x, short y) { return x >= y; }
  static boolean shortEq(short x, short y) { return x == y; }
  static boolean shortNe(short x, short y) { return x != y; }
  static short shortAnd(short x, short y) { return (short) (x & y); }
  static short shortXor(short x, short y) { return (short) (x ^ y); }
  static short shortOr(short x, short y) { return (short) (x | y); }
  static void shortTest() {
    Tester.checkEqual(shortPlus(Short.MIN_VALUE), (short) + Short.MIN_VALUE, "(short) + Short.MIN_VALUE");
    Tester.checkEqual(shortPlus((short) -1), (short) + (short) -1, "(short) + (short) -1");
    Tester.checkEqual(shortPlus((short) 0), (short) + (short) 0, "(short) + (short) 0");
    Tester.checkEqual(shortPlus((short) 1), (short) + (short) 1, "(short) + (short) 1");
    Tester.checkEqual(shortPlus(Short.MAX_VALUE), (short) + Short.MAX_VALUE, "(short) + Short.MAX_VALUE");
    Tester.checkEqual(shortMinus(Short.MIN_VALUE), (short) - Short.MIN_VALUE, "(short) - Short.MIN_VALUE");
    Tester.checkEqual(shortMinus((short) -1), (short) - (short) -1, "(short) - (short) -1");
    Tester.checkEqual(shortMinus((short) 0), (short) - (short) 0, "(short) - (short) 0");
    Tester.checkEqual(shortMinus((short) 1), (short) - (short) 1, "(short) - (short) 1");
    Tester.checkEqual(shortMinus(Short.MAX_VALUE), (short) - Short.MAX_VALUE, "(short) - Short.MAX_VALUE");
    Tester.checkEqual(shortBitNot(Short.MIN_VALUE), (short) ~ Short.MIN_VALUE, "(short) ~ Short.MIN_VALUE");
    Tester.checkEqual(shortBitNot((short) -1), (short) ~ (short) -1, "(short) ~ (short) -1");
    Tester.checkEqual(shortBitNot((short) 0), (short) ~ (short) 0, "(short) ~ (short) 0");
    Tester.checkEqual(shortBitNot((short) 1), (short) ~ (short) 1, "(short) ~ (short) 1");
    Tester.checkEqual(shortBitNot(Short.MAX_VALUE), (short) ~ Short.MAX_VALUE, "(short) ~ Short.MAX_VALUE");
    Tester.checkEqual(shortTimes(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE * Short.MIN_VALUE), "(short) (Short.MIN_VALUE * Short.MIN_VALUE)");
    Tester.checkEqual(shortTimes(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE * (short) -1), "(short) (Short.MIN_VALUE * (short) -1)");
    Tester.checkEqual(shortTimes(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE * (short) 0), "(short) (Short.MIN_VALUE * (short) 0)");
    Tester.checkEqual(shortTimes(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE * (short) 1), "(short) (Short.MIN_VALUE * (short) 1)");
    Tester.checkEqual(shortTimes(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE * Short.MAX_VALUE), "(short) (Short.MIN_VALUE * Short.MAX_VALUE)");
    Tester.checkEqual(shortTimes((short) -1, Short.MIN_VALUE), (short) ((short) -1 * Short.MIN_VALUE), "(short) ((short) -1 * Short.MIN_VALUE)");
    Tester.checkEqual(shortTimes((short) -1, (short) -1), (short) ((short) -1 * (short) -1), "(short) ((short) -1 * (short) -1)");
    Tester.checkEqual(shortTimes((short) -1, (short) 0), (short) ((short) -1 * (short) 0), "(short) ((short) -1 * (short) 0)");
    Tester.checkEqual(shortTimes((short) -1, (short) 1), (short) ((short) -1 * (short) 1), "(short) ((short) -1 * (short) 1)");
    Tester.checkEqual(shortTimes((short) -1, Short.MAX_VALUE), (short) ((short) -1 * Short.MAX_VALUE), "(short) ((short) -1 * Short.MAX_VALUE)");
    Tester.checkEqual(shortTimes((short) 0, Short.MIN_VALUE), (short) ((short) 0 * Short.MIN_VALUE), "(short) ((short) 0 * Short.MIN_VALUE)");
    Tester.checkEqual(shortTimes((short) 0, (short) -1), (short) ((short) 0 * (short) -1), "(short) ((short) 0 * (short) -1)");
    Tester.checkEqual(shortTimes((short) 0, (short) 0), (short) ((short) 0 * (short) 0), "(short) ((short) 0 * (short) 0)");
    Tester.checkEqual(shortTimes((short) 0, (short) 1), (short) ((short) 0 * (short) 1), "(short) ((short) 0 * (short) 1)");
    Tester.checkEqual(shortTimes((short) 0, Short.MAX_VALUE), (short) ((short) 0 * Short.MAX_VALUE), "(short) ((short) 0 * Short.MAX_VALUE)");
    Tester.checkEqual(shortTimes((short) 1, Short.MIN_VALUE), (short) ((short) 1 * Short.MIN_VALUE), "(short) ((short) 1 * Short.MIN_VALUE)");
    Tester.checkEqual(shortTimes((short) 1, (short) -1), (short) ((short) 1 * (short) -1), "(short) ((short) 1 * (short) -1)");
    Tester.checkEqual(shortTimes((short) 1, (short) 0), (short) ((short) 1 * (short) 0), "(short) ((short) 1 * (short) 0)");
    Tester.checkEqual(shortTimes((short) 1, (short) 1), (short) ((short) 1 * (short) 1), "(short) ((short) 1 * (short) 1)");
    Tester.checkEqual(shortTimes((short) 1, Short.MAX_VALUE), (short) ((short) 1 * Short.MAX_VALUE), "(short) ((short) 1 * Short.MAX_VALUE)");
    Tester.checkEqual(shortTimes(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE * Short.MIN_VALUE), "(short) (Short.MAX_VALUE * Short.MIN_VALUE)");
    Tester.checkEqual(shortTimes(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE * (short) -1), "(short) (Short.MAX_VALUE * (short) -1)");
    Tester.checkEqual(shortTimes(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE * (short) 0), "(short) (Short.MAX_VALUE * (short) 0)");
    Tester.checkEqual(shortTimes(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE * (short) 1), "(short) (Short.MAX_VALUE * (short) 1)");
    Tester.checkEqual(shortTimes(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE * Short.MAX_VALUE), "(short) (Short.MAX_VALUE * Short.MAX_VALUE)");
    Tester.checkEqual(shortDiv(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE / Short.MIN_VALUE), "(short) (Short.MIN_VALUE / Short.MIN_VALUE)");
    Tester.checkEqual(shortDiv(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE / (short) -1), "(short) (Short.MIN_VALUE / (short) -1)");
    Tester.checkEqual(shortDiv(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE / (short) 1), "(short) (Short.MIN_VALUE / (short) 1)");
    Tester.checkEqual(shortDiv(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE / Short.MAX_VALUE), "(short) (Short.MIN_VALUE / Short.MAX_VALUE)");
    Tester.checkEqual(shortDiv((short) -1, Short.MIN_VALUE), (short) ((short) -1 / Short.MIN_VALUE), "(short) ((short) -1 / Short.MIN_VALUE)");
    Tester.checkEqual(shortDiv((short) -1, (short) -1), (short) ((short) -1 / (short) -1), "(short) ((short) -1 / (short) -1)");
    Tester.checkEqual(shortDiv((short) -1, (short) 1), (short) ((short) -1 / (short) 1), "(short) ((short) -1 / (short) 1)");
    Tester.checkEqual(shortDiv((short) -1, Short.MAX_VALUE), (short) ((short) -1 / Short.MAX_VALUE), "(short) ((short) -1 / Short.MAX_VALUE)");
    Tester.checkEqual(shortDiv((short) 0, Short.MIN_VALUE), (short) ((short) 0 / Short.MIN_VALUE), "(short) ((short) 0 / Short.MIN_VALUE)");
    Tester.checkEqual(shortDiv((short) 0, (short) -1), (short) ((short) 0 / (short) -1), "(short) ((short) 0 / (short) -1)");
    Tester.checkEqual(shortDiv((short) 0, (short) 1), (short) ((short) 0 / (short) 1), "(short) ((short) 0 / (short) 1)");
    Tester.checkEqual(shortDiv((short) 0, Short.MAX_VALUE), (short) ((short) 0 / Short.MAX_VALUE), "(short) ((short) 0 / Short.MAX_VALUE)");
    Tester.checkEqual(shortDiv((short) 1, Short.MIN_VALUE), (short) ((short) 1 / Short.MIN_VALUE), "(short) ((short) 1 / Short.MIN_VALUE)");
    Tester.checkEqual(shortDiv((short) 1, (short) -1), (short) ((short) 1 / (short) -1), "(short) ((short) 1 / (short) -1)");
    Tester.checkEqual(shortDiv((short) 1, (short) 1), (short) ((short) 1 / (short) 1), "(short) ((short) 1 / (short) 1)");
    Tester.checkEqual(shortDiv((short) 1, Short.MAX_VALUE), (short) ((short) 1 / Short.MAX_VALUE), "(short) ((short) 1 / Short.MAX_VALUE)");
    Tester.checkEqual(shortDiv(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE / Short.MIN_VALUE), "(short) (Short.MAX_VALUE / Short.MIN_VALUE)");
    Tester.checkEqual(shortDiv(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE / (short) -1), "(short) (Short.MAX_VALUE / (short) -1)");
    Tester.checkEqual(shortDiv(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE / (short) 1), "(short) (Short.MAX_VALUE / (short) 1)");
    Tester.checkEqual(shortDiv(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE / Short.MAX_VALUE), "(short) (Short.MAX_VALUE / Short.MAX_VALUE)");
    Tester.checkEqual(shortRem(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE % Short.MIN_VALUE), "(short) (Short.MIN_VALUE % Short.MIN_VALUE)");
    Tester.checkEqual(shortRem(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE % (short) -1), "(short) (Short.MIN_VALUE % (short) -1)");
    Tester.checkEqual(shortRem(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE % (short) 1), "(short) (Short.MIN_VALUE % (short) 1)");
    Tester.checkEqual(shortRem(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE % Short.MAX_VALUE), "(short) (Short.MIN_VALUE % Short.MAX_VALUE)");
    Tester.checkEqual(shortRem((short) -1, Short.MIN_VALUE), (short) ((short) -1 % Short.MIN_VALUE), "(short) ((short) -1 % Short.MIN_VALUE)");
    Tester.checkEqual(shortRem((short) -1, (short) -1), (short) ((short) -1 % (short) -1), "(short) ((short) -1 % (short) -1)");
    Tester.checkEqual(shortRem((short) -1, (short) 1), (short) ((short) -1 % (short) 1), "(short) ((short) -1 % (short) 1)");
    Tester.checkEqual(shortRem((short) -1, Short.MAX_VALUE), (short) ((short) -1 % Short.MAX_VALUE), "(short) ((short) -1 % Short.MAX_VALUE)");
    Tester.checkEqual(shortRem((short) 0, Short.MIN_VALUE), (short) ((short) 0 % Short.MIN_VALUE), "(short) ((short) 0 % Short.MIN_VALUE)");
    Tester.checkEqual(shortRem((short) 0, (short) -1), (short) ((short) 0 % (short) -1), "(short) ((short) 0 % (short) -1)");
    Tester.checkEqual(shortRem((short) 0, (short) 1), (short) ((short) 0 % (short) 1), "(short) ((short) 0 % (short) 1)");
    Tester.checkEqual(shortRem((short) 0, Short.MAX_VALUE), (short) ((short) 0 % Short.MAX_VALUE), "(short) ((short) 0 % Short.MAX_VALUE)");
    Tester.checkEqual(shortRem((short) 1, Short.MIN_VALUE), (short) ((short) 1 % Short.MIN_VALUE), "(short) ((short) 1 % Short.MIN_VALUE)");
    Tester.checkEqual(shortRem((short) 1, (short) -1), (short) ((short) 1 % (short) -1), "(short) ((short) 1 % (short) -1)");
    Tester.checkEqual(shortRem((short) 1, (short) 1), (short) ((short) 1 % (short) 1), "(short) ((short) 1 % (short) 1)");
    Tester.checkEqual(shortRem((short) 1, Short.MAX_VALUE), (short) ((short) 1 % Short.MAX_VALUE), "(short) ((short) 1 % Short.MAX_VALUE)");
    Tester.checkEqual(shortRem(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE % Short.MIN_VALUE), "(short) (Short.MAX_VALUE % Short.MIN_VALUE)");
    Tester.checkEqual(shortRem(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE % (short) -1), "(short) (Short.MAX_VALUE % (short) -1)");
    Tester.checkEqual(shortRem(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE % (short) 1), "(short) (Short.MAX_VALUE % (short) 1)");
    Tester.checkEqual(shortRem(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE % Short.MAX_VALUE), "(short) (Short.MAX_VALUE % Short.MAX_VALUE)");
    Tester.checkEqual(shortAdd(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE + Short.MIN_VALUE), "(short) (Short.MIN_VALUE + Short.MIN_VALUE)");
    Tester.checkEqual(shortAdd(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE + (short) -1), "(short) (Short.MIN_VALUE + (short) -1)");
    Tester.checkEqual(shortAdd(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE + (short) 0), "(short) (Short.MIN_VALUE + (short) 0)");
    Tester.checkEqual(shortAdd(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE + (short) 1), "(short) (Short.MIN_VALUE + (short) 1)");
    Tester.checkEqual(shortAdd(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE + Short.MAX_VALUE), "(short) (Short.MIN_VALUE + Short.MAX_VALUE)");
    Tester.checkEqual(shortAdd((short) -1, Short.MIN_VALUE), (short) ((short) -1 + Short.MIN_VALUE), "(short) ((short) -1 + Short.MIN_VALUE)");
    Tester.checkEqual(shortAdd((short) -1, (short) -1), (short) ((short) -1 + (short) -1), "(short) ((short) -1 + (short) -1)");
    Tester.checkEqual(shortAdd((short) -1, (short) 0), (short) ((short) -1 + (short) 0), "(short) ((short) -1 + (short) 0)");
    Tester.checkEqual(shortAdd((short) -1, (short) 1), (short) ((short) -1 + (short) 1), "(short) ((short) -1 + (short) 1)");
    Tester.checkEqual(shortAdd((short) -1, Short.MAX_VALUE), (short) ((short) -1 + Short.MAX_VALUE), "(short) ((short) -1 + Short.MAX_VALUE)");
    Tester.checkEqual(shortAdd((short) 0, Short.MIN_VALUE), (short) ((short) 0 + Short.MIN_VALUE), "(short) ((short) 0 + Short.MIN_VALUE)");
    Tester.checkEqual(shortAdd((short) 0, (short) -1), (short) ((short) 0 + (short) -1), "(short) ((short) 0 + (short) -1)");
    Tester.checkEqual(shortAdd((short) 0, (short) 0), (short) ((short) 0 + (short) 0), "(short) ((short) 0 + (short) 0)");
    Tester.checkEqual(shortAdd((short) 0, (short) 1), (short) ((short) 0 + (short) 1), "(short) ((short) 0 + (short) 1)");
    Tester.checkEqual(shortAdd((short) 0, Short.MAX_VALUE), (short) ((short) 0 + Short.MAX_VALUE), "(short) ((short) 0 + Short.MAX_VALUE)");
    Tester.checkEqual(shortAdd((short) 1, Short.MIN_VALUE), (short) ((short) 1 + Short.MIN_VALUE), "(short) ((short) 1 + Short.MIN_VALUE)");
    Tester.checkEqual(shortAdd((short) 1, (short) -1), (short) ((short) 1 + (short) -1), "(short) ((short) 1 + (short) -1)");
    Tester.checkEqual(shortAdd((short) 1, (short) 0), (short) ((short) 1 + (short) 0), "(short) ((short) 1 + (short) 0)");
    Tester.checkEqual(shortAdd((short) 1, (short) 1), (short) ((short) 1 + (short) 1), "(short) ((short) 1 + (short) 1)");
    Tester.checkEqual(shortAdd((short) 1, Short.MAX_VALUE), (short) ((short) 1 + Short.MAX_VALUE), "(short) ((short) 1 + Short.MAX_VALUE)");
    Tester.checkEqual(shortAdd(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE + Short.MIN_VALUE), "(short) (Short.MAX_VALUE + Short.MIN_VALUE)");
    Tester.checkEqual(shortAdd(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE + (short) -1), "(short) (Short.MAX_VALUE + (short) -1)");
    Tester.checkEqual(shortAdd(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE + (short) 0), "(short) (Short.MAX_VALUE + (short) 0)");
    Tester.checkEqual(shortAdd(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE + (short) 1), "(short) (Short.MAX_VALUE + (short) 1)");
    Tester.checkEqual(shortAdd(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE + Short.MAX_VALUE), "(short) (Short.MAX_VALUE + Short.MAX_VALUE)");
    Tester.checkEqual(shortSub(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE - Short.MIN_VALUE), "(short) (Short.MIN_VALUE - Short.MIN_VALUE)");
    Tester.checkEqual(shortSub(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE - (short) -1), "(short) (Short.MIN_VALUE - (short) -1)");
    Tester.checkEqual(shortSub(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE - (short) 0), "(short) (Short.MIN_VALUE - (short) 0)");
    Tester.checkEqual(shortSub(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE - (short) 1), "(short) (Short.MIN_VALUE - (short) 1)");
    Tester.checkEqual(shortSub(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE - Short.MAX_VALUE), "(short) (Short.MIN_VALUE - Short.MAX_VALUE)");
    Tester.checkEqual(shortSub((short) -1, Short.MIN_VALUE), (short) ((short) -1 - Short.MIN_VALUE), "(short) ((short) -1 - Short.MIN_VALUE)");
    Tester.checkEqual(shortSub((short) -1, (short) -1), (short) ((short) -1 - (short) -1), "(short) ((short) -1 - (short) -1)");
    Tester.checkEqual(shortSub((short) -1, (short) 0), (short) ((short) -1 - (short) 0), "(short) ((short) -1 - (short) 0)");
    Tester.checkEqual(shortSub((short) -1, (short) 1), (short) ((short) -1 - (short) 1), "(short) ((short) -1 - (short) 1)");
    Tester.checkEqual(shortSub((short) -1, Short.MAX_VALUE), (short) ((short) -1 - Short.MAX_VALUE), "(short) ((short) -1 - Short.MAX_VALUE)");
    Tester.checkEqual(shortSub((short) 0, Short.MIN_VALUE), (short) ((short) 0 - Short.MIN_VALUE), "(short) ((short) 0 - Short.MIN_VALUE)");
    Tester.checkEqual(shortSub((short) 0, (short) -1), (short) ((short) 0 - (short) -1), "(short) ((short) 0 - (short) -1)");
    Tester.checkEqual(shortSub((short) 0, (short) 0), (short) ((short) 0 - (short) 0), "(short) ((short) 0 - (short) 0)");
    Tester.checkEqual(shortSub((short) 0, (short) 1), (short) ((short) 0 - (short) 1), "(short) ((short) 0 - (short) 1)");
    Tester.checkEqual(shortSub((short) 0, Short.MAX_VALUE), (short) ((short) 0 - Short.MAX_VALUE), "(short) ((short) 0 - Short.MAX_VALUE)");
    Tester.checkEqual(shortSub((short) 1, Short.MIN_VALUE), (short) ((short) 1 - Short.MIN_VALUE), "(short) ((short) 1 - Short.MIN_VALUE)");
    Tester.checkEqual(shortSub((short) 1, (short) -1), (short) ((short) 1 - (short) -1), "(short) ((short) 1 - (short) -1)");
    Tester.checkEqual(shortSub((short) 1, (short) 0), (short) ((short) 1 - (short) 0), "(short) ((short) 1 - (short) 0)");
    Tester.checkEqual(shortSub((short) 1, (short) 1), (short) ((short) 1 - (short) 1), "(short) ((short) 1 - (short) 1)");
    Tester.checkEqual(shortSub((short) 1, Short.MAX_VALUE), (short) ((short) 1 - Short.MAX_VALUE), "(short) ((short) 1 - Short.MAX_VALUE)");
    Tester.checkEqual(shortSub(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE - Short.MIN_VALUE), "(short) (Short.MAX_VALUE - Short.MIN_VALUE)");
    Tester.checkEqual(shortSub(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE - (short) -1), "(short) (Short.MAX_VALUE - (short) -1)");
    Tester.checkEqual(shortSub(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE - (short) 0), "(short) (Short.MAX_VALUE - (short) 0)");
    Tester.checkEqual(shortSub(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE - (short) 1), "(short) (Short.MAX_VALUE - (short) 1)");
    Tester.checkEqual(shortSub(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE - Short.MAX_VALUE), "(short) (Short.MAX_VALUE - Short.MAX_VALUE)");
    Tester.checkEqual(shortShl(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE << Short.MIN_VALUE), "(short) (Short.MIN_VALUE << Short.MIN_VALUE)");
    Tester.checkEqual(shortShl(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE << (short) -1), "(short) (Short.MIN_VALUE << (short) -1)");
    Tester.checkEqual(shortShl(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE << (short) 0), "(short) (Short.MIN_VALUE << (short) 0)");
    Tester.checkEqual(shortShl(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE << (short) 1), "(short) (Short.MIN_VALUE << (short) 1)");
    Tester.checkEqual(shortShl(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE << Short.MAX_VALUE), "(short) (Short.MIN_VALUE << Short.MAX_VALUE)");
    Tester.checkEqual(shortShl((short) -1, Short.MIN_VALUE), (short) ((short) -1 << Short.MIN_VALUE), "(short) ((short) -1 << Short.MIN_VALUE)");
    Tester.checkEqual(shortShl((short) -1, (short) -1), (short) ((short) -1 << (short) -1), "(short) ((short) -1 << (short) -1)");
    Tester.checkEqual(shortShl((short) -1, (short) 0), (short) ((short) -1 << (short) 0), "(short) ((short) -1 << (short) 0)");
    Tester.checkEqual(shortShl((short) -1, (short) 1), (short) ((short) -1 << (short) 1), "(short) ((short) -1 << (short) 1)");
    Tester.checkEqual(shortShl((short) -1, Short.MAX_VALUE), (short) ((short) -1 << Short.MAX_VALUE), "(short) ((short) -1 << Short.MAX_VALUE)");
    Tester.checkEqual(shortShl((short) 0, Short.MIN_VALUE), (short) ((short) 0 << Short.MIN_VALUE), "(short) ((short) 0 << Short.MIN_VALUE)");
    Tester.checkEqual(shortShl((short) 0, (short) -1), (short) ((short) 0 << (short) -1), "(short) ((short) 0 << (short) -1)");
    Tester.checkEqual(shortShl((short) 0, (short) 0), (short) ((short) 0 << (short) 0), "(short) ((short) 0 << (short) 0)");
    Tester.checkEqual(shortShl((short) 0, (short) 1), (short) ((short) 0 << (short) 1), "(short) ((short) 0 << (short) 1)");
    Tester.checkEqual(shortShl((short) 0, Short.MAX_VALUE), (short) ((short) 0 << Short.MAX_VALUE), "(short) ((short) 0 << Short.MAX_VALUE)");
    Tester.checkEqual(shortShl((short) 1, Short.MIN_VALUE), (short) ((short) 1 << Short.MIN_VALUE), "(short) ((short) 1 << Short.MIN_VALUE)");
    Tester.checkEqual(shortShl((short) 1, (short) -1), (short) ((short) 1 << (short) -1), "(short) ((short) 1 << (short) -1)");
    Tester.checkEqual(shortShl((short) 1, (short) 0), (short) ((short) 1 << (short) 0), "(short) ((short) 1 << (short) 0)");
    Tester.checkEqual(shortShl((short) 1, (short) 1), (short) ((short) 1 << (short) 1), "(short) ((short) 1 << (short) 1)");
    Tester.checkEqual(shortShl((short) 1, Short.MAX_VALUE), (short) ((short) 1 << Short.MAX_VALUE), "(short) ((short) 1 << Short.MAX_VALUE)");
    Tester.checkEqual(shortShl(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE << Short.MIN_VALUE), "(short) (Short.MAX_VALUE << Short.MIN_VALUE)");
    Tester.checkEqual(shortShl(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE << (short) -1), "(short) (Short.MAX_VALUE << (short) -1)");
    Tester.checkEqual(shortShl(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE << (short) 0), "(short) (Short.MAX_VALUE << (short) 0)");
    Tester.checkEqual(shortShl(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE << (short) 1), "(short) (Short.MAX_VALUE << (short) 1)");
    Tester.checkEqual(shortShl(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE << Short.MAX_VALUE), "(short) (Short.MAX_VALUE << Short.MAX_VALUE)");
    Tester.checkEqual(shortShr(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE >> Short.MIN_VALUE), "(short) (Short.MIN_VALUE >> Short.MIN_VALUE)");
    Tester.checkEqual(shortShr(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE >> (short) -1), "(short) (Short.MIN_VALUE >> (short) -1)");
    Tester.checkEqual(shortShr(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE >> (short) 0), "(short) (Short.MIN_VALUE >> (short) 0)");
    Tester.checkEqual(shortShr(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE >> (short) 1), "(short) (Short.MIN_VALUE >> (short) 1)");
    Tester.checkEqual(shortShr(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE >> Short.MAX_VALUE), "(short) (Short.MIN_VALUE >> Short.MAX_VALUE)");
    Tester.checkEqual(shortShr((short) -1, Short.MIN_VALUE), (short) ((short) -1 >> Short.MIN_VALUE), "(short) ((short) -1 >> Short.MIN_VALUE)");
    Tester.checkEqual(shortShr((short) -1, (short) -1), (short) ((short) -1 >> (short) -1), "(short) ((short) -1 >> (short) -1)");
    Tester.checkEqual(shortShr((short) -1, (short) 0), (short) ((short) -1 >> (short) 0), "(short) ((short) -1 >> (short) 0)");
    Tester.checkEqual(shortShr((short) -1, (short) 1), (short) ((short) -1 >> (short) 1), "(short) ((short) -1 >> (short) 1)");
    Tester.checkEqual(shortShr((short) -1, Short.MAX_VALUE), (short) ((short) -1 >> Short.MAX_VALUE), "(short) ((short) -1 >> Short.MAX_VALUE)");
    Tester.checkEqual(shortShr((short) 0, Short.MIN_VALUE), (short) ((short) 0 >> Short.MIN_VALUE), "(short) ((short) 0 >> Short.MIN_VALUE)");
    Tester.checkEqual(shortShr((short) 0, (short) -1), (short) ((short) 0 >> (short) -1), "(short) ((short) 0 >> (short) -1)");
    Tester.checkEqual(shortShr((short) 0, (short) 0), (short) ((short) 0 >> (short) 0), "(short) ((short) 0 >> (short) 0)");
    Tester.checkEqual(shortShr((short) 0, (short) 1), (short) ((short) 0 >> (short) 1), "(short) ((short) 0 >> (short) 1)");
    Tester.checkEqual(shortShr((short) 0, Short.MAX_VALUE), (short) ((short) 0 >> Short.MAX_VALUE), "(short) ((short) 0 >> Short.MAX_VALUE)");
    Tester.checkEqual(shortShr((short) 1, Short.MIN_VALUE), (short) ((short) 1 >> Short.MIN_VALUE), "(short) ((short) 1 >> Short.MIN_VALUE)");
    Tester.checkEqual(shortShr((short) 1, (short) -1), (short) ((short) 1 >> (short) -1), "(short) ((short) 1 >> (short) -1)");
    Tester.checkEqual(shortShr((short) 1, (short) 0), (short) ((short) 1 >> (short) 0), "(short) ((short) 1 >> (short) 0)");
    Tester.checkEqual(shortShr((short) 1, (short) 1), (short) ((short) 1 >> (short) 1), "(short) ((short) 1 >> (short) 1)");
    Tester.checkEqual(shortShr((short) 1, Short.MAX_VALUE), (short) ((short) 1 >> Short.MAX_VALUE), "(short) ((short) 1 >> Short.MAX_VALUE)");
    Tester.checkEqual(shortShr(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE >> Short.MIN_VALUE), "(short) (Short.MAX_VALUE >> Short.MIN_VALUE)");
    Tester.checkEqual(shortShr(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE >> (short) -1), "(short) (Short.MAX_VALUE >> (short) -1)");
    Tester.checkEqual(shortShr(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE >> (short) 0), "(short) (Short.MAX_VALUE >> (short) 0)");
    Tester.checkEqual(shortShr(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE >> (short) 1), "(short) (Short.MAX_VALUE >> (short) 1)");
    Tester.checkEqual(shortShr(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE >> Short.MAX_VALUE), "(short) (Short.MAX_VALUE >> Short.MAX_VALUE)");
    Tester.checkEqual(shortUshr(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE >>> Short.MIN_VALUE), "(short) (Short.MIN_VALUE >>> Short.MIN_VALUE)");
    Tester.checkEqual(shortUshr(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE >>> (short) -1), "(short) (Short.MIN_VALUE >>> (short) -1)");
    Tester.checkEqual(shortUshr(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE >>> (short) 0), "(short) (Short.MIN_VALUE >>> (short) 0)");
    Tester.checkEqual(shortUshr(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE >>> (short) 1), "(short) (Short.MIN_VALUE >>> (short) 1)");
    Tester.checkEqual(shortUshr(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE >>> Short.MAX_VALUE), "(short) (Short.MIN_VALUE >>> Short.MAX_VALUE)");
    Tester.checkEqual(shortUshr((short) -1, Short.MIN_VALUE), (short) ((short) -1 >>> Short.MIN_VALUE), "(short) ((short) -1 >>> Short.MIN_VALUE)");
    Tester.checkEqual(shortUshr((short) -1, (short) -1), (short) ((short) -1 >>> (short) -1), "(short) ((short) -1 >>> (short) -1)");
    Tester.checkEqual(shortUshr((short) -1, (short) 0), (short) ((short) -1 >>> (short) 0), "(short) ((short) -1 >>> (short) 0)");
    Tester.checkEqual(shortUshr((short) -1, (short) 1), (short) ((short) -1 >>> (short) 1), "(short) ((short) -1 >>> (short) 1)");
    Tester.checkEqual(shortUshr((short) -1, Short.MAX_VALUE), (short) ((short) -1 >>> Short.MAX_VALUE), "(short) ((short) -1 >>> Short.MAX_VALUE)");
    Tester.checkEqual(shortUshr((short) 0, Short.MIN_VALUE), (short) ((short) 0 >>> Short.MIN_VALUE), "(short) ((short) 0 >>> Short.MIN_VALUE)");
    Tester.checkEqual(shortUshr((short) 0, (short) -1), (short) ((short) 0 >>> (short) -1), "(short) ((short) 0 >>> (short) -1)");
    Tester.checkEqual(shortUshr((short) 0, (short) 0), (short) ((short) 0 >>> (short) 0), "(short) ((short) 0 >>> (short) 0)");
    Tester.checkEqual(shortUshr((short) 0, (short) 1), (short) ((short) 0 >>> (short) 1), "(short) ((short) 0 >>> (short) 1)");
    Tester.checkEqual(shortUshr((short) 0, Short.MAX_VALUE), (short) ((short) 0 >>> Short.MAX_VALUE), "(short) ((short) 0 >>> Short.MAX_VALUE)");
    Tester.checkEqual(shortUshr((short) 1, Short.MIN_VALUE), (short) ((short) 1 >>> Short.MIN_VALUE), "(short) ((short) 1 >>> Short.MIN_VALUE)");
    Tester.checkEqual(shortUshr((short) 1, (short) -1), (short) ((short) 1 >>> (short) -1), "(short) ((short) 1 >>> (short) -1)");
    Tester.checkEqual(shortUshr((short) 1, (short) 0), (short) ((short) 1 >>> (short) 0), "(short) ((short) 1 >>> (short) 0)");
    Tester.checkEqual(shortUshr((short) 1, (short) 1), (short) ((short) 1 >>> (short) 1), "(short) ((short) 1 >>> (short) 1)");
    Tester.checkEqual(shortUshr((short) 1, Short.MAX_VALUE), (short) ((short) 1 >>> Short.MAX_VALUE), "(short) ((short) 1 >>> Short.MAX_VALUE)");
    Tester.checkEqual(shortUshr(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE >>> Short.MIN_VALUE), "(short) (Short.MAX_VALUE >>> Short.MIN_VALUE)");
    Tester.checkEqual(shortUshr(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE >>> (short) -1), "(short) (Short.MAX_VALUE >>> (short) -1)");
    Tester.checkEqual(shortUshr(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE >>> (short) 0), "(short) (Short.MAX_VALUE >>> (short) 0)");
    Tester.checkEqual(shortUshr(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE >>> (short) 1), "(short) (Short.MAX_VALUE >>> (short) 1)");
    Tester.checkEqual(shortUshr(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE >>> Short.MAX_VALUE), "(short) (Short.MAX_VALUE >>> Short.MAX_VALUE)");
    Tester.checkEqual(shortLt(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE < Short.MIN_VALUE, "Short.MIN_VALUE < Short.MIN_VALUE");
    Tester.checkEqual(shortLt(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE < (short) -1, "Short.MIN_VALUE < (short) -1");
    Tester.checkEqual(shortLt(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE < (short) 0, "Short.MIN_VALUE < (short) 0");
    Tester.checkEqual(shortLt(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE < (short) 1, "Short.MIN_VALUE < (short) 1");
    Tester.checkEqual(shortLt(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE < Short.MAX_VALUE, "Short.MIN_VALUE < Short.MAX_VALUE");
    Tester.checkEqual(shortLt((short) -1, Short.MIN_VALUE), (short) -1 < Short.MIN_VALUE, "(short) -1 < Short.MIN_VALUE");
    Tester.checkEqual(shortLt((short) -1, (short) -1), (short) -1 < (short) -1, "(short) -1 < (short) -1");
    Tester.checkEqual(shortLt((short) -1, (short) 0), (short) -1 < (short) 0, "(short) -1 < (short) 0");
    Tester.checkEqual(shortLt((short) -1, (short) 1), (short) -1 < (short) 1, "(short) -1 < (short) 1");
    Tester.checkEqual(shortLt((short) -1, Short.MAX_VALUE), (short) -1 < Short.MAX_VALUE, "(short) -1 < Short.MAX_VALUE");
    Tester.checkEqual(shortLt((short) 0, Short.MIN_VALUE), (short) 0 < Short.MIN_VALUE, "(short) 0 < Short.MIN_VALUE");
    Tester.checkEqual(shortLt((short) 0, (short) -1), (short) 0 < (short) -1, "(short) 0 < (short) -1");
    Tester.checkEqual(shortLt((short) 0, (short) 0), (short) 0 < (short) 0, "(short) 0 < (short) 0");
    Tester.checkEqual(shortLt((short) 0, (short) 1), (short) 0 < (short) 1, "(short) 0 < (short) 1");
    Tester.checkEqual(shortLt((short) 0, Short.MAX_VALUE), (short) 0 < Short.MAX_VALUE, "(short) 0 < Short.MAX_VALUE");
    Tester.checkEqual(shortLt((short) 1, Short.MIN_VALUE), (short) 1 < Short.MIN_VALUE, "(short) 1 < Short.MIN_VALUE");
    Tester.checkEqual(shortLt((short) 1, (short) -1), (short) 1 < (short) -1, "(short) 1 < (short) -1");
    Tester.checkEqual(shortLt((short) 1, (short) 0), (short) 1 < (short) 0, "(short) 1 < (short) 0");
    Tester.checkEqual(shortLt((short) 1, (short) 1), (short) 1 < (short) 1, "(short) 1 < (short) 1");
    Tester.checkEqual(shortLt((short) 1, Short.MAX_VALUE), (short) 1 < Short.MAX_VALUE, "(short) 1 < Short.MAX_VALUE");
    Tester.checkEqual(shortLt(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE < Short.MIN_VALUE, "Short.MAX_VALUE < Short.MIN_VALUE");
    Tester.checkEqual(shortLt(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE < (short) -1, "Short.MAX_VALUE < (short) -1");
    Tester.checkEqual(shortLt(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE < (short) 0, "Short.MAX_VALUE < (short) 0");
    Tester.checkEqual(shortLt(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE < (short) 1, "Short.MAX_VALUE < (short) 1");
    Tester.checkEqual(shortLt(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE < Short.MAX_VALUE, "Short.MAX_VALUE < Short.MAX_VALUE");
    Tester.checkEqual(shortGt(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE > Short.MIN_VALUE, "Short.MIN_VALUE > Short.MIN_VALUE");
    Tester.checkEqual(shortGt(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE > (short) -1, "Short.MIN_VALUE > (short) -1");
    Tester.checkEqual(shortGt(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE > (short) 0, "Short.MIN_VALUE > (short) 0");
    Tester.checkEqual(shortGt(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE > (short) 1, "Short.MIN_VALUE > (short) 1");
    Tester.checkEqual(shortGt(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE > Short.MAX_VALUE, "Short.MIN_VALUE > Short.MAX_VALUE");
    Tester.checkEqual(shortGt((short) -1, Short.MIN_VALUE), (short) -1 > Short.MIN_VALUE, "(short) -1 > Short.MIN_VALUE");
    Tester.checkEqual(shortGt((short) -1, (short) -1), (short) -1 > (short) -1, "(short) -1 > (short) -1");
    Tester.checkEqual(shortGt((short) -1, (short) 0), (short) -1 > (short) 0, "(short) -1 > (short) 0");
    Tester.checkEqual(shortGt((short) -1, (short) 1), (short) -1 > (short) 1, "(short) -1 > (short) 1");
    Tester.checkEqual(shortGt((short) -1, Short.MAX_VALUE), (short) -1 > Short.MAX_VALUE, "(short) -1 > Short.MAX_VALUE");
    Tester.checkEqual(shortGt((short) 0, Short.MIN_VALUE), (short) 0 > Short.MIN_VALUE, "(short) 0 > Short.MIN_VALUE");
    Tester.checkEqual(shortGt((short) 0, (short) -1), (short) 0 > (short) -1, "(short) 0 > (short) -1");
    Tester.checkEqual(shortGt((short) 0, (short) 0), (short) 0 > (short) 0, "(short) 0 > (short) 0");
    Tester.checkEqual(shortGt((short) 0, (short) 1), (short) 0 > (short) 1, "(short) 0 > (short) 1");
    Tester.checkEqual(shortGt((short) 0, Short.MAX_VALUE), (short) 0 > Short.MAX_VALUE, "(short) 0 > Short.MAX_VALUE");
    Tester.checkEqual(shortGt((short) 1, Short.MIN_VALUE), (short) 1 > Short.MIN_VALUE, "(short) 1 > Short.MIN_VALUE");
    Tester.checkEqual(shortGt((short) 1, (short) -1), (short) 1 > (short) -1, "(short) 1 > (short) -1");
    Tester.checkEqual(shortGt((short) 1, (short) 0), (short) 1 > (short) 0, "(short) 1 > (short) 0");
    Tester.checkEqual(shortGt((short) 1, (short) 1), (short) 1 > (short) 1, "(short) 1 > (short) 1");
    Tester.checkEqual(shortGt((short) 1, Short.MAX_VALUE), (short) 1 > Short.MAX_VALUE, "(short) 1 > Short.MAX_VALUE");
    Tester.checkEqual(shortGt(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE > Short.MIN_VALUE, "Short.MAX_VALUE > Short.MIN_VALUE");
    Tester.checkEqual(shortGt(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE > (short) -1, "Short.MAX_VALUE > (short) -1");
    Tester.checkEqual(shortGt(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE > (short) 0, "Short.MAX_VALUE > (short) 0");
    Tester.checkEqual(shortGt(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE > (short) 1, "Short.MAX_VALUE > (short) 1");
    Tester.checkEqual(shortGt(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE > Short.MAX_VALUE, "Short.MAX_VALUE > Short.MAX_VALUE");
    Tester.checkEqual(shortLe(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE <= Short.MIN_VALUE, "Short.MIN_VALUE <= Short.MIN_VALUE");
    Tester.checkEqual(shortLe(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE <= (short) -1, "Short.MIN_VALUE <= (short) -1");
    Tester.checkEqual(shortLe(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE <= (short) 0, "Short.MIN_VALUE <= (short) 0");
    Tester.checkEqual(shortLe(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE <= (short) 1, "Short.MIN_VALUE <= (short) 1");
    Tester.checkEqual(shortLe(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE <= Short.MAX_VALUE, "Short.MIN_VALUE <= Short.MAX_VALUE");
    Tester.checkEqual(shortLe((short) -1, Short.MIN_VALUE), (short) -1 <= Short.MIN_VALUE, "(short) -1 <= Short.MIN_VALUE");
    Tester.checkEqual(shortLe((short) -1, (short) -1), (short) -1 <= (short) -1, "(short) -1 <= (short) -1");
    Tester.checkEqual(shortLe((short) -1, (short) 0), (short) -1 <= (short) 0, "(short) -1 <= (short) 0");
    Tester.checkEqual(shortLe((short) -1, (short) 1), (short) -1 <= (short) 1, "(short) -1 <= (short) 1");
    Tester.checkEqual(shortLe((short) -1, Short.MAX_VALUE), (short) -1 <= Short.MAX_VALUE, "(short) -1 <= Short.MAX_VALUE");
    Tester.checkEqual(shortLe((short) 0, Short.MIN_VALUE), (short) 0 <= Short.MIN_VALUE, "(short) 0 <= Short.MIN_VALUE");
    Tester.checkEqual(shortLe((short) 0, (short) -1), (short) 0 <= (short) -1, "(short) 0 <= (short) -1");
    Tester.checkEqual(shortLe((short) 0, (short) 0), (short) 0 <= (short) 0, "(short) 0 <= (short) 0");
    Tester.checkEqual(shortLe((short) 0, (short) 1), (short) 0 <= (short) 1, "(short) 0 <= (short) 1");
    Tester.checkEqual(shortLe((short) 0, Short.MAX_VALUE), (short) 0 <= Short.MAX_VALUE, "(short) 0 <= Short.MAX_VALUE");
    Tester.checkEqual(shortLe((short) 1, Short.MIN_VALUE), (short) 1 <= Short.MIN_VALUE, "(short) 1 <= Short.MIN_VALUE");
    Tester.checkEqual(shortLe((short) 1, (short) -1), (short) 1 <= (short) -1, "(short) 1 <= (short) -1");
    Tester.checkEqual(shortLe((short) 1, (short) 0), (short) 1 <= (short) 0, "(short) 1 <= (short) 0");
    Tester.checkEqual(shortLe((short) 1, (short) 1), (short) 1 <= (short) 1, "(short) 1 <= (short) 1");
    Tester.checkEqual(shortLe((short) 1, Short.MAX_VALUE), (short) 1 <= Short.MAX_VALUE, "(short) 1 <= Short.MAX_VALUE");
    Tester.checkEqual(shortLe(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE <= Short.MIN_VALUE, "Short.MAX_VALUE <= Short.MIN_VALUE");
    Tester.checkEqual(shortLe(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE <= (short) -1, "Short.MAX_VALUE <= (short) -1");
    Tester.checkEqual(shortLe(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE <= (short) 0, "Short.MAX_VALUE <= (short) 0");
    Tester.checkEqual(shortLe(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE <= (short) 1, "Short.MAX_VALUE <= (short) 1");
    Tester.checkEqual(shortLe(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE <= Short.MAX_VALUE, "Short.MAX_VALUE <= Short.MAX_VALUE");
    Tester.checkEqual(shortGe(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE >= Short.MIN_VALUE, "Short.MIN_VALUE >= Short.MIN_VALUE");
    Tester.checkEqual(shortGe(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE >= (short) -1, "Short.MIN_VALUE >= (short) -1");
    Tester.checkEqual(shortGe(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE >= (short) 0, "Short.MIN_VALUE >= (short) 0");
    Tester.checkEqual(shortGe(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE >= (short) 1, "Short.MIN_VALUE >= (short) 1");
    Tester.checkEqual(shortGe(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE >= Short.MAX_VALUE, "Short.MIN_VALUE >= Short.MAX_VALUE");
    Tester.checkEqual(shortGe((short) -1, Short.MIN_VALUE), (short) -1 >= Short.MIN_VALUE, "(short) -1 >= Short.MIN_VALUE");
    Tester.checkEqual(shortGe((short) -1, (short) -1), (short) -1 >= (short) -1, "(short) -1 >= (short) -1");
    Tester.checkEqual(shortGe((short) -1, (short) 0), (short) -1 >= (short) 0, "(short) -1 >= (short) 0");
    Tester.checkEqual(shortGe((short) -1, (short) 1), (short) -1 >= (short) 1, "(short) -1 >= (short) 1");
    Tester.checkEqual(shortGe((short) -1, Short.MAX_VALUE), (short) -1 >= Short.MAX_VALUE, "(short) -1 >= Short.MAX_VALUE");
    Tester.checkEqual(shortGe((short) 0, Short.MIN_VALUE), (short) 0 >= Short.MIN_VALUE, "(short) 0 >= Short.MIN_VALUE");
    Tester.checkEqual(shortGe((short) 0, (short) -1), (short) 0 >= (short) -1, "(short) 0 >= (short) -1");
    Tester.checkEqual(shortGe((short) 0, (short) 0), (short) 0 >= (short) 0, "(short) 0 >= (short) 0");
    Tester.checkEqual(shortGe((short) 0, (short) 1), (short) 0 >= (short) 1, "(short) 0 >= (short) 1");
    Tester.checkEqual(shortGe((short) 0, Short.MAX_VALUE), (short) 0 >= Short.MAX_VALUE, "(short) 0 >= Short.MAX_VALUE");
    Tester.checkEqual(shortGe((short) 1, Short.MIN_VALUE), (short) 1 >= Short.MIN_VALUE, "(short) 1 >= Short.MIN_VALUE");
    Tester.checkEqual(shortGe((short) 1, (short) -1), (short) 1 >= (short) -1, "(short) 1 >= (short) -1");
    Tester.checkEqual(shortGe((short) 1, (short) 0), (short) 1 >= (short) 0, "(short) 1 >= (short) 0");
    Tester.checkEqual(shortGe((short) 1, (short) 1), (short) 1 >= (short) 1, "(short) 1 >= (short) 1");
    Tester.checkEqual(shortGe((short) 1, Short.MAX_VALUE), (short) 1 >= Short.MAX_VALUE, "(short) 1 >= Short.MAX_VALUE");
    Tester.checkEqual(shortGe(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE >= Short.MIN_VALUE, "Short.MAX_VALUE >= Short.MIN_VALUE");
    Tester.checkEqual(shortGe(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE >= (short) -1, "Short.MAX_VALUE >= (short) -1");
    Tester.checkEqual(shortGe(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE >= (short) 0, "Short.MAX_VALUE >= (short) 0");
    Tester.checkEqual(shortGe(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE >= (short) 1, "Short.MAX_VALUE >= (short) 1");
    Tester.checkEqual(shortGe(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE >= Short.MAX_VALUE, "Short.MAX_VALUE >= Short.MAX_VALUE");
    Tester.checkEqual(shortEq(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE == Short.MIN_VALUE, "Short.MIN_VALUE == Short.MIN_VALUE");
    Tester.checkEqual(shortEq(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE == (short) -1, "Short.MIN_VALUE == (short) -1");
    Tester.checkEqual(shortEq(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE == (short) 0, "Short.MIN_VALUE == (short) 0");
    Tester.checkEqual(shortEq(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE == (short) 1, "Short.MIN_VALUE == (short) 1");
    Tester.checkEqual(shortEq(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE == Short.MAX_VALUE, "Short.MIN_VALUE == Short.MAX_VALUE");
    Tester.checkEqual(shortEq((short) -1, Short.MIN_VALUE), (short) -1 == Short.MIN_VALUE, "(short) -1 == Short.MIN_VALUE");
    Tester.checkEqual(shortEq((short) -1, (short) -1), (short) -1 == (short) -1, "(short) -1 == (short) -1");
    Tester.checkEqual(shortEq((short) -1, (short) 0), (short) -1 == (short) 0, "(short) -1 == (short) 0");
    Tester.checkEqual(shortEq((short) -1, (short) 1), (short) -1 == (short) 1, "(short) -1 == (short) 1");
    Tester.checkEqual(shortEq((short) -1, Short.MAX_VALUE), (short) -1 == Short.MAX_VALUE, "(short) -1 == Short.MAX_VALUE");
    Tester.checkEqual(shortEq((short) 0, Short.MIN_VALUE), (short) 0 == Short.MIN_VALUE, "(short) 0 == Short.MIN_VALUE");
    Tester.checkEqual(shortEq((short) 0, (short) -1), (short) 0 == (short) -1, "(short) 0 == (short) -1");
    Tester.checkEqual(shortEq((short) 0, (short) 0), (short) 0 == (short) 0, "(short) 0 == (short) 0");
    Tester.checkEqual(shortEq((short) 0, (short) 1), (short) 0 == (short) 1, "(short) 0 == (short) 1");
    Tester.checkEqual(shortEq((short) 0, Short.MAX_VALUE), (short) 0 == Short.MAX_VALUE, "(short) 0 == Short.MAX_VALUE");
    Tester.checkEqual(shortEq((short) 1, Short.MIN_VALUE), (short) 1 == Short.MIN_VALUE, "(short) 1 == Short.MIN_VALUE");
    Tester.checkEqual(shortEq((short) 1, (short) -1), (short) 1 == (short) -1, "(short) 1 == (short) -1");
    Tester.checkEqual(shortEq((short) 1, (short) 0), (short) 1 == (short) 0, "(short) 1 == (short) 0");
    Tester.checkEqual(shortEq((short) 1, (short) 1), (short) 1 == (short) 1, "(short) 1 == (short) 1");
    Tester.checkEqual(shortEq((short) 1, Short.MAX_VALUE), (short) 1 == Short.MAX_VALUE, "(short) 1 == Short.MAX_VALUE");
    Tester.checkEqual(shortEq(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE == Short.MIN_VALUE, "Short.MAX_VALUE == Short.MIN_VALUE");
    Tester.checkEqual(shortEq(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE == (short) -1, "Short.MAX_VALUE == (short) -1");
    Tester.checkEqual(shortEq(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE == (short) 0, "Short.MAX_VALUE == (short) 0");
    Tester.checkEqual(shortEq(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE == (short) 1, "Short.MAX_VALUE == (short) 1");
    Tester.checkEqual(shortEq(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE == Short.MAX_VALUE, "Short.MAX_VALUE == Short.MAX_VALUE");
    Tester.checkEqual(shortNe(Short.MIN_VALUE, Short.MIN_VALUE), Short.MIN_VALUE != Short.MIN_VALUE, "Short.MIN_VALUE != Short.MIN_VALUE");
    Tester.checkEqual(shortNe(Short.MIN_VALUE, (short) -1), Short.MIN_VALUE != (short) -1, "Short.MIN_VALUE != (short) -1");
    Tester.checkEqual(shortNe(Short.MIN_VALUE, (short) 0), Short.MIN_VALUE != (short) 0, "Short.MIN_VALUE != (short) 0");
    Tester.checkEqual(shortNe(Short.MIN_VALUE, (short) 1), Short.MIN_VALUE != (short) 1, "Short.MIN_VALUE != (short) 1");
    Tester.checkEqual(shortNe(Short.MIN_VALUE, Short.MAX_VALUE), Short.MIN_VALUE != Short.MAX_VALUE, "Short.MIN_VALUE != Short.MAX_VALUE");
    Tester.checkEqual(shortNe((short) -1, Short.MIN_VALUE), (short) -1 != Short.MIN_VALUE, "(short) -1 != Short.MIN_VALUE");
    Tester.checkEqual(shortNe((short) -1, (short) -1), (short) -1 != (short) -1, "(short) -1 != (short) -1");
    Tester.checkEqual(shortNe((short) -1, (short) 0), (short) -1 != (short) 0, "(short) -1 != (short) 0");
    Tester.checkEqual(shortNe((short) -1, (short) 1), (short) -1 != (short) 1, "(short) -1 != (short) 1");
    Tester.checkEqual(shortNe((short) -1, Short.MAX_VALUE), (short) -1 != Short.MAX_VALUE, "(short) -1 != Short.MAX_VALUE");
    Tester.checkEqual(shortNe((short) 0, Short.MIN_VALUE), (short) 0 != Short.MIN_VALUE, "(short) 0 != Short.MIN_VALUE");
    Tester.checkEqual(shortNe((short) 0, (short) -1), (short) 0 != (short) -1, "(short) 0 != (short) -1");
    Tester.checkEqual(shortNe((short) 0, (short) 0), (short) 0 != (short) 0, "(short) 0 != (short) 0");
    Tester.checkEqual(shortNe((short) 0, (short) 1), (short) 0 != (short) 1, "(short) 0 != (short) 1");
    Tester.checkEqual(shortNe((short) 0, Short.MAX_VALUE), (short) 0 != Short.MAX_VALUE, "(short) 0 != Short.MAX_VALUE");
    Tester.checkEqual(shortNe((short) 1, Short.MIN_VALUE), (short) 1 != Short.MIN_VALUE, "(short) 1 != Short.MIN_VALUE");
    Tester.checkEqual(shortNe((short) 1, (short) -1), (short) 1 != (short) -1, "(short) 1 != (short) -1");
    Tester.checkEqual(shortNe((short) 1, (short) 0), (short) 1 != (short) 0, "(short) 1 != (short) 0");
    Tester.checkEqual(shortNe((short) 1, (short) 1), (short) 1 != (short) 1, "(short) 1 != (short) 1");
    Tester.checkEqual(shortNe((short) 1, Short.MAX_VALUE), (short) 1 != Short.MAX_VALUE, "(short) 1 != Short.MAX_VALUE");
    Tester.checkEqual(shortNe(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE != Short.MIN_VALUE, "Short.MAX_VALUE != Short.MIN_VALUE");
    Tester.checkEqual(shortNe(Short.MAX_VALUE, (short) -1), Short.MAX_VALUE != (short) -1, "Short.MAX_VALUE != (short) -1");
    Tester.checkEqual(shortNe(Short.MAX_VALUE, (short) 0), Short.MAX_VALUE != (short) 0, "Short.MAX_VALUE != (short) 0");
    Tester.checkEqual(shortNe(Short.MAX_VALUE, (short) 1), Short.MAX_VALUE != (short) 1, "Short.MAX_VALUE != (short) 1");
    Tester.checkEqual(shortNe(Short.MAX_VALUE, Short.MAX_VALUE), Short.MAX_VALUE != Short.MAX_VALUE, "Short.MAX_VALUE != Short.MAX_VALUE");
    Tester.checkEqual(shortAnd(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE & Short.MIN_VALUE), "(short) (Short.MIN_VALUE & Short.MIN_VALUE)");
    Tester.checkEqual(shortAnd(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE & (short) -1), "(short) (Short.MIN_VALUE & (short) -1)");
    Tester.checkEqual(shortAnd(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE & (short) 0), "(short) (Short.MIN_VALUE & (short) 0)");
    Tester.checkEqual(shortAnd(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE & (short) 1), "(short) (Short.MIN_VALUE & (short) 1)");
    Tester.checkEqual(shortAnd(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE & Short.MAX_VALUE), "(short) (Short.MIN_VALUE & Short.MAX_VALUE)");
    Tester.checkEqual(shortAnd((short) -1, Short.MIN_VALUE), (short) ((short) -1 & Short.MIN_VALUE), "(short) ((short) -1 & Short.MIN_VALUE)");
    Tester.checkEqual(shortAnd((short) -1, (short) -1), (short) ((short) -1 & (short) -1), "(short) ((short) -1 & (short) -1)");
    Tester.checkEqual(shortAnd((short) -1, (short) 0), (short) ((short) -1 & (short) 0), "(short) ((short) -1 & (short) 0)");
    Tester.checkEqual(shortAnd((short) -1, (short) 1), (short) ((short) -1 & (short) 1), "(short) ((short) -1 & (short) 1)");
    Tester.checkEqual(shortAnd((short) -1, Short.MAX_VALUE), (short) ((short) -1 & Short.MAX_VALUE), "(short) ((short) -1 & Short.MAX_VALUE)");
    Tester.checkEqual(shortAnd((short) 0, Short.MIN_VALUE), (short) ((short) 0 & Short.MIN_VALUE), "(short) ((short) 0 & Short.MIN_VALUE)");
    Tester.checkEqual(shortAnd((short) 0, (short) -1), (short) ((short) 0 & (short) -1), "(short) ((short) 0 & (short) -1)");
    Tester.checkEqual(shortAnd((short) 0, (short) 0), (short) ((short) 0 & (short) 0), "(short) ((short) 0 & (short) 0)");
    Tester.checkEqual(shortAnd((short) 0, (short) 1), (short) ((short) 0 & (short) 1), "(short) ((short) 0 & (short) 1)");
    Tester.checkEqual(shortAnd((short) 0, Short.MAX_VALUE), (short) ((short) 0 & Short.MAX_VALUE), "(short) ((short) 0 & Short.MAX_VALUE)");
    Tester.checkEqual(shortAnd((short) 1, Short.MIN_VALUE), (short) ((short) 1 & Short.MIN_VALUE), "(short) ((short) 1 & Short.MIN_VALUE)");
    Tester.checkEqual(shortAnd((short) 1, (short) -1), (short) ((short) 1 & (short) -1), "(short) ((short) 1 & (short) -1)");
    Tester.checkEqual(shortAnd((short) 1, (short) 0), (short) ((short) 1 & (short) 0), "(short) ((short) 1 & (short) 0)");
    Tester.checkEqual(shortAnd((short) 1, (short) 1), (short) ((short) 1 & (short) 1), "(short) ((short) 1 & (short) 1)");
    Tester.checkEqual(shortAnd((short) 1, Short.MAX_VALUE), (short) ((short) 1 & Short.MAX_VALUE), "(short) ((short) 1 & Short.MAX_VALUE)");
    Tester.checkEqual(shortAnd(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE & Short.MIN_VALUE), "(short) (Short.MAX_VALUE & Short.MIN_VALUE)");
    Tester.checkEqual(shortAnd(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE & (short) -1), "(short) (Short.MAX_VALUE & (short) -1)");
    Tester.checkEqual(shortAnd(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE & (short) 0), "(short) (Short.MAX_VALUE & (short) 0)");
    Tester.checkEqual(shortAnd(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE & (short) 1), "(short) (Short.MAX_VALUE & (short) 1)");
    Tester.checkEqual(shortAnd(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE & Short.MAX_VALUE), "(short) (Short.MAX_VALUE & Short.MAX_VALUE)");
    Tester.checkEqual(shortXor(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE ^ Short.MIN_VALUE), "(short) (Short.MIN_VALUE ^ Short.MIN_VALUE)");
    Tester.checkEqual(shortXor(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE ^ (short) -1), "(short) (Short.MIN_VALUE ^ (short) -1)");
    Tester.checkEqual(shortXor(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE ^ (short) 0), "(short) (Short.MIN_VALUE ^ (short) 0)");
    Tester.checkEqual(shortXor(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE ^ (short) 1), "(short) (Short.MIN_VALUE ^ (short) 1)");
    Tester.checkEqual(shortXor(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE ^ Short.MAX_VALUE), "(short) (Short.MIN_VALUE ^ Short.MAX_VALUE)");
    Tester.checkEqual(shortXor((short) -1, Short.MIN_VALUE), (short) ((short) -1 ^ Short.MIN_VALUE), "(short) ((short) -1 ^ Short.MIN_VALUE)");
    Tester.checkEqual(shortXor((short) -1, (short) -1), (short) ((short) -1 ^ (short) -1), "(short) ((short) -1 ^ (short) -1)");
    Tester.checkEqual(shortXor((short) -1, (short) 0), (short) ((short) -1 ^ (short) 0), "(short) ((short) -1 ^ (short) 0)");
    Tester.checkEqual(shortXor((short) -1, (short) 1), (short) ((short) -1 ^ (short) 1), "(short) ((short) -1 ^ (short) 1)");
    Tester.checkEqual(shortXor((short) -1, Short.MAX_VALUE), (short) ((short) -1 ^ Short.MAX_VALUE), "(short) ((short) -1 ^ Short.MAX_VALUE)");
    Tester.checkEqual(shortXor((short) 0, Short.MIN_VALUE), (short) ((short) 0 ^ Short.MIN_VALUE), "(short) ((short) 0 ^ Short.MIN_VALUE)");
    Tester.checkEqual(shortXor((short) 0, (short) -1), (short) ((short) 0 ^ (short) -1), "(short) ((short) 0 ^ (short) -1)");
    Tester.checkEqual(shortXor((short) 0, (short) 0), (short) ((short) 0 ^ (short) 0), "(short) ((short) 0 ^ (short) 0)");
    Tester.checkEqual(shortXor((short) 0, (short) 1), (short) ((short) 0 ^ (short) 1), "(short) ((short) 0 ^ (short) 1)");
    Tester.checkEqual(shortXor((short) 0, Short.MAX_VALUE), (short) ((short) 0 ^ Short.MAX_VALUE), "(short) ((short) 0 ^ Short.MAX_VALUE)");
    Tester.checkEqual(shortXor((short) 1, Short.MIN_VALUE), (short) ((short) 1 ^ Short.MIN_VALUE), "(short) ((short) 1 ^ Short.MIN_VALUE)");
    Tester.checkEqual(shortXor((short) 1, (short) -1), (short) ((short) 1 ^ (short) -1), "(short) ((short) 1 ^ (short) -1)");
    Tester.checkEqual(shortXor((short) 1, (short) 0), (short) ((short) 1 ^ (short) 0), "(short) ((short) 1 ^ (short) 0)");
    Tester.checkEqual(shortXor((short) 1, (short) 1), (short) ((short) 1 ^ (short) 1), "(short) ((short) 1 ^ (short) 1)");
    Tester.checkEqual(shortXor((short) 1, Short.MAX_VALUE), (short) ((short) 1 ^ Short.MAX_VALUE), "(short) ((short) 1 ^ Short.MAX_VALUE)");
    Tester.checkEqual(shortXor(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE ^ Short.MIN_VALUE), "(short) (Short.MAX_VALUE ^ Short.MIN_VALUE)");
    Tester.checkEqual(shortXor(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE ^ (short) -1), "(short) (Short.MAX_VALUE ^ (short) -1)");
    Tester.checkEqual(shortXor(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE ^ (short) 0), "(short) (Short.MAX_VALUE ^ (short) 0)");
    Tester.checkEqual(shortXor(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE ^ (short) 1), "(short) (Short.MAX_VALUE ^ (short) 1)");
    Tester.checkEqual(shortXor(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE ^ Short.MAX_VALUE), "(short) (Short.MAX_VALUE ^ Short.MAX_VALUE)");
    Tester.checkEqual(shortOr(Short.MIN_VALUE, Short.MIN_VALUE), (short) (Short.MIN_VALUE | Short.MIN_VALUE), "(short) (Short.MIN_VALUE | Short.MIN_VALUE)");
    Tester.checkEqual(shortOr(Short.MIN_VALUE, (short) -1), (short) (Short.MIN_VALUE | (short) -1), "(short) (Short.MIN_VALUE | (short) -1)");
    Tester.checkEqual(shortOr(Short.MIN_VALUE, (short) 0), (short) (Short.MIN_VALUE | (short) 0), "(short) (Short.MIN_VALUE | (short) 0)");
    Tester.checkEqual(shortOr(Short.MIN_VALUE, (short) 1), (short) (Short.MIN_VALUE | (short) 1), "(short) (Short.MIN_VALUE | (short) 1)");
    Tester.checkEqual(shortOr(Short.MIN_VALUE, Short.MAX_VALUE), (short) (Short.MIN_VALUE | Short.MAX_VALUE), "(short) (Short.MIN_VALUE | Short.MAX_VALUE)");
    Tester.checkEqual(shortOr((short) -1, Short.MIN_VALUE), (short) ((short) -1 | Short.MIN_VALUE), "(short) ((short) -1 | Short.MIN_VALUE)");
    Tester.checkEqual(shortOr((short) -1, (short) -1), (short) ((short) -1 | (short) -1), "(short) ((short) -1 | (short) -1)");
    Tester.checkEqual(shortOr((short) -1, (short) 0), (short) ((short) -1 | (short) 0), "(short) ((short) -1 | (short) 0)");
    Tester.checkEqual(shortOr((short) -1, (short) 1), (short) ((short) -1 | (short) 1), "(short) ((short) -1 | (short) 1)");
    Tester.checkEqual(shortOr((short) -1, Short.MAX_VALUE), (short) ((short) -1 | Short.MAX_VALUE), "(short) ((short) -1 | Short.MAX_VALUE)");
    Tester.checkEqual(shortOr((short) 0, Short.MIN_VALUE), (short) ((short) 0 | Short.MIN_VALUE), "(short) ((short) 0 | Short.MIN_VALUE)");
    Tester.checkEqual(shortOr((short) 0, (short) -1), (short) ((short) 0 | (short) -1), "(short) ((short) 0 | (short) -1)");
    Tester.checkEqual(shortOr((short) 0, (short) 0), (short) ((short) 0 | (short) 0), "(short) ((short) 0 | (short) 0)");
    Tester.checkEqual(shortOr((short) 0, (short) 1), (short) ((short) 0 | (short) 1), "(short) ((short) 0 | (short) 1)");
    Tester.checkEqual(shortOr((short) 0, Short.MAX_VALUE), (short) ((short) 0 | Short.MAX_VALUE), "(short) ((short) 0 | Short.MAX_VALUE)");
    Tester.checkEqual(shortOr((short) 1, Short.MIN_VALUE), (short) ((short) 1 | Short.MIN_VALUE), "(short) ((short) 1 | Short.MIN_VALUE)");
    Tester.checkEqual(shortOr((short) 1, (short) -1), (short) ((short) 1 | (short) -1), "(short) ((short) 1 | (short) -1)");
    Tester.checkEqual(shortOr((short) 1, (short) 0), (short) ((short) 1 | (short) 0), "(short) ((short) 1 | (short) 0)");
    Tester.checkEqual(shortOr((short) 1, (short) 1), (short) ((short) 1 | (short) 1), "(short) ((short) 1 | (short) 1)");
    Tester.checkEqual(shortOr((short) 1, Short.MAX_VALUE), (short) ((short) 1 | Short.MAX_VALUE), "(short) ((short) 1 | Short.MAX_VALUE)");
    Tester.checkEqual(shortOr(Short.MAX_VALUE, Short.MIN_VALUE), (short) (Short.MAX_VALUE | Short.MIN_VALUE), "(short) (Short.MAX_VALUE | Short.MIN_VALUE)");
    Tester.checkEqual(shortOr(Short.MAX_VALUE, (short) -1), (short) (Short.MAX_VALUE | (short) -1), "(short) (Short.MAX_VALUE | (short) -1)");
    Tester.checkEqual(shortOr(Short.MAX_VALUE, (short) 0), (short) (Short.MAX_VALUE | (short) 0), "(short) (Short.MAX_VALUE | (short) 0)");
    Tester.checkEqual(shortOr(Short.MAX_VALUE, (short) 1), (short) (Short.MAX_VALUE | (short) 1), "(short) (Short.MAX_VALUE | (short) 1)");
    Tester.checkEqual(shortOr(Short.MAX_VALUE, Short.MAX_VALUE), (short) (Short.MAX_VALUE | Short.MAX_VALUE), "(short) (Short.MAX_VALUE | Short.MAX_VALUE)");
  }
  static void shortSwitch() {
    switch(0) {
      case ((((short) + Short.MIN_VALUE) == 0) ? 0 : 0):
      case ((((short) + (short) -1) == 0) ? 1 : 1):
      case ((((short) + (short) 0) == 0) ? 2 : 2):
      case ((((short) + (short) 1) == 0) ? 3 : 3):
      case ((((short) + Short.MAX_VALUE) == 0) ? 4 : 4):
      case ((((short) - Short.MIN_VALUE) == 0) ? 5 : 5):
      case ((((short) - (short) -1) == 0) ? 6 : 6):
      case ((((short) - (short) 0) == 0) ? 7 : 7):
      case ((((short) - (short) 1) == 0) ? 8 : 8):
      case ((((short) - Short.MAX_VALUE) == 0) ? 9 : 9):
      case ((((short) ~ Short.MIN_VALUE) == 0) ? 10 : 10):
      case ((((short) ~ (short) -1) == 0) ? 11 : 11):
      case ((((short) ~ (short) 0) == 0) ? 12 : 12):
      case ((((short) ~ (short) 1) == 0) ? 13 : 13):
      case ((((short) ~ Short.MAX_VALUE) == 0) ? 14 : 14):
      case ((((short) (Short.MIN_VALUE * Short.MIN_VALUE)) == 0) ? 15 : 15):
      case ((((short) (Short.MIN_VALUE * (short) -1)) == 0) ? 16 : 16):
      case ((((short) (Short.MIN_VALUE * (short) 0)) == 0) ? 17 : 17):
      case ((((short) (Short.MIN_VALUE * (short) 1)) == 0) ? 18 : 18):
      case ((((short) (Short.MIN_VALUE * Short.MAX_VALUE)) == 0) ? 19 : 19):
      case ((((short) ((short) -1 * Short.MIN_VALUE)) == 0) ? 20 : 20):
      case ((((short) ((short) -1 * (short) -1)) == 0) ? 21 : 21):
      case ((((short) ((short) -1 * (short) 0)) == 0) ? 22 : 22):
      case ((((short) ((short) -1 * (short) 1)) == 0) ? 23 : 23):
      case ((((short) ((short) -1 * Short.MAX_VALUE)) == 0) ? 24 : 24):
      case ((((short) ((short) 0 * Short.MIN_VALUE)) == 0) ? 25 : 25):
      case ((((short) ((short) 0 * (short) -1)) == 0) ? 26 : 26):
      case ((((short) ((short) 0 * (short) 0)) == 0) ? 27 : 27):
      case ((((short) ((short) 0 * (short) 1)) == 0) ? 28 : 28):
      case ((((short) ((short) 0 * Short.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((short) ((short) 1 * Short.MIN_VALUE)) == 0) ? 30 : 30):
      case ((((short) ((short) 1 * (short) -1)) == 0) ? 31 : 31):
      case ((((short) ((short) 1 * (short) 0)) == 0) ? 32 : 32):
      case ((((short) ((short) 1 * (short) 1)) == 0) ? 33 : 33):
      case ((((short) ((short) 1 * Short.MAX_VALUE)) == 0) ? 34 : 34):
      case ((((short) (Short.MAX_VALUE * Short.MIN_VALUE)) == 0) ? 35 : 35):
      case ((((short) (Short.MAX_VALUE * (short) -1)) == 0) ? 36 : 36):
      case ((((short) (Short.MAX_VALUE * (short) 0)) == 0) ? 37 : 37):
      case ((((short) (Short.MAX_VALUE * (short) 1)) == 0) ? 38 : 38):
      case ((((short) (Short.MAX_VALUE * Short.MAX_VALUE)) == 0) ? 39 : 39):
      case ((((short) (Short.MIN_VALUE / Short.MIN_VALUE)) == 0) ? 40 : 40):
      case ((((short) (Short.MIN_VALUE / (short) -1)) == 0) ? 41 : 41):
      case ((((short) (Short.MIN_VALUE / (short) 1)) == 0) ? 42 : 42):
      case ((((short) (Short.MIN_VALUE / Short.MAX_VALUE)) == 0) ? 43 : 43):
      case ((((short) ((short) -1 / Short.MIN_VALUE)) == 0) ? 44 : 44):
      case ((((short) ((short) -1 / (short) -1)) == 0) ? 45 : 45):
      case ((((short) ((short) -1 / (short) 1)) == 0) ? 46 : 46):
      case ((((short) ((short) -1 / Short.MAX_VALUE)) == 0) ? 47 : 47):
      case ((((short) ((short) 0 / Short.MIN_VALUE)) == 0) ? 48 : 48):
      case ((((short) ((short) 0 / (short) -1)) == 0) ? 49 : 49):
      case ((((short) ((short) 0 / (short) 1)) == 0) ? 50 : 50):
      case ((((short) ((short) 0 / Short.MAX_VALUE)) == 0) ? 51 : 51):
      case ((((short) ((short) 1 / Short.MIN_VALUE)) == 0) ? 52 : 52):
      case ((((short) ((short) 1 / (short) -1)) == 0) ? 53 : 53):
      case ((((short) ((short) 1 / (short) 1)) == 0) ? 54 : 54):
      case ((((short) ((short) 1 / Short.MAX_VALUE)) == 0) ? 55 : 55):
      case ((((short) (Short.MAX_VALUE / Short.MIN_VALUE)) == 0) ? 56 : 56):
      case ((((short) (Short.MAX_VALUE / (short) -1)) == 0) ? 57 : 57):
      case ((((short) (Short.MAX_VALUE / (short) 1)) == 0) ? 58 : 58):
      case ((((short) (Short.MAX_VALUE / Short.MAX_VALUE)) == 0) ? 59 : 59):
      case ((((short) (Short.MIN_VALUE % Short.MIN_VALUE)) == 0) ? 60 : 60):
      case ((((short) (Short.MIN_VALUE % (short) -1)) == 0) ? 61 : 61):
      case ((((short) (Short.MIN_VALUE % (short) 1)) == 0) ? 62 : 62):
      case ((((short) (Short.MIN_VALUE % Short.MAX_VALUE)) == 0) ? 63 : 63):
      case ((((short) ((short) -1 % Short.MIN_VALUE)) == 0) ? 64 : 64):
      case ((((short) ((short) -1 % (short) -1)) == 0) ? 65 : 65):
      case ((((short) ((short) -1 % (short) 1)) == 0) ? 66 : 66):
      case ((((short) ((short) -1 % Short.MAX_VALUE)) == 0) ? 67 : 67):
      case ((((short) ((short) 0 % Short.MIN_VALUE)) == 0) ? 68 : 68):
      case ((((short) ((short) 0 % (short) -1)) == 0) ? 69 : 69):
      case ((((short) ((short) 0 % (short) 1)) == 0) ? 70 : 70):
      case ((((short) ((short) 0 % Short.MAX_VALUE)) == 0) ? 71 : 71):
      case ((((short) ((short) 1 % Short.MIN_VALUE)) == 0) ? 72 : 72):
      case ((((short) ((short) 1 % (short) -1)) == 0) ? 73 : 73):
      case ((((short) ((short) 1 % (short) 1)) == 0) ? 74 : 74):
      case ((((short) ((short) 1 % Short.MAX_VALUE)) == 0) ? 75 : 75):
      case ((((short) (Short.MAX_VALUE % Short.MIN_VALUE)) == 0) ? 76 : 76):
      case ((((short) (Short.MAX_VALUE % (short) -1)) == 0) ? 77 : 77):
      case ((((short) (Short.MAX_VALUE % (short) 1)) == 0) ? 78 : 78):
      case ((((short) (Short.MAX_VALUE % Short.MAX_VALUE)) == 0) ? 79 : 79):
      case ((((short) (Short.MIN_VALUE + Short.MIN_VALUE)) == 0) ? 80 : 80):
      case ((((short) (Short.MIN_VALUE + (short) -1)) == 0) ? 81 : 81):
      case ((((short) (Short.MIN_VALUE + (short) 0)) == 0) ? 82 : 82):
      case ((((short) (Short.MIN_VALUE + (short) 1)) == 0) ? 83 : 83):
      case ((((short) (Short.MIN_VALUE + Short.MAX_VALUE)) == 0) ? 84 : 84):
      case ((((short) ((short) -1 + Short.MIN_VALUE)) == 0) ? 85 : 85):
      case ((((short) ((short) -1 + (short) -1)) == 0) ? 86 : 86):
      case ((((short) ((short) -1 + (short) 0)) == 0) ? 87 : 87):
      case ((((short) ((short) -1 + (short) 1)) == 0) ? 88 : 88):
      case ((((short) ((short) -1 + Short.MAX_VALUE)) == 0) ? 89 : 89):
      case ((((short) ((short) 0 + Short.MIN_VALUE)) == 0) ? 90 : 90):
      case ((((short) ((short) 0 + (short) -1)) == 0) ? 91 : 91):
      case ((((short) ((short) 0 + (short) 0)) == 0) ? 92 : 92):
      case ((((short) ((short) 0 + (short) 1)) == 0) ? 93 : 93):
      case ((((short) ((short) 0 + Short.MAX_VALUE)) == 0) ? 94 : 94):
      case ((((short) ((short) 1 + Short.MIN_VALUE)) == 0) ? 95 : 95):
      case ((((short) ((short) 1 + (short) -1)) == 0) ? 96 : 96):
      case ((((short) ((short) 1 + (short) 0)) == 0) ? 97 : 97):
      case ((((short) ((short) 1 + (short) 1)) == 0) ? 98 : 98):
      case ((((short) ((short) 1 + Short.MAX_VALUE)) == 0) ? 99 : 99):
      case ((((short) (Short.MAX_VALUE + Short.MIN_VALUE)) == 0) ? 100 : 100):
      case ((((short) (Short.MAX_VALUE + (short) -1)) == 0) ? 101 : 101):
      case ((((short) (Short.MAX_VALUE + (short) 0)) == 0) ? 102 : 102):
      case ((((short) (Short.MAX_VALUE + (short) 1)) == 0) ? 103 : 103):
      case ((((short) (Short.MAX_VALUE + Short.MAX_VALUE)) == 0) ? 104 : 104):
      case ((((short) (Short.MIN_VALUE - Short.MIN_VALUE)) == 0) ? 105 : 105):
      case ((((short) (Short.MIN_VALUE - (short) -1)) == 0) ? 106 : 106):
      case ((((short) (Short.MIN_VALUE - (short) 0)) == 0) ? 107 : 107):
      case ((((short) (Short.MIN_VALUE - (short) 1)) == 0) ? 108 : 108):
      case ((((short) (Short.MIN_VALUE - Short.MAX_VALUE)) == 0) ? 109 : 109):
      case ((((short) ((short) -1 - Short.MIN_VALUE)) == 0) ? 110 : 110):
      case ((((short) ((short) -1 - (short) -1)) == 0) ? 111 : 111):
      case ((((short) ((short) -1 - (short) 0)) == 0) ? 112 : 112):
      case ((((short) ((short) -1 - (short) 1)) == 0) ? 113 : 113):
      case ((((short) ((short) -1 - Short.MAX_VALUE)) == 0) ? 114 : 114):
      case ((((short) ((short) 0 - Short.MIN_VALUE)) == 0) ? 115 : 115):
      case ((((short) ((short) 0 - (short) -1)) == 0) ? 116 : 116):
      case ((((short) ((short) 0 - (short) 0)) == 0) ? 117 : 117):
      case ((((short) ((short) 0 - (short) 1)) == 0) ? 118 : 118):
      case ((((short) ((short) 0 - Short.MAX_VALUE)) == 0) ? 119 : 119):
      case ((((short) ((short) 1 - Short.MIN_VALUE)) == 0) ? 120 : 120):
      case ((((short) ((short) 1 - (short) -1)) == 0) ? 121 : 121):
      case ((((short) ((short) 1 - (short) 0)) == 0) ? 122 : 122):
      case ((((short) ((short) 1 - (short) 1)) == 0) ? 123 : 123):
      case ((((short) ((short) 1 - Short.MAX_VALUE)) == 0) ? 124 : 124):
      case ((((short) (Short.MAX_VALUE - Short.MIN_VALUE)) == 0) ? 125 : 125):
      case ((((short) (Short.MAX_VALUE - (short) -1)) == 0) ? 126 : 126):
      case ((((short) (Short.MAX_VALUE - (short) 0)) == 0) ? 127 : 127):
      case ((((short) (Short.MAX_VALUE - (short) 1)) == 0) ? 128 : 128):
      case ((((short) (Short.MAX_VALUE - Short.MAX_VALUE)) == 0) ? 129 : 129):
      case ((((short) (Short.MIN_VALUE << Short.MIN_VALUE)) == 0) ? 130 : 130):
      case ((((short) (Short.MIN_VALUE << (short) -1)) == 0) ? 131 : 131):
      case ((((short) (Short.MIN_VALUE << (short) 0)) == 0) ? 132 : 132):
      case ((((short) (Short.MIN_VALUE << (short) 1)) == 0) ? 133 : 133):
      case ((((short) (Short.MIN_VALUE << Short.MAX_VALUE)) == 0) ? 134 : 134):
      case ((((short) ((short) -1 << Short.MIN_VALUE)) == 0) ? 135 : 135):
      case ((((short) ((short) -1 << (short) -1)) == 0) ? 136 : 136):
      case ((((short) ((short) -1 << (short) 0)) == 0) ? 137 : 137):
      case ((((short) ((short) -1 << (short) 1)) == 0) ? 138 : 138):
      case ((((short) ((short) -1 << Short.MAX_VALUE)) == 0) ? 139 : 139):
      case ((((short) ((short) 0 << Short.MIN_VALUE)) == 0) ? 140 : 140):
      case ((((short) ((short) 0 << (short) -1)) == 0) ? 141 : 141):
      case ((((short) ((short) 0 << (short) 0)) == 0) ? 142 : 142):
      case ((((short) ((short) 0 << (short) 1)) == 0) ? 143 : 143):
      case ((((short) ((short) 0 << Short.MAX_VALUE)) == 0) ? 144 : 144):
      case ((((short) ((short) 1 << Short.MIN_VALUE)) == 0) ? 145 : 145):
      case ((((short) ((short) 1 << (short) -1)) == 0) ? 146 : 146):
      case ((((short) ((short) 1 << (short) 0)) == 0) ? 147 : 147):
      case ((((short) ((short) 1 << (short) 1)) == 0) ? 148 : 148):
      case ((((short) ((short) 1 << Short.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((short) (Short.MAX_VALUE << Short.MIN_VALUE)) == 0) ? 150 : 150):
      case ((((short) (Short.MAX_VALUE << (short) -1)) == 0) ? 151 : 151):
      case ((((short) (Short.MAX_VALUE << (short) 0)) == 0) ? 152 : 152):
      case ((((short) (Short.MAX_VALUE << (short) 1)) == 0) ? 153 : 153):
      case ((((short) (Short.MAX_VALUE << Short.MAX_VALUE)) == 0) ? 154 : 154):
      case ((((short) (Short.MIN_VALUE >> Short.MIN_VALUE)) == 0) ? 155 : 155):
      case ((((short) (Short.MIN_VALUE >> (short) -1)) == 0) ? 156 : 156):
      case ((((short) (Short.MIN_VALUE >> (short) 0)) == 0) ? 157 : 157):
      case ((((short) (Short.MIN_VALUE >> (short) 1)) == 0) ? 158 : 158):
      case ((((short) (Short.MIN_VALUE >> Short.MAX_VALUE)) == 0) ? 159 : 159):
      case ((((short) ((short) -1 >> Short.MIN_VALUE)) == 0) ? 160 : 160):
      case ((((short) ((short) -1 >> (short) -1)) == 0) ? 161 : 161):
      case ((((short) ((short) -1 >> (short) 0)) == 0) ? 162 : 162):
      case ((((short) ((short) -1 >> (short) 1)) == 0) ? 163 : 163):
      case ((((short) ((short) -1 >> Short.MAX_VALUE)) == 0) ? 164 : 164):
      case ((((short) ((short) 0 >> Short.MIN_VALUE)) == 0) ? 165 : 165):
      case ((((short) ((short) 0 >> (short) -1)) == 0) ? 166 : 166):
      case ((((short) ((short) 0 >> (short) 0)) == 0) ? 167 : 167):
      case ((((short) ((short) 0 >> (short) 1)) == 0) ? 168 : 168):
      case ((((short) ((short) 0 >> Short.MAX_VALUE)) == 0) ? 169 : 169):
      case ((((short) ((short) 1 >> Short.MIN_VALUE)) == 0) ? 170 : 170):
      case ((((short) ((short) 1 >> (short) -1)) == 0) ? 171 : 171):
      case ((((short) ((short) 1 >> (short) 0)) == 0) ? 172 : 172):
      case ((((short) ((short) 1 >> (short) 1)) == 0) ? 173 : 173):
      case ((((short) ((short) 1 >> Short.MAX_VALUE)) == 0) ? 174 : 174):
      case ((((short) (Short.MAX_VALUE >> Short.MIN_VALUE)) == 0) ? 175 : 175):
      case ((((short) (Short.MAX_VALUE >> (short) -1)) == 0) ? 176 : 176):
      case ((((short) (Short.MAX_VALUE >> (short) 0)) == 0) ? 177 : 177):
      case ((((short) (Short.MAX_VALUE >> (short) 1)) == 0) ? 178 : 178):
      case ((((short) (Short.MAX_VALUE >> Short.MAX_VALUE)) == 0) ? 179 : 179):
      case ((((short) (Short.MIN_VALUE >>> Short.MIN_VALUE)) == 0) ? 180 : 180):
      case ((((short) (Short.MIN_VALUE >>> (short) -1)) == 0) ? 181 : 181):
      case ((((short) (Short.MIN_VALUE >>> (short) 0)) == 0) ? 182 : 182):
      case ((((short) (Short.MIN_VALUE >>> (short) 1)) == 0) ? 183 : 183):
      case ((((short) (Short.MIN_VALUE >>> Short.MAX_VALUE)) == 0) ? 184 : 184):
      case ((((short) ((short) -1 >>> Short.MIN_VALUE)) == 0) ? 185 : 185):
      case ((((short) ((short) -1 >>> (short) -1)) == 0) ? 186 : 186):
      case ((((short) ((short) -1 >>> (short) 0)) == 0) ? 187 : 187):
      case ((((short) ((short) -1 >>> (short) 1)) == 0) ? 188 : 188):
      case ((((short) ((short) -1 >>> Short.MAX_VALUE)) == 0) ? 189 : 189):
      case ((((short) ((short) 0 >>> Short.MIN_VALUE)) == 0) ? 190 : 190):
      case ((((short) ((short) 0 >>> (short) -1)) == 0) ? 191 : 191):
      case ((((short) ((short) 0 >>> (short) 0)) == 0) ? 192 : 192):
      case ((((short) ((short) 0 >>> (short) 1)) == 0) ? 193 : 193):
      case ((((short) ((short) 0 >>> Short.MAX_VALUE)) == 0) ? 194 : 194):
      case ((((short) ((short) 1 >>> Short.MIN_VALUE)) == 0) ? 195 : 195):
      case ((((short) ((short) 1 >>> (short) -1)) == 0) ? 196 : 196):
      case ((((short) ((short) 1 >>> (short) 0)) == 0) ? 197 : 197):
      case ((((short) ((short) 1 >>> (short) 1)) == 0) ? 198 : 198):
      case ((((short) ((short) 1 >>> Short.MAX_VALUE)) == 0) ? 199 : 199):
      case ((((short) (Short.MAX_VALUE >>> Short.MIN_VALUE)) == 0) ? 200 : 200):
      case ((((short) (Short.MAX_VALUE >>> (short) -1)) == 0) ? 201 : 201):
      case ((((short) (Short.MAX_VALUE >>> (short) 0)) == 0) ? 202 : 202):
      case ((((short) (Short.MAX_VALUE >>> (short) 1)) == 0) ? 203 : 203):
      case ((((short) (Short.MAX_VALUE >>> Short.MAX_VALUE)) == 0) ? 204 : 204):
      case ((Short.MIN_VALUE < Short.MIN_VALUE) ? 205 : 205):
      case ((Short.MIN_VALUE < (short) -1) ? 206 : 206):
      case ((Short.MIN_VALUE < (short) 0) ? 207 : 207):
      case ((Short.MIN_VALUE < (short) 1) ? 208 : 208):
      case ((Short.MIN_VALUE < Short.MAX_VALUE) ? 209 : 209):
      case (((short) -1 < Short.MIN_VALUE) ? 210 : 210):
      case (((short) -1 < (short) -1) ? 211 : 211):
      case (((short) -1 < (short) 0) ? 212 : 212):
      case (((short) -1 < (short) 1) ? 213 : 213):
      case (((short) -1 < Short.MAX_VALUE) ? 214 : 214):
      case (((short) 0 < Short.MIN_VALUE) ? 215 : 215):
      case (((short) 0 < (short) -1) ? 216 : 216):
      case (((short) 0 < (short) 0) ? 217 : 217):
      case (((short) 0 < (short) 1) ? 218 : 218):
      case (((short) 0 < Short.MAX_VALUE) ? 219 : 219):
      case (((short) 1 < Short.MIN_VALUE) ? 220 : 220):
      case (((short) 1 < (short) -1) ? 221 : 221):
      case (((short) 1 < (short) 0) ? 222 : 222):
      case (((short) 1 < (short) 1) ? 223 : 223):
      case (((short) 1 < Short.MAX_VALUE) ? 224 : 224):
      case ((Short.MAX_VALUE < Short.MIN_VALUE) ? 225 : 225):
      case ((Short.MAX_VALUE < (short) -1) ? 226 : 226):
      case ((Short.MAX_VALUE < (short) 0) ? 227 : 227):
      case ((Short.MAX_VALUE < (short) 1) ? 228 : 228):
      case ((Short.MAX_VALUE < Short.MAX_VALUE) ? 229 : 229):
      case ((Short.MIN_VALUE > Short.MIN_VALUE) ? 230 : 230):
      case ((Short.MIN_VALUE > (short) -1) ? 231 : 231):
      case ((Short.MIN_VALUE > (short) 0) ? 232 : 232):
      case ((Short.MIN_VALUE > (short) 1) ? 233 : 233):
      case ((Short.MIN_VALUE > Short.MAX_VALUE) ? 234 : 234):
      case (((short) -1 > Short.MIN_VALUE) ? 235 : 235):
      case (((short) -1 > (short) -1) ? 236 : 236):
      case (((short) -1 > (short) 0) ? 237 : 237):
      case (((short) -1 > (short) 1) ? 238 : 238):
      case (((short) -1 > Short.MAX_VALUE) ? 239 : 239):
      case (((short) 0 > Short.MIN_VALUE) ? 240 : 240):
      case (((short) 0 > (short) -1) ? 241 : 241):
      case (((short) 0 > (short) 0) ? 242 : 242):
      case (((short) 0 > (short) 1) ? 243 : 243):
      case (((short) 0 > Short.MAX_VALUE) ? 244 : 244):
      case (((short) 1 > Short.MIN_VALUE) ? 245 : 245):
      case (((short) 1 > (short) -1) ? 246 : 246):
      case (((short) 1 > (short) 0) ? 247 : 247):
      case (((short) 1 > (short) 1) ? 248 : 248):
      case (((short) 1 > Short.MAX_VALUE) ? 249 : 249):
      case ((Short.MAX_VALUE > Short.MIN_VALUE) ? 250 : 250):
      case ((Short.MAX_VALUE > (short) -1) ? 251 : 251):
      case ((Short.MAX_VALUE > (short) 0) ? 252 : 252):
      case ((Short.MAX_VALUE > (short) 1) ? 253 : 253):
      case ((Short.MAX_VALUE > Short.MAX_VALUE) ? 254 : 254):
      case ((Short.MIN_VALUE <= Short.MIN_VALUE) ? 255 : 255):
      case ((Short.MIN_VALUE <= (short) -1) ? 256 : 256):
      case ((Short.MIN_VALUE <= (short) 0) ? 257 : 257):
      case ((Short.MIN_VALUE <= (short) 1) ? 258 : 258):
      case ((Short.MIN_VALUE <= Short.MAX_VALUE) ? 259 : 259):
      case (((short) -1 <= Short.MIN_VALUE) ? 260 : 260):
      case (((short) -1 <= (short) -1) ? 261 : 261):
      case (((short) -1 <= (short) 0) ? 262 : 262):
      case (((short) -1 <= (short) 1) ? 263 : 263):
      case (((short) -1 <= Short.MAX_VALUE) ? 264 : 264):
      case (((short) 0 <= Short.MIN_VALUE) ? 265 : 265):
      case (((short) 0 <= (short) -1) ? 266 : 266):
      case (((short) 0 <= (short) 0) ? 267 : 267):
      case (((short) 0 <= (short) 1) ? 268 : 268):
      case (((short) 0 <= Short.MAX_VALUE) ? 269 : 269):
      case (((short) 1 <= Short.MIN_VALUE) ? 270 : 270):
      case (((short) 1 <= (short) -1) ? 271 : 271):
      case (((short) 1 <= (short) 0) ? 272 : 272):
      case (((short) 1 <= (short) 1) ? 273 : 273):
      case (((short) 1 <= Short.MAX_VALUE) ? 274 : 274):
      case ((Short.MAX_VALUE <= Short.MIN_VALUE) ? 275 : 275):
      case ((Short.MAX_VALUE <= (short) -1) ? 276 : 276):
      case ((Short.MAX_VALUE <= (short) 0) ? 277 : 277):
      case ((Short.MAX_VALUE <= (short) 1) ? 278 : 278):
      case ((Short.MAX_VALUE <= Short.MAX_VALUE) ? 279 : 279):
      case ((Short.MIN_VALUE >= Short.MIN_VALUE) ? 280 : 280):
      case ((Short.MIN_VALUE >= (short) -1) ? 281 : 281):
      case ((Short.MIN_VALUE >= (short) 0) ? 282 : 282):
      case ((Short.MIN_VALUE >= (short) 1) ? 283 : 283):
      case ((Short.MIN_VALUE >= Short.MAX_VALUE) ? 284 : 284):
      case (((short) -1 >= Short.MIN_VALUE) ? 285 : 285):
      case (((short) -1 >= (short) -1) ? 286 : 286):
      case (((short) -1 >= (short) 0) ? 287 : 287):
      case (((short) -1 >= (short) 1) ? 288 : 288):
      case (((short) -1 >= Short.MAX_VALUE) ? 289 : 289):
      case (((short) 0 >= Short.MIN_VALUE) ? 290 : 290):
      case (((short) 0 >= (short) -1) ? 291 : 291):
      case (((short) 0 >= (short) 0) ? 292 : 292):
      case (((short) 0 >= (short) 1) ? 293 : 293):
      case (((short) 0 >= Short.MAX_VALUE) ? 294 : 294):
      case (((short) 1 >= Short.MIN_VALUE) ? 295 : 295):
      case (((short) 1 >= (short) -1) ? 296 : 296):
      case (((short) 1 >= (short) 0) ? 297 : 297):
      case (((short) 1 >= (short) 1) ? 298 : 298):
      case (((short) 1 >= Short.MAX_VALUE) ? 299 : 299):
      case ((Short.MAX_VALUE >= Short.MIN_VALUE) ? 300 : 300):
      case ((Short.MAX_VALUE >= (short) -1) ? 301 : 301):
      case ((Short.MAX_VALUE >= (short) 0) ? 302 : 302):
      case ((Short.MAX_VALUE >= (short) 1) ? 303 : 303):
      case ((Short.MAX_VALUE >= Short.MAX_VALUE) ? 304 : 304):
      case ((Short.MIN_VALUE == Short.MIN_VALUE) ? 305 : 305):
      case ((Short.MIN_VALUE == (short) -1) ? 306 : 306):
      case ((Short.MIN_VALUE == (short) 0) ? 307 : 307):
      case ((Short.MIN_VALUE == (short) 1) ? 308 : 308):
      case ((Short.MIN_VALUE == Short.MAX_VALUE) ? 309 : 309):
      case (((short) -1 == Short.MIN_VALUE) ? 310 : 310):
      case (((short) -1 == (short) -1) ? 311 : 311):
      case (((short) -1 == (short) 0) ? 312 : 312):
      case (((short) -1 == (short) 1) ? 313 : 313):
      case (((short) -1 == Short.MAX_VALUE) ? 314 : 314):
      case (((short) 0 == Short.MIN_VALUE) ? 315 : 315):
      case (((short) 0 == (short) -1) ? 316 : 316):
      case (((short) 0 == (short) 0) ? 317 : 317):
      case (((short) 0 == (short) 1) ? 318 : 318):
      case (((short) 0 == Short.MAX_VALUE) ? 319 : 319):
      case (((short) 1 == Short.MIN_VALUE) ? 320 : 320):
      case (((short) 1 == (short) -1) ? 321 : 321):
      case (((short) 1 == (short) 0) ? 322 : 322):
      case (((short) 1 == (short) 1) ? 323 : 323):
      case (((short) 1 == Short.MAX_VALUE) ? 324 : 324):
      case ((Short.MAX_VALUE == Short.MIN_VALUE) ? 325 : 325):
      case ((Short.MAX_VALUE == (short) -1) ? 326 : 326):
      case ((Short.MAX_VALUE == (short) 0) ? 327 : 327):
      case ((Short.MAX_VALUE == (short) 1) ? 328 : 328):
      case ((Short.MAX_VALUE == Short.MAX_VALUE) ? 329 : 329):
      case ((Short.MIN_VALUE != Short.MIN_VALUE) ? 330 : 330):
      case ((Short.MIN_VALUE != (short) -1) ? 331 : 331):
      case ((Short.MIN_VALUE != (short) 0) ? 332 : 332):
      case ((Short.MIN_VALUE != (short) 1) ? 333 : 333):
      case ((Short.MIN_VALUE != Short.MAX_VALUE) ? 334 : 334):
      case (((short) -1 != Short.MIN_VALUE) ? 335 : 335):
      case (((short) -1 != (short) -1) ? 336 : 336):
      case (((short) -1 != (short) 0) ? 337 : 337):
      case (((short) -1 != (short) 1) ? 338 : 338):
      case (((short) -1 != Short.MAX_VALUE) ? 339 : 339):
      case (((short) 0 != Short.MIN_VALUE) ? 340 : 340):
      case (((short) 0 != (short) -1) ? 341 : 341):
      case (((short) 0 != (short) 0) ? 342 : 342):
      case (((short) 0 != (short) 1) ? 343 : 343):
      case (((short) 0 != Short.MAX_VALUE) ? 344 : 344):
      case (((short) 1 != Short.MIN_VALUE) ? 345 : 345):
      case (((short) 1 != (short) -1) ? 346 : 346):
      case (((short) 1 != (short) 0) ? 347 : 347):
      case (((short) 1 != (short) 1) ? 348 : 348):
      case (((short) 1 != Short.MAX_VALUE) ? 349 : 349):
      case ((Short.MAX_VALUE != Short.MIN_VALUE) ? 350 : 350):
      case ((Short.MAX_VALUE != (short) -1) ? 351 : 351):
      case ((Short.MAX_VALUE != (short) 0) ? 352 : 352):
      case ((Short.MAX_VALUE != (short) 1) ? 353 : 353):
      case ((Short.MAX_VALUE != Short.MAX_VALUE) ? 354 : 354):
      case ((((short) (Short.MIN_VALUE & Short.MIN_VALUE)) == 0) ? 355 : 355):
      case ((((short) (Short.MIN_VALUE & (short) -1)) == 0) ? 356 : 356):
      case ((((short) (Short.MIN_VALUE & (short) 0)) == 0) ? 357 : 357):
      case ((((short) (Short.MIN_VALUE & (short) 1)) == 0) ? 358 : 358):
      case ((((short) (Short.MIN_VALUE & Short.MAX_VALUE)) == 0) ? 359 : 359):
      case ((((short) ((short) -1 & Short.MIN_VALUE)) == 0) ? 360 : 360):
      case ((((short) ((short) -1 & (short) -1)) == 0) ? 361 : 361):
      case ((((short) ((short) -1 & (short) 0)) == 0) ? 362 : 362):
      case ((((short) ((short) -1 & (short) 1)) == 0) ? 363 : 363):
      case ((((short) ((short) -1 & Short.MAX_VALUE)) == 0) ? 364 : 364):
      case ((((short) ((short) 0 & Short.MIN_VALUE)) == 0) ? 365 : 365):
      case ((((short) ((short) 0 & (short) -1)) == 0) ? 366 : 366):
      case ((((short) ((short) 0 & (short) 0)) == 0) ? 367 : 367):
      case ((((short) ((short) 0 & (short) 1)) == 0) ? 368 : 368):
      case ((((short) ((short) 0 & Short.MAX_VALUE)) == 0) ? 369 : 369):
      case ((((short) ((short) 1 & Short.MIN_VALUE)) == 0) ? 370 : 370):
      case ((((short) ((short) 1 & (short) -1)) == 0) ? 371 : 371):
      case ((((short) ((short) 1 & (short) 0)) == 0) ? 372 : 372):
      case ((((short) ((short) 1 & (short) 1)) == 0) ? 373 : 373):
      case ((((short) ((short) 1 & Short.MAX_VALUE)) == 0) ? 374 : 374):
      case ((((short) (Short.MAX_VALUE & Short.MIN_VALUE)) == 0) ? 375 : 375):
      case ((((short) (Short.MAX_VALUE & (short) -1)) == 0) ? 376 : 376):
      case ((((short) (Short.MAX_VALUE & (short) 0)) == 0) ? 377 : 377):
      case ((((short) (Short.MAX_VALUE & (short) 1)) == 0) ? 378 : 378):
      case ((((short) (Short.MAX_VALUE & Short.MAX_VALUE)) == 0) ? 379 : 379):
      case ((((short) (Short.MIN_VALUE ^ Short.MIN_VALUE)) == 0) ? 380 : 380):
      case ((((short) (Short.MIN_VALUE ^ (short) -1)) == 0) ? 381 : 381):
      case ((((short) (Short.MIN_VALUE ^ (short) 0)) == 0) ? 382 : 382):
      case ((((short) (Short.MIN_VALUE ^ (short) 1)) == 0) ? 383 : 383):
      case ((((short) (Short.MIN_VALUE ^ Short.MAX_VALUE)) == 0) ? 384 : 384):
      case ((((short) ((short) -1 ^ Short.MIN_VALUE)) == 0) ? 385 : 385):
      case ((((short) ((short) -1 ^ (short) -1)) == 0) ? 386 : 386):
      case ((((short) ((short) -1 ^ (short) 0)) == 0) ? 387 : 387):
      case ((((short) ((short) -1 ^ (short) 1)) == 0) ? 388 : 388):
      case ((((short) ((short) -1 ^ Short.MAX_VALUE)) == 0) ? 389 : 389):
      case ((((short) ((short) 0 ^ Short.MIN_VALUE)) == 0) ? 390 : 390):
      case ((((short) ((short) 0 ^ (short) -1)) == 0) ? 391 : 391):
      case ((((short) ((short) 0 ^ (short) 0)) == 0) ? 392 : 392):
      case ((((short) ((short) 0 ^ (short) 1)) == 0) ? 393 : 393):
      case ((((short) ((short) 0 ^ Short.MAX_VALUE)) == 0) ? 394 : 394):
      case ((((short) ((short) 1 ^ Short.MIN_VALUE)) == 0) ? 395 : 395):
      case ((((short) ((short) 1 ^ (short) -1)) == 0) ? 396 : 396):
      case ((((short) ((short) 1 ^ (short) 0)) == 0) ? 397 : 397):
      case ((((short) ((short) 1 ^ (short) 1)) == 0) ? 398 : 398):
      case ((((short) ((short) 1 ^ Short.MAX_VALUE)) == 0) ? 399 : 399):
      case ((((short) (Short.MAX_VALUE ^ Short.MIN_VALUE)) == 0) ? 400 : 400):
      case ((((short) (Short.MAX_VALUE ^ (short) -1)) == 0) ? 401 : 401):
      case ((((short) (Short.MAX_VALUE ^ (short) 0)) == 0) ? 402 : 402):
      case ((((short) (Short.MAX_VALUE ^ (short) 1)) == 0) ? 403 : 403):
      case ((((short) (Short.MAX_VALUE ^ Short.MAX_VALUE)) == 0) ? 404 : 404):
      case ((((short) (Short.MIN_VALUE | Short.MIN_VALUE)) == 0) ? 405 : 405):
      case ((((short) (Short.MIN_VALUE | (short) -1)) == 0) ? 406 : 406):
      case ((((short) (Short.MIN_VALUE | (short) 0)) == 0) ? 407 : 407):
      case ((((short) (Short.MIN_VALUE | (short) 1)) == 0) ? 408 : 408):
      case ((((short) (Short.MIN_VALUE | Short.MAX_VALUE)) == 0) ? 409 : 409):
      case ((((short) ((short) -1 | Short.MIN_VALUE)) == 0) ? 410 : 410):
      case ((((short) ((short) -1 | (short) -1)) == 0) ? 411 : 411):
      case ((((short) ((short) -1 | (short) 0)) == 0) ? 412 : 412):
      case ((((short) ((short) -1 | (short) 1)) == 0) ? 413 : 413):
      case ((((short) ((short) -1 | Short.MAX_VALUE)) == 0) ? 414 : 414):
      case ((((short) ((short) 0 | Short.MIN_VALUE)) == 0) ? 415 : 415):
      case ((((short) ((short) 0 | (short) -1)) == 0) ? 416 : 416):
      case ((((short) ((short) 0 | (short) 0)) == 0) ? 417 : 417):
      case ((((short) ((short) 0 | (short) 1)) == 0) ? 418 : 418):
      case ((((short) ((short) 0 | Short.MAX_VALUE)) == 0) ? 419 : 419):
      case ((((short) ((short) 1 | Short.MIN_VALUE)) == 0) ? 420 : 420):
      case ((((short) ((short) 1 | (short) -1)) == 0) ? 421 : 421):
      case ((((short) ((short) 1 | (short) 0)) == 0) ? 422 : 422):
      case ((((short) ((short) 1 | (short) 1)) == 0) ? 423 : 423):
      case ((((short) ((short) 1 | Short.MAX_VALUE)) == 0) ? 424 : 424):
      case ((((short) (Short.MAX_VALUE | Short.MIN_VALUE)) == 0) ? 425 : 425):
      case ((((short) (Short.MAX_VALUE | (short) -1)) == 0) ? 426 : 426):
      case ((((short) (Short.MAX_VALUE | (short) 0)) == 0) ? 427 : 427):
      case ((((short) (Short.MAX_VALUE | (short) 1)) == 0) ? 428 : 428):
      case ((((short) (Short.MAX_VALUE | Short.MAX_VALUE)) == 0) ? 429 : 429):
      default:
    }
  }

  // --------
  // char tests
  static char charPlus(char x) { return (char) + x; }
  static char charMinus(char x) { return (char) - x; }
  static char charBitNot(char x) { return (char) ~ x; }
  static char charTimes(char x, char y) { return (char) (x * y); }
  static char charDiv(char x, char y) { return (char) (x / y); }
  static char charRem(char x, char y) { return (char) (x % y); }
  static char charAdd(char x, char y) { return (char) (x + y); }
  static char charSub(char x, char y) { return (char) (x - y); }
  static char charShl(char x, char y) { return (char) (x << y); }
  static char charShr(char x, char y) { return (char) (x >> y); }
  static char charUshr(char x, char y) { return (char) (x >>> y); }
  static boolean charLt(char x, char y) { return x < y; }
  static boolean charGt(char x, char y) { return x > y; }
  static boolean charLe(char x, char y) { return x <= y; }
  static boolean charGe(char x, char y) { return x >= y; }
  static boolean charEq(char x, char y) { return x == y; }
  static boolean charNe(char x, char y) { return x != y; }
  static char charAnd(char x, char y) { return (char) (x & y); }
  static char charXor(char x, char y) { return (char) (x ^ y); }
  static char charOr(char x, char y) { return (char) (x | y); }
  static void charTest() {
    Tester.checkEqual(charPlus((char) 0), (char) + (char) 0, "(char) + (char) 0");
    Tester.checkEqual(charPlus((char) 1), (char) + (char) 1, "(char) + (char) 1");
    Tester.checkEqual(charPlus(Character.MAX_VALUE), (char) + Character.MAX_VALUE, "(char) + Character.MAX_VALUE");
    Tester.checkEqual(charMinus((char) 0), (char) - (char) 0, "(char) - (char) 0");
    Tester.checkEqual(charMinus((char) 1), (char) - (char) 1, "(char) - (char) 1");
    Tester.checkEqual(charMinus(Character.MAX_VALUE), (char) - Character.MAX_VALUE, "(char) - Character.MAX_VALUE");
    Tester.checkEqual(charBitNot((char) 0), (char) ~ (char) 0, "(char) ~ (char) 0");
    Tester.checkEqual(charBitNot((char) 1), (char) ~ (char) 1, "(char) ~ (char) 1");
    Tester.checkEqual(charBitNot(Character.MAX_VALUE), (char) ~ Character.MAX_VALUE, "(char) ~ Character.MAX_VALUE");
    Tester.checkEqual(charTimes((char) 0, (char) 0), (char) ((char) 0 * (char) 0), "(char) ((char) 0 * (char) 0)");
    Tester.checkEqual(charTimes((char) 0, (char) 1), (char) ((char) 0 * (char) 1), "(char) ((char) 0 * (char) 1)");
    Tester.checkEqual(charTimes((char) 0, Character.MAX_VALUE), (char) ((char) 0 * Character.MAX_VALUE), "(char) ((char) 0 * Character.MAX_VALUE)");
    Tester.checkEqual(charTimes((char) 1, (char) 0), (char) ((char) 1 * (char) 0), "(char) ((char) 1 * (char) 0)");
    Tester.checkEqual(charTimes((char) 1, (char) 1), (char) ((char) 1 * (char) 1), "(char) ((char) 1 * (char) 1)");
    Tester.checkEqual(charTimes((char) 1, Character.MAX_VALUE), (char) ((char) 1 * Character.MAX_VALUE), "(char) ((char) 1 * Character.MAX_VALUE)");
    Tester.checkEqual(charTimes(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE * (char) 0), "(char) (Character.MAX_VALUE * (char) 0)");
    Tester.checkEqual(charTimes(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE * (char) 1), "(char) (Character.MAX_VALUE * (char) 1)");
    Tester.checkEqual(charTimes(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE * Character.MAX_VALUE), "(char) (Character.MAX_VALUE * Character.MAX_VALUE)");
    Tester.checkEqual(charDiv((char) 0, (char) 1), (char) ((char) 0 / (char) 1), "(char) ((char) 0 / (char) 1)");
    Tester.checkEqual(charDiv((char) 0, Character.MAX_VALUE), (char) ((char) 0 / Character.MAX_VALUE), "(char) ((char) 0 / Character.MAX_VALUE)");
    Tester.checkEqual(charDiv((char) 1, (char) 1), (char) ((char) 1 / (char) 1), "(char) ((char) 1 / (char) 1)");
    Tester.checkEqual(charDiv((char) 1, Character.MAX_VALUE), (char) ((char) 1 / Character.MAX_VALUE), "(char) ((char) 1 / Character.MAX_VALUE)");
    Tester.checkEqual(charDiv(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE / (char) 1), "(char) (Character.MAX_VALUE / (char) 1)");
    Tester.checkEqual(charDiv(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE / Character.MAX_VALUE), "(char) (Character.MAX_VALUE / Character.MAX_VALUE)");
    Tester.checkEqual(charRem((char) 0, (char) 1), (char) ((char) 0 % (char) 1), "(char) ((char) 0 % (char) 1)");
    Tester.checkEqual(charRem((char) 0, Character.MAX_VALUE), (char) ((char) 0 % Character.MAX_VALUE), "(char) ((char) 0 % Character.MAX_VALUE)");
    Tester.checkEqual(charRem((char) 1, (char) 1), (char) ((char) 1 % (char) 1), "(char) ((char) 1 % (char) 1)");
    Tester.checkEqual(charRem((char) 1, Character.MAX_VALUE), (char) ((char) 1 % Character.MAX_VALUE), "(char) ((char) 1 % Character.MAX_VALUE)");
    Tester.checkEqual(charRem(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE % (char) 1), "(char) (Character.MAX_VALUE % (char) 1)");
    Tester.checkEqual(charRem(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE % Character.MAX_VALUE), "(char) (Character.MAX_VALUE % Character.MAX_VALUE)");
    Tester.checkEqual(charAdd((char) 0, (char) 0), (char) ((char) 0 + (char) 0), "(char) ((char) 0 + (char) 0)");
    Tester.checkEqual(charAdd((char) 0, (char) 1), (char) ((char) 0 + (char) 1), "(char) ((char) 0 + (char) 1)");
    Tester.checkEqual(charAdd((char) 0, Character.MAX_VALUE), (char) ((char) 0 + Character.MAX_VALUE), "(char) ((char) 0 + Character.MAX_VALUE)");
    Tester.checkEqual(charAdd((char) 1, (char) 0), (char) ((char) 1 + (char) 0), "(char) ((char) 1 + (char) 0)");
    Tester.checkEqual(charAdd((char) 1, (char) 1), (char) ((char) 1 + (char) 1), "(char) ((char) 1 + (char) 1)");
    Tester.checkEqual(charAdd((char) 1, Character.MAX_VALUE), (char) ((char) 1 + Character.MAX_VALUE), "(char) ((char) 1 + Character.MAX_VALUE)");
    Tester.checkEqual(charAdd(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE + (char) 0), "(char) (Character.MAX_VALUE + (char) 0)");
    Tester.checkEqual(charAdd(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE + (char) 1), "(char) (Character.MAX_VALUE + (char) 1)");
    Tester.checkEqual(charAdd(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE + Character.MAX_VALUE), "(char) (Character.MAX_VALUE + Character.MAX_VALUE)");
    Tester.checkEqual(charSub((char) 0, (char) 0), (char) ((char) 0 - (char) 0), "(char) ((char) 0 - (char) 0)");
    Tester.checkEqual(charSub((char) 0, (char) 1), (char) ((char) 0 - (char) 1), "(char) ((char) 0 - (char) 1)");
    Tester.checkEqual(charSub((char) 0, Character.MAX_VALUE), (char) ((char) 0 - Character.MAX_VALUE), "(char) ((char) 0 - Character.MAX_VALUE)");
    Tester.checkEqual(charSub((char) 1, (char) 0), (char) ((char) 1 - (char) 0), "(char) ((char) 1 - (char) 0)");
    Tester.checkEqual(charSub((char) 1, (char) 1), (char) ((char) 1 - (char) 1), "(char) ((char) 1 - (char) 1)");
    Tester.checkEqual(charSub((char) 1, Character.MAX_VALUE), (char) ((char) 1 - Character.MAX_VALUE), "(char) ((char) 1 - Character.MAX_VALUE)");
    Tester.checkEqual(charSub(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE - (char) 0), "(char) (Character.MAX_VALUE - (char) 0)");
    Tester.checkEqual(charSub(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE - (char) 1), "(char) (Character.MAX_VALUE - (char) 1)");
    Tester.checkEqual(charSub(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE - Character.MAX_VALUE), "(char) (Character.MAX_VALUE - Character.MAX_VALUE)");
    Tester.checkEqual(charShl((char) 0, (char) 0), (char) ((char) 0 << (char) 0), "(char) ((char) 0 << (char) 0)");
    Tester.checkEqual(charShl((char) 0, (char) 1), (char) ((char) 0 << (char) 1), "(char) ((char) 0 << (char) 1)");
    Tester.checkEqual(charShl((char) 0, Character.MAX_VALUE), (char) ((char) 0 << Character.MAX_VALUE), "(char) ((char) 0 << Character.MAX_VALUE)");
    Tester.checkEqual(charShl((char) 1, (char) 0), (char) ((char) 1 << (char) 0), "(char) ((char) 1 << (char) 0)");
    Tester.checkEqual(charShl((char) 1, (char) 1), (char) ((char) 1 << (char) 1), "(char) ((char) 1 << (char) 1)");
    Tester.checkEqual(charShl((char) 1, Character.MAX_VALUE), (char) ((char) 1 << Character.MAX_VALUE), "(char) ((char) 1 << Character.MAX_VALUE)");
    Tester.checkEqual(charShl(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE << (char) 0), "(char) (Character.MAX_VALUE << (char) 0)");
    Tester.checkEqual(charShl(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE << (char) 1), "(char) (Character.MAX_VALUE << (char) 1)");
    Tester.checkEqual(charShl(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE << Character.MAX_VALUE), "(char) (Character.MAX_VALUE << Character.MAX_VALUE)");
    Tester.checkEqual(charShr((char) 0, (char) 0), (char) ((char) 0 >> (char) 0), "(char) ((char) 0 >> (char) 0)");
    Tester.checkEqual(charShr((char) 0, (char) 1), (char) ((char) 0 >> (char) 1), "(char) ((char) 0 >> (char) 1)");
    Tester.checkEqual(charShr((char) 0, Character.MAX_VALUE), (char) ((char) 0 >> Character.MAX_VALUE), "(char) ((char) 0 >> Character.MAX_VALUE)");
    Tester.checkEqual(charShr((char) 1, (char) 0), (char) ((char) 1 >> (char) 0), "(char) ((char) 1 >> (char) 0)");
    Tester.checkEqual(charShr((char) 1, (char) 1), (char) ((char) 1 >> (char) 1), "(char) ((char) 1 >> (char) 1)");
    Tester.checkEqual(charShr((char) 1, Character.MAX_VALUE), (char) ((char) 1 >> Character.MAX_VALUE), "(char) ((char) 1 >> Character.MAX_VALUE)");
    Tester.checkEqual(charShr(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE >> (char) 0), "(char) (Character.MAX_VALUE >> (char) 0)");
    Tester.checkEqual(charShr(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE >> (char) 1), "(char) (Character.MAX_VALUE >> (char) 1)");
    Tester.checkEqual(charShr(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE >> Character.MAX_VALUE), "(char) (Character.MAX_VALUE >> Character.MAX_VALUE)");
    Tester.checkEqual(charUshr((char) 0, (char) 0), (char) ((char) 0 >>> (char) 0), "(char) ((char) 0 >>> (char) 0)");
    Tester.checkEqual(charUshr((char) 0, (char) 1), (char) ((char) 0 >>> (char) 1), "(char) ((char) 0 >>> (char) 1)");
    Tester.checkEqual(charUshr((char) 0, Character.MAX_VALUE), (char) ((char) 0 >>> Character.MAX_VALUE), "(char) ((char) 0 >>> Character.MAX_VALUE)");
    Tester.checkEqual(charUshr((char) 1, (char) 0), (char) ((char) 1 >>> (char) 0), "(char) ((char) 1 >>> (char) 0)");
    Tester.checkEqual(charUshr((char) 1, (char) 1), (char) ((char) 1 >>> (char) 1), "(char) ((char) 1 >>> (char) 1)");
    Tester.checkEqual(charUshr((char) 1, Character.MAX_VALUE), (char) ((char) 1 >>> Character.MAX_VALUE), "(char) ((char) 1 >>> Character.MAX_VALUE)");
    Tester.checkEqual(charUshr(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE >>> (char) 0), "(char) (Character.MAX_VALUE >>> (char) 0)");
    Tester.checkEqual(charUshr(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE >>> (char) 1), "(char) (Character.MAX_VALUE >>> (char) 1)");
    Tester.checkEqual(charUshr(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE >>> Character.MAX_VALUE), "(char) (Character.MAX_VALUE >>> Character.MAX_VALUE)");
    Tester.checkEqual(charLt((char) 0, (char) 0), (char) 0 < (char) 0, "(char) 0 < (char) 0");
    Tester.checkEqual(charLt((char) 0, (char) 1), (char) 0 < (char) 1, "(char) 0 < (char) 1");
    Tester.checkEqual(charLt((char) 0, Character.MAX_VALUE), (char) 0 < Character.MAX_VALUE, "(char) 0 < Character.MAX_VALUE");
    Tester.checkEqual(charLt((char) 1, (char) 0), (char) 1 < (char) 0, "(char) 1 < (char) 0");
    Tester.checkEqual(charLt((char) 1, (char) 1), (char) 1 < (char) 1, "(char) 1 < (char) 1");
    Tester.checkEqual(charLt((char) 1, Character.MAX_VALUE), (char) 1 < Character.MAX_VALUE, "(char) 1 < Character.MAX_VALUE");
    Tester.checkEqual(charLt(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE < (char) 0, "Character.MAX_VALUE < (char) 0");
    Tester.checkEqual(charLt(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE < (char) 1, "Character.MAX_VALUE < (char) 1");
    Tester.checkEqual(charLt(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE < Character.MAX_VALUE, "Character.MAX_VALUE < Character.MAX_VALUE");
    Tester.checkEqual(charGt((char) 0, (char) 0), (char) 0 > (char) 0, "(char) 0 > (char) 0");
    Tester.checkEqual(charGt((char) 0, (char) 1), (char) 0 > (char) 1, "(char) 0 > (char) 1");
    Tester.checkEqual(charGt((char) 0, Character.MAX_VALUE), (char) 0 > Character.MAX_VALUE, "(char) 0 > Character.MAX_VALUE");
    Tester.checkEqual(charGt((char) 1, (char) 0), (char) 1 > (char) 0, "(char) 1 > (char) 0");
    Tester.checkEqual(charGt((char) 1, (char) 1), (char) 1 > (char) 1, "(char) 1 > (char) 1");
    Tester.checkEqual(charGt((char) 1, Character.MAX_VALUE), (char) 1 > Character.MAX_VALUE, "(char) 1 > Character.MAX_VALUE");
    Tester.checkEqual(charGt(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE > (char) 0, "Character.MAX_VALUE > (char) 0");
    Tester.checkEqual(charGt(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE > (char) 1, "Character.MAX_VALUE > (char) 1");
    Tester.checkEqual(charGt(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE > Character.MAX_VALUE, "Character.MAX_VALUE > Character.MAX_VALUE");
    Tester.checkEqual(charLe((char) 0, (char) 0), (char) 0 <= (char) 0, "(char) 0 <= (char) 0");
    Tester.checkEqual(charLe((char) 0, (char) 1), (char) 0 <= (char) 1, "(char) 0 <= (char) 1");
    Tester.checkEqual(charLe((char) 0, Character.MAX_VALUE), (char) 0 <= Character.MAX_VALUE, "(char) 0 <= Character.MAX_VALUE");
    Tester.checkEqual(charLe((char) 1, (char) 0), (char) 1 <= (char) 0, "(char) 1 <= (char) 0");
    Tester.checkEqual(charLe((char) 1, (char) 1), (char) 1 <= (char) 1, "(char) 1 <= (char) 1");
    Tester.checkEqual(charLe((char) 1, Character.MAX_VALUE), (char) 1 <= Character.MAX_VALUE, "(char) 1 <= Character.MAX_VALUE");
    Tester.checkEqual(charLe(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE <= (char) 0, "Character.MAX_VALUE <= (char) 0");
    Tester.checkEqual(charLe(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE <= (char) 1, "Character.MAX_VALUE <= (char) 1");
    Tester.checkEqual(charLe(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE <= Character.MAX_VALUE, "Character.MAX_VALUE <= Character.MAX_VALUE");
    Tester.checkEqual(charGe((char) 0, (char) 0), (char) 0 >= (char) 0, "(char) 0 >= (char) 0");
    Tester.checkEqual(charGe((char) 0, (char) 1), (char) 0 >= (char) 1, "(char) 0 >= (char) 1");
    Tester.checkEqual(charGe((char) 0, Character.MAX_VALUE), (char) 0 >= Character.MAX_VALUE, "(char) 0 >= Character.MAX_VALUE");
    Tester.checkEqual(charGe((char) 1, (char) 0), (char) 1 >= (char) 0, "(char) 1 >= (char) 0");
    Tester.checkEqual(charGe((char) 1, (char) 1), (char) 1 >= (char) 1, "(char) 1 >= (char) 1");
    Tester.checkEqual(charGe((char) 1, Character.MAX_VALUE), (char) 1 >= Character.MAX_VALUE, "(char) 1 >= Character.MAX_VALUE");
    Tester.checkEqual(charGe(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE >= (char) 0, "Character.MAX_VALUE >= (char) 0");
    Tester.checkEqual(charGe(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE >= (char) 1, "Character.MAX_VALUE >= (char) 1");
    Tester.checkEqual(charGe(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE >= Character.MAX_VALUE, "Character.MAX_VALUE >= Character.MAX_VALUE");
    Tester.checkEqual(charEq((char) 0, (char) 0), (char) 0 == (char) 0, "(char) 0 == (char) 0");
    Tester.checkEqual(charEq((char) 0, (char) 1), (char) 0 == (char) 1, "(char) 0 == (char) 1");
    Tester.checkEqual(charEq((char) 0, Character.MAX_VALUE), (char) 0 == Character.MAX_VALUE, "(char) 0 == Character.MAX_VALUE");
    Tester.checkEqual(charEq((char) 1, (char) 0), (char) 1 == (char) 0, "(char) 1 == (char) 0");
    Tester.checkEqual(charEq((char) 1, (char) 1), (char) 1 == (char) 1, "(char) 1 == (char) 1");
    Tester.checkEqual(charEq((char) 1, Character.MAX_VALUE), (char) 1 == Character.MAX_VALUE, "(char) 1 == Character.MAX_VALUE");
    Tester.checkEqual(charEq(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE == (char) 0, "Character.MAX_VALUE == (char) 0");
    Tester.checkEqual(charEq(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE == (char) 1, "Character.MAX_VALUE == (char) 1");
    Tester.checkEqual(charEq(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE == Character.MAX_VALUE, "Character.MAX_VALUE == Character.MAX_VALUE");
    Tester.checkEqual(charNe((char) 0, (char) 0), (char) 0 != (char) 0, "(char) 0 != (char) 0");
    Tester.checkEqual(charNe((char) 0, (char) 1), (char) 0 != (char) 1, "(char) 0 != (char) 1");
    Tester.checkEqual(charNe((char) 0, Character.MAX_VALUE), (char) 0 != Character.MAX_VALUE, "(char) 0 != Character.MAX_VALUE");
    Tester.checkEqual(charNe((char) 1, (char) 0), (char) 1 != (char) 0, "(char) 1 != (char) 0");
    Tester.checkEqual(charNe((char) 1, (char) 1), (char) 1 != (char) 1, "(char) 1 != (char) 1");
    Tester.checkEqual(charNe((char) 1, Character.MAX_VALUE), (char) 1 != Character.MAX_VALUE, "(char) 1 != Character.MAX_VALUE");
    Tester.checkEqual(charNe(Character.MAX_VALUE, (char) 0), Character.MAX_VALUE != (char) 0, "Character.MAX_VALUE != (char) 0");
    Tester.checkEqual(charNe(Character.MAX_VALUE, (char) 1), Character.MAX_VALUE != (char) 1, "Character.MAX_VALUE != (char) 1");
    Tester.checkEqual(charNe(Character.MAX_VALUE, Character.MAX_VALUE), Character.MAX_VALUE != Character.MAX_VALUE, "Character.MAX_VALUE != Character.MAX_VALUE");
    Tester.checkEqual(charAnd((char) 0, (char) 0), (char) ((char) 0 & (char) 0), "(char) ((char) 0 & (char) 0)");
    Tester.checkEqual(charAnd((char) 0, (char) 1), (char) ((char) 0 & (char) 1), "(char) ((char) 0 & (char) 1)");
    Tester.checkEqual(charAnd((char) 0, Character.MAX_VALUE), (char) ((char) 0 & Character.MAX_VALUE), "(char) ((char) 0 & Character.MAX_VALUE)");
    Tester.checkEqual(charAnd((char) 1, (char) 0), (char) ((char) 1 & (char) 0), "(char) ((char) 1 & (char) 0)");
    Tester.checkEqual(charAnd((char) 1, (char) 1), (char) ((char) 1 & (char) 1), "(char) ((char) 1 & (char) 1)");
    Tester.checkEqual(charAnd((char) 1, Character.MAX_VALUE), (char) ((char) 1 & Character.MAX_VALUE), "(char) ((char) 1 & Character.MAX_VALUE)");
    Tester.checkEqual(charAnd(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE & (char) 0), "(char) (Character.MAX_VALUE & (char) 0)");
    Tester.checkEqual(charAnd(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE & (char) 1), "(char) (Character.MAX_VALUE & (char) 1)");
    Tester.checkEqual(charAnd(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE & Character.MAX_VALUE), "(char) (Character.MAX_VALUE & Character.MAX_VALUE)");
    Tester.checkEqual(charXor((char) 0, (char) 0), (char) ((char) 0 ^ (char) 0), "(char) ((char) 0 ^ (char) 0)");
    Tester.checkEqual(charXor((char) 0, (char) 1), (char) ((char) 0 ^ (char) 1), "(char) ((char) 0 ^ (char) 1)");
    Tester.checkEqual(charXor((char) 0, Character.MAX_VALUE), (char) ((char) 0 ^ Character.MAX_VALUE), "(char) ((char) 0 ^ Character.MAX_VALUE)");
    Tester.checkEqual(charXor((char) 1, (char) 0), (char) ((char) 1 ^ (char) 0), "(char) ((char) 1 ^ (char) 0)");
    Tester.checkEqual(charXor((char) 1, (char) 1), (char) ((char) 1 ^ (char) 1), "(char) ((char) 1 ^ (char) 1)");
    Tester.checkEqual(charXor((char) 1, Character.MAX_VALUE), (char) ((char) 1 ^ Character.MAX_VALUE), "(char) ((char) 1 ^ Character.MAX_VALUE)");
    Tester.checkEqual(charXor(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE ^ (char) 0), "(char) (Character.MAX_VALUE ^ (char) 0)");
    Tester.checkEqual(charXor(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE ^ (char) 1), "(char) (Character.MAX_VALUE ^ (char) 1)");
    Tester.checkEqual(charXor(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE ^ Character.MAX_VALUE), "(char) (Character.MAX_VALUE ^ Character.MAX_VALUE)");
    Tester.checkEqual(charOr((char) 0, (char) 0), (char) ((char) 0 | (char) 0), "(char) ((char) 0 | (char) 0)");
    Tester.checkEqual(charOr((char) 0, (char) 1), (char) ((char) 0 | (char) 1), "(char) ((char) 0 | (char) 1)");
    Tester.checkEqual(charOr((char) 0, Character.MAX_VALUE), (char) ((char) 0 | Character.MAX_VALUE), "(char) ((char) 0 | Character.MAX_VALUE)");
    Tester.checkEqual(charOr((char) 1, (char) 0), (char) ((char) 1 | (char) 0), "(char) ((char) 1 | (char) 0)");
    Tester.checkEqual(charOr((char) 1, (char) 1), (char) ((char) 1 | (char) 1), "(char) ((char) 1 | (char) 1)");
    Tester.checkEqual(charOr((char) 1, Character.MAX_VALUE), (char) ((char) 1 | Character.MAX_VALUE), "(char) ((char) 1 | Character.MAX_VALUE)");
    Tester.checkEqual(charOr(Character.MAX_VALUE, (char) 0), (char) (Character.MAX_VALUE | (char) 0), "(char) (Character.MAX_VALUE | (char) 0)");
    Tester.checkEqual(charOr(Character.MAX_VALUE, (char) 1), (char) (Character.MAX_VALUE | (char) 1), "(char) (Character.MAX_VALUE | (char) 1)");
    Tester.checkEqual(charOr(Character.MAX_VALUE, Character.MAX_VALUE), (char) (Character.MAX_VALUE | Character.MAX_VALUE), "(char) (Character.MAX_VALUE | Character.MAX_VALUE)");
  }
  static void charSwitch() {
    switch(0) {
      case ((((char) + (char) 0) == 0) ? 0 : 0):
      case ((((char) + (char) 1) == 0) ? 1 : 1):
      case ((((char) + Character.MAX_VALUE) == 0) ? 2 : 2):
      case ((((char) - (char) 0) == 0) ? 3 : 3):
      case ((((char) - (char) 1) == 0) ? 4 : 4):
      case ((((char) - Character.MAX_VALUE) == 0) ? 5 : 5):
      case ((((char) ~ (char) 0) == 0) ? 6 : 6):
      case ((((char) ~ (char) 1) == 0) ? 7 : 7):
      case ((((char) ~ Character.MAX_VALUE) == 0) ? 8 : 8):
      case ((((char) ((char) 0 * (char) 0)) == 0) ? 9 : 9):
      case ((((char) ((char) 0 * (char) 1)) == 0) ? 10 : 10):
      case ((((char) ((char) 0 * Character.MAX_VALUE)) == 0) ? 11 : 11):
      case ((((char) ((char) 1 * (char) 0)) == 0) ? 12 : 12):
      case ((((char) ((char) 1 * (char) 1)) == 0) ? 13 : 13):
      case ((((char) ((char) 1 * Character.MAX_VALUE)) == 0) ? 14 : 14):
      case ((((char) (Character.MAX_VALUE * (char) 0)) == 0) ? 15 : 15):
      case ((((char) (Character.MAX_VALUE * (char) 1)) == 0) ? 16 : 16):
      case ((((char) (Character.MAX_VALUE * Character.MAX_VALUE)) == 0) ? 17 : 17):
      case ((((char) ((char) 0 / (char) 1)) == 0) ? 18 : 18):
      case ((((char) ((char) 0 / Character.MAX_VALUE)) == 0) ? 19 : 19):
      case ((((char) ((char) 1 / (char) 1)) == 0) ? 20 : 20):
      case ((((char) ((char) 1 / Character.MAX_VALUE)) == 0) ? 21 : 21):
      case ((((char) (Character.MAX_VALUE / (char) 1)) == 0) ? 22 : 22):
      case ((((char) (Character.MAX_VALUE / Character.MAX_VALUE)) == 0) ? 23 : 23):
      case ((((char) ((char) 0 % (char) 1)) == 0) ? 24 : 24):
      case ((((char) ((char) 0 % Character.MAX_VALUE)) == 0) ? 25 : 25):
      case ((((char) ((char) 1 % (char) 1)) == 0) ? 26 : 26):
      case ((((char) ((char) 1 % Character.MAX_VALUE)) == 0) ? 27 : 27):
      case ((((char) (Character.MAX_VALUE % (char) 1)) == 0) ? 28 : 28):
      case ((((char) (Character.MAX_VALUE % Character.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((char) ((char) 0 + (char) 0)) == 0) ? 30 : 30):
      case ((((char) ((char) 0 + (char) 1)) == 0) ? 31 : 31):
      case ((((char) ((char) 0 + Character.MAX_VALUE)) == 0) ? 32 : 32):
      case ((((char) ((char) 1 + (char) 0)) == 0) ? 33 : 33):
      case ((((char) ((char) 1 + (char) 1)) == 0) ? 34 : 34):
      case ((((char) ((char) 1 + Character.MAX_VALUE)) == 0) ? 35 : 35):
      case ((((char) (Character.MAX_VALUE + (char) 0)) == 0) ? 36 : 36):
      case ((((char) (Character.MAX_VALUE + (char) 1)) == 0) ? 37 : 37):
      case ((((char) (Character.MAX_VALUE + Character.MAX_VALUE)) == 0) ? 38 : 38):
      case ((((char) ((char) 0 - (char) 0)) == 0) ? 39 : 39):
      case ((((char) ((char) 0 - (char) 1)) == 0) ? 40 : 40):
      case ((((char) ((char) 0 - Character.MAX_VALUE)) == 0) ? 41 : 41):
      case ((((char) ((char) 1 - (char) 0)) == 0) ? 42 : 42):
      case ((((char) ((char) 1 - (char) 1)) == 0) ? 43 : 43):
      case ((((char) ((char) 1 - Character.MAX_VALUE)) == 0) ? 44 : 44):
      case ((((char) (Character.MAX_VALUE - (char) 0)) == 0) ? 45 : 45):
      case ((((char) (Character.MAX_VALUE - (char) 1)) == 0) ? 46 : 46):
      case ((((char) (Character.MAX_VALUE - Character.MAX_VALUE)) == 0) ? 47 : 47):
      case ((((char) ((char) 0 << (char) 0)) == 0) ? 48 : 48):
      case ((((char) ((char) 0 << (char) 1)) == 0) ? 49 : 49):
      case ((((char) ((char) 0 << Character.MAX_VALUE)) == 0) ? 50 : 50):
      case ((((char) ((char) 1 << (char) 0)) == 0) ? 51 : 51):
      case ((((char) ((char) 1 << (char) 1)) == 0) ? 52 : 52):
      case ((((char) ((char) 1 << Character.MAX_VALUE)) == 0) ? 53 : 53):
      case ((((char) (Character.MAX_VALUE << (char) 0)) == 0) ? 54 : 54):
      case ((((char) (Character.MAX_VALUE << (char) 1)) == 0) ? 55 : 55):
      case ((((char) (Character.MAX_VALUE << Character.MAX_VALUE)) == 0) ? 56 : 56):
      case ((((char) ((char) 0 >> (char) 0)) == 0) ? 57 : 57):
      case ((((char) ((char) 0 >> (char) 1)) == 0) ? 58 : 58):
      case ((((char) ((char) 0 >> Character.MAX_VALUE)) == 0) ? 59 : 59):
      case ((((char) ((char) 1 >> (char) 0)) == 0) ? 60 : 60):
      case ((((char) ((char) 1 >> (char) 1)) == 0) ? 61 : 61):
      case ((((char) ((char) 1 >> Character.MAX_VALUE)) == 0) ? 62 : 62):
      case ((((char) (Character.MAX_VALUE >> (char) 0)) == 0) ? 63 : 63):
      case ((((char) (Character.MAX_VALUE >> (char) 1)) == 0) ? 64 : 64):
      case ((((char) (Character.MAX_VALUE >> Character.MAX_VALUE)) == 0) ? 65 : 65):
      case ((((char) ((char) 0 >>> (char) 0)) == 0) ? 66 : 66):
      case ((((char) ((char) 0 >>> (char) 1)) == 0) ? 67 : 67):
      case ((((char) ((char) 0 >>> Character.MAX_VALUE)) == 0) ? 68 : 68):
      case ((((char) ((char) 1 >>> (char) 0)) == 0) ? 69 : 69):
      case ((((char) ((char) 1 >>> (char) 1)) == 0) ? 70 : 70):
      case ((((char) ((char) 1 >>> Character.MAX_VALUE)) == 0) ? 71 : 71):
      case ((((char) (Character.MAX_VALUE >>> (char) 0)) == 0) ? 72 : 72):
      case ((((char) (Character.MAX_VALUE >>> (char) 1)) == 0) ? 73 : 73):
      case ((((char) (Character.MAX_VALUE >>> Character.MAX_VALUE)) == 0) ? 74 : 74):
      case (((char) 0 < (char) 0) ? 75 : 75):
      case (((char) 0 < (char) 1) ? 76 : 76):
      case (((char) 0 < Character.MAX_VALUE) ? 77 : 77):
      case (((char) 1 < (char) 0) ? 78 : 78):
      case (((char) 1 < (char) 1) ? 79 : 79):
      case (((char) 1 < Character.MAX_VALUE) ? 80 : 80):
      case ((Character.MAX_VALUE < (char) 0) ? 81 : 81):
      case ((Character.MAX_VALUE < (char) 1) ? 82 : 82):
      case ((Character.MAX_VALUE < Character.MAX_VALUE) ? 83 : 83):
      case (((char) 0 > (char) 0) ? 84 : 84):
      case (((char) 0 > (char) 1) ? 85 : 85):
      case (((char) 0 > Character.MAX_VALUE) ? 86 : 86):
      case (((char) 1 > (char) 0) ? 87 : 87):
      case (((char) 1 > (char) 1) ? 88 : 88):
      case (((char) 1 > Character.MAX_VALUE) ? 89 : 89):
      case ((Character.MAX_VALUE > (char) 0) ? 90 : 90):
      case ((Character.MAX_VALUE > (char) 1) ? 91 : 91):
      case ((Character.MAX_VALUE > Character.MAX_VALUE) ? 92 : 92):
      case (((char) 0 <= (char) 0) ? 93 : 93):
      case (((char) 0 <= (char) 1) ? 94 : 94):
      case (((char) 0 <= Character.MAX_VALUE) ? 95 : 95):
      case (((char) 1 <= (char) 0) ? 96 : 96):
      case (((char) 1 <= (char) 1) ? 97 : 97):
      case (((char) 1 <= Character.MAX_VALUE) ? 98 : 98):
      case ((Character.MAX_VALUE <= (char) 0) ? 99 : 99):
      case ((Character.MAX_VALUE <= (char) 1) ? 100 : 100):
      case ((Character.MAX_VALUE <= Character.MAX_VALUE) ? 101 : 101):
      case (((char) 0 >= (char) 0) ? 102 : 102):
      case (((char) 0 >= (char) 1) ? 103 : 103):
      case (((char) 0 >= Character.MAX_VALUE) ? 104 : 104):
      case (((char) 1 >= (char) 0) ? 105 : 105):
      case (((char) 1 >= (char) 1) ? 106 : 106):
      case (((char) 1 >= Character.MAX_VALUE) ? 107 : 107):
      case ((Character.MAX_VALUE >= (char) 0) ? 108 : 108):
      case ((Character.MAX_VALUE >= (char) 1) ? 109 : 109):
      case ((Character.MAX_VALUE >= Character.MAX_VALUE) ? 110 : 110):
      case (((char) 0 == (char) 0) ? 111 : 111):
      case (((char) 0 == (char) 1) ? 112 : 112):
      case (((char) 0 == Character.MAX_VALUE) ? 113 : 113):
      case (((char) 1 == (char) 0) ? 114 : 114):
      case (((char) 1 == (char) 1) ? 115 : 115):
      case (((char) 1 == Character.MAX_VALUE) ? 116 : 116):
      case ((Character.MAX_VALUE == (char) 0) ? 117 : 117):
      case ((Character.MAX_VALUE == (char) 1) ? 118 : 118):
      case ((Character.MAX_VALUE == Character.MAX_VALUE) ? 119 : 119):
      case (((char) 0 != (char) 0) ? 120 : 120):
      case (((char) 0 != (char) 1) ? 121 : 121):
      case (((char) 0 != Character.MAX_VALUE) ? 122 : 122):
      case (((char) 1 != (char) 0) ? 123 : 123):
      case (((char) 1 != (char) 1) ? 124 : 124):
      case (((char) 1 != Character.MAX_VALUE) ? 125 : 125):
      case ((Character.MAX_VALUE != (char) 0) ? 126 : 126):
      case ((Character.MAX_VALUE != (char) 1) ? 127 : 127):
      case ((Character.MAX_VALUE != Character.MAX_VALUE) ? 128 : 128):
      case ((((char) ((char) 0 & (char) 0)) == 0) ? 129 : 129):
      case ((((char) ((char) 0 & (char) 1)) == 0) ? 130 : 130):
      case ((((char) ((char) 0 & Character.MAX_VALUE)) == 0) ? 131 : 131):
      case ((((char) ((char) 1 & (char) 0)) == 0) ? 132 : 132):
      case ((((char) ((char) 1 & (char) 1)) == 0) ? 133 : 133):
      case ((((char) ((char) 1 & Character.MAX_VALUE)) == 0) ? 134 : 134):
      case ((((char) (Character.MAX_VALUE & (char) 0)) == 0) ? 135 : 135):
      case ((((char) (Character.MAX_VALUE & (char) 1)) == 0) ? 136 : 136):
      case ((((char) (Character.MAX_VALUE & Character.MAX_VALUE)) == 0) ? 137 : 137):
      case ((((char) ((char) 0 ^ (char) 0)) == 0) ? 138 : 138):
      case ((((char) ((char) 0 ^ (char) 1)) == 0) ? 139 : 139):
      case ((((char) ((char) 0 ^ Character.MAX_VALUE)) == 0) ? 140 : 140):
      case ((((char) ((char) 1 ^ (char) 0)) == 0) ? 141 : 141):
      case ((((char) ((char) 1 ^ (char) 1)) == 0) ? 142 : 142):
      case ((((char) ((char) 1 ^ Character.MAX_VALUE)) == 0) ? 143 : 143):
      case ((((char) (Character.MAX_VALUE ^ (char) 0)) == 0) ? 144 : 144):
      case ((((char) (Character.MAX_VALUE ^ (char) 1)) == 0) ? 145 : 145):
      case ((((char) (Character.MAX_VALUE ^ Character.MAX_VALUE)) == 0) ? 146 : 146):
      case ((((char) ((char) 0 | (char) 0)) == 0) ? 147 : 147):
      case ((((char) ((char) 0 | (char) 1)) == 0) ? 148 : 148):
      case ((((char) ((char) 0 | Character.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((char) ((char) 1 | (char) 0)) == 0) ? 150 : 150):
      case ((((char) ((char) 1 | (char) 1)) == 0) ? 151 : 151):
      case ((((char) ((char) 1 | Character.MAX_VALUE)) == 0) ? 152 : 152):
      case ((((char) (Character.MAX_VALUE | (char) 0)) == 0) ? 153 : 153):
      case ((((char) (Character.MAX_VALUE | (char) 1)) == 0) ? 154 : 154):
      case ((((char) (Character.MAX_VALUE | Character.MAX_VALUE)) == 0) ? 155 : 155):
      default:
    }
  }

  // --------
  // int tests
  static int intPlus(int x) { return (int) + x; }
  static int intMinus(int x) { return (int) - x; }
  static int intBitNot(int x) { return (int) ~ x; }
  static int intTimes(int x, int y) { return (int) (x * y); }
  static int intDiv(int x, int y) { return (int) (x / y); }
  static int intRem(int x, int y) { return (int) (x % y); }
  static int intAdd(int x, int y) { return (int) (x + y); }
  static int intSub(int x, int y) { return (int) (x - y); }
  static int intShl(int x, int y) { return (int) (x << y); }
  static int intShr(int x, int y) { return (int) (x >> y); }
  static int intUshr(int x, int y) { return (int) (x >>> y); }
  static boolean intLt(int x, int y) { return x < y; }
  static boolean intGt(int x, int y) { return x > y; }
  static boolean intLe(int x, int y) { return x <= y; }
  static boolean intGe(int x, int y) { return x >= y; }
  static boolean intEq(int x, int y) { return x == y; }
  static boolean intNe(int x, int y) { return x != y; }
  static int intAnd(int x, int y) { return (int) (x & y); }
  static int intXor(int x, int y) { return (int) (x ^ y); }
  static int intOr(int x, int y) { return (int) (x | y); }
  static void intTest() {
    Tester.checkEqual(intPlus(Integer.MIN_VALUE), (int) + Integer.MIN_VALUE, "(int) + Integer.MIN_VALUE");
    Tester.checkEqual(intPlus(-1), (int) + -1, "(int) + -1");
    Tester.checkEqual(intPlus(0), (int) + 0, "(int) + 0");
    Tester.checkEqual(intPlus(1), (int) + 1, "(int) + 1");
    Tester.checkEqual(intPlus(Integer.MAX_VALUE), (int) + Integer.MAX_VALUE, "(int) + Integer.MAX_VALUE");
    Tester.checkEqual(intMinus(Integer.MIN_VALUE), (int) - Integer.MIN_VALUE, "(int) - Integer.MIN_VALUE");
    Tester.checkEqual(intMinus(-1), (int) - -1, "(int) - -1");
    Tester.checkEqual(intMinus(0), (int) - 0, "(int) - 0");
    Tester.checkEqual(intMinus(1), (int) - 1, "(int) - 1");
    Tester.checkEqual(intMinus(Integer.MAX_VALUE), (int) - Integer.MAX_VALUE, "(int) - Integer.MAX_VALUE");
    Tester.checkEqual(intBitNot(Integer.MIN_VALUE), (int) ~ Integer.MIN_VALUE, "(int) ~ Integer.MIN_VALUE");
    Tester.checkEqual(intBitNot(-1), (int) ~ -1, "(int) ~ -1");
    Tester.checkEqual(intBitNot(0), (int) ~ 0, "(int) ~ 0");
    Tester.checkEqual(intBitNot(1), (int) ~ 1, "(int) ~ 1");
    Tester.checkEqual(intBitNot(Integer.MAX_VALUE), (int) ~ Integer.MAX_VALUE, "(int) ~ Integer.MAX_VALUE");
    Tester.checkEqual(intTimes(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE * Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE * Integer.MIN_VALUE)");
    Tester.checkEqual(intTimes(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE * -1), "(int) (Integer.MIN_VALUE * -1)");
    Tester.checkEqual(intTimes(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE * 0), "(int) (Integer.MIN_VALUE * 0)");
    Tester.checkEqual(intTimes(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE * 1), "(int) (Integer.MIN_VALUE * 1)");
    Tester.checkEqual(intTimes(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE * Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE * Integer.MAX_VALUE)");
    Tester.checkEqual(intTimes(-1, Integer.MIN_VALUE), (int) (-1 * Integer.MIN_VALUE), "(int) (-1 * Integer.MIN_VALUE)");
    Tester.checkEqual(intTimes(-1, -1), (int) (-1 * -1), "(int) (-1 * -1)");
    Tester.checkEqual(intTimes(-1, 0), (int) (-1 * 0), "(int) (-1 * 0)");
    Tester.checkEqual(intTimes(-1, 1), (int) (-1 * 1), "(int) (-1 * 1)");
    Tester.checkEqual(intTimes(-1, Integer.MAX_VALUE), (int) (-1 * Integer.MAX_VALUE), "(int) (-1 * Integer.MAX_VALUE)");
    Tester.checkEqual(intTimes(0, Integer.MIN_VALUE), (int) (0 * Integer.MIN_VALUE), "(int) (0 * Integer.MIN_VALUE)");
    Tester.checkEqual(intTimes(0, -1), (int) (0 * -1), "(int) (0 * -1)");
    Tester.checkEqual(intTimes(0, 0), (int) (0 * 0), "(int) (0 * 0)");
    Tester.checkEqual(intTimes(0, 1), (int) (0 * 1), "(int) (0 * 1)");
    Tester.checkEqual(intTimes(0, Integer.MAX_VALUE), (int) (0 * Integer.MAX_VALUE), "(int) (0 * Integer.MAX_VALUE)");
    Tester.checkEqual(intTimes(1, Integer.MIN_VALUE), (int) (1 * Integer.MIN_VALUE), "(int) (1 * Integer.MIN_VALUE)");
    Tester.checkEqual(intTimes(1, -1), (int) (1 * -1), "(int) (1 * -1)");
    Tester.checkEqual(intTimes(1, 0), (int) (1 * 0), "(int) (1 * 0)");
    Tester.checkEqual(intTimes(1, 1), (int) (1 * 1), "(int) (1 * 1)");
    Tester.checkEqual(intTimes(1, Integer.MAX_VALUE), (int) (1 * Integer.MAX_VALUE), "(int) (1 * Integer.MAX_VALUE)");
    Tester.checkEqual(intTimes(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE * Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE * Integer.MIN_VALUE)");
    Tester.checkEqual(intTimes(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE * -1), "(int) (Integer.MAX_VALUE * -1)");
    Tester.checkEqual(intTimes(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE * 0), "(int) (Integer.MAX_VALUE * 0)");
    Tester.checkEqual(intTimes(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE * 1), "(int) (Integer.MAX_VALUE * 1)");
    Tester.checkEqual(intTimes(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE * Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE * Integer.MAX_VALUE)");
    Tester.checkEqual(intDiv(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE / Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE / Integer.MIN_VALUE)");
    Tester.checkEqual(intDiv(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE / -1), "(int) (Integer.MIN_VALUE / -1)");
    Tester.checkEqual(intDiv(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE / 1), "(int) (Integer.MIN_VALUE / 1)");
    Tester.checkEqual(intDiv(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE / Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE / Integer.MAX_VALUE)");
    Tester.checkEqual(intDiv(-1, Integer.MIN_VALUE), (int) (-1 / Integer.MIN_VALUE), "(int) (-1 / Integer.MIN_VALUE)");
    Tester.checkEqual(intDiv(-1, -1), (int) (-1 / -1), "(int) (-1 / -1)");
    Tester.checkEqual(intDiv(-1, 1), (int) (-1 / 1), "(int) (-1 / 1)");
    Tester.checkEqual(intDiv(-1, Integer.MAX_VALUE), (int) (-1 / Integer.MAX_VALUE), "(int) (-1 / Integer.MAX_VALUE)");
    Tester.checkEqual(intDiv(0, Integer.MIN_VALUE), (int) (0 / Integer.MIN_VALUE), "(int) (0 / Integer.MIN_VALUE)");
    Tester.checkEqual(intDiv(0, -1), (int) (0 / -1), "(int) (0 / -1)");
    Tester.checkEqual(intDiv(0, 1), (int) (0 / 1), "(int) (0 / 1)");
    Tester.checkEqual(intDiv(0, Integer.MAX_VALUE), (int) (0 / Integer.MAX_VALUE), "(int) (0 / Integer.MAX_VALUE)");
    Tester.checkEqual(intDiv(1, Integer.MIN_VALUE), (int) (1 / Integer.MIN_VALUE), "(int) (1 / Integer.MIN_VALUE)");
    Tester.checkEqual(intDiv(1, -1), (int) (1 / -1), "(int) (1 / -1)");
    Tester.checkEqual(intDiv(1, 1), (int) (1 / 1), "(int) (1 / 1)");
    Tester.checkEqual(intDiv(1, Integer.MAX_VALUE), (int) (1 / Integer.MAX_VALUE), "(int) (1 / Integer.MAX_VALUE)");
    Tester.checkEqual(intDiv(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE / Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE / Integer.MIN_VALUE)");
    Tester.checkEqual(intDiv(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE / -1), "(int) (Integer.MAX_VALUE / -1)");
    Tester.checkEqual(intDiv(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE / 1), "(int) (Integer.MAX_VALUE / 1)");
    Tester.checkEqual(intDiv(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE / Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE / Integer.MAX_VALUE)");
    Tester.checkEqual(intRem(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE % Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE % Integer.MIN_VALUE)");
    Tester.checkEqual(intRem(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE % -1), "(int) (Integer.MIN_VALUE % -1)");
    Tester.checkEqual(intRem(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE % 1), "(int) (Integer.MIN_VALUE % 1)");
    Tester.checkEqual(intRem(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE % Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE % Integer.MAX_VALUE)");
    Tester.checkEqual(intRem(-1, Integer.MIN_VALUE), (int) (-1 % Integer.MIN_VALUE), "(int) (-1 % Integer.MIN_VALUE)");
    Tester.checkEqual(intRem(-1, -1), (int) (-1 % -1), "(int) (-1 % -1)");
    Tester.checkEqual(intRem(-1, 1), (int) (-1 % 1), "(int) (-1 % 1)");
    Tester.checkEqual(intRem(-1, Integer.MAX_VALUE), (int) (-1 % Integer.MAX_VALUE), "(int) (-1 % Integer.MAX_VALUE)");
    Tester.checkEqual(intRem(0, Integer.MIN_VALUE), (int) (0 % Integer.MIN_VALUE), "(int) (0 % Integer.MIN_VALUE)");
    Tester.checkEqual(intRem(0, -1), (int) (0 % -1), "(int) (0 % -1)");
    Tester.checkEqual(intRem(0, 1), (int) (0 % 1), "(int) (0 % 1)");
    Tester.checkEqual(intRem(0, Integer.MAX_VALUE), (int) (0 % Integer.MAX_VALUE), "(int) (0 % Integer.MAX_VALUE)");
    Tester.checkEqual(intRem(1, Integer.MIN_VALUE), (int) (1 % Integer.MIN_VALUE), "(int) (1 % Integer.MIN_VALUE)");
    Tester.checkEqual(intRem(1, -1), (int) (1 % -1), "(int) (1 % -1)");
    Tester.checkEqual(intRem(1, 1), (int) (1 % 1), "(int) (1 % 1)");
    Tester.checkEqual(intRem(1, Integer.MAX_VALUE), (int) (1 % Integer.MAX_VALUE), "(int) (1 % Integer.MAX_VALUE)");
    Tester.checkEqual(intRem(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE % Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE % Integer.MIN_VALUE)");
    Tester.checkEqual(intRem(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE % -1), "(int) (Integer.MAX_VALUE % -1)");
    Tester.checkEqual(intRem(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE % 1), "(int) (Integer.MAX_VALUE % 1)");
    Tester.checkEqual(intRem(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE % Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE % Integer.MAX_VALUE)");
    Tester.checkEqual(intAdd(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE + Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE + Integer.MIN_VALUE)");
    Tester.checkEqual(intAdd(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE + -1), "(int) (Integer.MIN_VALUE + -1)");
    Tester.checkEqual(intAdd(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE + 0), "(int) (Integer.MIN_VALUE + 0)");
    Tester.checkEqual(intAdd(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE + 1), "(int) (Integer.MIN_VALUE + 1)");
    Tester.checkEqual(intAdd(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE + Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE + Integer.MAX_VALUE)");
    Tester.checkEqual(intAdd(-1, Integer.MIN_VALUE), (int) (-1 + Integer.MIN_VALUE), "(int) (-1 + Integer.MIN_VALUE)");
    Tester.checkEqual(intAdd(-1, -1), (int) (-1 + -1), "(int) (-1 + -1)");
    Tester.checkEqual(intAdd(-1, 0), (int) (-1 + 0), "(int) (-1 + 0)");
    Tester.checkEqual(intAdd(-1, 1), (int) (-1 + 1), "(int) (-1 + 1)");
    Tester.checkEqual(intAdd(-1, Integer.MAX_VALUE), (int) (-1 + Integer.MAX_VALUE), "(int) (-1 + Integer.MAX_VALUE)");
    Tester.checkEqual(intAdd(0, Integer.MIN_VALUE), (int) (0 + Integer.MIN_VALUE), "(int) (0 + Integer.MIN_VALUE)");
    Tester.checkEqual(intAdd(0, -1), (int) (0 + -1), "(int) (0 + -1)");
    Tester.checkEqual(intAdd(0, 0), (int) (0 + 0), "(int) (0 + 0)");
    Tester.checkEqual(intAdd(0, 1), (int) (0 + 1), "(int) (0 + 1)");
    Tester.checkEqual(intAdd(0, Integer.MAX_VALUE), (int) (0 + Integer.MAX_VALUE), "(int) (0 + Integer.MAX_VALUE)");
    Tester.checkEqual(intAdd(1, Integer.MIN_VALUE), (int) (1 + Integer.MIN_VALUE), "(int) (1 + Integer.MIN_VALUE)");
    Tester.checkEqual(intAdd(1, -1), (int) (1 + -1), "(int) (1 + -1)");
    Tester.checkEqual(intAdd(1, 0), (int) (1 + 0), "(int) (1 + 0)");
    Tester.checkEqual(intAdd(1, 1), (int) (1 + 1), "(int) (1 + 1)");
    Tester.checkEqual(intAdd(1, Integer.MAX_VALUE), (int) (1 + Integer.MAX_VALUE), "(int) (1 + Integer.MAX_VALUE)");
    Tester.checkEqual(intAdd(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE + Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE + Integer.MIN_VALUE)");
    Tester.checkEqual(intAdd(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE + -1), "(int) (Integer.MAX_VALUE + -1)");
    Tester.checkEqual(intAdd(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE + 0), "(int) (Integer.MAX_VALUE + 0)");
    Tester.checkEqual(intAdd(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE + 1), "(int) (Integer.MAX_VALUE + 1)");
    Tester.checkEqual(intAdd(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE + Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE + Integer.MAX_VALUE)");
    Tester.checkEqual(intSub(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE - Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE - Integer.MIN_VALUE)");
    Tester.checkEqual(intSub(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE - -1), "(int) (Integer.MIN_VALUE - -1)");
    Tester.checkEqual(intSub(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE - 0), "(int) (Integer.MIN_VALUE - 0)");
    Tester.checkEqual(intSub(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE - 1), "(int) (Integer.MIN_VALUE - 1)");
    Tester.checkEqual(intSub(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE - Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE - Integer.MAX_VALUE)");
    Tester.checkEqual(intSub(-1, Integer.MIN_VALUE), (int) (-1 - Integer.MIN_VALUE), "(int) (-1 - Integer.MIN_VALUE)");
    Tester.checkEqual(intSub(-1, -1), (int) (-1 - -1), "(int) (-1 - -1)");
    Tester.checkEqual(intSub(-1, 0), (int) (-1 - 0), "(int) (-1 - 0)");
    Tester.checkEqual(intSub(-1, 1), (int) (-1 - 1), "(int) (-1 - 1)");
    Tester.checkEqual(intSub(-1, Integer.MAX_VALUE), (int) (-1 - Integer.MAX_VALUE), "(int) (-1 - Integer.MAX_VALUE)");
    Tester.checkEqual(intSub(0, Integer.MIN_VALUE), (int) (0 - Integer.MIN_VALUE), "(int) (0 - Integer.MIN_VALUE)");
    Tester.checkEqual(intSub(0, -1), (int) (0 - -1), "(int) (0 - -1)");
    Tester.checkEqual(intSub(0, 0), (int) (0 - 0), "(int) (0 - 0)");
    Tester.checkEqual(intSub(0, 1), (int) (0 - 1), "(int) (0 - 1)");
    Tester.checkEqual(intSub(0, Integer.MAX_VALUE), (int) (0 - Integer.MAX_VALUE), "(int) (0 - Integer.MAX_VALUE)");
    Tester.checkEqual(intSub(1, Integer.MIN_VALUE), (int) (1 - Integer.MIN_VALUE), "(int) (1 - Integer.MIN_VALUE)");
    Tester.checkEqual(intSub(1, -1), (int) (1 - -1), "(int) (1 - -1)");
    Tester.checkEqual(intSub(1, 0), (int) (1 - 0), "(int) (1 - 0)");
    Tester.checkEqual(intSub(1, 1), (int) (1 - 1), "(int) (1 - 1)");
    Tester.checkEqual(intSub(1, Integer.MAX_VALUE), (int) (1 - Integer.MAX_VALUE), "(int) (1 - Integer.MAX_VALUE)");
    Tester.checkEqual(intSub(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE - Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE - Integer.MIN_VALUE)");
    Tester.checkEqual(intSub(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE - -1), "(int) (Integer.MAX_VALUE - -1)");
    Tester.checkEqual(intSub(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE - 0), "(int) (Integer.MAX_VALUE - 0)");
    Tester.checkEqual(intSub(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE - 1), "(int) (Integer.MAX_VALUE - 1)");
    Tester.checkEqual(intSub(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE - Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE - Integer.MAX_VALUE)");
    Tester.checkEqual(intShl(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE << Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE << Integer.MIN_VALUE)");
    Tester.checkEqual(intShl(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE << -1), "(int) (Integer.MIN_VALUE << -1)");
    Tester.checkEqual(intShl(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE << 0), "(int) (Integer.MIN_VALUE << 0)");
    Tester.checkEqual(intShl(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE << 1), "(int) (Integer.MIN_VALUE << 1)");
    Tester.checkEqual(intShl(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE << Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE << Integer.MAX_VALUE)");
    Tester.checkEqual(intShl(-1, Integer.MIN_VALUE), (int) (-1 << Integer.MIN_VALUE), "(int) (-1 << Integer.MIN_VALUE)");
    Tester.checkEqual(intShl(-1, -1), (int) (-1 << -1), "(int) (-1 << -1)");
    Tester.checkEqual(intShl(-1, 0), (int) (-1 << 0), "(int) (-1 << 0)");
    Tester.checkEqual(intShl(-1, 1), (int) (-1 << 1), "(int) (-1 << 1)");
    Tester.checkEqual(intShl(-1, Integer.MAX_VALUE), (int) (-1 << Integer.MAX_VALUE), "(int) (-1 << Integer.MAX_VALUE)");
    Tester.checkEqual(intShl(0, Integer.MIN_VALUE), (int) (0 << Integer.MIN_VALUE), "(int) (0 << Integer.MIN_VALUE)");
    Tester.checkEqual(intShl(0, -1), (int) (0 << -1), "(int) (0 << -1)");
    Tester.checkEqual(intShl(0, 0), (int) (0 << 0), "(int) (0 << 0)");
    Tester.checkEqual(intShl(0, 1), (int) (0 << 1), "(int) (0 << 1)");
    Tester.checkEqual(intShl(0, Integer.MAX_VALUE), (int) (0 << Integer.MAX_VALUE), "(int) (0 << Integer.MAX_VALUE)");
    Tester.checkEqual(intShl(1, Integer.MIN_VALUE), (int) (1 << Integer.MIN_VALUE), "(int) (1 << Integer.MIN_VALUE)");
    Tester.checkEqual(intShl(1, -1), (int) (1 << -1), "(int) (1 << -1)");
    Tester.checkEqual(intShl(1, 0), (int) (1 << 0), "(int) (1 << 0)");
    Tester.checkEqual(intShl(1, 1), (int) (1 << 1), "(int) (1 << 1)");
    Tester.checkEqual(intShl(1, Integer.MAX_VALUE), (int) (1 << Integer.MAX_VALUE), "(int) (1 << Integer.MAX_VALUE)");
    Tester.checkEqual(intShl(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE << Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE << Integer.MIN_VALUE)");
    Tester.checkEqual(intShl(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE << -1), "(int) (Integer.MAX_VALUE << -1)");
    Tester.checkEqual(intShl(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE << 0), "(int) (Integer.MAX_VALUE << 0)");
    Tester.checkEqual(intShl(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE << 1), "(int) (Integer.MAX_VALUE << 1)");
    Tester.checkEqual(intShl(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE << Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE << Integer.MAX_VALUE)");
    Tester.checkEqual(intShr(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE >> Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE >> Integer.MIN_VALUE)");
    Tester.checkEqual(intShr(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE >> -1), "(int) (Integer.MIN_VALUE >> -1)");
    Tester.checkEqual(intShr(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE >> 0), "(int) (Integer.MIN_VALUE >> 0)");
    Tester.checkEqual(intShr(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE >> 1), "(int) (Integer.MIN_VALUE >> 1)");
    Tester.checkEqual(intShr(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE >> Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE >> Integer.MAX_VALUE)");
    Tester.checkEqual(intShr(-1, Integer.MIN_VALUE), (int) (-1 >> Integer.MIN_VALUE), "(int) (-1 >> Integer.MIN_VALUE)");
    Tester.checkEqual(intShr(-1, -1), (int) (-1 >> -1), "(int) (-1 >> -1)");
    Tester.checkEqual(intShr(-1, 0), (int) (-1 >> 0), "(int) (-1 >> 0)");
    Tester.checkEqual(intShr(-1, 1), (int) (-1 >> 1), "(int) (-1 >> 1)");
    Tester.checkEqual(intShr(-1, Integer.MAX_VALUE), (int) (-1 >> Integer.MAX_VALUE), "(int) (-1 >> Integer.MAX_VALUE)");
    Tester.checkEqual(intShr(0, Integer.MIN_VALUE), (int) (0 >> Integer.MIN_VALUE), "(int) (0 >> Integer.MIN_VALUE)");
    Tester.checkEqual(intShr(0, -1), (int) (0 >> -1), "(int) (0 >> -1)");
    Tester.checkEqual(intShr(0, 0), (int) (0 >> 0), "(int) (0 >> 0)");
    Tester.checkEqual(intShr(0, 1), (int) (0 >> 1), "(int) (0 >> 1)");
    Tester.checkEqual(intShr(0, Integer.MAX_VALUE), (int) (0 >> Integer.MAX_VALUE), "(int) (0 >> Integer.MAX_VALUE)");
    Tester.checkEqual(intShr(1, Integer.MIN_VALUE), (int) (1 >> Integer.MIN_VALUE), "(int) (1 >> Integer.MIN_VALUE)");
    Tester.checkEqual(intShr(1, -1), (int) (1 >> -1), "(int) (1 >> -1)");
    Tester.checkEqual(intShr(1, 0), (int) (1 >> 0), "(int) (1 >> 0)");
    Tester.checkEqual(intShr(1, 1), (int) (1 >> 1), "(int) (1 >> 1)");
    Tester.checkEqual(intShr(1, Integer.MAX_VALUE), (int) (1 >> Integer.MAX_VALUE), "(int) (1 >> Integer.MAX_VALUE)");
    Tester.checkEqual(intShr(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE >> Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE >> Integer.MIN_VALUE)");
    Tester.checkEqual(intShr(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE >> -1), "(int) (Integer.MAX_VALUE >> -1)");
    Tester.checkEqual(intShr(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE >> 0), "(int) (Integer.MAX_VALUE >> 0)");
    Tester.checkEqual(intShr(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE >> 1), "(int) (Integer.MAX_VALUE >> 1)");
    Tester.checkEqual(intShr(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE >> Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE >> Integer.MAX_VALUE)");
    Tester.checkEqual(intUshr(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE >>> Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE >>> Integer.MIN_VALUE)");
    Tester.checkEqual(intUshr(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE >>> -1), "(int) (Integer.MIN_VALUE >>> -1)");
    Tester.checkEqual(intUshr(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE >>> 0), "(int) (Integer.MIN_VALUE >>> 0)");
    Tester.checkEqual(intUshr(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE >>> 1), "(int) (Integer.MIN_VALUE >>> 1)");
    Tester.checkEqual(intUshr(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE >>> Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE >>> Integer.MAX_VALUE)");
    Tester.checkEqual(intUshr(-1, Integer.MIN_VALUE), (int) (-1 >>> Integer.MIN_VALUE), "(int) (-1 >>> Integer.MIN_VALUE)");
    Tester.checkEqual(intUshr(-1, -1), (int) (-1 >>> -1), "(int) (-1 >>> -1)");
    Tester.checkEqual(intUshr(-1, 0), (int) (-1 >>> 0), "(int) (-1 >>> 0)");
    Tester.checkEqual(intUshr(-1, 1), (int) (-1 >>> 1), "(int) (-1 >>> 1)");
    Tester.checkEqual(intUshr(-1, Integer.MAX_VALUE), (int) (-1 >>> Integer.MAX_VALUE), "(int) (-1 >>> Integer.MAX_VALUE)");
    Tester.checkEqual(intUshr(0, Integer.MIN_VALUE), (int) (0 >>> Integer.MIN_VALUE), "(int) (0 >>> Integer.MIN_VALUE)");
    Tester.checkEqual(intUshr(0, -1), (int) (0 >>> -1), "(int) (0 >>> -1)");
    Tester.checkEqual(intUshr(0, 0), (int) (0 >>> 0), "(int) (0 >>> 0)");
    Tester.checkEqual(intUshr(0, 1), (int) (0 >>> 1), "(int) (0 >>> 1)");
    Tester.checkEqual(intUshr(0, Integer.MAX_VALUE), (int) (0 >>> Integer.MAX_VALUE), "(int) (0 >>> Integer.MAX_VALUE)");
    Tester.checkEqual(intUshr(1, Integer.MIN_VALUE), (int) (1 >>> Integer.MIN_VALUE), "(int) (1 >>> Integer.MIN_VALUE)");
    Tester.checkEqual(intUshr(1, -1), (int) (1 >>> -1), "(int) (1 >>> -1)");
    Tester.checkEqual(intUshr(1, 0), (int) (1 >>> 0), "(int) (1 >>> 0)");
    Tester.checkEqual(intUshr(1, 1), (int) (1 >>> 1), "(int) (1 >>> 1)");
    Tester.checkEqual(intUshr(1, Integer.MAX_VALUE), (int) (1 >>> Integer.MAX_VALUE), "(int) (1 >>> Integer.MAX_VALUE)");
    Tester.checkEqual(intUshr(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE >>> Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE >>> Integer.MIN_VALUE)");
    Tester.checkEqual(intUshr(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE >>> -1), "(int) (Integer.MAX_VALUE >>> -1)");
    Tester.checkEqual(intUshr(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE >>> 0), "(int) (Integer.MAX_VALUE >>> 0)");
    Tester.checkEqual(intUshr(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE >>> 1), "(int) (Integer.MAX_VALUE >>> 1)");
    Tester.checkEqual(intUshr(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE >>> Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE >>> Integer.MAX_VALUE)");
    Tester.checkEqual(intLt(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE < Integer.MIN_VALUE, "Integer.MIN_VALUE < Integer.MIN_VALUE");
    Tester.checkEqual(intLt(Integer.MIN_VALUE, -1), Integer.MIN_VALUE < -1, "Integer.MIN_VALUE < -1");
    Tester.checkEqual(intLt(Integer.MIN_VALUE, 0), Integer.MIN_VALUE < 0, "Integer.MIN_VALUE < 0");
    Tester.checkEqual(intLt(Integer.MIN_VALUE, 1), Integer.MIN_VALUE < 1, "Integer.MIN_VALUE < 1");
    Tester.checkEqual(intLt(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE < Integer.MAX_VALUE, "Integer.MIN_VALUE < Integer.MAX_VALUE");
    Tester.checkEqual(intLt(-1, Integer.MIN_VALUE), -1 < Integer.MIN_VALUE, "-1 < Integer.MIN_VALUE");
    Tester.checkEqual(intLt(-1, -1), -1 < -1, "-1 < -1");
    Tester.checkEqual(intLt(-1, 0), -1 < 0, "-1 < 0");
    Tester.checkEqual(intLt(-1, 1), -1 < 1, "-1 < 1");
    Tester.checkEqual(intLt(-1, Integer.MAX_VALUE), -1 < Integer.MAX_VALUE, "-1 < Integer.MAX_VALUE");
    Tester.checkEqual(intLt(0, Integer.MIN_VALUE), 0 < Integer.MIN_VALUE, "0 < Integer.MIN_VALUE");
    Tester.checkEqual(intLt(0, -1), 0 < -1, "0 < -1");
    Tester.checkEqual(intLt(0, 0), 0 < 0, "0 < 0");
    Tester.checkEqual(intLt(0, 1), 0 < 1, "0 < 1");
    Tester.checkEqual(intLt(0, Integer.MAX_VALUE), 0 < Integer.MAX_VALUE, "0 < Integer.MAX_VALUE");
    Tester.checkEqual(intLt(1, Integer.MIN_VALUE), 1 < Integer.MIN_VALUE, "1 < Integer.MIN_VALUE");
    Tester.checkEqual(intLt(1, -1), 1 < -1, "1 < -1");
    Tester.checkEqual(intLt(1, 0), 1 < 0, "1 < 0");
    Tester.checkEqual(intLt(1, 1), 1 < 1, "1 < 1");
    Tester.checkEqual(intLt(1, Integer.MAX_VALUE), 1 < Integer.MAX_VALUE, "1 < Integer.MAX_VALUE");
    Tester.checkEqual(intLt(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE < Integer.MIN_VALUE, "Integer.MAX_VALUE < Integer.MIN_VALUE");
    Tester.checkEqual(intLt(Integer.MAX_VALUE, -1), Integer.MAX_VALUE < -1, "Integer.MAX_VALUE < -1");
    Tester.checkEqual(intLt(Integer.MAX_VALUE, 0), Integer.MAX_VALUE < 0, "Integer.MAX_VALUE < 0");
    Tester.checkEqual(intLt(Integer.MAX_VALUE, 1), Integer.MAX_VALUE < 1, "Integer.MAX_VALUE < 1");
    Tester.checkEqual(intLt(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE < Integer.MAX_VALUE, "Integer.MAX_VALUE < Integer.MAX_VALUE");
    Tester.checkEqual(intGt(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE > Integer.MIN_VALUE, "Integer.MIN_VALUE > Integer.MIN_VALUE");
    Tester.checkEqual(intGt(Integer.MIN_VALUE, -1), Integer.MIN_VALUE > -1, "Integer.MIN_VALUE > -1");
    Tester.checkEqual(intGt(Integer.MIN_VALUE, 0), Integer.MIN_VALUE > 0, "Integer.MIN_VALUE > 0");
    Tester.checkEqual(intGt(Integer.MIN_VALUE, 1), Integer.MIN_VALUE > 1, "Integer.MIN_VALUE > 1");
    Tester.checkEqual(intGt(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE > Integer.MAX_VALUE, "Integer.MIN_VALUE > Integer.MAX_VALUE");
    Tester.checkEqual(intGt(-1, Integer.MIN_VALUE), -1 > Integer.MIN_VALUE, "-1 > Integer.MIN_VALUE");
    Tester.checkEqual(intGt(-1, -1), -1 > -1, "-1 > -1");
    Tester.checkEqual(intGt(-1, 0), -1 > 0, "-1 > 0");
    Tester.checkEqual(intGt(-1, 1), -1 > 1, "-1 > 1");
    Tester.checkEqual(intGt(-1, Integer.MAX_VALUE), -1 > Integer.MAX_VALUE, "-1 > Integer.MAX_VALUE");
    Tester.checkEqual(intGt(0, Integer.MIN_VALUE), 0 > Integer.MIN_VALUE, "0 > Integer.MIN_VALUE");
    Tester.checkEqual(intGt(0, -1), 0 > -1, "0 > -1");
    Tester.checkEqual(intGt(0, 0), 0 > 0, "0 > 0");
    Tester.checkEqual(intGt(0, 1), 0 > 1, "0 > 1");
    Tester.checkEqual(intGt(0, Integer.MAX_VALUE), 0 > Integer.MAX_VALUE, "0 > Integer.MAX_VALUE");
    Tester.checkEqual(intGt(1, Integer.MIN_VALUE), 1 > Integer.MIN_VALUE, "1 > Integer.MIN_VALUE");
    Tester.checkEqual(intGt(1, -1), 1 > -1, "1 > -1");
    Tester.checkEqual(intGt(1, 0), 1 > 0, "1 > 0");
    Tester.checkEqual(intGt(1, 1), 1 > 1, "1 > 1");
    Tester.checkEqual(intGt(1, Integer.MAX_VALUE), 1 > Integer.MAX_VALUE, "1 > Integer.MAX_VALUE");
    Tester.checkEqual(intGt(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE > Integer.MIN_VALUE, "Integer.MAX_VALUE > Integer.MIN_VALUE");
    Tester.checkEqual(intGt(Integer.MAX_VALUE, -1), Integer.MAX_VALUE > -1, "Integer.MAX_VALUE > -1");
    Tester.checkEqual(intGt(Integer.MAX_VALUE, 0), Integer.MAX_VALUE > 0, "Integer.MAX_VALUE > 0");
    Tester.checkEqual(intGt(Integer.MAX_VALUE, 1), Integer.MAX_VALUE > 1, "Integer.MAX_VALUE > 1");
    Tester.checkEqual(intGt(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE > Integer.MAX_VALUE, "Integer.MAX_VALUE > Integer.MAX_VALUE");
    Tester.checkEqual(intLe(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE <= Integer.MIN_VALUE, "Integer.MIN_VALUE <= Integer.MIN_VALUE");
    Tester.checkEqual(intLe(Integer.MIN_VALUE, -1), Integer.MIN_VALUE <= -1, "Integer.MIN_VALUE <= -1");
    Tester.checkEqual(intLe(Integer.MIN_VALUE, 0), Integer.MIN_VALUE <= 0, "Integer.MIN_VALUE <= 0");
    Tester.checkEqual(intLe(Integer.MIN_VALUE, 1), Integer.MIN_VALUE <= 1, "Integer.MIN_VALUE <= 1");
    Tester.checkEqual(intLe(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE <= Integer.MAX_VALUE, "Integer.MIN_VALUE <= Integer.MAX_VALUE");
    Tester.checkEqual(intLe(-1, Integer.MIN_VALUE), -1 <= Integer.MIN_VALUE, "-1 <= Integer.MIN_VALUE");
    Tester.checkEqual(intLe(-1, -1), -1 <= -1, "-1 <= -1");
    Tester.checkEqual(intLe(-1, 0), -1 <= 0, "-1 <= 0");
    Tester.checkEqual(intLe(-1, 1), -1 <= 1, "-1 <= 1");
    Tester.checkEqual(intLe(-1, Integer.MAX_VALUE), -1 <= Integer.MAX_VALUE, "-1 <= Integer.MAX_VALUE");
    Tester.checkEqual(intLe(0, Integer.MIN_VALUE), 0 <= Integer.MIN_VALUE, "0 <= Integer.MIN_VALUE");
    Tester.checkEqual(intLe(0, -1), 0 <= -1, "0 <= -1");
    Tester.checkEqual(intLe(0, 0), 0 <= 0, "0 <= 0");
    Tester.checkEqual(intLe(0, 1), 0 <= 1, "0 <= 1");
    Tester.checkEqual(intLe(0, Integer.MAX_VALUE), 0 <= Integer.MAX_VALUE, "0 <= Integer.MAX_VALUE");
    Tester.checkEqual(intLe(1, Integer.MIN_VALUE), 1 <= Integer.MIN_VALUE, "1 <= Integer.MIN_VALUE");
    Tester.checkEqual(intLe(1, -1), 1 <= -1, "1 <= -1");
    Tester.checkEqual(intLe(1, 0), 1 <= 0, "1 <= 0");
    Tester.checkEqual(intLe(1, 1), 1 <= 1, "1 <= 1");
    Tester.checkEqual(intLe(1, Integer.MAX_VALUE), 1 <= Integer.MAX_VALUE, "1 <= Integer.MAX_VALUE");
    Tester.checkEqual(intLe(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE <= Integer.MIN_VALUE, "Integer.MAX_VALUE <= Integer.MIN_VALUE");
    Tester.checkEqual(intLe(Integer.MAX_VALUE, -1), Integer.MAX_VALUE <= -1, "Integer.MAX_VALUE <= -1");
    Tester.checkEqual(intLe(Integer.MAX_VALUE, 0), Integer.MAX_VALUE <= 0, "Integer.MAX_VALUE <= 0");
    Tester.checkEqual(intLe(Integer.MAX_VALUE, 1), Integer.MAX_VALUE <= 1, "Integer.MAX_VALUE <= 1");
    Tester.checkEqual(intLe(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE <= Integer.MAX_VALUE, "Integer.MAX_VALUE <= Integer.MAX_VALUE");
    Tester.checkEqual(intGe(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE >= Integer.MIN_VALUE, "Integer.MIN_VALUE >= Integer.MIN_VALUE");
    Tester.checkEqual(intGe(Integer.MIN_VALUE, -1), Integer.MIN_VALUE >= -1, "Integer.MIN_VALUE >= -1");
    Tester.checkEqual(intGe(Integer.MIN_VALUE, 0), Integer.MIN_VALUE >= 0, "Integer.MIN_VALUE >= 0");
    Tester.checkEqual(intGe(Integer.MIN_VALUE, 1), Integer.MIN_VALUE >= 1, "Integer.MIN_VALUE >= 1");
    Tester.checkEqual(intGe(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE >= Integer.MAX_VALUE, "Integer.MIN_VALUE >= Integer.MAX_VALUE");
    Tester.checkEqual(intGe(-1, Integer.MIN_VALUE), -1 >= Integer.MIN_VALUE, "-1 >= Integer.MIN_VALUE");
    Tester.checkEqual(intGe(-1, -1), -1 >= -1, "-1 >= -1");
    Tester.checkEqual(intGe(-1, 0), -1 >= 0, "-1 >= 0");
    Tester.checkEqual(intGe(-1, 1), -1 >= 1, "-1 >= 1");
    Tester.checkEqual(intGe(-1, Integer.MAX_VALUE), -1 >= Integer.MAX_VALUE, "-1 >= Integer.MAX_VALUE");
    Tester.checkEqual(intGe(0, Integer.MIN_VALUE), 0 >= Integer.MIN_VALUE, "0 >= Integer.MIN_VALUE");
    Tester.checkEqual(intGe(0, -1), 0 >= -1, "0 >= -1");
    Tester.checkEqual(intGe(0, 0), 0 >= 0, "0 >= 0");
    Tester.checkEqual(intGe(0, 1), 0 >= 1, "0 >= 1");
    Tester.checkEqual(intGe(0, Integer.MAX_VALUE), 0 >= Integer.MAX_VALUE, "0 >= Integer.MAX_VALUE");
    Tester.checkEqual(intGe(1, Integer.MIN_VALUE), 1 >= Integer.MIN_VALUE, "1 >= Integer.MIN_VALUE");
    Tester.checkEqual(intGe(1, -1), 1 >= -1, "1 >= -1");
    Tester.checkEqual(intGe(1, 0), 1 >= 0, "1 >= 0");
    Tester.checkEqual(intGe(1, 1), 1 >= 1, "1 >= 1");
    Tester.checkEqual(intGe(1, Integer.MAX_VALUE), 1 >= Integer.MAX_VALUE, "1 >= Integer.MAX_VALUE");
    Tester.checkEqual(intGe(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE >= Integer.MIN_VALUE, "Integer.MAX_VALUE >= Integer.MIN_VALUE");
    Tester.checkEqual(intGe(Integer.MAX_VALUE, -1), Integer.MAX_VALUE >= -1, "Integer.MAX_VALUE >= -1");
    Tester.checkEqual(intGe(Integer.MAX_VALUE, 0), Integer.MAX_VALUE >= 0, "Integer.MAX_VALUE >= 0");
    Tester.checkEqual(intGe(Integer.MAX_VALUE, 1), Integer.MAX_VALUE >= 1, "Integer.MAX_VALUE >= 1");
    Tester.checkEqual(intGe(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE >= Integer.MAX_VALUE, "Integer.MAX_VALUE >= Integer.MAX_VALUE");
    Tester.checkEqual(intEq(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE == Integer.MIN_VALUE, "Integer.MIN_VALUE == Integer.MIN_VALUE");
    Tester.checkEqual(intEq(Integer.MIN_VALUE, -1), Integer.MIN_VALUE == -1, "Integer.MIN_VALUE == -1");
    Tester.checkEqual(intEq(Integer.MIN_VALUE, 0), Integer.MIN_VALUE == 0, "Integer.MIN_VALUE == 0");
    Tester.checkEqual(intEq(Integer.MIN_VALUE, 1), Integer.MIN_VALUE == 1, "Integer.MIN_VALUE == 1");
    Tester.checkEqual(intEq(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE == Integer.MAX_VALUE, "Integer.MIN_VALUE == Integer.MAX_VALUE");
    Tester.checkEqual(intEq(-1, Integer.MIN_VALUE), -1 == Integer.MIN_VALUE, "-1 == Integer.MIN_VALUE");
    Tester.checkEqual(intEq(-1, -1), -1 == -1, "-1 == -1");
    Tester.checkEqual(intEq(-1, 0), -1 == 0, "-1 == 0");
    Tester.checkEqual(intEq(-1, 1), -1 == 1, "-1 == 1");
    Tester.checkEqual(intEq(-1, Integer.MAX_VALUE), -1 == Integer.MAX_VALUE, "-1 == Integer.MAX_VALUE");
    Tester.checkEqual(intEq(0, Integer.MIN_VALUE), 0 == Integer.MIN_VALUE, "0 == Integer.MIN_VALUE");
    Tester.checkEqual(intEq(0, -1), 0 == -1, "0 == -1");
    Tester.checkEqual(intEq(0, 0), 0 == 0, "0 == 0");
    Tester.checkEqual(intEq(0, 1), 0 == 1, "0 == 1");
    Tester.checkEqual(intEq(0, Integer.MAX_VALUE), 0 == Integer.MAX_VALUE, "0 == Integer.MAX_VALUE");
    Tester.checkEqual(intEq(1, Integer.MIN_VALUE), 1 == Integer.MIN_VALUE, "1 == Integer.MIN_VALUE");
    Tester.checkEqual(intEq(1, -1), 1 == -1, "1 == -1");
    Tester.checkEqual(intEq(1, 0), 1 == 0, "1 == 0");
    Tester.checkEqual(intEq(1, 1), 1 == 1, "1 == 1");
    Tester.checkEqual(intEq(1, Integer.MAX_VALUE), 1 == Integer.MAX_VALUE, "1 == Integer.MAX_VALUE");
    Tester.checkEqual(intEq(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE == Integer.MIN_VALUE, "Integer.MAX_VALUE == Integer.MIN_VALUE");
    Tester.checkEqual(intEq(Integer.MAX_VALUE, -1), Integer.MAX_VALUE == -1, "Integer.MAX_VALUE == -1");
    Tester.checkEqual(intEq(Integer.MAX_VALUE, 0), Integer.MAX_VALUE == 0, "Integer.MAX_VALUE == 0");
    Tester.checkEqual(intEq(Integer.MAX_VALUE, 1), Integer.MAX_VALUE == 1, "Integer.MAX_VALUE == 1");
    Tester.checkEqual(intEq(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE == Integer.MAX_VALUE, "Integer.MAX_VALUE == Integer.MAX_VALUE");
    Tester.checkEqual(intNe(Integer.MIN_VALUE, Integer.MIN_VALUE), Integer.MIN_VALUE != Integer.MIN_VALUE, "Integer.MIN_VALUE != Integer.MIN_VALUE");
    Tester.checkEqual(intNe(Integer.MIN_VALUE, -1), Integer.MIN_VALUE != -1, "Integer.MIN_VALUE != -1");
    Tester.checkEqual(intNe(Integer.MIN_VALUE, 0), Integer.MIN_VALUE != 0, "Integer.MIN_VALUE != 0");
    Tester.checkEqual(intNe(Integer.MIN_VALUE, 1), Integer.MIN_VALUE != 1, "Integer.MIN_VALUE != 1");
    Tester.checkEqual(intNe(Integer.MIN_VALUE, Integer.MAX_VALUE), Integer.MIN_VALUE != Integer.MAX_VALUE, "Integer.MIN_VALUE != Integer.MAX_VALUE");
    Tester.checkEqual(intNe(-1, Integer.MIN_VALUE), -1 != Integer.MIN_VALUE, "-1 != Integer.MIN_VALUE");
    Tester.checkEqual(intNe(-1, -1), -1 != -1, "-1 != -1");
    Tester.checkEqual(intNe(-1, 0), -1 != 0, "-1 != 0");
    Tester.checkEqual(intNe(-1, 1), -1 != 1, "-1 != 1");
    Tester.checkEqual(intNe(-1, Integer.MAX_VALUE), -1 != Integer.MAX_VALUE, "-1 != Integer.MAX_VALUE");
    Tester.checkEqual(intNe(0, Integer.MIN_VALUE), 0 != Integer.MIN_VALUE, "0 != Integer.MIN_VALUE");
    Tester.checkEqual(intNe(0, -1), 0 != -1, "0 != -1");
    Tester.checkEqual(intNe(0, 0), 0 != 0, "0 != 0");
    Tester.checkEqual(intNe(0, 1), 0 != 1, "0 != 1");
    Tester.checkEqual(intNe(0, Integer.MAX_VALUE), 0 != Integer.MAX_VALUE, "0 != Integer.MAX_VALUE");
    Tester.checkEqual(intNe(1, Integer.MIN_VALUE), 1 != Integer.MIN_VALUE, "1 != Integer.MIN_VALUE");
    Tester.checkEqual(intNe(1, -1), 1 != -1, "1 != -1");
    Tester.checkEqual(intNe(1, 0), 1 != 0, "1 != 0");
    Tester.checkEqual(intNe(1, 1), 1 != 1, "1 != 1");
    Tester.checkEqual(intNe(1, Integer.MAX_VALUE), 1 != Integer.MAX_VALUE, "1 != Integer.MAX_VALUE");
    Tester.checkEqual(intNe(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE != Integer.MIN_VALUE, "Integer.MAX_VALUE != Integer.MIN_VALUE");
    Tester.checkEqual(intNe(Integer.MAX_VALUE, -1), Integer.MAX_VALUE != -1, "Integer.MAX_VALUE != -1");
    Tester.checkEqual(intNe(Integer.MAX_VALUE, 0), Integer.MAX_VALUE != 0, "Integer.MAX_VALUE != 0");
    Tester.checkEqual(intNe(Integer.MAX_VALUE, 1), Integer.MAX_VALUE != 1, "Integer.MAX_VALUE != 1");
    Tester.checkEqual(intNe(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE != Integer.MAX_VALUE, "Integer.MAX_VALUE != Integer.MAX_VALUE");
    Tester.checkEqual(intAnd(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE & Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE & Integer.MIN_VALUE)");
    Tester.checkEqual(intAnd(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE & -1), "(int) (Integer.MIN_VALUE & -1)");
    Tester.checkEqual(intAnd(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE & 0), "(int) (Integer.MIN_VALUE & 0)");
    Tester.checkEqual(intAnd(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE & 1), "(int) (Integer.MIN_VALUE & 1)");
    Tester.checkEqual(intAnd(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE & Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE & Integer.MAX_VALUE)");
    Tester.checkEqual(intAnd(-1, Integer.MIN_VALUE), (int) (-1 & Integer.MIN_VALUE), "(int) (-1 & Integer.MIN_VALUE)");
    Tester.checkEqual(intAnd(-1, -1), (int) (-1 & -1), "(int) (-1 & -1)");
    Tester.checkEqual(intAnd(-1, 0), (int) (-1 & 0), "(int) (-1 & 0)");
    Tester.checkEqual(intAnd(-1, 1), (int) (-1 & 1), "(int) (-1 & 1)");
    Tester.checkEqual(intAnd(-1, Integer.MAX_VALUE), (int) (-1 & Integer.MAX_VALUE), "(int) (-1 & Integer.MAX_VALUE)");
    Tester.checkEqual(intAnd(0, Integer.MIN_VALUE), (int) (0 & Integer.MIN_VALUE), "(int) (0 & Integer.MIN_VALUE)");
    Tester.checkEqual(intAnd(0, -1), (int) (0 & -1), "(int) (0 & -1)");
    Tester.checkEqual(intAnd(0, 0), (int) (0 & 0), "(int) (0 & 0)");
    Tester.checkEqual(intAnd(0, 1), (int) (0 & 1), "(int) (0 & 1)");
    Tester.checkEqual(intAnd(0, Integer.MAX_VALUE), (int) (0 & Integer.MAX_VALUE), "(int) (0 & Integer.MAX_VALUE)");
    Tester.checkEqual(intAnd(1, Integer.MIN_VALUE), (int) (1 & Integer.MIN_VALUE), "(int) (1 & Integer.MIN_VALUE)");
    Tester.checkEqual(intAnd(1, -1), (int) (1 & -1), "(int) (1 & -1)");
    Tester.checkEqual(intAnd(1, 0), (int) (1 & 0), "(int) (1 & 0)");
    Tester.checkEqual(intAnd(1, 1), (int) (1 & 1), "(int) (1 & 1)");
    Tester.checkEqual(intAnd(1, Integer.MAX_VALUE), (int) (1 & Integer.MAX_VALUE), "(int) (1 & Integer.MAX_VALUE)");
    Tester.checkEqual(intAnd(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE & Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE & Integer.MIN_VALUE)");
    Tester.checkEqual(intAnd(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE & -1), "(int) (Integer.MAX_VALUE & -1)");
    Tester.checkEqual(intAnd(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE & 0), "(int) (Integer.MAX_VALUE & 0)");
    Tester.checkEqual(intAnd(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE & 1), "(int) (Integer.MAX_VALUE & 1)");
    Tester.checkEqual(intAnd(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE & Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE & Integer.MAX_VALUE)");
    Tester.checkEqual(intXor(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE ^ Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE ^ Integer.MIN_VALUE)");
    Tester.checkEqual(intXor(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE ^ -1), "(int) (Integer.MIN_VALUE ^ -1)");
    Tester.checkEqual(intXor(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE ^ 0), "(int) (Integer.MIN_VALUE ^ 0)");
    Tester.checkEqual(intXor(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE ^ 1), "(int) (Integer.MIN_VALUE ^ 1)");
    Tester.checkEqual(intXor(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE ^ Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE ^ Integer.MAX_VALUE)");
    Tester.checkEqual(intXor(-1, Integer.MIN_VALUE), (int) (-1 ^ Integer.MIN_VALUE), "(int) (-1 ^ Integer.MIN_VALUE)");
    Tester.checkEqual(intXor(-1, -1), (int) (-1 ^ -1), "(int) (-1 ^ -1)");
    Tester.checkEqual(intXor(-1, 0), (int) (-1 ^ 0), "(int) (-1 ^ 0)");
    Tester.checkEqual(intXor(-1, 1), (int) (-1 ^ 1), "(int) (-1 ^ 1)");
    Tester.checkEqual(intXor(-1, Integer.MAX_VALUE), (int) (-1 ^ Integer.MAX_VALUE), "(int) (-1 ^ Integer.MAX_VALUE)");
    Tester.checkEqual(intXor(0, Integer.MIN_VALUE), (int) (0 ^ Integer.MIN_VALUE), "(int) (0 ^ Integer.MIN_VALUE)");
    Tester.checkEqual(intXor(0, -1), (int) (0 ^ -1), "(int) (0 ^ -1)");
    Tester.checkEqual(intXor(0, 0), (int) (0 ^ 0), "(int) (0 ^ 0)");
    Tester.checkEqual(intXor(0, 1), (int) (0 ^ 1), "(int) (0 ^ 1)");
    Tester.checkEqual(intXor(0, Integer.MAX_VALUE), (int) (0 ^ Integer.MAX_VALUE), "(int) (0 ^ Integer.MAX_VALUE)");
    Tester.checkEqual(intXor(1, Integer.MIN_VALUE), (int) (1 ^ Integer.MIN_VALUE), "(int) (1 ^ Integer.MIN_VALUE)");
    Tester.checkEqual(intXor(1, -1), (int) (1 ^ -1), "(int) (1 ^ -1)");
    Tester.checkEqual(intXor(1, 0), (int) (1 ^ 0), "(int) (1 ^ 0)");
    Tester.checkEqual(intXor(1, 1), (int) (1 ^ 1), "(int) (1 ^ 1)");
    Tester.checkEqual(intXor(1, Integer.MAX_VALUE), (int) (1 ^ Integer.MAX_VALUE), "(int) (1 ^ Integer.MAX_VALUE)");
    Tester.checkEqual(intXor(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE ^ Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE ^ Integer.MIN_VALUE)");
    Tester.checkEqual(intXor(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE ^ -1), "(int) (Integer.MAX_VALUE ^ -1)");
    Tester.checkEqual(intXor(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE ^ 0), "(int) (Integer.MAX_VALUE ^ 0)");
    Tester.checkEqual(intXor(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE ^ 1), "(int) (Integer.MAX_VALUE ^ 1)");
    Tester.checkEqual(intXor(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE ^ Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE ^ Integer.MAX_VALUE)");
    Tester.checkEqual(intOr(Integer.MIN_VALUE, Integer.MIN_VALUE), (int) (Integer.MIN_VALUE | Integer.MIN_VALUE), "(int) (Integer.MIN_VALUE | Integer.MIN_VALUE)");
    Tester.checkEqual(intOr(Integer.MIN_VALUE, -1), (int) (Integer.MIN_VALUE | -1), "(int) (Integer.MIN_VALUE | -1)");
    Tester.checkEqual(intOr(Integer.MIN_VALUE, 0), (int) (Integer.MIN_VALUE | 0), "(int) (Integer.MIN_VALUE | 0)");
    Tester.checkEqual(intOr(Integer.MIN_VALUE, 1), (int) (Integer.MIN_VALUE | 1), "(int) (Integer.MIN_VALUE | 1)");
    Tester.checkEqual(intOr(Integer.MIN_VALUE, Integer.MAX_VALUE), (int) (Integer.MIN_VALUE | Integer.MAX_VALUE), "(int) (Integer.MIN_VALUE | Integer.MAX_VALUE)");
    Tester.checkEqual(intOr(-1, Integer.MIN_VALUE), (int) (-1 | Integer.MIN_VALUE), "(int) (-1 | Integer.MIN_VALUE)");
    Tester.checkEqual(intOr(-1, -1), (int) (-1 | -1), "(int) (-1 | -1)");
    Tester.checkEqual(intOr(-1, 0), (int) (-1 | 0), "(int) (-1 | 0)");
    Tester.checkEqual(intOr(-1, 1), (int) (-1 | 1), "(int) (-1 | 1)");
    Tester.checkEqual(intOr(-1, Integer.MAX_VALUE), (int) (-1 | Integer.MAX_VALUE), "(int) (-1 | Integer.MAX_VALUE)");
    Tester.checkEqual(intOr(0, Integer.MIN_VALUE), (int) (0 | Integer.MIN_VALUE), "(int) (0 | Integer.MIN_VALUE)");
    Tester.checkEqual(intOr(0, -1), (int) (0 | -1), "(int) (0 | -1)");
    Tester.checkEqual(intOr(0, 0), (int) (0 | 0), "(int) (0 | 0)");
    Tester.checkEqual(intOr(0, 1), (int) (0 | 1), "(int) (0 | 1)");
    Tester.checkEqual(intOr(0, Integer.MAX_VALUE), (int) (0 | Integer.MAX_VALUE), "(int) (0 | Integer.MAX_VALUE)");
    Tester.checkEqual(intOr(1, Integer.MIN_VALUE), (int) (1 | Integer.MIN_VALUE), "(int) (1 | Integer.MIN_VALUE)");
    Tester.checkEqual(intOr(1, -1), (int) (1 | -1), "(int) (1 | -1)");
    Tester.checkEqual(intOr(1, 0), (int) (1 | 0), "(int) (1 | 0)");
    Tester.checkEqual(intOr(1, 1), (int) (1 | 1), "(int) (1 | 1)");
    Tester.checkEqual(intOr(1, Integer.MAX_VALUE), (int) (1 | Integer.MAX_VALUE), "(int) (1 | Integer.MAX_VALUE)");
    Tester.checkEqual(intOr(Integer.MAX_VALUE, Integer.MIN_VALUE), (int) (Integer.MAX_VALUE | Integer.MIN_VALUE), "(int) (Integer.MAX_VALUE | Integer.MIN_VALUE)");
    Tester.checkEqual(intOr(Integer.MAX_VALUE, -1), (int) (Integer.MAX_VALUE | -1), "(int) (Integer.MAX_VALUE | -1)");
    Tester.checkEqual(intOr(Integer.MAX_VALUE, 0), (int) (Integer.MAX_VALUE | 0), "(int) (Integer.MAX_VALUE | 0)");
    Tester.checkEqual(intOr(Integer.MAX_VALUE, 1), (int) (Integer.MAX_VALUE | 1), "(int) (Integer.MAX_VALUE | 1)");
    Tester.checkEqual(intOr(Integer.MAX_VALUE, Integer.MAX_VALUE), (int) (Integer.MAX_VALUE | Integer.MAX_VALUE), "(int) (Integer.MAX_VALUE | Integer.MAX_VALUE)");
  }
  static void intSwitch() {
    switch(0) {
      case ((((int) + Integer.MIN_VALUE) == 0) ? 0 : 0):
      case ((((int) + -1) == 0) ? 1 : 1):
      case ((((int) + 0) == 0) ? 2 : 2):
      case ((((int) + 1) == 0) ? 3 : 3):
      case ((((int) + Integer.MAX_VALUE) == 0) ? 4 : 4):
      case ((((int) - Integer.MIN_VALUE) == 0) ? 5 : 5):
      case ((((int) - -1) == 0) ? 6 : 6):
      case ((((int) - 0) == 0) ? 7 : 7):
      case ((((int) - 1) == 0) ? 8 : 8):
      case ((((int) - Integer.MAX_VALUE) == 0) ? 9 : 9):
      case ((((int) ~ Integer.MIN_VALUE) == 0) ? 10 : 10):
      case ((((int) ~ -1) == 0) ? 11 : 11):
      case ((((int) ~ 0) == 0) ? 12 : 12):
      case ((((int) ~ 1) == 0) ? 13 : 13):
      case ((((int) ~ Integer.MAX_VALUE) == 0) ? 14 : 14):
      case ((((int) (Integer.MIN_VALUE * Integer.MIN_VALUE)) == 0) ? 15 : 15):
      case ((((int) (Integer.MIN_VALUE * -1)) == 0) ? 16 : 16):
      case ((((int) (Integer.MIN_VALUE * 0)) == 0) ? 17 : 17):
      case ((((int) (Integer.MIN_VALUE * 1)) == 0) ? 18 : 18):
      case ((((int) (Integer.MIN_VALUE * Integer.MAX_VALUE)) == 0) ? 19 : 19):
      case ((((int) (-1 * Integer.MIN_VALUE)) == 0) ? 20 : 20):
      case ((((int) (-1 * -1)) == 0) ? 21 : 21):
      case ((((int) (-1 * 0)) == 0) ? 22 : 22):
      case ((((int) (-1 * 1)) == 0) ? 23 : 23):
      case ((((int) (-1 * Integer.MAX_VALUE)) == 0) ? 24 : 24):
      case ((((int) (0 * Integer.MIN_VALUE)) == 0) ? 25 : 25):
      case ((((int) (0 * -1)) == 0) ? 26 : 26):
      case ((((int) (0 * 0)) == 0) ? 27 : 27):
      case ((((int) (0 * 1)) == 0) ? 28 : 28):
      case ((((int) (0 * Integer.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((int) (1 * Integer.MIN_VALUE)) == 0) ? 30 : 30):
      case ((((int) (1 * -1)) == 0) ? 31 : 31):
      case ((((int) (1 * 0)) == 0) ? 32 : 32):
      case ((((int) (1 * 1)) == 0) ? 33 : 33):
      case ((((int) (1 * Integer.MAX_VALUE)) == 0) ? 34 : 34):
      case ((((int) (Integer.MAX_VALUE * Integer.MIN_VALUE)) == 0) ? 35 : 35):
      case ((((int) (Integer.MAX_VALUE * -1)) == 0) ? 36 : 36):
      case ((((int) (Integer.MAX_VALUE * 0)) == 0) ? 37 : 37):
      case ((((int) (Integer.MAX_VALUE * 1)) == 0) ? 38 : 38):
      case ((((int) (Integer.MAX_VALUE * Integer.MAX_VALUE)) == 0) ? 39 : 39):
      case ((((int) (Integer.MIN_VALUE / Integer.MIN_VALUE)) == 0) ? 40 : 40):
      case ((((int) (Integer.MIN_VALUE / -1)) == 0) ? 41 : 41):
      case ((((int) (Integer.MIN_VALUE / 1)) == 0) ? 42 : 42):
      case ((((int) (Integer.MIN_VALUE / Integer.MAX_VALUE)) == 0) ? 43 : 43):
      case ((((int) (-1 / Integer.MIN_VALUE)) == 0) ? 44 : 44):
      case ((((int) (-1 / -1)) == 0) ? 45 : 45):
      case ((((int) (-1 / 1)) == 0) ? 46 : 46):
      case ((((int) (-1 / Integer.MAX_VALUE)) == 0) ? 47 : 47):
      case ((((int) (0 / Integer.MIN_VALUE)) == 0) ? 48 : 48):
      case ((((int) (0 / -1)) == 0) ? 49 : 49):
      case ((((int) (0 / 1)) == 0) ? 50 : 50):
      case ((((int) (0 / Integer.MAX_VALUE)) == 0) ? 51 : 51):
      case ((((int) (1 / Integer.MIN_VALUE)) == 0) ? 52 : 52):
      case ((((int) (1 / -1)) == 0) ? 53 : 53):
      case ((((int) (1 / 1)) == 0) ? 54 : 54):
      case ((((int) (1 / Integer.MAX_VALUE)) == 0) ? 55 : 55):
      case ((((int) (Integer.MAX_VALUE / Integer.MIN_VALUE)) == 0) ? 56 : 56):
      case ((((int) (Integer.MAX_VALUE / -1)) == 0) ? 57 : 57):
      case ((((int) (Integer.MAX_VALUE / 1)) == 0) ? 58 : 58):
      case ((((int) (Integer.MAX_VALUE / Integer.MAX_VALUE)) == 0) ? 59 : 59):
      case ((((int) (Integer.MIN_VALUE % Integer.MIN_VALUE)) == 0) ? 60 : 60):
      case ((((int) (Integer.MIN_VALUE % -1)) == 0) ? 61 : 61):
      case ((((int) (Integer.MIN_VALUE % 1)) == 0) ? 62 : 62):
      case ((((int) (Integer.MIN_VALUE % Integer.MAX_VALUE)) == 0) ? 63 : 63):
      case ((((int) (-1 % Integer.MIN_VALUE)) == 0) ? 64 : 64):
      case ((((int) (-1 % -1)) == 0) ? 65 : 65):
      case ((((int) (-1 % 1)) == 0) ? 66 : 66):
      case ((((int) (-1 % Integer.MAX_VALUE)) == 0) ? 67 : 67):
      case ((((int) (0 % Integer.MIN_VALUE)) == 0) ? 68 : 68):
      case ((((int) (0 % -1)) == 0) ? 69 : 69):
      case ((((int) (0 % 1)) == 0) ? 70 : 70):
      case ((((int) (0 % Integer.MAX_VALUE)) == 0) ? 71 : 71):
      case ((((int) (1 % Integer.MIN_VALUE)) == 0) ? 72 : 72):
      case ((((int) (1 % -1)) == 0) ? 73 : 73):
      case ((((int) (1 % 1)) == 0) ? 74 : 74):
      case ((((int) (1 % Integer.MAX_VALUE)) == 0) ? 75 : 75):
      case ((((int) (Integer.MAX_VALUE % Integer.MIN_VALUE)) == 0) ? 76 : 76):
      case ((((int) (Integer.MAX_VALUE % -1)) == 0) ? 77 : 77):
      case ((((int) (Integer.MAX_VALUE % 1)) == 0) ? 78 : 78):
      case ((((int) (Integer.MAX_VALUE % Integer.MAX_VALUE)) == 0) ? 79 : 79):
      case ((((int) (Integer.MIN_VALUE + Integer.MIN_VALUE)) == 0) ? 80 : 80):
      case ((((int) (Integer.MIN_VALUE + -1)) == 0) ? 81 : 81):
      case ((((int) (Integer.MIN_VALUE + 0)) == 0) ? 82 : 82):
      case ((((int) (Integer.MIN_VALUE + 1)) == 0) ? 83 : 83):
      case ((((int) (Integer.MIN_VALUE + Integer.MAX_VALUE)) == 0) ? 84 : 84):
      case ((((int) (-1 + Integer.MIN_VALUE)) == 0) ? 85 : 85):
      case ((((int) (-1 + -1)) == 0) ? 86 : 86):
      case ((((int) (-1 + 0)) == 0) ? 87 : 87):
      case ((((int) (-1 + 1)) == 0) ? 88 : 88):
      case ((((int) (-1 + Integer.MAX_VALUE)) == 0) ? 89 : 89):
      case ((((int) (0 + Integer.MIN_VALUE)) == 0) ? 90 : 90):
      case ((((int) (0 + -1)) == 0) ? 91 : 91):
      case ((((int) (0 + 0)) == 0) ? 92 : 92):
      case ((((int) (0 + 1)) == 0) ? 93 : 93):
      case ((((int) (0 + Integer.MAX_VALUE)) == 0) ? 94 : 94):
      case ((((int) (1 + Integer.MIN_VALUE)) == 0) ? 95 : 95):
      case ((((int) (1 + -1)) == 0) ? 96 : 96):
      case ((((int) (1 + 0)) == 0) ? 97 : 97):
      case ((((int) (1 + 1)) == 0) ? 98 : 98):
      case ((((int) (1 + Integer.MAX_VALUE)) == 0) ? 99 : 99):
      case ((((int) (Integer.MAX_VALUE + Integer.MIN_VALUE)) == 0) ? 100 : 100):
      case ((((int) (Integer.MAX_VALUE + -1)) == 0) ? 101 : 101):
      case ((((int) (Integer.MAX_VALUE + 0)) == 0) ? 102 : 102):
      case ((((int) (Integer.MAX_VALUE + 1)) == 0) ? 103 : 103):
      case ((((int) (Integer.MAX_VALUE + Integer.MAX_VALUE)) == 0) ? 104 : 104):
      case ((((int) (Integer.MIN_VALUE - Integer.MIN_VALUE)) == 0) ? 105 : 105):
      case ((((int) (Integer.MIN_VALUE - -1)) == 0) ? 106 : 106):
      case ((((int) (Integer.MIN_VALUE - 0)) == 0) ? 107 : 107):
      case ((((int) (Integer.MIN_VALUE - 1)) == 0) ? 108 : 108):
      case ((((int) (Integer.MIN_VALUE - Integer.MAX_VALUE)) == 0) ? 109 : 109):
      case ((((int) (-1 - Integer.MIN_VALUE)) == 0) ? 110 : 110):
      case ((((int) (-1 - -1)) == 0) ? 111 : 111):
      case ((((int) (-1 - 0)) == 0) ? 112 : 112):
      case ((((int) (-1 - 1)) == 0) ? 113 : 113):
      case ((((int) (-1 - Integer.MAX_VALUE)) == 0) ? 114 : 114):
      case ((((int) (0 - Integer.MIN_VALUE)) == 0) ? 115 : 115):
      case ((((int) (0 - -1)) == 0) ? 116 : 116):
      case ((((int) (0 - 0)) == 0) ? 117 : 117):
      case ((((int) (0 - 1)) == 0) ? 118 : 118):
      case ((((int) (0 - Integer.MAX_VALUE)) == 0) ? 119 : 119):
      case ((((int) (1 - Integer.MIN_VALUE)) == 0) ? 120 : 120):
      case ((((int) (1 - -1)) == 0) ? 121 : 121):
      case ((((int) (1 - 0)) == 0) ? 122 : 122):
      case ((((int) (1 - 1)) == 0) ? 123 : 123):
      case ((((int) (1 - Integer.MAX_VALUE)) == 0) ? 124 : 124):
      case ((((int) (Integer.MAX_VALUE - Integer.MIN_VALUE)) == 0) ? 125 : 125):
      case ((((int) (Integer.MAX_VALUE - -1)) == 0) ? 126 : 126):
      case ((((int) (Integer.MAX_VALUE - 0)) == 0) ? 127 : 127):
      case ((((int) (Integer.MAX_VALUE - 1)) == 0) ? 128 : 128):
      case ((((int) (Integer.MAX_VALUE - Integer.MAX_VALUE)) == 0) ? 129 : 129):
      case ((((int) (Integer.MIN_VALUE << Integer.MIN_VALUE)) == 0) ? 130 : 130):
      case ((((int) (Integer.MIN_VALUE << -1)) == 0) ? 131 : 131):
      case ((((int) (Integer.MIN_VALUE << 0)) == 0) ? 132 : 132):
      case ((((int) (Integer.MIN_VALUE << 1)) == 0) ? 133 : 133):
      case ((((int) (Integer.MIN_VALUE << Integer.MAX_VALUE)) == 0) ? 134 : 134):
      case ((((int) (-1 << Integer.MIN_VALUE)) == 0) ? 135 : 135):
      case ((((int) (-1 << -1)) == 0) ? 136 : 136):
      case ((((int) (-1 << 0)) == 0) ? 137 : 137):
      case ((((int) (-1 << 1)) == 0) ? 138 : 138):
      case ((((int) (-1 << Integer.MAX_VALUE)) == 0) ? 139 : 139):
      case ((((int) (0 << Integer.MIN_VALUE)) == 0) ? 140 : 140):
      case ((((int) (0 << -1)) == 0) ? 141 : 141):
      case ((((int) (0 << 0)) == 0) ? 142 : 142):
      case ((((int) (0 << 1)) == 0) ? 143 : 143):
      case ((((int) (0 << Integer.MAX_VALUE)) == 0) ? 144 : 144):
      case ((((int) (1 << Integer.MIN_VALUE)) == 0) ? 145 : 145):
      case ((((int) (1 << -1)) == 0) ? 146 : 146):
      case ((((int) (1 << 0)) == 0) ? 147 : 147):
      case ((((int) (1 << 1)) == 0) ? 148 : 148):
      case ((((int) (1 << Integer.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((int) (Integer.MAX_VALUE << Integer.MIN_VALUE)) == 0) ? 150 : 150):
      case ((((int) (Integer.MAX_VALUE << -1)) == 0) ? 151 : 151):
      case ((((int) (Integer.MAX_VALUE << 0)) == 0) ? 152 : 152):
      case ((((int) (Integer.MAX_VALUE << 1)) == 0) ? 153 : 153):
      case ((((int) (Integer.MAX_VALUE << Integer.MAX_VALUE)) == 0) ? 154 : 154):
      case ((((int) (Integer.MIN_VALUE >> Integer.MIN_VALUE)) == 0) ? 155 : 155):
      case ((((int) (Integer.MIN_VALUE >> -1)) == 0) ? 156 : 156):
      case ((((int) (Integer.MIN_VALUE >> 0)) == 0) ? 157 : 157):
      case ((((int) (Integer.MIN_VALUE >> 1)) == 0) ? 158 : 158):
      case ((((int) (Integer.MIN_VALUE >> Integer.MAX_VALUE)) == 0) ? 159 : 159):
      case ((((int) (-1 >> Integer.MIN_VALUE)) == 0) ? 160 : 160):
      case ((((int) (-1 >> -1)) == 0) ? 161 : 161):
      case ((((int) (-1 >> 0)) == 0) ? 162 : 162):
      case ((((int) (-1 >> 1)) == 0) ? 163 : 163):
      case ((((int) (-1 >> Integer.MAX_VALUE)) == 0) ? 164 : 164):
      case ((((int) (0 >> Integer.MIN_VALUE)) == 0) ? 165 : 165):
      case ((((int) (0 >> -1)) == 0) ? 166 : 166):
      case ((((int) (0 >> 0)) == 0) ? 167 : 167):
      case ((((int) (0 >> 1)) == 0) ? 168 : 168):
      case ((((int) (0 >> Integer.MAX_VALUE)) == 0) ? 169 : 169):
      case ((((int) (1 >> Integer.MIN_VALUE)) == 0) ? 170 : 170):
      case ((((int) (1 >> -1)) == 0) ? 171 : 171):
      case ((((int) (1 >> 0)) == 0) ? 172 : 172):
      case ((((int) (1 >> 1)) == 0) ? 173 : 173):
      case ((((int) (1 >> Integer.MAX_VALUE)) == 0) ? 174 : 174):
      case ((((int) (Integer.MAX_VALUE >> Integer.MIN_VALUE)) == 0) ? 175 : 175):
      case ((((int) (Integer.MAX_VALUE >> -1)) == 0) ? 176 : 176):
      case ((((int) (Integer.MAX_VALUE >> 0)) == 0) ? 177 : 177):
      case ((((int) (Integer.MAX_VALUE >> 1)) == 0) ? 178 : 178):
      case ((((int) (Integer.MAX_VALUE >> Integer.MAX_VALUE)) == 0) ? 179 : 179):
      case ((((int) (Integer.MIN_VALUE >>> Integer.MIN_VALUE)) == 0) ? 180 : 180):
      case ((((int) (Integer.MIN_VALUE >>> -1)) == 0) ? 181 : 181):
      case ((((int) (Integer.MIN_VALUE >>> 0)) == 0) ? 182 : 182):
      case ((((int) (Integer.MIN_VALUE >>> 1)) == 0) ? 183 : 183):
      case ((((int) (Integer.MIN_VALUE >>> Integer.MAX_VALUE)) == 0) ? 184 : 184):
      case ((((int) (-1 >>> Integer.MIN_VALUE)) == 0) ? 185 : 185):
      case ((((int) (-1 >>> -1)) == 0) ? 186 : 186):
      case ((((int) (-1 >>> 0)) == 0) ? 187 : 187):
      case ((((int) (-1 >>> 1)) == 0) ? 188 : 188):
      case ((((int) (-1 >>> Integer.MAX_VALUE)) == 0) ? 189 : 189):
      case ((((int) (0 >>> Integer.MIN_VALUE)) == 0) ? 190 : 190):
      case ((((int) (0 >>> -1)) == 0) ? 191 : 191):
      case ((((int) (0 >>> 0)) == 0) ? 192 : 192):
      case ((((int) (0 >>> 1)) == 0) ? 193 : 193):
      case ((((int) (0 >>> Integer.MAX_VALUE)) == 0) ? 194 : 194):
      case ((((int) (1 >>> Integer.MIN_VALUE)) == 0) ? 195 : 195):
      case ((((int) (1 >>> -1)) == 0) ? 196 : 196):
      case ((((int) (1 >>> 0)) == 0) ? 197 : 197):
      case ((((int) (1 >>> 1)) == 0) ? 198 : 198):
      case ((((int) (1 >>> Integer.MAX_VALUE)) == 0) ? 199 : 199):
      case ((((int) (Integer.MAX_VALUE >>> Integer.MIN_VALUE)) == 0) ? 200 : 200):
      case ((((int) (Integer.MAX_VALUE >>> -1)) == 0) ? 201 : 201):
      case ((((int) (Integer.MAX_VALUE >>> 0)) == 0) ? 202 : 202):
      case ((((int) (Integer.MAX_VALUE >>> 1)) == 0) ? 203 : 203):
      case ((((int) (Integer.MAX_VALUE >>> Integer.MAX_VALUE)) == 0) ? 204 : 204):
      case ((Integer.MIN_VALUE < Integer.MIN_VALUE) ? 205 : 205):
      case ((Integer.MIN_VALUE < -1) ? 206 : 206):
      case ((Integer.MIN_VALUE < 0) ? 207 : 207):
      case ((Integer.MIN_VALUE < 1) ? 208 : 208):
      case ((Integer.MIN_VALUE < Integer.MAX_VALUE) ? 209 : 209):
      case ((-1 < Integer.MIN_VALUE) ? 210 : 210):
      case ((-1 < -1) ? 211 : 211):
      case ((-1 < 0) ? 212 : 212):
      case ((-1 < 1) ? 213 : 213):
      case ((-1 < Integer.MAX_VALUE) ? 214 : 214):
      case ((0 < Integer.MIN_VALUE) ? 215 : 215):
      case ((0 < -1) ? 216 : 216):
      case ((0 < 0) ? 217 : 217):
      case ((0 < 1) ? 218 : 218):
      case ((0 < Integer.MAX_VALUE) ? 219 : 219):
      case ((1 < Integer.MIN_VALUE) ? 220 : 220):
      case ((1 < -1) ? 221 : 221):
      case ((1 < 0) ? 222 : 222):
      case ((1 < 1) ? 223 : 223):
      case ((1 < Integer.MAX_VALUE) ? 224 : 224):
      case ((Integer.MAX_VALUE < Integer.MIN_VALUE) ? 225 : 225):
      case ((Integer.MAX_VALUE < -1) ? 226 : 226):
      case ((Integer.MAX_VALUE < 0) ? 227 : 227):
      case ((Integer.MAX_VALUE < 1) ? 228 : 228):
      case ((Integer.MAX_VALUE < Integer.MAX_VALUE) ? 229 : 229):
      case ((Integer.MIN_VALUE > Integer.MIN_VALUE) ? 230 : 230):
      case ((Integer.MIN_VALUE > -1) ? 231 : 231):
      case ((Integer.MIN_VALUE > 0) ? 232 : 232):
      case ((Integer.MIN_VALUE > 1) ? 233 : 233):
      case ((Integer.MIN_VALUE > Integer.MAX_VALUE) ? 234 : 234):
      case ((-1 > Integer.MIN_VALUE) ? 235 : 235):
      case ((-1 > -1) ? 236 : 236):
      case ((-1 > 0) ? 237 : 237):
      case ((-1 > 1) ? 238 : 238):
      case ((-1 > Integer.MAX_VALUE) ? 239 : 239):
      case ((0 > Integer.MIN_VALUE) ? 240 : 240):
      case ((0 > -1) ? 241 : 241):
      case ((0 > 0) ? 242 : 242):
      case ((0 > 1) ? 243 : 243):
      case ((0 > Integer.MAX_VALUE) ? 244 : 244):
      case ((1 > Integer.MIN_VALUE) ? 245 : 245):
      case ((1 > -1) ? 246 : 246):
      case ((1 > 0) ? 247 : 247):
      case ((1 > 1) ? 248 : 248):
      case ((1 > Integer.MAX_VALUE) ? 249 : 249):
      case ((Integer.MAX_VALUE > Integer.MIN_VALUE) ? 250 : 250):
      case ((Integer.MAX_VALUE > -1) ? 251 : 251):
      case ((Integer.MAX_VALUE > 0) ? 252 : 252):
      case ((Integer.MAX_VALUE > 1) ? 253 : 253):
      case ((Integer.MAX_VALUE > Integer.MAX_VALUE) ? 254 : 254):
      case ((Integer.MIN_VALUE <= Integer.MIN_VALUE) ? 255 : 255):
      case ((Integer.MIN_VALUE <= -1) ? 256 : 256):
      case ((Integer.MIN_VALUE <= 0) ? 257 : 257):
      case ((Integer.MIN_VALUE <= 1) ? 258 : 258):
      case ((Integer.MIN_VALUE <= Integer.MAX_VALUE) ? 259 : 259):
      case ((-1 <= Integer.MIN_VALUE) ? 260 : 260):
      case ((-1 <= -1) ? 261 : 261):
      case ((-1 <= 0) ? 262 : 262):
      case ((-1 <= 1) ? 263 : 263):
      case ((-1 <= Integer.MAX_VALUE) ? 264 : 264):
      case ((0 <= Integer.MIN_VALUE) ? 265 : 265):
      case ((0 <= -1) ? 266 : 266):
      case ((0 <= 0) ? 267 : 267):
      case ((0 <= 1) ? 268 : 268):
      case ((0 <= Integer.MAX_VALUE) ? 269 : 269):
      case ((1 <= Integer.MIN_VALUE) ? 270 : 270):
      case ((1 <= -1) ? 271 : 271):
      case ((1 <= 0) ? 272 : 272):
      case ((1 <= 1) ? 273 : 273):
      case ((1 <= Integer.MAX_VALUE) ? 274 : 274):
      case ((Integer.MAX_VALUE <= Integer.MIN_VALUE) ? 275 : 275):
      case ((Integer.MAX_VALUE <= -1) ? 276 : 276):
      case ((Integer.MAX_VALUE <= 0) ? 277 : 277):
      case ((Integer.MAX_VALUE <= 1) ? 278 : 278):
      case ((Integer.MAX_VALUE <= Integer.MAX_VALUE) ? 279 : 279):
      case ((Integer.MIN_VALUE >= Integer.MIN_VALUE) ? 280 : 280):
      case ((Integer.MIN_VALUE >= -1) ? 281 : 281):
      case ((Integer.MIN_VALUE >= 0) ? 282 : 282):
      case ((Integer.MIN_VALUE >= 1) ? 283 : 283):
      case ((Integer.MIN_VALUE >= Integer.MAX_VALUE) ? 284 : 284):
      case ((-1 >= Integer.MIN_VALUE) ? 285 : 285):
      case ((-1 >= -1) ? 286 : 286):
      case ((-1 >= 0) ? 287 : 287):
      case ((-1 >= 1) ? 288 : 288):
      case ((-1 >= Integer.MAX_VALUE) ? 289 : 289):
      case ((0 >= Integer.MIN_VALUE) ? 290 : 290):
      case ((0 >= -1) ? 291 : 291):
      case ((0 >= 0) ? 292 : 292):
      case ((0 >= 1) ? 293 : 293):
      case ((0 >= Integer.MAX_VALUE) ? 294 : 294):
      case ((1 >= Integer.MIN_VALUE) ? 295 : 295):
      case ((1 >= -1) ? 296 : 296):
      case ((1 >= 0) ? 297 : 297):
      case ((1 >= 1) ? 298 : 298):
      case ((1 >= Integer.MAX_VALUE) ? 299 : 299):
      case ((Integer.MAX_VALUE >= Integer.MIN_VALUE) ? 300 : 300):
      case ((Integer.MAX_VALUE >= -1) ? 301 : 301):
      case ((Integer.MAX_VALUE >= 0) ? 302 : 302):
      case ((Integer.MAX_VALUE >= 1) ? 303 : 303):
      case ((Integer.MAX_VALUE >= Integer.MAX_VALUE) ? 304 : 304):
      case ((Integer.MIN_VALUE == Integer.MIN_VALUE) ? 305 : 305):
      case ((Integer.MIN_VALUE == -1) ? 306 : 306):
      case ((Integer.MIN_VALUE == 0) ? 307 : 307):
      case ((Integer.MIN_VALUE == 1) ? 308 : 308):
      case ((Integer.MIN_VALUE == Integer.MAX_VALUE) ? 309 : 309):
      case ((-1 == Integer.MIN_VALUE) ? 310 : 310):
      case ((-1 == -1) ? 311 : 311):
      case ((-1 == 0) ? 312 : 312):
      case ((-1 == 1) ? 313 : 313):
      case ((-1 == Integer.MAX_VALUE) ? 314 : 314):
      case ((0 == Integer.MIN_VALUE) ? 315 : 315):
      case ((0 == -1) ? 316 : 316):
      case ((0 == 0) ? 317 : 317):
      case ((0 == 1) ? 318 : 318):
      case ((0 == Integer.MAX_VALUE) ? 319 : 319):
      case ((1 == Integer.MIN_VALUE) ? 320 : 320):
      case ((1 == -1) ? 321 : 321):
      case ((1 == 0) ? 322 : 322):
      case ((1 == 1) ? 323 : 323):
      case ((1 == Integer.MAX_VALUE) ? 324 : 324):
      case ((Integer.MAX_VALUE == Integer.MIN_VALUE) ? 325 : 325):
      case ((Integer.MAX_VALUE == -1) ? 326 : 326):
      case ((Integer.MAX_VALUE == 0) ? 327 : 327):
      case ((Integer.MAX_VALUE == 1) ? 328 : 328):
      case ((Integer.MAX_VALUE == Integer.MAX_VALUE) ? 329 : 329):
      case ((Integer.MIN_VALUE != Integer.MIN_VALUE) ? 330 : 330):
      case ((Integer.MIN_VALUE != -1) ? 331 : 331):
      case ((Integer.MIN_VALUE != 0) ? 332 : 332):
      case ((Integer.MIN_VALUE != 1) ? 333 : 333):
      case ((Integer.MIN_VALUE != Integer.MAX_VALUE) ? 334 : 334):
      case ((-1 != Integer.MIN_VALUE) ? 335 : 335):
      case ((-1 != -1) ? 336 : 336):
      case ((-1 != 0) ? 337 : 337):
      case ((-1 != 1) ? 338 : 338):
      case ((-1 != Integer.MAX_VALUE) ? 339 : 339):
      case ((0 != Integer.MIN_VALUE) ? 340 : 340):
      case ((0 != -1) ? 341 : 341):
      case ((0 != 0) ? 342 : 342):
      case ((0 != 1) ? 343 : 343):
      case ((0 != Integer.MAX_VALUE) ? 344 : 344):
      case ((1 != Integer.MIN_VALUE) ? 345 : 345):
      case ((1 != -1) ? 346 : 346):
      case ((1 != 0) ? 347 : 347):
      case ((1 != 1) ? 348 : 348):
      case ((1 != Integer.MAX_VALUE) ? 349 : 349):
      case ((Integer.MAX_VALUE != Integer.MIN_VALUE) ? 350 : 350):
      case ((Integer.MAX_VALUE != -1) ? 351 : 351):
      case ((Integer.MAX_VALUE != 0) ? 352 : 352):
      case ((Integer.MAX_VALUE != 1) ? 353 : 353):
      case ((Integer.MAX_VALUE != Integer.MAX_VALUE) ? 354 : 354):
      case ((((int) (Integer.MIN_VALUE & Integer.MIN_VALUE)) == 0) ? 355 : 355):
      case ((((int) (Integer.MIN_VALUE & -1)) == 0) ? 356 : 356):
      case ((((int) (Integer.MIN_VALUE & 0)) == 0) ? 357 : 357):
      case ((((int) (Integer.MIN_VALUE & 1)) == 0) ? 358 : 358):
      case ((((int) (Integer.MIN_VALUE & Integer.MAX_VALUE)) == 0) ? 359 : 359):
      case ((((int) (-1 & Integer.MIN_VALUE)) == 0) ? 360 : 360):
      case ((((int) (-1 & -1)) == 0) ? 361 : 361):
      case ((((int) (-1 & 0)) == 0) ? 362 : 362):
      case ((((int) (-1 & 1)) == 0) ? 363 : 363):
      case ((((int) (-1 & Integer.MAX_VALUE)) == 0) ? 364 : 364):
      case ((((int) (0 & Integer.MIN_VALUE)) == 0) ? 365 : 365):
      case ((((int) (0 & -1)) == 0) ? 366 : 366):
      case ((((int) (0 & 0)) == 0) ? 367 : 367):
      case ((((int) (0 & 1)) == 0) ? 368 : 368):
      case ((((int) (0 & Integer.MAX_VALUE)) == 0) ? 369 : 369):
      case ((((int) (1 & Integer.MIN_VALUE)) == 0) ? 370 : 370):
      case ((((int) (1 & -1)) == 0) ? 371 : 371):
      case ((((int) (1 & 0)) == 0) ? 372 : 372):
      case ((((int) (1 & 1)) == 0) ? 373 : 373):
      case ((((int) (1 & Integer.MAX_VALUE)) == 0) ? 374 : 374):
      case ((((int) (Integer.MAX_VALUE & Integer.MIN_VALUE)) == 0) ? 375 : 375):
      case ((((int) (Integer.MAX_VALUE & -1)) == 0) ? 376 : 376):
      case ((((int) (Integer.MAX_VALUE & 0)) == 0) ? 377 : 377):
      case ((((int) (Integer.MAX_VALUE & 1)) == 0) ? 378 : 378):
      case ((((int) (Integer.MAX_VALUE & Integer.MAX_VALUE)) == 0) ? 379 : 379):
      case ((((int) (Integer.MIN_VALUE ^ Integer.MIN_VALUE)) == 0) ? 380 : 380):
      case ((((int) (Integer.MIN_VALUE ^ -1)) == 0) ? 381 : 381):
      case ((((int) (Integer.MIN_VALUE ^ 0)) == 0) ? 382 : 382):
      case ((((int) (Integer.MIN_VALUE ^ 1)) == 0) ? 383 : 383):
      case ((((int) (Integer.MIN_VALUE ^ Integer.MAX_VALUE)) == 0) ? 384 : 384):
      case ((((int) (-1 ^ Integer.MIN_VALUE)) == 0) ? 385 : 385):
      case ((((int) (-1 ^ -1)) == 0) ? 386 : 386):
      case ((((int) (-1 ^ 0)) == 0) ? 387 : 387):
      case ((((int) (-1 ^ 1)) == 0) ? 388 : 388):
      case ((((int) (-1 ^ Integer.MAX_VALUE)) == 0) ? 389 : 389):
      case ((((int) (0 ^ Integer.MIN_VALUE)) == 0) ? 390 : 390):
      case ((((int) (0 ^ -1)) == 0) ? 391 : 391):
      case ((((int) (0 ^ 0)) == 0) ? 392 : 392):
      case ((((int) (0 ^ 1)) == 0) ? 393 : 393):
      case ((((int) (0 ^ Integer.MAX_VALUE)) == 0) ? 394 : 394):
      case ((((int) (1 ^ Integer.MIN_VALUE)) == 0) ? 395 : 395):
      case ((((int) (1 ^ -1)) == 0) ? 396 : 396):
      case ((((int) (1 ^ 0)) == 0) ? 397 : 397):
      case ((((int) (1 ^ 1)) == 0) ? 398 : 398):
      case ((((int) (1 ^ Integer.MAX_VALUE)) == 0) ? 399 : 399):
      case ((((int) (Integer.MAX_VALUE ^ Integer.MIN_VALUE)) == 0) ? 400 : 400):
      case ((((int) (Integer.MAX_VALUE ^ -1)) == 0) ? 401 : 401):
      case ((((int) (Integer.MAX_VALUE ^ 0)) == 0) ? 402 : 402):
      case ((((int) (Integer.MAX_VALUE ^ 1)) == 0) ? 403 : 403):
      case ((((int) (Integer.MAX_VALUE ^ Integer.MAX_VALUE)) == 0) ? 404 : 404):
      case ((((int) (Integer.MIN_VALUE | Integer.MIN_VALUE)) == 0) ? 405 : 405):
      case ((((int) (Integer.MIN_VALUE | -1)) == 0) ? 406 : 406):
      case ((((int) (Integer.MIN_VALUE | 0)) == 0) ? 407 : 407):
      case ((((int) (Integer.MIN_VALUE | 1)) == 0) ? 408 : 408):
      case ((((int) (Integer.MIN_VALUE | Integer.MAX_VALUE)) == 0) ? 409 : 409):
      case ((((int) (-1 | Integer.MIN_VALUE)) == 0) ? 410 : 410):
      case ((((int) (-1 | -1)) == 0) ? 411 : 411):
      case ((((int) (-1 | 0)) == 0) ? 412 : 412):
      case ((((int) (-1 | 1)) == 0) ? 413 : 413):
      case ((((int) (-1 | Integer.MAX_VALUE)) == 0) ? 414 : 414):
      case ((((int) (0 | Integer.MIN_VALUE)) == 0) ? 415 : 415):
      case ((((int) (0 | -1)) == 0) ? 416 : 416):
      case ((((int) (0 | 0)) == 0) ? 417 : 417):
      case ((((int) (0 | 1)) == 0) ? 418 : 418):
      case ((((int) (0 | Integer.MAX_VALUE)) == 0) ? 419 : 419):
      case ((((int) (1 | Integer.MIN_VALUE)) == 0) ? 420 : 420):
      case ((((int) (1 | -1)) == 0) ? 421 : 421):
      case ((((int) (1 | 0)) == 0) ? 422 : 422):
      case ((((int) (1 | 1)) == 0) ? 423 : 423):
      case ((((int) (1 | Integer.MAX_VALUE)) == 0) ? 424 : 424):
      case ((((int) (Integer.MAX_VALUE | Integer.MIN_VALUE)) == 0) ? 425 : 425):
      case ((((int) (Integer.MAX_VALUE | -1)) == 0) ? 426 : 426):
      case ((((int) (Integer.MAX_VALUE | 0)) == 0) ? 427 : 427):
      case ((((int) (Integer.MAX_VALUE | 1)) == 0) ? 428 : 428):
      case ((((int) (Integer.MAX_VALUE | Integer.MAX_VALUE)) == 0) ? 429 : 429):
      default:
    }
  }

  // --------
  // long tests
  static long longPlus(long x) { return (long) + x; }
  static long longMinus(long x) { return (long) - x; }
  static long longBitNot(long x) { return (long) ~ x; }
  static long longTimes(long x, long y) { return (long) (x * y); }
  static long longDiv(long x, long y) { return (long) (x / y); }
  static long longRem(long x, long y) { return (long) (x % y); }
  static long longAdd(long x, long y) { return (long) (x + y); }
  static long longSub(long x, long y) { return (long) (x - y); }
  static long longShl(long x, long y) { return (long) (x << y); }
  static long longShr(long x, long y) { return (long) (x >> y); }
  static long longUshr(long x, long y) { return (long) (x >>> y); }
  static boolean longLt(long x, long y) { return x < y; }
  static boolean longGt(long x, long y) { return x > y; }
  static boolean longLe(long x, long y) { return x <= y; }
  static boolean longGe(long x, long y) { return x >= y; }
  static boolean longEq(long x, long y) { return x == y; }
  static boolean longNe(long x, long y) { return x != y; }
  static long longAnd(long x, long y) { return (long) (x & y); }
  static long longXor(long x, long y) { return (long) (x ^ y); }
  static long longOr(long x, long y) { return (long) (x | y); }
  static void longTest() {
    Tester.checkEqual(longPlus(Long.MIN_VALUE), (long) + Long.MIN_VALUE, "(long) + Long.MIN_VALUE");
    Tester.checkEqual(longPlus(-1L), (long) + -1L, "(long) + -1L");
    Tester.checkEqual(longPlus(0L), (long) + 0L, "(long) + 0L");
    Tester.checkEqual(longPlus(1L), (long) + 1L, "(long) + 1L");
    Tester.checkEqual(longPlus(Long.MAX_VALUE), (long) + Long.MAX_VALUE, "(long) + Long.MAX_VALUE");
    Tester.checkEqual(longMinus(Long.MIN_VALUE), (long) - Long.MIN_VALUE, "(long) - Long.MIN_VALUE");
    Tester.checkEqual(longMinus(-1L), (long) - -1L, "(long) - -1L");
    Tester.checkEqual(longMinus(0L), (long) - 0L, "(long) - 0L");
    Tester.checkEqual(longMinus(1L), (long) - 1L, "(long) - 1L");
    Tester.checkEqual(longMinus(Long.MAX_VALUE), (long) - Long.MAX_VALUE, "(long) - Long.MAX_VALUE");
    Tester.checkEqual(longBitNot(Long.MIN_VALUE), (long) ~ Long.MIN_VALUE, "(long) ~ Long.MIN_VALUE");
    Tester.checkEqual(longBitNot(-1L), (long) ~ -1L, "(long) ~ -1L");
    Tester.checkEqual(longBitNot(0L), (long) ~ 0L, "(long) ~ 0L");
    Tester.checkEqual(longBitNot(1L), (long) ~ 1L, "(long) ~ 1L");
    Tester.checkEqual(longBitNot(Long.MAX_VALUE), (long) ~ Long.MAX_VALUE, "(long) ~ Long.MAX_VALUE");
    Tester.checkEqual(longTimes(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE * Long.MIN_VALUE), "(long) (Long.MIN_VALUE * Long.MIN_VALUE)");
    Tester.checkEqual(longTimes(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE * -1L), "(long) (Long.MIN_VALUE * -1L)");
    Tester.checkEqual(longTimes(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE * 0L), "(long) (Long.MIN_VALUE * 0L)");
    Tester.checkEqual(longTimes(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE * 1L), "(long) (Long.MIN_VALUE * 1L)");
    Tester.checkEqual(longTimes(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE * Long.MAX_VALUE), "(long) (Long.MIN_VALUE * Long.MAX_VALUE)");
    Tester.checkEqual(longTimes(-1L, Long.MIN_VALUE), (long) (-1L * Long.MIN_VALUE), "(long) (-1L * Long.MIN_VALUE)");
    Tester.checkEqual(longTimes(-1L, -1L), (long) (-1L * -1L), "(long) (-1L * -1L)");
    Tester.checkEqual(longTimes(-1L, 0L), (long) (-1L * 0L), "(long) (-1L * 0L)");
    Tester.checkEqual(longTimes(-1L, 1L), (long) (-1L * 1L), "(long) (-1L * 1L)");
    Tester.checkEqual(longTimes(-1L, Long.MAX_VALUE), (long) (-1L * Long.MAX_VALUE), "(long) (-1L * Long.MAX_VALUE)");
    Tester.checkEqual(longTimes(0L, Long.MIN_VALUE), (long) (0L * Long.MIN_VALUE), "(long) (0L * Long.MIN_VALUE)");
    Tester.checkEqual(longTimes(0L, -1L), (long) (0L * -1L), "(long) (0L * -1L)");
    Tester.checkEqual(longTimes(0L, 0L), (long) (0L * 0L), "(long) (0L * 0L)");
    Tester.checkEqual(longTimes(0L, 1L), (long) (0L * 1L), "(long) (0L * 1L)");
    Tester.checkEqual(longTimes(0L, Long.MAX_VALUE), (long) (0L * Long.MAX_VALUE), "(long) (0L * Long.MAX_VALUE)");
    Tester.checkEqual(longTimes(1L, Long.MIN_VALUE), (long) (1L * Long.MIN_VALUE), "(long) (1L * Long.MIN_VALUE)");
    Tester.checkEqual(longTimes(1L, -1L), (long) (1L * -1L), "(long) (1L * -1L)");
    Tester.checkEqual(longTimes(1L, 0L), (long) (1L * 0L), "(long) (1L * 0L)");
    Tester.checkEqual(longTimes(1L, 1L), (long) (1L * 1L), "(long) (1L * 1L)");
    Tester.checkEqual(longTimes(1L, Long.MAX_VALUE), (long) (1L * Long.MAX_VALUE), "(long) (1L * Long.MAX_VALUE)");
    Tester.checkEqual(longTimes(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE * Long.MIN_VALUE), "(long) (Long.MAX_VALUE * Long.MIN_VALUE)");
    Tester.checkEqual(longTimes(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE * -1L), "(long) (Long.MAX_VALUE * -1L)");
    Tester.checkEqual(longTimes(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE * 0L), "(long) (Long.MAX_VALUE * 0L)");
    Tester.checkEqual(longTimes(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE * 1L), "(long) (Long.MAX_VALUE * 1L)");
    Tester.checkEqual(longTimes(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE * Long.MAX_VALUE), "(long) (Long.MAX_VALUE * Long.MAX_VALUE)");
    Tester.checkEqual(longDiv(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE / Long.MIN_VALUE), "(long) (Long.MIN_VALUE / Long.MIN_VALUE)");
    Tester.checkEqual(longDiv(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE / -1L), "(long) (Long.MIN_VALUE / -1L)");
    Tester.checkEqual(longDiv(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE / 1L), "(long) (Long.MIN_VALUE / 1L)");
    Tester.checkEqual(longDiv(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE / Long.MAX_VALUE), "(long) (Long.MIN_VALUE / Long.MAX_VALUE)");
    Tester.checkEqual(longDiv(-1L, Long.MIN_VALUE), (long) (-1L / Long.MIN_VALUE), "(long) (-1L / Long.MIN_VALUE)");
    Tester.checkEqual(longDiv(-1L, -1L), (long) (-1L / -1L), "(long) (-1L / -1L)");
    Tester.checkEqual(longDiv(-1L, 1L), (long) (-1L / 1L), "(long) (-1L / 1L)");
    Tester.checkEqual(longDiv(-1L, Long.MAX_VALUE), (long) (-1L / Long.MAX_VALUE), "(long) (-1L / Long.MAX_VALUE)");
    Tester.checkEqual(longDiv(0L, Long.MIN_VALUE), (long) (0L / Long.MIN_VALUE), "(long) (0L / Long.MIN_VALUE)");
    Tester.checkEqual(longDiv(0L, -1L), (long) (0L / -1L), "(long) (0L / -1L)");
    Tester.checkEqual(longDiv(0L, 1L), (long) (0L / 1L), "(long) (0L / 1L)");
    Tester.checkEqual(longDiv(0L, Long.MAX_VALUE), (long) (0L / Long.MAX_VALUE), "(long) (0L / Long.MAX_VALUE)");
    Tester.checkEqual(longDiv(1L, Long.MIN_VALUE), (long) (1L / Long.MIN_VALUE), "(long) (1L / Long.MIN_VALUE)");
    Tester.checkEqual(longDiv(1L, -1L), (long) (1L / -1L), "(long) (1L / -1L)");
    Tester.checkEqual(longDiv(1L, 1L), (long) (1L / 1L), "(long) (1L / 1L)");
    Tester.checkEqual(longDiv(1L, Long.MAX_VALUE), (long) (1L / Long.MAX_VALUE), "(long) (1L / Long.MAX_VALUE)");
    Tester.checkEqual(longDiv(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE / Long.MIN_VALUE), "(long) (Long.MAX_VALUE / Long.MIN_VALUE)");
    Tester.checkEqual(longDiv(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE / -1L), "(long) (Long.MAX_VALUE / -1L)");
    Tester.checkEqual(longDiv(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE / 1L), "(long) (Long.MAX_VALUE / 1L)");
    Tester.checkEqual(longDiv(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE / Long.MAX_VALUE), "(long) (Long.MAX_VALUE / Long.MAX_VALUE)");
    Tester.checkEqual(longRem(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE % Long.MIN_VALUE), "(long) (Long.MIN_VALUE % Long.MIN_VALUE)");
    Tester.checkEqual(longRem(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE % -1L), "(long) (Long.MIN_VALUE % -1L)");
    Tester.checkEqual(longRem(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE % 1L), "(long) (Long.MIN_VALUE % 1L)");
    Tester.checkEqual(longRem(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE % Long.MAX_VALUE), "(long) (Long.MIN_VALUE % Long.MAX_VALUE)");
    Tester.checkEqual(longRem(-1L, Long.MIN_VALUE), (long) (-1L % Long.MIN_VALUE), "(long) (-1L % Long.MIN_VALUE)");
    Tester.checkEqual(longRem(-1L, -1L), (long) (-1L % -1L), "(long) (-1L % -1L)");
    Tester.checkEqual(longRem(-1L, 1L), (long) (-1L % 1L), "(long) (-1L % 1L)");
    Tester.checkEqual(longRem(-1L, Long.MAX_VALUE), (long) (-1L % Long.MAX_VALUE), "(long) (-1L % Long.MAX_VALUE)");
    Tester.checkEqual(longRem(0L, Long.MIN_VALUE), (long) (0L % Long.MIN_VALUE), "(long) (0L % Long.MIN_VALUE)");
    Tester.checkEqual(longRem(0L, -1L), (long) (0L % -1L), "(long) (0L % -1L)");
    Tester.checkEqual(longRem(0L, 1L), (long) (0L % 1L), "(long) (0L % 1L)");
    Tester.checkEqual(longRem(0L, Long.MAX_VALUE), (long) (0L % Long.MAX_VALUE), "(long) (0L % Long.MAX_VALUE)");
    Tester.checkEqual(longRem(1L, Long.MIN_VALUE), (long) (1L % Long.MIN_VALUE), "(long) (1L % Long.MIN_VALUE)");
    Tester.checkEqual(longRem(1L, -1L), (long) (1L % -1L), "(long) (1L % -1L)");
    Tester.checkEqual(longRem(1L, 1L), (long) (1L % 1L), "(long) (1L % 1L)");
    Tester.checkEqual(longRem(1L, Long.MAX_VALUE), (long) (1L % Long.MAX_VALUE), "(long) (1L % Long.MAX_VALUE)");
    Tester.checkEqual(longRem(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE % Long.MIN_VALUE), "(long) (Long.MAX_VALUE % Long.MIN_VALUE)");
    Tester.checkEqual(longRem(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE % -1L), "(long) (Long.MAX_VALUE % -1L)");
    Tester.checkEqual(longRem(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE % 1L), "(long) (Long.MAX_VALUE % 1L)");
    Tester.checkEqual(longRem(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE % Long.MAX_VALUE), "(long) (Long.MAX_VALUE % Long.MAX_VALUE)");
    Tester.checkEqual(longAdd(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE + Long.MIN_VALUE), "(long) (Long.MIN_VALUE + Long.MIN_VALUE)");
    Tester.checkEqual(longAdd(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE + -1L), "(long) (Long.MIN_VALUE + -1L)");
    Tester.checkEqual(longAdd(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE + 0L), "(long) (Long.MIN_VALUE + 0L)");
    Tester.checkEqual(longAdd(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE + 1L), "(long) (Long.MIN_VALUE + 1L)");
    Tester.checkEqual(longAdd(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE + Long.MAX_VALUE), "(long) (Long.MIN_VALUE + Long.MAX_VALUE)");
    Tester.checkEqual(longAdd(-1L, Long.MIN_VALUE), (long) (-1L + Long.MIN_VALUE), "(long) (-1L + Long.MIN_VALUE)");
    Tester.checkEqual(longAdd(-1L, -1L), (long) (-1L + -1L), "(long) (-1L + -1L)");
    Tester.checkEqual(longAdd(-1L, 0L), (long) (-1L + 0L), "(long) (-1L + 0L)");
    Tester.checkEqual(longAdd(-1L, 1L), (long) (-1L + 1L), "(long) (-1L + 1L)");
    Tester.checkEqual(longAdd(-1L, Long.MAX_VALUE), (long) (-1L + Long.MAX_VALUE), "(long) (-1L + Long.MAX_VALUE)");
    Tester.checkEqual(longAdd(0L, Long.MIN_VALUE), (long) (0L + Long.MIN_VALUE), "(long) (0L + Long.MIN_VALUE)");
    Tester.checkEqual(longAdd(0L, -1L), (long) (0L + -1L), "(long) (0L + -1L)");
    Tester.checkEqual(longAdd(0L, 0L), (long) (0L + 0L), "(long) (0L + 0L)");
    Tester.checkEqual(longAdd(0L, 1L), (long) (0L + 1L), "(long) (0L + 1L)");
    Tester.checkEqual(longAdd(0L, Long.MAX_VALUE), (long) (0L + Long.MAX_VALUE), "(long) (0L + Long.MAX_VALUE)");
    Tester.checkEqual(longAdd(1L, Long.MIN_VALUE), (long) (1L + Long.MIN_VALUE), "(long) (1L + Long.MIN_VALUE)");
    Tester.checkEqual(longAdd(1L, -1L), (long) (1L + -1L), "(long) (1L + -1L)");
    Tester.checkEqual(longAdd(1L, 0L), (long) (1L + 0L), "(long) (1L + 0L)");
    Tester.checkEqual(longAdd(1L, 1L), (long) (1L + 1L), "(long) (1L + 1L)");
    Tester.checkEqual(longAdd(1L, Long.MAX_VALUE), (long) (1L + Long.MAX_VALUE), "(long) (1L + Long.MAX_VALUE)");
    Tester.checkEqual(longAdd(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE + Long.MIN_VALUE), "(long) (Long.MAX_VALUE + Long.MIN_VALUE)");
    Tester.checkEqual(longAdd(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE + -1L), "(long) (Long.MAX_VALUE + -1L)");
    Tester.checkEqual(longAdd(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE + 0L), "(long) (Long.MAX_VALUE + 0L)");
    Tester.checkEqual(longAdd(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE + 1L), "(long) (Long.MAX_VALUE + 1L)");
    Tester.checkEqual(longAdd(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE + Long.MAX_VALUE), "(long) (Long.MAX_VALUE + Long.MAX_VALUE)");
    Tester.checkEqual(longSub(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE - Long.MIN_VALUE), "(long) (Long.MIN_VALUE - Long.MIN_VALUE)");
    Tester.checkEqual(longSub(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE - -1L), "(long) (Long.MIN_VALUE - -1L)");
    Tester.checkEqual(longSub(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE - 0L), "(long) (Long.MIN_VALUE - 0L)");
    Tester.checkEqual(longSub(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE - 1L), "(long) (Long.MIN_VALUE - 1L)");
    Tester.checkEqual(longSub(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE - Long.MAX_VALUE), "(long) (Long.MIN_VALUE - Long.MAX_VALUE)");
    Tester.checkEqual(longSub(-1L, Long.MIN_VALUE), (long) (-1L - Long.MIN_VALUE), "(long) (-1L - Long.MIN_VALUE)");
    Tester.checkEqual(longSub(-1L, -1L), (long) (-1L - -1L), "(long) (-1L - -1L)");
    Tester.checkEqual(longSub(-1L, 0L), (long) (-1L - 0L), "(long) (-1L - 0L)");
    Tester.checkEqual(longSub(-1L, 1L), (long) (-1L - 1L), "(long) (-1L - 1L)");
    Tester.checkEqual(longSub(-1L, Long.MAX_VALUE), (long) (-1L - Long.MAX_VALUE), "(long) (-1L - Long.MAX_VALUE)");
    Tester.checkEqual(longSub(0L, Long.MIN_VALUE), (long) (0L - Long.MIN_VALUE), "(long) (0L - Long.MIN_VALUE)");
    Tester.checkEqual(longSub(0L, -1L), (long) (0L - -1L), "(long) (0L - -1L)");
    Tester.checkEqual(longSub(0L, 0L), (long) (0L - 0L), "(long) (0L - 0L)");
    Tester.checkEqual(longSub(0L, 1L), (long) (0L - 1L), "(long) (0L - 1L)");
    Tester.checkEqual(longSub(0L, Long.MAX_VALUE), (long) (0L - Long.MAX_VALUE), "(long) (0L - Long.MAX_VALUE)");
    Tester.checkEqual(longSub(1L, Long.MIN_VALUE), (long) (1L - Long.MIN_VALUE), "(long) (1L - Long.MIN_VALUE)");
    Tester.checkEqual(longSub(1L, -1L), (long) (1L - -1L), "(long) (1L - -1L)");
    Tester.checkEqual(longSub(1L, 0L), (long) (1L - 0L), "(long) (1L - 0L)");
    Tester.checkEqual(longSub(1L, 1L), (long) (1L - 1L), "(long) (1L - 1L)");
    Tester.checkEqual(longSub(1L, Long.MAX_VALUE), (long) (1L - Long.MAX_VALUE), "(long) (1L - Long.MAX_VALUE)");
    Tester.checkEqual(longSub(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE - Long.MIN_VALUE), "(long) (Long.MAX_VALUE - Long.MIN_VALUE)");
    Tester.checkEqual(longSub(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE - -1L), "(long) (Long.MAX_VALUE - -1L)");
    Tester.checkEqual(longSub(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE - 0L), "(long) (Long.MAX_VALUE - 0L)");
    Tester.checkEqual(longSub(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE - 1L), "(long) (Long.MAX_VALUE - 1L)");
    Tester.checkEqual(longSub(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE - Long.MAX_VALUE), "(long) (Long.MAX_VALUE - Long.MAX_VALUE)");
    Tester.checkEqual(longShl(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE << Long.MIN_VALUE), "(long) (Long.MIN_VALUE << Long.MIN_VALUE)");
    Tester.checkEqual(longShl(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE << -1L), "(long) (Long.MIN_VALUE << -1L)");
    Tester.checkEqual(longShl(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE << 0L), "(long) (Long.MIN_VALUE << 0L)");
    Tester.checkEqual(longShl(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE << 1L), "(long) (Long.MIN_VALUE << 1L)");
    Tester.checkEqual(longShl(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE << Long.MAX_VALUE), "(long) (Long.MIN_VALUE << Long.MAX_VALUE)");
    Tester.checkEqual(longShl(-1L, Long.MIN_VALUE), (long) (-1L << Long.MIN_VALUE), "(long) (-1L << Long.MIN_VALUE)");
    Tester.checkEqual(longShl(-1L, -1L), (long) (-1L << -1L), "(long) (-1L << -1L)");
    Tester.checkEqual(longShl(-1L, 0L), (long) (-1L << 0L), "(long) (-1L << 0L)");
    Tester.checkEqual(longShl(-1L, 1L), (long) (-1L << 1L), "(long) (-1L << 1L)");
    Tester.checkEqual(longShl(-1L, Long.MAX_VALUE), (long) (-1L << Long.MAX_VALUE), "(long) (-1L << Long.MAX_VALUE)");
    Tester.checkEqual(longShl(0L, Long.MIN_VALUE), (long) (0L << Long.MIN_VALUE), "(long) (0L << Long.MIN_VALUE)");
    Tester.checkEqual(longShl(0L, -1L), (long) (0L << -1L), "(long) (0L << -1L)");
    Tester.checkEqual(longShl(0L, 0L), (long) (0L << 0L), "(long) (0L << 0L)");
    Tester.checkEqual(longShl(0L, 1L), (long) (0L << 1L), "(long) (0L << 1L)");
    Tester.checkEqual(longShl(0L, Long.MAX_VALUE), (long) (0L << Long.MAX_VALUE), "(long) (0L << Long.MAX_VALUE)");
    Tester.checkEqual(longShl(1L, Long.MIN_VALUE), (long) (1L << Long.MIN_VALUE), "(long) (1L << Long.MIN_VALUE)");
    Tester.checkEqual(longShl(1L, -1L), (long) (1L << -1L), "(long) (1L << -1L)");
    Tester.checkEqual(longShl(1L, 0L), (long) (1L << 0L), "(long) (1L << 0L)");
    Tester.checkEqual(longShl(1L, 1L), (long) (1L << 1L), "(long) (1L << 1L)");
    Tester.checkEqual(longShl(1L, Long.MAX_VALUE), (long) (1L << Long.MAX_VALUE), "(long) (1L << Long.MAX_VALUE)");
    Tester.checkEqual(longShl(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE << Long.MIN_VALUE), "(long) (Long.MAX_VALUE << Long.MIN_VALUE)");
    Tester.checkEqual(longShl(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE << -1L), "(long) (Long.MAX_VALUE << -1L)");
    Tester.checkEqual(longShl(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE << 0L), "(long) (Long.MAX_VALUE << 0L)");
    Tester.checkEqual(longShl(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE << 1L), "(long) (Long.MAX_VALUE << 1L)");
    Tester.checkEqual(longShl(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE << Long.MAX_VALUE), "(long) (Long.MAX_VALUE << Long.MAX_VALUE)");
    Tester.checkEqual(longShr(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE >> Long.MIN_VALUE), "(long) (Long.MIN_VALUE >> Long.MIN_VALUE)");
    Tester.checkEqual(longShr(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE >> -1L), "(long) (Long.MIN_VALUE >> -1L)");
    Tester.checkEqual(longShr(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE >> 0L), "(long) (Long.MIN_VALUE >> 0L)");
    Tester.checkEqual(longShr(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE >> 1L), "(long) (Long.MIN_VALUE >> 1L)");
    Tester.checkEqual(longShr(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE >> Long.MAX_VALUE), "(long) (Long.MIN_VALUE >> Long.MAX_VALUE)");
    Tester.checkEqual(longShr(-1L, Long.MIN_VALUE), (long) (-1L >> Long.MIN_VALUE), "(long) (-1L >> Long.MIN_VALUE)");
    Tester.checkEqual(longShr(-1L, -1L), (long) (-1L >> -1L), "(long) (-1L >> -1L)");
    Tester.checkEqual(longShr(-1L, 0L), (long) (-1L >> 0L), "(long) (-1L >> 0L)");
    Tester.checkEqual(longShr(-1L, 1L), (long) (-1L >> 1L), "(long) (-1L >> 1L)");
    Tester.checkEqual(longShr(-1L, Long.MAX_VALUE), (long) (-1L >> Long.MAX_VALUE), "(long) (-1L >> Long.MAX_VALUE)");
    Tester.checkEqual(longShr(0L, Long.MIN_VALUE), (long) (0L >> Long.MIN_VALUE), "(long) (0L >> Long.MIN_VALUE)");
    Tester.checkEqual(longShr(0L, -1L), (long) (0L >> -1L), "(long) (0L >> -1L)");
    Tester.checkEqual(longShr(0L, 0L), (long) (0L >> 0L), "(long) (0L >> 0L)");
    Tester.checkEqual(longShr(0L, 1L), (long) (0L >> 1L), "(long) (0L >> 1L)");
    Tester.checkEqual(longShr(0L, Long.MAX_VALUE), (long) (0L >> Long.MAX_VALUE), "(long) (0L >> Long.MAX_VALUE)");
    Tester.checkEqual(longShr(1L, Long.MIN_VALUE), (long) (1L >> Long.MIN_VALUE), "(long) (1L >> Long.MIN_VALUE)");
    Tester.checkEqual(longShr(1L, -1L), (long) (1L >> -1L), "(long) (1L >> -1L)");
    Tester.checkEqual(longShr(1L, 0L), (long) (1L >> 0L), "(long) (1L >> 0L)");
    Tester.checkEqual(longShr(1L, 1L), (long) (1L >> 1L), "(long) (1L >> 1L)");
    Tester.checkEqual(longShr(1L, Long.MAX_VALUE), (long) (1L >> Long.MAX_VALUE), "(long) (1L >> Long.MAX_VALUE)");
    Tester.checkEqual(longShr(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE >> Long.MIN_VALUE), "(long) (Long.MAX_VALUE >> Long.MIN_VALUE)");
    Tester.checkEqual(longShr(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE >> -1L), "(long) (Long.MAX_VALUE >> -1L)");
    Tester.checkEqual(longShr(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE >> 0L), "(long) (Long.MAX_VALUE >> 0L)");
    Tester.checkEqual(longShr(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE >> 1L), "(long) (Long.MAX_VALUE >> 1L)");
    Tester.checkEqual(longShr(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE >> Long.MAX_VALUE), "(long) (Long.MAX_VALUE >> Long.MAX_VALUE)");
    Tester.checkEqual(longUshr(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE >>> Long.MIN_VALUE), "(long) (Long.MIN_VALUE >>> Long.MIN_VALUE)");
    Tester.checkEqual(longUshr(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE >>> -1L), "(long) (Long.MIN_VALUE >>> -1L)");
    Tester.checkEqual(longUshr(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE >>> 0L), "(long) (Long.MIN_VALUE >>> 0L)");
    Tester.checkEqual(longUshr(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE >>> 1L), "(long) (Long.MIN_VALUE >>> 1L)");
    Tester.checkEqual(longUshr(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE >>> Long.MAX_VALUE), "(long) (Long.MIN_VALUE >>> Long.MAX_VALUE)");
    Tester.checkEqual(longUshr(-1L, Long.MIN_VALUE), (long) (-1L >>> Long.MIN_VALUE), "(long) (-1L >>> Long.MIN_VALUE)");
    Tester.checkEqual(longUshr(-1L, -1L), (long) (-1L >>> -1L), "(long) (-1L >>> -1L)");
    Tester.checkEqual(longUshr(-1L, 0L), (long) (-1L >>> 0L), "(long) (-1L >>> 0L)");
    Tester.checkEqual(longUshr(-1L, 1L), (long) (-1L >>> 1L), "(long) (-1L >>> 1L)");
    Tester.checkEqual(longUshr(-1L, Long.MAX_VALUE), (long) (-1L >>> Long.MAX_VALUE), "(long) (-1L >>> Long.MAX_VALUE)");
    Tester.checkEqual(longUshr(0L, Long.MIN_VALUE), (long) (0L >>> Long.MIN_VALUE), "(long) (0L >>> Long.MIN_VALUE)");
    Tester.checkEqual(longUshr(0L, -1L), (long) (0L >>> -1L), "(long) (0L >>> -1L)");
    Tester.checkEqual(longUshr(0L, 0L), (long) (0L >>> 0L), "(long) (0L >>> 0L)");
    Tester.checkEqual(longUshr(0L, 1L), (long) (0L >>> 1L), "(long) (0L >>> 1L)");
    Tester.checkEqual(longUshr(0L, Long.MAX_VALUE), (long) (0L >>> Long.MAX_VALUE), "(long) (0L >>> Long.MAX_VALUE)");
    Tester.checkEqual(longUshr(1L, Long.MIN_VALUE), (long) (1L >>> Long.MIN_VALUE), "(long) (1L >>> Long.MIN_VALUE)");
    Tester.checkEqual(longUshr(1L, -1L), (long) (1L >>> -1L), "(long) (1L >>> -1L)");
    Tester.checkEqual(longUshr(1L, 0L), (long) (1L >>> 0L), "(long) (1L >>> 0L)");
    Tester.checkEqual(longUshr(1L, 1L), (long) (1L >>> 1L), "(long) (1L >>> 1L)");
    Tester.checkEqual(longUshr(1L, Long.MAX_VALUE), (long) (1L >>> Long.MAX_VALUE), "(long) (1L >>> Long.MAX_VALUE)");
    Tester.checkEqual(longUshr(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE >>> Long.MIN_VALUE), "(long) (Long.MAX_VALUE >>> Long.MIN_VALUE)");
    Tester.checkEqual(longUshr(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE >>> -1L), "(long) (Long.MAX_VALUE >>> -1L)");
    Tester.checkEqual(longUshr(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE >>> 0L), "(long) (Long.MAX_VALUE >>> 0L)");
    Tester.checkEqual(longUshr(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE >>> 1L), "(long) (Long.MAX_VALUE >>> 1L)");
    Tester.checkEqual(longUshr(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE >>> Long.MAX_VALUE), "(long) (Long.MAX_VALUE >>> Long.MAX_VALUE)");
    Tester.checkEqual(longLt(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE < Long.MIN_VALUE, "Long.MIN_VALUE < Long.MIN_VALUE");
    Tester.checkEqual(longLt(Long.MIN_VALUE, -1L), Long.MIN_VALUE < -1L, "Long.MIN_VALUE < -1L");
    Tester.checkEqual(longLt(Long.MIN_VALUE, 0L), Long.MIN_VALUE < 0L, "Long.MIN_VALUE < 0L");
    Tester.checkEqual(longLt(Long.MIN_VALUE, 1L), Long.MIN_VALUE < 1L, "Long.MIN_VALUE < 1L");
    Tester.checkEqual(longLt(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE < Long.MAX_VALUE, "Long.MIN_VALUE < Long.MAX_VALUE");
    Tester.checkEqual(longLt(-1L, Long.MIN_VALUE), -1L < Long.MIN_VALUE, "-1L < Long.MIN_VALUE");
    Tester.checkEqual(longLt(-1L, -1L), -1L < -1L, "-1L < -1L");
    Tester.checkEqual(longLt(-1L, 0L), -1L < 0L, "-1L < 0L");
    Tester.checkEqual(longLt(-1L, 1L), -1L < 1L, "-1L < 1L");
    Tester.checkEqual(longLt(-1L, Long.MAX_VALUE), -1L < Long.MAX_VALUE, "-1L < Long.MAX_VALUE");
    Tester.checkEqual(longLt(0L, Long.MIN_VALUE), 0L < Long.MIN_VALUE, "0L < Long.MIN_VALUE");
    Tester.checkEqual(longLt(0L, -1L), 0L < -1L, "0L < -1L");
    Tester.checkEqual(longLt(0L, 0L), 0L < 0L, "0L < 0L");
    Tester.checkEqual(longLt(0L, 1L), 0L < 1L, "0L < 1L");
    Tester.checkEqual(longLt(0L, Long.MAX_VALUE), 0L < Long.MAX_VALUE, "0L < Long.MAX_VALUE");
    Tester.checkEqual(longLt(1L, Long.MIN_VALUE), 1L < Long.MIN_VALUE, "1L < Long.MIN_VALUE");
    Tester.checkEqual(longLt(1L, -1L), 1L < -1L, "1L < -1L");
    Tester.checkEqual(longLt(1L, 0L), 1L < 0L, "1L < 0L");
    Tester.checkEqual(longLt(1L, 1L), 1L < 1L, "1L < 1L");
    Tester.checkEqual(longLt(1L, Long.MAX_VALUE), 1L < Long.MAX_VALUE, "1L < Long.MAX_VALUE");
    Tester.checkEqual(longLt(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE < Long.MIN_VALUE, "Long.MAX_VALUE < Long.MIN_VALUE");
    Tester.checkEqual(longLt(Long.MAX_VALUE, -1L), Long.MAX_VALUE < -1L, "Long.MAX_VALUE < -1L");
    Tester.checkEqual(longLt(Long.MAX_VALUE, 0L), Long.MAX_VALUE < 0L, "Long.MAX_VALUE < 0L");
    Tester.checkEqual(longLt(Long.MAX_VALUE, 1L), Long.MAX_VALUE < 1L, "Long.MAX_VALUE < 1L");
    Tester.checkEqual(longLt(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE < Long.MAX_VALUE, "Long.MAX_VALUE < Long.MAX_VALUE");
    Tester.checkEqual(longGt(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE > Long.MIN_VALUE, "Long.MIN_VALUE > Long.MIN_VALUE");
    Tester.checkEqual(longGt(Long.MIN_VALUE, -1L), Long.MIN_VALUE > -1L, "Long.MIN_VALUE > -1L");
    Tester.checkEqual(longGt(Long.MIN_VALUE, 0L), Long.MIN_VALUE > 0L, "Long.MIN_VALUE > 0L");
    Tester.checkEqual(longGt(Long.MIN_VALUE, 1L), Long.MIN_VALUE > 1L, "Long.MIN_VALUE > 1L");
    Tester.checkEqual(longGt(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE > Long.MAX_VALUE, "Long.MIN_VALUE > Long.MAX_VALUE");
    Tester.checkEqual(longGt(-1L, Long.MIN_VALUE), -1L > Long.MIN_VALUE, "-1L > Long.MIN_VALUE");
    Tester.checkEqual(longGt(-1L, -1L), -1L > -1L, "-1L > -1L");
    Tester.checkEqual(longGt(-1L, 0L), -1L > 0L, "-1L > 0L");
    Tester.checkEqual(longGt(-1L, 1L), -1L > 1L, "-1L > 1L");
    Tester.checkEqual(longGt(-1L, Long.MAX_VALUE), -1L > Long.MAX_VALUE, "-1L > Long.MAX_VALUE");
    Tester.checkEqual(longGt(0L, Long.MIN_VALUE), 0L > Long.MIN_VALUE, "0L > Long.MIN_VALUE");
    Tester.checkEqual(longGt(0L, -1L), 0L > -1L, "0L > -1L");
    Tester.checkEqual(longGt(0L, 0L), 0L > 0L, "0L > 0L");
    Tester.checkEqual(longGt(0L, 1L), 0L > 1L, "0L > 1L");
    Tester.checkEqual(longGt(0L, Long.MAX_VALUE), 0L > Long.MAX_VALUE, "0L > Long.MAX_VALUE");
    Tester.checkEqual(longGt(1L, Long.MIN_VALUE), 1L > Long.MIN_VALUE, "1L > Long.MIN_VALUE");
    Tester.checkEqual(longGt(1L, -1L), 1L > -1L, "1L > -1L");
    Tester.checkEqual(longGt(1L, 0L), 1L > 0L, "1L > 0L");
    Tester.checkEqual(longGt(1L, 1L), 1L > 1L, "1L > 1L");
    Tester.checkEqual(longGt(1L, Long.MAX_VALUE), 1L > Long.MAX_VALUE, "1L > Long.MAX_VALUE");
    Tester.checkEqual(longGt(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE > Long.MIN_VALUE, "Long.MAX_VALUE > Long.MIN_VALUE");
    Tester.checkEqual(longGt(Long.MAX_VALUE, -1L), Long.MAX_VALUE > -1L, "Long.MAX_VALUE > -1L");
    Tester.checkEqual(longGt(Long.MAX_VALUE, 0L), Long.MAX_VALUE > 0L, "Long.MAX_VALUE > 0L");
    Tester.checkEqual(longGt(Long.MAX_VALUE, 1L), Long.MAX_VALUE > 1L, "Long.MAX_VALUE > 1L");
    Tester.checkEqual(longGt(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE > Long.MAX_VALUE, "Long.MAX_VALUE > Long.MAX_VALUE");
    Tester.checkEqual(longLe(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE <= Long.MIN_VALUE, "Long.MIN_VALUE <= Long.MIN_VALUE");
    Tester.checkEqual(longLe(Long.MIN_VALUE, -1L), Long.MIN_VALUE <= -1L, "Long.MIN_VALUE <= -1L");
    Tester.checkEqual(longLe(Long.MIN_VALUE, 0L), Long.MIN_VALUE <= 0L, "Long.MIN_VALUE <= 0L");
    Tester.checkEqual(longLe(Long.MIN_VALUE, 1L), Long.MIN_VALUE <= 1L, "Long.MIN_VALUE <= 1L");
    Tester.checkEqual(longLe(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE <= Long.MAX_VALUE, "Long.MIN_VALUE <= Long.MAX_VALUE");
    Tester.checkEqual(longLe(-1L, Long.MIN_VALUE), -1L <= Long.MIN_VALUE, "-1L <= Long.MIN_VALUE");
    Tester.checkEqual(longLe(-1L, -1L), -1L <= -1L, "-1L <= -1L");
    Tester.checkEqual(longLe(-1L, 0L), -1L <= 0L, "-1L <= 0L");
    Tester.checkEqual(longLe(-1L, 1L), -1L <= 1L, "-1L <= 1L");
    Tester.checkEqual(longLe(-1L, Long.MAX_VALUE), -1L <= Long.MAX_VALUE, "-1L <= Long.MAX_VALUE");
    Tester.checkEqual(longLe(0L, Long.MIN_VALUE), 0L <= Long.MIN_VALUE, "0L <= Long.MIN_VALUE");
    Tester.checkEqual(longLe(0L, -1L), 0L <= -1L, "0L <= -1L");
    Tester.checkEqual(longLe(0L, 0L), 0L <= 0L, "0L <= 0L");
    Tester.checkEqual(longLe(0L, 1L), 0L <= 1L, "0L <= 1L");
    Tester.checkEqual(longLe(0L, Long.MAX_VALUE), 0L <= Long.MAX_VALUE, "0L <= Long.MAX_VALUE");
    Tester.checkEqual(longLe(1L, Long.MIN_VALUE), 1L <= Long.MIN_VALUE, "1L <= Long.MIN_VALUE");
    Tester.checkEqual(longLe(1L, -1L), 1L <= -1L, "1L <= -1L");
    Tester.checkEqual(longLe(1L, 0L), 1L <= 0L, "1L <= 0L");
    Tester.checkEqual(longLe(1L, 1L), 1L <= 1L, "1L <= 1L");
    Tester.checkEqual(longLe(1L, Long.MAX_VALUE), 1L <= Long.MAX_VALUE, "1L <= Long.MAX_VALUE");
    Tester.checkEqual(longLe(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE <= Long.MIN_VALUE, "Long.MAX_VALUE <= Long.MIN_VALUE");
    Tester.checkEqual(longLe(Long.MAX_VALUE, -1L), Long.MAX_VALUE <= -1L, "Long.MAX_VALUE <= -1L");
    Tester.checkEqual(longLe(Long.MAX_VALUE, 0L), Long.MAX_VALUE <= 0L, "Long.MAX_VALUE <= 0L");
    Tester.checkEqual(longLe(Long.MAX_VALUE, 1L), Long.MAX_VALUE <= 1L, "Long.MAX_VALUE <= 1L");
    Tester.checkEqual(longLe(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE <= Long.MAX_VALUE, "Long.MAX_VALUE <= Long.MAX_VALUE");
    Tester.checkEqual(longGe(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE >= Long.MIN_VALUE, "Long.MIN_VALUE >= Long.MIN_VALUE");
    Tester.checkEqual(longGe(Long.MIN_VALUE, -1L), Long.MIN_VALUE >= -1L, "Long.MIN_VALUE >= -1L");
    Tester.checkEqual(longGe(Long.MIN_VALUE, 0L), Long.MIN_VALUE >= 0L, "Long.MIN_VALUE >= 0L");
    Tester.checkEqual(longGe(Long.MIN_VALUE, 1L), Long.MIN_VALUE >= 1L, "Long.MIN_VALUE >= 1L");
    Tester.checkEqual(longGe(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE >= Long.MAX_VALUE, "Long.MIN_VALUE >= Long.MAX_VALUE");
    Tester.checkEqual(longGe(-1L, Long.MIN_VALUE), -1L >= Long.MIN_VALUE, "-1L >= Long.MIN_VALUE");
    Tester.checkEqual(longGe(-1L, -1L), -1L >= -1L, "-1L >= -1L");
    Tester.checkEqual(longGe(-1L, 0L), -1L >= 0L, "-1L >= 0L");
    Tester.checkEqual(longGe(-1L, 1L), -1L >= 1L, "-1L >= 1L");
    Tester.checkEqual(longGe(-1L, Long.MAX_VALUE), -1L >= Long.MAX_VALUE, "-1L >= Long.MAX_VALUE");
    Tester.checkEqual(longGe(0L, Long.MIN_VALUE), 0L >= Long.MIN_VALUE, "0L >= Long.MIN_VALUE");
    Tester.checkEqual(longGe(0L, -1L), 0L >= -1L, "0L >= -1L");
    Tester.checkEqual(longGe(0L, 0L), 0L >= 0L, "0L >= 0L");
    Tester.checkEqual(longGe(0L, 1L), 0L >= 1L, "0L >= 1L");
    Tester.checkEqual(longGe(0L, Long.MAX_VALUE), 0L >= Long.MAX_VALUE, "0L >= Long.MAX_VALUE");
    Tester.checkEqual(longGe(1L, Long.MIN_VALUE), 1L >= Long.MIN_VALUE, "1L >= Long.MIN_VALUE");
    Tester.checkEqual(longGe(1L, -1L), 1L >= -1L, "1L >= -1L");
    Tester.checkEqual(longGe(1L, 0L), 1L >= 0L, "1L >= 0L");
    Tester.checkEqual(longGe(1L, 1L), 1L >= 1L, "1L >= 1L");
    Tester.checkEqual(longGe(1L, Long.MAX_VALUE), 1L >= Long.MAX_VALUE, "1L >= Long.MAX_VALUE");
    Tester.checkEqual(longGe(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE >= Long.MIN_VALUE, "Long.MAX_VALUE >= Long.MIN_VALUE");
    Tester.checkEqual(longGe(Long.MAX_VALUE, -1L), Long.MAX_VALUE >= -1L, "Long.MAX_VALUE >= -1L");
    Tester.checkEqual(longGe(Long.MAX_VALUE, 0L), Long.MAX_VALUE >= 0L, "Long.MAX_VALUE >= 0L");
    Tester.checkEqual(longGe(Long.MAX_VALUE, 1L), Long.MAX_VALUE >= 1L, "Long.MAX_VALUE >= 1L");
    Tester.checkEqual(longGe(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE >= Long.MAX_VALUE, "Long.MAX_VALUE >= Long.MAX_VALUE");
    Tester.checkEqual(longEq(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE == Long.MIN_VALUE, "Long.MIN_VALUE == Long.MIN_VALUE");
    Tester.checkEqual(longEq(Long.MIN_VALUE, -1L), Long.MIN_VALUE == -1L, "Long.MIN_VALUE == -1L");
    Tester.checkEqual(longEq(Long.MIN_VALUE, 0L), Long.MIN_VALUE == 0L, "Long.MIN_VALUE == 0L");
    Tester.checkEqual(longEq(Long.MIN_VALUE, 1L), Long.MIN_VALUE == 1L, "Long.MIN_VALUE == 1L");
    Tester.checkEqual(longEq(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE == Long.MAX_VALUE, "Long.MIN_VALUE == Long.MAX_VALUE");
    Tester.checkEqual(longEq(-1L, Long.MIN_VALUE), -1L == Long.MIN_VALUE, "-1L == Long.MIN_VALUE");
    Tester.checkEqual(longEq(-1L, -1L), -1L == -1L, "-1L == -1L");
    Tester.checkEqual(longEq(-1L, 0L), -1L == 0L, "-1L == 0L");
    Tester.checkEqual(longEq(-1L, 1L), -1L == 1L, "-1L == 1L");
    Tester.checkEqual(longEq(-1L, Long.MAX_VALUE), -1L == Long.MAX_VALUE, "-1L == Long.MAX_VALUE");
    Tester.checkEqual(longEq(0L, Long.MIN_VALUE), 0L == Long.MIN_VALUE, "0L == Long.MIN_VALUE");
    Tester.checkEqual(longEq(0L, -1L), 0L == -1L, "0L == -1L");
    Tester.checkEqual(longEq(0L, 0L), 0L == 0L, "0L == 0L");
    Tester.checkEqual(longEq(0L, 1L), 0L == 1L, "0L == 1L");
    Tester.checkEqual(longEq(0L, Long.MAX_VALUE), 0L == Long.MAX_VALUE, "0L == Long.MAX_VALUE");
    Tester.checkEqual(longEq(1L, Long.MIN_VALUE), 1L == Long.MIN_VALUE, "1L == Long.MIN_VALUE");
    Tester.checkEqual(longEq(1L, -1L), 1L == -1L, "1L == -1L");
    Tester.checkEqual(longEq(1L, 0L), 1L == 0L, "1L == 0L");
    Tester.checkEqual(longEq(1L, 1L), 1L == 1L, "1L == 1L");
    Tester.checkEqual(longEq(1L, Long.MAX_VALUE), 1L == Long.MAX_VALUE, "1L == Long.MAX_VALUE");
    Tester.checkEqual(longEq(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE == Long.MIN_VALUE, "Long.MAX_VALUE == Long.MIN_VALUE");
    Tester.checkEqual(longEq(Long.MAX_VALUE, -1L), Long.MAX_VALUE == -1L, "Long.MAX_VALUE == -1L");
    Tester.checkEqual(longEq(Long.MAX_VALUE, 0L), Long.MAX_VALUE == 0L, "Long.MAX_VALUE == 0L");
    Tester.checkEqual(longEq(Long.MAX_VALUE, 1L), Long.MAX_VALUE == 1L, "Long.MAX_VALUE == 1L");
    Tester.checkEqual(longEq(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE == Long.MAX_VALUE, "Long.MAX_VALUE == Long.MAX_VALUE");
    Tester.checkEqual(longNe(Long.MIN_VALUE, Long.MIN_VALUE), Long.MIN_VALUE != Long.MIN_VALUE, "Long.MIN_VALUE != Long.MIN_VALUE");
    Tester.checkEqual(longNe(Long.MIN_VALUE, -1L), Long.MIN_VALUE != -1L, "Long.MIN_VALUE != -1L");
    Tester.checkEqual(longNe(Long.MIN_VALUE, 0L), Long.MIN_VALUE != 0L, "Long.MIN_VALUE != 0L");
    Tester.checkEqual(longNe(Long.MIN_VALUE, 1L), Long.MIN_VALUE != 1L, "Long.MIN_VALUE != 1L");
    Tester.checkEqual(longNe(Long.MIN_VALUE, Long.MAX_VALUE), Long.MIN_VALUE != Long.MAX_VALUE, "Long.MIN_VALUE != Long.MAX_VALUE");
    Tester.checkEqual(longNe(-1L, Long.MIN_VALUE), -1L != Long.MIN_VALUE, "-1L != Long.MIN_VALUE");
    Tester.checkEqual(longNe(-1L, -1L), -1L != -1L, "-1L != -1L");
    Tester.checkEqual(longNe(-1L, 0L), -1L != 0L, "-1L != 0L");
    Tester.checkEqual(longNe(-1L, 1L), -1L != 1L, "-1L != 1L");
    Tester.checkEqual(longNe(-1L, Long.MAX_VALUE), -1L != Long.MAX_VALUE, "-1L != Long.MAX_VALUE");
    Tester.checkEqual(longNe(0L, Long.MIN_VALUE), 0L != Long.MIN_VALUE, "0L != Long.MIN_VALUE");
    Tester.checkEqual(longNe(0L, -1L), 0L != -1L, "0L != -1L");
    Tester.checkEqual(longNe(0L, 0L), 0L != 0L, "0L != 0L");
    Tester.checkEqual(longNe(0L, 1L), 0L != 1L, "0L != 1L");
    Tester.checkEqual(longNe(0L, Long.MAX_VALUE), 0L != Long.MAX_VALUE, "0L != Long.MAX_VALUE");
    Tester.checkEqual(longNe(1L, Long.MIN_VALUE), 1L != Long.MIN_VALUE, "1L != Long.MIN_VALUE");
    Tester.checkEqual(longNe(1L, -1L), 1L != -1L, "1L != -1L");
    Tester.checkEqual(longNe(1L, 0L), 1L != 0L, "1L != 0L");
    Tester.checkEqual(longNe(1L, 1L), 1L != 1L, "1L != 1L");
    Tester.checkEqual(longNe(1L, Long.MAX_VALUE), 1L != Long.MAX_VALUE, "1L != Long.MAX_VALUE");
    Tester.checkEqual(longNe(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE != Long.MIN_VALUE, "Long.MAX_VALUE != Long.MIN_VALUE");
    Tester.checkEqual(longNe(Long.MAX_VALUE, -1L), Long.MAX_VALUE != -1L, "Long.MAX_VALUE != -1L");
    Tester.checkEqual(longNe(Long.MAX_VALUE, 0L), Long.MAX_VALUE != 0L, "Long.MAX_VALUE != 0L");
    Tester.checkEqual(longNe(Long.MAX_VALUE, 1L), Long.MAX_VALUE != 1L, "Long.MAX_VALUE != 1L");
    Tester.checkEqual(longNe(Long.MAX_VALUE, Long.MAX_VALUE), Long.MAX_VALUE != Long.MAX_VALUE, "Long.MAX_VALUE != Long.MAX_VALUE");
    Tester.checkEqual(longAnd(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE & Long.MIN_VALUE), "(long) (Long.MIN_VALUE & Long.MIN_VALUE)");
    Tester.checkEqual(longAnd(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE & -1L), "(long) (Long.MIN_VALUE & -1L)");
    Tester.checkEqual(longAnd(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE & 0L), "(long) (Long.MIN_VALUE & 0L)");
    Tester.checkEqual(longAnd(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE & 1L), "(long) (Long.MIN_VALUE & 1L)");
    Tester.checkEqual(longAnd(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE & Long.MAX_VALUE), "(long) (Long.MIN_VALUE & Long.MAX_VALUE)");
    Tester.checkEqual(longAnd(-1L, Long.MIN_VALUE), (long) (-1L & Long.MIN_VALUE), "(long) (-1L & Long.MIN_VALUE)");
    Tester.checkEqual(longAnd(-1L, -1L), (long) (-1L & -1L), "(long) (-1L & -1L)");
    Tester.checkEqual(longAnd(-1L, 0L), (long) (-1L & 0L), "(long) (-1L & 0L)");
    Tester.checkEqual(longAnd(-1L, 1L), (long) (-1L & 1L), "(long) (-1L & 1L)");
    Tester.checkEqual(longAnd(-1L, Long.MAX_VALUE), (long) (-1L & Long.MAX_VALUE), "(long) (-1L & Long.MAX_VALUE)");
    Tester.checkEqual(longAnd(0L, Long.MIN_VALUE), (long) (0L & Long.MIN_VALUE), "(long) (0L & Long.MIN_VALUE)");
    Tester.checkEqual(longAnd(0L, -1L), (long) (0L & -1L), "(long) (0L & -1L)");
    Tester.checkEqual(longAnd(0L, 0L), (long) (0L & 0L), "(long) (0L & 0L)");
    Tester.checkEqual(longAnd(0L, 1L), (long) (0L & 1L), "(long) (0L & 1L)");
    Tester.checkEqual(longAnd(0L, Long.MAX_VALUE), (long) (0L & Long.MAX_VALUE), "(long) (0L & Long.MAX_VALUE)");
    Tester.checkEqual(longAnd(1L, Long.MIN_VALUE), (long) (1L & Long.MIN_VALUE), "(long) (1L & Long.MIN_VALUE)");
    Tester.checkEqual(longAnd(1L, -1L), (long) (1L & -1L), "(long) (1L & -1L)");
    Tester.checkEqual(longAnd(1L, 0L), (long) (1L & 0L), "(long) (1L & 0L)");
    Tester.checkEqual(longAnd(1L, 1L), (long) (1L & 1L), "(long) (1L & 1L)");
    Tester.checkEqual(longAnd(1L, Long.MAX_VALUE), (long) (1L & Long.MAX_VALUE), "(long) (1L & Long.MAX_VALUE)");
    Tester.checkEqual(longAnd(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE & Long.MIN_VALUE), "(long) (Long.MAX_VALUE & Long.MIN_VALUE)");
    Tester.checkEqual(longAnd(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE & -1L), "(long) (Long.MAX_VALUE & -1L)");
    Tester.checkEqual(longAnd(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE & 0L), "(long) (Long.MAX_VALUE & 0L)");
    Tester.checkEqual(longAnd(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE & 1L), "(long) (Long.MAX_VALUE & 1L)");
    Tester.checkEqual(longAnd(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE & Long.MAX_VALUE), "(long) (Long.MAX_VALUE & Long.MAX_VALUE)");
    Tester.checkEqual(longXor(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE ^ Long.MIN_VALUE), "(long) (Long.MIN_VALUE ^ Long.MIN_VALUE)");
    Tester.checkEqual(longXor(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE ^ -1L), "(long) (Long.MIN_VALUE ^ -1L)");
    Tester.checkEqual(longXor(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE ^ 0L), "(long) (Long.MIN_VALUE ^ 0L)");
    Tester.checkEqual(longXor(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE ^ 1L), "(long) (Long.MIN_VALUE ^ 1L)");
    Tester.checkEqual(longXor(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE ^ Long.MAX_VALUE), "(long) (Long.MIN_VALUE ^ Long.MAX_VALUE)");
    Tester.checkEqual(longXor(-1L, Long.MIN_VALUE), (long) (-1L ^ Long.MIN_VALUE), "(long) (-1L ^ Long.MIN_VALUE)");
    Tester.checkEqual(longXor(-1L, -1L), (long) (-1L ^ -1L), "(long) (-1L ^ -1L)");
    Tester.checkEqual(longXor(-1L, 0L), (long) (-1L ^ 0L), "(long) (-1L ^ 0L)");
    Tester.checkEqual(longXor(-1L, 1L), (long) (-1L ^ 1L), "(long) (-1L ^ 1L)");
    Tester.checkEqual(longXor(-1L, Long.MAX_VALUE), (long) (-1L ^ Long.MAX_VALUE), "(long) (-1L ^ Long.MAX_VALUE)");
    Tester.checkEqual(longXor(0L, Long.MIN_VALUE), (long) (0L ^ Long.MIN_VALUE), "(long) (0L ^ Long.MIN_VALUE)");
    Tester.checkEqual(longXor(0L, -1L), (long) (0L ^ -1L), "(long) (0L ^ -1L)");
    Tester.checkEqual(longXor(0L, 0L), (long) (0L ^ 0L), "(long) (0L ^ 0L)");
    Tester.checkEqual(longXor(0L, 1L), (long) (0L ^ 1L), "(long) (0L ^ 1L)");
    Tester.checkEqual(longXor(0L, Long.MAX_VALUE), (long) (0L ^ Long.MAX_VALUE), "(long) (0L ^ Long.MAX_VALUE)");
    Tester.checkEqual(longXor(1L, Long.MIN_VALUE), (long) (1L ^ Long.MIN_VALUE), "(long) (1L ^ Long.MIN_VALUE)");
    Tester.checkEqual(longXor(1L, -1L), (long) (1L ^ -1L), "(long) (1L ^ -1L)");
    Tester.checkEqual(longXor(1L, 0L), (long) (1L ^ 0L), "(long) (1L ^ 0L)");
    Tester.checkEqual(longXor(1L, 1L), (long) (1L ^ 1L), "(long) (1L ^ 1L)");
    Tester.checkEqual(longXor(1L, Long.MAX_VALUE), (long) (1L ^ Long.MAX_VALUE), "(long) (1L ^ Long.MAX_VALUE)");
    Tester.checkEqual(longXor(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE ^ Long.MIN_VALUE), "(long) (Long.MAX_VALUE ^ Long.MIN_VALUE)");
    Tester.checkEqual(longXor(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE ^ -1L), "(long) (Long.MAX_VALUE ^ -1L)");
    Tester.checkEqual(longXor(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE ^ 0L), "(long) (Long.MAX_VALUE ^ 0L)");
    Tester.checkEqual(longXor(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE ^ 1L), "(long) (Long.MAX_VALUE ^ 1L)");
    Tester.checkEqual(longXor(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE ^ Long.MAX_VALUE), "(long) (Long.MAX_VALUE ^ Long.MAX_VALUE)");
    Tester.checkEqual(longOr(Long.MIN_VALUE, Long.MIN_VALUE), (long) (Long.MIN_VALUE | Long.MIN_VALUE), "(long) (Long.MIN_VALUE | Long.MIN_VALUE)");
    Tester.checkEqual(longOr(Long.MIN_VALUE, -1L), (long) (Long.MIN_VALUE | -1L), "(long) (Long.MIN_VALUE | -1L)");
    Tester.checkEqual(longOr(Long.MIN_VALUE, 0L), (long) (Long.MIN_VALUE | 0L), "(long) (Long.MIN_VALUE | 0L)");
    Tester.checkEqual(longOr(Long.MIN_VALUE, 1L), (long) (Long.MIN_VALUE | 1L), "(long) (Long.MIN_VALUE | 1L)");
    Tester.checkEqual(longOr(Long.MIN_VALUE, Long.MAX_VALUE), (long) (Long.MIN_VALUE | Long.MAX_VALUE), "(long) (Long.MIN_VALUE | Long.MAX_VALUE)");
    Tester.checkEqual(longOr(-1L, Long.MIN_VALUE), (long) (-1L | Long.MIN_VALUE), "(long) (-1L | Long.MIN_VALUE)");
    Tester.checkEqual(longOr(-1L, -1L), (long) (-1L | -1L), "(long) (-1L | -1L)");
    Tester.checkEqual(longOr(-1L, 0L), (long) (-1L | 0L), "(long) (-1L | 0L)");
    Tester.checkEqual(longOr(-1L, 1L), (long) (-1L | 1L), "(long) (-1L | 1L)");
    Tester.checkEqual(longOr(-1L, Long.MAX_VALUE), (long) (-1L | Long.MAX_VALUE), "(long) (-1L | Long.MAX_VALUE)");
    Tester.checkEqual(longOr(0L, Long.MIN_VALUE), (long) (0L | Long.MIN_VALUE), "(long) (0L | Long.MIN_VALUE)");
    Tester.checkEqual(longOr(0L, -1L), (long) (0L | -1L), "(long) (0L | -1L)");
    Tester.checkEqual(longOr(0L, 0L), (long) (0L | 0L), "(long) (0L | 0L)");
    Tester.checkEqual(longOr(0L, 1L), (long) (0L | 1L), "(long) (0L | 1L)");
    Tester.checkEqual(longOr(0L, Long.MAX_VALUE), (long) (0L | Long.MAX_VALUE), "(long) (0L | Long.MAX_VALUE)");
    Tester.checkEqual(longOr(1L, Long.MIN_VALUE), (long) (1L | Long.MIN_VALUE), "(long) (1L | Long.MIN_VALUE)");
    Tester.checkEqual(longOr(1L, -1L), (long) (1L | -1L), "(long) (1L | -1L)");
    Tester.checkEqual(longOr(1L, 0L), (long) (1L | 0L), "(long) (1L | 0L)");
    Tester.checkEqual(longOr(1L, 1L), (long) (1L | 1L), "(long) (1L | 1L)");
    Tester.checkEqual(longOr(1L, Long.MAX_VALUE), (long) (1L | Long.MAX_VALUE), "(long) (1L | Long.MAX_VALUE)");
    Tester.checkEqual(longOr(Long.MAX_VALUE, Long.MIN_VALUE), (long) (Long.MAX_VALUE | Long.MIN_VALUE), "(long) (Long.MAX_VALUE | Long.MIN_VALUE)");
    Tester.checkEqual(longOr(Long.MAX_VALUE, -1L), (long) (Long.MAX_VALUE | -1L), "(long) (Long.MAX_VALUE | -1L)");
    Tester.checkEqual(longOr(Long.MAX_VALUE, 0L), (long) (Long.MAX_VALUE | 0L), "(long) (Long.MAX_VALUE | 0L)");
    Tester.checkEqual(longOr(Long.MAX_VALUE, 1L), (long) (Long.MAX_VALUE | 1L), "(long) (Long.MAX_VALUE | 1L)");
    Tester.checkEqual(longOr(Long.MAX_VALUE, Long.MAX_VALUE), (long) (Long.MAX_VALUE | Long.MAX_VALUE), "(long) (Long.MAX_VALUE | Long.MAX_VALUE)");
  }
  static void longSwitch() {
    switch(0) {
      case ((((long) + Long.MIN_VALUE) == 0) ? 0 : 0):
      case ((((long) + -1L) == 0) ? 1 : 1):
      case ((((long) + 0L) == 0) ? 2 : 2):
      case ((((long) + 1L) == 0) ? 3 : 3):
      case ((((long) + Long.MAX_VALUE) == 0) ? 4 : 4):
      case ((((long) - Long.MIN_VALUE) == 0) ? 5 : 5):
      case ((((long) - -1L) == 0) ? 6 : 6):
      case ((((long) - 0L) == 0) ? 7 : 7):
      case ((((long) - 1L) == 0) ? 8 : 8):
      case ((((long) - Long.MAX_VALUE) == 0) ? 9 : 9):
      case ((((long) ~ Long.MIN_VALUE) == 0) ? 10 : 10):
      case ((((long) ~ -1L) == 0) ? 11 : 11):
      case ((((long) ~ 0L) == 0) ? 12 : 12):
      case ((((long) ~ 1L) == 0) ? 13 : 13):
      case ((((long) ~ Long.MAX_VALUE) == 0) ? 14 : 14):
      case ((((long) (Long.MIN_VALUE * Long.MIN_VALUE)) == 0) ? 15 : 15):
      case ((((long) (Long.MIN_VALUE * -1L)) == 0) ? 16 : 16):
      case ((((long) (Long.MIN_VALUE * 0L)) == 0) ? 17 : 17):
      case ((((long) (Long.MIN_VALUE * 1L)) == 0) ? 18 : 18):
      case ((((long) (Long.MIN_VALUE * Long.MAX_VALUE)) == 0) ? 19 : 19):
      case ((((long) (-1L * Long.MIN_VALUE)) == 0) ? 20 : 20):
      case ((((long) (-1L * -1L)) == 0) ? 21 : 21):
      case ((((long) (-1L * 0L)) == 0) ? 22 : 22):
      case ((((long) (-1L * 1L)) == 0) ? 23 : 23):
      case ((((long) (-1L * Long.MAX_VALUE)) == 0) ? 24 : 24):
      case ((((long) (0L * Long.MIN_VALUE)) == 0) ? 25 : 25):
      case ((((long) (0L * -1L)) == 0) ? 26 : 26):
      case ((((long) (0L * 0L)) == 0) ? 27 : 27):
      case ((((long) (0L * 1L)) == 0) ? 28 : 28):
      case ((((long) (0L * Long.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((long) (1L * Long.MIN_VALUE)) == 0) ? 30 : 30):
      case ((((long) (1L * -1L)) == 0) ? 31 : 31):
      case ((((long) (1L * 0L)) == 0) ? 32 : 32):
      case ((((long) (1L * 1L)) == 0) ? 33 : 33):
      case ((((long) (1L * Long.MAX_VALUE)) == 0) ? 34 : 34):
      case ((((long) (Long.MAX_VALUE * Long.MIN_VALUE)) == 0) ? 35 : 35):
      case ((((long) (Long.MAX_VALUE * -1L)) == 0) ? 36 : 36):
      case ((((long) (Long.MAX_VALUE * 0L)) == 0) ? 37 : 37):
      case ((((long) (Long.MAX_VALUE * 1L)) == 0) ? 38 : 38):
      case ((((long) (Long.MAX_VALUE * Long.MAX_VALUE)) == 0) ? 39 : 39):
      case ((((long) (Long.MIN_VALUE / Long.MIN_VALUE)) == 0) ? 40 : 40):
      case ((((long) (Long.MIN_VALUE / -1L)) == 0) ? 41 : 41):
      case ((((long) (Long.MIN_VALUE / 1L)) == 0) ? 42 : 42):
      case ((((long) (Long.MIN_VALUE / Long.MAX_VALUE)) == 0) ? 43 : 43):
      case ((((long) (-1L / Long.MIN_VALUE)) == 0) ? 44 : 44):
      case ((((long) (-1L / -1L)) == 0) ? 45 : 45):
      case ((((long) (-1L / 1L)) == 0) ? 46 : 46):
      case ((((long) (-1L / Long.MAX_VALUE)) == 0) ? 47 : 47):
      case ((((long) (0L / Long.MIN_VALUE)) == 0) ? 48 : 48):
      case ((((long) (0L / -1L)) == 0) ? 49 : 49):
      case ((((long) (0L / 1L)) == 0) ? 50 : 50):
      case ((((long) (0L / Long.MAX_VALUE)) == 0) ? 51 : 51):
      case ((((long) (1L / Long.MIN_VALUE)) == 0) ? 52 : 52):
      case ((((long) (1L / -1L)) == 0) ? 53 : 53):
      case ((((long) (1L / 1L)) == 0) ? 54 : 54):
      case ((((long) (1L / Long.MAX_VALUE)) == 0) ? 55 : 55):
      case ((((long) (Long.MAX_VALUE / Long.MIN_VALUE)) == 0) ? 56 : 56):
      case ((((long) (Long.MAX_VALUE / -1L)) == 0) ? 57 : 57):
      case ((((long) (Long.MAX_VALUE / 1L)) == 0) ? 58 : 58):
      case ((((long) (Long.MAX_VALUE / Long.MAX_VALUE)) == 0) ? 59 : 59):
      case ((((long) (Long.MIN_VALUE % Long.MIN_VALUE)) == 0) ? 60 : 60):
      case ((((long) (Long.MIN_VALUE % -1L)) == 0) ? 61 : 61):
      case ((((long) (Long.MIN_VALUE % 1L)) == 0) ? 62 : 62):
      case ((((long) (Long.MIN_VALUE % Long.MAX_VALUE)) == 0) ? 63 : 63):
      case ((((long) (-1L % Long.MIN_VALUE)) == 0) ? 64 : 64):
      case ((((long) (-1L % -1L)) == 0) ? 65 : 65):
      case ((((long) (-1L % 1L)) == 0) ? 66 : 66):
      case ((((long) (-1L % Long.MAX_VALUE)) == 0) ? 67 : 67):
      case ((((long) (0L % Long.MIN_VALUE)) == 0) ? 68 : 68):
      case ((((long) (0L % -1L)) == 0) ? 69 : 69):
      case ((((long) (0L % 1L)) == 0) ? 70 : 70):
      case ((((long) (0L % Long.MAX_VALUE)) == 0) ? 71 : 71):
      case ((((long) (1L % Long.MIN_VALUE)) == 0) ? 72 : 72):
      case ((((long) (1L % -1L)) == 0) ? 73 : 73):
      case ((((long) (1L % 1L)) == 0) ? 74 : 74):
      case ((((long) (1L % Long.MAX_VALUE)) == 0) ? 75 : 75):
      case ((((long) (Long.MAX_VALUE % Long.MIN_VALUE)) == 0) ? 76 : 76):
      case ((((long) (Long.MAX_VALUE % -1L)) == 0) ? 77 : 77):
      case ((((long) (Long.MAX_VALUE % 1L)) == 0) ? 78 : 78):
      case ((((long) (Long.MAX_VALUE % Long.MAX_VALUE)) == 0) ? 79 : 79):
      case ((((long) (Long.MIN_VALUE + Long.MIN_VALUE)) == 0) ? 80 : 80):
      case ((((long) (Long.MIN_VALUE + -1L)) == 0) ? 81 : 81):
      case ((((long) (Long.MIN_VALUE + 0L)) == 0) ? 82 : 82):
      case ((((long) (Long.MIN_VALUE + 1L)) == 0) ? 83 : 83):
      case ((((long) (Long.MIN_VALUE + Long.MAX_VALUE)) == 0) ? 84 : 84):
      case ((((long) (-1L + Long.MIN_VALUE)) == 0) ? 85 : 85):
      case ((((long) (-1L + -1L)) == 0) ? 86 : 86):
      case ((((long) (-1L + 0L)) == 0) ? 87 : 87):
      case ((((long) (-1L + 1L)) == 0) ? 88 : 88):
      case ((((long) (-1L + Long.MAX_VALUE)) == 0) ? 89 : 89):
      case ((((long) (0L + Long.MIN_VALUE)) == 0) ? 90 : 90):
      case ((((long) (0L + -1L)) == 0) ? 91 : 91):
      case ((((long) (0L + 0L)) == 0) ? 92 : 92):
      case ((((long) (0L + 1L)) == 0) ? 93 : 93):
      case ((((long) (0L + Long.MAX_VALUE)) == 0) ? 94 : 94):
      case ((((long) (1L + Long.MIN_VALUE)) == 0) ? 95 : 95):
      case ((((long) (1L + -1L)) == 0) ? 96 : 96):
      case ((((long) (1L + 0L)) == 0) ? 97 : 97):
      case ((((long) (1L + 1L)) == 0) ? 98 : 98):
      case ((((long) (1L + Long.MAX_VALUE)) == 0) ? 99 : 99):
      case ((((long) (Long.MAX_VALUE + Long.MIN_VALUE)) == 0) ? 100 : 100):
      case ((((long) (Long.MAX_VALUE + -1L)) == 0) ? 101 : 101):
      case ((((long) (Long.MAX_VALUE + 0L)) == 0) ? 102 : 102):
      case ((((long) (Long.MAX_VALUE + 1L)) == 0) ? 103 : 103):
      case ((((long) (Long.MAX_VALUE + Long.MAX_VALUE)) == 0) ? 104 : 104):
      case ((((long) (Long.MIN_VALUE - Long.MIN_VALUE)) == 0) ? 105 : 105):
      case ((((long) (Long.MIN_VALUE - -1L)) == 0) ? 106 : 106):
      case ((((long) (Long.MIN_VALUE - 0L)) == 0) ? 107 : 107):
      case ((((long) (Long.MIN_VALUE - 1L)) == 0) ? 108 : 108):
      case ((((long) (Long.MIN_VALUE - Long.MAX_VALUE)) == 0) ? 109 : 109):
      case ((((long) (-1L - Long.MIN_VALUE)) == 0) ? 110 : 110):
      case ((((long) (-1L - -1L)) == 0) ? 111 : 111):
      case ((((long) (-1L - 0L)) == 0) ? 112 : 112):
      case ((((long) (-1L - 1L)) == 0) ? 113 : 113):
      case ((((long) (-1L - Long.MAX_VALUE)) == 0) ? 114 : 114):
      case ((((long) (0L - Long.MIN_VALUE)) == 0) ? 115 : 115):
      case ((((long) (0L - -1L)) == 0) ? 116 : 116):
      case ((((long) (0L - 0L)) == 0) ? 117 : 117):
      case ((((long) (0L - 1L)) == 0) ? 118 : 118):
      case ((((long) (0L - Long.MAX_VALUE)) == 0) ? 119 : 119):
      case ((((long) (1L - Long.MIN_VALUE)) == 0) ? 120 : 120):
      case ((((long) (1L - -1L)) == 0) ? 121 : 121):
      case ((((long) (1L - 0L)) == 0) ? 122 : 122):
      case ((((long) (1L - 1L)) == 0) ? 123 : 123):
      case ((((long) (1L - Long.MAX_VALUE)) == 0) ? 124 : 124):
      case ((((long) (Long.MAX_VALUE - Long.MIN_VALUE)) == 0) ? 125 : 125):
      case ((((long) (Long.MAX_VALUE - -1L)) == 0) ? 126 : 126):
      case ((((long) (Long.MAX_VALUE - 0L)) == 0) ? 127 : 127):
      case ((((long) (Long.MAX_VALUE - 1L)) == 0) ? 128 : 128):
      case ((((long) (Long.MAX_VALUE - Long.MAX_VALUE)) == 0) ? 129 : 129):
      case ((((long) (Long.MIN_VALUE << Long.MIN_VALUE)) == 0) ? 130 : 130):
      case ((((long) (Long.MIN_VALUE << -1L)) == 0) ? 131 : 131):
      case ((((long) (Long.MIN_VALUE << 0L)) == 0) ? 132 : 132):
      case ((((long) (Long.MIN_VALUE << 1L)) == 0) ? 133 : 133):
      case ((((long) (Long.MIN_VALUE << Long.MAX_VALUE)) == 0) ? 134 : 134):
      case ((((long) (-1L << Long.MIN_VALUE)) == 0) ? 135 : 135):
      case ((((long) (-1L << -1L)) == 0) ? 136 : 136):
      case ((((long) (-1L << 0L)) == 0) ? 137 : 137):
      case ((((long) (-1L << 1L)) == 0) ? 138 : 138):
      case ((((long) (-1L << Long.MAX_VALUE)) == 0) ? 139 : 139):
      case ((((long) (0L << Long.MIN_VALUE)) == 0) ? 140 : 140):
      case ((((long) (0L << -1L)) == 0) ? 141 : 141):
      case ((((long) (0L << 0L)) == 0) ? 142 : 142):
      case ((((long) (0L << 1L)) == 0) ? 143 : 143):
      case ((((long) (0L << Long.MAX_VALUE)) == 0) ? 144 : 144):
      case ((((long) (1L << Long.MIN_VALUE)) == 0) ? 145 : 145):
      case ((((long) (1L << -1L)) == 0) ? 146 : 146):
      case ((((long) (1L << 0L)) == 0) ? 147 : 147):
      case ((((long) (1L << 1L)) == 0) ? 148 : 148):
      case ((((long) (1L << Long.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((long) (Long.MAX_VALUE << Long.MIN_VALUE)) == 0) ? 150 : 150):
      case ((((long) (Long.MAX_VALUE << -1L)) == 0) ? 151 : 151):
      case ((((long) (Long.MAX_VALUE << 0L)) == 0) ? 152 : 152):
      case ((((long) (Long.MAX_VALUE << 1L)) == 0) ? 153 : 153):
      case ((((long) (Long.MAX_VALUE << Long.MAX_VALUE)) == 0) ? 154 : 154):
      case ((((long) (Long.MIN_VALUE >> Long.MIN_VALUE)) == 0) ? 155 : 155):
      case ((((long) (Long.MIN_VALUE >> -1L)) == 0) ? 156 : 156):
      case ((((long) (Long.MIN_VALUE >> 0L)) == 0) ? 157 : 157):
      case ((((long) (Long.MIN_VALUE >> 1L)) == 0) ? 158 : 158):
      case ((((long) (Long.MIN_VALUE >> Long.MAX_VALUE)) == 0) ? 159 : 159):
      case ((((long) (-1L >> Long.MIN_VALUE)) == 0) ? 160 : 160):
      case ((((long) (-1L >> -1L)) == 0) ? 161 : 161):
      case ((((long) (-1L >> 0L)) == 0) ? 162 : 162):
      case ((((long) (-1L >> 1L)) == 0) ? 163 : 163):
      case ((((long) (-1L >> Long.MAX_VALUE)) == 0) ? 164 : 164):
      case ((((long) (0L >> Long.MIN_VALUE)) == 0) ? 165 : 165):
      case ((((long) (0L >> -1L)) == 0) ? 166 : 166):
      case ((((long) (0L >> 0L)) == 0) ? 167 : 167):
      case ((((long) (0L >> 1L)) == 0) ? 168 : 168):
      case ((((long) (0L >> Long.MAX_VALUE)) == 0) ? 169 : 169):
      case ((((long) (1L >> Long.MIN_VALUE)) == 0) ? 170 : 170):
      case ((((long) (1L >> -1L)) == 0) ? 171 : 171):
      case ((((long) (1L >> 0L)) == 0) ? 172 : 172):
      case ((((long) (1L >> 1L)) == 0) ? 173 : 173):
      case ((((long) (1L >> Long.MAX_VALUE)) == 0) ? 174 : 174):
      case ((((long) (Long.MAX_VALUE >> Long.MIN_VALUE)) == 0) ? 175 : 175):
      case ((((long) (Long.MAX_VALUE >> -1L)) == 0) ? 176 : 176):
      case ((((long) (Long.MAX_VALUE >> 0L)) == 0) ? 177 : 177):
      case ((((long) (Long.MAX_VALUE >> 1L)) == 0) ? 178 : 178):
      case ((((long) (Long.MAX_VALUE >> Long.MAX_VALUE)) == 0) ? 179 : 179):
      case ((((long) (Long.MIN_VALUE >>> Long.MIN_VALUE)) == 0) ? 180 : 180):
      case ((((long) (Long.MIN_VALUE >>> -1L)) == 0) ? 181 : 181):
      case ((((long) (Long.MIN_VALUE >>> 0L)) == 0) ? 182 : 182):
      case ((((long) (Long.MIN_VALUE >>> 1L)) == 0) ? 183 : 183):
      case ((((long) (Long.MIN_VALUE >>> Long.MAX_VALUE)) == 0) ? 184 : 184):
      case ((((long) (-1L >>> Long.MIN_VALUE)) == 0) ? 185 : 185):
      case ((((long) (-1L >>> -1L)) == 0) ? 186 : 186):
      case ((((long) (-1L >>> 0L)) == 0) ? 187 : 187):
      case ((((long) (-1L >>> 1L)) == 0) ? 188 : 188):
      case ((((long) (-1L >>> Long.MAX_VALUE)) == 0) ? 189 : 189):
      case ((((long) (0L >>> Long.MIN_VALUE)) == 0) ? 190 : 190):
      case ((((long) (0L >>> -1L)) == 0) ? 191 : 191):
      case ((((long) (0L >>> 0L)) == 0) ? 192 : 192):
      case ((((long) (0L >>> 1L)) == 0) ? 193 : 193):
      case ((((long) (0L >>> Long.MAX_VALUE)) == 0) ? 194 : 194):
      case ((((long) (1L >>> Long.MIN_VALUE)) == 0) ? 195 : 195):
      case ((((long) (1L >>> -1L)) == 0) ? 196 : 196):
      case ((((long) (1L >>> 0L)) == 0) ? 197 : 197):
      case ((((long) (1L >>> 1L)) == 0) ? 198 : 198):
      case ((((long) (1L >>> Long.MAX_VALUE)) == 0) ? 199 : 199):
      case ((((long) (Long.MAX_VALUE >>> Long.MIN_VALUE)) == 0) ? 200 : 200):
      case ((((long) (Long.MAX_VALUE >>> -1L)) == 0) ? 201 : 201):
      case ((((long) (Long.MAX_VALUE >>> 0L)) == 0) ? 202 : 202):
      case ((((long) (Long.MAX_VALUE >>> 1L)) == 0) ? 203 : 203):
      case ((((long) (Long.MAX_VALUE >>> Long.MAX_VALUE)) == 0) ? 204 : 204):
      case ((Long.MIN_VALUE < Long.MIN_VALUE) ? 205 : 205):
      case ((Long.MIN_VALUE < -1L) ? 206 : 206):
      case ((Long.MIN_VALUE < 0L) ? 207 : 207):
      case ((Long.MIN_VALUE < 1L) ? 208 : 208):
      case ((Long.MIN_VALUE < Long.MAX_VALUE) ? 209 : 209):
      case ((-1L < Long.MIN_VALUE) ? 210 : 210):
      case ((-1L < -1L) ? 211 : 211):
      case ((-1L < 0L) ? 212 : 212):
      case ((-1L < 1L) ? 213 : 213):
      case ((-1L < Long.MAX_VALUE) ? 214 : 214):
      case ((0L < Long.MIN_VALUE) ? 215 : 215):
      case ((0L < -1L) ? 216 : 216):
      case ((0L < 0L) ? 217 : 217):
      case ((0L < 1L) ? 218 : 218):
      case ((0L < Long.MAX_VALUE) ? 219 : 219):
      case ((1L < Long.MIN_VALUE) ? 220 : 220):
      case ((1L < -1L) ? 221 : 221):
      case ((1L < 0L) ? 222 : 222):
      case ((1L < 1L) ? 223 : 223):
      case ((1L < Long.MAX_VALUE) ? 224 : 224):
      case ((Long.MAX_VALUE < Long.MIN_VALUE) ? 225 : 225):
      case ((Long.MAX_VALUE < -1L) ? 226 : 226):
      case ((Long.MAX_VALUE < 0L) ? 227 : 227):
      case ((Long.MAX_VALUE < 1L) ? 228 : 228):
      case ((Long.MAX_VALUE < Long.MAX_VALUE) ? 229 : 229):
      case ((Long.MIN_VALUE > Long.MIN_VALUE) ? 230 : 230):
      case ((Long.MIN_VALUE > -1L) ? 231 : 231):
      case ((Long.MIN_VALUE > 0L) ? 232 : 232):
      case ((Long.MIN_VALUE > 1L) ? 233 : 233):
      case ((Long.MIN_VALUE > Long.MAX_VALUE) ? 234 : 234):
      case ((-1L > Long.MIN_VALUE) ? 235 : 235):
      case ((-1L > -1L) ? 236 : 236):
      case ((-1L > 0L) ? 237 : 237):
      case ((-1L > 1L) ? 238 : 238):
      case ((-1L > Long.MAX_VALUE) ? 239 : 239):
      case ((0L > Long.MIN_VALUE) ? 240 : 240):
      case ((0L > -1L) ? 241 : 241):
      case ((0L > 0L) ? 242 : 242):
      case ((0L > 1L) ? 243 : 243):
      case ((0L > Long.MAX_VALUE) ? 244 : 244):
      case ((1L > Long.MIN_VALUE) ? 245 : 245):
      case ((1L > -1L) ? 246 : 246):
      case ((1L > 0L) ? 247 : 247):
      case ((1L > 1L) ? 248 : 248):
      case ((1L > Long.MAX_VALUE) ? 249 : 249):
      case ((Long.MAX_VALUE > Long.MIN_VALUE) ? 250 : 250):
      case ((Long.MAX_VALUE > -1L) ? 251 : 251):
      case ((Long.MAX_VALUE > 0L) ? 252 : 252):
      case ((Long.MAX_VALUE > 1L) ? 253 : 253):
      case ((Long.MAX_VALUE > Long.MAX_VALUE) ? 254 : 254):
      case ((Long.MIN_VALUE <= Long.MIN_VALUE) ? 255 : 255):
      case ((Long.MIN_VALUE <= -1L) ? 256 : 256):
      case ((Long.MIN_VALUE <= 0L) ? 257 : 257):
      case ((Long.MIN_VALUE <= 1L) ? 258 : 258):
      case ((Long.MIN_VALUE <= Long.MAX_VALUE) ? 259 : 259):
      case ((-1L <= Long.MIN_VALUE) ? 260 : 260):
      case ((-1L <= -1L) ? 261 : 261):
      case ((-1L <= 0L) ? 262 : 262):
      case ((-1L <= 1L) ? 263 : 263):
      case ((-1L <= Long.MAX_VALUE) ? 264 : 264):
      case ((0L <= Long.MIN_VALUE) ? 265 : 265):
      case ((0L <= -1L) ? 266 : 266):
      case ((0L <= 0L) ? 267 : 267):
      case ((0L <= 1L) ? 268 : 268):
      case ((0L <= Long.MAX_VALUE) ? 269 : 269):
      case ((1L <= Long.MIN_VALUE) ? 270 : 270):
      case ((1L <= -1L) ? 271 : 271):
      case ((1L <= 0L) ? 272 : 272):
      case ((1L <= 1L) ? 273 : 273):
      case ((1L <= Long.MAX_VALUE) ? 274 : 274):
      case ((Long.MAX_VALUE <= Long.MIN_VALUE) ? 275 : 275):
      case ((Long.MAX_VALUE <= -1L) ? 276 : 276):
      case ((Long.MAX_VALUE <= 0L) ? 277 : 277):
      case ((Long.MAX_VALUE <= 1L) ? 278 : 278):
      case ((Long.MAX_VALUE <= Long.MAX_VALUE) ? 279 : 279):
      case ((Long.MIN_VALUE >= Long.MIN_VALUE) ? 280 : 280):
      case ((Long.MIN_VALUE >= -1L) ? 281 : 281):
      case ((Long.MIN_VALUE >= 0L) ? 282 : 282):
      case ((Long.MIN_VALUE >= 1L) ? 283 : 283):
      case ((Long.MIN_VALUE >= Long.MAX_VALUE) ? 284 : 284):
      case ((-1L >= Long.MIN_VALUE) ? 285 : 285):
      case ((-1L >= -1L) ? 286 : 286):
      case ((-1L >= 0L) ? 287 : 287):
      case ((-1L >= 1L) ? 288 : 288):
      case ((-1L >= Long.MAX_VALUE) ? 289 : 289):
      case ((0L >= Long.MIN_VALUE) ? 290 : 290):
      case ((0L >= -1L) ? 291 : 291):
      case ((0L >= 0L) ? 292 : 292):
      case ((0L >= 1L) ? 293 : 293):
      case ((0L >= Long.MAX_VALUE) ? 294 : 294):
      case ((1L >= Long.MIN_VALUE) ? 295 : 295):
      case ((1L >= -1L) ? 296 : 296):
      case ((1L >= 0L) ? 297 : 297):
      case ((1L >= 1L) ? 298 : 298):
      case ((1L >= Long.MAX_VALUE) ? 299 : 299):
      case ((Long.MAX_VALUE >= Long.MIN_VALUE) ? 300 : 300):
      case ((Long.MAX_VALUE >= -1L) ? 301 : 301):
      case ((Long.MAX_VALUE >= 0L) ? 302 : 302):
      case ((Long.MAX_VALUE >= 1L) ? 303 : 303):
      case ((Long.MAX_VALUE >= Long.MAX_VALUE) ? 304 : 304):
      case ((Long.MIN_VALUE == Long.MIN_VALUE) ? 305 : 305):
      case ((Long.MIN_VALUE == -1L) ? 306 : 306):
      case ((Long.MIN_VALUE == 0L) ? 307 : 307):
      case ((Long.MIN_VALUE == 1L) ? 308 : 308):
      case ((Long.MIN_VALUE == Long.MAX_VALUE) ? 309 : 309):
      case ((-1L == Long.MIN_VALUE) ? 310 : 310):
      case ((-1L == -1L) ? 311 : 311):
      case ((-1L == 0L) ? 312 : 312):
      case ((-1L == 1L) ? 313 : 313):
      case ((-1L == Long.MAX_VALUE) ? 314 : 314):
      case ((0L == Long.MIN_VALUE) ? 315 : 315):
      case ((0L == -1L) ? 316 : 316):
      case ((0L == 0L) ? 317 : 317):
      case ((0L == 1L) ? 318 : 318):
      case ((0L == Long.MAX_VALUE) ? 319 : 319):
      case ((1L == Long.MIN_VALUE) ? 320 : 320):
      case ((1L == -1L) ? 321 : 321):
      case ((1L == 0L) ? 322 : 322):
      case ((1L == 1L) ? 323 : 323):
      case ((1L == Long.MAX_VALUE) ? 324 : 324):
      case ((Long.MAX_VALUE == Long.MIN_VALUE) ? 325 : 325):
      case ((Long.MAX_VALUE == -1L) ? 326 : 326):
      case ((Long.MAX_VALUE == 0L) ? 327 : 327):
      case ((Long.MAX_VALUE == 1L) ? 328 : 328):
      case ((Long.MAX_VALUE == Long.MAX_VALUE) ? 329 : 329):
      case ((Long.MIN_VALUE != Long.MIN_VALUE) ? 330 : 330):
      case ((Long.MIN_VALUE != -1L) ? 331 : 331):
      case ((Long.MIN_VALUE != 0L) ? 332 : 332):
      case ((Long.MIN_VALUE != 1L) ? 333 : 333):
      case ((Long.MIN_VALUE != Long.MAX_VALUE) ? 334 : 334):
      case ((-1L != Long.MIN_VALUE) ? 335 : 335):
      case ((-1L != -1L) ? 336 : 336):
      case ((-1L != 0L) ? 337 : 337):
      case ((-1L != 1L) ? 338 : 338):
      case ((-1L != Long.MAX_VALUE) ? 339 : 339):
      case ((0L != Long.MIN_VALUE) ? 340 : 340):
      case ((0L != -1L) ? 341 : 341):
      case ((0L != 0L) ? 342 : 342):
      case ((0L != 1L) ? 343 : 343):
      case ((0L != Long.MAX_VALUE) ? 344 : 344):
      case ((1L != Long.MIN_VALUE) ? 345 : 345):
      case ((1L != -1L) ? 346 : 346):
      case ((1L != 0L) ? 347 : 347):
      case ((1L != 1L) ? 348 : 348):
      case ((1L != Long.MAX_VALUE) ? 349 : 349):
      case ((Long.MAX_VALUE != Long.MIN_VALUE) ? 350 : 350):
      case ((Long.MAX_VALUE != -1L) ? 351 : 351):
      case ((Long.MAX_VALUE != 0L) ? 352 : 352):
      case ((Long.MAX_VALUE != 1L) ? 353 : 353):
      case ((Long.MAX_VALUE != Long.MAX_VALUE) ? 354 : 354):
      case ((((long) (Long.MIN_VALUE & Long.MIN_VALUE)) == 0) ? 355 : 355):
      case ((((long) (Long.MIN_VALUE & -1L)) == 0) ? 356 : 356):
      case ((((long) (Long.MIN_VALUE & 0L)) == 0) ? 357 : 357):
      case ((((long) (Long.MIN_VALUE & 1L)) == 0) ? 358 : 358):
      case ((((long) (Long.MIN_VALUE & Long.MAX_VALUE)) == 0) ? 359 : 359):
      case ((((long) (-1L & Long.MIN_VALUE)) == 0) ? 360 : 360):
      case ((((long) (-1L & -1L)) == 0) ? 361 : 361):
      case ((((long) (-1L & 0L)) == 0) ? 362 : 362):
      case ((((long) (-1L & 1L)) == 0) ? 363 : 363):
      case ((((long) (-1L & Long.MAX_VALUE)) == 0) ? 364 : 364):
      case ((((long) (0L & Long.MIN_VALUE)) == 0) ? 365 : 365):
      case ((((long) (0L & -1L)) == 0) ? 366 : 366):
      case ((((long) (0L & 0L)) == 0) ? 367 : 367):
      case ((((long) (0L & 1L)) == 0) ? 368 : 368):
      case ((((long) (0L & Long.MAX_VALUE)) == 0) ? 369 : 369):
      case ((((long) (1L & Long.MIN_VALUE)) == 0) ? 370 : 370):
      case ((((long) (1L & -1L)) == 0) ? 371 : 371):
      case ((((long) (1L & 0L)) == 0) ? 372 : 372):
      case ((((long) (1L & 1L)) == 0) ? 373 : 373):
      case ((((long) (1L & Long.MAX_VALUE)) == 0) ? 374 : 374):
      case ((((long) (Long.MAX_VALUE & Long.MIN_VALUE)) == 0) ? 375 : 375):
      case ((((long) (Long.MAX_VALUE & -1L)) == 0) ? 376 : 376):
      case ((((long) (Long.MAX_VALUE & 0L)) == 0) ? 377 : 377):
      case ((((long) (Long.MAX_VALUE & 1L)) == 0) ? 378 : 378):
      case ((((long) (Long.MAX_VALUE & Long.MAX_VALUE)) == 0) ? 379 : 379):
      case ((((long) (Long.MIN_VALUE ^ Long.MIN_VALUE)) == 0) ? 380 : 380):
      case ((((long) (Long.MIN_VALUE ^ -1L)) == 0) ? 381 : 381):
      case ((((long) (Long.MIN_VALUE ^ 0L)) == 0) ? 382 : 382):
      case ((((long) (Long.MIN_VALUE ^ 1L)) == 0) ? 383 : 383):
      case ((((long) (Long.MIN_VALUE ^ Long.MAX_VALUE)) == 0) ? 384 : 384):
      case ((((long) (-1L ^ Long.MIN_VALUE)) == 0) ? 385 : 385):
      case ((((long) (-1L ^ -1L)) == 0) ? 386 : 386):
      case ((((long) (-1L ^ 0L)) == 0) ? 387 : 387):
      case ((((long) (-1L ^ 1L)) == 0) ? 388 : 388):
      case ((((long) (-1L ^ Long.MAX_VALUE)) == 0) ? 389 : 389):
      case ((((long) (0L ^ Long.MIN_VALUE)) == 0) ? 390 : 390):
      case ((((long) (0L ^ -1L)) == 0) ? 391 : 391):
      case ((((long) (0L ^ 0L)) == 0) ? 392 : 392):
      case ((((long) (0L ^ 1L)) == 0) ? 393 : 393):
      case ((((long) (0L ^ Long.MAX_VALUE)) == 0) ? 394 : 394):
      case ((((long) (1L ^ Long.MIN_VALUE)) == 0) ? 395 : 395):
      case ((((long) (1L ^ -1L)) == 0) ? 396 : 396):
      case ((((long) (1L ^ 0L)) == 0) ? 397 : 397):
      case ((((long) (1L ^ 1L)) == 0) ? 398 : 398):
      case ((((long) (1L ^ Long.MAX_VALUE)) == 0) ? 399 : 399):
      case ((((long) (Long.MAX_VALUE ^ Long.MIN_VALUE)) == 0) ? 400 : 400):
      case ((((long) (Long.MAX_VALUE ^ -1L)) == 0) ? 401 : 401):
      case ((((long) (Long.MAX_VALUE ^ 0L)) == 0) ? 402 : 402):
      case ((((long) (Long.MAX_VALUE ^ 1L)) == 0) ? 403 : 403):
      case ((((long) (Long.MAX_VALUE ^ Long.MAX_VALUE)) == 0) ? 404 : 404):
      case ((((long) (Long.MIN_VALUE | Long.MIN_VALUE)) == 0) ? 405 : 405):
      case ((((long) (Long.MIN_VALUE | -1L)) == 0) ? 406 : 406):
      case ((((long) (Long.MIN_VALUE | 0L)) == 0) ? 407 : 407):
      case ((((long) (Long.MIN_VALUE | 1L)) == 0) ? 408 : 408):
      case ((((long) (Long.MIN_VALUE | Long.MAX_VALUE)) == 0) ? 409 : 409):
      case ((((long) (-1L | Long.MIN_VALUE)) == 0) ? 410 : 410):
      case ((((long) (-1L | -1L)) == 0) ? 411 : 411):
      case ((((long) (-1L | 0L)) == 0) ? 412 : 412):
      case ((((long) (-1L | 1L)) == 0) ? 413 : 413):
      case ((((long) (-1L | Long.MAX_VALUE)) == 0) ? 414 : 414):
      case ((((long) (0L | Long.MIN_VALUE)) == 0) ? 415 : 415):
      case ((((long) (0L | -1L)) == 0) ? 416 : 416):
      case ((((long) (0L | 0L)) == 0) ? 417 : 417):
      case ((((long) (0L | 1L)) == 0) ? 418 : 418):
      case ((((long) (0L | Long.MAX_VALUE)) == 0) ? 419 : 419):
      case ((((long) (1L | Long.MIN_VALUE)) == 0) ? 420 : 420):
      case ((((long) (1L | -1L)) == 0) ? 421 : 421):
      case ((((long) (1L | 0L)) == 0) ? 422 : 422):
      case ((((long) (1L | 1L)) == 0) ? 423 : 423):
      case ((((long) (1L | Long.MAX_VALUE)) == 0) ? 424 : 424):
      case ((((long) (Long.MAX_VALUE | Long.MIN_VALUE)) == 0) ? 425 : 425):
      case ((((long) (Long.MAX_VALUE | -1L)) == 0) ? 426 : 426):
      case ((((long) (Long.MAX_VALUE | 0L)) == 0) ? 427 : 427):
      case ((((long) (Long.MAX_VALUE | 1L)) == 0) ? 428 : 428):
      case ((((long) (Long.MAX_VALUE | Long.MAX_VALUE)) == 0) ? 429 : 429):
      default:
    }
  }

  // --------
  // float tests
  static float floatPlus(float x) { return (float) + x; }
  static float floatMinus(float x) { return (float) - x; }
  static float floatTimes(float x, float y) { return (float) (x * y); }
  static float floatDiv(float x, float y) { return (float) (x / y); }
  static float floatRem(float x, float y) { return (float) (x % y); }
  static float floatAdd(float x, float y) { return (float) (x + y); }
  static float floatSub(float x, float y) { return (float) (x - y); }
  static boolean floatLt(float x, float y) { return x < y; }
  static boolean floatGt(float x, float y) { return x > y; }
  static boolean floatLe(float x, float y) { return x <= y; }
  static boolean floatGe(float x, float y) { return x >= y; }
  static boolean floatEq(float x, float y) { return x == y; }
  static boolean floatNe(float x, float y) { return x != y; }
  static void floatTest() {
    Tester.checkEqual(floatPlus(Float.NEGATIVE_INFINITY), (float) + Float.NEGATIVE_INFINITY, "(float) + Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatPlus(-1.0f), (float) + -1.0f, "(float) + -1.0f");
    Tester.checkEqual(floatPlus(-0.0f), (float) + -0.0f, "(float) + -0.0f");
    Tester.checkEqual(floatPlus(0.0f), (float) + 0.0f, "(float) + 0.0f");
    Tester.checkEqual(floatPlus(Float.MIN_VALUE), (float) + Float.MIN_VALUE, "(float) + Float.MIN_VALUE");
    Tester.checkEqual(floatPlus(1.0f), (float) + 1.0f, "(float) + 1.0f");
    Tester.checkEqual(floatPlus(Float.MAX_VALUE), (float) + Float.MAX_VALUE, "(float) + Float.MAX_VALUE");
    Tester.checkEqual(floatPlus(Float.POSITIVE_INFINITY), (float) + Float.POSITIVE_INFINITY, "(float) + Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatPlus(Float.NaN), (float) + Float.NaN, "(float) + Float.NaN");
    Tester.checkEqual(floatMinus(Float.NEGATIVE_INFINITY), (float) - Float.NEGATIVE_INFINITY, "(float) - Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatMinus(-1.0f), (float) - -1.0f, "(float) - -1.0f");
    Tester.checkEqual(floatMinus(-0.0f), (float) - -0.0f, "(float) - -0.0f");
    Tester.checkEqual(floatMinus(0.0f), (float) - 0.0f, "(float) - 0.0f");
    Tester.checkEqual(floatMinus(Float.MIN_VALUE), (float) - Float.MIN_VALUE, "(float) - Float.MIN_VALUE");
    Tester.checkEqual(floatMinus(1.0f), (float) - 1.0f, "(float) - 1.0f");
    Tester.checkEqual(floatMinus(Float.MAX_VALUE), (float) - Float.MAX_VALUE, "(float) - Float.MAX_VALUE");
    Tester.checkEqual(floatMinus(Float.POSITIVE_INFINITY), (float) - Float.POSITIVE_INFINITY, "(float) - Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatMinus(Float.NaN), (float) - Float.NaN, "(float) - Float.NaN");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, -1.0f), (float) (Float.NEGATIVE_INFINITY * -1.0f), "(float) (Float.NEGATIVE_INFINITY * -1.0f)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, -0.0f), (float) (Float.NEGATIVE_INFINITY * -0.0f), "(float) (Float.NEGATIVE_INFINITY * -0.0f)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, 0.0f), (float) (Float.NEGATIVE_INFINITY * 0.0f), "(float) (Float.NEGATIVE_INFINITY * 0.0f)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), (float) (Float.NEGATIVE_INFINITY * Float.MIN_VALUE), "(float) (Float.NEGATIVE_INFINITY * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, 1.0f), (float) (Float.NEGATIVE_INFINITY * 1.0f), "(float) (Float.NEGATIVE_INFINITY * 1.0f)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), (float) (Float.NEGATIVE_INFINITY * Float.MAX_VALUE), "(float) (Float.NEGATIVE_INFINITY * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY * Float.POSITIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.NEGATIVE_INFINITY, Float.NaN), (float) (Float.NEGATIVE_INFINITY * Float.NaN), "(float) (Float.NEGATIVE_INFINITY * Float.NaN)");
    Tester.checkEqual(floatTimes(-1.0f, Float.NEGATIVE_INFINITY), (float) (-1.0f * Float.NEGATIVE_INFINITY), "(float) (-1.0f * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(-1.0f, -1.0f), (float) (-1.0f * -1.0f), "(float) (-1.0f * -1.0f)");
    Tester.checkEqual(floatTimes(-1.0f, -0.0f), (float) (-1.0f * -0.0f), "(float) (-1.0f * -0.0f)");
    Tester.checkEqual(floatTimes(-1.0f, 0.0f), (float) (-1.0f * 0.0f), "(float) (-1.0f * 0.0f)");
    Tester.checkEqual(floatTimes(-1.0f, Float.MIN_VALUE), (float) (-1.0f * Float.MIN_VALUE), "(float) (-1.0f * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(-1.0f, 1.0f), (float) (-1.0f * 1.0f), "(float) (-1.0f * 1.0f)");
    Tester.checkEqual(floatTimes(-1.0f, Float.MAX_VALUE), (float) (-1.0f * Float.MAX_VALUE), "(float) (-1.0f * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(-1.0f, Float.POSITIVE_INFINITY), (float) (-1.0f * Float.POSITIVE_INFINITY), "(float) (-1.0f * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(-1.0f, Float.NaN), (float) (-1.0f * Float.NaN), "(float) (-1.0f * Float.NaN)");
    Tester.checkEqual(floatTimes(-0.0f, Float.NEGATIVE_INFINITY), (float) (-0.0f * Float.NEGATIVE_INFINITY), "(float) (-0.0f * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(-0.0f, -1.0f), (float) (-0.0f * -1.0f), "(float) (-0.0f * -1.0f)");
    Tester.checkEqual(floatTimes(-0.0f, -0.0f), (float) (-0.0f * -0.0f), "(float) (-0.0f * -0.0f)");
    Tester.checkEqual(floatTimes(-0.0f, 0.0f), (float) (-0.0f * 0.0f), "(float) (-0.0f * 0.0f)");
    Tester.checkEqual(floatTimes(-0.0f, Float.MIN_VALUE), (float) (-0.0f * Float.MIN_VALUE), "(float) (-0.0f * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(-0.0f, 1.0f), (float) (-0.0f * 1.0f), "(float) (-0.0f * 1.0f)");
    Tester.checkEqual(floatTimes(-0.0f, Float.MAX_VALUE), (float) (-0.0f * Float.MAX_VALUE), "(float) (-0.0f * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(-0.0f, Float.POSITIVE_INFINITY), (float) (-0.0f * Float.POSITIVE_INFINITY), "(float) (-0.0f * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(-0.0f, Float.NaN), (float) (-0.0f * Float.NaN), "(float) (-0.0f * Float.NaN)");
    Tester.checkEqual(floatTimes(0.0f, Float.NEGATIVE_INFINITY), (float) (0.0f * Float.NEGATIVE_INFINITY), "(float) (0.0f * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(0.0f, -1.0f), (float) (0.0f * -1.0f), "(float) (0.0f * -1.0f)");
    Tester.checkEqual(floatTimes(0.0f, -0.0f), (float) (0.0f * -0.0f), "(float) (0.0f * -0.0f)");
    Tester.checkEqual(floatTimes(0.0f, 0.0f), (float) (0.0f * 0.0f), "(float) (0.0f * 0.0f)");
    Tester.checkEqual(floatTimes(0.0f, Float.MIN_VALUE), (float) (0.0f * Float.MIN_VALUE), "(float) (0.0f * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(0.0f, 1.0f), (float) (0.0f * 1.0f), "(float) (0.0f * 1.0f)");
    Tester.checkEqual(floatTimes(0.0f, Float.MAX_VALUE), (float) (0.0f * Float.MAX_VALUE), "(float) (0.0f * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(0.0f, Float.POSITIVE_INFINITY), (float) (0.0f * Float.POSITIVE_INFINITY), "(float) (0.0f * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(0.0f, Float.NaN), (float) (0.0f * Float.NaN), "(float) (0.0f * Float.NaN)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MIN_VALUE * Float.NEGATIVE_INFINITY), "(float) (Float.MIN_VALUE * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, -1.0f), (float) (Float.MIN_VALUE * -1.0f), "(float) (Float.MIN_VALUE * -1.0f)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, -0.0f), (float) (Float.MIN_VALUE * -0.0f), "(float) (Float.MIN_VALUE * -0.0f)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, 0.0f), (float) (Float.MIN_VALUE * 0.0f), "(float) (Float.MIN_VALUE * 0.0f)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, Float.MIN_VALUE), (float) (Float.MIN_VALUE * Float.MIN_VALUE), "(float) (Float.MIN_VALUE * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, 1.0f), (float) (Float.MIN_VALUE * 1.0f), "(float) (Float.MIN_VALUE * 1.0f)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, Float.MAX_VALUE), (float) (Float.MIN_VALUE * Float.MAX_VALUE), "(float) (Float.MIN_VALUE * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MIN_VALUE * Float.POSITIVE_INFINITY), "(float) (Float.MIN_VALUE * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.MIN_VALUE, Float.NaN), (float) (Float.MIN_VALUE * Float.NaN), "(float) (Float.MIN_VALUE * Float.NaN)");
    Tester.checkEqual(floatTimes(1.0f, Float.NEGATIVE_INFINITY), (float) (1.0f * Float.NEGATIVE_INFINITY), "(float) (1.0f * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(1.0f, -1.0f), (float) (1.0f * -1.0f), "(float) (1.0f * -1.0f)");
    Tester.checkEqual(floatTimes(1.0f, -0.0f), (float) (1.0f * -0.0f), "(float) (1.0f * -0.0f)");
    Tester.checkEqual(floatTimes(1.0f, 0.0f), (float) (1.0f * 0.0f), "(float) (1.0f * 0.0f)");
    Tester.checkEqual(floatTimes(1.0f, Float.MIN_VALUE), (float) (1.0f * Float.MIN_VALUE), "(float) (1.0f * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(1.0f, 1.0f), (float) (1.0f * 1.0f), "(float) (1.0f * 1.0f)");
    Tester.checkEqual(floatTimes(1.0f, Float.MAX_VALUE), (float) (1.0f * Float.MAX_VALUE), "(float) (1.0f * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(1.0f, Float.POSITIVE_INFINITY), (float) (1.0f * Float.POSITIVE_INFINITY), "(float) (1.0f * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(1.0f, Float.NaN), (float) (1.0f * Float.NaN), "(float) (1.0f * Float.NaN)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MAX_VALUE * Float.NEGATIVE_INFINITY), "(float) (Float.MAX_VALUE * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, -1.0f), (float) (Float.MAX_VALUE * -1.0f), "(float) (Float.MAX_VALUE * -1.0f)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, -0.0f), (float) (Float.MAX_VALUE * -0.0f), "(float) (Float.MAX_VALUE * -0.0f)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, 0.0f), (float) (Float.MAX_VALUE * 0.0f), "(float) (Float.MAX_VALUE * 0.0f)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, Float.MIN_VALUE), (float) (Float.MAX_VALUE * Float.MIN_VALUE), "(float) (Float.MAX_VALUE * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, 1.0f), (float) (Float.MAX_VALUE * 1.0f), "(float) (Float.MAX_VALUE * 1.0f)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, Float.MAX_VALUE), (float) (Float.MAX_VALUE * Float.MAX_VALUE), "(float) (Float.MAX_VALUE * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MAX_VALUE * Float.POSITIVE_INFINITY), "(float) (Float.MAX_VALUE * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.MAX_VALUE, Float.NaN), (float) (Float.MAX_VALUE * Float.NaN), "(float) (Float.MAX_VALUE * Float.NaN)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.POSITIVE_INFINITY * Float.NEGATIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, -1.0f), (float) (Float.POSITIVE_INFINITY * -1.0f), "(float) (Float.POSITIVE_INFINITY * -1.0f)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, -0.0f), (float) (Float.POSITIVE_INFINITY * -0.0f), "(float) (Float.POSITIVE_INFINITY * -0.0f)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, 0.0f), (float) (Float.POSITIVE_INFINITY * 0.0f), "(float) (Float.POSITIVE_INFINITY * 0.0f)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, Float.MIN_VALUE), (float) (Float.POSITIVE_INFINITY * Float.MIN_VALUE), "(float) (Float.POSITIVE_INFINITY * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, 1.0f), (float) (Float.POSITIVE_INFINITY * 1.0f), "(float) (Float.POSITIVE_INFINITY * 1.0f)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, Float.MAX_VALUE), (float) (Float.POSITIVE_INFINITY * Float.MAX_VALUE), "(float) (Float.POSITIVE_INFINITY * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.POSITIVE_INFINITY, Float.NaN), (float) (Float.POSITIVE_INFINITY * Float.NaN), "(float) (Float.POSITIVE_INFINITY * Float.NaN)");
    Tester.checkEqual(floatTimes(Float.NaN, Float.NEGATIVE_INFINITY), (float) (Float.NaN * Float.NEGATIVE_INFINITY), "(float) (Float.NaN * Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.NaN, -1.0f), (float) (Float.NaN * -1.0f), "(float) (Float.NaN * -1.0f)");
    Tester.checkEqual(floatTimes(Float.NaN, -0.0f), (float) (Float.NaN * -0.0f), "(float) (Float.NaN * -0.0f)");
    Tester.checkEqual(floatTimes(Float.NaN, 0.0f), (float) (Float.NaN * 0.0f), "(float) (Float.NaN * 0.0f)");
    Tester.checkEqual(floatTimes(Float.NaN, Float.MIN_VALUE), (float) (Float.NaN * Float.MIN_VALUE), "(float) (Float.NaN * Float.MIN_VALUE)");
    Tester.checkEqual(floatTimes(Float.NaN, 1.0f), (float) (Float.NaN * 1.0f), "(float) (Float.NaN * 1.0f)");
    Tester.checkEqual(floatTimes(Float.NaN, Float.MAX_VALUE), (float) (Float.NaN * Float.MAX_VALUE), "(float) (Float.NaN * Float.MAX_VALUE)");
    Tester.checkEqual(floatTimes(Float.NaN, Float.POSITIVE_INFINITY), (float) (Float.NaN * Float.POSITIVE_INFINITY), "(float) (Float.NaN * Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatTimes(Float.NaN, Float.NaN), (float) (Float.NaN * Float.NaN), "(float) (Float.NaN * Float.NaN)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY / Float.NEGATIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, -1.0f), (float) (Float.NEGATIVE_INFINITY / -1.0f), "(float) (Float.NEGATIVE_INFINITY / -1.0f)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, -0.0f), (float) (Float.NEGATIVE_INFINITY / -0.0f), "(float) (Float.NEGATIVE_INFINITY / -0.0f)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, 0.0f), (float) (Float.NEGATIVE_INFINITY / 0.0f), "(float) (Float.NEGATIVE_INFINITY / 0.0f)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), (float) (Float.NEGATIVE_INFINITY / Float.MIN_VALUE), "(float) (Float.NEGATIVE_INFINITY / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, 1.0f), (float) (Float.NEGATIVE_INFINITY / 1.0f), "(float) (Float.NEGATIVE_INFINITY / 1.0f)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), (float) (Float.NEGATIVE_INFINITY / Float.MAX_VALUE), "(float) (Float.NEGATIVE_INFINITY / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY / Float.POSITIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.NEGATIVE_INFINITY, Float.NaN), (float) (Float.NEGATIVE_INFINITY / Float.NaN), "(float) (Float.NEGATIVE_INFINITY / Float.NaN)");
    Tester.checkEqual(floatDiv(-1.0f, Float.NEGATIVE_INFINITY), (float) (-1.0f / Float.NEGATIVE_INFINITY), "(float) (-1.0f / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(-1.0f, -1.0f), (float) (-1.0f / -1.0f), "(float) (-1.0f / -1.0f)");
    Tester.checkEqual(floatDiv(-1.0f, -0.0f), (float) (-1.0f / -0.0f), "(float) (-1.0f / -0.0f)");
    Tester.checkEqual(floatDiv(-1.0f, 0.0f), (float) (-1.0f / 0.0f), "(float) (-1.0f / 0.0f)");
    Tester.checkEqual(floatDiv(-1.0f, Float.MIN_VALUE), (float) (-1.0f / Float.MIN_VALUE), "(float) (-1.0f / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(-1.0f, 1.0f), (float) (-1.0f / 1.0f), "(float) (-1.0f / 1.0f)");
    Tester.checkEqual(floatDiv(-1.0f, Float.MAX_VALUE), (float) (-1.0f / Float.MAX_VALUE), "(float) (-1.0f / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(-1.0f, Float.POSITIVE_INFINITY), (float) (-1.0f / Float.POSITIVE_INFINITY), "(float) (-1.0f / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(-1.0f, Float.NaN), (float) (-1.0f / Float.NaN), "(float) (-1.0f / Float.NaN)");
    Tester.checkEqual(floatDiv(-0.0f, Float.NEGATIVE_INFINITY), (float) (-0.0f / Float.NEGATIVE_INFINITY), "(float) (-0.0f / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(-0.0f, -1.0f), (float) (-0.0f / -1.0f), "(float) (-0.0f / -1.0f)");
    Tester.checkEqual(floatDiv(-0.0f, -0.0f), (float) (-0.0f / -0.0f), "(float) (-0.0f / -0.0f)");
    Tester.checkEqual(floatDiv(-0.0f, 0.0f), (float) (-0.0f / 0.0f), "(float) (-0.0f / 0.0f)");
    Tester.checkEqual(floatDiv(-0.0f, Float.MIN_VALUE), (float) (-0.0f / Float.MIN_VALUE), "(float) (-0.0f / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(-0.0f, 1.0f), (float) (-0.0f / 1.0f), "(float) (-0.0f / 1.0f)");
    Tester.checkEqual(floatDiv(-0.0f, Float.MAX_VALUE), (float) (-0.0f / Float.MAX_VALUE), "(float) (-0.0f / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(-0.0f, Float.POSITIVE_INFINITY), (float) (-0.0f / Float.POSITIVE_INFINITY), "(float) (-0.0f / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(-0.0f, Float.NaN), (float) (-0.0f / Float.NaN), "(float) (-0.0f / Float.NaN)");
    Tester.checkEqual(floatDiv(0.0f, Float.NEGATIVE_INFINITY), (float) (0.0f / Float.NEGATIVE_INFINITY), "(float) (0.0f / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(0.0f, -1.0f), (float) (0.0f / -1.0f), "(float) (0.0f / -1.0f)");
    Tester.checkEqual(floatDiv(0.0f, -0.0f), (float) (0.0f / -0.0f), "(float) (0.0f / -0.0f)");
    Tester.checkEqual(floatDiv(0.0f, 0.0f), (float) (0.0f / 0.0f), "(float) (0.0f / 0.0f)");
    Tester.checkEqual(floatDiv(0.0f, Float.MIN_VALUE), (float) (0.0f / Float.MIN_VALUE), "(float) (0.0f / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(0.0f, 1.0f), (float) (0.0f / 1.0f), "(float) (0.0f / 1.0f)");
    Tester.checkEqual(floatDiv(0.0f, Float.MAX_VALUE), (float) (0.0f / Float.MAX_VALUE), "(float) (0.0f / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(0.0f, Float.POSITIVE_INFINITY), (float) (0.0f / Float.POSITIVE_INFINITY), "(float) (0.0f / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(0.0f, Float.NaN), (float) (0.0f / Float.NaN), "(float) (0.0f / Float.NaN)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MIN_VALUE / Float.NEGATIVE_INFINITY), "(float) (Float.MIN_VALUE / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, -1.0f), (float) (Float.MIN_VALUE / -1.0f), "(float) (Float.MIN_VALUE / -1.0f)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, -0.0f), (float) (Float.MIN_VALUE / -0.0f), "(float) (Float.MIN_VALUE / -0.0f)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, 0.0f), (float) (Float.MIN_VALUE / 0.0f), "(float) (Float.MIN_VALUE / 0.0f)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, Float.MIN_VALUE), (float) (Float.MIN_VALUE / Float.MIN_VALUE), "(float) (Float.MIN_VALUE / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, 1.0f), (float) (Float.MIN_VALUE / 1.0f), "(float) (Float.MIN_VALUE / 1.0f)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, Float.MAX_VALUE), (float) (Float.MIN_VALUE / Float.MAX_VALUE), "(float) (Float.MIN_VALUE / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MIN_VALUE / Float.POSITIVE_INFINITY), "(float) (Float.MIN_VALUE / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.MIN_VALUE, Float.NaN), (float) (Float.MIN_VALUE / Float.NaN), "(float) (Float.MIN_VALUE / Float.NaN)");
    Tester.checkEqual(floatDiv(1.0f, Float.NEGATIVE_INFINITY), (float) (1.0f / Float.NEGATIVE_INFINITY), "(float) (1.0f / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(1.0f, -1.0f), (float) (1.0f / -1.0f), "(float) (1.0f / -1.0f)");
    Tester.checkEqual(floatDiv(1.0f, -0.0f), (float) (1.0f / -0.0f), "(float) (1.0f / -0.0f)");
    Tester.checkEqual(floatDiv(1.0f, 0.0f), (float) (1.0f / 0.0f), "(float) (1.0f / 0.0f)");
    Tester.checkEqual(floatDiv(1.0f, Float.MIN_VALUE), (float) (1.0f / Float.MIN_VALUE), "(float) (1.0f / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(1.0f, 1.0f), (float) (1.0f / 1.0f), "(float) (1.0f / 1.0f)");
    Tester.checkEqual(floatDiv(1.0f, Float.MAX_VALUE), (float) (1.0f / Float.MAX_VALUE), "(float) (1.0f / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(1.0f, Float.POSITIVE_INFINITY), (float) (1.0f / Float.POSITIVE_INFINITY), "(float) (1.0f / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(1.0f, Float.NaN), (float) (1.0f / Float.NaN), "(float) (1.0f / Float.NaN)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MAX_VALUE / Float.NEGATIVE_INFINITY), "(float) (Float.MAX_VALUE / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, -1.0f), (float) (Float.MAX_VALUE / -1.0f), "(float) (Float.MAX_VALUE / -1.0f)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, -0.0f), (float) (Float.MAX_VALUE / -0.0f), "(float) (Float.MAX_VALUE / -0.0f)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, 0.0f), (float) (Float.MAX_VALUE / 0.0f), "(float) (Float.MAX_VALUE / 0.0f)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, Float.MIN_VALUE), (float) (Float.MAX_VALUE / Float.MIN_VALUE), "(float) (Float.MAX_VALUE / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, 1.0f), (float) (Float.MAX_VALUE / 1.0f), "(float) (Float.MAX_VALUE / 1.0f)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, Float.MAX_VALUE), (float) (Float.MAX_VALUE / Float.MAX_VALUE), "(float) (Float.MAX_VALUE / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MAX_VALUE / Float.POSITIVE_INFINITY), "(float) (Float.MAX_VALUE / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.MAX_VALUE, Float.NaN), (float) (Float.MAX_VALUE / Float.NaN), "(float) (Float.MAX_VALUE / Float.NaN)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.POSITIVE_INFINITY / Float.NEGATIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, -1.0f), (float) (Float.POSITIVE_INFINITY / -1.0f), "(float) (Float.POSITIVE_INFINITY / -1.0f)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, -0.0f), (float) (Float.POSITIVE_INFINITY / -0.0f), "(float) (Float.POSITIVE_INFINITY / -0.0f)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, 0.0f), (float) (Float.POSITIVE_INFINITY / 0.0f), "(float) (Float.POSITIVE_INFINITY / 0.0f)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, Float.MIN_VALUE), (float) (Float.POSITIVE_INFINITY / Float.MIN_VALUE), "(float) (Float.POSITIVE_INFINITY / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, 1.0f), (float) (Float.POSITIVE_INFINITY / 1.0f), "(float) (Float.POSITIVE_INFINITY / 1.0f)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, Float.MAX_VALUE), (float) (Float.POSITIVE_INFINITY / Float.MAX_VALUE), "(float) (Float.POSITIVE_INFINITY / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.POSITIVE_INFINITY / Float.POSITIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.POSITIVE_INFINITY, Float.NaN), (float) (Float.POSITIVE_INFINITY / Float.NaN), "(float) (Float.POSITIVE_INFINITY / Float.NaN)");
    Tester.checkEqual(floatDiv(Float.NaN, Float.NEGATIVE_INFINITY), (float) (Float.NaN / Float.NEGATIVE_INFINITY), "(float) (Float.NaN / Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.NaN, -1.0f), (float) (Float.NaN / -1.0f), "(float) (Float.NaN / -1.0f)");
    Tester.checkEqual(floatDiv(Float.NaN, -0.0f), (float) (Float.NaN / -0.0f), "(float) (Float.NaN / -0.0f)");
    Tester.checkEqual(floatDiv(Float.NaN, 0.0f), (float) (Float.NaN / 0.0f), "(float) (Float.NaN / 0.0f)");
    Tester.checkEqual(floatDiv(Float.NaN, Float.MIN_VALUE), (float) (Float.NaN / Float.MIN_VALUE), "(float) (Float.NaN / Float.MIN_VALUE)");
    Tester.checkEqual(floatDiv(Float.NaN, 1.0f), (float) (Float.NaN / 1.0f), "(float) (Float.NaN / 1.0f)");
    Tester.checkEqual(floatDiv(Float.NaN, Float.MAX_VALUE), (float) (Float.NaN / Float.MAX_VALUE), "(float) (Float.NaN / Float.MAX_VALUE)");
    Tester.checkEqual(floatDiv(Float.NaN, Float.POSITIVE_INFINITY), (float) (Float.NaN / Float.POSITIVE_INFINITY), "(float) (Float.NaN / Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatDiv(Float.NaN, Float.NaN), (float) (Float.NaN / Float.NaN), "(float) (Float.NaN / Float.NaN)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, -1.0f), (float) (Float.NEGATIVE_INFINITY % -1.0f), "(float) (Float.NEGATIVE_INFINITY % -1.0f)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, -0.0f), (float) (Float.NEGATIVE_INFINITY % -0.0f), "(float) (Float.NEGATIVE_INFINITY % -0.0f)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, 0.0f), (float) (Float.NEGATIVE_INFINITY % 0.0f), "(float) (Float.NEGATIVE_INFINITY % 0.0f)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), (float) (Float.NEGATIVE_INFINITY % Float.MIN_VALUE), "(float) (Float.NEGATIVE_INFINITY % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, 1.0f), (float) (Float.NEGATIVE_INFINITY % 1.0f), "(float) (Float.NEGATIVE_INFINITY % 1.0f)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), (float) (Float.NEGATIVE_INFINITY % Float.MAX_VALUE), "(float) (Float.NEGATIVE_INFINITY % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.NEGATIVE_INFINITY, Float.NaN), (float) (Float.NEGATIVE_INFINITY % Float.NaN), "(float) (Float.NEGATIVE_INFINITY % Float.NaN)");
    Tester.checkEqual(floatRem(-1.0f, Float.NEGATIVE_INFINITY), (float) (-1.0f % Float.NEGATIVE_INFINITY), "(float) (-1.0f % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(-1.0f, -1.0f), (float) (-1.0f % -1.0f), "(float) (-1.0f % -1.0f)");
    Tester.checkEqual(floatRem(-1.0f, -0.0f), (float) (-1.0f % -0.0f), "(float) (-1.0f % -0.0f)");
    Tester.checkEqual(floatRem(-1.0f, 0.0f), (float) (-1.0f % 0.0f), "(float) (-1.0f % 0.0f)");
    Tester.checkEqual(floatRem(-1.0f, Float.MIN_VALUE), (float) (-1.0f % Float.MIN_VALUE), "(float) (-1.0f % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(-1.0f, 1.0f), (float) (-1.0f % 1.0f), "(float) (-1.0f % 1.0f)");
    Tester.checkEqual(floatRem(-1.0f, Float.MAX_VALUE), (float) (-1.0f % Float.MAX_VALUE), "(float) (-1.0f % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(-1.0f, Float.POSITIVE_INFINITY), (float) (-1.0f % Float.POSITIVE_INFINITY), "(float) (-1.0f % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(-1.0f, Float.NaN), (float) (-1.0f % Float.NaN), "(float) (-1.0f % Float.NaN)");
    Tester.checkEqual(floatRem(-0.0f, Float.NEGATIVE_INFINITY), (float) (-0.0f % Float.NEGATIVE_INFINITY), "(float) (-0.0f % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(-0.0f, -1.0f), (float) (-0.0f % -1.0f), "(float) (-0.0f % -1.0f)");
    Tester.checkEqual(floatRem(-0.0f, -0.0f), (float) (-0.0f % -0.0f), "(float) (-0.0f % -0.0f)");
    Tester.checkEqual(floatRem(-0.0f, 0.0f), (float) (-0.0f % 0.0f), "(float) (-0.0f % 0.0f)");
    Tester.checkEqual(floatRem(-0.0f, Float.MIN_VALUE), (float) (-0.0f % Float.MIN_VALUE), "(float) (-0.0f % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(-0.0f, 1.0f), (float) (-0.0f % 1.0f), "(float) (-0.0f % 1.0f)");
    Tester.checkEqual(floatRem(-0.0f, Float.MAX_VALUE), (float) (-0.0f % Float.MAX_VALUE), "(float) (-0.0f % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(-0.0f, Float.POSITIVE_INFINITY), (float) (-0.0f % Float.POSITIVE_INFINITY), "(float) (-0.0f % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(-0.0f, Float.NaN), (float) (-0.0f % Float.NaN), "(float) (-0.0f % Float.NaN)");
    Tester.checkEqual(floatRem(0.0f, Float.NEGATIVE_INFINITY), (float) (0.0f % Float.NEGATIVE_INFINITY), "(float) (0.0f % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(0.0f, -1.0f), (float) (0.0f % -1.0f), "(float) (0.0f % -1.0f)");
    Tester.checkEqual(floatRem(0.0f, -0.0f), (float) (0.0f % -0.0f), "(float) (0.0f % -0.0f)");
    Tester.checkEqual(floatRem(0.0f, 0.0f), (float) (0.0f % 0.0f), "(float) (0.0f % 0.0f)");
    Tester.checkEqual(floatRem(0.0f, Float.MIN_VALUE), (float) (0.0f % Float.MIN_VALUE), "(float) (0.0f % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(0.0f, 1.0f), (float) (0.0f % 1.0f), "(float) (0.0f % 1.0f)");
    Tester.checkEqual(floatRem(0.0f, Float.MAX_VALUE), (float) (0.0f % Float.MAX_VALUE), "(float) (0.0f % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(0.0f, Float.POSITIVE_INFINITY), (float) (0.0f % Float.POSITIVE_INFINITY), "(float) (0.0f % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(0.0f, Float.NaN), (float) (0.0f % Float.NaN), "(float) (0.0f % Float.NaN)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MIN_VALUE % Float.NEGATIVE_INFINITY), "(float) (Float.MIN_VALUE % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, -1.0f), (float) (Float.MIN_VALUE % -1.0f), "(float) (Float.MIN_VALUE % -1.0f)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, -0.0f), (float) (Float.MIN_VALUE % -0.0f), "(float) (Float.MIN_VALUE % -0.0f)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, 0.0f), (float) (Float.MIN_VALUE % 0.0f), "(float) (Float.MIN_VALUE % 0.0f)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, Float.MIN_VALUE), (float) (Float.MIN_VALUE % Float.MIN_VALUE), "(float) (Float.MIN_VALUE % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, 1.0f), (float) (Float.MIN_VALUE % 1.0f), "(float) (Float.MIN_VALUE % 1.0f)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, Float.MAX_VALUE), (float) (Float.MIN_VALUE % Float.MAX_VALUE), "(float) (Float.MIN_VALUE % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MIN_VALUE % Float.POSITIVE_INFINITY), "(float) (Float.MIN_VALUE % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.MIN_VALUE, Float.NaN), (float) (Float.MIN_VALUE % Float.NaN), "(float) (Float.MIN_VALUE % Float.NaN)");
    Tester.checkEqual(floatRem(1.0f, Float.NEGATIVE_INFINITY), (float) (1.0f % Float.NEGATIVE_INFINITY), "(float) (1.0f % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(1.0f, -1.0f), (float) (1.0f % -1.0f), "(float) (1.0f % -1.0f)");
    Tester.checkEqual(floatRem(1.0f, -0.0f), (float) (1.0f % -0.0f), "(float) (1.0f % -0.0f)");
    Tester.checkEqual(floatRem(1.0f, 0.0f), (float) (1.0f % 0.0f), "(float) (1.0f % 0.0f)");
    Tester.checkEqual(floatRem(1.0f, Float.MIN_VALUE), (float) (1.0f % Float.MIN_VALUE), "(float) (1.0f % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(1.0f, 1.0f), (float) (1.0f % 1.0f), "(float) (1.0f % 1.0f)");
    Tester.checkEqual(floatRem(1.0f, Float.MAX_VALUE), (float) (1.0f % Float.MAX_VALUE), "(float) (1.0f % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(1.0f, Float.POSITIVE_INFINITY), (float) (1.0f % Float.POSITIVE_INFINITY), "(float) (1.0f % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(1.0f, Float.NaN), (float) (1.0f % Float.NaN), "(float) (1.0f % Float.NaN)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MAX_VALUE % Float.NEGATIVE_INFINITY), "(float) (Float.MAX_VALUE % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, -1.0f), (float) (Float.MAX_VALUE % -1.0f), "(float) (Float.MAX_VALUE % -1.0f)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, -0.0f), (float) (Float.MAX_VALUE % -0.0f), "(float) (Float.MAX_VALUE % -0.0f)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, 0.0f), (float) (Float.MAX_VALUE % 0.0f), "(float) (Float.MAX_VALUE % 0.0f)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, Float.MIN_VALUE), (float) (Float.MAX_VALUE % Float.MIN_VALUE), "(float) (Float.MAX_VALUE % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, 1.0f), (float) (Float.MAX_VALUE % 1.0f), "(float) (Float.MAX_VALUE % 1.0f)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, Float.MAX_VALUE), (float) (Float.MAX_VALUE % Float.MAX_VALUE), "(float) (Float.MAX_VALUE % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MAX_VALUE % Float.POSITIVE_INFINITY), "(float) (Float.MAX_VALUE % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.MAX_VALUE, Float.NaN), (float) (Float.MAX_VALUE % Float.NaN), "(float) (Float.MAX_VALUE % Float.NaN)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, -1.0f), (float) (Float.POSITIVE_INFINITY % -1.0f), "(float) (Float.POSITIVE_INFINITY % -1.0f)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, -0.0f), (float) (Float.POSITIVE_INFINITY % -0.0f), "(float) (Float.POSITIVE_INFINITY % -0.0f)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, 0.0f), (float) (Float.POSITIVE_INFINITY % 0.0f), "(float) (Float.POSITIVE_INFINITY % 0.0f)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, Float.MIN_VALUE), (float) (Float.POSITIVE_INFINITY % Float.MIN_VALUE), "(float) (Float.POSITIVE_INFINITY % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, 1.0f), (float) (Float.POSITIVE_INFINITY % 1.0f), "(float) (Float.POSITIVE_INFINITY % 1.0f)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, Float.MAX_VALUE), (float) (Float.POSITIVE_INFINITY % Float.MAX_VALUE), "(float) (Float.POSITIVE_INFINITY % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.POSITIVE_INFINITY, Float.NaN), (float) (Float.POSITIVE_INFINITY % Float.NaN), "(float) (Float.POSITIVE_INFINITY % Float.NaN)");
    Tester.checkEqual(floatRem(Float.NaN, Float.NEGATIVE_INFINITY), (float) (Float.NaN % Float.NEGATIVE_INFINITY), "(float) (Float.NaN % Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.NaN, -1.0f), (float) (Float.NaN % -1.0f), "(float) (Float.NaN % -1.0f)");
    Tester.checkEqual(floatRem(Float.NaN, -0.0f), (float) (Float.NaN % -0.0f), "(float) (Float.NaN % -0.0f)");
    Tester.checkEqual(floatRem(Float.NaN, 0.0f), (float) (Float.NaN % 0.0f), "(float) (Float.NaN % 0.0f)");
    Tester.checkEqual(floatRem(Float.NaN, Float.MIN_VALUE), (float) (Float.NaN % Float.MIN_VALUE), "(float) (Float.NaN % Float.MIN_VALUE)");
    Tester.checkEqual(floatRem(Float.NaN, 1.0f), (float) (Float.NaN % 1.0f), "(float) (Float.NaN % 1.0f)");
    Tester.checkEqual(floatRem(Float.NaN, Float.MAX_VALUE), (float) (Float.NaN % Float.MAX_VALUE), "(float) (Float.NaN % Float.MAX_VALUE)");
    Tester.checkEqual(floatRem(Float.NaN, Float.POSITIVE_INFINITY), (float) (Float.NaN % Float.POSITIVE_INFINITY), "(float) (Float.NaN % Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatRem(Float.NaN, Float.NaN), (float) (Float.NaN % Float.NaN), "(float) (Float.NaN % Float.NaN)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, -1.0f), (float) (Float.NEGATIVE_INFINITY + -1.0f), "(float) (Float.NEGATIVE_INFINITY + -1.0f)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, -0.0f), (float) (Float.NEGATIVE_INFINITY + -0.0f), "(float) (Float.NEGATIVE_INFINITY + -0.0f)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, 0.0f), (float) (Float.NEGATIVE_INFINITY + 0.0f), "(float) (Float.NEGATIVE_INFINITY + 0.0f)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), (float) (Float.NEGATIVE_INFINITY + Float.MIN_VALUE), "(float) (Float.NEGATIVE_INFINITY + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, 1.0f), (float) (Float.NEGATIVE_INFINITY + 1.0f), "(float) (Float.NEGATIVE_INFINITY + 1.0f)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), (float) (Float.NEGATIVE_INFINITY + Float.MAX_VALUE), "(float) (Float.NEGATIVE_INFINITY + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.NEGATIVE_INFINITY, Float.NaN), (float) (Float.NEGATIVE_INFINITY + Float.NaN), "(float) (Float.NEGATIVE_INFINITY + Float.NaN)");
    Tester.checkEqual(floatAdd(-1.0f, Float.NEGATIVE_INFINITY), (float) (-1.0f + Float.NEGATIVE_INFINITY), "(float) (-1.0f + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(-1.0f, -1.0f), (float) (-1.0f + -1.0f), "(float) (-1.0f + -1.0f)");
    Tester.checkEqual(floatAdd(-1.0f, -0.0f), (float) (-1.0f + -0.0f), "(float) (-1.0f + -0.0f)");
    Tester.checkEqual(floatAdd(-1.0f, 0.0f), (float) (-1.0f + 0.0f), "(float) (-1.0f + 0.0f)");
    Tester.checkEqual(floatAdd(-1.0f, Float.MIN_VALUE), (float) (-1.0f + Float.MIN_VALUE), "(float) (-1.0f + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(-1.0f, 1.0f), (float) (-1.0f + 1.0f), "(float) (-1.0f + 1.0f)");
    Tester.checkEqual(floatAdd(-1.0f, Float.MAX_VALUE), (float) (-1.0f + Float.MAX_VALUE), "(float) (-1.0f + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(-1.0f, Float.POSITIVE_INFINITY), (float) (-1.0f + Float.POSITIVE_INFINITY), "(float) (-1.0f + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(-1.0f, Float.NaN), (float) (-1.0f + Float.NaN), "(float) (-1.0f + Float.NaN)");
    Tester.checkEqual(floatAdd(-0.0f, Float.NEGATIVE_INFINITY), (float) (-0.0f + Float.NEGATIVE_INFINITY), "(float) (-0.0f + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(-0.0f, -1.0f), (float) (-0.0f + -1.0f), "(float) (-0.0f + -1.0f)");
    Tester.checkEqual(floatAdd(-0.0f, -0.0f), (float) (-0.0f + -0.0f), "(float) (-0.0f + -0.0f)");
    Tester.checkEqual(floatAdd(-0.0f, 0.0f), (float) (-0.0f + 0.0f), "(float) (-0.0f + 0.0f)");
    Tester.checkEqual(floatAdd(-0.0f, Float.MIN_VALUE), (float) (-0.0f + Float.MIN_VALUE), "(float) (-0.0f + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(-0.0f, 1.0f), (float) (-0.0f + 1.0f), "(float) (-0.0f + 1.0f)");
    Tester.checkEqual(floatAdd(-0.0f, Float.MAX_VALUE), (float) (-0.0f + Float.MAX_VALUE), "(float) (-0.0f + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(-0.0f, Float.POSITIVE_INFINITY), (float) (-0.0f + Float.POSITIVE_INFINITY), "(float) (-0.0f + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(-0.0f, Float.NaN), (float) (-0.0f + Float.NaN), "(float) (-0.0f + Float.NaN)");
    Tester.checkEqual(floatAdd(0.0f, Float.NEGATIVE_INFINITY), (float) (0.0f + Float.NEGATIVE_INFINITY), "(float) (0.0f + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(0.0f, -1.0f), (float) (0.0f + -1.0f), "(float) (0.0f + -1.0f)");
    Tester.checkEqual(floatAdd(0.0f, -0.0f), (float) (0.0f + -0.0f), "(float) (0.0f + -0.0f)");
    Tester.checkEqual(floatAdd(0.0f, 0.0f), (float) (0.0f + 0.0f), "(float) (0.0f + 0.0f)");
    Tester.checkEqual(floatAdd(0.0f, Float.MIN_VALUE), (float) (0.0f + Float.MIN_VALUE), "(float) (0.0f + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(0.0f, 1.0f), (float) (0.0f + 1.0f), "(float) (0.0f + 1.0f)");
    Tester.checkEqual(floatAdd(0.0f, Float.MAX_VALUE), (float) (0.0f + Float.MAX_VALUE), "(float) (0.0f + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(0.0f, Float.POSITIVE_INFINITY), (float) (0.0f + Float.POSITIVE_INFINITY), "(float) (0.0f + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(0.0f, Float.NaN), (float) (0.0f + Float.NaN), "(float) (0.0f + Float.NaN)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MIN_VALUE + Float.NEGATIVE_INFINITY), "(float) (Float.MIN_VALUE + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, -1.0f), (float) (Float.MIN_VALUE + -1.0f), "(float) (Float.MIN_VALUE + -1.0f)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, -0.0f), (float) (Float.MIN_VALUE + -0.0f), "(float) (Float.MIN_VALUE + -0.0f)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, 0.0f), (float) (Float.MIN_VALUE + 0.0f), "(float) (Float.MIN_VALUE + 0.0f)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, Float.MIN_VALUE), (float) (Float.MIN_VALUE + Float.MIN_VALUE), "(float) (Float.MIN_VALUE + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, 1.0f), (float) (Float.MIN_VALUE + 1.0f), "(float) (Float.MIN_VALUE + 1.0f)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, Float.MAX_VALUE), (float) (Float.MIN_VALUE + Float.MAX_VALUE), "(float) (Float.MIN_VALUE + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MIN_VALUE + Float.POSITIVE_INFINITY), "(float) (Float.MIN_VALUE + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.MIN_VALUE, Float.NaN), (float) (Float.MIN_VALUE + Float.NaN), "(float) (Float.MIN_VALUE + Float.NaN)");
    Tester.checkEqual(floatAdd(1.0f, Float.NEGATIVE_INFINITY), (float) (1.0f + Float.NEGATIVE_INFINITY), "(float) (1.0f + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(1.0f, -1.0f), (float) (1.0f + -1.0f), "(float) (1.0f + -1.0f)");
    Tester.checkEqual(floatAdd(1.0f, -0.0f), (float) (1.0f + -0.0f), "(float) (1.0f + -0.0f)");
    Tester.checkEqual(floatAdd(1.0f, 0.0f), (float) (1.0f + 0.0f), "(float) (1.0f + 0.0f)");
    Tester.checkEqual(floatAdd(1.0f, Float.MIN_VALUE), (float) (1.0f + Float.MIN_VALUE), "(float) (1.0f + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(1.0f, 1.0f), (float) (1.0f + 1.0f), "(float) (1.0f + 1.0f)");
    Tester.checkEqual(floatAdd(1.0f, Float.MAX_VALUE), (float) (1.0f + Float.MAX_VALUE), "(float) (1.0f + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(1.0f, Float.POSITIVE_INFINITY), (float) (1.0f + Float.POSITIVE_INFINITY), "(float) (1.0f + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(1.0f, Float.NaN), (float) (1.0f + Float.NaN), "(float) (1.0f + Float.NaN)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MAX_VALUE + Float.NEGATIVE_INFINITY), "(float) (Float.MAX_VALUE + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, -1.0f), (float) (Float.MAX_VALUE + -1.0f), "(float) (Float.MAX_VALUE + -1.0f)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, -0.0f), (float) (Float.MAX_VALUE + -0.0f), "(float) (Float.MAX_VALUE + -0.0f)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, 0.0f), (float) (Float.MAX_VALUE + 0.0f), "(float) (Float.MAX_VALUE + 0.0f)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, Float.MIN_VALUE), (float) (Float.MAX_VALUE + Float.MIN_VALUE), "(float) (Float.MAX_VALUE + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, 1.0f), (float) (Float.MAX_VALUE + 1.0f), "(float) (Float.MAX_VALUE + 1.0f)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, Float.MAX_VALUE), (float) (Float.MAX_VALUE + Float.MAX_VALUE), "(float) (Float.MAX_VALUE + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MAX_VALUE + Float.POSITIVE_INFINITY), "(float) (Float.MAX_VALUE + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.MAX_VALUE, Float.NaN), (float) (Float.MAX_VALUE + Float.NaN), "(float) (Float.MAX_VALUE + Float.NaN)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.POSITIVE_INFINITY + Float.NEGATIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, -1.0f), (float) (Float.POSITIVE_INFINITY + -1.0f), "(float) (Float.POSITIVE_INFINITY + -1.0f)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, -0.0f), (float) (Float.POSITIVE_INFINITY + -0.0f), "(float) (Float.POSITIVE_INFINITY + -0.0f)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, 0.0f), (float) (Float.POSITIVE_INFINITY + 0.0f), "(float) (Float.POSITIVE_INFINITY + 0.0f)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, Float.MIN_VALUE), (float) (Float.POSITIVE_INFINITY + Float.MIN_VALUE), "(float) (Float.POSITIVE_INFINITY + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, 1.0f), (float) (Float.POSITIVE_INFINITY + 1.0f), "(float) (Float.POSITIVE_INFINITY + 1.0f)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, Float.MAX_VALUE), (float) (Float.POSITIVE_INFINITY + Float.MAX_VALUE), "(float) (Float.POSITIVE_INFINITY + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.POSITIVE_INFINITY + Float.POSITIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.POSITIVE_INFINITY, Float.NaN), (float) (Float.POSITIVE_INFINITY + Float.NaN), "(float) (Float.POSITIVE_INFINITY + Float.NaN)");
    Tester.checkEqual(floatAdd(Float.NaN, Float.NEGATIVE_INFINITY), (float) (Float.NaN + Float.NEGATIVE_INFINITY), "(float) (Float.NaN + Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.NaN, -1.0f), (float) (Float.NaN + -1.0f), "(float) (Float.NaN + -1.0f)");
    Tester.checkEqual(floatAdd(Float.NaN, -0.0f), (float) (Float.NaN + -0.0f), "(float) (Float.NaN + -0.0f)");
    Tester.checkEqual(floatAdd(Float.NaN, 0.0f), (float) (Float.NaN + 0.0f), "(float) (Float.NaN + 0.0f)");
    Tester.checkEqual(floatAdd(Float.NaN, Float.MIN_VALUE), (float) (Float.NaN + Float.MIN_VALUE), "(float) (Float.NaN + Float.MIN_VALUE)");
    Tester.checkEqual(floatAdd(Float.NaN, 1.0f), (float) (Float.NaN + 1.0f), "(float) (Float.NaN + 1.0f)");
    Tester.checkEqual(floatAdd(Float.NaN, Float.MAX_VALUE), (float) (Float.NaN + Float.MAX_VALUE), "(float) (Float.NaN + Float.MAX_VALUE)");
    Tester.checkEqual(floatAdd(Float.NaN, Float.POSITIVE_INFINITY), (float) (Float.NaN + Float.POSITIVE_INFINITY), "(float) (Float.NaN + Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatAdd(Float.NaN, Float.NaN), (float) (Float.NaN + Float.NaN), "(float) (Float.NaN + Float.NaN)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY - Float.NEGATIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, -1.0f), (float) (Float.NEGATIVE_INFINITY - -1.0f), "(float) (Float.NEGATIVE_INFINITY - -1.0f)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, -0.0f), (float) (Float.NEGATIVE_INFINITY - -0.0f), "(float) (Float.NEGATIVE_INFINITY - -0.0f)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, 0.0f), (float) (Float.NEGATIVE_INFINITY - 0.0f), "(float) (Float.NEGATIVE_INFINITY - 0.0f)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), (float) (Float.NEGATIVE_INFINITY - Float.MIN_VALUE), "(float) (Float.NEGATIVE_INFINITY - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, 1.0f), (float) (Float.NEGATIVE_INFINITY - 1.0f), "(float) (Float.NEGATIVE_INFINITY - 1.0f)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), (float) (Float.NEGATIVE_INFINITY - Float.MAX_VALUE), "(float) (Float.NEGATIVE_INFINITY - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.NEGATIVE_INFINITY - Float.POSITIVE_INFINITY), "(float) (Float.NEGATIVE_INFINITY - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.NEGATIVE_INFINITY, Float.NaN), (float) (Float.NEGATIVE_INFINITY - Float.NaN), "(float) (Float.NEGATIVE_INFINITY - Float.NaN)");
    Tester.checkEqual(floatSub(-1.0f, Float.NEGATIVE_INFINITY), (float) (-1.0f - Float.NEGATIVE_INFINITY), "(float) (-1.0f - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(-1.0f, -1.0f), (float) (-1.0f - -1.0f), "(float) (-1.0f - -1.0f)");
    Tester.checkEqual(floatSub(-1.0f, -0.0f), (float) (-1.0f - -0.0f), "(float) (-1.0f - -0.0f)");
    Tester.checkEqual(floatSub(-1.0f, 0.0f), (float) (-1.0f - 0.0f), "(float) (-1.0f - 0.0f)");
    Tester.checkEqual(floatSub(-1.0f, Float.MIN_VALUE), (float) (-1.0f - Float.MIN_VALUE), "(float) (-1.0f - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(-1.0f, 1.0f), (float) (-1.0f - 1.0f), "(float) (-1.0f - 1.0f)");
    Tester.checkEqual(floatSub(-1.0f, Float.MAX_VALUE), (float) (-1.0f - Float.MAX_VALUE), "(float) (-1.0f - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(-1.0f, Float.POSITIVE_INFINITY), (float) (-1.0f - Float.POSITIVE_INFINITY), "(float) (-1.0f - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(-1.0f, Float.NaN), (float) (-1.0f - Float.NaN), "(float) (-1.0f - Float.NaN)");
    Tester.checkEqual(floatSub(-0.0f, Float.NEGATIVE_INFINITY), (float) (-0.0f - Float.NEGATIVE_INFINITY), "(float) (-0.0f - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(-0.0f, -1.0f), (float) (-0.0f - -1.0f), "(float) (-0.0f - -1.0f)");
    Tester.checkEqual(floatSub(-0.0f, -0.0f), (float) (-0.0f - -0.0f), "(float) (-0.0f - -0.0f)");
    Tester.checkEqual(floatSub(-0.0f, 0.0f), (float) (-0.0f - 0.0f), "(float) (-0.0f - 0.0f)");
    Tester.checkEqual(floatSub(-0.0f, Float.MIN_VALUE), (float) (-0.0f - Float.MIN_VALUE), "(float) (-0.0f - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(-0.0f, 1.0f), (float) (-0.0f - 1.0f), "(float) (-0.0f - 1.0f)");
    Tester.checkEqual(floatSub(-0.0f, Float.MAX_VALUE), (float) (-0.0f - Float.MAX_VALUE), "(float) (-0.0f - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(-0.0f, Float.POSITIVE_INFINITY), (float) (-0.0f - Float.POSITIVE_INFINITY), "(float) (-0.0f - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(-0.0f, Float.NaN), (float) (-0.0f - Float.NaN), "(float) (-0.0f - Float.NaN)");
    Tester.checkEqual(floatSub(0.0f, Float.NEGATIVE_INFINITY), (float) (0.0f - Float.NEGATIVE_INFINITY), "(float) (0.0f - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(0.0f, -1.0f), (float) (0.0f - -1.0f), "(float) (0.0f - -1.0f)");
    Tester.checkEqual(floatSub(0.0f, -0.0f), (float) (0.0f - -0.0f), "(float) (0.0f - -0.0f)");
    Tester.checkEqual(floatSub(0.0f, 0.0f), (float) (0.0f - 0.0f), "(float) (0.0f - 0.0f)");
    Tester.checkEqual(floatSub(0.0f, Float.MIN_VALUE), (float) (0.0f - Float.MIN_VALUE), "(float) (0.0f - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(0.0f, 1.0f), (float) (0.0f - 1.0f), "(float) (0.0f - 1.0f)");
    Tester.checkEqual(floatSub(0.0f, Float.MAX_VALUE), (float) (0.0f - Float.MAX_VALUE), "(float) (0.0f - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(0.0f, Float.POSITIVE_INFINITY), (float) (0.0f - Float.POSITIVE_INFINITY), "(float) (0.0f - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(0.0f, Float.NaN), (float) (0.0f - Float.NaN), "(float) (0.0f - Float.NaN)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MIN_VALUE - Float.NEGATIVE_INFINITY), "(float) (Float.MIN_VALUE - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, -1.0f), (float) (Float.MIN_VALUE - -1.0f), "(float) (Float.MIN_VALUE - -1.0f)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, -0.0f), (float) (Float.MIN_VALUE - -0.0f), "(float) (Float.MIN_VALUE - -0.0f)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, 0.0f), (float) (Float.MIN_VALUE - 0.0f), "(float) (Float.MIN_VALUE - 0.0f)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, Float.MIN_VALUE), (float) (Float.MIN_VALUE - Float.MIN_VALUE), "(float) (Float.MIN_VALUE - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, 1.0f), (float) (Float.MIN_VALUE - 1.0f), "(float) (Float.MIN_VALUE - 1.0f)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, Float.MAX_VALUE), (float) (Float.MIN_VALUE - Float.MAX_VALUE), "(float) (Float.MIN_VALUE - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MIN_VALUE - Float.POSITIVE_INFINITY), "(float) (Float.MIN_VALUE - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.MIN_VALUE, Float.NaN), (float) (Float.MIN_VALUE - Float.NaN), "(float) (Float.MIN_VALUE - Float.NaN)");
    Tester.checkEqual(floatSub(1.0f, Float.NEGATIVE_INFINITY), (float) (1.0f - Float.NEGATIVE_INFINITY), "(float) (1.0f - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(1.0f, -1.0f), (float) (1.0f - -1.0f), "(float) (1.0f - -1.0f)");
    Tester.checkEqual(floatSub(1.0f, -0.0f), (float) (1.0f - -0.0f), "(float) (1.0f - -0.0f)");
    Tester.checkEqual(floatSub(1.0f, 0.0f), (float) (1.0f - 0.0f), "(float) (1.0f - 0.0f)");
    Tester.checkEqual(floatSub(1.0f, Float.MIN_VALUE), (float) (1.0f - Float.MIN_VALUE), "(float) (1.0f - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(1.0f, 1.0f), (float) (1.0f - 1.0f), "(float) (1.0f - 1.0f)");
    Tester.checkEqual(floatSub(1.0f, Float.MAX_VALUE), (float) (1.0f - Float.MAX_VALUE), "(float) (1.0f - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(1.0f, Float.POSITIVE_INFINITY), (float) (1.0f - Float.POSITIVE_INFINITY), "(float) (1.0f - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(1.0f, Float.NaN), (float) (1.0f - Float.NaN), "(float) (1.0f - Float.NaN)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), (float) (Float.MAX_VALUE - Float.NEGATIVE_INFINITY), "(float) (Float.MAX_VALUE - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, -1.0f), (float) (Float.MAX_VALUE - -1.0f), "(float) (Float.MAX_VALUE - -1.0f)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, -0.0f), (float) (Float.MAX_VALUE - -0.0f), "(float) (Float.MAX_VALUE - -0.0f)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, 0.0f), (float) (Float.MAX_VALUE - 0.0f), "(float) (Float.MAX_VALUE - 0.0f)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, Float.MIN_VALUE), (float) (Float.MAX_VALUE - Float.MIN_VALUE), "(float) (Float.MAX_VALUE - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, 1.0f), (float) (Float.MAX_VALUE - 1.0f), "(float) (Float.MAX_VALUE - 1.0f)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, Float.MAX_VALUE), (float) (Float.MAX_VALUE - Float.MAX_VALUE), "(float) (Float.MAX_VALUE - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, Float.POSITIVE_INFINITY), (float) (Float.MAX_VALUE - Float.POSITIVE_INFINITY), "(float) (Float.MAX_VALUE - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.MAX_VALUE, Float.NaN), (float) (Float.MAX_VALUE - Float.NaN), "(float) (Float.MAX_VALUE - Float.NaN)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), (float) (Float.POSITIVE_INFINITY - Float.NEGATIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, -1.0f), (float) (Float.POSITIVE_INFINITY - -1.0f), "(float) (Float.POSITIVE_INFINITY - -1.0f)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, -0.0f), (float) (Float.POSITIVE_INFINITY - -0.0f), "(float) (Float.POSITIVE_INFINITY - -0.0f)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, 0.0f), (float) (Float.POSITIVE_INFINITY - 0.0f), "(float) (Float.POSITIVE_INFINITY - 0.0f)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, Float.MIN_VALUE), (float) (Float.POSITIVE_INFINITY - Float.MIN_VALUE), "(float) (Float.POSITIVE_INFINITY - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, 1.0f), (float) (Float.POSITIVE_INFINITY - 1.0f), "(float) (Float.POSITIVE_INFINITY - 1.0f)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, Float.MAX_VALUE), (float) (Float.POSITIVE_INFINITY - Float.MAX_VALUE), "(float) (Float.POSITIVE_INFINITY - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), (float) (Float.POSITIVE_INFINITY - Float.POSITIVE_INFINITY), "(float) (Float.POSITIVE_INFINITY - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.POSITIVE_INFINITY, Float.NaN), (float) (Float.POSITIVE_INFINITY - Float.NaN), "(float) (Float.POSITIVE_INFINITY - Float.NaN)");
    Tester.checkEqual(floatSub(Float.NaN, Float.NEGATIVE_INFINITY), (float) (Float.NaN - Float.NEGATIVE_INFINITY), "(float) (Float.NaN - Float.NEGATIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.NaN, -1.0f), (float) (Float.NaN - -1.0f), "(float) (Float.NaN - -1.0f)");
    Tester.checkEqual(floatSub(Float.NaN, -0.0f), (float) (Float.NaN - -0.0f), "(float) (Float.NaN - -0.0f)");
    Tester.checkEqual(floatSub(Float.NaN, 0.0f), (float) (Float.NaN - 0.0f), "(float) (Float.NaN - 0.0f)");
    Tester.checkEqual(floatSub(Float.NaN, Float.MIN_VALUE), (float) (Float.NaN - Float.MIN_VALUE), "(float) (Float.NaN - Float.MIN_VALUE)");
    Tester.checkEqual(floatSub(Float.NaN, 1.0f), (float) (Float.NaN - 1.0f), "(float) (Float.NaN - 1.0f)");
    Tester.checkEqual(floatSub(Float.NaN, Float.MAX_VALUE), (float) (Float.NaN - Float.MAX_VALUE), "(float) (Float.NaN - Float.MAX_VALUE)");
    Tester.checkEqual(floatSub(Float.NaN, Float.POSITIVE_INFINITY), (float) (Float.NaN - Float.POSITIVE_INFINITY), "(float) (Float.NaN - Float.POSITIVE_INFINITY)");
    Tester.checkEqual(floatSub(Float.NaN, Float.NaN), (float) (Float.NaN - Float.NaN), "(float) (Float.NaN - Float.NaN)");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY < Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY < -1.0f, "Float.NEGATIVE_INFINITY < -1.0f");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY < -0.0f, "Float.NEGATIVE_INFINITY < -0.0f");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY < 0.0f, "Float.NEGATIVE_INFINITY < 0.0f");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY < Float.MIN_VALUE, "Float.NEGATIVE_INFINITY < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY < 1.0f, "Float.NEGATIVE_INFINITY < 1.0f");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY < Float.MAX_VALUE, "Float.NEGATIVE_INFINITY < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY < Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY < Float.NaN, "Float.NEGATIVE_INFINITY < Float.NaN");
    Tester.checkEqual(floatLt(-1.0f, Float.NEGATIVE_INFINITY), -1.0f < Float.NEGATIVE_INFINITY, "-1.0f < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(-1.0f, -1.0f), -1.0f < -1.0f, "-1.0f < -1.0f");
    Tester.checkEqual(floatLt(-1.0f, -0.0f), -1.0f < -0.0f, "-1.0f < -0.0f");
    Tester.checkEqual(floatLt(-1.0f, 0.0f), -1.0f < 0.0f, "-1.0f < 0.0f");
    Tester.checkEqual(floatLt(-1.0f, Float.MIN_VALUE), -1.0f < Float.MIN_VALUE, "-1.0f < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(-1.0f, 1.0f), -1.0f < 1.0f, "-1.0f < 1.0f");
    Tester.checkEqual(floatLt(-1.0f, Float.MAX_VALUE), -1.0f < Float.MAX_VALUE, "-1.0f < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(-1.0f, Float.POSITIVE_INFINITY), -1.0f < Float.POSITIVE_INFINITY, "-1.0f < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(-1.0f, Float.NaN), -1.0f < Float.NaN, "-1.0f < Float.NaN");
    Tester.checkEqual(floatLt(-0.0f, Float.NEGATIVE_INFINITY), -0.0f < Float.NEGATIVE_INFINITY, "-0.0f < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(-0.0f, -1.0f), -0.0f < -1.0f, "-0.0f < -1.0f");
    Tester.checkEqual(floatLt(-0.0f, -0.0f), -0.0f < -0.0f, "-0.0f < -0.0f");
    Tester.checkEqual(floatLt(-0.0f, 0.0f), -0.0f < 0.0f, "-0.0f < 0.0f");
    Tester.checkEqual(floatLt(-0.0f, Float.MIN_VALUE), -0.0f < Float.MIN_VALUE, "-0.0f < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(-0.0f, 1.0f), -0.0f < 1.0f, "-0.0f < 1.0f");
    Tester.checkEqual(floatLt(-0.0f, Float.MAX_VALUE), -0.0f < Float.MAX_VALUE, "-0.0f < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(-0.0f, Float.POSITIVE_INFINITY), -0.0f < Float.POSITIVE_INFINITY, "-0.0f < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(-0.0f, Float.NaN), -0.0f < Float.NaN, "-0.0f < Float.NaN");
    Tester.checkEqual(floatLt(0.0f, Float.NEGATIVE_INFINITY), 0.0f < Float.NEGATIVE_INFINITY, "0.0f < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(0.0f, -1.0f), 0.0f < -1.0f, "0.0f < -1.0f");
    Tester.checkEqual(floatLt(0.0f, -0.0f), 0.0f < -0.0f, "0.0f < -0.0f");
    Tester.checkEqual(floatLt(0.0f, 0.0f), 0.0f < 0.0f, "0.0f < 0.0f");
    Tester.checkEqual(floatLt(0.0f, Float.MIN_VALUE), 0.0f < Float.MIN_VALUE, "0.0f < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(0.0f, 1.0f), 0.0f < 1.0f, "0.0f < 1.0f");
    Tester.checkEqual(floatLt(0.0f, Float.MAX_VALUE), 0.0f < Float.MAX_VALUE, "0.0f < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(0.0f, Float.POSITIVE_INFINITY), 0.0f < Float.POSITIVE_INFINITY, "0.0f < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(0.0f, Float.NaN), 0.0f < Float.NaN, "0.0f < Float.NaN");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE < Float.NEGATIVE_INFINITY, "Float.MIN_VALUE < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE < -1.0f, "Float.MIN_VALUE < -1.0f");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE < -0.0f, "Float.MIN_VALUE < -0.0f");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE < 0.0f, "Float.MIN_VALUE < 0.0f");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE < Float.MIN_VALUE, "Float.MIN_VALUE < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE < 1.0f, "Float.MIN_VALUE < 1.0f");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE < Float.MAX_VALUE, "Float.MIN_VALUE < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE < Float.POSITIVE_INFINITY, "Float.MIN_VALUE < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE < Float.NaN, "Float.MIN_VALUE < Float.NaN");
    Tester.checkEqual(floatLt(1.0f, Float.NEGATIVE_INFINITY), 1.0f < Float.NEGATIVE_INFINITY, "1.0f < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(1.0f, -1.0f), 1.0f < -1.0f, "1.0f < -1.0f");
    Tester.checkEqual(floatLt(1.0f, -0.0f), 1.0f < -0.0f, "1.0f < -0.0f");
    Tester.checkEqual(floatLt(1.0f, 0.0f), 1.0f < 0.0f, "1.0f < 0.0f");
    Tester.checkEqual(floatLt(1.0f, Float.MIN_VALUE), 1.0f < Float.MIN_VALUE, "1.0f < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(1.0f, 1.0f), 1.0f < 1.0f, "1.0f < 1.0f");
    Tester.checkEqual(floatLt(1.0f, Float.MAX_VALUE), 1.0f < Float.MAX_VALUE, "1.0f < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(1.0f, Float.POSITIVE_INFINITY), 1.0f < Float.POSITIVE_INFINITY, "1.0f < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(1.0f, Float.NaN), 1.0f < Float.NaN, "1.0f < Float.NaN");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE < Float.NEGATIVE_INFINITY, "Float.MAX_VALUE < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE < -1.0f, "Float.MAX_VALUE < -1.0f");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE < -0.0f, "Float.MAX_VALUE < -0.0f");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE < 0.0f, "Float.MAX_VALUE < 0.0f");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE < Float.MIN_VALUE, "Float.MAX_VALUE < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE < 1.0f, "Float.MAX_VALUE < 1.0f");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE < Float.MAX_VALUE, "Float.MAX_VALUE < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE < Float.POSITIVE_INFINITY, "Float.MAX_VALUE < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE < Float.NaN, "Float.MAX_VALUE < Float.NaN");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY < Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY < -1.0f, "Float.POSITIVE_INFINITY < -1.0f");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY < -0.0f, "Float.POSITIVE_INFINITY < -0.0f");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY < 0.0f, "Float.POSITIVE_INFINITY < 0.0f");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY < Float.MIN_VALUE, "Float.POSITIVE_INFINITY < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY < 1.0f, "Float.POSITIVE_INFINITY < 1.0f");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY < Float.MAX_VALUE, "Float.POSITIVE_INFINITY < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY < Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY < Float.NaN, "Float.POSITIVE_INFINITY < Float.NaN");
    Tester.checkEqual(floatLt(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN < Float.NEGATIVE_INFINITY, "Float.NaN < Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.NaN, -1.0f), Float.NaN < -1.0f, "Float.NaN < -1.0f");
    Tester.checkEqual(floatLt(Float.NaN, -0.0f), Float.NaN < -0.0f, "Float.NaN < -0.0f");
    Tester.checkEqual(floatLt(Float.NaN, 0.0f), Float.NaN < 0.0f, "Float.NaN < 0.0f");
    Tester.checkEqual(floatLt(Float.NaN, Float.MIN_VALUE), Float.NaN < Float.MIN_VALUE, "Float.NaN < Float.MIN_VALUE");
    Tester.checkEqual(floatLt(Float.NaN, 1.0f), Float.NaN < 1.0f, "Float.NaN < 1.0f");
    Tester.checkEqual(floatLt(Float.NaN, Float.MAX_VALUE), Float.NaN < Float.MAX_VALUE, "Float.NaN < Float.MAX_VALUE");
    Tester.checkEqual(floatLt(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN < Float.POSITIVE_INFINITY, "Float.NaN < Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLt(Float.NaN, Float.NaN), Float.NaN < Float.NaN, "Float.NaN < Float.NaN");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY > Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY > -1.0f, "Float.NEGATIVE_INFINITY > -1.0f");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY > -0.0f, "Float.NEGATIVE_INFINITY > -0.0f");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY > 0.0f, "Float.NEGATIVE_INFINITY > 0.0f");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY > Float.MIN_VALUE, "Float.NEGATIVE_INFINITY > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY > 1.0f, "Float.NEGATIVE_INFINITY > 1.0f");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY > Float.MAX_VALUE, "Float.NEGATIVE_INFINITY > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY > Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY > Float.NaN, "Float.NEGATIVE_INFINITY > Float.NaN");
    Tester.checkEqual(floatGt(-1.0f, Float.NEGATIVE_INFINITY), -1.0f > Float.NEGATIVE_INFINITY, "-1.0f > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(-1.0f, -1.0f), -1.0f > -1.0f, "-1.0f > -1.0f");
    Tester.checkEqual(floatGt(-1.0f, -0.0f), -1.0f > -0.0f, "-1.0f > -0.0f");
    Tester.checkEqual(floatGt(-1.0f, 0.0f), -1.0f > 0.0f, "-1.0f > 0.0f");
    Tester.checkEqual(floatGt(-1.0f, Float.MIN_VALUE), -1.0f > Float.MIN_VALUE, "-1.0f > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(-1.0f, 1.0f), -1.0f > 1.0f, "-1.0f > 1.0f");
    Tester.checkEqual(floatGt(-1.0f, Float.MAX_VALUE), -1.0f > Float.MAX_VALUE, "-1.0f > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(-1.0f, Float.POSITIVE_INFINITY), -1.0f > Float.POSITIVE_INFINITY, "-1.0f > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(-1.0f, Float.NaN), -1.0f > Float.NaN, "-1.0f > Float.NaN");
    Tester.checkEqual(floatGt(-0.0f, Float.NEGATIVE_INFINITY), -0.0f > Float.NEGATIVE_INFINITY, "-0.0f > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(-0.0f, -1.0f), -0.0f > -1.0f, "-0.0f > -1.0f");
    Tester.checkEqual(floatGt(-0.0f, -0.0f), -0.0f > -0.0f, "-0.0f > -0.0f");
    Tester.checkEqual(floatGt(-0.0f, 0.0f), -0.0f > 0.0f, "-0.0f > 0.0f");
    Tester.checkEqual(floatGt(-0.0f, Float.MIN_VALUE), -0.0f > Float.MIN_VALUE, "-0.0f > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(-0.0f, 1.0f), -0.0f > 1.0f, "-0.0f > 1.0f");
    Tester.checkEqual(floatGt(-0.0f, Float.MAX_VALUE), -0.0f > Float.MAX_VALUE, "-0.0f > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(-0.0f, Float.POSITIVE_INFINITY), -0.0f > Float.POSITIVE_INFINITY, "-0.0f > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(-0.0f, Float.NaN), -0.0f > Float.NaN, "-0.0f > Float.NaN");
    Tester.checkEqual(floatGt(0.0f, Float.NEGATIVE_INFINITY), 0.0f > Float.NEGATIVE_INFINITY, "0.0f > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(0.0f, -1.0f), 0.0f > -1.0f, "0.0f > -1.0f");
    Tester.checkEqual(floatGt(0.0f, -0.0f), 0.0f > -0.0f, "0.0f > -0.0f");
    Tester.checkEqual(floatGt(0.0f, 0.0f), 0.0f > 0.0f, "0.0f > 0.0f");
    Tester.checkEqual(floatGt(0.0f, Float.MIN_VALUE), 0.0f > Float.MIN_VALUE, "0.0f > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(0.0f, 1.0f), 0.0f > 1.0f, "0.0f > 1.0f");
    Tester.checkEqual(floatGt(0.0f, Float.MAX_VALUE), 0.0f > Float.MAX_VALUE, "0.0f > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(0.0f, Float.POSITIVE_INFINITY), 0.0f > Float.POSITIVE_INFINITY, "0.0f > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(0.0f, Float.NaN), 0.0f > Float.NaN, "0.0f > Float.NaN");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE > Float.NEGATIVE_INFINITY, "Float.MIN_VALUE > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE > -1.0f, "Float.MIN_VALUE > -1.0f");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE > -0.0f, "Float.MIN_VALUE > -0.0f");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE > 0.0f, "Float.MIN_VALUE > 0.0f");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE > Float.MIN_VALUE, "Float.MIN_VALUE > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE > 1.0f, "Float.MIN_VALUE > 1.0f");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE > Float.MAX_VALUE, "Float.MIN_VALUE > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE > Float.POSITIVE_INFINITY, "Float.MIN_VALUE > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE > Float.NaN, "Float.MIN_VALUE > Float.NaN");
    Tester.checkEqual(floatGt(1.0f, Float.NEGATIVE_INFINITY), 1.0f > Float.NEGATIVE_INFINITY, "1.0f > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(1.0f, -1.0f), 1.0f > -1.0f, "1.0f > -1.0f");
    Tester.checkEqual(floatGt(1.0f, -0.0f), 1.0f > -0.0f, "1.0f > -0.0f");
    Tester.checkEqual(floatGt(1.0f, 0.0f), 1.0f > 0.0f, "1.0f > 0.0f");
    Tester.checkEqual(floatGt(1.0f, Float.MIN_VALUE), 1.0f > Float.MIN_VALUE, "1.0f > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(1.0f, 1.0f), 1.0f > 1.0f, "1.0f > 1.0f");
    Tester.checkEqual(floatGt(1.0f, Float.MAX_VALUE), 1.0f > Float.MAX_VALUE, "1.0f > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(1.0f, Float.POSITIVE_INFINITY), 1.0f > Float.POSITIVE_INFINITY, "1.0f > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(1.0f, Float.NaN), 1.0f > Float.NaN, "1.0f > Float.NaN");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE > Float.NEGATIVE_INFINITY, "Float.MAX_VALUE > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE > -1.0f, "Float.MAX_VALUE > -1.0f");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE > -0.0f, "Float.MAX_VALUE > -0.0f");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE > 0.0f, "Float.MAX_VALUE > 0.0f");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE > Float.MIN_VALUE, "Float.MAX_VALUE > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE > 1.0f, "Float.MAX_VALUE > 1.0f");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE > Float.MAX_VALUE, "Float.MAX_VALUE > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE > Float.POSITIVE_INFINITY, "Float.MAX_VALUE > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE > Float.NaN, "Float.MAX_VALUE > Float.NaN");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY > Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY > -1.0f, "Float.POSITIVE_INFINITY > -1.0f");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY > -0.0f, "Float.POSITIVE_INFINITY > -0.0f");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY > 0.0f, "Float.POSITIVE_INFINITY > 0.0f");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY > Float.MIN_VALUE, "Float.POSITIVE_INFINITY > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY > 1.0f, "Float.POSITIVE_INFINITY > 1.0f");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY > Float.MAX_VALUE, "Float.POSITIVE_INFINITY > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY > Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY > Float.NaN, "Float.POSITIVE_INFINITY > Float.NaN");
    Tester.checkEqual(floatGt(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN > Float.NEGATIVE_INFINITY, "Float.NaN > Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.NaN, -1.0f), Float.NaN > -1.0f, "Float.NaN > -1.0f");
    Tester.checkEqual(floatGt(Float.NaN, -0.0f), Float.NaN > -0.0f, "Float.NaN > -0.0f");
    Tester.checkEqual(floatGt(Float.NaN, 0.0f), Float.NaN > 0.0f, "Float.NaN > 0.0f");
    Tester.checkEqual(floatGt(Float.NaN, Float.MIN_VALUE), Float.NaN > Float.MIN_VALUE, "Float.NaN > Float.MIN_VALUE");
    Tester.checkEqual(floatGt(Float.NaN, 1.0f), Float.NaN > 1.0f, "Float.NaN > 1.0f");
    Tester.checkEqual(floatGt(Float.NaN, Float.MAX_VALUE), Float.NaN > Float.MAX_VALUE, "Float.NaN > Float.MAX_VALUE");
    Tester.checkEqual(floatGt(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN > Float.POSITIVE_INFINITY, "Float.NaN > Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGt(Float.NaN, Float.NaN), Float.NaN > Float.NaN, "Float.NaN > Float.NaN");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY <= Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY <= -1.0f, "Float.NEGATIVE_INFINITY <= -1.0f");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY <= -0.0f, "Float.NEGATIVE_INFINITY <= -0.0f");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY <= 0.0f, "Float.NEGATIVE_INFINITY <= 0.0f");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY <= Float.MIN_VALUE, "Float.NEGATIVE_INFINITY <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY <= 1.0f, "Float.NEGATIVE_INFINITY <= 1.0f");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY <= Float.MAX_VALUE, "Float.NEGATIVE_INFINITY <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY <= Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY <= Float.NaN, "Float.NEGATIVE_INFINITY <= Float.NaN");
    Tester.checkEqual(floatLe(-1.0f, Float.NEGATIVE_INFINITY), -1.0f <= Float.NEGATIVE_INFINITY, "-1.0f <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(-1.0f, -1.0f), -1.0f <= -1.0f, "-1.0f <= -1.0f");
    Tester.checkEqual(floatLe(-1.0f, -0.0f), -1.0f <= -0.0f, "-1.0f <= -0.0f");
    Tester.checkEqual(floatLe(-1.0f, 0.0f), -1.0f <= 0.0f, "-1.0f <= 0.0f");
    Tester.checkEqual(floatLe(-1.0f, Float.MIN_VALUE), -1.0f <= Float.MIN_VALUE, "-1.0f <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(-1.0f, 1.0f), -1.0f <= 1.0f, "-1.0f <= 1.0f");
    Tester.checkEqual(floatLe(-1.0f, Float.MAX_VALUE), -1.0f <= Float.MAX_VALUE, "-1.0f <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(-1.0f, Float.POSITIVE_INFINITY), -1.0f <= Float.POSITIVE_INFINITY, "-1.0f <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(-1.0f, Float.NaN), -1.0f <= Float.NaN, "-1.0f <= Float.NaN");
    Tester.checkEqual(floatLe(-0.0f, Float.NEGATIVE_INFINITY), -0.0f <= Float.NEGATIVE_INFINITY, "-0.0f <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(-0.0f, -1.0f), -0.0f <= -1.0f, "-0.0f <= -1.0f");
    Tester.checkEqual(floatLe(-0.0f, -0.0f), -0.0f <= -0.0f, "-0.0f <= -0.0f");
    Tester.checkEqual(floatLe(-0.0f, 0.0f), -0.0f <= 0.0f, "-0.0f <= 0.0f");
    Tester.checkEqual(floatLe(-0.0f, Float.MIN_VALUE), -0.0f <= Float.MIN_VALUE, "-0.0f <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(-0.0f, 1.0f), -0.0f <= 1.0f, "-0.0f <= 1.0f");
    Tester.checkEqual(floatLe(-0.0f, Float.MAX_VALUE), -0.0f <= Float.MAX_VALUE, "-0.0f <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(-0.0f, Float.POSITIVE_INFINITY), -0.0f <= Float.POSITIVE_INFINITY, "-0.0f <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(-0.0f, Float.NaN), -0.0f <= Float.NaN, "-0.0f <= Float.NaN");
    Tester.checkEqual(floatLe(0.0f, Float.NEGATIVE_INFINITY), 0.0f <= Float.NEGATIVE_INFINITY, "0.0f <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(0.0f, -1.0f), 0.0f <= -1.0f, "0.0f <= -1.0f");
    Tester.checkEqual(floatLe(0.0f, -0.0f), 0.0f <= -0.0f, "0.0f <= -0.0f");
    Tester.checkEqual(floatLe(0.0f, 0.0f), 0.0f <= 0.0f, "0.0f <= 0.0f");
    Tester.checkEqual(floatLe(0.0f, Float.MIN_VALUE), 0.0f <= Float.MIN_VALUE, "0.0f <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(0.0f, 1.0f), 0.0f <= 1.0f, "0.0f <= 1.0f");
    Tester.checkEqual(floatLe(0.0f, Float.MAX_VALUE), 0.0f <= Float.MAX_VALUE, "0.0f <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(0.0f, Float.POSITIVE_INFINITY), 0.0f <= Float.POSITIVE_INFINITY, "0.0f <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(0.0f, Float.NaN), 0.0f <= Float.NaN, "0.0f <= Float.NaN");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE <= Float.NEGATIVE_INFINITY, "Float.MIN_VALUE <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE <= -1.0f, "Float.MIN_VALUE <= -1.0f");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE <= -0.0f, "Float.MIN_VALUE <= -0.0f");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE <= 0.0f, "Float.MIN_VALUE <= 0.0f");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE <= Float.MIN_VALUE, "Float.MIN_VALUE <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE <= 1.0f, "Float.MIN_VALUE <= 1.0f");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE <= Float.MAX_VALUE, "Float.MIN_VALUE <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE <= Float.POSITIVE_INFINITY, "Float.MIN_VALUE <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE <= Float.NaN, "Float.MIN_VALUE <= Float.NaN");
    Tester.checkEqual(floatLe(1.0f, Float.NEGATIVE_INFINITY), 1.0f <= Float.NEGATIVE_INFINITY, "1.0f <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(1.0f, -1.0f), 1.0f <= -1.0f, "1.0f <= -1.0f");
    Tester.checkEqual(floatLe(1.0f, -0.0f), 1.0f <= -0.0f, "1.0f <= -0.0f");
    Tester.checkEqual(floatLe(1.0f, 0.0f), 1.0f <= 0.0f, "1.0f <= 0.0f");
    Tester.checkEqual(floatLe(1.0f, Float.MIN_VALUE), 1.0f <= Float.MIN_VALUE, "1.0f <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(1.0f, 1.0f), 1.0f <= 1.0f, "1.0f <= 1.0f");
    Tester.checkEqual(floatLe(1.0f, Float.MAX_VALUE), 1.0f <= Float.MAX_VALUE, "1.0f <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(1.0f, Float.POSITIVE_INFINITY), 1.0f <= Float.POSITIVE_INFINITY, "1.0f <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(1.0f, Float.NaN), 1.0f <= Float.NaN, "1.0f <= Float.NaN");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE <= Float.NEGATIVE_INFINITY, "Float.MAX_VALUE <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE <= -1.0f, "Float.MAX_VALUE <= -1.0f");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE <= -0.0f, "Float.MAX_VALUE <= -0.0f");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE <= 0.0f, "Float.MAX_VALUE <= 0.0f");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE <= Float.MIN_VALUE, "Float.MAX_VALUE <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE <= 1.0f, "Float.MAX_VALUE <= 1.0f");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE <= Float.MAX_VALUE, "Float.MAX_VALUE <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE <= Float.POSITIVE_INFINITY, "Float.MAX_VALUE <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE <= Float.NaN, "Float.MAX_VALUE <= Float.NaN");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY <= Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY <= -1.0f, "Float.POSITIVE_INFINITY <= -1.0f");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY <= -0.0f, "Float.POSITIVE_INFINITY <= -0.0f");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY <= 0.0f, "Float.POSITIVE_INFINITY <= 0.0f");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY <= Float.MIN_VALUE, "Float.POSITIVE_INFINITY <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY <= 1.0f, "Float.POSITIVE_INFINITY <= 1.0f");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY <= Float.MAX_VALUE, "Float.POSITIVE_INFINITY <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY <= Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY <= Float.NaN, "Float.POSITIVE_INFINITY <= Float.NaN");
    Tester.checkEqual(floatLe(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN <= Float.NEGATIVE_INFINITY, "Float.NaN <= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.NaN, -1.0f), Float.NaN <= -1.0f, "Float.NaN <= -1.0f");
    Tester.checkEqual(floatLe(Float.NaN, -0.0f), Float.NaN <= -0.0f, "Float.NaN <= -0.0f");
    Tester.checkEqual(floatLe(Float.NaN, 0.0f), Float.NaN <= 0.0f, "Float.NaN <= 0.0f");
    Tester.checkEqual(floatLe(Float.NaN, Float.MIN_VALUE), Float.NaN <= Float.MIN_VALUE, "Float.NaN <= Float.MIN_VALUE");
    Tester.checkEqual(floatLe(Float.NaN, 1.0f), Float.NaN <= 1.0f, "Float.NaN <= 1.0f");
    Tester.checkEqual(floatLe(Float.NaN, Float.MAX_VALUE), Float.NaN <= Float.MAX_VALUE, "Float.NaN <= Float.MAX_VALUE");
    Tester.checkEqual(floatLe(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN <= Float.POSITIVE_INFINITY, "Float.NaN <= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatLe(Float.NaN, Float.NaN), Float.NaN <= Float.NaN, "Float.NaN <= Float.NaN");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY >= Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY >= -1.0f, "Float.NEGATIVE_INFINITY >= -1.0f");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY >= -0.0f, "Float.NEGATIVE_INFINITY >= -0.0f");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY >= 0.0f, "Float.NEGATIVE_INFINITY >= 0.0f");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY >= Float.MIN_VALUE, "Float.NEGATIVE_INFINITY >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY >= 1.0f, "Float.NEGATIVE_INFINITY >= 1.0f");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY >= Float.MAX_VALUE, "Float.NEGATIVE_INFINITY >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY >= Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY >= Float.NaN, "Float.NEGATIVE_INFINITY >= Float.NaN");
    Tester.checkEqual(floatGe(-1.0f, Float.NEGATIVE_INFINITY), -1.0f >= Float.NEGATIVE_INFINITY, "-1.0f >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(-1.0f, -1.0f), -1.0f >= -1.0f, "-1.0f >= -1.0f");
    Tester.checkEqual(floatGe(-1.0f, -0.0f), -1.0f >= -0.0f, "-1.0f >= -0.0f");
    Tester.checkEqual(floatGe(-1.0f, 0.0f), -1.0f >= 0.0f, "-1.0f >= 0.0f");
    Tester.checkEqual(floatGe(-1.0f, Float.MIN_VALUE), -1.0f >= Float.MIN_VALUE, "-1.0f >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(-1.0f, 1.0f), -1.0f >= 1.0f, "-1.0f >= 1.0f");
    Tester.checkEqual(floatGe(-1.0f, Float.MAX_VALUE), -1.0f >= Float.MAX_VALUE, "-1.0f >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(-1.0f, Float.POSITIVE_INFINITY), -1.0f >= Float.POSITIVE_INFINITY, "-1.0f >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(-1.0f, Float.NaN), -1.0f >= Float.NaN, "-1.0f >= Float.NaN");
    Tester.checkEqual(floatGe(-0.0f, Float.NEGATIVE_INFINITY), -0.0f >= Float.NEGATIVE_INFINITY, "-0.0f >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(-0.0f, -1.0f), -0.0f >= -1.0f, "-0.0f >= -1.0f");
    Tester.checkEqual(floatGe(-0.0f, -0.0f), -0.0f >= -0.0f, "-0.0f >= -0.0f");
    Tester.checkEqual(floatGe(-0.0f, 0.0f), -0.0f >= 0.0f, "-0.0f >= 0.0f");
    Tester.checkEqual(floatGe(-0.0f, Float.MIN_VALUE), -0.0f >= Float.MIN_VALUE, "-0.0f >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(-0.0f, 1.0f), -0.0f >= 1.0f, "-0.0f >= 1.0f");
    Tester.checkEqual(floatGe(-0.0f, Float.MAX_VALUE), -0.0f >= Float.MAX_VALUE, "-0.0f >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(-0.0f, Float.POSITIVE_INFINITY), -0.0f >= Float.POSITIVE_INFINITY, "-0.0f >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(-0.0f, Float.NaN), -0.0f >= Float.NaN, "-0.0f >= Float.NaN");
    Tester.checkEqual(floatGe(0.0f, Float.NEGATIVE_INFINITY), 0.0f >= Float.NEGATIVE_INFINITY, "0.0f >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(0.0f, -1.0f), 0.0f >= -1.0f, "0.0f >= -1.0f");
    Tester.checkEqual(floatGe(0.0f, -0.0f), 0.0f >= -0.0f, "0.0f >= -0.0f");
    Tester.checkEqual(floatGe(0.0f, 0.0f), 0.0f >= 0.0f, "0.0f >= 0.0f");
    Tester.checkEqual(floatGe(0.0f, Float.MIN_VALUE), 0.0f >= Float.MIN_VALUE, "0.0f >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(0.0f, 1.0f), 0.0f >= 1.0f, "0.0f >= 1.0f");
    Tester.checkEqual(floatGe(0.0f, Float.MAX_VALUE), 0.0f >= Float.MAX_VALUE, "0.0f >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(0.0f, Float.POSITIVE_INFINITY), 0.0f >= Float.POSITIVE_INFINITY, "0.0f >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(0.0f, Float.NaN), 0.0f >= Float.NaN, "0.0f >= Float.NaN");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE >= Float.NEGATIVE_INFINITY, "Float.MIN_VALUE >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE >= -1.0f, "Float.MIN_VALUE >= -1.0f");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE >= -0.0f, "Float.MIN_VALUE >= -0.0f");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE >= 0.0f, "Float.MIN_VALUE >= 0.0f");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE >= Float.MIN_VALUE, "Float.MIN_VALUE >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE >= 1.0f, "Float.MIN_VALUE >= 1.0f");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE >= Float.MAX_VALUE, "Float.MIN_VALUE >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE >= Float.POSITIVE_INFINITY, "Float.MIN_VALUE >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE >= Float.NaN, "Float.MIN_VALUE >= Float.NaN");
    Tester.checkEqual(floatGe(1.0f, Float.NEGATIVE_INFINITY), 1.0f >= Float.NEGATIVE_INFINITY, "1.0f >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(1.0f, -1.0f), 1.0f >= -1.0f, "1.0f >= -1.0f");
    Tester.checkEqual(floatGe(1.0f, -0.0f), 1.0f >= -0.0f, "1.0f >= -0.0f");
    Tester.checkEqual(floatGe(1.0f, 0.0f), 1.0f >= 0.0f, "1.0f >= 0.0f");
    Tester.checkEqual(floatGe(1.0f, Float.MIN_VALUE), 1.0f >= Float.MIN_VALUE, "1.0f >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(1.0f, 1.0f), 1.0f >= 1.0f, "1.0f >= 1.0f");
    Tester.checkEqual(floatGe(1.0f, Float.MAX_VALUE), 1.0f >= Float.MAX_VALUE, "1.0f >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(1.0f, Float.POSITIVE_INFINITY), 1.0f >= Float.POSITIVE_INFINITY, "1.0f >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(1.0f, Float.NaN), 1.0f >= Float.NaN, "1.0f >= Float.NaN");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE >= Float.NEGATIVE_INFINITY, "Float.MAX_VALUE >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE >= -1.0f, "Float.MAX_VALUE >= -1.0f");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE >= -0.0f, "Float.MAX_VALUE >= -0.0f");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE >= 0.0f, "Float.MAX_VALUE >= 0.0f");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE >= Float.MIN_VALUE, "Float.MAX_VALUE >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE >= 1.0f, "Float.MAX_VALUE >= 1.0f");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE >= Float.MAX_VALUE, "Float.MAX_VALUE >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE >= Float.POSITIVE_INFINITY, "Float.MAX_VALUE >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE >= Float.NaN, "Float.MAX_VALUE >= Float.NaN");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY >= Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY >= -1.0f, "Float.POSITIVE_INFINITY >= -1.0f");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY >= -0.0f, "Float.POSITIVE_INFINITY >= -0.0f");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY >= 0.0f, "Float.POSITIVE_INFINITY >= 0.0f");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY >= Float.MIN_VALUE, "Float.POSITIVE_INFINITY >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY >= 1.0f, "Float.POSITIVE_INFINITY >= 1.0f");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY >= Float.MAX_VALUE, "Float.POSITIVE_INFINITY >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY >= Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY >= Float.NaN, "Float.POSITIVE_INFINITY >= Float.NaN");
    Tester.checkEqual(floatGe(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN >= Float.NEGATIVE_INFINITY, "Float.NaN >= Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.NaN, -1.0f), Float.NaN >= -1.0f, "Float.NaN >= -1.0f");
    Tester.checkEqual(floatGe(Float.NaN, -0.0f), Float.NaN >= -0.0f, "Float.NaN >= -0.0f");
    Tester.checkEqual(floatGe(Float.NaN, 0.0f), Float.NaN >= 0.0f, "Float.NaN >= 0.0f");
    Tester.checkEqual(floatGe(Float.NaN, Float.MIN_VALUE), Float.NaN >= Float.MIN_VALUE, "Float.NaN >= Float.MIN_VALUE");
    Tester.checkEqual(floatGe(Float.NaN, 1.0f), Float.NaN >= 1.0f, "Float.NaN >= 1.0f");
    Tester.checkEqual(floatGe(Float.NaN, Float.MAX_VALUE), Float.NaN >= Float.MAX_VALUE, "Float.NaN >= Float.MAX_VALUE");
    Tester.checkEqual(floatGe(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN >= Float.POSITIVE_INFINITY, "Float.NaN >= Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatGe(Float.NaN, Float.NaN), Float.NaN >= Float.NaN, "Float.NaN >= Float.NaN");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY == Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY == -1.0f, "Float.NEGATIVE_INFINITY == -1.0f");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY == -0.0f, "Float.NEGATIVE_INFINITY == -0.0f");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY == 0.0f, "Float.NEGATIVE_INFINITY == 0.0f");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY == Float.MIN_VALUE, "Float.NEGATIVE_INFINITY == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY == 1.0f, "Float.NEGATIVE_INFINITY == 1.0f");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY == Float.MAX_VALUE, "Float.NEGATIVE_INFINITY == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY == Float.NaN, "Float.NEGATIVE_INFINITY == Float.NaN");
    Tester.checkEqual(floatEq(-1.0f, Float.NEGATIVE_INFINITY), -1.0f == Float.NEGATIVE_INFINITY, "-1.0f == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(-1.0f, -1.0f), -1.0f == -1.0f, "-1.0f == -1.0f");
    Tester.checkEqual(floatEq(-1.0f, -0.0f), -1.0f == -0.0f, "-1.0f == -0.0f");
    Tester.checkEqual(floatEq(-1.0f, 0.0f), -1.0f == 0.0f, "-1.0f == 0.0f");
    Tester.checkEqual(floatEq(-1.0f, Float.MIN_VALUE), -1.0f == Float.MIN_VALUE, "-1.0f == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(-1.0f, 1.0f), -1.0f == 1.0f, "-1.0f == 1.0f");
    Tester.checkEqual(floatEq(-1.0f, Float.MAX_VALUE), -1.0f == Float.MAX_VALUE, "-1.0f == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(-1.0f, Float.POSITIVE_INFINITY), -1.0f == Float.POSITIVE_INFINITY, "-1.0f == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(-1.0f, Float.NaN), -1.0f == Float.NaN, "-1.0f == Float.NaN");
    Tester.checkEqual(floatEq(-0.0f, Float.NEGATIVE_INFINITY), -0.0f == Float.NEGATIVE_INFINITY, "-0.0f == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(-0.0f, -1.0f), -0.0f == -1.0f, "-0.0f == -1.0f");
    Tester.checkEqual(floatEq(-0.0f, -0.0f), -0.0f == -0.0f, "-0.0f == -0.0f");
    Tester.checkEqual(floatEq(-0.0f, 0.0f), -0.0f == 0.0f, "-0.0f == 0.0f");
    Tester.checkEqual(floatEq(-0.0f, Float.MIN_VALUE), -0.0f == Float.MIN_VALUE, "-0.0f == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(-0.0f, 1.0f), -0.0f == 1.0f, "-0.0f == 1.0f");
    Tester.checkEqual(floatEq(-0.0f, Float.MAX_VALUE), -0.0f == Float.MAX_VALUE, "-0.0f == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(-0.0f, Float.POSITIVE_INFINITY), -0.0f == Float.POSITIVE_INFINITY, "-0.0f == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(-0.0f, Float.NaN), -0.0f == Float.NaN, "-0.0f == Float.NaN");
    Tester.checkEqual(floatEq(0.0f, Float.NEGATIVE_INFINITY), 0.0f == Float.NEGATIVE_INFINITY, "0.0f == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(0.0f, -1.0f), 0.0f == -1.0f, "0.0f == -1.0f");
    Tester.checkEqual(floatEq(0.0f, -0.0f), 0.0f == -0.0f, "0.0f == -0.0f");
    Tester.checkEqual(floatEq(0.0f, 0.0f), 0.0f == 0.0f, "0.0f == 0.0f");
    Tester.checkEqual(floatEq(0.0f, Float.MIN_VALUE), 0.0f == Float.MIN_VALUE, "0.0f == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(0.0f, 1.0f), 0.0f == 1.0f, "0.0f == 1.0f");
    Tester.checkEqual(floatEq(0.0f, Float.MAX_VALUE), 0.0f == Float.MAX_VALUE, "0.0f == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(0.0f, Float.POSITIVE_INFINITY), 0.0f == Float.POSITIVE_INFINITY, "0.0f == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(0.0f, Float.NaN), 0.0f == Float.NaN, "0.0f == Float.NaN");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE == Float.NEGATIVE_INFINITY, "Float.MIN_VALUE == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE == -1.0f, "Float.MIN_VALUE == -1.0f");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE == -0.0f, "Float.MIN_VALUE == -0.0f");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE == 0.0f, "Float.MIN_VALUE == 0.0f");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE == Float.MIN_VALUE, "Float.MIN_VALUE == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE == 1.0f, "Float.MIN_VALUE == 1.0f");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE == Float.MAX_VALUE, "Float.MIN_VALUE == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE == Float.POSITIVE_INFINITY, "Float.MIN_VALUE == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE == Float.NaN, "Float.MIN_VALUE == Float.NaN");
    Tester.checkEqual(floatEq(1.0f, Float.NEGATIVE_INFINITY), 1.0f == Float.NEGATIVE_INFINITY, "1.0f == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(1.0f, -1.0f), 1.0f == -1.0f, "1.0f == -1.0f");
    Tester.checkEqual(floatEq(1.0f, -0.0f), 1.0f == -0.0f, "1.0f == -0.0f");
    Tester.checkEqual(floatEq(1.0f, 0.0f), 1.0f == 0.0f, "1.0f == 0.0f");
    Tester.checkEqual(floatEq(1.0f, Float.MIN_VALUE), 1.0f == Float.MIN_VALUE, "1.0f == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(1.0f, 1.0f), 1.0f == 1.0f, "1.0f == 1.0f");
    Tester.checkEqual(floatEq(1.0f, Float.MAX_VALUE), 1.0f == Float.MAX_VALUE, "1.0f == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(1.0f, Float.POSITIVE_INFINITY), 1.0f == Float.POSITIVE_INFINITY, "1.0f == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(1.0f, Float.NaN), 1.0f == Float.NaN, "1.0f == Float.NaN");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE == Float.NEGATIVE_INFINITY, "Float.MAX_VALUE == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE == -1.0f, "Float.MAX_VALUE == -1.0f");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE == -0.0f, "Float.MAX_VALUE == -0.0f");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE == 0.0f, "Float.MAX_VALUE == 0.0f");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE == Float.MIN_VALUE, "Float.MAX_VALUE == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE == 1.0f, "Float.MAX_VALUE == 1.0f");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE == Float.MAX_VALUE, "Float.MAX_VALUE == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE == Float.POSITIVE_INFINITY, "Float.MAX_VALUE == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE == Float.NaN, "Float.MAX_VALUE == Float.NaN");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY == -1.0f, "Float.POSITIVE_INFINITY == -1.0f");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY == -0.0f, "Float.POSITIVE_INFINITY == -0.0f");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY == 0.0f, "Float.POSITIVE_INFINITY == 0.0f");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY == Float.MIN_VALUE, "Float.POSITIVE_INFINITY == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY == 1.0f, "Float.POSITIVE_INFINITY == 1.0f");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY == Float.MAX_VALUE, "Float.POSITIVE_INFINITY == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY == Float.NaN, "Float.POSITIVE_INFINITY == Float.NaN");
    Tester.checkEqual(floatEq(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN == Float.NEGATIVE_INFINITY, "Float.NaN == Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.NaN, -1.0f), Float.NaN == -1.0f, "Float.NaN == -1.0f");
    Tester.checkEqual(floatEq(Float.NaN, -0.0f), Float.NaN == -0.0f, "Float.NaN == -0.0f");
    Tester.checkEqual(floatEq(Float.NaN, 0.0f), Float.NaN == 0.0f, "Float.NaN == 0.0f");
    Tester.checkEqual(floatEq(Float.NaN, Float.MIN_VALUE), Float.NaN == Float.MIN_VALUE, "Float.NaN == Float.MIN_VALUE");
    Tester.checkEqual(floatEq(Float.NaN, 1.0f), Float.NaN == 1.0f, "Float.NaN == 1.0f");
    Tester.checkEqual(floatEq(Float.NaN, Float.MAX_VALUE), Float.NaN == Float.MAX_VALUE, "Float.NaN == Float.MAX_VALUE");
    Tester.checkEqual(floatEq(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN == Float.POSITIVE_INFINITY, "Float.NaN == Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatEq(Float.NaN, Float.NaN), Float.NaN == Float.NaN, "Float.NaN == Float.NaN");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY != Float.NEGATIVE_INFINITY, "Float.NEGATIVE_INFINITY != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, -1.0f), Float.NEGATIVE_INFINITY != -1.0f, "Float.NEGATIVE_INFINITY != -1.0f");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, -0.0f), Float.NEGATIVE_INFINITY != -0.0f, "Float.NEGATIVE_INFINITY != -0.0f");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, 0.0f), Float.NEGATIVE_INFINITY != 0.0f, "Float.NEGATIVE_INFINITY != 0.0f");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, Float.MIN_VALUE), Float.NEGATIVE_INFINITY != Float.MIN_VALUE, "Float.NEGATIVE_INFINITY != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, 1.0f), Float.NEGATIVE_INFINITY != 1.0f, "Float.NEGATIVE_INFINITY != 1.0f");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, Float.MAX_VALUE), Float.NEGATIVE_INFINITY != Float.MAX_VALUE, "Float.NEGATIVE_INFINITY != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY), Float.NEGATIVE_INFINITY != Float.POSITIVE_INFINITY, "Float.NEGATIVE_INFINITY != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.NEGATIVE_INFINITY, Float.NaN), Float.NEGATIVE_INFINITY != Float.NaN, "Float.NEGATIVE_INFINITY != Float.NaN");
    Tester.checkEqual(floatNe(-1.0f, Float.NEGATIVE_INFINITY), -1.0f != Float.NEGATIVE_INFINITY, "-1.0f != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(-1.0f, -1.0f), -1.0f != -1.0f, "-1.0f != -1.0f");
    Tester.checkEqual(floatNe(-1.0f, -0.0f), -1.0f != -0.0f, "-1.0f != -0.0f");
    Tester.checkEqual(floatNe(-1.0f, 0.0f), -1.0f != 0.0f, "-1.0f != 0.0f");
    Tester.checkEqual(floatNe(-1.0f, Float.MIN_VALUE), -1.0f != Float.MIN_VALUE, "-1.0f != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(-1.0f, 1.0f), -1.0f != 1.0f, "-1.0f != 1.0f");
    Tester.checkEqual(floatNe(-1.0f, Float.MAX_VALUE), -1.0f != Float.MAX_VALUE, "-1.0f != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(-1.0f, Float.POSITIVE_INFINITY), -1.0f != Float.POSITIVE_INFINITY, "-1.0f != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(-1.0f, Float.NaN), -1.0f != Float.NaN, "-1.0f != Float.NaN");
    Tester.checkEqual(floatNe(-0.0f, Float.NEGATIVE_INFINITY), -0.0f != Float.NEGATIVE_INFINITY, "-0.0f != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(-0.0f, -1.0f), -0.0f != -1.0f, "-0.0f != -1.0f");
    Tester.checkEqual(floatNe(-0.0f, -0.0f), -0.0f != -0.0f, "-0.0f != -0.0f");
    Tester.checkEqual(floatNe(-0.0f, 0.0f), -0.0f != 0.0f, "-0.0f != 0.0f");
    Tester.checkEqual(floatNe(-0.0f, Float.MIN_VALUE), -0.0f != Float.MIN_VALUE, "-0.0f != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(-0.0f, 1.0f), -0.0f != 1.0f, "-0.0f != 1.0f");
    Tester.checkEqual(floatNe(-0.0f, Float.MAX_VALUE), -0.0f != Float.MAX_VALUE, "-0.0f != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(-0.0f, Float.POSITIVE_INFINITY), -0.0f != Float.POSITIVE_INFINITY, "-0.0f != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(-0.0f, Float.NaN), -0.0f != Float.NaN, "-0.0f != Float.NaN");
    Tester.checkEqual(floatNe(0.0f, Float.NEGATIVE_INFINITY), 0.0f != Float.NEGATIVE_INFINITY, "0.0f != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(0.0f, -1.0f), 0.0f != -1.0f, "0.0f != -1.0f");
    Tester.checkEqual(floatNe(0.0f, -0.0f), 0.0f != -0.0f, "0.0f != -0.0f");
    Tester.checkEqual(floatNe(0.0f, 0.0f), 0.0f != 0.0f, "0.0f != 0.0f");
    Tester.checkEqual(floatNe(0.0f, Float.MIN_VALUE), 0.0f != Float.MIN_VALUE, "0.0f != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(0.0f, 1.0f), 0.0f != 1.0f, "0.0f != 1.0f");
    Tester.checkEqual(floatNe(0.0f, Float.MAX_VALUE), 0.0f != Float.MAX_VALUE, "0.0f != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(0.0f, Float.POSITIVE_INFINITY), 0.0f != Float.POSITIVE_INFINITY, "0.0f != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(0.0f, Float.NaN), 0.0f != Float.NaN, "0.0f != Float.NaN");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, Float.NEGATIVE_INFINITY), Float.MIN_VALUE != Float.NEGATIVE_INFINITY, "Float.MIN_VALUE != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, -1.0f), Float.MIN_VALUE != -1.0f, "Float.MIN_VALUE != -1.0f");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, -0.0f), Float.MIN_VALUE != -0.0f, "Float.MIN_VALUE != -0.0f");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, 0.0f), Float.MIN_VALUE != 0.0f, "Float.MIN_VALUE != 0.0f");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, Float.MIN_VALUE), Float.MIN_VALUE != Float.MIN_VALUE, "Float.MIN_VALUE != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, 1.0f), Float.MIN_VALUE != 1.0f, "Float.MIN_VALUE != 1.0f");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, Float.MAX_VALUE), Float.MIN_VALUE != Float.MAX_VALUE, "Float.MIN_VALUE != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, Float.POSITIVE_INFINITY), Float.MIN_VALUE != Float.POSITIVE_INFINITY, "Float.MIN_VALUE != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.MIN_VALUE, Float.NaN), Float.MIN_VALUE != Float.NaN, "Float.MIN_VALUE != Float.NaN");
    Tester.checkEqual(floatNe(1.0f, Float.NEGATIVE_INFINITY), 1.0f != Float.NEGATIVE_INFINITY, "1.0f != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(1.0f, -1.0f), 1.0f != -1.0f, "1.0f != -1.0f");
    Tester.checkEqual(floatNe(1.0f, -0.0f), 1.0f != -0.0f, "1.0f != -0.0f");
    Tester.checkEqual(floatNe(1.0f, 0.0f), 1.0f != 0.0f, "1.0f != 0.0f");
    Tester.checkEqual(floatNe(1.0f, Float.MIN_VALUE), 1.0f != Float.MIN_VALUE, "1.0f != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(1.0f, 1.0f), 1.0f != 1.0f, "1.0f != 1.0f");
    Tester.checkEqual(floatNe(1.0f, Float.MAX_VALUE), 1.0f != Float.MAX_VALUE, "1.0f != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(1.0f, Float.POSITIVE_INFINITY), 1.0f != Float.POSITIVE_INFINITY, "1.0f != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(1.0f, Float.NaN), 1.0f != Float.NaN, "1.0f != Float.NaN");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, Float.NEGATIVE_INFINITY), Float.MAX_VALUE != Float.NEGATIVE_INFINITY, "Float.MAX_VALUE != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, -1.0f), Float.MAX_VALUE != -1.0f, "Float.MAX_VALUE != -1.0f");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, -0.0f), Float.MAX_VALUE != -0.0f, "Float.MAX_VALUE != -0.0f");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, 0.0f), Float.MAX_VALUE != 0.0f, "Float.MAX_VALUE != 0.0f");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE != Float.MIN_VALUE, "Float.MAX_VALUE != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, 1.0f), Float.MAX_VALUE != 1.0f, "Float.MAX_VALUE != 1.0f");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, Float.MAX_VALUE), Float.MAX_VALUE != Float.MAX_VALUE, "Float.MAX_VALUE != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, Float.POSITIVE_INFINITY), Float.MAX_VALUE != Float.POSITIVE_INFINITY, "Float.MAX_VALUE != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.MAX_VALUE, Float.NaN), Float.MAX_VALUE != Float.NaN, "Float.MAX_VALUE != Float.NaN");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY), Float.POSITIVE_INFINITY != Float.NEGATIVE_INFINITY, "Float.POSITIVE_INFINITY != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, -1.0f), Float.POSITIVE_INFINITY != -1.0f, "Float.POSITIVE_INFINITY != -1.0f");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, -0.0f), Float.POSITIVE_INFINITY != -0.0f, "Float.POSITIVE_INFINITY != -0.0f");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, 0.0f), Float.POSITIVE_INFINITY != 0.0f, "Float.POSITIVE_INFINITY != 0.0f");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, Float.MIN_VALUE), Float.POSITIVE_INFINITY != Float.MIN_VALUE, "Float.POSITIVE_INFINITY != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, 1.0f), Float.POSITIVE_INFINITY != 1.0f, "Float.POSITIVE_INFINITY != 1.0f");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, Float.MAX_VALUE), Float.POSITIVE_INFINITY != Float.MAX_VALUE, "Float.POSITIVE_INFINITY != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY != Float.POSITIVE_INFINITY, "Float.POSITIVE_INFINITY != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.POSITIVE_INFINITY, Float.NaN), Float.POSITIVE_INFINITY != Float.NaN, "Float.POSITIVE_INFINITY != Float.NaN");
    Tester.checkEqual(floatNe(Float.NaN, Float.NEGATIVE_INFINITY), Float.NaN != Float.NEGATIVE_INFINITY, "Float.NaN != Float.NEGATIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.NaN, -1.0f), Float.NaN != -1.0f, "Float.NaN != -1.0f");
    Tester.checkEqual(floatNe(Float.NaN, -0.0f), Float.NaN != -0.0f, "Float.NaN != -0.0f");
    Tester.checkEqual(floatNe(Float.NaN, 0.0f), Float.NaN != 0.0f, "Float.NaN != 0.0f");
    Tester.checkEqual(floatNe(Float.NaN, Float.MIN_VALUE), Float.NaN != Float.MIN_VALUE, "Float.NaN != Float.MIN_VALUE");
    Tester.checkEqual(floatNe(Float.NaN, 1.0f), Float.NaN != 1.0f, "Float.NaN != 1.0f");
    Tester.checkEqual(floatNe(Float.NaN, Float.MAX_VALUE), Float.NaN != Float.MAX_VALUE, "Float.NaN != Float.MAX_VALUE");
    Tester.checkEqual(floatNe(Float.NaN, Float.POSITIVE_INFINITY), Float.NaN != Float.POSITIVE_INFINITY, "Float.NaN != Float.POSITIVE_INFINITY");
    Tester.checkEqual(floatNe(Float.NaN, Float.NaN), Float.NaN != Float.NaN, "Float.NaN != Float.NaN");
  }
  static void floatSwitch() {
    switch(0) {
      case ((((float) + Float.NEGATIVE_INFINITY) == 0) ? 0 : 0):
      case ((((float) + -1.0f) == 0) ? 1 : 1):
      case ((((float) + -0.0f) == 0) ? 2 : 2):
      case ((((float) + 0.0f) == 0) ? 3 : 3):
      case ((((float) + Float.MIN_VALUE) == 0) ? 4 : 4):
      case ((((float) + 1.0f) == 0) ? 5 : 5):
      case ((((float) + Float.MAX_VALUE) == 0) ? 6 : 6):
      case ((((float) + Float.POSITIVE_INFINITY) == 0) ? 7 : 7):
      case ((((float) + Float.NaN) == 0) ? 8 : 8):
      case ((((float) - Float.NEGATIVE_INFINITY) == 0) ? 9 : 9):
      case ((((float) - -1.0f) == 0) ? 10 : 10):
      case ((((float) - -0.0f) == 0) ? 11 : 11):
      case ((((float) - 0.0f) == 0) ? 12 : 12):
      case ((((float) - Float.MIN_VALUE) == 0) ? 13 : 13):
      case ((((float) - 1.0f) == 0) ? 14 : 14):
      case ((((float) - Float.MAX_VALUE) == 0) ? 15 : 15):
      case ((((float) - Float.POSITIVE_INFINITY) == 0) ? 16 : 16):
      case ((((float) - Float.NaN) == 0) ? 17 : 17):
      case ((((float) (Float.NEGATIVE_INFINITY * Float.NEGATIVE_INFINITY)) == 0) ? 18 : 18):
      case ((((float) (Float.NEGATIVE_INFINITY * -1.0f)) == 0) ? 19 : 19):
      case ((((float) (Float.NEGATIVE_INFINITY * -0.0f)) == 0) ? 20 : 20):
      case ((((float) (Float.NEGATIVE_INFINITY * 0.0f)) == 0) ? 21 : 21):
      case ((((float) (Float.NEGATIVE_INFINITY * Float.MIN_VALUE)) == 0) ? 22 : 22):
      case ((((float) (Float.NEGATIVE_INFINITY * 1.0f)) == 0) ? 23 : 23):
      case ((((float) (Float.NEGATIVE_INFINITY * Float.MAX_VALUE)) == 0) ? 24 : 24):
      case ((((float) (Float.NEGATIVE_INFINITY * Float.POSITIVE_INFINITY)) == 0) ? 25 : 25):
      case ((((float) (Float.NEGATIVE_INFINITY * Float.NaN)) == 0) ? 26 : 26):
      case ((((float) (-1.0f * Float.NEGATIVE_INFINITY)) == 0) ? 27 : 27):
      case ((((float) (-1.0f * -1.0f)) == 0) ? 28 : 28):
      case ((((float) (-1.0f * -0.0f)) == 0) ? 29 : 29):
      case ((((float) (-1.0f * 0.0f)) == 0) ? 30 : 30):
      case ((((float) (-1.0f * Float.MIN_VALUE)) == 0) ? 31 : 31):
      case ((((float) (-1.0f * 1.0f)) == 0) ? 32 : 32):
      case ((((float) (-1.0f * Float.MAX_VALUE)) == 0) ? 33 : 33):
      case ((((float) (-1.0f * Float.POSITIVE_INFINITY)) == 0) ? 34 : 34):
      case ((((float) (-1.0f * Float.NaN)) == 0) ? 35 : 35):
      case ((((float) (-0.0f * Float.NEGATIVE_INFINITY)) == 0) ? 36 : 36):
      case ((((float) (-0.0f * -1.0f)) == 0) ? 37 : 37):
      case ((((float) (-0.0f * -0.0f)) == 0) ? 38 : 38):
      case ((((float) (-0.0f * 0.0f)) == 0) ? 39 : 39):
      case ((((float) (-0.0f * Float.MIN_VALUE)) == 0) ? 40 : 40):
      case ((((float) (-0.0f * 1.0f)) == 0) ? 41 : 41):
      case ((((float) (-0.0f * Float.MAX_VALUE)) == 0) ? 42 : 42):
      case ((((float) (-0.0f * Float.POSITIVE_INFINITY)) == 0) ? 43 : 43):
      case ((((float) (-0.0f * Float.NaN)) == 0) ? 44 : 44):
      case ((((float) (0.0f * Float.NEGATIVE_INFINITY)) == 0) ? 45 : 45):
      case ((((float) (0.0f * -1.0f)) == 0) ? 46 : 46):
      case ((((float) (0.0f * -0.0f)) == 0) ? 47 : 47):
      case ((((float) (0.0f * 0.0f)) == 0) ? 48 : 48):
      case ((((float) (0.0f * Float.MIN_VALUE)) == 0) ? 49 : 49):
      case ((((float) (0.0f * 1.0f)) == 0) ? 50 : 50):
      case ((((float) (0.0f * Float.MAX_VALUE)) == 0) ? 51 : 51):
      case ((((float) (0.0f * Float.POSITIVE_INFINITY)) == 0) ? 52 : 52):
      case ((((float) (0.0f * Float.NaN)) == 0) ? 53 : 53):
      case ((((float) (Float.MIN_VALUE * Float.NEGATIVE_INFINITY)) == 0) ? 54 : 54):
      case ((((float) (Float.MIN_VALUE * -1.0f)) == 0) ? 55 : 55):
      case ((((float) (Float.MIN_VALUE * -0.0f)) == 0) ? 56 : 56):
      case ((((float) (Float.MIN_VALUE * 0.0f)) == 0) ? 57 : 57):
      case ((((float) (Float.MIN_VALUE * Float.MIN_VALUE)) == 0) ? 58 : 58):
      case ((((float) (Float.MIN_VALUE * 1.0f)) == 0) ? 59 : 59):
      case ((((float) (Float.MIN_VALUE * Float.MAX_VALUE)) == 0) ? 60 : 60):
      case ((((float) (Float.MIN_VALUE * Float.POSITIVE_INFINITY)) == 0) ? 61 : 61):
      case ((((float) (Float.MIN_VALUE * Float.NaN)) == 0) ? 62 : 62):
      case ((((float) (1.0f * Float.NEGATIVE_INFINITY)) == 0) ? 63 : 63):
      case ((((float) (1.0f * -1.0f)) == 0) ? 64 : 64):
      case ((((float) (1.0f * -0.0f)) == 0) ? 65 : 65):
      case ((((float) (1.0f * 0.0f)) == 0) ? 66 : 66):
      case ((((float) (1.0f * Float.MIN_VALUE)) == 0) ? 67 : 67):
      case ((((float) (1.0f * 1.0f)) == 0) ? 68 : 68):
      case ((((float) (1.0f * Float.MAX_VALUE)) == 0) ? 69 : 69):
      case ((((float) (1.0f * Float.POSITIVE_INFINITY)) == 0) ? 70 : 70):
      case ((((float) (1.0f * Float.NaN)) == 0) ? 71 : 71):
      case ((((float) (Float.MAX_VALUE * Float.NEGATIVE_INFINITY)) == 0) ? 72 : 72):
      case ((((float) (Float.MAX_VALUE * -1.0f)) == 0) ? 73 : 73):
      case ((((float) (Float.MAX_VALUE * -0.0f)) == 0) ? 74 : 74):
      case ((((float) (Float.MAX_VALUE * 0.0f)) == 0) ? 75 : 75):
      case ((((float) (Float.MAX_VALUE * Float.MIN_VALUE)) == 0) ? 76 : 76):
      case ((((float) (Float.MAX_VALUE * 1.0f)) == 0) ? 77 : 77):
      case ((((float) (Float.MAX_VALUE * Float.MAX_VALUE)) == 0) ? 78 : 78):
      case ((((float) (Float.MAX_VALUE * Float.POSITIVE_INFINITY)) == 0) ? 79 : 79):
      case ((((float) (Float.MAX_VALUE * Float.NaN)) == 0) ? 80 : 80):
      case ((((float) (Float.POSITIVE_INFINITY * Float.NEGATIVE_INFINITY)) == 0) ? 81 : 81):
      case ((((float) (Float.POSITIVE_INFINITY * -1.0f)) == 0) ? 82 : 82):
      case ((((float) (Float.POSITIVE_INFINITY * -0.0f)) == 0) ? 83 : 83):
      case ((((float) (Float.POSITIVE_INFINITY * 0.0f)) == 0) ? 84 : 84):
      case ((((float) (Float.POSITIVE_INFINITY * Float.MIN_VALUE)) == 0) ? 85 : 85):
      case ((((float) (Float.POSITIVE_INFINITY * 1.0f)) == 0) ? 86 : 86):
      case ((((float) (Float.POSITIVE_INFINITY * Float.MAX_VALUE)) == 0) ? 87 : 87):
      case ((((float) (Float.POSITIVE_INFINITY * Float.POSITIVE_INFINITY)) == 0) ? 88 : 88):
      case ((((float) (Float.POSITIVE_INFINITY * Float.NaN)) == 0) ? 89 : 89):
      case ((((float) (Float.NaN * Float.NEGATIVE_INFINITY)) == 0) ? 90 : 90):
      case ((((float) (Float.NaN * -1.0f)) == 0) ? 91 : 91):
      case ((((float) (Float.NaN * -0.0f)) == 0) ? 92 : 92):
      case ((((float) (Float.NaN * 0.0f)) == 0) ? 93 : 93):
      case ((((float) (Float.NaN * Float.MIN_VALUE)) == 0) ? 94 : 94):
      case ((((float) (Float.NaN * 1.0f)) == 0) ? 95 : 95):
      case ((((float) (Float.NaN * Float.MAX_VALUE)) == 0) ? 96 : 96):
      case ((((float) (Float.NaN * Float.POSITIVE_INFINITY)) == 0) ? 97 : 97):
      case ((((float) (Float.NaN * Float.NaN)) == 0) ? 98 : 98):
      case ((((float) (Float.NEGATIVE_INFINITY / Float.NEGATIVE_INFINITY)) == 0) ? 99 : 99):
      case ((((float) (Float.NEGATIVE_INFINITY / -1.0f)) == 0) ? 100 : 100):
      case ((((float) (Float.NEGATIVE_INFINITY / -0.0f)) == 0) ? 101 : 101):
      case ((((float) (Float.NEGATIVE_INFINITY / 0.0f)) == 0) ? 102 : 102):
      case ((((float) (Float.NEGATIVE_INFINITY / Float.MIN_VALUE)) == 0) ? 103 : 103):
      case ((((float) (Float.NEGATIVE_INFINITY / 1.0f)) == 0) ? 104 : 104):
      case ((((float) (Float.NEGATIVE_INFINITY / Float.MAX_VALUE)) == 0) ? 105 : 105):
      case ((((float) (Float.NEGATIVE_INFINITY / Float.POSITIVE_INFINITY)) == 0) ? 106 : 106):
      case ((((float) (Float.NEGATIVE_INFINITY / Float.NaN)) == 0) ? 107 : 107):
      case ((((float) (-1.0f / Float.NEGATIVE_INFINITY)) == 0) ? 108 : 108):
      case ((((float) (-1.0f / -1.0f)) == 0) ? 109 : 109):
      case ((((float) (-1.0f / -0.0f)) == 0) ? 110 : 110):
      case ((((float) (-1.0f / 0.0f)) == 0) ? 111 : 111):
      case ((((float) (-1.0f / Float.MIN_VALUE)) == 0) ? 112 : 112):
      case ((((float) (-1.0f / 1.0f)) == 0) ? 113 : 113):
      case ((((float) (-1.0f / Float.MAX_VALUE)) == 0) ? 114 : 114):
      case ((((float) (-1.0f / Float.POSITIVE_INFINITY)) == 0) ? 115 : 115):
      case ((((float) (-1.0f / Float.NaN)) == 0) ? 116 : 116):
      case ((((float) (-0.0f / Float.NEGATIVE_INFINITY)) == 0) ? 117 : 117):
      case ((((float) (-0.0f / -1.0f)) == 0) ? 118 : 118):
      case ((((float) (-0.0f / -0.0f)) == 0) ? 119 : 119):
      case ((((float) (-0.0f / 0.0f)) == 0) ? 120 : 120):
      case ((((float) (-0.0f / Float.MIN_VALUE)) == 0) ? 121 : 121):
      case ((((float) (-0.0f / 1.0f)) == 0) ? 122 : 122):
      case ((((float) (-0.0f / Float.MAX_VALUE)) == 0) ? 123 : 123):
      case ((((float) (-0.0f / Float.POSITIVE_INFINITY)) == 0) ? 124 : 124):
      case ((((float) (-0.0f / Float.NaN)) == 0) ? 125 : 125):
      case ((((float) (0.0f / Float.NEGATIVE_INFINITY)) == 0) ? 126 : 126):
      case ((((float) (0.0f / -1.0f)) == 0) ? 127 : 127):
      case ((((float) (0.0f / -0.0f)) == 0) ? 128 : 128):
      case ((((float) (0.0f / 0.0f)) == 0) ? 129 : 129):
      case ((((float) (0.0f / Float.MIN_VALUE)) == 0) ? 130 : 130):
      case ((((float) (0.0f / 1.0f)) == 0) ? 131 : 131):
      case ((((float) (0.0f / Float.MAX_VALUE)) == 0) ? 132 : 132):
      case ((((float) (0.0f / Float.POSITIVE_INFINITY)) == 0) ? 133 : 133):
      case ((((float) (0.0f / Float.NaN)) == 0) ? 134 : 134):
      case ((((float) (Float.MIN_VALUE / Float.NEGATIVE_INFINITY)) == 0) ? 135 : 135):
      case ((((float) (Float.MIN_VALUE / -1.0f)) == 0) ? 136 : 136):
      case ((((float) (Float.MIN_VALUE / -0.0f)) == 0) ? 137 : 137):
      case ((((float) (Float.MIN_VALUE / 0.0f)) == 0) ? 138 : 138):
      case ((((float) (Float.MIN_VALUE / Float.MIN_VALUE)) == 0) ? 139 : 139):
      case ((((float) (Float.MIN_VALUE / 1.0f)) == 0) ? 140 : 140):
      case ((((float) (Float.MIN_VALUE / Float.MAX_VALUE)) == 0) ? 141 : 141):
      case ((((float) (Float.MIN_VALUE / Float.POSITIVE_INFINITY)) == 0) ? 142 : 142):
      case ((((float) (Float.MIN_VALUE / Float.NaN)) == 0) ? 143 : 143):
      case ((((float) (1.0f / Float.NEGATIVE_INFINITY)) == 0) ? 144 : 144):
      case ((((float) (1.0f / -1.0f)) == 0) ? 145 : 145):
      case ((((float) (1.0f / -0.0f)) == 0) ? 146 : 146):
      case ((((float) (1.0f / 0.0f)) == 0) ? 147 : 147):
      case ((((float) (1.0f / Float.MIN_VALUE)) == 0) ? 148 : 148):
      case ((((float) (1.0f / 1.0f)) == 0) ? 149 : 149):
      case ((((float) (1.0f / Float.MAX_VALUE)) == 0) ? 150 : 150):
      case ((((float) (1.0f / Float.POSITIVE_INFINITY)) == 0) ? 151 : 151):
      case ((((float) (1.0f / Float.NaN)) == 0) ? 152 : 152):
      case ((((float) (Float.MAX_VALUE / Float.NEGATIVE_INFINITY)) == 0) ? 153 : 153):
      case ((((float) (Float.MAX_VALUE / -1.0f)) == 0) ? 154 : 154):
      case ((((float) (Float.MAX_VALUE / -0.0f)) == 0) ? 155 : 155):
      case ((((float) (Float.MAX_VALUE / 0.0f)) == 0) ? 156 : 156):
      case ((((float) (Float.MAX_VALUE / Float.MIN_VALUE)) == 0) ? 157 : 157):
      case ((((float) (Float.MAX_VALUE / 1.0f)) == 0) ? 158 : 158):
      case ((((float) (Float.MAX_VALUE / Float.MAX_VALUE)) == 0) ? 159 : 159):
      case ((((float) (Float.MAX_VALUE / Float.POSITIVE_INFINITY)) == 0) ? 160 : 160):
      case ((((float) (Float.MAX_VALUE / Float.NaN)) == 0) ? 161 : 161):
      case ((((float) (Float.POSITIVE_INFINITY / Float.NEGATIVE_INFINITY)) == 0) ? 162 : 162):
      case ((((float) (Float.POSITIVE_INFINITY / -1.0f)) == 0) ? 163 : 163):
      case ((((float) (Float.POSITIVE_INFINITY / -0.0f)) == 0) ? 164 : 164):
      case ((((float) (Float.POSITIVE_INFINITY / 0.0f)) == 0) ? 165 : 165):
      case ((((float) (Float.POSITIVE_INFINITY / Float.MIN_VALUE)) == 0) ? 166 : 166):
      case ((((float) (Float.POSITIVE_INFINITY / 1.0f)) == 0) ? 167 : 167):
      case ((((float) (Float.POSITIVE_INFINITY / Float.MAX_VALUE)) == 0) ? 168 : 168):
      case ((((float) (Float.POSITIVE_INFINITY / Float.POSITIVE_INFINITY)) == 0) ? 169 : 169):
      case ((((float) (Float.POSITIVE_INFINITY / Float.NaN)) == 0) ? 170 : 170):
      case ((((float) (Float.NaN / Float.NEGATIVE_INFINITY)) == 0) ? 171 : 171):
      case ((((float) (Float.NaN / -1.0f)) == 0) ? 172 : 172):
      case ((((float) (Float.NaN / -0.0f)) == 0) ? 173 : 173):
      case ((((float) (Float.NaN / 0.0f)) == 0) ? 174 : 174):
      case ((((float) (Float.NaN / Float.MIN_VALUE)) == 0) ? 175 : 175):
      case ((((float) (Float.NaN / 1.0f)) == 0) ? 176 : 176):
      case ((((float) (Float.NaN / Float.MAX_VALUE)) == 0) ? 177 : 177):
      case ((((float) (Float.NaN / Float.POSITIVE_INFINITY)) == 0) ? 178 : 178):
      case ((((float) (Float.NaN / Float.NaN)) == 0) ? 179 : 179):
      case ((((float) (Float.NEGATIVE_INFINITY % Float.NEGATIVE_INFINITY)) == 0) ? 180 : 180):
      case ((((float) (Float.NEGATIVE_INFINITY % -1.0f)) == 0) ? 181 : 181):
      case ((((float) (Float.NEGATIVE_INFINITY % -0.0f)) == 0) ? 182 : 182):
      case ((((float) (Float.NEGATIVE_INFINITY % 0.0f)) == 0) ? 183 : 183):
      case ((((float) (Float.NEGATIVE_INFINITY % Float.MIN_VALUE)) == 0) ? 184 : 184):
      case ((((float) (Float.NEGATIVE_INFINITY % 1.0f)) == 0) ? 185 : 185):
      case ((((float) (Float.NEGATIVE_INFINITY % Float.MAX_VALUE)) == 0) ? 186 : 186):
      case ((((float) (Float.NEGATIVE_INFINITY % Float.POSITIVE_INFINITY)) == 0) ? 187 : 187):
      case ((((float) (Float.NEGATIVE_INFINITY % Float.NaN)) == 0) ? 188 : 188):
      case ((((float) (-1.0f % Float.NEGATIVE_INFINITY)) == 0) ? 189 : 189):
      case ((((float) (-1.0f % -1.0f)) == 0) ? 190 : 190):
      case ((((float) (-1.0f % -0.0f)) == 0) ? 191 : 191):
      case ((((float) (-1.0f % 0.0f)) == 0) ? 192 : 192):
      case ((((float) (-1.0f % Float.MIN_VALUE)) == 0) ? 193 : 193):
      case ((((float) (-1.0f % 1.0f)) == 0) ? 194 : 194):
      case ((((float) (-1.0f % Float.MAX_VALUE)) == 0) ? 195 : 195):
      case ((((float) (-1.0f % Float.POSITIVE_INFINITY)) == 0) ? 196 : 196):
      case ((((float) (-1.0f % Float.NaN)) == 0) ? 197 : 197):
      case ((((float) (-0.0f % Float.NEGATIVE_INFINITY)) == 0) ? 198 : 198):
      case ((((float) (-0.0f % -1.0f)) == 0) ? 199 : 199):
      case ((((float) (-0.0f % -0.0f)) == 0) ? 200 : 200):
      case ((((float) (-0.0f % 0.0f)) == 0) ? 201 : 201):
      case ((((float) (-0.0f % Float.MIN_VALUE)) == 0) ? 202 : 202):
      case ((((float) (-0.0f % 1.0f)) == 0) ? 203 : 203):
      case ((((float) (-0.0f % Float.MAX_VALUE)) == 0) ? 204 : 204):
      case ((((float) (-0.0f % Float.POSITIVE_INFINITY)) == 0) ? 205 : 205):
      case ((((float) (-0.0f % Float.NaN)) == 0) ? 206 : 206):
      case ((((float) (0.0f % Float.NEGATIVE_INFINITY)) == 0) ? 207 : 207):
      case ((((float) (0.0f % -1.0f)) == 0) ? 208 : 208):
      case ((((float) (0.0f % -0.0f)) == 0) ? 209 : 209):
      case ((((float) (0.0f % 0.0f)) == 0) ? 210 : 210):
      case ((((float) (0.0f % Float.MIN_VALUE)) == 0) ? 211 : 211):
      case ((((float) (0.0f % 1.0f)) == 0) ? 212 : 212):
      case ((((float) (0.0f % Float.MAX_VALUE)) == 0) ? 213 : 213):
      case ((((float) (0.0f % Float.POSITIVE_INFINITY)) == 0) ? 214 : 214):
      case ((((float) (0.0f % Float.NaN)) == 0) ? 215 : 215):
      case ((((float) (Float.MIN_VALUE % Float.NEGATIVE_INFINITY)) == 0) ? 216 : 216):
      case ((((float) (Float.MIN_VALUE % -1.0f)) == 0) ? 217 : 217):
      case ((((float) (Float.MIN_VALUE % -0.0f)) == 0) ? 218 : 218):
      case ((((float) (Float.MIN_VALUE % 0.0f)) == 0) ? 219 : 219):
      case ((((float) (Float.MIN_VALUE % Float.MIN_VALUE)) == 0) ? 220 : 220):
      case ((((float) (Float.MIN_VALUE % 1.0f)) == 0) ? 221 : 221):
      case ((((float) (Float.MIN_VALUE % Float.MAX_VALUE)) == 0) ? 222 : 222):
      case ((((float) (Float.MIN_VALUE % Float.POSITIVE_INFINITY)) == 0) ? 223 : 223):
      case ((((float) (Float.MIN_VALUE % Float.NaN)) == 0) ? 224 : 224):
      case ((((float) (1.0f % Float.NEGATIVE_INFINITY)) == 0) ? 225 : 225):
      case ((((float) (1.0f % -1.0f)) == 0) ? 226 : 226):
      case ((((float) (1.0f % -0.0f)) == 0) ? 227 : 227):
      case ((((float) (1.0f % 0.0f)) == 0) ? 228 : 228):
      case ((((float) (1.0f % Float.MIN_VALUE)) == 0) ? 229 : 229):
      case ((((float) (1.0f % 1.0f)) == 0) ? 230 : 230):
      case ((((float) (1.0f % Float.MAX_VALUE)) == 0) ? 231 : 231):
      case ((((float) (1.0f % Float.POSITIVE_INFINITY)) == 0) ? 232 : 232):
      case ((((float) (1.0f % Float.NaN)) == 0) ? 233 : 233):
      case ((((float) (Float.MAX_VALUE % Float.NEGATIVE_INFINITY)) == 0) ? 234 : 234):
      case ((((float) (Float.MAX_VALUE % -1.0f)) == 0) ? 235 : 235):
      case ((((float) (Float.MAX_VALUE % -0.0f)) == 0) ? 236 : 236):
      case ((((float) (Float.MAX_VALUE % 0.0f)) == 0) ? 237 : 237):
      case ((((float) (Float.MAX_VALUE % Float.MIN_VALUE)) == 0) ? 238 : 238):
      case ((((float) (Float.MAX_VALUE % 1.0f)) == 0) ? 239 : 239):
      case ((((float) (Float.MAX_VALUE % Float.MAX_VALUE)) == 0) ? 240 : 240):
      case ((((float) (Float.MAX_VALUE % Float.POSITIVE_INFINITY)) == 0) ? 241 : 241):
      case ((((float) (Float.MAX_VALUE % Float.NaN)) == 0) ? 242 : 242):
      case ((((float) (Float.POSITIVE_INFINITY % Float.NEGATIVE_INFINITY)) == 0) ? 243 : 243):
      case ((((float) (Float.POSITIVE_INFINITY % -1.0f)) == 0) ? 244 : 244):
      case ((((float) (Float.POSITIVE_INFINITY % -0.0f)) == 0) ? 245 : 245):
      case ((((float) (Float.POSITIVE_INFINITY % 0.0f)) == 0) ? 246 : 246):
      case ((((float) (Float.POSITIVE_INFINITY % Float.MIN_VALUE)) == 0) ? 247 : 247):
      case ((((float) (Float.POSITIVE_INFINITY % 1.0f)) == 0) ? 248 : 248):
      case ((((float) (Float.POSITIVE_INFINITY % Float.MAX_VALUE)) == 0) ? 249 : 249):
      case ((((float) (Float.POSITIVE_INFINITY % Float.POSITIVE_INFINITY)) == 0) ? 250 : 250):
      case ((((float) (Float.POSITIVE_INFINITY % Float.NaN)) == 0) ? 251 : 251):
      case ((((float) (Float.NaN % Float.NEGATIVE_INFINITY)) == 0) ? 252 : 252):
      case ((((float) (Float.NaN % -1.0f)) == 0) ? 253 : 253):
      case ((((float) (Float.NaN % -0.0f)) == 0) ? 254 : 254):
      case ((((float) (Float.NaN % 0.0f)) == 0) ? 255 : 255):
      case ((((float) (Float.NaN % Float.MIN_VALUE)) == 0) ? 256 : 256):
      case ((((float) (Float.NaN % 1.0f)) == 0) ? 257 : 257):
      case ((((float) (Float.NaN % Float.MAX_VALUE)) == 0) ? 258 : 258):
      case ((((float) (Float.NaN % Float.POSITIVE_INFINITY)) == 0) ? 259 : 259):
      case ((((float) (Float.NaN % Float.NaN)) == 0) ? 260 : 260):
      case ((((float) (Float.NEGATIVE_INFINITY + Float.NEGATIVE_INFINITY)) == 0) ? 261 : 261):
      case ((((float) (Float.NEGATIVE_INFINITY + -1.0f)) == 0) ? 262 : 262):
      case ((((float) (Float.NEGATIVE_INFINITY + -0.0f)) == 0) ? 263 : 263):
      case ((((float) (Float.NEGATIVE_INFINITY + 0.0f)) == 0) ? 264 : 264):
      case ((((float) (Float.NEGATIVE_INFINITY + Float.MIN_VALUE)) == 0) ? 265 : 265):
      case ((((float) (Float.NEGATIVE_INFINITY + 1.0f)) == 0) ? 266 : 266):
      case ((((float) (Float.NEGATIVE_INFINITY + Float.MAX_VALUE)) == 0) ? 267 : 267):
      case ((((float) (Float.NEGATIVE_INFINITY + Float.POSITIVE_INFINITY)) == 0) ? 268 : 268):
      case ((((float) (Float.NEGATIVE_INFINITY + Float.NaN)) == 0) ? 269 : 269):
      case ((((float) (-1.0f + Float.NEGATIVE_INFINITY)) == 0) ? 270 : 270):
      case ((((float) (-1.0f + -1.0f)) == 0) ? 271 : 271):
      case ((((float) (-1.0f + -0.0f)) == 0) ? 272 : 272):
      case ((((float) (-1.0f + 0.0f)) == 0) ? 273 : 273):
      case ((((float) (-1.0f + Float.MIN_VALUE)) == 0) ? 274 : 274):
      case ((((float) (-1.0f + 1.0f)) == 0) ? 275 : 275):
      case ((((float) (-1.0f + Float.MAX_VALUE)) == 0) ? 276 : 276):
      case ((((float) (-1.0f + Float.POSITIVE_INFINITY)) == 0) ? 277 : 277):
      case ((((float) (-1.0f + Float.NaN)) == 0) ? 278 : 278):
      case ((((float) (-0.0f + Float.NEGATIVE_INFINITY)) == 0) ? 279 : 279):
      case ((((float) (-0.0f + -1.0f)) == 0) ? 280 : 280):
      case ((((float) (-0.0f + -0.0f)) == 0) ? 281 : 281):
      case ((((float) (-0.0f + 0.0f)) == 0) ? 282 : 282):
      case ((((float) (-0.0f + Float.MIN_VALUE)) == 0) ? 283 : 283):
      case ((((float) (-0.0f + 1.0f)) == 0) ? 284 : 284):
      case ((((float) (-0.0f + Float.MAX_VALUE)) == 0) ? 285 : 285):
      case ((((float) (-0.0f + Float.POSITIVE_INFINITY)) == 0) ? 286 : 286):
      case ((((float) (-0.0f + Float.NaN)) == 0) ? 287 : 287):
      case ((((float) (0.0f + Float.NEGATIVE_INFINITY)) == 0) ? 288 : 288):
      case ((((float) (0.0f + -1.0f)) == 0) ? 289 : 289):
      case ((((float) (0.0f + -0.0f)) == 0) ? 290 : 290):
      case ((((float) (0.0f + 0.0f)) == 0) ? 291 : 291):
      case ((((float) (0.0f + Float.MIN_VALUE)) == 0) ? 292 : 292):
      case ((((float) (0.0f + 1.0f)) == 0) ? 293 : 293):
      case ((((float) (0.0f + Float.MAX_VALUE)) == 0) ? 294 : 294):
      case ((((float) (0.0f + Float.POSITIVE_INFINITY)) == 0) ? 295 : 295):
      case ((((float) (0.0f + Float.NaN)) == 0) ? 296 : 296):
      case ((((float) (Float.MIN_VALUE + Float.NEGATIVE_INFINITY)) == 0) ? 297 : 297):
      case ((((float) (Float.MIN_VALUE + -1.0f)) == 0) ? 298 : 298):
      case ((((float) (Float.MIN_VALUE + -0.0f)) == 0) ? 299 : 299):
      case ((((float) (Float.MIN_VALUE + 0.0f)) == 0) ? 300 : 300):
      case ((((float) (Float.MIN_VALUE + Float.MIN_VALUE)) == 0) ? 301 : 301):
      case ((((float) (Float.MIN_VALUE + 1.0f)) == 0) ? 302 : 302):
      case ((((float) (Float.MIN_VALUE + Float.MAX_VALUE)) == 0) ? 303 : 303):
      case ((((float) (Float.MIN_VALUE + Float.POSITIVE_INFINITY)) == 0) ? 304 : 304):
      case ((((float) (Float.MIN_VALUE + Float.NaN)) == 0) ? 305 : 305):
      case ((((float) (1.0f + Float.NEGATIVE_INFINITY)) == 0) ? 306 : 306):
      case ((((float) (1.0f + -1.0f)) == 0) ? 307 : 307):
      case ((((float) (1.0f + -0.0f)) == 0) ? 308 : 308):
      case ((((float) (1.0f + 0.0f)) == 0) ? 309 : 309):
      case ((((float) (1.0f + Float.MIN_VALUE)) == 0) ? 310 : 310):
      case ((((float) (1.0f + 1.0f)) == 0) ? 311 : 311):
      case ((((float) (1.0f + Float.MAX_VALUE)) == 0) ? 312 : 312):
      case ((((float) (1.0f + Float.POSITIVE_INFINITY)) == 0) ? 313 : 313):
      case ((((float) (1.0f + Float.NaN)) == 0) ? 314 : 314):
      case ((((float) (Float.MAX_VALUE + Float.NEGATIVE_INFINITY)) == 0) ? 315 : 315):
      case ((((float) (Float.MAX_VALUE + -1.0f)) == 0) ? 316 : 316):
      case ((((float) (Float.MAX_VALUE + -0.0f)) == 0) ? 317 : 317):
      case ((((float) (Float.MAX_VALUE + 0.0f)) == 0) ? 318 : 318):
      case ((((float) (Float.MAX_VALUE + Float.MIN_VALUE)) == 0) ? 319 : 319):
      case ((((float) (Float.MAX_VALUE + 1.0f)) == 0) ? 320 : 320):
      case ((((float) (Float.MAX_VALUE + Float.MAX_VALUE)) == 0) ? 321 : 321):
      case ((((float) (Float.MAX_VALUE + Float.POSITIVE_INFINITY)) == 0) ? 322 : 322):
      case ((((float) (Float.MAX_VALUE + Float.NaN)) == 0) ? 323 : 323):
      case ((((float) (Float.POSITIVE_INFINITY + Float.NEGATIVE_INFINITY)) == 0) ? 324 : 324):
      case ((((float) (Float.POSITIVE_INFINITY + -1.0f)) == 0) ? 325 : 325):
      case ((((float) (Float.POSITIVE_INFINITY + -0.0f)) == 0) ? 326 : 326):
      case ((((float) (Float.POSITIVE_INFINITY + 0.0f)) == 0) ? 327 : 327):
      case ((((float) (Float.POSITIVE_INFINITY + Float.MIN_VALUE)) == 0) ? 328 : 328):
      case ((((float) (Float.POSITIVE_INFINITY + 1.0f)) == 0) ? 329 : 329):
      case ((((float) (Float.POSITIVE_INFINITY + Float.MAX_VALUE)) == 0) ? 330 : 330):
      case ((((float) (Float.POSITIVE_INFINITY + Float.POSITIVE_INFINITY)) == 0) ? 331 : 331):
      case ((((float) (Float.POSITIVE_INFINITY + Float.NaN)) == 0) ? 332 : 332):
      case ((((float) (Float.NaN + Float.NEGATIVE_INFINITY)) == 0) ? 333 : 333):
      case ((((float) (Float.NaN + -1.0f)) == 0) ? 334 : 334):
      case ((((float) (Float.NaN + -0.0f)) == 0) ? 335 : 335):
      case ((((float) (Float.NaN + 0.0f)) == 0) ? 336 : 336):
      case ((((float) (Float.NaN + Float.MIN_VALUE)) == 0) ? 337 : 337):
      case ((((float) (Float.NaN + 1.0f)) == 0) ? 338 : 338):
      case ((((float) (Float.NaN + Float.MAX_VALUE)) == 0) ? 339 : 339):
      case ((((float) (Float.NaN + Float.POSITIVE_INFINITY)) == 0) ? 340 : 340):
      case ((((float) (Float.NaN + Float.NaN)) == 0) ? 341 : 341):
      case ((((float) (Float.NEGATIVE_INFINITY - Float.NEGATIVE_INFINITY)) == 0) ? 342 : 342):
      case ((((float) (Float.NEGATIVE_INFINITY - -1.0f)) == 0) ? 343 : 343):
      case ((((float) (Float.NEGATIVE_INFINITY - -0.0f)) == 0) ? 344 : 344):
      case ((((float) (Float.NEGATIVE_INFINITY - 0.0f)) == 0) ? 345 : 345):
      case ((((float) (Float.NEGATIVE_INFINITY - Float.MIN_VALUE)) == 0) ? 346 : 346):
      case ((((float) (Float.NEGATIVE_INFINITY - 1.0f)) == 0) ? 347 : 347):
      case ((((float) (Float.NEGATIVE_INFINITY - Float.MAX_VALUE)) == 0) ? 348 : 348):
      case ((((float) (Float.NEGATIVE_INFINITY - Float.POSITIVE_INFINITY)) == 0) ? 349 : 349):
      case ((((float) (Float.NEGATIVE_INFINITY - Float.NaN)) == 0) ? 350 : 350):
      case ((((float) (-1.0f - Float.NEGATIVE_INFINITY)) == 0) ? 351 : 351):
      case ((((float) (-1.0f - -1.0f)) == 0) ? 352 : 352):
      case ((((float) (-1.0f - -0.0f)) == 0) ? 353 : 353):
      case ((((float) (-1.0f - 0.0f)) == 0) ? 354 : 354):
      case ((((float) (-1.0f - Float.MIN_VALUE)) == 0) ? 355 : 355):
      case ((((float) (-1.0f - 1.0f)) == 0) ? 356 : 356):
      case ((((float) (-1.0f - Float.MAX_VALUE)) == 0) ? 357 : 357):
      case ((((float) (-1.0f - Float.POSITIVE_INFINITY)) == 0) ? 358 : 358):
      case ((((float) (-1.0f - Float.NaN)) == 0) ? 359 : 359):
      case ((((float) (-0.0f - Float.NEGATIVE_INFINITY)) == 0) ? 360 : 360):
      case ((((float) (-0.0f - -1.0f)) == 0) ? 361 : 361):
      case ((((float) (-0.0f - -0.0f)) == 0) ? 362 : 362):
      case ((((float) (-0.0f - 0.0f)) == 0) ? 363 : 363):
      case ((((float) (-0.0f - Float.MIN_VALUE)) == 0) ? 364 : 364):
      case ((((float) (-0.0f - 1.0f)) == 0) ? 365 : 365):
      case ((((float) (-0.0f - Float.MAX_VALUE)) == 0) ? 366 : 366):
      case ((((float) (-0.0f - Float.POSITIVE_INFINITY)) == 0) ? 367 : 367):
      case ((((float) (-0.0f - Float.NaN)) == 0) ? 368 : 368):
      case ((((float) (0.0f - Float.NEGATIVE_INFINITY)) == 0) ? 369 : 369):
      case ((((float) (0.0f - -1.0f)) == 0) ? 370 : 370):
      case ((((float) (0.0f - -0.0f)) == 0) ? 371 : 371):
      case ((((float) (0.0f - 0.0f)) == 0) ? 372 : 372):
      case ((((float) (0.0f - Float.MIN_VALUE)) == 0) ? 373 : 373):
      case ((((float) (0.0f - 1.0f)) == 0) ? 374 : 374):
      case ((((float) (0.0f - Float.MAX_VALUE)) == 0) ? 375 : 375):
      case ((((float) (0.0f - Float.POSITIVE_INFINITY)) == 0) ? 376 : 376):
      case ((((float) (0.0f - Float.NaN)) == 0) ? 377 : 377):
      case ((((float) (Float.MIN_VALUE - Float.NEGATIVE_INFINITY)) == 0) ? 378 : 378):
      case ((((float) (Float.MIN_VALUE - -1.0f)) == 0) ? 379 : 379):
      case ((((float) (Float.MIN_VALUE - -0.0f)) == 0) ? 380 : 380):
      case ((((float) (Float.MIN_VALUE - 0.0f)) == 0) ? 381 : 381):
      case ((((float) (Float.MIN_VALUE - Float.MIN_VALUE)) == 0) ? 382 : 382):
      case ((((float) (Float.MIN_VALUE - 1.0f)) == 0) ? 383 : 383):
      case ((((float) (Float.MIN_VALUE - Float.MAX_VALUE)) == 0) ? 384 : 384):
      case ((((float) (Float.MIN_VALUE - Float.POSITIVE_INFINITY)) == 0) ? 385 : 385):
      case ((((float) (Float.MIN_VALUE - Float.NaN)) == 0) ? 386 : 386):
      case ((((float) (1.0f - Float.NEGATIVE_INFINITY)) == 0) ? 387 : 387):
      case ((((float) (1.0f - -1.0f)) == 0) ? 388 : 388):
      case ((((float) (1.0f - -0.0f)) == 0) ? 389 : 389):
      case ((((float) (1.0f - 0.0f)) == 0) ? 390 : 390):
      case ((((float) (1.0f - Float.MIN_VALUE)) == 0) ? 391 : 391):
      case ((((float) (1.0f - 1.0f)) == 0) ? 392 : 392):
      case ((((float) (1.0f - Float.MAX_VALUE)) == 0) ? 393 : 393):
      case ((((float) (1.0f - Float.POSITIVE_INFINITY)) == 0) ? 394 : 394):
      case ((((float) (1.0f - Float.NaN)) == 0) ? 395 : 395):
      case ((((float) (Float.MAX_VALUE - Float.NEGATIVE_INFINITY)) == 0) ? 396 : 396):
      case ((((float) (Float.MAX_VALUE - -1.0f)) == 0) ? 397 : 397):
      case ((((float) (Float.MAX_VALUE - -0.0f)) == 0) ? 398 : 398):
      case ((((float) (Float.MAX_VALUE - 0.0f)) == 0) ? 399 : 399):
      case ((((float) (Float.MAX_VALUE - Float.MIN_VALUE)) == 0) ? 400 : 400):
      case ((((float) (Float.MAX_VALUE - 1.0f)) == 0) ? 401 : 401):
      case ((((float) (Float.MAX_VALUE - Float.MAX_VALUE)) == 0) ? 402 : 402):
      case ((((float) (Float.MAX_VALUE - Float.POSITIVE_INFINITY)) == 0) ? 403 : 403):
      case ((((float) (Float.MAX_VALUE - Float.NaN)) == 0) ? 404 : 404):
      case ((((float) (Float.POSITIVE_INFINITY - Float.NEGATIVE_INFINITY)) == 0) ? 405 : 405):
      case ((((float) (Float.POSITIVE_INFINITY - -1.0f)) == 0) ? 406 : 406):
      case ((((float) (Float.POSITIVE_INFINITY - -0.0f)) == 0) ? 407 : 407):
      case ((((float) (Float.POSITIVE_INFINITY - 0.0f)) == 0) ? 408 : 408):
      case ((((float) (Float.POSITIVE_INFINITY - Float.MIN_VALUE)) == 0) ? 409 : 409):
      case ((((float) (Float.POSITIVE_INFINITY - 1.0f)) == 0) ? 410 : 410):
      case ((((float) (Float.POSITIVE_INFINITY - Float.MAX_VALUE)) == 0) ? 411 : 411):
      case ((((float) (Float.POSITIVE_INFINITY - Float.POSITIVE_INFINITY)) == 0) ? 412 : 412):
      case ((((float) (Float.POSITIVE_INFINITY - Float.NaN)) == 0) ? 413 : 413):
      case ((((float) (Float.NaN - Float.NEGATIVE_INFINITY)) == 0) ? 414 : 414):
      case ((((float) (Float.NaN - -1.0f)) == 0) ? 415 : 415):
      case ((((float) (Float.NaN - -0.0f)) == 0) ? 416 : 416):
      case ((((float) (Float.NaN - 0.0f)) == 0) ? 417 : 417):
      case ((((float) (Float.NaN - Float.MIN_VALUE)) == 0) ? 418 : 418):
      case ((((float) (Float.NaN - 1.0f)) == 0) ? 419 : 419):
      case ((((float) (Float.NaN - Float.MAX_VALUE)) == 0) ? 420 : 420):
      case ((((float) (Float.NaN - Float.POSITIVE_INFINITY)) == 0) ? 421 : 421):
      case ((((float) (Float.NaN - Float.NaN)) == 0) ? 422 : 422):
      case ((Float.NEGATIVE_INFINITY < Float.NEGATIVE_INFINITY) ? 423 : 423):
      case ((Float.NEGATIVE_INFINITY < -1.0f) ? 424 : 424):
      case ((Float.NEGATIVE_INFINITY < -0.0f) ? 425 : 425):
      case ((Float.NEGATIVE_INFINITY < 0.0f) ? 426 : 426):
      case ((Float.NEGATIVE_INFINITY < Float.MIN_VALUE) ? 427 : 427):
      case ((Float.NEGATIVE_INFINITY < 1.0f) ? 428 : 428):
      case ((Float.NEGATIVE_INFINITY < Float.MAX_VALUE) ? 429 : 429):
      case ((Float.NEGATIVE_INFINITY < Float.POSITIVE_INFINITY) ? 430 : 430):
      case ((Float.NEGATIVE_INFINITY < Float.NaN) ? 431 : 431):
      case ((-1.0f < Float.NEGATIVE_INFINITY) ? 432 : 432):
      case ((-1.0f < -1.0f) ? 433 : 433):
      case ((-1.0f < -0.0f) ? 434 : 434):
      case ((-1.0f < 0.0f) ? 435 : 435):
      case ((-1.0f < Float.MIN_VALUE) ? 436 : 436):
      case ((-1.0f < 1.0f) ? 437 : 437):
      case ((-1.0f < Float.MAX_VALUE) ? 438 : 438):
      case ((-1.0f < Float.POSITIVE_INFINITY) ? 439 : 439):
      case ((-1.0f < Float.NaN) ? 440 : 440):
      case ((-0.0f < Float.NEGATIVE_INFINITY) ? 441 : 441):
      case ((-0.0f < -1.0f) ? 442 : 442):
      case ((-0.0f < -0.0f) ? 443 : 443):
      case ((-0.0f < 0.0f) ? 444 : 444):
      case ((-0.0f < Float.MIN_VALUE) ? 445 : 445):
      case ((-0.0f < 1.0f) ? 446 : 446):
      case ((-0.0f < Float.MAX_VALUE) ? 447 : 447):
      case ((-0.0f < Float.POSITIVE_INFINITY) ? 448 : 448):
      case ((-0.0f < Float.NaN) ? 449 : 449):
      case ((0.0f < Float.NEGATIVE_INFINITY) ? 450 : 450):
      case ((0.0f < -1.0f) ? 451 : 451):
      case ((0.0f < -0.0f) ? 452 : 452):
      case ((0.0f < 0.0f) ? 453 : 453):
      case ((0.0f < Float.MIN_VALUE) ? 454 : 454):
      case ((0.0f < 1.0f) ? 455 : 455):
      case ((0.0f < Float.MAX_VALUE) ? 456 : 456):
      case ((0.0f < Float.POSITIVE_INFINITY) ? 457 : 457):
      case ((0.0f < Float.NaN) ? 458 : 458):
      case ((Float.MIN_VALUE < Float.NEGATIVE_INFINITY) ? 459 : 459):
      case ((Float.MIN_VALUE < -1.0f) ? 460 : 460):
      case ((Float.MIN_VALUE < -0.0f) ? 461 : 461):
      case ((Float.MIN_VALUE < 0.0f) ? 462 : 462):
      case ((Float.MIN_VALUE < Float.MIN_VALUE) ? 463 : 463):
      case ((Float.MIN_VALUE < 1.0f) ? 464 : 464):
      case ((Float.MIN_VALUE < Float.MAX_VALUE) ? 465 : 465):
      case ((Float.MIN_VALUE < Float.POSITIVE_INFINITY) ? 466 : 466):
      case ((Float.MIN_VALUE < Float.NaN) ? 467 : 467):
      case ((1.0f < Float.NEGATIVE_INFINITY) ? 468 : 468):
      case ((1.0f < -1.0f) ? 469 : 469):
      case ((1.0f < -0.0f) ? 470 : 470):
      case ((1.0f < 0.0f) ? 471 : 471):
      case ((1.0f < Float.MIN_VALUE) ? 472 : 472):
      case ((1.0f < 1.0f) ? 473 : 473):
      case ((1.0f < Float.MAX_VALUE) ? 474 : 474):
      case ((1.0f < Float.POSITIVE_INFINITY) ? 475 : 475):
      case ((1.0f < Float.NaN) ? 476 : 476):
      case ((Float.MAX_VALUE < Float.NEGATIVE_INFINITY) ? 477 : 477):
      case ((Float.MAX_VALUE < -1.0f) ? 478 : 478):
      case ((Float.MAX_VALUE < -0.0f) ? 479 : 479):
      case ((Float.MAX_VALUE < 0.0f) ? 480 : 480):
      case ((Float.MAX_VALUE < Float.MIN_VALUE) ? 481 : 481):
      case ((Float.MAX_VALUE < 1.0f) ? 482 : 482):
      case ((Float.MAX_VALUE < Float.MAX_VALUE) ? 483 : 483):
      case ((Float.MAX_VALUE < Float.POSITIVE_INFINITY) ? 484 : 484):
      case ((Float.MAX_VALUE < Float.NaN) ? 485 : 485):
      case ((Float.POSITIVE_INFINITY < Float.NEGATIVE_INFINITY) ? 486 : 486):
      case ((Float.POSITIVE_INFINITY < -1.0f) ? 487 : 487):
      case ((Float.POSITIVE_INFINITY < -0.0f) ? 488 : 488):
      case ((Float.POSITIVE_INFINITY < 0.0f) ? 489 : 489):
      case ((Float.POSITIVE_INFINITY < Float.MIN_VALUE) ? 490 : 490):
      case ((Float.POSITIVE_INFINITY < 1.0f) ? 491 : 491):
      case ((Float.POSITIVE_INFINITY < Float.MAX_VALUE) ? 492 : 492):
      case ((Float.POSITIVE_INFINITY < Float.POSITIVE_INFINITY) ? 493 : 493):
      case ((Float.POSITIVE_INFINITY < Float.NaN) ? 494 : 494):
      case ((Float.NaN < Float.NEGATIVE_INFINITY) ? 495 : 495):
      case ((Float.NaN < -1.0f) ? 496 : 496):
      case ((Float.NaN < -0.0f) ? 497 : 497):
      case ((Float.NaN < 0.0f) ? 498 : 498):
      case ((Float.NaN < Float.MIN_VALUE) ? 499 : 499):
      case ((Float.NaN < 1.0f) ? 500 : 500):
      case ((Float.NaN < Float.MAX_VALUE) ? 501 : 501):
      case ((Float.NaN < Float.POSITIVE_INFINITY) ? 502 : 502):
      case ((Float.NaN < Float.NaN) ? 503 : 503):
      case ((Float.NEGATIVE_INFINITY > Float.NEGATIVE_INFINITY) ? 504 : 504):
      case ((Float.NEGATIVE_INFINITY > -1.0f) ? 505 : 505):
      case ((Float.NEGATIVE_INFINITY > -0.0f) ? 506 : 506):
      case ((Float.NEGATIVE_INFINITY > 0.0f) ? 507 : 507):
      case ((Float.NEGATIVE_INFINITY > Float.MIN_VALUE) ? 508 : 508):
      case ((Float.NEGATIVE_INFINITY > 1.0f) ? 509 : 509):
      case ((Float.NEGATIVE_INFINITY > Float.MAX_VALUE) ? 510 : 510):
      case ((Float.NEGATIVE_INFINITY > Float.POSITIVE_INFINITY) ? 511 : 511):
      case ((Float.NEGATIVE_INFINITY > Float.NaN) ? 512 : 512):
      case ((-1.0f > Float.NEGATIVE_INFINITY) ? 513 : 513):
      case ((-1.0f > -1.0f) ? 514 : 514):
      case ((-1.0f > -0.0f) ? 515 : 515):
      case ((-1.0f > 0.0f) ? 516 : 516):
      case ((-1.0f > Float.MIN_VALUE) ? 517 : 517):
      case ((-1.0f > 1.0f) ? 518 : 518):
      case ((-1.0f > Float.MAX_VALUE) ? 519 : 519):
      case ((-1.0f > Float.POSITIVE_INFINITY) ? 520 : 520):
      case ((-1.0f > Float.NaN) ? 521 : 521):
      case ((-0.0f > Float.NEGATIVE_INFINITY) ? 522 : 522):
      case ((-0.0f > -1.0f) ? 523 : 523):
      case ((-0.0f > -0.0f) ? 524 : 524):
      case ((-0.0f > 0.0f) ? 525 : 525):
      case ((-0.0f > Float.MIN_VALUE) ? 526 : 526):
      case ((-0.0f > 1.0f) ? 527 : 527):
      case ((-0.0f > Float.MAX_VALUE) ? 528 : 528):
      case ((-0.0f > Float.POSITIVE_INFINITY) ? 529 : 529):
      case ((-0.0f > Float.NaN) ? 530 : 530):
      case ((0.0f > Float.NEGATIVE_INFINITY) ? 531 : 531):
      case ((0.0f > -1.0f) ? 532 : 532):
      case ((0.0f > -0.0f) ? 533 : 533):
      case ((0.0f > 0.0f) ? 534 : 534):
      case ((0.0f > Float.MIN_VALUE) ? 535 : 535):
      case ((0.0f > 1.0f) ? 536 : 536):
      case ((0.0f > Float.MAX_VALUE) ? 537 : 537):
      case ((0.0f > Float.POSITIVE_INFINITY) ? 538 : 538):
      case ((0.0f > Float.NaN) ? 539 : 539):
      case ((Float.MIN_VALUE > Float.NEGATIVE_INFINITY) ? 540 : 540):
      case ((Float.MIN_VALUE > -1.0f) ? 541 : 541):
      case ((Float.MIN_VALUE > -0.0f) ? 542 : 542):
      case ((Float.MIN_VALUE > 0.0f) ? 543 : 543):
      case ((Float.MIN_VALUE > Float.MIN_VALUE) ? 544 : 544):
      case ((Float.MIN_VALUE > 1.0f) ? 545 : 545):
      case ((Float.MIN_VALUE > Float.MAX_VALUE) ? 546 : 546):
      case ((Float.MIN_VALUE > Float.POSITIVE_INFINITY) ? 547 : 547):
      case ((Float.MIN_VALUE > Float.NaN) ? 548 : 548):
      case ((1.0f > Float.NEGATIVE_INFINITY) ? 549 : 549):
      case ((1.0f > -1.0f) ? 550 : 550):
      case ((1.0f > -0.0f) ? 551 : 551):
      case ((1.0f > 0.0f) ? 552 : 552):
      case ((1.0f > Float.MIN_VALUE) ? 553 : 553):
      case ((1.0f > 1.0f) ? 554 : 554):
      case ((1.0f > Float.MAX_VALUE) ? 555 : 555):
      case ((1.0f > Float.POSITIVE_INFINITY) ? 556 : 556):
      case ((1.0f > Float.NaN) ? 557 : 557):
      case ((Float.MAX_VALUE > Float.NEGATIVE_INFINITY) ? 558 : 558):
      case ((Float.MAX_VALUE > -1.0f) ? 559 : 559):
      case ((Float.MAX_VALUE > -0.0f) ? 560 : 560):
      case ((Float.MAX_VALUE > 0.0f) ? 561 : 561):
      case ((Float.MAX_VALUE > Float.MIN_VALUE) ? 562 : 562):
      case ((Float.MAX_VALUE > 1.0f) ? 563 : 563):
      case ((Float.MAX_VALUE > Float.MAX_VALUE) ? 564 : 564):
      case ((Float.MAX_VALUE > Float.POSITIVE_INFINITY) ? 565 : 565):
      case ((Float.MAX_VALUE > Float.NaN) ? 566 : 566):
      case ((Float.POSITIVE_INFINITY > Float.NEGATIVE_INFINITY) ? 567 : 567):
      case ((Float.POSITIVE_INFINITY > -1.0f) ? 568 : 568):
      case ((Float.POSITIVE_INFINITY > -0.0f) ? 569 : 569):
      case ((Float.POSITIVE_INFINITY > 0.0f) ? 570 : 570):
      case ((Float.POSITIVE_INFINITY > Float.MIN_VALUE) ? 571 : 571):
      case ((Float.POSITIVE_INFINITY > 1.0f) ? 572 : 572):
      case ((Float.POSITIVE_INFINITY > Float.MAX_VALUE) ? 573 : 573):
      case ((Float.POSITIVE_INFINITY > Float.POSITIVE_INFINITY) ? 574 : 574):
      case ((Float.POSITIVE_INFINITY > Float.NaN) ? 575 : 575):
      case ((Float.NaN > Float.NEGATIVE_INFINITY) ? 576 : 576):
      case ((Float.NaN > -1.0f) ? 577 : 577):
      case ((Float.NaN > -0.0f) ? 578 : 578):
      case ((Float.NaN > 0.0f) ? 579 : 579):
      case ((Float.NaN > Float.MIN_VALUE) ? 580 : 580):
      case ((Float.NaN > 1.0f) ? 581 : 581):
      case ((Float.NaN > Float.MAX_VALUE) ? 582 : 582):
      case ((Float.NaN > Float.POSITIVE_INFINITY) ? 583 : 583):
      case ((Float.NaN > Float.NaN) ? 584 : 584):
      case ((Float.NEGATIVE_INFINITY <= Float.NEGATIVE_INFINITY) ? 585 : 585):
      case ((Float.NEGATIVE_INFINITY <= -1.0f) ? 586 : 586):
      case ((Float.NEGATIVE_INFINITY <= -0.0f) ? 587 : 587):
      case ((Float.NEGATIVE_INFINITY <= 0.0f) ? 588 : 588):
      case ((Float.NEGATIVE_INFINITY <= Float.MIN_VALUE) ? 589 : 589):
      case ((Float.NEGATIVE_INFINITY <= 1.0f) ? 590 : 590):
      case ((Float.NEGATIVE_INFINITY <= Float.MAX_VALUE) ? 591 : 591):
      case ((Float.NEGATIVE_INFINITY <= Float.POSITIVE_INFINITY) ? 592 : 592):
      case ((Float.NEGATIVE_INFINITY <= Float.NaN) ? 593 : 593):
      case ((-1.0f <= Float.NEGATIVE_INFINITY) ? 594 : 594):
      case ((-1.0f <= -1.0f) ? 595 : 595):
      case ((-1.0f <= -0.0f) ? 596 : 596):
      case ((-1.0f <= 0.0f) ? 597 : 597):
      case ((-1.0f <= Float.MIN_VALUE) ? 598 : 598):
      case ((-1.0f <= 1.0f) ? 599 : 599):
      case ((-1.0f <= Float.MAX_VALUE) ? 600 : 600):
      case ((-1.0f <= Float.POSITIVE_INFINITY) ? 601 : 601):
      case ((-1.0f <= Float.NaN) ? 602 : 602):
      case ((-0.0f <= Float.NEGATIVE_INFINITY) ? 603 : 603):
      case ((-0.0f <= -1.0f) ? 604 : 604):
      case ((-0.0f <= -0.0f) ? 605 : 605):
      case ((-0.0f <= 0.0f) ? 606 : 606):
      case ((-0.0f <= Float.MIN_VALUE) ? 607 : 607):
      case ((-0.0f <= 1.0f) ? 608 : 608):
      case ((-0.0f <= Float.MAX_VALUE) ? 609 : 609):
      case ((-0.0f <= Float.POSITIVE_INFINITY) ? 610 : 610):
      case ((-0.0f <= Float.NaN) ? 611 : 611):
      case ((0.0f <= Float.NEGATIVE_INFINITY) ? 612 : 612):
      case ((0.0f <= -1.0f) ? 613 : 613):
      case ((0.0f <= -0.0f) ? 614 : 614):
      case ((0.0f <= 0.0f) ? 615 : 615):
      case ((0.0f <= Float.MIN_VALUE) ? 616 : 616):
      case ((0.0f <= 1.0f) ? 617 : 617):
      case ((0.0f <= Float.MAX_VALUE) ? 618 : 618):
      case ((0.0f <= Float.POSITIVE_INFINITY) ? 619 : 619):
      case ((0.0f <= Float.NaN) ? 620 : 620):
      case ((Float.MIN_VALUE <= Float.NEGATIVE_INFINITY) ? 621 : 621):
      case ((Float.MIN_VALUE <= -1.0f) ? 622 : 622):
      case ((Float.MIN_VALUE <= -0.0f) ? 623 : 623):
      case ((Float.MIN_VALUE <= 0.0f) ? 624 : 624):
      case ((Float.MIN_VALUE <= Float.MIN_VALUE) ? 625 : 625):
      case ((Float.MIN_VALUE <= 1.0f) ? 626 : 626):
      case ((Float.MIN_VALUE <= Float.MAX_VALUE) ? 627 : 627):
      case ((Float.MIN_VALUE <= Float.POSITIVE_INFINITY) ? 628 : 628):
      case ((Float.MIN_VALUE <= Float.NaN) ? 629 : 629):
      case ((1.0f <= Float.NEGATIVE_INFINITY) ? 630 : 630):
      case ((1.0f <= -1.0f) ? 631 : 631):
      case ((1.0f <= -0.0f) ? 632 : 632):
      case ((1.0f <= 0.0f) ? 633 : 633):
      case ((1.0f <= Float.MIN_VALUE) ? 634 : 634):
      case ((1.0f <= 1.0f) ? 635 : 635):
      case ((1.0f <= Float.MAX_VALUE) ? 636 : 636):
      case ((1.0f <= Float.POSITIVE_INFINITY) ? 637 : 637):
      case ((1.0f <= Float.NaN) ? 638 : 638):
      case ((Float.MAX_VALUE <= Float.NEGATIVE_INFINITY) ? 639 : 639):
      case ((Float.MAX_VALUE <= -1.0f) ? 640 : 640):
      case ((Float.MAX_VALUE <= -0.0f) ? 641 : 641):
      case ((Float.MAX_VALUE <= 0.0f) ? 642 : 642):
      case ((Float.MAX_VALUE <= Float.MIN_VALUE) ? 643 : 643):
      case ((Float.MAX_VALUE <= 1.0f) ? 644 : 644):
      case ((Float.MAX_VALUE <= Float.MAX_VALUE) ? 645 : 645):
      case ((Float.MAX_VALUE <= Float.POSITIVE_INFINITY) ? 646 : 646):
      case ((Float.MAX_VALUE <= Float.NaN) ? 647 : 647):
      case ((Float.POSITIVE_INFINITY <= Float.NEGATIVE_INFINITY) ? 648 : 648):
      case ((Float.POSITIVE_INFINITY <= -1.0f) ? 649 : 649):
      case ((Float.POSITIVE_INFINITY <= -0.0f) ? 650 : 650):
      case ((Float.POSITIVE_INFINITY <= 0.0f) ? 651 : 651):
      case ((Float.POSITIVE_INFINITY <= Float.MIN_VALUE) ? 652 : 652):
      case ((Float.POSITIVE_INFINITY <= 1.0f) ? 653 : 653):
      case ((Float.POSITIVE_INFINITY <= Float.MAX_VALUE) ? 654 : 654):
      case ((Float.POSITIVE_INFINITY <= Float.POSITIVE_INFINITY) ? 655 : 655):
      case ((Float.POSITIVE_INFINITY <= Float.NaN) ? 656 : 656):
      case ((Float.NaN <= Float.NEGATIVE_INFINITY) ? 657 : 657):
      case ((Float.NaN <= -1.0f) ? 658 : 658):
      case ((Float.NaN <= -0.0f) ? 659 : 659):
      case ((Float.NaN <= 0.0f) ? 660 : 660):
      case ((Float.NaN <= Float.MIN_VALUE) ? 661 : 661):
      case ((Float.NaN <= 1.0f) ? 662 : 662):
      case ((Float.NaN <= Float.MAX_VALUE) ? 663 : 663):
      case ((Float.NaN <= Float.POSITIVE_INFINITY) ? 664 : 664):
      case ((Float.NaN <= Float.NaN) ? 665 : 665):
      case ((Float.NEGATIVE_INFINITY >= Float.NEGATIVE_INFINITY) ? 666 : 666):
      case ((Float.NEGATIVE_INFINITY >= -1.0f) ? 667 : 667):
      case ((Float.NEGATIVE_INFINITY >= -0.0f) ? 668 : 668):
      case ((Float.NEGATIVE_INFINITY >= 0.0f) ? 669 : 669):
      case ((Float.NEGATIVE_INFINITY >= Float.MIN_VALUE) ? 670 : 670):
      case ((Float.NEGATIVE_INFINITY >= 1.0f) ? 671 : 671):
      case ((Float.NEGATIVE_INFINITY >= Float.MAX_VALUE) ? 672 : 672):
      case ((Float.NEGATIVE_INFINITY >= Float.POSITIVE_INFINITY) ? 673 : 673):
      case ((Float.NEGATIVE_INFINITY >= Float.NaN) ? 674 : 674):
      case ((-1.0f >= Float.NEGATIVE_INFINITY) ? 675 : 675):
      case ((-1.0f >= -1.0f) ? 676 : 676):
      case ((-1.0f >= -0.0f) ? 677 : 677):
      case ((-1.0f >= 0.0f) ? 678 : 678):
      case ((-1.0f >= Float.MIN_VALUE) ? 679 : 679):
      case ((-1.0f >= 1.0f) ? 680 : 680):
      case ((-1.0f >= Float.MAX_VALUE) ? 681 : 681):
      case ((-1.0f >= Float.POSITIVE_INFINITY) ? 682 : 682):
      case ((-1.0f >= Float.NaN) ? 683 : 683):
      case ((-0.0f >= Float.NEGATIVE_INFINITY) ? 684 : 684):
      case ((-0.0f >= -1.0f) ? 685 : 685):
      case ((-0.0f >= -0.0f) ? 686 : 686):
      case ((-0.0f >= 0.0f) ? 687 : 687):
      case ((-0.0f >= Float.MIN_VALUE) ? 688 : 688):
      case ((-0.0f >= 1.0f) ? 689 : 689):
      case ((-0.0f >= Float.MAX_VALUE) ? 690 : 690):
      case ((-0.0f >= Float.POSITIVE_INFINITY) ? 691 : 691):
      case ((-0.0f >= Float.NaN) ? 692 : 692):
      case ((0.0f >= Float.NEGATIVE_INFINITY) ? 693 : 693):
      case ((0.0f >= -1.0f) ? 694 : 694):
      case ((0.0f >= -0.0f) ? 695 : 695):
      case ((0.0f >= 0.0f) ? 696 : 696):
      case ((0.0f >= Float.MIN_VALUE) ? 697 : 697):
      case ((0.0f >= 1.0f) ? 698 : 698):
      case ((0.0f >= Float.MAX_VALUE) ? 699 : 699):
      case ((0.0f >= Float.POSITIVE_INFINITY) ? 700 : 700):
      case ((0.0f >= Float.NaN) ? 701 : 701):
      case ((Float.MIN_VALUE >= Float.NEGATIVE_INFINITY) ? 702 : 702):
      case ((Float.MIN_VALUE >= -1.0f) ? 703 : 703):
      case ((Float.MIN_VALUE >= -0.0f) ? 704 : 704):
      case ((Float.MIN_VALUE >= 0.0f) ? 705 : 705):
      case ((Float.MIN_VALUE >= Float.MIN_VALUE) ? 706 : 706):
      case ((Float.MIN_VALUE >= 1.0f) ? 707 : 707):
      case ((Float.MIN_VALUE >= Float.MAX_VALUE) ? 708 : 708):
      case ((Float.MIN_VALUE >= Float.POSITIVE_INFINITY) ? 709 : 709):
      case ((Float.MIN_VALUE >= Float.NaN) ? 710 : 710):
      case ((1.0f >= Float.NEGATIVE_INFINITY) ? 711 : 711):
      case ((1.0f >= -1.0f) ? 712 : 712):
      case ((1.0f >= -0.0f) ? 713 : 713):
      case ((1.0f >= 0.0f) ? 714 : 714):
      case ((1.0f >= Float.MIN_VALUE) ? 715 : 715):
      case ((1.0f >= 1.0f) ? 716 : 716):
      case ((1.0f >= Float.MAX_VALUE) ? 717 : 717):
      case ((1.0f >= Float.POSITIVE_INFINITY) ? 718 : 718):
      case ((1.0f >= Float.NaN) ? 719 : 719):
      case ((Float.MAX_VALUE >= Float.NEGATIVE_INFINITY) ? 720 : 720):
      case ((Float.MAX_VALUE >= -1.0f) ? 721 : 721):
      case ((Float.MAX_VALUE >= -0.0f) ? 722 : 722):
      case ((Float.MAX_VALUE >= 0.0f) ? 723 : 723):
      case ((Float.MAX_VALUE >= Float.MIN_VALUE) ? 724 : 724):
      case ((Float.MAX_VALUE >= 1.0f) ? 725 : 725):
      case ((Float.MAX_VALUE >= Float.MAX_VALUE) ? 726 : 726):
      case ((Float.MAX_VALUE >= Float.POSITIVE_INFINITY) ? 727 : 727):
      case ((Float.MAX_VALUE >= Float.NaN) ? 728 : 728):
      case ((Float.POSITIVE_INFINITY >= Float.NEGATIVE_INFINITY) ? 729 : 729):
      case ((Float.POSITIVE_INFINITY >= -1.0f) ? 730 : 730):
      case ((Float.POSITIVE_INFINITY >= -0.0f) ? 731 : 731):
      case ((Float.POSITIVE_INFINITY >= 0.0f) ? 732 : 732):
      case ((Float.POSITIVE_INFINITY >= Float.MIN_VALUE) ? 733 : 733):
      case ((Float.POSITIVE_INFINITY >= 1.0f) ? 734 : 734):
      case ((Float.POSITIVE_INFINITY >= Float.MAX_VALUE) ? 735 : 735):
      case ((Float.POSITIVE_INFINITY >= Float.POSITIVE_INFINITY) ? 736 : 736):
      case ((Float.POSITIVE_INFINITY >= Float.NaN) ? 737 : 737):
      case ((Float.NaN >= Float.NEGATIVE_INFINITY) ? 738 : 738):
      case ((Float.NaN >= -1.0f) ? 739 : 739):
      case ((Float.NaN >= -0.0f) ? 740 : 740):
      case ((Float.NaN >= 0.0f) ? 741 : 741):
      case ((Float.NaN >= Float.MIN_VALUE) ? 742 : 742):
      case ((Float.NaN >= 1.0f) ? 743 : 743):
      case ((Float.NaN >= Float.MAX_VALUE) ? 744 : 744):
      case ((Float.NaN >= Float.POSITIVE_INFINITY) ? 745 : 745):
      case ((Float.NaN >= Float.NaN) ? 746 : 746):
      case ((Float.NEGATIVE_INFINITY == Float.NEGATIVE_INFINITY) ? 747 : 747):
      case ((Float.NEGATIVE_INFINITY == -1.0f) ? 748 : 748):
      case ((Float.NEGATIVE_INFINITY == -0.0f) ? 749 : 749):
      case ((Float.NEGATIVE_INFINITY == 0.0f) ? 750 : 750):
      case ((Float.NEGATIVE_INFINITY == Float.MIN_VALUE) ? 751 : 751):
      case ((Float.NEGATIVE_INFINITY == 1.0f) ? 752 : 752):
      case ((Float.NEGATIVE_INFINITY == Float.MAX_VALUE) ? 753 : 753):
      case ((Float.NEGATIVE_INFINITY == Float.POSITIVE_INFINITY) ? 754 : 754):
      case ((Float.NEGATIVE_INFINITY == Float.NaN) ? 755 : 755):
      case ((-1.0f == Float.NEGATIVE_INFINITY) ? 756 : 756):
      case ((-1.0f == -1.0f) ? 757 : 757):
      case ((-1.0f == -0.0f) ? 758 : 758):
      case ((-1.0f == 0.0f) ? 759 : 759):
      case ((-1.0f == Float.MIN_VALUE) ? 760 : 760):
      case ((-1.0f == 1.0f) ? 761 : 761):
      case ((-1.0f == Float.MAX_VALUE) ? 762 : 762):
      case ((-1.0f == Float.POSITIVE_INFINITY) ? 763 : 763):
      case ((-1.0f == Float.NaN) ? 764 : 764):
      case ((-0.0f == Float.NEGATIVE_INFINITY) ? 765 : 765):
      case ((-0.0f == -1.0f) ? 766 : 766):
      case ((-0.0f == -0.0f) ? 767 : 767):
      case ((-0.0f == 0.0f) ? 768 : 768):
      case ((-0.0f == Float.MIN_VALUE) ? 769 : 769):
      case ((-0.0f == 1.0f) ? 770 : 770):
      case ((-0.0f == Float.MAX_VALUE) ? 771 : 771):
      case ((-0.0f == Float.POSITIVE_INFINITY) ? 772 : 772):
      case ((-0.0f == Float.NaN) ? 773 : 773):
      case ((0.0f == Float.NEGATIVE_INFINITY) ? 774 : 774):
      case ((0.0f == -1.0f) ? 775 : 775):
      case ((0.0f == -0.0f) ? 776 : 776):
      case ((0.0f == 0.0f) ? 777 : 777):
      case ((0.0f == Float.MIN_VALUE) ? 778 : 778):
      case ((0.0f == 1.0f) ? 779 : 779):
      case ((0.0f == Float.MAX_VALUE) ? 780 : 780):
      case ((0.0f == Float.POSITIVE_INFINITY) ? 781 : 781):
      case ((0.0f == Float.NaN) ? 782 : 782):
      case ((Float.MIN_VALUE == Float.NEGATIVE_INFINITY) ? 783 : 783):
      case ((Float.MIN_VALUE == -1.0f) ? 784 : 784):
      case ((Float.MIN_VALUE == -0.0f) ? 785 : 785):
      case ((Float.MIN_VALUE == 0.0f) ? 786 : 786):
      case ((Float.MIN_VALUE == Float.MIN_VALUE) ? 787 : 787):
      case ((Float.MIN_VALUE == 1.0f) ? 788 : 788):
      case ((Float.MIN_VALUE == Float.MAX_VALUE) ? 789 : 789):
      case ((Float.MIN_VALUE == Float.POSITIVE_INFINITY) ? 790 : 790):
      case ((Float.MIN_VALUE == Float.NaN) ? 791 : 791):
      case ((1.0f == Float.NEGATIVE_INFINITY) ? 792 : 792):
      case ((1.0f == -1.0f) ? 793 : 793):
      case ((1.0f == -0.0f) ? 794 : 794):
      case ((1.0f == 0.0f) ? 795 : 795):
      case ((1.0f == Float.MIN_VALUE) ? 796 : 796):
      case ((1.0f == 1.0f) ? 797 : 797):
      case ((1.0f == Float.MAX_VALUE) ? 798 : 798):
      case ((1.0f == Float.POSITIVE_INFINITY) ? 799 : 799):
      case ((1.0f == Float.NaN) ? 800 : 800):
      case ((Float.MAX_VALUE == Float.NEGATIVE_INFINITY) ? 801 : 801):
      case ((Float.MAX_VALUE == -1.0f) ? 802 : 802):
      case ((Float.MAX_VALUE == -0.0f) ? 803 : 803):
      case ((Float.MAX_VALUE == 0.0f) ? 804 : 804):
      case ((Float.MAX_VALUE == Float.MIN_VALUE) ? 805 : 805):
      case ((Float.MAX_VALUE == 1.0f) ? 806 : 806):
      case ((Float.MAX_VALUE == Float.MAX_VALUE) ? 807 : 807):
      case ((Float.MAX_VALUE == Float.POSITIVE_INFINITY) ? 808 : 808):
      case ((Float.MAX_VALUE == Float.NaN) ? 809 : 809):
      case ((Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY) ? 810 : 810):
      case ((Float.POSITIVE_INFINITY == -1.0f) ? 811 : 811):
      case ((Float.POSITIVE_INFINITY == -0.0f) ? 812 : 812):
      case ((Float.POSITIVE_INFINITY == 0.0f) ? 813 : 813):
      case ((Float.POSITIVE_INFINITY == Float.MIN_VALUE) ? 814 : 814):
      case ((Float.POSITIVE_INFINITY == 1.0f) ? 815 : 815):
      case ((Float.POSITIVE_INFINITY == Float.MAX_VALUE) ? 816 : 816):
      case ((Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY) ? 817 : 817):
      case ((Float.POSITIVE_INFINITY == Float.NaN) ? 818 : 818):
      case ((Float.NaN == Float.NEGATIVE_INFINITY) ? 819 : 819):
      case ((Float.NaN == -1.0f) ? 820 : 820):
      case ((Float.NaN == -0.0f) ? 821 : 821):
      case ((Float.NaN == 0.0f) ? 822 : 822):
      case ((Float.NaN == Float.MIN_VALUE) ? 823 : 823):
      case ((Float.NaN == 1.0f) ? 824 : 824):
      case ((Float.NaN == Float.MAX_VALUE) ? 825 : 825):
      case ((Float.NaN == Float.POSITIVE_INFINITY) ? 826 : 826):
      case ((Float.NaN == Float.NaN) ? 827 : 827):
      case ((Float.NEGATIVE_INFINITY != Float.NEGATIVE_INFINITY) ? 828 : 828):
      case ((Float.NEGATIVE_INFINITY != -1.0f) ? 829 : 829):
      case ((Float.NEGATIVE_INFINITY != -0.0f) ? 830 : 830):
      case ((Float.NEGATIVE_INFINITY != 0.0f) ? 831 : 831):
      case ((Float.NEGATIVE_INFINITY != Float.MIN_VALUE) ? 832 : 832):
      case ((Float.NEGATIVE_INFINITY != 1.0f) ? 833 : 833):
      case ((Float.NEGATIVE_INFINITY != Float.MAX_VALUE) ? 834 : 834):
      case ((Float.NEGATIVE_INFINITY != Float.POSITIVE_INFINITY) ? 835 : 835):
      case ((Float.NEGATIVE_INFINITY != Float.NaN) ? 836 : 836):
      case ((-1.0f != Float.NEGATIVE_INFINITY) ? 837 : 837):
      case ((-1.0f != -1.0f) ? 838 : 838):
      case ((-1.0f != -0.0f) ? 839 : 839):
      case ((-1.0f != 0.0f) ? 840 : 840):
      case ((-1.0f != Float.MIN_VALUE) ? 841 : 841):
      case ((-1.0f != 1.0f) ? 842 : 842):
      case ((-1.0f != Float.MAX_VALUE) ? 843 : 843):
      case ((-1.0f != Float.POSITIVE_INFINITY) ? 844 : 844):
      case ((-1.0f != Float.NaN) ? 845 : 845):
      case ((-0.0f != Float.NEGATIVE_INFINITY) ? 846 : 846):
      case ((-0.0f != -1.0f) ? 847 : 847):
      case ((-0.0f != -0.0f) ? 848 : 848):
      case ((-0.0f != 0.0f) ? 849 : 849):
      case ((-0.0f != Float.MIN_VALUE) ? 850 : 850):
      case ((-0.0f != 1.0f) ? 851 : 851):
      case ((-0.0f != Float.MAX_VALUE) ? 852 : 852):
      case ((-0.0f != Float.POSITIVE_INFINITY) ? 853 : 853):
      case ((-0.0f != Float.NaN) ? 854 : 854):
      case ((0.0f != Float.NEGATIVE_INFINITY) ? 855 : 855):
      case ((0.0f != -1.0f) ? 856 : 856):
      case ((0.0f != -0.0f) ? 857 : 857):
      case ((0.0f != 0.0f) ? 858 : 858):
      case ((0.0f != Float.MIN_VALUE) ? 859 : 859):
      case ((0.0f != 1.0f) ? 860 : 860):
      case ((0.0f != Float.MAX_VALUE) ? 861 : 861):
      case ((0.0f != Float.POSITIVE_INFINITY) ? 862 : 862):
      case ((0.0f != Float.NaN) ? 863 : 863):
      case ((Float.MIN_VALUE != Float.NEGATIVE_INFINITY) ? 864 : 864):
      case ((Float.MIN_VALUE != -1.0f) ? 865 : 865):
      case ((Float.MIN_VALUE != -0.0f) ? 866 : 866):
      case ((Float.MIN_VALUE != 0.0f) ? 867 : 867):
      case ((Float.MIN_VALUE != Float.MIN_VALUE) ? 868 : 868):
      case ((Float.MIN_VALUE != 1.0f) ? 869 : 869):
      case ((Float.MIN_VALUE != Float.MAX_VALUE) ? 870 : 870):
      case ((Float.MIN_VALUE != Float.POSITIVE_INFINITY) ? 871 : 871):
      case ((Float.MIN_VALUE != Float.NaN) ? 872 : 872):
      case ((1.0f != Float.NEGATIVE_INFINITY) ? 873 : 873):
      case ((1.0f != -1.0f) ? 874 : 874):
      case ((1.0f != -0.0f) ? 875 : 875):
      case ((1.0f != 0.0f) ? 876 : 876):
      case ((1.0f != Float.MIN_VALUE) ? 877 : 877):
      case ((1.0f != 1.0f) ? 878 : 878):
      case ((1.0f != Float.MAX_VALUE) ? 879 : 879):
      case ((1.0f != Float.POSITIVE_INFINITY) ? 880 : 880):
      case ((1.0f != Float.NaN) ? 881 : 881):
      case ((Float.MAX_VALUE != Float.NEGATIVE_INFINITY) ? 882 : 882):
      case ((Float.MAX_VALUE != -1.0f) ? 883 : 883):
      case ((Float.MAX_VALUE != -0.0f) ? 884 : 884):
      case ((Float.MAX_VALUE != 0.0f) ? 885 : 885):
      case ((Float.MAX_VALUE != Float.MIN_VALUE) ? 886 : 886):
      case ((Float.MAX_VALUE != 1.0f) ? 887 : 887):
      case ((Float.MAX_VALUE != Float.MAX_VALUE) ? 888 : 888):
      case ((Float.MAX_VALUE != Float.POSITIVE_INFINITY) ? 889 : 889):
      case ((Float.MAX_VALUE != Float.NaN) ? 890 : 890):
      case ((Float.POSITIVE_INFINITY != Float.NEGATIVE_INFINITY) ? 891 : 891):
      case ((Float.POSITIVE_INFINITY != -1.0f) ? 892 : 892):
      case ((Float.POSITIVE_INFINITY != -0.0f) ? 893 : 893):
      case ((Float.POSITIVE_INFINITY != 0.0f) ? 894 : 894):
      case ((Float.POSITIVE_INFINITY != Float.MIN_VALUE) ? 895 : 895):
      case ((Float.POSITIVE_INFINITY != 1.0f) ? 896 : 896):
      case ((Float.POSITIVE_INFINITY != Float.MAX_VALUE) ? 897 : 897):
      case ((Float.POSITIVE_INFINITY != Float.POSITIVE_INFINITY) ? 898 : 898):
      case ((Float.POSITIVE_INFINITY != Float.NaN) ? 899 : 899):
      case ((Float.NaN != Float.NEGATIVE_INFINITY) ? 900 : 900):
      case ((Float.NaN != -1.0f) ? 901 : 901):
      case ((Float.NaN != -0.0f) ? 902 : 902):
      case ((Float.NaN != 0.0f) ? 903 : 903):
      case ((Float.NaN != Float.MIN_VALUE) ? 904 : 904):
      case ((Float.NaN != 1.0f) ? 905 : 905):
      case ((Float.NaN != Float.MAX_VALUE) ? 906 : 906):
      case ((Float.NaN != Float.POSITIVE_INFINITY) ? 907 : 907):
      case ((Float.NaN != Float.NaN) ? 908 : 908):
      default:
    }
  }

  // --------
  // double tests
  static double doublePlus(double x) { return (double) + x; }
  static double doubleMinus(double x) { return (double) - x; }
  static double doubleTimes(double x, double y) { return (double) (x * y); }
  static double doubleDiv(double x, double y) { return (double) (x / y); }
  static double doubleRem(double x, double y) { return (double) (x % y); }
  static double doubleAdd(double x, double y) { return (double) (x + y); }
  static double doubleSub(double x, double y) { return (double) (x - y); }
  static boolean doubleLt(double x, double y) { return x < y; }
  static boolean doubleGt(double x, double y) { return x > y; }
  static boolean doubleLe(double x, double y) { return x <= y; }
  static boolean doubleGe(double x, double y) { return x >= y; }
  static boolean doubleEq(double x, double y) { return x == y; }
  static boolean doubleNe(double x, double y) { return x != y; }
  static void doubleTest() {
    Tester.checkEqual(doublePlus(Double.NEGATIVE_INFINITY), (double) + Double.NEGATIVE_INFINITY, "(double) + Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doublePlus(-1.0), (double) + -1.0, "(double) + -1.0");
    Tester.checkEqual(doublePlus(-0.0), (double) + -0.0, "(double) + -0.0");
    Tester.checkEqual(doublePlus(0.0), (double) + 0.0, "(double) + 0.0");
    Tester.checkEqual(doublePlus(1.0), (double) + 1.0, "(double) + 1.0");
    Tester.checkEqual(doublePlus(Double.MAX_VALUE), (double) + Double.MAX_VALUE, "(double) + Double.MAX_VALUE");
    Tester.checkEqual(doublePlus(Double.POSITIVE_INFINITY), (double) + Double.POSITIVE_INFINITY, "(double) + Double.POSITIVE_INFINITY");
    Tester.checkEqual(doublePlus(Double.NaN), (double) + Double.NaN, "(double) + Double.NaN");
    Tester.checkEqual(doubleMinus(Double.NEGATIVE_INFINITY), (double) - Double.NEGATIVE_INFINITY, "(double) - Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleMinus(-1.0), (double) - -1.0, "(double) - -1.0");
    Tester.checkEqual(doubleMinus(-0.0), (double) - -0.0, "(double) - -0.0");
    Tester.checkEqual(doubleMinus(0.0), (double) - 0.0, "(double) - 0.0");
    Tester.checkEqual(doubleMinus(1.0), (double) - 1.0, "(double) - 1.0");
    Tester.checkEqual(doubleMinus(Double.MAX_VALUE), (double) - Double.MAX_VALUE, "(double) - Double.MAX_VALUE");
    Tester.checkEqual(doubleMinus(Double.POSITIVE_INFINITY), (double) - Double.POSITIVE_INFINITY, "(double) - Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleMinus(Double.NaN), (double) - Double.NaN, "(double) - Double.NaN");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY * Double.NEGATIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, -1.0), (double) (Double.NEGATIVE_INFINITY * -1.0), "(double) (Double.NEGATIVE_INFINITY * -1.0)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, -0.0), (double) (Double.NEGATIVE_INFINITY * -0.0), "(double) (Double.NEGATIVE_INFINITY * -0.0)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, 0.0), (double) (Double.NEGATIVE_INFINITY * 0.0), "(double) (Double.NEGATIVE_INFINITY * 0.0)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, 1.0), (double) (Double.NEGATIVE_INFINITY * 1.0), "(double) (Double.NEGATIVE_INFINITY * 1.0)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), (double) (Double.NEGATIVE_INFINITY * Double.MAX_VALUE), "(double) (Double.NEGATIVE_INFINITY * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.NEGATIVE_INFINITY, Double.NaN), (double) (Double.NEGATIVE_INFINITY * Double.NaN), "(double) (Double.NEGATIVE_INFINITY * Double.NaN)");
    Tester.checkEqual(doubleTimes(-1.0, Double.NEGATIVE_INFINITY), (double) (-1.0 * Double.NEGATIVE_INFINITY), "(double) (-1.0 * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(-1.0, -1.0), (double) (-1.0 * -1.0), "(double) (-1.0 * -1.0)");
    Tester.checkEqual(doubleTimes(-1.0, -0.0), (double) (-1.0 * -0.0), "(double) (-1.0 * -0.0)");
    Tester.checkEqual(doubleTimes(-1.0, 0.0), (double) (-1.0 * 0.0), "(double) (-1.0 * 0.0)");
    Tester.checkEqual(doubleTimes(-1.0, 1.0), (double) (-1.0 * 1.0), "(double) (-1.0 * 1.0)");
    Tester.checkEqual(doubleTimes(-1.0, Double.MAX_VALUE), (double) (-1.0 * Double.MAX_VALUE), "(double) (-1.0 * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(-1.0, Double.POSITIVE_INFINITY), (double) (-1.0 * Double.POSITIVE_INFINITY), "(double) (-1.0 * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(-1.0, Double.NaN), (double) (-1.0 * Double.NaN), "(double) (-1.0 * Double.NaN)");
    Tester.checkEqual(doubleTimes(-0.0, Double.NEGATIVE_INFINITY), (double) (-0.0 * Double.NEGATIVE_INFINITY), "(double) (-0.0 * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(-0.0, -1.0), (double) (-0.0 * -1.0), "(double) (-0.0 * -1.0)");
    Tester.checkEqual(doubleTimes(-0.0, -0.0), (double) (-0.0 * -0.0), "(double) (-0.0 * -0.0)");
    Tester.checkEqual(doubleTimes(-0.0, 0.0), (double) (-0.0 * 0.0), "(double) (-0.0 * 0.0)");
    Tester.checkEqual(doubleTimes(-0.0, 1.0), (double) (-0.0 * 1.0), "(double) (-0.0 * 1.0)");
    Tester.checkEqual(doubleTimes(-0.0, Double.MAX_VALUE), (double) (-0.0 * Double.MAX_VALUE), "(double) (-0.0 * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(-0.0, Double.POSITIVE_INFINITY), (double) (-0.0 * Double.POSITIVE_INFINITY), "(double) (-0.0 * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(-0.0, Double.NaN), (double) (-0.0 * Double.NaN), "(double) (-0.0 * Double.NaN)");
    Tester.checkEqual(doubleTimes(0.0, Double.NEGATIVE_INFINITY), (double) (0.0 * Double.NEGATIVE_INFINITY), "(double) (0.0 * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(0.0, -1.0), (double) (0.0 * -1.0), "(double) (0.0 * -1.0)");
    Tester.checkEqual(doubleTimes(0.0, -0.0), (double) (0.0 * -0.0), "(double) (0.0 * -0.0)");
    Tester.checkEqual(doubleTimes(0.0, 0.0), (double) (0.0 * 0.0), "(double) (0.0 * 0.0)");
    Tester.checkEqual(doubleTimes(0.0, 1.0), (double) (0.0 * 1.0), "(double) (0.0 * 1.0)");
    Tester.checkEqual(doubleTimes(0.0, Double.MAX_VALUE), (double) (0.0 * Double.MAX_VALUE), "(double) (0.0 * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(0.0, Double.POSITIVE_INFINITY), (double) (0.0 * Double.POSITIVE_INFINITY), "(double) (0.0 * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(0.0, Double.NaN), (double) (0.0 * Double.NaN), "(double) (0.0 * Double.NaN)");
    Tester.checkEqual(doubleTimes(1.0, Double.NEGATIVE_INFINITY), (double) (1.0 * Double.NEGATIVE_INFINITY), "(double) (1.0 * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(1.0, -1.0), (double) (1.0 * -1.0), "(double) (1.0 * -1.0)");
    Tester.checkEqual(doubleTimes(1.0, -0.0), (double) (1.0 * -0.0), "(double) (1.0 * -0.0)");
    Tester.checkEqual(doubleTimes(1.0, 0.0), (double) (1.0 * 0.0), "(double) (1.0 * 0.0)");
    Tester.checkEqual(doubleTimes(1.0, 1.0), (double) (1.0 * 1.0), "(double) (1.0 * 1.0)");
    Tester.checkEqual(doubleTimes(1.0, Double.MAX_VALUE), (double) (1.0 * Double.MAX_VALUE), "(double) (1.0 * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(1.0, Double.POSITIVE_INFINITY), (double) (1.0 * Double.POSITIVE_INFINITY), "(double) (1.0 * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(1.0, Double.NaN), (double) (1.0 * Double.NaN), "(double) (1.0 * Double.NaN)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), (double) (Double.MAX_VALUE * Double.NEGATIVE_INFINITY), "(double) (Double.MAX_VALUE * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, -1.0), (double) (Double.MAX_VALUE * -1.0), "(double) (Double.MAX_VALUE * -1.0)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, -0.0), (double) (Double.MAX_VALUE * -0.0), "(double) (Double.MAX_VALUE * -0.0)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, 0.0), (double) (Double.MAX_VALUE * 0.0), "(double) (Double.MAX_VALUE * 0.0)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, 1.0), (double) (Double.MAX_VALUE * 1.0), "(double) (Double.MAX_VALUE * 1.0)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, Double.MAX_VALUE), (double) (Double.MAX_VALUE * Double.MAX_VALUE), "(double) (Double.MAX_VALUE * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, Double.POSITIVE_INFINITY), (double) (Double.MAX_VALUE * Double.POSITIVE_INFINITY), "(double) (Double.MAX_VALUE * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.MAX_VALUE, Double.NaN), (double) (Double.MAX_VALUE * Double.NaN), "(double) (Double.MAX_VALUE * Double.NaN)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, -1.0), (double) (Double.POSITIVE_INFINITY * -1.0), "(double) (Double.POSITIVE_INFINITY * -1.0)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, -0.0), (double) (Double.POSITIVE_INFINITY * -0.0), "(double) (Double.POSITIVE_INFINITY * -0.0)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, 0.0), (double) (Double.POSITIVE_INFINITY * 0.0), "(double) (Double.POSITIVE_INFINITY * 0.0)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, 1.0), (double) (Double.POSITIVE_INFINITY * 1.0), "(double) (Double.POSITIVE_INFINITY * 1.0)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, Double.MAX_VALUE), (double) (Double.POSITIVE_INFINITY * Double.MAX_VALUE), "(double) (Double.POSITIVE_INFINITY * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.POSITIVE_INFINITY, Double.NaN), (double) (Double.POSITIVE_INFINITY * Double.NaN), "(double) (Double.POSITIVE_INFINITY * Double.NaN)");
    Tester.checkEqual(doubleTimes(Double.NaN, Double.NEGATIVE_INFINITY), (double) (Double.NaN * Double.NEGATIVE_INFINITY), "(double) (Double.NaN * Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.NaN, -1.0), (double) (Double.NaN * -1.0), "(double) (Double.NaN * -1.0)");
    Tester.checkEqual(doubleTimes(Double.NaN, -0.0), (double) (Double.NaN * -0.0), "(double) (Double.NaN * -0.0)");
    Tester.checkEqual(doubleTimes(Double.NaN, 0.0), (double) (Double.NaN * 0.0), "(double) (Double.NaN * 0.0)");
    Tester.checkEqual(doubleTimes(Double.NaN, 1.0), (double) (Double.NaN * 1.0), "(double) (Double.NaN * 1.0)");
    Tester.checkEqual(doubleTimes(Double.NaN, Double.MAX_VALUE), (double) (Double.NaN * Double.MAX_VALUE), "(double) (Double.NaN * Double.MAX_VALUE)");
    Tester.checkEqual(doubleTimes(Double.NaN, Double.POSITIVE_INFINITY), (double) (Double.NaN * Double.POSITIVE_INFINITY), "(double) (Double.NaN * Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleTimes(Double.NaN, Double.NaN), (double) (Double.NaN * Double.NaN), "(double) (Double.NaN * Double.NaN)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, -1.0), (double) (Double.NEGATIVE_INFINITY / -1.0), "(double) (Double.NEGATIVE_INFINITY / -1.0)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, -0.0), (double) (Double.NEGATIVE_INFINITY / -0.0), "(double) (Double.NEGATIVE_INFINITY / -0.0)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, 0.0), (double) (Double.NEGATIVE_INFINITY / 0.0), "(double) (Double.NEGATIVE_INFINITY / 0.0)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, 1.0), (double) (Double.NEGATIVE_INFINITY / 1.0), "(double) (Double.NEGATIVE_INFINITY / 1.0)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), (double) (Double.NEGATIVE_INFINITY / Double.MAX_VALUE), "(double) (Double.NEGATIVE_INFINITY / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.NEGATIVE_INFINITY, Double.NaN), (double) (Double.NEGATIVE_INFINITY / Double.NaN), "(double) (Double.NEGATIVE_INFINITY / Double.NaN)");
    Tester.checkEqual(doubleDiv(-1.0, Double.NEGATIVE_INFINITY), (double) (-1.0 / Double.NEGATIVE_INFINITY), "(double) (-1.0 / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(-1.0, -1.0), (double) (-1.0 / -1.0), "(double) (-1.0 / -1.0)");
    Tester.checkEqual(doubleDiv(-1.0, -0.0), (double) (-1.0 / -0.0), "(double) (-1.0 / -0.0)");
    Tester.checkEqual(doubleDiv(-1.0, 0.0), (double) (-1.0 / 0.0), "(double) (-1.0 / 0.0)");
    Tester.checkEqual(doubleDiv(-1.0, 1.0), (double) (-1.0 / 1.0), "(double) (-1.0 / 1.0)");
    Tester.checkEqual(doubleDiv(-1.0, Double.MAX_VALUE), (double) (-1.0 / Double.MAX_VALUE), "(double) (-1.0 / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(-1.0, Double.POSITIVE_INFINITY), (double) (-1.0 / Double.POSITIVE_INFINITY), "(double) (-1.0 / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(-1.0, Double.NaN), (double) (-1.0 / Double.NaN), "(double) (-1.0 / Double.NaN)");
    Tester.checkEqual(doubleDiv(-0.0, Double.NEGATIVE_INFINITY), (double) (-0.0 / Double.NEGATIVE_INFINITY), "(double) (-0.0 / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(-0.0, -1.0), (double) (-0.0 / -1.0), "(double) (-0.0 / -1.0)");
    Tester.checkEqual(doubleDiv(-0.0, -0.0), (double) (-0.0 / -0.0), "(double) (-0.0 / -0.0)");
    Tester.checkEqual(doubleDiv(-0.0, 0.0), (double) (-0.0 / 0.0), "(double) (-0.0 / 0.0)");
    Tester.checkEqual(doubleDiv(-0.0, 1.0), (double) (-0.0 / 1.0), "(double) (-0.0 / 1.0)");
    Tester.checkEqual(doubleDiv(-0.0, Double.MAX_VALUE), (double) (-0.0 / Double.MAX_VALUE), "(double) (-0.0 / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(-0.0, Double.POSITIVE_INFINITY), (double) (-0.0 / Double.POSITIVE_INFINITY), "(double) (-0.0 / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(-0.0, Double.NaN), (double) (-0.0 / Double.NaN), "(double) (-0.0 / Double.NaN)");
    Tester.checkEqual(doubleDiv(0.0, Double.NEGATIVE_INFINITY), (double) (0.0 / Double.NEGATIVE_INFINITY), "(double) (0.0 / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(0.0, -1.0), (double) (0.0 / -1.0), "(double) (0.0 / -1.0)");
    Tester.checkEqual(doubleDiv(0.0, -0.0), (double) (0.0 / -0.0), "(double) (0.0 / -0.0)");
    Tester.checkEqual(doubleDiv(0.0, 0.0), (double) (0.0 / 0.0), "(double) (0.0 / 0.0)");
    Tester.checkEqual(doubleDiv(0.0, 1.0), (double) (0.0 / 1.0), "(double) (0.0 / 1.0)");
    Tester.checkEqual(doubleDiv(0.0, Double.MAX_VALUE), (double) (0.0 / Double.MAX_VALUE), "(double) (0.0 / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(0.0, Double.POSITIVE_INFINITY), (double) (0.0 / Double.POSITIVE_INFINITY), "(double) (0.0 / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(0.0, Double.NaN), (double) (0.0 / Double.NaN), "(double) (0.0 / Double.NaN)");
    Tester.checkEqual(doubleDiv(1.0, Double.NEGATIVE_INFINITY), (double) (1.0 / Double.NEGATIVE_INFINITY), "(double) (1.0 / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(1.0, -1.0), (double) (1.0 / -1.0), "(double) (1.0 / -1.0)");
    Tester.checkEqual(doubleDiv(1.0, -0.0), (double) (1.0 / -0.0), "(double) (1.0 / -0.0)");
    Tester.checkEqual(doubleDiv(1.0, 0.0), (double) (1.0 / 0.0), "(double) (1.0 / 0.0)");
    Tester.checkEqual(doubleDiv(1.0, 1.0), (double) (1.0 / 1.0), "(double) (1.0 / 1.0)");
    Tester.checkEqual(doubleDiv(1.0, Double.MAX_VALUE), (double) (1.0 / Double.MAX_VALUE), "(double) (1.0 / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(1.0, Double.POSITIVE_INFINITY), (double) (1.0 / Double.POSITIVE_INFINITY), "(double) (1.0 / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(1.0, Double.NaN), (double) (1.0 / Double.NaN), "(double) (1.0 / Double.NaN)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), (double) (Double.MAX_VALUE / Double.NEGATIVE_INFINITY), "(double) (Double.MAX_VALUE / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, -1.0), (double) (Double.MAX_VALUE / -1.0), "(double) (Double.MAX_VALUE / -1.0)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, -0.0), (double) (Double.MAX_VALUE / -0.0), "(double) (Double.MAX_VALUE / -0.0)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, 0.0), (double) (Double.MAX_VALUE / 0.0), "(double) (Double.MAX_VALUE / 0.0)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, 1.0), (double) (Double.MAX_VALUE / 1.0), "(double) (Double.MAX_VALUE / 1.0)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, Double.MAX_VALUE), (double) (Double.MAX_VALUE / Double.MAX_VALUE), "(double) (Double.MAX_VALUE / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, Double.POSITIVE_INFINITY), (double) (Double.MAX_VALUE / Double.POSITIVE_INFINITY), "(double) (Double.MAX_VALUE / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.MAX_VALUE, Double.NaN), (double) (Double.MAX_VALUE / Double.NaN), "(double) (Double.MAX_VALUE / Double.NaN)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, -1.0), (double) (Double.POSITIVE_INFINITY / -1.0), "(double) (Double.POSITIVE_INFINITY / -1.0)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, -0.0), (double) (Double.POSITIVE_INFINITY / -0.0), "(double) (Double.POSITIVE_INFINITY / -0.0)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, 0.0), (double) (Double.POSITIVE_INFINITY / 0.0), "(double) (Double.POSITIVE_INFINITY / 0.0)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, 1.0), (double) (Double.POSITIVE_INFINITY / 1.0), "(double) (Double.POSITIVE_INFINITY / 1.0)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, Double.MAX_VALUE), (double) (Double.POSITIVE_INFINITY / Double.MAX_VALUE), "(double) (Double.POSITIVE_INFINITY / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.POSITIVE_INFINITY, Double.NaN), (double) (Double.POSITIVE_INFINITY / Double.NaN), "(double) (Double.POSITIVE_INFINITY / Double.NaN)");
    Tester.checkEqual(doubleDiv(Double.NaN, Double.NEGATIVE_INFINITY), (double) (Double.NaN / Double.NEGATIVE_INFINITY), "(double) (Double.NaN / Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.NaN, -1.0), (double) (Double.NaN / -1.0), "(double) (Double.NaN / -1.0)");
    Tester.checkEqual(doubleDiv(Double.NaN, -0.0), (double) (Double.NaN / -0.0), "(double) (Double.NaN / -0.0)");
    Tester.checkEqual(doubleDiv(Double.NaN, 0.0), (double) (Double.NaN / 0.0), "(double) (Double.NaN / 0.0)");
    Tester.checkEqual(doubleDiv(Double.NaN, 1.0), (double) (Double.NaN / 1.0), "(double) (Double.NaN / 1.0)");
    Tester.checkEqual(doubleDiv(Double.NaN, Double.MAX_VALUE), (double) (Double.NaN / Double.MAX_VALUE), "(double) (Double.NaN / Double.MAX_VALUE)");
    Tester.checkEqual(doubleDiv(Double.NaN, Double.POSITIVE_INFINITY), (double) (Double.NaN / Double.POSITIVE_INFINITY), "(double) (Double.NaN / Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleDiv(Double.NaN, Double.NaN), (double) (Double.NaN / Double.NaN), "(double) (Double.NaN / Double.NaN)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY % Double.NEGATIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, -1.0), (double) (Double.NEGATIVE_INFINITY % -1.0), "(double) (Double.NEGATIVE_INFINITY % -1.0)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, -0.0), (double) (Double.NEGATIVE_INFINITY % -0.0), "(double) (Double.NEGATIVE_INFINITY % -0.0)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, 0.0), (double) (Double.NEGATIVE_INFINITY % 0.0), "(double) (Double.NEGATIVE_INFINITY % 0.0)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, 1.0), (double) (Double.NEGATIVE_INFINITY % 1.0), "(double) (Double.NEGATIVE_INFINITY % 1.0)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), (double) (Double.NEGATIVE_INFINITY % Double.MAX_VALUE), "(double) (Double.NEGATIVE_INFINITY % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY % Double.POSITIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.NEGATIVE_INFINITY, Double.NaN), (double) (Double.NEGATIVE_INFINITY % Double.NaN), "(double) (Double.NEGATIVE_INFINITY % Double.NaN)");
    Tester.checkEqual(doubleRem(-1.0, Double.NEGATIVE_INFINITY), (double) (-1.0 % Double.NEGATIVE_INFINITY), "(double) (-1.0 % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(-1.0, -1.0), (double) (-1.0 % -1.0), "(double) (-1.0 % -1.0)");
    Tester.checkEqual(doubleRem(-1.0, -0.0), (double) (-1.0 % -0.0), "(double) (-1.0 % -0.0)");
    Tester.checkEqual(doubleRem(-1.0, 0.0), (double) (-1.0 % 0.0), "(double) (-1.0 % 0.0)");
    Tester.checkEqual(doubleRem(-1.0, 1.0), (double) (-1.0 % 1.0), "(double) (-1.0 % 1.0)");
    Tester.checkEqual(doubleRem(-1.0, Double.MAX_VALUE), (double) (-1.0 % Double.MAX_VALUE), "(double) (-1.0 % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(-1.0, Double.POSITIVE_INFINITY), (double) (-1.0 % Double.POSITIVE_INFINITY), "(double) (-1.0 % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(-1.0, Double.NaN), (double) (-1.0 % Double.NaN), "(double) (-1.0 % Double.NaN)");
    Tester.checkEqual(doubleRem(-0.0, Double.NEGATIVE_INFINITY), (double) (-0.0 % Double.NEGATIVE_INFINITY), "(double) (-0.0 % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(-0.0, -1.0), (double) (-0.0 % -1.0), "(double) (-0.0 % -1.0)");
    Tester.checkEqual(doubleRem(-0.0, -0.0), (double) (-0.0 % -0.0), "(double) (-0.0 % -0.0)");
    Tester.checkEqual(doubleRem(-0.0, 0.0), (double) (-0.0 % 0.0), "(double) (-0.0 % 0.0)");
    Tester.checkEqual(doubleRem(-0.0, 1.0), (double) (-0.0 % 1.0), "(double) (-0.0 % 1.0)");
    Tester.checkEqual(doubleRem(-0.0, Double.MAX_VALUE), (double) (-0.0 % Double.MAX_VALUE), "(double) (-0.0 % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(-0.0, Double.POSITIVE_INFINITY), (double) (-0.0 % Double.POSITIVE_INFINITY), "(double) (-0.0 % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(-0.0, Double.NaN), (double) (-0.0 % Double.NaN), "(double) (-0.0 % Double.NaN)");
    Tester.checkEqual(doubleRem(0.0, Double.NEGATIVE_INFINITY), (double) (0.0 % Double.NEGATIVE_INFINITY), "(double) (0.0 % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(0.0, -1.0), (double) (0.0 % -1.0), "(double) (0.0 % -1.0)");
    Tester.checkEqual(doubleRem(0.0, -0.0), (double) (0.0 % -0.0), "(double) (0.0 % -0.0)");
    Tester.checkEqual(doubleRem(0.0, 0.0), (double) (0.0 % 0.0), "(double) (0.0 % 0.0)");
    Tester.checkEqual(doubleRem(0.0, 1.0), (double) (0.0 % 1.0), "(double) (0.0 % 1.0)");
    Tester.checkEqual(doubleRem(0.0, Double.MAX_VALUE), (double) (0.0 % Double.MAX_VALUE), "(double) (0.0 % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(0.0, Double.POSITIVE_INFINITY), (double) (0.0 % Double.POSITIVE_INFINITY), "(double) (0.0 % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(0.0, Double.NaN), (double) (0.0 % Double.NaN), "(double) (0.0 % Double.NaN)");
    Tester.checkEqual(doubleRem(1.0, Double.NEGATIVE_INFINITY), (double) (1.0 % Double.NEGATIVE_INFINITY), "(double) (1.0 % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(1.0, -1.0), (double) (1.0 % -1.0), "(double) (1.0 % -1.0)");
    Tester.checkEqual(doubleRem(1.0, -0.0), (double) (1.0 % -0.0), "(double) (1.0 % -0.0)");
    Tester.checkEqual(doubleRem(1.0, 0.0), (double) (1.0 % 0.0), "(double) (1.0 % 0.0)");
    Tester.checkEqual(doubleRem(1.0, 1.0), (double) (1.0 % 1.0), "(double) (1.0 % 1.0)");
    Tester.checkEqual(doubleRem(1.0, Double.MAX_VALUE), (double) (1.0 % Double.MAX_VALUE), "(double) (1.0 % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(1.0, Double.POSITIVE_INFINITY), (double) (1.0 % Double.POSITIVE_INFINITY), "(double) (1.0 % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(1.0, Double.NaN), (double) (1.0 % Double.NaN), "(double) (1.0 % Double.NaN)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), (double) (Double.MAX_VALUE % Double.NEGATIVE_INFINITY), "(double) (Double.MAX_VALUE % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, -1.0), (double) (Double.MAX_VALUE % -1.0), "(double) (Double.MAX_VALUE % -1.0)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, -0.0), (double) (Double.MAX_VALUE % -0.0), "(double) (Double.MAX_VALUE % -0.0)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, 0.0), (double) (Double.MAX_VALUE % 0.0), "(double) (Double.MAX_VALUE % 0.0)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, 1.0), (double) (Double.MAX_VALUE % 1.0), "(double) (Double.MAX_VALUE % 1.0)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, Double.MAX_VALUE), (double) (Double.MAX_VALUE % Double.MAX_VALUE), "(double) (Double.MAX_VALUE % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, Double.POSITIVE_INFINITY), (double) (Double.MAX_VALUE % Double.POSITIVE_INFINITY), "(double) (Double.MAX_VALUE % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.MAX_VALUE, Double.NaN), (double) (Double.MAX_VALUE % Double.NaN), "(double) (Double.MAX_VALUE % Double.NaN)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.POSITIVE_INFINITY % Double.NEGATIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, -1.0), (double) (Double.POSITIVE_INFINITY % -1.0), "(double) (Double.POSITIVE_INFINITY % -1.0)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, -0.0), (double) (Double.POSITIVE_INFINITY % -0.0), "(double) (Double.POSITIVE_INFINITY % -0.0)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, 0.0), (double) (Double.POSITIVE_INFINITY % 0.0), "(double) (Double.POSITIVE_INFINITY % 0.0)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, 1.0), (double) (Double.POSITIVE_INFINITY % 1.0), "(double) (Double.POSITIVE_INFINITY % 1.0)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, Double.MAX_VALUE), (double) (Double.POSITIVE_INFINITY % Double.MAX_VALUE), "(double) (Double.POSITIVE_INFINITY % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.POSITIVE_INFINITY % Double.POSITIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.POSITIVE_INFINITY, Double.NaN), (double) (Double.POSITIVE_INFINITY % Double.NaN), "(double) (Double.POSITIVE_INFINITY % Double.NaN)");
    Tester.checkEqual(doubleRem(Double.NaN, Double.NEGATIVE_INFINITY), (double) (Double.NaN % Double.NEGATIVE_INFINITY), "(double) (Double.NaN % Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.NaN, -1.0), (double) (Double.NaN % -1.0), "(double) (Double.NaN % -1.0)");
    Tester.checkEqual(doubleRem(Double.NaN, -0.0), (double) (Double.NaN % -0.0), "(double) (Double.NaN % -0.0)");
    Tester.checkEqual(doubleRem(Double.NaN, 0.0), (double) (Double.NaN % 0.0), "(double) (Double.NaN % 0.0)");
    Tester.checkEqual(doubleRem(Double.NaN, 1.0), (double) (Double.NaN % 1.0), "(double) (Double.NaN % 1.0)");
    Tester.checkEqual(doubleRem(Double.NaN, Double.MAX_VALUE), (double) (Double.NaN % Double.MAX_VALUE), "(double) (Double.NaN % Double.MAX_VALUE)");
    Tester.checkEqual(doubleRem(Double.NaN, Double.POSITIVE_INFINITY), (double) (Double.NaN % Double.POSITIVE_INFINITY), "(double) (Double.NaN % Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleRem(Double.NaN, Double.NaN), (double) (Double.NaN % Double.NaN), "(double) (Double.NaN % Double.NaN)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, -1.0), (double) (Double.NEGATIVE_INFINITY + -1.0), "(double) (Double.NEGATIVE_INFINITY + -1.0)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, -0.0), (double) (Double.NEGATIVE_INFINITY + -0.0), "(double) (Double.NEGATIVE_INFINITY + -0.0)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, 0.0), (double) (Double.NEGATIVE_INFINITY + 0.0), "(double) (Double.NEGATIVE_INFINITY + 0.0)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, 1.0), (double) (Double.NEGATIVE_INFINITY + 1.0), "(double) (Double.NEGATIVE_INFINITY + 1.0)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), (double) (Double.NEGATIVE_INFINITY + Double.MAX_VALUE), "(double) (Double.NEGATIVE_INFINITY + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.NEGATIVE_INFINITY, Double.NaN), (double) (Double.NEGATIVE_INFINITY + Double.NaN), "(double) (Double.NEGATIVE_INFINITY + Double.NaN)");
    Tester.checkEqual(doubleAdd(-1.0, Double.NEGATIVE_INFINITY), (double) (-1.0 + Double.NEGATIVE_INFINITY), "(double) (-1.0 + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(-1.0, -1.0), (double) (-1.0 + -1.0), "(double) (-1.0 + -1.0)");
    Tester.checkEqual(doubleAdd(-1.0, -0.0), (double) (-1.0 + -0.0), "(double) (-1.0 + -0.0)");
    Tester.checkEqual(doubleAdd(-1.0, 0.0), (double) (-1.0 + 0.0), "(double) (-1.0 + 0.0)");
    Tester.checkEqual(doubleAdd(-1.0, 1.0), (double) (-1.0 + 1.0), "(double) (-1.0 + 1.0)");
    Tester.checkEqual(doubleAdd(-1.0, Double.MAX_VALUE), (double) (-1.0 + Double.MAX_VALUE), "(double) (-1.0 + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(-1.0, Double.POSITIVE_INFINITY), (double) (-1.0 + Double.POSITIVE_INFINITY), "(double) (-1.0 + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(-1.0, Double.NaN), (double) (-1.0 + Double.NaN), "(double) (-1.0 + Double.NaN)");
    Tester.checkEqual(doubleAdd(-0.0, Double.NEGATIVE_INFINITY), (double) (-0.0 + Double.NEGATIVE_INFINITY), "(double) (-0.0 + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(-0.0, -1.0), (double) (-0.0 + -1.0), "(double) (-0.0 + -1.0)");
    Tester.checkEqual(doubleAdd(-0.0, -0.0), (double) (-0.0 + -0.0), "(double) (-0.0 + -0.0)");
    Tester.checkEqual(doubleAdd(-0.0, 0.0), (double) (-0.0 + 0.0), "(double) (-0.0 + 0.0)");
    Tester.checkEqual(doubleAdd(-0.0, 1.0), (double) (-0.0 + 1.0), "(double) (-0.0 + 1.0)");
    Tester.checkEqual(doubleAdd(-0.0, Double.MAX_VALUE), (double) (-0.0 + Double.MAX_VALUE), "(double) (-0.0 + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(-0.0, Double.POSITIVE_INFINITY), (double) (-0.0 + Double.POSITIVE_INFINITY), "(double) (-0.0 + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(-0.0, Double.NaN), (double) (-0.0 + Double.NaN), "(double) (-0.0 + Double.NaN)");
    Tester.checkEqual(doubleAdd(0.0, Double.NEGATIVE_INFINITY), (double) (0.0 + Double.NEGATIVE_INFINITY), "(double) (0.0 + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(0.0, -1.0), (double) (0.0 + -1.0), "(double) (0.0 + -1.0)");
    Tester.checkEqual(doubleAdd(0.0, -0.0), (double) (0.0 + -0.0), "(double) (0.0 + -0.0)");
    Tester.checkEqual(doubleAdd(0.0, 0.0), (double) (0.0 + 0.0), "(double) (0.0 + 0.0)");
    Tester.checkEqual(doubleAdd(0.0, 1.0), (double) (0.0 + 1.0), "(double) (0.0 + 1.0)");
    Tester.checkEqual(doubleAdd(0.0, Double.MAX_VALUE), (double) (0.0 + Double.MAX_VALUE), "(double) (0.0 + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(0.0, Double.POSITIVE_INFINITY), (double) (0.0 + Double.POSITIVE_INFINITY), "(double) (0.0 + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(0.0, Double.NaN), (double) (0.0 + Double.NaN), "(double) (0.0 + Double.NaN)");
    Tester.checkEqual(doubleAdd(1.0, Double.NEGATIVE_INFINITY), (double) (1.0 + Double.NEGATIVE_INFINITY), "(double) (1.0 + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(1.0, -1.0), (double) (1.0 + -1.0), "(double) (1.0 + -1.0)");
    Tester.checkEqual(doubleAdd(1.0, -0.0), (double) (1.0 + -0.0), "(double) (1.0 + -0.0)");
    Tester.checkEqual(doubleAdd(1.0, 0.0), (double) (1.0 + 0.0), "(double) (1.0 + 0.0)");
    Tester.checkEqual(doubleAdd(1.0, 1.0), (double) (1.0 + 1.0), "(double) (1.0 + 1.0)");
    Tester.checkEqual(doubleAdd(1.0, Double.MAX_VALUE), (double) (1.0 + Double.MAX_VALUE), "(double) (1.0 + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(1.0, Double.POSITIVE_INFINITY), (double) (1.0 + Double.POSITIVE_INFINITY), "(double) (1.0 + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(1.0, Double.NaN), (double) (1.0 + Double.NaN), "(double) (1.0 + Double.NaN)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), (double) (Double.MAX_VALUE + Double.NEGATIVE_INFINITY), "(double) (Double.MAX_VALUE + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, -1.0), (double) (Double.MAX_VALUE + -1.0), "(double) (Double.MAX_VALUE + -1.0)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, -0.0), (double) (Double.MAX_VALUE + -0.0), "(double) (Double.MAX_VALUE + -0.0)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, 0.0), (double) (Double.MAX_VALUE + 0.0), "(double) (Double.MAX_VALUE + 0.0)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, 1.0), (double) (Double.MAX_VALUE + 1.0), "(double) (Double.MAX_VALUE + 1.0)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, Double.MAX_VALUE), (double) (Double.MAX_VALUE + Double.MAX_VALUE), "(double) (Double.MAX_VALUE + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, Double.POSITIVE_INFINITY), (double) (Double.MAX_VALUE + Double.POSITIVE_INFINITY), "(double) (Double.MAX_VALUE + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.MAX_VALUE, Double.NaN), (double) (Double.MAX_VALUE + Double.NaN), "(double) (Double.MAX_VALUE + Double.NaN)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, -1.0), (double) (Double.POSITIVE_INFINITY + -1.0), "(double) (Double.POSITIVE_INFINITY + -1.0)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, -0.0), (double) (Double.POSITIVE_INFINITY + -0.0), "(double) (Double.POSITIVE_INFINITY + -0.0)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, 0.0), (double) (Double.POSITIVE_INFINITY + 0.0), "(double) (Double.POSITIVE_INFINITY + 0.0)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, 1.0), (double) (Double.POSITIVE_INFINITY + 1.0), "(double) (Double.POSITIVE_INFINITY + 1.0)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, Double.MAX_VALUE), (double) (Double.POSITIVE_INFINITY + Double.MAX_VALUE), "(double) (Double.POSITIVE_INFINITY + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.POSITIVE_INFINITY + Double.POSITIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.POSITIVE_INFINITY, Double.NaN), (double) (Double.POSITIVE_INFINITY + Double.NaN), "(double) (Double.POSITIVE_INFINITY + Double.NaN)");
    Tester.checkEqual(doubleAdd(Double.NaN, Double.NEGATIVE_INFINITY), (double) (Double.NaN + Double.NEGATIVE_INFINITY), "(double) (Double.NaN + Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.NaN, -1.0), (double) (Double.NaN + -1.0), "(double) (Double.NaN + -1.0)");
    Tester.checkEqual(doubleAdd(Double.NaN, -0.0), (double) (Double.NaN + -0.0), "(double) (Double.NaN + -0.0)");
    Tester.checkEqual(doubleAdd(Double.NaN, 0.0), (double) (Double.NaN + 0.0), "(double) (Double.NaN + 0.0)");
    Tester.checkEqual(doubleAdd(Double.NaN, 1.0), (double) (Double.NaN + 1.0), "(double) (Double.NaN + 1.0)");
    Tester.checkEqual(doubleAdd(Double.NaN, Double.MAX_VALUE), (double) (Double.NaN + Double.MAX_VALUE), "(double) (Double.NaN + Double.MAX_VALUE)");
    Tester.checkEqual(doubleAdd(Double.NaN, Double.POSITIVE_INFINITY), (double) (Double.NaN + Double.POSITIVE_INFINITY), "(double) (Double.NaN + Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleAdd(Double.NaN, Double.NaN), (double) (Double.NaN + Double.NaN), "(double) (Double.NaN + Double.NaN)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY - Double.NEGATIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, -1.0), (double) (Double.NEGATIVE_INFINITY - -1.0), "(double) (Double.NEGATIVE_INFINITY - -1.0)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, -0.0), (double) (Double.NEGATIVE_INFINITY - -0.0), "(double) (Double.NEGATIVE_INFINITY - -0.0)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, 0.0), (double) (Double.NEGATIVE_INFINITY - 0.0), "(double) (Double.NEGATIVE_INFINITY - 0.0)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, 1.0), (double) (Double.NEGATIVE_INFINITY - 1.0), "(double) (Double.NEGATIVE_INFINITY - 1.0)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), (double) (Double.NEGATIVE_INFINITY - Double.MAX_VALUE), "(double) (Double.NEGATIVE_INFINITY - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.NEGATIVE_INFINITY - Double.POSITIVE_INFINITY), "(double) (Double.NEGATIVE_INFINITY - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.NEGATIVE_INFINITY, Double.NaN), (double) (Double.NEGATIVE_INFINITY - Double.NaN), "(double) (Double.NEGATIVE_INFINITY - Double.NaN)");
    Tester.checkEqual(doubleSub(-1.0, Double.NEGATIVE_INFINITY), (double) (-1.0 - Double.NEGATIVE_INFINITY), "(double) (-1.0 - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(-1.0, -1.0), (double) (-1.0 - -1.0), "(double) (-1.0 - -1.0)");
    Tester.checkEqual(doubleSub(-1.0, -0.0), (double) (-1.0 - -0.0), "(double) (-1.0 - -0.0)");
    Tester.checkEqual(doubleSub(-1.0, 0.0), (double) (-1.0 - 0.0), "(double) (-1.0 - 0.0)");
    Tester.checkEqual(doubleSub(-1.0, 1.0), (double) (-1.0 - 1.0), "(double) (-1.0 - 1.0)");
    Tester.checkEqual(doubleSub(-1.0, Double.MAX_VALUE), (double) (-1.0 - Double.MAX_VALUE), "(double) (-1.0 - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(-1.0, Double.POSITIVE_INFINITY), (double) (-1.0 - Double.POSITIVE_INFINITY), "(double) (-1.0 - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(-1.0, Double.NaN), (double) (-1.0 - Double.NaN), "(double) (-1.0 - Double.NaN)");
    Tester.checkEqual(doubleSub(-0.0, Double.NEGATIVE_INFINITY), (double) (-0.0 - Double.NEGATIVE_INFINITY), "(double) (-0.0 - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(-0.0, -1.0), (double) (-0.0 - -1.0), "(double) (-0.0 - -1.0)");
    Tester.checkEqual(doubleSub(-0.0, -0.0), (double) (-0.0 - -0.0), "(double) (-0.0 - -0.0)");
    Tester.checkEqual(doubleSub(-0.0, 0.0), (double) (-0.0 - 0.0), "(double) (-0.0 - 0.0)");
    Tester.checkEqual(doubleSub(-0.0, 1.0), (double) (-0.0 - 1.0), "(double) (-0.0 - 1.0)");
    Tester.checkEqual(doubleSub(-0.0, Double.MAX_VALUE), (double) (-0.0 - Double.MAX_VALUE), "(double) (-0.0 - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(-0.0, Double.POSITIVE_INFINITY), (double) (-0.0 - Double.POSITIVE_INFINITY), "(double) (-0.0 - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(-0.0, Double.NaN), (double) (-0.0 - Double.NaN), "(double) (-0.0 - Double.NaN)");
    Tester.checkEqual(doubleSub(0.0, Double.NEGATIVE_INFINITY), (double) (0.0 - Double.NEGATIVE_INFINITY), "(double) (0.0 - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(0.0, -1.0), (double) (0.0 - -1.0), "(double) (0.0 - -1.0)");
    Tester.checkEqual(doubleSub(0.0, -0.0), (double) (0.0 - -0.0), "(double) (0.0 - -0.0)");
    Tester.checkEqual(doubleSub(0.0, 0.0), (double) (0.0 - 0.0), "(double) (0.0 - 0.0)");
    Tester.checkEqual(doubleSub(0.0, 1.0), (double) (0.0 - 1.0), "(double) (0.0 - 1.0)");
    Tester.checkEqual(doubleSub(0.0, Double.MAX_VALUE), (double) (0.0 - Double.MAX_VALUE), "(double) (0.0 - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(0.0, Double.POSITIVE_INFINITY), (double) (0.0 - Double.POSITIVE_INFINITY), "(double) (0.0 - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(0.0, Double.NaN), (double) (0.0 - Double.NaN), "(double) (0.0 - Double.NaN)");
    Tester.checkEqual(doubleSub(1.0, Double.NEGATIVE_INFINITY), (double) (1.0 - Double.NEGATIVE_INFINITY), "(double) (1.0 - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(1.0, -1.0), (double) (1.0 - -1.0), "(double) (1.0 - -1.0)");
    Tester.checkEqual(doubleSub(1.0, -0.0), (double) (1.0 - -0.0), "(double) (1.0 - -0.0)");
    Tester.checkEqual(doubleSub(1.0, 0.0), (double) (1.0 - 0.0), "(double) (1.0 - 0.0)");
    Tester.checkEqual(doubleSub(1.0, 1.0), (double) (1.0 - 1.0), "(double) (1.0 - 1.0)");
    Tester.checkEqual(doubleSub(1.0, Double.MAX_VALUE), (double) (1.0 - Double.MAX_VALUE), "(double) (1.0 - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(1.0, Double.POSITIVE_INFINITY), (double) (1.0 - Double.POSITIVE_INFINITY), "(double) (1.0 - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(1.0, Double.NaN), (double) (1.0 - Double.NaN), "(double) (1.0 - Double.NaN)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), (double) (Double.MAX_VALUE - Double.NEGATIVE_INFINITY), "(double) (Double.MAX_VALUE - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, -1.0), (double) (Double.MAX_VALUE - -1.0), "(double) (Double.MAX_VALUE - -1.0)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, -0.0), (double) (Double.MAX_VALUE - -0.0), "(double) (Double.MAX_VALUE - -0.0)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, 0.0), (double) (Double.MAX_VALUE - 0.0), "(double) (Double.MAX_VALUE - 0.0)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, 1.0), (double) (Double.MAX_VALUE - 1.0), "(double) (Double.MAX_VALUE - 1.0)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, Double.MAX_VALUE), (double) (Double.MAX_VALUE - Double.MAX_VALUE), "(double) (Double.MAX_VALUE - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, Double.POSITIVE_INFINITY), (double) (Double.MAX_VALUE - Double.POSITIVE_INFINITY), "(double) (Double.MAX_VALUE - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.MAX_VALUE, Double.NaN), (double) (Double.MAX_VALUE - Double.NaN), "(double) (Double.MAX_VALUE - Double.NaN)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), (double) (Double.POSITIVE_INFINITY - Double.NEGATIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, -1.0), (double) (Double.POSITIVE_INFINITY - -1.0), "(double) (Double.POSITIVE_INFINITY - -1.0)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, -0.0), (double) (Double.POSITIVE_INFINITY - -0.0), "(double) (Double.POSITIVE_INFINITY - -0.0)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, 0.0), (double) (Double.POSITIVE_INFINITY - 0.0), "(double) (Double.POSITIVE_INFINITY - 0.0)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, 1.0), (double) (Double.POSITIVE_INFINITY - 1.0), "(double) (Double.POSITIVE_INFINITY - 1.0)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, Double.MAX_VALUE), (double) (Double.POSITIVE_INFINITY - Double.MAX_VALUE), "(double) (Double.POSITIVE_INFINITY - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), (double) (Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY), "(double) (Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.POSITIVE_INFINITY, Double.NaN), (double) (Double.POSITIVE_INFINITY - Double.NaN), "(double) (Double.POSITIVE_INFINITY - Double.NaN)");
    Tester.checkEqual(doubleSub(Double.NaN, Double.NEGATIVE_INFINITY), (double) (Double.NaN - Double.NEGATIVE_INFINITY), "(double) (Double.NaN - Double.NEGATIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.NaN, -1.0), (double) (Double.NaN - -1.0), "(double) (Double.NaN - -1.0)");
    Tester.checkEqual(doubleSub(Double.NaN, -0.0), (double) (Double.NaN - -0.0), "(double) (Double.NaN - -0.0)");
    Tester.checkEqual(doubleSub(Double.NaN, 0.0), (double) (Double.NaN - 0.0), "(double) (Double.NaN - 0.0)");
    Tester.checkEqual(doubleSub(Double.NaN, 1.0), (double) (Double.NaN - 1.0), "(double) (Double.NaN - 1.0)");
    Tester.checkEqual(doubleSub(Double.NaN, Double.MAX_VALUE), (double) (Double.NaN - Double.MAX_VALUE), "(double) (Double.NaN - Double.MAX_VALUE)");
    Tester.checkEqual(doubleSub(Double.NaN, Double.POSITIVE_INFINITY), (double) (Double.NaN - Double.POSITIVE_INFINITY), "(double) (Double.NaN - Double.POSITIVE_INFINITY)");
    Tester.checkEqual(doubleSub(Double.NaN, Double.NaN), (double) (Double.NaN - Double.NaN), "(double) (Double.NaN - Double.NaN)");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY < -1.0, "Double.NEGATIVE_INFINITY < -1.0");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY < -0.0, "Double.NEGATIVE_INFINITY < -0.0");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY < 0.0, "Double.NEGATIVE_INFINITY < 0.0");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY < 1.0, "Double.NEGATIVE_INFINITY < 1.0");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY < Double.MAX_VALUE, "Double.NEGATIVE_INFINITY < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY < Double.NaN, "Double.NEGATIVE_INFINITY < Double.NaN");
    Tester.checkEqual(doubleLt(-1.0, Double.NEGATIVE_INFINITY), -1.0 < Double.NEGATIVE_INFINITY, "-1.0 < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(-1.0, -1.0), -1.0 < -1.0, "-1.0 < -1.0");
    Tester.checkEqual(doubleLt(-1.0, -0.0), -1.0 < -0.0, "-1.0 < -0.0");
    Tester.checkEqual(doubleLt(-1.0, 0.0), -1.0 < 0.0, "-1.0 < 0.0");
    Tester.checkEqual(doubleLt(-1.0, 1.0), -1.0 < 1.0, "-1.0 < 1.0");
    Tester.checkEqual(doubleLt(-1.0, Double.MAX_VALUE), -1.0 < Double.MAX_VALUE, "-1.0 < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(-1.0, Double.POSITIVE_INFINITY), -1.0 < Double.POSITIVE_INFINITY, "-1.0 < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(-1.0, Double.NaN), -1.0 < Double.NaN, "-1.0 < Double.NaN");
    Tester.checkEqual(doubleLt(-0.0, Double.NEGATIVE_INFINITY), -0.0 < Double.NEGATIVE_INFINITY, "-0.0 < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(-0.0, -1.0), -0.0 < -1.0, "-0.0 < -1.0");
    Tester.checkEqual(doubleLt(-0.0, -0.0), -0.0 < -0.0, "-0.0 < -0.0");
    Tester.checkEqual(doubleLt(-0.0, 0.0), -0.0 < 0.0, "-0.0 < 0.0");
    Tester.checkEqual(doubleLt(-0.0, 1.0), -0.0 < 1.0, "-0.0 < 1.0");
    Tester.checkEqual(doubleLt(-0.0, Double.MAX_VALUE), -0.0 < Double.MAX_VALUE, "-0.0 < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(-0.0, Double.POSITIVE_INFINITY), -0.0 < Double.POSITIVE_INFINITY, "-0.0 < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(-0.0, Double.NaN), -0.0 < Double.NaN, "-0.0 < Double.NaN");
    Tester.checkEqual(doubleLt(0.0, Double.NEGATIVE_INFINITY), 0.0 < Double.NEGATIVE_INFINITY, "0.0 < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(0.0, -1.0), 0.0 < -1.0, "0.0 < -1.0");
    Tester.checkEqual(doubleLt(0.0, -0.0), 0.0 < -0.0, "0.0 < -0.0");
    Tester.checkEqual(doubleLt(0.0, 0.0), 0.0 < 0.0, "0.0 < 0.0");
    Tester.checkEqual(doubleLt(0.0, 1.0), 0.0 < 1.0, "0.0 < 1.0");
    Tester.checkEqual(doubleLt(0.0, Double.MAX_VALUE), 0.0 < Double.MAX_VALUE, "0.0 < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(0.0, Double.POSITIVE_INFINITY), 0.0 < Double.POSITIVE_INFINITY, "0.0 < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(0.0, Double.NaN), 0.0 < Double.NaN, "0.0 < Double.NaN");
    Tester.checkEqual(doubleLt(1.0, Double.NEGATIVE_INFINITY), 1.0 < Double.NEGATIVE_INFINITY, "1.0 < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(1.0, -1.0), 1.0 < -1.0, "1.0 < -1.0");
    Tester.checkEqual(doubleLt(1.0, -0.0), 1.0 < -0.0, "1.0 < -0.0");
    Tester.checkEqual(doubleLt(1.0, 0.0), 1.0 < 0.0, "1.0 < 0.0");
    Tester.checkEqual(doubleLt(1.0, 1.0), 1.0 < 1.0, "1.0 < 1.0");
    Tester.checkEqual(doubleLt(1.0, Double.MAX_VALUE), 1.0 < Double.MAX_VALUE, "1.0 < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(1.0, Double.POSITIVE_INFINITY), 1.0 < Double.POSITIVE_INFINITY, "1.0 < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(1.0, Double.NaN), 1.0 < Double.NaN, "1.0 < Double.NaN");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE < Double.NEGATIVE_INFINITY, "Double.MAX_VALUE < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, -1.0), Double.MAX_VALUE < -1.0, "Double.MAX_VALUE < -1.0");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, -0.0), Double.MAX_VALUE < -0.0, "Double.MAX_VALUE < -0.0");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, 0.0), Double.MAX_VALUE < 0.0, "Double.MAX_VALUE < 0.0");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, 1.0), Double.MAX_VALUE < 1.0, "Double.MAX_VALUE < 1.0");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE < Double.MAX_VALUE, "Double.MAX_VALUE < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE < Double.POSITIVE_INFINITY, "Double.MAX_VALUE < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE < Double.NaN, "Double.MAX_VALUE < Double.NaN");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY < Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY < -1.0, "Double.POSITIVE_INFINITY < -1.0");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY < -0.0, "Double.POSITIVE_INFINITY < -0.0");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY < 0.0, "Double.POSITIVE_INFINITY < 0.0");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY < 1.0, "Double.POSITIVE_INFINITY < 1.0");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY < Double.MAX_VALUE, "Double.POSITIVE_INFINITY < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY < Double.NaN, "Double.POSITIVE_INFINITY < Double.NaN");
    Tester.checkEqual(doubleLt(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN < Double.NEGATIVE_INFINITY, "Double.NaN < Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.NaN, -1.0), Double.NaN < -1.0, "Double.NaN < -1.0");
    Tester.checkEqual(doubleLt(Double.NaN, -0.0), Double.NaN < -0.0, "Double.NaN < -0.0");
    Tester.checkEqual(doubleLt(Double.NaN, 0.0), Double.NaN < 0.0, "Double.NaN < 0.0");
    Tester.checkEqual(doubleLt(Double.NaN, 1.0), Double.NaN < 1.0, "Double.NaN < 1.0");
    Tester.checkEqual(doubleLt(Double.NaN, Double.MAX_VALUE), Double.NaN < Double.MAX_VALUE, "Double.NaN < Double.MAX_VALUE");
    Tester.checkEqual(doubleLt(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN < Double.POSITIVE_INFINITY, "Double.NaN < Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLt(Double.NaN, Double.NaN), Double.NaN < Double.NaN, "Double.NaN < Double.NaN");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY > -1.0, "Double.NEGATIVE_INFINITY > -1.0");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY > -0.0, "Double.NEGATIVE_INFINITY > -0.0");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY > 0.0, "Double.NEGATIVE_INFINITY > 0.0");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY > 1.0, "Double.NEGATIVE_INFINITY > 1.0");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY > Double.MAX_VALUE, "Double.NEGATIVE_INFINITY > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY > Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY > Double.NaN, "Double.NEGATIVE_INFINITY > Double.NaN");
    Tester.checkEqual(doubleGt(-1.0, Double.NEGATIVE_INFINITY), -1.0 > Double.NEGATIVE_INFINITY, "-1.0 > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(-1.0, -1.0), -1.0 > -1.0, "-1.0 > -1.0");
    Tester.checkEqual(doubleGt(-1.0, -0.0), -1.0 > -0.0, "-1.0 > -0.0");
    Tester.checkEqual(doubleGt(-1.0, 0.0), -1.0 > 0.0, "-1.0 > 0.0");
    Tester.checkEqual(doubleGt(-1.0, 1.0), -1.0 > 1.0, "-1.0 > 1.0");
    Tester.checkEqual(doubleGt(-1.0, Double.MAX_VALUE), -1.0 > Double.MAX_VALUE, "-1.0 > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(-1.0, Double.POSITIVE_INFINITY), -1.0 > Double.POSITIVE_INFINITY, "-1.0 > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(-1.0, Double.NaN), -1.0 > Double.NaN, "-1.0 > Double.NaN");
    Tester.checkEqual(doubleGt(-0.0, Double.NEGATIVE_INFINITY), -0.0 > Double.NEGATIVE_INFINITY, "-0.0 > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(-0.0, -1.0), -0.0 > -1.0, "-0.0 > -1.0");
    Tester.checkEqual(doubleGt(-0.0, -0.0), -0.0 > -0.0, "-0.0 > -0.0");
    Tester.checkEqual(doubleGt(-0.0, 0.0), -0.0 > 0.0, "-0.0 > 0.0");
    Tester.checkEqual(doubleGt(-0.0, 1.0), -0.0 > 1.0, "-0.0 > 1.0");
    Tester.checkEqual(doubleGt(-0.0, Double.MAX_VALUE), -0.0 > Double.MAX_VALUE, "-0.0 > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(-0.0, Double.POSITIVE_INFINITY), -0.0 > Double.POSITIVE_INFINITY, "-0.0 > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(-0.0, Double.NaN), -0.0 > Double.NaN, "-0.0 > Double.NaN");
    Tester.checkEqual(doubleGt(0.0, Double.NEGATIVE_INFINITY), 0.0 > Double.NEGATIVE_INFINITY, "0.0 > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(0.0, -1.0), 0.0 > -1.0, "0.0 > -1.0");
    Tester.checkEqual(doubleGt(0.0, -0.0), 0.0 > -0.0, "0.0 > -0.0");
    Tester.checkEqual(doubleGt(0.0, 0.0), 0.0 > 0.0, "0.0 > 0.0");
    Tester.checkEqual(doubleGt(0.0, 1.0), 0.0 > 1.0, "0.0 > 1.0");
    Tester.checkEqual(doubleGt(0.0, Double.MAX_VALUE), 0.0 > Double.MAX_VALUE, "0.0 > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(0.0, Double.POSITIVE_INFINITY), 0.0 > Double.POSITIVE_INFINITY, "0.0 > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(0.0, Double.NaN), 0.0 > Double.NaN, "0.0 > Double.NaN");
    Tester.checkEqual(doubleGt(1.0, Double.NEGATIVE_INFINITY), 1.0 > Double.NEGATIVE_INFINITY, "1.0 > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(1.0, -1.0), 1.0 > -1.0, "1.0 > -1.0");
    Tester.checkEqual(doubleGt(1.0, -0.0), 1.0 > -0.0, "1.0 > -0.0");
    Tester.checkEqual(doubleGt(1.0, 0.0), 1.0 > 0.0, "1.0 > 0.0");
    Tester.checkEqual(doubleGt(1.0, 1.0), 1.0 > 1.0, "1.0 > 1.0");
    Tester.checkEqual(doubleGt(1.0, Double.MAX_VALUE), 1.0 > Double.MAX_VALUE, "1.0 > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(1.0, Double.POSITIVE_INFINITY), 1.0 > Double.POSITIVE_INFINITY, "1.0 > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(1.0, Double.NaN), 1.0 > Double.NaN, "1.0 > Double.NaN");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE > Double.NEGATIVE_INFINITY, "Double.MAX_VALUE > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, -1.0), Double.MAX_VALUE > -1.0, "Double.MAX_VALUE > -1.0");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, -0.0), Double.MAX_VALUE > -0.0, "Double.MAX_VALUE > -0.0");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, 0.0), Double.MAX_VALUE > 0.0, "Double.MAX_VALUE > 0.0");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, 1.0), Double.MAX_VALUE > 1.0, "Double.MAX_VALUE > 1.0");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE > Double.MAX_VALUE, "Double.MAX_VALUE > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE > Double.POSITIVE_INFINITY, "Double.MAX_VALUE > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE > Double.NaN, "Double.MAX_VALUE > Double.NaN");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY > Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY > -1.0, "Double.POSITIVE_INFINITY > -1.0");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY > -0.0, "Double.POSITIVE_INFINITY > -0.0");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY > 0.0, "Double.POSITIVE_INFINITY > 0.0");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY > 1.0, "Double.POSITIVE_INFINITY > 1.0");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY > Double.MAX_VALUE, "Double.POSITIVE_INFINITY > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY > Double.NaN, "Double.POSITIVE_INFINITY > Double.NaN");
    Tester.checkEqual(doubleGt(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN > Double.NEGATIVE_INFINITY, "Double.NaN > Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.NaN, -1.0), Double.NaN > -1.0, "Double.NaN > -1.0");
    Tester.checkEqual(doubleGt(Double.NaN, -0.0), Double.NaN > -0.0, "Double.NaN > -0.0");
    Tester.checkEqual(doubleGt(Double.NaN, 0.0), Double.NaN > 0.0, "Double.NaN > 0.0");
    Tester.checkEqual(doubleGt(Double.NaN, 1.0), Double.NaN > 1.0, "Double.NaN > 1.0");
    Tester.checkEqual(doubleGt(Double.NaN, Double.MAX_VALUE), Double.NaN > Double.MAX_VALUE, "Double.NaN > Double.MAX_VALUE");
    Tester.checkEqual(doubleGt(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN > Double.POSITIVE_INFINITY, "Double.NaN > Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGt(Double.NaN, Double.NaN), Double.NaN > Double.NaN, "Double.NaN > Double.NaN");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY <= Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY <= -1.0, "Double.NEGATIVE_INFINITY <= -1.0");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY <= -0.0, "Double.NEGATIVE_INFINITY <= -0.0");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY <= 0.0, "Double.NEGATIVE_INFINITY <= 0.0");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY <= 1.0, "Double.NEGATIVE_INFINITY <= 1.0");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY <= Double.MAX_VALUE, "Double.NEGATIVE_INFINITY <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY <= Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY <= Double.NaN, "Double.NEGATIVE_INFINITY <= Double.NaN");
    Tester.checkEqual(doubleLe(-1.0, Double.NEGATIVE_INFINITY), -1.0 <= Double.NEGATIVE_INFINITY, "-1.0 <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(-1.0, -1.0), -1.0 <= -1.0, "-1.0 <= -1.0");
    Tester.checkEqual(doubleLe(-1.0, -0.0), -1.0 <= -0.0, "-1.0 <= -0.0");
    Tester.checkEqual(doubleLe(-1.0, 0.0), -1.0 <= 0.0, "-1.0 <= 0.0");
    Tester.checkEqual(doubleLe(-1.0, 1.0), -1.0 <= 1.0, "-1.0 <= 1.0");
    Tester.checkEqual(doubleLe(-1.0, Double.MAX_VALUE), -1.0 <= Double.MAX_VALUE, "-1.0 <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(-1.0, Double.POSITIVE_INFINITY), -1.0 <= Double.POSITIVE_INFINITY, "-1.0 <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(-1.0, Double.NaN), -1.0 <= Double.NaN, "-1.0 <= Double.NaN");
    Tester.checkEqual(doubleLe(-0.0, Double.NEGATIVE_INFINITY), -0.0 <= Double.NEGATIVE_INFINITY, "-0.0 <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(-0.0, -1.0), -0.0 <= -1.0, "-0.0 <= -1.0");
    Tester.checkEqual(doubleLe(-0.0, -0.0), -0.0 <= -0.0, "-0.0 <= -0.0");
    Tester.checkEqual(doubleLe(-0.0, 0.0), -0.0 <= 0.0, "-0.0 <= 0.0");
    Tester.checkEqual(doubleLe(-0.0, 1.0), -0.0 <= 1.0, "-0.0 <= 1.0");
    Tester.checkEqual(doubleLe(-0.0, Double.MAX_VALUE), -0.0 <= Double.MAX_VALUE, "-0.0 <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(-0.0, Double.POSITIVE_INFINITY), -0.0 <= Double.POSITIVE_INFINITY, "-0.0 <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(-0.0, Double.NaN), -0.0 <= Double.NaN, "-0.0 <= Double.NaN");
    Tester.checkEqual(doubleLe(0.0, Double.NEGATIVE_INFINITY), 0.0 <= Double.NEGATIVE_INFINITY, "0.0 <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(0.0, -1.0), 0.0 <= -1.0, "0.0 <= -1.0");
    Tester.checkEqual(doubleLe(0.0, -0.0), 0.0 <= -0.0, "0.0 <= -0.0");
    Tester.checkEqual(doubleLe(0.0, 0.0), 0.0 <= 0.0, "0.0 <= 0.0");
    Tester.checkEqual(doubleLe(0.0, 1.0), 0.0 <= 1.0, "0.0 <= 1.0");
    Tester.checkEqual(doubleLe(0.0, Double.MAX_VALUE), 0.0 <= Double.MAX_VALUE, "0.0 <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(0.0, Double.POSITIVE_INFINITY), 0.0 <= Double.POSITIVE_INFINITY, "0.0 <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(0.0, Double.NaN), 0.0 <= Double.NaN, "0.0 <= Double.NaN");
    Tester.checkEqual(doubleLe(1.0, Double.NEGATIVE_INFINITY), 1.0 <= Double.NEGATIVE_INFINITY, "1.0 <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(1.0, -1.0), 1.0 <= -1.0, "1.0 <= -1.0");
    Tester.checkEqual(doubleLe(1.0, -0.0), 1.0 <= -0.0, "1.0 <= -0.0");
    Tester.checkEqual(doubleLe(1.0, 0.0), 1.0 <= 0.0, "1.0 <= 0.0");
    Tester.checkEqual(doubleLe(1.0, 1.0), 1.0 <= 1.0, "1.0 <= 1.0");
    Tester.checkEqual(doubleLe(1.0, Double.MAX_VALUE), 1.0 <= Double.MAX_VALUE, "1.0 <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(1.0, Double.POSITIVE_INFINITY), 1.0 <= Double.POSITIVE_INFINITY, "1.0 <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(1.0, Double.NaN), 1.0 <= Double.NaN, "1.0 <= Double.NaN");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE <= Double.NEGATIVE_INFINITY, "Double.MAX_VALUE <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, -1.0), Double.MAX_VALUE <= -1.0, "Double.MAX_VALUE <= -1.0");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, -0.0), Double.MAX_VALUE <= -0.0, "Double.MAX_VALUE <= -0.0");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, 0.0), Double.MAX_VALUE <= 0.0, "Double.MAX_VALUE <= 0.0");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, 1.0), Double.MAX_VALUE <= 1.0, "Double.MAX_VALUE <= 1.0");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE <= Double.MAX_VALUE, "Double.MAX_VALUE <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE <= Double.POSITIVE_INFINITY, "Double.MAX_VALUE <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE <= Double.NaN, "Double.MAX_VALUE <= Double.NaN");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY <= Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY <= -1.0, "Double.POSITIVE_INFINITY <= -1.0");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY <= -0.0, "Double.POSITIVE_INFINITY <= -0.0");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY <= 0.0, "Double.POSITIVE_INFINITY <= 0.0");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY <= 1.0, "Double.POSITIVE_INFINITY <= 1.0");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY <= Double.MAX_VALUE, "Double.POSITIVE_INFINITY <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY <= Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY <= Double.NaN, "Double.POSITIVE_INFINITY <= Double.NaN");
    Tester.checkEqual(doubleLe(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN <= Double.NEGATIVE_INFINITY, "Double.NaN <= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.NaN, -1.0), Double.NaN <= -1.0, "Double.NaN <= -1.0");
    Tester.checkEqual(doubleLe(Double.NaN, -0.0), Double.NaN <= -0.0, "Double.NaN <= -0.0");
    Tester.checkEqual(doubleLe(Double.NaN, 0.0), Double.NaN <= 0.0, "Double.NaN <= 0.0");
    Tester.checkEqual(doubleLe(Double.NaN, 1.0), Double.NaN <= 1.0, "Double.NaN <= 1.0");
    Tester.checkEqual(doubleLe(Double.NaN, Double.MAX_VALUE), Double.NaN <= Double.MAX_VALUE, "Double.NaN <= Double.MAX_VALUE");
    Tester.checkEqual(doubleLe(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN <= Double.POSITIVE_INFINITY, "Double.NaN <= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleLe(Double.NaN, Double.NaN), Double.NaN <= Double.NaN, "Double.NaN <= Double.NaN");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY >= Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY >= -1.0, "Double.NEGATIVE_INFINITY >= -1.0");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY >= -0.0, "Double.NEGATIVE_INFINITY >= -0.0");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY >= 0.0, "Double.NEGATIVE_INFINITY >= 0.0");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY >= 1.0, "Double.NEGATIVE_INFINITY >= 1.0");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY >= Double.MAX_VALUE, "Double.NEGATIVE_INFINITY >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY >= Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY >= Double.NaN, "Double.NEGATIVE_INFINITY >= Double.NaN");
    Tester.checkEqual(doubleGe(-1.0, Double.NEGATIVE_INFINITY), -1.0 >= Double.NEGATIVE_INFINITY, "-1.0 >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(-1.0, -1.0), -1.0 >= -1.0, "-1.0 >= -1.0");
    Tester.checkEqual(doubleGe(-1.0, -0.0), -1.0 >= -0.0, "-1.0 >= -0.0");
    Tester.checkEqual(doubleGe(-1.0, 0.0), -1.0 >= 0.0, "-1.0 >= 0.0");
    Tester.checkEqual(doubleGe(-1.0, 1.0), -1.0 >= 1.0, "-1.0 >= 1.0");
    Tester.checkEqual(doubleGe(-1.0, Double.MAX_VALUE), -1.0 >= Double.MAX_VALUE, "-1.0 >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(-1.0, Double.POSITIVE_INFINITY), -1.0 >= Double.POSITIVE_INFINITY, "-1.0 >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(-1.0, Double.NaN), -1.0 >= Double.NaN, "-1.0 >= Double.NaN");
    Tester.checkEqual(doubleGe(-0.0, Double.NEGATIVE_INFINITY), -0.0 >= Double.NEGATIVE_INFINITY, "-0.0 >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(-0.0, -1.0), -0.0 >= -1.0, "-0.0 >= -1.0");
    Tester.checkEqual(doubleGe(-0.0, -0.0), -0.0 >= -0.0, "-0.0 >= -0.0");
    Tester.checkEqual(doubleGe(-0.0, 0.0), -0.0 >= 0.0, "-0.0 >= 0.0");
    Tester.checkEqual(doubleGe(-0.0, 1.0), -0.0 >= 1.0, "-0.0 >= 1.0");
    Tester.checkEqual(doubleGe(-0.0, Double.MAX_VALUE), -0.0 >= Double.MAX_VALUE, "-0.0 >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(-0.0, Double.POSITIVE_INFINITY), -0.0 >= Double.POSITIVE_INFINITY, "-0.0 >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(-0.0, Double.NaN), -0.0 >= Double.NaN, "-0.0 >= Double.NaN");
    Tester.checkEqual(doubleGe(0.0, Double.NEGATIVE_INFINITY), 0.0 >= Double.NEGATIVE_INFINITY, "0.0 >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(0.0, -1.0), 0.0 >= -1.0, "0.0 >= -1.0");
    Tester.checkEqual(doubleGe(0.0, -0.0), 0.0 >= -0.0, "0.0 >= -0.0");
    Tester.checkEqual(doubleGe(0.0, 0.0), 0.0 >= 0.0, "0.0 >= 0.0");
    Tester.checkEqual(doubleGe(0.0, 1.0), 0.0 >= 1.0, "0.0 >= 1.0");
    Tester.checkEqual(doubleGe(0.0, Double.MAX_VALUE), 0.0 >= Double.MAX_VALUE, "0.0 >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(0.0, Double.POSITIVE_INFINITY), 0.0 >= Double.POSITIVE_INFINITY, "0.0 >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(0.0, Double.NaN), 0.0 >= Double.NaN, "0.0 >= Double.NaN");
    Tester.checkEqual(doubleGe(1.0, Double.NEGATIVE_INFINITY), 1.0 >= Double.NEGATIVE_INFINITY, "1.0 >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(1.0, -1.0), 1.0 >= -1.0, "1.0 >= -1.0");
    Tester.checkEqual(doubleGe(1.0, -0.0), 1.0 >= -0.0, "1.0 >= -0.0");
    Tester.checkEqual(doubleGe(1.0, 0.0), 1.0 >= 0.0, "1.0 >= 0.0");
    Tester.checkEqual(doubleGe(1.0, 1.0), 1.0 >= 1.0, "1.0 >= 1.0");
    Tester.checkEqual(doubleGe(1.0, Double.MAX_VALUE), 1.0 >= Double.MAX_VALUE, "1.0 >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(1.0, Double.POSITIVE_INFINITY), 1.0 >= Double.POSITIVE_INFINITY, "1.0 >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(1.0, Double.NaN), 1.0 >= Double.NaN, "1.0 >= Double.NaN");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE >= Double.NEGATIVE_INFINITY, "Double.MAX_VALUE >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, -1.0), Double.MAX_VALUE >= -1.0, "Double.MAX_VALUE >= -1.0");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, -0.0), Double.MAX_VALUE >= -0.0, "Double.MAX_VALUE >= -0.0");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, 0.0), Double.MAX_VALUE >= 0.0, "Double.MAX_VALUE >= 0.0");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, 1.0), Double.MAX_VALUE >= 1.0, "Double.MAX_VALUE >= 1.0");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE >= Double.MAX_VALUE, "Double.MAX_VALUE >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE >= Double.POSITIVE_INFINITY, "Double.MAX_VALUE >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE >= Double.NaN, "Double.MAX_VALUE >= Double.NaN");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY >= Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY >= -1.0, "Double.POSITIVE_INFINITY >= -1.0");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY >= -0.0, "Double.POSITIVE_INFINITY >= -0.0");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY >= 0.0, "Double.POSITIVE_INFINITY >= 0.0");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY >= 1.0, "Double.POSITIVE_INFINITY >= 1.0");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY >= Double.MAX_VALUE, "Double.POSITIVE_INFINITY >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY >= Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY >= Double.NaN, "Double.POSITIVE_INFINITY >= Double.NaN");
    Tester.checkEqual(doubleGe(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN >= Double.NEGATIVE_INFINITY, "Double.NaN >= Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.NaN, -1.0), Double.NaN >= -1.0, "Double.NaN >= -1.0");
    Tester.checkEqual(doubleGe(Double.NaN, -0.0), Double.NaN >= -0.0, "Double.NaN >= -0.0");
    Tester.checkEqual(doubleGe(Double.NaN, 0.0), Double.NaN >= 0.0, "Double.NaN >= 0.0");
    Tester.checkEqual(doubleGe(Double.NaN, 1.0), Double.NaN >= 1.0, "Double.NaN >= 1.0");
    Tester.checkEqual(doubleGe(Double.NaN, Double.MAX_VALUE), Double.NaN >= Double.MAX_VALUE, "Double.NaN >= Double.MAX_VALUE");
    Tester.checkEqual(doubleGe(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN >= Double.POSITIVE_INFINITY, "Double.NaN >= Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleGe(Double.NaN, Double.NaN), Double.NaN >= Double.NaN, "Double.NaN >= Double.NaN");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY == -1.0, "Double.NEGATIVE_INFINITY == -1.0");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY == -0.0, "Double.NEGATIVE_INFINITY == -0.0");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY == 0.0, "Double.NEGATIVE_INFINITY == 0.0");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY == 1.0, "Double.NEGATIVE_INFINITY == 1.0");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY == Double.MAX_VALUE, "Double.NEGATIVE_INFINITY == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY == Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY == Double.NaN, "Double.NEGATIVE_INFINITY == Double.NaN");
    Tester.checkEqual(doubleEq(-1.0, Double.NEGATIVE_INFINITY), -1.0 == Double.NEGATIVE_INFINITY, "-1.0 == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(-1.0, -1.0), -1.0 == -1.0, "-1.0 == -1.0");
    Tester.checkEqual(doubleEq(-1.0, -0.0), -1.0 == -0.0, "-1.0 == -0.0");
    Tester.checkEqual(doubleEq(-1.0, 0.0), -1.0 == 0.0, "-1.0 == 0.0");
    Tester.checkEqual(doubleEq(-1.0, 1.0), -1.0 == 1.0, "-1.0 == 1.0");
    Tester.checkEqual(doubleEq(-1.0, Double.MAX_VALUE), -1.0 == Double.MAX_VALUE, "-1.0 == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(-1.0, Double.POSITIVE_INFINITY), -1.0 == Double.POSITIVE_INFINITY, "-1.0 == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(-1.0, Double.NaN), -1.0 == Double.NaN, "-1.0 == Double.NaN");
    Tester.checkEqual(doubleEq(-0.0, Double.NEGATIVE_INFINITY), -0.0 == Double.NEGATIVE_INFINITY, "-0.0 == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(-0.0, -1.0), -0.0 == -1.0, "-0.0 == -1.0");
    Tester.checkEqual(doubleEq(-0.0, -0.0), -0.0 == -0.0, "-0.0 == -0.0");
    Tester.checkEqual(doubleEq(-0.0, 0.0), -0.0 == 0.0, "-0.0 == 0.0");
    Tester.checkEqual(doubleEq(-0.0, 1.0), -0.0 == 1.0, "-0.0 == 1.0");
    Tester.checkEqual(doubleEq(-0.0, Double.MAX_VALUE), -0.0 == Double.MAX_VALUE, "-0.0 == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(-0.0, Double.POSITIVE_INFINITY), -0.0 == Double.POSITIVE_INFINITY, "-0.0 == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(-0.0, Double.NaN), -0.0 == Double.NaN, "-0.0 == Double.NaN");
    Tester.checkEqual(doubleEq(0.0, Double.NEGATIVE_INFINITY), 0.0 == Double.NEGATIVE_INFINITY, "0.0 == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(0.0, -1.0), 0.0 == -1.0, "0.0 == -1.0");
    Tester.checkEqual(doubleEq(0.0, -0.0), 0.0 == -0.0, "0.0 == -0.0");
    Tester.checkEqual(doubleEq(0.0, 0.0), 0.0 == 0.0, "0.0 == 0.0");
    Tester.checkEqual(doubleEq(0.0, 1.0), 0.0 == 1.0, "0.0 == 1.0");
    Tester.checkEqual(doubleEq(0.0, Double.MAX_VALUE), 0.0 == Double.MAX_VALUE, "0.0 == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(0.0, Double.POSITIVE_INFINITY), 0.0 == Double.POSITIVE_INFINITY, "0.0 == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(0.0, Double.NaN), 0.0 == Double.NaN, "0.0 == Double.NaN");
    Tester.checkEqual(doubleEq(1.0, Double.NEGATIVE_INFINITY), 1.0 == Double.NEGATIVE_INFINITY, "1.0 == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(1.0, -1.0), 1.0 == -1.0, "1.0 == -1.0");
    Tester.checkEqual(doubleEq(1.0, -0.0), 1.0 == -0.0, "1.0 == -0.0");
    Tester.checkEqual(doubleEq(1.0, 0.0), 1.0 == 0.0, "1.0 == 0.0");
    Tester.checkEqual(doubleEq(1.0, 1.0), 1.0 == 1.0, "1.0 == 1.0");
    Tester.checkEqual(doubleEq(1.0, Double.MAX_VALUE), 1.0 == Double.MAX_VALUE, "1.0 == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(1.0, Double.POSITIVE_INFINITY), 1.0 == Double.POSITIVE_INFINITY, "1.0 == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(1.0, Double.NaN), 1.0 == Double.NaN, "1.0 == Double.NaN");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE == Double.NEGATIVE_INFINITY, "Double.MAX_VALUE == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, -1.0), Double.MAX_VALUE == -1.0, "Double.MAX_VALUE == -1.0");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, -0.0), Double.MAX_VALUE == -0.0, "Double.MAX_VALUE == -0.0");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, 0.0), Double.MAX_VALUE == 0.0, "Double.MAX_VALUE == 0.0");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, 1.0), Double.MAX_VALUE == 1.0, "Double.MAX_VALUE == 1.0");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE == Double.MAX_VALUE, "Double.MAX_VALUE == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE == Double.POSITIVE_INFINITY, "Double.MAX_VALUE == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE == Double.NaN, "Double.MAX_VALUE == Double.NaN");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY == -1.0, "Double.POSITIVE_INFINITY == -1.0");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY == -0.0, "Double.POSITIVE_INFINITY == -0.0");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY == 0.0, "Double.POSITIVE_INFINITY == 0.0");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY == 1.0, "Double.POSITIVE_INFINITY == 1.0");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY == Double.MAX_VALUE, "Double.POSITIVE_INFINITY == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY == Double.NaN, "Double.POSITIVE_INFINITY == Double.NaN");
    Tester.checkEqual(doubleEq(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN == Double.NEGATIVE_INFINITY, "Double.NaN == Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.NaN, -1.0), Double.NaN == -1.0, "Double.NaN == -1.0");
    Tester.checkEqual(doubleEq(Double.NaN, -0.0), Double.NaN == -0.0, "Double.NaN == -0.0");
    Tester.checkEqual(doubleEq(Double.NaN, 0.0), Double.NaN == 0.0, "Double.NaN == 0.0");
    Tester.checkEqual(doubleEq(Double.NaN, 1.0), Double.NaN == 1.0, "Double.NaN == 1.0");
    Tester.checkEqual(doubleEq(Double.NaN, Double.MAX_VALUE), Double.NaN == Double.MAX_VALUE, "Double.NaN == Double.MAX_VALUE");
    Tester.checkEqual(doubleEq(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN == Double.POSITIVE_INFINITY, "Double.NaN == Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleEq(Double.NaN, Double.NaN), Double.NaN == Double.NaN, "Double.NaN == Double.NaN");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY != Double.NEGATIVE_INFINITY, "Double.NEGATIVE_INFINITY != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, -1.0), Double.NEGATIVE_INFINITY != -1.0, "Double.NEGATIVE_INFINITY != -1.0");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, -0.0), Double.NEGATIVE_INFINITY != -0.0, "Double.NEGATIVE_INFINITY != -0.0");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, 0.0), Double.NEGATIVE_INFINITY != 0.0, "Double.NEGATIVE_INFINITY != 0.0");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, 1.0), Double.NEGATIVE_INFINITY != 1.0, "Double.NEGATIVE_INFINITY != 1.0");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, Double.MAX_VALUE), Double.NEGATIVE_INFINITY != Double.MAX_VALUE, "Double.NEGATIVE_INFINITY != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), Double.NEGATIVE_INFINITY != Double.POSITIVE_INFINITY, "Double.NEGATIVE_INFINITY != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.NEGATIVE_INFINITY, Double.NaN), Double.NEGATIVE_INFINITY != Double.NaN, "Double.NEGATIVE_INFINITY != Double.NaN");
    Tester.checkEqual(doubleNe(-1.0, Double.NEGATIVE_INFINITY), -1.0 != Double.NEGATIVE_INFINITY, "-1.0 != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(-1.0, -1.0), -1.0 != -1.0, "-1.0 != -1.0");
    Tester.checkEqual(doubleNe(-1.0, -0.0), -1.0 != -0.0, "-1.0 != -0.0");
    Tester.checkEqual(doubleNe(-1.0, 0.0), -1.0 != 0.0, "-1.0 != 0.0");
    Tester.checkEqual(doubleNe(-1.0, 1.0), -1.0 != 1.0, "-1.0 != 1.0");
    Tester.checkEqual(doubleNe(-1.0, Double.MAX_VALUE), -1.0 != Double.MAX_VALUE, "-1.0 != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(-1.0, Double.POSITIVE_INFINITY), -1.0 != Double.POSITIVE_INFINITY, "-1.0 != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(-1.0, Double.NaN), -1.0 != Double.NaN, "-1.0 != Double.NaN");
    Tester.checkEqual(doubleNe(-0.0, Double.NEGATIVE_INFINITY), -0.0 != Double.NEGATIVE_INFINITY, "-0.0 != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(-0.0, -1.0), -0.0 != -1.0, "-0.0 != -1.0");
    Tester.checkEqual(doubleNe(-0.0, -0.0), -0.0 != -0.0, "-0.0 != -0.0");
    Tester.checkEqual(doubleNe(-0.0, 0.0), -0.0 != 0.0, "-0.0 != 0.0");
    Tester.checkEqual(doubleNe(-0.0, 1.0), -0.0 != 1.0, "-0.0 != 1.0");
    Tester.checkEqual(doubleNe(-0.0, Double.MAX_VALUE), -0.0 != Double.MAX_VALUE, "-0.0 != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(-0.0, Double.POSITIVE_INFINITY), -0.0 != Double.POSITIVE_INFINITY, "-0.0 != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(-0.0, Double.NaN), -0.0 != Double.NaN, "-0.0 != Double.NaN");
    Tester.checkEqual(doubleNe(0.0, Double.NEGATIVE_INFINITY), 0.0 != Double.NEGATIVE_INFINITY, "0.0 != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(0.0, -1.0), 0.0 != -1.0, "0.0 != -1.0");
    Tester.checkEqual(doubleNe(0.0, -0.0), 0.0 != -0.0, "0.0 != -0.0");
    Tester.checkEqual(doubleNe(0.0, 0.0), 0.0 != 0.0, "0.0 != 0.0");
    Tester.checkEqual(doubleNe(0.0, 1.0), 0.0 != 1.0, "0.0 != 1.0");
    Tester.checkEqual(doubleNe(0.0, Double.MAX_VALUE), 0.0 != Double.MAX_VALUE, "0.0 != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(0.0, Double.POSITIVE_INFINITY), 0.0 != Double.POSITIVE_INFINITY, "0.0 != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(0.0, Double.NaN), 0.0 != Double.NaN, "0.0 != Double.NaN");
    Tester.checkEqual(doubleNe(1.0, Double.NEGATIVE_INFINITY), 1.0 != Double.NEGATIVE_INFINITY, "1.0 != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(1.0, -1.0), 1.0 != -1.0, "1.0 != -1.0");
    Tester.checkEqual(doubleNe(1.0, -0.0), 1.0 != -0.0, "1.0 != -0.0");
    Tester.checkEqual(doubleNe(1.0, 0.0), 1.0 != 0.0, "1.0 != 0.0");
    Tester.checkEqual(doubleNe(1.0, 1.0), 1.0 != 1.0, "1.0 != 1.0");
    Tester.checkEqual(doubleNe(1.0, Double.MAX_VALUE), 1.0 != Double.MAX_VALUE, "1.0 != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(1.0, Double.POSITIVE_INFINITY), 1.0 != Double.POSITIVE_INFINITY, "1.0 != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(1.0, Double.NaN), 1.0 != Double.NaN, "1.0 != Double.NaN");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, Double.NEGATIVE_INFINITY), Double.MAX_VALUE != Double.NEGATIVE_INFINITY, "Double.MAX_VALUE != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, -1.0), Double.MAX_VALUE != -1.0, "Double.MAX_VALUE != -1.0");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, -0.0), Double.MAX_VALUE != -0.0, "Double.MAX_VALUE != -0.0");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, 0.0), Double.MAX_VALUE != 0.0, "Double.MAX_VALUE != 0.0");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, 1.0), Double.MAX_VALUE != 1.0, "Double.MAX_VALUE != 1.0");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, Double.MAX_VALUE), Double.MAX_VALUE != Double.MAX_VALUE, "Double.MAX_VALUE != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, Double.POSITIVE_INFINITY), Double.MAX_VALUE != Double.POSITIVE_INFINITY, "Double.MAX_VALUE != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.MAX_VALUE, Double.NaN), Double.MAX_VALUE != Double.NaN, "Double.MAX_VALUE != Double.NaN");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY != Double.NEGATIVE_INFINITY, "Double.POSITIVE_INFINITY != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, -1.0), Double.POSITIVE_INFINITY != -1.0, "Double.POSITIVE_INFINITY != -1.0");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, -0.0), Double.POSITIVE_INFINITY != -0.0, "Double.POSITIVE_INFINITY != -0.0");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, 0.0), Double.POSITIVE_INFINITY != 0.0, "Double.POSITIVE_INFINITY != 0.0");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, 1.0), Double.POSITIVE_INFINITY != 1.0, "Double.POSITIVE_INFINITY != 1.0");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, Double.MAX_VALUE), Double.POSITIVE_INFINITY != Double.MAX_VALUE, "Double.POSITIVE_INFINITY != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY != Double.POSITIVE_INFINITY, "Double.POSITIVE_INFINITY != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.POSITIVE_INFINITY, Double.NaN), Double.POSITIVE_INFINITY != Double.NaN, "Double.POSITIVE_INFINITY != Double.NaN");
    Tester.checkEqual(doubleNe(Double.NaN, Double.NEGATIVE_INFINITY), Double.NaN != Double.NEGATIVE_INFINITY, "Double.NaN != Double.NEGATIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.NaN, -1.0), Double.NaN != -1.0, "Double.NaN != -1.0");
    Tester.checkEqual(doubleNe(Double.NaN, -0.0), Double.NaN != -0.0, "Double.NaN != -0.0");
    Tester.checkEqual(doubleNe(Double.NaN, 0.0), Double.NaN != 0.0, "Double.NaN != 0.0");
    Tester.checkEqual(doubleNe(Double.NaN, 1.0), Double.NaN != 1.0, "Double.NaN != 1.0");
    Tester.checkEqual(doubleNe(Double.NaN, Double.MAX_VALUE), Double.NaN != Double.MAX_VALUE, "Double.NaN != Double.MAX_VALUE");
    Tester.checkEqual(doubleNe(Double.NaN, Double.POSITIVE_INFINITY), Double.NaN != Double.POSITIVE_INFINITY, "Double.NaN != Double.POSITIVE_INFINITY");
    Tester.checkEqual(doubleNe(Double.NaN, Double.NaN), Double.NaN != Double.NaN, "Double.NaN != Double.NaN");
  }
  static void doubleSwitch() {
    switch(0) {
      case ((((double) + Double.NEGATIVE_INFINITY) == 0) ? 0 : 0):
      case ((((double) + -1.0) == 0) ? 1 : 1):
      case ((((double) + -0.0) == 0) ? 2 : 2):
      case ((((double) + 0.0) == 0) ? 3 : 3):
      case ((((double) + 1.0) == 0) ? 4 : 4):
      case ((((double) + Double.MAX_VALUE) == 0) ? 5 : 5):
      case ((((double) + Double.POSITIVE_INFINITY) == 0) ? 6 : 6):
      case ((((double) + Double.NaN) == 0) ? 7 : 7):
      case ((((double) - Double.NEGATIVE_INFINITY) == 0) ? 8 : 8):
      case ((((double) - -1.0) == 0) ? 9 : 9):
      case ((((double) - -0.0) == 0) ? 10 : 10):
      case ((((double) - 0.0) == 0) ? 11 : 11):
      case ((((double) - 1.0) == 0) ? 12 : 12):
      case ((((double) - Double.MAX_VALUE) == 0) ? 13 : 13):
      case ((((double) - Double.POSITIVE_INFINITY) == 0) ? 14 : 14):
      case ((((double) - Double.NaN) == 0) ? 15 : 15):
      case ((((double) (Double.NEGATIVE_INFINITY * Double.NEGATIVE_INFINITY)) == 0) ? 16 : 16):
      case ((((double) (Double.NEGATIVE_INFINITY * -1.0)) == 0) ? 17 : 17):
      case ((((double) (Double.NEGATIVE_INFINITY * -0.0)) == 0) ? 18 : 18):
      case ((((double) (Double.NEGATIVE_INFINITY * 0.0)) == 0) ? 19 : 19):
      case ((((double) (Double.NEGATIVE_INFINITY * 1.0)) == 0) ? 20 : 20):
      case ((((double) (Double.NEGATIVE_INFINITY * Double.MAX_VALUE)) == 0) ? 21 : 21):
      case ((((double) (Double.NEGATIVE_INFINITY * Double.POSITIVE_INFINITY)) == 0) ? 22 : 22):
      case ((((double) (Double.NEGATIVE_INFINITY * Double.NaN)) == 0) ? 23 : 23):
      case ((((double) (-1.0 * Double.NEGATIVE_INFINITY)) == 0) ? 24 : 24):
      case ((((double) (-1.0 * -1.0)) == 0) ? 25 : 25):
      case ((((double) (-1.0 * -0.0)) == 0) ? 26 : 26):
      case ((((double) (-1.0 * 0.0)) == 0) ? 27 : 27):
      case ((((double) (-1.0 * 1.0)) == 0) ? 28 : 28):
      case ((((double) (-1.0 * Double.MAX_VALUE)) == 0) ? 29 : 29):
      case ((((double) (-1.0 * Double.POSITIVE_INFINITY)) == 0) ? 30 : 30):
      case ((((double) (-1.0 * Double.NaN)) == 0) ? 31 : 31):
      case ((((double) (-0.0 * Double.NEGATIVE_INFINITY)) == 0) ? 32 : 32):
      case ((((double) (-0.0 * -1.0)) == 0) ? 33 : 33):
      case ((((double) (-0.0 * -0.0)) == 0) ? 34 : 34):
      case ((((double) (-0.0 * 0.0)) == 0) ? 35 : 35):
      case ((((double) (-0.0 * 1.0)) == 0) ? 36 : 36):
      case ((((double) (-0.0 * Double.MAX_VALUE)) == 0) ? 37 : 37):
      case ((((double) (-0.0 * Double.POSITIVE_INFINITY)) == 0) ? 38 : 38):
      case ((((double) (-0.0 * Double.NaN)) == 0) ? 39 : 39):
      case ((((double) (0.0 * Double.NEGATIVE_INFINITY)) == 0) ? 40 : 40):
      case ((((double) (0.0 * -1.0)) == 0) ? 41 : 41):
      case ((((double) (0.0 * -0.0)) == 0) ? 42 : 42):
      case ((((double) (0.0 * 0.0)) == 0) ? 43 : 43):
      case ((((double) (0.0 * 1.0)) == 0) ? 44 : 44):
      case ((((double) (0.0 * Double.MAX_VALUE)) == 0) ? 45 : 45):
      case ((((double) (0.0 * Double.POSITIVE_INFINITY)) == 0) ? 46 : 46):
      case ((((double) (0.0 * Double.NaN)) == 0) ? 47 : 47):
      case ((((double) (1.0 * Double.NEGATIVE_INFINITY)) == 0) ? 48 : 48):
      case ((((double) (1.0 * -1.0)) == 0) ? 49 : 49):
      case ((((double) (1.0 * -0.0)) == 0) ? 50 : 50):
      case ((((double) (1.0 * 0.0)) == 0) ? 51 : 51):
      case ((((double) (1.0 * 1.0)) == 0) ? 52 : 52):
      case ((((double) (1.0 * Double.MAX_VALUE)) == 0) ? 53 : 53):
      case ((((double) (1.0 * Double.POSITIVE_INFINITY)) == 0) ? 54 : 54):
      case ((((double) (1.0 * Double.NaN)) == 0) ? 55 : 55):
      case ((((double) (Double.MAX_VALUE * Double.NEGATIVE_INFINITY)) == 0) ? 56 : 56):
      case ((((double) (Double.MAX_VALUE * -1.0)) == 0) ? 57 : 57):
      case ((((double) (Double.MAX_VALUE * -0.0)) == 0) ? 58 : 58):
      case ((((double) (Double.MAX_VALUE * 0.0)) == 0) ? 59 : 59):
      case ((((double) (Double.MAX_VALUE * 1.0)) == 0) ? 60 : 60):
      case ((((double) (Double.MAX_VALUE * Double.MAX_VALUE)) == 0) ? 61 : 61):
      case ((((double) (Double.MAX_VALUE * Double.POSITIVE_INFINITY)) == 0) ? 62 : 62):
      case ((((double) (Double.MAX_VALUE * Double.NaN)) == 0) ? 63 : 63):
      case ((((double) (Double.POSITIVE_INFINITY * Double.NEGATIVE_INFINITY)) == 0) ? 64 : 64):
      case ((((double) (Double.POSITIVE_INFINITY * -1.0)) == 0) ? 65 : 65):
      case ((((double) (Double.POSITIVE_INFINITY * -0.0)) == 0) ? 66 : 66):
      case ((((double) (Double.POSITIVE_INFINITY * 0.0)) == 0) ? 67 : 67):
      case ((((double) (Double.POSITIVE_INFINITY * 1.0)) == 0) ? 68 : 68):
      case ((((double) (Double.POSITIVE_INFINITY * Double.MAX_VALUE)) == 0) ? 69 : 69):
      case ((((double) (Double.POSITIVE_INFINITY * Double.POSITIVE_INFINITY)) == 0) ? 70 : 70):
      case ((((double) (Double.POSITIVE_INFINITY * Double.NaN)) == 0) ? 71 : 71):
      case ((((double) (Double.NaN * Double.NEGATIVE_INFINITY)) == 0) ? 72 : 72):
      case ((((double) (Double.NaN * -1.0)) == 0) ? 73 : 73):
      case ((((double) (Double.NaN * -0.0)) == 0) ? 74 : 74):
      case ((((double) (Double.NaN * 0.0)) == 0) ? 75 : 75):
      case ((((double) (Double.NaN * 1.0)) == 0) ? 76 : 76):
      case ((((double) (Double.NaN * Double.MAX_VALUE)) == 0) ? 77 : 77):
      case ((((double) (Double.NaN * Double.POSITIVE_INFINITY)) == 0) ? 78 : 78):
      case ((((double) (Double.NaN * Double.NaN)) == 0) ? 79 : 79):
      case ((((double) (Double.NEGATIVE_INFINITY / Double.NEGATIVE_INFINITY)) == 0) ? 80 : 80):
      case ((((double) (Double.NEGATIVE_INFINITY / -1.0)) == 0) ? 81 : 81):
      case ((((double) (Double.NEGATIVE_INFINITY / -0.0)) == 0) ? 82 : 82):
      case ((((double) (Double.NEGATIVE_INFINITY / 0.0)) == 0) ? 83 : 83):
      case ((((double) (Double.NEGATIVE_INFINITY / 1.0)) == 0) ? 84 : 84):
      case ((((double) (Double.NEGATIVE_INFINITY / Double.MAX_VALUE)) == 0) ? 85 : 85):
      case ((((double) (Double.NEGATIVE_INFINITY / Double.POSITIVE_INFINITY)) == 0) ? 86 : 86):
      case ((((double) (Double.NEGATIVE_INFINITY / Double.NaN)) == 0) ? 87 : 87):
      case ((((double) (-1.0 / Double.NEGATIVE_INFINITY)) == 0) ? 88 : 88):
      case ((((double) (-1.0 / -1.0)) == 0) ? 89 : 89):
      case ((((double) (-1.0 / -0.0)) == 0) ? 90 : 90):
      case ((((double) (-1.0 / 0.0)) == 0) ? 91 : 91):
      case ((((double) (-1.0 / 1.0)) == 0) ? 92 : 92):
      case ((((double) (-1.0 / Double.MAX_VALUE)) == 0) ? 93 : 93):
      case ((((double) (-1.0 / Double.POSITIVE_INFINITY)) == 0) ? 94 : 94):
      case ((((double) (-1.0 / Double.NaN)) == 0) ? 95 : 95):
      case ((((double) (-0.0 / Double.NEGATIVE_INFINITY)) == 0) ? 96 : 96):
      case ((((double) (-0.0 / -1.0)) == 0) ? 97 : 97):
      case ((((double) (-0.0 / -0.0)) == 0) ? 98 : 98):
      case ((((double) (-0.0 / 0.0)) == 0) ? 99 : 99):
      case ((((double) (-0.0 / 1.0)) == 0) ? 100 : 100):
      case ((((double) (-0.0 / Double.MAX_VALUE)) == 0) ? 101 : 101):
      case ((((double) (-0.0 / Double.POSITIVE_INFINITY)) == 0) ? 102 : 102):
      case ((((double) (-0.0 / Double.NaN)) == 0) ? 103 : 103):
      case ((((double) (0.0 / Double.NEGATIVE_INFINITY)) == 0) ? 104 : 104):
      case ((((double) (0.0 / -1.0)) == 0) ? 105 : 105):
      case ((((double) (0.0 / -0.0)) == 0) ? 106 : 106):
      case ((((double) (0.0 / 0.0)) == 0) ? 107 : 107):
      case ((((double) (0.0 / 1.0)) == 0) ? 108 : 108):
      case ((((double) (0.0 / Double.MAX_VALUE)) == 0) ? 109 : 109):
      case ((((double) (0.0 / Double.POSITIVE_INFINITY)) == 0) ? 110 : 110):
      case ((((double) (0.0 / Double.NaN)) == 0) ? 111 : 111):
      case ((((double) (1.0 / Double.NEGATIVE_INFINITY)) == 0) ? 112 : 112):
      case ((((double) (1.0 / -1.0)) == 0) ? 113 : 113):
      case ((((double) (1.0 / -0.0)) == 0) ? 114 : 114):
      case ((((double) (1.0 / 0.0)) == 0) ? 115 : 115):
      case ((((double) (1.0 / 1.0)) == 0) ? 116 : 116):
      case ((((double) (1.0 / Double.MAX_VALUE)) == 0) ? 117 : 117):
      case ((((double) (1.0 / Double.POSITIVE_INFINITY)) == 0) ? 118 : 118):
      case ((((double) (1.0 / Double.NaN)) == 0) ? 119 : 119):
      case ((((double) (Double.MAX_VALUE / Double.NEGATIVE_INFINITY)) == 0) ? 120 : 120):
      case ((((double) (Double.MAX_VALUE / -1.0)) == 0) ? 121 : 121):
      case ((((double) (Double.MAX_VALUE / -0.0)) == 0) ? 122 : 122):
      case ((((double) (Double.MAX_VALUE / 0.0)) == 0) ? 123 : 123):
      case ((((double) (Double.MAX_VALUE / 1.0)) == 0) ? 124 : 124):
      case ((((double) (Double.MAX_VALUE / Double.MAX_VALUE)) == 0) ? 125 : 125):
      case ((((double) (Double.MAX_VALUE / Double.POSITIVE_INFINITY)) == 0) ? 126 : 126):
      case ((((double) (Double.MAX_VALUE / Double.NaN)) == 0) ? 127 : 127):
      case ((((double) (Double.POSITIVE_INFINITY / Double.NEGATIVE_INFINITY)) == 0) ? 128 : 128):
      case ((((double) (Double.POSITIVE_INFINITY / -1.0)) == 0) ? 129 : 129):
      case ((((double) (Double.POSITIVE_INFINITY / -0.0)) == 0) ? 130 : 130):
      case ((((double) (Double.POSITIVE_INFINITY / 0.0)) == 0) ? 131 : 131):
      case ((((double) (Double.POSITIVE_INFINITY / 1.0)) == 0) ? 132 : 132):
      case ((((double) (Double.POSITIVE_INFINITY / Double.MAX_VALUE)) == 0) ? 133 : 133):
      case ((((double) (Double.POSITIVE_INFINITY / Double.POSITIVE_INFINITY)) == 0) ? 134 : 134):
      case ((((double) (Double.POSITIVE_INFINITY / Double.NaN)) == 0) ? 135 : 135):
      case ((((double) (Double.NaN / Double.NEGATIVE_INFINITY)) == 0) ? 136 : 136):
      case ((((double) (Double.NaN / -1.0)) == 0) ? 137 : 137):
      case ((((double) (Double.NaN / -0.0)) == 0) ? 138 : 138):
      case ((((double) (Double.NaN / 0.0)) == 0) ? 139 : 139):
      case ((((double) (Double.NaN / 1.0)) == 0) ? 140 : 140):
      case ((((double) (Double.NaN / Double.MAX_VALUE)) == 0) ? 141 : 141):
      case ((((double) (Double.NaN / Double.POSITIVE_INFINITY)) == 0) ? 142 : 142):
      case ((((double) (Double.NaN / Double.NaN)) == 0) ? 143 : 143):
      case ((((double) (Double.NEGATIVE_INFINITY % Double.NEGATIVE_INFINITY)) == 0) ? 144 : 144):
      case ((((double) (Double.NEGATIVE_INFINITY % -1.0)) == 0) ? 145 : 145):
      case ((((double) (Double.NEGATIVE_INFINITY % -0.0)) == 0) ? 146 : 146):
      case ((((double) (Double.NEGATIVE_INFINITY % 0.0)) == 0) ? 147 : 147):
      case ((((double) (Double.NEGATIVE_INFINITY % 1.0)) == 0) ? 148 : 148):
      case ((((double) (Double.NEGATIVE_INFINITY % Double.MAX_VALUE)) == 0) ? 149 : 149):
      case ((((double) (Double.NEGATIVE_INFINITY % Double.POSITIVE_INFINITY)) == 0) ? 150 : 150):
      case ((((double) (Double.NEGATIVE_INFINITY % Double.NaN)) == 0) ? 151 : 151):
      case ((((double) (-1.0 % Double.NEGATIVE_INFINITY)) == 0) ? 152 : 152):
      case ((((double) (-1.0 % -1.0)) == 0) ? 153 : 153):
      case ((((double) (-1.0 % -0.0)) == 0) ? 154 : 154):
      case ((((double) (-1.0 % 0.0)) == 0) ? 155 : 155):
      case ((((double) (-1.0 % 1.0)) == 0) ? 156 : 156):
      case ((((double) (-1.0 % Double.MAX_VALUE)) == 0) ? 157 : 157):
      case ((((double) (-1.0 % Double.POSITIVE_INFINITY)) == 0) ? 158 : 158):
      case ((((double) (-1.0 % Double.NaN)) == 0) ? 159 : 159):
      case ((((double) (-0.0 % Double.NEGATIVE_INFINITY)) == 0) ? 160 : 160):
      case ((((double) (-0.0 % -1.0)) == 0) ? 161 : 161):
      case ((((double) (-0.0 % -0.0)) == 0) ? 162 : 162):
      case ((((double) (-0.0 % 0.0)) == 0) ? 163 : 163):
      case ((((double) (-0.0 % 1.0)) == 0) ? 164 : 164):
      case ((((double) (-0.0 % Double.MAX_VALUE)) == 0) ? 165 : 165):
      case ((((double) (-0.0 % Double.POSITIVE_INFINITY)) == 0) ? 166 : 166):
      case ((((double) (-0.0 % Double.NaN)) == 0) ? 167 : 167):
      case ((((double) (0.0 % Double.NEGATIVE_INFINITY)) == 0) ? 168 : 168):
      case ((((double) (0.0 % -1.0)) == 0) ? 169 : 169):
      case ((((double) (0.0 % -0.0)) == 0) ? 170 : 170):
      case ((((double) (0.0 % 0.0)) == 0) ? 171 : 171):
      case ((((double) (0.0 % 1.0)) == 0) ? 172 : 172):
      case ((((double) (0.0 % Double.MAX_VALUE)) == 0) ? 173 : 173):
      case ((((double) (0.0 % Double.POSITIVE_INFINITY)) == 0) ? 174 : 174):
      case ((((double) (0.0 % Double.NaN)) == 0) ? 175 : 175):
      case ((((double) (1.0 % Double.NEGATIVE_INFINITY)) == 0) ? 176 : 176):
      case ((((double) (1.0 % -1.0)) == 0) ? 177 : 177):
      case ((((double) (1.0 % -0.0)) == 0) ? 178 : 178):
      case ((((double) (1.0 % 0.0)) == 0) ? 179 : 179):
      case ((((double) (1.0 % 1.0)) == 0) ? 180 : 180):
      case ((((double) (1.0 % Double.MAX_VALUE)) == 0) ? 181 : 181):
      case ((((double) (1.0 % Double.POSITIVE_INFINITY)) == 0) ? 182 : 182):
      case ((((double) (1.0 % Double.NaN)) == 0) ? 183 : 183):
      case ((((double) (Double.MAX_VALUE % Double.NEGATIVE_INFINITY)) == 0) ? 184 : 184):
      case ((((double) (Double.MAX_VALUE % -1.0)) == 0) ? 185 : 185):
      case ((((double) (Double.MAX_VALUE % -0.0)) == 0) ? 186 : 186):
      case ((((double) (Double.MAX_VALUE % 0.0)) == 0) ? 187 : 187):
      case ((((double) (Double.MAX_VALUE % 1.0)) == 0) ? 188 : 188):
      case ((((double) (Double.MAX_VALUE % Double.MAX_VALUE)) == 0) ? 189 : 189):
      case ((((double) (Double.MAX_VALUE % Double.POSITIVE_INFINITY)) == 0) ? 190 : 190):
      case ((((double) (Double.MAX_VALUE % Double.NaN)) == 0) ? 191 : 191):
      case ((((double) (Double.POSITIVE_INFINITY % Double.NEGATIVE_INFINITY)) == 0) ? 192 : 192):
      case ((((double) (Double.POSITIVE_INFINITY % -1.0)) == 0) ? 193 : 193):
      case ((((double) (Double.POSITIVE_INFINITY % -0.0)) == 0) ? 194 : 194):
      case ((((double) (Double.POSITIVE_INFINITY % 0.0)) == 0) ? 195 : 195):
      case ((((double) (Double.POSITIVE_INFINITY % 1.0)) == 0) ? 196 : 196):
      case ((((double) (Double.POSITIVE_INFINITY % Double.MAX_VALUE)) == 0) ? 197 : 197):
      case ((((double) (Double.POSITIVE_INFINITY % Double.POSITIVE_INFINITY)) == 0) ? 198 : 198):
      case ((((double) (Double.POSITIVE_INFINITY % Double.NaN)) == 0) ? 199 : 199):
      case ((((double) (Double.NaN % Double.NEGATIVE_INFINITY)) == 0) ? 200 : 200):
      case ((((double) (Double.NaN % -1.0)) == 0) ? 201 : 201):
      case ((((double) (Double.NaN % -0.0)) == 0) ? 202 : 202):
      case ((((double) (Double.NaN % 0.0)) == 0) ? 203 : 203):
      case ((((double) (Double.NaN % 1.0)) == 0) ? 204 : 204):
      case ((((double) (Double.NaN % Double.MAX_VALUE)) == 0) ? 205 : 205):
      case ((((double) (Double.NaN % Double.POSITIVE_INFINITY)) == 0) ? 206 : 206):
      case ((((double) (Double.NaN % Double.NaN)) == 0) ? 207 : 207):
      case ((((double) (Double.NEGATIVE_INFINITY + Double.NEGATIVE_INFINITY)) == 0) ? 208 : 208):
      case ((((double) (Double.NEGATIVE_INFINITY + -1.0)) == 0) ? 209 : 209):
      case ((((double) (Double.NEGATIVE_INFINITY + -0.0)) == 0) ? 210 : 210):
      case ((((double) (Double.NEGATIVE_INFINITY + 0.0)) == 0) ? 211 : 211):
      case ((((double) (Double.NEGATIVE_INFINITY + 1.0)) == 0) ? 212 : 212):
      case ((((double) (Double.NEGATIVE_INFINITY + Double.MAX_VALUE)) == 0) ? 213 : 213):
      case ((((double) (Double.NEGATIVE_INFINITY + Double.POSITIVE_INFINITY)) == 0) ? 214 : 214):
      case ((((double) (Double.NEGATIVE_INFINITY + Double.NaN)) == 0) ? 215 : 215):
      case ((((double) (-1.0 + Double.NEGATIVE_INFINITY)) == 0) ? 216 : 216):
      case ((((double) (-1.0 + -1.0)) == 0) ? 217 : 217):
      case ((((double) (-1.0 + -0.0)) == 0) ? 218 : 218):
      case ((((double) (-1.0 + 0.0)) == 0) ? 219 : 219):
      case ((((double) (-1.0 + 1.0)) == 0) ? 220 : 220):
      case ((((double) (-1.0 + Double.MAX_VALUE)) == 0) ? 221 : 221):
      case ((((double) (-1.0 + Double.POSITIVE_INFINITY)) == 0) ? 222 : 222):
      case ((((double) (-1.0 + Double.NaN)) == 0) ? 223 : 223):
      case ((((double) (-0.0 + Double.NEGATIVE_INFINITY)) == 0) ? 224 : 224):
      case ((((double) (-0.0 + -1.0)) == 0) ? 225 : 225):
      case ((((double) (-0.0 + -0.0)) == 0) ? 226 : 226):
      case ((((double) (-0.0 + 0.0)) == 0) ? 227 : 227):
      case ((((double) (-0.0 + 1.0)) == 0) ? 228 : 228):
      case ((((double) (-0.0 + Double.MAX_VALUE)) == 0) ? 229 : 229):
      case ((((double) (-0.0 + Double.POSITIVE_INFINITY)) == 0) ? 230 : 230):
      case ((((double) (-0.0 + Double.NaN)) == 0) ? 231 : 231):
      case ((((double) (0.0 + Double.NEGATIVE_INFINITY)) == 0) ? 232 : 232):
      case ((((double) (0.0 + -1.0)) == 0) ? 233 : 233):
      case ((((double) (0.0 + -0.0)) == 0) ? 234 : 234):
      case ((((double) (0.0 + 0.0)) == 0) ? 235 : 235):
      case ((((double) (0.0 + 1.0)) == 0) ? 236 : 236):
      case ((((double) (0.0 + Double.MAX_VALUE)) == 0) ? 237 : 237):
      case ((((double) (0.0 + Double.POSITIVE_INFINITY)) == 0) ? 238 : 238):
      case ((((double) (0.0 + Double.NaN)) == 0) ? 239 : 239):
      case ((((double) (1.0 + Double.NEGATIVE_INFINITY)) == 0) ? 240 : 240):
      case ((((double) (1.0 + -1.0)) == 0) ? 241 : 241):
      case ((((double) (1.0 + -0.0)) == 0) ? 242 : 242):
      case ((((double) (1.0 + 0.0)) == 0) ? 243 : 243):
      case ((((double) (1.0 + 1.0)) == 0) ? 244 : 244):
      case ((((double) (1.0 + Double.MAX_VALUE)) == 0) ? 245 : 245):
      case ((((double) (1.0 + Double.POSITIVE_INFINITY)) == 0) ? 246 : 246):
      case ((((double) (1.0 + Double.NaN)) == 0) ? 247 : 247):
      case ((((double) (Double.MAX_VALUE + Double.NEGATIVE_INFINITY)) == 0) ? 248 : 248):
      case ((((double) (Double.MAX_VALUE + -1.0)) == 0) ? 249 : 249):
      case ((((double) (Double.MAX_VALUE + -0.0)) == 0) ? 250 : 250):
      case ((((double) (Double.MAX_VALUE + 0.0)) == 0) ? 251 : 251):
      case ((((double) (Double.MAX_VALUE + 1.0)) == 0) ? 252 : 252):
      case ((((double) (Double.MAX_VALUE + Double.MAX_VALUE)) == 0) ? 253 : 253):
      case ((((double) (Double.MAX_VALUE + Double.POSITIVE_INFINITY)) == 0) ? 254 : 254):
      case ((((double) (Double.MAX_VALUE + Double.NaN)) == 0) ? 255 : 255):
      case ((((double) (Double.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY)) == 0) ? 256 : 256):
      case ((((double) (Double.POSITIVE_INFINITY + -1.0)) == 0) ? 257 : 257):
      case ((((double) (Double.POSITIVE_INFINITY + -0.0)) == 0) ? 258 : 258):
      case ((((double) (Double.POSITIVE_INFINITY + 0.0)) == 0) ? 259 : 259):
      case ((((double) (Double.POSITIVE_INFINITY + 1.0)) == 0) ? 260 : 260):
      case ((((double) (Double.POSITIVE_INFINITY + Double.MAX_VALUE)) == 0) ? 261 : 261):
      case ((((double) (Double.POSITIVE_INFINITY + Double.POSITIVE_INFINITY)) == 0) ? 262 : 262):
      case ((((double) (Double.POSITIVE_INFINITY + Double.NaN)) == 0) ? 263 : 263):
      case ((((double) (Double.NaN + Double.NEGATIVE_INFINITY)) == 0) ? 264 : 264):
      case ((((double) (Double.NaN + -1.0)) == 0) ? 265 : 265):
      case ((((double) (Double.NaN + -0.0)) == 0) ? 266 : 266):
      case ((((double) (Double.NaN + 0.0)) == 0) ? 267 : 267):
      case ((((double) (Double.NaN + 1.0)) == 0) ? 268 : 268):
      case ((((double) (Double.NaN + Double.MAX_VALUE)) == 0) ? 269 : 269):
      case ((((double) (Double.NaN + Double.POSITIVE_INFINITY)) == 0) ? 270 : 270):
      case ((((double) (Double.NaN + Double.NaN)) == 0) ? 271 : 271):
      case ((((double) (Double.NEGATIVE_INFINITY - Double.NEGATIVE_INFINITY)) == 0) ? 272 : 272):
      case ((((double) (Double.NEGATIVE_INFINITY - -1.0)) == 0) ? 273 : 273):
      case ((((double) (Double.NEGATIVE_INFINITY - -0.0)) == 0) ? 274 : 274):
      case ((((double) (Double.NEGATIVE_INFINITY - 0.0)) == 0) ? 275 : 275):
      case ((((double) (Double.NEGATIVE_INFINITY - 1.0)) == 0) ? 276 : 276):
      case ((((double) (Double.NEGATIVE_INFINITY - Double.MAX_VALUE)) == 0) ? 277 : 277):
      case ((((double) (Double.NEGATIVE_INFINITY - Double.POSITIVE_INFINITY)) == 0) ? 278 : 278):
      case ((((double) (Double.NEGATIVE_INFINITY - Double.NaN)) == 0) ? 279 : 279):
      case ((((double) (-1.0 - Double.NEGATIVE_INFINITY)) == 0) ? 280 : 280):
      case ((((double) (-1.0 - -1.0)) == 0) ? 281 : 281):
      case ((((double) (-1.0 - -0.0)) == 0) ? 282 : 282):
      case ((((double) (-1.0 - 0.0)) == 0) ? 283 : 283):
      case ((((double) (-1.0 - 1.0)) == 0) ? 284 : 284):
      case ((((double) (-1.0 - Double.MAX_VALUE)) == 0) ? 285 : 285):
      case ((((double) (-1.0 - Double.POSITIVE_INFINITY)) == 0) ? 286 : 286):
      case ((((double) (-1.0 - Double.NaN)) == 0) ? 287 : 287):
      case ((((double) (-0.0 - Double.NEGATIVE_INFINITY)) == 0) ? 288 : 288):
      case ((((double) (-0.0 - -1.0)) == 0) ? 289 : 289):
      case ((((double) (-0.0 - -0.0)) == 0) ? 290 : 290):
      case ((((double) (-0.0 - 0.0)) == 0) ? 291 : 291):
      case ((((double) (-0.0 - 1.0)) == 0) ? 292 : 292):
      case ((((double) (-0.0 - Double.MAX_VALUE)) == 0) ? 293 : 293):
      case ((((double) (-0.0 - Double.POSITIVE_INFINITY)) == 0) ? 294 : 294):
      case ((((double) (-0.0 - Double.NaN)) == 0) ? 295 : 295):
      case ((((double) (0.0 - Double.NEGATIVE_INFINITY)) == 0) ? 296 : 296):
      case ((((double) (0.0 - -1.0)) == 0) ? 297 : 297):
      case ((((double) (0.0 - -0.0)) == 0) ? 298 : 298):
      case ((((double) (0.0 - 0.0)) == 0) ? 299 : 299):
      case ((((double) (0.0 - 1.0)) == 0) ? 300 : 300):
      case ((((double) (0.0 - Double.MAX_VALUE)) == 0) ? 301 : 301):
      case ((((double) (0.0 - Double.POSITIVE_INFINITY)) == 0) ? 302 : 302):
      case ((((double) (0.0 - Double.NaN)) == 0) ? 303 : 303):
      case ((((double) (1.0 - Double.NEGATIVE_INFINITY)) == 0) ? 304 : 304):
      case ((((double) (1.0 - -1.0)) == 0) ? 305 : 305):
      case ((((double) (1.0 - -0.0)) == 0) ? 306 : 306):
      case ((((double) (1.0 - 0.0)) == 0) ? 307 : 307):
      case ((((double) (1.0 - 1.0)) == 0) ? 308 : 308):
      case ((((double) (1.0 - Double.MAX_VALUE)) == 0) ? 309 : 309):
      case ((((double) (1.0 - Double.POSITIVE_INFINITY)) == 0) ? 310 : 310):
      case ((((double) (1.0 - Double.NaN)) == 0) ? 311 : 311):
      case ((((double) (Double.MAX_VALUE - Double.NEGATIVE_INFINITY)) == 0) ? 312 : 312):
      case ((((double) (Double.MAX_VALUE - -1.0)) == 0) ? 313 : 313):
      case ((((double) (Double.MAX_VALUE - -0.0)) == 0) ? 314 : 314):
      case ((((double) (Double.MAX_VALUE - 0.0)) == 0) ? 315 : 315):
      case ((((double) (Double.MAX_VALUE - 1.0)) == 0) ? 316 : 316):
      case ((((double) (Double.MAX_VALUE - Double.MAX_VALUE)) == 0) ? 317 : 317):
      case ((((double) (Double.MAX_VALUE - Double.POSITIVE_INFINITY)) == 0) ? 318 : 318):
      case ((((double) (Double.MAX_VALUE - Double.NaN)) == 0) ? 319 : 319):
      case ((((double) (Double.POSITIVE_INFINITY - Double.NEGATIVE_INFINITY)) == 0) ? 320 : 320):
      case ((((double) (Double.POSITIVE_INFINITY - -1.0)) == 0) ? 321 : 321):
      case ((((double) (Double.POSITIVE_INFINITY - -0.0)) == 0) ? 322 : 322):
      case ((((double) (Double.POSITIVE_INFINITY - 0.0)) == 0) ? 323 : 323):
      case ((((double) (Double.POSITIVE_INFINITY - 1.0)) == 0) ? 324 : 324):
      case ((((double) (Double.POSITIVE_INFINITY - Double.MAX_VALUE)) == 0) ? 325 : 325):
      case ((((double) (Double.POSITIVE_INFINITY - Double.POSITIVE_INFINITY)) == 0) ? 326 : 326):
      case ((((double) (Double.POSITIVE_INFINITY - Double.NaN)) == 0) ? 327 : 327):
      case ((((double) (Double.NaN - Double.NEGATIVE_INFINITY)) == 0) ? 328 : 328):
      case ((((double) (Double.NaN - -1.0)) == 0) ? 329 : 329):
      case ((((double) (Double.NaN - -0.0)) == 0) ? 330 : 330):
      case ((((double) (Double.NaN - 0.0)) == 0) ? 331 : 331):
      case ((((double) (Double.NaN - 1.0)) == 0) ? 332 : 332):
      case ((((double) (Double.NaN - Double.MAX_VALUE)) == 0) ? 333 : 333):
      case ((((double) (Double.NaN - Double.POSITIVE_INFINITY)) == 0) ? 334 : 334):
      case ((((double) (Double.NaN - Double.NaN)) == 0) ? 335 : 335):
      case ((Double.NEGATIVE_INFINITY < Double.NEGATIVE_INFINITY) ? 336 : 336):
      case ((Double.NEGATIVE_INFINITY < -1.0) ? 337 : 337):
      case ((Double.NEGATIVE_INFINITY < -0.0) ? 338 : 338):
      case ((Double.NEGATIVE_INFINITY < 0.0) ? 339 : 339):
      case ((Double.NEGATIVE_INFINITY < 1.0) ? 340 : 340):
      case ((Double.NEGATIVE_INFINITY < Double.MAX_VALUE) ? 341 : 341):
      case ((Double.NEGATIVE_INFINITY < Double.POSITIVE_INFINITY) ? 342 : 342):
      case ((Double.NEGATIVE_INFINITY < Double.NaN) ? 343 : 343):
      case ((-1.0 < Double.NEGATIVE_INFINITY) ? 344 : 344):
      case ((-1.0 < -1.0) ? 345 : 345):
      case ((-1.0 < -0.0) ? 346 : 346):
      case ((-1.0 < 0.0) ? 347 : 347):
      case ((-1.0 < 1.0) ? 348 : 348):
      case ((-1.0 < Double.MAX_VALUE) ? 349 : 349):
      case ((-1.0 < Double.POSITIVE_INFINITY) ? 350 : 350):
      case ((-1.0 < Double.NaN) ? 351 : 351):
      case ((-0.0 < Double.NEGATIVE_INFINITY) ? 352 : 352):
      case ((-0.0 < -1.0) ? 353 : 353):
      case ((-0.0 < -0.0) ? 354 : 354):
      case ((-0.0 < 0.0) ? 355 : 355):
      case ((-0.0 < 1.0) ? 356 : 356):
      case ((-0.0 < Double.MAX_VALUE) ? 357 : 357):
      case ((-0.0 < Double.POSITIVE_INFINITY) ? 358 : 358):
      case ((-0.0 < Double.NaN) ? 359 : 359):
      case ((0.0 < Double.NEGATIVE_INFINITY) ? 360 : 360):
      case ((0.0 < -1.0) ? 361 : 361):
      case ((0.0 < -0.0) ? 362 : 362):
      case ((0.0 < 0.0) ? 363 : 363):
      case ((0.0 < 1.0) ? 364 : 364):
      case ((0.0 < Double.MAX_VALUE) ? 365 : 365):
      case ((0.0 < Double.POSITIVE_INFINITY) ? 366 : 366):
      case ((0.0 < Double.NaN) ? 367 : 367):
      case ((1.0 < Double.NEGATIVE_INFINITY) ? 368 : 368):
      case ((1.0 < -1.0) ? 369 : 369):
      case ((1.0 < -0.0) ? 370 : 370):
      case ((1.0 < 0.0) ? 371 : 371):
      case ((1.0 < 1.0) ? 372 : 372):
      case ((1.0 < Double.MAX_VALUE) ? 373 : 373):
      case ((1.0 < Double.POSITIVE_INFINITY) ? 374 : 374):
      case ((1.0 < Double.NaN) ? 375 : 375):
      case ((Double.MAX_VALUE < Double.NEGATIVE_INFINITY) ? 376 : 376):
      case ((Double.MAX_VALUE < -1.0) ? 377 : 377):
      case ((Double.MAX_VALUE < -0.0) ? 378 : 378):
      case ((Double.MAX_VALUE < 0.0) ? 379 : 379):
      case ((Double.MAX_VALUE < 1.0) ? 380 : 380):
      case ((Double.MAX_VALUE < Double.MAX_VALUE) ? 381 : 381):
      case ((Double.MAX_VALUE < Double.POSITIVE_INFINITY) ? 382 : 382):
      case ((Double.MAX_VALUE < Double.NaN) ? 383 : 383):
      case ((Double.POSITIVE_INFINITY < Double.NEGATIVE_INFINITY) ? 384 : 384):
      case ((Double.POSITIVE_INFINITY < -1.0) ? 385 : 385):
      case ((Double.POSITIVE_INFINITY < -0.0) ? 386 : 386):
      case ((Double.POSITIVE_INFINITY < 0.0) ? 387 : 387):
      case ((Double.POSITIVE_INFINITY < 1.0) ? 388 : 388):
      case ((Double.POSITIVE_INFINITY < Double.MAX_VALUE) ? 389 : 389):
      case ((Double.POSITIVE_INFINITY < Double.POSITIVE_INFINITY) ? 390 : 390):
      case ((Double.POSITIVE_INFINITY < Double.NaN) ? 391 : 391):
      case ((Double.NaN < Double.NEGATIVE_INFINITY) ? 392 : 392):
      case ((Double.NaN < -1.0) ? 393 : 393):
      case ((Double.NaN < -0.0) ? 394 : 394):
      case ((Double.NaN < 0.0) ? 395 : 395):
      case ((Double.NaN < 1.0) ? 396 : 396):
      case ((Double.NaN < Double.MAX_VALUE) ? 397 : 397):
      case ((Double.NaN < Double.POSITIVE_INFINITY) ? 398 : 398):
      case ((Double.NaN < Double.NaN) ? 399 : 399):
      case ((Double.NEGATIVE_INFINITY > Double.NEGATIVE_INFINITY) ? 400 : 400):
      case ((Double.NEGATIVE_INFINITY > -1.0) ? 401 : 401):
      case ((Double.NEGATIVE_INFINITY > -0.0) ? 402 : 402):
      case ((Double.NEGATIVE_INFINITY > 0.0) ? 403 : 403):
      case ((Double.NEGATIVE_INFINITY > 1.0) ? 404 : 404):
      case ((Double.NEGATIVE_INFINITY > Double.MAX_VALUE) ? 405 : 405):
      case ((Double.NEGATIVE_INFINITY > Double.POSITIVE_INFINITY) ? 406 : 406):
      case ((Double.NEGATIVE_INFINITY > Double.NaN) ? 407 : 407):
      case ((-1.0 > Double.NEGATIVE_INFINITY) ? 408 : 408):
      case ((-1.0 > -1.0) ? 409 : 409):
      case ((-1.0 > -0.0) ? 410 : 410):
      case ((-1.0 > 0.0) ? 411 : 411):
      case ((-1.0 > 1.0) ? 412 : 412):
      case ((-1.0 > Double.MAX_VALUE) ? 413 : 413):
      case ((-1.0 > Double.POSITIVE_INFINITY) ? 414 : 414):
      case ((-1.0 > Double.NaN) ? 415 : 415):
      case ((-0.0 > Double.NEGATIVE_INFINITY) ? 416 : 416):
      case ((-0.0 > -1.0) ? 417 : 417):
      case ((-0.0 > -0.0) ? 418 : 418):
      case ((-0.0 > 0.0) ? 419 : 419):
      case ((-0.0 > 1.0) ? 420 : 420):
      case ((-0.0 > Double.MAX_VALUE) ? 421 : 421):
      case ((-0.0 > Double.POSITIVE_INFINITY) ? 422 : 422):
      case ((-0.0 > Double.NaN) ? 423 : 423):
      case ((0.0 > Double.NEGATIVE_INFINITY) ? 424 : 424):
      case ((0.0 > -1.0) ? 425 : 425):
      case ((0.0 > -0.0) ? 426 : 426):
      case ((0.0 > 0.0) ? 427 : 427):
      case ((0.0 > 1.0) ? 428 : 428):
      case ((0.0 > Double.MAX_VALUE) ? 429 : 429):
      case ((0.0 > Double.POSITIVE_INFINITY) ? 430 : 430):
      case ((0.0 > Double.NaN) ? 431 : 431):
      case ((1.0 > Double.NEGATIVE_INFINITY) ? 432 : 432):
      case ((1.0 > -1.0) ? 433 : 433):
      case ((1.0 > -0.0) ? 434 : 434):
      case ((1.0 > 0.0) ? 435 : 435):
      case ((1.0 > 1.0) ? 436 : 436):
      case ((1.0 > Double.MAX_VALUE) ? 437 : 437):
      case ((1.0 > Double.POSITIVE_INFINITY) ? 438 : 438):
      case ((1.0 > Double.NaN) ? 439 : 439):
      case ((Double.MAX_VALUE > Double.NEGATIVE_INFINITY) ? 440 : 440):
      case ((Double.MAX_VALUE > -1.0) ? 441 : 441):
      case ((Double.MAX_VALUE > -0.0) ? 442 : 442):
      case ((Double.MAX_VALUE > 0.0) ? 443 : 443):
      case ((Double.MAX_VALUE > 1.0) ? 444 : 444):
      case ((Double.MAX_VALUE > Double.MAX_VALUE) ? 445 : 445):
      case ((Double.MAX_VALUE > Double.POSITIVE_INFINITY) ? 446 : 446):
      case ((Double.MAX_VALUE > Double.NaN) ? 447 : 447):
      case ((Double.POSITIVE_INFINITY > Double.NEGATIVE_INFINITY) ? 448 : 448):
      case ((Double.POSITIVE_INFINITY > -1.0) ? 449 : 449):
      case ((Double.POSITIVE_INFINITY > -0.0) ? 450 : 450):
      case ((Double.POSITIVE_INFINITY > 0.0) ? 451 : 451):
      case ((Double.POSITIVE_INFINITY > 1.0) ? 452 : 452):
      case ((Double.POSITIVE_INFINITY > Double.MAX_VALUE) ? 453 : 453):
      case ((Double.POSITIVE_INFINITY > Double.POSITIVE_INFINITY) ? 454 : 454):
      case ((Double.POSITIVE_INFINITY > Double.NaN) ? 455 : 455):
      case ((Double.NaN > Double.NEGATIVE_INFINITY) ? 456 : 456):
      case ((Double.NaN > -1.0) ? 457 : 457):
      case ((Double.NaN > -0.0) ? 458 : 458):
      case ((Double.NaN > 0.0) ? 459 : 459):
      case ((Double.NaN > 1.0) ? 460 : 460):
      case ((Double.NaN > Double.MAX_VALUE) ? 461 : 461):
      case ((Double.NaN > Double.POSITIVE_INFINITY) ? 462 : 462):
      case ((Double.NaN > Double.NaN) ? 463 : 463):
      case ((Double.NEGATIVE_INFINITY <= Double.NEGATIVE_INFINITY) ? 464 : 464):
      case ((Double.NEGATIVE_INFINITY <= -1.0) ? 465 : 465):
      case ((Double.NEGATIVE_INFINITY <= -0.0) ? 466 : 466):
      case ((Double.NEGATIVE_INFINITY <= 0.0) ? 467 : 467):
      case ((Double.NEGATIVE_INFINITY <= 1.0) ? 468 : 468):
      case ((Double.NEGATIVE_INFINITY <= Double.MAX_VALUE) ? 469 : 469):
      case ((Double.NEGATIVE_INFINITY <= Double.POSITIVE_INFINITY) ? 470 : 470):
      case ((Double.NEGATIVE_INFINITY <= Double.NaN) ? 471 : 471):
      case ((-1.0 <= Double.NEGATIVE_INFINITY) ? 472 : 472):
      case ((-1.0 <= -1.0) ? 473 : 473):
      case ((-1.0 <= -0.0) ? 474 : 474):
      case ((-1.0 <= 0.0) ? 475 : 475):
      case ((-1.0 <= 1.0) ? 476 : 476):
      case ((-1.0 <= Double.MAX_VALUE) ? 477 : 477):
      case ((-1.0 <= Double.POSITIVE_INFINITY) ? 478 : 478):
      case ((-1.0 <= Double.NaN) ? 479 : 479):
      case ((-0.0 <= Double.NEGATIVE_INFINITY) ? 480 : 480):
      case ((-0.0 <= -1.0) ? 481 : 481):
      case ((-0.0 <= -0.0) ? 482 : 482):
      case ((-0.0 <= 0.0) ? 483 : 483):
      case ((-0.0 <= 1.0) ? 484 : 484):
      case ((-0.0 <= Double.MAX_VALUE) ? 485 : 485):
      case ((-0.0 <= Double.POSITIVE_INFINITY) ? 486 : 486):
      case ((-0.0 <= Double.NaN) ? 487 : 487):
      case ((0.0 <= Double.NEGATIVE_INFINITY) ? 488 : 488):
      case ((0.0 <= -1.0) ? 489 : 489):
      case ((0.0 <= -0.0) ? 490 : 490):
      case ((0.0 <= 0.0) ? 491 : 491):
      case ((0.0 <= 1.0) ? 492 : 492):
      case ((0.0 <= Double.MAX_VALUE) ? 493 : 493):
      case ((0.0 <= Double.POSITIVE_INFINITY) ? 494 : 494):
      case ((0.0 <= Double.NaN) ? 495 : 495):
      case ((1.0 <= Double.NEGATIVE_INFINITY) ? 496 : 496):
      case ((1.0 <= -1.0) ? 497 : 497):
      case ((1.0 <= -0.0) ? 498 : 498):
      case ((1.0 <= 0.0) ? 499 : 499):
      case ((1.0 <= 1.0) ? 500 : 500):
      case ((1.0 <= Double.MAX_VALUE) ? 501 : 501):
      case ((1.0 <= Double.POSITIVE_INFINITY) ? 502 : 502):
      case ((1.0 <= Double.NaN) ? 503 : 503):
      case ((Double.MAX_VALUE <= Double.NEGATIVE_INFINITY) ? 504 : 504):
      case ((Double.MAX_VALUE <= -1.0) ? 505 : 505):
      case ((Double.MAX_VALUE <= -0.0) ? 506 : 506):
      case ((Double.MAX_VALUE <= 0.0) ? 507 : 507):
      case ((Double.MAX_VALUE <= 1.0) ? 508 : 508):
      case ((Double.MAX_VALUE <= Double.MAX_VALUE) ? 509 : 509):
      case ((Double.MAX_VALUE <= Double.POSITIVE_INFINITY) ? 510 : 510):
      case ((Double.MAX_VALUE <= Double.NaN) ? 511 : 511):
      case ((Double.POSITIVE_INFINITY <= Double.NEGATIVE_INFINITY) ? 512 : 512):
      case ((Double.POSITIVE_INFINITY <= -1.0) ? 513 : 513):
      case ((Double.POSITIVE_INFINITY <= -0.0) ? 514 : 514):
      case ((Double.POSITIVE_INFINITY <= 0.0) ? 515 : 515):
      case ((Double.POSITIVE_INFINITY <= 1.0) ? 516 : 516):
      case ((Double.POSITIVE_INFINITY <= Double.MAX_VALUE) ? 517 : 517):
      case ((Double.POSITIVE_INFINITY <= Double.POSITIVE_INFINITY) ? 518 : 518):
      case ((Double.POSITIVE_INFINITY <= Double.NaN) ? 519 : 519):
      case ((Double.NaN <= Double.NEGATIVE_INFINITY) ? 520 : 520):
      case ((Double.NaN <= -1.0) ? 521 : 521):
      case ((Double.NaN <= -0.0) ? 522 : 522):
      case ((Double.NaN <= 0.0) ? 523 : 523):
      case ((Double.NaN <= 1.0) ? 524 : 524):
      case ((Double.NaN <= Double.MAX_VALUE) ? 525 : 525):
      case ((Double.NaN <= Double.POSITIVE_INFINITY) ? 526 : 526):
      case ((Double.NaN <= Double.NaN) ? 527 : 527):
      case ((Double.NEGATIVE_INFINITY >= Double.NEGATIVE_INFINITY) ? 528 : 528):
      case ((Double.NEGATIVE_INFINITY >= -1.0) ? 529 : 529):
      case ((Double.NEGATIVE_INFINITY >= -0.0) ? 530 : 530):
      case ((Double.NEGATIVE_INFINITY >= 0.0) ? 531 : 531):
      case ((Double.NEGATIVE_INFINITY >= 1.0) ? 532 : 532):
      case ((Double.NEGATIVE_INFINITY >= Double.MAX_VALUE) ? 533 : 533):
      case ((Double.NEGATIVE_INFINITY >= Double.POSITIVE_INFINITY) ? 534 : 534):
      case ((Double.NEGATIVE_INFINITY >= Double.NaN) ? 535 : 535):
      case ((-1.0 >= Double.NEGATIVE_INFINITY) ? 536 : 536):
      case ((-1.0 >= -1.0) ? 537 : 537):
      case ((-1.0 >= -0.0) ? 538 : 538):
      case ((-1.0 >= 0.0) ? 539 : 539):
      case ((-1.0 >= 1.0) ? 540 : 540):
      case ((-1.0 >= Double.MAX_VALUE) ? 541 : 541):
      case ((-1.0 >= Double.POSITIVE_INFINITY) ? 542 : 542):
      case ((-1.0 >= Double.NaN) ? 543 : 543):
      case ((-0.0 >= Double.NEGATIVE_INFINITY) ? 544 : 544):
      case ((-0.0 >= -1.0) ? 545 : 545):
      case ((-0.0 >= -0.0) ? 546 : 546):
      case ((-0.0 >= 0.0) ? 547 : 547):
      case ((-0.0 >= 1.0) ? 548 : 548):
      case ((-0.0 >= Double.MAX_VALUE) ? 549 : 549):
      case ((-0.0 >= Double.POSITIVE_INFINITY) ? 550 : 550):
      case ((-0.0 >= Double.NaN) ? 551 : 551):
      case ((0.0 >= Double.NEGATIVE_INFINITY) ? 552 : 552):
      case ((0.0 >= -1.0) ? 553 : 553):
      case ((0.0 >= -0.0) ? 554 : 554):
      case ((0.0 >= 0.0) ? 555 : 555):
      case ((0.0 >= 1.0) ? 556 : 556):
      case ((0.0 >= Double.MAX_VALUE) ? 557 : 557):
      case ((0.0 >= Double.POSITIVE_INFINITY) ? 558 : 558):
      case ((0.0 >= Double.NaN) ? 559 : 559):
      case ((1.0 >= Double.NEGATIVE_INFINITY) ? 560 : 560):
      case ((1.0 >= -1.0) ? 561 : 561):
      case ((1.0 >= -0.0) ? 562 : 562):
      case ((1.0 >= 0.0) ? 563 : 563):
      case ((1.0 >= 1.0) ? 564 : 564):
      case ((1.0 >= Double.MAX_VALUE) ? 565 : 565):
      case ((1.0 >= Double.POSITIVE_INFINITY) ? 566 : 566):
      case ((1.0 >= Double.NaN) ? 567 : 567):
      case ((Double.MAX_VALUE >= Double.NEGATIVE_INFINITY) ? 568 : 568):
      case ((Double.MAX_VALUE >= -1.0) ? 569 : 569):
      case ((Double.MAX_VALUE >= -0.0) ? 570 : 570):
      case ((Double.MAX_VALUE >= 0.0) ? 571 : 571):
      case ((Double.MAX_VALUE >= 1.0) ? 572 : 572):
      case ((Double.MAX_VALUE >= Double.MAX_VALUE) ? 573 : 573):
      case ((Double.MAX_VALUE >= Double.POSITIVE_INFINITY) ? 574 : 574):
      case ((Double.MAX_VALUE >= Double.NaN) ? 575 : 575):
      case ((Double.POSITIVE_INFINITY >= Double.NEGATIVE_INFINITY) ? 576 : 576):
      case ((Double.POSITIVE_INFINITY >= -1.0) ? 577 : 577):
      case ((Double.POSITIVE_INFINITY >= -0.0) ? 578 : 578):
      case ((Double.POSITIVE_INFINITY >= 0.0) ? 579 : 579):
      case ((Double.POSITIVE_INFINITY >= 1.0) ? 580 : 580):
      case ((Double.POSITIVE_INFINITY >= Double.MAX_VALUE) ? 581 : 581):
      case ((Double.POSITIVE_INFINITY >= Double.POSITIVE_INFINITY) ? 582 : 582):
      case ((Double.POSITIVE_INFINITY >= Double.NaN) ? 583 : 583):
      case ((Double.NaN >= Double.NEGATIVE_INFINITY) ? 584 : 584):
      case ((Double.NaN >= -1.0) ? 585 : 585):
      case ((Double.NaN >= -0.0) ? 586 : 586):
      case ((Double.NaN >= 0.0) ? 587 : 587):
      case ((Double.NaN >= 1.0) ? 588 : 588):
      case ((Double.NaN >= Double.MAX_VALUE) ? 589 : 589):
      case ((Double.NaN >= Double.POSITIVE_INFINITY) ? 590 : 590):
      case ((Double.NaN >= Double.NaN) ? 591 : 591):
      case ((Double.NEGATIVE_INFINITY == Double.NEGATIVE_INFINITY) ? 592 : 592):
      case ((Double.NEGATIVE_INFINITY == -1.0) ? 593 : 593):
      case ((Double.NEGATIVE_INFINITY == -0.0) ? 594 : 594):
      case ((Double.NEGATIVE_INFINITY == 0.0) ? 595 : 595):
      case ((Double.NEGATIVE_INFINITY == 1.0) ? 596 : 596):
      case ((Double.NEGATIVE_INFINITY == Double.MAX_VALUE) ? 597 : 597):
      case ((Double.NEGATIVE_INFINITY == Double.POSITIVE_INFINITY) ? 598 : 598):
      case ((Double.NEGATIVE_INFINITY == Double.NaN) ? 599 : 599):
      case ((-1.0 == Double.NEGATIVE_INFINITY) ? 600 : 600):
      case ((-1.0 == -1.0) ? 601 : 601):
      case ((-1.0 == -0.0) ? 602 : 602):
      case ((-1.0 == 0.0) ? 603 : 603):
      case ((-1.0 == 1.0) ? 604 : 604):
      case ((-1.0 == Double.MAX_VALUE) ? 605 : 605):
      case ((-1.0 == Double.POSITIVE_INFINITY) ? 606 : 606):
      case ((-1.0 == Double.NaN) ? 607 : 607):
      case ((-0.0 == Double.NEGATIVE_INFINITY) ? 608 : 608):
      case ((-0.0 == -1.0) ? 609 : 609):
      case ((-0.0 == -0.0) ? 610 : 610):
      case ((-0.0 == 0.0) ? 611 : 611):
      case ((-0.0 == 1.0) ? 612 : 612):
      case ((-0.0 == Double.MAX_VALUE) ? 613 : 613):
      case ((-0.0 == Double.POSITIVE_INFINITY) ? 614 : 614):
      case ((-0.0 == Double.NaN) ? 615 : 615):
      case ((0.0 == Double.NEGATIVE_INFINITY) ? 616 : 616):
      case ((0.0 == -1.0) ? 617 : 617):
      case ((0.0 == -0.0) ? 618 : 618):
      case ((0.0 == 0.0) ? 619 : 619):
      case ((0.0 == 1.0) ? 620 : 620):
      case ((0.0 == Double.MAX_VALUE) ? 621 : 621):
      case ((0.0 == Double.POSITIVE_INFINITY) ? 622 : 622):
      case ((0.0 == Double.NaN) ? 623 : 623):
      case ((1.0 == Double.NEGATIVE_INFINITY) ? 624 : 624):
      case ((1.0 == -1.0) ? 625 : 625):
      case ((1.0 == -0.0) ? 626 : 626):
      case ((1.0 == 0.0) ? 627 : 627):
      case ((1.0 == 1.0) ? 628 : 628):
      case ((1.0 == Double.MAX_VALUE) ? 629 : 629):
      case ((1.0 == Double.POSITIVE_INFINITY) ? 630 : 630):
      case ((1.0 == Double.NaN) ? 631 : 631):
      case ((Double.MAX_VALUE == Double.NEGATIVE_INFINITY) ? 632 : 632):
      case ((Double.MAX_VALUE == -1.0) ? 633 : 633):
      case ((Double.MAX_VALUE == -0.0) ? 634 : 634):
      case ((Double.MAX_VALUE == 0.0) ? 635 : 635):
      case ((Double.MAX_VALUE == 1.0) ? 636 : 636):
      case ((Double.MAX_VALUE == Double.MAX_VALUE) ? 637 : 637):
      case ((Double.MAX_VALUE == Double.POSITIVE_INFINITY) ? 638 : 638):
      case ((Double.MAX_VALUE == Double.NaN) ? 639 : 639):
      case ((Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY) ? 640 : 640):
      case ((Double.POSITIVE_INFINITY == -1.0) ? 641 : 641):
      case ((Double.POSITIVE_INFINITY == -0.0) ? 642 : 642):
      case ((Double.POSITIVE_INFINITY == 0.0) ? 643 : 643):
      case ((Double.POSITIVE_INFINITY == 1.0) ? 644 : 644):
      case ((Double.POSITIVE_INFINITY == Double.MAX_VALUE) ? 645 : 645):
      case ((Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY) ? 646 : 646):
      case ((Double.POSITIVE_INFINITY == Double.NaN) ? 647 : 647):
      case ((Double.NaN == Double.NEGATIVE_INFINITY) ? 648 : 648):
      case ((Double.NaN == -1.0) ? 649 : 649):
      case ((Double.NaN == -0.0) ? 650 : 650):
      case ((Double.NaN == 0.0) ? 651 : 651):
      case ((Double.NaN == 1.0) ? 652 : 652):
      case ((Double.NaN == Double.MAX_VALUE) ? 653 : 653):
      case ((Double.NaN == Double.POSITIVE_INFINITY) ? 654 : 654):
      case ((Double.NaN == Double.NaN) ? 655 : 655):
      case ((Double.NEGATIVE_INFINITY != Double.NEGATIVE_INFINITY) ? 656 : 656):
      case ((Double.NEGATIVE_INFINITY != -1.0) ? 657 : 657):
      case ((Double.NEGATIVE_INFINITY != -0.0) ? 658 : 658):
      case ((Double.NEGATIVE_INFINITY != 0.0) ? 659 : 659):
      case ((Double.NEGATIVE_INFINITY != 1.0) ? 660 : 660):
      case ((Double.NEGATIVE_INFINITY != Double.MAX_VALUE) ? 661 : 661):
      case ((Double.NEGATIVE_INFINITY != Double.POSITIVE_INFINITY) ? 662 : 662):
      case ((Double.NEGATIVE_INFINITY != Double.NaN) ? 663 : 663):
      case ((-1.0 != Double.NEGATIVE_INFINITY) ? 664 : 664):
      case ((-1.0 != -1.0) ? 665 : 665):
      case ((-1.0 != -0.0) ? 666 : 666):
      case ((-1.0 != 0.0) ? 667 : 667):
      case ((-1.0 != 1.0) ? 668 : 668):
      case ((-1.0 != Double.MAX_VALUE) ? 669 : 669):
      case ((-1.0 != Double.POSITIVE_INFINITY) ? 670 : 670):
      case ((-1.0 != Double.NaN) ? 671 : 671):
      case ((-0.0 != Double.NEGATIVE_INFINITY) ? 672 : 672):
      case ((-0.0 != -1.0) ? 673 : 673):
      case ((-0.0 != -0.0) ? 674 : 674):
      case ((-0.0 != 0.0) ? 675 : 675):
      case ((-0.0 != 1.0) ? 676 : 676):
      case ((-0.0 != Double.MAX_VALUE) ? 677 : 677):
      case ((-0.0 != Double.POSITIVE_INFINITY) ? 678 : 678):
      case ((-0.0 != Double.NaN) ? 679 : 679):
      case ((0.0 != Double.NEGATIVE_INFINITY) ? 680 : 680):
      case ((0.0 != -1.0) ? 681 : 681):
      case ((0.0 != -0.0) ? 682 : 682):
      case ((0.0 != 0.0) ? 683 : 683):
      case ((0.0 != 1.0) ? 684 : 684):
      case ((0.0 != Double.MAX_VALUE) ? 685 : 685):
      case ((0.0 != Double.POSITIVE_INFINITY) ? 686 : 686):
      case ((0.0 != Double.NaN) ? 687 : 687):
      case ((1.0 != Double.NEGATIVE_INFINITY) ? 688 : 688):
      case ((1.0 != -1.0) ? 689 : 689):
      case ((1.0 != -0.0) ? 690 : 690):
      case ((1.0 != 0.0) ? 691 : 691):
      case ((1.0 != 1.0) ? 692 : 692):
      case ((1.0 != Double.MAX_VALUE) ? 693 : 693):
      case ((1.0 != Double.POSITIVE_INFINITY) ? 694 : 694):
      case ((1.0 != Double.NaN) ? 695 : 695):
      case ((Double.MAX_VALUE != Double.NEGATIVE_INFINITY) ? 696 : 696):
      case ((Double.MAX_VALUE != -1.0) ? 697 : 697):
      case ((Double.MAX_VALUE != -0.0) ? 698 : 698):
      case ((Double.MAX_VALUE != 0.0) ? 699 : 699):
      case ((Double.MAX_VALUE != 1.0) ? 700 : 700):
      case ((Double.MAX_VALUE != Double.MAX_VALUE) ? 701 : 701):
      case ((Double.MAX_VALUE != Double.POSITIVE_INFINITY) ? 702 : 702):
      case ((Double.MAX_VALUE != Double.NaN) ? 703 : 703):
      case ((Double.POSITIVE_INFINITY != Double.NEGATIVE_INFINITY) ? 704 : 704):
      case ((Double.POSITIVE_INFINITY != -1.0) ? 705 : 705):
      case ((Double.POSITIVE_INFINITY != -0.0) ? 706 : 706):
      case ((Double.POSITIVE_INFINITY != 0.0) ? 707 : 707):
      case ((Double.POSITIVE_INFINITY != 1.0) ? 708 : 708):
      case ((Double.POSITIVE_INFINITY != Double.MAX_VALUE) ? 709 : 709):
      case ((Double.POSITIVE_INFINITY != Double.POSITIVE_INFINITY) ? 710 : 710):
      case ((Double.POSITIVE_INFINITY != Double.NaN) ? 711 : 711):
      case ((Double.NaN != Double.NEGATIVE_INFINITY) ? 712 : 712):
      case ((Double.NaN != -1.0) ? 713 : 713):
      case ((Double.NaN != -0.0) ? 714 : 714):
      case ((Double.NaN != 0.0) ? 715 : 715):
      case ((Double.NaN != 1.0) ? 716 : 716):
      case ((Double.NaN != Double.MAX_VALUE) ? 717 : 717):
      case ((Double.NaN != Double.POSITIVE_INFINITY) ? 718 : 718):
      case ((Double.NaN != Double.NaN) ? 719 : 719):
      default:
    }
  }

  // --------
  // boolean tests
  static boolean booleanLogNot(boolean x) { return ! x; }
  static boolean booleanEq(boolean x, boolean y) { return x == y; }
  static boolean booleanNe(boolean x, boolean y) { return x != y; }
  static boolean booleanAnd(boolean x, boolean y) { return (boolean) (x & y); }
  static boolean booleanXor(boolean x, boolean y) { return (boolean) (x ^ y); }
  static boolean booleanOr(boolean x, boolean y) { return (boolean) (x | y); }
  static void booleanTest() {
    Tester.checkEqual(booleanLogNot(true), ! true, "! true");
    Tester.checkEqual(booleanLogNot(false), ! false, "! false");
    Tester.checkEqual(booleanEq(true, true), true == true, "true == true");
    Tester.checkEqual(booleanEq(true, false), true == false, "true == false");
    Tester.checkEqual(booleanEq(false, true), false == true, "false == true");
    Tester.checkEqual(booleanEq(false, false), false == false, "false == false");
    Tester.checkEqual(booleanNe(true, true), true != true, "true != true");
    Tester.checkEqual(booleanNe(true, false), true != false, "true != false");
    Tester.checkEqual(booleanNe(false, true), false != true, "false != true");
    Tester.checkEqual(booleanNe(false, false), false != false, "false != false");
    Tester.checkEqual(booleanAnd(true, true), (boolean) (true & true), "(boolean) (true & true)");
    Tester.checkEqual(booleanAnd(true, false), (boolean) (true & false), "(boolean) (true & false)");
    Tester.checkEqual(booleanAnd(false, true), (boolean) (false & true), "(boolean) (false & true)");
    Tester.checkEqual(booleanAnd(false, false), (boolean) (false & false), "(boolean) (false & false)");
    Tester.checkEqual(booleanXor(true, true), (boolean) (true ^ true), "(boolean) (true ^ true)");
    Tester.checkEqual(booleanXor(true, false), (boolean) (true ^ false), "(boolean) (true ^ false)");
    Tester.checkEqual(booleanXor(false, true), (boolean) (false ^ true), "(boolean) (false ^ true)");
    Tester.checkEqual(booleanXor(false, false), (boolean) (false ^ false), "(boolean) (false ^ false)");
    Tester.checkEqual(booleanOr(true, true), (boolean) (true | true), "(boolean) (true | true)");
    Tester.checkEqual(booleanOr(true, false), (boolean) (true | false), "(boolean) (true | false)");
    Tester.checkEqual(booleanOr(false, true), (boolean) (false | true), "(boolean) (false | true)");
    Tester.checkEqual(booleanOr(false, false), (boolean) (false | false), "(boolean) (false | false)");
  }
  static void booleanSwitch() {
    switch(0) {
      case ((! true) ? 0 : 0):
      case ((! false) ? 1 : 1):
      case ((true == true) ? 2 : 2):
      case ((true == false) ? 3 : 3):
      case ((false == true) ? 4 : 4):
      case ((false == false) ? 5 : 5):
      case ((true != true) ? 6 : 6):
      case ((true != false) ? 7 : 7):
      case ((false != true) ? 8 : 8):
      case ((false != false) ? 9 : 9):
      case ((((boolean) (true & true)) == true) ? 10 : 10):
      case ((((boolean) (true & false)) == true) ? 11 : 11):
      case ((((boolean) (false & true)) == true) ? 12 : 12):
      case ((((boolean) (false & false)) == true) ? 13 : 13):
      case ((((boolean) (true ^ true)) == true) ? 14 : 14):
      case ((((boolean) (true ^ false)) == true) ? 15 : 15):
      case ((((boolean) (false ^ true)) == true) ? 16 : 16):
      case ((((boolean) (false ^ false)) == true) ? 17 : 17):
      case ((((boolean) (true | true)) == true) ? 18 : 18):
      case ((((boolean) (true | false)) == true) ? 19 : 19):
      case ((((boolean) (false | true)) == true) ? 20 : 20):
      case ((((boolean) (false | false)) == true) ? 21 : 21):
      default:
    }
  }
}


/*

(define table
  '((byte ("Byte.MIN_VALUE" "(byte) -1" "(byte) 0" "(byte) 1" "Byte.MAX_VALUE") 
      ("+" "-" "~") 
      ("*" "/" "%" "+" "-" "<<" ">>" ">>>" "<" ">" "<=" ">=" "==" "!=" "&" "^" "|"))
    (short ("Short.MIN_VALUE" "(short) -1" "(short) 0" "(short) 1" "Short.MAX_VALUE") 
      ("+" "-" "~") 
      ("*" "/" "%" "+" "-" "<<" ">>" ">>>" "<" ">" "<=" ">=" "==" "!=" "&" "^" "|"))
    (char ("(char) 0" "(char) 1" "Character.MAX_VALUE") 
      ("+" "-" "~") 
      ("*" "/" "%" "+" "-" "<<" ">>" ">>>" "<" ">" "<=" ">=" "==" "!=" "&" "^" "|"))
    (int ("Integer.MIN_VALUE" "-1" "0" "1" "Integer.MAX_VALUE") 
      ("+" "-" "~") 
      ("*" "/" "%" "+" "-" "<<" ">>" ">>>" "<" ">" "<=" ">=" "==" "!=" "&" "^" "|"))
    (long ("Long.MIN_VALUE" "-1L" "0L" "1L" "Long.MAX_VALUE") 
      ("+" "-" "~") 
      ("*" "/" "%" "+" "-" "<<" ">>" ">>>" "<" ">" "<=" ">=" "==" "!=" "&" "^" "|"))
    (float ("Float.NEGATIVE_INFINITY" "-1.0f" "-0.0f" "0.0f" "Float.MIN_VALUE" "1.0f" "Float.MAX_VALUE" "Float.POSITIVE_INFINITY" "Float.NaN") 
      ("+" "-") 
      ("*" "/" "%" "+" "-" "<" ">" "<=" ">=" "==" "!="))
    (double ("Double.NEGATIVE_INFINITY" "-1.0" "-0.0" "0.0"
	     ;; "Double.MIN_VALUE"  NOT CORRECT IN 1.3
	     "1.0" "Double.MAX_VALUE" "Double.POSITIVE_INFINITY" "Double.NaN") 
      ("+" "-") 
      ("*" "/" "%" "+" "-" "<" ">" "<=" ">=" "==" "!="))
    (boolean (true false) 
      ("!") 
      ("==" "!=" "&" "^" "|"))))

(define booleanOps '("<" ">" "<=" ">=" "==" "!=" "!"))
(define divisionOps '("/" "%"))
(define zeroes '("(byte) 0" "(char) 0" "(short) 0" "0" "0L"))

(define unames
  '(("+" "Plus") ("-" "Minus") ("~" "BitNot") ("!" "LogNot")))

(define binames
  '(("*" "Times") ("/" "Div") ("%" "Rem") ("+" "Add") ("-" "Sub")
    ("<<" "Shl") (">>" "Shr") (">>>" "Ushr")
    ("<" "Lt") (">" "Gt") ("<=" "Le") (">=" "Ge") ("==" "Eq") ("!=" "Ne")
    ("&" "And") ("^" "Xor") ("|" "Or")))

(define gen
  (lambda ()
    (printf "import org.aspectj.testing.Tester;~n~n")
    (printf "public strictfp class BigOps {~n")
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
	  (vals (cadr ls))
	  (unops (caddr ls))
	  (binops (cadddr ls)))
      (let* ((elems (filterElems (make-elems vals unops binops)))
	     (uvals vals)
	     (ufuns (map (lambda (op) (genUnopFun type op)) unops))
	     (bifuns (map (lambda (op) (genBinopFun type op)) binops)))
	(string-append
	  (format "~n  // --------~n")
	  (format "  // ~a tests~n" type)
	  (apply string-append ufuns)
	  (apply string-append bifuns)
	  (genTester type elems)
	  (genSwitch type elems))))))

(define filterElems
  (lambda (ls)
    (let f ((ls ls))
      (if (null? ls) '()
	  (apply (lambda (exprfun callfun op args)
		   (if (and (member op divisionOps)
			    (member (cadr args) zeroes))
		       (f (cdr ls))
		       (cons (car ls) (f (cdr ls)))))
	    (car ls))))))

(define make-elems
  (lambda (vals unops binops)
    (append
      (map (lambda (x) (cons genUnopExpr (cons genUnopCall x)))
	(cross unops (map list vals)))
      (map (lambda (x) (cons genBinopExpr (cons genBinopCall x)))
	(cross binops (cross vals vals))))))

(define genTester
  (lambda (type ls)
    (string-append (format "  static void ~aTest() {~n" type)
      (apply string-append
	(map (lambda (ls)
	       (apply (lambda (exprfun callfun op args)
			(apply callfun type op args))
		 ls))
	  ls))
      (format "  }~n"))))

(define genSwitch
  (lambda (type ls)
    (string-append (format "  static void ~aSwitch() {~n    switch(0) {~n" type)
      (apply string-append
	(map (lambda (elem num) (apply genCase type num elem))
	  ls (iota ls)))
      (format "      default:~n    }~n  }~n"))))

(define genCase
  (lambda (type num exprfun callfun op args)
    (if (member op booleanOps)
	(format "      case ((~a) ? ~a : ~a):~n" (apply exprfun type op args) num num)
	(format "      case (((~a) == ~a) ? ~a : ~a):~n"
	  (apply exprfun type op args)
	  (if (eq? type 'boolean) 'true "0")
	  num num))))

(define genUnopExpr
  (lambda (type op val0)
    (if (member op booleanOps)
	(format "~a ~a" op val0)
	(format "(~a) ~a ~a" type op val0))))

(define genUnopFun
  (lambda (type op)
    (format "  static ~a ~a~a(~a x) { return ~a; }~n"
      (if (member op booleanOps) "boolean" type)
      type (cadr (assoc op unames))
      type
      (genUnopExpr type op "x"))))

(define genUnopCall
  (lambda (type op val)
    (format "    Tester.checkEqual(~a~a(~a), ~a, \"~a\");~n"
      type (cadr (assoc op unames))
      val
      (genUnopExpr type op val)
      (genUnopExpr type op val))))

(define genBinopExpr
  (lambda (type op val0 val1)
    (if (member op booleanOps)
	(format "~a ~a ~a" val0 op val1)
	(format "(~a) (~a ~a ~a)" type val0 op val1))))

(define genBinopFun
  (lambda (type op)
    (format "  static ~a ~a~a(~a x, ~a y) { return ~a; }~n"
      (if (member op booleanOps) "boolean" type)
      type (cadr (assoc op binames))
      type type
      (genBinopExpr type op "x" "y"))))

(define genBinopCall
  (lambda (type op val0 val1)
    (format "    Tester.checkEqual(~a~a(~a, ~a), ~a, \"~a\");~n"
      type (cadr (assoc op binames))
      val0
      val1
      (genBinopExpr type op val0 val1)
      (genBinopExpr type op val0 val1))))

(define cross2
  (lambda (a b c)
    (map (lambda (x) (cons (car x ) (cadr x)))
      (cross a (cross b c)))))

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
