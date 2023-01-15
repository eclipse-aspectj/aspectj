/*******************************************************************************
 * Copyright (c) 2023 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Test helper class reflecting nesting structure for AND ('&&'), OR ('||') and NOT ('!') pointcuts, enabling
 * comparisons disregarding order of AND/OR pointcuts on the same nesting level. For this class, there is no difference
 * between 'A && B && C', 'A && C && B', 'C && B '&& A' etc., i.e. the commutative law is respected.
 *
 * @author Alexander Kriegisch
 */
public class LogicalPointcutStructure {

  private enum PointcutType { NOT, AND, OR, TEXT }

  private final PointcutType type;
  private final List<LogicalPointcutStructure> children = new ArrayList<>();
  private final Set<LogicalPointcutStructure> childrenSet = new HashSet<>();
  private final String text;

  private LogicalPointcutStructure(PointcutType type, LogicalPointcutStructure... children) {
    this(type, null, children);
  }

  private LogicalPointcutStructure(PointcutType type, String text) {
    this(type, text, (LogicalPointcutStructure[]) null);
  }

  private LogicalPointcutStructure(PointcutType type, String text, LogicalPointcutStructure... children) {
    if (type == null)
      throw new IllegalArgumentException("pointcutType must be != null");
    if (text == null && children == null)
      throw new IllegalArgumentException("either text or children must be != null");
    if (text != null && children != null)
      throw new IllegalArgumentException("cannot have both text and children, one must be null");
    if (text != null && type != PointcutType.TEXT)
      throw new IllegalArgumentException("if text is given, type must match to be TEXT");
    if (children != null && type == PointcutType.TEXT)
      throw new IllegalArgumentException("if children are given, type must be != TEXT");

    this.type = type;
    this.text = text;
    if (children != null) {
      this.children.addAll(Arrays.asList(children));
      this.childrenSet.addAll(this.children);
    }
  }

  public PointcutType getType() {
    return type;
  }

  public List<LogicalPointcutStructure> getChildren() {
    return children;
  }

  public String getText() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    LogicalPointcutStructure that = (LogicalPointcutStructure) o;

    if (type != that.type)
      return false;
    if (!childrenSet.equals(that.childrenSet))
      return false;
    return Objects.equals(text, that.text);
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + childrenSet.hashCode();
    result = 31 * result + (text != null ? text.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return type == PointcutType.TEXT
      ? "\"" + text + "\""
      : type + "(" + children.toString().replaceFirst("^.(.*).$", "$1") + ")";
  }

  public static LogicalPointcutStructure NOT(LogicalPointcutStructure child) {
    return new LogicalPointcutStructure(PointcutType.NOT, child);
  }

  public static LogicalPointcutStructure NOT(Object childObject) {
    LogicalPointcutStructure child;
    if (childObject instanceof LogicalPointcutStructure)
      child = (LogicalPointcutStructure) childObject;
    else if (childObject instanceof String)
      child = TEXT((String) childObject);
    else
      throw new IllegalArgumentException("each child must be either LogicalPointcutStructure or String");
    return new LogicalPointcutStructure(PointcutType.NOT, child);
  }

  public static LogicalPointcutStructure AND(LogicalPointcutStructure... children) {
    return new LogicalPointcutStructure(PointcutType.AND, children);
  }

  public static LogicalPointcutStructure AND(Object... childObjects) {
    LogicalPointcutStructure[] children = new LogicalPointcutStructure[childObjects.length];
    LogicalPointcutStructure child;
    Object childObject;
    for (int i = 0; i < childObjects.length; i++) {
      childObject = childObjects[i];
      if (childObject instanceof LogicalPointcutStructure)
        child = (LogicalPointcutStructure) childObject;
      else if (childObject instanceof String)
        child = TEXT((String) childObject);
      else
        throw new IllegalArgumentException("each child must be either LogicalPointcutStructure or String");
      children[i] = child;
    }
    return new LogicalPointcutStructure(PointcutType.AND, children);
  }

  public static LogicalPointcutStructure OR(LogicalPointcutStructure... children) {
    return new LogicalPointcutStructure(PointcutType.OR, children);
  }

