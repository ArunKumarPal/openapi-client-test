package com.arun.swagger.example;

import com.arun.swagger.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SwaggerPetstoreTest  extends BaseTest {

    private static final String OPENAPI_SPEC_PATH = "https://petstore.swagger.io/v2/swagger.json";
    private Class<?> apiClass;
    private Object apiBearerTokenInstance;
    private Object apikeyApiInstance;

    @BeforeAll
    public void setup() throws Exception {
        super.setup();
        String apiClassName = "io.swagger.client.api.PetApi";
        apiClass = classMap.get(apiClassName);
        apiBearerTokenInstance = apiClass.getDeclaredConstructor().newInstance();
        apikeyApiInstance = apiClass.getDeclaredConstructor().newInstance();
        Method setClient = apiClass.getMethod("setApiClient", apiClient);
        setClient.invoke(apiBearerTokenInstance, apiClientBearerTokenInstance);
        setClient.invoke(apikeyApiInstance, apiClientApikeyInstance);
    }

    @Override
    protected String getSwaggerFilePath() {
        return OPENAPI_SPEC_PATH;
    }

    @Test
    public void testGetPet() throws Exception {

        Method getPetById = apiClass.getMethod("getPetById", Long.class);
        try {

            Object responseGeocode = getPetById.invoke(apikeyApiInstance, 1L);
            assertNotNull(responseGeocode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPostPet() throws Exception {
        Class<?> petClass = classMap.get("io.swagger.client.model.Pet");
        Class<?> catagoryClass = classMap.get("io.swagger.client.model.Category");
        Object petRequest = petClass.getDeclaredConstructor().newInstance();
        Object catagoryRequest = catagoryClass.getDeclaredConstructor().newInstance();
        Method setCatagoryId = catagoryClass.getDeclaredMethod("setId", Long.class);
        Method setCatagoryName = catagoryClass.getDeclaredMethod("setName", String.class);
        setCatagoryId.invoke(catagoryRequest, 1L);
        setCatagoryName.invoke(catagoryRequest, "Dogs");
        Method setPetCategory = petClass.getDeclaredMethod("setCategory", catagoryClass);
        setPetCategory.invoke(petRequest, catagoryRequest);
        Method setPetName = petClass.getDeclaredMethod("setName", String.class);
        setPetName.invoke(petRequest, "Tommy");
        Method setPetId = petClass.getDeclaredMethod("setId", Long.class);
        setPetId.invoke(petRequest, 123L);
        Method addAddressesItem = petClass.getMethod("addPhotoUrlsItem", String.class);
        addAddressesItem.invoke(petRequest, "http://example.com/photo1");
        Method addPet = apiClass.getMethod("addPet", petClass);
        addPet.invoke(apikeyApiInstance, petRequest);

    }
}
