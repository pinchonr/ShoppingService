package com.example;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import com.example.Shop;

import static org.junit.Assert.assertEquals;

public class ShopTest extends JerseyTest {
	
	private String token;

    @Override
    protected Application configure() {
    	enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(Shop.class);
    }
    
    /**
     * Test to see that the message "Shopping service" is sent in the response.
     */
    @Test
    public void testGetIt() {
        final String responseMsg = target().path("ShoppingService/").request().get(String.class);
        assertEquals("Shopping Service", responseMsg);
    }
    
    /**
     * Test to authenticate and get a token
     */
    @Test
    public void testAuthenticate() {
    	String credentials="username=user&password=password";
    	final Response response = target("/ShoppingService/auth").queryParam("from", "Client").queryParam("to", "Shop").request().post(Entity.entity(credentials, MediaType.APPLICATION_FORM_URLENCODED),Response.class);
    	assertEquals(200,response.getStatus());
    	this.token=response.readEntity(String.class);
    	testBuyReq();
    	testBookReqGuard();
    	testBookReqWithInvalidIsbn();
    	testGetAllBooks();
    	testBookReqWithValidIsbn();
    }

    /**
     * 
     */
    private void testBuyReq() {
    	String quantity= "quantity=10";
    	final Response response = target("/ShoppingService/books/978-0-3213-5668-0").queryParam("from", "Client").queryParam("to", "Shop").queryParam("token", this.token).request().put(Entity.entity(quantity, MediaType.APPLICATION_FORM_URLENCODED),Response.class);
    	assertEquals(200, response.getStatus());	
	}

	/**
     * Test to see if I can get all books (A mock can be used here if destination server is down or not deployed yet)
     */
    public void testGetAllBooks() {
        final Response response = target("/ShoppingService/books").queryParam("from", "Client").queryParam("to", "Shop").queryParam("token", this.token).request().get(Response.class);
        assertEquals(200, response.getStatus());
    }
    
    /**
     * Test to see that the GUARD work correctly
     */
    public void testBookReqGuard() {
        final Response response = target("/ShoppingService/books/").queryParam("from", "badUser").queryParam("to", "Shop").queryParam("token", this.token).request().get(Response.class);
        assertEquals(400, response.getStatus());
        assertEquals("{\"status\":\"400\", \"error\":\"Bad request! Please verify your sender (from), your receiver (to) and your token\"}", response.readEntity(String.class));
    }
    
    /**
     * Test to see that you get a Bad Request error if you send a wrong isbn
     */
    public void testBookReqWithInvalidIsbn() {
        final Response response = target("/ShoppingService/books/978-0-3213-56680").queryParam("from", "Client").queryParam("to", "Shop").queryParam("token", this.token).request().get(Response.class);
        assertEquals(400, response.getStatus());
        assertEquals("{\"status\":\"400\", \"error\":\"Invalid isbn 13!\"}", response.readEntity(String.class));
    }
    
    /**
     * Assuming the StockService server is up and everything went fine (A mock can be used here if destination server is down or not deployed yet)
     */
    public void testBookReqWithValidIsbn() {
        final Response response = target("/ShoppingService/books/978-0-3213-5668-0").queryParam("from", "Client").queryParam("to", "Shop").queryParam("token", this.token).request().get(Response.class);
        assertEquals(200, response.getStatus());
    }
}
