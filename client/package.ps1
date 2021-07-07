# This script prepares and runs the jpackage command to generate a Windows AOS Client installer.

Push-Location $PSScriptRoot\target

# Remove existing file if it exists.
Write-Output "Removing existing exe file."
Get-ChildItem *.exe | ForEach-Object { Remove-Item -Path $_.FullName -Force }
Write-Output "Done."

# Get list of dependency modules that maven copied into the lib directory.
$modules = Get-ChildItem -Path lib -Name | ForEach-Object { "lib\$_" }
# Add our own main module.
$mainModuleJar = Get-ChildItem -Name -Include "aos-client-*.jar" -Exclude "*-jar-with-dependencies.jar"
$modules += $mainModuleJar
Write-Output "Found modules: $modules"
$modulePath = $modules -join ';'

Write-Output "Running jpackage..."
jpackage `
 --type exe `
 --name "Ace-of-Shades" `
 --description "Top-down 2D shooter game inspired by Ace of Spades." `
 --module-path "$modulePath" `
 --module aos_client/nl.andrewlalis.aos_client.launcher.Launcher `
 --win-shortcut `
 --win-dir-chooser
Write-Output "Done!"
