import org.aspectj.lang.reflect.*;

public aspect Deow {

  declare warning : call(* System.*(..)) : "dont call system methods";
  declare error : call(* System.*(..)) : "dont call system methods";
  
  public static void main(String[] args) {
    AjType myType = AjTypeSystem.getAjType(Deow.class);
    DeclareErrorOrWarning[] deows = myType.getDeclareErrorOrWarnings();
    if (deows.length != 2) throw new RuntimeException("Excepting 2 deows, got: " + deows.length);
    int errorCount = 0;
    int warningCount = 0;
    if (deows[0].isError()) {
    	errorCount++;
    } else {
    	warningCount++;
    }
    if (deows[1].isError()) {
    	errorCount++;
    } else {
    	warningCount++;
    }
    if (errorCount != 1) { throw new RuntimeException("Expecting 1 declare error but found " + errorCount); }
    if (warningCount != 1) { throw new RuntimeException("Expecting 1 declare warning but found " + warningCount); }
    if (!deows[0].getMessage().equals("dont call system methods")) throw new RuntimeException("Bad message");
    if (!deows[1].getMessage().equals("dont call system methods")) throw new RuntimeException("Bad message");
    if (!deows[0].getPointcutExpression().toString().equals("call(* java.lang.System.*(..))")) throw new RuntimeException("Bad pc: " + deows[0].getPointcutExpression());
    if (!deows[1].getPointcutExpression().toString().equals("call(* java.lang.System.*(..))")) throw new RuntimeException("Bad pc: " + deows[0].getPointcutExpression());
  }
}