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
import java.util.Arrays;

public class WangLandau {


	protected GridDiagram gDiagram;
	//private int mode; use if this is merged with regular grid algorithm
	protected int upperSize;
	protected int lowerSize;
	protected Energy currentEnergy;
	protected Energy nextEnergy;
	protected HashMap<Energy, Double> weights;
	protected HashMap<Energy, Double> estimatedError;
	protected String outputPath;
	protected double defaultWeight = 0.0;
	protected boolean makeMovie = false;
	protected Random rand = new Random();
	protected boolean generalizedStabilizations = true;


	public WangLandau(String knotName, int[] initEnergyType){
		gDiagram = new GridDiagram(knotName);
		Energy.setEnergyType(initEnergyType);
		calcAndSetCurrentEnergy();
		weights = new HashMap<>();
		estimatedError = new HashMap<>();
		upperSize = 100;//TODO don't hardcode
		lowerSize = 0;//TODO don't hardcode
		gDiagram.printToTerminal();
	}

	/**
	*	Set the filepath for all output files.
	*	@param filename The filepath for output .wts, .grds, etc. files. Do not include an extension.
	*/
	public void setOutputPath(String filename){ outputPath = filename; }
	/**
	*	Sets the maximum grid size to use in the state space.
	*	Default value is 100.
	*	@param newbound The new upper bound on grid size.
	*/
	public void setUpperSize(int newbound){ upperSize = newbound; }
	/**
	*	Sets the minimum grid size to use in the state space.
	*	Default value is 0.
	*	If this value is below the arc index of the link being explored, then it is effectively just the arc index.
	*	@param newbound The new lower bound on grid size.
	*/
	public void setLowerSize(int newbound){ lowerSize = newbound; }
	/**
	*	Set the seed for the random number generator for replication of runs,
	*	or to ensure that different runs are not identical, (which may happen if they are run simultaneously).
	*	@param newSeed The seed ot use.
	*/
	public void setRandomSeed(long newSeed){
		rand.setSeed(newSeed);
	}
	/**
	*	Set whether to use generalized stabilizations (true) or regular stabilizations (false).
	*	Default true.
	*	@param newval boolean determining whether to use generalized stabilizations or not
	*/
	public void setGeneralizedStabilizations(boolean newval){
		generalizedStabilizations = newval;
	}

	/**
	*	Get the current weights being used by this instance of Wang-Landau.
	*	The weights are stored in a HashMap<Energy,Double>
	*	@return Wang-Landau weights
	*/
	public HashMap<Energy, Double> getWeights(){ return weights; }
	/**
	*	Finds the largest current weight and uses that as the starting weight for all newly discovered
	*	weights during training instead of using 0.
	*	@deprecated ?
	*/
	public void setDefaultWeightToMax(){
		if (weights.isEmpty()){
			defaultWeight = 0;
		}
		else{
			defaultWeight = Collections.min(weights.values());
		}
	}

