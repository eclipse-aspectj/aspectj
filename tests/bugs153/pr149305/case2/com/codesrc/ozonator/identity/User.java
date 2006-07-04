package com.codesrc.ozonator.identity;

public class User
{
  private String name;

  public String getName()
  {
    return name;
  }

  public void setName( String name)
  {
    this.name = name;
  }

  static public void main(String args[])
  {
    User u = new User();
    System.out.println(u.getName());
    u.setName("blah blah");
    System.out.println(u.getName());
  }
  
}
