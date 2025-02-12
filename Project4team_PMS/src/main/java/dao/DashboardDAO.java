package dao;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class DashboardDAO extends ParentDAO{
	
	public JSONArray getDashboard(String customerId) { // 현재 고객 ID로 대시보드 조회
		JSONArray dashboards = new JSONArray();
		// CLIENT 테이블이랑 TASK 테이블이랑 조인해서 누가 어느 작업을 올렸는지 조회한다.
		String query = "SELECT CD.DASHBOARD_ID, D.CUSTOMER_ID, D.JSONSTR DASHBOARD_JSON "
				+ "FROM DASHBOARD D, CLIENT C, CLIENT_DASHBOARD CD "
				+ "WHERE C.CUSTOMER_ID = ? AND CD.DASHBOARD_ID = D.DASHBOARD_ID AND C.CUSTOMER_ID = CD.CUSTOMER_ID";
		try {
			connDB();
			stmt = con.prepareStatement(query);
			stmt.setString(1, customerId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				// DASHBOARD 테이블 데이터 생성
				JSONObject dashboardObj = new JSONObject();
				dashboardObj.put("dashboardData", rs.getString("DASHBOARD_JSON"));
				dashboardObj.put("dashboardId", rs.getString("DASHBOARD_ID"));
				dashboardObj.put("customerId", rs.getString("CUSTOMER_ID"));

				// JSONArray에 추가
				dashboards.add(dashboardObj);
			}
			System.out.println(dashboards.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResources();
		}

		return dashboards;
	}

	public int insertDashboard(String dashboardName, String customerId) {
		String checkDashboardKey = "SELECT MAX(Dashboard_id) + 1 AS MK FROM DASHBOARD";
		String insertDashboardQuery = "INSERT INTO DASHBOARD (DASHBOARD_ID, CUSTOMER_ID, JSONSTR) VALUES (?, ?, ?)";
		String insertClientDashboardQuery = "INSERT INTO CLIENT_DASHBOARD (CUSTOMER_ID, DASHBOARD_ID) VALUES (?, ?)";

		// 현재 날짜 가져오기
		Date today = new Date();
		SimpleDateFormat formatMethod = new SimpleDateFormat("yyyy-MM-dd");
		String todayDateStr = formatMethod.format(today);
		
		// dashboard 테이블에 들어갈 json
		JSONObject jsonstr = new JSONObject();
		jsonstr.put("name", dashboardName);
		jsonstr.put("startdate", todayDateStr);

		System.out.println("-------------------------------------------------------hh");
		System.out.println(jsonstr.toJSONString());
		try {
			connDB();

			// 1. DASHBOARD의 최대 키 값 + 1 가져오기
			stmt = con.prepareStatement(checkDashboardKey);
			rs = stmt.executeQuery();
			rs.next();
			int index = rs.getInt("MK");

			// 2. 대시보드 삽입
			stmt = con.prepareStatement(insertDashboardQuery);
			stmt.setLong(1, index);
			stmt.setString(2, customerId);
			stmt.setString(3, jsonstr.toJSONString()); // JSON 데이터 저장
			stmt.executeUpdate();

			// 3. CLIENT DASHBOARD 삽입
			stmt = con.prepareStatement(insertClientDashboardQuery);
			stmt.setString(1, customerId);
			stmt.setLong(2, index);

			return stmt.executeUpdate(); // 성공 시 1 반환
		} catch (SQLException e) {
			e.printStackTrace();
			return 0; // 실패 시 0 반환
		} finally {
			closeResources();
		}
	}

	// 대시보드 업데이트
	public boolean updateDashboard(String dashboardId, String dashboardName, String startDate) {
		String query = "UPDATE DASHBOARD SET JSONSTR = ? WHERE DASHBOARD_ID = ?";
		try {
			connDB();
			stmt = con.prepareStatement(query);

			// 문자열 포매팅으로 사용자의 입력값 할당
			// '{"name":"날씨앱","startdate":"2024-01-30"}'
			String jsonStr = String.format("{\"name\": \"%s\", \"startdate\": \"%s\"}", dashboardName, startDate);

			stmt.setString(1, jsonStr);
			stmt.setString(2, dashboardId);

			int rowsUpdated = stmt.executeUpdate(); // 수정 성공하면 '1' 반환
			return rowsUpdated > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeResources();
		}
	}

	// 대시보드 나가기
	public boolean outDashboard(String customerId, String dashboardId) {
		String query = "DELETE FROM CLIENT_DASHBOARD WHERE CUSTOMER_ID = ? AND DASHBOARD_ID = ?";

		try {
			connDB();
			stmt = con.prepareStatement(query);
			stmt.setString(1, customerId);
			stmt.setString(2, dashboardId);

			int rowsDeleted = stmt.executeUpdate(); // 삭제된 행 수 반환

			return rowsDeleted > 0; // 성공 시 true, 실패 시 false
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeResources();
		}
	}
	
	// 대시보드 삭제하기
	public boolean deleteDashboard(String dashboardId) {
		String dashboardDeleteQuery = "DELETE FROM DASHBOARD WHERE DASHBOARD_ID = ?";

		try {
			connDB();
			stmt = con.prepareStatement(dashboardDeleteQuery);
			stmt.setString(1, dashboardId);
			int rowsDeleted = stmt.executeUpdate(); // 삭제된 행 수 반환
			boolean isDeleted = rowsDeleted > 0; // 성공 시 true, 실패 시 false
			
			return isDeleted; // 성공 시 true, 실패 시 false
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			closeResources();
		}
	}
}