	//TODO get rid of this?
	public void setMakeMovie(boolean newval){
		makeMovie = newval;
	}
	/**
	*	Initializes the Wang-Landau weights from a file
	*	@param filename The filepath to load the weights from. Include the extension.
	*/
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
	/**
	*	Saves the current weights to the file specified by outputPath
	*/
	protected boolean saveWeightsToFile(){
		if (outputPath != null){
			try{
				FileOutputStream outFile = new FileOutputStream(new File(outputPath+".wts"));
				ObjectOutputStream outObj = new ObjectOutputStream(outFile);
				outObj.writeObject(weights);
				outObj.writeObject(estimatedError);
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
	//TODO remove?
	protected void printToMovie(HashMap<Energy, Integer> histogram){//WARNING: this assumes size is energy
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
	/**
	*	@return the current grid diagram
	*/
	public GridDiagram getGridDiagram(){ return gDiagram; }

	/**
	*	Transfer Energy object in nextEnergy to currentEnergy.
	*	This should be done after a move is successfully performed.
	*/
	protected void updateCurrentEnergyFromNext(){
		currentEnergy = nextEnergy;
	}
	/**
	*	Set currentEnergy to an Energy object calculated from the current grid diagram.
	*/
	public void calcAndSetCurrentEnergy(){
		currentEnergy = new Energy(gDiagram);
	}
	/**
	*	Set nextEnergy according to the current grid diagram and a proposed move to perform on it.
	*	@param movetype the proposed move as a MOVETYPE constant from the GridDiagram class
	*	@param arguments the list of arguments which would would be fed to the move's method when performed
	*/
	public void calcAndSetNextEnergy(int movetype, int[] arguments){
		nextEnergy = new Energy(currentEnergy, gDiagram, movetype, arguments);
	}

	/**
	*	Takes a proposed move, calculates the acceptance probability based on the Wang-Landau weights,
	*	then tests if it is accepted and returns whether or not the move should be performed.
	*	I.e. this is the Metropolis-Hastings step.
	*	@param movetype the proposed move as a MOVETYPE constant from the GridDiagram class
	*	@param arguments the list of arguments which would would be fed to the move's method when performed
	*	@return true if the move is accepted, false if it is rejected
	*/
	public boolean calcAndCheckProbability(int movetype, int[] arguments){
		calcAndSetNextEnergy(movetype, arguments);
		double prob;
		switch (movetype) {
			case GridDiagram.MOVETYPE_COMMUTATION:
				prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0);
				break;
			case GridDiagram.MOVETYPE_DESTABILIZATION:
				if (generalizedStabilizations){
					prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0) - Math.log(2*(gDiagram.getSize()-1));
				}else{
					prob = weights.getOrDefault(currentEnergy, 0.0) - weights.getOrDefault(nextEnergy, 0.0)+Math.log(gDiagram.getSize())-Math.log(4*(gDiagram.getSize()-1));
				}
				break;
			case GridDiagram.MOVETYPE_STABILIZATION:
				if (generalizedStabilizations){
					prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0) + Math.log(2*gDiagram.getSize());
				}else{
					prob = weights.getOrDefault(currentEnergy, 0.0)-weights.getOrDefault(nextEnergy, 0.0)+Math.log(4*gDiagram.getSize())-Math.log(gDiagram.getSize()+1);
				}
				break;
			default:
				prob = -999999;
				System.out.println("Error with calculateProbabilityOfMove: moveType not valid");
				System.exit(1);
		}
		return Math.log(rand.nextDouble()) < prob;
	}

	/**
	*	Reset all entries in the histogram to 0.
	*	Usually performed after the modification factor is updated.
	*	@param histogram The histogram to be cleared.
	*/
	protected void clearHistogram(HashMap<Energy, Integer> histogram){
		for (Energy key : weights.keySet()){
			histogram.put(key, 0);
		}
	}

	/**
	*	Runs the Wang-Landau algorithm, periodically updating and saving the Wang-Landau weights.
	*	@param steps The number of attempted steps to perform between weight updates
	*	@param flatCheckFreq The number of weight updates to perform before checking whether or not to move to the next f.
	*	This is also when the weights are saved to the output file.
	*	@param fStart The first value of f to use. This number is added to the stored weights when they are updated.
	*	@param fFinal The training ends when f is less than this value.
	*	@param fModFactor f is multiplied by this number every time it is updated.
	*/
	public void train(int steps, int flatCheckFreq, double fStart, double fFinal, double fModFactor){
		HashMap<Energy, Integer> histogram = new HashMap<>();
		//boolean isFirstF = true;
		double fCurrent = fStart;
		double currentWeight;//currentMaxWeight, currentMinWeight,
		Energy groundState = new Energy(gDiagram);
		/*if (makeMovie){
			try{
				Files.deleteIfExists(Paths.get(outputPath+"_weights.txt"));
				Files.deleteIfExists(Paths.get(outputPath+"_histogram.txt"));
			}catch (IOException e){
				System.out.println(e);
			}

		}*/
		clearHistogram(histogram);//initialize histogram with all known energy states
		histogram.put(currentEnergy, 0);//initialize histogram entry for starting state. This is to help with the stopping condition.
		//run(steps*10);//warmup
		System.out.println("fCurrent "+fCurrent);
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
			if (checkFlat(histogram, fCurrent, fModFactor)){//+(isFirstF ? 10 : 0)){
				System.out.println("Histogram: "+histogram.entrySet());
				System.out.println("Weights: "+weights.entrySet());
				System.out.println("Estimated Error: "+estimatedError.entrySet());
				saveWeightsToFile();
				fCurrent = fCurrent*fModFactor;
				//System.out.println("estimated_sigma(delta) sqrt(fCurrent*largest/smallest)");
				System.out.println("fCurrent "+fCurrent);
				//isFirstF = false;
				clearHistogram(histogram);
			}
		}
		//System.out.println("Final Weights: ");
		//System.out.println(weights.entrySet());
	}

	/**
	*	Returns true if there are enough samples to justify moving to the next modification factor
	*/
	protected boolean checkFlat(HashMap<Energy, Integer> histogram, double fCurrent, double fModFactor){
		boolean isFlat = true;
		double currentWeight;
		double neighborWeight;
		for (Energy key : histogram.keySet()){
			currentWeight = weights.getOrDefault(key, 0.0);
			if (currentWeight == 0.0){
				return false;
			}
			Energy[] neighborhood = key.getNeighborhood();
			neighborWeight = currentWeight;
			for (Energy neighbor : neighborhood){
				if (0.0 < weights.getOrDefault(neighbor, 0.0) && weights.getOrDefault(neighbor, 0.0) < neighborWeight){
					neighborWeight = weights.getOrDefault(neighbor, 0.0);
				}
			}
			//System.out.println(""+key+" "+currentWeight);
			/*if (1.0/(2.0*fCurrent)*(neighborWeight-currentWeight+Math.log(1.0/(fModFactor*fCurrent))) < 0){
				System.out.println(Arrays.toString(neighborhood));
				System.out.println("fCurrent "+fCurrent);
				System.out.println("fModFactor "+fModFactor);
				System.out.println("weight "+currentWeight);
				System.out.println("smallest neighbor "+neighborWeight);
				System.out.println("threshold "+1.0/(2.0*fCurrent)*(neighborWeight-currentWeight+Math.log(1.0/(fModFactor*fCurrent)))+"\n");
				//System.out.println(""+weights);
			}*/
			if (histogram.get(key) < 1.0/(2.0*fCurrent)*(neighborWeight-currentWeight+Math.log(1.0/(fModFactor*fCurrent)))){
				//isFlat = false;
				return false;
			}
			estimatedError.put(key, Math.sqrt(Math.exp(currentWeight-neighborWeight)*fCurrent));
		}
		return isFlat;
	}


