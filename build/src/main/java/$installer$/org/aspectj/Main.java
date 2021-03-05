/* *******************************************************************
 * Copyright (c) 2000-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package $installer$.org.aspectj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Invoke the Installer gui. There are two ways to run without GUI by passing parameters to main:
 * <ol>
 * <li>pass <code>-text {pathToPropertiesFile}</code>:
 * <ul>
 * <li>"-text".equals(arg[0])</li>
 * <li>arg[1] is the path to a properties file which defines name="output.dir" value="{path to output dir}" name="context.javaPath"
 * value="{path to JDKDIR}", i.e,.
 *
 * <pre>
 * output.dir=c:/latest
 *   "context.javaPath=c:/apps/jdk1.3.1
 * </pre>
 *
 * </li>
 * <li>outputDir must be created and empty (i.e., no overwriting</li>
 * <li>the VM being invoked should be the target vm</li>
 * </ul>
 * </li>
 * <li>pass <code>-to {pathToTargetDir}</code>:
 * <ul>
 * <li>"-to".equals(arg[0])</li>
 * <li>arg[1] is the path to a writable install directory.</li>
 * </ul>
 * </li>
 */
public class Main {
	public static void main(String[] args) {
		Options.loadArgs(args);
		boolean hasGui = true;
		Properties properties = new Properties();
		InputStream istream = null;
		try {
			istream = Main.class.getResourceAsStream(Installer.RESOURCE_DIR + "/properties.txt");
			if (istream == null) {
				System.err.println("unable to load properties.txt using Main.class - exiting");
				Main.exit(-1);
			}
			properties.load(istream);
			// when running outside GUI, load values into properties
			// so that property-value resolution works
			// (otherwise, could just set values below).
			// XXX not sure if this indirection is actually needed.
			if (null != Options.textProperties) {
				istream.close();
				istream = new FileInputStream(Options.textProperties);
				properties.load(istream);
				hasGui = false;
			} else if (null != Options.targetDir) {
				String path = null;
				try {
					path = Options.targetDir.getCanonicalPath();
				} catch (IOException e) {
					path = Options.targetDir.getAbsolutePath();
				}
				String javaPath = ConfigureLauncherPane.getDefaultJavaHomeLocation();
				if (null == javaPath) {
					System.err.println("using GUI - unable to find java");
				} else {
					properties.setProperty("output.dir", path);
					properties.setProperty("context.javaPath", javaPath);
					hasGui = false;
				}
			}
		} catch (IOException ioe) {
			handleException(ioe);
		} finally {
			if (null != istream) {
				try {
					istream.close();
				} catch (IOException e) {
				} // ignore
			}
		}

		try {
			String className = (String) properties.get("installer.main.class");
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			Installer installer = (Installer) Class.forName(className).newInstance();
			InstallContext installerContext = new InstallContext(properties);
			installerContext.setHasGui(hasGui);
			installer.setContext(installerContext);
			if (installerContext.hasGui()) { // let context force whether or not to run gui
				installer.runGUI();
			} else {
				// set output dir and java path in context after minimal validation
				String propName = "output.dir";
				String propValue = properties.getProperty(propName);
				if (null == propValue) {
					throw new Exception("expecting property " + propName);
				}
				String outputDirName = propValue;
				propName = "context.javaPath";
				propValue = properties.getProperty(propName);
				if (null == propValue) {
					throw new Exception("expecting property " + propName);
				}
				String javaPath = propValue;
				File outputDir = new File(outputDirName);
				if (!outputDir.isDirectory()) {
					throw new Exception("not a dir outputDirName: " + outputDirName + " dir: " + outputDir);
				}
				if (!outputDir.canWrite()) {
					throw new Exception("cannot write outputDirName: " + outputDirName + " dir: " + outputDir);
				}
				InstallContext context = installer.getContext(); // todo: why not use installerContext?
				context.setOutputDir(outputDir);
				context.javaPath = new File(javaPath);
				// todo: check javaPath for ... bin/java? lib/rt.jar?
				if (!outputDir.isDirectory() || !outputDir.canRead()) {
					throw new Exception("invalid javaPath: " + javaPath);
				}
				// directly set context and run
				WizardPane.setContext(installerContext);
				installer.run();
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	public static void handleException(Throwable e) {
		System.out.println("internal error: " + e.toString());
		e.printStackTrace();
		Main.exit(-1);
	}

	/** indirection for System.exit - todo apply cleanup here as necessary */
	public static void exit(int value) {
		System.exit(value);
	}
} // class Main

class Options {
	public static boolean verbose = false;
	public static String textProperties = null;
	public static File targetDir = null;
	public static boolean forceError1 = false;
	public static boolean forceError2 = false;
	public static boolean forceHandConfigure = false;

	public static void loadArgs(String[] args) {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg == null) {
				continue;
			}

			if (arg.equals("-verbose")) {
				verbose = true;
			} else if (arg.equals("-forceError1")) {
				forceError1 = true;
			} else if (arg.equals("-forceError2")) {
				forceError2 = true;
			} else if (arg.equals("-forceHandConfigure")) {
				forceHandConfigure = true;
			} else if (arg.equals("-text")) {
				if (i + 1 < args.length) {
					textProperties = args[++i];
				}
			} else if (arg.equals("-to")) {
				String next = "no argument";
				if (i + 1 < args.length) {
					next = args[++i];
					File targDir = new File(next);
					if (targDir.isDirectory() && targDir.canWrite()) {
						targetDir = targDir;
					}
				}
				if (null == targetDir) {
					System.err.println("invalid -to dir: " + next);
				}
			}
		}
	}
}

/** tools installer installs the entire 1.1+ distribution */
class ToolsInstaller extends Installer {
	public String getTitle() {
		return "Installer for AspectJ(TM)";
	}

	public String getPrefix() {
		return "tools";
	}

	public String getReadmeFilename() {
		return "README-AspectJ.html";
	}

	public ToolsInstaller() {
		InstallPane installPane = new InstallPane(true);
		setInstallPane(installPane);
		panes = new WizardPane[] { new IntroPane(), new ConfigureLauncherPane(), new LocationPane(), installPane, new FinishPane() };
	}
}

class DocsInstaller extends Installer {
	public String getTitle() {
		return "AspectJ(TM) Documentation and Examples Installer";
	}

	public String getPrefix() {
		return "docs";
	}

	public DocsInstaller() {
		InstallPane installPane = new InstallPane(false);
		setInstallPane(installPane);
		panes = new WizardPane[] { new IntroPane(), new LocationPane(), installPane, new FinishPane() };
	}
}

class AJDEForJBuilderInstaller extends Installer {
	public String getTitle() {
		return "AspectJ(TM) Support for JBuilder";
	}

	public String getPrefix() {
		return "ajdeForJBuilder";
	}

