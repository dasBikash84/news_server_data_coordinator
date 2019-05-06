package com.dasbikash.news_server_data_coordinator.exceptions.handlers

import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.DataCoordinatorException
import com.dasbikash.news_server_data_coordinator.exceptions.HighestLevelException
import com.dasbikash.news_server_data_coordinator.exceptions.LowLevelException
import com.dasbikash.news_server_data_coordinator.exceptions.MediumLevelException
import com.dasbikash.news_server_data_coordinator.model.db_entity.ErrorLog
import com.dasbikash.news_server_data_coordinator.utils.EmailUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

object DataCoordinatorExceptionHandler {

    fun handleException(ex:DataCoordinatorException){
        Observable.just(ex)
                .subscribeOn(Schedulers.io())
                .map {
                    when(it){
                        is HighestLevelException    -> highestLevelExceptionHandler(it)
                        is MediumLevelException     -> mediumLevelExceptionHandler(it)
                        is LowLevelException        -> lowLevelExceptionHandler(it)
                        else                        -> dataCoordinatorExceptionHandler(it)
                    }
                }
                .doOnError {
                    handleException(DataCoordinatorException(it))
                }
                .subscribe()
    }

    private fun dataCoordinatorExceptionHandler(ex: DataCoordinatorException) {
        val session = DbSessionManager.getNewSession()
        LoggerUtils.logError(ex,session)
    }

    private fun lowLevelExceptionHandler(ex: DataCoordinatorException) {
        dataCoordinatorExceptionHandler(ex)
    }

    private fun mediumLevelExceptionHandler(ex: MediumLevelException) {
        dataCoordinatorExceptionHandler(ex)
    }

    private fun highestLevelExceptionHandler(ex: HighestLevelException) {
        EmailUtils.sendEmail("Major error on News-Server Data Coordinator App!!! Cause: ${ex.message ?: ex.cause}",ErrorLog(ex).toString())
        dataCoordinatorExceptionHandler(ex)
    }

}