# Installing ExaMinecraft (2 minutes)

You need three things: Fabric Loader, Fabric API, and the mod jar.

## 1. Install Fabric Loader

1. Download the Fabric installer from https://fabricmc.net/use/installer/
2. Run it, pick **Minecraft 26.2**, click **Install**.
3. This adds a "fabric-loader-26.2" profile to your normal Minecraft launcher.

## 2. Drop in the two jars

Put these two files into your `mods` folder
(Windows: press Win+R, type `%appdata%\.minecraft\mods`, Enter):

1. **Fabric API** - download the 26.2 version from
   https://modrinth.com/mod/fabric-api
2. **examinecraft-x.x.x.jar** - the mod file you were given.

## 3. Play

Start the launcher, select the **fabric-loader-26.2** profile, and join.
You'll know it worked if you see the **ExaMinecraft** tab in the creative
inventory.

## Joining the community server

The server runs the same mod - you MUST have it installed (steps above) or
you won't be able to connect. Then just add the server address as usual.

## For the server admin

Install a Fabric 26.2 dedicated server (same installer, "Server" tab), then
drop the same two jars into the server's `mods` folder and restart.
