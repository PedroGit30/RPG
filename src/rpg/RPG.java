package rpg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.ls.LSProgressEvent;

import main.Start;

/**
 * Notes. A point represented by the class Point. A segment is represented by 2
 * followed points in the used structure. ex: [p1,p4,p5,p6,p3] -> this array
 * means that p1-p4 is a line segment, p4-p5, p5-p6, p6-p3 are also line segment
 * and p3-p1 is the last line segment.
 */

public class RPG {

	public HashSet<Point> firstPoints;
	boolean bestImprov;
	boolean firstImprov;
	boolean lessConflicts;
	boolean randomImprov;
	public int iterCount = 0;
	public float time = 0;
	public double startTemp = 0;
	public double endTemp = 0;

	public RPG(int n, int m) {
		firstPoints = new LinkedHashSet<Point>();		
		getRandomPoints(n, m);
	}

	/**
	 * Generates n points from m to -m. Function modified to not allow more than
	 * tree collinear points to avoid cicles.
	 */
	private void getRandomPoints(int n, int m) {

		Random rand = new Random(System.currentTimeMillis());

		for (int i = 0; i < n;) {

			Point p = new Point(rand.nextInt(2 * m + 1) - m, rand.nextInt(2 * m + 1) - m);
			if (!firstPoints.contains(p)) {
				
				if (Start.notColinear) {
					
					boolean colinnear = false;
					for (Point p1 : firstPoints) {
						for (Point p2 : firstPoints) {
							if (p1 == p2)
								continue;
							float area = ((p2.x - p1.x) * (p.y - p1.y) - (p2.y - p1.y) * (p.x - p1.x)) / 2;
							if (area == 0) {								
								colinnear = true;
								break;
							}
						}
					}
					if (!colinnear) {
						firstPoints.add(p);
						i++;
					}
				}
				else {
					firstPoints.add(p);
					i++;
				}
				System.out.print("\033[H\033[2J");
				System.out.println("Generating Points =  " + (int) (((float) i / n) * 100) + "%");				
			}
		}		
	}

	/**
	 * Receives a collection of points and return an array of the points with random
	 * order.
	 */
	public Point[] pointPerm(Collection<Point> points) {

		int n = points.size();
		Point[] newPoints = points.toArray(new Point[points.size()]);
		Random rand = new Random();

		for (int i = 0; i < n; i++) {
			int randomIndex = rand.nextInt(n);
			Point temp = newPoints[i];
			newPoints[i] = newPoints[randomIndex];
			newPoints[randomIndex] = temp;
		}

		return newPoints;
	}

	/**
	 * Receives a collection of points and return an array of the points ordered by
	 * closest to closest. Starts with the first point in the collection
	 */
	public Point[] nearestFirst(Collection<Point> points) {

		LinkedHashSet<Point> newPoints = new LinkedHashSet<>();
		Point p = points.iterator().next();
		newPoints.add(p);

		for (int i = 0; i < points.size() - 1; i++) {

			int min = -1;
			Point nearest = null;

			for (Point p1 : points) {
				if (newPoints.contains(p1))
					continue;
				int dist = (p.x - p1.x) * (p.x - p1.x) + (p.y - p1.y) * (p.y - p1.y);

				if (min == -1 || dist < min) {
					min = dist;
					nearest = p1;
				}
			}
			newPoints.add(nearest);
			p = nearest;
		}
		return newPoints.toArray(new Point[newPoints.size()]);
	}

	
	public boolean intersect(Point p1, Point p2, Point p3, Point p4) {

		if (p4 == p1)
			return false;

		int o1 = orientation(p1, p2, p3);
		int o2 = orientation(p1, p2, p4);
		int o3 = orientation(p3, p4, p1);
		int o4 = orientation(p3, p4, p2);

		// Non colinear points
		if (o1 != o2 && o3 != o4)
			return true;

		if(Start.notColinear) return false;
		
		// collinear points

		// Only allow if internal product is positive		
		if ((p2.x - p1.x) * (p4.x - p3.x) + (p2.y - p1.y) * (p4.y - p3.y) <= 0)
			return false;

		// p1, p2 and p3 are colinear and p3 lies on segment p1q1
		if (o1 == 0 && onSegment(p1, p3, p2))
			return true;

		// p1, p2 and p4 are colinear and p4 lies on segment p1q1
		if (o2 == 0 && onSegment(p1, p4, p2))
			return true;

		// p3, p4 and p1 are colinear and p1 lies on segment p2q2
		if (o3 == 0 && onSegment(p3, p1, p4))
			return true;

		// p3, p4 and p2 are colinear and p2 lies on segment p2q2
		if (o4 == 0 && onSegment(p3, p2, p4))
			return true;

		return false;
	}

