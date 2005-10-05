/*
 * Copyright (c) 1998-2002 Xerox Corporation,
 *               2004 Contributors.  All rights reserved.
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

/**
 * Add bound properties and serialization to Point objects
 */
aspect BoundPoint {
  /*
   * privately declare a field on Point to hold the property
   * change support object.  `this' is a reference to a Point object.
   */
  private PropertyChangeSupport Point.support = new PropertyChangeSupport(this);

  /*
   * Declare property change registration methods on Point,
   * and introduce implementation of the Serializable interface.
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
   * Send property change event after X setter completes normally.
   * Use around advice to keep the old value on the stack.
   */
  void around(Point p): execution(void Point.setX(int)) && target(p) {
      int oldValue = p.getX();
      proceed(p);
      firePropertyChange(p, "x", oldValue, p.getX());
  }

  /**
   * Send property change event after Y setter completes normally.
   * Use around advice to keep the old value on the stack.
   */
  void around(Point p): execution(void Point.setY(int)) && target(p) {
      int oldValue = p.getY();
      proceed(p);
      firePropertyChange(p, "y", oldValue, p.getY());
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
