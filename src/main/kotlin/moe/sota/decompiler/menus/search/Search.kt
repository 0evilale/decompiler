package moe.sota.decompiler.menus.search

import com.formdev.flatlaf.extras.components.FlatMenu
import moe.sota.decompiler.services.LanguageService

class Search : FlatMenu() {

    init {
        text = LanguageService.getTranslation("search")
        add(SearchFind)
        add(SearchReplace)
        add(SearchGoToLine)
        addSeparator()
        add(SearchFindInArchive)
    }

}
