/*******************************************************************************
 * Copyright (c) 2009 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
import java.io.Serializable;

public interface GenericService<T extends Serializable> {
	Object get1(T t);

	Object get2(Serializable s);
}
