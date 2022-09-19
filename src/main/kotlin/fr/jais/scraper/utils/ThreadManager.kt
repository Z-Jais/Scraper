package fr.jais.scraper.utils

import java.util.logging.Level

object ThreadManager {
    private val threads = mutableListOf<Thread>()

    fun start(runnable: Runnable) {
        val thread = Thread(runnable)
        thread.isDaemon = false
        thread.start()
        threads.add(thread)
    }

    fun stopAll() {
        threads.forEach {
            try {
                it.interrupt()
            } catch (e: Exception) {
                Logger.log(Level.SEVERE, "Error while stopping thread ${it.name}", e)
            }
        }
    }
}
