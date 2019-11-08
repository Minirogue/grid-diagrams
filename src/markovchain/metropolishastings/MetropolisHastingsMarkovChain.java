package markovchain.metropolishastings;

import markovchain.MarkovChain;
import markovchain.MarkovMove;

public abstract class MetropolisHastingsMarkovChain<MarkovState> extends MarkovChain<MarkovState> {


    @Override
    public MarkovState step(MarkovState markovState) {
        MarkovMove<MarkovState> move = getMoveSelector().getRandomMove(markovState);
        if (Math.random() < getAcceptanceProbability(markovState, move)){
            return move.perform();
        }else{
            return markovState;
        }
    }

    public abstract double getAcceptanceProbability(MarkovState markovState, MarkovMove<MarkovState> move);
}
