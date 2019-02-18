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

public class StylesPanel extends JPanel implements ActionListener {
	private static final String[] tabNames = { "钢之强击", "钢之迅速", "钢之群击", "银之强击", "银之迅速", "银之群击" };
	private static final String[][][] fieldNames = {
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "咽喉斩I", "咽喉斩II", "咽喉斩III", "", "" },
					{ "压溃之击I", "压溃之击II", "压溃之击III", "", "" }, { "血腥狂暴I", "血腥狂暴II", "血腥狂暴III", "", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "麻痹I", "麻痹II", "麻痹III", "", "" },
					{ "连珠攻击I", "连珠攻击II", "连珠攻击III", "", "" }, { "切断肌腱I", "切断肌腱II", "切断肌腱III", "", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "精准之击I", "精准之击II", "精准之击III", "", "" },
					{ "半回旋击I", "半回旋击II", "半回旋击III", "", "" }, { "绊倒I", "绊倒II", "绊倒III", "", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "深砍I", "深砍II", "深砍III", "", "" },
					{ "死亡打击I", "死亡打击II", "死亡打击III", "", "" }, { "PATINADO I", "PATINADO II", "PATINADO III", "", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "绞握之痛I", "绞握之痛II", "绞握之痛III", "", "" },
					{ "闪光斩I", "闪光斩II", "闪光斩III", "", "" }, { "恶意之击I", "恶意之击II", "恶意之击III", "", "" } },
			{ { "等级1", "等级2", "等级3", "等级4", "等级5" }, { "致命打击I", "致命打击II", "致命打击III", "", "" },
					{ "风暴I", "风暴II", "风暴III", "", "" }, { "击倒I", "击倒II", "击倒III", "", "" } } };
	private static final String[][][] databaseLabels = { {
			{ "StyleSteelStrong1", "StyleSteelStrong2", "StyleSteelStrong3", "StyleSteelStrong4", "StyleSteelStrong5" },
			{ "StyleSteelStrong1 Upgrade1", "StyleSteelStrong2 Upgrade1", "StyleSteelStrong3 Upgrade1", "", "" },
			{ "StyleSteelStrong1 Upgrade2", "StyleSteelStrong2 Upgrade2", "StyleSteelStrong3 Upgrade2", "", "" },
			{ "StyleSteelStrong1 Upgrade3", "StyleSteelStrong2 Upgrade3", "StyleSteelStrong3 Upgrade3", "", "" } },
			{ { "StyleSteelFast1", "StyleSteelFast2", "StyleSteelFast3", "StyleSteelFast4", "StyleSteelFast5" },
					{ "StyleSteelFast1 Upgrade1", "StyleSteelFast2 Upgrade1", "StyleSteelFast3 Upgrade1", "", "" },
					{ "StyleSteelFast1 Upgrade2", "StyleSteelFast2 Upgrade2", "StyleSteelFast3 Upgrade2", "", "" },
					{ "StyleSteelFast1 Upgrade3", "StyleSteelFast2 Upgrade3", "StyleSteelFast3 Upgrade3", "", "" } },
			{ { "StyleSteelGroup1", "StyleSteelGroup2", "StyleSteelGroup3", "StyleSteelGroup4", "StyleSteelGroup5" },
					{ "StyleSteelGroup1 Upgrade1", "StyleSteelGroup2 Upgrade1", "StyleSteelGroup3 Upgrade1", "", "" },
					{ "StyleSteelGroup1 Upgrade2", "StyleSteelGroup2 Upgrade2", "StyleSteelGroup3 Upgrade2", "", "" },
					{ "StyleSteelGroup1 Upgrade3", "StyleSteelGroup2 Upgrade3", "StyleSteelGroup3 Upgrade3", "", "" } },
			{ { "StyleSilverStrong1", "StyleSilverStrong2", "StyleSilverStrong3", "StyleSilverStrong4",
					"StyleSilverStrong5" },
					{ "StyleSilverStrong1 Upgrade1", "StyleSilverStrong2 Upgrade1", "StyleSilverStrong3 Upgrade1", "",
							"" },
					{ "StyleSilverStrong1 Upgrade2", "StyleSilverStrong2 Upgrade2", "StyleSilverStrong3 Upgrade2", "",
							"" },
					{ "StyleSilverStrong1 Upgrade3", "StyleSilverStrong2 Upgrade3", "StyleSilverStrong3 Upgrade3", "",
							"" } },
			{ { "StyleSilverFast1", "StyleSilverFast2", "StyleSilverFast3", "StyleSilverFast4", "StyleSilverFast5" },
					{ "StyleSilverFast1 Upgrade1", "StyleSilverFast2 Upgrade1", "StyleSilverFast3 Upgrade1", "", "" },
					{ "StyleSilverFast1 Upgrade2", "StyleSilverFast2 Upgrade2", "StyleSilverFast3 Upgrade2", "", "" },
					{ "StyleSilverFast1 Upgrade3", "StyleSilverFast2 Upgrade3", "StyleSilverFast3 Upgrade3", "", "" } },
			{ { "StyleSilverGroup1", "StyleSilverGroup2", "StyleSilverGroup3", "StyleSilverGroup4",
					"StyleSilverGroup5" },
					{ "StyleSilverGroup1 Upgrade1", "StyleSilverGroup2 Upgrade1", "StyleSilverGroup3 Upgrade1", "",
							"" },
					{ "StyleSilverGroup1 Upgrade2", "StyleSilverGroup2 Upgrade2", "StyleSilverGroup3 Upgrade2", "",
							"" },
					{ "StyleSilverGroup1 Upgrade3", "StyleSilverGroup2 Upgrade3", "StyleSilverGroup3 Upgrade3", "",
							"" } } };
	private boolean[][] done = new boolean[4][5];
	private int tab;
	private DBList list;
	private int[] levels;
	private Map<String, JCheckBox> labelMap;
	private JCheckBox[][][] fields;
	private JTabbedPane tabbedPane;

	public StylesPanel() {
		this.tabbedPane = new JTabbedPane();
		int tabs = fieldNames.length;
		int rows = fieldNames[0].length;
		int cols = fieldNames[0][0].length;
		this.fields = new JCheckBox[tabs][rows][cols];
		this.levels = new int[tabs];
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
					for (int t1 = 1; t1 < 4; t1++) {
						for (int t2 = col; t2 < 3; t2++) {
							this.fields[tab][t1][t2].setSelected(false);
							removeAbility(t1, t2);
						}
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
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 3; b++) {
				done[a][b] = this.fields[tab][a][b].isSelected();
			}
			done[0][3] = this.fields[tab][0][3].isSelected();
			done[0][4] = this.fields[tab][0][4].isSelected();
		}
	}

	public void addAbility(int row, int col) {
		if (done[row][col] == false) {
			String abilityLabel = databaseLabels[tab][row][col];
			DBList fieldList = new DBList(2);
			fieldList.addElement(new DBElement(10, 0, "RnAbName", abilityLabel));
			fieldList.addElement(new DBElement(0, 0, "RnAbStk", new Integer(0)));
			list.addElement(new DBElement(14, 48879, "", fieldList));
			if ((row == 0) && (col > this.levels[tab])) {
				this.levels[tab] = col;
			}
			Main.dataModified = true;
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
		if ((row == 0) && (col == this.levels[tab]))
			this.levels[tab] = (col - 1);
	}

	public void getFields(DBList list) throws DBException {
	}
}