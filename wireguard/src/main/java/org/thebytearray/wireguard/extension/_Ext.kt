package org.thebytearray.wireguard.extension




/**
 * TheByteArray
 *
 * @developer Tamim Hossain
 * @mail contact@thebytearray.org
 */

const val THRESHOLD = 1000L
const val DIVISOR = 1024.0

fun Long.toSpeedString(): String = this.toTrafficString() + "/s"

fun Long.toTrafficString(): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    var size = this.toDouble()
    var unitIndex = 0
    while (size >= THRESHOLD && unitIndex < units.size - 1) {
        size /= DIVISOR
        unitIndex++
    }
    return String.format("%.1f %s", size, units[unitIndex])
}
fun Long.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}