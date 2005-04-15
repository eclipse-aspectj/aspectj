// "@DeclareWarning"

import org.aspectj.lang.annotation.*;

aspect A{
  @DeclareWarning("call(* *..warnedCall())")
  static final String aMessage = "This call is warned";
  
  void warnedCall(){
  }
  
  public static void main(String [] args){
	  warnedCall();
  }
}
