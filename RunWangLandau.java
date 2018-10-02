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

    public static void main(String[] args){
        parseArgs(args);
        WangLandau wl = new WangLandau(knotType, energy);
        wl.setUpperSize(maxSize);
        wl.setLowerSize(minSize);
        if (inputWeightsFile != null){
            wl.loadWeightsFromFile(inputWeightsFile);
        }
        if (outputWeightsFile != null){
            wl.setWeightsSaveFile(outputWeightsFile);
        }

        wl.train(steps, flatCheckFrequency, fStart, fFinal, fChange);
    }

    private static void parseArgs(String[] args){
        int[] newArr;
        for (int i=0; i<args.length; i++){
            switch (args[i]){
                case "-k":
                case "--knot-type":
                    knotType = args[i+1];
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
                case "--input":
                    inputWeightsFile = args[i+1];
                    break;
                case "-o":
                case "--output":
                    outputWeightsFile = args[i+1];
                    break;
                case "-M":
                case "--max-size":
                    maxSize = Integer.valueOf(args[i+1]);
                    break;
                case "-m":
                case "--min-size":
                    minSize = Integer.valueOf(args[i+1]);
                    break;
            }
        }
    }

    private static int[] appendInt(int[] oldArr, int newEntry){
        int[] newArr = new int[oldArr.length+1];
        System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
        newArr[oldArr.length] = newEntry;
        return newArr;
    }
}

