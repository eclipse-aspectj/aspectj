package de.scrum_master.galileo.filter;


public abstract class BasicFilter 
{
	protected abstract String getLogMessage();
  public void run() {
    System.out.println("run()");
  }
}
