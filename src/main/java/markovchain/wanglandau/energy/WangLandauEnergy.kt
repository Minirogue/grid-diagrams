package markovchain.wanglandau.energy

import markovchain.MarkovMove

/**
 *
 * @param <MarkovState> The class that represents states in the Markov chain
 * @param <E> The implementation of the specific energy type (added as a generic so the subclassed methods return instances of that specific subclass)
</E></MarkovState> */
abstract class WangLandauEnergy<MarkovState, MM : MarkovMove<MarkovState>, E : WangLandauEnergy<MarkovState, MM, E>> {

    /**
     * @return A new WangLandauEnergy object holding the same value as this one.
     */
    abstract fun copy(): E

    /**
     * @param move A proposed MarkovMove object.
     * @return An energy object containing the energy of the state that will be obtained by performing the given move.
     */
    abstract fun getNextEnergyFromMove(move: MM): E

    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
    abstract override fun toString(): String


    /**
     * Factory class for a given energy type.
     */
    abstract class Factory<MarkovState, MM : MarkovMove<MarkovState>, E : WangLandauEnergy<MarkovState, MM, E>> {

        /**
         * @param state A MarkovState for which the energy should be calculated.
         * @return A WangLandauEnergy object calculated for state.
         */
        abstract fun getEnergyFromState(state: MarkovState): E
    }
}
