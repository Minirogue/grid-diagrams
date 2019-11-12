package markovchain;

public interface MarkovMoveSelector<MarkovState> {

    MarkovMove<MarkovState> getRandomMove(MarkovState state);

}
