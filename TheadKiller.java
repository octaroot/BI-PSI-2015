/**
 * @author martin (cernama9@fit.cvut.cz)
 * @since 7.3.15.
 */

/**
 * This class handles the timeouts
 */
final class TheadKiller implements Runnable
{

	Thread        target;
	ClientHandler runnable;

	/**
	 * Constructor
	 *
	 * @param target Target's thread
	 * @param runnable Target's runnable instance
	 */
	public TheadKiller(Thread target, ClientHandler runnable)
	{
		this.target = target;
		this.runnable = runnable;
	}

	@Override
	public void run()
	{
		int timeout = 45;
		while (timeout > 0)
		{
			if (!target.isAlive()) return;

			try
			{
				Thread.sleep(1000);
				timeout--;
			}
			catch (InterruptedException e)
			{
				System.err.println("Couldn't put ThreadKiller to sleep");
				e.printStackTrace();
			}
		}

		/**
		 * It's simple, we kill the batman
		 */
		runnable.kill();
	}
}