	public AJDEForJBuilderInstaller() {
		InstallPane installPane = new InstallPane(false);
		setInstallPane(installPane);
		panes = new WizardPane[] { new IntroPane(), new LocationPane() {
			public String getDefaultLocation() {
				if (context.onWindows()) {
					// check some default locations
					String[] paths = { "c:\\JBuilder6\\lib\\ext", "c:\\apps\\JBuilder6\\lib\\ext",
							"c:\\Program Files\\JBuilder6\\lib\\ext" };
					int pathIndex = 0;
					for (; pathIndex < paths.length; pathIndex++) {
						if (new File(paths[pathIndex]).exists()) {
							return paths[pathIndex];
						}
					}
					return "c:\\JBuilder6\\lib\\ext";
				} else {
					return "/usr/JBuilder6/lib/ext";
				}
			}

			/**
			 * Make sure that the old jar file gets removed.
			 */
			public void verify() {
				File jbuilder = new File(location.getText() + "/../../lib/jbuilder.jar");
				if (!jbuilder.exists() && hasGui()) {
					int ret = JOptionPane.showConfirmDialog(frame, "The location you specified does not seem to be a "
							+ "valid JBuilder install directory." + " Continue?", "Confirm Install", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (ret != JOptionPane.YES_OPTION) {
						Main.exit(-1);
					} else {
						// do nothing
					}
				}

				File oldFile = new File(location.getText() + "/ajbuilder.jar");
				if (oldFile.exists() && hasGui()) {
					int ret = JOptionPane.showConfirmDialog(frame,
							"This old version of AJDE for JBuilder (\"ajbuilder.jar\") exists"
									+ " and must be removed from the install directory." + " OK to delete?", "Confirm Delete",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (ret != JOptionPane.YES_OPTION) {
						Main.exit(-1);
					} else {
						oldFile.delete();
					}
				}
			}
		}, installPane, new FinishPane() };
	}
}

class AJDEForForteInstaller extends Installer {
	public String getTitle() {
		return "AspectJ(TM) Support for Forte 4J";
	}

	public String getPrefix() {
		return "ajdeForForte";
	}

	private String installLoc = "";

	public AJDEForForteInstaller() {
		InstallPane installPane = new InstallPane(false);
		setInstallPane(installPane);
		panes = new WizardPane[] { new IntroPane(), new LocationPane() {
			public String getDefaultLocation() {
				if (context.onWindows()) {
					// check some default locations
					String[] paths = { "c:\\forte4j\\modules", "c:\\apps\\forte4j\\modules", "c:\\Program Files\\forte4j\\modules" };
					int pathIndex = 0;
					for (; pathIndex < paths.length; pathIndex++) {
						if (new File(paths[pathIndex]).exists()) {
							return paths[pathIndex];
						}
					}
					return "c:\\forte4j\\modules";
				} else {
					return "/usr/forte4j/modules";
				}
			}

			public void verify() {
				File forte = new File(location.getText() + "/../lib/openide.jar");
				installLoc = location.getText();
				if (!forte.exists() && hasGui()) {
					int ret = JOptionPane.showConfirmDialog(frame, "The location you specified does not seem to be a "
							+ "valid Forte install directory." + " Continue?", "Confirm Install", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (ret != JOptionPane.YES_OPTION) {
						Main.exit(-1);
					} else {
						// do nothing
					}
				}
			}
		}, installPane, new FinishPane() {
			public void finalActions() { // todo verify dir ../lib/ext exists?
				// !!! this should be done with two install locations, not by moving a file
				new File(installLoc + "/../lib/ext/aspectjrt.jar").delete();
				new File(installLoc + "/aspectjrt.jar").renameTo(new File((installLoc + "/../lib/ext/aspectjrt.jar")));
				new File(installLoc + "/aspectjrt.jar").delete();
			}
		} };
	}
}

class SrcInstaller extends Installer {
	public String getTitle() {
		return "AspectJ(TM) Compiler and Core Tools Sources Installer";
	}

	public String getPrefix() {
		return "sources";
	}

	public SrcInstaller() {
		InstallPane installPane = new InstallPane(false);
		setInstallPane(installPane);
		panes = new WizardPane[] { new IntroPane(), new LocationPane(), installPane, new FinishPane() };
	}
}

abstract class Installer {
	static final String EXIT_MESSAGE = "Are you sure you want to cancel the installation?";
	static final String EXIT_TITLE = "Exiting installer";
	/**
	 * relative directory in jar from package $installer$.org.aspectj for loading resources - todo must be tracked during build
	 */
	public static final String RESOURCE_DIR = "resources";

	JFrame frame;
	InstallContext context;
	/** special pane that actually does the installation */
	InstallPane installPane;

	public Installer() {
	}

	protected void setInstallPane(InstallPane installPane) {
		this.installPane = installPane;
	}

	public InstallPane getInstallPane() {
		return installPane;
	}

	/** directly run the install pane, if any */
	public void run() {
		if (null != installPane) {
			installPane.run();
		}
	}

	public abstract String getPrefix();

	public String getReadmeFilename() {
		return "README-" + getPrefix().toUpperCase() + ".html";
	}

	public void setContext(InstallContext context) {
		this.context = context;
		context.installer = this;
	}

	public InstallContext getContext() {
		return context;
	}

	public String getTitle() {
		return "AspectJ(TM) Installer";
	}

	public int getWidth() {
		return 640;
	}

	public int getHeight() {
		return 460;
	}

	protected WizardPane[] panes = new WizardPane[0];

	public WizardPane[] getPanes() {
		return panes;
	}

	public int findPaneIndex(WizardPane pane) {
		for (int i = 0; i < panes.length; i++) {
			if (panes[i] == pane) {
				return i;
			}
		}
		return -1;
	}

	Component header, footer, body;

	public void runGUI() {
		frame = new JFrame(getTitle());
		WindowListener wl = new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				Main.exit(-1); // -1 unless exiting through done button
			}
		};
		frame.addWindowListener(wl);

		if (Options.forceError1) {
			throw new RuntimeException("forced error1 for testing purposes");
		}

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

		int x = (int) (size.getWidth() - getWidth()) / 2;
		int y = (int) (size.getHeight() - getHeight()) / 2;

		//include a few sanity checks on starting position
		if (x < 0) {
			x = 0;
		}
		if (x > 600) {
			x = 600;
		}
		if (y < 0) {
			y = 0;
		}
		if (y > 400) {
			y = 400;
		}

		frame.setLocation(x, y);
		frame.setSize(getWidth(), getHeight());
		moveToPane(getPanes()[0]);
		frame.setVisible(true);
	}

	public void moveToPane(WizardPane pane) {
		WizardPane.setContext(this.context);

		Dimension size = frame.getContentPane().getSize();

		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout());
		header = makeHeader();
		contents.add(header, BorderLayout.NORTH);

		body = pane.getPanel();
		contents.add(body, BorderLayout.CENTER);

		footer = pane.getButtons();
		contents.add(footer, BorderLayout.SOUTH);

		contents.revalidate();
		contents.setSize(size);

		frame.setContentPane(contents);

		//XXX deal with threading here?
		pane.run();
	}

