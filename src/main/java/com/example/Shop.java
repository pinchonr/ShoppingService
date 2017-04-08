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
		//Example of valid isbn13: 978-0-596-52068-7
		String regex="^(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$";
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

	private String getStock(String isbn,String from,String to, String corr)
	{
		String url = "https://blueberry-crisp-72094.herokuapp.com/StockService/stock/"+isbn;
		BufferedReader in=null;
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		try{
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");

			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			return response.toString();
		}
		catch(Exception e){
			return e.getMessage();
		}
		finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					//ignore error
				}
			}
		}

	}

}
