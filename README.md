# Maskinporten CLI

A command line tool for creating clients and keys in [Maskinporten](https://samarbeid.digdir.no/maskinporten/maskinporten/25).

## Prerequisites

* A [virksomhetssertifikat](https://docs.digdir.no/eformidling_virksomhetssertifikat.html) to authenticate with
Maskinporten.
* An admin client issued by Digdir: https://docs.digdir.no/maskinporten_sjolvbetjening_api.html#tilgang-administrasjon-av-api

## Run

```bash
# Optionally, change password of virksomhetssertifikat as it's highly sensitive
./p12-to-jks.sh in.p12 out.p12 my_org_test

# Just ignore the produced JKS file, we actually only need the p12 file.

# Run application
export MASKINPORTEN_ENV=dev # optional, defaults to dev environment (ver2 for Maskinporten)
export MASKINPORTEN_KEYSTORE_PATH=/tmp/my-cert.p12 # p12 file from last step
export MASKINPORTEN_KEYSTORE_PASSWORD=some_keystore_password # the password you set when running p12-to-jks.sh
export MASKINPORTEN_KEY_ALIAS=my_org_test # alias of key in keystore
export MASKINPORTEN_KEY_PASSWORD=some_key_password # the password you set when running p12-to-jks.sh
export MASKINPORTEN_ADMIN_CLIENT_ID=someclient # client ID received from Maskinporten
export MASKINPORTEN_CONSUMER_ORG=123456789 # your organization number in Enhetsregisteret

./gradlew build

# Commands:
./maskinporten create client --name myclient --description "Test client" --scopes scope1,scope2
./maskinporten create key --client-id my_client_id
./maskinporten list keys --client-id my_client_id
./maskinporten token --scope scope1 --client-id my_client_id

# Or, setup Intellij run configuration doing the same
```

Generating Maskinporten token for a client with its own client key (from `create key`):
```bash
# You need the same config options as above, but replace the keystore,
# key alias and passwords environment variables with the values from
# the "create key" command.
   
./maskinporten token --scope scope1 --client-id my_client_id --key-id my_key_id
```

# Various links and resources

* [DigDir - how does Maskinporten work?](https://docs.digdir.no/maskinporten_auth_server-to-server-oauth2.html)
* [Folkeregisteret - Get stated with Maskinporten](https://skatteetaten.github.io/folkeregisteret-api-dokumentasjon/maskinporten/)
* [Maskinporten guide for API consumers](https://docs.digdir.no/maskinporten_guide_apikonsument.html)
* [Maskinporten self-service API](https://docs.digdir.no/maskinporten_sjolvbetjening_api.html)
* OAuth well-known endpoints for [IdPorten](https://docs.digdir.no/oidc_func_wellknown.html) and [Maskinporten](https://docs.digdir.no/maskinporten_func_wellknown.html)
* [Admin-API for OIDC integrations](https://docs.digdir.no/oidc_api_admin.html), i.e. how establish and modify clients.
