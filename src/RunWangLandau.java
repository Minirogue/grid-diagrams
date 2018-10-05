//package grid_tools;

public class RunWangLandau {

    private static int maxSize = 100;
    private static int minSize = 0;
    private static String knotType = "3_1";
    private static int[] energy = new int[0];
    private static String inputWeightsFile;
    private static String outputWeightsFile;
    private static int steps = 10000;
    private static double fStart = 1;
    private static double fFinal = .01;
    private static double fChange = .5;
    private static int flatCheckFrequency = 20;
    private static boolean isTraining = false;

    public static void main(String[] args){
        parseArgs(args);
        WangLandau wl = new WangLandau(knotType, energy);
        wl.setUpperSize(maxSize);
        wl.setLowerSize(minSize);
        if (inputWeightsFile != null){
            wl.loadWeightsFromFile(inputWeightsFile);
            wl.setDefaultWeightToMax();
        }
        if (outputWeightsFile != null){
            wl.setWeightsSaveFile(outputWeightsFile);
        }

        System.out.println("Running Wang-Landau for "+knotType+" with the following parameters:");
        System.out.println("Minimum size: "+minSize);
        System.out.println("Maximum size: "+maxSize);
        System.out.println("Energy = "+energyTypeAsString());
        System.out.println("With starting weights from "+ inputWeightsFile);
        System.out.println("Saving weights to "+outputWeightsFile);
        System.out.println("Sampling every "+steps+" steps");
        System.out.println("Weights modified initially by e^"+fStart);
        System.out.println("With exponent changing by a factor of "+fChange);
        System.out.println("Until modification factor is less than e^"+fFinal);
        System.out.println("Checking for flatness every "+flatCheckFrequency+" samples");
        System.out.println("Training = "+isTraining);
        wl.train(steps, flatCheckFrequency, fStart, fFinal, fChange);
    }

    private static void parseArgs(String[] args){
        for (int i=0; i<args.length; i++){
            switch (args[i]){
                case "-k":
                case "--knot-type":
                    knotType = args[i+1];
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
                case "--output-weights":
                    outputWeightsFile = args[i+1];
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
                case "--train":
                    isTraining = true;
                    break;
                default:
                    System.out.println("Unknown argument: "+args[i]);
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
}

