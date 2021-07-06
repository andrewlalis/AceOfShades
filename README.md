# Ace of Shades
Top-down 2D team-deathmatch shooter inspired by Ace of Spades, and originally made for the Java Discord server's June 2021 JavaJam.

## Download and Play

Go to [releases](https://github.com/andrewlalis/AceOfShades/releases) to download the application (server and client). *This game requires [Java 16](https://adoptopenjdk.net/?variant=openjdk16&jvmVariant=hotspot)!*

For gameplay help and information on how to set up your own server, please see the [help page](https://github.com/andrewlalis/AceOfShades/blob/main/help.md).

## Program Structure

Ace of Shades is a modular application using Java 16 and multiple Maven modules for the different parts of the game. The modules listed below can be found under a directory of the same name inside the root of this project.

- **core** - Contains any utility classes that are used by both the server and client, like vectors and network message objects.
- **server** - Multiplayer server that runs an instance of the game that clients can connect to. Includes all game logic.
- **client** - The program that's run by a single user playing the game. This includes all of the game's rendering code.
- **server-registry** - An HTTP server that acts as a global registry of game servers. Clients can query the registry for a list of available public servers, and servers can upload their metadata to the registry so that clients can see and connect to them.

## Contributing

This project is, and always will remain open source, and contributions are very much welcome! Take a look at the [list of issues](https://github.com/andrewlalis/AceOfShades/issues) to see if there's something you might be able to improve.

To submit your contribution, fork the repository, make your change, and then create a pull request to the main repository. Make sure to reference the issue that your pull request is for.

## Reporting Issues

To report bugs or other issues you encounter while playing, please [create a new Bug Report here](https://github.com/andrewlalis/AceOfShades/issues/new/choose).