	public Icon loadImage(String name) {
		return new javax.swing.ImageIcon(this.getClass().getResource(name));
	}

	public Component makeHeader() {
		return new JLabel(loadImage(Installer.RESOURCE_DIR + "/aspectjBanner.gif"));
	}

	public ActionListener makeNextAction(final WizardPane pane) {
		int nextPaneIndex = findPaneIndex(pane) + 1;
		if (nextPaneIndex >= getPanes().length) {
			return null;
		}

		final WizardPane nextPane = getPanes()[nextPaneIndex];
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pane.finish();
				moveToPane(nextPane);
			}
		};
	}

	public ActionListener makeBackAction(final WizardPane pane) {
		int nextPaneIndex = findPaneIndex(pane) - 1;
		if (nextPaneIndex < 0) {
			return null;
		}

		final WizardPane nextPane = getPanes()[nextPaneIndex];
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveToPane(nextPane);
			}
		};
	}

	public ActionListener makeCancelAction(WizardPane pane) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int ret = JOptionPane.showConfirmDialog(frame, EXIT_MESSAGE, EXIT_TITLE, JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (ret == JOptionPane.YES_OPTION) {
					Main.exit(-1);
				}
			}
		};
	}

	public ActionListener makeFinishAction(WizardPane pane) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.exit(0);
			}
		};
	}
}

// willing to go up to 3 levels deep to find either jre or jdk

// jre\[*\]lib\ext
// jdk*\lib\tools.jar

/*****
 * final static int MAX_DEPTH = 4; public static void findPaths(String prefix, File currentDir, int currentDepth) { if (currentDepth
 * > MAX_DEPTH) return; if (!currentDir.exists() || !currentDir.isDirectory()) return; File [] files = currentDir.listFiles(); if
 * (files == null) return; for (int i=0; i<files.length; i++) { if (files[i] == null) continue; if (!files[i].isDirectory())
 * continue; if (files[i].getName().startsWith(prefix)) { System.out.println("found: " + files[i]); } else { findPaths(prefix,
 * files[i], currentDepth + 1); } } }
 *
 * public static void findPaths(String prefix) { File [] files = File.listRoots(); for (int i=1; i<files.length; i++) { if
 * (!files[i].isDirectory()) continue; if (files[i].getName().toLowerCase().startsWith(prefix)) { System.out.println("found: " +
 * files[i]); } else { findPaths(prefix, files[i], 1); } } }
 *****/

class InstallContext {
	public InstallContext(Map properties) {
		this.properties = properties;
		properties.put("user.home", System.getProperty("user.home"));
		//System.out.println("new install context");
	}

	private File outputDir;

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;

		properties.put("installer.output.dir", outputDir.getAbsolutePath());
		properties.put("installer.output.dir.bin", new File(outputDir, "bin").getAbsolutePath());
		properties.put("installer.output.dir.doc", new File(outputDir, "doc").getAbsolutePath());
		properties.put("installer.output.aspectjrt", new File(new File(outputDir, "lib"), "aspectjrt.jar").getAbsolutePath());
		properties.put("installer.output.readme", new File(outputDir, installer.getReadmeFilename()).getAbsolutePath());
	}

	public File getOutputDir() {
		return outputDir;
	}

	private boolean hasGui;
	public File javaPath;
	public File toolsJarPath;

	public Installer installer;

	private Map<String,String> properties;

	public boolean hasGui() {
		return hasGui;
	}

	public void setHasGui(boolean hasGui) {
		if (this.hasGui != hasGui) {
			this.hasGui = hasGui;
		}
	}

	public Font getFont() {
		return new Font("Serif", Font.PLAIN, 14);
	}

	public String getOS() {
		return System.getProperty("os.name");
	}

	public boolean onOS2() {
		return getOS().equals("OS2") || getOS().equals("OS/2");
	}

	public boolean onWindows() {
		return getOS().startsWith("Windows") || onOS2();
	}

	public boolean onWindowsPro() {
		// TODO: Think about a more future-proof solution also checking 'os.version' system property. See also this table:
		//       https://github.com/openjdk/jdk/blob/9604ee82690f89320614b37bfef4178abc869777/src/java.base/windows/native/libjava/java_props_md.c#L446
		//       Alternatively, explicitly exclude unsupported versions because those won't change in the future.
		return getOS().matches("^Windows (NT|2000|XP|Vista|Server|7|8|10).*");
	}

	public boolean onMacintosh() {
		return getOS().startsWith("Mac");
	}

	public boolean onUnix() {
		return !onWindows();
	}

	static final String[] TEXT_EXTENSIONS = { ".txt", ".text", ".htm", ".html", ".java", ".ajava", "README", ".lst" };

	public boolean isTextFile(File file) {
		String name = file.getName();

		for (String textExtension : TEXT_EXTENSIONS) {
			if (name.endsWith(textExtension)) {
				return true;
			}
		}

		return false;
	}

	public void handleException(Throwable e) {
		System.out.println("internal error: " + e.toString());
		e.printStackTrace();
		if (hasGui()) {
			JOptionPane.showMessageDialog(installer.frame, e.toString(), "Unexpected Exception", JOptionPane.ERROR_MESSAGE);
		}
	}

	final static String OVERWRITE_MESSAGE = "Overwrite file ";
	final static String OVERWRITE_TITLE = "Overwrite?";

	final static String[] OVERWRITE_OPTIONS = { "Yes", "No", "Yes to all" //, "No to all"
	};

	final static int OVERWRITE_YES = 0;
	final static int OVERWRITE_NO = 1;
	final static int OVERWRITE_ALL = 2;
	//final static int OVERWRITE_NONE = 3;

	int overwriteState = OVERWRITE_NO;

	boolean shouldOverwrite(final File file) {
		//System.out.println("overwrite: " + file + " state " + overwriteState);
		if (overwriteState == OVERWRITE_ALL) {
			return true;
			//if (overwriteState == OVERWRITE_NONE) return false;
		}

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					int ret = JOptionPane.showOptionDialog(installer.frame, OVERWRITE_MESSAGE + file.getPath(), OVERWRITE_TITLE,
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, OVERWRITE_OPTIONS,
							OVERWRITE_OPTIONS[OVERWRITE_YES]);

					overwriteState = ret;
				}
			});
		} catch (InvocationTargetException ite) {
			handleException(ite.getTargetException());
		} catch (InterruptedException ie) {
		}

		return overwriteState == OVERWRITE_YES || overwriteState == OVERWRITE_ALL;
	}

	public Map<String,String> getProperties() {
		return properties;
	}
}

