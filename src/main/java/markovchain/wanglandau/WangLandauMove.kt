package markovchain.wanglandau

import markovchain.MarkovMove
import markovchain.wanglandau.energy.WangLandauEnergy

/**
 * A wrapper around the MarkovMoves used as transitions in the MarkovChain
 *
 * @param MarkovState The class for the underlying state space.
 * @param MM          The MarkovMoves being used to perform transitions.
 * @param E
 */
class WangLandauMove<MarkovState, MM : MarkovMove<MarkovState>, E : WangLandauEnergy<MarkovState, MM, E>>
/**
 * @param startingState   The current state, from which the move will be performed.
 * @param markovMove      The proposed MarkovMove.
 */ internal constructor(
        private val startingState: WangLandauState<MarkovState, E>,
        val markovMove: MM) : MarkovMove<WangLandauState<MarkovState, E>> {
    /**
     * @return The energy of the state that would be obtained by calling [.perform]
     */
    val nextEnergy: E by lazy {startingState.energy.getNextEnergyFromMove(markovMove)}

    /**
     * @return The energy of the state that this move is being taken from.
     */
    val currentEnergy: E = startingState.energy

    override fun perform(): WangLandauState<MarkovState, E> = WangLandauState(markovMove.perform(), nextEnergy)
}