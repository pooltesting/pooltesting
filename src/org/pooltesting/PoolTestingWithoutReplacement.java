package org.pooltesting;

import java.util.Arrays;

public class PoolTestingWithoutReplacement {
	
	static double PROB;
	static final double[] PROBS = {0.01, 0.05, 0.1, 0.2, 0.3};
	
	static final int MAX = 201;
	
	static double[][] table; // muestra total dio positivo x #total de muestras a evaluar => esperanza de #kits de testeo
	static int[][] choose; // óptimo de #muestras a testear en conjunto, partiendo del estado en cuestión
	
	/**
	 * Devuelve la 'menor' esperanza de cantidad de kits de testeo que se pueden usar para evaluar "total" muestras, de
	 * las que, cuando positive es 1, se sabe que hay (al menos) una positiva.
	 *
	 */
	static double getExpected(int positive, int total) {
		double ret = table[positive][total];
		if (ret > -0.5) return ret;
		if (total == 0) return table[positive][total] = 0;
		if (total == 1) {
			if (positive == 0) choose[positive][total] = 1;
			return table[positive][total] = positive == 1 ? 0.0 : 1.0;
		}
		ret = total;
		double tmp;
		for (int i = 1; i + positive <= total; i++) {
			double probNeg = probNegative(i);
			if (positive == 1) {
				double totImpossible = probNegative(total);
				probNeg = (probNeg - totImpossible) / (1.0 - totImpossible);
			}
			tmp = 1 + probNeg * getExpected(positive, total - i) + (1 - probNeg) * getExpected(0, total - i);
			tmp += getExpected(1, i) * (1.0 - probNeg);
			if (tmp < ret) {
				ret = tmp;
				choose[positive][total] = i;
			}
		}
		return table[positive][total] = ret;
	}
	
	static double probNegative(int sample) {
		return Math.pow(1 - PROB, sample);
	}
	
	public static void main(String[] args) {
		table = new double[MAX][MAX];
		choose = new int[MAX][MAX];
		for (int i = 0; i < PROBS.length; i++) {
			PROB = PROBS[i];
			for (int j = 0; j < MAX; j++) {
				Arrays.fill(table[j], -1);
			}
			double bestFactor = 0;
			int bestIdx = 0;
			for (int idx = 1; idx < MAX; idx++) {
				double tests = getExpected(0, idx);
				if (bestFactor < idx / tests && Math.abs(bestFactor - idx / tests) > 1e-6) {
					bestFactor = idx / tests;
					bestIdx = idx;
				}
			}
			System.out.println("Prevalencia " + PROB + ": Testear " + bestIdx+ " muestras requiere " + bestIdx / bestFactor + " kits de testeo. Si da positivo, testear un tamaño de grupo: " + choose[1][bestIdx] + ". Multiplicador de uso de kits: " + bestFactor);
		}
	}
}
