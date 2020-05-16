package cmanager.gui;

import cmanager.CacheListModel;
import cmanager.geo.Geocache;
import cmanager.geo.GeocacheLog;
import cmanager.oc.ShadowList;
import cmanager.oc.Util;
import cmanager.okapi.User;
import cmanager.settings.Settings;
import cmanager.util.DesktopUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class DuplicateDialog extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JFrame THIS = this;
    private final JPanel contentPanel = new JPanel();
    private final JTree tree;
    private final DefaultMutableTreeNode rootNode;
    private final JProgressBar progressBar;
    private String selectedURL;
    private final JScrollPane scrollPaneTree;

    private AtomicBoolean stopBackgroundThread = new AtomicBoolean(false);
    private Thread backgroundThread = null;

    private final ArrayList<GeocacheLog> logsCopied = new ArrayList<>();
    private ShadowList shadowList = null;

    /** Create the dialog. */
    public DuplicateDialog(final CacheListModel clm, final User user, final String uuid) {
        setResizable(true);
        this.setMinimumSize(new Dimension(600, 300));
        Logo.setLogo(this);

        setTitle("Duplicate Finder");
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new CardLayout(0, 0));

        final JPanel panelProgress = new JPanel();
        panelProgress.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPanel.add(panelProgress, "name_449323361533634");
        panelProgress.setLayout(new BorderLayout(0, 0));

        final JPanel panelBorder = new JPanel();
        panelBorder.setBorder(new EmptyBorder(20, 5, 5, 5));
        panelProgress.add(panelBorder, BorderLayout.NORTH);
        panelBorder.setLayout(new BorderLayout(0, 0));

        progressBar = new JProgressBar();
        panelBorder.add(progressBar);

        // create the root node
        rootNode = new DefaultMutableTreeNode("Root");

        final JPanel panelTree = new JPanel();
        contentPanel.add(panelTree, "2");
        panelTree.setLayout(new BorderLayout(0, 0));

        final JPanel panelUrl = new JPanel();
        panelTree.add(panelUrl, BorderLayout.SOUTH);
        panelUrl.setLayout(new BorderLayout(0, 0));

        final JButton btnURL = new JButton("");
        btnURL.setBorderPainted(false);
        btnURL.setOpaque(false);
        btnURL.setContentAreaFilled(false);
        // btnURL.setBackground(new JPanel().getBackground());
        btnURL.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (selectedURL != null) {
                            DesktopUtil.openUrl(selectedURL);
                        }
                    }
                });
        panelUrl.add(btnURL);

        final JPanel panel = new JPanel();
        panelUrl.add(panel, BorderLayout.SOUTH);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        final JButton btnClipboard = new JButton("Export all as text to clipboard");
        panel.add(btnClipboard);
        btnClipboard.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        final StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < rootNode.getChildCount(); i++) {
                            final DefaultMutableTreeNode dmtn =
                                    (DefaultMutableTreeNode) rootNode.getChildAt(i);
                            final Geocache g = (Geocache) dmtn.getUserObject();
                            sb.append(g.toString()).append(System.lineSeparator());

                            for (int j = 0; j < dmtn.getChildCount(); j++) {
                                final DefaultMutableTreeNode child =
                                        (DefaultMutableTreeNode) dmtn.getChildAt(j);
                                final Geocache g2 = (Geocache) child.getUserObject();
                                sb.append("  ")
                                        .append(g2.toString())
                                        .append(System.lineSeparator());
                            }
                            sb.append(System.lineSeparator());
                        }

                        final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clpbrd.setContents(new StringSelection(sb.toString()), null);
                    }
                });

        tree = new JTree(rootNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent e) {
                        final DefaultMutableTreeNode node =
                                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        if (node == null) {
                            return;
                        }

                        final Object userObj = node.getUserObject();
                        if (userObj instanceof Geocache) {
                            final Geocache g = (Geocache) userObj;
                            URL2Button(g.getURL());
                        }
                    }

                    private void URL2Button(String url) {
                        selectedURL = url;
                        btnURL.setText(
                                "<HTML><FONT color=\"#000099\"><U>" + url + "</U></FONT></HTML>");
                    }
                });
        tree.addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent me) {
                        if (me.getClickCount() >= 2) {
                            final DefaultMutableTreeNode node =
                                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                            if (node == null) {
                                return;
                            }

                            final Object userObj = node.getUserObject();
                            if (userObj instanceof Geocache) {
                                final Geocache oc = (Geocache) userObj;

                                if (uuid != null && oc.isOC()) {
                                    final DefaultMutableTreeNode parent =
                                            (DefaultMutableTreeNode) node.getParent();
                                    final Geocache gc = (Geocache) parent.getUserObject();

                                    try {
                                        final CopyLogDialog cld =
                                                new CopyLogDialog(gc, oc, logsCopied, shadowList);
                                        cld.setLocationRelativeTo(THIS);
                                        FrameHelper.showModalFrame(cld, THIS);
                                    } catch (Throwable e) {
                                        ExceptionPanel.showErrorDialog(THIS, e);
                                    }
                                }
                            }
                        }
                    }
                });

        scrollPaneTree =
                new JScrollPane(
                        tree,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panelTree.add(scrollPaneTree);

        final JPanel panelCopyMessage = new JPanel();
        panelTree.add(panelCopyMessage, BorderLayout.NORTH);
        if (uuid == null) {
            panelCopyMessage.setVisible(false);
        }
        panelCopyMessage.setLayout(new BorderLayout(0, 0));

        final JLabel lblDoubleClickAn = new JLabel("Double Click an OC cache to open copy dialog.");
        lblDoubleClickAn.setHorizontalAlignment(SwingConstants.CENTER);
        panelCopyMessage.add(lblDoubleClickAn);

        final JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new BorderLayout(0, 0));

        final JPanel panel_1 = new JPanel();
        buttonPane.add(panel_1, BorderLayout.EAST);

        final JButton okButton = new JButton("Dismiss");
        panel_1.add(okButton);
        okButton.setHorizontalAlignment(SwingConstants.RIGHT);
        okButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        THIS.setVisible(false);
                        if (backgroundThread != null) stopBackgroundThread.set(true);
                        THIS.dispose();
                    }
                });
        getRootPane().setDefaultButton(okButton);

        final JPanel panel_2 = new JPanel();
        buttonPane.add(panel_2, BorderLayout.WEST);

        final JLabel labelCandidates = new JLabel("0");
        panel_2.add(labelCandidates);

        final JLabel lblHits = new JLabel("Candidates");
        panel_2.add(lblHits);

        backgroundThread =
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    // update local copy of shadow list and load it
                                    ShadowList.updateShadowList();
                                    shadowList = ShadowList.loadShadowList();

                                    Util.findOnOc(
                                            stopBackgroundThread,
                                            clm,
                                            new Util.OutputInterface() {
                                                public void setProgress(
                                                        Integer count, Integer max) {
                                                    progressBar.setMaximum(max);
                                                    progressBar.setValue(count);
                                                    progressBar.setString(
                                                            count.toString()
                                                                    + "/"
                                                                    + max.toString());
                                                    progressBar.setStringPainted(true);
                                                }

                                                private Geocache lastGc = null;
                                                private DefaultMutableTreeNode lastNode = null;
                                                private Integer candidates = 0;

                                                public void match(Geocache gc, Geocache oc) {
                                                    candidates++;
                                                    labelCandidates.setText(candidates.toString());

                                                    if (gc != lastGc) {
                                                        lastGc = gc;
                                                        lastNode = new DefaultMutableTreeNode(gc);
                                                        rootNode.add(lastNode);
                                                    }
                                                    lastNode.add(new DefaultMutableTreeNode(oc));
                                                }
                                            },
                                            user,
                                            uuid,
                                            shadowList);
                                    switchCards();

                                    if (stopBackgroundThread.get()) {
                                        return;
                                    }

                                    // sort
                                    final ArrayList<DefaultMutableTreeNode> sortedList =
                                            new ArrayList<>();
                                    final ArrayList<DefaultMutableTreeNode> list =
                                            new ArrayList<>();

                                    // get all entrys
                                    for (int i = 0; i < rootNode.getChildCount(); i++) {
                                        list.add((DefaultMutableTreeNode) rootNode.getChildAt(i));
                                    }
                                    rootNode.removeAllChildren();

                                    // sort
                                    final String gcUsername =
                                            Settings.getS(Settings.Key.GC_USERNAME);
                                    while (!list.isEmpty()) {
                                        DefaultMutableTreeNode next = null;

                                        for (int j = 0; j < list.size(); j++) {
                                            final DefaultMutableTreeNode curr = list.get(j);
                                            if (next == null) {
                                                next = curr;
                                            } else {
                                                final Geocache gNext =
                                                        (Geocache) next.getUserObject();
                                                final Geocache gCurr =
                                                        (Geocache) curr.getUserObject();

                                                GeocacheLog logNext = null;
                                                for (final GeocacheLog log : gNext.getLogs()) {
                                                    if (log.isAuthor(gcUsername)
                                                            && log.isFoundLog()) {
                                                        logNext = log;
                                                        break;
                                                    }
                                                }

                                                GeocacheLog logCurr = null;
                                                for (final GeocacheLog log : gCurr.getLogs()) {
                                                    if (log.isAuthor(gcUsername)
                                                            && log.isFoundLog()) {
                                                        logCurr = log;
                                                        break;
                                                    }
                                                }

                                                if (logCurr == null) {
                                                    continue;
                                                }
                                                if (logNext == null) {
                                                    next = curr;
                                                    continue;
                                                }

                                                if (logCurr.getDate().isAfter(logNext.getDate())) {
                                                    next = curr;
                                                }
                                            }
                                        }
                                        list.remove(next);
                                        sortedList.add(next);
                                    }

                                    // add entrys
                                    for (int i = 0; i < sortedList.size(); i++) {
                                        rootNode.insert(sortedList.get(i), i);
                                    }

                                    for (int i = 0; i < tree.getRowCount(); i++) {
                                        tree.expandRow(i);
                                    }
                                    tree.setRootVisible(false);

                                    if (tree.getRowCount() == 0) {
                                        tree.setVisible(false);
                                    }
                                } catch (Throwable e1) {
                                    // Since Thread.stop() is used, the threads will most likely
                                    // complain in weird ways. We do not care about these
                                    // exceptions.
                                    if (!stopBackgroundThread.get()) {
                                        ExceptionPanel.showErrorDialog(THIS, e1);
                                    }
                                    THIS.setVisible(false);
                                }
                            }
                        });
        backgroundThread.start();
    }

    private void switchCards() {
        final CardLayout cl = (CardLayout) (contentPanel.getLayout());
        cl.show(contentPanel, "2");
    }
}