abstract class WizardPane {
	static InstallContext context;

	protected JButton backButton = null;
	protected JButton nextButton = null;
	protected JButton cancelButton = null;

	public static void setContext(InstallContext con) {
		context = con;
	}

	public abstract JPanel makePanel();

	protected JTextArea makeTextArea(String data) {
		JTextArea text = new JTextArea(data);
		text.setOpaque(false);
		text.setFont(context.getFont());
		text.setEditable(false);
		return text;
	}

	/** @return false only if there is an InstallContext saying there is no GUI */
	protected boolean hasGui() {
		final InstallContext icontext = context;
		return ((null == icontext) || icontext.hasGui());
	}

	public static String stringFromStream(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "US-ASCII"));

		StringBuffer ret = new StringBuffer();
		int data;
		while ((data = reader.read()) != -1) {
			ret.append((char) data);
		}
		return ret.toString();
	}

	public static String removeHead(String text) {
		int startIndex = text.indexOf("<head>");
		int stopIndex = text.indexOf("</head>");
		if (startIndex == -1 || stopIndex == -1) {
			return text;
		}
		stopIndex += 7;
		return text.substring(0, startIndex) + text.substring(stopIndex);
	}

	static String styleHeader = "<head></head>";/*
												 * <STYLE TYPE=\"text/css\"><!--\n" + " h2 {\n" + "  	font-size: x-large;\n" +
												 * "   font-family: Serif;\n" + "   font-weight: normal;\n" + " }\n" + " p {\n" +
												 * "   font-family: Serif;\n" + "   font-weight: normal;\n" + //"   color:black;\n"
												 * + "}</head>\n";
												 */

	public static String applyProperties(String text, Map<String,String> map) {
		// ${name} -> map.get(name).toString()
		int lastIndex = 0;
		StringBuffer buf = new StringBuffer();

		int startIndex;
		while ((startIndex = text.indexOf("${", lastIndex)) != -1) {
			int endIndex = text.indexOf('}', startIndex);
			//XXX internal error here
			if (endIndex == -1) {
				break;
			}
			buf.append(text.substring(lastIndex, startIndex));
			String key = text.substring(startIndex + 2, endIndex);
			lastIndex = endIndex + 1;
			Object replaceText = (map == null ? null : map.get(key));
			//System.out.println("key: " + key + " -> " + replaceText);
			if (replaceText == null) {
				replaceText = "NOT_FOUND";
			}
			buf.append(replaceText.toString());
		}
		buf.append(text.substring(lastIndex));

		return buf.toString();
	}

	public static String applyProperties(String text) {
		return applyProperties(text, (context == null ? null : context.getProperties()));
	}

	protected String loadText(String filename) {
		String fullname = Installer.RESOURCE_DIR + "/" + filename;
		//context.installer.getPrefix() + "-" + filename;

		try {
			String text = stringFromStream(getClass().getResourceAsStream(fullname));
			text = styleHeader + removeHead(text);
			text = applyProperties(text);
			//System.out.println(text);
			return text;
		} catch (IOException e) {
			context.handleException(e);
			return "";
		}
	}

	protected JEditorPane makeHTMLArea(String filename) {
		JEditorPane editorPane = new JEditorPane("text/html", loadText(filename));
		/*
		 * { public void paint(Graphics g) { Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		 * RenderingHints.VALUE_ANTIALIAS_ON); super.paint(g2); } };
		 */
		editorPane.setEditable(false);
		editorPane.setOpaque(false);
		return editorPane;
	}

	protected void setHTMLArea(JEditorPane pane, String filename) {
		pane.setText(loadText(filename));
	}

	protected JPanel makeLocationBox(String label, JTextField textField, JButton browseButton) {
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));

		textField.setFont(context.getFont());
		textField.selectAll();
		box.add(textField);

		box.add(browseButton);
		Border border = BorderFactory.createTitledBorder(label);
		final int INSET = 8;
		border = new CompoundBorder(border, new EmptyBorder(1, INSET, INSET, INSET));
		box.setBorder(border);

		return box;
	}

	private JPanel panel = null;

	public JPanel getPanel() {
		if (panel == null) {
			panel = makePanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		}
		return panel;
	}

	protected void setListener(JButton button, ActionListener listener) {
		if (listener == null) {
			button.setEnabled(false);
		} else {
			button.addActionListener(listener);
		}
	}

	protected Component makeButtons(Installer installer) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		backButton = new JButton("Back");
		setListener(backButton, installer.makeBackAction(this));
		panel.add(backButton);

		nextButton = new JButton("Next");
		setListener(nextButton, installer.makeNextAction(this));
		panel.add(nextButton); //.setDefaultCapable(true);

		JLabel space = new JLabel();
		space.setPreferredSize(new Dimension(20, 0));
		panel.add(space);

		cancelButton = new JButton("Cancel");
		setListener(cancelButton, installer.makeCancelAction(this));
		panel.add(cancelButton);

		return panel;
	}

	private Component buttons = null;

	public Component getButtons() {
		if (buttons == null) {
			buttons = makeButtons(context.installer);
			//buttons.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		}
		context.installer.frame.getRootPane().setDefaultButton(nextButton);
		return buttons;
	}

	public void finish() {
		if (Options.forceError2) {
			throw new RuntimeException("forced error2 for testing purposes");
		}
	}

	public void run() {
	}
}

class IntroPane extends WizardPane {
	public JPanel makePanel() {
		Component text = makeHTMLArea("intro.html");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(text);
		return panel;
	}
}

class FinishPane extends WizardPane {
	public JPanel makePanel() {
		Component text = makeHTMLArea("finish.html");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(text);
		finalActions();
		return panel;
	}

	public Component makeButtons(Installer installer) {
		Component ret = super.makeButtons(installer);
		nextButton.setText("Finish");
		nextButton.setEnabled(true);
		nextButton.addActionListener(installer.makeFinishAction(this));
		backButton.setEnabled(false);
		cancelButton.setEnabled(false);
		return ret;
	}

	public void finalActions() {
	}
}

class LocationPane extends WizardPane implements ActionListener {
	//XXX need more sophisticated default location finding
	//XXX would like to find the place they last chose...
	public String getDefaultLocation() {
		if (context.onWindows()) {
			return "c:\\aspectj1.9";
		} else {
			return new File(System.getProperty("user.home"), "aspectj1.9").getAbsolutePath();
		}
	}

	protected JTextField location;

