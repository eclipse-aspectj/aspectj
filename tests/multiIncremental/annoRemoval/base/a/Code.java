package a;
import java.lang.annotation.*;

aspect Remover{
  declare @field: int Code.i: -@Anno;
  declare @field: int Code.j: @Anno;
}

public class Code {

  @Anno
  public int i;

  public int j;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {}
