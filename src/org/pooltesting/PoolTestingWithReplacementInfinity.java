package org.pooltesting;

import java.util.HashSet;
import java.util.Set;

public class PoolTestingWithReplacementInfinity {
	
	static double PROB;
	static final double[] PROBS = {0.001, 0.01, 0.05, 0.1, 0.2, 0.3};
	
	static final int MAX = 1001;
	
	static double X;
	static double[] table;
	static int[] choose;
	
	static double calc(int positiveSize) {
		double ret = table[positiveSize];
		if (ret > -0.5) return ret;
		if (positiveSize == 1) {
			return table[positiveSize] = 0;
		}
		ret = positiveSize + 1;
		for (int i = 1; i < positiveSize; i++) {
			double totImpossible = probNegative(positiveSize);
			double probNeg = (probNegative(i) - totImpossible) / (1.0 - totImpossible);
			double tmp = 1 + probNeg * calc(positiveSize - i)
					+ (1 - probNeg) * (calc(i) + (positiveSize - i)	* X);
			if (tmp < ret) {
				ret = tmp;
				choose[positiveSize] = i;
			}
		}
		return table[positiveSize] = ret;
	}
	
	static double bestForSize(int target) {
		double left = 0, right = 1;
		for (int i = 0; i < 35; i++) {
			X = (left + right) / 2;
			for (int j = 0; j <= target; j++) {
				table[j] = -1;
			}
			double bestExpectedPositive = 1 + (1 - probNegative(target)) * calc(target);
			double mustBe = X * target;
			if (mustBe < bestExpectedPositive) {
				left = X;
			} else {
				right = X;
			}
		}
		return (left + right) / 2;
	}
	
	static double[] probNegative;
	static void initProbNegative(int size) {
		probNegative = new double[size];
		probNegative[0] = 1;
		for (int i = 1; i < size; i++) {
			probNegative[i] = (1 - PROB) * probNegative[i-1];
		}
	}
	static double probNegative(int sample) {
		return probNegative[sample];
	}
	
	static Set<Integer> got;
	static final String TAB = "\t\t\t\t\t\t\t\t\t";
	
	/**
	 * Imprime el árbol (se ve bien con tabs de 4 espacios)
	 */
	static void print(int curPos, int infTotal, String pref) {
		if (curPos <= 1) {
			System.out.println((curPos == 1 ? "(1) " : "") + "-");
			return;
		}
		if (got.contains(curPos)) {
			System.out.println("(" + curPos + ") Idem");
			return;
		}
		got.add(curPos);
		int next = choose[curPos];
		String val = "(" + curPos + ") " + next;
		System.out.print(val + "\t--> pos =>\t");
		String preTabs = TAB.substring(0, 1 + val.length() / 4);
		print(next, infTotal, pref + preTabs + "|\t\t\t");
		System.out.print(pref + preTabs + "\\-> neg =>\t");
		print(curPos - next, infTotal - next, pref + preTabs + "\t\t\t");
	}
	
	public static void main(String[] args) {
		table = new double[MAX];
		choose = new int[MAX];
		for (int i = 0; i < PROBS.length; i++) {
			PROB = PROBS[i];
			initProbNegative(MAX);
			
			int left = 1, right = MAX - 1;
			while (left + 5 < right) {
				int p1 = left + (right - left) / 3;
				int p2 = left + 2 * (right - left) / 3;
				double bestP2 = bestForSize(p2);
				if (bestForSize(p1) <= bestP2) {
					right = p2;
				} else {
					left = p1;
				}
			}
			double bestFactor = 0;
			int bestIdx = 0;
			for (int idx = left; idx <= right; idx++) {
				double factor = 1 / bestForSize(idx);
				if (bestFactor < factor) {
					bestFactor = factor;
					bestIdx = idx;
				}
			}
			bestForSize(bestIdx);
			System.out.println("Prevalencia " + PROB + ": Testear " + bestIdx+ " muestras requiere " + bestIdx / bestFactor + " kits de testeo. Multiplicador de uso de kits: " + bestFactor);
			got = new HashSet<>();
			print(bestIdx, bestIdx, "");
			System.out.println();
		}
	}
}
