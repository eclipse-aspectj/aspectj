package com;
import java.util.List;

public class Meths {

  @Anno
  public void m() { }

  @Anno @Anno2(a=3254)
  private List<String> n(int i,long l,List<Integer> li) {return null;}

}
