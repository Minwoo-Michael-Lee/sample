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
 * 성적변화표(간단) API
 * - gradesTB에 시점 컬럼이 없어서 type(시험/학기 구분값)을 기간 축으로 보고 평균 성적을 계산합니다.
 *
 * GET /api/grade-trend?studentId=...
 */
public class GradeTrendServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        String sql = "SELECT type, AVG(grade) AS avg_grade, COUNT(*) AS cnt " +
                "FROM NEISDB.gradesTB WHERE s_id = ? GROUP BY type ORDER BY type";
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(getServletContext());
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("type", rs.getInt(1));
                    r.put("avgGrade", rs.getDouble(2));
                    r.put("count", rs.getInt(3));
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


