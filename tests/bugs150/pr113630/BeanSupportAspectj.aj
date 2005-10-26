/**
 * 
 */
package com.blueprint.util.aspectj5.test;

import com.blueprint.util.aspectj5.test.PropertySupportAspect5.PropertySupport;

public aspect BeanSupportAspectj {
     declare parents: @javaBean * implements PropertySupport;
}
