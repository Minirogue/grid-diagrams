package markovchain;

public interface MarkovMoveSelector<MarkovState, MM extends MarkovMove<MarkovState>> {

    MM getRandomMove(MarkovState state);

}
