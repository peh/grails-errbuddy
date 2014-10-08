package errbuddy

import grails.util.Environment
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent

class ErrbuddyLogAppender extends AppenderSkeleton {

    def application
    ErrbuddyService service
    boolean initialized = false
    boolean exceptionsOnly
    private String exceptionBucket
    private String logBucket

    @Override
    protected void append(LoggingEvent event) {

        //this should never throw anything otherwise we would end in a deadlock
        try {
            if (!initialized) {
                init()
            }

            ErrbuddyPutObject putObject

            if (event.throwableInformation || event.level.isGreaterOrEqual(Level.ERROR)) {
                putObject = new ErrbuddyErrorObject(
                        bucket: exceptionBucket,
                        message: event.message,
                        level: parseLevel(event.level),
                )
                if (event.throwableInformation) {
                    def throwable = event.throwableInformation.throwable
                    putObject.message = putObject.message ?: throwable.message
                    putObject.exception = throwable.class.canonicalName
                    putObject.stackTrace = new ArrayList(throwable.stackTrace.length + 1)
                    putObject.stackTrace << "${throwable.class}: $throwable.message"
                    throwable.stackTrace.each { putObject.stackTrace << it.toString() }
                }
            } else if (!event.throwableInformation && !exceptionsOnly) {
                putObject = new ErrbuddyLogObject(bucket: logBucket, message: event.message, level: parseLevel(event.level))
            }

            if (putObject) {
                service.put(putObject)
            }
        } catch (Throwable e) {
            if (Environment.developmentMode) {
                println(e.message)
            }
        }
    }

    void close() {

    }

    boolean requiresLayout() {
        return false
    }

    private void init() {
        def conf = application.config.grails.plugin.errbuddy
        Level level = Level.ERROR
        try {
            level = Level.toLevel(conf.threshold.toString())
        } catch (e) {
            println("$conf.threshold can not be parsed to a logging level, please review your configuration, defaulting to ERROR")
        }
        setThreshold(level)
        exceptionBucket = conf.buckets.error ?: 'error'
        logBucket = conf.buckets.log ?: 'log'
        exceptionsOnly = conf.exceptionsOnly == null ? true : conf.exceptionsOnly as boolean
        if (!exceptionsOnly && !logBucket) {
            println("ErrbuddyLogAppender.init() ignoring exceptionsOnly=false since no logBucket is defined")
            exceptionsOnly = true
        }
        if (exceptionBucket) {
            initialized = true
        }
    }

    private static String parseLevel(Level level) {
        switch (level) {
            case Level.FATAL: return 'FATAL'
            case Level.ERROR: return 'ERROR'
            case Level.WARN: return 'WARN'
            default: return 'LOG'
        }
    }
}
