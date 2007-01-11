// Simple aspect that tramples all over Simple.java

public aspect AspectITD {
	
  int Simple.fieldint = 5;
  
  String Simple.fieldstring = "hello";
  
  public int Simple.returnint() {
  	return 5;
  }
  
  public String Simple.returnstring() {
  	return "abc";
  }
 
}