package griddiagrams;//package grid_tools;

import griddiagrams.markovchain.GridMove;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Class for grid diagram representations of links including
 * methods for transforming them and performing calculations on them
 */
public class GridDiagram implements Serializable {
    public static final long serialVersionUID = 0;
    public static final String GRIDS_HASHMAP_SER = "grids_hashmap.ser";


    //Move subtypes for stabilization
    public static final int INSERT_XO_COLUMN = 0;
    public static final int INSERT_OX_COLUMN = 1;
    public static final int INSERT_XO_ROW = 2;
    public static final int INSERT_OX_ROW = 3;

    //Move types
    public static final int MOVETYPE_NONE = -1;
    public static final int MOVETYPE_STABILIZATION = 1;
    public static final int MOVETYPE_DESTABILIZATION = 2;
    public static final int MOVETYPE_COMMUTATION = 0;

    //Move subtypes for commutation and destabilization
    public static final int MOVE_SUBTYPE_COLUMN = 1;
    public static final int MOVE_SUBTYPE_ROW = 2;

    /* The actual grid is stored as a list of rows and a list of columns. Only one of these lists is necessary,
     *  but using both cuts down on a lot of unnecessary computations.
     */
    private ArrayList<Row> rows;
    private ArrayList<Column> cols;
    //The size of the grid is stored and updated as necessary.
    private int size;


    /**
     * Takes indices of X's and O's in both rows and columns to create a GridDiagram.
     *
     * @param initXCol The ith entry corresponds to which row holds the X in the ith column.
     * @param initOCol The ith entry corresponds to which row holds the O in the ith column.
     * @param initXRow The ith entry corresponds to which column holds the X in the ith row.
     * @param initORow The ith entry corresponds to which column holds the O in the ith row.
     */
    public GridDiagram(ArrayList<Integer> initXCol, ArrayList<Integer> initOCol, ArrayList<Integer> initXRow, ArrayList<Integer> initORow) {
        rows = new ArrayList<>();
        cols = new ArrayList<>();
        for (int i = 0; i < initXCol.size(); i++) {
            cols.add(new Column(initXCol.get(i), initOCol.get(i)));
            rows.add(new Row(initXRow.get(i), initORow.get(i)));
        }
        this.size = initXCol.size();
    }

    /**
     * Takes indices of X's and O's in columns to create a GridDiagram.
     *
     * @param initXCol The ith entry corresponds to which row holds the X in the ith column.
     * @param initOCol The ith entry corresponds to which row holds the O in the ith column.
     */
    public GridDiagram(int[] initXCol, int[] initOCol) {
        int[] initXRow = new int[initXCol.length];
        int[] initORow = new int[initOCol.length];
        for (int i = 0; i < initXCol.length; i++) {
            initXRow[initXCol[i]] = i;
            initORow[initOCol[i]] = i;
        }
        rows = new ArrayList<>();
        cols = new ArrayList<>();
        for (int i = 0; i < initXCol.length; i++) {
            cols.add(new Column(initXCol[i], initOCol[i]));
            rows.add(new Row(initXRow[i], initORow[i]));
        }
        this.size = initXCol.length;
    }

    /**
     * @deprecated Use {@link #getGridDiagramFromResource(String)} instead.
     */
    public GridDiagram(String linkname) {
        GridDiagram fromRes = getGridDiagramFromResource(linkname);
        this.rows = fromRes.rows;
        this.cols = fromRes.cols;
        this.size = fromRes.size;
    }

