import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.geom.Ellipse2D.*;

class Dot {
	public static final int OFFSET = 2;
	public static final int DOWN_DIST = 10;
	public static final int DISTANCE = 30;
	final static int MIDDLE_SIZE = 6;
	final static int LARGE_SIZE = MIDDLE_SIZE * 2;
	final static int SMALL_SIZE = MIDDLE_SIZE / 2;
	final static Color SELECTED_COLOR = Color.RED;
	final static Color NORMAL_COLOR = Color.GREEN;
	private Color myNormalColor = Color.GREEN;
	static boolean TOO_MANY_DOT = false;
	int dotIndex;
	int size = MIDDLE_SIZE;
	boolean selectedState = false;
	boolean dragSelectedState = false;
	boolean popupMenuSelectedState = false;
	Point location;
	Color color;

	public Dot(Point location_, int dotIndex_) {
		dotIndex = dotIndex_;
		location = new Point(location_);
		color = myNormalColor;
		selectedState = false;
		dragSelectedState = false;
		popupMenuSelectedState = false;
	}

	public static void setTooManyDot(boolean tooMany) {
		TOO_MANY_DOT = tooMany;
	}

	public boolean setSize(int size_val) {
		if (size_val <= 0 || size_val > LARGE_SIZE)
			return false;
		size = size_val;
		return true;
	}

	public void setColor(Color color_) {
		color = color_;
	}

	public void setToMyNormalColor() {
		color = myNormalColor;
	}

	public void setMyNormalColor(Color color_) {
		myNormalColor = color_;
	}

	public boolean dotSelected(Point selectedPoint) {
		double dist = Math.sqrt((Math.pow(
				selectedPoint.getX() - location.getX(), 2) + Math.pow(
				selectedPoint.getY() - location.getY(), 2)) / 4);
		return dist <= size;
	}

	public void setSelectedState(boolean select) {
		selectedState = select;
	}

	public boolean getSelectedState() {
		return selectedState;
	}

	public void setDragSelectedState(boolean select) {
		dragSelectedState = select;
	}

	public boolean getDragSelectedState() {
		return dragSelectedState;
	}

	public void drawDot(Graphics2D g2, String label) {
		Color refColor = g2.getColor();
		Color tempColor = new Color(refColor.getRed(), refColor.getGreen(),
				refColor.getBlue());
		g2.setColor(color);
		g2.fillOval((int) location.getX() - size, (int) location.getY() - size,
				size * 2, size * 2);
		g2.setColor(tempColor);
		int len = label.length();
		if (!TOO_MANY_DOT)
			g2.drawString(label, (int) location.getX() - len * OFFSET,
					(int) location.getY() + DISTANCE);
		else
			g2.drawString(label, (int) location.getX() - len * OFFSET,
					(int) location.getY() + DISTANCE + (dotIndex % 2)
							* DOWN_DIST);
	}

	public Point getDotLocation() {
		return location;
	}

	public void freshDot(Point selectedPoint) {
		location.setLocation(selectedPoint.getX(), location.getY());
	}

	public int getDotIndex() {
		return dotIndex;
	}

	public boolean getPopupMenuSelectedState() {
		return popupMenuSelectedState;
	}

	public void setPopupMenuSelectedState(boolean select) {
		popupMenuSelectedState = select;
	}
}