package griddiagrams.markovchain;

import griddiagrams.GridDiagram;
import markovchain.MarkovMoveSelector;

/**
 * GridMoveSelector selects a move by first randomly selecting from stabilization, destabilization, and commutation.
 * If stabilization is selected, then a row/column is selected randomly from the 2*gridSize choices, a grid line is selected from the gridSize+1 choices, and the order of the inserted X and O is selected randomly from the 2 choices.
 * If destabilization is selected, then a row/column is selected randomly from the 2*gridSize choices.
 * If commutation is selected, then a row/column is selected randomly from the 2*gridSize choices.
 */
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
