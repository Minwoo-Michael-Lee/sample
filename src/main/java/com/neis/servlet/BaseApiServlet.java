package com.neis.servlet;

import com.neis.util.JsonUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseApiServlet extends HttpServlet {
    protected void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("message", message);
        JsonUtil.writeJson(resp, body);
    }
}


