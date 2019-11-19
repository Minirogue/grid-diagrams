package griddiagrams.markovchain

import griddiagrams.GridDiagram
import griddiagrams.markovchain.canonicalalgorithm.CanonicalGridAlgorithm
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException


fun main(args: Array<String>) {
    val parser = ArgumentParsers.newFor("prog").build()
            .description("Run grid diagram Markov chains. Can run either canonical-like or Wang-Landau")
    parser.addArgument("-k", "--knot-type")
            .dest("knot_name")
            .type(String::class.java)
            .help("The knot type used in the algorithm.")
    parser.addArgument("-n")
            .type(Int::class.java)
            .dest("samples")
            .help("Number of samples to collect.")
    parser.addArgument("--step-size")
            .type(Int::class.java)
            .dest("step-size")
            .help("Number of steps to take between samples/weight adjustments")

    val subparsers = parser.addSubparsers()

    val wangLandauParser = subparsers.addParser("wl").aliases("wanglandau", "wang-landau")


    val canonicalParser = subparsers.addParser("canonical")
            .setDefault("algorithm", RunCanonical())
    canonicalParser.addArgument("-z")
            .dest("z")
            .type(Double::class.java)
            .help("The z value which controls the posterior distribution.")


    try {
        val res = parser.parseArgs(args)
        (res.get("algorithm") as Algorithm).takeSamples(
                res.getString("knot_name"),
                res.getDouble("z"),
                res.getInt("samples"),
                res.getInt("step-size")
        )
    } catch (e: ArgumentParserException) {
        parser.handleError(e)
    }



}

interface  Algorithm{
    fun takeSamples(knotName: String, z: Double, numSamples: Int, sampleFrequency:Int)
}

class RunCanonical(): Algorithm{


    override fun takeSamples(knotName: String, z: Double, numSamples: Int, sampleFrequency:Int) {
        val algorithm = CanonicalGridAlgorithm(z)
        val gridDiagram = GridDiagram.getGridDiagramFromResource(knotName)
        println(algorithm.sample(numSamples,sampleFrequency, gridDiagram))
    }
}
