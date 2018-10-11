//package grid_tools;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class GridDiagram implements Serializable {
    public static final long serialVersionUID = 0;


    public static final int INSERT_XO_COLUMN = 0;
    public static final int INSERT_OX_COLUMN = 1;
    public static final int INSERT_XO_ROW = 2;
    public static final int INSERT_OX_ROW = 3;

    public static final int MOVETYPE_STABILIZATION = 1;
    public static final int MOVETYPE_DESTABILIZATION = 2;
    public static final int MOVETYPE_COMMUTATION = 0;

    public static final int MOVE_SUBTYPE_COLUMN = 1;
    public static final int MOVE_SUBTYPE_ROW = 2;


    private ArrayList<Row> rows;
    private ArrayList<Column> cols;
    private int size;

    public GridDiagram(ArrayList<Integer> initXCol, ArrayList<Integer> initOCol, ArrayList<Integer> initXRow, ArrayList<Integer> initORow){
        rows = new ArrayList<>();
        cols = new ArrayList<>();
        for (int i=0; i<initXCol.size(); i++){
            cols.add(new Column(initXCol.get(i), initOCol.get(i)));
            rows.add(new Row(initXRow.get(i), initORow.get(i)));
        }
        this.size = initXCol.size();
    }
    public GridDiagram(int[] initXCol, int[] initOCol){
        int[] initXRow = new int[initXCol.length];
        int[] initORow = new int[initOCol.length];
        for (int i=0; i<initXCol.length; i++){
            initXRow[initXCol[i]] = i;
            initORow[initOCol[i]] = i;
        }
        rows = new ArrayList<>();
        cols = new ArrayList<>();
        for (int i=0; i<initXCol.length; i++){
            cols.add(new Column(initXCol[i], initOCol[i]));
            rows.add(new Row(initXRow[i], initORow[i]));
        }
        this.size = initXCol.length;
    }
    public GridDiagram(String linkName){
        try{
            InputStream inFile = getClass().getClassLoader().getResourceAsStream("knot_conformations/"+linkName+".grd");
            ObjectInputStream inObj = new ObjectInputStream(inFile);
            GridDiagram diagramFromFile = (GridDiagram)inObj.readObject();
            inObj.close();
            inFile.close();
            rows = diagramFromFile.getRows();
            cols = diagramFromFile.getCols();
            size = diagramFromFile.getSize();
        } catch (FileNotFoundException e){
            System.err.println("File not found");
        } catch (IOException e){
            //System.out.println("Error initializing input stream");
            System.err.println(e);
        } catch (ClassNotFoundException e){
            System.err.println("File not correctly formatted");
        }
    }


    public int getSize(){ return size; }
    public Column getCol(int i){ return cols.get(i); }
    public Row getRow(int i){ return rows.get(i); }


    private ArrayList<Row> getRows(){ return rows; }
    private ArrayList<Column> getCols(){ return cols; }

    public void printToTerminal(){
        //System.out.println("Here is the grid:");
        for (int i=0; i<size; i++){
            int xloc = rows.get(i).getXCol();
            int oloc = rows.get(i).getOCol();
            System.out.print('\n');
            for (int j=0; j<size; j++){
                if (j==xloc){
                    System.out.print('X');
                }else if (j==oloc){
                    System.out.print('O');
                }
                else{
                    System.out.print('-');
                }
            }
        }
        System.out.print('\n');
    }

    public void commuteRow(int rownumber){
        int nextrow = (rownumber+1)%size;
        Collections.swap(rows, rownumber, nextrow);
        cols.get(rows.get(rownumber).getXCol()).setXRow(rownumber);
        cols.get(rows.get(rownumber).getOCol()).setORow(rownumber);
        cols.get(rows.get(nextrow).getXCol()).setXRow(nextrow);
        cols.get(rows.get(nextrow).getOCol()).setORow(nextrow);
    }
    public void commuteCol(int colnumber){
        int nextcol = (colnumber+1)%size;
        Collections.swap(cols, colnumber, nextcol);
        rows.get(cols.get(colnumber).getXRow()).setXCol(colnumber);
        rows.get(cols.get(colnumber).getORow()).setOCol(colnumber);
        rows.get(cols.get(nextcol).getXRow()).setXCol(nextcol);
        rows.get(cols.get(nextcol).getORow()).setOCol(nextcol);
    }
    public boolean isCommuteRowValid(int rownumber){
        Row thisRow = rows.get(rownumber);
        Row nextRow = rows.get((rownumber+1)%size);
        int maxThisRow, minThisRow, maxNextRow, minNextRow;
        if (thisRow.getXCol() > thisRow.getOCol()) {
            maxThisRow = thisRow.getXCol();
            minThisRow = thisRow.getOCol();
        }
        else {
            maxThisRow = thisRow.getOCol();
            minThisRow = thisRow.getXCol();
        }
        if (nextRow.getXCol() > nextRow.getOCol()) {
            maxNextRow = nextRow.getXCol();
            minNextRow = nextRow.getOCol();
        }
		else {
            maxNextRow = nextRow.getOCol();
            minNextRow = nextRow.getXCol();
        }
        return !((maxThisRow > maxNextRow && maxNextRow > minThisRow && minThisRow > minNextRow)
                || (maxNextRow > maxThisRow && maxThisRow > minNextRow && minNextRow > minThisRow));
    }
    public boolean isCommuteColValid(int colnumber){
        Column thisCol = cols.get(colnumber);
        Column nextCol = cols.get((colnumber+1)%size);
        int maxThisCol, minThisCol, maxNextCol, minNextCol;
        if (thisCol.getXRow() > thisCol.getORow()) {
            maxThisCol = thisCol.getXRow();
            minThisCol = thisCol.getORow();
        }
        else {
            maxThisCol = thisCol.getORow();
            minThisCol = thisCol.getXRow();
        }
        if (nextCol.getXRow() > nextCol.getORow()) {
            maxNextCol = nextCol.getXRow();
            minNextCol = nextCol.getORow();
        }
        else {
            maxNextCol = nextCol.getORow();
            minNextCol = nextCol.getXRow();
        }
        return !((maxThisCol > maxNextCol && maxNextCol > minThisCol && minThisCol > minNextCol)
                || (maxNextCol > maxThisCol && maxThisCol > minNextCol && minNextCol > minThisCol));
    }
    public boolean commuteRowIfValid(int row){
        if (isCommuteRowValid(row)){
            commuteRow(row);
            return true;
        }
        return false;
    }
    public boolean commuteColIfValid(int col){
        if (isCommuteColValid(col)){
            commuteCol(col);
            return true;
        }
        return false;
    }

    public void translate(int horizontal, int vertical){
        ArrayList<Row> newRows = new ArrayList<>();
        ArrayList<Column> newCols = new ArrayList<>();
        Row oldRow;
        Column oldCol;
        for (int i=0; i<size; i++){
            oldRow = rows.get((i-vertical+size)%size);
            oldCol = cols.get((i-horizontal+size)%size);
            newRows.add(new Row((oldRow.getXCol()+horizontal)%size, (oldRow.getOCol()+horizontal)%size));
            newCols.add(new Column((oldCol.getXRow()+vertical)%size, (oldCol.getORow()+vertical)%size));
        }
        rows = newRows;
        cols = newCols;
    }

    public void stabilize(int rownumber, int colnumber, int type){
        Row thisRow;
        for (int i=rownumber; i<size; i++){
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i+1);
            cols.get(thisRow.getOCol()).setORow(i+1);
        }
        Row newRow = new Row(-1,-1);
        rows.add(rownumber, newRow);
        Column thisCol;
        for (int j=colnumber; j<size; j++){
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j+1);
            rows.get(thisCol.getORow()).setOCol(j+1);
        }
        Column newCol = new Column(-1, -1);
        cols.add(colnumber, newCol);
        //size++;
        //printToTerminal();
        switch (type){
            case INSERT_XO_COLUMN:
                thisRow = rows.get(rownumber+1);
                thisCol = cols.get(thisRow.getOCol());
                newCol.setXRow(rownumber);
                newCol.setORow(rownumber+1);
                thisCol.setORow(rownumber);
                newRow.setXCol(colnumber);
                newRow.setOCol(thisRow.getOCol());
                thisRow.setOCol(colnumber);
                break;
            case INSERT_OX_COLUMN:
                thisRow = rows.get(rownumber+1);
                thisCol = cols.get(thisRow.getXCol());
                newCol.setXRow(rownumber+1);
                newCol.setORow(rownumber);
                thisCol.setXRow(rownumber);
                newRow.setXCol(thisRow.getXCol());
                newRow.setOCol(colnumber);
                thisRow.setXCol(colnumber);
                break;
            case INSERT_XO_ROW:
                thisCol = cols.get(colnumber+1);
                thisRow = rows.get(thisCol.getORow());
                newRow.setXCol(colnumber);
                newRow.setOCol(colnumber+1);
                thisRow.setOCol(colnumber);
                newCol.setXRow(rownumber);
                newCol.setORow(thisCol.getORow());
                thisCol.setORow(rownumber);
                break;
            case INSERT_OX_ROW:
                thisCol = cols.get(colnumber+1);
                thisRow = rows.get(thisCol.getXRow());
                newRow.setXCol(colnumber+1);
                newRow.setOCol(colnumber);
                thisRow.setXCol(colnumber);
                newCol.setXRow(thisCol.getXRow());
                newCol.setORow(rownumber);
                thisCol.setXRow(rownumber);
                break;
            default:
                break;
        }
        size++;
    }

    public void destabilizeCol(int colnumber){
        Column thisCol;
        Row thisRow;

        thisCol = cols.get(colnumber);
        int rowX = thisCol.getXRow();
        int rowO = thisCol.getORow();
        int largerRow = Math.max(rowX,rowO);
        int savedXCol = rows.get(rowO).getXCol();
        int savedOCol = rows.get(rowX).getOCol();

        thisRow = rows.get(largerRow-1);
        thisRow.setXCol(savedXCol);
        thisRow.setOCol(savedOCol);
        cols.get(savedXCol).setXRow(largerRow-1);
        cols.get(savedOCol).setORow(largerRow-1);
        
        for (int j=colnumber+1; j<size; j++){
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j-1);
            rows.get(thisCol.getORow()).setOCol(j-1);
        }
        
        cols.remove(colnumber);
        
        for (int i=largerRow+1; i<size; i++){
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i-1);
            cols.get(thisRow.getOCol()).setORow(i-1);
        }
        
        rows.remove(largerRow);
        size--;
    }
    public void destabilizeRow(int rownumber){
        Row thisRow;
        Column thisCol;

        thisRow = rows.get(rownumber);
        int colX = thisRow.getXCol();
        int colO = thisRow.getOCol();
        int largercolumn = Math.max(colO,colX);
        int savedXRow = cols.get(colO).getXRow();
        int savedORow = cols.get(colX).getORow();

        thisCol = cols.get(largercolumn-1);
        thisCol.setXRow(savedXRow);
        thisCol.setORow(savedORow);
        rows.get(savedXRow).setXCol(largercolumn-1);
        rows.get(savedORow).setOCol(largercolumn-1);
        
        for (int i=rownumber+1; i<size; i++){
            thisRow = rows.get(i);
            cols.get(thisRow.getXCol()).setXRow(i-1);
            cols.get(thisRow.getOCol()).setORow(i-1);
        }
        
        rows.remove(rownumber);
        
        for (int j=largercolumn+1; j<size; j++){
            thisCol = cols.get(j);
            rows.get(thisCol.getXRow()).setXCol(j-1);
            rows.get(thisCol.getORow()).setOCol(j-1);
        }
        
        cols.remove(largercolumn);
        size--;
    }
    public boolean isDestabilizeRowValid(int row){
        Row thisRow = rows.get(row);
        return thisRow.getMaxCol()-thisRow.getMinCol()==1 && size>2;
    }
    public boolean isDestabilizeColValid(int col){
        Column thisCol = cols.get(col);
        return thisCol.getMaxRow()-thisCol.getMinRow()==1 && size>2;
    }
    public boolean destabilizeIfValid(int row, int col){
        if (isDestabilizeColValid(col)){
            //System.out.println("column destabilizable");
            destabilizeCol(col);
            return true;
        }
        if (isDestabilizeRowValid(row)){
            //System.out.println("row destabilizable");
            destabilizeRow(row);
            return true;
        }
        return false;
    }

    public boolean isRowMatchColumns(){
        for (int i=0; i<size; i++){
            if (rows.get(i).getXCol() == size){
                System.out.println("row "+i+" is a problem at X");
            } else if (rows.get(i).getOCol() == size){
                System.out.println("row "+i+" is a problem at O");
            } else if (cols.get(i).getXRow() == size){
                System.out.println("col "+i+" is a problem at X");
            } else if (cols.get(i).getORow() == size){
                System.out.println("col "+i+" is a problem at O");
            }
            if (cols.get(rows.get(i).getXCol()).getXRow() != i || cols.get(rows.get(i).getOCol()).getORow() != i){
                return false;
            }
        }
        return true;
    }

    public int calcWrithe(){
        int totalwrithe = 0;
        Row thisRow;
        Column thisCol;
        for (int i=1; i<size-1; i++){
            thisRow = rows.get(i);
            for (int j=Math.min(thisRow.getXCol(), thisRow.getOCol())+1; j<Math.max(thisRow.getXCol(), thisRow.getOCol());j++){
                thisCol = cols.get(j);
                if (Math.min(thisCol.getXRow(), thisCol.getORow()) < i && i < Math.max(thisCol.getXRow(), thisCol.getORow())){
                    totalwrithe += -(thisCol.getXRow()>thisCol.getORow() ? 1 : -1)*(thisRow.getXCol()>thisRow.getOCol() ? 1 : -1);
                }
            }
        }
        return totalwrithe;
    }

    public int deltaWrithe(int movetype, int[] arguments){
        //System.out.println("deltawrithe called");
        int delta = 0;
        switch (movetype) {
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

    private int stabilizeDeltaWrithe(int[] arguments){
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
                } else{
                    return 0;
                }
                xAndO = (arguments[2] == INSERT_OX_COLUMN ? thisRow.getDirection() : -thisRow.getDirection());
                upOrDown = (thisCol.getMinRow() == arguments[0] ? 1 : -1 );
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
                } else{
                    return 0;
                }
                xAndO = (arguments[2] == INSERT_OX_ROW ? -thisCol.getDirection() : thisCol.getDirection());
                leftOrRight = (thisRow.getMinCol() == arguments[0] ? 1 : -1);
                break;
        }
        if (leftOrRight*upOrDown == xAndO){
            return xAndO;
        }
        return 0;
    }    

    private int destabilizeDeltaWrithe(int[] arguments){
        if (arguments[2] == MOVE_SUBTYPE_ROW){
            Row thisRow;
            thisRow = rows.get(arguments[0]);
            Column xcol = cols.get(thisRow.getXCol());
            Column ocol = cols.get(thisRow.getOCol());
            if (xcol.getDirection() == ocol.getDirection()){
                return 0;
            }
            else{
                Row crossingRow;
                if (xcol.getMaxRow()-xcol.getMinRow() < ocol.getMaxRow()-ocol.getMinRow()){
                    crossingRow = rows.get(xcol.getORow());
                    if (crossingRow.getMinCol() < thisRow.getOCol() && thisRow.getOCol() < crossingRow.getMaxCol()){
                        return -crossingRow.getDirection()*ocol.getDirection();
                    } else {
                        return 0;
                    }
                } else {
                    crossingRow = rows.get(ocol.getXRow());
                    if (crossingRow.getMinCol() < thisRow.getXCol() && thisRow.getXCol() < crossingRow.getMaxCol()){
                        return -crossingRow.getDirection()*xcol.getDirection();
                    }
                    else{
                        return 0;
                    }
                }
            }
        }
        else if (arguments[2] == MOVE_SUBTYPE_COLUMN){
            Column thisCol;
            thisCol = cols.get(arguments[1]);
            Row xRow = rows.get(thisCol.getXRow());
            Row oRow = rows.get(thisCol.getORow());
            if (xRow.getDirection() == oRow.getDirection()){
                return 0;
            }
            else{
                Column crossingCol;
                if (xRow.getMaxCol()-xRow.getMinCol() < oRow.getMaxCol()-oRow.getMinCol()){
                    crossingCol = cols.get(xRow.getOCol());
                    if (crossingCol.getMinRow() < thisCol.getORow() && thisCol.getORow() < crossingCol.getMaxRow()){
                        return -crossingCol.getDirection()*oRow.getDirection();
                    } else {
                        return 0;
                    }
                } else {
                    crossingCol = cols.get(oRow.getXCol());
                    if (crossingCol.getMinRow() < thisCol.getXRow() && thisCol.getXRow() < crossingCol.getMaxRow()){
                        return -crossingCol.getDirection()*xRow.getDirection();
                    }
                    else{
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    //[rowcol, roworcol]
    private int commuteDeltaWrithe(int[] arguments){
        int delta = 0;
        if (arguments[1] == MOVE_SUBTYPE_COLUMN){
            Column thisCol = cols.get(arguments[0]);
            Column nextCol = cols.get((arguments[0]+1)%getSize());
                //Replace col with arguments[0]
            if (arguments[0] == getSize()-1){
                if (thisCol.getXRow() != nextCol.getORow()){
                    delta += (cols.get(rows.get(thisCol.getXRow()).getOCol()).getDirection() == thisCol.getDirection() ? thisCol.getDirection() : 0);
                    delta += (cols.get(rows.get(nextCol.getORow()).getXCol()).getDirection() == nextCol.getDirection() ? nextCol.getDirection() : 0);
                }
                else{
                    if (thisCol.getDirection() == nextCol.getDirection()){
                        delta += 2*thisCol.getDirection();
                    }
                    else if (nextCol.getLength() < thisCol.getLength()){
                        delta += nextCol.getDirection();
                    }
                    else{
                        delta += -nextCol.getDirection();
                    }
                }
                if (thisCol.getORow() != nextCol.getXRow()){
                    delta += (cols.get(rows.get(thisCol.getORow()).getXCol()).getDirection() == thisCol.getDirection() ? -thisCol.getDirection() : 0);
                    delta += (cols.get(rows.get(nextCol.getXRow()).getOCol()).getDirection() == nextCol.getDirection() ? -nextCol.getDirection() : 0);
                }
                else{
                    if (thisCol.getDirection() == nextCol.getDirection()){
                        delta += -2*thisCol.getDirection();
                    }
                    else if (nextCol.getLength() < thisCol.getLength()){
                        delta += -nextCol.getDirection();
                    }
                    else{
                        delta += nextCol.getDirection();
                    }
                }
            }
            else if (thisCol.getDirection() == -nextCol.getDirection()){
                if (thisCol.getXRow() == nextCol.getORow()){
                    delta += (thisCol.getLength() < nextCol.getLength() ? thisCol.getDirection() : -thisCol.getDirection());
                }
                if (thisCol.getORow() == nextCol.getXRow()){
                    delta += (thisCol.getLength() < nextCol.getLength() ? -thisCol.getDirection() : thisCol.getDirection());
                }
            }
        }
        else if (arguments[1] == MOVE_SUBTYPE_ROW){
            Row thisRow = rows.get(arguments[0]);
            Row nextRow = rows.get((arguments[0]+1)%getSize());
                //Replace col with arguments[0]
            if (arguments[0] == getSize()-1){
                if (thisRow.getXCol() != nextRow.getOCol()) {
                    delta += (rows.get(cols.get(thisRow.getXCol()).getORow()).getDirection() == thisRow.getDirection() ? -thisRow.getDirection() : 0);
                    delta += (rows.get(cols.get(nextRow.getOCol()).getXRow()).getDirection() == nextRow.getDirection() ? -nextRow.getDirection() : 0);
                }
                else{
                    if (thisRow.getDirection() == nextRow.getDirection()){
                        delta += -2*thisRow.getDirection();
                    }
                    else if (nextRow.getLength() < thisRow.getLength()){
                        delta += thisRow.getDirection();
                    }
                    else{
                        delta += -thisRow.getDirection();
                    }
                }
                if (thisRow.getOCol() != nextRow.getXCol()){
                    delta += (rows.get(cols.get(thisRow.getOCol()).getXRow()).getDirection() == thisRow.getDirection() ? thisRow.getDirection() : 0);
                    delta += (rows.get(cols.get(nextRow.getXCol()).getORow()).getDirection() == nextRow.getDirection() ? nextRow.getDirection() : 0);
                }
                else{
                    if (thisRow.getDirection() == nextRow.getDirection()){
                        delta += 2*thisRow.getDirection();
                    }
                    else if (nextRow.getLength() < thisRow.getLength()){
                        delta += -thisRow.getDirection();
                    }
                    else{
                        delta += thisRow.getDirection();
                    }
                }
            }
            else if (thisRow.getDirection() == -nextRow.getDirection()){
                if (thisRow.getXCol() == nextRow.getOCol()){
                    delta += (thisRow.getLength() < nextRow.getLength() ? -thisRow.getDirection() : thisRow.getDirection());
                }
                if (thisRow.getOCol() == nextRow.getXCol()){
                    delta += (thisRow.getLength() < nextRow.getLength() ? thisRow.getDirection() : -thisRow.getDirection());
                }
            }
        }
        return delta;
    }

    public static int deltaSize(int moveType){
        switch (moveType){
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





    public class Row implements Serializable{
        public static final long serialVersionUID = 0;

        private int xCol;
        private int oCol;
        private int minCol;
        private int maxCol;
        private int direction;
        private int length;

        public Row(int initXCol, int initOCol){
            xCol = initXCol;
            oCol = initOCol;
            setMinAndMax();
        }

        @Override
        public String toString(){
            return "X: "+xCol+" O: "+oCol;
        }

        public int getXCol() { return xCol; }
        public int getOCol() { return oCol; }
        public int getMinCol(){ return minCol; }
        public int getMaxCol(){ return maxCol; }
        public int getDirection(){ return direction; }
        public int getLength(){ return length; }
        public void setXCol(int newXCol) {
            this.xCol = newXCol;
            setMinAndMax();
        }
        public void setOCol(int newOCol) {
            this.oCol = newOCol;
            setMinAndMax();
        }
        private void setMinAndMax(){
            if (xCol < oCol){
                minCol = xCol;
                maxCol = oCol;
            } else{
                minCol = oCol;
                maxCol = xCol;
            }
            length = xCol-oCol;
            direction = (int)Math.signum(length);
            length = length*direction;
        }

        public class Column implements Serializable{
            public static final long serialVersionUID = 0;

            private int xRow;
            private int oRow;
            private int minRow;
            private int maxRow;
            private int direction;
            private int length;

            public Column(int initXRow, int initORow){
                xRow = initXRow;
                oRow = initORow;
                setMaxAndMin();
            }

            @Override
            public String toString(){
                return "X: "+xRow+" O: "+oRow;
            }

            public int getXRow() {
                return xRow;
            }
            public int getORow() {
                return oRow;
            }
            public int getMinRow(){ return minRow; }
            public int getMaxRow(){ return maxRow; }
            public int getDirection(){ return direction; }
            public int getLength(){ return length; }
            public void setXRow(int newXRow) {
                xRow = newXRow;
                setMaxAndMin();
            }
            public void setORow(int newORow) {
                oRow = newORow;
                setMaxAndMin();
            }
            private void setMaxAndMin(){
                if (xRow < oRow){
                    minRow = xRow;
                    maxRow = oRow;
                } else{
                    minRow = oRow;
                    maxRow = xRow;
                }
                length = oRow - xRow;
                direction = (int)Math.signum(length);
                length = length*direction;
            }
        }
    }
    public class Column implements Serializable{
        public static final long serialVersionUID = 0;

        private int xRow;
        private int oRow;
        private int minRow;
        private int maxRow;
        private int direction;
        private int length;

        public Column(int initXRow, int initORow){
            xRow = initXRow;
            oRow = initORow;
            setMaxAndMin();
        }

        @Override
        public String toString(){
            return "X: "+xRow+" O: "+oRow;
        }

        public int getXRow() {
            return xRow;
        }
        public int getORow() {
            return oRow;
        }
        public int getMinRow(){ return minRow; }
        public int getMaxRow(){ return maxRow; }
        public int getDirection(){ return direction; }
        public int getLength(){ return length; }
        public void setXRow(int newXRow) {
            xRow = newXRow;
            setMaxAndMin();
        }
        public void setORow(int newORow) {
            oRow = newORow;
            setMaxAndMin();
        }
        private void setMaxAndMin(){
            if (xRow < oRow){
                minRow = xRow;
                maxRow = oRow;
            } else{
                minRow = oRow;
                maxRow = xRow;
            }
            length = oRow - xRow;
            direction = (int)Math.signum(length);
            length = length*direction;
        }
    }
}