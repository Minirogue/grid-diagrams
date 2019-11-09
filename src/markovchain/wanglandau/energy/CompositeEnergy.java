package markovchain.wanglandau.energy;

import markovchain.MarkovMove;

import java.util.ArrayList;
import java.util.List;

class CompositeEnergy<MarkovState> extends WangLandauEnergy<MarkovState, CompositeEnergy<MarkovState>> {

    private List<WangLandauEnergy<MarkovState, ?>> energy;

    private CompositeEnergy(List<WangLandauEnergy<MarkovState, ?>> energy) {
        this.energy = energy;
    }

    private List<WangLandauEnergy<MarkovState, ?>> getEnergyList() {
        return energy;
    }

    @Override
    public CompositeEnergy<MarkovState> nextEnergyFromMove(MarkovState markovState, MarkovMove<MarkovState> moveToNextState) {
        List<WangLandauEnergy<MarkovState, ?>> newEnergyList = new ArrayList<>(getEnergyList().size());
        for (WangLandauEnergy<MarkovState, ?> en : getEnergyList()){
            newEnergyList.add(en.nextEnergyFromMove(markovState, moveToNextState));
        }
        return new CompositeEnergy<>(newEnergyList);
    }

    @Override
    public CompositeEnergy<MarkovState> copy() {
        List<WangLandauEnergy<MarkovState, ?>> newEnergy = new ArrayList<>(getEnergyList().size());
        for (WangLandauEnergy<MarkovState, ?> oldEnergy : getEnergyList()){
            newEnergy.add(oldEnergy.copy());
        }
        return new CompositeEnergy<>(newEnergy);
    }

    @Override
    public int hashCode() {
        int newHash = 0;
        for (WangLandauEnergy componentEnergy : energy) {
            newHash = newHash * 31 ^ componentEnergy.hashCode();
        }
        return newHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CompositeEnergy) {
            List otherEnergyArr = ((CompositeEnergy) o).getEnergyList();
            if (otherEnergyArr.size() != energy.size()) {
                return false;
            }
            for (int i = 0; i < energy.size(); i++) {
                if (!otherEnergyArr.get(i).equals(energy.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < energy.size(); i++) {
            stringBuilder.append(energy.get(i).toString());
            if (i < energy.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public static class CompositeEnergyFactory<MarkovState> extends EnergyFactory<MarkovState, CompositeEnergy<MarkovState>> {

        private List<EnergyFactory<MarkovState, ?>> factories;

        public CompositeEnergyFactory(List<EnergyFactory<MarkovState, ?>> factories){
            this.factories = factories;
        }


        @Override
        public CompositeEnergy<MarkovState> getEnergyFromState(MarkovState markovState) {
            List<WangLandauEnergy<MarkovState, ?>> newEnergy = new ArrayList<>(factories.size());
            for (EnergyFactory<MarkovState, ?> factory : factories){
                newEnergy.add(factory.getEnergyFromState(markovState));
            }
            return new CompositeEnergy<>(newEnergy);
        }
    }
}
