package com.HrshD1eux.Scan.actions

import androidx.compose.ui.graphics.vector.ImageVector

data class Action(
    val label: String,
    val icon: ImageVector? = null,
    val isPrimary: Boolean = false,
    val execute: () -> Unit
)
