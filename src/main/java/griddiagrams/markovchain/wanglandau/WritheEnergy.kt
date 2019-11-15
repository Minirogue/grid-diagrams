package griddiagrams.markovchain.wanglandau

import griddiagrams.GridDiagram
import griddiagrams.markovchain.GridMove
import markovchain.wanglandau.energy.WangLandauEnergy

class WritheEnergy(private val writhe: Int) : WangLandauEnergy<GridDiagram, GridMove, WritheEnergy>() {


    override fun copy(): WritheEnergy {
        return WritheEnergy(writhe)
    }

    override fun getNextEnergyFromMove(move: GridMove): WritheEnergy {
        val deltaWrithe = move.gridFromBeforeMove.deltaWrithe(move.moveType, move.moveArguments)
        return WritheEnergy(writhe + deltaWrithe)
    }



    override fun hashCode(): Int {
        return writhe
    }

    override fun equals(other: Any?): Boolean {
        return if (other is WritheEnergy){
            other.writhe == writhe
        }else{
            false
        }
    }

    override fun toString(): String {
        return writhe.toString()
    }


    class Factory : WangLandauEnergy.Factory<GridDiagram, GridMove, WritheEnergy>() {
        override fun getEnergyFromState(state: GridDiagram): WritheEnergy {
            return WritheEnergy(state.calcWrithe())
        }
    }
}