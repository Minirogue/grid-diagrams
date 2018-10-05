//package grid_tools;

import java.io.Serializable;

class Energy implements Serializable {
    public static final long serialVersionUID = 0;

    public static final int ENERGYTYPE_SIZE = 1;
    public static final int ENERGYTYPE_WRITHE = 2;

    private static int[] energyType;

    private final Serializable[] energyStates;
    private int cachedHash;
    private boolean isHashCached;


    public Energy(GridDiagram gDiagram){
        energyStates = new Serializable[energyType.length];
        for (int i = 0; i < energyType.length; i++) {
            switch (energyType[i]) {
                case ENERGYTYPE_SIZE:
                    energyStates[i] = gDiagram.getSize();
                    break;
                case ENERGYTYPE_WRITHE:
                    energyStates[i] = gDiagram.calcWrithe();
                    break;
            }
        }
        isHashCached = false;
    }
    public Energy(Energy currentEnergy, GridDiagram gDiagram, int movetype, int[] arguments){
        Serializable[] currentEnergyArr = currentEnergy.getEnergyState();
        energyStates = new Serializable[currentEnergyArr.length];
        for (int i=0; i<energyType.length; i++){
            switch (energyType[i]){
                case ENERGYTYPE_SIZE:
                    energyStates[i] = (int)currentEnergyArr[i] + GridDiagram.deltaSize(movetype);
                    break;
                case ENERGYTYPE_WRITHE:
                    energyStates[i] = (int)currentEnergyArr[i] + gDiagram.deltaWrithe(movetype, arguments);
                    break;
            }
        }
        isHashCached = false;
    }

    public Serializable[] getEnergyState(){
        return energyStates;
    }

    public static void setEnergyType(int[] newEnergyType){
        energyType = newEnergyType;
    }


    @Override
    public boolean equals(Object o){
        if (o instanceof Energy) {
            Serializable[] otherEnergyArr = ((Energy) o).getEnergyState();
            if (otherEnergyArr.length != energyStates.length) {
                return false;
            }
            for (int i = 0; i < energyStates.length; i++) {
                if (!otherEnergyArr[i].equals(energyStates[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        if (!isHashCached){
            int calcHash = 0;
            for (int i=0; i<energyStates.length; i++){
                switch (energyType[i]){
                    case ENERGYTYPE_SIZE:
                        calcHash = (cachedHash*31)+(int)energyStates[i];
                        break;
                    case ENERGYTYPE_WRITHE:
                        calcHash = (cachedHash*31)+(int)energyStates[i];
                        break;
                }
            }
            cachedHash = calcHash;
            isHashCached = true;
        }
        return cachedHash;
    }

    @Override
    public String toString() {
        String thisString = "(";
        for (int i = 0; i < energyStates.length; i++) {
            thisString += energyStates[i].toString();
            if (i < energyStates.length - 1) {
                thisString += ", ";
            }
        }
        return thisString+")";
    }
}