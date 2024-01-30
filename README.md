# zhugeduck Battlecode24 Repository

This is our team zhugeduck's submission for MIT Battlecode 2024.

## Contributors
- Trung Dang: University of Massachusetts Amherst
- Garrett Hinkley: University of Massachusetts Amherst
- Hoang Anh Nguyen:  VNU University of Engineering and Technology
- Minh Do: University of Massachusetts Amherst

## Contest Results

We terminated the development process by the end of the International Qualifier. We got a rating of 1743, which ranked 7/100 among international teams and 32/400 overall. 

## Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.

### Postmortem [Coming soon]

### Configuration 

Look at `gradle.properties` for project-wide configuration.

If you are having any problems with the default client, please report to teh devs and
feel free to set the `compatibilityClient` configuration to `true` to download a different
version of the client.
