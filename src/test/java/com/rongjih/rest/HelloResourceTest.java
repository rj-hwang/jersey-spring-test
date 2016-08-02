package com.rongjih.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-test.xml")
public class HelloResourceTest {
	/**
	 * inject the WebTarget instance by spring for test.
	 * here no any dependencies on Jersey, just jax-rs api
	 */
	@Inject
	public WebTarget target;

	@Test
	public void hello() {
		Response res = target.path("hello").request().get();
		assertEquals(OK.getStatusCode(), res.getStatus());
		assertEquals(MediaType.TEXT_PLAIN_TYPE, res.getMediaType());
		assertEquals("Hello", res.readEntity(String.class));
	}

	@Test
	public void resourceInstanceIsSingleton() {
		assertEquals(target.path("hello/hash-code").request().get(String.class),
				target.path("hello/hash-code").request().get(String.class));
	}
}