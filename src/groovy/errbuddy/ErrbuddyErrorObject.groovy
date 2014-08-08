package errbuddy

class ErrbuddyErrorObject extends ErrbuddyPutObject {

    String exception
    List stackTrace = []
    String level

    private static final String TYPE = 'ERROR'

    @Override
    protected String getType() {
        TYPE
    }

    @Override
    protected Map getPostBodyExtension() {
        [
                exception : exception,
                level     : level,
                stacktrace: stackTrace
        ]
    }

}
