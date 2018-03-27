# pus-decorator
Decorates a static web application for use on nav.no

## usage
convert your application like this:

https://github.com/navikt/jobbsokerkompetanse/commit/a24450465ee4b49e7772f7739c8249ae95a59a97


## configuration
configure using the following environment variables:
 - APPLICATION_NAME (required)
 - APPRES_CMS_URL (required) example: https://appres.nav.no
 - FOOTER_TYPE (optional)
 https://github.com/navikt/pus-decorator/blob/master/src/main/java/no/nav/pus/decorator/FooterType.java
 - ENVIORMENT_PREFIX (optional) sets prefiks for /environment.js deafults to enviorment
 - PUBLIC_ (optional)  that starts with gets eksposed as /environment.js with ENVIORMENT_PREFIX
