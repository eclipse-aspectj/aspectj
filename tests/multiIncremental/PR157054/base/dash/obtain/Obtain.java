/*
* Copyright (C) 2005  John D. Heintz
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public License
* as published by the Free Software Foundation; either version 2.1
* of the License.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Library General Public License for more details.
*
* John D. Heintz can be reached at: jheintz@pobox.com 
*/
package dash.obtain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The @Obtain annotation provides a marker for fields that will be "obtain"-ed at 
 * runtime based on lookups to the ProviderService service. See the ObtainLookup class 
 * for documentation of the details about the annotated field that is provided to the 
 * Provider service.  
 * 
 * <p>An AspectJ Aspect is used to control the interception of field accesses and 
 * provide the binding behavior into the ProviderService service. The AspectJ pointcut
 * used is:
 * <pre>pointcut obtain_get(): get(@Obtain * *);</pre>
 * 
 * <p>If the ProviderService service fails to correctly Obtain a target object 
 * a NullPointerException will be thrown into the code accessing the field.
 * 
 * <p>A single field will be obtained once per instance.
 * 
 * @see dash.obtain.provider.ProviderService
 * @see dash.obtain.provider.ObtainLookup
 * @throws java.lang.NullPointerException
 * @author jheintz
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Obtain {
	public String value() default "";
}