	/**
	*	Since the important info stored in the weights list is the difference between the weights, they can all be subtracted
	*	by the same number and the weights will represent the same information. This method renormalizes the weights all to the
	*	smallest value. This helps avoid overflow, especially if considering the exponentiation of these weights.
	*/
	protected void normalizeWeights(){
		double reduction_value = Collections.min(weights.values())-1;
		for (Energy key : weights.keySet()){
			weights.put(key, weights.get(key)-reduction_value);
		}
	}

	/**
	*	Using the Wang-Landau weights, sample grid diagrams. They are saved to outputpath+".grds".
	*	@param steps The number of steps to take between samples.
	*	@param numsamples The number of samples to take.
	*/
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

	/**
	*	Take steps along the Markov chain defined by the Wang-Landau weights.
	*	@param steps The number of steps to take
	*/
	protected void run(int steps){
		int movetype;
		int vertex;
		int moveSubtype;
		int insertedVertex;
			for (int i = 0; i<steps; i++){
				//uncomment next line to help with energy delta debugging
				//System.out.println(gDiagram);
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
								if (generalizedStabilizations){
									if (gDiagram.getSize() > lowerSize) {
										vertex = (int)(rand.nextDouble() * gDiagram.getSize() * 2);
										moveSubtype = vertex % 2;
										vertex = vertex / 2;
										switch (moveSubtype) {
											case 0:
												if (gDiagram.isDestabilizeRowValid(vertex)) {
													if (calcAndCheckProbability(movetype, new int[]{vertex, GridDiagram.MOVE_SUBTYPE_ROW})) {
														gDiagram.destabilizeRow(vertex);
														updateCurrentEnergyFromNext();
													}
												}
												break;
											case 1:
												if (gDiagram.isDestabilizeColValid(vertex)) {
													if (calcAndCheckProbability(movetype, new int[]{vertex, GridDiagram.MOVE_SUBTYPE_COLUMN})) {
														gDiagram.destabilizeCol(vertex);
														updateCurrentEnergyFromNext();
													}
												}
												break;
										}
									}
								}else{
									if (gDiagram.getSize() > lowerSize){
										vertex = (int)(rand.nextDouble() * gDiagram.getSize() * 2);
										if (gDiagram.isDestabilizeRowValid(vertex/2) && ((vertex%2==0 && gDiagram.isDestabilizeColValid(gDiagram.getRow(vertex/2).getXCol())) || (vertex%2==1 && gDiagram.isDestabilizeColValid(gDiagram.getRow(vertex/2).getOCol())))){
											if(calcAndCheckProbability(movetype, new int[]{vertex/2, GridDiagram.MOVE_SUBTYPE_ROW})){
												gDiagram.destabilizeRow(vertex/2);
												updateCurrentEnergyFromNext();
											}
										}
						
									}
								}
							break;
						case GridDiagram.MOVETYPE_STABILIZATION:
							if (generalizedStabilizations){
								if (gDiagram.getSize() < upperSize) {
									vertex = (int)(rand.nextDouble() * gDiagram.getSize() * 4);
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
							}else{
								if (gDiagram.getSize() < upperSize) {
									vertex = (int)(rand.nextDouble() * gDiagram.getSize() * 2);
									if (vertex%2 == 0){
										insertedVertex = gDiagram.getRow(vertex/2).getXCol();
									}else{
										insertedVertex = gDiagram.getRow(vertex/2).getOCol();
									}
									vertex = vertex/2;
									moveSubtype = (int)(rand.nextDouble() * 4);
									insertedVertex += moveSubtype%2;
									moveSubtype = moveSubtype/2;
									if (calcAndCheckProbability(movetype, new int[]{vertex, insertedVertex, moveSubtype})) {
										gDiagram.stabilize(vertex, insertedVertex, moveSubtype);
										updateCurrentEnergyFromNext();
									}
								}
							}
							break;
						default:
							System.err.print("Invalid Grid Move [WangLandau:run()]");
							System.exit(1);
							break;
					}
				//}
				//Uncomment the following block to debug energy change calculations
				/*if (!currentEnergy.equals(new Energy(gDiagram))){
					System.err.println("error in energy: "+currentEnergy);
					System.err.println(currentEnergy);
					System.err.println(new Energy(gDiagram));
					System.err.println(movetype);
					System.err.println(gDiagram);
					System.exit(1);
				}*/
		}
	}
}