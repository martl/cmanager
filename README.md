# cmanager

The cache manager (cmanager) is a Java based program which is able to manage GPX files and synchronize geocache logs from [Geocaching.com](https://geocaching.com) to [Opencaching.de](https://opencaching.de). It therefore loads a GPX file with the users cache founds (e.g. `myfounds.gpx`). After configuring an OKAPI token in the settings, the user is able to match his/her founds against caches listed on Opencaching.de.

Further information in German:

* http://wiki.opencaching.de/index.php/Cmanager
* http://forum.opencaching.de/

## License & Source Code

`cmanager` is distributed under the [The GNU General Public License v3](http://www.gnu.org/licenses/gpl-3.0-standalone.html).
The sources are available on GitHub ([link](https://github.com/FriedrichFroebel/cmanager)).

## Distribution / "Download"

Releases are published on GitHubs as ["Releases"](https://github.com/FriedrichFroebel/cmanager/releases).

## Building from Source

### Prerequisites

- Java development kit (JDK) in version >= 8.
- You need to provide API keys for compiling `cmanager`. See next section for details.

### API keys

Request your personal API keys for the supported [OpenCaching](http://www.opencaching.eu/) sites, currently:

* [opencaching.de OKAPI signup](https://www.opencaching.de/okapi/signup.html)

Copy [`templates/oc_okapi.properties`](https://github.com/FriedrichFroebel/cmanager/blob/master/templates/oc_okapi.properties) to the root directory of the Git repository. Then edit `oc_okapi.properties` and insert your keys.

### Building with Gradle

Run `gradle build` from the root directory of the Git repository (or use `./gradlew build` if you do not have Gradle installed locally).

### JAR

To create a JAR file, run `gradle jar` (or `./gradlew jar`). The JAR file will be located in `build/libs`.

## Usage

### Starting the application with Gradle

Run `gradle run` (or `./gradlew run`) from the root directory of the Git repository.

### Starting the application JAR file

Run `java -jar cm-0.2.46.jar` from the directory containing the JAR file.
