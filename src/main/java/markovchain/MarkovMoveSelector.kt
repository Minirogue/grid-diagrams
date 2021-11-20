package markovchain;

/**
 * Transitions can be chosen in several different ways for a given state space.
 * The way transitions are chosen can define the Markov chain itself.
 * This class represents the way that Markov steps are selected, which may affect detailed balance calculations.
 *
 * @param <MarkovState> The class representing the objects in the Markov chain.
 * @param <MM>          The MarkovMove class being used by the Markov chain.
 */
public interface MarkovMoveSelector<MarkovState, MM extends MarkovMove<MarkovState>> {

    /**
     * @param state The current MarkovState, from which a transition is being chosen.
     * @return A MarkovMove which represents a potential transition to a next state.
     */
    MM getRandomMove(MarkovState state);

}
