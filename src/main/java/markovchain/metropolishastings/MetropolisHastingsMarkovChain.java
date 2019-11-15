package markovchain.metropolishastings;

import markovchain.MarkovChain;
import markovchain.MarkovMove;

public abstract class MetropolisHastingsMarkovChain<MarkovState, MM extends MarkovMove<MarkovState>> extends MarkovChain<MarkovState, MM> {


    @Override
    public MarkovState step(MarkovState markovState) {
        MM move = getMoveSelector().getRandomMove(markovState);
        if (isMoveWithinConstraints(move) && Math.random() < getAcceptanceProbability(move)){
            return move.perform();
        }else{
            return markovState;
        }
    }

    public abstract double getAcceptanceProbability(MM move);
}
