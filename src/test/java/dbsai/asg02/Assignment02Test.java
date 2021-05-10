package dbsai.asg02;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class Assignment02Test {


    /**
     * Map of all the existing implementations from the submissions.
     */
    public static final Map<String, Supplier<Assignment02>> GROUPS = Map.of(
            // sample solution at index 0
            "00", Assignment02Group99::new
    );

    /**
     * Parameters for the tests, i.e., group names.
     *
     * @return list of singleton object arrays containing the group numbers
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return GROUPS.keySet().stream().sorted().map(s -> new Object[]{s}).collect(Collectors.toList());
    }

    /**
     * Supplier for parameterized constructor.
     */
    private final Supplier<Assignment02> constructor;

    /**
     * Constructs a test instance for a given group.
     *
     * @param group group name
     */
    public Assignment02Test(final String group) {
        this.constructor = GROUPS.get(group);
    }

    /**
     * Tests reading single bits against a BitSet.
     */
    @Test
    public void testReadBlock() {
        // 1111111100000101
        final byte[] bitmap = {5, 127};

        final BitSet groundTruth = BitSet.valueOf(bitmap);

        // test blockFree
        final Assignment02 bitmapUtil = constructor.get();
        for (int i = 0; i < bitmap.length * Byte.SIZE; i++) {
            assertEquals(!groundTruth.get(i), bitmapUtil.blockFree(bitmap, i));
        }
    }

    /**
     * Tests setting single bits.
     */
    @Test
    public void testWriteBlock() {
        // 1111111100000101
        final byte[] bitmap = {5, 127};
        final Assignment02 bitmapUtil = constructor.get();
        bitmapUtil.markBlock(bitmap, 1, false);
        assertFalse(bitmapUtil.blockFree(bitmap, 1));
        assertEquals(7, bitmap[0]);
        bitmapUtil.markBlock(bitmap, 1, true);
        assertTrue(bitmapUtil.blockFree(bitmap, 1));
        assertEquals(5, bitmap[0]);
        bitmapUtil.markBlock(bitmap, 15, false);
        assertEquals(-1, bitmap[1]);
    }

    /**
     * Tests a sequence of random read and write operations against a BitSet.
     */
    @Test
    public void fuzzyTest() {
        final Assignment02 bitmapUtil = constructor.get();
        final Random rng = new Random();
        final byte[] bits = new byte[16];
        final BitSet groundTruth = new BitSet();
        for (int i = 0; i < 10_000; i++) {
            final int block = rng.nextInt(bits.length * Byte.SIZE);
            final boolean free = rng.nextBoolean();
            bitmapUtil.markBlock(bits, block, free);
            groundTruth.set(block, !free);
            for (int j = 0; j < bits.length * Byte.SIZE; j++) {
                assertEquals(!groundTruth.get(j), bitmapUtil.blockFree(bits, j));
            }
        }
    }
}
