import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.util.*;
import java.awt.geom.*;
import java.awt.geom.Ellipse2D.*;

class DrawMonitor extends JPanel implements MouseListener, MouseMotionListener {
	DrawArc da;
	Sentence sentence;
	Vector<Dot> dotVector = new Vector();
	ArcArrow[][] arcVector;
	TwoDot twoDot = new TwoDot();
	JButton delete;
	JButton changeColor;
	JButton changeLabel;
	JTextArea textArea;
	JPopupMenu dotPopupMenu = new JPopupMenu();
	JPopupMenu arcPopupMenu = new JPopupMenu();
	JMenuItem colorPaneOfDot = new JMenuItem("Color Pane");
	JMenuItem changeSizeOfDot = new JMenuItem("Change Size");
	JMenuItem deleteArc = new JMenuItem("Delete");
	JMenuItem colorPaneOfArc = new JMenuItem("Color Pane");
	int width;
	int height;
	final static int HEIGHT_OF_TEXTAREA = 17;
	final static int WIDTH_OF_TEXTAREA = 12;
	private ArcArrow lastSelectedArc;

	public DrawMonitor(Sentence sentence_, int width_, int height_, DrawArc d) {

		da = d;
		width = width_;
		height = height_;
		sentence = sentence_;
		this.setLayout(new BorderLayout());
		addMouseListener(this);
		addMouseMotionListener(this);
		createButtonArea();
		createPopupMenu();
		initialDotAndArc();
	}

