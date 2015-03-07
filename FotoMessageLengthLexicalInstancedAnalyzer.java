import java.util.LinkedList;

/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */

/**
 * Derived lexical analyzer capable of analyzing the string one char at a time.
 */
final class FotoMessageLengthLexicalInstancedAnalyzer extends LexicalAnalyzer
{

	private AnalyzerStates     state;
	private LinkedList<String> tokens;

	/**
	 * Constructor
	 */
	FotoMessageLengthLexicalInstancedAnalyzer()
	{
		this.state = AnalyzerStates.EMPTY;
		this.tokens = new LinkedList<String>();
	}

	/**
	 * Processes a character
	 *
	 * @param c Character of the input string
	 * @throws SyntaxIncorrect
	 */
	public void parseCharacter(char c) throws SyntaxIncorrect
	{
		handleInput(c);
	}

	/**
	 * Checks whether the analyzer has reached a final state
	 *
	 * @return True if final state has been reached, false otherwise
	 */
	public boolean isStateFinal()
	{
		return state == AnalyzerStates.DONE;
	}

	/**
	 * Internal input handling
	 *
	 * @param c Character of the input string
	 * @throws SyntaxIncorrect
	 */
	private void handleInput(char c) throws SyntaxIncorrect
	{
		switch (state)
		{

			case NUMBER:
				if (c == ' ')
				{
					state = AnalyzerStates.DONE;
					break;
				}
			case EMPTY:
				if (c < '0' || c > '9') throw new SyntaxIncorrect("Length has to be numerical");
				state = AnalyzerStates.NUMBER;
				break;
			case DONE:
				throw new SyntaxIncorrect("Cannot read anymore, word already accepted");
		}
	}

	@Override
	public LinkedList<String> parseTokens(String input) throws SyntaxIncorrect, InputNotAccepted
	{

		for (int i = 0; i < input.length(); i++)
		{
			handleInput(input.charAt(i));
		}

		if (state != AnalyzerStates.DONE)
			throw new InputNotAccepted("Length not specified - not accepted by PayloadTypeLexicalAnalyzer");

		tokens.add(state.toString());

		return tokens;
	}

	protected enum AnalyzerStates
	{
		EMPTY, NUMBER, DONE
	}
}
