package dao;

import java.sql.*;

import javax.naming.NamingException;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientDAO extends ParentDAO {

	public int insert(String cEmail, String jsonstr) throws NamingException, SQLException {
		System.out.println(cEmail);
		System.out.println(jsonstr);
		stmt = null;
		connDB();

		try {
			// 이메일 중복 확인
			String checkSql = "SELECT COUNT(*) FROM CLIENT WHERE customer_id = ?";
			PreparedStatement checkStmt = con.prepareStatement(checkSql);
			checkStmt.setString(1, cEmail);
			rs = checkStmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				return 1; // 이메일 중복
			}

			// 이메일 삽입
			String sql = "INSERT INTO CLIENT(customer_id, jsonstr) VALUES(?, ?)";

			stmt = con.prepareStatement(sql);
			stmt.setString(1, cEmail);
			stmt.setString(2, jsonstr);
			int count = stmt.executeUpdate();

			// 초대받은 사용자인지 확인
			String inviteSql = "SELECT FRIEND_ID, DASHBOARD_ID FROM INVITE WHERE FRIEND_ID = ?";
			stmt2 = con.prepareStatement(inviteSql);
			stmt2.setString(1, cEmail);
			rs = stmt2.executeQuery();

			// 대시보드 할당해주기, invite 테이블에서 삭제하기
			while (rs.next()) {
				String friendId = rs.getString("FRIEND_ID");
				String dashboardId = rs.getString("DASHBOARD_ID");

				// 대시보드 할당하기
				String queryAddClient_dashboard = "INSERT INTO CLIENT_DASHBOARD (CUSTOMER_ID, DASHBOARD_ID) VALUES (?, ?)";
				stmt = con.prepareStatement(queryAddClient_dashboard);
				stmt.setString(1, friendId);
				stmt.setString(2, dashboardId);
				stmt.executeUpdate();

				// invite 테이블에서 삭제하기
				String queryDeleteInvite = "DELETE FROM INVITE WHERE FRIEND_ID = ? AND DASHBOARD_ID = ?";
				stmt = con.prepareStatement(queryDeleteInvite);
				stmt.setString(1, friendId);
				stmt.setString(2, dashboardId);
				stmt.executeUpdate();
			}

			return (count == 1) ? 0 : 2;

		} finally {
			closeResources();
		}
	}

	public int login(String email, String password) throws SQLException, ParseException {
		try {
			connDB();
			System.out.println(email);
			String query = "SELECT jsonstr FROM CLIENT WHERE customer_id = ?";
			stmt = con.prepareStatement(query);
			stmt.setString(1, email);

			rs = stmt.executeQuery();
			if (!rs.next())
				return 1; // 일치하는 이메일(id)X

			String jsonstr = rs.getString("jsonstr");
			JSONObject obj = (JSONObject) (new JSONParser()).parse(jsonstr);
			String pass = obj.get("password").toString();
			System.out.println(pass);

			if (!password.equals(pass)) // 비민번호 틀림
				return 2;

			return 0; // 로그인 성공
		} finally {
			closeResources();
		}
	}

	public String getUserNameByCustomerId(String customerId) {
		String userName = null;
		try {
			connDB();
			String sql = "SELECT JSON_VALUE(JSONSTR, '$.name')	 AS NAME FROM CLIENT WHERE CUSTOMER_ID = ?";
			stmt = con.prepareStatement(sql);
			stmt.setString(1, customerId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				userName = rs.getString("NAME"); // JSON에서 추출한 이름 가져오기
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResources();
		}
		return userName;
	}
	
	public int updateUserInfo(String customerId, String newName, String newPassword) {
	    try {
	        connDB();

	        String selectQuery = "SELECT jsonstr FROM CLIENT WHERE customer_id = ?";
	        stmt = con.prepareStatement(selectQuery);
	        stmt.setString(1, customerId);
	        rs = stmt.executeQuery();

	        if (!rs.next()) {
	            return 1; // ❌ 존재하지 않는 사용자
	        }

	        String jsonStr = rs.getString("jsonstr");
	        JSONObject obj = (JSONObject) (new JSONParser()).parse(jsonStr);

	        if (newName != null && !newName.isEmpty()) {
	            obj.put("name", newName);
	        }
	        if (newPassword != null && !newPassword.isEmpty()) {
	            obj.put("password", newPassword);
	        }

	        String updatedJsonStr = obj.toJSONString();

	        String updateQuery = "UPDATE CLIENT SET jsonstr = ? WHERE customer_id = ?";
	        stmt = con.prepareStatement(updateQuery);
	        stmt.setString(1, updatedJsonStr);
	        stmt.setString(2, customerId);
	        int result = stmt.executeUpdate();

	        return (result > 0) ? 0 : 2;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 3;
	    } finally {
	        closeResources();
	    }
	}
	
	// 대시보드 삭제하기
		public boolean deleteClient(String customerId) {
			String deleteClientQuery = "DELETE FROM CLIENT WHERE CUSTOMER_ID = ?";

			boolean isDeleted = true;
			try {
				connDB();
				
				stmt = con.prepareStatement(deleteClientQuery);
				stmt.setString(1, customerId);
				int rowsDeleted = stmt.executeUpdate(); // 삭제된 행 수 반환
				isDeleted = isDeleted && rowsDeleted > 0; // 성공 시 true, 실패 시 false 
				
				return isDeleted; // 성공 시 true, 실패 시 false
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			} finally {
				closeResources();
			}
		}
}
