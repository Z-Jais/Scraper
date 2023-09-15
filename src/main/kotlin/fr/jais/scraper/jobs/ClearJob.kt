package fr.jais.scraper.jobs

import fr.jais.scraper.Scraper
import fr.jais.scraper.utils.Logger
import org.quartz.Job
import org.quartz.JobExecutionContext

class ClearJob : Job {
    override fun execute(p0: JobExecutionContext?) {
        Logger.info("Reset all platforms...")
        Scraper.instance.platforms.forEach { it.reset() }
    }
}
