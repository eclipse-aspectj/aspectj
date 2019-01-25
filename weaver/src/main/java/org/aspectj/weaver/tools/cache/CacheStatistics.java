/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         initial implementation
 *******************************************************************************/

package org.aspectj.weaver.tools.cache;

/**
 * Maintains some basic statistics on the class cache.
 */
public class CacheStatistics {
	private volatile int hits;
	private volatile int misses;
	private volatile int weaved;
	private volatile int generated;
	private volatile int ignored;
	private volatile int puts;
	private volatile int puts_ignored;

	public void hit() {
		hits++;
	}

	public void miss() {
		misses++;
	}

	public void weaved() {
		weaved++;
	}

	public void generated() {
		generated++;
	}

	public void ignored() {
		ignored++;
	}

	public void put() {
		puts++;
	}

	public void putIgnored() {
		puts_ignored++;
	}


	public int getHits() {
		return hits;
	}

	public int getMisses() {
		return misses;
	}

	public int getWeaved() {
		return weaved;
	}

	public int getGenerated() {
		return generated;
	}

	public int getIgnored() {
		return ignored;
	}

	public int getPuts() {
		return puts;
	}

	public int getPutsIgnored() {
		return puts_ignored;
	}


	public void reset() {
		hits = 0;
		misses = 0;
		weaved = 0;
		generated = 0;
		ignored = 0;
		puts = 0;
		puts_ignored = 0;
	}

	@Override
	public String toString() {
		return "CacheStatistics{" +
				"hits=" + hits +
				", misses=" + misses +
				", weaved=" + weaved +
				", generated=" + generated +
				", ignored=" + ignored +
				", puts=" + puts +
				", puts_ignored=" + puts_ignored +
				'}';
	}
}
