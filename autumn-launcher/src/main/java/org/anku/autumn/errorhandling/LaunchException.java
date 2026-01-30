package org.anku.autumn.errorhandling;

import java.io.IOException;

public class LaunchException extends RuntimeException {

    public LaunchException(IOException e) {
        super(e);
    }
}
