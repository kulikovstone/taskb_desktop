package com.mkulikov.taskb.core.navigation

import androidx.navigation3.runtime.NavKey

interface IAppNavigator {
    fun navigate(key: NavKey)
    fun replaceTop(key: NavKey)
    fun popUpTo(key: NavKey, inclusive: Boolean = false)
    fun replaceAll(key: NavKey)
    fun back()
}
