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

public class AttributesPanel extends JPanel implements ActionListener {
	private static final String[] tabNames = { "力量", "机敏", "耐力", "智慧" };
	private static final String[][][] fieldNames = {
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "亢奋", "姿势", "热情", "对流血抗性", "受伤抗性" },
					{ "真之斗志", "恢复", "击倒抗性", "石皮术", "增加生命值" }, { "", "斗殴", "生存本能", "攻势", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "剥取", "挡开箭矢", "对盲目抗性", "巧技", "警觉" },
					{ "掠食者", "挡回", "敏捷", "佯攻", "精确" }, { "", "拳击", "限制燃烧", "对燃烧抵抗", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "重量级", "吸收", "耐力恢复", "麻痹免疫力", "药水耐力" },
					{ "变异", "对毒素抗性", "对疼痛抗性", "肌力", "增加耐力" }, { "", "耐力恢复", "苏醒", "改变新陈代谢", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "酿造药水", "药草学", "净化仪式的知识", "集中", "精神耐力" },
					{ "月出", "怪物知识", "原料取出", "生命仪式的知识", "强度" }, { "", "油品备制", "炸弹备制", "魔法狂乱", "" } } };
	private static final String[][][] databaseLabels = {
			{ { "Strength1", "Strength2", "Strength3", "Strength4", "Strength5" },
					{ "Strength1 Upgrade1", "Strength2 Upgrade1", "Strength3 Upgrade1", "Strength4 Upgrade1",
							"Strength5 Upgrade1" },
					{ "Strength1 Upgrade2", "Strength2 Upgrade2", "Strength3 Upgrade2", "Strength4 Upgrade2",
							"Strength5 Upgrade2" },
					{ "", "Strength2 Upgrade3", "Strength3 Upgrade3", "Strength4 Upgrade3", "" } },
			{ { "Dexterity1", "Dexterity2", "Dexterity3", "Dexterity4", "Dexterity5" },
					{ "Dexterity1 Upgrade1", "Dexterity2 Upgrade1", "Dexterity3 Upgrade1", "Dexterity4 Upgrade1",
							"Dexterity5 Upgrade1" },
					{ "Dexterity1 Upgrade2", "Dexterity2 Upgrade2", "Dexterity3 Upgrade2", "Dexterity4 Upgrade2",
							"Dexterity5 Upgrade2" },
					{ "", "Dexterity2 Upgrade3", "Dexterity3 Upgrade3", "Dexterity4 Upgrade3", "" } },
			{ { "Endurance1", "Endurance2", "Endurance3", "Endurance4", "Endurance5" },
					{ "Endurance1 Upgrade1", "Endurance2 Upgrade1", "Endurance3 Upgrade1", "Endurance4 Upgrade1",
							"Endurance5 Upgrade1" },
					{ "Endurance1 Upgrade2", "Endurance2 Upgrade2", "Endurance3 Upgrade2", "Endurance4 Upgrade2",
							"Endurance5 Upgrade2" },
					{ "", "Endurance2 Upgrade3", "Endurance3 Upgrade3", "Endurance4 Upgrade3", "" } },
			{ { "Intelligence1", "Intelligence2", "Intelligence3", "Intelligence4", "Intelligence5" },
					{ "Intelligence1 Upgrade1", "Intelligence2 Upgrade1", "Intelligence3 Upgrade1",
							"Intelligence4 Upgrade1", "Intelligence5 Upgrade1" },
					{ "Intelligence1 Upgrade2", "Intelligence2 Upgrade2", "Intelligence3 Upgrade2",
							"Intelligence4 Upgrade2", "Intelligence5 Upgrade2" },
					{ "", "Intelligence2 Upgrade3", "Intelligence3 Upgrade3", "Intelligence4 Upgrade3", "" } } };
	private static final String[][] associatedLabels = { { "Dexterity1 Upgrade1", "Skinning" },
			{ "Intelligence2 Upgrade1", "HerbGathering" }, { "Intelligence2 Upgrade3", "GreaseMaking" },
			{ "Intelligence3 Upgrade1", "RitualOfPurify" }, { "Intelligence3 Upgrade2", "Anatomy" },
			{ "Intelligence3 Upgrade3", "BombMaking" }, { "Intelligence4 Upgrade2", "RitualOfLife" } };
	private boolean[][] done = new boolean[4][5];
	private DBList list;
	private int tab;
	private int[] levels;
	private Map<String, JCheckBox> labelMap;
	private JCheckBox[][][] fields;
	private JTabbedPane tabbedPane;

	public AttributesPanel() {
		this.tabbedPane = new JTabbedPane();
		int tabs = fieldNames.length;
		int rows = fieldNames[0].length;
		int cols = fieldNames[0][0].length;
		this.fields = new JCheckBox[tabs][rows][cols];
		this.levels = new int[tabs];
		this.labelMap = new HashMap<String, JCheckBox>(tabs * rows * cols);
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
			DBList playerList = (DBList) list.getElement(0).getValue();
			list = (DBList) playerList.getElement("CharAbilities").getValue();
			if (field.isSelected()) {
				this.fields[tab][row][col].setSelected(false);
				doneInitialize();
				for (int t = 0; t <= col; t++) {
					this.fields[tab][0][t].setSelected(true);
					addAbility(0, t);
				}
				if (row != 0) {
					addAbility(row, col);
				}
				this.fields[tab][row][col].setSelected(true);
			} else {
				if (row == 0) {
					for (int t = col; t < 5; t++) {
						this.fields[tab][0][t].setSelected(false);
						removeAbility(0, t);
					}
					for (int t1 = 1; t1 < 3; t1++) {
						for (int t2 = col; t2 < 5; t2++) {
							this.fields[tab][t1][t2].setSelected(false);
							removeAbility(t1, t2);
						}
					}
					if (col > 0)
						for (int t = col; t < 4; t++) {
							this.fields[tab][3][t].setSelected(false);
							removeAbility(3, t);
						}
					else
						for (int t = 1; t < 4; t++) {
							this.fields[tab][3][t].setSelected(false);
							removeAbility(3, t);
						}
				} else
					removeAbility(row, col);
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
			this.levels[tab] = -1;
		}
		DBElement element = list.getElement("CharAbilities");
		if (element == null) {
			throw new DBException("CharAbilities field not found");
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
				if ((row == 0) && (col > this.levels[tab]))
					this.levels[tab] = col;
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

	public void addAbility(int row, int col) {
		if (done[row][col] == false) {
			String abilityLabel = databaseLabels[tab][row][col];
			DBList fieldList = new DBList(2);
			fieldList.addElement(new DBElement(10, 0, "RnAbName", abilityLabel));
			fieldList.addElement(new DBElement(0, 0, "RnAbStk", new Integer(0)));
			list.addElement(new DBElement(14, 48879, "", fieldList));
			for (int i = 0; i < associatedLabels.length; i++) {
				if (abilityLabel.equals(associatedLabels[i][0])) {
					String associatedLabel = associatedLabels[i][1];
					fieldList = new DBList(2);
					fieldList.addElement(new DBElement(10, 0, "RnAbName", associatedLabel));
					fieldList.addElement(new DBElement(0, 0, "RnAbStk", new Integer(0)));
					list.addElement(new DBElement(14, 48879, "", fieldList));
					break;
				}
			}
		}
	}

	public void removeAbility(int row, int col) throws DBException {
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
		for (int i = 0; i < associatedLabels.length; i++) {
			if (abilityLabel.equals(associatedLabels[i][0])) {
				String associatedLabel = associatedLabels[i][1];
				count = list.getElementCount();
				for (int j = 0; j < count; j++) {
					DBList fieldList = (DBList) list.getElement(j).getValue();
					String name = fieldList.getString("RnAbName");
					if (name.equals(associatedLabel)) {
						list.removeElement(j);
						Main.dataModified = true;
						break;
					}
				}
				break;
			}
		}
	}

	public void getFields(DBList list) throws DBException {
	}
}