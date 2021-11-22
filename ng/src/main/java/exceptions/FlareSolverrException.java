package exceptions;

import java.io.InterruptedIOException;

public class FlareSolverrException extends InterruptedIOException {
    private static final long serialVersionUID = 4100528060712500567L;

    public FlareSolverrException() {
        super();
    }
    
    public FlareSolverrException(String s) {
        super(s);
    }
}
