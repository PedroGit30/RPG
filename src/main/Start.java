package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import rpg.DrawGraph;
import rpg.Point;
import rpg.RPG;

public class Start {

	public static boolean useUI = false;
	public static boolean outImage = false;
	public static boolean notColinear = false;
	static int range = 15;
	static int nPoints = 35;
	static boolean hillClimbing = false;
	static boolean simulatedAnneling = false;
	static String fileOut = null;
	static LinkedList<String> hcArgs;
	static LinkedList<String> saArgs;
	static FileWriter fw = null;
	static final String hillOpt[] = { "Best improvement", "BI", "First improvement", "FI", "Less conflicts", "LC",
			"Random", "R" };
	static final String pointOpt[] = { " | Points:Nearest first", "NF", " | Points:Random", "R" };
	static final String hill = "Hill Climbing:";
	static final String sa = "Simulated Anneling";
	static LinkedList<String> output = new LinkedList<>();
	static RPG[] rpg = new RPG[1];

	public static void main(String args[]) throws IOException {

		
		for (int i = 0; i < args.length; i++) {

			try {
				switch (args[i]) {

				case "-UI":
					useUI = true;
					break;

				case "-img":
					outImage = true;
					break;

				case "-r":
					range = Integer.parseInt(args[i + 1]);
					break;

				case "-n":
					nPoints = Integer.parseInt(args[i + 1]);
					break;

				case "-s":
					int scale = Integer.parseInt(args[i + 1]);
					if (scale < 20)
						DrawGraph.dontDrawAxes();
					DrawGraph.setScale(scale);
					break;

				case "-nc":
					notColinear = true;
					break;

				case "-hc":
					hillClimbing = true;
					hcArgs = new LinkedList<>();

					for (int k = i + 1; k < args.length && args[k].charAt(0) != '-'; k++) {
						System.out.println(args[k]);
						hcArgs.addLast(args[k]);
					}

					break;

				case "-sa":
					simulatedAnneling = true;
					saArgs = new LinkedList<>();

					for (int k = i + 1; k < args.length; k++) {
						if (args[k].charAt(0) == '-')
							break;
						saArgs.addLast(args[k]);
					}

					break;

				case "-o":
					fileOut = args[i + 1];
					break;

				case "-p":
					DrawGraph.drawPoints();
					break;

				case "-f":
					DrawGraph.fill();
					break;

				case "-i":
					rpg = new RPG[Integer.parseInt(args[i + 1])];
					break;

				}

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("wrong arguments");
				return;
			}
		}

		for (int i = 0;i < rpg.length;i++) {
			rpg[i] = new RPG(nPoints, range);
		}	

		output.addLast("-----------------------------------------------LOG-------------------------------------------");

		if (!hillClimbing && !simulatedAnneling) {

			for (int i = 0; i < hillOpt.length; i = i + 2) {
				String ho = hillOpt[i];
				String arg1 = hillOpt[i + 1];
				for (int j = 0; j < pointOpt.length; j = j + 2) {
					String po = pointOpt[j];
					String arg2 = pointOpt[j + 1];
					execute(hill + ho + po, arg1, arg2);
				}
			}

			for (int i = 0; i < pointOpt.length; i = i + 2) {
				String po = pointOpt[i];
				String arg1 = pointOpt[i + 1];
				execute(sa + po, arg1, null);
			}

		}
		if (hillClimbing) {

			Iterator<String> iter = hcArgs.iterator();

			while (iter.hasNext()) {

				String s1 = null;
				String s2 = null;
				String arg1 = iter.next();
				String arg2 = iter.next();

				for (int i = 1; i < hillOpt.length; i = i + 2) {
					if (hillOpt[i].equals(arg1))
						s1 = hillOpt[i - 1];
				}

				for (int i = 1; i < pointOpt.length; i = i + 2) {
					if (pointOpt[i].equals(arg2))
						s2 = pointOpt[i - 1];
				}

				execute(hill + s1 + s2, arg1, arg2);

			}

		}
		if (simulatedAnneling) {

			Iterator<String> iter = saArgs.iterator();

			while (iter.hasNext()) {

				String s1 = null;
				String arg1 = iter.next();

				for (int i = 1; i < pointOpt.length; i++) {
					if (pointOpt[i].equals(arg1))
						s1 = pointOpt[i - 1];
				}

				execute(sa + s1, arg1, null);
			}
		}

		output.addLast("-----------------------------------------------END LOG---------------------------------------");
		showOutput();
		if (fw != null)
			fw.close();

	}

	static void execute(String s, String arg1, String arg2) throws IOException {

		output.addLast("---------------------------------------------------------------------------------------------");
		output.addLast(s);

		if (useUI || outImage)
			DrawGraph.changeTitle(s);

		int n = rpg.length;
		float avgTime = 0;
		int avgIter = 0;
		double avgStartTemp = 0;
		double avgEndTemp = 0;

		for (int i = 0; i < n; i++) {
			if (arg2 == null) {				
				rpg[i].simulatedAnneling(arg1);
				avgStartTemp += rpg[i].startTemp;
				avgEndTemp += rpg[i].endTemp;
			}				
			else
				rpg[i].hillClimbing(arg1, arg2);
			avgTime += rpg[i].time;
			avgIter += rpg[i].iterCount;
		}
		avgTime /= n;
		avgIter /= n;
		avgStartTemp /= n;
		avgEndTemp /= n;

		if(n > 1) {
			output.addLast("Executed " + n + " times with diferent points");
			output.addLast("Average number of iterations: " + avgIter);
			output.addLast("Average time: " + avgTime);
			if(arg2 == null) {
				output.addLast("Average initial temp: " + avgStartTemp);
				output.addLast("Average final temp: " + avgEndTemp);				
			}
		}
		else {
			output.addLast("Number of iterations: " + avgIter);
			output.addLast("Time: " + avgTime);	
			if(arg2 == null) {
				output.addLast("Initial temp: " + avgStartTemp);
				output.addLast("Final temp: " + avgEndTemp);
			}
		}		
		output.addLast("---------------------------------------------------------------------------------------------");
		showOutput();

	}

	static void showOutput() throws IOException {

		if (fileOut != null && fw == null) {
			fw = new FileWriter(fileOut);
		}

		for (String s : output) {
			if (fileOut == null)
				System.out.println(s);
			else
				fw.write(s + "\n");
		}
		output.clear();
	}

}
