package de.rohith;
public class HelloWorld {

    public static void main(String[] args) {
        PrinterWorld p = new PrinterWorld();
        p.print(); 
        Integer i = p.returnInt();
        Integer[] intArray = p.returnArrayWithCloning();
        Integer[] array2 = p.returnArrayWithoutCloning();
    }
}
