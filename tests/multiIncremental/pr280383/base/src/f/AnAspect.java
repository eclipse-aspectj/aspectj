package f;

public aspect AnAspect {
        f.AClass.new() {
                this();
        }
}  