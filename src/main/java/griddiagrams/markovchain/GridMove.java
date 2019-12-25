package griddiagrams.markovchain;

import griddiagrams.GridDiagram;
import markovchain.MarkovMove;

public class GridMove implements MarkovMove<GridDiagram> {

    private static final int[] stabSubTypes = new int[]{GridDiagram.INSERT_XO_COLUMN, GridDiagram.INSERT_OX_COLUMN, GridDiagram.INSERT_XO_ROW, GridDiagram.INSERT_OX_ROW};

    private final GridDiagram initialGrid;
    private int moveType; // Stabilization, destabilization or commutation defined by the MOVETYPE constants in GridDiagram
    private int moveSubType; // For destabilizations and commutations, this says whether rowOrColumnIndex points to a row or a column. For stabilizations it also determines the order of the new entries.
    private final int rowOrColumnIndex; // The row/column index of the move
    private int insertedLocation; // The grid line of insertion for stabilizations (not used for destabilizations and commutations).
    private final int[] arguments;// The arguments that are used to calculate energy changes.

    /**
     * Main constructor for GridMove.
     *
     * @param initialGrid          The grid on which the move is being performed.
     * @param moveType             The GridDiagram.MOVETYPE constant associated to this move
     * @param fourTimesRowColIndex Must be 0 <= fourTimesRowColIndex < 4*initialGrid.getSize(). This defines where the move happens, and the order of the inserted entries in stabilizations.
     * @param insertedLocation     Only used for stabilizations. This is where the insertion takes place.
     */
    GridMove(GridDiagram initialGrid, int moveType, int fourTimesRowColIndex, int insertedLocation) {
        this.initialGrid = initialGrid;
        this.moveType = moveType;
        this.rowOrColumnIndex = fourTimesRowColIndex / 4;
        if (fourTimesRowColIndex < 0 || this.rowOrColumnIndex >= initialGrid.getSize()) {
            throw new IllegalArgumentException("GridMove constructor received an out of bounds index. Ensure that 0 <= fourTimesRowColIndex < 4*grid size");
        }
        switch (moveType) {
            case GridDiagram.MOVETYPE_DESTABILIZATION:
            case GridDiagram.MOVETYPE_COMMUTATION:
                this.moveSubType = fourTimesRowColIndex % 2 == 0 ? GridDiagram.MOVE_SUBTYPE_ROW : GridDiagram.MOVE_SUBTYPE_COLUMN;
                this.arguments = new int[]{rowOrColumnIndex, moveSubType};
                break;
            case GridDiagram.MOVETYPE_STABILIZATION:
                if (insertedLocation < 0 || insertedLocation > initialGrid.getSize()) {
                    throw new IllegalArgumentException("GridMove constructor received an invalid insertedLocation. Ensure that 0 <= insertedLocation <= grid size.");
                }
                this.insertedLocation = insertedLocation;
                this.moveSubType = stabSubTypes[fourTimesRowColIndex % 4];
                this.arguments = new int[]{rowOrColumnIndex, insertedLocation, moveSubType};
                break;
            default:
                throw new IllegalArgumentException("GridMove constructor received invalid moveType. Use the static final MOVETYPE fields from GridDiagram.");
            case GridDiagram.MOVETYPE_NONE:
                this.arguments = new int[]{};
                break;
        }
        if (!isValid()) {
            this.moveType = GridDiagram.MOVETYPE_NONE;
        }
    }

    /**
     * Checks to see if this move is actually a valid move.
     *
     * @return True if performing the move is a valid move. False if it is a commutation between interleaved rows/columns, or if it is a destabilization in a row/column where the entries are not adjacent.
     */
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

    /**
     * @return The move type which is equivalent to one of the GridDiagram.MOVETYPE constants.
     */
    public int getMoveType() {
        return moveType;
    }

    /**
     * Destabilizations and commutations are defined by their row/column index.
     * Whether a row or column is being referenced is determined by moveSubType which is a GridDiagram constant.
     * If the move is a stabilization, then it is defined by a different subtype declaring the order of the inserted entries as well as row/column, and also by the index of the grid line where the insertion is to take place.
     *
     * @return If the move is a destabilization or a commutation, then [rowOrColumnIndex, moveSubType]. If the move is a stabilization, then [rowOrColumnIndex, insertedLocation, moveSubType].
     */
    public int[] getMoveArguments() {
        return arguments;
    }

    /**
     * @return The GridDiagram before the move is performed on it.
     */
    public GridDiagram getGridFromBeforeMove() {
        return initialGrid;
    }
}
