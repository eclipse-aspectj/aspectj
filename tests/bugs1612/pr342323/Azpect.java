package ppp;
privileged aspect Azpect {
  public void Bean.foo() {
	  Runnable r = new Runnable() {
		    public void run() {
		        System.out.println("aspect foo");
		    }
	  };
	  r.run();
  }
}