	boolean onSegment(Point p1, Point p2, Point p3) {

		return (p2.x <= Math.max(p1.x, p3.x) && p2.x >= Math.min(p1.x, p3.x) && p2.y <= Math.max(p1.y, p3.y)
				&& p2.y >= Math.min(p1.y, p3.y));

	}

	int orientation(Point p1, Point p2, Point p3) {

		int val = (p2.y - p1.y) * (p3.x - p2.x) - (p2.x - p1.x) * (p3.y - p2.y);

		if (val == 0)
			return 0; // colinear
		if (val > 0)
			return 1; // clock wise
		return 2; // counterclock wise

	}

	/**
	 * Returns an Map with all the possible exchanges according to the 2-exchange
	 * algorithm. An entry in the map [1,[4,9]] means that the point with the index
	 * 1 in points can be change for the elements with the indexes 4 and 9.
	 */
	public HashMap<Integer, LinkedList<Integer>> findAllIntersections(Point[] points) {

		HashMap<Integer, LinkedList<Integer>> exchange = new HashMap<>();
		int n = points.length;

		for (int i = 0; i + 1 < n; i++) {

			Point p1 = points[i];
			Point p2 = points[i + 1];

			for (int j = i + 1; j < n; j++) {

				Point p3 = points[j];
				Point p4;

				if (j + 1 >= n)
					p4 = points[0];
				else
					p4 = points[j + 1];

				// check if segment p1 p3 or p2 p4 is already in the initial candidate
				if (j - i == 1)
					continue;

				if (intersect(p1, p2, p3, p4)) {
//					System.out.println(i + " " + (i+1) + " intesects " + j + " " + (j+1));
					LinkedList<Integer> value = exchange.get(i + 1);
					if (value == null) {
						LinkedList<Integer> list = new LinkedList<>();
						list.add(j);
						exchange.put(i + 1, list);
					} else {
						value.add(j);
					}
				}
			}
		}
		return exchange;
	}

	/*
	 * Function used in the first improvement heuristic because we just need the
	 * first line segments that satisfies the 2-exchange. returns the 2 indexes in
	 * the points to swap.
	 */
	private int[] findFirstIntersect(Point[] points) {

		int n = points.length;

		for (int i = 0; i + 1 < n; i++) {

			Point p1 = points[i];
			Point p2 = points[i + 1];

			for (int j = i + 1; j < n; j++) {

				Point p3 = points[j];
				Point p4;

				if (j + 1 >= n)
					p4 = points[0];
				else
					p4 = points[j + 1];

				// check if segment p1 p3 or p2 p4 is already in the initial candidate
				if (j - i == 1)
					continue;

				if (intersect(p1, p2, p3, p4)) {
					return new int[] { i + 1, j };
				}
			}
		}
		return null;
	}

	/**
	 * Swaps the two indexes in the points array.
	 */
	public void swap(Point[] points, int i, int j) {
		Point temp = points[i];
		points[i] = points[j];
		points[j] = temp;
		reverse(points, i, j);
	}

	private void reverse(Point[] points, int i, int j) {

		LinkedList<Point> list = new LinkedList<>();

		for (int k = i + 1; k < j; k++) {
			list.addFirst(points[k]);
		}

		for (int k = i + 1; k < j; k++) {
			points[k] = list.removeFirst();
		}
	}

