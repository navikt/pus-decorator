# pus-decorator
Decorates a static web application for use on nav.no

## supported functionality
 - dynamic decoration of static web applications at runtime with resources from appres.nav.no / www.nav.no/dekoratoren
 - [multiple single page apps](https://github.com/navikt/pus-decorator#multiple-single-page-applications) 
 - [reverse proxy](https://github.com/navikt/pus-decorator#proxy-configuration) for api calls
 - [feature toggling](https://github.com/navikt/pus-decorator#apifeature) with unleash
 - [environment variables](https://github.com/navikt/pus-decorator#environmentjs) exposed to frontend
 - CSP (content security policy) for added security
 - enforced login
 - OIDC token validation and security level authorization

pus-decorator comes with much functionality out of the box, but may be deactivated using the following environment variables:
 - `DISABLE_DECORATOR`
 - `DISABLE_PROXY`
 - `DISABLE_UNLEASH`
 - `DISABLE_FRONTEND_LOGGER`

BETA:
Use the environment variabel `GZIP_ENABLED` in the `Dockerfile` to turn on gzipping for files: `ENV GZIP_ENABLED=true`.

## usage
use this image as baseimage and set the required configuration parameters. 
The new image will serve a static web application from `index.html` 
found in directory `/app` with the enabled functionality.

convert your application like this:

https://github.com/navikt/jobbsokerkompetanse/commit/a24450465ee4b49e7772f7739c8249ae95a59a97


## configuration

pus-decorator checks for a configuration file at `/decorator.yaml` unless a different path is explicitly set through
the `CONFIGURATION_LOCATION` environment variable . 
If either of these files are absent or if some required attributes are undefined, pus-decorator will apply sane and safe default.
Using a minimalistic configuration file, only overriding or extending default behaviour is therefore fine.

Please see  the 
[example configuration file](https://github.com/navikt/pus-decorator/blob/master/decorator.example.yaml)


typically, you add the following line to the `Dockerfile` to add `decorator.yaml` to the docker-container:

```ADD decorator.yaml /decorator.yaml```


### environment variables

in addition to the configuration file the following environment variables are supported:
 - APPLICATION_NAME (required)
 - APPRES_CMS_URL (required) example: https://appres.nav.no
 - HEADER_TYPE (optional:https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/HeaderType.java)
 - FOOTER_TYPE (optional)
 https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/FooterType.java
 - EXTRA_DECORATOR_PARAMS (optional) forward extra parameters to the decorator based on https://github.com/navikt/nav-dekoratoren/#parametere. Example: &chatbot=true&feedback=false
 - ENVIRONMENT_CONTEXT (optional) sets context name for `/environment.js` and `/api/feature.js`. Defaults to application name (see below)
 - CONTEXT_PATH (optional) if set is the contextpath of the application. Defaults to APPLICATION_NAME
 - CONTENT_URL (optional) application to be decorated will be fetched from this url. If not set, the application is read from local disk
 - UNLEASH_API_URL (optional) unleash server url. Defaults to `https://unleashproxy.nais.oera.no/api/` 
 - CONFIGURATION_LOCATION (optional) set path of configuration file, this will override the default which is set to `/decorator.yaml`
 - DISABLE_PRAGMA_HEADER=true|false (optional) removes the `Pragma: no-cache` header from responses
 - ALLOW_CLIENT_STORAGE=true|false (optional) if true, removes the `no-store, must-revalidate` directives from the `Cache-Control` header

### proxy configuration
Please see the 
[example configuration file](https://github.com/navikt/pus-decorator/blob/master/decorator.example.yaml)
 
### Multiple single page applications
Please see  the 
[example configuration file](https://github.com/navikt/pus-decorator/blob/master/decorator.example.yaml)

### enforced login
If your application requires the user to be logged in, the pus-decorator can enforce this:
1. in `app-config.yaml`, set the `webproxy`-flag to `true` (see example below)
2. in `Dockerfile`, set the environment variable `OIDC_LOGIN_URL` to `/veilarbstepup/oidc` (see example below)

`app-config.yaml`:
```
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

 
```
