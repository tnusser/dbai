package dbsai.asg02;

/**
 * Interface for the bit operations from Assignment 2.
 *
 * @author Leo Woerteler &lt;leonard.woerteler@uni.kn&gt;
 */
public interface Assignment02 {
    /**
     * Checks if the block with the given number is free in the given bit map.
     *
     * @param bitmap bit map represented as an array of bytes
     * @param blockNr index of the block in the bit map
     * @return {@code true} if the block if free, {@code false} otherwise
     */
    boolean blockFree(byte[] bitmap, int blockNr);

    /**
     * Marks a block as either free or occupied in the given bit map.
     *
     * @param bitmap bit map represented as an array of bytes
     * @param blockNr index of the block to mark as free or occupied
     * @param free {@code true} to mark as free, {@code false} to mark as occupied
     */
    void markBlock(byte[] bitmap, int blockNr, boolean free);
}
