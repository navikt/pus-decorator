# pus-decorator
Decorates a static web application for use on nav.no

## supported functionality
 - dynamic decoration of static web applications at runtime with resources from appres.nav.no
 - [reverse proxy](https://github.com/navikt/pus-decorator#proxy-configuration) for api calls
 - [feature toggling](https://github.com/navikt/pus-decorator#apifeature) with unleash
 - [environment variables](https://github.com/navikt/pus-decorator#environmentjs) exposed to frontend
 - CSP (content security policy) for added security
 - enforced login

pus-decorator comes with much functionality out of the box, but may be deactivated using the following environment variables:
 - `DISABLE_DECORATOR`
 - `DISABLE_PROXY`
 - `DISABLE_UNLEASH`


## usage
convert your application like this:

https://github.com/navikt/jobbsokerkompetanse/commit/a24450465ee4b49e7772f7739c8249ae95a59a97


## configuration
configure using the following environment variables:
 - APPLICATION_NAME (required)
 - APPRES_CMS_URL (required) example: https://appres.nav.no
 - HEADER_TYPE (optional:https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/HeaderType.java)
 - FOOTER_TYPE (optional)
 https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/FooterType.java
 - ENVIRONMENT_CONTEXT (optional) sets context name for `/environment.js`. Defaults to application name (see below)
 - CONTEXT_PATH (optional) if set is the contextpath of the application. Defaults to APPLICATION_NAME
 - CONTENT_URL (optional) application to be decorated will be fetched from this url. If not set, the application is read from local disk
 - UNLEASH_API_URL (optional) unleash server url. Defaults to `https://unleashproxy.nais.oera.no/api/`
 - OIDC_LOGIN_URL (optional) url to be used to login users with AzureAD B2C. If undefined, users are not logged in by pus-decorator 
 
### proxy configuration
if the file `/proxy.json` exists in the docker container, it will be parsed and used to configure proxying against other services. It should have the following format:
```
[
  {
    "contextPath": "/backend",
    "baseUrl": "http://my-backend-api"
  },
  {
    "contextPath": "/logger",
    "baseUrl": "http://my-logger"
    "requestRewrite": "REMOVE_CONTEXT_PATH"
  },
  {
    "contextPath": "/example",
    "baseUrl": "https://www.example.com"
    "requestRewrite": "REMOVE_CONTEXT_PATH",
    "pingRequestPath": "/ping" 
  }
  ...
]
```
the above example will create the following proxy-setup:

| end-user request                                         | proxied request url                       |
|----------------------------------------------------------|-------------------------------------------|
| https://my-decorated-app.com/backend/hello-world         | http://my-backend-api/backend/hello-world |
| https://my-decorated-app.com/logger/log                  | http://my-logger/log                      |
| https://my-decorated-app.com/example/a/great/example     | https://www.example.com/a/great/example   |


| context-path | ping request url                       |
|--------------|----------------------------------------|
| /backend     | http://my-backend-api/backend/api/ping |
| /logger      | http://my-logger/api/ping              |
| /example     | https://www.example.com/ping           |


### Multiple single page applications
If the file `/spa.config.json` exists in the docker container, it will be parsed and used to configure 
specified url patterns to forward targets. All applications will be decorated. It should have the following format:
```
[
    {
        "forwardTarget": "app-1.html",
        "urlPattern": "/app1"
    },
    {
        "forwardTarget": "smaller-app.html",
        "urlPattern": "/small/app"
    }
] 
```

If no config exists, urlpattern `/*` forwards to `index.html` and `/demo/*` forwards to `/demo/index.html`.

 
## /environment.js
endpoint that exposes system properties/environment variables matching this regex: `^PUBLIC_.+`. Example:

```
GET /myapp/environment.js

myapp={};
myapp.my_property='content of PUBLIC_MY_PROPERTY';
myapp.another_property='content of PUBLIC_ANOTHER_PROPERTY';
```


## /api/feature
endpoint that evaluates a list of feature toggles using unleash.

Example that evaluates `toggle-a`, `toggle-b` and `toggle-c`
```
GET /myapp/api/feature?feature=toggle-a&feature=toggle-b&feature=toggle-c
```