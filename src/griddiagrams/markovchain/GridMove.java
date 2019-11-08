package griddiagrams.markovchain;

import griddiagrams.GridDiagram;
import markovchain.MarkovMove;

public class GridMove implements MarkovMove<GridDiagram> {

    private GridDiagram startingDiagram;
    private int moveType;
    private int fourTimesVertex;
    private int insertedVertex;

    public GridMove(GridDiagram startingDiagram, int moveType, int fourTimesVertex, int insertedVertex) {
        this.startingDiagram = startingDiagram;
        this.moveType = moveType;
        this.fourTimesVertex = fourTimesVertex;
        this.insertedVertex = insertedVertex;
    }


    @Override
    public GridDiagram perform() {
        switch (moveType) {
            case GridDiagram.MOVETYPE_COMMUTATION:
                if (fourTimesVertex % 2 == 0) {
                    startingDiagram.commuteRowIfValid(fourTimesVertex / 4);
                } else {
                    startingDiagram.commuteColIfValid(fourTimesVertex / 4);
                }
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                if (fourTimesVertex % 2 == 0 && startingDiagram.isDestabilizeRowValid(fourTimesVertex / 4)) {
                    startingDiagram.destabilizeRow(fourTimesVertex / 4);
                } else if (fourTimesVertex % 2 == 1 && startingDiagram.isDestabilizeColValid(fourTimesVertex / 4)) {
                    startingDiagram.destabilizeCol(fourTimesVertex / 4);
                }
                break;
            case GridDiagram.MOVETYPE_STABILIZATION:
                switch (fourTimesVertex % 4) {
                    case GridDiagram.INSERT_XO_COLUMN:
                    case GridDiagram.INSERT_OX_COLUMN:
                        startingDiagram.stabilize(fourTimesVertex / 4, insertedVertex, fourTimesVertex % 4);
                        break;
                    case GridDiagram.INSERT_XO_ROW:
                    case GridDiagram.INSERT_OX_ROW:
                        startingDiagram.stabilize(insertedVertex, fourTimesVertex / 4, fourTimesVertex % 4);
                        break;
                }
                break;
        }
        return startingDiagram;
    }

    @Override
    public Object[] getMoveData() {
        return new Integer[]{moveType, fourTimesVertex, insertedVertex};
    }

    @Override
    public GridDiagram getStartingState() {
        return startingDiagram;
    }
}
