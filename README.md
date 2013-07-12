# Phonegap/Cordova AirFlinger Volume Plugin #

**Known Issues**
- This plugin is not yet plugman compatible. I will work on that in a future version.
- More complete documentation and readme, as well as a sample project will be included when the project is production ready.



## Adding the Plugin to your project ##

1. Add the AirFlingerPlugin.h and AirFlingerPlugin.m files to your "Plugins" folder in your PhoneGap project
2. Add the AirFlingerPlugin.js files to your "www" folder on disk, and add a reference to the .js file after cordova.js.
3. Add the AirFlinger library to your Xcode project.
4. Add to config.xml under plugins:` <plugin name="AirFlingerPlugin" value="AirFlingerPlugin" />` or, if using the new format,
` <feature name="AirFlingerPlugin">
        <param name="ios-package" value="AirFlingerPlugin" />
    </feature>`
- On android, in the value attribute be sure to include the package name as well, for example `value="org.apache.cordova.AirFlingerPlugin"`



## EXAMPLE USAGE ##

1. Use the `window.plugins.airflinger.initializeAF(success, failure)` to start Airflinger.
2. If successful, call `window.plugins.airflinger.getDeviceList(success, failure)`
3. The success callback will have a parameter in the form of a JSON object.
4. Use methods like getVolume and setVolume to adjust the volume of the device.

