package moe.sota.decompiler.menus.search

import com.formdev.flatlaf.extras.components.FlatMenuItem
import moe.sota.decompiler.controllers.WindowController
import moe.sota.decompiler.services.LanguageService
import moe.sota.decompiler.views.FindInArchiveDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

object SearchFindInArchive : FlatMenuItem(), ActionListener {

    private var dialog: FindInArchiveDialog? = null

    init {
        accelerator = KeyStroke.getKeyStroke(
            KeyEvent.VK_F,
            Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx or InputEvent.SHIFT_DOWN_MASK
        )
        mnemonic = KeyEvent.VK_I
        text = LanguageService.getTranslation("search.findInArchive")
        addActionListener(this)
    }

    override fun actionPerformed(p0: ActionEvent?) {
        if (dialog == null || !dialog!!.isDisplayable)
            dialog = FindInArchiveDialog(WindowController.view as Frame)
        dialog!!.isVisible = true
        dialog!!.toFront()
    }

}
