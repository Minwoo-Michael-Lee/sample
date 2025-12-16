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
 * 대입지원 - 자료 생성/신청 API
 *
 * GET  /api/college-apply?studentId=...
 * POST /api/college-apply (x-www-form-urlencoded)
 *   - studentId, type, graduate(YYYY-MM-DD)
 */
public class CollegeApplyServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        String sql = "SELECT type, graduate, state, reg_date, final_gen_date, provide_code " +
                "FROM NEISDB.collegeApplyTB WHERE s_id = ? " +
                "ORDER BY reg_date DESC, provide_code DESC LIMIT 50";
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(getServletContext());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("type", rs.getInt(1));
                    r.put("graduate", rs.getString(2));
                    r.put("state", rs.getInt(3));
                    r.put("regDate", rs.getString(4));
                    r.put("finalGenDate", rs.getString(5));
                    r.put("provideCode", rs.getString(6));
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        String typeStr = trim(req.getParameter("type"));
        String graduateStr = trim(req.getParameter("graduate"));

        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }
        if (typeStr == null) {
            writeError(resp, 400, "type 파라미터가 필요합니다.");
            return;
        }
        if (graduateStr == null) {
            writeError(resp, 400, "graduate 파라미터가 필요합니다. (YYYY-MM-DD)");
            return;
        }

        int type;
        try {
            type = Integer.parseInt(typeStr);
        } catch (Exception e) {
            writeError(resp, 400, "type 값이 올바르지 않습니다.");
            return;
        }

        LocalDate graduate;
        try {
            graduate = LocalDate.parse(graduateStr);
        } catch (Exception e) {
            writeError(resp, 400, "graduate 값이 올바르지 않습니다. (YYYY-MM-DD)");
            return;
        }

        String provideCode = "P" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
        int state = 0; // 신청 상태(초기)

        String sql = "INSERT INTO NEISDB.collegeApplyTB (type, s_id, graduate, state, reg_date, provide_code) " +
                "VALUES (?, ?, ?, ?, CURDATE(), ?)";

        try (Connection conn = DBConnection.getConnection(getServletContext());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, type);
            ps.setString(2, studentId);
            ps.setString(3, graduate.toString());
            ps.setInt(4, state);
            ps.setString(5, provideCode);
            ps.executeUpdate();
        } catch (Exception e) {
            writeError(resp, 500, "DB 저장 중 오류: " + e.getMessage());
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("studentId", studentId);
        body.put("type", type);
        body.put("graduate", graduate.toString());
        body.put("state", state);
        body.put("provideCode", provideCode);
        JsonUtil.writeJson(resp, body);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}


