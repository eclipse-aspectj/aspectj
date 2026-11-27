/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.apache.bcel.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * SoftHashMap holds its values through soft references that remember their key. When the GC reclaims a value it clears the
 * reference and puts it on a queue, and the map removes the matching entry the next time it processes that queue, which is on put,
 * remove, clear and size. get() also drops an entry whose value is gone.
 *
 * The tests use the map's own backing map, so they can see exactly what the map has removed, and they simulate the GC.
 */
public class SoftHashMapTest extends TestCase {

	private final Map<String, SoftHashMap.SoftReferenceWithKey<String, String>> backing = new HashMap<>();
	private final SoftHashMap<String, String> map = new SoftHashMap<>(backing);

	/** What the GC does when it reclaims a value: the reference is cleared and put on the reference queue. */
	private void reclaim(String key) {
		SoftHashMap.SoftReferenceWithKey<String, String> reference = backing.get(key);
		reference.clear();
		reference.enqueue();
	}

	/** The GC has reclaimed the value but the reference has not been queued yet, a state the map has to cope with in get(). */
	private void reclaimWithoutQueueing(String key) {
		backing.get(key).clear();
	}

	private void putAll(String... keys) {
		for (String key : keys) {
			map.put(key, "value of " + key);
		}
	}

	private static Set<String> setOf(String... keys) {
		Set<String> result = new HashSet<>();
		for (String key : keys) {
			result.add(key);
		}
		return result;
	}

	// ---- construction

	public void testANewMapIsEmpty() {
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
		assertNull(map.get("anything"));
		assertEquals(0, new SoftHashMap<String, String>().size());
	}

	public void testTheMapUsesTheBackingMapItIsGiven() {
		assertTrue(backing.isEmpty());
		putAll("a");
		assertEquals(setOf("a"), backing.keySet());
	}

	// ---- put and get

	public void testGetReturnsWhatWasPut() {
		map.put("a", "one");
		assertEquals("one", map.get("a"));
	}

	public void testGetOfAnAbsentKeyReturnsNull() {
		putAll("a");
		assertNull(map.get("b"));
	}

	public void testPutOfANewKeyReturnsNull() {
		assertNull(map.put("a", "one"));
	}

	public void testPutOfAnExistingKeyReturnsThePreviousValueAndReplacesIt() {
		map.put("a", "one");
		assertEquals("one", map.put("a", "two"));
		assertEquals("two", map.get("a"));
		assertEquals(1, map.size());
	}

	public void testPutOverAnEntryWhoseValueWasReclaimedReturnsNull() {
		map.put("a", "one");
		reclaimWithoutQueueing("a");
		assertNull(map.put("a", "two"));
		assertEquals("two", map.get("a"));
	}

	public void testGetOfAnEntryWhoseValueWasReclaimedReturnsNullAndDropsTheEntry() {
		putAll("a", "b");
		reclaim("a");
		assertNull(map.get("a"));
		assertFalse(backing.containsKey("a"));
		assertEquals("value of b", map.get("b"));
	}

	public void testGetOfAnEntryReclaimedButNotYetQueuedReturnsNullAndDropsTheEntry() {
		putAll("a");
		reclaimWithoutQueueing("a");
		assertNull(map.get("a"));
		assertFalse(backing.containsKey("a"));
	}

	// ---- the entries of reclaimed values are removed when the map processes its queue

	public void testPutRemovesTheEntriesOfReclaimedValues() {
		putAll("a", "b", "c");
		reclaim("a");
		reclaim("b");
		map.put("d", "value of d");
		assertEquals(setOf("c", "d"), backing.keySet());
	}

	public void testRemoveRemovesTheEntriesOfReclaimedValues() {
		putAll("a", "b", "c");
		reclaim("a");
		map.remove("c");
		assertEquals(setOf("b"), backing.keySet());
	}

	public void testSizeRemovesTheEntriesOfReclaimedValuesAndCountsTheRest() {
		putAll("a", "b", "c");
		reclaim("a");
		reclaim("c");
		assertEquals(1, map.size());
		assertEquals(setOf("b"), backing.keySet());
	}

	public void testClearRemovesEverything() {
		putAll("a", "b", "c");
		reclaim("a");
		map.clear();
		assertEquals(0, map.size());
		assertTrue(backing.isEmpty());
		assertNull(map.get("b"));
	}

	public void testTheMapCanBeUsedAgainAfterClear() {
		putAll("a");
		map.clear();
		putAll("a");
		assertEquals("value of a", map.get("a"));
		assertEquals(1, map.size());
	}

	public void testOnlyReclaimedValuesAreRemoved() {
		putAll("a", "b", "c");
		reclaim("b");
		map.size();
		assertEquals("value of a", map.get("a"));
		assertEquals("value of c", map.get("c"));
	}

	// ---- remove

	public void testRemoveReturnsTheValueAndRemovesTheEntry() {
		putAll("a", "b");
		assertEquals("value of a", map.remove("a"));
		assertNull(map.get("a"));
		assertEquals(1, map.size());
	}

