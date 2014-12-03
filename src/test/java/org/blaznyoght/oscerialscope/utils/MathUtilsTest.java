package org.blaznyoght.oscerialscope.utils;

import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

	@Test
	public void testMap() {
		Assert.assertEquals(40/2.0, MathUtils.map(40, 0, 120, 0, 60));
		Assert.assertEquals(40.0, MathUtils.map(0, -40, 40, 0, 80));
		Assert.assertEquals(0.0, MathUtils.map(0, -40, 40, 40, -40));
		Assert.assertEquals(20.0, MathUtils.map(-20, -40, 40, 40, -40));
	}

}
