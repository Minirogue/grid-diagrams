package griddiagrams.markovchain.wanglandau;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import griddiagrams.markovchain.GridMoveSelector;
import markovchain.MarkovMoveSelector;
import markovchain.wanglandau.WangLandauMarkovChain;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.HashMap;

public class GridDiagramWangLandau<E extends WangLandauEnergy<GridDiagram, GridMove, E>> extends WangLandauMarkovChain<GridDiagram, GridMove, E> {

    private final MarkovMoveSelector<GridDiagram, GridMove> markovMoveSelector = new GridMoveSelector();
    private final WangLandauEnergy.Factory<GridDiagram, GridMove, E> energyFactory;
    private final int maxSize;

    public GridDiagramWangLandau(int maxSize, WangLandauEnergy.Factory<GridDiagram, GridMove, E> energyFactory){
        this.maxSize = maxSize;
        this.energyFactory = energyFactory;
    }

    @Override
    public MarkovMoveSelector<GridDiagram, GridMove> getMarkovStateMoveSelector() {
        return markovMoveSelector;
    }

    @Override
    public WangLandauEnergy.Factory<GridDiagram,GridMove, E> getEnergyFactory() {
        return energyFactory;
    }

    @Override
    public double getAcceptanceAdjustment(GridMove move) {
        double adjustment = 1.0;
        int currentGridSize = move.getGridFromBeforeMove().getSize();
        switch (move.getMoveType()) {
            case GridDiagram.MOVETYPE_STABILIZATION:
                adjustment = 2.0 * currentGridSize;
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                adjustment = 1.0 / (2.0 * (currentGridSize - 1));
                break;
        }
        return adjustment;
    }


    @Override
    public boolean isTrainingOver() {
        HashMap<E, Integer> histogram = getHistogram();
        //System.out.println("isTrainingOver called");
        //System.out.println(getHistogram().toString());
        for (E key : histogram.keySet()) {
            //System.out.println(key.toString()+" "+histogram.get(key));
            if (histogram.get(key) < 10000) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMarkovMoveMoveWithinConstraints(GridMove move) {
        return (move.getGridFromBeforeMove().getSize() <= maxSize || move.getMoveType() != GridDiagram.MOVETYPE_STABILIZATION);
    }

    @Override
    public GridDiagram deepCopyMarkovState(GridDiagram gridDiagram) {
        return gridDiagram.copy();
    }
}
