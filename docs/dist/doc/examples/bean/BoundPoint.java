/*
 * Copyright (c) 1998-2002 Xerox Corporation.  All rights reserved.
 *
 * Use and copying of this software and preparation of derivative works based
 * upon this software are permitted.  Any distribution of this software or
 * derivative works must comply with all applicable United States export
 * control laws.
 *
 * This software is made available AS IS, and Xerox Corporation makes no
 * warranty about the software, its performance or its conformity to any
 * specification.
 */

package bean;

import java.beans.*;
import java.io.Serializable;

/*
 * Add bound properties and serialization to point objects
 */

aspect BoundPoint {
  /*
   * privately introduce a field into Point to hold the property
   * change support object.  `this' is a reference to a Point object.
   */
  private PropertyChangeSupport Point.support = new PropertyChangeSupport(this);

  /*
   * Introduce the property change registration methods into Point.
   * also introduce implementation of the Serializable interface.
   */
  public void Point.addPropertyChangeListener(PropertyChangeListener listener){
    support.addPropertyChangeListener(listener);
  }

  public void Point.addPropertyChangeListener(String propertyName,
                                              PropertyChangeListener listener){

    support.addPropertyChangeListener(propertyName, listener);
  }

  public void Point.removePropertyChangeListener(String propertyName,
                                                 PropertyChangeListener listener) {
    support.removePropertyChangeListener(propertyName, listener);
  }

  public void Point.removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  public void Point.hasListeners(String propertyName) {
    support.hasListeners(propertyName);
  }

  declare parents: Point implements Serializable;

  /**
   * Pointcut describing the set<property> methods on Point.
   * (uses a wildcard in the method name)
   */
  pointcut setter(Point p): call(void Point.set*(*)) && target(p);

  /**
   * Advice to get the property change event fired when the
   * setters are called. It's around advice because you need
   * the old value of the property.
   */
  void around(Point p): setter(p) {
        String propertyName =
      thisJoinPointStaticPart.getSignature().getName().substring("set".length());
        int oldX = p.getX();
        int oldY = p.getY();
        proceed(p);
        if (propertyName.equals("X")){
      firePropertyChange(p, propertyName, oldX, p.getX());
        } else {
      firePropertyChange(p, propertyName, oldY, p.getY());
        }
  }

  /*
   * Utility to fire the property change event.
   */
  void firePropertyChange(Point p,
                          String property,
                          double oldval,
                          double newval) {
        p.support.firePropertyChange(property,
                                 new Double(oldval),
                                 new Double(newval));
  }
}
