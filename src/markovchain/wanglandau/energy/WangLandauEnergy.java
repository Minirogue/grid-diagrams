package markovchain.wanglandau.energy;

import markovchain.MarkovMove;
import markovchain.wanglandau.WangLandauState;

public abstract class WangLandauEnergy<MarkovState, E extends WangLandauEnergy<MarkovState, E>> {


    public abstract E nextEnergyFromMove(MarkovState state, MarkovMove<MarkovState> moveToNextState);

    public abstract E copy();

    //Must override hashCode() and equals() for HashMap of energies to work correctly
    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object o);

    //The user should be able to read the energy
    @Override
    public abstract String toString();

    public abstract static class EnergyFactory<MarkovState, E extends WangLandauEnergy<MarkovState, E>> {

        public abstract E getEnergyFromState(MarkovState state);
    }
}
