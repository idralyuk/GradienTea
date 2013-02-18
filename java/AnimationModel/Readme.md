
Animation Context
=================

All animations and the groups of pixels they animate are grouped together by the specific dome geomerry

Pixel Groups
------------
To facilitate animation, pixel panels can be grouped into hierarchical groups. These groups can either be manually defined (using an editor) or problematically defined (e.g. rings of panels start at a particular vertex). These can be constructed and saved for later use with animations.

Unattached Animations (Patterns)
--------------------------------
Parameterized animations which can applied to any pixel group. They can be combined to form simple reusable patterns. An example pattern might be a color chase of two colors moving in opposite directions within their group. These animations do not have a concept of duration, simply defining time as a fraction between 0 and 1.

Attached Animations
-------------------
An animation which is attached to a specific group of pixels and has had its parameters defined. These animations also do not have a temporal component.

Animation Scenes
----------------
A collection of attached animations, each given a starting time and duration, meant to be played back as a single unit. These are the final components that will be played on the dome.


Dome Mappings
-------------
Pixels in animations are defined in terms of what panel on a dome they illuminate; they are not based on DMX channels. A dome mapping serves to map the panels of a particular dome specification (4v, 5 illuminated layers) to actual DMX channels for playback. They can be used to render animations in real-time to DMX, or to pre-render the animations into DMX "frames" for later playback.