/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */

/**
 * This class help to validate image data
 */
final class ImageDataValidator
{
	/**
	 * Calculates checksum of data specified
	 * @param data The data to calculate the checksum of
	 * @return The checksum
	 */
	public static int calculateChecksum(byte[] data)
	{
		int total = 0;

		for (byte aData : data)
		{
			total += (aData & 0xFF);
		}

		return total;
	}
}
