package markovchain;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the core logic of a Markov chain as it is typically used for Monte Carlo sampling.
 *
 * @param <MarkovState> The class that represents individual states in the Markov chain. This could be a class representing the Ising model, knot diagrams, knot embeddings, etc.
 * @param <MM>          The class that represents transitions in the Markov chain.
 */
public abstract class MarkovChain<MarkovState, MM extends MarkovMove<MarkovState>> {

    /**
     * Perform a single step in the Markov chain.
     *
     * @param state The state before performing the step. It may be modified by the step.
     * @return The state that was stepped to.
     */
    public MarkovState step(MarkovState state) {
        MM move = getMoveSelector().getRandomMove(state);
        if (isMoveWithinConstraints(move)) {
            return move.perform();
        } else {
            return state;
        }
    }

    /**
     * Perform several steps in the Markov chain.
     *
     * @param state The starting point for these steps. This object may be modified by the chain.
     * @param steps The number of steps to take in the Markov chain.
     * @return The state obtained after taking the desired steps.
     */
    public MarkovState run(MarkovState state, int steps) {
        for (int t = 0; t < steps; t++) {
            state = step(state);
        }
        return state;
    }


    /**
     * Perform Monte Carlo sampling of the Markov chain.
     *
     * @param state           The initial state of the Markov chain. This object may be modified by the chain.
     * @param sampleFrequency The number of steps to take within the Markov chain between each sample.
     * @param numSamples      The number of desired samples.
     * @return A List containing the obtained samples
     */
    public List<Object> sample(MarkovState state, int sampleFrequency, int numSamples) {
        List<Object> sampleList = new ArrayList<>();
        for (int t = 0; t < numSamples; t++) {
            state = run(state, sampleFrequency);
            sampleList.add(sampleProperty(state));
        }
        return sampleList;
    }

    /**
     * Returns the property being sampled.  By default, it returns the state.
     * This may cause issues if the state is reused and modified in the implementation of MarkovMove.
     * It is recommended to override this method to create a copy of the given state if that is what is being sampled.
     *
     * @param state The state being sampled.
     * @return The property of the given state being sampled.
     */
    protected Object sampleProperty(MarkovState state){
        return state;
    }

    /**
     * Override this method to impose constraints on the state space.
     *
     * @param move The move being proposed.
     * @return true if the proposed move is allowed, false if it is not.
     */
    protected boolean isMoveWithinConstraints(MM move) {
        //System.out.println("MarkovChain.isMoveWithinConstraints called");
        return true;
    }

    /**
     * @return The MarkovMoveSelector object that is to be used for choosing transitions.
     */
    protected abstract MarkovMoveSelector<MarkovState, MM> getMoveSelector();

}
