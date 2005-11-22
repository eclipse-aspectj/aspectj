import java.io.IOException;

public aspect Pr103051 {
    declare soft: IOException: within(Pr103051) && adviceexecution();

    before() : execution(* main(..)) {
        throw new IOException("test");
    }

    public static void main(String args[]) {
    }
}