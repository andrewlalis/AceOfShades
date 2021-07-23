# Networking Protocol
In order to make Ace of Shades as performant as possible, the application makes use of a custom, rather low-level system of communication between the server and client. This document explains how this protocol works.

## Tick Updates
Every time the server computes one game tick, it sends a packet to each client. This packet includes the following information:

- Updated position, velocity, and orientation data for all physics objects.
- 
