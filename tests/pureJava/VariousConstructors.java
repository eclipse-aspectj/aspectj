import org.aspectj.testing.Tester;

import java.awt.Component;

public class VariousConstructors {
    public static void main(String[] args) {
	Component c = new Component() {};
	AbstractC ac = new AbstractC() {};
    }
}

abstract class AbstractC {}
