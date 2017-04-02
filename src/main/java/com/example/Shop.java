package com.example;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.sql.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;



/**
 * Root resource (exposed at "myresource" path)
 */
@Path("ShoppingService")
public class Shop {
	private String account;
	private String corr;
	private boolean hasAnErrorOccured= false;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return Response the HTTP response
	 */
	@Path("/books")    
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response bookReq(@FormParam("account")String account,@FormParam("isbn")String isbn,@FormParam("from")String from,@FormParam("to")String to, @FormParam("corr")String corr) {
		
		String regex="^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$";
		if(from.equals("Client")&& to.equals("Shop")&&corr.equals(account)&&isbn!=null){
			this.account=account;
			this.corr=corr;
		}
		if(!isbn.matches(regex)){
			return Response.status(400).entity("Invalid isbn!").build();
		}
		String stock= getStock(isbn, from, to, corr);
		String result = "Stock of the book with isbn "+isbn+" :"+stock;
		return Response.status(200).entity(result).build();
	}

	@Path("/books")    
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "To get the stock of a book, please post account, isbn, from, to and corr to /ShoppingService/books, to purchase a book please post isbn, quantity,from, to and corr to /WholesalerService.";
	}

	/**
	private Connection getConnection() throws Exception {
		// Class.forName("org.postgresql.Driver");
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath();

		return DriverManager.getConnection(dbUrl, username, password);
	}
	*/

	private String getStock(String isbn,String from,String to, String corr)
	{
		String result=null;
		try {

			URL url = new URL("http://localhost:7000/StockService/stock");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			String input = "account:account,isbn:9780596520687,from:Client,to:Shop,corr:account";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			
			while ((output = br.readLine()) != null) {
				result+=output;
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			return e.getMessage();

		  } catch (IOException e) {

			return e.getMessage();

		 }

		return result;
	}

}
