package net.openhft.chronicle.wire;

/*
 * Created by Peter Lawrey on 05/06/2017.
 */
public interface MethodFilter {
    boolean shouldHandleMessage(String method, Object firstArg);
}
