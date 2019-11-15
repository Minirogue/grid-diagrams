package markovchain.wanglandau;

import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;
import markovchain.wanglandau.energy.WangLandauEnergy;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @param <MarkovState> The class which represents the objects in the Markov chain
 * @param <MM>          The class which defines moves on the Markov chain
 * @param <E>           The class representing the WangLandauEnergy that will be used to explore the Markov chain
 */
public abstract class WangLandauMarkovChain<MarkovState, MM extends MarkovMove<MarkovState>, E extends WangLandauEnergy<MarkovState, MM, E>> extends MetropolisHastingsMarkovChain<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState, MM, E>> {

    //We store and update the log of the Wang-Landau weights to avoid overflows
    private HashMap<E, Double> logWeights = new HashMap<>();
    //The histogram stores the counts of each energy found, which may be used for a stopping condition
    private HashMap<E, Integer> histogram = new HashMap<>();
    private MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState, MM, E>> wangLandauMoveSelector = wangLandauState -> new WangLandauMove<>(wangLandauState, getMarkovStateMoveSelector().getRandomMove(wangLandauState.getState()));

    /**
     * Trains a set of Wang-Landau weights.
     *
     * @param state           The initial state where the Wang-Landau weight training will begin.
     * @param logWeights      A preseeded list of Wang-Landau weights to use as a starting point for the training. This can be null or output from a previous training.
     * @param updateFrequency The number of steps that will be taken between each weight update. Must be at least 1.
     * @param logUpdateFactor The weights will be updated by *= e^(logUpdateFactor) at each step. Must be greater than 0.
     * @return A HashMap where the keys are energy values of type {@link E} and the values are log weights, i.e. the ratio of Wang-Landau weights for energies i and j is represented by e^(logWeight[i] - logWeight[j])
     */
    public HashMap<E, Double> train(MarkovState state, HashMap<E, Double> logWeights, int updateFrequency, double logUpdateFactor) {
        if (logWeights != null) {
            this.logWeights = logWeights;
        } else {
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


    /**
     * Method overriden from {@link super#getAcceptanceProbability(MarkovMove)}
     */
    @Override
    public final double getAcceptanceProbability(WangLandauMove<MarkovState, MM, E> move) {
        double logDiff = logWeights.getOrDefault(move.getCurrentEnergy(), 0.0) - logWeights.getOrDefault(move.getNextEnergy(), 0.0);
        logDiff += Math.log(getAcceptanceAdjustment(move.getMarkovMove()));
        if (logDiff >= 0) { //Avoid math overflow errors from trying to exponentiate large numbers
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
        histogram.put(energy, histogram.getOrDefault(energy, 0) + 1);
    }

    @Override
    public MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState, MM, E>> getMoveSelector() {
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


    public double getAcceptanceAdjustment(MM move) {
        return 1.0;
    }

    public abstract MarkovState deepCopyMarkovState(MarkovState markovState);

    public abstract MarkovMoveSelector<MarkovState, MM> getMarkovStateMoveSelector();

    public abstract WangLandauEnergy.Factory<MarkovState, MM, E> getEnergyFactory();

    public abstract boolean isTrainingOver();
}
