#import "AirFlingerPlugin.h"
#import <Cordova/CDV.h>
#import "TVersityAFAirFlinger.h"

@implementation AirFlingerPlugin


- (void)initializeAF:(CDVInvokedUrlCommand*)command {
    NSLog(@"made it into initializeAF");
    CDVPluginResult* pluginResult = nil;
    //NSString* echo = [command.arguments objectAtIndex:0];
     
    TVersityAFAirFlinger *airFlinger = [[TVersityAFAirFlinger getInstance] initInstance:PORT delegate:self eventMask:TVersityAFEventPlayerStatusChange | TVersityAFEventServerStatusChange | TVersityAFEventPlayerVolumeChanged];
    
    if (airFlinger) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"initiated"];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}





- (void)getDeviceList:(CDVInvokedUrlCommand*)command {
    NSLog(@"made it into getDeviceList");
    CDVPluginResult* pluginResult = nil;
    TVersityAFAirFlinger *airFlinger = [TVersityAFAirFlinger getInstance];
    
    BOOL isFound = NO;
    int timeToWait = WAIT_TIME;
    NSArray *players;
    int numTries = NUM_TRIES;
    
    while (isFound == NO) {
        NSDate *future = [NSDate dateWithTimeIntervalSinceNow: timeToWait];
        [NSThread sleepUntilDate:future];
        NSLog(@"waited %d", timeToWait);
        
        players = [airFlinger getCurrentDevices:TVersityAFDevicePlayer];
        if (players) {
            isFound = YES;
        }else{
            if (numTries > 3) {
                break;
            }else{
                numTries++;
            }
        }
    }
    
    if (isFound == YES){
        NSMutableArray *devices = [[NSMutableArray alloc] init];
        
        for(TVersityAFDevice *device in players) {
            //[devices setValue:device.deviceId forKey:@"deviceId"];
            [devices addObject:device.deviceId];
            NSLog(@" === device:%@", device.deviceId);
        }
        NSLog(@" === num of devices:%lu", (unsigned long)[players count]);
        
        //[devices setValue:@"89898989898" forKey:@"deviceId"];
        //[devices addObject:@"89898989898" ];
        
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:devices options:kNilOptions error:nil];
        NSString *devicesJson = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:devicesJson];
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}





- (void)terminateAF:(CDVInvokedUrlCommand*)command {
    NSLog(@"made it into terminateAF");
    CDVPluginResult* pluginResult = nil;
    TVersityAFAirFlinger *airFlinger = [TVersityAFAirFlinger getInstance];

    if (airFlinger) {
        [airFlinger terminate];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"terminated"];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}





