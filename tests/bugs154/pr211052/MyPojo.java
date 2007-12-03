

aspect ConfigureTracing {
        declare @type : MyPojo : @Tracing(level = LoggingLevel.WARN);
//        declare @method : * MyPojo.calculate() : @TestAnnotation;
}

@interface Tracing { LoggingLevel level(); }

@interface TestAnnotation {}

class Level {
  Level(int i) {}
  public final static Level INFO = new Level(1);
  public final static Level WARN = new Level(2);
  public final static Level ERROR = new Level(3);
}
//@Tracing(level = LoggingLevel.WARN)
enum LoggingLevel {
  INFO(Level.INFO),
  WARN(Level.WARN),
  ERROR(Level.ERROR);
      
  private final Level level;
  private LoggingLevel(Level level) {this.level = level;}
  public Level getLevel() {return level;}
}

public class MyPojo {
  public static void calculate() {
  }
  public static void main(String []argv) {
	  
  }
}
