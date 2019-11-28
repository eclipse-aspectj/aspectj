package foo;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;

import javax.sql.DataSource;


public aspect DataSourceConnectionAspectCode {
  declare parents: BaseClass implements DataSource;

  before() : execution(public java.sql.Connection javax.sql.DataSource+.getConnection(..)) {
    System.out.println(thisJoinPoint);
  }
}
