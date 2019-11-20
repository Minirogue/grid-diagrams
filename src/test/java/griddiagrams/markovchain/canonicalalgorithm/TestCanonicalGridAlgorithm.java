package griddiagrams.markovchain.canonicalalgorithm;

import griddiagrams.GridDiagram;

import java.util.List;

public class TestCanonicalGridAlgorithm {


    public static void main(String[] args) {
        CanonicalGridAlgorithm canonicalGridAlgorithm = new CanonicalGridAlgorithm(.9);
        GridDiagram gd = new GridDiagram(new int[]{0,1}, new int[]{1,0});
        List<GridDiagram> samples = canonicalGridAlgorithm.sample(gd, 10000, 30);
        for (GridDiagram sample : samples){
            System.out.println(sample.toString());
        }
    }
}
