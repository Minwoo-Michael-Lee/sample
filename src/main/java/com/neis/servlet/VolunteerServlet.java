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
 * 봉사활동실적 조회 API
 * - 스키마에 학생ID 기반 봉사 테이블이 없어서,
 *   NEISDB.studentsTB(name,birthday) ↔ V1365DB.volunteerTB(v_name,v_birth)를 매칭하여 조회합니다.
 * GET /api/volunteer?studentId=...
 */
public class VolunteerServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        String getStudentSql = "SELECT name, birthday FROM NEISDB.studentsTB WHERE id = ?";
        String getVolSql =
                "SELECT v.date, v.place, v.time " +
                "FROM V1365DB.volunteerTB v " +
                "WHERE v.v_name = ? AND v.v_birth = ? " +
                "ORDER BY v.date DESC";

        String name;
        String birth;
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(getServletContext())) {
            try (PreparedStatement ps = conn.prepareStatement(getStudentSql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        writeError(resp, 404, "학생을 찾을 수 없습니다. studentId=" + studentId);
                        return;
                    }
                    name = rs.getString(1);
                    birth = rs.getString(2);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(getVolSql)) {
                ps.setString(1, name);
                ps.setString(2, birth);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("date", rs.getString(1));
                        r.put("place", rs.getString(2));
                        r.put("time", rs.getInt(3));
                        rows.add(r);
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
        body.put("matchedStudent", Map.of("name", name, "birthday", birth));
        body.put("rows", rows);
        JsonUtil.writeJson(resp, body);
    }

    private static String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}


