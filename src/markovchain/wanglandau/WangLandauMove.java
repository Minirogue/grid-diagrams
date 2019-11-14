package markovchain.wanglandau;

import markovchain.MarkovMove;
import markovchain.wanglandau.energy.WangLandauEnergy;

public class WangLandauMove<MarkovState, MM extends MarkovMove<MarkovState>, E extends WangLandauEnergy<MarkovState,MM, E>> implements MarkovMove<WangLandauState<MarkovState, E>> {


    private WangLandauState<MarkovState, E> startingState;
    private MM markovMove;
    private E nextEnergy;

    WangLandauMove(WangLandauState<MarkovState, E> wangLandauState, MM markovMove) {
        this.startingState = wangLandauState;
        this.markovMove = markovMove;
    }

    E getNextEnergy() {
        if (nextEnergy == null){
            //The fact that both getCurrentEnergy() and markovMove hold intrinsic information about
            //the same MarkovState make this line feel kind of icky. Maybe that's just me.
            nextEnergy = getCurrentEnergy().getNextEnergyFromMove(markovMove);
        }
        return nextEnergy;
    }

    E getCurrentEnergy(){
        return startingState.getEnergy();
    }

    MM getMarkovMove(){
        return markovMove;
    }

    @Override
    public WangLandauState<MarkovState, E> perform() {
        return new WangLandauState<>(markovMove.perform(), getNextEnergy());
    }

}
