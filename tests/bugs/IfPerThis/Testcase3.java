// Compiler limitation, can't put if() directly in per clause 
aspect Testcase3 perthis(execution(* doCommand(..)) && if(commandInterceptionEnabled)) {

  private static boolean commandInterceptionEnabled = true;

  public Testcase3() {
	System.out.println("Created a PerThis aspect : " + this.toString());
  }

  before(ICommand command) : execution(* doCommand(..)) && this(command) {
	System.out.println("Invoking command bean:  "+ command);
  }

  public static void main(String[] args) {
	ICommand c1 = new Command("hello");
	ICommand c2 = new Command("hello again");
	c1.doCommand();
	c2.doCommand();
  }

}

interface ICommand {
   void doCommand();
}

class Command implements ICommand {
 
  private String output = "";

  public Command(String s) { this.output = s; }

  public void doCommand() {
	System.out.println(output + "(" + this + ")");
  }
   
}