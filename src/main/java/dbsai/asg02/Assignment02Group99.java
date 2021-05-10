package dbsai.asg02;

public class Assignment02Group99 implements Assignment02 {

    @Override
    public boolean blockFree(final byte[] bitmap, final int blockNr) {
        if (blockNr < 0 || blockNr >= bitmap.length * Byte.SIZE) {
            throw new IndexOutOfBoundsException();
        }
        return (bitmap[blockNr / Byte.SIZE] & (1 << (blockNr % Byte.SIZE))) == 0;
    }

    @Override
    public void markBlock(final byte[] bitmap, final int blockNr, final boolean free) {
        if (blockNr < 0 || blockNr >= bitmap.length * Byte.SIZE) {
            throw new IndexOutOfBoundsException();
        }
        final int index = blockNr / Byte.SIZE;
        final int mask = 1 << (blockNr % Byte.SIZE);
        if (free) {
            bitmap[index] &= ~mask;
        } else {
            bitmap[index] |= mask;
        }
    }
}
