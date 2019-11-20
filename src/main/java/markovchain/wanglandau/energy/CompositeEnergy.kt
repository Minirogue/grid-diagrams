package markovchain.wanglandau.energy

import markovchain.MarkovMove
import java.util.*

class CompositeEnergy<MarkovState, MM : MarkovMove<MarkovState>>
/**
 * Constructs a CompositeEnergy out of the a List of WangLandauEnergy objects.
 *
 * @param energy List of WangLandauEnergy objects which, together, define a full energy state.
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

    override fun copy(): CompositeEnergy<MarkovState, MM> {
        val newEnergy: MutableList<WangLandauEnergy<MarkovState, MM, *>> = ArrayList(energyList.size)
        for (oldEnergy: WangLandauEnergy<MarkovState, MM, *> in energyList) {
            newEnergy.add(oldEnergy.copy() as WangLandauEnergy<MarkovState, MM, *>)
        }
        return CompositeEnergy(newEnergy)
    }

    override fun hashCode(): Int {
        var newHash = 0
        for (componentEnergy in energyList) {
            newHash = newHash * 31 xor componentEnergy.hashCode()
        }
        return newHash
    }

    /**
     * @param o Object to compare.
     * @return True if o is a CompositeEnergy that contains energies of the same type, in the same order, that are equal.
     */
    override fun equals(o: Any?): Boolean {
        return if (o is CompositeEnergy<*, *>) {
            val otherEnergyArr: List<*> = o.energyList
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
     * @param factories A List of WangLandauEnergy.Factory objects defining the energies to be composed
     */(//Factories for each energy being composed
            private val factories: List<Factory<MarkovState, MM, *>>) : Factory<MarkovState, MM, CompositeEnergy<MarkovState, MM>>() {

        override fun getEnergyFromState(markovState: MarkovState): CompositeEnergy<MarkovState, MM> {
            val newEnergy: MutableList<WangLandauEnergy<MarkovState, MM, *>> = ArrayList()
            for (factory in factories) {
                newEnergy.add(factory.getEnergyFromState(markovState) as WangLandauEnergy<MarkovState, MM, *>)
            }
            return CompositeEnergy(newEnergy)
        }

    }

}