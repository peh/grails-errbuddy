package grails.plugins.errbuddy

abstract class ErrbuddyPutObject {

    long time
    Type type
    String message
    String hostname

    String actionName
    String controllerName
    String serviceName
    String path
    String identifier

    Map<String, String> requestParameters
    Map<String, String> sessionParameters

    protected abstract String getType()

    protected abstract Map getPostBodyExtension()

    Map getPostBody() {
        [
                identifier       : identifier,
                time             : time,
                message          : message,
                controller       : controllerName,
                action           : actionName,
                service          : serviceName,
                path             : path,
                hostname         : hostname,
                requestParameters: requestParameters,
                sessionParameters: sessionParameters,
                type             : type.name()
        ] + postBodyExtension
    }

    static enum Type {
        ERROR,
        LOG,
        PERFORMANCE
    }
}
