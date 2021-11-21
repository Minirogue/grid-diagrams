package markovchain.wanglandau;

import markovchain.wanglandau.energy.WangLandauEnergy;

/**
 * A wrapper class representing a state in the Markov chain and its corresponding energy value.
 *
 * @param <MarkovState> The class representing states in the Markov Chain
 */
public class WangLandauState<MarkovState, E extends WangLandauEnergy<MarkovState, ?, E>> {
    private final MarkovState state;
    private final E energy;

    /**
     * @param state  The underlying MarkovState represented by this object.
     * @param energy The energy value of the given state.
     */
    WangLandauState(MarkovState state, E energy) {
        this.state = state;
        this.energy = energy;
    }

    /**
     * @return The state in the Markov chain represented by this object.
     */
    public MarkovState getState() {
        return state;
    }

    /**
     * @return The energy of the state obtained from {@link #getState()}
     */
    public E getEnergy() {
        return energy;
    }

}
