package com.arun.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.arun.swagger.client.SwaggerClient;
import com.arun.swagger.client.TokenModel;
import com.squareup.okhttp.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {
    public static String authTokenValue;

    public static Map<String, Class<?>> classMap;
    public static final String GENERATION_OUTPUT_DIR = "target/generated-client";
    public static final String COMPILED_OUTPUT_DIR = "target/compiled-client";
    public static final String BASE_PATH = "https://petstore.swagger.io/v2/pet";
    public static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    public static Class<?> apiClient;
    public static Object apiClientBearerTokenInstance;
    public static Object apiClientApikeyInstance;

    @BeforeAll
    public void setup() throws Exception {
        classMap = SwaggerClient.createSwaggerClient(getSwaggerFilePath(), GENERATION_OUTPUT_DIR, COMPILED_OUTPUT_DIR);
        String apiClientClassName = "io.swagger.client.ApiClient";
        apiClient = classMap.get(apiClientClassName);
        apiClientBearerTokenInstance = apiClient.getDeclaredConstructor().newInstance();
        apiClientApikeyInstance = apiClient.getDeclaredConstructor().newInstance();
        String baseUrl = System.getProperty("baseUrl");
        if (StringUtils.isNotBlank(baseUrl)) {
            apiClient.getMethod("setBasePath", String.class).invoke(apiClientBearerTokenInstance, baseUrl);
            apiClient.getMethod("setBasePath", String.class).invoke(apiClientApikeyInstance, baseUrl);
        } else {
            apiClient.getMethod("setBasePath", String.class).invoke(apiClientBearerTokenInstance, BASE_PATH);
            apiClient.getMethod("setBasePath", String.class).invoke(apiClientApikeyInstance, BASE_PATH);
        }
//        authTokenValue = authTokenValue == null ? getAccessToken() : authTokenValue;
//        Method setToken = apiClient.getMethod("setAccessToken", String.class);
//        setToken.invoke(apiClientBearerTokenInstance, authTokenValue);

        Method addDefaultHeaderMethod = apiClient.getMethod("addDefaultHeader", String.class, String.class);
        addDefaultHeaderMethod.invoke(apiClientApikeyInstance, "Authorization", getApikeySecretAuth());
    }


//    private String getAccessToken() throws IOException {
//        String apiKey = System.getProperty("authApiKey");
//        String apiSecret = System.getProperty("authApiSecret");
//        String baseUrl = System.getProperty("baseUrl");
//        baseUrl = baseUrl != null && baseUrl.isBlank() ? baseUrl : BASE_PATH;
//        OkHttpClient client = new OkHttpClient();
//        String combinedKeySecret = apiKey + ":" + apiSecret;
//        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
//        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&scope=default".getBytes());
//        Request request = new Request.Builder()
//                .url(baseUrl + "/auth/v2/token")
//                .method("POST", body)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(combinedKeySecret.getBytes()))
//                .build();
//        Response response = client.newCall(request).execute();
//        assert response.body() != null;
//        assertTrue(response.isSuccessful(), "TOKEN GENERATION FAILED ");
//        return mapper.readValue(response.body().string(), TokenModel.class).getAccess_token();
//    }

    private String getApikeySecretAuth() {
        String apiKey = System.getProperty("authApiKey", "defaultApiKeyValue");
        assertTrue(StringUtils.isNotBlank(apiKey), "INVALID API KEY");
        String apiSecret = System.getProperty("authApiSecret", "defaultApiSecretValue");
        assertTrue(StringUtils.isNotBlank(apiSecret), "INVALID API SECRET");
        String combinedKeySecret = apiKey + ":" + apiSecret;
        return "Apikey " + Base64.getEncoder().encodeToString(combinedKeySecret.getBytes());
    }

    protected abstract String getSwaggerFilePath();

    @AfterAll
    public void tearDown() throws Exception {
        deleteDirectory(new File(GENERATION_OUTPUT_DIR));
        deleteDirectory(new File(COMPILED_OUTPUT_DIR));
    }

    private static void deleteDirectory(File directory) throws Exception {
        if (directory.exists()) {
            Files.walk(directory.toPath())
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    public void verifyErrorResponse(InvocationTargetException exception, String errorMsgExpected, String errorCodeExpected) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Throwable cause = exception.getCause();
        Class<?> exceptionClass = classMap.get("io.swagger.client.ApiException");
        Object exceptionObject = assertInstanceOf(exceptionClass, cause);
        Method errorResponseBodyMethod = exceptionClass.getMethod("getResponseBody");
        String responseBody = (String) errorResponseBodyMethod.invoke(exceptionObject);
        Class<?> errorResponseClass = classMap.get("io.swagger.client.model.ErrorResponse");
        Object errorResponse = mapper.readValue(responseBody, errorResponseClass);
        Method errorListMethod = errorResponseClass.getMethod("getErrors");
        List<?> errorList = (List<?>) errorListMethod.invoke(errorResponse);
        Object errorDetails = errorList.getFirst();
        Class<?> errorListClass = classMap.get("io.swagger.client.model.ErrorDetail");
        assertEquals(errorMsgExpected, errorListClass.getMethod("getErrorMessage").invoke(errorDetails).toString());
        assertEquals(errorCodeExpected, errorListClass.getMethod("getStatus").invoke(errorDetails).toString());
    }

    public void verifyErrorResponseV2(InvocationTargetException exception, String id, String errorMsgExpected, String errorCodeExpected) throws JsonProcessingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Throwable cause = exception.getCause();
        Class<?> exceptionClass = classMap.get("io.swagger.client.ApiException");
        Object exceptionObject = assertInstanceOf(exceptionClass, cause);
        Method errorResponseBodyMethod = exceptionClass.getMethod("getResponseBody");
        String responseBody = (String) errorResponseBodyMethod.invoke(exceptionObject);
        Class<?> errorResponseClass = classMap.get("io.swagger.client.model.ErrorResponse");
        Object errorResponse = mapper.readValue(responseBody, errorResponseClass);
        Method errorListMethod = errorResponseClass.getMethod("getErrors");
        String responseId = errorResponseClass.getMethod("getId").invoke(errorResponse).toString();
        List<?> errorList = (List<?>) errorListMethod.invoke(errorResponse);
        Object errorDetails = errorList.getFirst();
        Class<?> errorListClass = classMap.get("io.swagger.client.model.ErrorDetail");
        assertEquals(id, responseId, "request id is not matched from response id");
        assertEquals(errorMsgExpected, errorListClass.getMethod("getDetail").invoke(errorDetails).toString(), "error details is not match");
        assertEquals(errorCodeExpected, errorListClass.getMethod("getCode").invoke(errorDetails).toString(), "error code is not matched");
    }
}
