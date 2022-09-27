package fr.jais.scraper.utils

import java.util.logging.Level

object ThreadManager {
    private val threads = mutableListOf<Thread>()

    fun start(name: String? = null, runnable: Runnable) {
        val thread = Thread(runnable)
        thread.name = name ?: "Thread-${threads.size}"
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
