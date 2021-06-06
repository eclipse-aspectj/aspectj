/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
@Aspect
public class DeclareParentsImplementsReweavableTestAspect {

    @DeclareParents(
            value="ataspectj.DeclareParentsImplementsReweavableTest.Target",
            defaultImpl = DeclareParentsImplementsReweavableTest.Imp2.class
    )
    public static DeclareParentsImplementsReweavableTest.I2 i2;

}
