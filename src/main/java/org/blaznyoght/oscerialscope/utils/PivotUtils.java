package org.blaznyoght.oscerialscope.utils;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;

public class PivotUtils {

	public static <T> List<T> translateJavaList2PivotList(java.util.List<T> javaList) {
		List<T> pivotList = new ArrayList<T>();
		for(T item : javaList) {
			pivotList.add(item);
		}
		return pivotList;		
	}
}
