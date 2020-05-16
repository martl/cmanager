package cmanager.gui;

import cmanager.ThreadStore;
import cmanager.geo.Coordinate;
import cmanager.geo.Geocache;
import cmanager.geo.Location;
import cmanager.geo.LocationList;
import cmanager.okapi.OKAPI;
import cmanager.okapi.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class LocationDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JPanel contentPanel = new JPanel();
    private final JTable table;
    private final JTextField txtName;
    private final JTextField txtLat;
    private final JTextField txtLon;

    public boolean modified = false;

    private final LocationDialog THIS = this;

    /** Create the dialog. */
    public LocationDialog(JFrame owner) {
        super(owner);

        setTitle("Locations");
        getContentPane().setLayout(new BorderLayout());

        final String columnNames[] = {"Name", "Lat", "Lon"};
        final String dataValues[][] = {};
        final DefaultTableModel dtm = new DefaultTableModel(dataValues, columnNames);

        try {
            final ArrayList<Location> locations = LocationList.getList().getLocations();
            for (final Location l : locations) {
                final String values[] = {l.getName(), l.getLat().toString(), l.getLon().toString()};
                dtm.addRow(values);
            }
        } catch (Exception e) {
            ExceptionPanel.showErrorDialog(this, e);
        }

        final JPanel panelMaster = new JPanel();
        panelMaster.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(panelMaster, BorderLayout.CENTER);
        panelMaster.setLayout(new BorderLayout(0, 0));

        final JPanel buttonPaneOutter = new JPanel();
        panelMaster.add(buttonPaneOutter, BorderLayout.SOUTH);
        buttonPaneOutter.setBorder(null);
        buttonPaneOutter.setLayout(new BorderLayout(0, 0));

        final JPanel buttonPaneOkCancel = new JPanel();
        buttonPaneOkCancel.setBorder(null);
        buttonPaneOutter.add(buttonPaneOkCancel, BorderLayout.SOUTH);
        buttonPaneOkCancel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        final JButton btnOK = new JButton("OK");
        btnOK.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            final ArrayList<Location> locations = new ArrayList<>();

                            final DefaultTableModel dtm = ((DefaultTableModel) table.getModel());
                            for (int i = 0; i < dtm.getRowCount(); i++) {
                                final Location l =
                                        new Location(
                                                (String) table.getValueAt(i, 0),
                                                Double.valueOf((String) table.getValueAt(i, 1)),
                                                Double.valueOf((String) table.getValueAt(i, 2)));
                                locations.add(l);
                            }

                            LocationList.getList().setLocations(locations);
                        } catch (Throwable t) {
                            ExceptionPanel.showErrorDialog(THIS, t);
                            return;
                        }

                        modified = true;
                        dispose();
                    }
                });
        buttonPaneOkCancel.add(btnOK);

        final JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
        buttonPaneOkCancel.add(btnCancel);

        final JPanel buttonPanelEdit = new JPanel();
        buttonPanelEdit.setBorder(new LineBorder(new Color(0, 0, 0)));
        buttonPaneOutter.add(buttonPanelEdit, BorderLayout.NORTH);
        buttonPanelEdit.setLayout(new BorderLayout(0, 0));

        final JPanel panelText = new JPanel();
        buttonPanelEdit.add(panelText, BorderLayout.NORTH);
        final GridBagLayout gbl_panelText = new GridBagLayout();
        gbl_panelText.columnWidths = new int[] {215, 215, 0};
        gbl_panelText.rowHeights = new int[] {19, 19, 19, 0};
        gbl_panelText.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
        gbl_panelText.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        panelText.setLayout(gbl_panelText);

        final JLabel lblNewLabel = new JLabel("Name:");
        final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        panelText.add(lblNewLabel, gbc_lblNewLabel);

        txtName = new JTextField();
        final GridBagConstraints gbc_txtName = new GridBagConstraints();
        gbc_txtName.fill = GridBagConstraints.BOTH;
        gbc_txtName.insets = new Insets(0, 0, 5, 0);
        gbc_txtName.gridx = 1;
        gbc_txtName.gridy = 0;
        panelText.add(txtName, gbc_txtName);
        txtName.setColumns(10);

        final JLabel lblNewLabel_1 = new JLabel("Lat:");
        final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 0;
        gbc_lblNewLabel_1.gridy = 1;
        panelText.add(lblNewLabel_1, gbc_lblNewLabel_1);

        txtLat = new JTextField();
        final GridBagConstraints gbc_txtLat = new GridBagConstraints();
        gbc_txtLat.fill = GridBagConstraints.BOTH;
        gbc_txtLat.insets = new Insets(0, 0, 5, 0);
        gbc_txtLat.gridx = 1;
        gbc_txtLat.gridy = 1;
        panelText.add(txtLat, gbc_txtLat);
        txtLat.setColumns(10);

        final JLabel lblNewLabel_2 = new JLabel("Lon:");
        final GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
        gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
        gbc_lblNewLabel_2.gridx = 0;
        gbc_lblNewLabel_2.gridy = 2;
        panelText.add(lblNewLabel_2, gbc_lblNewLabel_2);

        txtLon = new JTextField();
        final GridBagConstraints gbc_txtLon = new GridBagConstraints();
        gbc_txtLon.fill = GridBagConstraints.BOTH;
        gbc_txtLon.gridx = 1;
        gbc_txtLon.gridy = 2;
        panelText.add(txtLon, gbc_txtLon);
        txtLon.setColumns(10);

        final JPanel panelButton = new JPanel();
        buttonPanelEdit.add(panelButton, BorderLayout.SOUTH);

        final JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final int row = table.getSelectedRow();
                        if (row == -1) {
                            return;
                        }
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                    }
                });
        panelButton.add(btnRemove);

        final JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final int row = table.getSelectedRow();
                        if (row == -1) {
                            return;
                        }

                        try {
                            new Location(
                                    txtName.getText(),
                                    Double.valueOf(txtLat.getText()),
                                    Double.valueOf(txtLon.getText()));
                        } catch (Throwable t) {
                            ExceptionPanel.showErrorDialog(THIS, t);
                            return;
                        }

                        final DefaultTableModel dtm = ((DefaultTableModel) table.getModel());
                        dtm.setValueAt(txtName.getText(), row, 0);
                        dtm.setValueAt(txtLat.getText(), row, 1);
                        dtm.setValueAt(txtLon.getText(), row, 2);
                    }
                });
        panelButton.add(btnUpdate);

        final JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            new Location(
                                    txtName.getText(),
                                    Double.valueOf(txtLat.getText()),
                                    Double.valueOf(txtLon.getText()));
                        } catch (Throwable t) {
                            ExceptionPanel.showErrorDialog(THIS, t);
                            return;
                        }

                        final DefaultTableModel dtm = ((DefaultTableModel) table.getModel());
                        final String values[] = {
                            txtName.getText(), txtLat.getText(), txtLon.getText()
                        };
                        dtm.addRow(values);
                    }
                });
        panelButton.add(btnAdd);

        final JSeparator separator = new JSeparator();
        panelButton.add(separator);

        final JButton btnRetrieve = new JButton("OKAPI Coordinates");
        btnRetrieve.setEnabled(false);
        btnRetrieve.setFont(new Font("Dialog", Font.BOLD, 9));
        btnRetrieve.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final User user = User.getOKAPIUser();
                        try {
                            final Coordinate c = OKAPI.getHomeCoordinates(user);
                            txtName.setText("OKAPI Home Coordinate");
                            txtLat.setText(c.getLat().toString());
                            txtLon.setText(c.getLon().toString());
                        } catch (Exception ex) {
                            ExceptionPanel.showErrorDialog(THIS, ex);
                        }
                    }
                });
        panelButton.add(btnRetrieve);

        panelMaster.add(contentPanel);
        contentPanel.setLayout(new FlowLayout());
        contentPanel.setBorder(null);
        table = new JTable(dtm);
        table.getSelectionModel()
                .addListSelectionListener(
                        new ListSelectionListener() {
                            public void valueChanged(ListSelectionEvent arg0) {
                                final int row = table.getSelectedRow();
                                if (row == -1) {
                                    return;
                                }

                                txtName.setText((String) table.getValueAt(row, 0));
                                txtLat.setText((String) table.getValueAt(row, 1));
                                txtLon.setText((String) table.getValueAt(row, 2));
                            }
                        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);

        final JScrollPane scrollPane =
                new JScrollPane(
                        table,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane);

        pack();

        ThreadStore ts = new ThreadStore();
        ts.addAndRun(
                new Thread(
                        new Runnable() {
                            public void run() {
                                final User user = User.getOKAPIUser();
                                try {
                                    if (user.getOkapiToken() != null
                                            && OKAPI.getUUID(user) != null) {
                                        btnRetrieve.setEnabled(true);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }));
    }

    public void setGeocache(Geocache g) {
        txtName.setText(g.getName());
        txtLat.setText(g.getCoordinate().getLat().toString());
        txtLon.setText(g.getCoordinate().getLon().toString());
    }
}
