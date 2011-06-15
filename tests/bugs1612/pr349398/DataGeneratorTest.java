//package info.unterstein.hagen.moderne.ea6.a3;

public class DataGeneratorTest  {
  public static void main(String []argv) {
    new DataGeneratorTest().testGetData();
    new DataGeneratorTest().testGetDataSpeedUp();
  }

    public void testGetData() {
        DataGenerator generator = new DataGenerator();
        assertEquals(new Integer(0), generator.getData(0));
        assertEquals(new Integer(23), generator.getData(1));
        assertEquals(new Integer(2 * 23), generator.getData(2));
    }

    public void assertEquals(Object o, Object p) {
      if (!o.equals(p)) {
        throw new IllegalStateException();
      }
    }

    public void assertTrue(boolean b) {
      if (!b) {
        throw new IllegalStateException();
      }
    }

    public void testGetDataSpeedUp() {
        DataGenerator generator = new DataGenerator();
        long before = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            generator.getData(i);
        }
        for (int i = 0; i < 5; i++) {
            generator.getData(0);
        }
        long after = System.currentTimeMillis();
        assertTrue((after - before) < 600);
    }
}

