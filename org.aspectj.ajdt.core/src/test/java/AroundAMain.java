/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

import org.aspectj.runtime.internal.AroundClosure;
import org.aspectj.util.Reflection;

public class AroundAMain {//extends TestCase {

	public AroundAMain(String name) {
		//		super(name);
	}


	public static void main(String[] args) throws ClassNotFoundException {
		AroundClosure closure = new AroundClosure() {
			@Override
			public Object run(Object[] args) throws Throwable {
				//				System.out.println("run with: " + Arrays.asList(args));
				return 10;
			}
		};

		Object instance = Reflection.getStaticField(Class.forName("AroundA"),
				"ajc$perSingletonInstance");

		Reflection.invoke(Class.forName("AroundA"), instance, "ajc$around$AroundA$1$73ebb943", // was $AroundA$46
				10, true, closure);

		Reflection.invoke(Class.forName("AroundA"), instance, "ajc$around$AroundA$2$a758212d",  // Was $AroundA$c5
				"hello there", closure);
		Reflection.invoke(Class.forName("AroundA"), instance, "ajc$around$AroundA$3$a758212d",  // Was $AroundA$150
				new String[1], closure);

	}
}
