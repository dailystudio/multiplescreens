package com.dailystudio.multiplescreens.ui

enum class DrawState {
    Exit,
    Connect,
    Start,
    Stop,
    Resume,
    Pause,
}

open class DrawAction(val state: DrawState)

open class DrawConnectAction(val sid: String): DrawAction(DrawState.Connect)
open class DrawStartAction: DrawAction(DrawState.Start)
open class DrawStopAction: DrawAction(DrawState.Stop)
open class DrawResumeAction: DrawAction(DrawState.Resume)
open class DrawPauseAction: DrawAction(DrawState.Pause)
open class DrawExitAction: DrawAction(DrawState.Exit)
