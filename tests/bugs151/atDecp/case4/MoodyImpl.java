package theapp;

import moody.*;

public class MoodyImpl implements Moody {
  private Mood mood = Mood.HAPPY;

  public Mood getMood() { return mood; }
  public void setMood(Mood mood) { this.mood = mood; }
}
