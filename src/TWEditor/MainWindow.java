package TWEditor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainWindow extends JFrame implements ActionListener {
	private boolean windowMinimized = false;
	private boolean titleModified = false;
	private JTabbedPane tabbedPane;
	private StatsPanel statsPanel;
	private AttributesPanel attributesPanel;
	private SignsPanel signsPanel;
	private StylesPanel stylesPanel;
	private EquipPanel equipPanel;
	private InventoryPanel inventoryPanel;
	private QuestsPanel questsPanel;

	public MainWindow() {
		super("巫师存档编辑器");
		setDefaultCloseOperation(2);
		String propValue = Main.properties.getProperty("window.main.position");
		if (propValue != null) {
			int sep = propValue.indexOf(',');
			int frameX = Integer.parseInt(propValue.substring(0, sep));
			int frameY = Integer.parseInt(propValue.substring(sep + 1));
			setLocation(frameX, frameY);
		}
		int frameWidth = 800;
		int frameHeight = 600;
		propValue = Main.properties.getProperty("window.main.size");
		if (propValue != null) {
			int sep = propValue.indexOf(',');
			frameWidth = Math.max(Integer.parseInt(propValue.substring(0, sep)), frameWidth);
			frameHeight = Math.max(Integer.parseInt(propValue.substring(sep + 1)), frameHeight);
		}
		setPreferredSize(new Dimension(frameWidth, frameHeight));
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		JMenu menu = new JMenu("文件");
		menu.setMnemonic(70);
		JMenuItem menuItem = new JMenuItem("打开");
		menuItem.setActionCommand("open");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("保存");
		menuItem.setActionCommand("save");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("关闭");
		menuItem.setActionCommand("close");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menu.addSeparator();
		menuItem = new JMenuItem("退出");
		menuItem.setActionCommand("exit");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		menu = new JMenu("编辑");
		menu.setMnemonic(65);
		menuItem = new JMenuItem("解包存档");
		menuItem.setActionCommand("unpack save");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuItem = new JMenuItem("重打包存档");
		menuItem.setActionCommand("repack save");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		menu = new JMenu("帮助");
		menu.setMnemonic(72);
		menuItem = new JMenuItem("关于");
		menuItem.setActionCommand("about");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		menuBar.add(menu);
		setJMenuBar(menuBar);
		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setVisible(false);
		setContentPane(this.tabbedPane);
		JPanel panel = new JPanel();
		this.statsPanel = new StatsPanel();
		panel.add(this.statsPanel);
		this.tabbedPane.addTab("总览", panel);
		panel = new JPanel();
		this.attributesPanel = new AttributesPanel();
		panel.add(this.attributesPanel);
		this.tabbedPane.addTab("属性", panel);
		panel = new JPanel();
		this.signsPanel = new SignsPanel();
		panel.add(this.signsPanel);
		this.tabbedPane.addTab("法印", panel);
		panel = new JPanel();
		this.stylesPanel = new StylesPanel();
		panel.add(this.stylesPanel);
		this.tabbedPane.addTab("剑术", panel);
		panel = new JPanel();
		this.equipPanel = new EquipPanel();
		panel.add(this.equipPanel);
		this.tabbedPane.addTab("装备", panel);
		panel = new JPanel();
		this.inventoryPanel = new InventoryPanel();
		panel.add(this.inventoryPanel);
		this.tabbedPane.addTab("物品", panel);
		panel = new JPanel();
		this.questsPanel = new QuestsPanel();
		panel.add(this.questsPanel);
		this.tabbedPane.addTab("任务", panel);
		addWindowListener(new ApplicationWindowListener(this));
	}

	public void setTitle(String title) {
		if (title != null) {
			super.setTitle(title);
			this.titleModified = false;
		} else if (Main.saveDatabase == null) {
			super.setTitle("巫师存档编辑器");
			this.titleModified = false;
		} else if ((Main.dataModified) && (!this.titleModified)) {
			super.setTitle("巫师存档编辑器 - " + Main.saveDatabase.getName() + "*");
			this.titleModified = true;
		} else if ((!Main.dataModified) && (this.titleModified)) {
			super.setTitle("巫师存档编辑器 - " + Main.saveDatabase.getName());
			this.titleModified = false;
		}
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			String action = ae.getActionCommand();
			if (action.equals("open")) {
				openFile();
				if (Main.saveDatabase != null)
					setTitle("巫师存档编辑器 - " + Main.saveDatabase.getName());
				else
					setTitle(null);
			} else if (action.equals("about")) {
				aboutProgram();
			} else if (action.equals("exit")) {
				exitProgram();
			} else if (Main.saveDatabase == null) {
				JOptionPane.showMessageDialog(this, "没有打开的存档", "没有存档", 0);
			} else if (action.equals("save")) {
				saveFile();
				setTitle(null);
			} else if (action.equals("close")) {
				closeFile();
				setTitle(null);
			} else if (action.equals("unpack save")) {
				unpackSave();
			} else if (action.equals("repack save")) {
				packSave();
				setTitle(null);
			}
		} catch (Throwable exc) {
			Main.logException("处理异常", exc);
		}
	}

	private void openFile() {
		if (!closeFile()) {
			return;
		}
		String currentDirectory = Main.properties.getProperty("current.directory");
		JFileChooser chooser;
		if (currentDirectory != null) {
			File dirFile = new File(currentDirectory);
			if ((dirFile.exists()) && (dirFile.isDirectory()))
				chooser = new JFileChooser(dirFile);
			else
				chooser = new JFileChooser(Main.gamePath + Main.fileSeparator + "saves");
		} else {
			chooser = new JFileChooser(Main.gamePath + Main.fileSeparator + "saves");
		}
		chooser.putClientProperty("FileChooser.useShellFolder", Boolean.valueOf(Main.useShellFolder));
		chooser.setDialogTitle("选择存档");
		if (chooser.showOpenDialog(this) != 0) {
			return;
		}
		File file = chooser.getSelectedFile();
		Main.properties.setProperty("current.directory", file.getParent());
		loadSave(file);
	}

	private void loadSave(File file) {
		String saveName = file.getName();
		int sep = saveName.lastIndexOf('.');
		if (sep > 0) {
			saveName = saveName.substring(0, sep);
		}
		ProgressDialog dialog = new ProgressDialog(this, "加载中 " + saveName);
		LoadFile task = new LoadFile(dialog, file);
		task.start();
		boolean success = dialog.showDialog();
		if (success)
			try {
				Main.dataChanging = true;
				DBList list = (DBList) Main.database.getTopLevelStruct().getValue();
				list = (DBList) list.getElement("Mod_PlayerList").getValue();
				list = (DBList) list.getElement(0).getValue();
				this.statsPanel.setFields(list);
				this.attributesPanel.setFields(list);
				this.signsPanel.setFields(list);
				this.stylesPanel.setFields(list);
				this.equipPanel.setFields(list);
				this.inventoryPanel.setFields(list);
				this.questsPanel.setFields(list);
				this.tabbedPane.setSelectedIndex(0);
				this.tabbedPane.setVisible(true);
				Main.dataChanging = false;
				Main.dataModified = false;
			} catch (DBException exc) {
				Main.logException("数据库格式无效", exc);
			} catch (IOException exc) {
				Main.logException("读取错误", exc);
			}
	}

	private boolean saveFile() {
		if (Main.saveDatabase == null) {
			return false;
		}
		boolean saved = false;
		try {
			DBList list = (DBList) Main.database.getTopLevelStruct().getValue();
			list = (DBList) list.getElement("Mod_PlayerList").getValue();
			list = (DBList) list.getElement(0).getValue();
			this.statsPanel.getFields(list);
			this.attributesPanel.getFields(list);
			this.signsPanel.getFields(list);
			this.stylesPanel.getFields(list);
			this.equipPanel.getFields(list);
			this.inventoryPanel.getFields(list);
			this.questsPanel.getFields(list);
			ProgressDialog dialog = new ProgressDialog(this, "保存中 " + Main.saveDatabase.getName());
			SaveFile task = new SaveFile(dialog);
			task.start();
			saved = dialog.showDialog();
			if (saved)
				Main.dataModified = false;
		} catch (DBException exc) {
			Main.logException("数据库格式无效", exc);
		}
		return saved;
	}

	private boolean closeFile() {
		if (Main.saveDatabase == null) {
			return true;
		}
		if (Main.dataModified) {
			int option = JOptionPane.showConfirmDialog(this, "存档已被更改，是否保存？", "保存修改", 1);
			if (option == 2) {
				return false;
			}
			if ((option == 0) && (!saveFile())) {
				return false;
			}
		}
		Main.database = null;
		Main.modDatabase = null;
		Main.saveDatabase = null;
		Main.dataModified = false;
		this.tabbedPane.setVisible(false);
		return true;
	}

	private void unpackSave() {
		String extractDirectory = Main.properties.getProperty("extract.directory");
		JFileChooser chooser;
		if (extractDirectory != null) {
			File dirFile = new File(extractDirectory);
			if ((dirFile.exists()) && (dirFile.isDirectory()))
				chooser = new JFileChooser(dirFile);
			else
				chooser = new JFileChooser();
		} else {
			chooser = new JFileChooser();
		}
		chooser.putClientProperty("FileChooser.useShellFolder", Boolean.valueOf(Main.useShellFolder));
		chooser.setDialogTitle("选择目标目录");
		chooser.setApproveButtonText("选择");
		chooser.setFileSelectionMode(1);
		if (chooser.showOpenDialog(this) != 0) {
			return;
		}
		File dirFile = chooser.getSelectedFile();
		Main.properties.setProperty("extract.directory", dirFile.getPath());
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		ProgressDialog dialog = new ProgressDialog(this, "解包中 " + Main.saveDatabase.getName());
		UnpackSave task = new UnpackSave(dialog, dirFile);
		task.start();
		if (dialog.showDialog())
			JOptionPane.showMessageDialog(this, "存档已解包到" + dirFile.getPath(), "存档解包", 1);
	}

	private void packSave() {
		if (Main.dataModified) {
			int option = JOptionPane.showConfirmDialog(this, "当前存档已被修改，这些修改将无效。你想继续吗？", "保存修改", 0);
			if (option != 0) {
				return;
			}
		}
		String extractDirectory = Main.properties.getProperty("extract.directory");
		JFileChooser chooser;
		if (extractDirectory != null) {
			File dirFile = new File(extractDirectory);
			if ((dirFile.exists()) && (dirFile.isDirectory()))
				chooser = new JFileChooser(dirFile);
			else
				chooser = new JFileChooser();
		} else {
			chooser = new JFileChooser();
		}
		chooser.putClientProperty("FileChooser.useShellFolder", Boolean.valueOf(Main.useShellFolder));
		chooser.setDialogTitle("选择源目录");
		chooser.setApproveButtonText("选择");
		chooser.setFileSelectionMode(1);
		if (chooser.showOpenDialog(this) != 0) {
			return;
		}
		File dirFile = chooser.getSelectedFile();
		Main.properties.setProperty("extract.directory", dirFile.getPath());
		if (!dirFile.exists()) {
			JOptionPane.showMessageDialog(this, "源目录不存在", "目录不存在", 0);
			return;
		}
		Main.dataModified = false;
		ProgressDialog dialog = new ProgressDialog(this, "打包中" + Main.saveDatabase.getName());
		PackFile task = new PackFile(dialog, dirFile);
		task.start();
		boolean saved = dialog.showDialog();
		File file = Main.saveDatabase.getFile();
		closeFile();
		if (saved)
			loadSave(file);
	}

	private void exitProgram() {
		closeFile();
		if (Main.modFile.exists()) {
			Main.modFile.delete();
		}
		if (Main.databaseFile.exists()) {
			Main.databaseFile.delete();
		}
		if (!this.windowMinimized) {
			Point p = Main.mainWindow.getLocation();
			Dimension d = Main.mainWindow.getSize();
			Main.properties.setProperty("window.main.position", p.x + "," + p.y);
			Main.properties.setProperty("window.main.size", d.width + "," + d.height);
		}
		Main.saveProperties();
		System.exit(0);
	}

	private void aboutProgram() {
		StringBuilder info = new StringBuilder(256);
		info.append("<html>巫师存档编辑器2.1汉化修改版<br>");
		info.append("<br>用户名: ");
		info.append(System.getProperty("user.name"));
		info.append("<br>用户目录: ");
		info.append(System.getProperty("user.home"));
		info.append("<br><br>系统: ");
		info.append(System.getProperty("os.name"));
		info.append("<br>系统版本: ");
		info.append(System.getProperty("os.version"));
		info.append("<br>系统补丁级别: ");
		info.append(System.getProperty("sun.os.patch.level"));
		info.append("<br><br>Java供应商: ");
		info.append(System.getProperty("java.vendor"));
		info.append("<br>Java版本: ");
		info.append(System.getProperty("java.version"));
		info.append("<br>Java目录: ");
		info.append(System.getProperty("java.home"));
		info.append("<br>Java类路径: ");
		info.append(System.getProperty("java.class.path"));
		info.append("<br><br>巫师安装目录: ");
		info.append(Main.installPath);
		info.append("<br>巫师数据目录: ");
		info.append(Main.gamePath);
		info.append("<br>临时文件目录: ");
		info.append(Main.tmpDir);
		info.append("<br>语言标识符: ");
		info.append(Main.languageID);
		info.append("</html>");
		JOptionPane.showMessageDialog(this, info.toString(), "关于巫师存档修改器", 1);
	}

	private class ApplicationWindowListener extends WindowAdapter {
		public ApplicationWindowListener(JFrame window) {
		}

		public void windowIconified(WindowEvent we) {
			MainWindow.this.windowMinimized = true;
		}

		public void windowDeiconified(WindowEvent we) {
			MainWindow.this.windowMinimized = false;
		}

		public void windowClosing(WindowEvent we) {
			try {
				MainWindow.this.exitProgram();
			} catch (Exception exc) {
				Main.logException("关闭应用程序窗口时出现异常", exc);
			}
		}
	}
}