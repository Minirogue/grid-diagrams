package griddiagrams.markovchain.canonicalalgorithm;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMoveSelector;
import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;
import markovchain.metropolishastings.MetropolisHastingsMarkovChain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CanonicalGridAlgorithm extends MetropolisHastingsMarkovChain<GridDiagram> {

    private double z = 1;
    private List<Double[]> probabilities = new ArrayList<>();
    private MarkovMoveSelector<GridDiagram> moveSelector = new GridMoveSelector();

    public CanonicalGridAlgorithm(double z) {
        this.z = z;
    }

    @Override
    public GridDiagram copy(GridDiagram originalGridDiagram) {
        //This method uses the technique of serializing and deserializing from http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
        GridDiagram newGridDiagram = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(originalGridDiagram);
            out.flush();
            out.close();
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            newGridDiagram = (GridDiagram) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return newGridDiagram;
    }

    @Override
    public double getAcceptanceProbability(GridDiagram gridDiagram, MarkovMove<GridDiagram> move) {
        int n = gridDiagram.getSize();
        int delta;
        switch ((Integer) move.getMoveData()[0]) {
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
    public MarkovMoveSelector<GridDiagram> getMoveSelector() {
        return moveSelector;
    }


}
