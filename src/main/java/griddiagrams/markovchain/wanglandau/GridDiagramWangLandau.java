package griddiagrams.markovchain.wanglandau;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import griddiagrams.markovchain.GridMoveSelector;
import markovchain.MarkovMoveSelector;
import markovchain.wanglandau.WangLandauMarkovChain;
import markovchain.wanglandau.WangLandauState;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.HashMap;

public class GridDiagramWangLandau<E extends WangLandauEnergy<GridDiagram, GridMove, E>> extends WangLandauMarkovChain<GridDiagram, GridMove, E> {

    private final MarkovMoveSelector<GridDiagram, GridMove> markovMoveSelector = new GridMoveSelector();// Create a single GridMoveSelector to be returned by getMoveSelector()
    private final WangLandauEnergy.WangLandauEnergyFactory<GridDiagram, GridMove, E> energyFactory;// This is how the energy for the algorithm is determined
    private final int maxSize;// Used to constrain the algorithm to a finite algorithm

    /** The main constructor for GridDiagramWangLandau.
     *
     * @param energyFactory This defines the energy used to define each Wang-Landau weight.
     * @param maxSize The largest permissible size for the grid diagrams in this algorithm.
     */
    public GridDiagramWangLandau(WangLandauEnergy.WangLandauEnergyFactory<GridDiagram, GridMove, E> energyFactory, int maxSize) {
        this.maxSize = maxSize;
        this.energyFactory = energyFactory;
    }

    @Override
    public MarkovMoveSelector<GridDiagram, GridMove> getMarkovStateMoveSelector() {
        return markovMoveSelector;
    }

    @Override
    public WangLandauEnergy.WangLandauEnergyFactory<GridDiagram, GridMove, E> getEnergyFactory() {
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

    /**
     * By default this is set to bound the grid size above by the maxSize given to the constructor.
     * Override this if a different constraint is desired.
     *
     * @param move The proposed move that might push the state outside of the desired constraints
     * @return True if the move is okay. False if it violates the constraints.
     */
    @Override
    public boolean isMarkovMoveMoveWithinConstraints(GridMove move) {
        return (move.getGridFromBeforeMove().getSize() < maxSize || move.getMoveType() != GridDiagram.MOVETYPE_STABILIZATION);
    }

    @Override
    public Object sampleProperty(WangLandauState<GridDiagram, E> wangLandauState) {
        return wangLandauState.getState().copy();
    }

}
