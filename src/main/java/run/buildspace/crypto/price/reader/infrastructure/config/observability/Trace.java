package run.buildspace.crypto.price.reader.infrastructure.config.observability;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Consumer;

@UtilityClass
public class Trace {


    public static <T> void trace(Consumer<T> action, T arg1) {
        MDC.put("traceId", UUID.randomUUID().toString());
        try {
            action.accept(arg1);
        } finally {
            MDC.clear();
        }
    }

    public static <T, U, V> void trace(TriConsumer<T, U, V> action, T arg1, U arg2, V arg3) {
        MDC.put("traceId", UUID.randomUUID().toString());
        try {
            action.accept(arg1, arg2, arg3);
        } finally {
            MDC.clear();
        }
    }
}
