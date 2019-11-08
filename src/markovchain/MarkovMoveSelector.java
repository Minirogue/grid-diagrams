package markovchain;

public interface MarkovMoveSelector<MarkovState> {

    public MarkovMove<MarkovState> getRandomMove(MarkovState state);

}
