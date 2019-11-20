package griddiagrams.markovchain.wanglandau;

import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import markovchain.wanglandau.energy.CompositeEnergy;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.ArrayList;
import java.util.List;

public class TestGridWangLandau {

    public static void main(String[] args) {
        List<WangLandauEnergy.WangLandauEnergyFactory<GridDiagram, GridMove, ?>> factoryList = new ArrayList<>();
        factoryList.add(new SizeEnergy.SizeEnergyFactory());
        factoryList.add(new WritheEnergy.WritheEnergyFactory());
        CompositeEnergy.CompositeEnergyFactory<GridDiagram, GridMove> compositeEnergyFactory =
                new CompositeEnergy.CompositeEnergyFactory<>(factoryList);
        GridDiagramWangLandau<CompositeEnergy<GridDiagram,GridMove>> alg = new GridDiagramWangLandau<>(compositeEnergyFactory, 5);
        GridDiagram gd = new GridDiagram(new int[]{0, 1}, new int[]{1, 0});


        System.out.println(alg.train(gd, 100, 1));


    }

}
