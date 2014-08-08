package errbuddy

class ErrbuddyPerformanceFilters {

    def grailsApplication
    def errbuddyService

    def filters = {
        all(
                controller: '*',
                action: '*',
                controllerExclude: "${grailsApplication.config.grails.plugin.errbuddy.performance.excludes.controller}|assets",
                actionExclude: grailsApplication.config.grails.plugin.errbuddy.performance.excludes.action
        ) {
            before = {
                if (grailsApplication.config.grails.plugin.errbuddy.performance.enabled) {
                    session.errbuddyRequestStart = System.currentTimeMillis()
                }
            }
            after = { Map model ->
                if (session.errbuddyRequestStart) {
                    def end = System.currentTimeMillis()
                    errbuddyService.put(new ErrbuddyPerfomanceObject(
                            bucket: grailsApplication.config.grails.plugin.errbuddy.buckets.performance,
                            runtime: end - session.errbuddyRequestStart,
                            start: session.errbuddyRequestStart,
                            end: end
                    ))
                }
            }
            afterView = { Exception e ->

            }
        }
    }
}
