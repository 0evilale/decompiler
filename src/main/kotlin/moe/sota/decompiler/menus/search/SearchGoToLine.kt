package moe.sota.decompiler.menus.search

import com.formdev.flatlaf.extras.components.FlatMenuItem
import moe.sota.decompiler.controllers.TabsController
import moe.sota.decompiler.controllers.WindowController
import moe.sota.decompiler.services.LanguageService
import moe.sota.decompiler.views.TabView
import org.fife.rsta.ui.GoToDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

object SearchGoToLine : FlatMenuItem(), ActionListener {

    init {
        accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
        mnemonic = KeyEvent.VK_G
        text = LanguageService.getTranslation("search.goToLine")
        addActionListener(this)
    }

    override fun actionPerformed(p0: ActionEvent?) {
        val tab = TabsController.INSTANCE.view.selectedComponent as? TabView ?: return
        val dialog = GoToDialog(WindowController.view as Frame)
        dialog.maxLineNumberAllowed = tab.lineCount
        dialog.isVisible = true
        val line = dialog.lineNumber
        if (line > 0) tab.scrollToLine(line)
    }

}