    /**
     * Construct griddiagrams.GridDiagram from the link name (assuming the .grd file is correctly in place)
     *
     * @param linkName The link type of the desired grid diagram
     */
    public static GridDiagram getGridDiagramFromResource(String linkName) {
        try {
            InputStream inFile = GridDiagram.class.getClassLoader().getResourceAsStream(GRIDS_HASHMAP_SER);
            ObjectInputStream inObj = new ObjectInputStream(inFile);
            Object fileObj = inObj.readObject();
            inObj.close();
            if (inFile != null) {
                inFile.close();
            }
            if (fileObj instanceof Map) {
                Map gridMap = (Map) fileObj;
                if (gridMap.containsKey(linkName)) {
                    Object gridObj = gridMap.get(linkName);
                    if (gridObj instanceof int[][]) {
                        int[][] gridArr = (int[][]) gridObj;
                        if (gridArr.length == 2 && gridArr[0].length == gridArr[1].length) {
                            return new GridDiagram(gridArr[0], gridArr[1]);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Link name not found in resources");
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Grid resource file not found");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("File not correctly formatted");
            System.exit(1);
        }
        System.err.println("Uh oh, this line should be unreachable (GridDiagram.getGridDiagramFromResource");
        return new GridDiagram(new int[]{0, 1}, new int[]{1, 0});
    }

    /**
     * @return A new GridDiagram object that is a (deep) copy of this one.
     */
    public GridDiagram copy() {
        //This method uses the technique of serializing and deserializing from http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
        GridDiagram newGridDiagram = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
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

    /**
     * Turns this grid diagram into the connect sum of it and another then returns itself
     */
    public GridDiagram concatenate(GridDiagram g2) {
        //Get the coordinates where the grids' vertices will connect
        int xrow = getCol(size - 1).getXRow();
        int orow = getCol(size - 1).getORow();
        int xcol = g2.getRow(0).getXCol();
        int ocol = g2.getRow(0).getOCol();
        //we remove the last column of this grid as it is deleted and its entries are merged into the connecting vertices
        cols.remove(size - 1);
        for (int i = 0; i < g2.getSize(); i++) {
            Column g2Col = g2.getCol(i);
            Row g2Row = g2.getRow(i);
            cols.add(new Column(g2Col.getXRow() + size - 1, g2Col.getORow() + size - 1));
            if (i > 0) {//we don't want to add the first row to the new grid as its entries are merged into the connecting vertices
                rows.add(new Row(g2Row.getXCol() + size - 1, g2Col.getORow() + size - 1));
            }
        }
        //Now to merge the grids properly
        getRow(xrow).setXCol(xcol + size - 1);
        getRow(orow).setOCol(ocol + size - 1);
        getCol(xcol + size - 1).setXRow(xrow);
        getCol(ocol + size - 1).setORow(orow);
        size = size + g2.getSize() - 1;
        return this;
    }


    /**
     * Converts a grid diagram to its mirror image.then returns itself..
     */
    public GridDiagram mirror() {
        ArrayList<Row> newRows = new ArrayList<>();
        ArrayList<Column> newCol = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            newCol.add(getCol(size - 1 - i));
            newRows.add(new Row(size - getRow(i).getXCol() - 1, size - getRow(i).getOCol() - 1));
        }
        cols = newCol;
        rows = newRows;
        return this;
    }

    /**
     * The size of an n-by-n grid diagram is simply n.
     *
     * @return Size of the grid diagram
     */
    public int getSize() {
        return size;
    }

    /**
     * @param i the column to return, counting from the left starting at 0
     * @return a Column object representing column i
     */
    private Column getCol(int i) {
        return cols.get(i);
    }

    /**
     * @param i the row to return, counting from the top starting at 0
     * @return a Row object representing row i
     */
    public Row getRow(int i) {
        return rows.get(i);
    }


    private ArrayList<Row> getRows() {
        return rows;
    }

    private ArrayList<Column> getCols() {
        return cols;
    }

    /**
     * Prints a representation of the grid to the terminal
     *
     * @deprecated now that toString has been implemented
     */
    public void printToTerminal() {
        //System.out.println("Here is the grid:");
        System.out.println(toString());
    }

    /**
     * @return A text-based graphical representation of the grid diagram (assuming equal character width)
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(size * (size + 1));
        for (int i = 0; i < size; i++) {
            int xloc = rows.get(i).getXCol();
            int oloc = rows.get(i).getOCol();
            str.append('\n');
            for (int j = 0; j < size; j++) {
                if (j == xloc) {
                    str.append('X');
                } else if (j == oloc) {
                    str.append('O');
                } else {
                    str.append('-');
                }
            }
        }
        str.append('\n');
        return str.toString();
    }

    /**
     * Commutes given row with the next row (cyclically). This does not check whether or not they are interleaved.
     *
     * @param rownumber The row to be commuted.
     */
    public void commuteRow(int rownumber) {
        int nextrow = (rownumber + 1) % size;
        Collections.swap(rows, rownumber, nextrow);
        cols.get(rows.get(rownumber).getXCol()).setXRow(rownumber);
        cols.get(rows.get(rownumber).getOCol()).setORow(rownumber);
        cols.get(rows.get(nextrow).getXCol()).setXRow(nextrow);
        cols.get(rows.get(nextrow).getOCol()).setORow(nextrow);
    }

    /**
     * Commutes given column with the next column (cyclically). This does not check whether or not they are interleaved.
     *
     * @param colnumber The column to be commuted.
     */
    public void commuteCol(int colnumber) {
        int nextcol = (colnumber + 1) % size;
        Collections.swap(cols, colnumber, nextcol);
        rows.get(cols.get(colnumber).getXRow()).setXCol(colnumber);
        rows.get(cols.get(colnumber).getORow()).setOCol(colnumber);
        rows.get(cols.get(nextcol).getXRow()).setXCol(nextcol);
        rows.get(cols.get(nextcol).getORow()).setOCol(nextcol);
    }

    /**
     * Checks if the given row and the next one (cyclically) are interleaved or not.
     *
     * @param rownumber The row to check.
     * @return True if the rows are not interleaved (commutation is a valid move). False if they are interleaved (commutation is not a valid move).
     */
    public boolean isCommuteRowValid(int rownumber) {
        Row thisRow = rows.get(rownumber);
        Row nextRow = rows.get((rownumber + 1) % size);
        int maxThisRow, minThisRow, maxNextRow, minNextRow;
        if (thisRow.getXCol() > thisRow.getOCol()) {
            maxThisRow = thisRow.getXCol();
            minThisRow = thisRow.getOCol();
        } else {
            maxThisRow = thisRow.getOCol();
            minThisRow = thisRow.getXCol();
        }
        if (nextRow.getXCol() > nextRow.getOCol()) {
            maxNextRow = nextRow.getXCol();
            minNextRow = nextRow.getOCol();
        } else {
            maxNextRow = nextRow.getOCol();
            minNextRow = nextRow.getXCol();
        }
        return !((maxThisRow > maxNextRow && maxNextRow > minThisRow && minThisRow > minNextRow)
                || (maxNextRow > maxThisRow && maxThisRow > minNextRow && minNextRow > minThisRow));
    }

    /**
     * Checks if the given column and the next one (cyclically) are interleaved or not.
     *
     * @param colnumber The column to check.
     * @return True if the columns are not interleaved (commutation is a valid move). False if they are interleaved (commutation is not a valid move).
     */
    public boolean isCommuteColValid(int colnumber) {
        Column thisCol = cols.get(colnumber);
        Column nextCol = cols.get((colnumber + 1) % size);
        int maxThisCol, minThisCol, maxNextCol, minNextCol;
        if (thisCol.getXRow() > thisCol.getORow()) {
            maxThisCol = thisCol.getXRow();
            minThisCol = thisCol.getORow();
        } else {
            maxThisCol = thisCol.getORow();
            minThisCol = thisCol.getXRow();
        }
        if (nextCol.getXRow() > nextCol.getORow()) {
            maxNextCol = nextCol.getXRow();
            minNextCol = nextCol.getORow();
        } else {
            maxNextCol = nextCol.getORow();
            minNextCol = nextCol.getXRow();
        }
        return !((maxThisCol > maxNextCol && maxNextCol > minThisCol && minThisCol > minNextCol)
                || (maxNextCol > maxThisCol && maxThisCol > minNextCol && minNextCol > minThisCol));
    }

    /**
     * Checks whether the given row and the next one (cyclically) are interleaved or not.
     * If they are not, then they are commuted.
     *
     * @param row The row to check/commute.
     * @return True if commutation occurred, otherwise false.
     */
    public boolean commuteRowIfValid(int row) {
        if (isCommuteRowValid(row)) {
            commuteRow(row);
            return true;
        }
        return false;
    }

    /**
     * Checks whether the given column and the next one (cyclically) are interleaved or not.
     * If they are not, then they are commuted.
     *
     * @param col The column to check/commute.
     * @return True if commutation occurred, otherwise false.
     */
    public boolean commuteColIfValid(int col) {
        if (isCommuteColValid(col)) {
            commuteCol(col);
            return true;
        }
        return false;
    }

    /**
     * Performs a translation (aka cyclic permutation).
     *
     * @param horizontal Number of units to translate to the right.
     * @param vertical   Number of units to translate down.
     */
    public void translate(int horizontal, int vertical) {
        ArrayList<Row> newRows = new ArrayList<>();
        ArrayList<Column> newCols = new ArrayList<>();
        Row oldRow;
        Column oldCol;
        for (int i = 0; i < size; i++) {
            oldRow = rows.get((i - vertical + size) % size);
            oldCol = cols.get((i - horizontal + size) % size);
            newRows.add(new Row((oldRow.getXCol() + horizontal) % size, (oldRow.getOCol() + horizontal) % size));
            newCols.add(new Column((oldCol.getXRow() + vertical) % size, (oldCol.getORow() + vertical) % size));
        }
        rows = newRows;
        cols = newCols;
    }

    /**
     * Perform a stabilization move.
     *
     * @param rowNumber The row/horizontal grid line where the move will be performed.
     * @param colNumber The column/vertical grid line where the move will be performed.
     * @param type      One of the constants INSERT_XO_ROW, INSERT_OX_ROW, INSERT_XO_COLUMN, or INSERT_OX_COLUMN, which determines the order and direction of the inserted entries.
     */
    public void stabilize(int rowNumber, int colNumber, int type) {
        Row thisRow;
        for (int i = rowNumber; i < size; i++) {
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i + 1);
            cols.get(thisRow.getOCol()).setORow(i + 1);
        }
        Row newRow = new Row(-1, -1);
        rows.add(rowNumber, newRow);
        Column thisCol;
        for (int j = colNumber; j < size; j++) {
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j + 1);
            rows.get(thisCol.getORow()).setOCol(j + 1);
        }
        Column newCol = new Column(-1, -1);
        cols.add(colNumber, newCol);
        //size++;
        //printToTerminal();
        switch (type) {
            case INSERT_XO_COLUMN:
                thisRow = rows.get(rowNumber + 1);
                thisCol = cols.get(thisRow.getOCol());
                newCol.setXRow(rowNumber);
                newCol.setORow(rowNumber + 1);
                thisCol.setORow(rowNumber);
                newRow.setXCol(colNumber);
                newRow.setOCol(thisRow.getOCol());
                thisRow.setOCol(colNumber);
                break;
            case INSERT_OX_COLUMN:
                thisRow = rows.get(rowNumber + 1);
                thisCol = cols.get(thisRow.getXCol());
                newCol.setXRow(rowNumber + 1);
                newCol.setORow(rowNumber);
                thisCol.setXRow(rowNumber);
                newRow.setXCol(thisRow.getXCol());
                newRow.setOCol(colNumber);
                thisRow.setXCol(colNumber);
                break;
            case INSERT_XO_ROW:
                thisCol = cols.get(colNumber + 1);
                thisRow = rows.get(thisCol.getORow());
                newRow.setXCol(colNumber);
                newRow.setOCol(colNumber + 1);
                thisRow.setOCol(colNumber);
                newCol.setXRow(rowNumber);
                newCol.setORow(thisCol.getORow());
                thisCol.setORow(rowNumber);
                break;
            case INSERT_OX_ROW:
                thisCol = cols.get(colNumber + 1);
                thisRow = rows.get(thisCol.getXRow());
                newRow.setXCol(colNumber + 1);
                newRow.setOCol(colNumber);
                thisRow.setXCol(colNumber);
                newCol.setXRow(thisCol.getXRow());
                newCol.setORow(rowNumber);
                thisCol.setXRow(rowNumber);
                break;
            default:
                break;
        }
        size++;
    }

    /**
     * Perform a destabilization in the given column.
     * This does not check to make sure that the destabilization is valid.
     *
     * @param colnumber The column in which the destabilization will take place.
     */
    public void destabilizeCol(int colnumber) {
        Column thisCol;
        Row thisRow;

        thisCol = cols.get(colnumber);
        int rowX = thisCol.getXRow();
        int rowO = thisCol.getORow();
        int largerRow = Math.max(rowX, rowO);
        int savedXCol = rows.get(rowO).getXCol();
        int savedOCol = rows.get(rowX).getOCol();

        thisRow = rows.get(largerRow - 1);
        thisRow.setXCol(savedXCol);
        thisRow.setOCol(savedOCol);
        cols.get(savedXCol).setXRow(largerRow - 1);
        cols.get(savedOCol).setORow(largerRow - 1);

        for (int j = colnumber + 1; j < size; j++) {
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j - 1);
            rows.get(thisCol.getORow()).setOCol(j - 1);
        }

        cols.remove(colnumber);

        for (int i = largerRow + 1; i < size; i++) {
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i - 1);
            cols.get(thisRow.getOCol()).setORow(i - 1);
        }

        rows.remove(largerRow);
        size--;
    }

