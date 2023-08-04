package org.example.ioc;


import jakarta.annotation.Nullable;

import java.util.Objects;

public class ApplicationContextUtils {
    // static 保存applicationContext 保证随时获取
    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getRequiredApplicationContext(){
        return Objects.requireNonNull(getApplicationContext(), "context not set");
    }

    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtils.applicationContext = applicationContext;
    }
}
