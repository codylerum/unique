package com.outjected.unique;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.outjected.unique.UniqueLong;

public class GenerationTest {

	@Test
	public void test() {
		UniqueLong gen = new UniqueLong(1417392000000L, 0, 0);
		Assert.assertNotEquals(gen.generate(), gen.generate());
	}

	@Test
	public void generateAMillion() {
		final int ITERATIONS = 1_000_000;
		UniqueLong gen = new UniqueLong(1417392000000L, 0, 0);
		HashSet<Long> results = new HashSet<>(ITERATIONS, 100);
		for (int i = 0; i < ITERATIONS; i++) {
			results.add(gen.generate());
		}

		// Since a Set can't have duplicates there must be exactly 1,000,000
		Assert.assertEquals(ITERATIONS, results.size());
	}
}
