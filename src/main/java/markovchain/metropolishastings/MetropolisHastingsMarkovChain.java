package markovchain.metropolishastings;

import markovchain.MarkovChain;
import markovchain.MarkovMove;

/**
 * This class is a subclass of {@link MarkovChain} but implements acceptance probabilities for a Metropolis-Hastings style Markov chain.
 *
 * @param <MarkovState> {@inheritDoc}
 * @param <MM> {@inheritDoc}
 */
public abstract class MetropolisHastingsMarkovChain<MarkovState, MM extends MarkovMove<MarkovState>> extends MarkovChain<MarkovState, MM> {


    /**
     * {@inheritDoc}
     * Implements a check against {@link #getAcceptanceProbability(MarkovMove)} to determine whether or not to accept the transition.
     *
     * @param markovState {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public MarkovState step(MarkovState markovState) {
        MM move = getMoveSelector().getRandomMove(markovState);
        if (isMoveWithinConstraints(move) && Math.random() < getAcceptanceProbability(move)){
            return move.perform();
        }else{
            return markovState;
        }
    }

    /**
     * This is what makes it a Metropolis-Hastings Markov chain.
     * Once the move is selected, it will be accepted or rejected based on this probability.
     *
     * @param move The proposed MarkovMove
     * @return The probability that this move should be accepted and performed.
     */
    protected abstract double getAcceptanceProbability(MM move);
}
