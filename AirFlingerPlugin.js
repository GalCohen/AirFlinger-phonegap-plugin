/*
 * Temporary Scope to contain the plugin.
 *  - More information here:
 *     https://github.com/apache/incubator-cordova-ios/blob/master/guides/Cordova%20Plugin%20Upgrade%20Guide.md
 */
(function() {
 /* Get local ref to global PhoneGap/Cordova/cordova object for exec function.
  - This increases the compatibility of the plugin. */
 var cordovaRef = window.PhoneGap || window.Cordova || window.cordova; // old to new fallbacks
 
 function AirFlinger() {
 this.onVolumeChange = null;
 this.onStatusChange = null;
 }
 
 AirFlinger.prototype.initializeAF = function(success, failure){
 console.log("initializeAF called");
 return cordovaRef.exec(success, failure, "AirFlingerPlugin", "initializeAF", ["args"]);
 };
 
 AirFlinger.prototype.getDeviceList = function(success, failure){
 console.log("getDeviceList called");
 return cordovaRef.exec(success, failure, "AirFlingerPlugin", "getDeviceList", ["args"]);
 };
 
 
 AirFlinger.prototype.terminateAF = function(success, failure){
 console.log("terminateAF called");
 return cordovaRef.exec(success, failure, "AirFlingerPlugin", "terminateAF", ["args"]);
 };
 
 
 AirFlinger.prototype.setVolume = function(success, failure, player, volume){
 console.log("setVolume called");
 return cordovaRef.exec(success, failure, "AirFlingerPlugin", "setVolume", [player, volume]);
 };
 
 
 AirFlinger.prototype.getVolume = function(success, failure, player){
 console.log("getVolume called");
 return cordovaRef.exec(success, failure, "AirFlingerPlugin", "getVolume", [player]);
 };
 
 AirFlinger.prototype._volumeDidChange = function() {
 if (typeof this.onVolumeChange === 'function') { this.onVolumeChange(); }
 };
 
 AirFlinger.prototype._statusDidChange = function() {
 if (typeof this.onStatusChange === 'function') { this.onStatusChange(); }
 };
 
 
 cordovaRef.addConstructor(function() {
                           if (!window.plugins) {
                           window.plugins = {};
                           }
                           if (!window.plugins.airFlinger) {
                           window.plugins.airFlinger = new AirFlinger();
                           console.log("**************************** AirFlinger ready *************************");
                           }
                           });
 })(); /* End of Temporary Scope. */