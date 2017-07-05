package com.example;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.example.model.JsonError;

import java.net.URL;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;



/**
 * Root resource (exposed at "ShoppingService" path)
 */
@Path("ShoppingService")
public class Shop {

	private final String STOCK_BASE_URL="https://blueberry-crisp-72094.herokuapp.com";
	private final String WHOLESALER_BASE_URL="http://wholesalerservice-167311.appspot.com";

	/**
	 * Get the Shopping Service String
	 */
	@Path("/")    
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getInfo() {
		return "Shopping Service";
	}

	/**
	 * Get token or error for given credentials (at JSON format)s
	 * @param username
	 * @param password
	 * @param from
	 * @param to
	 * @return Response
	 */
	@Path("/auth")    
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUser(@FormParam("username") String username, @FormParam("password") String password,@QueryParam("from")String from,@QueryParam("to")String to) {
		try {

			if(!from.equals("Client")||!to.equals("Shop")){
				return jsonErrorResponseBuilder(400,"Bad request!\n Please verify your sender (from), your receiver (to)");
			}
			authenticate(username, password);
			String token = createToken();

			return Response.ok(token).build();

		} catch (Exception e) {
			return jsonErrorResponseBuilder(401, "Invalid credentials");
		}    
	}

	/**
	 * Get all books in database (at JSON format)
	 * @param from
	 * @param to
	 * @param token
	 * @return Response
	 */
	@Path("/books")    
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getAllBooks(@QueryParam("from")String from,@QueryParam("to")String to,@QueryParam("token")String token) {
		if(!from.equals("Client")||!to.equals("Shop")||!isTokenValid(token)){
			return jsonErrorResponseBuilder(400,"Bad request! Please verify your sender (from), your receiver (to) and your token");
		}
		return getServiceInformations("GET",STOCK_BASE_URL+"/StockService/stocks?from=Shop&to=Stocsk");
	}

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return Response the HTTP response
	 */
	@Path("/books/{isbn}")    
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response bookReq(@PathParam("isbn")String isbn,@QueryParam("from")String from,@QueryParam("to")String to,@QueryParam("token")String token) {

		//Example of valid isbn13: 978-0-596-52068-7 matched by this regex
		String regex="^(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$";

		if(!from.equals("Client")||!to.equals("Shop")||isbn==null||!isTokenValid(token)){
			return jsonErrorResponseBuilder(400,"Bad request!\n Please verify your sender (from), your receiver (to), your isbn (not null) and your token");
		}
		if(!isbn.matches(regex)){
			return jsonErrorResponseBuilder(400,"Invalid isbn 13!");
		}
		return getServiceInformations("GET",STOCK_BASE_URL+"/StockService/stocks/"+isbn+"?from=Shop&to=Stock");
	}

