package com.agustin.tarati.game.core

/**
 * In Tarati the pieces are called "Cob" and the upgrades pieces are called "Roc"
 */
data class Cob(val color: Color, val isUpgraded: Boolean = false)