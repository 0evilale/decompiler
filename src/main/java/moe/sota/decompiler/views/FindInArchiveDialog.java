package moe.sota.decompiler.views;

import moe.sota.decompiler.controllers.TabsController;
import moe.sota.decompiler.controllers.TreeController;
import moe.sota.decompiler.models.ArchiveModel;
import moe.sota.decompiler.services.ArchiveSearchService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FindInArchiveDialog extends JDialog {

    private final JTextField searchField;
    private final JCheckBox caseSensitiveCheck;
    private final JProgressBar progressBar;
    private final DefaultTableModel tableModel;
    private List<ArchiveSearchService.SearchMatch> lastResults;

    public FindInArchiveDialog(Frame parent) {
        super(parent, "Find in Archive", false);
        setSize(750, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(5, 5));

        searchField = new JTextField(28);
        caseSensitiveCheck = new JCheckBox("Case sensitive");
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> startSearch());
        searchField.addActionListener(e -> startSearch());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.add(new JLabel("Find:"));
        topPanel.add(searchField);
        topPanel.add(caseSensitiveCheck);
        topPanel.add(searchButton);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"File", "Line", "Text"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(450);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && lastResults != null) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        ArchiveSearchService.SearchMatch match = lastResults.get(row);
                        TabsController.getINSTANCE().addTabAndScrollTo(match.fileModel(), match.lineNumber());
                    }
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);
    }

    private void startSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        ArchiveModel archive = TreeController.getINSTANCE().getArchiveModel();
        if (archive == null) {
            JOptionPane.showMessageDialog(this, "No archive loaded.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        lastResults = null;
        progressBar.setValue(0);
        progressBar.setVisible(true);

        ArchiveSearchService.searchAsync(archive, query, caseSensitiveCheck.isSelected(),
                progress -> SwingUtilities.invokeLater(() -> progressBar.setValue(progress))
        ).thenAccept(results -> SwingUtilities.invokeLater(() -> {
            lastResults = results;
            for (ArchiveSearchService.SearchMatch match : results)
                tableModel.addRow(new Object[]{match.fileModel().getName(), match.lineNumber(), match.lineContent()});
            progressBar.setVisible(false);
        }));
    }

}
