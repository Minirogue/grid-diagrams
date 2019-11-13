package markovchain;

import java.util.ArrayList;
import java.util.List;

public abstract class MarkovChain<MarkovState> {

    /**
     * Perform several steps in the Markov chain.
     *
     * @param steps The number of steps to take in the Markov chain.
     * @param state The starting point for these steps. This object may be modified by the chain.
     * @return The state obtained after taking the desired steps.
     */
    public MarkovState run(int steps, MarkovState state) {
        for (int t = 0; t < steps; t++) {
            state = step(state);
        }
        return state;
    }

    /**
     * Perform a single step in the Markov chain.
     *
     * @param state The state before performing the step. It may be modified by the step.
     * @return The state that was stepped to.
     */
    public MarkovState step(MarkovState state) {
        MarkovMove<MarkovState> move = getMoveSelector().getRandomMove(state);
        if (isMoveWithinConstraints(state, move)){
            return move.perform();
        }else {
            return state;
        }
    }

    /**
     * Perform Monte Carlo sampling of the Markov chain.
     *
     * @param numSamples      The number of desired samples.
     * @param sampleFrequency The number of steps to take within the Markov chain between each sample.
     * @param state           The initial state of the Markov chain. This object may be modified by the chain.
     * @return A List containing the obtained samples
     */
    public List<MarkovState> sample(int numSamples, int sampleFrequency, MarkovState state) {
        List<MarkovState> sampleList = new ArrayList<>();
        for (int t = 0; t < numSamples; t++) {
            state = run(sampleFrequency, state);
            sampleList.add(copy(state));
        }
        return sampleList;
    }

    /**
     * Create and return a copy of the given state so it may be stored safely while the original is modified.
     * Here's a stackoverflow question for making such a copy: https://stackoverflow.com/questions/64036/how-do-you-make-a-deep-copy-of-an-object-in-java.
     *
     * @param state The state to produce a copy of.
     * @return A copy of the given state with no references to the original object.
     */
    public abstract MarkovState copy(MarkovState state);

    /**
     * Override this method to impose constraints on the state space.
     *
     * @param state The current state from which the move is being proposed.
     * @param move The move being proposed.
     * @return true if the proposed move is allowed, false if it is not.
     */
    public boolean isMoveWithinConstraints(MarkovState state, MarkovMove<MarkovState> move){
        //System.out.println("MarkovChain.isMoveWithinConstraints called");
        return true;
    }

    public abstract MarkovMoveSelector<MarkovState> getMoveSelector();

}
