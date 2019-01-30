/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
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

package org.aspectj.tools.ajbrowser.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.swing.AJButtonMenuCombo;
import org.aspectj.ajde.ui.swing.BuildConfigPopupMenu;
import org.aspectj.ajde.ui.swing.MultiStructureViewPanel;
import org.aspectj.asm.IProgramElement;
import org.aspectj.tools.ajbrowser.BrowserManager;
import org.aspectj.tools.ajbrowser.ui.BrowserMessageHandler;
import org.aspectj.tools.ajbrowser.ui.EditorManager;

/**
 * UI for standalone operation.
 * 
 * @author Mik Kersten
 */
public class TopFrame extends JFrame {

	private static final long serialVersionUID = 1007473581156451702L;

	private static final File CURRENT_DIR = new File(".");

	public JLabel statusText_label = new JLabel();

	private JPanel editor_panel = null;
	private JPanel sourceEditor_panel = null;

	private JMenuBar menuBar = new JMenuBar();
	private JMenu jMenu1 = new JMenu();
	private JMenu jMenu2 = new JMenu();
	private JMenuItem projectBuild_menuItem = new JMenuItem();
	private FlowLayout left_flowLayout = new FlowLayout();
	private JMenuItem jMenuItem1 = new JMenuItem();
	private JMenuItem exit_menuItem = new JMenuItem();
	private JSplitPane top_splitPane = new JSplitPane();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JMenuItem projectRun_menuItem = new JMenuItem();
	private JMenuItem projectRunOther_menuItem = new JMenuItem();
	private JPanel status_panel = new JPanel();
	private BorderLayout borderLayout4 = new BorderLayout();
	private Border emptyBorder = BorderFactory.createEmptyBorder();
	private JPanel toolbar_panel = new JPanel();
	private JSplitPane right_splitPane = new JSplitPane();
	private MessageHandlerPanel messages_panel = null;
	private JMenu tools_menu = new JMenu();
	private JMenuItem joinpointProbe_menuItem = new JMenuItem();
	private JMenuItem projectDebug_menuItem = new JMenuItem();
	private JMenuItem svProperties_menuItem = new JMenuItem();
	private File lastChosenDir = CURRENT_DIR;

	JPanel toolBar_panel = new JPanel();
	JToolBar build_toolBar = new JToolBar();
	JButton closeConfig_button = new JButton();
	JButton openConfig_button = new JButton();
	JButton run_button = new JButton();
	JToolBar project_toolBar = new JToolBar();
	JButton save_button = new JButton();
	JButton options_button = new JButton();
	JButton editConfig_button = new JButton();
	JToolBar file_toolBar = new JToolBar();
	JPanel filler_panel = new JPanel();
	BorderLayout borderLayout5 = new BorderLayout();
	BorderLayout borderLayout6 = new BorderLayout();
	Border border8;
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JPanel multiView_panel;

	private AJButtonMenuCombo buildCombo;

