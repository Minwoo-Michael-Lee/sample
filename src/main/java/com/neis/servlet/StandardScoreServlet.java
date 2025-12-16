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
 * 표준점수 분석(간단) API
 * - 동일 (type, subject) 집단의 평균/표준편차를 구해 학생 점수를 z-score로 변환합니다.
 * - 실제 표준점수 체계는 정책에 따라 달라질 수 있어, MVP 수준의 분석만 제공합니다.
 *
 * GET /api/standard-score?studentId=...
 */
public class StandardScoreServlet extends BaseApiServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String studentId = trim(req.getParameter("studentId"));
        if (studentId == null) {
            writeError(resp, 400, "studentId 파라미터가 필요합니다.");
            return;
        }

        // 1) 학생의 과목별 성적 조회
        String studentSql = "SELECT type, subject, grade FROM NEISDB.gradesTB WHERE s_id = ? ORDER BY type, subject";

        // 2) 집단 통계(평균/표준편차) - 동일 type,subject 전체 학생
        String statSql = "SELECT AVG(grade) AS mean_g, STDDEV_POP(grade) AS sd_g, COUNT(*) AS cnt " +
                "FROM NEISDB.gradesTB WHERE type = ? AND subject = ?";

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection(getServletContext())) {
            try (PreparedStatement ps = conn.prepareStatement(studentSql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int type = rs.getInt(1);
                        int subject = rs.getInt(2);
                        int grade = rs.getInt(3);

                        double mean = 0.0;
                        double sd = 0.0;
                        int cnt = 0;
                        try (PreparedStatement ps2 = conn.prepareStatement(statSql)) {
                            ps2.setInt(1, type);
                            ps2.setInt(2, subject);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    mean = rs2.getDouble(1);
                                    sd = rs2.getDouble(2);
                                    cnt = rs2.getInt(3);
                                }
                            }
                        }

                        double z = (sd == 0.0) ? 0.0 : (grade - mean) / sd;
                        // 임의 표준점수: 50 + 10*z
                        double standard = 50.0 + 10.0 * z;

                        Map<String, Object> r = new LinkedHashMap<>();
                        r.put("type", type);
                        r.put("subject", subject);
                        r.put("grade", grade);
                        r.put("mean", mean);
                        r.put("sd", sd);
                        r.put("count", cnt);
                        r.put("z", z);
                        r.put("standardScore", standard);
                        rows.add(r);
                    }
                }
            }
        } catch (Exception e) {
            writeError(resp, 500, "DB 조회/계산 중 오류: " + e.getMessage());
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


