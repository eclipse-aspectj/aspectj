package foo;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

@Aspect
public class DataSourceConnectionAspectAnno {
  // This statement crashes the AspectJ compiler
  @DeclareParents("foo.BaseClass")
  private DataSource dataSource;
  
  @Before("execution(public java.sql.Connection javax.sql.DataSource+.getConnection(..))")
  public void myAdvice(JoinPoint thisJoinPoint) {
    System.out.println(thisJoinPoint);
  }
}
