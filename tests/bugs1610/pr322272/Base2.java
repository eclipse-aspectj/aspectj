import java.lang.annotation.*;

public abstract aspect Base2<B> {
  declare @type: F*: @Anno;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
}