	public JPanel makePanel() {
		Component text = makeHTMLArea("location.html");

		location = new JTextField(getDefaultLocation());
		JButton browse = new JButton("Browse...");
		browse.addActionListener(this);

		JPanel locationBox = makeLocationBox("installation directory", location, browse);

		GridBagLayout bag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(bag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		bag.setConstraints(text, c);
		panel.add(text);

		c.weighty = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		bag.setConstraints(locationBox, c);
		panel.add(locationBox);

		//XXX set next button to read install
		//context.nextButton.setText("Install");

		return panel;
	}

	public Component makeButtons(Installer installer) {
		Component ret = super.makeButtons(installer);
		nextButton.setText("Install");
		return ret;
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser(); // {
		//    public void approveSelection() {
		//        System.out.println("approved selection");
		//    }
		//}; //field.getText());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = chooser.showDialog(location, "Select");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			String name = file.getPath();
			location.setText(name);
			location.selectAll();
		}
	}

	/**
	 * Override to do any additional checks.
	 */
	protected void verify() {
	}

	public void finish() {
		verify();
		context.setOutputDir(new File(location.getText()));
	}
}

class ConfigureLauncherPane extends WizardPane {
	/*
	 * //XXX check that the returned file is valid public String getDefaultJavaLocation() { String name = "java"; if
	 * (context.onWindows()) name += ".exe";
	 *
	 * if (Options.verbose) { System.out.println("java.home: " + System.getProperty("java.home")); System.out.println("  java: " +
	 * new File(new File(System.getProperty("java.home"), "bin"), name)); System.out.println("  java: " + new File(new
	 * File(System.getProperty("java.home"), "bin"), name).getPath()); }
	 *
	 * return new File(new File(System.getProperty("java.home"), "bin"), name).getPath(); }
	 */

	public static String getDefaultJavaHomeLocation() {
		if (!Options.forceHandConfigure) {
			File javaHome = findJavaHome();
			if (javaHome != null) {
				return javaHome.getPath();
			}
		}
		return null;
	}

	public void chooseFile(JTextField field) {
		JFileChooser chooser = new JFileChooser(); //field.getText());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(field, "Select");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			field.setText(name);
			field.selectAll();
		}
	}

	public ActionListener makeJavaLocationBrowseListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFile(javaLocation);
			}
		};
	}

	//     public ActionListener makeToolsJarLocationBrowseListener() {
	//         return new ActionListener() {
	//             public void actionPerformed(ActionEvent e) {
	//                 chooseFile(toolsJarLocation);
	//             }
	//         };
	//     }

	private JTextField javaLocation;

	//private JTextField toolsJarLocation;

	public JPanel makePanel() {
		String javaPath = getDefaultJavaHomeLocation();
		//String toolsJarPath = getDefaultToolsJarLocation();

		Component text;
		if (javaPath == null) {
			javaPath = "<java home not found>";
			text = makeHTMLArea("configure-hand.html");
		} else {
			text = makeHTMLArea("configure-auto.html");
		}

		javaLocation = new JTextField(javaPath);
		JButton javaLocationBrowse = new JButton("Browse...");
		javaLocationBrowse.addActionListener(makeJavaLocationBrowseListener());

		JPanel javaLocationBox = makeLocationBox("java home directory", javaLocation, javaLocationBrowse);

		//         toolsJarLocation = new JTextField(toolsJarPath);
		//         JButton toolsJarLocationBrowse = new JButton("Browse...");
		//         toolsJarLocationBrowse.addActionListener(makeToolsJarLocationBrowseListener());

		//         JPanel toolsJarLocationBox = makeLocationBox("full path to tools.jar", toolsJarLocation, toolsJarLocationBrowse);

		GridBagLayout bag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(bag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		//c.ipady = 10;
		c.gridwidth = GridBagConstraints.REMAINDER;
		bag.setConstraints(text, c);
		panel.add(text);

		c.weighty = 0.0;
		//c.fill = GridBagConstraints.VERTICAL;
		bag.setConstraints(javaLocationBox, c);
		panel.add(javaLocationBox);

		//         c.weighty = 0.25;
		//         JLabel space = new JLabel();
		//         bag.setConstraints(space, c);
		//         panel.add(space);

		//         c.weighty = 0.0;
		//         bag.setConstraints(toolsJarLocationBox, c);
		//         panel.add(toolsJarLocationBox);

		c.weighty = 0.5;
		JLabel space = new JLabel();
		bag.setConstraints(space, c);
		panel.add(space);

		return panel;
	}

	public void finish() {
		context.javaPath = new File(javaLocation.getText());
		//         context.toolsJarPath = new File(toolsJarLocation.getText());

		//XXX need much more work on helping the user get these paths right
		//         if (context.javaPath.isDirectory()) {
		//             context.javaPath = new File(context.javaPath, "java");
		//         }
		//         if (context.toolsJarPath.isDirectory()) {
		//             context.toolsJarPath = new File(context.toolsJarPath, "tools.jar");
		//         }
	}

	//XXX add user.home to prefixes in a rational way
	public static final String[] windowsPaths = { "c:\\jdk", "c:\\apps\\jdk", "${user.home}\\jdk" };

	public static final String[] unixPaths = { "/usr/local/bin/jdk", "/usr/bin/jdk", "/usr/bin/jdk", "${user.home}/jdk" };

	public static final String[] suffixes = { "1.3.1", "1.3", "1.2", "13", "12", "2", "", "1.4" };

	public static boolean windows = true;

	public static boolean isLegalJavaHome(File home) {
		File bin = new File(home, "bin");
		return new File(bin, "java").isFile() || new File(bin, "java.exe").isFile();
	}

	public static boolean isLegalJDKHome(File home) {
		File lib = new File(home, "lib");
		return new File(lib, "tools.jar").isFile();
	}

	public static File findJavaHome() {
		String s = System.getProperty("java.home");
		File javaHome = null;
		if (s != null) {
			javaHome = new File(s);
			if (isLegalJDKHome(javaHome)) {
				return javaHome;
			}
			if (isLegalJavaHome(javaHome)) {
				File parent = javaHome.getParentFile();
				if (parent != null && isLegalJDKHome(parent)) {
					return parent;
				}
			}
		}

		String[] paths;
		if (windows) {
			paths = windowsPaths;
		} else {
			paths = unixPaths;
		}

		for (String suffix : suffixes) {
			for (String path : paths) {
				String prefix = path;
				prefix = applyProperties(prefix);
				File test = new File(prefix + suffix);
				if (isLegalJavaHome(test)) {
					if (isLegalJDKHome(test)) {
						return test;
					} else if (javaHome == null) {
						javaHome = test;
					}
				}
			}
		}
		return javaHome;
	}
}

class InstallPane extends WizardPane {
	private JProgressBar progressBar;
	private JTextField progressItem;
	private JEditorPane message;

	private boolean makeLaunchScripts = false;

	public InstallPane(boolean makeLaunchScripts) {
		this.makeLaunchScripts = makeLaunchScripts;
	}

