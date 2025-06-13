package cn.hfbin.auth.context;

import cn.hfbin.common.token.model.JwtUserInfo;

public class UserContext {
    private static final ThreadLocal<JwtUserInfo> userHolder = new ThreadLocal<>();

    public static void setUser(JwtUserInfo user) {
        userHolder.set(user);
    }

    public static JwtUserInfo getUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}
