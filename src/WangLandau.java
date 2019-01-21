//package grid_tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class WangLandau {


	private GridDiagram gDiagram;
	//private int mode; use if this is merged with regular grid algorithm
	private int upperSize;
	private int lowerSize;
	private Energy currentEnergy;
	private Energy nextEnergy;
	private HashMap<Energy, Double> weights;
	private String outputPath;
	private double defaultWeight = 0.0;
	private boolean makeMovie = false;
	private int histThreshold = Integer.MAX_VALUE;
	private Random rand = new Random();


	public WangLandau(String knotName, int[] initEnergyType){
		gDiagram = new GridDiagram(knotName);
		Energy.setEnergyType(initEnergyType);
		calcAndSetCurrentEnergy();
		weights = new HashMap<>();
		upperSize = 100;//TODO don't hardcode
		lowerSize = 0;//TODO don't hardcode
		gDiagram.printToTerminal();
	}

	public void setOutputPath(String filename){ outputPath = filename; }
	public void setUpperSize(int newbound){ upperSize = newbound; }
	public void setLowerSize(int newbound){ lowerSize = newbound; }
	public void setHistThreshold(int newthreshold){
		histThreshold = newthreshold;
	}
	public void setRandomSeed(long newSeed){
		rand.setSeed(newSeed);
	}

	public HashMap<Energy, Double> getWeights(){ return weights; }
	public void setDefaultWeightToMax(){
		if (weights.isEmpty()){
			defaultWeight = 0;
		}
		else{
			defaultWeight = Collections.min(weights.values());
		}
	}

	public void setMakeMovie(boolean newval){
		makeMovie = newval;
	}
	public boolean loadWeightsFromFile(String filename){
		try{
			FileInputStream inFile = new FileInputStream(new File(filename));
			ObjectInputStream inObj = new ObjectInputStream(inFile);
			weights = (HashMap<Energy, Double>)inObj.readObject();
			inObj.close();
			inFile.close();
			return true;
		} catch (FileNotFoundException e){
			System.out.println(e);
			return false;
		} catch (IOException e){
			//System.out.println("Error initializing input stream");
			System.out.println(e);
			return false;
		} catch (ClassNotFoundException e){
			System.out.println("File not correctly formatted");
		}
		return false;
	}
	private boolean saveWeightsToFile(){
		if (outputPath != null){
			try{
				FileOutputStream outFile = new FileOutputStream(new File(outputPath+".wts"));
				ObjectOutputStream outObj = new ObjectOutputStream(outFile);
				outObj.writeObject(weights);
				outObj.close();
				outFile.close();
				return true;
			} catch (FileNotFoundException e){
				System.out.println(e);
				return false;
			} catch (IOException e){
				System.out.println(e);
				return false;
			}
		}
		return false;
	}
	private void printToMovie(HashMap<Energy, Integer> histogram){//WARNING: this assumes size is energy
		String weightString = "";
		String histString = "";
		try{
			File weightFile = new File(outputPath+"_weights.txt");
			File histFile = new File(outputPath+"_histogram.txt");
			FileWriter weightWriter = new FileWriter(weightFile, true);
			FileWriter histWriter = new FileWriter(histFile, true);
			for (int i = lowerSize; i <= upperSize; i++){
				weightString += weights.getOrDefault(new Energy(new Integer[]{i}),0.0)+" ";
				histString += histogram.getOrDefault(new Energy(new Integer[]{i}),0)+" ";
			}
			weightWriter.write(weightString+"\n");
			histWriter.write(histString+"\n");
			weightWriter.close();
			histWriter.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	public GridDiagram getGridDiagram(){ return gDiagram; }
	public void updateCurrentEnergyFromNext(){
		currentEnergy = nextEnergy;//TODO should I construct a new object here?
	}
	public void calcAndSetCurrentEnergy(){
		currentEnergy = new Energy(gDiagram);
	}

	public void calcAndSetNextEnergy(int movetype, int[] arguments){
		nextEnergy = new Energy(currentEnergy, gDiagram, movetype, arguments);
	}

	public boolean calcAndCheckProbability(int movetype, int[] arguments){
		calcAndSetNextEnergy(movetype, arguments);
		double prob;
		switch (movetype) {//TODO this needs to be reviewed mathematically, as the numbers are not coming out right for the unknot
			case GridDiagram.MOVETYPE_COMMUTATION:
				prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0);
				break;
			case GridDiagram.MOVETYPE_DESTABILIZATION:
				prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0) - Math.log(4*(gDiagram.getSize()-1));
				break;
			case GridDiagram.MOVETYPE_STABILIZATION:
				prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0) + Math.log(4*gDiagram.getSize());
				break;
			default:
				prob = -999999;
				System.out.println("Error with calculateProbabilityOfMove: moveType not valid");
				System.exit(1);
		}
		return Math.log(rand.nextDouble()) < prob;
	}

	private void clearHistogram(HashMap<Energy, Integer> histogram){
		for (Energy key : weights.keySet()){
			histogram.put(key, 0);
		}
	}

	public void train(int steps, int flatCheckFreq, double fStart, double fFinal, double fModFactor){
		HashMap<Energy, Integer> histogram = new HashMap<>();
		//boolean isFirstF = true;
		double fCurrent = fStart;
		double currentWeight;//currentMaxWeight, currentMinWeight, 
		int currentMinHistogram, currentMaxHistogram;
		double stopThreshold = 1/Math.sqrt(fCurrent);
		if (makeMovie){
			try{
				Files.deleteIfExists(Paths.get(outputPath+"_weights.txt"));
				Files.deleteIfExists(Paths.get(outputPath+"_histogram.txt"));
			}catch (IOException e){
				System.out.println(e);
			}

		}
		clearHistogram(histogram);//initialize histogram with all known energy states
		histogram.put(currentEnergy, 0);//initialize histogram entry for starting state. This is to help with the stopping condition.
		//run(steps*10);//warmup
		while (fCurrent >= fFinal){
			for (int i = 0; i<flatCheckFreq; i++){
				run(steps);
				currentWeight = weights.getOrDefault(currentEnergy, defaultWeight)+fCurrent;//TODO could the 0 here be replaced with some function of the existing weights?
				/*if (currentMaxWeight < currentWeight){
					currentMaxWeight = currentWeight;
				}*/
				weights.put(currentEnergy,currentWeight);
				histogram.put(currentEnergy, histogram.getOrDefault(currentEnergy,0)+1);
				if (makeMovie){
					printToMovie(histogram);
				}
			}
			currentMinHistogram = Collections.min(histogram.values());
			//currentMaxHistogram = Collections.max(histogram.values());
			if (currentMinHistogram > stopThreshold){//+(isFirstF ? 10 : 0)){
				normalizeWeights();
				System.out.println("Passed with f=exp("+fCurrent+") and stopping threshold "+stopThreshold);//+((isFirstF ? " + 10." : ".")));
				System.out.println("Saving weights:");
				System.out.println(""+weights.entrySet());
				saveWeightsToFile();
				fCurrent = fCurrent*fModFactor;
				stopThreshold = 1/Math.sqrt(fCurrent);
				//isFirstF = false;
				clearHistogram(histogram);
				System.out.println("Now running with f=exp("+fCurrent+") and stopping threshold "+stopThreshold);
			}
		}
		System.out.println("Final Weights: ");
		System.out.println(weights.entrySet());
	}

	private void normalizeWeights(){
		double reduction_value = Collections.min(weights.values())-1;
		for (Energy key : weights.keySet()){
			weights.put(key, weights.get(key)-reduction_value);
		}
	}

	public void sample(int steps, int numsamples){
		try (FileOutputStream outFile = new FileOutputStream(new File(outputPath+".grds"));
			BufferedOutputStream bufferedOut = new BufferedOutputStream(outFile, 8192*16);
			ObjectOutputStream outObj = new ObjectOutputStream(bufferedOut))
		{
			run(steps*10);//warmup
			int[][] savableGrid;
			for (int i=0; i<numsamples; i++){
				run(steps);
				savableGrid = gDiagram.getSavableGrid();
				outObj.writeObject(savableGrid);
			}
			bufferedOut.flush();
		}catch (IOException e){
			System.err.println(e);
			System.exit(1);
		}
	}

	private void run(int steps){
		int movetype;
		int vertex;
		int moveSubtype;
		int insertedVertex;
			for (int i = 0; i<steps; i++){
				//TODO implement energies for translations
				/*if (rand.nextDouble() < 0.01){
					movetype = (int)(rand.nextDouble()*gDiagram.getSize()*gDiagram.getSize());
					gDiagram.translate(movetype%gDiagram.getSize(), movetype/gDiagram.getSize());
				}*/
				//else{
					movetype = (int)(rand.nextDouble()*(3));
					switch (movetype){
						case GridDiagram.MOVETYPE_COMMUTATION:
							vertex = (int)(rand.nextDouble()*gDiagram.getSize()*2);
							if (vertex%2 == 0){
								if (gDiagram.isCommuteRowValid(vertex/2) && calcAndCheckProbability(movetype, new int[]{vertex/2, GridDiagram.MOVE_SUBTYPE_ROW})) {
									gDiagram.commuteRow(vertex/2);
									updateCurrentEnergyFromNext();
								}
							}
							else{
								if (gDiagram.isCommuteColValid(vertex/2) && calcAndCheckProbability(movetype, new int[]{vertex/2, GridDiagram.MOVE_SUBTYPE_COLUMN})) {
									gDiagram.commuteCol(vertex/2);
									updateCurrentEnergyFromNext();
								}
							}
							break;
						case GridDiagram.MOVETYPE_DESTABILIZATION:
								if (gDiagram.getSize() > lowerSize) {
									vertex = (int) (rand.nextDouble() * gDiagram.getSize() * 2);
									moveSubtype = vertex % 2;
									vertex = vertex / 2;
									switch (moveSubtype) {
										case 0:
											moveSubtype = gDiagram.getRow(vertex).getXCol();
											break;
										case 1:
											moveSubtype = gDiagram.getRow(vertex).getOCol();
											break;
									}
									if (gDiagram.isDestabilizeRowValid(vertex)) {
										if (calcAndCheckProbability(movetype, new int[]{vertex, moveSubtype, GridDiagram.MOVE_SUBTYPE_ROW})) {
											gDiagram.destabilizeRow(vertex);
											updateCurrentEnergyFromNext();
										}
									} else if (gDiagram.isDestabilizeColValid(moveSubtype)) {
										if (calcAndCheckProbability(movetype, new int[]{vertex, moveSubtype, GridDiagram.MOVE_SUBTYPE_COLUMN})) {
											gDiagram.destabilizeCol(moveSubtype);
											updateCurrentEnergyFromNext();
										}
									}
								}
							break;
						case GridDiagram.MOVETYPE_STABILIZATION:
							if (gDiagram.getSize() < upperSize) {
								vertex = (int) (rand.nextDouble() * gDiagram.getSize() * 4);
								moveSubtype = vertex % 4;
								vertex = vertex / 4;
								insertedVertex = (int) (rand.nextDouble() * (gDiagram.getSize() + 1));
								if (calcAndCheckProbability(movetype, new int[]{vertex, insertedVertex, moveSubtype})) {
									switch (moveSubtype) {
										case GridDiagram.INSERT_XO_COLUMN:
										case GridDiagram.INSERT_OX_COLUMN:
											//System.out.println("Stabilize insert column"+" "+vertex+" "+insertedVertex);
											gDiagram.stabilize(vertex, insertedVertex, moveSubtype);
											break;
										case GridDiagram.INSERT_XO_ROW:
										case GridDiagram.INSERT_OX_ROW:
											//System.out.println("Stabilize insert row"+" "+insertedVertex+" "+vertex);
											gDiagram.stabilize(insertedVertex, vertex, moveSubtype);
											break;
									}
									updateCurrentEnergyFromNext();
								}
							}
							break;
						default:
							System.err.print("Invalid Grid Move [WangLandau:run()]");
							System.exit(1);
							break;
					}
				//}
				//Uncomment the next couple of lines to debug energy change calculations
				/*if (!currentEnergy.equals(new Energy(gDiagram))){
					System.out.println("error in energy: "+currentEnergy);
					System.out.println(currentEnergy);
					System.out.println(new Energy(gDiagram));
					System.out.println(movetype);
					System.exit(1);
				}*/
		}
	}
}