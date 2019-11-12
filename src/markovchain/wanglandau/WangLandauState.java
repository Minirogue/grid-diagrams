package markovchain.wanglandau;

import markovchain.wanglandau.energy.WangLandauEnergy;

public class WangLandauState<MarkovState> {
    private MarkovState state;
    private WangLandauEnergy<MarkovState, ?> energy;

    public WangLandauState(MarkovState state, WangLandauEnergy<MarkovState, ?> energy) {
        this.state = state;
        this.energy = energy;
    }

    public MarkovState getState() {
        return state;
    }

/*
    public void setState(MarkovState state) {
        this.state = state;
    }
*/

    public WangLandauEnergy<MarkovState, ?> getEnergy() {
        return energy;
    }

   /* public void setEnergy(WangLandauEnergy<MarkovState, ?> energy) {
        this.energy = energy;
    }*/
}
