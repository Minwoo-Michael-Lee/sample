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
 * 대입지원 - 대학별 수신 현황 API
 *
 * GET /api/college-receive-status?studentId=...
 */
public class CollegeReceiveStatusServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        // provide_code 기준으로 대학 제공 이력을 조회
        String sql =
                "SELECT a.provide_code, h.college, h.major, h.req_date " +
                "FROM NEISDB.collegeApplyTB a " +
                "JOIN NEISDB.collegeProvideHistoryTB h ON h.provide_code = a.provide_code " +
                "WHERE a.s_id = ? " +
                "ORDER BY h.req_date DESC";

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(getServletContext());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("provideCode", rs.getString(1));
                    r.put("college", rs.getString(2));
                    r.put("major", rs.getString(3));
                    r.put("reqDate", rs.getString(4));
                    rows.add(r);
                }
            }
        } catch (Exception e) {
            writeError(resp, 500, "DB 조회 중 오류: " + e.getMessage());
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("studentId", studentId);
        body.put("rows", rows);
        JsonUtil.writeJson(resp, body);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}


