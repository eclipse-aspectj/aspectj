//package info.unterstein.hagen.moderne.ea6.a3;

public class DataGenerator {

    private static final int MAGIC_NUMBER = 23;

    public Integer getData(Integer i) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        return new Integer(i * MAGIC_NUMBER);
    }
}