  public static LogicalPointcutStructure OR(Object... childObjects) {
    LogicalPointcutStructure[] children = new LogicalPointcutStructure[childObjects.length];
    LogicalPointcutStructure child;
    Object childObject;
    for (int i = 0; i < childObjects.length; i++) {
      childObject = childObjects[i];
      if (childObject instanceof LogicalPointcutStructure)
        child = (LogicalPointcutStructure) childObject;
      else if (childObject instanceof String)
        child = TEXT((String) childObject);
      else
        throw new IllegalArgumentException("each child must be either LogicalPointcutStructure or String");
      children[i] = child;
    }
    return new LogicalPointcutStructure(PointcutType.OR, children);
  }

  public static LogicalPointcutStructure TEXT(String text) {
    return new LogicalPointcutStructure(PointcutType.TEXT, text);
  }

  public static LogicalPointcutStructure fromPointcut(Pointcut pointcut) {
    if (pointcut instanceof NotPointcut) {
      NotPointcut notPointcut = (NotPointcut) pointcut;
      return NOT(fromPointcut(notPointcut.getNegatedPointcut()));
    }
    else if (pointcut instanceof AndPointcut) {
      List<LogicalPointcutStructure> children = new ArrayList<>();
      AndPointcut andPointcut = (AndPointcut) pointcut;
      children.add(fromPointcut(andPointcut.getRight()));
      while (andPointcut.getLeft() instanceof AndPointcut) {
        andPointcut = (AndPointcut) andPointcut.getLeft();
        children.add(fromPointcut(andPointcut.getRight()));
      }
      children.add(fromPointcut(andPointcut.getLeft()));
      return AND(children.toArray(new LogicalPointcutStructure[0]));
    }
    else if (pointcut instanceof OrPointcut) {
      List<LogicalPointcutStructure> children = new ArrayList<>();
      OrPointcut orPointcut = (OrPointcut) pointcut;
      children.add(fromPointcut(orPointcut.getRight()));
      while (orPointcut.getLeft() instanceof OrPointcut) {
        orPointcut = (OrPointcut) orPointcut.getLeft();
        children.add(fromPointcut(orPointcut.getRight()));
      }
      children.add(fromPointcut(orPointcut.getLeft()));
      return OR(children.toArray(new LogicalPointcutStructure[0]));
    }
    else {
      return TEXT(pointcut.toString());
    }
  }

  public static void main(String[] args) {
    // true
    System.out.println(verifyToString(
      OR("A", NOT(OR("B", "C"))),
      "OR(\"A\", NOT(OR(\"B\", \"C\")))"
    ));
    // true
    System.out.println(verifyToString(
      OR(AND("A", NOT(OR("B", "C")), "D"), NOT(OR("E", "F"))),
      "OR(AND(\"A\", NOT(OR(\"B\", \"C\")), \"D\"), NOT(OR(\"E\", \"F\")))"
    ));

    // true
    System.out.println(verifyEquals(
      OR("A", NOT(OR("B", "C"))),
      OR(NOT(OR("C", "B")), "A")
    ));
    // true
    System.out.println(verifyEquals(
      OR(AND("A", NOT(OR("B", "C")), "D"), NOT(OR("E", "F"))),
      OR(NOT(OR("F", "E")), AND("A", NOT(OR("C", "B")), "D"))
    ));
    // false
    System.out.println(verifyEquals(
      OR(AND("A", NOT(OR("B", "C")), "D"), NOT(OR("E", "F"))),
      OR(NOT(OR("F", "E")), AND(NOT(OR("C", "B", "D")), "A"))
    ));
  }

  private static boolean verifyToString(LogicalPointcutStructure structure, String toStringExpected) {
    System.out.println();
    System.out.println("Expected: " + toStringExpected);
    System.out.println("Actual:   " + structure);
    return toStringExpected.equals(structure.toString());
  }

  private static boolean verifyEquals(LogicalPointcutStructure structure1, LogicalPointcutStructure structure2) {
    System.out.println();
    System.out.println("Structure 1: " + structure1);
    System.out.println("Structure 2: " + structure2);
    return structure1.equals(structure2);
  }

}
