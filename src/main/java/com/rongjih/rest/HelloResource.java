package com.rongjih.rest;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Named
@Singleton
@Path("hello")
public class HelloResource {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return "Hello";
	}

	@GET
	@Path("hash-code")
	@Produces(MediaType.TEXT_PLAIN)
	public String getHashCode() {
		return String.valueOf(this.hashCode());
	}
}