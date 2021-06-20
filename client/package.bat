cd target
jpackage^
 --type exe^
 --name "Ace-of-Shades"^
 --description "Top-down 2D shooter game inspired by Ace of Spades."^
 --module-path "aos-client-3.1.jar;../../core/target/aos-core-3.1.jar"^
 --module aos_client/nl.andrewlalis.aos_client.Client^
 --win-shortcut^
 --win-dir-chooser