	private void initialDotAndArc() {
		Dot.setTooManyDot(sentence.getNodeNum() > 23);
		int gap = Math.min(
				(width - WIDTH_OF_TEXTAREA * 10) / (sentence.getNodeNum() + 2),
				200);
		int base = (sentence.getNodeNum() % 2 == 0) ? (gap / 2) : 0;
		base += (width - WIDTH_OF_TEXTAREA * 10) / 2;
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			dotVector.add(new Dot(
					new Point(base + (index - sentence.getNodeNum() / 2) * gap,
							height * 3 / 4), index));
		arcVector = new ArcArrow[sentence.getNodeNum()][sentence.getNodeNum()];
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
				arcVector[index][index1] = new ArcArrow();
				arcVector[index][index1].initialArcArrow(
						dotVector.elementAt(index).getDotLocation(), dotVector
								.elementAt(index1).getDotLocation(),
						ArcArrow.DEFAULT_EXTENT);
			}
	}

	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		for (int index = 0; index < dotVector.size(); ++index)
			dotVector.elementAt(index).drawDot(g2, sentence.getLabel(index));
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
				if (!(sentence.getString(index, index1).equals(""))) {
					arcVector[index][index1].setColor(sentence.getColor(index,
							index1));
					arcVector[index][index1].drawArcArrow(g2,
							sentence.getString(index, index1));
				}
			}
	}

	public void mouseClicked(MouseEvent e) {
		Point selectedPoint = e.getPoint();
		if (e.getButton() == MouseEvent.BUTTON1) {
			for (int index = 0; index < dotVector.size(); ++index) { // warn:
																		// make
																		// sure
																		// that
																		// no
																		// more
																		// than
																		// one
																		// dot
																		// can
																		// be
																		// selected
				Dot tempDot = dotVector.elementAt(index);
				if (tempDot.dotSelected(selectedPoint)) {
					if (lastSelectedArc != null)
						lastSelectedArc.setArcChangeSelectedState(false);
					twoDot.addOneDot(tempDot);
					if (twoDot.getState() == TwoDot.TWO_DOT) {

						String relation = JOptionPane.showInputDialog(this,
								"use a string to describe the relation:",
								"Input a string", JOptionPane.PLAIN_MESSAGE);
						while (relation == null || relation.equals("")) {
							int option = JOptionPane.showConfirmDialog(this,
									"Don't want to add an arc?",
									"Are you sure?", JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.NO_OPTION)
								break;
							relation = JOptionPane
									.showInputDialog(
											this,
											"use a string to describe the relation:",
											"Input a string",
											JOptionPane.PLAIN_MESSAGE);
						}

						if (relation == null)
							relation = "";
						sentence.setline(twoDot.getFirstDot().getDotIndex(),
								twoDot.getSecondDot().getDotIndex(), relation);

						if (sentence.CheckTree() == false) {
							JOptionPane.showMessageDialog(this,
									"Warning: it is not a tree now!");
						}
						if (relation.equals("") == false)
							showOperation("add an arc");

						da.TreeModified = true;

						sentence.clearBack();
						sentence.saveMatrix();

						da.undoItem.setEnabled(true);
						da.unDoButton.setEnabled(true);
						da.redoItem.setEnabled(false);
						da.reDoButton.setEnabled(false);
						twoDot.resetState();
					}
					repaint();
					return;
				}
			}

			for (int index = 0; index < sentence.getNodeNum(); ++index)
				for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
					if (sentence.getString(index, index1).equals(""))
						continue;
					ArcArrow tempArcArrow = arcVector[index][index1];

					if (tempArcArrow.arrowSelected(selectedPoint)) {
						if (twoDot.getState() == TwoDot.ONE_DOT)
							twoDot.resetState();
						tempArcArrow.setArcChangeSelectedState(!(tempArcArrow
								.getArcChangeSelectedState()));
						if (lastSelectedArc != null
								&& lastSelectedArc != tempArcArrow)
							lastSelectedArc.setArcChangeSelectedState(false);
						if (tempArcArrow.getArcChangeSelectedState())
							lastSelectedArc = tempArcArrow;
						else
							lastSelectedArc = null;
						repaint();
						return;
					}
				}
		}
	}

	public void mousePressed(MouseEvent e) {
		Point selectedPoint = e.getPoint();
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
				ArcArrow tempArcArrow = arcVector[index][index1];
				if (tempArcArrow.arrowSelected(selectedPoint)) {
					tempArcArrow.setSelectedState(true);
					return;
				}
			}

		for (int index = 0; index < dotVector.size(); ++index) {
			Dot tempDot = dotVector.elementAt(index);
			if (tempDot.dotSelected(selectedPoint)) {
				tempDot.setDragSelectedState(true);
				return;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		Point selectedPoint = e.getPoint();
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
				ArcArrow tempArcArrow = arcVector[index][index1];
				if (tempArcArrow.getSelectedState()) {
					tempArcArrow.setSelectedState(false);
					return;
				}
			}

		for (int index = 0; index < dotVector.size(); ++index) {
			Dot tempDot = dotVector.elementAt(index);
			if (tempDot.getDragSelectedState()) {
				tempDot.setDragSelectedState(false);
				return;
			}
		}
	}

	public void mouseDragged(MouseEvent e) {
		Point selectedPoint = e.getPoint();
		for (int index = 0; index < sentence.getNodeNum(); ++index)
			for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
				ArcArrow tempArcArrow = arcVector[index][index1];
				if (tempArcArrow.getSelectedState()) {
					tempArcArrow.freshArc(ArcArrow.FRESH_BY_DRAG_MID,
							selectedPoint);
					repaint();
					return;
				}
			}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	private void createPopupMenu() {
		colorPaneOfDot.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				for (int index = 0; index < dotVector.size(); ++index) {
					Dot tempDot = dotVector.elementAt(index);
					if (tempDot.getPopupMenuSelectedState()) {
						Color newColor = JColorChooser.showDialog(null,
								"Choose Your Sweet Color", null);
						if (newColor != null) {
							tempDot.setMyNormalColor(newColor);
							tempDot.setToMyNormalColor();
							tempDot.setPopupMenuSelectedState(false);
							repaint();
							return;
						}
					}
				}
			}
		});

		changeSizeOfDot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() != changeSizeOfDot)
					return;
				for (int index = 0; index < dotVector.size(); ++index) {
					Dot tempDot = dotVector.elementAt(index);
					if (tempDot.getPopupMenuSelectedState()) {
						tempDot.setPopupMenuSelectedState(false);
						return;
					}
				}
			}
		});

		dotPopupMenu.add(colorPaneOfDot);
		dotPopupMenu.add(changeSizeOfDot);
		arcPopupMenu.add(deleteArc);
		arcPopupMenu.add(colorPaneOfArc);
	}

	public void createButtonArea() {
		ImageIcon icon;
		icon = new ImageIcon(da.basePosition + "delete.png");
		delete = new JButton(icon);
		icon = new ImageIcon(da.basePosition + "color.png");
		changeColor = new JButton(icon);
		icon = new ImageIcon(da.basePosition + "edit.png");
		changeLabel = new JButton(icon);
		JPanel buttonArea = new JPanel();
		GridBagLayout grid = new GridBagLayout();
		buttonArea.setLayout(grid);
		GridBagConstraints cons;
		int gridx, gridy, gridwidth, gridheight, anchor, fill, ipadx, ipady;
		double weightx = 0, weighty = 0;
		Insets insets;
		gridx = 0;
		gridy = 0;
		gridwidth = 1;
		gridheight = 1;
		anchor = GridBagConstraints.CENTER;
		fill = GridBagConstraints.NONE;
		insets = new Insets(0, 0, 0, 0);
		ipadx = 0;
		ipady = 0;
		cons = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, insets, ipadx, ipady);
		grid.setConstraints(delete, cons);
		gridx = GridBagConstraints.RELATIVE;
		gridy = 0;
		cons = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, insets, ipadx, ipady);
		grid.setConstraints(changeColor, cons);

		JLabel operation = new JLabel("  OPERATIONS: ");
		operation.setBorder(new LineBorder(Color.BLACK, 1));
		gridx = 0;
		gridy = 0;
		gridwidth = 1;
		gridheight = 1;
		cons = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, insets, ipadx, ipady);
		grid.setConstraints(operation, cons);
		buttonArea.add(operation);

		textArea = new JTextArea(HEIGHT_OF_TEXTAREA, WIDTH_OF_TEXTAREA);
		textArea.setBorder(new LineBorder(Color.WHITE, 0));
		JScrollPane jsp = new JScrollPane(textArea);
		jsp.setVisible(true);
		jsp.setBorder(new LineBorder(Color.WHITE, 0));
		textArea.setEditable(false);
		gridx = 0;
		gridy = 1;
		gridheight = GridBagConstraints.REMAINDER;
		gridwidth = 2;
		cons = new GridBagConstraints(gridx, gridy, gridwidth, gridheight,
				weightx, weighty, anchor, fill, insets, ipadx, ipady);
		grid.setConstraints(jsp, cons);
		buttonArea.add(jsp);

		buttonArea.setVisible(true);

		this.add("East", buttonArea);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int index = 0; index < sentence.getNodeNum(); ++index)
					for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
						ArcArrow tempArcArrow = arcVector[index][index1];
						if (tempArcArrow.getArcChangeSelectedState()) {
							int option = JOptionPane.showConfirmDialog(null,
									"Do you want to remove this arc?",
									"Be Careful", JOptionPane.YES_NO_OPTION);
							if (option == JOptionPane.YES_OPTION) {
								sentence.setline(index, index1, "");
								showOperation("delete an arc");
								sentence.clearBack();
								sentence.saveMatrix();
								da.TreeModified = true;
								da.undoItem.setEnabled(true);
								da.unDoButton.setEnabled(true);
								da.redoItem.setEnabled(false);
								da.reDoButton.setEnabled(false);
							}

							tempArcArrow.setArcChangeSelectedState(false);
							lastSelectedArc = null;
							repaint();

							return;
						}
					}

				for (int index = 0; index < dotVector.size(); ++index) {
					Dot tempDot = dotVector.elementAt(index);
					if (tempDot.getSelectedState()) {
						JOptionPane.showMessageDialog(null,
								"You can't remove this dot!", "Sorry",
								JOptionPane.WARNING_MESSAGE);
						twoDot.resetState();
						repaint();
						return;
					}
				}
			}
		});
		changeColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int index = 0; index < sentence.getNodeNum(); ++index)
					for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
						ArcArrow tempArcArrow = arcVector[index][index1];
						if (tempArcArrow.getArcChangeSelectedState()) {
							Color newColor = JColorChooser.showDialog(null,
									"Color Pane", null);
							if (newColor != null) {
								tempArcArrow.setColor(newColor);
								sentence.setlinecolor(index, index1, newColor);
								sentence.clearBack();
								sentence.saveMatrix();
								da.TreeModified = true;
								da.undoItem.setEnabled(true);
								da.unDoButton.setEnabled(true);
								da.redoItem.setEnabled(false);
								da.reDoButton.setEnabled(false);
								repaint();
								showOperation("change arc color");
							}
							tempArcArrow.setArcChangeSelectedState(false);
							lastSelectedArc = null;
							return;
						}
					}

				for (int index = 0; index < dotVector.size(); ++index) {
					Dot tempDot = dotVector.elementAt(index);
					if (tempDot.getSelectedState()) {
						Color newColor = JColorChooser.showDialog(null,
								"Color Pane", null);
						while (newColor != null
								&& newColor.equals(Dot.SELECTED_COLOR)) {
							JOptionPane
									.showMessageDialog(
											null,
											"You can't choose this color!\nPlease choose again!",
											"Sorry",
											JOptionPane.WARNING_MESSAGE);
							newColor = JColorChooser.showDialog(null,
									"Color Pane", null);

						}
						if (newColor != null) {
							tempDot.setMyNormalColor(newColor);
							showOperation("change dot color");
						}
						tempDot.setToMyNormalColor();
						twoDot.resetState();
						repaint();
						return;
					}
				}
			}
		});
		changeLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int index = 0; index < sentence.getNodeNum(); ++index)
					for (int index1 = 0; index1 < sentence.getNodeNum(); ++index1) {
						ArcArrow tempArcArrow = arcVector[index][index1];
						if (tempArcArrow.getArcChangeSelectedState()) {
							String label = JOptionPane
									.showInputDialog(null,
											"use a new string to describe: ",
											"Input a string",
											JOptionPane.PLAIN_MESSAGE);
							while (label == null || label.equals("")) {
								JOptionPane.showMessageDialog(null,
										"Please input a string!",
										"Input again",
										JOptionPane.WARNING_MESSAGE);
								label = JOptionPane.showInputDialog(null,
										"use a new string to describe: ",
										"Input a string",
										JOptionPane.PLAIN_MESSAGE);
							}
							sentence.setline(index, index1, label);
							sentence.clearBack();
							sentence.saveMatrix();
							da.undoItem.setEnabled(true);
							da.unDoButton.setEnabled(true);
							da.redoItem.setEnabled(false);
							da.reDoButton.setEnabled(false);
							showOperation("change label");
							lastSelectedArc = null;
							tempArcArrow.setArcChangeSelectedState(false);
							repaint();
							return;
						}
					}
			}
		});
	}

	public void showOperation(String operation) {
		textArea.append(operation + "\n");
	}

	public JButton getDeleteButton() {
		return delete;
	}

	public JButton getColorButton() {
		return changeColor;
	}

	public JButton getLabelButton() {
		return changeLabel;
	}
}
