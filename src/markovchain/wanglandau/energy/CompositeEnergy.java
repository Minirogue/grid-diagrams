package markovchain.wanglandau.energy;

import markovchain.MarkovMove;

import java.util.ArrayList;
import java.util.List;

public class CompositeEnergy<MarkovState, MM extends MarkovMove<MarkovState>> extends WangLandauEnergy<MarkovState, MM, CompositeEnergy<MarkovState, MM>> {

    private List<WangLandauEnergy<MarkovState, MM, ?>> energy;

    private CompositeEnergy(List<WangLandauEnergy<MarkovState, MM, ?>> energy) {
        this.energy = energy;
    }

    private List<WangLandauEnergy<MarkovState, MM, ?>> getEnergyList() {
        return energy;
    }

    @Override
    public CompositeEnergy<MarkovState, MM> getNextEnergyFromMove(MM moveToNextState) {
        List<WangLandauEnergy<MarkovState, MM, ?>> newEnergyList = new ArrayList<>(getEnergyList().size());
        for (WangLandauEnergy<MarkovState, MM, ?> en : getEnergyList()) {
            newEnergyList.add(en.getNextEnergyFromMove(moveToNextState));
        }
        return new CompositeEnergy<>(newEnergyList);
    }

    @Override
    public CompositeEnergy<MarkovState, MM> copy() {
        List<WangLandauEnergy<MarkovState, MM, ?>> newEnergy = new ArrayList<>(getEnergyList().size());
        for (WangLandauEnergy<MarkovState, MM, ?> oldEnergy : getEnergyList()) {
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

    public static class CompositeEnergyFactory<MarkovState, MM extends MarkovMove<MarkovState>> extends Factory<MarkovState, MM, CompositeEnergy<MarkovState, MM>> {

        private List<Factory<MarkovState, MM, ?>> factories;

        public CompositeEnergyFactory(List<Factory<MarkovState, MM, ?>> factories) {
            this.factories = factories;
        }


        @Override
        public CompositeEnergy<MarkovState, MM> getEnergyFromState(MarkovState markovState) {
            List<WangLandauEnergy<MarkovState, MM, ?>> newEnergy = new ArrayList<>();
            for (Factory<MarkovState, MM, ?> factory : factories) {
                newEnergy.add(factory.getEnergyFromState(markovState));
            }
            return new CompositeEnergy<>(newEnergy);
        }
    }
}
