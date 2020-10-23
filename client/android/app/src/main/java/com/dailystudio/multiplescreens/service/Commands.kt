package com.dailystudio.multiplescreens.service

import android.graphics.Rect

enum class CmdCode {
    REPORT_SCREEN_INFO,
    UPDATE_SCREEN_INFO,
    SYNC_GRIDS_MAP,
}

open class Command(val uuid: String,
                   val cmdCode: CmdCode) {

    override fun toString(): String {
        return buildString {
            append("[$uuid, cmd: $cmdCode]")
        }
    }

}

open class CmdUpdateScreenInfo(uuid: String,
                               val seq: Int,
                               val gridWidthInDp: Int,
                               val gridHeightInDp: Int,
                               val drawingBoundInDp: Rect?
): Command(uuid, CmdCode.REPORT_SCREEN_INFO) {

    override fun toString(): String {
        return buildString {
            append(super.toString())
            append(": seq = $seq, ")
            append("grid = [${gridWidthInDp}dp x ${gridHeightInDp}dp], ")
            append("drawingBoundInDp = $drawingBoundInDp")
        }
    }

}

open class CmdGridsMap(uuid: String,
                       val map: Array<Array<Int>>
): Command(uuid, CmdCode.SYNC_GRIDS_MAP) {

    override fun toString(): String {
        return buildString {
            append(super.toString())
            append(": map = $map")
        }
    }

}

open class CmdReportScreenInfo(uuid: String,
                               val widthInDp: Int,
                               val heightInDp: Int): Command(uuid, CmdCode.REPORT_SCREEN_INFO) {

    override fun toString(): String {
        return buildString {
            append(super.toString())
            append(": screen = [${widthInDp}dp x ${heightInDp}dp]")
        }
    }

}