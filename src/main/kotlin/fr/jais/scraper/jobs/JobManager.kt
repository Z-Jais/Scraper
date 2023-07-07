package fr.jais.scraper.jobs

import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory

class JobManager {
    private val scheduler = StdSchedulerFactory().scheduler

    fun scheduleJob(cronExpression: String, jobClass: Class<out Job>) {
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobClass.simpleName)
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(jobClass.simpleName)
            .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    fun start() {
        scheduler.start()
    }

    fun shutdown() {
        scheduler.shutdown()
    }
}