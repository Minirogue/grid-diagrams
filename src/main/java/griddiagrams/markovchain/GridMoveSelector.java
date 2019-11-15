package griddiagrams.markovchain;

import griddiagrams.GridDiagram;
import markovchain.MarkovMove;
import markovchain.MarkovMoveSelector;

public class GridMoveSelector implements MarkovMoveSelector<GridDiagram, GridMove> {
    @Override
    public GridMove getRandomMove(GridDiagram gridDiagram) {
        int moveType = (int) (Math.random() * (3));
        int vertex = (int) (Math.random() * gridDiagram.getSize() * 4);
        int insertedVertex = 0;
        if (moveType == GridDiagram.MOVETYPE_STABILIZATION) {
            insertedVertex = (int) (Math.random() * (gridDiagram.getSize() + 1));
        }
        return new GridMove(gridDiagram, moveType, vertex, insertedVertex);
    }
}
