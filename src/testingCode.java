//import GridAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;

public class testingCode {

    public static void main(String[] args){
        GridDiagram sixcone = new GridDiagram("3_1");
        sixcone.concatenate(new GridDiagram("3_1"));
        GridDiagram sixctwo = new GridDiagram("3_1");
        sixctwo.mirror();
        sixctwo.concatenate(new GridDiagram("3_1"));
        for (int i = 0; i < 9; i++){
            System.out.println("i = "+i);
            if (sixcone.getRow(sixcone.getCol(i).getXRow()).getXCol() != i
                || sixcone.getRow(sixcone.getCol(i).getORow()).getOCol() != i){
                System.out.println("error in 6c_1 column index "+i);
            }
            if (sixctwo.getRow(sixctwo.getCol(i).getXRow()).getXCol() != i
                || sixctwo.getRow(sixctwo.getCol(i).getORow()).getOCol() != i){
                System.out.println("error in 6c_2 column index "+i);
            }

        }
            try{
                FileOutputStream outFile = new FileOutputStream(new File("bin/knot_conformations/6c_1.grd"));
                ObjectOutputStream outObj = new ObjectOutputStream(outFile);
                outObj.writeObject(sixcone);
                outObj.close();
                outFile.close();
            } catch (FileNotFoundException e){
                System.out.println(e);
            } catch (IOException e){
                System.out.println(e);
            }
            try{
                FileOutputStream outFile = new FileOutputStream(new File("bin/knot_conformations/6c_2.grd"));
                ObjectOutputStream outObj = new ObjectOutputStream(outFile);
                outObj.writeObject(sixctwo);
                outObj.close();
                outFile.close();
            } catch (FileNotFoundException e){
                System.out.println(e);
            } catch (IOException e){
                System.out.println(e);
            }
    }

}