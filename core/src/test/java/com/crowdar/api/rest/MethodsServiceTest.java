package com.crowdar.api.rest;

import com.crowdar.core.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.crowdar.core.PropertyManager;

import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestClient.class, PropertyManager.class})
public class MethodsServiceTest {
	private static final String JSON_CLASS_RESPONSE = "request";

	@Mock
    private RestClient restClient;
	private Response response;
	private Request request;
	private String jsonRequest;
	private Map<String, String> parametersMap;
	
	@Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
		jsonRequest = MethodsServiceTest.JSON_CLASS_RESPONSE;

        request = new Request();
		request.setBody("");
		request.setUrlParameters("");
		request.setHeaders("");
				
		PowerMockito.mockStatic(RestClient.class);
        when(RestClient.class, "getRestclientInstance").thenReturn(restClient);
		
        PowerMockito.mockStatic(PropertyManager.class);
		when(PropertyManager.class, "getProperty", Mockito.anyString())
			.thenReturn("http://test.test");
        
		
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		response = new Response(200, "OK", new Object(), new Headers(map));
		
		Mockito.when(
				restClient.get(
						"http://test.test", 
						Response.class, 
						request.getBody().toString(), 
						request.getUrlParameters(), 
						request.getHeaders().toString()
				)
		).thenReturn(response);
		
		Mockito.when(
				restClient.post(
						"http://test.test", 
						Response.class, 
						request.getBody().toString(), 
						request.getUrlParameters(), 
						request.getHeaders().toString()
				)
		).thenReturn(response);
		
		Mockito.when(
				restClient.patch(
						"http://test.test", 
						Response.class, 
						request.getBody().toString(), 
						request.getUrlParameters(), 
						request.getHeaders().toString()
				)
		).thenReturn(response);
		
		Mockito.when(
				restClient.delete(
						"http://test.test", 
						Response.class, 
						request.getBody().toString(), 
						request.getUrlParameters(), 
						request.getHeaders().toString()
				)
		).thenReturn(response);
		
		Mockito.when(
				restClient.put(
						"http://test.test", 
						Response.class, 
						request.getBody().toString(), 
						request.getUrlParameters(), 
						request.getHeaders().toString()
				)
		).thenReturn(response);
    }
	
    @Test
	public void whenCallSomeApiWithGetMethod() throws Exception{
		Response responseFromStaticMethod = MethodsService.get(getRequest(jsonRequest,null), Response.class);
		Mockito.verify(restClient, Mockito.times(1)).get(
				"http://test.test", 
				Response.class, 
				request.getBody().toString(), 
				request.getUrlParameters(), 
				request.getHeaders().toString());
		Assert.assertTrue(response.equals(responseFromStaticMethod));
		Assert.assertTrue(APIManager.getLastResponse().equals(response));
	}
    
    
    @Test
	public void whenCallSomeApiWithPostMethod() throws Exception{
		Response responseFromStaticMethod = MethodsService.post(getRequest(jsonRequest,parametersMap), Response.class);
		Mockito.verify(restClient, Mockito.times(1)).post(
				"http://test.test", 
				Response.class, 
				request.getBody().toString(), 
				request.getUrlParameters(), 
				request.getHeaders().toString());
		Assert.assertTrue(response.equals(responseFromStaticMethod));
		Assert.assertTrue(APIManager.getLastResponse().equals(response));
	}
    
    @Test
	public void whenCallSomeApiWithPatchMethod() throws Exception{
		Response responseFromStaticMethod = MethodsService.patch(getRequest(jsonRequest,parametersMap), Response.class);
		Mockito.verify(restClient, Mockito.times(1)).patch(
				"http://test.test", 
				Response.class, 
				request.getBody().toString(), 
				request.getUrlParameters(), 
				request.getHeaders().toString());
		Assert.assertTrue(response.equals(responseFromStaticMethod));
		Assert.assertTrue(APIManager.getLastResponse().equals(response));
	}
    
    @Test
	public void whenCallSomeApiWithDeleteMethod() throws Exception{
		Response responseFromStaticMethod = MethodsService.delete(getRequest(jsonRequest,parametersMap), Response.class);
		Mockito.verify(restClient, Mockito.times(1)).delete(
				"http://test.test", 
				Response.class, 
				request.getBody().toString(), 
				request.getUrlParameters(), 
				request.getHeaders().toString());
		Assert.assertTrue(response.equals(responseFromStaticMethod));
		Assert.assertTrue(APIManager.getLastResponse().equals(response));
	}
    
    @Test
	public void whenCallSomeApiWithPutMethod() throws Exception{
		Response responseFromStaticMethod = MethodsService.put(getRequest(jsonRequest,parametersMap), Response.class);
		Mockito.verify(restClient, Mockito.times(1)).put(
				"http://test.test", 
				Response.class, 
				request.getBody().toString(), 
				request.getUrlParameters(), 
				request.getHeaders().toString());
		Assert.assertTrue(response.equals(responseFromStaticMethod));
		Assert.assertTrue(APIManager.getLastResponse().equals(response));
	}


	private static Request getRequest(String jsonFileName, Map<String, String> replacementParameters) {
		String jsonRequest = JsonUtils.getJSONFromFile(jsonFileName);

		if(replacementParameters != null) {
			for (String key : replacementParameters.keySet()) {
				jsonRequest=jsonRequest.replace("{{"+key+"}}", replacementParameters.get(key));
			}
		}

		return JsonUtils.deserialize(jsonRequest, Request.class);
	}



}