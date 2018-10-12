//import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.ArrayList;


//TODO add options for input/output
//TODO add options for interpreting data in different ways

public class AnalyzeGrids {

	private static String filePath = "testingsamples";

	public static void main(String[] args){
        filePath = args[0];
		HashMap<Integer, ArrayList<Integer>> fullData = new HashMap();
		try (FileInputStream fis = new FileInputStream(filePath+".grds");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            int[][] savableGrid;
            GridDiagram gd;
            ArrayList<Integer> currentAL;
            int size;
            while(true){
                savableGrid = (int[][])ois.readObject();
                gd = new GridDiagram(savableGrid[0], savableGrid[1]);
                size = gd.getSize();
                if (fullData.containsKey(size)){
                	currentAL = fullData.get(size);
                }else{
                	currentAL = new ArrayList();
                	fullData.put(size, currentAL);
                }
                currentAL.add(gd.calcWrithe());
            }
        }catch (EOFException e){
            //We need to reach the end of the file. There doesn't seem to be a better way to deal with this.
        }
        catch (ClassNotFoundException e){
            System.err.println(e);
        }
        catch (IOException e){
            System.err.println(e);
        }
        //System.out.println(fullData);
        for (int size : fullData.keySet()){
        	//System.out.println("Writhe average for "+size+": ");
        	try(FileWriter fw = new FileWriter(filePath+"_"+size+".txt");
        		BufferedWriter bw = new BufferedWriter(fw)){
	        	for (Integer wr : fullData.get(size)){
	        		bw.write(wr+"\n");
	        	}
	        } catch(IOException e){
	        	System.err.println(e);
	        }
        	//System.out.println(""+((double)sum)/((double)fullData.get(size).size()));
        	//System.out.println(fullData.get(size));
        }
	}
}