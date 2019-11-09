package markovchain.wanglandau;

import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.HashMap;

public abstract class WangLandauMarkovChain<MarkovState> extends MetropolisHastingsMarkovChain<WangLandauState<MarkovState>> {

    private HashMap<WangLandauEnergy<MarkovState, ?>, Double> logWeights = new HashMap<>();
    private HashMap<WangLandauEnergy<MarkovState, ?>, Integer> histogram = new HashMap<>();

    public HashMap<WangLandauEnergy<MarkovState, ?>, Double> train(MarkovState state, int updateFrequency, double logUpdateFactor) {
        WangLandauState<MarkovState> wangLandauState = new WangLandauState<>(state, getEnergyFactory().getEnergyFromState(state));
        updateWeight(wangLandauState.getEnergy(), 0);;
        while (!isTrainingOver()) {
            wangLandauState = run(updateFrequency, wangLandauState);
            //System.out.println(wangLandauState.getState().toString());
            updateWeight(wangLandauState.getEnergy(), logUpdateFactor);
        }
        return getLogWeights();
    }


    @Override
    public double getAcceptanceProbability(WangLandauState<MarkovState> wangLandauState, MarkovMove<WangLandauState<MarkovState>> move) {
        return Math.exp(logWeights.getOrDefault(wangLandauState.getEnergy(), 0.0) - logWeights.getOrDefault(((WangLandauMove) move).getNextEnergy(), 0.0));
    }

    protected HashMap<WangLandauEnergy<MarkovState, ?>, Double> getLogWeights() {
        return logWeights;
    }
    public HashMap<WangLandauEnergy<MarkovState, ?>, Integer> getHistogram() {
        return histogram;
    }

    private void updateWeight(WangLandauEnergy<MarkovState, ?> energy, double logUpdateFactor) {
        logWeights.put(energy, logWeights.getOrDefault(energy, 0.0) + logUpdateFactor);
        histogram.put(energy, histogram.getOrDefault(energy, 0)+1);
    }

    class WangLandauMove implements MarkovMove<WangLandauState<MarkovState>> {

        private WangLandauState<MarkovState> startingState;
        private MarkovMove<MarkovState> markovMove;
        private WangLandauEnergy<MarkovState, ?> nextEnergy;

        WangLandauMove(WangLandauState<MarkovState> wangLandauState, MarkovMove<MarkovState> markovMove) {
            this.startingState = wangLandauState;
            this.markovMove = markovMove;
            nextEnergy = wangLandauState.getEnergy().nextEnergyFromMove(wangLandauState.getState(), markovMove);
        }

        WangLandauEnergy<MarkovState, ?> getNextEnergy() {
            return nextEnergy;
        }

        @Override
        public WangLandauState<MarkovState> perform() {
            return new WangLandauState<>(markovMove.perform(), nextEnergy);
        }

        @Override
        public Object[] getMoveData() {
            return markovMove.getMoveData();
        }

        @Override
        public WangLandauState<MarkovState> getStartingState() {
            return startingState;
        }
    }

    @Override
    public MarkovMoveSelector<WangLandauState<MarkovState>> getMoveSelector() {
        return wangLandauState -> new WangLandauMove(wangLandauState, getMarkovStateMoveSelector().getRandomMove(wangLandauState.getState()));
    }

    @Override
    public WangLandauState<MarkovState> copy(WangLandauState<MarkovState> wangLandauState) {
        return new WangLandauState<>(copyState(wangLandauState.getState()), wangLandauState.getEnergy().copy());
    }

    public abstract MarkovState copyState(MarkovState markovState);

    public abstract MarkovMoveSelector<MarkovState> getMarkovStateMoveSelector();

    public abstract WangLandauEnergy.EnergyFactory<MarkovState, ?> getEnergyFactory();

    public abstract boolean isTrainingOver();
}
