package org.pooltesting;

public class MaxChainPoolTesting {
	static final int MAX_POOL_SIZE = 1001;
	static final int MAX_CHAIN = 10;
	
	static double PROB;
	static final double[] PROBS = {0.001, 0.01, 0.05, 0.1, 0.2, 0.3};
	
	static double[][][][] table; // max_chain_length x has_positive x forced_size (0 means no forced) x total_remaining
	static int[][][][] choose;
	
	static double getExpected(int maxChain, int hasPositive, int forced, int remaining) {
		double ret = table[maxChain][hasPositive][forced][remaining];
		if (ret > -0.5) return ret;
		if (remaining == 0) return table[maxChain][hasPositive][forced][remaining] = 0;
		if (remaining == 1) return table[maxChain][hasPositive][forced][remaining] = forced == 1 || hasPositive == 0 ? 1 : 0;
		if (forced == 0) {
			if (maxChain <= 1) {
				choose[maxChain][hasPositive][0][remaining] = 1;
				return table[maxChain][hasPositive][0][remaining] = remaining;
			}
			if (hasPositive == 0) {
				double probNeg = probNegative(remaining);
				ret = 1 + (1.0 - probNeg) * getExpected(maxChain - 1, 1, 0, remaining);
			} else {
				ret = remaining + 1;
				for (int j = 1; j < remaining; j++) {
					double tmp = getExpected(maxChain - 1, hasPositive, j, remaining);
					if (ret > tmp) {
						ret = tmp;
						choose[maxChain][hasPositive][0][remaining] = j;
					}
				}
			}
			return table[maxChain][hasPositive][0][remaining] = ret;
		}
		double probNeg = probNegative(forced);
		if (hasPositive == 1) {
			double totImpossible = probNegative(remaining);
			probNeg = (probNeg - totImpossible) / (1.0 - totImpossible);
		}
		int nRem = remaining - forced;
		ret = 1.0;
		if (probNeg > 0) ret += probNeg * getExpected(maxChain, hasPositive, Math.min(forced, nRem), nRem);
		if (probNeg < 1) ret += (1.0 - probNeg) * (getExpected(maxChain, 1, 0, forced) + getExpected(maxChain, 0, Math.min(forced, nRem), nRem));
		
		return table[maxChain][hasPositive][forced][remaining] = ret;
	}
	
	static double probNegative(int sample) {
		return Math.pow(1 - PROB, sample);
	}
	
	public static void main(String[] args) {
		table = new double[MAX_CHAIN][2][MAX_POOL_SIZE][MAX_POOL_SIZE];
		choose = new int[MAX_CHAIN][2][MAX_POOL_SIZE][MAX_POOL_SIZE];
		for (int idx = 0; idx < PROBS.length; idx++) {
			PROB = PROBS[idx];
			
			for (int chainLength = 1; chainLength < MAX_CHAIN; chainLength++) {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < MAX_POOL_SIZE; j++) {
						for (int k = 0; k < MAX_POOL_SIZE; k++) {
							table[chainLength][i][j][k] = -1.0;
						}
					}
				}
			}
			System.out.println("PROB: " + PROB);
			System.out.println("=============");
			for (int chainLength = 1; chainLength < MAX_CHAIN; chainLength++) {
				double bestFactor = 0;
				int bestIdx = 0;
				for (int i = 1; i < MAX_POOL_SIZE; i++) {
					double tests = getExpected(chainLength, 0, 0, i);
					if (bestFactor < i / tests) {
						bestFactor = i / tests;
						bestIdx = i;
					}
				}
				System.out.println("Max chain: " + chainLength + ", best size: " +
						bestIdx + " => " + bestIdx / bestFactor + " from " + choose[chainLength - 1][1][0][bestIdx] + " factor: " + bestFactor);
			}
			System.out.println();
		}
	}
}
