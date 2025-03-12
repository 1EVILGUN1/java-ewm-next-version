package ewm.stats.error.expection;

public class BadRequestExceptions extends RuntimeException {
    public BadRequestExceptions(String message) {
        super(message);
    }
}
