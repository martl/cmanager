package cmanager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public abstract class CacheListFilterPanel extends JPanel {

    protected enum FILTER_TYPE {
        BETWEEN_ONE_AND_FIVE_FILTER_VALUE,
        SINGLE_FILTER_VALUE
    }

    private static final long serialVersionUID = -6181151635761995945L;

    private final CacheListFilterPanel THIS = this;
    private JComboBox<Double> leftComboBox;
    private JComboBox<Double> rightComboBox;
    private JLabel lblLeft;
    private JLabel lblRight;
    private final JButton btnRemove;

    protected boolean inverted = false;
    protected JPanel panel_1;
    private final JButton btnUpdate;
    private final JToggleButton tglbtnInvert;
    protected JPanel panel_2;
    protected JLabel lblLeft2;
    protected JTextField textField;
    private final JPanel panel_4;

    private final ArrayList<Runnable> runOnRemove = new ArrayList<>();
    protected Runnable runDoModelUpdateNow = null;
    protected final ArrayList<Runnable> runOnFilterUpdate = new ArrayList<>();

    /** Create the panel. */
    public CacheListFilterPanel(FILTER_TYPE filterType) {
        KeyAdapter keyEnterUpdate =
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent arg0) {
                        if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                            btnUpdate.doClick();
                        }
                    }
                };

        setLayout(new BorderLayout(0, 0));

        final JPanel panel = new JPanel();
        add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout(0, 0));

        final JPanel panelButtons = new JPanel();
        panel.add(panelButtons, BorderLayout.EAST);
        panelButtons.setLayout(new BorderLayout(0, 0));

        final JPanel panel_3 = new JPanel();
        panelButtons.add(panel_3);

        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        runDoModelUpdateNow.run();
                        for (Runnable action : runOnFilterUpdate) {
                            action.run();
                        }
                    }
                });

        tglbtnInvert = new JToggleButton("Invert");
        tglbtnInvert.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        inverted = tglbtnInvert.isSelected();
                        runDoModelUpdateNow.run();
                        for (Runnable action : runOnFilterUpdate) {
                            action.run();
                        }
                    }
                });

        btnRemove = new JButton("X");
        btnRemove.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        final Container parent = THIS.getParent();
                        parent.remove(THIS);
                        parent.revalidate();

                        for (Runnable action : runOnRemove) {
                            action.run();
                        }
                    }
                });

        panel_3.add(tglbtnInvert);
        panel_3.add(btnUpdate);
        panel_3.add(btnRemove);

        panel_4 = new JPanel();
        panel.add(panel_4, BorderLayout.CENTER);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));

        if (filterType == FILTER_TYPE.SINGLE_FILTER_VALUE) {
            panel_2 = new JPanel();
            panel_4.add(panel_2);
            panel_2.setLayout(new BorderLayout(5, 10));

            lblLeft2 = new JLabel("New label");
            panel_2.add(lblLeft2, BorderLayout.WEST);

            textField = new JTextField();
            panel_2.add(textField, BorderLayout.CENTER);
            textField.addKeyListener(keyEnterUpdate);
        } else if (filterType == FILTER_TYPE.BETWEEN_ONE_AND_FIVE_FILTER_VALUE) {
            final Double[] values = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0};
            panel_1 = new JPanel();
            panel_4.add(panel_1);
            panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

            final JPanel panelLeft = new JPanel();
            panel_1.add(panelLeft);
            panelLeft.setLayout(new BorderLayout(5, 0));

            lblLeft = new JLabel("Label");
            panelLeft.add(lblLeft, BorderLayout.WEST);

            leftComboBox = new JComboBox<Double>(values);
            leftComboBox.setMaximumRowCount(values.length);
            leftComboBox.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            runDoModelUpdateNow.run();
                            for (Runnable action : runOnFilterUpdate) {
                                action.run();
                            }
                        }
                    });
            panelLeft.add(leftComboBox, BorderLayout.EAST);

            final JPanel panelRight = new JPanel();
            panel_1.add(panelRight);
            panelRight.setLayout(new BorderLayout(5, 0));

            lblRight = new JLabel("Label");
            panelRight.add(lblRight, BorderLayout.WEST);

            rightComboBox = new JComboBox<Double>(values);
            rightComboBox.setMaximumRowCount(values.length);
            rightComboBox.setSelectedIndex(values.length - 1);
            rightComboBox.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            runDoModelUpdateNow.run();
                            for (Runnable action : runOnFilterUpdate) {
                                action.run();
                            }
                        }
                    });
            panelRight.add(rightComboBox, BorderLayout.EAST);
        }
    }

    public void addRunOnFilterUpdate(Runnable action) {
        runOnFilterUpdate.add(action);
    }

    public void addRemoveAction(Runnable action) {
        runOnRemove.add(action);
    }

    protected JLabel getLblLeft() {
        return lblLeft;
    }

    protected JLabel getLblRight() {
        return lblRight;
    }

    protected JButton getBtnRemove() {
        return btnRemove;
    }

    protected JButton getBtnUpdate() {
        return btnUpdate;
    }

    protected Double getValueRight() {
        return rightComboBox.getItemAt(rightComboBox.getSelectedIndex());
    }

    protected Double getValueLeft() {
        return leftComboBox.getItemAt(leftComboBox.getSelectedIndex());
    }
}
