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
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;


//TODO add options for input/output
//TODO add options for interpreting data in different ways

public class ExtendWeights {

	private static final int SIZE = 1;
	private static final int SIZE_AND_WRITHE = 2;


	private static String inFilePath;
	private static String outFilePath;
	private static int mode = -1;
    private static int newMaxSize = 0;

	public static void main(String[] args){
        parseArgs(args);
		switch (mode){
			case SIZE:
				extendSizeWeights();
				break;
			case SIZE_AND_WRITHE:
				extendSizeAndWritheWeights();
				break;
			default:
				System.err.println("No valid mode option detected");
				System.exit(1);
				break;
		}
	}

    private static void extendSizeWeights(){
        HashMap<Energy, Double> weights = new HashMap();
        try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            FileOutputStream outFile = new FileOutputStream(new File(outFilePath+".wts"), false);
            ObjectOutputStream outObj = new ObjectOutputStream(outFile))
        {
            weights = (HashMap<Energy, Double>)ois.readObject();
            int oldMaxSize = 0;
            for (Energy key : weights.keySet()){
                if ((Integer)key.getEnergyState()[0] > oldMaxSize){
                    oldMaxSize = (Integer)key.getEnergyState()[0];
                }
            }
            Energy.setEnergyType(new int[]{Energy.ENERGYTYPE_SIZE});
            double diffSize = weights.get(new Energy(new Serializable[]{oldMaxSize}))-weights.get(new Energy(new Serializable[]{oldMaxSize-1}));
            for (int i = oldMaxSize+1; i <= newMaxSize; i++){
                weights.put(new Energy(new Serializable[]{i}), weights.get(new Energy(new Serializable[]{i-1}))+diffSize);
            }
            System.out.println("New Weights:");
            System.out.println(weights.entrySet());
            outObj.writeObject(weights);
        }catch (EOFException e){
            //We need to reach the end of the file. There doesn't seem to be a better way to deal with this.
        }
        catch (ClassNotFoundException e){
            System.err.println(e);
        }
        catch (IOException e){
            System.err.println(e);
        }
    }
    private static void extendSizeAndWritheWeights(){
        HashMap<Energy, Double> weights = new HashMap();
        Energy.setEnergyType(new int[]{Energy.ENERGYTYPE_SIZE, Energy.ENERGYTYPE_WRITHE});
        try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            FileOutputStream outFile = new FileOutputStream(new File(outFilePath+".wts"), false);
            ObjectOutputStream outObj = new ObjectOutputStream(outFile))
        {
            weights = (HashMap<Energy, Double>)ois.readObject();
            int oldMaxSize = 0;
            int oldMinWrithe = 0;
            int oldMaxWrithe = 0;
            for (Energy key : weights.keySet()){
                if ((Integer)key.getEnergyState()[0] > oldMaxSize){
                    oldMaxSize = (Integer)key.getEnergyState()[0];
                }
                if ((Integer)key.getEnergyState()[1] > oldMaxWrithe){
                    oldMaxWrithe = (Integer)key.getEnergyState()[1];
                }
                if ((Integer)key.getEnergyState()[1] < oldMinWrithe){
                    oldMinWrithe = (Integer)key.getEnergyState()[1];
                }
            }
            Double sizeDiff = weights.get(new Energy(new Serializable[]{oldMaxSize,oldMinWrithe+1}))-weights.get(new Energy(new Serializable[]{oldMaxSize-1,oldMinWrithe+1}));
            Double writheDiff = weights.get(new Energy(new Serializable[]{oldMaxSize,oldMinWrithe+1}))-weights.get(new Energy(new Serializable[]{oldMaxSize,oldMinWrithe}));
            for (int i = oldMaxSize+1; i <= newMaxSize; i++){            
                for (int j = oldMinWrithe-(i-oldMaxSize)+1; j<=oldMaxWrithe+(i-oldMaxSize)-1; j++){
                    weights.put(new Energy(new Serializable[]{i,j}), weights.get(new Energy(new Serializable[]{i-1,j}))+sizeDiff);
                }
                weights.put(new Energy(new Serializable[]{i,oldMinWrithe-(i-oldMaxSize)}), weights.get(new Energy(new Serializable[]{i,oldMinWrithe-(i-oldMaxSize)+1}))-writheDiff);
                weights.put(new Energy(new Serializable[]{i,oldMaxWrithe+(i-oldMaxSize)}), weights.get(new Energy(new Serializable[]{i,oldMaxWrithe+(i-oldMaxSize)-1}))-writheDiff);
            }
            System.out.println("New Weights:");
            System.out.println(weights.entrySet());
            outObj.writeObject(weights);
        }catch (EOFException e){
            //We need to reach the end of the file. There doesn't seem to be a better way to deal with this.
        }
        catch (ClassNotFoundException e){
            System.err.println(e);
        }
        catch (IOException e){
            System.err.println(e);
        }
    }

	private static void parseArgs(String[] args){
        for (int i=0; i<args.length; i++){
            switch (args[i]){
                case "-i":
                case "--input-path":
                    inFilePath = args[i+1];
                    i++;
                    break;
                case "-o":
                case "--output-path":
                    outFilePath = args[i+1];
                    i++;
                    break;
                case "-SW":
                	mode = SIZE_AND_WRITHE;
                	break;
                case "-S":
                	mode = SIZE;
                	break;
                case "-M":
                    newMaxSize = Integer.valueOf(args[i+1]);
                    i++;
                    break;
                default:
                    System.out.println("Unknown argument: "+args[i]);
                    break;
            }//TODO make sure options have good designators
        }
    }
}