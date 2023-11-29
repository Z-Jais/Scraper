package fr.jais.scraper.utils

object ThreadManager {
    private val threads = mutableListOf<Thread>()

    fun start(name: String? = null, runnable: Runnable) {
        val thread = Thread(runnable)
        thread.name = name ?: "Thread-${threads.size}"
        thread.isDaemon = false
        thread.start()
        threads.add(thread)
    }
}