	public JPanel makePanel() {
		message = makeHTMLArea("install-start.html");

		progressBar = new JProgressBar();

		progressItem = new JTextField();
		progressItem.setOpaque(false);
		progressItem.setFont(context.getFont());
		progressItem.setEditable(false);

		GridBagLayout bag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel panel = new JPanel(bag);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		//c.ipady = 10;
		c.gridwidth = GridBagConstraints.REMAINDER;
		bag.setConstraints(message, c);
		panel.add(message);

		c.weighty = 0.0;
		//c.fill = GridBagConstraints.VERTICAL;
		bag.setConstraints(progressBar, c);
		panel.add(progressBar);

		c.weighty = 0.1;
		JLabel space = new JLabel();
		bag.setConstraints(space, c);
		panel.add(space);

		c.weighty = 0.0;
		bag.setConstraints(progressItem, c);
		panel.add(progressItem);

		c.weighty = 0.5;
		space = new JLabel();
		bag.setConstraints(space, c);
		panel.add(space);

		return panel;
	}

	class InstallRunner implements Runnable {
		public InstallRunner() {
		}

		public void run() {
			try {
				new CurrentJarUnpacker(context, InstallPane.this).unpack(Installer.RESOURCE_DIR + "/contents.txt",
						context.getOutputDir());

				if (makeLaunchScripts) {
					LaunchScriptMaker lsm = new LaunchScriptMaker(context);
					lsm.writeScript("ajc");
					lsm.writeScript("ajdoc");
					//lsm.writeScript("ajdb");
					lsm.writeScript("ajbrowser");

					// Moved to the bin dir in 1.2.1
					// we should now come back and make the generation of this
					// script uniform with those above.
					lsm.writeAJLaunchScript("aj", false);
					lsm.writeAJLaunchScript("aj5", true);
				}
				if (hasGui()) {
					progressBar.setValue(100);
					setHTMLArea(message, "install-finish.html");
				}
			} catch (IOException ioe) {
				context.handleException(ioe);
			}

			if (hasGui()) {
				cancelButton.setEnabled(false);
				nextButton.setEnabled(true);
			}
		}
	}

	public Component makeButtons(Installer installer) {
		Component ret = super.makeButtons(installer);
		//nextButton.setText("Finish");
		nextButton.setEnabled(false);
		//nextButton.addActionListener(installer.makeFinishAction(this));
		backButton.setEnabled(false);
		return ret;
	}

	public void run() {
		Thread thread = new Thread(new InstallRunner());
		thread.start();
	}

	public void progressMessage(final String message) {
		if (!hasGui()) {
			return;
		}
		try {
			//XXX performance tradeoff between invokeAndWait and invokeLater...
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					progressItem.setText(message);
				}
			});
		} catch (InvocationTargetException ite) {
			context.handleException(ite.getTargetException());
		} catch (InterruptedException ie) {
		}
	}

	int nBytes = 0;
	int bytesWritten = 0;

	public void progressBytesWritten(int bytes) {
		if (!hasGui()) {
			return;
		}
		bytesWritten += bytes;
		final int PCT = (int) (100.0 * bytesWritten / nBytes);
		//System.out.println("bytesWritten: " + bytesWritten);
		try {
			//XXX performance tradeoff between invokeAndWait and invokeLater...
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					progressBar.setValue(PCT);
				}
			});
		} catch (InvocationTargetException ite) {
			context.handleException(ite.getTargetException());
		} catch (InterruptedException ie) {
		}
	}
}

class CurrentJarUnpacker {
	InstallContext context;
	InstallPane installPane;

	public CurrentJarUnpacker(InstallContext context, InstallPane installPane) {
		this.context = context;
		this.installPane = installPane;
	}

	public File makeOutputFile(String name, File outputFile) {
		int index;
		int lastIndex = 0;

		while ((index = name.indexOf('/', lastIndex)) != -1) {
			outputFile = new File(outputFile, name.substring(lastIndex, index));
			lastIndex = index + 1;
		}

		return new File(outputFile, name.substring(lastIndex));
	}

	final static int BUF_SIZE = 4096;

	public void writeStream(InputStream zis, File outputFile) throws IOException {
		if (outputFile.exists()) {
			if (!context.shouldOverwrite(outputFile)) {
				return;
			}
		}

		installPane.progressMessage("writing " + outputFile.getAbsolutePath());

		outputFile.getParentFile().mkdirs();

		if (context.isTextFile(outputFile)) {
			writeTextStream(zis, outputFile);
		} else {
			writeBinaryStream(zis, outputFile);
		}
	}

	public void writeBinaryStream(InputStream zis, File outputFile) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		int nRead = 0;

		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));

		while ((nRead = zis.read(buffer)) != -1) {
			os.write(buffer, 0, nRead);
			installPane.progressBytesWritten(nRead);
		}
		os.close();
	}

	public void writeTextStream(InputStream zis, File outputFile) throws IOException {
		BufferedWriter os = new BufferedWriter(new FileWriter(outputFile));
		BufferedReader r = new BufferedReader(new InputStreamReader(zis, "US-ASCII"));

		String l;
		while ((l = r.readLine()) != null) {
			os.write(l);
			os.newLine();
			installPane.progressBytesWritten(l.length() + 1);
		}
		os.close();
	}

	public void writeResource(String name, File outputDir) throws IOException {
		File outputFile = makeOutputFile(name, outputDir);
		//System.out.println("finding name: " + name);
		writeStream(getClass().getResourceAsStream("/" + name), outputFile);
	}

	public void writeResource(JarFile jarFile, JarEntry entry, File outputDir) throws IOException {
		String name = entry.getName().substring(6);
		File outputFile = makeOutputFile(name, outputDir);
		//System.out.println("finding name: " + name);
//		writeStream(getClass().getResourceAsStream("/" + name), outputFile);
		writeStream(jarFile.getInputStream(entry), outputFile);
	}

	public void unpack(String contentsName, File outputDir) throws IOException {
		URL url = getClass().getResource(contentsName);

		// Process everything under 'files/**' copying to the target
		// install directory with 'files/' removed
		JarURLConnection juc = (JarURLConnection) url.openConnection();
		JarFile jf = juc.getJarFile();
		Enumeration<JarEntry> entries = jf.entries();
		while (entries.hasMoreElements()) {
			JarEntry je = entries.nextElement();
			if (je.getName().startsWith("files/") && !je.getName().endsWith("/")) {
				writeResource(jf, je, outputDir);
			}
		}

//		InputStream stream = url.openStream();
//		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "US-ASCII"));
//		String line = reader.readLine();
//		installPane.nBytes = Integer.parseInt(line);
//
//		while ((line = reader.readLine()) != null) {
//			writeResource(line, outputDir);
//		}

		installPane.progressMessage("done writing");
	}
}

class LaunchScriptMaker {
	static final String toolsPackage = "org.aspectj.tools";

	InstallContext context;

