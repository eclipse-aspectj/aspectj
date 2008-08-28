import java.util.List;

public class C {
	public <T> C(T b){
  }
  
  public <T> T returnT(T a){
	  return a;
  }
  
  public <Q extends List> Q returnQ(Q a){
	  return a;
  }
  
  public <T, Q> void doubleGeneric(Q a, T b){
  }
}
