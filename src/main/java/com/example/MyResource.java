package com.example;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;



/**
 * Root resource (exposed at "myresource" path)
 */
@Path("ShoppingService")
public class MyResource {
	private String account;
	private String corr;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@Path("/books/")    
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response bookReq(String account,String isbn,String from, String to, String corr) {
		String regex="^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$";
		if(from.equals("Client")&& to.equals("Shop")&&corr.equals(account)){
			this.account=account;
			this.corr=corr;
		}
		if(!isbn.matches(regex)){
			return Response.status(400).entity("Invalid isbn!").build();
		}
		String result = "Stock of the book with isbn "+isbn+" :";
		return Response.status(200).entity(result).build();
	}

	@Path("db")    
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getItbd() {
		return showDatabase();
	}

	private Connection getConnection() throws Exception {
		// Class.forName("org.postgresql.Driver");
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}

	private String showDatabase()
	{
		try {
			Connection connection = getConnection();

			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

			String out = "Hello!\n";
			while (rs.next()) {
				out += "Read from DB: " + rs.getTimestamp("tick") + "\n";
			}

			return out;
		} catch (Exception e) {
			return "There was an error: " + e.getMessage();
		}
	}

}
