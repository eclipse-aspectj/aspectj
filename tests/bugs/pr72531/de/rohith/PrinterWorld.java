package de.rohith;
public class PrinterWorld {
	private Integer[] intArray = new Integer[2];
	public PrinterWorld() {
		
	}
    public void print() {
        System.out.println("Hello World!"); 
    }
    
    public Integer returnInt() {
    	return new Integer(3);
    }
    
    public Integer[] returnArrayWithCloning() {
    	for (int i = 0; i < intArray.length; i++) {
			intArray[i] = new Integer(i++);
		}
    	return (Integer[])intArray.clone();
    }
    
    public Integer[] returnArrayWithoutCloning() {
    	return intArray;
    }
}
