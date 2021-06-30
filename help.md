# Ace of Shades - Help Information
In this document, we'll go over how to use the launcher, basic game controls, and some explanations of game mechanics so that you can start playing right away.

## Launcher
The launcher is a small application that makes it easy to connect to different servers to join their hosted games. The first thing you'll see is a list of servers. **You can remove or edit a server by right-clicking on it**, and **you can add new servers using the Add Server button** at the bottom.

Each entry in the list is just a way to remember a specific address (and optionally your preferred username when connecting to that server), so that **you can double-click on a server to instantly connect**.

> Servers have an *address* which consists usually of an IP address and port number. For example, there might be a server whose address is `123.123.123.123:54321`.
> 
> It's up to the server to decide whether to allow you to join, so pick a sensible username.

### Public Servers

Besides manually entering a server's address, you can search for available public servers via the **Search** button. It will open a new window where you can browse the list of all known public servers, and here **you can double-click to join the server directly**, or **right-click to see other options**, including copying the public server's information to your normal server list, so that you can connect to it later without searching.

## Controls
To control your player in-game, the following are the default controls:

| Control      | Description                                          |
| ------------ | ---------------------------------------------------- |
| `WASD`       | Move player forward, left, backward, and right.      |
| `SHIFT`      | Sprint while moving. Shooting accuracy is decreased. |
| `CTRL`       | Sneak while moving. Shooting accuracy is increased.  |
| `R`          | Reload your weapon.                                  |
| `T`          | Start typing a message in chat.                      |
| `/`          | Start typing a command in chat.                      |
| `LEFT-CLICK` | Use your weapon.                                     |
| `SCROLL`     | Zoom in or out.                                      |
| `MOUSE-MOVE` | Aim your weapon.                                     |
| `ENTER`      | Send your message or command in chat.                |

> Be careful when typing a message or command in chat! Other players can and will try to kill you.

## Basic Mechanics
In most scenarios, when you join a server, you'll be placed onto a *team*. Each team has a spawn point, which is where you'll start out. There's also a resupply area for each team, where you can replenish your health and ammunition when running low.

> You can only resupply once in a while. Different servers may set their own rules, but the default is **30 seconds**.

Each time you kill someone from another team, your own team's score increases. Different servers may come up with different rules for what constitutes a victory, but the premise is simple: **kill as many enemies as possible**.

You can quit at any time by closing the game window.

> Some servers may have policies which discourage *combat-logging* (disconnecting when about to die), and they may ban you from reconnecting! Take this into account, and play fair.

## Hosting a Server

Read ahead if you would like to learn about how to host an AOS server for your self and others to play on, either privately or publicly.

### Requirements

In order to run the server software, you will need at least Java 16 installed. This help document won't go into the specifics of how to do this, since there are many guides already on the internet. [You can start by downloading from AdoptOpenJDK's website.](https://adoptopenjdk.net/installation.html)

If you want players from outside your local network to be able to connect to your server, you will need to configure your router's port-forwarding rules to allow TCP and UDP traffic on the port that the server will use. By default, the server starts on port 8035. Port-forwarding is slightly different for every router, so if you're not sure how to do it, search online for a guide that's intended for your specific router.

### Running the Server

All you need to do is download the latest `aos-server-XXX.jar` file from this GitHub repository's [releases page](https://github.com/andrewlalis/AceOfShades/releases). Once you've done that, you should be able to start the server by running the following command:

```bash
java -jar aos-server-XXX.jar
```

> Replace `XXX` with the version of the server which you downloaded.

### Make it Public

There are a few things you need to configure before your server will appear in the global registry of servers that clients browse through.

1. You must set the `registry-settings.discoverable` property to `true`. When `discoverable` is false (it is by default false), the server will not appear in the registry, even if all other information is correct.
2. The `registry-settings.registry-uri` property must point to the address of the global registry server. If you can paste the value into your browser followed by "/serverInfo", and you get some data, then you've most likely set this correctly. The current global registry server runs at
3. Make sure that `registry-settings.update-interval` is set to a value no less than 10 seconds, and no higher than 300 seconds (5 minutes). Failure to do this may mean that your server could be permanently banned from the registry (an IP ban).
4. Set the server's metadata, which will be shown to clients:
   `registry-settings.name` - The name of the server, as it will appear in the list. Make this short, easy to read, and recognizable. No more than 64 characters.
   `registry-settings.address` - The public address of the server that clients can use to connect. This should include both the IP address/hostname and port, in the form `IP:PORT` or `HOSTNAME:PORT`. No more than 255 characters.
   `registry-settings.description` - A short description of your server, so that clients can better decide if they want to join. No more than 1024 characters.
   `registry-settings.location` - The name of your server's location. Set this to a country or city name, so that clients can better decide if they want to join based on their connection.

Once all these things are done, you can restart your server, and it should appear shortly in clients' search results when they're browsing public servers. 

> Note that this registry service is provided as-is, and any attempts to abuse the service or provide misleading or harmful information will result in a permanent IP ban for your server. This includes inappropriate or invalid server names, descriptions, addresses, or locations. If you have trouble deciding whether or not something would be considered inappropriate, assume that it is.

