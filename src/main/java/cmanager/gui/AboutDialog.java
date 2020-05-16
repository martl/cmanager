package cmanager.gui;

import cmanager.global.Constants;
import cmanager.global.Version;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JLayeredPane contentPanel = new JLayeredPane();

    private final AboutDialog THIS = this;

    /** Create the dialog. */
    public AboutDialog() {
        setTitle("About " + Constants.APP_NAME);

        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        final GridBagLayout gbl_contentPanel = new GridBagLayout();
        gbl_contentPanel.columnWidths = new int[] {0, 0};
        gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
        gbl_contentPanel.columnWeights = new double[] {1.0, Double.MIN_VALUE};
        gbl_contentPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        contentPanel.setLayout(gbl_contentPanel);

        final JLabel lblAppname = new JLabel(Constants.APP_NAME);
        lblAppname.setFont(new Font("Dialog", Font.BOLD, 15));
        final GridBagConstraints gbc_lblAppname = new GridBagConstraints();
        gbc_lblAppname.insets = new Insets(0, 0, 5, 0);
        gbc_lblAppname.anchor = GridBagConstraints.NORTH;
        gbc_lblAppname.gridx = 0;
        gbc_lblAppname.gridy = 0;
        contentPanel.add(lblAppname, gbc_lblAppname);

        final JLabel lblVersion = new JLabel(Version.VERSION);
        lblVersion.setFont(new Font("Dialog", Font.PLAIN, 12));
        final GridBagConstraints gbc_lblVersion = new GridBagConstraints();
        gbc_lblVersion.insets = new Insets(0, 0, 5, 0);
        gbc_lblVersion.gridx = 0;
        gbc_lblVersion.gridy = 1;
        contentPanel.add(lblVersion, gbc_lblVersion);

        final JLabel lblAuthor =
                new JLabel(
                        "<html>"
                                + "<b>Original code by</b> "
                                + "Samsung1 - jm@rq-project.net"
                                + "<br/>"
                                + "<b>Modifications by</b> "
                                + "FriedrichFr&ouml;bel"
                                + "</html>");
        lblAuthor.setFont(new Font("Dialog", Font.PLAIN, 12));
        final GridBagConstraints gbc_lblAuthor = new GridBagConstraints();
        gbc_lblAuthor.insets = new Insets(40, 0, 5, 0);
        gbc_lblAuthor.gridx = 0;
        gbc_lblAuthor.gridy = 3;
        contentPanel.add(lblAuthor, gbc_lblAuthor);

        final JLabel lblThanks =
                new JLabel("Special thanks to the great people at forum.opencaching.de.");
        lblThanks.setFont(new Font("Dialog", Font.PLAIN, 12));
        final GridBagConstraints gbc_lblThanks = new GridBagConstraints();
        gbc_lblThanks.insets = new Insets(80, 0, 5, 0);
        gbc_lblThanks.gridx = 0;
        gbc_lblThanks.gridy = 5;
        contentPanel.add(lblThanks, gbc_lblThanks);

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        final JButton btnClose = new JButton("Close");
        btnClose.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        THIS.setVisible(false);
                    }
                });
        btnClose.setActionCommand("OK");
        buttonPane.add(btnClose);
        getRootPane().setDefaultButton(btnClose);

        setResizable(false);
        super.pack();
    }
}
