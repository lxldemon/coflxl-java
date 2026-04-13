package com.coflxl.api.core.datasource;

public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void set(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }

    public static String get() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
