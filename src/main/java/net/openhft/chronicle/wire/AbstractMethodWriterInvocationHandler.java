/*
 * Copyright 2016 higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.wire;

import net.openhft.chronicle.core.util.AbstractInvocationHandler;
import net.openhft.chronicle.core.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by Peter Lawrey on 25/03/16.
 */
public abstract class AbstractMethodWriterInvocationHandler extends AbstractInvocationHandler implements MethodWriterInvocationHandler {
    private final Map<Method, Class[]> parameterMap = new ConcurrentHashMap<>();

    protected boolean recordHistory;
    protected String genericEvent = "";
    private MethodWriterListener methodWriterListener;
    // Note the Object[] passed in creates an object on every call.


    public AbstractMethodWriterInvocationHandler() {
        super(HashMap::new);
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) {
        if (methodWriterListener != null)
            methodWriterListener.onWrite(method.getName(), args);

        handleInvoke(method, args);
        return ObjectUtils.defaultValue(method.getReturnType());
    }

    @Override
    public void genericEvent(String genericEvent) {
        this.genericEvent = genericEvent;
    }

    protected abstract void handleInvoke(Method method, Object[] args);

    protected void handleInvoke(@NotNull Method method, Object[] args, Wire wire) {
        if (recordHistory) {
            wire.write(MethodReader.HISTORY)
                    .marshallable(MessageHistory.get());
        }
        String methodName = method.getName();
        if (methodName.equals(genericEvent)) {
            writeGenericEvent(wire, method, args);
            return;
        }
        writeEvent(wire, method, methodName, args);
    }

    private void writeEvent(Wire wire, @NotNull Method method, String methodName, Object[] args) {
        ValueOut valueOut = wire.writeEventName(methodName);
        Class[] parameterTypes = parameterMap.computeIfAbsent(method, Method::getParameterTypes);
        switch (parameterTypes.length) {
            case 0:
                valueOut.text("");
                break;
            case 1:
                valueOut.object(parameterTypes[0], args[0]);
                break;
            default:
                final Class[] finalParameterTypes = parameterTypes;
                valueOut.sequence(args, (a, v) -> {
                    for (int i = 0; i < finalParameterTypes.length; i++)
                        v.object(finalParameterTypes[i], a[i]);
                });
        }
    }

    private void writeGenericEvent(Wire wire, @NotNull Method method, Object[] args) {
        ValueOut valueOut = wire.writeEventName(args[0].toString());
        Class[] parameterTypes = parameterMap.computeIfAbsent(method, Method::getParameterTypes);
        switch (parameterTypes.length) {
            case 1:
                valueOut.text("");
                break;
            case 2:
                valueOut.object(parameterTypes[1], args[1]);
                break;
            default:
                final Class[] finalParameterTypes = parameterTypes;
                valueOut.sequence(args, (a, v) -> {
                    for (int i = 1; i < finalParameterTypes.length; i++)
                        v.object(finalParameterTypes[i], a[i]);
                });
        }
    }

    @Override
    public void recordHistory(boolean recordHistory) {
        this.recordHistory = recordHistory;
    }

    @Override
    public void methodWriterListener(MethodWriterListener methodWriterListener) {
        this.methodWriterListener = methodWriterListener;
    }
}
