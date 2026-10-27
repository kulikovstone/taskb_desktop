package com.mkulikov.taskb.ui.icon

import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Add
import com.composables.icons.materialsymbols.outlined.Arrow_back
import com.composables.icons.materialsymbols.outlined.Arrow_downward
import com.composables.icons.materialsymbols.outlined.Check
import com.composables.icons.materialsymbols.outlined.Close
import com.composables.icons.materialsymbols.outlined.Delete
import com.composables.icons.materialsymbols.outlined.Filter_alt
import com.composables.icons.materialsymbols.outlined.Folder
import com.composables.icons.materialsymbols.outlined.Search
import com.composables.icons.materialsymbols.outlined.Today
import com.composables.icons.materialsymbols.outlined.Chevron_right
import com.composables.icons.materialsymbols.outlined.Chevron_left
import com.composables.icons.materialsymbols.outlined.Check_box_outline_blank
import com.composables.icons.materialsymbols.outlinedfilled.Folder
import com.composables.icons.materialsymbols.outlinedfilled.Cancel
import com.composables.icons.materialsymbols.outlinedfilled.Check_box

object TaskBIcons {
    val Back get() = MaterialSymbols.Outlined.Arrow_back
    val Add get() = MaterialSymbols.Outlined.Add
    val Done get() = MaterialSymbols.Outlined.Check
    val Clear get() = MaterialSymbols.Outlined.Close
    val Delete get() = MaterialSymbols.Outlined.Delete
    val Sort get() = MaterialSymbols.Outlined.Arrow_downward
    val Filter get() = MaterialSymbols.Outlined.Filter_alt
    val Folder get() = MaterialSymbols.Outlined.Folder
    val FolderFilled get() = MaterialSymbols.OutlinedFilled.Folder
    val Search get() = MaterialSymbols.Outlined.Search
    val Cancel get() = MaterialSymbols.OutlinedFilled.Cancel
    val Calendar get() = MaterialSymbols.Outlined.Today
    val ChevronRight get() = MaterialSymbols.Outlined.Chevron_right
    val ChevronLeft get() = MaterialSymbols.Outlined.Chevron_left
    val Checked get() = MaterialSymbols.OutlinedFilled.Check_box
    val Unchecked get() = MaterialSymbols.Outlined.Check_box_outline_blank
}
