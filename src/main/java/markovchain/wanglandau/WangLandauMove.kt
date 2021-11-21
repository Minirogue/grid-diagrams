package markovchain.wanglandau;

import markovchain.MarkovMove;
import markovchain.wanglandau.energy.WangLandauEnergy;

/**
 * A wrapper around the MarkovMoves used as transitions in the MarkovChain
 *
 * @param <MarkovState> The class for the underlying state space.
 * @param <MM>          The MarkovMoves being used to perform transitions.
 * @param <E>
 */
public class WangLandauMove<MarkovState, MM extends MarkovMove<MarkovState>, E extends WangLandauEnergy<MarkovState, MM, E>> implements MarkovMove<WangLandauState<MarkovState, E>> {

    //The state (with energy) before performing the move
    private final WangLandauState<MarkovState, E> startingState;
    //The wrapped move being proposed
    private final MM markovMove;
    //The energy for the next state
    private E nextEnergy;

    /**
     * @param wangLandauState The current state, from which the move will be performed.
     * @param markovMove      The proposed MarkovMove.
     */
    WangLandauMove(WangLandauState<MarkovState, E> wangLandauState, MM markovMove) {
        this.startingState = wangLandauState;
        this.markovMove = markovMove;
    }

    /**
     * @return The energy of the state that would be obtained by calling {@link #perform()}
     */
    E getNextEnergy() {
        if (nextEnergy == null) {//nextEnergy is only calculated when it is needed, then cached.
            //The fact that both getCurrentEnergy() and markovMove hold intrinsic information about
            //the same MarkovState make this line feel kind of icky. Maybe that's just me.
            nextEnergy = getCurrentEnergy().getNextEnergyFromMove(markovMove);
        }
        return nextEnergy;
    }

    /**
     * @return The energy of the state that this move is being taken from.
     */
    E getCurrentEnergy() {
        return startingState.getEnergy();
    }

    /**
     * @return The underlying move in the Markov chain being wrapped by this object.
     */
    MM getMarkovMove() {
        return markovMove;
    }

    @Override
    public WangLandauState<MarkovState, E> perform() {
        return new WangLandauState<>(markovMove.perform(), getNextEnergy());
    }

}
