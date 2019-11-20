package griddiagrams.markovchain

import griddiagrams.GridDiagram
import griddiagrams.markovchain.canonicalalgorithm.CanonicalGridAlgorithm
import griddiagrams.markovchain.wanglandau.GridDiagramWangLandau
import griddiagrams.markovchain.wanglandau.SizeEnergy
import griddiagrams.markovchain.wanglandau.WritheEnergy
import markovchain.wanglandau.energy.CompositeEnergy
import markovchain.wanglandau.energy.WangLandauEnergy
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.ArgumentParserException


const val CANONICAL_ALGORITHM = "canonical"
const val WANG_LANDAU_TRAINING = "waing-landau-training"

fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("prog").build()
            .description("Run grid diagram Markov chains. Can run either canonical-like or Wang-Landau")

    val subparsers = parser.addSubparsers()

    val wangLandauParser = subparsers.addParser("wanglandau-train")//.aliases("wl", "wang-landau")
            .setDefault("algorithm", WANG_LANDAU_TRAINING)
    wangLandauParser.addArgument("-k", "--knot-type")
            .dest("knot_name")
            .required(true)
            .type(String::class.java)
            .help("The knot type used in the algorithm.")
    wangLandauParser.addArgument("--step-size")
            .required(true)
            .type(Int::class.java)
            .dest("step-size")
            .help("Number of steps to take between weight adjustments")
    wangLandauParser.addArgument("--max-size")
            .required(true)
            .type(Int::class.java)
            .dest("max-size")
            .help("The upper bound (inclusive) on grid sizes")
    wangLandauParser.addArgument("-f")
            .required(true)
            .type(Double::class.java)
            .dest("f")
            .help("The log of the update factor. Weights will be updated according to log(weight) += log(f).")
    //The following arguments are for energy types.
    wangLandauParser.addArgument("-w", "--writhe")
            .dest("energy")
            .action(Arguments.appendConst())
            .setConst(WritheEnergy.WritheEnergyFactory())
            .help("Include writhe as an energy component.")
    wangLandauParser.addArgument("-s", "--grid-size")
            .dest("energy")
            .action(Arguments.appendConst())
            .setConst(SizeEnergy.SizeEnergyFactory())
            .help("Include grid size as an energy component.")

    val canonicalParser = subparsers.addParser("canonical")
            .setDefault("algorithm", CANONICAL_ALGORITHM)
    canonicalParser.addArgument("-z")
            .required(true)
            .dest("z")
            .type(Double::class.java)
            .help("The z value which controls the posterior distribution.")
    canonicalParser.addArgument("-k", "--knot-type")
            .required(true)
            .dest("knot_name")
            .type(String::class.java)
            .help("The knot type used in the algorithm.")
    canonicalParser.addArgument("-n")
            .setDefault(1000)
            .type(Int::class.java)
            .dest("samples")
            .help("Number of samples to collect.")
    canonicalParser.addArgument("--step-size")
            .setDefault(10000)
            .type(Int::class.java)
            .dest("step-size")
            .help("Number of steps to take between samples adjustments")


    try {
        val res = parser.parseArgs(args)
        when (res.get("algorithm") as String) {
            CANONICAL_ALGORITHM -> takeCanonicalSamples(res.getString("knot_name"),
                    res.getDouble("z"),
                    res.getInt("samples"),
                    res.getInt("step-size")
            )
            WANG_LANDAU_TRAINING -> wangLandauTrain(res.getString("knot_name"),
                    res.getList<WangLandauEnergy.WangLandauEnergyFactory<GridDiagram, GridMove, *>>("energy"),
                    res.getInt("max-size"),
                    res.getInt("step-size"),
                    res.getDouble("f"))
        }

    } catch (e: ArgumentParserException) {
        parser.handleError(e)
    }


}


fun wangLandauTrain(knotName: String, energyFactoryList:List<WangLandauEnergy.WangLandauEnergyFactory<GridDiagram,GridMove,*>>,maxSize: Int, updateFrequency: Int, logUpdateFactor: Double) {
    val algorithm = GridDiagramWangLandau<CompositeEnergy<GridDiagram, GridMove>>(CompositeEnergy.CompositeEnergyFactory(energyFactoryList), maxSize)
    algorithm.setLogWeights(HashMap<CompositeEnergy<GridDiagram, GridMove>, Double>())
    val gridDiagram = GridDiagram.getGridDiagramFromResource(knotName)
    val logWeights = algorithm.train(gridDiagram, updateFrequency, logUpdateFactor)
    println(logWeights)
}

fun takeCanonicalSamples(knotName: String, z: Double, numSamples: Int, sampleFrequency: Int) {
    val algorithm = CanonicalGridAlgorithm(z)
    var gridDiagram = GridDiagram.getGridDiagramFromResource(knotName)
    gridDiagram = algorithm.run(gridDiagram, sampleFrequency*10) //warmup
    val samples = algorithm.sample(gridDiagram, sampleFrequency, numSamples)
    println(samples)
}
