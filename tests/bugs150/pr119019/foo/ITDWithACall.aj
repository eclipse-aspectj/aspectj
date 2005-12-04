package foo;

import bar.TargetITDClass;

public aspect ITDWithACall {

  public static void main(String []argv) {
    new TargetITDClass().doSomething("hello");
  }

    public void TargetITDClass.doSomething(String param) {
        String changedParam= changeParam(param);
    }


    protected static String changeParam(String param) { /// <= HERE
        return param + "-modified";
    }
	
}
