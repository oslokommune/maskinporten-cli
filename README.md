# Maskinporten CLI

A command line tool (CLI) for admin of clients and keys in [Maskinporten](https://samarbeid.digdir.no/maskinporten/maskinporten/25).

## Prerequisites

* A [virksomhetssertifikat](https://docs.digdir.no/eformidling_virksomhetssertifikat.html) to authenticate with
Maskinporten.
* An admin client issued by Digdir: https://docs.digdir.no/maskinporten_sjolvbetjening_api.html#tilgang-administrasjon-av-api

## Build the CLI application

The Maskinporten CLI is built using [Gradle](https://gradle.org/):

```bash
./gradlew build
```

## Configuration

Before using the Maskinporten CLI, you need to configure it using environment variables, as shown below:

Example configuration:
```bash
# Run application
export MASKINPORTEN_ENV=dev # 'prod' or 'dev'. Optional, defaults to dev environment ('ver2' for Maskinporten)
export MASKINPORTEN_ADMIN_CLIENT_ID=someclient # Admin client ID received from Maskinporten
export MASKINPORTEN_CONSUMER_ORG=123456789 # Your organization number in Enhetsregisteret
export MASKINPORTEN_KEYSTORE_PATH=/path/to/my-cert.p12 # Path to the keystore containing your 'virksomhetssertifikat'
export MASKINPORTEN_KEYSTORE_PASSWORD=my_keystore_password # Password for your 'virksomhetssertifikat' keystore
export MASKINPORTEN_KEY_ALIAS=my_key_alias # Alias of key in keystore
export MASKINPORTEN_KEY_PASSWORD=my_key_password # The password of your 'virksomhetssertifikat'
```

You can also use command line arguments to set or override these values. The command line arguments use similar names but without the `maskinporten` prefix, e.g. `--keystore-password`.

The key password may be the same as your keystore password. To find the key aliases in your keystore file, you can use the `keytool` command:

```bash
keytool -v -list -keystore /path/to/my_cert.p12
```

## Running the CLI

You can show documentation of the available commands and options using the `--help` (or `-h`) argument:
```bash
$ ./maskinporten -h
Usage: maskinporten [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  create  Creates a client or keys in Maskinporten
  list    List client keys from Maskinporten
  token   Generate a token in Maskinporten
```

Here is an example of creating a client and key:
```bash
# Commands:
./maskinporten create client --name myclient --description "Test client" --scopes scope1,scope2
./maskinporten create key --client-id my_client_id
./maskinporten list keys --client-id my_client_id

# Or, setup Intellij run configuration doing the same
```

Generating a Maskinporten token for a client with its own client key (from `create key` above):
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
