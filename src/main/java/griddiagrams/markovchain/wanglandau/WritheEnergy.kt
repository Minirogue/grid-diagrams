package griddiagrams.markovchain.wanglandau

import griddiagrams.GridDiagram
import griddiagrams.markovchain.GridMove
import markovchain.wanglandau.energy.WangLandauEnergy

data class WritheEnergy(private val writhe: Int) : WangLandauEnergy<GridDiagram, GridMove, WritheEnergy>() {

    override fun getNextEnergyFromMove(move: GridMove): WritheEnergy {
        val deltaWrithe = move.gridFromBeforeMove.deltaWrithe(move.moveType, move.moveArguments)
        return WritheEnergy(writhe + deltaWrithe)
    }

    override fun toString(): String {
        return writhe.toString()
    }


    class WritheEnergyFactory : WangLandauEnergyFactory<GridDiagram, GridMove, WritheEnergy>() {
        override fun getEnergyFromState(state: GridDiagram): WritheEnergy {
            return WritheEnergy(state.calcWrithe())
        }
    }
}