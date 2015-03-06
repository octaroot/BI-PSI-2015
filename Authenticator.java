/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
final class Authenticator {
    public static boolean credentialsValid(String username, String password)
    {
        try {
            return calculateASCII(username) == Integer.parseInt(password);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static int calculateASCII(String input)
    {
        int total = 0;
        for (int i = 0; i < input.length(); i++)
            total += (int)input.charAt(i);

        return total;
    }
}