    /**
     * Perform a destabilization in the given row.
     * This does not check to make sure that the destabilization is valid.
     *
     * @param rownumber The row in which the destabilization will take place.
     */
    public void destabilizeRow(int rownumber) {
        Row thisRow;
        Column thisCol;

        thisRow = rows.get(rownumber);
        int colX = thisRow.getXCol();
        int colO = thisRow.getOCol();
        int largercolumn = Math.max(colO, colX);
        int savedXRow = cols.get(colO).getXRow();
        int savedORow = cols.get(colX).getORow();

        thisCol = cols.get(largercolumn - 1);
        thisCol.setXRow(savedXRow);
        thisCol.setORow(savedORow);
        rows.get(savedXRow).setXCol(largercolumn - 1);
        rows.get(savedORow).setOCol(largercolumn - 1);

        for (int i = rownumber + 1; i < size; i++) {
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i - 1);
            cols.get(thisRow.getOCol()).setORow(i - 1);
        }

        rows.remove(rownumber);

        for (int j = largercolumn + 1; j < size; j++) {
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j - 1);
            rows.get(thisCol.getORow()).setOCol(j - 1);
        }

        cols.remove(largercolumn);
        size--;
    }

    /**
     * Checks to see if destabilizing in the given row is valid.
     *
     * @param row The row of the proposed destabilization.
     * @return True if the entries in the row are adjacent (valid destabilization). False otherwise.
     */
    public boolean isDestabilizeRowValid(int row) {
        Row thisRow = rows.get(row);
        return thisRow.getMaxCol() - thisRow.getMinCol() == 1 && size > 2;
    }

    /**
     * Checks to see if destabilizing in the given column is valid.
     *
     * @param col The column of the proposed destabilization.
     * @return True if the entries in the column are adjacent (valid destabilization). False otherwise.
     */
    public boolean isDestabilizeColValid(int col) {
        Column thisCol = cols.get(col);
        return thisCol.getMaxRow() - thisCol.getMinRow() == 1 && size > 2;
    }

    /**
     * Performs elementary destabilization at the given index if it is adjacent both vertically and horizontally to other entries.
     *
     * @param row Row index of entry to be destabilized.
     * @param col Column index of entry to be destabilized.
     * @return True if destabilization occurred.
     */
    public boolean destabilizeIfValid(int row, int col) {
        if (isDestabilizeColValid(col)) {
            //System.out.println("column destabilizable");
            destabilizeCol(col);
            return true;
        }
        if (isDestabilizeRowValid(row)) {
            //System.out.println("row destabilizable");
            destabilizeRow(row);
            return true;
        }
        return false;
    }

    /**
     * Checks to make sure the rows and column entries match each other.
     * Useful for debugging.
     *
     * @return True if there are no issues with the entries. False if they don't match.
     */
    public boolean isRowMatchColumns() {
        for (int i = 0; i < size; i++) {
            if (rows.get(i).getXCol() == size) {
                System.out.println("row " + i + " is a problem at X");
            } else if (rows.get(i).getOCol() == size) {
                System.out.println("row " + i + " is a problem at O");
            } else if (cols.get(i).getXRow() == size) {
                System.out.println("col " + i + " is a problem at X");
            } else if (cols.get(i).getORow() == size) {
                System.out.println("col " + i + " is a problem at O");
            }
            if (cols.get(rows.get(i).getXCol()).getXRow() != i || cols.get(rows.get(i).getOCol()).getORow() != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate projected writhe.
     *
     * @return The projected writhe of this grid diagram.
     */
    public int calcWrithe() {
        int totalwrithe = 0;
        Row thisRow;
        Column thisCol;
        for (int i = 1; i < size - 1; i++) {
            thisRow = rows.get(i);
            for (int j = Math.min(thisRow.getXCol(), thisRow.getOCol()) + 1; j < Math.max(thisRow.getXCol(), thisRow.getOCol()); j++) {
                thisCol = cols.get(j);
                if (Math.min(thisCol.getXRow(), thisCol.getORow()) < i && i < Math.max(thisCol.getXRow(), thisCol.getORow())) {
                    totalwrithe += -(thisCol.getXRow() > thisCol.getORow() ? 1 : -1) * (thisRow.getXCol() > thisRow.getOCol() ? 1 : -1);
                }
            }
        }
        return totalwrithe;
    }

    /**
     * Calculates a number that can be added to the current projected writhe to get the projected writhe of the grid diagram resulting from the given move.
     *
     * @param movetype  One of MOVETYPE_COMMUTATION, MOVETYPE_STABILIZATION, or MOVETYPE_DESTABILIZATION.
     * @param arguments The parameters of the given move. This should match the return of {@link GridMove#getMoveArguments()}
     * @return The change in writhe that will occur from the given move.
     */
    public int deltaWrithe(int movetype, int[] arguments) {
        //System.out.println("deltawrithe called");
        int delta = 0;
        switch (movetype) {
            case MOVETYPE_NONE:
                return 0;
            case MOVETYPE_COMMUTATION:
                //System.out.println("commutation");
                return commuteDeltaWrithe(arguments);
            case MOVETYPE_DESTABILIZATION:
                //System.out.println("destab");
                return destabilizeDeltaWrithe(arguments);
            case MOVETYPE_STABILIZATION:
                return stabilizeDeltaWrithe(arguments);
        }
        return delta;
    }

    /**
     * Calculates the change in writhe from performing the given stabilization.
     *
     * @param arguments The arguments of the stabilization. Should match the return of {@link GridMove#getMoveArguments()}
     * @return The change in writhe from performing the given stabilization.
     */
    private int stabilizeDeltaWrithe(int[] arguments) {
        Row thisRow;
        Column thisCol;
        int leftOrRight = 0;
        int upOrDown = 0;
        int xAndO = 0;
        switch (arguments[2]) {
            case INSERT_OX_COLUMN:
            case INSERT_XO_COLUMN:
                thisRow = rows.get(arguments[0]);
                if (arguments[1] <= thisRow.getMinCol()) {
                    leftOrRight = -1;
                    thisCol = cols.get(thisRow.getMinCol());
                } else if (thisRow.getMaxCol() < arguments[1]) {
                    leftOrRight = 1;
                    thisCol = cols.get(thisRow.getMaxCol());
                } else {
                    return 0;
                }
                xAndO = (arguments[2] == INSERT_OX_COLUMN ? thisRow.getDirection() : -thisRow.getDirection());
                upOrDown = (thisCol.getMinRow() == arguments[0] ? 1 : -1);
                break;
            case INSERT_OX_ROW:
            case INSERT_XO_ROW:
                thisCol = cols.get(arguments[0]);
                if (arguments[1] <= thisCol.getMinRow()) {
                    upOrDown = -1;
                    thisRow = rows.get(thisCol.getMinRow());
                } else if (thisCol.getMaxRow() < arguments[1]) {
                    upOrDown = 1;
                    thisRow = rows.get(thisCol.getMaxRow());
                } else {
                    return 0;
                }
                xAndO = (arguments[2] == INSERT_OX_ROW ? -thisCol.getDirection() : thisCol.getDirection());
                leftOrRight = (thisRow.getMinCol() == arguments[0] ? 1 : -1);
                break;
        }
        if (leftOrRight * upOrDown == xAndO) {
            return xAndO;
        }
        return 0;
    }

    /**
     * Calculates the change in writhe from performing the given destabilization.
     *
     * @param arguments The arguments of the destabilization. Should match the return of {@link GridMove#getMoveArguments()}
     * @return The change in writhe from performing the given destabilization.
     */
    private int destabilizeDeltaWrithe(int[] arguments) {
        if (arguments[1] == MOVE_SUBTYPE_ROW) {
            Row thisRow;
            thisRow = rows.get(arguments[0]);
            Column xcol = cols.get(thisRow.getXCol());
            Column ocol = cols.get(thisRow.getOCol());
            if (xcol.getDirection() == ocol.getDirection()) {
                return 0;
            } else {
                Row crossingRow;
                if (xcol.getMaxRow() - xcol.getMinRow() < ocol.getMaxRow() - ocol.getMinRow()) {
                    crossingRow = rows.get(xcol.getORow());
                    if (crossingRow.getMinCol() < thisRow.getOCol() && thisRow.getOCol() < crossingRow.getMaxCol()) {
                        return -crossingRow.getDirection() * ocol.getDirection();
                    } else {
                        return 0;
                    }
                } else {
                    crossingRow = rows.get(ocol.getXRow());
                    if (crossingRow.getMinCol() < thisRow.getXCol() && thisRow.getXCol() < crossingRow.getMaxCol()) {
                        return -crossingRow.getDirection() * xcol.getDirection();
                    } else {
                        return 0;
                    }
                }
            }
        } else if (arguments[1] == MOVE_SUBTYPE_COLUMN) {
            Column thisCol;
            thisCol = cols.get(arguments[0]);
            Row xRow = rows.get(thisCol.getXRow());
            Row oRow = rows.get(thisCol.getORow());
            if (xRow.getDirection() == oRow.getDirection()) {
                return 0;
            } else {
                Column crossingCol;
                if (xRow.getMaxCol() - xRow.getMinCol() < oRow.getMaxCol() - oRow.getMinCol()) {
                    crossingCol = cols.get(xRow.getOCol());
                    if (crossingCol.getMinRow() < thisCol.getORow() && thisCol.getORow() < crossingCol.getMaxRow()) {
                        return -crossingCol.getDirection() * oRow.getDirection();
                    } else {
                        return 0;
                    }
                } else {
                    crossingCol = cols.get(oRow.getXCol());
                    if (crossingCol.getMinRow() < thisCol.getXRow() && thisCol.getXRow() < crossingCol.getMaxRow()) {
                        return -crossingCol.getDirection() * xRow.getDirection();
                    } else {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * @return A pair of integer arrays which can be used with {@link #GridDiagram(int[], int[])} to recreate this grid diagram.
     */
    public int[][] getSavableGrid() {
        int[] xCol = new int[getSize()];
        int[] oCol = new int[getSize()];
        Column thisCol;
        for (int i = 0; i < getSize(); i++) {
            thisCol = cols.get(i);
            xCol[i] = thisCol.getXRow();
            oCol[i] = thisCol.getORow();
        }
        return new int[][]{xCol, oCol};
    }

    /**
     * @param arguments An integer array where arguments[1] is MOVE_SUBTYPE_COLUMN or MOVE_SUBTYPE_ROW and arguments[0] is the row/column where the move is being performed
     * @return The amount that the writhe will change if the proposed move is performed
     */
    private int commuteDeltaWrithe(int[] arguments) {
        int delta = 0;
        if (arguments[1] == MOVE_SUBTYPE_COLUMN) {
            Column thisCol = cols.get(arguments[0]);
            Column nextCol = cols.get((arguments[0] + 1) % getSize());
            //Replace col with arguments[0]
            if (arguments[0] == getSize() - 1) {
                if (thisCol.getXRow() != nextCol.getORow()) {
                    delta += (cols.get(rows.get(thisCol.getXRow()).getOCol()).getDirection() == thisCol.getDirection() ? thisCol.getDirection() : 0);
                    delta += (cols.get(rows.get(nextCol.getORow()).getXCol()).getDirection() == nextCol.getDirection() ? nextCol.getDirection() : 0);
                } else {
                    if (thisCol.getDirection() == nextCol.getDirection()) {
                        delta += 2 * thisCol.getDirection();
                    } else if (nextCol.getLength() < thisCol.getLength()) {
                        delta += nextCol.getDirection();
                    } else {
                        delta += -nextCol.getDirection();
                    }
                }
                if (thisCol.getORow() != nextCol.getXRow()) {
                    delta += (cols.get(rows.get(thisCol.getORow()).getXCol()).getDirection() == thisCol.getDirection() ? -thisCol.getDirection() : 0);
                    delta += (cols.get(rows.get(nextCol.getXRow()).getOCol()).getDirection() == nextCol.getDirection() ? -nextCol.getDirection() : 0);
                } else {
                    if (thisCol.getDirection() == nextCol.getDirection()) {
                        delta += -2 * thisCol.getDirection();
                    } else if (nextCol.getLength() < thisCol.getLength()) {
                        delta += -nextCol.getDirection();
                    } else {
                        delta += nextCol.getDirection();
                    }
                }
            } else if (thisCol.getDirection() == -nextCol.getDirection()) {
                if (thisCol.getXRow() == nextCol.getORow()) {
                    delta += (thisCol.getLength() < nextCol.getLength() ? thisCol.getDirection() : -thisCol.getDirection());
                }
                if (thisCol.getORow() == nextCol.getXRow()) {
                    delta += (thisCol.getLength() < nextCol.getLength() ? -thisCol.getDirection() : thisCol.getDirection());
                }
            }
        } else if (arguments[1] == MOVE_SUBTYPE_ROW) {
            Row thisRow = rows.get(arguments[0]);
            Row nextRow = rows.get((arguments[0] + 1) % getSize());
            //Replace col with arguments[0]
            if (arguments[0] == getSize() - 1) {
                if (thisRow.getXCol() != nextRow.getOCol()) {
                    delta += (rows.get(cols.get(thisRow.getXCol()).getORow()).getDirection() == thisRow.getDirection() ? -thisRow.getDirection() : 0);
                    delta += (rows.get(cols.get(nextRow.getOCol()).getXRow()).getDirection() == nextRow.getDirection() ? -nextRow.getDirection() : 0);
                } else {
                    if (thisRow.getDirection() == nextRow.getDirection()) {
                        delta += -2 * thisRow.getDirection();
                    } else if (nextRow.getLength() < thisRow.getLength()) {
                        delta += thisRow.getDirection();
                    } else {
                        delta += -thisRow.getDirection();
                    }
                }
                if (thisRow.getOCol() != nextRow.getXCol()) {
                    delta += (rows.get(cols.get(thisRow.getOCol()).getXRow()).getDirection() == thisRow.getDirection() ? thisRow.getDirection() : 0);
                    delta += (rows.get(cols.get(nextRow.getXCol()).getORow()).getDirection() == nextRow.getDirection() ? nextRow.getDirection() : 0);
                } else {
                    if (thisRow.getDirection() == nextRow.getDirection()) {
                        delta += 2 * thisRow.getDirection();
                    } else if (nextRow.getLength() < thisRow.getLength()) {
                        delta += -thisRow.getDirection();
                    } else {
                        delta += thisRow.getDirection();
                    }
                }
            } else if (thisRow.getDirection() == -nextRow.getDirection()) {
                if (thisRow.getXCol() == nextRow.getOCol()) {
                    delta += (thisRow.getLength() < nextRow.getLength() ? -thisRow.getDirection() : thisRow.getDirection());
                }
                if (thisRow.getOCol() == nextRow.getXCol()) {
                    delta += (thisRow.getLength() < nextRow.getLength() ? thisRow.getDirection() : -thisRow.getDirection());
                }
            }
        }
        return delta;
    }

    /**
     * Calculates the change in size from a given move.
     *
     * @param moveType The MOVETYPE_COMMUTATION, MOVETYPE_DESTABILIZATION, or MOVETYPE_STABILIZATION
     * @return The change in size that will result from the move.
     */
    public static int deltaSize(int moveType) {
        switch (moveType) {
            case GridDiagram.MOVETYPE_STABILIZATION:
                return 1;
            case GridDiagram.MOVETYPE_DESTABILIZATION:
                return -1;
            case GridDiagram.MOVETYPE_COMMUTATION:
                return 0;
            default:
                System.out.println("Error with deltaSize: moveType not valid");
                System.exit(1);
        }
        return 999;//This should never get called
    }


    /**
     * Represents the entries in a row of the grid diagram.
     */
    public static class Row implements Serializable {
        public static final long serialVersionUID = 0;

        //The column indices of the entries in this row
        private int xCol;
        private int oCol;

        //The indices again, but as minimum/maximum indices
        private int minCol;
        private int maxCol;

        private int direction;
        private int length;

        Row(int initXCol, int initOCol) {
            xCol = initXCol;
            oCol = initOCol;
            setMinAndMax();
        }

        /**
         * @return The column indices of the X and O in this row as a readable string. Useful for debugging.
         */
        @Override
        public String toString() {
            return "X: " + xCol + " O: " + oCol;
        }

        /**
         * @return The column index of the X in this row
         */
        public int getXCol() {
            return xCol;
        }

        /**
         * @return The column index of the O in this row
         */
        public int getOCol() {
            return oCol;
        }

        /**
         * @return The smaller column index in this row.
         */
        int getMinCol() {
            return minCol;
        }

        /**
         * @return The larger column index in this row.
         */
        int getMaxCol() {
            return maxCol;
        }

        /**
         * @return Positive if edge is oriented to the right, and negative if oriented to the left.
         */
        int getDirection() {
            return direction;
        }

        /**
         * @return The length of the edge in this row.
         */
        int getLength() {
            return length;
        }


        void setXCol(int newXCol) {
            this.xCol = newXCol;
            setMinAndMax();
        }

        void setOCol(int newOCol) {
            this.oCol = newOCol;
            setMinAndMax();
        }

        private void setMinAndMax() {
            if (xCol < oCol) {
                minCol = xCol;
                maxCol = oCol;
            } else {
                minCol = oCol;
                maxCol = xCol;
            }
            length = xCol - oCol;
            direction = (int) Math.signum(length);
            length = length * direction;
        }
    }

    /**
     * Represents the entries of a column in the grid diagram.
     */
    public static class Column implements Serializable {
        public static final long serialVersionUID = 0;

        //The row indices of the X and O in this column
        private int xRow;
        private int oRow;

        //The indices again, but as max/min
        private int minRow;
        private int maxRow;

        private int direction;
        private int length;

        public Column(int initXRow, int initORow) {
            xRow = initXRow;
            oRow = initORow;
            setMaxAndMin();
        }

        /**
         * @return The row indices of the X and O in this column as a readable string. Useful for debugging.
         */
        @Override
        public String toString() {
            return "X: " + xRow + " O: " + oRow;
        }

        /**
         * @return The row index of the X in this column.
         */
        int getXRow() {
            return xRow;
        }

        /**
         * @return The row index of the O in this column.
         */
        int getORow() {
            return oRow;
        }

        /**
         * @return The smaller of the row indices in this column.
         */
        int getMinRow() {
            return minRow;
        }

        /**
         * @return The larger of the row indices in this column.
         */
        int getMaxRow() {
            return maxRow;
        }

        /**
         * @return Positive or negative depending on the orientation of the edge in this column. (I think positive is oriented down, but I need to double check)
         */
        public int getDirection() {
            return direction;
        }

        /**
         * @return The length of the edge in this column.
         */
        public int getLength() {
            return length;
        }

        void setXRow(int newXRow) {
            xRow = newXRow;
            setMaxAndMin();
        }

        void setORow(int newORow) {
            oRow = newORow;
            setMaxAndMin();
        }

        private void setMaxAndMin() {
            if (xRow < oRow) {
                minRow = xRow;
                maxRow = oRow;
            } else {
                minRow = oRow;
                maxRow = xRow;
            }
            length = oRow - xRow;
            direction = (int) Math.signum(length);
            length = length * direction;
        }
    }
}