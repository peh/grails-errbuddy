package errbuddy

class ErrbuddyPerfomanceObject extends ErrbuddyPutObject{

    long runtime
    long start
    long end


    private static final String TYPE = 'PERFORMANCE'

    @Override
    protected String getType() {
        TYPE
    }

    @Override
    protected Map getPostBodyExtension() {
        [
                runtime: runtime,
                start: start,
                end: end
        ]
    }

}