	/**
	 * Ask stockService stock for given isbn and call wholesaler if needed (if not update stock)
	 * @param isbn
	 * @param quantity
	 * @param from
	 * @param to
	 * @param token
	 * @return Response
	 */
	@PUT
	@Path("/books/{isbn}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response buyReq(@PathParam("isbn")String isbn, @FormParam("quantity") int quantity, @QueryParam("from")String from,@QueryParam("to")String to,@QueryParam("token")String token){
		//Example of valid isbn13: 978-0-596-52068-7 matched by this regex
		String regex="^(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$";

		if(!from.equals("Client")||!to.equals("Shop")||isbn==null||!isTokenValid(token)){
			return jsonErrorResponseBuilder(400,"Bad request!\n Please verify your sender (from), your receiver (to), your isbn (not null) and your token");
		}
		if(!isbn.matches(regex)){
			return jsonErrorResponseBuilder(400,"Invalid isbn 13!");
		}
		if(quantity<0){
			return jsonErrorResponseBuilder(400,"quantity can't be less than 0");
		}
		System.out.println("calling: "+STOCK_BASE_URL+"/StockService/stocks/"+isbn+"?from=Shop&to=Stock");
		Response stockResponse=null;
		try{
		stockResponse = getServiceInformations("GET",STOCK_BASE_URL+"/StockService/stocks/"+isbn+"?from=Shop&to=Stock");
		System.out.println(stockResponse.getStatus()+"  "+stockResponse.readEntity(String.class));
		return stockResponse;
		}
		catch(Exception e){
			return jsonErrorResponseBuilder(200,stockResponse.readEntity(String.class));
		}
		
		
		/*if(stockResponse.getStatus() == 200){
			String jsonResponse= stockResponse.readEntity(String.class);
			JSONObject json= new JSONObject(jsonResponse);
			int currentStock=json.getInt("stock");

			if(currentStock<quantity){
				Response wholesalerResponse=getServiceInformations("GET",WHOLESALER_BASE_URL+"/wholesalerService/restock?isbn="+ isbn +"&quantity=" + quantity + "&key=1234&from=Shopping&to=Wholesaler");
				return Response.status(wholesalerResponse.getStatus()).entity(wholesalerResponse.readEntity(String.class)).type(MediaType.APPLICATION_JSON).build();
			}
			else{
				Response stockResponseAfterPut =getServiceInformations("PUT",STOCK_BASE_URL+"/StockService/stocks/"+isbn+"?from=Shop&to=Stock&newStock="+(currentStock-quantity));
				return Response.status(stockResponseAfterPut.getStatus()).entity(stockResponseAfterPut.readEntity(String.class)).type(MediaType.APPLICATION_JSON).build();

			}
		}
		else{
			return jsonErrorResponseBuilder(stockResponse.getStatus(), stockResponse.readEntity(String.class));
		}*/
	}

	/**
	 * Method making a request with HttpUrlConnexion to the given service.
	 * @param method
	 * @param path
	 * @return Response
	 */
	private Response getServiceInformations(String method,String path)
	{
		String url = path;
		BufferedReader in=null;
		String inputLine;
		StringBuffer BodyResponse = new StringBuffer();

		try{
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			con.setRequestMethod(method);
			if(con.getResponseCode()>=200 && con.getResponseCode()<300){
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			else{
				in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			while ((inputLine = in.readLine()) != null) {
				BodyResponse.append(inputLine);
			}
			return Response.status(con.getResponseCode()).entity(BodyResponse.toString()).type(MediaType.APPLICATION_JSON).build();

		}
		catch(Exception e){
			return jsonErrorResponseBuilder(500,"An error occured while getting the stock (OPENING CONNECTION): "+e.getMessage());
		}
		finally{
			try { if (in!=null) in.close(); } catch (Exception e) {}
		}

	}

	/**
	 * Create a Response with APPLICATION/JSON type with given status and message
	 * @param status
	 * @param message
	 * @return
	 */
	private Response jsonErrorResponseBuilder(int status,String message){
		return Response.status(status).entity(new JsonError(status, message).toJson()).type(MediaType.APPLICATION_JSON).build();

	}

	/**
	 * throw an exception if credentials are invalid (catched to send an Unauthorized response)
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	private void authenticate(String username, String password) throws Exception {
		if(!username.equals("user")||!password.equals("password")){
			throw new AuthenticationException("Bad credentials");
		}

	}

	/**
	 * Create a token and write it in a file
	 * @return token
	 */
	private String createToken() {
		//Creating token
		String token = UUID.randomUUID().toString();

		//Stock the token in a file
		BufferedWriter output = null;
		File file = null;
		try {
			file = new File("tokens.txt");
			output = new BufferedWriter(new FileWriter(file));
			output.write(token+"\n");
		} catch ( IOException e ) {
			e.printStackTrace();
		} finally {
			try { if ( output != null ) output.close(); } catch (IOException e) {e.printStackTrace();}
		}
		return token;
	}

	/**
	 * Verify if the token is present in the token file
	 * @param token
	 */
	private boolean isTokenValid(String token){
		String line;
		BufferedReader br=null;
		boolean found= false;
		try {
			br = new BufferedReader(new FileReader("tokens.txt"));

			while((line=br.readLine())!=null){
				if(line.trim().equals(token)){
					found=true;
				}
			}
			return found;

		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		} finally {
			try {if ( br != null )br.close();} catch (IOException e) {e.printStackTrace();}
		}

	}

}
