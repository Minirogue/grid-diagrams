//import GridAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;

public class testingCode {

    public static void main(String[] args){
        /*
        int steps = 1000;
        int[] energytype = new int[]{Energy.ENERGYTYPE_SIZE, Energy.ENERGYTYPE_WRITHE};
        String knottype = "3_1";
        WangLandau wl = new WangLandau(knottype, energytype);
        wl.setWeightsSaveFile("testWeights.ser");
        wl.setUpperSize(50);
        wl.train(steps, 200, 1.0, .01, .5);
        */

        /*
        int[] energytype = new int[]{Energy.ENERGYTYPE_SIZE};
        String knottype = "3_1";
        WangLandau wl = new WangLandau(knottype, energytype);
        wl.loadWeightsFromFile("testWeights.ser");
        System.out.println(wl.getWeights().values());
        */

        HashMap<Energy, Double> weights = new HashMap<>();
        try{
            FileInputStream inFile = new FileInputStream(new File("testingfile.ser"));
            ObjectInputStream inObj = new ObjectInputStream(inFile);
            weights = (HashMap<Energy, Double>)inObj.readObject();
            inObj.close();
            inFile.close();
        } catch (FileNotFoundException e){
            System.out.println("File not found");
        } catch (IOException e){
            //System.out.println("Error initializing input stream");
            System.out.println(e);
        } catch (ClassNotFoundException e){
            System.out.println("File not correctly formatted");
        }
        Energy.setEnergyType(new int[]{Energy.ENERGYTYPE_SIZE});
        Energy e = new Energy(new GridDiagram("3_1"));
        System.out.println(weights.keySet());
        System.out.println(weights.values());
        System.out.println(e);
        Iterator iterator = weights.keySet().iterator();
        while (iterator.hasNext()){
            Energy storedEn = (Energy)iterator.next();
            System.out.println(storedEn.getEnergyState()[0]);
            System.out.println(storedEn.equals(e));
        }
        System.out.println(weights.containsKey(e));
        System.out.println(weights.get(e));
    }

}