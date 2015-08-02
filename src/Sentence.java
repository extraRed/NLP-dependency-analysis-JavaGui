import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

class TreeLine implements Serializable {
	public String rel;
	public Color col;

	public TreeLine(String s, Color c) {
		rel = new String(s);
		col = new Color(c.getRGB());
	}
}

class TreeNode implements Serializable {
	public String label;

	public TreeNode(String s) {
		label = new String(s);
	}

}

public class Sentence implements Serializable {
	public String content;
	public int node_num, line_num;
	public int cur_version;
	public TreeNode node[];
	public TreeLine tempMatrix[][];
	public ArrayList<TreeLine[][]> record;
	public int father[], flag[];

	public Sentence() {
	}

	public Sentence(String c, int num1, int num2) {
		cur_version = -1;
		content = new String(c);
		node_num = num1;
		line_num = num2;
		// record = new ArrayList<String[][]>();
		record = new ArrayList<TreeLine[][]>();
		node = new TreeNode[num1 + 1];
		father = new int[num1 + 1];
		flag = new int[num1 + 1];
		// tempMatrix = new String[num1+1][num1+1];
		tempMatrix = new TreeLine[num1 + 1][num1 + 1];
		clearTree();
	}

	public void clearTree() {
		for (int i = 0; i < node_num; i++)
			for (int j = 0; j < node_num; j++)
				// tempMatrix[i][j]="";
				tempMatrix[i][j] = new TreeLine("", Color.BLUE);
	}

	public void setnode(int id, String n) {
		node[id] = new TreeNode(n);
	}

	public void addline(int st, int ed, String l, Color c) {
		tempMatrix[st][ed] = new TreeLine(l, c);
	}

	public void setline(int st, int ed, String l) {
		for (int i = 0; i < node_num; i++)
			System.arraycopy(CurMatrix()[i], 0, tempMatrix[i], 0, node_num);
		tempMatrix[st][ed] = new TreeLine(l, tempMatrix[st][ed].col);
	}

	public void setlinecolor(int st, int ed, Color c) {
		for (int i = 0; i < node_num; i++)
			System.arraycopy(CurMatrix()[i], 0, tempMatrix[i], 0, node_num);
		tempMatrix[st][ed] = new TreeLine(tempMatrix[st][ed].rel, c);
	}

	public void removeline(int st, int ed) {
		// tempMatrix[st][ed] = "";
		tempMatrix[st][ed].rel = "";
	}

	public void saveMatrix() {
		TreeLine t[][] = new TreeLine[node_num + 1][node_num + 1];
		for (int i = 0; i < node_num; i++)
			System.arraycopy(tempMatrix[i], 0, t[i], 0, node_num);
		record.add(t);
		cur_version++;
	}

	public void undo() {
		cur_version--;
	}

	public void redo() {
		cur_version++;
	}

	public void clearBack() {
		int siz = record.size();
		for (int i = siz - 1; i >= cur_version + 1; i--) {
			record.remove(i);
		}
	}

	public boolean canUndo() {
		if (cur_version > 0)
			return true;
		return false;
	}

	public boolean canRedo() {
		if (cur_version < record.size() - 1)
			return true;
		return false;
	}

	public TreeLine[][] CurMatrix() {
		if (cur_version >= 0)
			return record.get(cur_version);
		else
			return tempMatrix;
	}

	public int findset(int x)// ��
	{
		if (x != father[x])
			father[x] = findset(father[x]);// ·��ѹ��
		return father[x];
	}

	public int unionset(int a, int b)// ��
	{
		int x = findset(a);
		int y = findset(b);

		if (y != b)// b������a�ϣ�Ҫ��֤b�Ǹ����ڵ㣬����b�����������ڵ�
			return 1;

		if (x == y)// ���a,b��ͬһ�����У����ٽ��в������ͻ������
			return 1;
		else
			father[y] = x;// �����������������Ժϲ�������
		return 0;
	}

	public boolean CheckTree() {
		int key = 0, root = 0;
		for (int i = 0; i < node_num; i++) {
			father[i] = i;
			flag[i] = 0;
		}
		for (int i = 0; i < node_num; i++)
			for (int j = 0; j < node_num; j++) {
				// if (tempMatrix[i][j].equals("") == false) {
				if (tempMatrix[i][j].rel.equals("") == false) {

					flag[i] = 1;
					flag[j] = 1;
					key += unionset(i, j);
				}
			}
		for (int i = 0; i < node_num; i++) {
			if (flag[i] == 1 && father[i] == i)
				root++;

			if (root >= 2)
				break;
		}

		if (key > 0 || root >= 2)
			return false;
		return true;
	}

	public void writeToFile(FileOutputStream outStream) throws IOException {
		ObjectOutputStream ooStream = new ObjectOutputStream(outStream);
		ooStream.writeObject(this);
		ooStream.flush();
	} // writeToFile()

	public void readFromFile(FileInputStream inStream) throws IOException,
			ClassNotFoundException {
		ObjectInputStream oiStream = new ObjectInputStream(inStream);
		Sentence tempste = (Sentence) (oiStream.readObject());
		this.cur_version = -1;
		this.content = tempste.content;
		this.node_num = tempste.node_num;
		this.line_num = tempste.line_num;
		this.node = new TreeNode[this.node_num + 1];
		this.father = new int[node_num + 1];
		this.flag = new int[node_num + 1];
		this.record = new ArrayList<TreeLine[][]>();
		this.tempMatrix = new TreeLine[node_num + 1][node_num + 1];
		for (int i = 0; i < node_num; i++) {
			this.node[i] = tempste.node[i];
			for (int j = 0; j < node_num; j++) {
				this.tempMatrix[i][j] = tempste.tempMatrix[i][j];
			}
		}
	} // readFromFile()

	public int getNodeNum() {
		return node_num;
	}

	public int getLineNum() {
		return line_num;
	}

	public String getString(int index1, int index2) {
		return this.CurMatrix()[index1][index2].rel;
	}

	public Color getColor(int index1, int index2) {
		return this.CurMatrix()[index1][index2].col;
	}

	public String getLabel(int id) {
		return node[id].label;
	}
}
