package com.buyamovie.api.cart;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.buyamovie.usersession.UserSession;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class CartAddServlet
 */
@WebServlet(name = "CartAddServlet", urlPatterns = "/api/cart/add")
public class CartAddServlet extends HttpServlet {
	private static final long serialVersionUID = 14L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		String movieID = request.getParameter("id");
		String movieTitle = request.getParameter("title");
		String price = request.getParameter("price");
		String quantity = request.getParameter("quantity");
		String userEmail = request.getParameter("user");

		HttpSession session = request.getSession();
		UserSession currentUser = (UserSession) session.getAttribute("user_session");
		if(!currentUser.getUserEmail().equalsIgnoreCase(userEmail)) {
			JsonObject resultData = new JsonObject();

			resultData.addProperty("status", "fail");
			resultData.addProperty("message", "User not found");

			// Write JSON string to output
			out.write(resultData.toString());
			// Set response status to 200 (OK)
			response.setStatus(200);
			out.close();
			return;
		}

		try {
			// Get a connection from dataSource
			Connection dbConnection = dataSource.getConnection();

			JsonObject resultData = new JsonObject();

			String cartID = getCartID(dbConnection, userEmail);
			
			String query = "INSERT INTO cart_items\n" + 
					"VALUES (NULL,?,?,?,?)\n" + 
					"ON DUPLICATE KEY UPDATE\n" + 
					"	cart_items.quantity = cart_items.quantity + 1";
			
			PreparedStatement statement = dbConnection.prepareStatement(query);
			statement.setString(1, cartID);
			statement.setString(2, movieID);
			statement.setInt(3, Integer.parseInt(quantity));
			statement.setFloat(4, Float.parseFloat(price));

			if (statement.executeUpdate() == 0)
				throw new SQLException("Failed to add movie to cart");
			
			resultData.addProperty("status", "success");
			resultData.addProperty("message", movieTitle + " added to cart");

			// Write JSON string to output
			out.write(resultData.toString());
			// Set response status to 200 (OK)
			response.setStatus(200);
			// Close everything
			dbConnection.close();
			//rSet.close();
			//statement.close();
		} catch (Exception e) {
			// Write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// Set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		out.close();
	}

	private String getCartID(Connection dbConnection, String userEmail) throws SQLException {
		String cartID = "";
		String query = "SELECT carts.id\n" + 
				"FROM carts\n" + 
				"WHERE carts.user_email = ?";
		PreparedStatement statement = dbConnection.prepareStatement(query);
		statement.setString(1, userEmail);

		ResultSet rSet = statement.executeQuery();
		
		if (!rSet.next()) {
			query = "INSERT INTO carts (user_email)\n" + 
					"VALUES (?)";

			statement = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

			statement.setString(1, userEmail);

			if (statement.executeUpdate() == 0)
				throw new SQLException("Creating new cart failed. No cart created");

			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
				cartID = Integer.toString(generatedKeys.getInt(1));
				return cartID;
			}
			else
				throw new SQLException("Creating new cart failed. No cart created");
		} else {
			cartID = rSet.getString("id");
			return cartID;
		}
	}
}