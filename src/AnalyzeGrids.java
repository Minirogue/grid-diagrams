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



//TODO add options for input/output
//TODO add options for interpreting data in different ways

public class AnalyzeGrids {

	private static final int SIZE_TRAINED_AND_SAMPLED_MODE = 1;
	private static final int SIZEWRITHE_TRAINED_AND_SAMPLED_MODE = 2;
    private static final int SIZE_WEIGHTS = 3;
    private static final int WEIGHTS_TO_PARSE = 4;

	private static String inFilePath;
	private static String outFilePath;
	private static int mode = -1;

	public static void main(String[] args){
        parseArgs(args);
		switch (mode){
			case SIZE_TRAINED_AND_SAMPLED_MODE:
				parseWrithesOfSamples();
				break;
			case SIZEWRITHE_TRAINED_AND_SAMPLED_MODE:
				analyzeSizeWritheWeights();
				break;
            case SIZE_WEIGHTS:
                printSizeWeights();
                break;
            case WEIGHTS_TO_PARSE:
                weightsToParse();
                break;
			default:
				System.err.println("No valid mode option detected");
				System.exit(1);
				break;
		}
	}

    private static void weightsToParse(){
        boolean sizeNormed = false;//to norm each size
        HashMap<Energy, Double> weights = new HashMap();
        try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            weights = (HashMap<Energy, Double>)ois.readObject();
            HashMap<Integer, Double> minWeightOfSize = new HashMap();
            if (sizeNormed){
                for (Map.Entry<Energy, Double> entry : weights.entrySet()){
                    if (entry.getValue() < minWeightOfSize.getOrDefault((int)entry.getKey().getEnergyState()[0],Double.MAX_VALUE)){
                        minWeightOfSize.put((int)entry.getKey().getEnergyState()[0], entry.getValue());
                    }
                }
            }
            String thisData;
            for (Map.Entry<Energy, Double> entry : weights.entrySet()){
                    thisData = "";
                    for (Serializable s : entry.getKey().getEnergyState()){
                        thisData += s.toString()+" ";
                    }
                    thisData += (entry.getValue()-minWeightOfSize.getOrDefault((int)entry.getKey().getEnergyState()[0],0.0));
                   System.out.println(thisData);
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
    }

    private static void printSizeWeights(){
        HashMap<Energy, Double> weights = new HashMap();
        try (FileInputStream fis = new FileInputStream(inFilePath+".ser");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            weights = (HashMap<Energy, Double>)ois.readObject();
            for (Map.Entry<Energy, Double> entry : weights.entrySet()){
               System.out.println(entry.getKey().getEnergyState()[0]+" "+entry.getValue());
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
    }

	private static void analyzeSizeWritheWeights(){
		HashMap<Energy,Double> weights = null;
		try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
        	weights = (HashMap<Energy, Double>)ois.readObject();
        }catch (EOFException e){
        	System.err.println(e);
        	System.err.println("No data in file?");
        }
        catch (ClassNotFoundException e){
            System.err.println(e);
        }
        catch (IOException e){
            System.err.println(e);
        }
        if (weights != null){
        	HashMap<Integer, ArrayList<Double>> sizeToWrithes = new HashMap();
        	HashMap<Integer, ArrayList<Double>> sizeToWeights = new HashMap();
        	Energy thisEnergy;
        	Double thisWeight;
        	int thisSize;
        	double thisWrithe;
        	ArrayList<Double> writheAL;
        	ArrayList<Double> weightAL;
        	for (Map.Entry<Energy, Double> entry : weights.entrySet()){
        		thisEnergy = entry.getKey();
        		thisSize = (int)thisEnergy.getEnergyState()[0];
        		thisWrithe = (int)thisEnergy.getEnergyState()[1];
        		thisWeight = (Double)entry.getValue();
        		if (sizeToWrithes.containsKey(thisSize)){
        			writheAL = sizeToWrithes.get(thisSize);
        			weightAL = sizeToWeights.get(thisSize);
        		}else{
        			writheAL = new ArrayList<Double>();
        			weightAL = new ArrayList<Double>();
        			sizeToWrithes.put(thisSize, writheAL);
        			sizeToWeights.put(thisSize, weightAL);
        		}
        		weightAL.add(thisWeight);
        		writheAL.add(thisWrithe);
        	}
        	for (int currSize : sizeToWeights.keySet()){
        		writheAL = sizeToWrithes.get(currSize);
        		weightAL = sizeToWeights.get(currSize);
   				double reduction_value = Collections.min(weightAL);
   				double sum = 0;
   				double count = 0;
   				//System.out.println(writheAL);
   				//System.out.println(weightAL);
   				for (int i = 0; i<writheAL.size(); i++){
   					thisWeight = weightAL.get(i);
   					thisWrithe = writheAL.get(i);
   					sum += Math.exp(thisWeight-reduction_value)*thisWrithe;
   					count += Math.exp(thisWeight-reduction_value);
   				}

   				System.out.println(currSize+" "+(sum/count));
        	}
        }
	}

	private static void splitWrithesToFiles(){
		HashMap<Integer, ArrayList<Integer>> fullData = new HashMap();
		try (FileInputStream fis = new FileInputStream(inFilePath+".grds");
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
        	try(FileWriter fw = new FileWriter(outFilePath+"_"+size+".txt");
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

    private static void parseWrithesOfSamples(){
        try (FileInputStream fis = new FileInputStream(inFilePath+".grds");
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
                System.out.println(gd.getSize()+" "+gd.calcWrithe());
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
                	mode = SIZEWRITHE_TRAINED_AND_SAMPLED_MODE;
                	break;
                case "-S":
                	mode = SIZE_TRAINED_AND_SAMPLED_MODE;
                	break;
                case "-Swts":
                    mode = SIZE_WEIGHTS;
                    break;
                case "--make-weights-parseable":
                    mode = WEIGHTS_TO_PARSE;
                    break;
                default:
                    System.out.println("Unknown argument: "+args[i]);
                    break;
            }//TODO make sure options have good designators
        }
    }
}