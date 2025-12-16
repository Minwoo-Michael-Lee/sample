package com.neis.db;

import javax.servlet.ServletContext;

/**
 * DB 설정 로더.
 * 우선순위: 환경변수 > web.xml context-param > 기본값
 */
public final class DBConfig {
    private DBConfig() {}

    public static String getJdbcUrl(ServletContext ctx) {
        String env = System.getenv("NEIS_DB_URL");
        if (env != null && !env.isBlank()) return env;
        String init = ctx.getInitParameter("NEIS_DB_URL");
        if (init != null && !init.isBlank()) return init;
        return "jdbc:mariadb://localhost:3306/NEISDB?useUnicode=true&characterEncoding=UTF-8";
    }

    public static String getUser(ServletContext ctx) {
        String env = System.getenv("NEIS_DB_USER");
        if (env != null && !env.isBlank()) return env;
        String init = ctx.getInitParameter("NEIS_DB_USER");
        if (init != null && !init.isBlank()) return init;
        return "root";
    }

    public static String getPassword(ServletContext ctx) {
        String env = System.getenv("NEIS_DB_PASSWORD");
        if (env != null && !env.isBlank()) return env;
        String init = ctx.getInitParameter("NEIS_DB_PASSWORD");
        if (init != null && !init.isBlank()) return init;
        return "";
    }
}


