package org.aspectj.weaver;

import org.aspectj.bridge.ISourceLocation;

public class Utility {

	/*
	 * Ensure we report a nice source location - particular in the case where the source info is missing (binary weave).
	 */
	public static String beautifyLocation(ISourceLocation isl) {
		StringBuilder nice = new StringBuilder();
		if (isl == null || isl.getSourceFile() == null || isl.getSourceFile().getName().contains("no debug info available")) {
			nice.append("no debug info available");
		} else {
			// can't use File.getName() as this fails when a Linux box
			// encounters a path created on Windows and vice-versa
			int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
			if (takeFrom == -1) {
				takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
			}
			nice.append(isl.getSourceFile().getPath().substring(takeFrom + 1));
			if (isl.getLine() != 0) {
				nice.append(":").append(isl.getLine());
			}
		}
		return nice.toString();
	}

	/**
	 * Checks for suppression specified on the member or on the declaring type of that member
	 */
	public static boolean isSuppressing(Member member, String lintkey) {
		boolean isSuppressing = Utils.isSuppressing(member.getAnnotations(), lintkey);
		if (isSuppressing) {
			return true;
		}
		UnresolvedType type = member.getDeclaringType();
		if (type instanceof ResolvedType) {
			return Utils.isSuppressing(((ResolvedType) type).getAnnotations(), lintkey);
		}
		return false;
	}
}
