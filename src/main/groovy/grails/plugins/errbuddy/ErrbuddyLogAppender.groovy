package grails.plugins.errbuddy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.AppenderBase
import grails.core.GrailsApplication
import grails.util.Environment
import grails.util.Holders
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory

import java.lang.reflect.InvocationTargetException

class ErrbuddyLogAppender<E> extends AppenderBase<E> {

    GrailsApplication grailsApplication
    ErrbuddyService errbuddyService

    boolean enabled = false
    private static ErrbuddyLogAppender INSTANCE

    private ErrbuddyLogAppender(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        this.errbuddyService = grailsApplication.mainContext.getBean("errbuddyService")
    }

    static ErrbuddyLogAppender getInstance() {
        if (!INSTANCE) {
            INSTANCE = new ErrbuddyLogAppender(Holders.grailsApplication)
        }
        INSTANCE
    }

    private static String parseLevel(Level level) {
        switch (level) {
            case Level.ERROR: return 'ERROR'
            case Level.WARN: return 'WARN'
            default: return 'LOG'
        }
    }

    void enable() {
        if (!enabled) {
            Level level = Level.ERROR
            if (config.threshold.toString()) {
                try {
                    level = Level.toLevel(config.threshold.toString())
                } catch (e) {
                    println("$config.threshold can not be parsed to a logging level, please review your configuration, defaulting to ERROR")
                }
            }
            def errorFilter = new ThresholdFilter(level: level)
            errorFilter.start()
            this.addFilter(errorFilter)
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            this.context = LoggerFactory.getILoggerFactory()
            this.start()
            rootLogger.addAppender(this)
            enabled = true
        }
    }

    def getConfig() {
        grailsApplication.config.grails.plugin.errbuddy
    }

    @Override
    protected void append(E eventObject) {
        //this should never throw anything otherwise we would end in a deadlock
        try {
            ErrbuddyPutObject putObject

            if (eventObject.throwableProxy || eventObject.level.isGreaterOrEqual(Level.ERROR)) {
                putObject = new ErrbuddyErrorObject(
                        type: ErrbuddyPutObject.Type.ERROR,
                        message: eventObject.formattedMessage,
                        level: parseLevel(eventObject.level),
                        identifier: RandomStringUtils.randomAlphanumeric(32)
                )
                if (eventObject.throwableProxy) {
                    putObject.stackTrace = []
                    Throwable throwable = eventObject.throwableProxy.throwable
                    if (throwable instanceof InvocationTargetException) {
                        throwable = throwable.targetException
                        putObject.message = throwable.message
                        putObject.exception = throwable.class.canonicalName
                    } else {
                        putObject.stackTrace << "${throwable.class}: $throwable.message".toString()
                    }
                    throwable.stackTrace.each { putObject.stackTrace << it.toString() }
                }

            } else {
                putObject = new ErrbuddyLogObject(type: ErrbuddyPutObject.Type.LOG, message: eventObject.formattedMessage, level: parseLevel(eventObject.level))
            }

            if (putObject) {
                errbuddyService.put(putObject)
            }
        } catch (Throwable e) {
            if (Environment.developmentMode || config.errbuddy.throwError) {
                e.printStackTrace()
            }
        }
    }
}
