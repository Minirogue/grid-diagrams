import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;

public class WangLandau {


	private GridDiagram gDiagram;
	//private int mode; use if this is merged with regular grid algorithm
	private int upperSize;
	private int lowerSize;
	private Energy currentEnergy;
	private Energy nextEnergy;
	private HashMap<Energy, Double> weights;
	private String saveFile;
	private double defaultWeight = 0.0;


	public WangLandau(String knotName, int[] initEnergyType){
		gDiagram = new GridDiagram(knotName);
		Energy.setEnergyType(initEnergyType);
		currentEnergy = new Energy(gDiagram);
		weights = new HashMap<>();
		upperSize = 100;//TODO don't hardcode
		lowerSize = 0;//TODO don't hardcode
		gDiagram.printToTerminal();
	}

	public void setWeightsSaveFile(String filename){ saveFile = filename; }
	public void setUpperSize(int newbound){ upperSize = newbound; }
	public void setLowerSize(int newbound){ lowerSize = newbound; }

	public HashMap<Energy, Double> getWeights(){ return weights; }
	public void setDefaultWeightToMax(){
		if (weights.isEmpty()){
			defaultWeight = 0;
		}
		else{
			defaultWeight = Collections.min(weights.values());
		}
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
			System.out.println("File not found");
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
		if (saveFile != null){
			try{
				FileOutputStream outFile = new FileOutputStream(new File(saveFile));
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
		switch (movetype) {
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
		return Math.log(Math.random()) < prob;
	}


	public void train(int steps, int flatCheckFreq, double fStart, double fFinal, double fModFactor){
		HashMap<Energy, Integer> histogram = new HashMap<>();
		double fCurrent = fStart;
		double currentWeight;//currentMaxWeight, currentMinWeight, 
		int currentMinHistogram, currentMaxHistogram;
		/*currentMinWeight = Integer.MAX_VALUE;
		currentMaxWeight = Integer.MIN_VALUE;
		for (HashMap.Entry<Energy, Double> entry : weights.entrySet()){
			histogram.put(entry.getKey(), 0);
			currentWeight = entry.getValue();
			if (currentWeight < currentMinWeight){
				currentMinWeight = currentWeight;
			}
			else if (currentMaxWeight < currentWeight){
				currentMaxWeight = currentWeight;
			}
		}*/
		for (Energy key : weights.keySet()){
			histogram.put(key, 0);
		}
		run(steps*10);//warmup
		while (fCurrent > fFinal){
			for (int i = 0; i<flatCheckFreq; i++){
				run(steps);
				currentWeight = weights.getOrDefault(currentEnergy, defaultWeight)+fCurrent;//TODO could the 0 here be replaced with some function of the existing weights?
				/*if (currentMaxWeight < currentWeight){
					currentMaxWeight = currentWeight;
				}*/
				weights.put(currentEnergy,currentWeight);
				histogram.put(currentEnergy, histogram.getOrDefault(currentEnergy,0)+1);
			}
			currentMinHistogram = Collections.min(histogram.values());
			currentMaxHistogram = Collections.max(histogram.values());//TODO maybe there's a better way to track these
			if (currentMinHistogram > .8*currentMaxHistogram){
				normalizeWeights();
				System.out.println("Saving weights");
				System.out.println("Keys "+weights.keySet());
				System.out.println("Values "+weights.values());
				saveWeightsToFile();
				fCurrent = fCurrent*fModFactor;
				for (Energy key : histogram.keySet()){
					histogram.put(key, 0);
				}
			}
		}
		System.out.println("Final Weights: ");
		System.out.println(weights.keySet());
		System.out.println(weights.values());
	}

	private void normalizeWeights(){
		double reduction_value = Collections.min(weights.values())-1;
		for (Energy key : weights.keySet()){
			weights.put(key, weights.get(key)-reduction_value);
		}
	}

	private void run(int steps){
		int movetype;
		int vertex;
		int moveSubtype;
		int insertedVertex;
			for (int i = 0; i<steps; i++){
				//TODO implement energies for translations
				/*if (Math.random() < 0.01){
					movetype = (int)(Math.random()*gDiagram.getSize()*gDiagram.getSize());
					gDiagram.translate(movetype%gDiagram.getSize(), movetype/gDiagram.getSize());
				}*/
				//else{
					movetype = (int)(Math.random()*(3));
					switch (movetype){
						case GridDiagram.MOVETYPE_COMMUTATION:
							vertex = (int)(Math.random()*gDiagram.getSize()*2);
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
									vertex = (int) (Math.random() * gDiagram.getSize() * 2);
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
								vertex = (int) (Math.random() * gDiagram.getSize() * 4);
								moveSubtype = vertex % 4;
								vertex = vertex / 4;
								insertedVertex = (int) (Math.random() * (gDiagram.getSize() + 1));
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