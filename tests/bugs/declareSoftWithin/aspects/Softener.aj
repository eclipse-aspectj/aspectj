package aspects;

import test.NoSoftener;

/**
 * @author Ron Bodkin
 * @author Jim Hugunin
 */
public aspect Softener extends SoftLib {
    public pointcut scope() : within(NoSoftener);
}

abstract aspect SoftLib {
    abstract pointcut scope();

    public pointcut callsThrowingChecked() : call(* *(..)) && scope();
    declare soft: NoSuchMethodException: callsThrowingChecked();
}