	public void testRemoveOfAnAbsentKeyReturnsNull() {
		putAll("a");
		assertNull(map.remove("b"));
		assertEquals(1, map.size());
	}

	public void testRemoveOfAnEntryWhoseValueWasReclaimedReturnsNull() {
		putAll("a");
		reclaimWithoutQueueing("a");
		assertNull(map.remove("a"));
		assertFalse(backing.containsKey("a"));
	}

	// ---- the views a Map provides: they show the live entries, with the real values, as a snapshot

	public void testEntrySetHoldsTheKeysAndTheRealValues() {
		putAll("a", "b");
		Set<Map.Entry<String, String>> expected = new HashSet<>();
		expected.add(new SimpleEntry<>("a", "value of a"));
		expected.add(new SimpleEntry<>("b", "value of b"));
		assertEquals(expected, map.entrySet());
	}

	public void testEntrySetOmitsEntriesWhoseValueWasReclaimed() {
		putAll("a", "b", "c");
		reclaim("a");
		reclaimWithoutQueueing("b");
		Set<Map.Entry<String, String>> expected = new HashSet<>();
		expected.add(new SimpleEntry<>("c", "value of c"));
		assertEquals(expected, map.entrySet());
	}

	public void testEntrySetIsASnapshotSoChangingItDoesNotChangeTheMap() {
		putAll("a", "b");
		Set<Map.Entry<String, String>> snapshot = map.entrySet();
		snapshot.clear();
		assertEquals(2, map.size());
		assertEquals("value of a", map.get("a"));
	}

	public void testKeySetAndValues() {
		putAll("a", "b");
		assertEquals(setOf("a", "b"), map.keySet());
		assertEquals(setOf("value of a", "value of b"), new HashSet<>(map.values()));
	}

	public void testContainsKeyAndContainsValueIgnoreReclaimedValues() {
		putAll("a", "b");
		reclaim("a");
		assertFalse(map.containsKey("a"));
		assertFalse(map.containsValue("value of a"));
		assertTrue(map.containsKey("b"));
		assertTrue(map.containsValue("value of b"));
	}

	public void testEqualsAndHashCodeAreThoseOfAMapWithTheSameEntries() {
		putAll("a", "b");
		Map<String, String> same = new HashMap<>();
		same.put("a", "value of a");
		same.put("b", "value of b");
		assertEquals(same, map);
		assertEquals(map, same);
		assertEquals(same.hashCode(), map.hashCode());
		same.put("c", "value of c");
		assertFalse(same.equals(map));
	}

	public void testToStringShowsTheEntries() {
		map.put("a", "one");
		assertEquals("{a=one}", map.toString());
	}

	// ---- discardValueReferences simulates the GC for tests of code that uses a SoftHashMap

	public void testDiscardValueReferencesReclaimsExactlyTheRequestedNumberOfValues() {
		putAll("a", "b", "c", "d", "e");
		map.discardValueReferences(2);
		assertEquals(3, map.size());
	}

	public void testDiscardValueReferencesOfZeroReclaimsNothing() {
		putAll("a", "b");
		map.discardValueReferences(0);
		assertEquals(2, map.size());
	}

	public void testDiscardValueReferencesOfMoreThanThereAreReclaimsAll() {
		putAll("a", "b", "c");
		map.discardValueReferences(10);
		assertEquals(0, map.size());
	}

	public void testAValueDiscardedByDiscardValueReferencesIsGone() {
		putAll("a");
		map.discardValueReferences(1);
		assertNull(map.get("a"));
	}

	// ---- counting the misses that were caused by the GC

	public void testGetOfAnEntryWhoseValueWasReclaimedIsAnEvictedMiss() {
		putAll("a");
		reclaim("a");
		assertNull(map.get("a"));
		assertEquals(1, map.evictedMisses());
	}

	public void testGetOfAnEntryReclaimedButNotYetQueuedIsAnEvictedMiss() {
		putAll("a");
		reclaimWithoutQueueing("a");
		assertNull(map.get("a"));
		assertEquals(1, map.evictedMisses());
	}

	public void testAnEntryRemovedByTheMapBeforeItIsAskedForIsNotAnEvictedMiss() {
		putAll("a");
		reclaim("a");
		assertEquals(0, map.size()); // size() processes the queue and removes the entry
		assertNull(map.get("a"));
		assertEquals(0, map.evictedMisses());
	}

	public void testAnAbsentKeyIsNotAnEvictedMiss() {
		assertNull(map.get("absent"));
		assertEquals(0, map.evictedMisses());
	}

	public void testEachEvictedMissIsCountedAndResetEvictedMissesStartsAgain() {
		putAll("a", "b");
		reclaim("a");
		reclaim("b");
		map.get("a");
		map.get("b");
		assertEquals(2, map.evictedMisses());
		map.resetEvictedMisses();
		assertEquals(0, map.evictedMisses());
	}
}
