package com.dabwish.dabwish.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

private val logger = KotlinLogging.logger {}

object TransactionUtil {

    fun afterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    try {
                        action()
                    } catch (e: Exception) {
                        logger.error("Error executing afterCommit action", e)
                    }
                }
            })
        } else {
            logger.warn("No active transaction found for afterCommit, executing immediately.")
            action()
        }
    }

    fun afterRollback(action: () -> Unit) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCompletion(status: Int) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        try {
                            logger.info("Transaction rolled back. Executing compensating action.")
                            action()
                        } catch (e: Exception) {
                            logger.error("Error executing afterRollback compensating action", e)
                        }
                    }
                }
            })
        } else {
            logger.warn("afterRollback called without active transaction. Action ignored.")
        }
    }
}