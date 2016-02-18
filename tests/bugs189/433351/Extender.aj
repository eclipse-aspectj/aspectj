package test.extender;
import test.*;

public aspect Extender {

	declare parents: InterfaceProj1 extends InterfaceProj2;

//	declare parents: test.ClassProj1 extends ClassProj2;

}
