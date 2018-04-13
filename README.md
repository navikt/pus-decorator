# pus-decorator
Decorates a static web application for use on nav.no

## usage
convert your application like this:

https://github.com/navikt/jobbsokerkompetanse/commit/a24450465ee4b49e7772f7739c8249ae95a59a97


## configuration
configure using the following environment variables:
 - APPLICATION_NAME (required)
 - APPRES_CMS_URL (required) example: https://appres.nav.no
 - HEADER_TYPE (optional: specify WITH_MENU or WITHOUT_MENU in order to show or hide the menu structure beneath the logo.)
 - FOOTER_TYPE (optional)
 https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/FooterType.java
 - ENVIRONMENT_CONTEXT (optional) sets context name for `/environment.js`. Defaults to application name (see below)
 
 
## /environment.js
endpoint that exposes system properties/environment variables matching this regex: `^PUBLIC_.+`. Example:

```
GET /myapp/environment.js

myapp={};
myapp.my_property='content of PUBLIC_MY_PROPERTY';
myapp.another_property='content of PUBLIC_ANOTHER_PROPERTY';
```
