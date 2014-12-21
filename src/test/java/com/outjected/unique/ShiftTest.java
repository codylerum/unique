package com.outjected.unique;

import org.junit.Assert;
import org.junit.Test;

import com.outjected.unique.UniqueLong;

public class ShiftTest {

	@Test
	public void createWorkerPart() {
		Assert.assertEquals(0L, UniqueLong.createWorkerPart(0, 0));
		Assert.assertEquals(4096L, UniqueLong.createWorkerPart(0, 1));
		Assert.assertEquals(262144L, UniqueLong.createWorkerPart(1, 0));
		Assert.assertEquals(266240L, UniqueLong.createWorkerPart(1, 1));
		Assert.assertEquals(4190208L, UniqueLong.createWorkerPart(15, 63));
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDataCenter() {
		UniqueLong.createWorkerPart(-1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeServer() {
		UniqueLong.createWorkerPart(0, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooLargeDataCenter() {
		UniqueLong.createWorkerPart(16, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void tooLargeServer() {
		UniqueLong.createWorkerPart(0, 64);
	}
}
