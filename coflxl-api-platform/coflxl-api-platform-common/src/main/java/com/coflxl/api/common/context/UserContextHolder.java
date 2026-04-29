package com.coflxl.api.common.context;

public class UserContextHolder {
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }

    public static void clear() {
        USERNAME_HOLDER.remove();
    }
}
