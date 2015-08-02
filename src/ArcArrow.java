import static java.awt.geom.AffineTransform.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import java.awt.geom.Arc2D.*;
import java.awt.geom.*;

class ArcArrow {
	Arc2D.Double arc = new Arc2D.Double();
	Color color = Color.BLUE;
	double radius;
	double extent;
	Point2D.Double pos3 = new Point2D.Double();
	Point2D.Double center = new Point2D.Double();
	Point2D.Double midPoint = new Point2D.Double();
	Point pos1;
	Point pos2;
	private boolean direction;
	final public static boolean RIGHT = true;
	final public static boolean LEFT = false;

	private boolean arcSelectedState = false; // drag
	private boolean arcChangeSelectedState = false; //

	final public static double DEFAULT_EXTENT = Math.PI / 2;
	final public static int CALC_BY_EXTENT = 2;
	final public static int ARROW_SIZE = 4;
	final public static int FRESH_BY_DRAG_END = 0;
	final public static int FRESH_BY_DRAG_MID = 1;
	final public static int OFFSET = 2;
	final public static int DISTANCE = 10;
	final public static int LEAST_HEIGHT = 10;

	public ArcArrow() {
	}

	public ArcArrow(Point pos10, Point pos20, double extent_) {
		initialArcArrow(pos10, pos20, extent_);
	}

	public void initialArcArrow(Point pos10, Point pos20, double extent_) {
		pos1 = pos10;
		pos2 = pos20;
		extent = extent_;
		calcArcByExtent(); // pos3 must be calced
	}

	public void drawArcArrow(Graphics2D g2, String relation) {
		Color refColor = g2.getColor();
		Color tempColor = new Color(refColor.getRed(), refColor.getGreen(),
				refColor.getBlue());
		Font tempFont = new Font(g2.getFont().getName(), g2.getFont()
				.getStyle(), g2.getFont().getSize());
		if (arcChangeSelectedState)
			g2.setFont(new Font("Arc", Font.BOLD, 30));
		g2.setColor(color);
		g2.draw(arc);
		GeneralPath triangle = new GeneralPath();
		if (direction == RIGHT) {
			triangle.moveTo(pos3.getX() - ARROW_SIZE, pos3.getY() + ARROW_SIZE);
			triangle.lineTo(pos3.getX() - ARROW_SIZE, pos3.getY() - ARROW_SIZE);
			triangle.lineTo(pos3.getX() + ARROW_SIZE * 2, pos3.getY());
		} else {
			triangle.moveTo(pos3.getX() + ARROW_SIZE, pos3.getY() + ARROW_SIZE);
			triangle.lineTo(pos3.getX() + ARROW_SIZE, pos3.getY() - ARROW_SIZE);
			triangle.lineTo(pos3.getX() - ARROW_SIZE * 2, pos3.getY());
		}
		triangle.closePath();
		g2.fill(triangle);
		g2.drawString(relation, (int) pos3.getX() - relation.length() * OFFSET,
				(int) pos3.getY() - DISTANCE);
		g2.setColor(tempColor);
		g2.setFont(tempFont);
	}

	public void freshArc(int byWhat, Point mid) {
		if (byWhat == FRESH_BY_DRAG_END)
			calcArcByExtent();
		else if (byWhat == FRESH_BY_DRAG_MID)
			calcArcByPos3(mid);
	}

	public void setColor(Color color_) {
		color = color_;
	}

	public boolean arrowSelected(Point selectedPoint) {
		double dist = Math
				.sqrt((Math.pow(selectedPoint.getX() - pos3.getX(), 2) + Math
						.pow(selectedPoint.getY() - pos3.getY(), 2)) / 4);
		return dist <= ARROW_SIZE;
	}

	public void setSelectedState(boolean select) {
		arcSelectedState = select;
	}

	public boolean getSelectedState() {
		return arcSelectedState;
	}

	public boolean getArcChangeSelectedState() {
		return arcChangeSelectedState;
	}

	public void setArcChangeSelectedState(boolean select) {
		arcChangeSelectedState = select;
	}

	private void calcArcByExtent() {
		double dist = Math.sqrt((Math.pow(pos1.getX() - pos2.getX(), 2) + Math
				.pow(pos1.getY() - pos2.getY(), 2)) / 4);
		radius = dist / Math.sin(extent / 2);
		midPoint.setLocation((pos1.getX() + pos2.getX()) / 2,
				(pos1.getY() + pos2.getY()) / 2);
		// fit for p1.y == p2.y
		double dx = pos2.getX() - pos1.getX();
		direction = dx > 0 ? RIGHT : LEFT;
		double centerX = midPoint.getX();
		double centerY;
		if (extent > Math.PI)
			centerY = midPoint.getY() + radius * Math.cos(Math.PI - extent / 2);
		else
			centerY = midPoint.getY() + radius * Math.cos(extent / 2);
		if (centerY - radius >= LEAST_HEIGHT) {
			pos3.setLocation(centerX, centerY - radius);
			center.setLocation(centerX, centerY);
			arc.setArcByCenter(centerX, centerY, radius, 0, 0, 0);
			if (direction == RIGHT)
				arc.setAngles(pos2, pos1);
			else
				arc.setAngles(pos1, pos2);
		} else {
			pos3.setLocation(centerX, LEAST_HEIGHT);
			calcArcByPos3(new Point((int) pos3.getX(), (int) pos3.getY()));
		}
	}

	private void calcArcByPos3(Point selectedPoint) {
		if (selectedPoint.getY() < LEAST_HEIGHT)
			pos3.setLocation(pos3.getX(), LEAST_HEIGHT);
		else if (selectedPoint.getY() < pos1.getY())
			pos3.setLocation(pos3.getX(), selectedPoint.getY());

		midPoint.setLocation((pos1.getX() + pos2.getX()) / 2,
				(pos1.getY() + pos2.getY()) / 2);

		double distPow2 = (Math.pow(pos1.getX() - pos2.getX(), 2) + Math.pow(
				pos1.getY() - pos2.getY(), 2)) / 4;
		double lenPow2 = Math.pow(pos3.getX() - midPoint.getX(), 2)
				+ Math.pow(pos3.getY() - midPoint.getY(), 2);
		double len = Math.sqrt(lenPow2);

		radius = (distPow2 + lenPow2) / (2 * len);

		// fit for p1.x == p2.x
		double centerX = pos3.getX();
		double centerY = pos3.getY() + radius;
		center.setLocation(centerX, centerY);
		arc.setArcByCenter(centerX, centerY, radius, 0, 0, 0);
		if (direction == RIGHT)
			arc.setAngles(pos2, pos1);
		else
			arc.setAngles(pos1, pos2);

		// calc extent
		double sameSide = (centerY - midPoint.getY()) * (centerY - pos3.getY());
		if (sameSide > 0)// pos3 and midPoint are at the same side of center
			extent = Math.asin(Math.sqrt(distPow2) / radius);
		else if (sameSide < 0)
			extent = Math.PI - Math.asin(Math.sqrt(distPow2) / radius);
	}

}