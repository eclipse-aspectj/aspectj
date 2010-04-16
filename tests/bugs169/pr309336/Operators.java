//package com.msr;

import java.rmi.RemoteException;

public abstract class Operators {
/*
	public interface Operator14<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable, E13 extends Throwable, E14 extends Throwable> {
		T execute(String aArg) throws E1, E2, E3, E4, E5, E6, E7, E8, E9, E10,
				E11, E12, E13, E14, RemoteException;
	}

	public interface Operator13<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable, E13 extends Throwable>
			extends
			Operator14<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E13> {
		 T execute(String aArg) throws E1, E2, E3, E4, E5, E6, E7, E8, E9,
		 E10, E11, E12, RemoteException;
	}

	public interface Operator12<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable, E12 extends Throwable>
			extends
			Operator13<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E12> {
		// T execute(String aMindServer, String aSessionId, String
		// aMindServerIdentifier ) throws E1, E2, E3, E4, E5, E6, E7, E8, E9,
		// E10, E11, E12, RemoteException;
	}

*/
	public interface Operator11<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable, E11 extends Throwable>
{/*
			extends
			Operator12<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E11> {
*/
          T execute(String aArg) throws E1,E2,E3,E4,E5,E7,E8,E10,E11, RemoteException;
	}

	public interface Operator10<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable, E10 extends Throwable>
			extends Operator11<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E10> {

	}

	public interface Operator9<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable>
			extends Operator10<T, E1, E2, E3, E4, E5, E6, E7, E8, E9, E9> {
	}

	public interface Operator8<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable>
			extends Operator9<T, E1, E2, E3, E4, E5, E6, E7, E8, E8> {
	}

	public interface Operator7<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable>
			extends Operator8<T, E1, E2, E3, E4, E5, E6, E7, E7> {
	}

	//
	public interface Operator6<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable>
			extends Operator7<T, E1, E2, E3, E4, E5, E6, E6> {

	}

	public interface Operator5<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable>
			extends Operator6<T, E1, E2, E3, E4, E5, E5> {
	}

	public interface Operator4<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable>
			extends Operator5<T, E1, E2, E3, E4, E4> {
	}

	public interface Operator3<T, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable>
			extends Operator4<T, E1, E2, E3, E3> {
	}

	public interface Operator2<T, E1 extends Throwable, E2 extends Throwable>
			extends Operator3<T, E1, E2, E2> {

	}

	public interface Operator1<T, E1 extends Throwable> extends
			Operator2<T, E1, E1> {
	}

	public interface Operator<T> extends Operator1<T, RuntimeException> {
	}
}
