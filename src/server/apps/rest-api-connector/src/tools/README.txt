SAMLTool is quick-n-dirty and has two features. It lets you validate a
SAML response (you probably don't care) and lets you sign a request that
your wrote yourself (this is important).

First, you need the various support jars. These can be found in xlib/jars
and QA will probably have their own copy, but here is what you need:

  -rw-rw-rw-  1 amorgan Domain Users  277722 2015-04-20  commons-httpclient-3.0-rc2.jar
  -rw-rw-rw-  1 amorgan Domain Users   31638 2015-04-21  commons-logging.jar
  -rw-rw-rw-  1 amorgan Domain Users  367021 12-07 14:48 esapi-2.1.0.jar
  -rw-rw-rw-  1 amorgan Domain Users  630341 12-07 14:56 joda-time-2.9.1.jar
  -rw-rw-rw-  1 amorgan Domain Users  489883 2015-04-21  log4j-1.2.17.jar
  -rw-rw-rw-  1 amorgan Domain Users  258278 12-01 16:20 not-yet-commons-ssl-0.3.9.jar
  -rw-rw-rw-  1 amorgan Domain Users 1413830 12-07 15:49 opensaml-2.6.5.jar
  -rw-rw-rw-  1 amorgan Domain Users  574362 12-07 15:49 openws-1.5.5.jar
  -rw-rw-rw-  1 amorgan Domain Users   32119 2015-04-21  slf4j-api-1.7.10.jar
  -rw-rw-rw-  1 amorgan Domain Users  220536 02-25 15:05 xml-apis.jar
  -rw-rw-rw-  1 amorgan Domain Users  655992 12-07 15:49 xmlsec-1.5.7.jar
  -rw-rw-rw-  1 amorgan Domain Users  672540 12-07 15:49 xmltooling-1.4.5.jar

The version numbers aren't essential, but the files are. You need all of these.

To build:

> buildsamltool.bat "<path to support jars>"

To run:

> runsamltool.bat "<path to support jars>" (VALIDATE <response file name | SIGN <query file name> <keystore file name> <key id> <password>)

VALIDATE will validate the SAML response saved in the given
file. Right now validation consists of nothing more than parsing the
file and verifying the signature. An unhelpful exception will be
thrown if there is a failure.

SIGN will sign a query that you have written. The PDP requires signed
queries, so this is important. A sample query has been checked
in. Feel free to modify it. A perfectly reasonable keystore is
dcc-keystore. In that is a cert with an id of "dcc" and the password
"password". So, to sign this you'd do

> runsamletool.bat "<path to support jars>" SIGN sample.request.xml c:/somepathto/dcc-keystore.jks dcc password

If the request you give is malformed in some way you'll get, you guess it, an
unhelpful exception.
