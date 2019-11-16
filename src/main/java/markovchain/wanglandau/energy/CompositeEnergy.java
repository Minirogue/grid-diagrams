package markovchain.wanglandau.energy;

import markovchain.MarkovMove;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CompositeEnergy<MarkovState, MM extends MarkovMove<MarkovState>> extends WangLandauEnergy<MarkovState, MM, CompositeEnergy<MarkovState, MM>> {

    //Stores the list of individual energy states
    private final List<WangLandauEnergy<MarkovState, MM, ?>> energy;

    /**
     * Constructs a CompositeEnergy out of the a List of WangLandauEnergy objects.
     *
     * @param energy List of WangLandauEnergy objects which, together, define a full energy state.
     */
    private CompositeEnergy(List<WangLandauEnergy<MarkovState, MM, ?>> energy) {
        this.energy = energy;
    }

    /**
     * @return Get the set of energy values that comprise this CompositeEnergy as a List.
     */
    private List<WangLandauEnergy<MarkovState, MM, ?>> getEnergyList() {
        return energy;
    }

    /**
     * @param moveToNextState A proposed MarkovMove
     * @return A CompositeEnergy for the state obtained from moveToNextState that is made of WangLandauEnergy objects of the same types as this object.
     */
    @NotNull
    @Override
    public CompositeEnergy<MarkovState, MM> getNextEnergyFromMove(@NotNull MM moveToNextState) {
        List<WangLandauEnergy<MarkovState, MM, ?>> newEnergyList = new ArrayList<>(getEnergyList().size());
        for (WangLandauEnergy<MarkovState, MM, ?> en : getEnergyList()) {
            newEnergyList.add(en.getNextEnergyFromMove(moveToNextState));
        }
        return new CompositeEnergy<>(newEnergyList);
    }

    @NotNull
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

    /**
     * @param o Object to compare.
     * @return True if o is a CompositeEnergy that contains energies of the same type, in the same order, that are equal.
     */
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


    /**
     * @return A string formatted as a tuple of the energy values that make up this energy.
     */
    @NotNull
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

        //Factories for each energy being composed
        private final List<Factory<MarkovState, MM, ?>> factories;

        /**
         * @param factories A List of WangLandauEnergy.Factory objects defining the energies to be composed
         */
        public CompositeEnergyFactory(List<Factory<MarkovState, MM, ?>> factories) {
            this.factories = factories;
        }


        @NotNull
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
