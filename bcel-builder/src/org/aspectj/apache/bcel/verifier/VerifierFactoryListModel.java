package org.aspectj.apache.bcel.verifier;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import javax.swing.event.ListDataEvent;
/**
 * This class implements an adapter; it implements both a Swing ListModel and
 * a VerifierFactoryObserver.
 *
 * @version $Id: VerifierFactoryListModel.java,v 1.4 2004/11/22 08:31:27 aclement Exp $
 * @author Enver Haase
 */
public class VerifierFactoryListModel implements org.aspectj.apache.bcel.verifier.VerifierFactoryObserver, javax.swing.ListModel{

  private java.util.ArrayList listeners = new java.util.ArrayList();

  private java.util.TreeSet cache = new java.util.TreeSet();

  public VerifierFactoryListModel() {
    VerifierFactory.attach(this);
    update(null); // fill cache.
  }

  public synchronized void update(String s){
    int size = listeners.size();

    Verifier[] verifiers = VerifierFactory.getVerifiers();
    int num_of_verifiers = verifiers.length;
    cache.clear();
    for (int i=0; i<num_of_verifiers; i++) {
      cache.add(verifiers[i].getClassName());
    }

    for (int i=0; i<size; i++){
      ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, num_of_verifiers-1);
      ((javax.swing.event.ListDataListener) (listeners.get(i))).contentsChanged(e);
    }
  }

  public synchronized void addListDataListener(javax.swing.event.ListDataListener l){
    listeners.add(l);
  }

  public synchronized void removeListDataListener(javax.swing.event.ListDataListener l){
    listeners.remove(l);
  }

  public synchronized int getSize(){
    return cache.size();
  }

  public synchronized Object getElementAt(int index){
    return (cache.toArray())[index];
  }
}
