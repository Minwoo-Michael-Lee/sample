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
import java.time.LocalDate;
import java.util.*;

/**
 * 출결상황 조회 API
 * GET /api/attendance?studentId=...&from=YYYY-MM-DD&to=YYYY-MM-DD
 */
public class AttendanceServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        LocalDate to = parseDateOrDefault(req.getParameter("to"), LocalDate.now());
        LocalDate from = parseDateOrDefault(req.getParameter("from"), to.minusDays(30));

        String sql = "SELECT `date`, attend FROM NEISDB.attendenceTB WHERE s_id = ? AND `date` BETWEEN ? AND ? ORDER BY `date` DESC";
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(getServletContext());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, from.toString());
            ps.setString(3, to.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("date", rs.getString(1));
                    r.put("attend", rs.getInt(2));
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
        body.put("from", from.toString());
        body.put("to", to.toString());
        body.put("rows", rows);
        JsonUtil.writeJson(resp, body);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseDateOrDefault(String s, LocalDate def) {
        String t = trim(s);
        if (t == null) return def;
        try {
            return LocalDate.parse(t);
        } catch (Exception ignore) {
            return def;
        }
    }
}


