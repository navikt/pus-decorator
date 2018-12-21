# pus-decorator
Decorates a static web application for use on nav.no

## supported functionality
 - dynamic decoration of static web applications at runtime with resources from appres.nav.no
 - [multiple single page apps](https://github.com/navikt/pus-decorator#multiple-single-page-applications) 
 - [reverse proxy](https://github.com/navikt/pus-decorator#proxy-configuration) for api calls
 - [feature toggling](https://github.com/navikt/pus-decorator#apifeature) with unleash
 - [environment variables](https://github.com/navikt/pus-decorator#environmentjs) exposed to frontend
 - CSP (content security policy) for added security
 - enforced login

pus-decorator comes with much functionality out of the box, but may be deactivated using the following environment variables:
 - `DISABLE_DECORATOR`
 - `DISABLE_PROXY`
 - `DISABLE_UNLEASH`
 - `DISABLE_FRONTEND_LOGGER`


## usage
use this image as baseimage and set the required configuration parameters. 
The new image will serve a static web application from `index.html` 
found in directory `/app` with the enabled functionality.

convert your application like this:

https://github.com/navikt/jobbsokerkompetanse/commit/a24450465ee4b49e7772f7739c8249ae95a59a97


## configuration
configure using the following environment variables:
 - APPLICATION_NAME (required)
 - APPRES_CMS_URL (required) example: https://appres.nav.no
 - HEADER_TYPE (optional:https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/HeaderType.java)
 - FOOTER_TYPE (optional)
 https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/FooterType.java
 - ENVIRONMENT_CONTEXT (optional) sets context name for `/environment.js` and `/api/feature.js`. Defaults to application name (see below)
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
    "contextPath": "/pingable",
    "baseUrl": "http://pingable-service"
    "pingRequestPath": "/" 
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
| https://my-decorated-app.com/pingable                    | http://pingable-service/pingable          |
| https://my-decorated-app.com/logger/log                  | http://my-logger/log                      |
| https://my-decorated-app.com/example/a/great/example     | https://www.example.com/a/great/example   |


| proxy-target     | ping request url                       |
|------------------|----------------------------------------|
| my-backend-api   | http://my-backend-api/backend/api/ping |
| pingable-service | http://pingable-service/               |
| my-logger        | http://my-logger/api/ping              |
| www.example.com  | https://www.example.com/ping           |

add the following line to the `Dockerfile` to add `proxy.json` to the docker-container:
`ADD proxy.json /proxy.json`

### Multiple single page applications
If the file `/spa.config.json` exists in the docker container, it will be parsed and used to configure 
specified url patterns to forward targets. Files as forward targets are expected under `/app` and will 
be decorated. The configuration should have the following format:
```
[
    {
        "forwardTarget": "/app-1.html",
        "urlPattern": "/app1/*"
    },
    {
        "forwardTarget": "/smaller-app.html",
        "urlPattern": "/small/app/*"
    }
] 
```

If no config exists, urlpattern `/*` forwards to `index.html` and `/demo/*` forwards to `/demo/index.html`.

### enforced login
If your application requires the user to be logged in, the pus-decorator can enforce this:
1. in `app-config.yaml`, depend on the fasit-resources `aad_b2c_clientid` and `aad_b2c_discovery` (see example below)
2. in `app-config.yaml`, set the `webproxy`-flag to `true` (see example below)
3. in `Dockerfile`, set the environment variable `OIDC_LOGIN_URL` to `/veilarbstepup/oidc` (see example below)

`app-config.yaml`:
```
fasitResources:  
  used:                         
  - alias: aad_b2c_clientid
    resourceType: credential
  - alias: aad_b2c_discovery
    resourceType: baseurl
    
webproxy: true
```
`Dockerfile`:
```
ENV OIDC_LOGIN_URL /veilarbstepup/oidc
``` 

## /environment.js
endpoint that exposes system properties/environment variables matching this regex: `^PUBLIC_.+`. Example:

```
GET /myapp/environment.js

myapp = window.myapp || {};
myapp['prop2']='content2';
myapp['prop1']='content1';
myapp['MY_ENV_VARIABLE']='tester environment';
myapp['prop']='content';
```


## Feature-evaluation 
Two different endpoints that evaluates a list of feature toggles using Unleash.

Example that evaluates `toggle-a`, `toggle-b` and `toggle-c`
```
GET /myapp/api/feature?feature=toggle-a&feature=toggle-b&feature=toggle-c

Returns application/json

{
    "toggle-b":false,
    "toggle-a":true,
    "toggle-c":false    
}


GET /myapp/api/feature.js?feature=toggle-a&feature=toggle-b&feature=toggle-c

Returns application/javascript

myapp = window.myapp || {};
myapp['toggle-b']=false;
myapp['toggle-a']=true;
myapp['toggle-c']=false;
 
```
