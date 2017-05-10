package com.example;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.example.Shop;

public class MyResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(Shop.class);
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetInfo() {
        final String responseMsg = target().path("ShoppingService/books").request().get(String.class);

        assertEquals("To get the stock of a book, please post account, isbn, from and to at https://secret-crag-41539.herokuapp.com/ShoppingService/books/, to purchase a book please post isbn, quantity,from and to at https://secret-crag-41539.herokuapp.com/ShoppingService/books/", responseMsg);
    }
}
