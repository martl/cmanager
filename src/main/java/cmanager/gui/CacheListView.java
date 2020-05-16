package cmanager.gui;

import cmanager.CacheListController;
import cmanager.CacheListFilterModel;
import cmanager.CacheListModel;
import cmanager.geo.Geocache;
import cmanager.geo.GeocacheType;
import cmanager.global.Compatibility;
import cmanager.global.Constants;
import cmanager.osm.PersitentTileCache;
import cmanager.util.DesktopUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class CacheListView extends JInternalFrame {

    private static final long serialVersionUID = -3610178481183679565L;

    private final CacheListController clc;
    private final JTable table;
    private final CachePanel panelCache;
    private final JLabel lblCacheCount;
    private final JLabel lblLblwaypointscount;
    private final CustomJMapViewer mapViewer;
    private final JPanel panelFilters;

    private Point popupPoint;

    /** Create the frame. */
    public CacheListView(
            final CacheListController clc, final CacheListView.RunLocationDialogI ldi) {
        this.clc = clc;

        final AbstractTableModel atm = clc.getTableModel();
        table = new JTable(atm);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel()
                .addListSelectionListener(
                        new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent arg0) {
                                updateCachePanelToSelection();
                                updateMapMarkers();
                            }
                        });
        table.setAutoCreateRowSorter(true);

        final DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        panelFilters = new JPanel();
        panelFilters.setVisible(false);
        getContentPane().add(panelFilters, BorderLayout.NORTH);
        panelFilters.setLayout(new BoxLayout(panelFilters, BoxLayout.Y_AXIS));

        final JScrollPane scrollPane =
                new JScrollPane(
                        table,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(300, 300));

        final JSplitPane splitPane1 = new JSplitPane();
        getContentPane().add(splitPane1, BorderLayout.CENTER);
        splitPane1.setLeftComponent(scrollPane);

        final JSplitPane splitPane2 = new JSplitPane();
        splitPane1.setRightComponent(splitPane2);
        splitPane2.setVisible(false);

        panelCache = new CachePanel();
        panelCache.setVisible(false);
        splitPane2.setLeftComponent(panelCache);

        final JPanel panelMap = new JPanel();
        panelMap.setVisible(false);
        splitPane2.setRightComponent(panelMap);
        panelMap.setLayout(new BorderLayout(0, 0));

        mapViewer =
                new CustomJMapViewer(new PersitentTileCache(Constants.CACHE_FOLDER + "maps.osm/"));
        mapViewer.setFocusable(true);
        panelMap.add(mapViewer, BorderLayout.CENTER);

        final JPanel panel_2 = new JPanel();
        panelMap.add(panel_2, BorderLayout.SOUTH);

        final JLabel lblMapHelp =
                new JLabel("Drag map with right mouse, selection box with left mouse.");
        lblMapHelp.setFont(new Font("Dialog", Font.BOLD, 9));
        panel_2.add(lblMapHelp);

        // Make map movable with mouse
        final DefaultMapController mapController = new DefaultMapController(mapViewer);
        mapController.setMovementMouseButton(MouseEvent.BUTTON2);

        mapViewer.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);

                        if (e.getButton() == MouseEvent.BUTTON1) {
                            final Point p = e.getPoint();
                            final Geocache g = getMapFocusedCache(p);
                            if (g == null) {
                                return;
                            }

                            if (e.getClickCount() == 1
                                    && ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
                                DesktopUtil.openUrl(g.getURL());
                            } else if (e.getClickCount() == 1) {
                                panelCache.setCache(g);
                            }
                        }
                    }
                });
        mapViewer.addMouseMotionListener(
                new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        final Point p = e.getPoint();
                        final Geocache g = getMapFocusedCache(p);

                        String tip = null;
                        if (g != null) {
                            tip = g.getName();
                        }
                        mapViewer.setToolTipText(tip);
                    }
                });

        // box selection
        final MouseAdapter ma =
                new MouseAdapter() {
                    private Point start = null;
                    private Point end = null;

                    public void mouseReleased(MouseEvent e) {
                        if (end == null || start == null) {
                            return;
                        }

                        final ArrayList<Geocache> list = getMapSelectedCaches(start, e.getPoint());
                        table.clearSelection();
                        addToTableSelection(list);

                        start = null;
                        end = null;
                        mapViewer.setPoints(null, null);
                    }

                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            start = e.getPoint();
                        } else {
                            start = null;
                        }
                    }

                    public void mouseDragged(MouseEvent e) {
                        if (start == null) {
                            return;
                        }

                        end = e.getPoint();
                        mapViewer.setPoints(start, end);
                    }
                };
        mapViewer.addMouseListener(ma);
        mapViewer.addMouseMotionListener(ma);

        final JPanel panelBar = new JPanel();
        getContentPane().add(panelBar, BorderLayout.SOUTH);
        panelBar.setLayout(new BorderLayout(0, 0));

        final JPanel panel = new JPanel();
        panelBar.add(panel, BorderLayout.EAST);
        panel.setLayout(new BorderLayout(0, 0));

        final JPanel panelCaches = new JPanel();
        panel.add(panelCaches, BorderLayout.NORTH);
        panelCaches.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        lblCacheCount = new JLabel("0");
        panelCaches.add(lblCacheCount);

        final JLabel lblCaches = new JLabel("Caches");
        panelCaches.add(lblCaches);

        final JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.SOUTH);
        panel_1.setLayout(new BorderLayout(10, 0));

        lblLblwaypointscount = new JLabel("0 Waypoints");
        lblLblwaypointscount.setHorizontalAlignment(SwingConstants.CENTER);
        lblLblwaypointscount.setFont(new Font("Dialog", Font.BOLD, 10));
        panel_1.add(lblLblwaypointscount, BorderLayout.NORTH);

        final JPanel panelSelected = new JPanel();
        panel_1.add(panelSelected, BorderLayout.SOUTH);
        panelSelected.setLayout(new BorderLayout(10, 0));
        panelSelected.setVisible(false);

        final JSeparator separator = new JSeparator();
        panelSelected.add(separator, BorderLayout.NORTH);

        final JPanel panel_4 = new JPanel();
        panelSelected.add(panel_4, BorderLayout.SOUTH);
        panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        final JLabel lblSelected = new JLabel("0");
        lblSelected.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel_4.add(lblSelected);

        final JLabel lblNewLabel_1 = new JLabel("Selected");
        lblNewLabel_1.setFont(new Font("Dialog", Font.PLAIN, 10));
        panel_4.add(lblNewLabel_1);

        table.getSelectionModel()
                .addListSelectionListener(
                        new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent e) {
                                final int selected = table.getSelectedRowCount();
                                if (selected == 0) {
                                    panelSelected.setVisible(false);
                                } else {
                                    lblSelected.setText(Integer.valueOf(selected).toString());
                                    panelSelected.setVisible(true);
                                }
                            }
                        });

        final JPanel panelButtons = new JPanel();
        panelBar.add(panelButtons, BorderLayout.WEST);

        final JToggleButton tglbtnList = new JToggleButton("List");
        tglbtnList.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        scrollPane.setVisible(tglbtnList.isSelected());
                        fixSplitPanes(splitPane1, splitPane2);
                    }
                });
        tglbtnList.setSelected(true);
        panelButtons.add(tglbtnList);

        final JToggleButton tglbtnMap = new JToggleButton("Map");
        tglbtnMap.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        panelMap.setVisible(tglbtnMap.isSelected());
                        fixSplitPanes(splitPane1, splitPane2);

                        SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        getMapViewer().setDisplayToFitMapMarkers();
                                    }
                                });
                    }
                });

        final JToggleButton tglbtnCache = new JToggleButton("Cache");
        tglbtnCache.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        panelCache.setVisible(tglbtnCache.isSelected());
                        fixSplitPanes(splitPane1, splitPane2);
                    }
                });
        panelButtons.add(tglbtnCache);
        panelButtons.add(tglbtnMap);

        table.addMouseListener(
                new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        popupPoint = e.getPoint();
                    }
                });

        final JPopupMenu jpm = new JPopupMenu();
        table.setComponentPopupMenu(jpm);

        final JMenuItem mnLocationDialog = new JMenuItem("Add as Location");
        mnLocationDialog.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final int row = table.rowAtPoint(popupPoint);
                        final CacheListModel.CLMTableModel model =
                                (CacheListModel.CLMTableModel) table.getModel();
                        final Geocache g = model.getObject(table.convertRowIndexToModel(row));
                        ldi.openDialog(g);
                    }
                });
        jpm.add(mnLocationDialog);

        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('C', Compatibility.SHORTCUT_KEY_MASK));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('V', Compatibility.SHORTCUT_KEY_MASK));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('X', Compatibility.SHORTCUT_KEY_MASK));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('A', Compatibility.SHORTCUT_KEY_MASK));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('I', Compatibility.SHORTCUT_KEY_MASK));
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .getParent()
                .remove(KeyStroke.getKeyStroke('Z', Compatibility.SHORTCUT_KEY_MASK));
    }

    public void updateCachePanelToSelection() {
        final CacheListModel.CLMTableModel model = (CacheListModel.CLMTableModel) table.getModel();
        if (table.getSelectedRows().length == 1) {
            final Geocache g =
                    model.getObject(table.convertRowIndexToModel(table.getSelectedRow()));
            panelCache.setCache(g);
        }
        if (table.getSelectedRows().length == 0) {
            panelCache.setCache(null);
        }
    }

    private boolean doNotUpdateMakers = false;

    public void updateMapMarkers() {
        if (doNotUpdateMakers) {
            return;
        }

        mapViewer.removeAllMapMarkers();

        final CacheListModel.CLMTableModel tableModel =
                (CacheListModel.CLMTableModel) table.getModel();
        if (table.getSelectedRows().length > 0) {
            for (final int selection : table.getSelectedRows()) {
                final Geocache g = tableModel.getObject(table.convertRowIndexToModel(selection));
                addMapMarker(g);
            }
        } else {
            for (final Geocache g : clc.getModel().getList()) {
                addMapMarker(g);
            }
        }

        mapViewer.setDisplayToFitMapMarkers();
    }

    private void addMapMarker(Geocache g) {
        final MapMarkerDot mmd = new MapMarkerCache(g);
        mapViewer.addMapMarker(mmd);
    }

    private class MapMarkerCache extends MapMarkerDot {
        private Geocache g;

        public MapMarkerCache(Geocache g) {
            super(
                    new org.openstreetmap.gui.jmapviewer.Coordinate(
                            g.getCoordinate().getLat(), g.getCoordinate().getLon()));
            this.g = g;

            setName("");

            if (g.getType() == GeocacheType.getTradiType()) {
                setColor(new Color(0x009900));
            } else if (g.getType() == GeocacheType.getMultiType()) {
                setColor(new Color(0xFFCC00));
            } else if (g.getType() == GeocacheType.getMysteryType()) {
                setColor(new Color(0x0066FF));
            } else {
                setColor(Color.GRAY);
            }
        }

        public void setColor(Color c) {
            super.setColor(Color.BLACK);
            super.setBackColor(c);
        }

        public Geocache getCache() {
            return g;
        }
    }

    private ArrayList<Geocache> getMapSelectedCaches(Point p1, Point p2) {
        final ArrayList<Geocache> list = new ArrayList<>();
        if (p1 == null || p2 == null) {
            return list;
        }

        final int x1 = p1.x < p2.x ? p1.x : p2.x;
        final int x2 = p1.x >= p2.x ? p1.x : p2.x;
        final int y1 = p1.y < p2.y ? p1.y : p2.y;
        final int y2 = p1.y >= p2.y ? p1.y : p2.y;

        for (final MapMarker mm : mapViewer.getMapMarkerList()) {
            final MapMarkerCache mmc = (MapMarkerCache) mm;
            final Point makerPos = mapViewer.getMapPosition(mm.getLat(), mm.getLon());

            if (makerPos != null
                    && makerPos.x >= x1
                    && makerPos.x <= x2
                    && makerPos.y >= y1
                    && makerPos.y <= y2) {
                list.add(mmc.getCache());
            }
        }
        return list;
    }

    public void addToTableSelection(Geocache g) {
        final ArrayList<Geocache> list = new ArrayList<>();
        list.add(g);
        addToTableSelection(list);
    }

    public void addToTableSelection(final ArrayList<Geocache> list_in) {
        doNotUpdateMakers = true;

        final LinkedList<Geocache> list = new LinkedList<>();
        list.addAll(list_in);

        final CacheListModel.CLMTableModel tableModel =
                (CacheListModel.CLMTableModel) table.getModel();
        for (int i = 0; !list_in.isEmpty() && i < table.getRowCount(); i++) {
            final Geocache gTable = tableModel.getObject(table.convertRowIndexToModel(i));

            Iterator<Geocache> it = list.iterator();
            while (it.hasNext()) {
                final Geocache g = it.next();
                if (gTable == g) {
                    table.addRowSelectionInterval(i, i); // slow -> disablUpdateMakers
                    it.remove();
                    break;
                }
            }
        }

        doNotUpdateMakers = false;
        updateMapMarkers();
    }

    private void addRowSelectionInterval(int i1, int i2) {
        if (i1 > i2) {
            return;
        }

        table.addRowSelectionInterval(i1, i2);
    }

    public void invertTableSelection() {
        doNotUpdateMakers = true;

        if (table.getSelectedRowCount() == 0) {
            table.selectAll();
        } else {
            final int selection[] = table.getSelectedRows();
            table.clearSelection();

            addRowSelectionInterval(0, selection[0] - 1); // preceding rows
            for (int i = 0; i < selection.length - 1; i++) {
                addRowSelectionInterval(selection[i] + 1, selection[i + 1] - 1);
            }
            // proceding rows
            addRowSelectionInterval(selection[selection.length - 1] + 1, table.getRowCount() - 1);
        }

        doNotUpdateMakers = false;
        updateMapMarkers();
    }

    private Geocache getMapFocusedCache(Point p) {
        final int X = p.x + 3;
        final int Y = p.y + 3;
        final java.util.List<MapMarker> ar = mapViewer.getMapMarkerList();

        Iterator<MapMarker> i = ar.iterator();
        while (i.hasNext()) {
            final MapMarkerCache mapMarker = (MapMarkerCache) i.next();

            final Point MarkerPosition =
                    mapViewer.getMapPosition(mapMarker.getLat(), mapMarker.getLon());
            if (MarkerPosition != null) {
                final int centerX = MarkerPosition.x;
                final int centerY = MarkerPosition.y;

                // calculate the radius from the touch to the center of the dot
                final double radCircle =
                        Math.sqrt(
                                (((centerX - X) * (centerX - X)) + (centerY - Y) * (centerY - Y)));

                if (radCircle < 10) {
                    return mapMarker.getCache();
                }
            }
        }

        return null;
    }

    public ArrayList<Geocache> getSelectedCaches() {
        final CacheListModel.CLMTableModel model = (CacheListModel.CLMTableModel) table.getModel();
        final ArrayList<Geocache> selected = new ArrayList<>();
        for (int row : table.getSelectedRows()) {
            final Geocache g = model.getObject(table.convertRowIndexToModel(row));
            selected.add(g);
        }
        return selected;
    }

    public void resetView() {
        updateTableView();
        panelCache.setCache(null);
    }

    public void updateTableView() {
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
    }

    public static void fixSplitPanes(JSplitPane pane1, JSplitPane pane2) {
        if (fixSplitPane(pane2, 0.5)) {
            fixSplitPane(pane1, 0.3);
        } else {
            fixSplitPane(pane1, 0.5);
        }
    }

    public static boolean fixSplitPane(JSplitPane pane, double dividerLocation) {
        boolean retVal;
        pane.setVisible(
                pane.getLeftComponent().isVisible() || pane.getRightComponent().isVisible());
        if (pane.getLeftComponent().isVisible() && pane.getRightComponent().isVisible()) {
            pane.setDividerSize(new JSplitPane().getDividerSize());
            pane.setDividerLocation(dividerLocation);
            retVal = true;
        } else {
            pane.setDividerSize(0);
            retVal = false;
        }

        pane.revalidate();
        pane.repaint();
        return retVal;
    }

    public void setCacheCount(Integer count) {
        lblCacheCount.setText(count.toString());
    }

    public void setWaypointCount(Integer count, Integer orphans) {
        String text = count.toString() + " Waypoints";
        if (orphans > 0) {
            text = text + " (" + orphans.toString() + " Orphans)";
        }
        lblLblwaypointscount.setText(text);
    }

    public void addFilter(final CacheListFilterModel filter) {
        filter.addRemoveAction(
                new Runnable() {
                    public void run() {
                        clc.removeFilter(filter);
                    }
                });
        filter.addRunOnFilterUpdate(
                new Runnable() {
                    public void run() {
                        clc.filtersUpdated();
                    }
                });

        panelFilters.add(filter);
        panelFilters.setVisible(true);
        panelFilters.revalidate();
    }

    public void tableSelectAllNone() {
        if (table.getSelectedRowCount() == table.getRowCount()) {
            table.clearSelection();
        } else {
            table.selectAll();
        }
    }

    public JTable getTable() {
        return table;
    }

    public JLabel getLblCacheCount() {
        return lblCacheCount;
    }

    public JMapViewer getMapViewer() {
        return mapViewer;
    }

    public interface RunLocationDialogI {
        public void openDialog(Geocache g);
    }
}
