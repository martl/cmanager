package cmanager.gui;

import cmanager.CacheListController;
import cmanager.CacheListFilterCacheName;
import cmanager.CacheListFilterDifficulty;
import cmanager.CacheListFilterDistance;
import cmanager.CacheListFilterNotFoundBy;
import cmanager.CacheListFilterTerrain;
import cmanager.FileHelper;
import cmanager.geo.Geocache;
import cmanager.geo.Location;
import cmanager.geo.LocationList;
import cmanager.global.Compatibility;
import cmanager.global.Constants;
import cmanager.network.Updates;
import cmanager.okapi.OKAPI;
import cmanager.okapi.User;
import cmanager.settings.Settings;
import cmanager.util.DesktopUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainWindow extends JFrame {

    private static final long serialVersionUID = 6384767256902991990L;

    private final JFrame THIS = this;
    private final JPanel contentPane;
    private final JDesktopPane desktopPane;
    private final JMenu mnWindows;
    private final JComboBox<Location> comboBox;

    /** Create the frame. */
    public MainWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1050, 550));
        setLocationRelativeTo(null);
        Logo.setLogo(this);

        final JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        final JMenu mnMenu = new JMenu("Menu");
        menuBar.add(mnMenu);

        mnWindows = new JMenu("Windows");

        final JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke('O', Compatibility.SHORTCUT_KEY_MASK));
        mntmOpen.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        openFile(true);
                    }
                });

        final JMenuItem mntmNew = new JMenuItem("New");
        mntmNew.setAccelerator(KeyStroke.getKeyStroke('N', Compatibility.SHORTCUT_KEY_MASK));
        mntmNew.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            CacheListController.newCLC(
                                    desktopPane,
                                    mnWindows,
                                    (Location) comboBox.getSelectedItem(),
                                    null,
                                    new CacheListView.RunLocationDialogI() {
                                        public void openDialog(Geocache g) {
                                            openLocationDialog(g);
                                        }
                                    });
                        } catch (Throwable e1) {
                        }
                    }
                });
        mnMenu.add(mntmNew);
        mnMenu.add(mntmOpen);

        final JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        THIS.dispatchEvent(new WindowEvent(THIS, WindowEvent.WINDOW_CLOSING));
                    }
                });

        final JMenuItem mntmSettings = new JMenuItem("Settings");
        mntmSettings.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        SettingsDialog sw = new SettingsDialog(THIS);
                        sw.setModalityType(ModalityType.APPLICATION_MODAL);
                        sw.setLocationRelativeTo(THIS);
                        sw.setVisible(true);
                    }
                });

        final JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setAccelerator(KeyStroke.getKeyStroke('S', Compatibility.SHORTCUT_KEY_MASK));
        mntmSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        saveFile(false);
                    }
                });
        mntmSave.setEnabled(false);
        mnMenu.add(mntmSave);

        final JMenuItem mntmSaveAs = new JMenuItem("Save As");
        mntmSaveAs.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        saveFile(true);
                    }
                });
        mntmSaveAs.setEnabled(false);
        mnMenu.add(mntmSaveAs);

        final JSeparator separator = new JSeparator();
        mnMenu.add(separator);
        mnMenu.add(mntmSettings);

        final JSeparator separator_1 = new JSeparator();
        mnMenu.add(separator_1);
        mnMenu.add(mntmExit);

        final JMenu mnList = new JMenu("List");
        mnList.setEnabled(false);
        menuBar.add(mnList);

        final JMenuItem mntmFindSimilarOc = new JMenuItem("Find on OC");
        mntmFindSimilarOc.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        findOnOC(null, null);
                    }
                });
        mnList.add(mntmFindSimilarOc);

        final JMenuItem mntmFind = new JMenuItem("Sync with OC");
        mntmFind.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            User user = null;
                            String uuid = null;
                            try {
                                user = User.getOKAPIUser();
                                uuid = OKAPI.getUUID(user);
                                if (uuid == null) {
                                    throw new NullPointerException();
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                        THIS,
                                        "Testing the OKAPI token failed. Check your settings!",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            findOnOC(user, uuid);
                        } catch (Throwable ex) {
                            ExceptionPanel.showErrorDialog(THIS, ex);
                        }
                    }
                });
        mnList.add(mntmFind);

        final JSeparator separator_2 = new JSeparator();
        mnList.add(separator_2);

        final JMenuItem mntmCopy = new JMenuItem("Copy");
        mntmCopy.setAccelerator(KeyStroke.getKeyStroke('C', Compatibility.SHORTCUT_KEY_MASK));
        mntmCopy.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane).copySelected();
                    }
                });

        final JMenuItem mntmSelectAll = new JMenuItem("Select All / none");
        mntmSelectAll.setAccelerator(KeyStroke.getKeyStroke('A', Compatibility.SHORTCUT_KEY_MASK));
        mntmSelectAll.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .getView()
                                .tableSelectAllNone();
                    }
                });
        mnList.add(mntmSelectAll);

        final JMenuItem mntInvertSelection = new JMenuItem("Invert Selection");
        mntInvertSelection.setAccelerator(
                KeyStroke.getKeyStroke('I', Compatibility.SHORTCUT_KEY_MASK));
        mntInvertSelection.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .getView()
                                .invertTableSelection();
                    }
                });
        mnList.add(mntInvertSelection);

        final JSeparator separator_6 = new JSeparator();
        mnList.add(separator_6);
        mnList.add(mntmCopy);

        final JMenuItem mntmPaste = new JMenuItem("Paste");
        mntmPaste.setAccelerator(KeyStroke.getKeyStroke('V', Compatibility.SHORTCUT_KEY_MASK));
        mntmPaste.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane).pasteSelected();
                    }
                });

        final JMenuItem mntmCut = new JMenuItem("Cut");
        mntmCut.setAccelerator(KeyStroke.getKeyStroke('X', Compatibility.SHORTCUT_KEY_MASK));
        mntmCut.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane).cutSelected();
                    }
                });
        mnList.add(mntmCut);
        mnList.add(mntmPaste);

        final JMenuItem mntmDeleteSelectedCaches = new JMenuItem("Delete");
        mntmDeleteSelectedCaches.setAccelerator(
                KeyStroke.getKeyStroke('D', Compatibility.SHORTCUT_KEY_MASK));
        mntmDeleteSelectedCaches.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .removeSelectedCaches();
                    }
                });
        mnList.add(mntmDeleteSelectedCaches);

        final JSeparator separator_7 = new JSeparator();
        mnList.add(separator_7);

        final JMenuItem mntmUndoAction = new JMenuItem("Undo");
        mntmUndoAction.setAccelerator(KeyStroke.getKeyStroke('Z', Compatibility.SHORTCUT_KEY_MASK));
        mntmUndoAction.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .replayLastUndoAction();
                    }
                });
        mnList.add(mntmUndoAction);

        // only enable undo menu when undo actions are available
        mnList.addMenuListener(
                new MenuAdapter() {
                    public void menuSelected(MenuEvent e) {
                        mntmUndoAction.setEnabled(
                                CacheListController.getTopViewCacheController(desktopPane)
                                                .getUndoActionCount()
                                        > 0);
                    }
                });

        final JSeparator separator_3 = new JSeparator();
        mnList.add(separator_3);

        final JSeparator separator_4 = new JSeparator();
        mnList.add(separator_4);

        final JMenuItem mntmAddFromFile = new JMenuItem("Add from File");
        mntmAddFromFile.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openFile(false);
                    }
                });
        mnList.add(mntmAddFromFile);

        final JMenu mnFilter = new JMenu("Filter");
        mnFilter.setEnabled(false);
        menuBar.add(mnFilter);

        final JMenu mntmAddFilter = new JMenu("Add");
        mnFilter.add(mntmAddFilter);

        final JMenuItem mntmTerrainFilter = new JMenuItem("Terrain");
        mntmTerrainFilter.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .addFilter(new CacheListFilterTerrain());
                    }
                });
        mntmAddFilter.add(mntmTerrainFilter);

        final JMenuItem mntmCacheNameFilter = new JMenuItem("Cachename");
        mntmCacheNameFilter.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .addFilter(new CacheListFilterCacheName());
                    }
                });
        mntmAddFilter.add(mntmCacheNameFilter);

        final JMenuItem mntmDifficultyFilter = new JMenuItem("Difficulty");
        mntmDifficultyFilter.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .addFilter(new CacheListFilterDifficulty());
                    }
                });
        mntmAddFilter.add(mntmDifficultyFilter);

        final JMenuItem mntmNotFoundBy = new JMenuItem("Not Found By");
        mntmNotFoundBy.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .addFilter(new CacheListFilterNotFoundBy());
                    }
                });
        mntmAddFilter.add(mntmNotFoundBy);

        final JSeparator separator_5 = new JSeparator();
        mnFilter.add(separator_5);

        final JMenuItem mntmDeleteCachesNot = new JMenuItem("Delete Caches NOT in Filter");
        mntmDeleteCachesNot.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        CacheListController.getTopViewCacheController(desktopPane)
                                .removeCachesNotInFilter();
                    }
                });
        mnFilter.add(mntmDeleteCachesNot);

        final JMenuItem mntmDistance = new JMenuItem("Distance");
        mntmDistance.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        final CacheListFilterDistance filter = new CacheListFilterDistance();
                        CacheListController.getTopViewCacheController(desktopPane)
                                .addFilter(filter);

                        // set current location
                        filter.setLocation((Location) comboBox.getSelectedItem());

                        // update filter on location change
                        final ActionListener al =
                                new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        filter.setLocation((Location) comboBox.getSelectedItem());
                                    }
                                };
                        comboBox.addActionListener(al);

                        // remove update hook on removal
                        filter.addRemoveAction(
                                new Runnable() {
                                    public void run() {
                                        comboBox.removeActionListener(al);
                                    }
                                });
                    }
                });
        mntmAddFilter.add(mntmDistance);

        mnWindows.setEnabled(false);
        menuBar.add(mnWindows);

        final JMenu menuInfo = new JMenu("Information");
        final JMenuItem menuItemAbout = new JMenuItem("About");
        menuItemAbout.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        final AboutDialog dialog = new AboutDialog();
                        dialog.setLocationRelativeTo(THIS);
                        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                        dialog.setVisible(true);
                    }
                });
        menuInfo.add(menuItemAbout);
        menuBar.add(menuInfo);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        desktopPane = new JDesktopPane();
        contentPane.add(desktopPane, BorderLayout.CENTER);
        desktopPane.addContainerListener(
                new ContainerListener() {
                    public void componentRemoved(ContainerEvent e) {
                        updateVisibility(desktopPane.getAllFrames().length != 0);
                    }

                    public void componentAdded(ContainerEvent e) {
                        updateVisibility(desktopPane.getAllFrames().length != 0);
                    }

                    private void updateVisibility(boolean visible) {
                        mntmSave.setEnabled(visible);
                        mntmSaveAs.setEnabled(visible);
                        mnList.setEnabled(visible);
                        mnFilter.setEnabled(visible);
                        // mntmDeleteSelectedCaches.setEnabled(visible);
                        mnWindows.setEnabled(visible);
                    }
                });
        desktopPane.setMinimumSize(new Dimension(200, 200));

        final JPanel panelSouth = new JPanel();
        panelSouth.setLayout(new BorderLayout(0, 0));
        contentPane.add(panelSouth, BorderLayout.SOUTH);

        final ExceptionPanel panelException = ExceptionPanel.getPanel();
        panelSouth.add(panelException, BorderLayout.NORTH);

        final JPanel panelUpdate = new JPanel();
        panelSouth.add(panelUpdate, BorderLayout.SOUTH);

        final JButton btnUpdate = new JButton("");
        btnUpdate.setVisible(false);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setOpaque(false);
        btnUpdate.setContentAreaFilled(false);
        btnUpdate.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        DesktopUtil.openUrl(
                                "https://github.com/FriedrichFroebel/cmanager/releases");
                    }
                });
        panelUpdate.add(btnUpdate);

        new SwingWorker<Void, Boolean>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(Updates.updateAvailable_block());
                return null;
            }

            @Override
            protected void process(List<Boolean> chunks) {
                // Display update message if there is another version available
                if (chunks.get(0)) {
                    setText(
                            "Version "
                                    + Updates.getNewVersion()
                                    + " of "
                                    + Constants.APP_NAME
                                    + " is available. Click here for updates!");
                    btnUpdate.setVisible(true);
                }
            }

            private void setText(String text) {
                btnUpdate.setText(
                        "<HTML><FONT color=\"#008000\"><U>" + text + "</U></FONT></HTML>");
            }
        }.execute();

        final JPanel panelNorth = new JPanel();
        contentPane.add(panelNorth, BorderLayout.NORTH);
        panelNorth.setLayout(new BorderLayout(0, 0));

        final JPanel panel = new JPanel();
        panelNorth.add(panel, BorderLayout.EAST);

        final JLabel lblNewLabel = new JLabel("Location:");
        lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 10));
        panel.add(lblNewLabel);

        comboBox = new JComboBox<Location>();
        comboBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        propagateSelectedLocationComboboxEntry();
                    }
                });
        comboBox.setFont(new Font("Dialog", Font.BOLD, 10));
        panel.add(comboBox);

        final JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        openLocationDialog(null);
                    }
                });
        btnEdit.setFont(new Font("Dialog", Font.BOLD, 10));
        panel.add(btnEdit);

        updateLocationCombobox();
        propagateSelectedLocationComboboxEntry();

        //
        // store and reopen cache lists
        //

        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        try {
                            CacheListController.storePersistanceInfo(desktopPane);
                        } catch (IOException e1) {
                            ExceptionPanel.showErrorDialog(THIS, e1);
                        }
                    }
                });

        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        actionWithWaitDialog(
                                new Runnable() {
                                    public void run() {
                                        CacheListController.reopenPersitantCLCs(
                                                desktopPane,
                                                mnWindows,
                                                (Location) comboBox.getSelectedItem(),
                                                new CacheListView.RunLocationDialogI() {
                                                    public void openDialog(Geocache g) {
                                                        openLocationDialog(g);
                                                    }
                                                });
                                    }
                                },
                                THIS);
                    }
                });
    }

    private void updateLocationCombobox() {
        ArrayList<Location> locations = LocationList.getList().getLocations();

        comboBox.removeAllItems();
        for (final Location l : locations) {
            comboBox.addItem(l);
        }
    }

    private void openLocationDialog(Geocache g) {
        LocationDialog ld = new LocationDialog(this);
        if (g != null) {
            ld.setGeocache(g);
        }
        ld.setLocationRelativeTo(THIS);
        ld.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        ld.setVisible(true);

        if (ld.modified) {
            updateLocationCombobox();
            propagateSelectedLocationComboboxEntry();
        }
    }

    private void propagateSelectedLocationComboboxEntry() {
        CacheListController.setAllRelativeLocations((Location) comboBox.getSelectedItem());
        repaint(); // update front most table
    }

    private void saveFile(boolean saveAs) {
        final CacheListController clc = CacheListController.getTopViewCacheController(desktopPane);

        String strPath = null;
        try {
            strPath = clc.getPath().toString();
        } catch (Exception e) {
            saveAs = true;
        }

        if (saveAs) {
            if (strPath == null) {
                strPath = Settings.getS(Settings.Key.FILE_CHOOSER_LOAD_GPX);
            }
            final JFileChooser chooser = new JFileChooser(strPath);
            chooser.setFileFilter(new FileNameExtensionFilter("ZIP Archive", "zip"));

            if (chooser.showSaveDialog(THIS) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            strPath = chooser.getSelectedFile().getAbsolutePath();
        }

        if (!FileHelper.getFileExtension(strPath).equals("zip")) {
            strPath += ".zip";
        }

        if (saveAs) {
            final File f = new File(strPath);
            if (f.exists() && !f.isDirectory()) {
                final int dialogResult =
                        JOptionPane.showConfirmDialog(
                                THIS,
                                "The choosen file already exists. Overwrite it?",
                                "Warning",
                                JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            Settings.set(
                    Settings.Key.FILE_CHOOSER_LOAD_GPX, Paths.get(strPath).getParent().toString());
        }

        actionWithWaitDialog(
                new Runnable() {
                    private Path path;

                    public Runnable setPath(Path path) {
                        this.path = path;
                        return this;
                    }

                    public void run() {
                        try {
                            clc.store(path);
                        } catch (Throwable e) {
                            ExceptionPanel.showErrorDialog(THIS, e);
                        }
                    }
                }.setPath(Paths.get(strPath)),
                THIS);
    }

    private void openFile(final boolean createNewList) {
        String lastPath = Settings.getS(Settings.Key.FILE_CHOOSER_LOAD_GPX);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(
                new FileNameExtensionFilter("GPS Exchange Format | ZIP Archive", "gpx", "zip"));

        if (chooser.showOpenDialog(THIS) == JFileChooser.APPROVE_OPTION) {
            lastPath = chooser.getSelectedFile().getPath();
            Settings.set(
                    Settings.Key.FILE_CHOOSER_LOAD_GPX, Paths.get(lastPath).getParent().toString());

            actionWithWaitDialog(
                    new Runnable() {
                        public void run() {
                            try {
                                if (createNewList) {
                                    CacheListController.newCLC(
                                            desktopPane,
                                            mnWindows,
                                            (Location) comboBox.getSelectedItem(),
                                            chooser.getSelectedFile().getAbsolutePath(),
                                            new CacheListView.RunLocationDialogI() {
                                                public void openDialog(Geocache g) {
                                                    openLocationDialog(g);
                                                }
                                            });
                                } else {
                                    CacheListController.getTopViewCacheController(desktopPane)
                                            .addFromFile(
                                                    chooser.getSelectedFile().getAbsolutePath());
                                }
                            } catch (Throwable e) {
                                // Main.gc();
                                ExceptionPanel.showErrorDialog(THIS, e);
                            }
                        }
                    },
                    THIS);
        }
    }

    /**
     * Display a "Please stand-by" dialog with a static delay of 25 milliseconds.
     *
     * @param task The task to execute with this wait dialog.
     * @param parent The parent component.
     */
    public static void actionWithWaitDialog(final Runnable task, Component parent) {
        MainWindow.actionWithWaitDialog(task, parent, 25);
    }

    /**
     * Display a "Please stand-by" dialog with a configurable delay.
     *
     * @param task The task to execute with this wait dialog.
     * @param parent The parent component.
     * @param delayMilliseconds The delay in milliseconds. If this time has not passed, the dialog
     *     will not be closed.
     */
    public static void actionWithWaitDialog(
            final Runnable task, Component parent, final int delayMilliseconds) {
        final WaitDialog wait = new WaitDialog();

        wait.setModalityType(ModalityType.APPLICATION_MODAL);
        wait.setLocationRelativeTo(parent);

        new Thread(
                        new Runnable() {
                            public void run() {
                                while (!wait.isVisible()) {
                                    try {
                                        Thread.sleep(delayMilliseconds);
                                    } catch (InterruptedException e) {
                                    }
                                }

                                task.run();
                                wait.setVisible(false);
                            }
                        })
                .start();

        wait.setVisible(true);
        wait.repaint();
    }

    private void findOnOC(User user, String uuid) {
        final DuplicateDialog dd =
                new DuplicateDialog(
                        CacheListController.getTopViewCacheController(desktopPane).getModel(),
                        user,
                        uuid);

        FrameHelper.showModalFrame(dd, THIS);
    }
}
