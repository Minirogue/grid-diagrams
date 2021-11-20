package griddiagrams.markovchain.wanglandau

import griddiagrams.GridDiagram
import griddiagrams.markovchain.GridMove
import markovchain.wanglandau.energy.WangLandauEnergy
import kotlin.system.exitProcess

data class SizeEnergy constructor(private val size: Int) : WangLandauEnergy<GridDiagram, GridMove, SizeEnergy>() {

    override fun getNextEnergyFromMove(move: GridMove): SizeEnergy {
        //The change in grid size from performing a move depends only on the move chosen.
        return when (move.moveType) {
            GridDiagram.MOVETYPE_NONE, GridDiagram.MOVETYPE_COMMUTATION -> SizeEnergy(size)
            GridDiagram.MOVETYPE_DESTABILIZATION -> SizeEnergy(size - 1)
            GridDiagram.MOVETYPE_STABILIZATION -> SizeEnergy(size + 1)
            else -> {
                System.err.println("Error in SizeEnergy.nextEnergyFromMove(), type not found")
                exitProcess(1)
            }
        }
    }

    override fun toString(): String {
        return size.toString()
    }

    class SizeEnergyFactory : WangLandauEnergyFactory<GridDiagram, GridMove, SizeEnergy>() {
        override fun getEnergyFromState(state: GridDiagram): SizeEnergy {
            return SizeEnergy(state.size)
        }
    }

}