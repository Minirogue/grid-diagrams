package markovchain.wanglandau.energy

import markovchain.MarkovMove
import java.util.*

class CompositeEnergy<MarkovState, MM : MarkovMove<MarkovState>>
/**
 * Constructs a CompositeEnergy out of the a List of WangLandauEnergy objects.
 *
 * @param energyList List of WangLandauEnergy objects which, together, define a full energy state.
 */ private constructor(
        /**
         * @return Get the set of energy values that comprise this CompositeEnergy as a List.
         */
        //Stores the list of individual energy states
        private val energyList: List<WangLandauEnergy<MarkovState, MM, *>>) : WangLandauEnergy<MarkovState, MM, CompositeEnergy<MarkovState, MM>>() {

    /**
     * @param move A proposed MarkovMove
     * @return A CompositeEnergy for the state obtained from moveToNextState that is made of WangLandauEnergy objects of the same types as this object.
     */
    override fun getNextEnergyFromMove(move: MM): CompositeEnergy<MarkovState, MM> {
        val newEnergyList: MutableList<WangLandauEnergy<MarkovState, MM, *>> = ArrayList(energyList.size)
        for (en: WangLandauEnergy<MarkovState, MM, *> in energyList) {
            newEnergyList.add(en.getNextEnergyFromMove(move) as WangLandauEnergy<MarkovState, MM, *>)
        }
        return CompositeEnergy(newEnergyList)
    }

    override fun hashCode(): Int {
        var newHash = 0
        for (componentEnergy in energyList) {
            newHash = newHash * 31 xor componentEnergy.hashCode()
        }
        return newHash
    }

    /**
     * @param other Object to compare.
     * @return True if o is a CompositeEnergy that contains energies of the same type, in the same order, that are equal.
     */
    override fun equals(other: Any?): Boolean {
        return if (other is CompositeEnergy<*, *>) {
            val otherEnergyArr: List<*> = other.energyList
            if (otherEnergyArr.size != energyList.size) {
                return false
            }
            for (i in energyList.indices) {
                if (otherEnergyArr[i] != energyList[i]) {
                    return false
                }
            }
            true
        } else {
            false
        }
    }

    /**
     * @return A string formatted as a tuple of the energy values that make up this energy.
     */
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("(")
        for (i in energyList.indices) {
            stringBuilder.append(energyList[i].toString())
            if (i < energyList.size - 1) {
                stringBuilder.append(", ")
            }
        }
        stringBuilder.append(")")
        return stringBuilder.toString()
    }

    class CompositeEnergyFactory<MarkovState, MM : MarkovMove<MarkovState>>
    /**
     * @param wangLandauEnergyFactories A List of WangLandauEnergy.Factory objects defining the energies to be composed
     */(//Factories for each energy being composed
            private val wangLandauEnergyFactories: List<WangLandauEnergyFactory<MarkovState, MM, *>>) : WangLandauEnergyFactory<MarkovState, MM, CompositeEnergy<MarkovState, MM>>() {

        override fun getEnergyFromState(state: MarkovState): CompositeEnergy<MarkovState, MM> {
            val newEnergy: MutableList<WangLandauEnergy<MarkovState, MM, *>> = ArrayList()
            for (factory in wangLandauEnergyFactories) {
                newEnergy.add(factory.getEnergyFromState(state) as WangLandauEnergy<MarkovState, MM, *>)
            }
            return CompositeEnergy(newEnergy)
        }

    }

}