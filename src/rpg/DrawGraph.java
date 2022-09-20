package rpg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Class that draws a graph given a set of points.
 */

@SuppressWarnings("serial")
public class DrawGraph extends JPanel {

	private static final int PREF_W = 800;
	private static final int PREF_H = 800;
	private static final Color GRAPH_COLOR = Color.green;
	private static final Color GRAPH_POINT_COLOR = new Color(150, 50, 50, 180);
	private static final Stroke GRAPH_STROKE = new BasicStroke(3f);
	private static final int GRAPH_POINT_WIDTH = 7;
	private static int scale = 20;
	private static JFrame frame;
	private static JFrame frame2;
	private static boolean step = true;
	private static DrawGraph mainPanel;
	private static Object obj = null;
	private static String title;
	private static boolean drawAxis = true;
	private static boolean drawPoints = false;
	private static boolean fill = false;
	private Point[] points;

	public DrawGraph(Point[] points) {
		this.points = points;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int halfWidth = getWidth() / 2;
		int halfHeigth = getHeight() / 2;
		// create x and y axes
		g2.drawLine(halfWidth, getHeight(), halfWidth, 0);
		g2.drawLine(0, halfHeigth, getWidth(), halfHeigth);

		if (drawAxis) {

			// create hatch marks for -y axis.
			for (int i = 0; i * scale < getHeight(); i++) {
				int x0 = halfWidth - 10;
				int x1 = halfWidth + 10;
				int y0 = halfHeigth + i * scale;
				int y1 = y0;
				g2.drawLine(x0, y0, x1, y1);
				if (i == 0)
					continue;
				if (i < 10) {
					g2.drawString("-" + i, x0 - 20, y1 + 5);
				} else {
					g2.drawString("-" + i, x0 - 23, y1 + 5);
				}
			}
			// create hatch marks for +y axis.
			for (int i = 0; i * scale < getHeight(); i++) {
				int x0 = halfWidth - 10;
				int x1 = halfWidth + 10;
				int y0 = halfHeigth - i * scale;
				int y1 = y0;
				g2.drawLine(x0, y0, x1, y1);
				if (i == 0)
					continue;
				if (i < 10) {
					g2.drawString(i + "", x0 - 20, y1 + 5);
				} else {
					g2.drawString(i + "", x0 - 23, y1 + 5);
				}
			}

			// create hatch marks for +x axis.
			for (int i = 0; i * scale < halfWidth; i++) {
				int x0 = halfWidth + i * scale;
				int x1 = x0;
				int y0 = halfHeigth - 10;
				int y1 = halfHeigth + 10;
				if (i == 0) {
					g2.drawString(i + "", x1 - 20, y1 + 20);
				} else if (i < 10) {
					g2.drawString(i + "", x1 - 3, y1 + 20);
				} else {
					g2.drawString(i + "", x1 - 5, y1 + 20);
				}
				g2.drawLine(x0, y0, x1, y1);
			}
			// create hatch marks for -x axis.
			for (int i = 0; i * scale < (halfWidth); i++) {
				int x0 = halfWidth - i * scale;
				int x1 = x0;
				int y0 = halfHeigth - 10;
				int y1 = halfHeigth + 10;
				g2.drawLine(x0, y0, x1, y1);
				if (i == 0)
					continue;
				if (i < 10) {
					g2.drawString("-" + i, x1 - 7, y1 + 20);
				} else {
					g2.drawString("-" + i, x1 - 9, y1 + 20);
				}
			}
		}

		Stroke oldStroke = g2.getStroke();
		g2.setColor(GRAPH_COLOR);
		g2.setStroke(GRAPH_STROKE);
		int xi[] = new int[points.length];
		int yi[] = new int[points.length];
		
		for(int i = 0;i < points.length;i++) {
			xi[i] = halfWidth + points[i].x * scale;
			yi[i] = halfHeigth - points[i].y * scale;
		}
		if(fill) {
			g2.fillPolygon(xi,yi,points.length);
		}
		else g2.drawPolygon(xi,yi,points.length);		

		if (drawPoints) {

			g2.setStroke(oldStroke);
			g2.setColor(GRAPH_POINT_COLOR);

			for (int i = 0; i < points.length; i++) {
				int x = (halfWidth + (points[i].x * scale)) - GRAPH_POINT_WIDTH / 2;
				int y = (halfHeigth - (points[i].y * scale)) - GRAPH_POINT_WIDTH / 2;
//				g2.drawString(""+i, x, y);
				g2.fillOval(x, y, GRAPH_POINT_WIDTH, GRAPH_POINT_WIDTH);
				
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}

	private static void createAndShowGui(Point[] points) {

		mainPanel = new DrawGraph(points);
		frame = new JFrame(title);

		frame2 = new JFrame("Controls");
		frame2.setSize(150, 185);
		JButton b = new JButton("step-by-step");
		JButton b2 = new JButton("stop");
		JButton b3 = new JButton("start");
		b.setBounds(0, 0, 150, 50);
		b2.setBounds(0, 50, 150, 50);
		b3.setBounds(0, 100, 150, 50);
		frame2.add(b);
		frame2.add(b2);
		frame2.add(b3);

		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				synchronized (obj) {
					obj.notify();
				}
			}
		});

		b2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				step = true;
			}
		});

		b3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				step = false;
				synchronized (obj) {
					obj.notify();
				}
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		frame2.setLayout(null);
		frame2.setLocationByPlatform(true);
		frame2.setVisible(true);
	}

	public static void dontDrawAxes() {
		drawAxis = false;
	}
	
	public static void drawPoints() {
		drawPoints = true;
	}
	
	public static void fill() {
		fill = true;
	}

	public static void changeTitle(String newTitle) {
		title = newTitle;
	}

	public static void end() {
		step = true;
		changeTitle(frame.getTitle() + "(Finished)");
		frame2.dispose();
	}

	public static void toImage() {

		BufferedImage img = new BufferedImage(mainPanel.getWidth(), mainPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
		mainPanel.printAll(img.getGraphics());
		try {
			File image = new File(frame.getTitle() + ".png");
			ImageIO.write(img, "png", image);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setScale(int newScale) {
		scale = newScale;
	}	

	public static void update(Point[] points, Object o) {
		obj = o;
		mainPanel.points = points;
		frame.repaint();
		if (step) {
			synchronized (obj) {
				try {
					obj.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void createImage(Point[] points) {
		mainPanel = new DrawGraph(points);
		frame = new JFrame(title);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		toImage();
		frame.dispose();
	}

	public static void showGraph(Point[] points) {
		createAndShowGui(points);
	}
}
