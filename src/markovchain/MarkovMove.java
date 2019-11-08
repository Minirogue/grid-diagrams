package markovchain;

/**
 * This interface should be used to implement the command pattern: https://en.wikipedia.org/wiki/Command_pattern#Java
 * @param <MarkovState> The class representing a single state in the Markov chain.
 */
public interface MarkovMove<MarkovState> {

    /**
     * Performs the move represented by the MarkovMove object.
     * @return The state resulting from performing the move.
     */
    public MarkovState perform();

    /**
     *
     * @return Data describing the move defined by this object.
     */
    public Object[] getMoveData();

    /**
     *
     * @return The state that this move will be performed on.
     */
    public MarkovState getStartingState();

}
