package hu.econsult.jwt;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class ValidatorService {

	@Value("${validate.url}")
	private String VALIDATE_URL;
	
	@Value("${authenticate.url}")
	private String AUTHENTICATE_URL;
	
	@Value("${logout.url}")
	private String LOGOUT_URL;

	public static final int CLIENT_CONNECT_TIMEOUT = 50_000;
	public static final int CLIENT_READ_TIMEOUT = 50_000;
	

	private Client getClient() {
		Client client = ClientBuilder.newClient();
     	client.property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT);
		client.property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT);

		return client;
	}
	

	private WebTarget validateTokenTarget(String token) {
		Client client = getClient();
		WebTarget target = client.target(VALIDATE_URL); // + token if PathVariable
		return target;
	}
	
	private WebTarget logoutTokenTarget(String token) {
		Client client = getClient();
		WebTarget target = client.target(LOGOUT_URL + token);
		return target;
	}

	public boolean isValidToken(String token) {
		WebTarget target = validateTokenTarget(token);
		Response jaxWsResponse = target.request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, token)
				.get();
		return jaxWsResponse.readEntity(Boolean.class);
	}
	
	public boolean logoutToken(String token) {
		WebTarget target = logoutTokenTarget(token);
		Response serviceResponse = target.request().get();
		return apiResponseCheck(serviceResponse);
	}

	private boolean apiResponseCheck(Response serviceResponse) {
		if (serviceResponse.getStatus() != 200)
			return false;
		return true;
	}
	
	public JsonNode authenticate(String username, String password) {
		String jsonstr = "{  \"password\": \"" + password +"\",  \"username\": \""+ username +"\"  }";
		
		InputStream stream = new ByteArrayInputStream(jsonstr.getBytes(StandardCharsets.UTF_8));
		Client client = getClient();
		WebTarget authEndpoint = client.target(AUTHENTICATE_URL);
		Response jaxWsResponse = authEndpoint.request(MediaType.APPLICATION_JSON).post(Entity.json(stream));
		JsonNode response = jaxWsResponse.readEntity(JsonNode.class);
		
		return response;
	}
}