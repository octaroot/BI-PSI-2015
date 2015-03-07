import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 6.3.15.
 */
abstract class LexicalAnalyzer
{
	/**
	 * Generates tokens from input specified
	 *
	 * @param input The input to be lexically analyzed
	 * @return The list of tokens generated
	 * @throws SyntaxIncorrect
	 * @throws InputNotAccepted
	 */
	abstract public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted;

	/**
	 * The states of the lexical analyzer finite state machine
	 */
	protected enum AnalyzerStates {}
}

/**
 * Signifies that the syntax of a command being lexically analysed incorrect
 */
final class SyntaxIncorrect extends Exception
{
	public SyntaxIncorrect(String message)
	{
		super(message);
	}
}

/**
 * The input has not been accepted by the lexical analyzer
 */
final class InputNotAccepted extends Exception
{
	public InputNotAccepted(String message)
	{
		super(message);
	}
}
