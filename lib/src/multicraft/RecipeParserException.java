package multicraft;

public class RecipeParserException extends RuntimeException{
    public RecipeParserException() {
        super();
    }

    public RecipeParserException(String message) {
        super(message);
    }

    public RecipeParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipeParserException(Throwable cause) {
        super(cause);
    }

    protected RecipeParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
