package test;

public class SecondTestExecutable extends AbstractExecutable {

	public void execute() {
		// should not happen because of ExecutionAspect prevents execution
		throw new RuntimeException();
	}
	
	public static void main(String[] args) {
		new SecondTestExecutable().execute(); 
	} 
}
