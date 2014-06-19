package test.extender;
import test.*;

public aspect Extender2 {

	declare parents: InterfaceProj1 extends java.io.Serializable;

//	declare parents: test.ClassProj1 extends ClassProj2;

}
