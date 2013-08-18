#!/usr/bin/env bash

echo -n Stopping Dome Server... 
launchctl unload dome-server.plist && echo OK

echo -n Stopping Dome Server... 
launchctl unload dome-controller.plist && echo OK


echo -n Starting Dome Server... 
launchctl load dome-server.plist && echo OK

echo -n Starting Dome Controller... 
launchctl load dome-controller.plist && echo OK