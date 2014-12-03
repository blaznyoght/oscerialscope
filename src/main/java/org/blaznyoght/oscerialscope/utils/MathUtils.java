package org.blaznyoght.oscerialscope.utils;

public class MathUtils {

	public static Number map(Number from, Number fromMin, Number fromMax, Number toMin, Number toMax) {
		return (from.doubleValue() - fromMin.doubleValue()) * (toMax.doubleValue() - toMin.doubleValue()) / (fromMax.doubleValue() - fromMin.doubleValue()) + toMin.doubleValue();
	}
}
