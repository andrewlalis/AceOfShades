cd target
jpackage^
 --type exe^
 --name "Ace-of-Shades"^
 --description "Top-down 2D shooter game inspired by Ace of Spades."^
 --module-path "aos-client-4.0.jar;../../core/target/aos-core-4.0.jar"^
 --module aos_client/nl.andrewlalis.aos_client.launcher.Launcher^
 --win-shortcut^
 --win-dir-chooser