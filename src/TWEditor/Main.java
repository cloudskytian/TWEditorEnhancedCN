package TWEditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class Main {
	public static JFrame mainWindow;
	public static String fileSeparator;
	public static String lineSeparator;
	public static boolean useShellFolder = true;
	public static String installPath;
	public static String installDataPath;
	public static String gamePath;
	public static String tmpDir;
	public static File propFile;
	public static Properties properties;
	public static StringsDatabase stringsDatabase;
	public static int languageID = 21;
	public static Map<String, Object> resourceFiles;
	public static List<ItemTemplate> itemTemplates;
	public static SaveDatabase saveDatabase;
	public static File databaseFile;
	public static Database database;
	public static String savePrefix;
	public static String modName;
	public static File modFile;
	public static ResourceDatabase modDatabase;
	public static List<Quest> quests;
	public static boolean dataModified = false;
	public static boolean dataChanging = false;
	private static String deferredText;
	private static Throwable deferredException;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			File dirFile;
			fileSeparator = System.getProperty("file.separator");
			lineSeparator = System.getProperty("line.separator");
			tmpDir = System.getProperty("java.io.tmpdir");
			databaseFile = new File(tmpDir + "TWEditor.ifo");
			modFile = new File(tmpDir + "TWEditor.mod");
			String option = System.getProperty("UseShellFolder");
			if (option != null && option.equals("0")) {
				useShellFolder = false;
			}
			if (!(dirFile = new File(System.getProperty("user.home") + fileSeparator + "Application Data"
					+ fileSeparator + "ScripterRon")).exists()) {
				dirFile.mkdirs();
			}
			propFile = new File(dirFile.getPath() + fileSeparator + "TWEditor.properties");
			properties = new Properties();
			if (propFile.exists()) {
				FileInputStream in = new FileInputStream(propFile);
				properties.load(in);
				in.close();
			}
			String regString = "reg query \"HKLM\\Software\\CD Projekt Red\\The Witcher\"";
			while (installPath == null) {
				Process process = Runtime.getRuntime().exec(regString);
				StreamReader streamReader = new StreamReader(process.getInputStream());
				streamReader.start();
				process.waitFor();
				streamReader.join();
				Pattern p = Pattern.compile("\\s*(\\S*)\\s*(\\S*)\\s*(.*)");
				String line;
				while ((line = streamReader.getLine()) != null) {
					Matcher m = p.matcher(line);
					if ((m.matches()) && (m.groupCount() == 3) && (m.group(2).equals("REG_SZ"))) {
						String keyName = m.group(1);
						if ((keyName.equals("InstallFolder")) && (installPath == null))
							installPath = m.group(3);
					}
				}
				if (regString != "reg query \"HKLM\\Software\\WOW6432Node\\CD Projekt Red\\The Witcher\"")
					regString = "reg query \"HKLM\\Software\\WOW6432Node\\CD Projekt Red\\The Witcher\"";
				else
					break;
			}
			if ((installPath = System.getProperty("TW.install.path")) == null) {
				installPath = properties.getProperty("install.path");
			}
			if ((System.getProperty("TW.language")) == null) {
			}
			properties.setProperty("java.version", System.getProperty("java.version"));
			properties.setProperty("java.home", System.getProperty("java.home"));
			properties.setProperty("os.name", System.getProperty("os.name"));
			properties.setProperty("sun.os.patch.level", System.getProperty("sun.os.patch.level"));
			properties.setProperty("user.name", System.getProperty("user.name"));
			properties.setProperty("user.home", System.getProperty("user.home"));
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Main.createAndShowGUI();
				}
			});
		} catch (Throwable exc) {
			Main.logException("程序初始化异常", exc);
		}
	}

	private static void processOverrides(File dirFile) {
		for (File file : dirFile.listFiles()) {
			String ext;
			if (file.isDirectory()) {
				Main.processOverrides(file);
				continue;
			}
			String name = file.getName().toLowerCase();
			int sep = name.lastIndexOf(46);
			if (sep <= 0 || !(ext = name.substring(sep)).equals(".2da") && !ext.equals(".uti"))
				continue;
			resourceFiles.put(name, file);
		}
	}

	public static void createAndShowGUI() {
		try {
			File dirFile;
			File stringsFile;
			JFrame.setDefaultLookAndFeelDecorated(true);
			while (true) {
				File exePathPath = null;
				if (installPath != null) {
					exePathPath = new File(installPath + fileSeparator + "System" + fileSeparator + "witcher.exe");
					if (exePathPath.exists())
						break;
					else
						JOptionPane.showMessageDialog(mainWindow, "你选择的目录有误，请重新选择", "目录错误", 0);
				}
				dirFile = null;
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("选择游戏目录");
				chooser.setApproveButtonText("选择");
				chooser.setFileSelectionMode(1);
				if (chooser.showOpenDialog(mainWindow) == 0) {
					dirFile = chooser.getSelectedFile();
					installPath = dirFile.getPath();
				} else
					throw new IOException("无法找到巫师安装目录");
				Runtime.getRuntime().exec(
						"reg add \"HKEY_LOCAL_MACHINE\\SOFTWARE\\CD Projekt Red\\The Witcher\" /v InstallFolder /t REG_SZ /d \""
								+ installPath + "\"\\ /f");
			}
			installDataPath = installPath + fileSeparator + "Data";
			dirFile = new File(installDataPath);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			gamePath = System.getProperty("TW.data.path");
			if (gamePath == null) {
				File defaultDir = FileSystemView.getFileSystemView().getDefaultDirectory();
				gamePath = defaultDir + fileSeparator + "The Witcher";
			}
			if (!(dirFile = new File(gamePath + fileSeparator + "saves")).exists()) {
				dirFile.mkdirs();
			}
			if (!(stringsFile = new File(installDataPath + fileSeparator + "dialog_" + languageID + ".tlk")).exists()) {
				throw new IOException("本地化字符串数据库" + stringsFile.getPath() + "不存在");
			}
			stringsDatabase = new StringsDatabase(stringsFile);
			KeyDatabase keyDatabase = new KeyDatabase(installDataPath + fileSeparator + "main.key");
			List<KeyEntry> keyEntries = keyDatabase.getEntries();
			resourceFiles = new HashMap<String, Object>(keyEntries.size());
			for (KeyEntry keyEntry : keyEntries) {
				String ext;
				String name = keyEntry.getFileName().toLowerCase();
				int sep = name.lastIndexOf(46);
				if (sep <= 0 || !(ext = name.substring(sep)).equals(".2da") && !ext.equals(".uti"))
					continue;
				resourceFiles.put(name, keyEntry);
			}
			Main.processOverrides(new File(installDataPath));
			properties.setProperty("install.path", installPath);
			properties.setProperty("language", Integer.toString(languageID));
			properties.setProperty("game.path", gamePath);
			properties.setProperty("temp.path", tmpDir);
			mainWindow = new MainWindow();
			mainWindow.pack();
			mainWindow.setVisible(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Main.buildTemplates();
				}
			});
		} catch (Throwable exc) {
			logException("初始化应用程序窗口时出现异常", exc);
		}
	}

	public static void buildTemplates() {
		ProgressDialog dialog = new ProgressDialog(mainWindow, "正在加载项目模板");
		LoadTemplates task = new LoadTemplates(dialog);
		task.start();
		dialog.showDialog();
	}

	public static void saveProperties() {
		try {
			FileOutputStream out = new FileOutputStream(propFile);
			properties.store(out, "TWEditor Properties");
			out.close();
		} catch (Throwable exc) {
			logException("保存应用程序属性时出现异常", exc);
		}
	}

	public static String getString(int stringRef) {
		return stringsDatabase.getString(stringRef);
	}

	public static String getLabel(int stringRef) {
		return stringsDatabase.getLabel(stringRef);
	}

	public static String getHeading(int stringRef) {
		return stringsDatabase.getHeading(stringRef);
	}

	public static void logException(String text, Throwable exc) {
		System.runFinalization();
		System.gc();
		if (SwingUtilities.isEventDispatchThread()) {
			StringBuilder string = new StringBuilder(512);
			string.append("<html><b>");
			string.append(text);
			string.append("</b><br><br>");
			string.append("<b>");
			string.append(exc.toString());
			string.append("</b><br><br>");
			StackTraceElement[] trace = exc.getStackTrace();
			int count = 0;
			for (StackTraceElement elem : trace) {
				string.append(elem.toString());
				string.append("<br>");
				count++;
				if (count == 25) {
					break;
				}
			}
			string.append("</html>");
			JOptionPane.showMessageDialog(mainWindow, string, "错误", 0);
		} else if (deferredException == null) {
			deferredText = text;
			deferredException = exc;
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						Main.logException(Main.deferredText, Main.deferredException);
					}
				});
			} catch (Throwable swingException) {
				deferredException = null;
				deferredText = null;
			}
		}
	}

	public static void dumpData(String text, byte[] data, int offset, int length) {
		System.out.println(text);
		for (int i = 0; i < length; i++) {
			if (i % 32 == 0)
				System.out.print(String.format(" %14X  ", new Object[] { Integer.valueOf(i) }));
			else if (i % 4 == 0) {
				System.out.print(" ");
			}
			System.out.print(String.format("%02X", new Object[] { Byte.valueOf(data[(offset + i)]) }));
			if (i % 32 == 31) {
				System.out.println();
			}
		}
		if (length % 32 != 0)
			System.out.println();
	}
}