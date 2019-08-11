package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

object RxJavaUtils {

    fun doTaskInBackGround(task:()->Unit){
        Observable.just(true)
                .subscribeOn(Schedulers.io())
                .map { task() }
                .subscribeWith(object : Observer<Unit>{
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(t: Unit) {}

                    override fun onError(e: Throwable) {
                        val session = DbSessionManager.getNewSession()
                        LoggerUtils.logError(e,session)
                        session.close()
                        e.printStackTrace()
                    }
                })
    }
}