package de.scrum_master.galileo.filter;


public class JsoupFilter extends BasicFilter {
	@Override
	protected String getLogMessage() {
      System.out.println("JsoupFilter.getLogMessage()");
		return "Cleaning up HTML, removing clutter, fixing structure";
	}

  public static void main(String []argv) {
    new JsoupFilter().run();
  }



}
