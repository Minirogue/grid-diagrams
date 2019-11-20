package markovchain.wanglandau;

import markovchain.MarkovChain;
import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;
import markovchain.wanglandau.energy.WangLandauEnergy;

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
    private final MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState, MM, E>> wangLandauMoveSelector = wangLandauState -> new WangLandauMove<>(wangLandauState, getMarkovStateMoveSelector().getRandomMove(wangLandauState.getState()));

    /**
     * Trains a set of Wang-Landau weights.
     *
     * @param state           The initial state where the Wang-Landau weight training will begin.
     * @param updateFrequency The number of steps that will be taken between each weight update. Must be at least 1.
     * @param logUpdateFactor The weights will be updated by *= e^(logUpdateFactor) at each step. Must be greater than 0.
     * @return A HashMap where the keys are energy values of type {@link E} and the values are log weights, i.e. the ratio of Wang-Landau weights for energies i and j is represented by e^(logWeight[i] - logWeight[j])
     */
    public HashMap<E, Double> train(MarkovState state, int updateFrequency, double logUpdateFactor) {
        WangLandauState<MarkovState, E> wangLandauState = new WangLandauState<>(state, getEnergyFactory().getEnergyFromState(state));
        updateWeight(wangLandauState.getEnergy(), 0);
        while (!isTrainingOver()) {
            wangLandauState = run(wangLandauState, updateFrequency);
            updateWeight(wangLandauState.getEnergy(), logUpdateFactor);
        }
        return getLogWeights();//TODO this.logweights might be left unclean here.
    }


    /**
     * Method overridden from {@link super#getAcceptanceProbability(MM)}.
     * Calculates probability of accepting a move in the Wang-Landau Markov chain.
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


    /**
     * @return The current Wang-Landau log weights used to determine transitions in the Markov chain. This will change while {@link #train(Object, int, double)} is running.
     */
    protected HashMap<E, Double> getLogWeights() {
        return logWeights;
    }

    public void setLogWeights(HashMap<E, Double> newWeights) {
        this.logWeights = newWeights;
    }

    /**
     * @return The current count of times an energy was used to update a weight during training. This might be used by {@link #isTrainingOver()}.
     */
    protected HashMap<E, Integer> getHistogram() {
        return histogram;
    }

    /**
     * @param energy          The energy for which the associated weight should be updated.
     * @param logUpdateFactor How to update the weight. This would be ln(f) in most Wang-Landau literature.
     */
    private void updateWeight(E energy, double logUpdateFactor) {
        logWeights.put(energy, logWeights.getOrDefault(energy, 0.0) + logUpdateFactor);
        histogram.put(energy, histogram.getOrDefault(energy, 0) + 1);
    }

    /**
     * Overrides {@link super#getMoveSelector()}.
     * Since this class wraps {@link MarkovState} with {@link WangLandauState}, {@link #getMarkovStateMoveSelector()} must be overridden by subclasses instead.
     */
    @Override
    public final MarkovMoveSelector<WangLandauState<MarkovState, E>, WangLandauMove<MarkovState, MM, E>> getMoveSelector() {
        return wangLandauMoveSelector;
    }

    /**
     * Overrides {@link super#deepCopy(MarkovState)}.
     * Since this class wraps {@link MarkovState} with {@link WangLandauState}, {@link #deepCopyMarkovState(MarkovState)} must be overridden by subclasses instead.
     */
    @Override
    public final WangLandauState<MarkovState, E> deepCopy(WangLandauState<MarkovState, E> wangLandauState) {
        return new WangLandauState<>(deepCopyMarkovState(wangLandauState.getState()), wangLandauState.getEnergy().copy());
    }

    /**
     * Overrides {@link super#isMoveWithinConstraints(MM)}.
     * Since this class wraps {@link MarkovState} with {@link WangLandauState}, {@link #isMarkovMoveMoveWithinConstraints(MM)} must be overridden by subclasses instead.
     */
    @Override
    public boolean isMoveWithinConstraints(WangLandauMove<MarkovState, MM, E> move) {
        return isMarkovMoveMoveWithinConstraints(move.getMarkovMove());
    }

    /**
     * See {@link MarkovChain#isMoveWithinConstraints(MM)}.
     */
    protected abstract boolean isMarkovMoveMoveWithinConstraints(MM move);

    /**
     * In general, this should return (the probability of choosing this move)/(the probability of choosing the inverse of this move from the next state).
     * This value comes from solving detailed balance of Wang-Landau for dynamic-sized systems.
     * For constant-sized systems, this is likely just 1.
     *
     * @param move Move proposed.
     * @return The coefficient to the ratio of weights in the acceptance probability obtained from detailed balance. For most constant-size systems this will just be 1.
     */
    protected double getAcceptanceAdjustment(MM move) {
        return 1.0;
    }

    /**
     * See {@link MarkovChain#deepCopy(MarkovState)}.
     */
    protected abstract MarkovState deepCopyMarkovState(MarkovState markovState);

    /**
     * See {@link MarkovChain#getMoveSelector()}.
     */
    protected abstract MarkovMoveSelector<MarkovState, MM> getMarkovStateMoveSelector();

    /**
     * @return The {@link WangLandauEnergy.WangLandauEnergyFactory} used to obtain energy states.
     */
    protected abstract WangLandauEnergy.WangLandauEnergyFactory<MarkovState, MM, E> getEnergyFactory();

    /**
     * Determines when to terminate Wang-Landau training.
     * Traditionally this is a "check for flatness."
     *
     * @return If it is deemed that {@link #train(Object, int, double)} should terminate, then this returns true, otherwise false.
     */
    protected abstract boolean isTrainingOver();
}
