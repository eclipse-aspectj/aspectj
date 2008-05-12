package concrete;

import java.util.Vector;

public class ConcreteClass extends SuperClass<WetCement> {

	@Override
	public Vector<WetCement> getSomeTs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSomeTs(Vector<WetCement> newTs) {
		// TODO Auto-generated method stub
		someTs.addAll(newTs);
	}

}
