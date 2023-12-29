# Whitefox authentication

By default, whitefox runs as a insecure server.

## Token authentication

We provide a simple way to secure your application using a token provided as an environment variable.

Two properties from application.properties are used to control the authentication methods of your instance:

```properties
whitefox.server.authentication.enabled=${WHITEFOX_SERVER_AUTHENTICATION_ENABLED:-false}
whitefox.server.authentication.bearerToken=${WHITEFOX_SERVER_AUTHENTICATION_BEARERTOKEN}
```

Setting:
- `WHITEFOX_SERVER_AUTHENTICATION_ENABLED=true`
- `WHITEFOX_SERVER_AUTHENTICATION_BEARERTOKEN=myToken`

in your deployment will trigger the simple token authentication mechanism.
Every request afterwards will have to have a header: `Authorization: Bearer myToken`(general case: `Authorization: Bearer ${WHITEFOX_SERVER_AUTHENTICATION_BEARERTOKEN}`), 
otherwise you will receive a 401 error authentication error on the client.

If the environment variables are omitted or if `WHITEFOX_SERVER_AUTHENTICATION_ENABLED=false` the app will continue in an insecure way.

## Basic authentication
TBD