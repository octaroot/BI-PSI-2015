/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */
final class ImageDataValidator {
    public static int calculateChecksum(byte[] data) {
        int total = 0;

        for (byte aData : data) {
            total += (int) aData;
        }

        return total;
    }
}
