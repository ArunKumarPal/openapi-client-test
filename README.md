# OpenAPI Client Test Framework

A dynamic test framework for testing any Swagger/OpenAPI specification by automatically generating, compiling, and loading client code at runtime.

## Author

**Arun Kumar Pal**
- GitHub: [@ArunKumarPal](https://github.com/ArunKumarPal)
- Repository: [openapi-client-test](https://github.com/ArunKumarPal/openapi-client-test)

---

## 📋 Overview

This project provides a flexible testing framework that can test any Swagger/OpenAPI specification without manual client code generation. It dynamically:
1. Generates client code from OpenAPI/Swagger specifications
2. Compiles the generated code at runtime
3. Loads the compiled classes dynamically
4. Executes tests using reflection

The framework is ideal for API testing, integration testing, and validating OpenAPI specifications with real API calls.

---

## 🚀 Features

- **Dynamic Client Generation**: Automatically generates client code from any Swagger/OpenAPI specification
- **Runtime Compilation**: Compiles generated code on-the-fly without manual build steps
- **Reflection-Based Testing**: Uses Java reflection to invoke API methods dynamically
- **Authentication Support**: Supports multiple authentication methods:
  - Bearer Token authentication (OAuth2)
  - API Key authentication
  - Basic authentication
- **Flexible Configuration**: Configure base URLs, API keys, and secrets via system properties
- **Sample Tests Included**: Pre-configured test for Swagger Petstore API

---

## 🛠️ Technology Stack

- **Java 21**
- **Maven** - Build and dependency management
- **JUnit 5** - Testing framework
- **Swagger Codegen V3** - Client code generation
- **OkHttp** - HTTP client
- **Jackson** - JSON processing
- **Gson** - Additional JSON support

---

## 📁 Project Structure

```
openapi-client-test/
├── src/
│   ├── main/java/com/arun/swagger/client/
│   │   ├── SwaggerClient.java              # Main client orchestrator
│   │   ├── SwaggerClientGenerator.java     # Code generation logic
│   │   ├── SwaggerSourceCompiler.java      # Runtime compilation
│   │   ├── SwaggerClientClassLoader.java   # Dynamic class loading
│   │   └── TokenModel.java                 # OAuth token model
│   └── test/java/com/arun/swagger/
│       ├── BaseTest.java                    # Base test class with setup
│       └── example/
│           └── SwaggerPetstoreTest.java     # Sample Petstore API test
├── target/
│   ├── generated-client/                    # Generated client code
│   └── compiled-client/                     # Compiled classes
└── pom.xml                                  # Maven configuration
```

---

## 🎯 Usage

### 1. Prerequisites

- Java 21 or higher
- Maven 3.6+
- Internet connection (for downloading Swagger specs and dependencies)

### 2. Installation

Clone the repository and build the project:

```bash
git clone https://github.com/ArunKumarPal/openapi-client-test.git
cd openapi-client-test
mvn clean install
```

### 3. Running Sample Tests

Run the included Swagger Petstore test:

```bash
mvn test -Dtest=SwaggerPetstoreTest
```

### 4. Creating Your Own Tests

Follow these steps to add your own API tests to the framework:

#### Step 1: Get Your OpenAPI/Swagger Specification

First, obtain the URL or file path to your API's OpenAPI/Swagger specification:
- URL: `https://api.example.com/swagger.json` or `https://api.example.com/openapi.yaml`
- Local file: `src/test/resources/my-api-spec.json`

#### Step 2: Create a New Test Class

Create a new test class in `src/test/java/com/arun/swagger/` following the naming convention: `Swagger<ServiceName>Test.java`

**Example: `SwaggerUserServiceTest.java`**

```java
package com.arun.swagger.userservice;

import com.arun.swagger.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SwaggerUserServiceTest extends BaseTest {
    
    // Your OpenAPI/Swagger specification URL or file path
    private static final String OPENAPI_SPEC_PATH = "https://api.example.com/v1/swagger.json";
    
    // Class references for your API
    private Class<?> userApiClass;
    private Object userApiInstance;

    @BeforeAll
    public void setup() throws Exception {
        // Call parent setup to generate and compile client
        super.setup();
        
        // Load your API class (check generated code for exact class name)
        String apiClassName = "io.swagger.client.api.UserApi";
        userApiClass = classMap.get(apiClassName);
        
        // Create API instance
        userApiInstance = userApiClass.getDeclaredConstructor().newInstance();
        
        // Set API client with authentication
        Method setClient = userApiClass.getMethod("setApiClient", apiClient);
        setClient.invoke(userApiInstance, apiClientBearerTokenInstance); // or apiClientApikeyInstance
    }

    @Override
    protected String getSwaggerFilePath() {
        return OPENAPI_SPEC_PATH;
    }

    @Test
    public void testGetUser() throws Exception {
        // Your test logic here
        Method getUserById = userApiClass.getMethod("getUserById", String.class);
        Object response = getUserById.invoke(userApiInstance, "user123");
        assertNotNull(response);
    }
}
```

#### Step 3: Identify API Classes and Methods

After the first test run, check the generated code in `target/generated-client/src/main/java/io/swagger/client/` to identify:
- API class names (e.g., `UserApi`, `ProductApi`)
- Model class names (e.g., `User`, `Product`)
- Method names and parameters

**Tip:** Run a simple test first to generate the client code, then explore the generated files.

#### Step 4: Write Test Methods

Use Java reflection to invoke API methods and test responses:

##### Example: GET Request Test
```java
@Test
public void testGetUserById() throws Exception {
    Method getUserById = userApiClass.getMethod("getUserById", String.class);
    Object response = getUserById.invoke(userApiInstance, "user123");
    
    assertNotNull(response);
    
    // Access response fields using reflection
    Class<?> userClass = classMap.get("io.swagger.client.model.User");
    Method getId = userClass.getMethod("getId");
    String userId = (String) getId.invoke(response);
    assertEquals("user123", userId);
}
```

##### Example: POST Request Test
```java
@Test
public void testCreateUser() throws Exception {
    // Get model class
    Class<?> userClass = classMap.get("io.swagger.client.model.User");
    Object userRequest = userClass.getDeclaredConstructor().newInstance();
    
    // Set request fields
    Method setUsername = userClass.getDeclaredMethod("setUsername", String.class);
    setUsername.invoke(userRequest, "john.doe");
    
    Method setEmail = userClass.getDeclaredMethod("setEmail", String.class);
    setEmail.invoke(userRequest, "john.doe@example.com");
    
    // Call API method
    Method createUser = userApiClass.getMethod("createUser", userClass);
    Object response = createUser.invoke(userApiInstance, userRequest);
    
    assertNotNull(response);
}
```

##### Example: PUT Request Test
```java
@Test
public void testUpdateUser() throws Exception {
    Class<?> userClass = classMap.get("io.swagger.client.model.User");
    Object userRequest = userClass.getDeclaredConstructor().newInstance();
    
    Method setId = userClass.getDeclaredMethod("setId", String.class);
    setId.invoke(userRequest, "user123");
    
    Method setUsername = userClass.getDeclaredMethod("setUsername", String.class);
    setUsername.invoke(userRequest, "john.doe.updated");
    
    Method updateUser = userApiClass.getMethod("updateUser", String.class, userClass);
    Object response = updateUser.invoke(userApiInstance, "user123", userRequest);
    
    assertNotNull(response);
}
```

##### Example: DELETE Request Test
```java
@Test
public void testDeleteUser() throws Exception {
    Method deleteUser = userApiClass.getMethod("deleteUser", String.class);
    deleteUser.invoke(userApiInstance, "user123");
    // No exception means success
}
```

##### Example: Test with Query Parameters
```java
@Test
public void testSearchUsers() throws Exception {
    Method searchUsers = userApiClass.getMethod("searchUsers", String.class, Integer.class, Integer.class);
    Object response = searchUsers.invoke(userApiInstance, "john", 0, 10); // query, page, size
    
    assertNotNull(response);
    
    // If response is a list
    List<?> userList = (List<?>) response;
    assertTrue(userList.size() > 0);
}
```

#### Step 5: Configure Authentication (Optional)

The framework supports multiple authentication methods:

##### API Key Authentication (Default)
```bash
mvn test -DauthApiKey=your-api-key -DauthApiSecret=your-api-secret
```

##### Bearer Token Authentication
Uncomment the token generation code in `BaseTest.java`:

```java
// Uncomment these lines in BaseTest.java setup() method:
authTokenValue = authTokenValue == null ? getAccessToken() : authTokenValue;
Method setToken = apiClient.getMethod("setAccessToken", String.class);
setToken.invoke(apiClientBearerTokenInstance, authTokenValue);

// Uncomment the getAccessToken() method:
private String getAccessToken() throws IOException {
    String apiKey = System.getProperty("authApiKey");
    String apiSecret = System.getProperty("authApiSecret");
    String baseUrl = System.getProperty("baseUrl");
    baseUrl = baseUrl != null && baseUrl.isBlank() ? baseUrl : BASE_PATH;
    OkHttpClient client = new OkHttpClient();
    String combinedKeySecret = apiKey + ":" + apiSecret;
    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
    RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&scope=default".getBytes());
    Request request = new Request.Builder()
            .url(baseUrl + "/auth/v2/token")
            .method("POST", body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(combinedKeySecret.getBytes()))
            .build();
    Response response = client.newCall(request).execute();
    assert response.body() != null;
    assertTrue(response.isSuccessful(), "TOKEN GENERATION FAILED ");
    return mapper.readValue(response.body().string(), TokenModel.class).getAccess_token();
}
```

Then run with credentials:
```bash
mvn test -DauthApiKey=your-api-key -DauthApiSecret=your-api-secret -DbaseUrl=https://api.example.com
```

#### Step 6: Run Your Tests

Run your new test class:

```bash
# Run your specific test class
mvn test -Dtest=SwaggerUserServiceTest

# Run all tests for your service
mvn test -DserviceName=UserService

# Run with custom configuration
mvn test -DserviceName=UserService \
  -DbaseUrl=https://api.example.com \
  -DauthApiKey=your-api-key \
  -DauthApiSecret=your-api-secret
```

#### Step 7: Organize Your Tests

Create a package structure for better organization:

```
src/test/java/com/arun/swagger/
├── BaseTest.java
├── example/
│   └── SwaggerPetstoreTest.java
├── userservice/
│   ├── SwaggerUserServiceTest.java
│   ├── UserAuthenticationTest.java
│   └── UserManagementTest.java
├── productservice/
│   └── SwaggerProductServiceTest.java
└── orderservice/
    └── SwaggerOrderServiceTest.java
```

#### Step 8: Handle Complex Scenarios

##### Testing with Nested Objects
```java
@Test
public void testCreateOrder() throws Exception {
    // Create nested objects
    Class<?> orderClass = classMap.get("io.swagger.client.model.Order");
    Class<?> addressClass = classMap.get("io.swagger.client.model.Address");
    Class<?> itemClass = classMap.get("io.swagger.client.model.OrderItem");
    
    // Create address
    Object address = addressClass.getDeclaredConstructor().newInstance();
    Method setStreet = addressClass.getDeclaredMethod("setStreet", String.class);
    setStreet.invoke(address, "123 Main St");
    
    // Create order item
    Object item = itemClass.getDeclaredConstructor().newInstance();
    Method setProductId = itemClass.getDeclaredMethod("setProductId", String.class);
    setProductId.invoke(item, "prod123");
    
    // Create order with nested objects
    Object order = orderClass.getDeclaredConstructor().newInstance();
    Method setAddress = orderClass.getDeclaredMethod("setAddress", addressClass);
    setAddress.invoke(order, address);
    
    Method addItem = orderClass.getMethod("addItemsItem", itemClass);
    addItem.invoke(order, item);
    
    // Call API
    Method createOrder = orderApiClass.getMethod("createOrder", orderClass);
    Object response = createOrder.invoke(orderApiInstance, order);
    assertNotNull(response);
}
```

##### Testing Error Responses
```java
@Test
public void testGetUserNotFound() throws Exception {
    Method getUserById = userApiClass.getMethod("getUserById", String.class);
    
    try {
        getUserById.invoke(userApiInstance, "invalid-user-id");
        fail("Expected exception for invalid user");
    } catch (InvocationTargetException exception) {
        // Verify error response using BaseTest helper method
        verifyErrorResponse(exception, "User not found", "404");
    }
}
```

##### Testing with Custom Headers
```java
@BeforeAll
public void setup() throws Exception {
    super.setup();
    
    // Add custom headers to API client
    Method addDefaultHeader = apiClient.getMethod("addDefaultHeader", String.class, String.class);
    addDefaultHeader.invoke(apiClientApikeyInstance, "X-Custom-Header", "custom-value");
    addDefaultHeader.invoke(apiClientApikeyInstance, "X-Request-ID", "test-request-123");
}
```

##### Testing List Operations
```java
@Test
public void testGetAllUsers() throws Exception {
    Method getAllUsers = userApiClass.getMethod("getAllUsers");
    Object response = getAllUsers.invoke(userApiInstance);
    
    // Cast to list
    List<?> userList = (List<?>) response;
    assertNotNull(userList);
    assertTrue(userList.size() > 0);
    
    // Verify first user
    Object firstUser = userList.get(0);
    Class<?> userClass = classMap.get("io.swagger.client.model.User");
    Method getUsername = userClass.getMethod("getUsername");
    String username = (String) getUsername.invoke(firstUser);
    assertNotNull(username);
}
```

#### Step 9: Test Locally

Before committing, verify your tests work:

```bash
# Clean and run all tests
mvn clean test

# Run specific service tests
mvn test -DserviceName=UserService

# Run with debug output
mvn test -DserviceName=UserService -X

# Run specific test method
mvn test -Dtest=SwaggerUserServiceTest#testGetUser
```

#### Step 10: Commit and Integrate

Once your tests are working:

```bash
# Add your test files
git add src/test/java/com/arun/swagger/userservice/

# Commit your changes
git commit -m "Add UserService API tests"

# Push to repository
git push origin main

# Tests will run automatically in CI/CD pipeline
```

---

### Quick Reference: Common Test Patterns

#### Pattern 1: Simple GET Request
```java
Method getMethod = apiClass.getMethod("getResource", String.class);
Object response = getMethod.invoke(apiInstance, "resource-id");
assertNotNull(response);
```

#### Pattern 2: POST with Request Body
```java
Class<?> requestClass = classMap.get("io.swagger.client.model.Request");
Object request = requestClass.getDeclaredConstructor().newInstance();
// Set request fields using reflection...
Method postMethod = apiClass.getMethod("createResource", requestClass);
Object response = postMethod.invoke(apiInstance, request);
assertNotNull(response);
```

#### Pattern 3: PUT/Update Request
```java
Method updateMethod = apiClass.getMethod("updateResource", String.class, requestClass);
Object response = updateMethod.invoke(apiInstance, "resource-id", requestObject);
```

#### Pattern 4: DELETE Request
```java
Method deleteMethod = apiClass.getMethod("deleteResource", String.class);
deleteMethod.invoke(apiInstance, "resource-id");
// No exception means successful deletion
```

#### Pattern 5: List/Collection Response
```java
Method listMethod = apiClass.getMethod("listResources");
Object response = listMethod.invoke(apiInstance);
List<?> items = (List<?>) response;
assertTrue(items.size() > 0);
```

#### Pattern 6: Error Handling
```java
try {
    method.invoke(apiInstance, invalidParams);
    fail("Expected ApiException");
} catch (InvocationTargetException e) {
    verifyErrorResponse(e, "Expected error message", "400");
}
```

#### Pattern 7: Pagination
```java
Method listMethod = apiClass.getMethod("listResources", Integer.class, Integer.class);
Object response = listMethod.invoke(apiInstance, 0, 10); // page, size
```

#### Step 3: Configure Base URL (Optional)

Override the base URL at runtime:

```bash
mvn test -DbaseUrl=https://api.example.com
```

---

## 📝 Example: Swagger Petstore Test

The included `SwaggerPetstoreTest.java` demonstrates:

### GET Request Example
```java
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
```

### POST Request Example
```java
@Test
public void testPostPet() throws Exception {
    Class<?> petClass = classMap.get("io.swagger.client.model.Pet");
    Class<?> catagoryClass = classMap.get("io.swagger.client.model.Category");
    
    // Create category
    Object catagoryRequest = catagoryClass.getDeclaredConstructor().newInstance();
    Method setCatagoryId = catagoryClass.getDeclaredMethod("setId", Long.class);
    Method setCatagoryName = catagoryClass.getDeclaredMethod("setName", String.class);
    setCatagoryId.invoke(catagoryRequest, 1L);
    setCatagoryName.invoke(catagoryRequest, "Dogs");
    
    // Create pet
    Object petRequest = petClass.getDeclaredConstructor().newInstance();
    Method setPetCategory = petClass.getDeclaredMethod("setCategory", catagoryClass);
    setPetCategory.invoke(petRequest, catagoryRequest);
    Method setPetName = petClass.getDeclaredMethod("setName", String.class);
    setPetName.invoke(petRequest, "Tommy");
    Method setPetId = petClass.getDeclaredMethod("setId", Long.class);
    setPetId.invoke(petRequest, 123L);
    
    // Add pet
    Method addPet = apiClass.getMethod("addPet", petClass);
    addPet.invoke(apikeyApiInstance, petRequest);
}
```

---

## 🔧 Configuration

### System Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `serviceName` | Service name pattern for test execution | `*` (all tests) | `PetStore`, `UserService` |
| `baseUrl` | API base URL | Swagger spec base path | `https://api.example.com` |
| `authApiKey` | API key for authentication | `defaultApiKeyValue` | `your-api-key` |
| `authApiSecret` | API secret for authentication | `defaultApiSecretValue` | `your-api-secret` |

### Running Tests with Parameters

```bash
# Run specific service tests
mvn test -DserviceName=PetStore

# Run with custom base URL
mvn test -DserviceName=UserService -DbaseUrl=https://api.example.com

# Run with authentication
mvn test -DserviceName=OrderService \
  -DbaseUrl=https://api.example.com \
  -DauthApiKey=your-key \
  -DauthApiSecret=your-secret

# Run all tests (default)
mvn test
```

### Maven Configuration

Key dependencies in `pom.xml`:
- `swagger-codegen-cli` - Client generation
- `junit-jupiter` - Testing
- `okhttp` - HTTP client
- `gson` - JSON processing
- `jackson` - JSON/XML binding

#### Surefire Plugin Configuration
The project uses Maven Surefire Plugin to support service-specific test execution:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M7</version>
    <configuration>
        <includes>
            <include>**/BaseTests.java</include>
            <include>**/${serviceName}*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

This allows you to run tests by service name pattern, making it easy to organize and execute tests in CI/CD pipelines.

---

## 🎨 Key Components

### SwaggerClient
Main orchestrator that coordinates generation, compilation, and class loading.

### SwaggerClientGenerator
Generates client code from OpenAPI/Swagger specifications using Swagger Codegen.

### SwaggerSourceCompiler
Compiles generated Java source files at runtime using Java Compiler API.

### SwaggerClientClassLoader
Custom class loader for loading compiled classes dynamically.

### BaseTest
Abstract base class providing:
- Client generation and setup
- Authentication configuration
- Error response validation helpers
- Cleanup after tests

---

## 🧪 Testing Different APIs

To test your own API:

1. Get the OpenAPI/Swagger specification URL
2. Create a new test class extending `BaseTest`
3. Override `getSwaggerFilePath()` to return your spec URL
4. Implement test methods using reflection
5. Run tests with appropriate authentication parameters

---

## 🔄 CI/CD Integration

This framework is designed to easily integrate into your CI/CD pipelines, making it perfect for automated API testing in your development workflow.

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/ArunKumarPal/openapi-client-test.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        
        stage('Run API Tests') {
            steps {
                sh '''
                    mvn test -DserviceName=YourService \
                    -DbaseUrl=${API_BASE_URL} \
                    -DauthApiKey=${API_KEY} \
                    -DauthApiSecret=${API_SECRET}
                '''
            }
        }
    }
    
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
```

### GitHub Actions Example

```yaml
name: API Tests

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean install -DskipTests
    
    - name: Run API Tests
      run: |
        mvn test -DserviceName=YourService \
        -DbaseUrl=${{ secrets.API_BASE_URL }} \
        -DauthApiKey=${{ secrets.API_KEY }} \
        -DauthApiSecret=${{ secrets.API_SECRET }}
    
    - name: Publish Test Report
      uses: dorny/test-reporter@v1
      if: always()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit
```

### GitLab CI Example

```yaml
stages:
  - build
  - test

build:
  stage: build
  script:
    - mvn clean install -DskipTests

test:
  stage: test
  script:
    - mvn test -DserviceName=$SERVICE_NAME
      -DbaseUrl=$API_BASE_URL
      -DauthApiKey=$API_KEY
      -DauthApiSecret=$API_SECRET
  artifacts:
    reports:
      junit: target/surefire-reports/*.xml
```

### Running Specific Service Tests

The framework supports running specific service tests using the `serviceName` parameter:

```bash
# Run all tests for a specific service
mvn test -DserviceName=PetStore

# Run specific service tests with custom configuration
mvn test -DserviceName=UserService \
  -DbaseUrl=https://api.example.com \
  -DauthApiKey=your-key \
  -DauthApiSecret=your-secret
```

The Maven Surefire plugin is configured to run tests matching the pattern: `**/${serviceName}*Test.java`

### Integrating into Your Code Repository

This framework is designed to be Git-friendly and can be easily integrated into your existing repositories:

#### Option 1: Submodule Approach
```bash
# Add as a Git submodule
cd your-project-repo
git submodule add https://github.com/ArunKumarPal/openapi-client-test.git api-tests
git submodule update --init --recursive

# Run tests from your repository
cd api-tests
mvn test -DserviceName=YourService
```

#### Option 2: Direct Integration
```bash
# Clone and copy test structure into your repository
git clone https://github.com/ArunKumarPal/openapi-client-test.git
cp -r openapi-client-test/src/test/java/com/arun/swagger your-project/src/test/java/
cp -r openapi-client-test/src/main/java/com/arun/swagger your-project/src/main/java/

# Add dependencies to your pom.xml
# Run tests as part of your build
mvn test -DserviceName=YourService
```

#### Option 3: Fork and Customize
```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/openapi-client-test.git
cd openapi-client-test

# Add your test cases
# Create service-specific test classes
# Commit and push your changes
git add .
git commit -m "Add tests for MyService"
git push origin main
```

### CI/CD Best Practices

1. **Environment Variables**: Store sensitive data (API keys, secrets) in CI/CD environment variables
2. **Service Segregation**: Use `serviceName` parameter to run different test suites
3. **Parallel Execution**: Run tests for different services in parallel jobs
4. **Test Reports**: Always publish test reports for visibility
5. **Fail Fast**: Configure pipelines to fail on test failures
6. **Scheduled Runs**: Set up cron jobs for regular API health checks

### Running Multiple Services

```bash
# Script to run tests for multiple services
#!/bin/bash

services=("PetStore" "UserService" "OrderService")

for service in "${services[@]}"
do
    echo "Running tests for $service..."
    mvn test -DserviceName=$service \
      -DbaseUrl=$BASE_URL \
      -DauthApiKey=$API_KEY \
      -DauthApiSecret=$API_SECRET
done
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 🐛 Known Issues & Limitations

- Requires Java 21 or higher
- Generated client code follows Swagger Codegen V3 conventions
- Some complex OpenAPI 3.0 features may require manual adjustments
- Authentication token generation code is commented out by default

---

## 📞 Support

For questions, issues, or suggestions:
- Open an issue on [GitHub Issues](https://github.com/ArunKumarPal/openapi-client-test/issues)
- Contact: Arun Kumar Pal

---

## 🙏 Acknowledgments

- [Swagger Codegen](https://github.com/swagger-api/swagger-codegen) - Client code generation
- [Swagger Petstore](https://petstore.swagger.io/) - Sample API for testing
- [OkHttp](https://square.github.io/okhttp/) - HTTP client library

---

**Happy Testing! 🎉**