	/**
	 * Calculates the the segment exchange that gives the best perimeter.
	 */
	private int[] bestPerimeter(Point[] points) {

		int bestPerim = -1;
		int swapI = 0;
		int swapJ = 0;

		for (Map.Entry<Integer, LinkedList<Integer>> entry : findAllIntersections(points).entrySet()) {

			int index = entry.getKey();
			LinkedList<Integer> toSwap = entry.getValue();

			for (int index2 : toSwap) {

				swap(points, index, index2);
				int perim = 0;

				for (int i = 0; i < points.length; i++) {
					Point p = points[i];
					Point p1;
					if (i + 1 >= points.length)
						p1 = points[0];
					else
						p1 = points[i + 1];
					perim += (p.x - p1.x) * (p.x - p1.x) + (p.y - p1.y) * (p.y - p1.y);
				}

				if (bestPerim == -1 || perim < bestPerim) {
					bestPerim = perim;
					swapI = index;
					swapJ = index2;
				}
				swap(points, index, index2);
			}
		}

		if (bestPerim == -1)
			return null;
		return new int[] { swapI, swapJ };
	}

	/**
	 * Calculates witch one of the possible candidates as the less conflicts.
	 */
	private int[] lessConflicts(Point[] points) {

		int minConflits = -1;
		int swapI = 0;
		int swapJ = 0;

		HashMap<Integer, LinkedList<Integer>> intersections = findAllIntersections(points);		
		int i = 0;
		for (Map.Entry<Integer, LinkedList<Integer>> entry : intersections.entrySet()) {			
			
			int index = entry.getKey();
			LinkedList<Integer> toSwap = entry.getValue();
			int numberConflits = 0;

			for (int index2 : toSwap) {

				swap(points, index, index2);

				HashMap<Integer, LinkedList<Integer>> conflits = findAllIntersections(points);
				for (LinkedList<Integer> list : conflits.values()) {
					numberConflits += list.size();
				}

				if (minConflits == -1 || numberConflits < minConflits) {
					minConflits = numberConflits;
					swapI = index;
					swapJ = index2;
				}

				swap(points, index, index2);
			}
			i++;
		}
		if (minConflits == -1)
			return null;
		return new int[] { swapI, swapJ };
	}

	/**
	 * Returns a random candidate from all the possible ones.
	 */
	private int[] randomIntersect(Point[] points) {

		HashMap<Integer, LinkedList<Integer>> conflits = findAllIntersections(points);
		if (conflits.size() == 0)
			return null;
		Random rand = new Random();
		int n = rand.nextInt(conflits.size());
		int swapI = 0;
		int swapJ = 0;

		LinkedList<Integer> list = null;

		for (Map.Entry<Integer, LinkedList<Integer>> entry : conflits.entrySet()) {

			swapI = entry.getKey();
			list = entry.getValue();
			if (n == 0)
				break;
			n--;
		}

		swapJ = list.get(rand.nextInt(list.size()));
		return new int[] { swapI, swapJ };
	}

	private int[] choseHeuristic(Point[] points) {

		if (bestImprov)
			return bestPerimeter(points);
		if (firstImprov)
			return findFirstIntersect(points);
		if (lessConflicts)
			return lessConflicts(points);
		if (randomImprov)
			return randomIntersect(points);

		return null;
	}

	private void choseOutput(Point[] points) {

		if (Start.useUI && Start.outImage) {
			DrawGraph.toImage();
			DrawGraph.end();
		} else if (Start.outImage) {
			DrawGraph.createImage(points);
		} else if (Start.useUI) {
			DrawGraph.end();
		}

	}

