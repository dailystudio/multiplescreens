package com.dailystudio.multiplescreens.service

enum class CmdCode {
    REPORT_SCREEN_INFO,
    UPDATE_SCREEN_INFO
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
                               val xOffsetInDp: Int,
                               val yOffsetInDp: Int
): Command(uuid, CmdCode.REPORT_SCREEN_INFO) {

    override fun toString(): String {
        return buildString {
            append(super.toString())
            append(": seq = $seq, ")
            append("grid = [${gridWidthInDp}dp x ${gridHeightInDp}dp], ")
            append("xOffset = ${xOffsetInDp}dp, ")
            append("yOffset = ${yOffsetInDp}dp")
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