- (void)getVolume:(CDVInvokedUrlCommand*)command {
    NSLog(@"made it into setVolumeValue");
    CDVPluginResult* pluginResult = nil;
    NSString* deviceID = [command.arguments objectAtIndex:0];
    
    
    TVersityAFAirFlinger *airFlinger = [TVersityAFAirFlinger getInstance];
    
    if (!airFlinger) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    TVersityAFPlayer* player = (TVersityAFPlayer*) [self _findPlayerByID:deviceID];
    NSLog(@"returned to getVolume");
    
    if (!player) {
        pluginResult = [CDVPluginResult resultWithStatus:@"DeviceIDNotFoundError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    if (![self _isPlayerActive:player]){
        pluginResult = [CDVPluginResult resultWithStatus:@"playerNotActiveError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    NSLog(@"returned to getVolume again");
        
    NSError * error = nil;
    __block int volume = 0;
    BOOL isSuccess = [player getVolume:^(NSInteger volumeLevel, BOOL isError, NSError *blockError) {
                            NSLog(@"made it into getvol block...");
                            //CDVPluginResult* pluginResult = nil;
                            if (isError) {
                             NSString *errorMsg = [NSString stringWithFormat:@"get volume failed with error:%@", blockError];
                             NSLog(@"%@", errorMsg);
                                NSLog(@"error message and all that jazz");
                             }else{
                                 volume = volumeLevel;
                                 NSLog(@"-=-=- volumelevel:%ld", (long)volumeLevel);
                             }
                         }
                         error:&error];
    
    if(!isSuccess) {
        NSString *errorMsg = [NSString stringWithFormat:@"get volume failed with error:%@", error];
        NSLog(@"%@", errorMsg);
        
        pluginResult = [CDVPluginResult resultWithStatus:@"volumegetError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        pluginResult = [CDVPluginResult resultWithStatus:volume];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}





- (void)setVolume:(CDVInvokedUrlCommand*)command {
    NSLog(@"made it into setVolume");
    CDVPluginResult* pluginResult = nil;
    NSString* playerString = [command.arguments objectAtIndex:0];
    NSString* volumeString = [command.arguments objectAtIndex:1];
    NSNumber* volume = [volumeString integerValue];
    
    NSLog(@"player:%@", playerString);
    NSLog(@"volume:%@", volumeString);
    
    TVersityAFAirFlinger *airFlinger = [TVersityAFAirFlinger getInstance];
    
    if (!airFlinger) {
        pluginResult = [CDVPluginResult resultWithStatus:@"airflingerNotInitError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    TVersityAFPlayer* player = (TVersityAFPlayer*) [self _findPlayerByID:playerString];
    NSLog(@"returned to setVolume");
    
    if (!player) {
        pluginResult = [CDVPluginResult resultWithStatus:@"DeviceIDNotFoundError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    if (![self _isPlayerActive:player]){
        pluginResult = [CDVPluginResult resultWithStatus:@"playerNotActiveError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    NSError * error = nil;
    BOOL isSuccess = [player setVolume:volume
                                 block:^(BOOL isError, NSError *blockError) {
                                     if (isError) {
                                         NSString *errorMsg = [NSString stringWithFormat:@"set volume failed with error:%@", blockError];
                                         NSLog(@"%@", errorMsg);
                                     }
                                 }
                                 error:&error];
    
    if(!isSuccess) {
        NSString *errorMsg = [NSString stringWithFormat:@"set volume failed with error:%@", error];
        NSLog(@"%@", errorMsg);
        
        pluginResult = [CDVPluginResult resultWithStatus:@"volumchangeError"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"volchanged"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}




// ------------------------------------- Private methods -------------------------------

- (BOOL)_isPlayerActive:(TVersityAFPlayer *)player {
    NSLog(@"made it into _isPlayerActive");
    if (!player){
        return NO;
    }
    return  ((player.currentStatus != TVersityAFDvStatOffline) &&
             (player.currentStatus != TVersityAFDvStatUnknown));
}




- (TVersityAFPlayer*) _findPlayerByID: (NSString*)deviceID {
    NSLog(@"made it into _findPlayerByID..ID:%@", deviceID);
    TVersityAFAirFlinger *airFlinger = [TVersityAFAirFlinger getInstance];
    
    if (!airFlinger) {
        return nil;
    }
    
    NSArray *players;
    players = [airFlinger getCurrentDevices:TVersityAFDevicePlayer];
    
    if (!players) {
        return nil;
    }
   
    for(id device in players) {
        if ([[device deviceId] isEqualToString:deviceID]) {
            TVersityAFPlayer *player = ([device isKindOfClass:[TVersityAFPlayer class]]) ? (TVersityAFPlayer *) device : nil;
            
            if (player == nil){ //should never happen
                NSLog(@"internal error, found the right device but not the right type %@", [device deviceId]);
                return nil;
            }
            return player;
        }
    }
    
    return nil;
}




#pragma mark TVersityAFDelegate methods

// Since in the initialization of the sdk in startSDK method, we registered to player status change events,
// this method of the delegate will be called each time a player device status changes.
// In this demo app we just print these status updates in a text view.
- (void)deviceStatusChanged:(TVersityAFDevice *)device
             previousStatus:(TVersityAFDeviceStatus)previousStatus
{
    NSString* text = [NSString stringWithFormat:@"Device:%@\nNew Status:%d Old Status:%d\n",device, device.currentStatus, previousStatus];
    [self.webView stringByEvaluatingJavaScriptFromString:@"window.plugins.airFlinger._statusDidChange()"];
    NSLog(@"Log text: %@", text);
}



// Since in the initialization of the sdk in startSDK method, we registered to player volume change events,
// this method of the delegate will be called each time a player device volume changes.
// If currently a single device controller is displayed, we set the volume in the it's volume slider control.
// We also keep the volume in an instance variable so that we can initialize with it a single device controller when needed.
- (void)deviceVolumeChange:(TVersityAFDevice *)device newVolume:(int)volumeLevel
{
    NSLog(@"Volume changed to: %i", volumeLevel);
    [self.webView stringByEvaluatingJavaScriptFromString:@"window.plugins.airFlinger._volumeDidChange()"];
}

@end