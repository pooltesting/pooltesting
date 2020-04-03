package org.pooltesting;

import java.util.Arrays;

public class PoolTestingWithReplacement {
	
	static double PROB;
	static final double[] PROBS = {0.01, 0.05, 0.1, 0.2, 0.3};
	
	static final int MAX = 201;
	
	static double[][] table; // #muestras en grupo que dio positivo x #total de muestras a evaluar => esperanza de #kits de testeo
	static int[][] choose; // óptimo de #muestras a testear en conjunto, partiendo del estado en cuestión
	
	/**
	 * Devuelve la 'menor' esperanza de cantidad de kits de testeo que se pueden usar para evaluar "total" muestras, de
	 * las que se sabe que hay un grupo de "positiveSize" muestras con al menos una positiva. (Debe ser positiveSize <= total)
	 * Si "positiveSize" es 0, no se sabe de ningún grupo de muestras que haya testeado positivo.
	 *
	 */
	static double getExpected(int positiveSize, int total) {
		double ret = table[positiveSize][total];
		if (ret > -0.5) return ret;
		if (total == 0) {
			// no quedan muestras a evaluar => 0 kits
			return table[positiveSize][total] = 0;
		}
		if (positiveSize == 1) {
			// en un grupo de 1 muestra hay positvos => tiene que ser positivo => queda por evaluar el resto (total - 1)
			return table[positiveSize][total] = getExpected(0, total - 1);
		}
		ret = total + 1;
		if (positiveSize > 0) {
			// tenemos un grupo de muestras que tienen (al menos) un positivo
			for (int i = 1; i < positiveSize; i++) {
				// iteramos los posibles tamaños para evaluar a continuación
				double totImpossible = probNegative(positiveSize);
				double probNeg = (probNegative(i) - totImpossible) / (1.0 - totImpossible); // probabilidad de que el grupo de i muestras de negativo
				double tmp = 1 + probNeg * getExpected(positiveSize - i, total - i) // los i dan negativo => sabemos que c/u es negativo
						+ (1 - probNeg) * getExpected(i, total); // si da positivo, al resto (positiveSize - i) lo podemos "meter" en la misma bolsa que los total - positiveSize.
				if (tmp < ret) {
					ret = tmp;
					choose[positiveSize][total] = i;
				}
			}
			return table[positiveSize][total] = ret;
		}
		// Acá positiveSize == 0
		for (int i = 1; i <= total; i++) {
			// iteramos los posibles tamaños para evaluar a continuación
			double probNeg = probNegative(i);
			double tmp = 1 + probNeg * getExpected(0, total - i) // los i dan negativo => sabemos que c/u es negativo
					+ (1 - probNeg) * getExpected(i, total); // si da positivo, los indicamos en el primer parámetro de la llamada recursiva
			if (tmp < ret) {
				ret = tmp;
				choose[positiveSize][total] = i;
			}
		}
		return table[positiveSize][total] = ret;
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
				if (bestFactor < idx / tests) {
					bestFactor = idx / tests;
					bestIdx = idx;
				}
			}
			System.out.println("Prevalencia " + PROB + ": Testear " + bestIdx+ " muestras requiere " + bestIdx / bestFactor + " kits de testeo. Cantidad de muestras en el primer grupo: " + choose[0][bestIdx] + ". Multiplicador de uso de kits: " + bestFactor);
		}
	}
}
