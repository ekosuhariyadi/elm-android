package com.codangcoding.elmandroid

import com.codangcoding.elmandroid.AbstractMsg.*
import com.codangcoding.elmandroid.AbstractCmd.*
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class Program(private val outputScheduler: Scheduler) {

    private val msgRelay: BehaviorRelay<Pair<Msg, State>> =
            BehaviorRelay.create()
    private lateinit var component: TEAComponent
    private lateinit var state: State

    fun init(initialState: State, component: TEAComponent): Disposable {
        state = initialState
        this.component = component

        return msgRelay
                .map { (msg, state) ->
                    component.update(msg, state)
                }
                .observeOn(outputScheduler)
                .map { (state, cmd) ->
                    this.state = state
                    component.view(state)
                    (state to cmd)
                }
                .filter { (_, cmd) -> cmd !is None }
                .observeOn(Schedulers.io())
                .flatMap { (_, cmd) ->
                    component
                            .call(cmd)
                            .onErrorResumeNext { error ->
                                Single.just(ErrorMsg(error, cmd))
                            }
                            .toObservable()
                }
                .subscribe { msg ->
                    when (msg) {
                        is Idle -> {
                            /** Intentionally do nothing */
                        }
                        else -> msgRelay.accept(msg to this.state)
                    }
                }
    }

    fun accept(msg: Msg) {
        msgRelay.accept(msg to state)
    }
}