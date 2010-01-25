/**
 * Copyright (c) 2009  Collaborative Development Group, C.S. Dept., University of Bari
 *
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0  which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package test;

import it.uniba.di.cdg.penelope.ui.annotation.View;
import it.uniba.di.cdg.penelope.ui.mvp.IManagedView;
import it.uniba.di.cdg.penelope.ui.mvp.AbstractViewEnhancerAspect;

/**
 * 
 */
public class ViewEnhancerIntegrationTest {

@View
	public static class MockView {
		
	}
	
	static aspect ViewEnhancerAspect extends AbstractViewEnhancerAspect {
		pointcut scope() : within( test.ViewEnhancerIntegrationTest );
	}
	
	public void simulateViewCreation() {
	}
	
	public void shouldAugmentView() {
		//given @View class has been augmented

		//when
		MockView view = new MockView(); 
		
		//then
	//	assertTrue( view instanceof IManagedView );
	}
}
