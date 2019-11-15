import griddiagrams.GridDiagram;
import griddiagrams.markovchain.canonicalalgorithm.CanonicalGridAlgorithm;

import java.util.List;

public class TestCanonicalGridAlgorithm {


    public static void main(String[] args) {
        CanonicalGridAlgorithm canonicalGridAlgorithm = new CanonicalGridAlgorithm(.9);
        GridDiagram gd = new GridDiagram(new int[]{0,1}, new int[]{1,0});
        List<GridDiagram> samples = canonicalGridAlgorithm.sample(30, 10000, gd);
        for (GridDiagram sample : samples){
            System.out.println(sample.toString());
        }
    }
}
