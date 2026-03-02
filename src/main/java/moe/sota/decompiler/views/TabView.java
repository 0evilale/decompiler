package moe.sota.decompiler.views;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import moe.sota.decompiler.controllers.TabController;
import moe.sota.decompiler.models.FileModel;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

@Getter
public class TabView extends JPanel implements MouseWheelListener, SearchListener {

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final FindToolBar findToolBar;
    private final ReplaceToolBar replaceToolBar;
    @Setter
    private TabController controller;
    private final FileModel fileModel;

    @SneakyThrows
    public TabView(FileModel fileModel) {
        this.fileModel = fileModel;
        setLayout(new BorderLayout());

        Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream("themes/RSyntaxTheme.xml"));

        textArea = new RSyntaxTextArea();
        theme.apply(textArea);
        textArea.addMouseWheelListener(this);
        textArea.setBracketMatchingEnabled(false);
        textArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        textArea.setDropTarget(null);
        textArea.setEditable(false);
        textArea.setHighlightCurrentLine(false);
        textArea.setMarkOccurrences(true);
        textArea.setMarkOccurrencesDelay(300);

        scrollPane = new RTextScrollPane(textArea);
        theme.apply(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        findToolBar = new FindToolBar(this);
        findToolBar.setVisible(false);
        findToolBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                textArea.requestFocusInWindow();
            }
        });

        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setVisible(false);
        replaceToolBar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                textArea.requestFocusInWindow();
            }
        });

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(findToolBar);
        southPanel.add(replaceToolBar);
        add(southPanel, BorderLayout.SOUTH);

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        textArea.getInputMap().put(escape, "hideSearchBars");
        textArea.getActionMap().put("hideSearchBars", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                hideSearchBars();
            }
        });

        setFontSize(getFont().getSize() + 2);
    }

    public void showFind() {
        replaceToolBar.setVisible(false);
        String selected = textArea.getSelectedText();
        if (selected != null && !selected.isEmpty())
            findToolBar.setSearchContext(new SearchContext(selected));
        findToolBar.setVisible(true);
        revalidate();
        SwingUtilities.invokeLater(findToolBar::requestFocusInWindow);
    }

    public void showReplace() {
        findToolBar.setVisible(false);
        String selected = textArea.getSelectedText();
        if (selected != null && !selected.isEmpty())
            replaceToolBar.setSearchContext(new SearchContext(selected));
        replaceToolBar.setVisible(true);
        revalidate();
        SwingUtilities.invokeLater(replaceToolBar::requestFocusInWindow);
    }

    public void hideSearchBars() {
        findToolBar.setVisible(false);
        replaceToolBar.setVisible(false);
        revalidate();
        textArea.requestFocusInWindow();
    }

    public int getLineCount() {
        return textArea.getLineCount();
    }

    public void scrollToLine(int line) {
        try {
            int pos = textArea.getLineStartOffset(line - 1);
            textArea.setCaretPosition(pos);
            textArea.requestFocusInWindow();
        } catch (BadLocationException e) {
            // line out of range, ignore
        }
    }

    @Override
    public String getSelectedText() {
        return textArea.getSelectedText();
    }

    @Override
    public void searchEvent(@NotNull SearchEvent e) {
        SearchContext context = e.getSearchContext();
        switch (e.getType()) {
            case MARK_ALL -> SearchEngine.markAll(textArea, context);
            case FIND -> SearchEngine.find(textArea, context);
            case REPLACE -> {
                textArea.setEditable(true);
                SearchEngine.replace(textArea, context);
                textArea.setEditable(false);
            }
            case REPLACE_ALL -> {
                textArea.setEditable(true);
                SearchEngine.replaceAll(textArea, context);
                textArea.setEditable(false);
            }
        }
    }

    // TODO: global font size
    @Override
    public void mouseWheelMoved(@NotNull MouseWheelEvent event) {
        if (event.isControlDown() || event.isMetaDown())
            setFontSize(Math.min(50, Math.max(10, textArea.getFont().getSize() - event.getWheelRotation())));
        else
            for (MouseWheelListener listener : scrollPane.getMouseWheelListeners())
                listener.mouseWheelMoved(event);
    }

    private void setFontSize(float size) {
        Font font = textArea.getFont().deriveFont(size);
        textArea.setFont(font);
        scrollPane.getGutter().setLineNumberFont(font);
    }

    public void setScrollPane(JScrollPane imageScrollPane) {
        setLayout(new BorderLayout());
        removeAll();

        JLabel imageLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon(fileModel.getBytes());
        imageLabel.setIcon(originalIcon);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);

        imageScrollPane.setViewportView(imageLabel);
        imageScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        imageScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        imageScrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {

                int height = getHeight();
                int width = (int) (originalIcon.getIconWidth() * ((double) height / originalIcon.getIconHeight()));

                if (width > getWidth()) {
                    width = getWidth();
                    height = (int) (originalIcon.getIconHeight() * ((double) width / originalIcon.getIconWidth()));
                }

                // Verify if the image is bigger than the scroll pane
                if (originalIcon.getIconWidth() > width || originalIcon.getIconHeight() > height) {
                    Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                    imageLabel.setPreferredSize(new Dimension(width, height));
                } else {
                    imageLabel.setIcon(originalIcon);
                    imageLabel.setPreferredSize(new Dimension(originalIcon.getIconWidth(), originalIcon.getIconHeight()));
                }
            }
        });

        add(imageScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

}