	public void init(MultiStructureViewPanel multiViewPanel,
			MessageHandlerPanel compilerMessagesPanel, JPanel editorPanel) {
		try {
			this.multiView_panel = multiViewPanel;
			this.messages_panel = compilerMessagesPanel;
			this.editor_panel = editorPanel;
			this.sourceEditor_panel = editorPanel;

			jbInit();
			svProperties_menuItem.setIcon(Ajde.getDefault()
					.getIconRegistry().getBrowserOptionsIcon());
			projectBuild_menuItem.setIcon(Ajde.getDefault()
					.getIconRegistry().getBuildIcon());
			projectRun_menuItem.setIcon(Ajde.getDefault()
					.getIconRegistry().getExecuteIcon());
			projectRunOther_menuItem.setIcon(Ajde.getDefault()
					.getIconRegistry().getExecuteIcon());
			projectDebug_menuItem.setIcon(Ajde.getDefault()
					.getIconRegistry().getDebugIcon());

			this.setJMenuBar(menuBar);
			this.setIconImage(((ImageIcon) Ajde.getDefault()
					.getIconRegistry().getStructureSwingIcon(
							IProgramElement.Kind.ADVICE)).getImage());
			this.setLocation(75, 10);
			this.setSize(900, 650);
			this.setTitle(BrowserManager.TITLE);
			// bindKeys();
			fixButtonBorders();
			messages_panel.setVisible(false);

			JPopupMenu orderMenu = new BuildConfigPopupMenu(
					new AbstractAction() {

						private static final long serialVersionUID = 1L;

						public void actionPerformed(ActionEvent arg0) {
							BrowserManager.getDefault().saveAll();
						}
					});

			buildCombo = new AJButtonMenuCombo("Build", "Build", Ajde
					.getDefault().getIconRegistry().getBuildIcon(), orderMenu,
					false);

			build_toolBar.add(buildCombo, 1);
			refreshBuildMenu();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refreshBuildMenu() {
		JPopupMenu orderMenu = new BuildConfigPopupMenu(new AbstractAction() {
			private static final long serialVersionUID = -3204840278758386318L;

			public void actionPerformed(ActionEvent arg0) {
				BrowserManager.getDefault().saveAll();
			}
		});

		buildCombo.setMenu(orderMenu);
	}

	public void setEditorPanel(JPanel panel) {
		editor_panel = panel;
		right_splitPane.remove(editor_panel);
		right_splitPane.add(panel, JSplitPane.TOP);
		panel.setVisible(true);
	}

	/**
	 * @todo get rid of this method and make jbinit() work properly
	 */
	private void fixButtonBorders() {
		run_button.setBorder(null);
		options_button.setBorder(null);
		openConfig_button.setBorder(null);
		closeConfig_button.setBorder(null);
		save_button.setBorder(null);
		editConfig_button.setBorder(null);
	}

	private void jbInit() throws Exception {
		border8 = BorderFactory.createCompoundBorder(BorderFactory
				.createEtchedBorder(Color.white, new Color(156, 156, 158)),
				BorderFactory.createEmptyBorder(2, 2, 2, 2));
		emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		jMenu1.setFont(new java.awt.Font("Dialog", 0, 11));
		jMenu1.setText("File");
		jMenu1.setMnemonic(KeyEvent.VK_F);
		jMenu2.setFont(new java.awt.Font("Dialog", 0, 11));
		jMenu2.setText("Project");
		jMenu2.setMnemonic(KeyEvent.VK_P);
		projectBuild_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		projectBuild_menuItem.setText("Build");
		projectBuild_menuItem.setMnemonic(KeyEvent.VK_B);
		projectBuild_menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_B, ActionEvent.ALT_MASK));

		projectBuild_menuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						projectBuild_menuItem_actionPerformed(e);
					}
				});
		left_flowLayout.setAlignment(FlowLayout.LEFT);
		jMenuItem1.setFont(new java.awt.Font("Dialog", 0, 11));
		jMenuItem1.setText("Save");
		jMenuItem1.setMnemonic(KeyEvent.VK_S);
		jMenuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.ALT_MASK));
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jMenuItem1_actionPerformed(e);
			}
		});
		exit_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		exit_menuItem.setText("Exit");
		exit_menuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit_menuItem_actionPerformed(e);
			}
		});
		top_splitPane.setPreferredSize(new Dimension(706, 800));
		top_splitPane.setDividerSize(4);
		this.getContentPane().setLayout(borderLayout3);
		projectRun_menuItem.setEnabled(true);
		projectRun_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		projectRun_menuItem.setText("Run in same VM");
		projectRun_menuItem
				.setToolTipText("Run in same VM (hold shift down to run in separate process)");
		projectRun_menuItem.setMnemonic(KeyEvent.VK_R);
		projectRun_menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.ALT_MASK));
		projectRun_menuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						projectRun_menuItem_actionPerformed(e);
					}
				});
		projectRunOther_menuItem.setEnabled(true);
		projectRunOther_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		projectRunOther_menuItem.setText("Run in separate process");
		projectRunOther_menuItem.setMnemonic(KeyEvent.VK_P);
		projectRunOther_menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, ActionEvent.ALT_MASK));
		projectRunOther_menuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						projectRunOther_menuItem_actionPerformed(e);
					}
				});
		statusText_label.setFont(new java.awt.Font("Dialog", 0, 11));
		statusText_label.setBorder(BorderFactory.createLoweredBevelBorder());
		statusText_label.setMaximumSize(new Dimension(2000, 20));
		statusText_label.setPreferredSize(new Dimension(300, 20));
		status_panel.setLayout(borderLayout4);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				this_windowClosed(e);
			}

			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
		});
		toolbar_panel.setLayout(borderLayout5);
		right_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		right_splitPane.setBorder(null);
		right_splitPane.setDividerSize(4);
		tools_menu.setFont(new java.awt.Font("Dialog", 0, 11));
		tools_menu.setText("Tools");
		tools_menu.setMnemonic(KeyEvent.VK_T);
		projectDebug_menuItem.setEnabled(false);
		projectDebug_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		projectDebug_menuItem.setText("Debug");
		svProperties_menuItem
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						svProperties_menuItem_actionPerformed(e);
					}
				});
		svProperties_menuItem.setText("Options...");
		svProperties_menuItem.setActionCommand("AJDE Console...");
		svProperties_menuItem.setFont(new java.awt.Font("Dialog", 0, 11));
		svProperties_menuItem.setMnemonic(KeyEvent.VK_O);
		svProperties_menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.ALT_MASK));
		build_toolBar.setBorder(emptyBorder);
		build_toolBar.setFloatable(false);
		closeConfig_button.setMaximumSize(new Dimension(100, 20));
		closeConfig_button.setEnabled(true);
		closeConfig_button.setFont(new java.awt.Font("Dialog", 0, 11));
		closeConfig_button.setBorder(null);
		closeConfig_button.setMinimumSize(new Dimension(24, 20));
		closeConfig_button.setPreferredSize(new Dimension(20, 20));
		closeConfig_button.setToolTipText("Close build configuration");
		closeConfig_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getCloseConfigIcon());
		closeConfig_button
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						closeConfig_button_actionPerformed(e);
					}
				});
		openConfig_button.setMaximumSize(new Dimension(100, 20));
		openConfig_button.setEnabled(true);
		openConfig_button.setFont(new java.awt.Font("Dialog", 0, 11));
		openConfig_button.setBorder(null);
		openConfig_button.setMinimumSize(new Dimension(24, 20));
		openConfig_button.setPreferredSize(new Dimension(20, 20));
		openConfig_button.setToolTipText("Select build configuration...");
		openConfig_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getOpenConfigIcon());
		openConfig_button
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openConfig_button_actionPerformed(e);
					}
				});
		run_button.setMaximumSize(new Dimension(60, 20));
		run_button.setEnabled(true);
		run_button.setFont(new java.awt.Font("Dialog", 0, 11));
		run_button.setBorder(null);
		run_button.setMinimumSize(new Dimension(24, 20));
		run_button.setPreferredSize(new Dimension(20, 20));
		run_button
				.setToolTipText("Run in same VM (hold shift down to run in separate process)");
		run_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getExecuteIcon());
		run_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run_button_actionPerformed(e);
			}
		});
		project_toolBar.setBorder(emptyBorder);
		save_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_button_actionPerformed(e);
			}
		});
		save_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getSaveIcon());
		save_button.setText("Save");
		save_button.setToolTipText("Save");
		save_button.setPreferredSize(new Dimension(55, 20));
		save_button.setMinimumSize(new Dimension(24, 20));
		save_button.setFont(new java.awt.Font("Dialog", 0, 11));
		save_button.setBorder(null);
		save_button.setMaximumSize(new Dimension(60, 20));
		options_button.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options_button_actionPerformed(e);
			}
		});
		options_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getBrowserOptionsIcon());
		options_button.setText("Options");
		options_button.setToolTipText("Options...");
		options_button.setPreferredSize(new Dimension(60, 20));
		options_button.setMinimumSize(new Dimension(24, 20));
		options_button.setFont(new java.awt.Font("Dialog", 0, 11));
		options_button.setBorder(null);
		options_button.setMaximumSize(new Dimension(80, 20));
		editConfig_button
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						editConfig_button_actionPerformed(e);
					}
				});
		editConfig_button.setIcon(Ajde.getDefault().getIconRegistry()
				.getStructureSwingIcon(IProgramElement.Kind.FILE_LST));
		editConfig_button.setText("Edit Config");
		editConfig_button.setToolTipText("Edit Config...");
		editConfig_button.setPreferredSize(new Dimension(80, 20));
		editConfig_button.setMinimumSize(new Dimension(24, 20));
		editConfig_button.setFont(new java.awt.Font("Dialog", 0, 11));
		editConfig_button.setBorder(null);
		editConfig_button.setMaximumSize(new Dimension(80, 20));
		file_toolBar.setBorder(emptyBorder);
		toolBar_panel.setLayout(borderLayout6);
		jLabel1.setFont(new java.awt.Font("Dialog", 0, 11));
		jLabel1.setText(" Build: ");
		jLabel2.setText("      Run: ");
		jLabel2.setFont(new java.awt.Font("Dialog", 0, 11));
		// fileStructure_panel.setFont(new java.awt.Font("Dialog", 0, 11));
		// browser_panel.setFont(new java.awt.Font("Dialog", 0, 11));
		this.getContentPane().add(top_splitPane, BorderLayout.CENTER);
		top_splitPane.add(right_splitPane, JSplitPane.RIGHT);
		top_splitPane.add(multiView_panel, JSplitPane.LEFT);
		right_splitPane.add(messages_panel, JSplitPane.BOTTOM);
		right_splitPane.add(editor_panel, JSplitPane.TOP);
		// structureView_pane.add(fileStructure_panel, JSplitPane.RIGHT);
		// structureView_pane.add(browser_panel, JSplitPane.LEFT);
		this.getContentPane().add(status_panel, BorderLayout.SOUTH);
		status_panel.add(statusText_label, BorderLayout.CENTER);
		this.getContentPane().add(toolbar_panel, BorderLayout.NORTH);
		toolbar_panel.add(filler_panel, BorderLayout.CENTER);
		toolbar_panel.add(toolBar_panel, BorderLayout.WEST);
		// file_toolBar.add(editConfig_button, null);
		file_toolBar.add(save_button, null);
		file_toolBar.add(options_button, null);
		toolBar_panel.add(build_toolBar, BorderLayout.WEST);
		toolBar_panel.add(project_toolBar, BorderLayout.CENTER);
		project_toolBar.add(jLabel2, null);
		project_toolBar.add(run_button, null);
		build_toolBar.add(jLabel1, null);
		build_toolBar.add(openConfig_button, null);
		build_toolBar.add(closeConfig_button, null);
		toolBar_panel.add(file_toolBar, BorderLayout.EAST);
		menuBar.add(jMenu1);
		menuBar.add(jMenu2);
		menuBar.add(tools_menu);
		jMenu1.add(jMenuItem1);
		jMenu1.addSeparator();
		jMenu1.add(exit_menuItem);
		jMenu2.add(projectBuild_menuItem);
		jMenu2.add(projectRun_menuItem);
		jMenu2.add(projectRunOther_menuItem);
		// jMenu2.add(projectDebug_menuItem);
		tools_menu.add(joinpointProbe_menuItem);
		tools_menu.add(svProperties_menuItem);
		top_splitPane.setDividerLocation(380);
		right_splitPane.setDividerLocation(500);
		project_toolBar.addSeparator();
		project_toolBar.addSeparator();
	}

	private void exit_menuItem_actionPerformed(ActionEvent e) {
		quit();
	}

	private void this_windowClosing(WindowEvent e) {
		quit();
	}

	private void quit() {
		this.dispose();
		System.exit(0);
	}

	void treeMode_comboBox_actionPerformed(ActionEvent e) {
	}

	void save_button_actionPerformed(ActionEvent e) {
		BrowserManager.getDefault().getEditorManager().saveContents();
	}

	void this_windowClosed(WindowEvent e) {
		quit();
	}

	public void showMessagesPanel(BrowserMessageHandler handler) {
		right_splitPane.setDividerLocation(right_splitPane.getHeight() - 100);
		messages_panel.showMessageHandlerPanel(handler, true);
	}

	public void hideMessagesPanel(BrowserMessageHandler handler) {
		right_splitPane.setDividerLocation(right_splitPane.getHeight());
		messages_panel.showMessageHandlerPanel(handler, false);
	}

	void jMenuItem1_actionPerformed(ActionEvent e) {
		BrowserManager.getDefault().getEditorManager().saveContents();
	}

	void projectBuild_menuItem_actionPerformed(ActionEvent e) {
		BrowserManager.getDefault().saveAll();
		if (EditorManager.isShiftDown(e.getModifiers())) {
			buildFresh();
		} else {
			build();
		}
	}

	void run_button_actionPerformed(ActionEvent e) {
		if (EditorManager.isShiftDown(e.getModifiers())) {
			runInNewVM();
		} else {
			runInSameVM();
		}
	}

	void projectRunOther_menuItem_actionPerformed(ActionEvent e) {
		runInNewVM();
	}

	void projectRun_menuItem_actionPerformed(ActionEvent e) {
		if (EditorManager.isShiftDown(e.getModifiers())) {
			runInNewVM();
		} else {
			runInSameVM();
		}
	}

	void build_button_actionPerformed(ActionEvent e) {
		BrowserManager.getDefault().saveAll();
		if (EditorManager.isShiftDown(e.getModifiers())) {
			buildFresh();
		} else {
			build();
		}
	}

	void options_button_actionPerformed(ActionEvent e) {
		Ajde.getDefault().showOptionsFrame();
	}

	void editConfig_button_actionPerformed(ActionEvent e) {
		BrowserManager.getDefault().openFile(
				Ajde.getDefault().getBuildConfigManager()
						.getActiveConfigFile());
		refreshBuildMenu();
	}

	public void resetSourceEditorPanel() {
		right_splitPane.removeAll();
		right_splitPane.add(sourceEditor_panel, JSplitPane.TOP);
	}

	private void svProperties_menuItem_actionPerformed(ActionEvent e) {
		Ajde.getDefault().showOptionsFrame();
	}

	private void openConfig_button_actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(lastChosenDir);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return (f.getPath().endsWith(".lst") || f.isDirectory());
			}

			public String getDescription() {
				return "AspectJ Build Configuration (*.lst)";
			}
		});
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File result = fileChooser.getSelectedFile();
			String path = result.getAbsolutePath();// .replace('\\', '/');
			if (!Ajde.getDefault().getBuildConfigManager()
					.getAllBuildConfigFiles().contains(path)) {
				Ajde.getDefault().getBuildConfigManager()
						.getAllBuildConfigFiles().add(0, path);
			}
			Ajde.getDefault().getBuildConfigManager().setActiveConfigFile(
					path);
			lastChosenDir = result.getParentFile();
			if (null == lastChosenDir) {
				lastChosenDir = CURRENT_DIR;
			}
			refreshBuildMenu();
		}
	}

	private void closeConfig_button_actionPerformed(ActionEvent e) {
		Ajde.getDefault().getBuildConfigManager()
				.getAllBuildConfigFiles().remove(
						Ajde.getDefault().getBuildConfigManager()
								.getActiveConfigFile());
		if (!Ajde.getDefault().getBuildConfigManager()
				.getAllBuildConfigFiles().isEmpty()) {
			Ajde.getDefault().getBuildConfigManager().setActiveConfigFile(
					(String) Ajde.getDefault().getBuildConfigManager()
							.getAllBuildConfigFiles().get(0));
		}
		refreshBuildMenu();
	}

	private void buildFresh() {
		Ajde.getDefault().runBuildInDifferentThread(Ajde.getDefault()
				.getBuildConfigManager().getActiveConfigFile(), true);
	}

	private void build() {
		Ajde.getDefault().runBuildInDifferentThread(Ajde.getDefault()
				.getBuildConfigManager().getActiveConfigFile(), false);
	}

	private void runInSameVM() {
		Ajde.getDefault().runInSameVM();
	}

	private void runInNewVM() {
		Ajde.getDefault().runInNewVM();
	}

}
