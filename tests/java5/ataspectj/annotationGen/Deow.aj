import org.aspectj.lang.reflect.*;

public aspect Deow {

  declare warning : call(* System.*(..)) : "dont call system methods";
  declare error : call(* System.*(..)) : "dont call system methods";
  
  public static void main(String[] args) {
    AjType myType = AjTypeSystem.getAjType(Deow.class);
    DeclareErrorOrWarning[] deows = myType.getDeclareErrorOrWarnings();
    if (deows.length != 2) throw new RuntimeException("Excepting 2 deows, got: " + deows.length);
    if (deows[0].isError()) throw new RuntimeException("Expecting a warning");
    if (!deows[1].isError()) throw new RuntimeException("Expecting an error");
    if (!deows[0].getMessage().equals("dont call system methods")) throw new RuntimeException("Bad message");
    if (!deows[1].getMessage().equals("dont call system methods")) throw new RuntimeException("Bad message");
    if (!deows[0].getPointcutExpression().toString().equals("call(* java.lang.System.*(..))")) throw new RuntimeException("Bad pc: " + deows[0].getPointcutExpression());
    if (!deows[1].getPointcutExpression().toString().equals("call(* java.lang.System.*(..))")) throw new RuntimeException("Bad pc: " + deows[0].getPointcutExpression());
  }
}