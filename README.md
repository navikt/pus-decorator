# pus-decorator
Decorates a static web application for use on nav.no

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
 
### proxy configuration
if the file `/proxy.json` exists in the docker container, it will be parsed and used to configure proxying against other services. It should have the following format:
```
[
  {
    "contextPath": "/api",
    "serviceName": "backend-api"
  },
  {
    "contextPath": "/log",
    "serviceName": "logger"
  },
  ...
]
```

 
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