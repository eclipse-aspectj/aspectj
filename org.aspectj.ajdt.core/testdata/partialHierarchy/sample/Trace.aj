package sample;



public aspect Trace {
    public interface Traced {}
    declare parents: (sample.* && !Trace) extends Traced;

//    private Logger Traced.logger;
    before(Traced current) : 
	execution(Traced+.new(..)) && !execution(Traced.new()) && this(current) {
	current.getLogger().severe("entering ctor for "+thisJoinPoint);
    }

    public Logger Traced.getLogger() {
 //       if (logger == null) {
//	    logger = Logger.getLogger(getClass().toString());
 //       }
//	return logger;
return null;
    }
}

class Logger {
  public void severe(String s) {
  }
}
