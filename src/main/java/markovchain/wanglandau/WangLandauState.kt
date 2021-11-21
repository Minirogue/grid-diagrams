package markovchain.wanglandau

import markovchain.wanglandau.energy.WangLandauEnergy

/**
 * A wrapper class representing a state in the Markov chain and its corresponding energy value.
 *
 * @param MarkovState The class type representing states in the Markov Chain
 * @param state  The underlying MarkovState represented by this object.
 * @param energy The energy value of the given state.
 */
data class WangLandauState<MarkovState, E : WangLandauEnergy<MarkovState, *, E>>(val state: MarkovState, val energy: E)