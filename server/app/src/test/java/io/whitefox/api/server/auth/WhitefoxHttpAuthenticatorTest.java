package io.whitefox.api.server.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.Header;
import io.whitefox.api.model.v1.generated.CreateMetastore;
import io.whitefox.api.model.v1.generated.MetastoreProperties;
import io.whitefox.api.model.v1.generated.SimpleAwsCredentials;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(WhitefoxHttpAuthenticatorTest.AuthenticationProfile.class)
public class WhitefoxHttpAuthenticatorTest {

    private final CreateMetastore createMetastore = new CreateMetastore()
            .name("glue_metastore_prod")
            .skipValidation(false)
            .type(CreateMetastore.TypeEnum.GLUE)
            .properties(new MetastoreProperties()
                    .catalogId("123")
                    .credentials(new SimpleAwsCredentials()
                            .awsAccessKeyId("access")
                            .awsSecretAccessKey("secret")
                            .region("eu-west1")));

    public static class AuthenticationProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("whitefox.server.authentication.enabled", "true", "whitefox.server.authentication.bearerToken", "myToken");
        }
    }

    public static class NoAuthenticationProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("whitefox.server.authentication.enabled", "false");
        }
    }

//    @Nested
//    @TestProfile(WhitefoxHttpAuthenticatorTest.AuthenticationProfile.class)
//    class TestAuthorized {

        @Test
        void expectDenied(){
            given()
                    .when()
                    .get("/whitefox-api/v1/metastores/{name}", createMetastore.getName())
                    .then()
                    .statusCode(401);
        }

        @Test
        void expectAcceptedWithAuth(){
            given()
                    .when()
                    .header(new Header("Authorization", "Bearer myToken"))
                    .get("/whitefox-api/v1/metastores/{name}", createMetastore.getName())
                    .then()
                    .statusCode(404);
        }
//    }

    @Nested
    @TestProfile(WhitefoxHttpAuthenticatorTest.NoAuthenticationProfile.class)
    class TestNotAuthorized {

        @Test
        void expectAccepted(){
            given()
                    .when()
                    .get("/whitefox-api/v1/metastores/{name}", createMetastore.getName())
                    .then()
                    .statusCode(404);
        }
    }


}
