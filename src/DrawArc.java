import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;

import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class DrawArc extends JFrame implements ActionListener, MouseListener,KeyListener {
	private static int sentence_cnt = 0;
	private LexicalizedParser lp = LexicalizedParser
			.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	private JPanel showarea = new JPanel();
	private JTextArea display = new JTextArea(10,100);

	private JMenuBar mBar = new JMenuBar();
	private JMenu fileMenu, texteditMenu, treeeditMenu,showMenu,helpMenu;
	private JMenuItem quitItem, opentextItem,opentreeItem,savetextItem, savetreeItem, closeItem;
	private JMenuItem cutItem, copyItem, pasteItem, selectItem;
	public JMenuItem undoItem,redoItem,clearItem;
	private JMenuItem showStanford,showEmpty,showPrevious;
	private JMenuItem helpItem;

	private ImageIcon icon;
	public JButton unDoButton, reDoButton, openButton, saveResButton, clearTreeButton;
	private String scratchPad = "";
	private String fileName = "";
	private String parseSentence = "";
	private Sentence ste;

	public boolean TextModified,TreeModified,hasTree;
	private DrawMonitor mainPanel;
	private Map<String, String> steMap = new HashMap<String, String>();
	final public static String basePosition = "D:\\大二下\\Java程序设计\\程序\\Draw\\";
	final public static String savePosition = "D:\\大二下\\Java程序设计\\程序\\Draw\\results\\";
	final public static String picPosition = "D:\\大二下\\Java程序设计\\程序\\Draw\\pictures\\";
	final public static String TreeStoragePosition = "D:\\大二下\\Java程序设计\\程序\\Draw\\TreeStorage.txt";
	
	private Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	public DrawArc() {
		super("Parser");
		TextModified=TreeModified=false;
		display.setFont(new Font("Serif", 0, 20));
		display.setLineWrap(true);
		display.addMouseListener(this);
		display.addKeyListener(this);
		this.getContentPane().setLayout(new BorderLayout());
		JScrollPane jsp = new JScrollPane();
		
		 try {
			 int space;
			 String tempStr;
	            File MapInfo = new File(TreeStoragePosition);//文件路径
	            BufferedReader br = new BufferedReader(new FileReader(MapInfo));
	            while ((tempStr = br.readLine()) != null) {
	            	sentence_cnt++;
	                space=tempStr.lastIndexOf(' ');
	                steMap.put(tempStr.substring(0, space), tempStr.substring(space+1, tempStr.length()));
	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		
		showarea.setLayout(new FlowLayout(FlowLayout.LEFT));

		icon=new ImageIcon(basePosition+"undo.png");
		unDoButton = new JButton(icon);
		unDoButton.setEnabled(false);
		unDoButton.addMouseListener(this);
		
		icon=new ImageIcon(basePosition+"redo.png");
		reDoButton = new JButton(icon);
		reDoButton.setEnabled(false);
		reDoButton.addMouseListener(this);
		
		icon=new ImageIcon(basePosition+"open.png");
		openButton = new JButton(icon);
		openButton.addMouseListener(this);
		
		icon=new ImageIcon(basePosition+"save.png");
		saveResButton = new JButton(icon);
		saveResButton.addMouseListener(this);
		
		icon=new ImageIcon(basePosition+"clear.png");
		clearTreeButton = new JButton(icon);
		clearTreeButton.addMouseListener(this);

		mainPanel=new DrawMonitor(new Sentence("",0,0),(int)(d.getWidth()-75),(int)(d.getHeight()-400),this);
		
		showarea.add(unDoButton);
		showarea.add(reDoButton);
		showarea.add(openButton);
		showarea.add(saveResButton);
		showarea.add(clearTreeButton);
		showarea.add(mainPanel.getDeleteButton());
		showarea.add(mainPanel.getColorButton());
		showarea.add(mainPanel.getLabelButton());
		//mainPanel.setSize(100, 400);
		this.getContentPane().add("North", showarea);
		this.getContentPane().add("Center", mainPanel);
		this.getContentPane().add("South",jsp);
		jsp.setViewportView(display);

		this.setJMenuBar(mBar);
		initFileMenu();
		initTextEditMenu();
		initTreeEditMenu();
		initShowMenu();
		initHelpMenu();
		
		setSize((int)d.getWidth(),(int)(d.getHeight()-50));
		//setExtendedState(JFrame.MAXIMIZED_BOTH); 
		setResizable(false);
		setUndecorated(true); 
		getRootPane().setWindowDecorationStyle(JRootPane.NONE); 
		setVisible(true);
	}

	private void initFileMenu() {
		fileMenu = new JMenu("File");
		mBar.add(fileMenu);
		opentextItem = new JMenuItem("OpenText");
		opentextItem.addActionListener(this);
		fileMenu.add(opentextItem);
		
		opentreeItem = new JMenuItem("OpenTree");
		opentreeItem.addActionListener(this);
		fileMenu.add(opentreeItem);
		
		savetextItem = new JMenuItem("SaveText");
		savetextItem.addActionListener(this);
		fileMenu.add(savetextItem);
		
		savetreeItem = new JMenuItem("SaveTree");
		savetreeItem.addActionListener(this);
		fileMenu.add(savetreeItem);
		
		closeItem = new JMenuItem("Close");
		closeItem.addActionListener(this);
		fileMenu.add(closeItem);
		fileMenu.addSeparator();
		
		quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(this);
		fileMenu.add(quitItem);
	}

	private void initTextEditMenu() {
		texteditMenu = new JMenu("EditText");
		mBar.add(texteditMenu);
		cutItem = new JMenuItem("Cut");
		cutItem.addActionListener(this);
		texteditMenu.add(cutItem);
		copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(this);
		texteditMenu.add(copyItem);
		pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(this);
		texteditMenu.add(pasteItem);
		texteditMenu.addSeparator();
		selectItem = new JMenuItem("SelectAll");
		selectItem.addActionListener(this);
		texteditMenu.add(selectItem);
	}

	private void initTreeEditMenu()	{
		//undoItem,redoItem,clearItem;
		treeeditMenu = new JMenu("EditTree");
		mBar.add(treeeditMenu);
		undoItem = new JMenuItem("Undo");
		undoItem.setEnabled(false);
		undoItem.addActionListener(this);
		treeeditMenu.add(undoItem);
		
		redoItem = new JMenuItem("Redo");
		redoItem.setEnabled(false);
		redoItem.addActionListener(this);
		treeeditMenu.add(redoItem);
		
		clearItem = new JMenuItem("Clear");
		clearItem.addActionListener(this);
		treeeditMenu.add(clearItem);
	}
	private void initShowMenu() {
		//showStandford,showEmpty,showPrevious;
		showMenu = new JMenu("ShowOption");
		mBar.add(showMenu);
		showStanford = new JMenuItem("ShowStanford");
		showStanford.addActionListener(this);
		showMenu.add(showStanford);
		
		showEmpty = new JMenuItem("ShowEmpty");
		showEmpty.addActionListener(this);
		showMenu.add(showEmpty);
		
		showPrevious = new JMenuItem("ShowPrevious");
		showPrevious.setEnabled(false);
		showPrevious.addActionListener(this);
		showMenu.add(showPrevious);
	}
	private void initHelpMenu() {
		helpMenu = new JMenu("Help");
		mBar.add(helpMenu);
		helpItem = new JMenuItem("Instructions");
		helpItem.addActionListener(this);
		helpMenu.add(helpItem);
	}

	public void parseEmpty(String sent) {
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(
				new StringReader(sent)).tokenize();
		ste = new Sentence(sent, rawWords.size(), 0);
		ste.setnode(0, "ROOT");
		for (int i = 1; i <= rawWords.size(); ++i) {
			String token = rawWords.get(i-1).toString();
			int s1 = token.indexOf('=');
			int s2 = token.indexOf(' ');
			// System.out.println(token.substring(s1 + 1, s2));
			ste.setnode(i, token.substring(s1 + 1, s2));
		}
		TreeModified=true;
		hasTree=true;
		ste.saveMatrix();
		showarea.remove(mainPanel.getLabelButton());
		showarea.remove(mainPanel.getDeleteButton());
		showarea.remove(mainPanel.getColorButton());
		this.getContentPane().remove(mainPanel);
		mainPanel=new DrawMonitor(ste,(int)(d.getWidth()-75),(int)(d.getHeight()-400),this);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		showarea.add(mainPanel.getDeleteButton());
		showarea.add(mainPanel.getColorButton());
		showarea.add(mainPanel.getLabelButton());
		mainPanel.updateUI();
	}

	public void parseStanford(LexicalizedParser lp, String sent) {
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(
				new StringReader(sent)).tokenize();
		Tree parse = lp.apply(rawWords);
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		ste = new Sentence(sent, rawWords.size(), tdl.size());
		Iterator<TypedDependency> it = tdl.iterator();
		ste.setnode(0, "ROOT");
		for (int i = 1; i <= rawWords.size(); ++i) {
			String token = rawWords.get(i-1).toString();
			int s1 = token.indexOf('=');
			int s2 = token.indexOf(' ');
			ste.setnode(i, token.substring(s1 + 1, s2));
		}
		while (it.hasNext()) {
			int st, ed;
			int sid = -1, eid = -1;
			String temp = it.next().toString();
			st = temp.indexOf('(');
			String rel = temp.substring(0, st);
			st = temp.indexOf('-');
			ed = temp.indexOf(',');
			try {
				sid = Integer.parseInt(temp.substring(st + 1, ed));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			st = temp.lastIndexOf('-');
			ed = temp.lastIndexOf(')');
			try {
				eid = Integer.parseInt(temp.substring(st + 1, ed));	
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			ste.addline(sid, eid, rel, Color.BLUE);
		}
		TreeModified=true;
		hasTree=true;
		ste.saveMatrix();
		showarea.remove(mainPanel.getDeleteButton());
		showarea.remove(mainPanel.getColorButton());
		showarea.remove(mainPanel.getLabelButton());
		this.getContentPane().remove(mainPanel);
		mainPanel=new DrawMonitor(ste,(int)(d.getWidth()-75),(int)(d.getHeight()-400),this);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		showarea.add(mainPanel.getDeleteButton());
		showarea.add(mainPanel.getColorButton());
		showarea.add(mainPanel.getLabelButton());
		mainPanel.updateUI();
	}
	public void undoOp()
	{
		mainPanel.showOperation("undo");
		ste.undo();
		if (ste.canUndo() == false)
		{
			undoItem.setEnabled(false);
			unDoButton.setEnabled(false);
		}
		redoItem.setEnabled(true);
		reDoButton.setEnabled(true);
		this.getContentPane().remove(mainPanel);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		mainPanel.updateUI();
	}
	public void redoOp()
	{
		mainPanel.showOperation("redo");
		ste.redo();
		if (ste.canRedo() == false)
		{
			redoItem.setEnabled(false);
			reDoButton.setEnabled(false);
		}
		undoItem.setEnabled(true);
		unDoButton.setEnabled(true);
		this.getContentPane().remove(mainPanel);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		mainPanel.updateUI();
	}
	public void clearOp()
	{
		mainPanel.showOperation("clearAll");
		ste.clearTree();
		ste.clearBack();
		ste.saveMatrix();
		this.getContentPane().remove(mainPanel);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		mainPanel.updateUI();
		undoItem.setEnabled(true);
		unDoButton.setEnabled(true);
		redoItem.setEnabled(false);
		reDoButton.setEnabled(false);
		TreeModified=true;
	}
	public void SaveTree()	{
		TreeModified=false;
		String resName = savePosition + "res" + sentence_cnt	//生成保存文件名
				+ ".txt";
		writeRecords(resName);		//将对象序列化
		steMap.put(parseSentence, resName);	//加入到小树库中
		JOptionPane.showMessageDialog(this, "Tree infomation saved in " + resName);
		try{
			FileWriter writer = new FileWriter(TreeStoragePosition, true);	//为总的树库添加信息
			writer.write(parseSentence+" "+resName+"\n");
			writer.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		sentence_cnt++;
	}
	public void SaveText()
	{
		TextModified=false;
		if (fileName.equals("")) { // 如果文件没有被保存过,即文件名为空
			FileDialog d = new FileDialog(this, "save file",
					FileDialog.SAVE);// 保存文件对话框
			d.setVisible(true);
			String s = display.getText();// 得到所输入的文本内容
			try// 异常处理
			{
				File f = new File(d.getDirectory() + d.getFile());// 新建文件
				fileName = d.getDirectory() + d.getFile();// 得到文件名
				if(d.getDirectory()==null)
					return ;
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));// 输入到文件中
				bw.write(s, 0, s.length());
				bw.close();
			} catch (FileNotFoundException fe_) {
				System.out.println("file not found");
				System.exit(0);
			} catch (IOException ie_) {
				System.out.println(" IO error");
				System.exit(0);
			}
		} else // 如果文件已经保存过
		{
			String s = display.getText();// 得到所输入的文本内容
			try// 异常处理
			{
				File f = new File(fileName);// 新建文件
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));// 输入到文件中
				bw.write(s, 0, s.length());
				bw.close();
			} catch (FileNotFoundException fe_) {
				System.out.println("file not found");
				System.exit(0);
			} catch (IOException ie_) {
				System.out.println(" IO error");
				System.exit(0);
			}
		}
	}
	public void SavePic() {
		try {
			// 拷贝屏幕到一个BufferedImage对象screenshot
			BufferedImage screenshot = (new Robot())
					.createScreenCapture(new Rectangle(mainPanel.getX(), mainPanel.getY()+50,
							(int) (d.getWidth()-150), (int)( d.getHeight()/2-mainPanel.getY()-10)));

			String name = picPosition+"res"+sentence_cnt +".jpg";
			File f = new File(name);
			// 将screenshot对象写入图像文件
			ImageIO.write(screenshot, "jpg", f);
			JOptionPane.showMessageDialog(this, "Image saved in " + name);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public void ShowPrevious(String TreeName)
	{
		if(TreeName.equals("/"))			//从文件中读出树的信息
			readRecords(steMap.get(parseSentence));
		else 
			readRecords(TreeName);
		
		ste.saveMatrix();
		//重画主面板
		this.getContentPane().remove(mainPanel);
		showarea.remove(mainPanel.getLabelButton());
		showarea.remove(mainPanel.getDeleteButton());
		showarea.remove(mainPanel.getColorButton());
		mainPanel=new DrawMonitor(ste,(int)(d.getWidth()-75),(int)(d.getHeight()-400),this);
		mainPanel.repaint();
		mainPanel.setBackground(Color.WHITE);
		this.getContentPane().add("Center",mainPanel);
		showarea.add(mainPanel.getDeleteButton());
		showarea.add(mainPanel.getColorButton());
		showarea.add(mainPanel.getLabelButton());
		mainPanel.updateUI();
		hasTree=true;
	}
	public int getStartPos(int offset)
	{
		int res = 0;
		int start1 = display.getText().lastIndexOf('.', offset);
		int start2 = display.getText().lastIndexOf('?', offset);
		int start3 = display.getText().lastIndexOf('!', offset);
		int start4 = display.getText().lastIndexOf(';', offset);
		res=Math.max(Math.max(start4, start3),Math.max(start2, start1));
		return res;
	}
	public int getEndPos(int offset)
	{
		int res = 0;
		int start1 = display.getText().indexOf('.', offset);
		if(start1==-1)
			start1=Integer.MAX_VALUE;
		int start2 = display.getText().indexOf('?', offset);
		if(start2==-1)
			start2=Integer.MAX_VALUE;
		int start3 = display.getText().indexOf('!', offset);
		if(start3==-1)
			start3=Integer.MAX_VALUE;
		int start4 = display.getText().indexOf(';', offset);
		if(start4==-1)
			start4=Integer.MAX_VALUE;
		res=Math.min(Math.min(start4, start3),Math.min(start2, start1));
		return res;
	}
	public void actionPerformed(ActionEvent e) {
		JMenuItem m = (JMenuItem) e.getSource();
		if (m == opentextItem) { // to do Open here
			FileDialog d = new FileDialog(this, "open file", FileDialog.LOAD);// 打开文件对话框
			d.setVisible(true);
			if (d.getDirectory() == null)
				return;
			File f = new File(d.getDirectory() + d.getFile()); // 建立新文件
			fileName = d.getDirectory() + d.getFile();// 得到文件名
			char ch[] = new char[(int) f.length()];// /用此文件的长度建立一个字符数组
			try// 异常处理
			{
				// 读出数据，并存入字符数组ch中
				BufferedReader bw = new BufferedReader(new FileReader(f));
				bw.read(ch);
				bw.close();
			} catch (FileNotFoundException fe) {
				System.out.println("file not found");
				System.exit(0);
			} catch (IOException ie) {
				System.out.println("IO error");
				System.exit(0);
			}
			String s = new String(ch);
			display.setText(s);// 设置文本区为所打开文件的内容

		}else if(m==opentreeItem)
		{
				FileDialog d = new FileDialog(this, "open file", FileDialog.LOAD);// 打开文件对话框
				d.setVisible(true);
				if (d.getDirectory() == null)
					return;
				if (d.getDirectory().equals(savePosition)==false)
				{
					JOptionPane.showMessageDialog(this, "The path should be"+savePosition);
					return ;
				}
				String treeFileName = d.getDirectory() + d.getFile();// 得到文件名
				ShowPrevious(treeFileName);
				parseSentence=ste.content;
				display.setText(parseSentence);
				hasTree=true;
		}
		else if (m == savetextItem) { // to do Save here
			SaveText();
		}else if(m==savetreeItem) {
			if (parseSentence.trim().length() == 0)
			{
				JOptionPane.showMessageDialog(this, "You have to build a tree first!");
				return;
			}
			int n_=JOptionPane.showConfirmDialog(this, "Save an image copy of the tree as well?",null,JOptionPane.YES_NO_OPTION);
			if(n_==JOptionPane.YES_OPTION)
				SavePic();
			SaveTree();
		}
		else if (m == closeItem) {
			if(TextModified==true)
			{
				int n=JOptionPane.showConfirmDialog(this, "Text not saved, do you want to save it?",null,JOptionPane.YES_NO_OPTION);
				if(n==JOptionPane.YES_OPTION)
					SaveText();
			}
			if(TreeModified==true)
			{
				int n=JOptionPane.showConfirmDialog(this, "Tree not saved, do you want to save it?",null,JOptionPane.YES_NO_OPTION);
				if(n==JOptionPane.YES_OPTION)
				{
					int n_=JOptionPane.showConfirmDialog(this, "Save an image copy of the tree as well?",null,JOptionPane.YES_NO_OPTION);
					if(n_==JOptionPane.YES_OPTION)
						SavePic();
					SaveTree();
				}
			}
			TextModified=false;
			undoItem.setEnabled(false);
			unDoButton.setEnabled(false);
			redoItem.setEnabled(false);
			reDoButton.setEnabled(false);
			parseSentence="";
			display.setText("");
			showarea.remove(mainPanel.getLabelButton());
			showarea.remove(mainPanel.getDeleteButton());
			showarea.remove(mainPanel.getColorButton());
			this.getContentPane().remove(mainPanel);
			mainPanel=new DrawMonitor(new Sentence("",0,0),(int)(d.getWidth()-75),(int)(d.getHeight()-400),this);
			mainPanel.repaint();
			this.getContentPane().add("Center",mainPanel);
			showarea.add(mainPanel.getDeleteButton());
			showarea.add(mainPanel.getColorButton());
			showarea.add(mainPanel.getLabelButton());
			mainPanel.updateUI();
		} else if (m == quitItem) {
			if(TextModified==true)
			{
				int n=JOptionPane.showConfirmDialog(this, "Text not saved, do you want to save it?",null,JOptionPane.YES_NO_OPTION);
				if(n==JOptionPane.YES_OPTION)
					SaveText();
			}
			if(TreeModified==true)
			{
				int n=JOptionPane.showConfirmDialog(this, "Tree not saved, do you want to save it?",null,JOptionPane.YES_NO_OPTION);
				if(n==JOptionPane.YES_OPTION)
				{
					int n_=JOptionPane.showConfirmDialog(this, "Save an image copy of the tree as well?",null,JOptionPane.YES_NO_OPTION);
					if(n_==JOptionPane.YES_OPTION)
						SavePic();
					SaveTree();
				}
			}
			System.exit(0);
		} else if (m == cutItem) {//剪切
			scratchPad = display.getSelectedText();
			display.replaceRange("", display.getSelectionStart(),
					display.getSelectionEnd());
		} else if (m == copyItem) {//复制
			scratchPad = display.getSelectedText();
		} else if (m == pasteItem) {//粘贴
			display.insert(scratchPad, display.getCaretPosition());
		} else if (m == selectItem) {//全选
			display.selectAll();
		}else if (m == showStanford){
			if (parseSentence.trim().length() == 0)	{
				JOptionPane.showMessageDialog(this, "You have to choose a sentence first!");
				return;
			}
			parseStanford(lp, parseSentence);
		}else if (m==showEmpty){
			if (parseSentence.trim().length() == 0)	{
				JOptionPane.showMessageDialog(this, "You have to choose a sentence first!");
				return;
			}
			parseEmpty(parseSentence);
		}else if(m==showPrevious){
			ShowPrevious("/");
		}else if(m==undoItem){
			undoOp();
		}else if(m==redoItem){
			redoOp();
		}else if(m==clearItem){
			if(hasTree==false)	{
				JOptionPane.showMessageDialog(this, "You have to build a tree first!");
				return ;
			}
			clearOp();
		}
		else if(m==helpItem){
			JOptionPane.showMessageDialog(this, "请参见我们的帮助文档:help.txt");
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		Object obj = e.getSource();
		if (obj == display) {
			DefaultHighlighter h = (DefaultHighlighter) display
					.getHighlighter();	//高亮类
			DefaultHighlighter.DefaultHighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(
					new Color(226, 239, 255));	//设置高亮颜色
			try {
				int offset = display.getCaretPosition();	//得到插入符位置
				if (offset >= display.getText().length())
					return;
				int start = getStartPos(offset);	//得到选中的句子
				int end = getEndPos(offset);
				parseSentence = new String(display.getText().substring(
						start + 1, end + 1 ));	
				if (steMap.containsKey(parseSentence) == true)
					showPrevious.setEnabled(true);
				h.removeAllHighlights();
				h.addHighlight(start + 1, end + 1 , p);	//高亮显示
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		} 
		else if(obj==unDoButton)
		{
			undoOp();
		}
		else if(obj==reDoButton)
		{
			redoOp();
		}
		else if(obj==openButton)
		{
			FileDialog d = new FileDialog(this, "open file", FileDialog.LOAD);// 打开文件对话框
			d.setVisible(true);
			if (d.getDirectory() == null)
				return;
			if (d.getDirectory().equals(savePosition)==false)
			{
				JOptionPane.showMessageDialog(this, "The path should be"+savePosition);
				return ;
			}
			String treeFileName = d.getDirectory() + d.getFile();// 得到文件名
			ShowPrevious(treeFileName);
			parseSentence=ste.content;
			display.setText(parseSentence);
			hasTree=true;
		}
		else if(obj==clearTreeButton)
		{
			if(hasTree==false)	{
				JOptionPane.showMessageDialog(this, "You have to build a tree first!");
				return ;
			}
			clearOp();
		}
		else if(obj==saveResButton)
		{
			if (parseSentence.trim().length() == 0)
			{
				JOptionPane.showMessageDialog(this, "You have to build a tree first!");
				return;
			}
			int n_=JOptionPane.showConfirmDialog(this, "Save an image copy of the tree as well?",null,JOptionPane.YES_NO_OPTION);
			if(n_==JOptionPane.YES_OPTION)
				SavePic();
			SaveTree();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
	}

	public void writeRecords(String fileName) {
		try {
			FileOutputStream outStream = new FileOutputStream(fileName);
			ste.writeToFile(outStream); // and write it to file
			outStream.close();
		} catch (IOException e) {
			System.out.println("IOERROR: " + e.getMessage() + "\n");
		}
	} // writeRecords()

	private void readRecords(String fileName) {
		try {
			FileInputStream inStream = new FileInputStream(fileName);
			try {
				ste = new Sentence(); // create a new sentence
				ste.readFromFile(inStream); // and read its data

			} catch (IOException e) { // Until IOException
			}
			inStream.close();
		} catch (FileNotFoundException e) {
			display.append("IOERROR: File NOT Found: " + fileName + "\n");
		} catch (IOException e) {
			display.append("IOERROR: " + e.getMessage() + "\n");
		} catch (ClassNotFoundException e) {
			display.append("ERROR: Class NOT found " + e.getMessage() + "\n");
		}
	} // readRecords()



	public void keyTyped(KeyEvent e) {
		TextModified=true;
	}

	public void keyPressed(KeyEvent e) {
	}
	
	public void keyReleased(KeyEvent e) {	
	}
	public static void main(String[] args) {
		DrawArc d = new DrawArc();
		//d.setSize(700, 700);

		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}


}