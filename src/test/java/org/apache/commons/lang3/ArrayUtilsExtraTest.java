package org.apache.commons.lang3;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class ArrayUtilsExtraTest {

    @Before
    public void setUp() {
        // nothing to init
    }

    @Test
    public void testToArrayVarargsAndClone() {
        String[] a = ArrayUtils.toArray("a", "b", "c");
        assertArrayEquals(new String[] {"a","b","c"}, a);

        String[] cloned = ArrayUtils.clone(a);
        assertArrayEquals(a, cloned);
        assertNotSame(a, cloned);
    }

    @Test
    public void testNullToEmptyAndIsEmptyGetLength() {
        Object[] nobj = ArrayUtils.nullToEmpty((Object[]) null);
        assertNotNull(nobj);
        assertEquals(0, nobj.length);
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertEquals(0, ArrayUtils.getLength(null));
    }

    @Test
    public void testSubarrayBoundaries() {
        Integer[] src = new Integer[] {1,2,3,4};
        Integer[] s1 = ArrayUtils.subarray(src, -5, 2); // start negative -> clipped
        assertArrayEquals(new Integer[] {1,2}, s1);

        Integer[] s2 = ArrayUtils.subarray(src, 1, 100); // end too large -> clipped
        assertArrayEquals(new Integer[] {2,3,4}, s2);

        Integer[] s3 = ArrayUtils.subarray(src, 2, 2); // empty
        assertArrayEquals(new Integer[] {}, s3);
    }

    @Test
    public void testAddAndAddAtIndex() {
        Integer[] base = new Integer[] {1, 2};
        Integer[] added = ArrayUtils.add(base, 3);
        assertArrayEquals(new Integer[] {1,2,3}, added);

        Integer[] addedAt = ArrayUtils.add(base, 1, 99);
        assertArrayEquals(new Integer[] {1,99,2}, addedAt);
    }

    @Test
    public void testRemoveAndRemoveElement() {
        Integer[] base = new Integer[] {10, 20, 30};
        Integer[] removed = ArrayUtils.remove(base, 1); // removes 20
        assertArrayEquals(new Integer[] {10,30}, removed);

        Integer[] removedElement = ArrayUtils.removeElement(base, (Integer)30);
        assertArrayEquals(new Integer[] {10,20}, removedElement);

        // removing from single-element array -> empty
        Integer[] single = new Integer[] {42};
        Integer[] removedSingle = ArrayUtils.remove(single, 0);
        assertArrayEquals(new Integer[] {}, removedSingle);
    }

    @Test
    public void testPrimitiveToObjectAndBack() {
        int[] ints = new int[] {1,2,3};
        Integer[] boxed = ArrayUtils.toObject(ints);
        assertArrayEquals(new Integer[] {1,2,3}, boxed);

        int[] unboxed = ArrayUtils.toPrimitive(boxed);
        assertArrayEquals(new int[] {1,2,3}, unboxed);
    }

    @Test
    public void testContainsIndexOfLastIndexOf() {
        String[] arr = new String[] {"x","y","z","y"};
        assertTrue(ArrayUtils.contains(arr, "y"));
        assertEquals(1, ArrayUtils.indexOf(arr, "y"));
        assertEquals(3, ArrayUtils.lastIndexOf(arr, "y"));
        assertEquals(-1, ArrayUtils.indexOf(arr, "not-present"));
    }

    @Test
    public void testAddAllAndRemoveAllIndices() {
        Integer[] a = new Integer[] {1,2};
        Integer[] b = new Integer[] {3,4};
        Integer[] joined = ArrayUtils.addAll(a, b);
        assertArrayEquals(new Integer[] {1,2,3,4}, joined);

        Integer[] removed = ArrayUtils.removeAll(joined, 1, 2); // removes 2 and 3 (by indices)
        // After removing indices 1 and 2 from [1,2,3,4] -> [1,4]
        assertArrayEquals(new Integer[] {1,4}, removed);
    }
}
