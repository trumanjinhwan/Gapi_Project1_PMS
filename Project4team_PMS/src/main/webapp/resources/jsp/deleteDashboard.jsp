<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.*" %>

<%
    request.setCharacterEncoding("UTF-8");

    String dashboardId = request.getParameter("dashboardId");

    DashboardDAO dao = new DashboardDAO();
    boolean isDeleted = dao.deleteDashboard(dashboardId);

    if (isDeleted) {
        out.print("OK");
    } else {
        out.print("ERROR");
    }
%>