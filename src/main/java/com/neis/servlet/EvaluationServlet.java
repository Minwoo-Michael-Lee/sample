package com.neis.servlet;

import com.neis.db.DBConnection;
import com.neis.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 자녀평가(요약) API
 * - 행동특성(NEISDB.traitTB)
 * - NCS 이수상황(NEISDB.NCSTB)
 *
 * GET /api/evaluation?studentId=...
 */
public class EvaluationServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        Map<String, Object> traits = new LinkedHashMap<>();
        List<Map<String, Object>> ncs = new ArrayList<>();

        String traitSql = "SELECT s_grade, traits FROM NEISDB.traitTB WHERE s_id = ? ORDER BY s_grade DESC LIMIT 10";
        String ncsSql = "SELECT s_grade, s_term, code, grade FROM NEISDB.NCSTB WHERE s_id = ? ORDER BY s_grade DESC, s_term DESC LIMIT 200";

        try (Connection conn = DBConnection.getConnection(getServletContext())) {
            try (PreparedStatement ps = conn.prepareStatement(traitSql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("grade", rs.getInt(1));
                        r.put("traits", rs.getString(2));
                        list.add(r);
                    }
                    traits.put("rows", list);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(ncsSql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("grade", rs.getInt(1));
                        r.put("term", rs.getInt(2));
                        r.put("code", rs.getString(3));
                        r.put("score", rs.getInt(4));
                        ncs.add(r);
                    }
                }
            }
        } catch (Exception e) {
            writeError(resp, 500, "DB 조회 중 오류: " + e.getMessage());
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("studentId", studentId);
        body.put("traits", traits);
        body.put("ncs", ncs);
        JsonUtil.writeJson(resp, body);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}


