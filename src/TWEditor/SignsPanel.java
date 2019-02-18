package TWEditor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class SignsPanel extends JPanel implements ActionListener {
	private static final String[] tabNames = { "阿尔德", "伊格尼", "昆恩", "亚克席", "亚登" };
	private static final String[][][] fieldNames = {
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "学生", "弟子", "专精者", "专家", "大师" },
					{ "击晕", "解除武装", "爆击之拳", "延伸持续时间", "狂风" }, { "", "骤风", "雷鸣", "增加效率", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "学生", "弟子", "专精者", "专家", "大师" },
					{ "伤害之法I", "伤害之法II", "燃烧之刃", "炼狱", "延伸持续时间" }, { "", "燃烧", "火焰之壁", "增加效率", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "学生", "弟子", "专精者", "专家", "大师" },
					{ "防御性护壁I", "防御性护壁II", "防御性护壁III", "生存之域", "共振" }, { "", "延伸持续时间", "增加强度", "增加效率", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "学生", "弟子", "专精者", "专家", "大师" },
					{ "着魔", "催眠", "慌张", "恐怖", "盟友" }, { "", "延伸持续时间I", "延伸持续时间II", "增加效率", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "学生", "弟子", "专精者", "专家", "大师" },
					{ "疼痛之印", "无畏", "茫然之印", "盲目之印", "死亡之圈" }, { "", "抄写", "削弱之印", "增加效率", "" } } };
	private static final String[][][] databaseLabels = {
			{ { "Aard1", "Aard2", "Aard3", "Aard4", "Aard5" },
					{ "Aard1 Powerup", "Aard2 Powerup", "Aard3 Powerup", "Aard4 Powerup", "Aard5 Powerup" },
					{ "Aard1 Upgrade1", "Aard2 Upgrade1", "Aard3 Upgrade1", "Aard4 Upgrade1", "Aard5 Upgrade1" },
					{ "", "Aard2 Upgrade2", "Aard3 Upgrade2", "Aard4 Upgrade2", "" } },
			{ { "Igni1", "Igni2", "Igni3", "Igni4", "Igni5" },
					{ "Igni1 Powerup", "Igni2 Powerup", "Igni3 Powerup", "Igni4 Powerup", "Igni5 Powerup" },
					{ "Igni1 Upgrade1", "Igni2 Upgrade1", "Igni3 Upgrade1", "Igni4 Upgrade1", "Igni5 Upgrade1" },
					{ "", "Igni2 Upgrade2", "Igni3 Upgrade2", "Igni4 Upgrade2", "" } },
			{ { "Quen1", "Quen2", "Quen3", "Quen4", "Quen5" },
					{ "Quen1 Powerup", "Quen2 Powerup", "Quen3 Powerup", "Quen4 Powerup", "Quen5 Powerup" },
					{ "Quen1 Upgrade1", "Quen2 Upgrade1", "Quen3 Upgrade1", "Quen4 Upgrade1", "Quen5 Upgrade1" },
					{ "", "Quen2 Upgrade2", "Quen3 Upgrade2", "Quen4 Upgrade2", "" } },
			{ { "Axi1", "Axi2", "Axi3", "Axi4", "Axi5" },
					{ "Axi1 Powerup", "Axi2 Powerup", "Axi3 Powerup", "Axi4 Powerup", "Axi5 Powerup" },
					{ "Axi1 Upgrade1", "Axi2 Upgrade1", "Axi3 Upgrade1", "Axi4 Upgrade1", "Axi5 Upgrade1" },
					{ "", "Axi2 Upgrade2", "Axi3 Upgrade2", "Axi4 Upgrade2", "" } },
			{ { "Yrden1", "Yrden2", "Yrden3", "Yrden4", "Yrden5" },
					{ "Yrden1 Powerup", "Yrden2 Powerup", "Yrden3 Powerup", "Yrden4 Powerup", "Yrden5 Powerup" },
					{ "Yrden1 Upgrade1", "Yrden2 Upgrade1", "Yrden3 Upgrade1", "Yrden4 Upgrade1", "Yrden5 Upgrade1" },
					{ "", "Yrden2 Upgrade2", "Yrden3 Upgrade2", "Yrden4 Upgrade2", "" } } };
	private boolean[][] done = new boolean[4][5];
	private DBList list;
	private DBList playerList;
	private int tab;
	private int[][] signLevels;
	private Map<String, JCheckBox> labelMap;
	private JCheckBox[][][] fields;
	private JTabbedPane tabbedPane;

	public SignsPanel() {
		this.tabbedPane = new JTabbedPane();
		int tabs = fieldNames.length;
		int rows = fieldNames[0].length;
		int cols = fieldNames[0][0].length;
		this.fields = new JCheckBox[tabs][rows][cols];
		this.signLevels = new int[tabs][2];
		this.labelMap = new HashMap(tabs * rows * cols);
		for (int tab = 0; tab < tabs; tab++) {
			JPanel panel = new JPanel(new GridLayout(0, cols, 5, 5));
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					if (fieldNames[tab][row][col].length() > 0) {
						JCheckBox field = new JCheckBox(fieldNames[tab][row][col]);
						field.setActionCommand(Integer.toString(tab * 100 + row * 10 + col));
						field.addActionListener(this);
						this.fields[tab][row][col] = field;
						panel.add(field);
						this.labelMap.put(databaseLabels[tab][row][col], field);
					} else {
						panel.add(new JLabel());
					}
				}
			}
			this.tabbedPane.addTab(tabNames[tab], panel);
		}
		add(this.tabbedPane);
	}

	public void actionPerformed(ActionEvent ae) {
		if ((!(ae.getSource() instanceof JCheckBox)) || (Main.dataChanging)) {
			return;
		}
		try {
			int value = Integer.parseInt(ae.getActionCommand());
			tab = value / 100;
			int row = value % 100 / 10;
			int col = value % 10;
			JCheckBox field = this.fields[tab][row][col];
			list = (DBList) Main.database.getTopLevelStruct().getValue();
			list = (DBList) list.getElement("Mod_PlayerList").getValue();
			playerList = (DBList) list.getElement(0).getValue();
			list = (DBList) playerList.getElement("CharAbilities").getValue();
			if (field.isSelected()) {
				this.fields[tab][row][col].setSelected(false);
				doneInitialize();
				for (int t = 0; t <= col; t++) {
					this.fields[tab][0][t].setSelected(true);
					addSign(0, t);
				}
				if (row == 1) {
					for (int t = 0; t <= col; t++) {
						this.fields[tab][1][t].setSelected(true);
						addSign(1, t);
					}
				} else if (row != 0) {
					addSign(row, col);
				}
				this.fields[tab][row][col].setSelected(true);
			} else {
				if (row == 0) {
					for (int t = col; t < 5; t++) {
						this.fields[tab][0][t].setSelected(false);
						removeSign(0, t);
					}
					for (int t1 = 1; t1 < 3; t1++) {
						for (int t2 = col; t2 < 5; t2++) {
							this.fields[tab][t1][t2].setSelected(false);
							removeSign(t1, t2);
						}
					}
					if (col > 0)
						for (int t = col; t < 4; t++) {
							this.fields[tab][3][t].setSelected(false);
							removeSign(3, t);
						}
					else
						for (int t = 1; t < 4; t++) {
							this.fields[tab][3][t].setSelected(false);
							removeSign(3, t);
						}
				} else if (row == 1) {
					for (int t = col; t < 5; t++) {
						this.fields[tab][1][t].setSelected(false);
						removeSign(1, t);
					}
				} else
					removeSign(row, col);
			}
		} catch (Throwable exc) {
			Main.logException("处理异常", exc);
		}
	}

	public void setFields(DBList list) throws DBException {
		for (int tab = 0; tab < this.fields.length; tab++) {
			for (int row = 0; row < this.fields[0].length; row++) {
				for (int col = 0; col < this.fields[0][0].length; col++) {
					if (this.fields[tab][row][col] != null) {
						this.fields[tab][row][col].setSelected(false);
					}
				}
			}
			this.signLevels[tab][0] = -1;
			this.signLevels[tab][1] = -1;
		}
		DBElement element = list.getElement("CharAbilities");
		if (element == null) {
			throw new DBException("CharAbilities字段未找到");
		}
		DBList abilityList = (DBList) element.getValue();
		int count = abilityList.getElementCount();
		for (int index = 0; index < count; index++) {
			DBList fieldList = (DBList) abilityList.getElement(index).getValue();
			String abilityName = fieldList.getString("RnAbName");
			JCheckBox field = (JCheckBox) this.labelMap.get(abilityName);
			if (field != null) {
				field.setSelected(true);
				int value = Integer.parseInt(field.getActionCommand());
				int tab = value / 100;
				int row = value % 100 / 10;
				int col = value % 10;
				if ((row < 2) && (col > this.signLevels[tab][row]))
					this.signLevels[tab][row] = col;
			}
		}
	}

	public void doneInitialize() {
		for (int a = 0; a < 3; a++) {
			for (int b = 0; b < 5; b++) {
				done[a][b] = this.fields[tab][a][b].isSelected();
			}
			done[3][1] = this.fields[tab][3][1].isSelected();
			done[3][2] = this.fields[tab][3][2].isSelected();
			done[3][3] = this.fields[tab][3][3].isSelected();
		}
	}

	public void addSign(int row, int col) throws DBException {
		if (done[row][col] == false) {
			String abilityLabel = databaseLabels[tab][row][col];
			DBList fieldList = new DBList(2);
			fieldList.addElement(new DBElement(10, 0, "RnAbName", abilityLabel));
			fieldList.addElement(new DBElement(0, 0, "RnAbStk", new Integer(0)));
			list.addElement(new DBElement(14, 48879, "", fieldList));
			Main.dataModified = true;
		}
	}

	public void removeSign(int row, int col) throws DBException {
		String abilityLabel = databaseLabels[tab][row][col];
		int count = list.getElementCount();
		for (int i = 0; i < count; i++) {
			DBList fieldList = (DBList) list.getElement(i).getValue();
			String name = fieldList.getString("RnAbName");
			if (abilityLabel.equals(name)) {
				list.removeElement(i);
				Main.dataModified = true;
				break;
			}
		}
	}

	public void getFields(DBList list) throws DBException {
	}
}