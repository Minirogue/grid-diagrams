package griddiagrams.markovchain.canonicalalgorithm;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import griddiagrams.markovchain.GridMoveSelector;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;

import java.util.ArrayList;
import java.util.List;

public class CanonicalGridAlgorithm extends MetropolisHastingsMarkovChain<GridDiagram, GridMove> {

    private final double z;
    private final List<Double[]> probabilities = new ArrayList<>();
    private final MarkovMoveSelector<GridDiagram, GridMove> moveSelector = new GridMoveSelector();

    public CanonicalGridAlgorithm(double z) {
        this.z = z;
    }

    @Override
    public GridDiagram deepCopy(GridDiagram originalGridDiagram) {
        return originalGridDiagram.copy();
    }

    @Override
    public double getAcceptanceProbability(GridMove move) {
        int n = move.getGridFromBeforeMove().getSize();
        int delta;
        switch (move.getMoveType()) {
            case GridDiagram.MOVETYPE_NONE:
            case GridDiagram.MOVETYPE_COMMUTATION:
                delta = 0;
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                delta = -1;
                break;
            case GridDiagram.MOVETYPE_STABILIZATION:
                delta = 1;
                break;
            default:
                System.err.println("Error in CanonicalGridAlgorithm: getAcceptanceProbability");
                return -1;
        }
        while (probabilities.size() < n + 1) {
            probabilities.add(new Double[]{1.0, Math.min(4 * z / probabilities.size(), 1.0), Math.min((probabilities.size() - 1) / (4 * z), 1.0)});
        }
        return probabilities.get(n)[(delta + 3) % 3];
    }

    @Override
    public MarkovMoveSelector<GridDiagram, GridMove> getMoveSelector() {
        return moveSelector;
    }


}
