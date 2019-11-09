package griddiagrams.markovchain.canonicalalgorithm;//package grid_tools;

import griddiagrams.GridDiagram;

import java.util.ArrayList;

public class CanonicalGridAlgorithmOld {

	private GridDiagram gDiagram;
	private double z;
	ArrayList<Double[]> probabilities;

	public CanonicalGridAlgorithmOld(String knotName, double initZ){
		gDiagram = new GridDiagram(knotName);
		probabilities = new ArrayList<>();
		probabilities.add(new Double[]{1.0,1.0,1.0});
		z = initZ;
	}

	public GridDiagram getGridDiagram(){ return gDiagram; }


	private void run(int steps){
		int movetype;
		int vertex;
		int moveSubtype;
		int insertedVertex;
		int prevWrithe, delta;
			for (int i = 0; i<steps; i++){
				prevWrithe = gDiagram.calcWrithe();
				//delta = 0;
				gDiagram.printToTerminal();
				if (Math.random() < 0.01){
					movetype = (int)(Math.random()*gDiagram.getSize()*gDiagram.getSize());
					gDiagram.translate(movetype%gDiagram.getSize(), movetype/gDiagram.getSize());
				}
				else{
					movetype = (int)(Math.random()*(3));
					switch (movetype){
						case GridDiagram.MOVETYPE_COMMUTATION:
							vertex = (int)(Math.random()*gDiagram.getSize()*2);
							if (vertex%2 == 0){
								//System.out.println("commute row"+(vertex/2));
								//delta = gDiagram.deltaWrithe(movetype, new int[]{vertex/2, griddiagrams.GridDiagram.MOVE_SUBTYPE_ROW});
								gDiagram.commuteRowIfValid(vertex/2);
							}
							else{
								//System.out.println("commute column"+(vertex/2));
								//delta = gDiagram.deltaWrithe(movetype, new int[]{vertex/2, griddiagrams.GridDiagram.MOVE_SUBTYPE_COLUMN});
								gDiagram.commuteColIfValid(vertex/2);
							}
							/*if (prevWrithe+delta != gDiagram.calcWrithe()){
									System.out.println("writhe before: "+prevWrithe);
									System.out.println("deltaWrithe: "+delta);
									System.out.println("writhe after: "+gDiagram.calcWrithe());
									gDiagram.printToTerminal();
									System.exit(1);
							}*/
							break;
						case GridDiagram.MOVETYPE_DESTABILIZATION:
							if (Math.random() < getZPn(gDiagram.getSize(), -1)){
								vertex = (int)(Math.random()*gDiagram.getSize()*2);
								moveSubtype = vertex%2;
								vertex = vertex/2;
								switch (moveSubtype){
								case 0:
									moveSubtype = gDiagram.getRow(vertex).getXCol();
									break;
								case 1:
									moveSubtype = gDiagram.getRow(vertex).getOCol();
									break;
								}
								if (gDiagram.isDestabilizeRowValid(vertex)){
									//delta = gDiagram.deltaWrithe(movetype, new int[]{vertex, moveSubtype, griddiagrams.GridDiagram.MOVE_SUBTYPE_ROW});
									gDiagram.destabilizeRow(vertex);
								}
								else if (gDiagram.isDestabilizeColValid(moveSubtype)){
									//delta = gDiagram.deltaWrithe(movetype, new int[]{vertex, moveSubtype, griddiagrams.GridDiagram.MOVE_SUBTYPE_COLUMN});
									gDiagram.destabilizeCol(moveSubtype);
								}
								/*if (prevWrithe+delta != gDiagram.calcWrithe()){
									System.out.println("writhe before: "+prevWrithe);
									System.out.println("deltaWrithe: "+delta);
									System.out.println("writhe after: "+gDiagram.calcWrithe());
									gDiagram.printToTerminal();
									System.exit(1);
								}*/
							}
							break;
						case GridDiagram.MOVETYPE_STABILIZATION:
							if (Math.random() < getZPn(gDiagram.getSize(), 1)){
								vertex = (int)(Math.random()*gDiagram.getSize()*4);
								moveSubtype = vertex%4;
								vertex = vertex/4;
								insertedVertex = (int)(Math.random()*(gDiagram.getSize()+1));
								//delta = gDiagram.deltaWrithe(movetype, new int[]{vertex, insertedvertex, moveSubtype})
								switch (moveSubtype){
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
							}
							break;
					}
				}
				//gDiagram.printToTerminal();
				/*if (!gDiagram.isRowMatchColumns()){
					System.out.println("row column mismatch");
					break;
				}*/
		}
	}
	private double getZPn(int n, int delta){
		while (probabilities.size() < n+1){
			probabilities.add(new Double[]{1.0, Math.min(4*z/probabilities.size(), 1.0), Math.min((probabilities.size()-1)/(4*z), 1.0)});
		}
		return probabilities.get(n)[(delta+3)%3];
	}
}