package de.scrum_master.app;

public class Apple {
  private String type;
  private boolean sweet;

  public Apple(String type, boolean sweet) {
    this.type = type;
    this.sweet = sweet;
  }

  public String getType() {
    return type;
  }

  public boolean isSweet() {
    return sweet;
  }
}

