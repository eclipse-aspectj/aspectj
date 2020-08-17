/* *******************************************************************
 * Copyright (c) 2007 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Linton Ye https://bugs.eclipse.org/bugs/show_bug.cgi?id=193065
 * ******************************************************************/

package org.aspectj.weaver;

import java.util.Collection;

/**
 * <p>
 * This interface is introduced to support tools like PointcutDoctor.
 * </p>
 * <p>
 * A CustomMungerFactory is used to create ShadowMungers and/or ConcreteTypeMungers so that an extender can extract extra
 * information during the weaving process.
 * </p>
 * <p>
 * A CustomMungerFactory is assigned to a weaver through its AjCompiler in extenders' code, and gets invoked by the weaver right
 * before the weaving starts. The custom shadow/type mungers being created will be added into the shadow/type munger list in the
 * weaver and participate the weaving process. For example, the match method of each custom shadow munger will be called against
 * each shadow.
 * </p>
 *
 * @author lintonye
 *
 */
public interface CustomMungerFactory {

	/**
	 * @param aspectType
	 * @return a Collection&lt;ShadowMunger&gt; of custom shadow mungers for the given aspect
	 */
	Collection<ShadowMunger> createCustomShadowMungers(ResolvedType aspectType);

	/**
	 * @param aspectType
	 * @return a Collection&lt;ConcreteTypeMunger&gt; of custom type mungers for the given aspect
	 */
	Collection<ConcreteTypeMunger> createCustomTypeMungers(ResolvedType aspectType);

	Collection<ShadowMunger> getAllCreatedCustomShadowMungers();

	Collection<ConcreteTypeMunger> getAllCreatedCustomTypeMungers();
}
