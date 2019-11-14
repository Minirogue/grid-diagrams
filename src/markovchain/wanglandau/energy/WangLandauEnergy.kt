package markovchain.wanglandau.energy

import markovchain.MarkovMove
import markovchain.wanglandau.WangLandauMove
import markovchain.wanglandau.WangLandauState

/**
 *
 * @param <MarkovState> The class that represents states in the Markov chain
 * @param <E> The implementation of the specific energy type (added as a generic so the subclassed methods return instances of that specific subclass)
</E></MarkovState> */
abstract class WangLandauEnergy<MarkovState, MM : MarkovMove<MarkovState>, E : WangLandauEnergy<MarkovState, MM, E>> {

    abstract fun copy(): E

    abstract fun getNextEnergyFromMove(move: MM): E


    //Must override hashCode() and equals() for HashMap of energies to work correctly
    abstract override fun hashCode(): Int

    abstract override fun equals(other: Any?): Boolean

    //The user should be able to read the energy
    abstract override fun toString(): String


    abstract class Factory<MarkovState, MM : MarkovMove<MarkovState>, E : WangLandauEnergy<MarkovState, MM, E>> {

        abstract fun getEnergyFromState(state: MarkovState): E
    }
}
