/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
import java.util.List;

public class StreamToList {
	// Stream.toList() was added in Java 16
	public static List<Integer> toList() {
		return List.of(1).stream().toList();
	}
}
