/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */

/**
 * This class handles authentication of users
 */
final class Authenticator
{
	/**
	 * Checks whether the credentials are valid
	 *
	 * @param username The username
	 * @param password The password
	 * @return True when valid, false otherwise
	 */
	public static boolean areCredentialsValid(String username, String password)
	{
		try
		{
			return calculateASCIISum(username) == Integer.parseInt(password);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Calculates the sum of ASCII values in a string specified
	 *
	 * @param input String of ASCII characters to be summed up
	 * @return The total sum of ASCII values
	 */
	private static int calculateASCIISum(String input)
	{
		int total = 0;
		for (int i = 0; i < input.length(); i++)
			total += (int) input.charAt(i);

		return total;
	}
}
