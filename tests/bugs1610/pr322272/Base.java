import java.lang.annotation.*;

public abstract aspect Base {
  declare @type: F*: @Anno;
}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
}
