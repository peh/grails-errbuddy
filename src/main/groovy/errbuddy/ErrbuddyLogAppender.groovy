package errbuddy

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.core.AppenderBase
import grails.core.GrailsApplication
import grails.util.Environment
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory

class ErrbuddyLogAppender<E> extends AppenderBase<E> {

    GrailsApplication grailsApplication
    ErrbuddyService errbuddyService

    boolean enabled = false
    boolean exceptionsOnly

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
            try {
                level = Level.toLevel(conf.threshold.toString())
            } catch (e) {
                println("$config.threshold can not be parsed to a logging level, please review your configuration, defaulting to ERROR")
            }
            //        setThreshold(level)
            exceptionsOnly = config.errbuddy.exceptionsOnly == null ? true : config.errbuddy.exceptionsOnly as boolean
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
            this.context = LoggerFactory.getILoggerFactory()
            this.start()
            rootLogger.addAppender(this)
            enabled = true
        }
    }

    def getConfig() {
        grailsApplication.config.grails.plugins.errbuddy
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

                if (eventObject.throwableInformation) {
                    Throwable throwable = eventObject.throwableInformation.throwable
                    throwable.metaClass.errbuddyIdentifier = putObject.identifier
                    putObject.message = putObject.message ?: throwable.message
                    putObject.exception = throwable.class.canonicalName
                    putObject.stackTrace = new ArrayList(throwable.stackTrace.length + 1)
                    putObject.stackTrace << "${throwable.class}: $throwable.message"
                    throwable.stackTrace.each { putObject.stackTrace << it.toString() }
                }

            } else if (!eventObject.throwableProxy && !exceptionsOnly) {
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
