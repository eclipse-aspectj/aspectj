package b;

import a.Outer;

/** @testcase PR#626 declared parent not defined in scope of target class declaration (CE in -usejavac only) */
public interface Foo {
    public static aspect Specific extends Outer {
        declare parents: Foo extends Inner;
    }
}

