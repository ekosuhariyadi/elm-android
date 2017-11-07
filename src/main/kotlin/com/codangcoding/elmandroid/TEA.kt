package com.codangcoding.elmandroid

import com.codangcoding.elmandroid.AbstractCmd.Cmd
import com.codangcoding.elmandroid.AbstractMsg.Msg
import io.reactivex.Single

open class State

sealed class AbstractMsg {
    open class Msg : AbstractMsg()
    object Idle : Msg()
    object Init : Msg()
    class ErrorMsg(val error: Throwable, val cmd: Cmd) : Msg()
}

sealed class AbstractCmd {
    open class Cmd : AbstractCmd()
    object None : Cmd()
}

interface TEAComponent {

    fun update(msg: Msg, state: State): Pair<State, Cmd>

    fun view(state: State)

    fun call(cmd: Cmd): Single<Msg>
}

