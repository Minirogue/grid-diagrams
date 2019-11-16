package griddiagrams.markovchain;

import griddiagrams.GridDiagram;
import markovchain.MarkovMove;

public class GridMove implements MarkovMove<GridDiagram> {

    private static final int[] stabSubTypes = new int[]{GridDiagram.INSERT_XO_COLUMN, GridDiagram.INSERT_OX_COLUMN, GridDiagram.INSERT_XO_ROW, GridDiagram.INSERT_OX_ROW};

    private final GridDiagram initialGrid;
    private int moveType;
    private int moveSubType;
    private final int rowOrColumnIndex;
    private int insertedLocation;
    private final int[] arguments;

    public GridMove(GridDiagram initialGrid, int moveType, int fourTimesRowColIndex, int insertedLocation) {
        this.initialGrid = initialGrid;
        this.moveType = moveType;
        this.rowOrColumnIndex = fourTimesRowColIndex / 4;
        switch (moveType) {
            case GridDiagram.MOVETYPE_DESTABILIZATION:
            case GridDiagram.MOVETYPE_COMMUTATION:
                this.moveSubType = fourTimesRowColIndex % 2 == 0 ? GridDiagram.MOVE_SUBTYPE_ROW : GridDiagram.MOVE_SUBTYPE_COLUMN;
                this.arguments = new int[]{rowOrColumnIndex, moveSubType};
                break;
            case GridDiagram.MOVETYPE_STABILIZATION:
                this.moveSubType = stabSubTypes[fourTimesRowColIndex % 4];
                this.insertedLocation = insertedLocation;
                this.arguments = new int[]{rowOrColumnIndex, insertedLocation, moveSubType};
                break;
            case GridDiagram.MOVETYPE_NONE:
            default:
                this.arguments = new int[]{};
                break;
        }
        if (!isValid()) {
            this.moveType = GridDiagram.MOVETYPE_NONE;
        }

    }

    private boolean isValid() {
        switch (moveType) {
            case GridDiagram.MOVETYPE_COMMUTATION:
                if (moveSubType == GridDiagram.MOVE_SUBTYPE_ROW) {
                    return initialGrid.isCommuteRowValid(rowOrColumnIndex);
                } else if (moveSubType == GridDiagram.MOVE_SUBTYPE_COLUMN) {
                    return initialGrid.isCommuteColValid(rowOrColumnIndex);
                }
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                if (moveSubType == GridDiagram.MOVE_SUBTYPE_ROW) {
                    return initialGrid.isDestabilizeRowValid(rowOrColumnIndex);
                } else if (moveSubType == GridDiagram.MOVE_SUBTYPE_COLUMN) {
                    return initialGrid.isDestabilizeColValid(rowOrColumnIndex);
                }
                break;
            case GridDiagram.MOVETYPE_NONE:
            case GridDiagram.MOVETYPE_STABILIZATION:
                return true;
            default:
                return false;
        }
        System.err.println("Error in GridMove.isValid()");
        return false;
    }

    @Override
    public GridDiagram perform() {
        //This implementation does not check the validity of each move, as that is taken care of when the constructor calls isValid()
        switch (moveType) {
            case GridDiagram.MOVETYPE_NONE:
                break;
            case GridDiagram.MOVETYPE_COMMUTATION:
                if (moveSubType == GridDiagram.MOVE_SUBTYPE_ROW) {
                    initialGrid.commuteRow(rowOrColumnIndex);
                } else if (moveSubType == GridDiagram.MOVE_SUBTYPE_COLUMN) {
                    initialGrid.commuteCol(rowOrColumnIndex);
                }
                break;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                if (moveSubType == GridDiagram.MOVE_SUBTYPE_ROW) {
                    initialGrid.destabilizeRow(rowOrColumnIndex);
                } else if (moveSubType == GridDiagram.MOVE_SUBTYPE_COLUMN) {
                    initialGrid.destabilizeCol(rowOrColumnIndex);
                }
                break;
            case GridDiagram.MOVETYPE_STABILIZATION:
                switch (moveSubType) {
                    case GridDiagram.INSERT_XO_COLUMN:
                    case GridDiagram.INSERT_OX_COLUMN:
                        initialGrid.stabilize(rowOrColumnIndex, insertedLocation, moveSubType);
                        break;
                    case GridDiagram.INSERT_XO_ROW:
                    case GridDiagram.INSERT_OX_ROW:
                        initialGrid.stabilize(insertedLocation, rowOrColumnIndex, moveSubType);
                        break;
                }
                break;
        }
        return initialGrid;
    }

    public int getMoveType() {
        return moveType;
    }

    public int[] getMoveArguments() {
        return arguments;
    }

    public GridDiagram getGridFromBeforeMove() {
        return initialGrid;
    }
}
