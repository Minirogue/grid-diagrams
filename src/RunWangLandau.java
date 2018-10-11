//package grid_tools;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class RunWangLandau {

    private static int maxSize = 100;
    private static int minSize = 0;
    private static String knotType = "3_1";
    private static int[] energy = new int[0];
    private static String inputWeightsFile;
    private static String outputFilePath;
    private static int steps = 10000;
    private static double fStart = 1;
    private static double fFinal = .01;
    private static double fChange = .5;
    private static int flatCheckFrequency = 20;
    private static boolean isTraining = false;
    private static boolean isSampling = false;
    private static boolean isMakeMovie = false;
    private static int histThreshold = Integer.MAX_VALUE;
    private static int numSamples = 10000;

    public static void main(String[] args){
        parseArgs(args);
        WangLandau wl = new WangLandau(knotType, energy);
        wl.setUpperSize(maxSize);
        wl.setLowerSize(minSize);
        if (inputWeightsFile != null){
            wl.loadWeightsFromFile(inputWeightsFile);
            wl.setDefaultWeightToMax();
        }
        if (outputFilePath == null){
    		System.err.println("output path not specified");
    		System.exit(1);
    	}else{
        	wl.setOutputPath(outputFilePath);
        }
        if (isTraining){
        	wl.setMakeMovie(isMakeMovie);
        	wl.setHistThreshold(histThreshold);
        	printTrainingLogFile();
        	wl.train(steps, flatCheckFrequency, fStart, fFinal, fChange);
        }
        else if (isSampling){
        	printSampleLogFile();
        	wl.sample(steps, numSamples);
        }
        else {
        	System.out.println("No mode selected. Use either --training or --sampling option.");
        	System.exit(1);
        }
    }

    private static void parseArgs(String[] args){
        for (int i=0; i<args.length; i++){
            switch (args[i]){
                case "-k":
                case "--knot-type":
                    knotType = args[i+1].replaceAll("\\s+","");;
                    i++;
                    break;
                case "-S":
                case "--size":
                    energy = appendInt(energy, Energy.ENERGYTYPE_SIZE);
                    break;
                case "-W":
                case "--writhe":
                    energy = appendInt(energy, Energy.ENERGYTYPE_WRITHE);
                    break;
                case "-i":
                case "--input-weights":
                    inputWeightsFile = args[i+1];
                    i++;
                    break;
                case "-o":
                case "--output-path":
                    outputFilePath = args[i+1];
                    i++;
                    break;
                case "-M":
                case "--max-size":
                    maxSize = Integer.valueOf(args[i+1]);
                    i++;
                    break;
                case "-m":
                case "--min-size":
                    minSize = Integer.valueOf(args[i+1]);
                    i++;
                    break;
                case "-f":
                case "--initial-f":
                    fStart = Double.valueOf(args[i+1]);
                    i++;
                    break;
                case "-ff":
                case "--final-f":
                    fFinal = Double.valueOf(args[i+1]);
                    i++;
                    break;
                case "-d":
                case "--delta-f":
                    fChange = Double.valueOf(args[i+1]);
                    i++;
                    break;
                case "-F":
                case "--flat-check-frequency":
                    flatCheckFrequency = Integer.valueOf(args[i+1]);
                    i++;
                    break;
                case "-s":
                case "--steps":
                    steps = Integer.valueOf(args[i+1]);
                    i++;
                    break;
                case "-t":
                case "--training":
                    isTraining = true;
                    break;
                case "--sampling":
                	isSampling = true;
                	numSamples = Integer.valueOf(args[i+1]);
                	i++;
                	break;
                case "-T":
                case "--threshold":
                	histThreshold = Integer.valueOf(args[i+1]);
                	i++;
                	break;
                case "--movie":
                    isMakeMovie = true;
                    break;
                default:
                    System.out.println("Unknown argument: "+args[i]);
                    break;
            }//TODO make sure options have good designators
        }
    }

    private static int[] appendInt(int[] oldArr, int newEntry){
        int[] newArr = new int[oldArr.length+1];
        System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
        newArr[oldArr.length] = newEntry;
        return newArr;
    }

    private static String energyTypeAsString(){
        String returnString = "(";
        for (int i=0; i< energy.length; i++){
            switch (energy[i]){
                case Energy.ENERGYTYPE_SIZE:
                    returnString += "Size";
                    break;
                case Energy.ENERGYTYPE_WRITHE:
                    returnString += "Writhe";
                    break;
            }
            if (i==energy.length-1){
                returnString += ")";
            }
            else{
                returnString += ", ";
            }
        }
        return returnString;
    }

    private static void printTrainingLogFile(){
        try{
            PrintWriter writer = new PrintWriter(outputFilePath+".log", "UTF-8");
            writer.println("Training Wang-Landau for "+knotType+" with the following parameters:");
            writer.println("Minimum size: "+minSize);
            writer.println("Maximum size: "+maxSize);
            writer.println("Energy = "+energyTypeAsString());
            writer.println("With starting weights from "+ inputWeightsFile);
            writer.println("Saving weights to "+outputFilePath+".wts");
            writer.println("Sampling every "+steps+" steps");
            writer.println("Weights modified initially by e^"+fStart);
            writer.println("With exponent changing by a factor of "+fChange);
            writer.println("Until modification factor is less than e^"+fFinal);
            writer.println("Checking for flatness every "+flatCheckFrequency+" samples");
            writer.println("Histogram threshold = "+histThreshold);
            writer.println("outputting to movie? "+isMakeMovie);
            writer.close();
        }catch (FileNotFoundException e){
            System.out.println(e);
            System.exit(1);
        }catch (UnsupportedEncodingException e){
            System.out.println (e);
        }
    }

    private static void printSampleLogFile(){
    	try{
            PrintWriter writer = new PrintWriter(outputFilePath+".log", "UTF-8");
            writer.println("Sampling Wang-Landau for "+knotType+" with the following parameters:");
            writer.println("Minimum size: "+minSize);
            writer.println("Maximum size: "+maxSize);
            writer.println("Energy = "+energyTypeAsString());
            writer.println("With starting weights from "+ inputWeightsFile);
            writer.println("Saving samples to "+outputFilePath+".grds");
            writer.println("Sampling every "+steps+" steps");
            writer.close();
        }catch (FileNotFoundException e){
            System.out.println(e);
            System.exit(1);
        }catch (UnsupportedEncodingException e){
            System.out.println (e);
        }
    }
}

