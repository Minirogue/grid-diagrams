package markovchain.wanglandau;

import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.HashMap;

public abstract class WangLandauMarkovChain<MarkovState, MM extends MarkovMove<MarkovState>, E extends WangLandauEnergy<MarkovState,MM, E>> extends MetropolisHastingsMarkovChain<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState,MM,E>> {

    private HashMap<E, Double> logWeights = new HashMap<>();
    private HashMap<E, Integer> histogram = new HashMap<>();
    private MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState,MM,E>> wangLandauMoveSelector = wangLandauState -> new WangLandauMove<>(wangLandauState, getMarkovStateMoveSelector().getRandomMove(wangLandauState.getState()));

    public HashMap<E, Double> train(MarkovState state, HashMap<E, Double> logWeights, int updateFrequency, double logUpdateFactor) {
        if (logWeights != null){
            this.logWeights = logWeights;
        }else {
            this.logWeights = new HashMap<>();
        }
        WangLandauState<MarkovState, E> wangLandauState = new WangLandauState<>(state, getEnergyFactory().getEnergyFromState(state));
        updateWeight(wangLandauState.getEnergy(), 0);
        while (!isTrainingOver()) {
            wangLandauState = run(updateFrequency, wangLandauState);
            updateWeight(wangLandauState.getEnergy(), logUpdateFactor);
        }
        return getLogWeights();//TODO this.logweights might be left unclean here.
    }


    @Override
    public double getAcceptanceProbability(WangLandauMove<MarkovState, MM, E> move) {
        double logDiff = logWeights.getOrDefault(move.getCurrentEnergy(), 0.0) - logWeights.getOrDefault(move.getNextEnergy(), 0.0);
        logDiff += Math.log(getAcceptanceAdjustment(move.getMarkovMove()));
        if (logDiff >= 0){ //Avoid math overflow errors from trying to exponentiate large numbers
            return 1;
        } else {
            return Math.exp(logDiff);
        }
    }

    protected HashMap<E, Double> getLogWeights() {
        return logWeights;
    }
    protected HashMap<E, Integer> getHistogram() {
        return histogram;
    }

    private void updateWeight(E energy, double logUpdateFactor) {
        logWeights.put(energy, logWeights.getOrDefault(energy, 0.0) + logUpdateFactor);
        histogram.put(energy, histogram.getOrDefault(energy, 0)+1);
    }

    @Override
    public MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState,MM,E>> getMoveSelector() {
        return wangLandauMoveSelector;
    }

    @Override
    public WangLandauState<MarkovState, E> deepCopy(WangLandauState<MarkovState, E> wangLandauState) {
        return new WangLandauState<>(deepCopyMarkovState(wangLandauState.getState()), wangLandauState.getEnergy().copy());
    }

    @Override
    public boolean isMoveWithinConstraints(WangLandauMove<MarkovState, MM, E> move) {
        return isMarkovMoveMoveWithinConstraints(move.getMarkovMove());
    }
    public abstract boolean isMarkovMoveMoveWithinConstraints(MM move);


    public double getAcceptanceAdjustment(MM move){
        return 1.0;
    }

    public abstract MarkovState deepCopyMarkovState(MarkovState markovState);

    public abstract MarkovMoveSelector<MarkovState, MM> getMarkovStateMoveSelector();

    public abstract WangLandauEnergy.Factory<MarkovState,MM,E> getEnergyFactory();

    public abstract boolean isTrainingOver();
}
