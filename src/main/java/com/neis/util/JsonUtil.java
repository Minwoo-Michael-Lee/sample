package com.neis.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {}

    public static void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }
}


