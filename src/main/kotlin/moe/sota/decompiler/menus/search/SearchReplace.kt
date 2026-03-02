package moe.sota.decompiler.menus.search

import com.formdev.flatlaf.extras.components.FlatMenuItem
import moe.sota.decompiler.controllers.TabsController
import moe.sota.decompiler.services.LanguageService
import moe.sota.decompiler.views.TabView
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

object SearchReplace : FlatMenuItem(), ActionListener {

    init {
        accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_H, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
        mnemonic = KeyEvent.VK_H
        text = LanguageService.getTranslation("search.replace")
        addActionListener(this)
    }

    override fun actionPerformed(p0: ActionEvent?) {
        (TabsController.INSTANCE.view.selectedComponent as? TabView)?.showReplace()
    }

}
