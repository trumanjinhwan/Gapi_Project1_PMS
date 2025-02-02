package dao;

import java.sql.*;
import javamailApi.*;

public class FriendDAO {
	String driver = "oracle.jdbc.driver.OracleDriver";
	String url = "jdbc:oracle:thin:@localhost:1521/orcl";
	String user = "c##apple";
	String password = "1111";

	private Connection con;
	private PreparedStatement stmt;
	private ResultSet rs;

	// 동료 추가 및 이메일 전송
	public String addFriend(String customerId, String email) {
		String queryCheckUser = "SELECT CUSTOMER_ID, JSONSTR FROM CLIENT WHERE CUSTOMER_ID = ?";
		String queryAddFriend = "INSERT INTO FRIENDLIST (CUSTOMER_ID, FRIEND_ID) VALUES (?, ?)";

		try {
			connDB();
			
			System.out.println("보내는 사람: " + customerId + " 받는 사람: " + email);

			// 1. 이메일로 사용자가 존재하는지 확인
			stmt = con.prepareStatement(queryCheckUser);
			stmt.setString(1, email);
			rs = stmt.executeQuery();

			if (rs.next()) {
				String userName = rs.getString("CUSTOMER_ID");

				// 2. 사용자 존재 시 동료 목록에 추가
				stmt = con.prepareStatement(queryAddFriend);
				stmt.setString(1, customerId);
				stmt.setString(2, email);
				int result = stmt.executeUpdate();

				if (result > 0) {
					// 이메일 전송
					EmailSender.sendInvitationEmail(customerId, email, userName);
					return "OK";
				} else {
					return "동료 추가 실패";
				}
			} else {
				return "해당 이메일에 해당하는 사용자가 없습니다.";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "데이터베이스 오류";
		} finally {
			closeResources();
		}
	}

	public void connDB() {
		try {
			Class.forName(driver); // JDBC 드라이버 로드
			System.out.println("JDBC driver loading success.");

			try {
				con = DriverManager.getConnection(url, user, password); // 첫 번째 URL로 연결
				System.out.println("Oracle connection success with URL: " + url);
			} catch (Exception e) {
				System.out.println("Connection failed with URL: " + url);
				System.out.println("Retrying with alternate URL...");

				// 대체 URL로 연결
				String alternateUrl = "jdbc:oracle:thin:@localhost:1521/XE";
				con = DriverManager.getConnection(alternateUrl, user, password);
				System.out.println("Oracle connection success with alternate URL: " + alternateUrl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 6. 자원 해제
	private void closeResources() {
		try {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