	public LaunchScriptMaker(InstallContext context) {
		this.context = context;
	}

	/**
	 *
	 */
	public void writeAJLaunchScript(String name, boolean isJava5) throws IOException {
		if (!context.onUnix()) {
			if (context.onOS2()) {
				name += ".cmd";
			} else if (context.onWindows()) {
				name += ".bat";
			}
		}

		File destDir = new File(context.getOutputDir(), "bin");
		destDir.mkdirs();
		File file = new File(destDir, name);

		PrintStream ps = getPrintStream(file);
		writeAJLaunchScriptContent(ps, isJava5);
		ps.close();

		if (context.onUnix()) {
			makeExecutable(file);
		}
	}

	/**
	 * @param ps
	 */
	private void writeAJLaunchScriptContent(PrintStream ps, boolean isJava5) {
		if (context.onUnix()) {
			writeUnixHeader(ps);
			if (isJava5) {
				writeAJ5UnixLaunchLine(ps);
			} else {
				writeAJUnixLaunchLine(ps);
			}
		} else {
			writeWindowsHeader(ps);
			if (isJava5) {
				writeAJ5WindowsLaunchLine(ps);
			} else {
				writeAJWindowsLaunchLine(ps);
			}
		}
	}

	/**
	 * @param ps
	 */
	private void writeAJWindowsLaunchLine(PrintStream ps) {
		ps.println("\"%JAVA_HOME%\\bin\\java\" -classpath " + "\"%ASPECTJ_HOME%\\lib\\aspectjweaver.jar\""
				+ " \"-Djava.system.class.loader=org.aspectj.weaver.loadtime.WeavingURLClassLoader\""
				+ " \"-Daj.class.path=%ASPECTPATH%;%CLASSPATH%\"" + " \"-Daj.aspect.path=%ASPECTPATH%\"" + " "
				+ makeScriptArgs(false));
	}

	/**
	 * @param ps
	 */
	private void writeAJ5WindowsLaunchLine(PrintStream ps) {
		ps.println("\"%JAVA_HOME%\\bin\\java\" -classpath " + "\"%ASPECTJ_HOME%\\lib\\aspectjweaver.jar;%CLASSPATH%\""
				+ " \"-javaagent:%ASPECTJ_HOME%\\lib\\aspectjweaver.jar\"" + " " + makeScriptArgs(false));
	}

	/**
	 * @param ps
	 */
	private void writeAJUnixLaunchLine(PrintStream ps) {
		ps.println("\"$JAVA_HOME/bin/java\" -classpath" + " \"$ASPECTJ_HOME/lib/aspectjweaver.jar\""
				+ " \"-Djava.system.class.loader=org.aspectj.weaver.loadtime.WeavingURLClassLoader\""
				+ " \"-Daj.class.path=$ASPECTPATH:$CLASSPATH\"" + " \"-Daj.aspect.path=$ASPECTPATH\"" + " " + makeScriptArgs(true));
	}

	/**
	 * @param ps
	 */
	private void writeAJ5UnixLaunchLine(PrintStream ps) {
		ps.println("\"$JAVA_HOME/bin/java\" -classpath" + " \"$ASPECTJ_HOME/lib/aspectjweaver.jar:$CLASSPATH\""
				+ " \"-javaagent:$ASPECTJ_HOME/lib/aspectjweaver.jar\"" + " " + makeScriptArgs(true));
	}

	private void writeWindowsHeader(PrintStream ps) {
		ps.println("@echo off");
		ps.println("REM This file generated by AspectJ installer");
		ps.println("REM Created on " + new java.util.Date() + " by " + System.getProperty("user.name"));
		ps.println("");
		ps.println("if \"%JAVA_HOME%\" == \"\" set JAVA_HOME=" + context.javaPath.getAbsolutePath());
		ps.println("if \"%ASPECTJ_HOME%\" == \"\" set ASPECTJ_HOME=" + context.getOutputDir().getAbsolutePath());
		ps.println("");

		ps.println("if exist \"%JAVA_HOME%\\bin\\java.exe\" goto haveJava");
		ps.println("if exist \"%JAVA_HOME%\\bin\\java.bat\" goto haveJava");
		ps.println("if exist \"%JAVA_HOME%\\bin\\java\" goto haveJava");

		ps.println("echo java does not exist as %JAVA_HOME%\\bin\\java");
		ps.println("echo please fix the JAVA_HOME environment variable");
		ps.println(":haveJava");
	}

	private void writeWindowsLaunchLine(String className, PrintStream ps) {
		ps.println("\"%JAVA_HOME%\\bin\\java\" -classpath " +
		//                   "\"%ASPECTJ_HOME%\\lib\\aspectjtools.jar;%CLASSPATH%\""+
				"\"%ASPECTJ_HOME%\\lib\\aspectjtools.jar;%JAVA_HOME%\\lib\\tools.jar;%CLASSPATH%\"" + " -Xmx64M " + className + //" -defaultClasspath " + "\"%CLASSPATH%\"" +
				" " + makeScriptArgs(false));
	}

	private void writeUnixHeader(PrintStream ps) {
		File binsh = new File(File.separator + "bin", "sh");
		if (binsh.canRead()) {
			ps.println("#!" + binsh.getPath());
		}
		ps.println("# This file generated by AspectJ installer");
		ps.println("# Created on " + new java.util.Date() + " by " + System.getProperty("user.name"));
		ps.println("");
		ps.println("if [ \"$JAVA_HOME\" = \"\" ] ; then JAVA_HOME=" + quote(true, false, context.javaPath.getAbsolutePath()));
		ps.println("fi");
		ps.println("if [ \"$ASPECTJ_HOME\" = \"\" ] ; then ASPECTJ_HOME=" + quote(true, false, context.getOutputDir()));
		ps.println("fi");
		ps.println("");
	}

	private void writeUnixLaunchLine(String className, PrintStream ps) {
		String sep = File.pathSeparator;
		ps.println("\"$JAVA_HOME/bin/java\" -classpath " + "\"$ASPECTJ_HOME/lib/aspectjtools.jar" + sep
				+ "$JAVA_HOME/lib/tools.jar" + sep + "$CLASSPATH\"" + " -Xmx64M " + className + " " + makeScriptArgs(true));
	}

	private void makeExecutable(File file) {
		try {
			Runtime curRuntime = Runtime.getRuntime();
			curRuntime.exec("chmod 777 " + quote(true, false, file));
		} catch (Throwable t) {
			// ignore any errors that occur while trying to chmod
		}
	}

	private String makeScriptArgs(boolean unixStyle) {
		if (unixStyle) {
			return "\"$@\"";
		} else if (context.onWindowsPro()) {
			return "%*";
		} else {
			return "%1 %2 %3 %4 %5 %6 %7 %8 %9";
		}
	}

