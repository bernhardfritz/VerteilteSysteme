package chord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainC {
	
	private static List<Integer> hopCounts = new ArrayList<Integer>();
	
	public synchronized static void addHopCount(int hopCount) {
		hopCounts.add(hopCount);
	}

	public static void main(String[] args) {
		int m = 12;
		
		List<Node> randomNodes = new ArrayList<Node>();
		Node n0 = new Node(0, m);
		n0.join(null);
		for (int i = 1; i < 100; i++) {
			Node randomNode = null;
			do {
				randomNode = new Node(new Random().nextInt((int)Math.pow(2, m)), m);
			} while (randomNodes.contains(randomNode));
			randomNodes.add(randomNode);
			randomNode.join(n0);
		}
		
		randomNodes.add(n0);
		for (Node n1 : randomNodes) {
			for (Node n2 : randomNodes) {
				if (n1 != n2) {
					n1.sendMSG("Test", n2);
				}
			}
		}
		
		System.out.println();
		Collections.sort(hopCounts);
		System.out.println("Min HopCount = " + hopCounts.get(0));
		System.out.println("Max HopCount = " + hopCounts.get(hopCounts.size()-1));
		int sum = 0;
		for (Integer hopCount : hopCounts) {
			sum += hopCount;
		}
		System.out.println("Avg HopCount = " + new BigDecimal(sum).divide(new BigDecimal(hopCounts.size()), 0, RoundingMode.HALF_UP));
		System.out.println("Message Count = " + hopCounts.size());
		
		/**
		 * Mit 500 Nodes werden max und avg HopCount etwas steigen (max <= 12, avg ~ max/2), min HopCount bleibt bei 1 (min >= 1).
		 */
	}
}