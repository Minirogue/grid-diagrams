package griddiagrams.markovchain.wanglandau;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import griddiagrams.markovchain.GridMoveSelector;
import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.wanglandau.WangLandauMarkovChain;
import markovchain.wanglandau.WangLandauState;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.HashMap;

public class GridDiagramWangLandau extends WangLandauMarkovChain<GridDiagram> {

    private MarkovMoveSelector<GridDiagram> markovMoveSelector = new GridMoveSelector();
    private WangLandauEnergy.EnergyFactory<GridDiagram, ?> energyFactory = new SizeEnergy.SizeEnergyFactory();

    @Override
    public MarkovMoveSelector<GridDiagram> getMarkovStateMoveSelector() {
        return markovMoveSelector;
    }

    @Override
    public WangLandauEnergy.EnergyFactory<GridDiagram, ?> getEnergyFactory() {
        return energyFactory;
    }

    @Override
    public double getAcceptanceProbability(WangLandauState<GridDiagram> wangLandauState, MarkovMove<WangLandauState<GridDiagram>> move) {
        double adjustment = 1.0;
        switch ((int) move.getMoveData()[0]) {
            case GridDiagram.MOVETYPE_STABILIZATION:
                adjustment = 2.0 * wangLandauState.getState().getSize();
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                adjustment = 1.0 / (2.0 * (wangLandauState.getState().getSize() - 1));
                break;
        }
        return super.getAcceptanceProbability(wangLandauState, move) * adjustment;
    }

    @Override
    public boolean isTrainingOver() {
        HashMap<WangLandauEnergy<GridDiagram, ?>, Integer> histogram = getHistogram();
        //System.out.println("isTrainingOver called");
        //System.out.println(getHistogram().toString());
        for (WangLandauEnergy<GridDiagram, ?> key : histogram.keySet()) {
            //System.out.println(key.toString()+" "+histogram.get(key));
            if (histogram.get(key) < 10000) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMoveWithinConstraints(WangLandauState<GridDiagram> gridDiagramWangLandauState, MarkovMove<WangLandauState<GridDiagram>> move) {
        //System.out.println("GridDiagramWangLandau.isMoveWithinConstraints called");
        return (gridDiagramWangLandauState.getState().getSize() <= 10 || (int) move.getMoveData()[0] != GridDiagram.MOVETYPE_STABILIZATION);//TODO implement more broadly
    }

    @Override
    public GridDiagram copyState(GridDiagram gridDiagram) {
        return gridDiagram.copy();
    }
}