	private String quote(boolean unixStyle, boolean forceQuotes, File file) {
		return quote(unixStyle, forceQuotes, file.getAbsolutePath());
	}

	private String quote(boolean unixStyle, boolean forceQuotes, String s) {
		if (context.onWindows() && unixStyle) {
			s = s.replace('\\', '/');
		}

		if (!forceQuotes && s.indexOf(' ') == -1) {
			return s;
		}
		return "\"" + s + "\"";
	}

	private File makeScriptFile(String name, boolean unixStyle) throws IOException {
		if (!unixStyle) {
			if (context.onOS2()) {
				name += ".cmd";
			} else if (context.onWindows()) {
				name += ".bat";
			}
		}

		//XXX probably want a context.getOutputBinDir()
		File bindir = new File(context.getOutputDir(), "bin");
		bindir.mkdirs();
		File file = new File(bindir, name);
		return file;
	}

	private PrintStream getPrintStream(File file) throws IOException {
		return new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
	}

	String makeClassPathVar(boolean unixStyle) {
		if (unixStyle) {
			return "$CLASSPATH";
		} else {
			return "%CLASSPATH%";
		}
	}

	public String makeClassPath(boolean unixStyle) throws IOException {
		return context.toolsJarPath.getAbsolutePath() + File.pathSeparator
				+
				//XXX want context.getOutputLibDir()
				new File(new File(context.getOutputDir(), "lib"), "aspectjtools.jar").getAbsolutePath() + File.pathSeparator
				+ makeClassPathVar(unixStyle);
	}

	public void writeScript(String className, PrintStream ps, boolean unixStyle) throws IOException {
		if (unixStyle) {
			writeUnixHeader(ps);
			writeUnixLaunchLine(className, ps);
		} else {
			writeWindowsHeader(ps);
			writeWindowsLaunchLine(className, ps);
		}

		/*
		 * ps.print(quote(unixStyle, false, context.javaPath.getAbsolutePath())); ps.print(" "); ps.print("-classpath ");
		 * ps.print(quote(unixStyle, true, makeClassPath(unixStyle))); ps.print(" "); ps.print("-Xmx64M "); ps.print(className);
		 * ps.print(" "); ps.print(makeScriptArgs(unixStyle));
		 */
	}

	public void writeScript(String className, boolean unixStyle) throws IOException {
		File file = makeScriptFile(className, unixStyle);
		if (!checkExistingFile(file)) {
			return;
		}
		PrintStream ps = getPrintStream(file);
		writeScript(toolsPackage + '.' + className + ".Main", ps, unixStyle);
		ps.close();
		//??? unixStyle vs. onUnix()
		if (context.onUnix()) {
			makeExecutable(file);
		}
	}

	public boolean checkExistingFile(File file) {
		if (!file.exists()) {
			return true;
		}

		return context.shouldOverwrite(file);
	}

	/*
	 * final static String OVERWRITE_MESSAGE = "Overwrite launch script "; final static String OVERWRITE_TITLE = "Overwrite?";
	 *
	 * final static String[] OVERWRITE_OPTIONS = { "Yes", "No", "Yes to all", "No to all" };
	 *
	 * final static int OVERWRITE_YES = 0; final static int OVERWRITE_NO = 1; final static int OVERWRITE_ALL = 2; final static int
	 * OVERWRITE_NONE = 3;
	 *
	 * int overwriteState = OVERWRITE_NO; boolean shouldOverwrite(final File file) { if (overwriteState == OVERWRITE_ALL) return
	 * true; if (overwriteState == OVERWRITE_NONE) return false;
	 *
	 * try { SwingUtilities.invokeAndWait(new Runnable() { public void run() { int ret =
	 * JOptionPane.showOptionDialog(context.installer.frame, OVERWRITE_MESSAGE+file.getPath(), OVERWRITE_TITLE,
	 * JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, OVERWRITE_OPTIONS, OVERWRITE_OPTIONS[OVERWRITE_YES]);
	 *
	 * overwriteState = ret; } }); } catch (InvocationTargetException ite) { context.handleException(ite.getTargetException()); }
	 * catch (InterruptedException ie) { }
	 *
	 * return overwriteState == OVERWRITE_YES || overwriteState == OVERWRITE_ALL; }
	 */

	public void writeScript(String className) throws IOException {
		writeScript(className, true);
		if (context.onWindows()) {
			writeScript(className, false);
		}
	}
}

class JarUnpacker {
	InstallContext context;
	InstallPane installPane;

	public JarUnpacker(InstallContext context, InstallPane installPane) {
		this.context = context;
		this.installPane = installPane;
	}

	public File makeOutputFile(String name, File outputFile) {
		int index;
		int lastIndex = 0;

		while ((index = name.indexOf('/', lastIndex)) != -1) {
			outputFile = new File(outputFile, name.substring(lastIndex, index));
			lastIndex = index + 1;
		}

		return new File(outputFile, name.substring(lastIndex));
	}

	final static int BUF_SIZE = 4096;

	public void writeStream(ZipInputStream zis, File outputFile) throws IOException {
		if (outputFile.exists()) {
			if (!context.shouldOverwrite(outputFile)) {
				return;
			}
		}

		installPane.progressMessage("writing " + outputFile.getAbsolutePath());

		outputFile.getParentFile().mkdirs();

		if (context.isTextFile(outputFile)) {
			writeTextStream(zis, outputFile);
		} else {
			writeBinaryStream(zis, outputFile);
		}
	}

	public void writeBinaryStream(ZipInputStream zis, File outputFile) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		int nRead = 0;

		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));

		while ((nRead = zis.read(buffer)) != -1) {
			os.write(buffer, 0, nRead);
			installPane.progressBytesWritten(nRead);
		}
		os.close();
	}

	public void writeTextStream(ZipInputStream zis, File outputFile) throws IOException {
		BufferedWriter os = new BufferedWriter(new FileWriter(outputFile));
		BufferedReader r = new BufferedReader(new InputStreamReader(zis, "US-ASCII"));

		String l;
		while ((l = r.readLine()) != null) {
			os.write(l);
			os.newLine();
			installPane.progressBytesWritten(l.length() + 1);
		}
		os.close();
	}

	public void writeEntry(ZipInputStream zis, ZipEntry entry, File outputDir) throws IOException {
		if (entry.isDirectory()) {
			return;
		}

		String name = entry.getName();
		File outputFile = makeOutputFile(name, outputDir);
		writeStream(zis, outputFile);
	}

	public void unpack(String jarName, File outputDir) throws IOException {
		URL url = getClass().getResource(jarName);
		InputStream stream = url.openStream();
		ZipInputStream zis = new ZipInputStream(stream);
		//        int i = 0;

		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			//            final String name = entry.getName();
			writeEntry(zis, entry, outputDir);
			//
		}
		installPane.progressMessage("done writing");
	}
}
