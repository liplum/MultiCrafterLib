package net.liplum.multicraft;

public class MulticrafterRecipeAnalyzerException extends RuntimeException{
    public MulticrafterRecipeAnalyzerException() {
        super();
    }

    public MulticrafterRecipeAnalyzerException(String message) {
        super(message);
    }

    public MulticrafterRecipeAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MulticrafterRecipeAnalyzerException(Throwable cause) {
        super(cause);
    }

    protected MulticrafterRecipeAnalyzerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
