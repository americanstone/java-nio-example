package client;

import java.io.Serializable;

public interface Command<T> extends Serializable {
    Serializable execute(T data);
}