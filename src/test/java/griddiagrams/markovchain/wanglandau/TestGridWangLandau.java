import griddiagrams.GridDiagram;
import griddiagrams.markovchain.GridMove;
import griddiagrams.markovchain.wanglandau.GridDiagramWangLandau;
import griddiagrams.markovchain.wanglandau.SizeEnergy;
import griddiagrams.markovchain.wanglandau.WritheEnergy;
import markovchain.wanglandau.energy.CompositeEnergy;
import markovchain.wanglandau.energy.WangLandauEnergy;

import java.util.ArrayList;
import java.util.List;

public class TestGridWangLandau {

    public static void main(String[] args) {
        List<WangLandauEnergy.Factory<GridDiagram, GridMove, ?>> factoryList = new ArrayList<>();
        factoryList.add(new SizeEnergy.SizeEnergyFactory());
        factoryList.add(new WritheEnergy.Factory());
        CompositeEnergy.CompositeEnergyFactory<GridDiagram, GridMove> compositeEnergyFactory =
                new CompositeEnergy.CompositeEnergyFactory<>(factoryList);
        GridDiagramWangLandau<CompositeEnergy<GridDiagram,GridMove>> alg = new GridDiagramWangLandau<>(5, compositeEnergyFactory);
        GridDiagram gd = new GridDiagram(new int[]{0, 1}, new int[]{1, 0});


        System.out.println(alg.train(gd, null, 100, 1));


    }

}
