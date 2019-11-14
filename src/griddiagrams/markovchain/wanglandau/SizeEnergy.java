package griddiagrams.markovchain.wanglandau;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import markovchain.wanglandau.energy.WangLandauEnergy;

public class SizeEnergy extends WangLandauEnergy<GridDiagram, GridMove, SizeEnergy> {

    private int gridSize;

    private SizeEnergy(int gridSize) {
        this.gridSize = gridSize;
    }

    private int getSize() {
        return gridSize;
    }


    @Override
    public SizeEnergy copy() {
        return new SizeEnergy(gridSize);
    }

    @Override
    public SizeEnergy getNextEnergyFromMove(GridMove moveToNextState) {
        switch (moveToNextState.getMoveType()) {
            case GridDiagram.MOVETYPE_NONE:
            case GridDiagram.MOVETYPE_COMMUTATION:
                return new SizeEnergy(getSize());
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                return new SizeEnergy(getSize() - 1);
            case GridDiagram.MOVETYPE_STABILIZATION:
                return new SizeEnergy(getSize() + 1);
            default:
                System.err.println("Error in SizeEnergy.nextEnergyFromMove(), type not found");
                return new SizeEnergy(-1);
        }
    }

    @Override
    public int hashCode() {
        return gridSize;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SizeEnergy) {
            return this.getSize() == ((SizeEnergy) o).getSize();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Integer.toString(getSize());
    }

    public static class SizeEnergyFactory extends Factory<GridDiagram, GridMove, SizeEnergy> {
        @Override
        public SizeEnergy getEnergyFromState(GridDiagram gridDiagram) {
            return new SizeEnergy(gridDiagram.getSize());
        }


    }
}
