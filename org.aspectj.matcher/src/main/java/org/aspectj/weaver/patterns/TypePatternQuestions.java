/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package org.aspectj.weaver.patterns;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ResolvedType;


public class TypePatternQuestions {
	private Map<Question,FuzzyBoolean> questionsAndAnswers = new HashMap<>();

	public FuzzyBoolean askQuestion(TypePattern pattern, ResolvedType type,
									TypePattern.MatchKind kind)
	{
		Question question = new Question(pattern, type, kind);
		//??? should we use this table to do caching or is that a pessimization
		//??? if we do that optimization we can also do error checking that the result
		//??? doesn't change
		FuzzyBoolean answer = question.ask();
		questionsAndAnswers.put(question, answer);
		return answer;
	}

	public Question anyChanges() {
		for (Map.Entry<Question, FuzzyBoolean> entry : questionsAndAnswers.entrySet()) {
			Question question = entry.getKey();
			FuzzyBoolean expectedAnswer = entry.getValue();

			FuzzyBoolean currentAnswer = question.ask();
			//System.out.println(question + ":" + currentAnswer);
			if (currentAnswer != expectedAnswer) {
				return question;
			}
		}

		return null;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("TypePatternQuestions{");
		for (Map.Entry<Question,FuzzyBoolean> entry: questionsAndAnswers.entrySet()) {
			Question question = entry.getKey();
			FuzzyBoolean expectedAnswer = entry.getValue();
			buf.append(question);
			buf.append(":");
			buf.append(expectedAnswer);
			buf.append(", ");
		}
		buf.append("}");
		return buf.toString();
	}


	public class Question {
		TypePattern pattern;
		ResolvedType type;
		TypePattern.MatchKind kind;

		public Question(TypePattern pattern, ResolvedType type,
									TypePattern.MatchKind kind) {
			super();
			this.pattern = pattern;
			this.type = type;
			this.kind = kind;
		}

		public FuzzyBoolean ask() {
			return pattern.matches(type, kind);
		}

		public boolean equals(Object other) {
			if (!(other instanceof Question)) return false;
			Question o = (Question)other;
			return o.pattern.equals(pattern) && o.type.equals(type) && o.kind == kind;
		}

		public int hashCode() {
	        int result = 17;
	        result = 37*result + kind.hashCode();
	        result = 37*result + pattern.hashCode();
	        result = 37*result + type.hashCode();
	        return result;
	    }

		public String toString() {
			return "?(" + pattern + ", " + type + ", " + kind + ")";
		}
	}
}
