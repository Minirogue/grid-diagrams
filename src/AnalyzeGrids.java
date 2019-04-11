//import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
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
    private static final int AVERAGE_30_WEIGHTS = 5;
    private static final int MAKE_SAMPLE_ML_VECTOR = 6;
    private static final int MAKE_ML_VECTOR = 7;
    private static final int SIZEWRITHE_MAKE_TEX_TABLE = 8;
    private static final int PRINT_SIZEWRITHE_WEIGHTS = 9;

	private static String inFilePath;
	private static String outFilePath;
    private static String knotName;
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
			case SIZEWRITHE_MAKE_TEX_TABLE:
				sizeWritheMaxMin();
				break;
            case SIZE_WEIGHTS:
                printSizeWeights();
                break;
            case WEIGHTS_TO_PARSE:
                weightsToParse();
                break;
            case AVERAGE_30_WEIGHTS:
            	average30Weights();
            	break;
            case MAKE_SAMPLE_ML_VECTOR:
                makeSampleMLVector();
                break;
            case MAKE_ML_VECTOR:
                makeMLVectors();
                break;
            case PRINT_SIZEWRITHE_WEIGHTS:
                printSizeWritheWeights();
                break;
			default:
				System.err.println("No valid mode option detected");
				System.exit(1);
				break;
		}
	}

    private static void makeSampleMLVector(){
        String[] knotlist = new String[]{"3_1", "4_1", "5_1", "5_2", "6_1", "6_2", "6_3","7_1","7_2","7_3","7_4","7_5","7_6","7_7",
            "8_1","8_2","8_3","8_4","8_5","8_6","8_7","8_8","8_9","8_10","8_11","8_12","8_13","8_14","8_15","8_16","8_17","8_18",
            "8_19","8_20","8_21"};
        GridDiagram gd;
        int[][] savableGrid;
        String outString;
        for (String knotname : knotlist){
            outString = knotname;
            gd = new GridDiagram(knotname);
            savableGrid = gd.getSavableGrid();
            for (int i : savableGrid[0]){
                outString += " "+i;
            }
            for (int j = 0; j<(20-savableGrid[0].length); j++){
                outString += " -1";
            }
            for (int i : savableGrid[1]){
                outString += " "+i;
            }
            for (int j = 0; j<(20-savableGrid[1].length); j++){
                outString += " -1";
            }
            System.out.println(outString);
        }
    }

    private static String rowToVec(int x, int o, int length){
        String returnString = "";
        for (int i = 0; i<length; i++){
            if (i==x || i==o){
                returnString += " 1";
            }
            else{
                returnString += " 0";
            }
        }
        return returnString;
    }

    private static void makeMLVectors(){
        try (FileInputStream fis = new FileInputStream(inFilePath+".grds");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            int[][] savableGrid;
            String outString;
            while(true){
                outString = knotName;
                savableGrid = (int[][])ois.readObject();
                if (savableGrid[0].length <= 20){
                    for (int i=0; i<savableGrid[0].length; i++){
                        outString += rowToVec(savableGrid[0][i], savableGrid[1][i], 20);
                    }
                    for (int i=0; i<20-savableGrid[0].length; i++){
                        outString += rowToVec(-1, -1, 20);
                    }
                    System.out.println(outString);
                }
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


	private static void average30Weights(){
		HashMap<Energy, ArrayList<Double>> allinfo = new HashMap();
        ArrayList thisArrayList;
        String currentfilename;
        HashMap<Energy, Double> theseWeights;
        for (int i = 1; i<=30; i++){
            currentfilename = inFilePath+"_"+i+".wts";
            try (FileInputStream fis = new FileInputStream(currentfilename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis))
            {
                theseWeights = (HashMap<Energy, Double>)ois.readObject();
                for (Energy thisEnergy : theseWeights.keySet()){
                    thisArrayList = allinfo.getOrDefault(thisEnergy, new ArrayList());
                    thisArrayList.add(theseWeights.get(thisEnergy));
                    allinfo.put(thisEnergy, thisArrayList);
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
        HashMap<Energy, Double> averageWeights = new HashMap();
        double averageWeight;
        for (Map.Entry<Energy, ArrayList<Double>> entry : allinfo.entrySet()){
            averageWeight = printAverageInfo(entry.getKey(), entry.getValue());
            averageWeights.put(entry.getKey(), averageWeight);
        }
        try (FileOutputStream outFile = new FileOutputStream(new File(outFilePath+".wts"));
            ObjectOutputStream outObj = new ObjectOutputStream(outFile))
        {
            outObj.writeObject(averageWeights);
        }catch (FileNotFoundException e){
            System.out.println(e);
        } catch (IOException e){
            System.out.println(e);
        }
        
	}

    private static double printAverageInfo(Energy en, ArrayList<Double> weights){
        double sum = 0;
        for (double w : weights){
            sum += w;
        }
        double mean = sum/weights.size();
        double std = 0;
        for (double w : weights){
            std += Math.pow((w-mean), 2);
        }
        std = Math.sqrt(std/weights.size());
        double ciLower = mean - 1.96*std/Math.sqrt(weights.size());
        double ciUpper = mean + 1.96*std/Math.sqrt(weights.size());
        double maxdistance = (Double)Collections.max(weights) - (Double)Collections.min(weights);
        //System.out.println("For energy "+en+" CI = ("+ciLower+", "+ciUpper+") CI width = "+(ciUpper-ciLower)+" range: "+maxdistance);
        //System.out.println(en.getEnergyState()[0]+" "+mean+" "+ciLower+" "+ciUpper);
        String outline = ""+en.getEnergyState()[0];
        for (Double entry : weights){
            outline += " "+entry;
        }
        System.out.println(outline);
        return mean;
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
        try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
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

    private static void printSizeWritheWeights(){
        HashMap<Energy,Double> weights = null;
        HashMap<Energy,Double> estimatedError = null;
        try (FileInputStream fis = new FileInputStream(inFilePath+".wts");
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis))
        {
            weights = (HashMap<Energy, Double>)ois.readObject();
            estimatedError = (HashMap<Energy, Double>)ois.readObject();
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
            //Energy eVal;
            double wVal;
            double error;
            for (Energy eVal : weights.keySet()){
                //eVal = entry.getKey();
                wVal = weights.get(eVal);
                error = estimatedError.get(eVal);
                System.out.println(eVal.toString()+" "+wVal+" "+error);
            }
        }
    }

	private static void sizeWritheMaxMin(){
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
        	int maxSize = 0;
        	int minSize = 0;
        	double maxWrithe = -Double.MAX_VALUE;
        	double minWrithe = Double.MAX_VALUE;
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
   				double avg = sum/count;
   				if (!Double.isNaN(avg) && avg < minWrithe){
   					minWrithe = avg;
   					minSize = currSize;
   				}
   				if (!Double.isNaN(avg) && avg > maxWrithe){
   					maxWrithe = avg;
   					maxSize = currSize;
   				}
   				
        	}
        	System.out.println("$"+knotName+"$ & $"+minSize+"$ & $"+minWrithe+"$ & $"+maxSize+"$ & $"+maxWrithe+"$ \\");
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
                case "-k":
                case "--knot-name":
                    knotName = args[i+1];
                    i++;
                    break;
                case "-SW":
                	mode = SIZEWRITHE_TRAINED_AND_SAMPLED_MODE;
                	break;
                case "-SWtex":
                	mode = SIZEWRITHE_MAKE_TEX_TABLE;
                	break;
                case "-SWPure":
                    mode = PRINT_SIZEWRITHE_WEIGHTS;
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
                case "--average-30-weights":
                    mode = AVERAGE_30_WEIGHTS;
                    break;
                case "--generate-sample-ml-vector":
                    mode = MAKE_SAMPLE_ML_VECTOR;
                    break;
                case "-ML":
                    mode = MAKE_ML_VECTOR;
                    break;
                default:
                    System.out.println("Unknown argument: "+args[i]);
                    break;
            }//TODO make sure options have good designators
        }
    }
}