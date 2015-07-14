package errbuddy

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

    def request

    Map requestParameters
    Map sessionParameters

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
                type             : type
        ] + postBodyExtension
    }

    public static enum Type {
        ERROR,
        LOG,
        PERFORMANCE
    }
}