	/**
	 * Hill climbing algorithm that finds a solution using the given one of the
	 * possible heuristics.
	 */
	synchronized public void hillClimbing(String heuristic, String firstCandidate) {
		
		long start = System.currentTimeMillis();
		Point[] points = null;
		bestImprov = false;
		firstImprov = false;
		lessConflicts = false;
		randomImprov = false;
		iterCount = 0;
		
		switch (heuristic) {

		case "BI":
			bestImprov = true;
			break;
		case "FI":
			firstImprov = true;
			break;
		case "LC":
			lessConflicts = true;
			break;
		case "R":
			randomImprov = true;
			break;
		default:
			System.err.println("Wrong options on hill climbing");
			return;
		}

		switch (firstCandidate) {

		case "NF":
			points = nearestFirst(firstPoints);
			break;
		case "R":
			points = pointPerm(firstPoints);
			break;
		default:
			System.err.println("Wrong options on hill climbing");
			return;			
		}

		if (Start.useUI) {
			DrawGraph.showGraph(points);
			DrawGraph.update(points, this);
		}

		int toSwap[];

		while ((toSwap = choseHeuristic(points)) != null) {
			swap(points, toSwap[0], toSwap[1]);
			iterCount++;

			if (Start.useUI)
				DrawGraph.update(points, this);
		}
		
		time = (float)(System.currentTimeMillis() - start) / 1000;
		choseOutput(points);

	}
	
	private double getTempR(double n) {
		
		double temp = n/30;
		int factor = 30;
		
		for(int i = 100;i < n;i=i+100) {
			if(i >= 600) factor *= 4; 
			if(i >= 700) factor = factor/3;
			if(i >= 900) factor = factor/3;
			temp *= factor;			
		}
		return temp;
	}
	
	private double getTempNF(double n) {
		double temp = 1;
		
		for(int i = 100;i < n;i=i+100) {
			if(n > 500) temp++;
			if( n > 1000) temp += 15;
			if( n > 1800) temp += 80;
			else temp++;			
		}
		return temp;
		
	}
	
	

	synchronized public void simulatedAnneling(String firstCandidate) {
				
		long start = System.currentTimeMillis();		
		int conflicts = 0;
		double progression = 0;
		double temp = 0;
		iterCount = 0;
		Point[] points;						
		
		HashMap<Integer, LinkedList<Integer>> intersect;
		
		switch (firstCandidate) {

		case "NF":
			
			points = nearestFirst(firstPoints);
			intersect = findAllIntersections(points);
			
			for (LinkedList<Integer> l : intersect.values()) {
				conflicts += l.size();
			}
			
			temp = getTempNF(points.length);
			progression = 0.97;						
			
			break;
		case "R":
			
			points = pointPerm(firstPoints);
			intersect = findAllIntersections(points);
			
			for (LinkedList<Integer> l : intersect.values()) {
				conflicts += l.size();
			}
			
			temp = getTempR(points.length);			
			progression = 0.99 - (0.035/((double)points.length));
						
			break;
		default:
			System.err.println("Wrong options on simulated anneling");
			return;			
		}						

		if (Start.useUI) {
			DrawGraph.showGraph(points);
			DrawGraph.update(points, this);
		}

		startTemp = temp;
		
		while (intersect.size() != 0) {			
			
			mainLoop: for (Map.Entry<Integer, LinkedList<Integer>> entry : intersect.entrySet()) {
				int i = entry.getKey();

				for (int j : entry.getValue()) {
					int newConflicts = 0;					
					swap(points, i, j);
					
					HashMap<Integer,LinkedList<Integer>> newIntersect = findAllIntersections(points);

					for (LinkedList<Integer> l : newIntersect.values()) {
						newConflicts += l.size();
					}

					if (newConflicts <= conflicts
							|| Math.pow(Math.E, (conflicts - newConflicts) / temp) >= Math.random()) {
						
						conflicts = newConflicts;
						intersect = newIntersect;
						temp = temp * progression;
						iterCount++;	
						break mainLoop;
					} else {						
						swap(points, i, j);							
					}
				}
			}		
			if (Start.useUI)
				DrawGraph.update(points, this);
		}
		endTemp = temp;
		time = (float)(System.currentTimeMillis() - start) / 1000;
		choseOutput(points);
	}

}
