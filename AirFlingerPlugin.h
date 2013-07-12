#import <Cordova/CDV.h>
#import "TVersityAFAirFlinger.h"

#define PORT 8080
#define WAIT_TIME 2
#define NUM_TRIES 3

@interface AirFlingerPlugin : CDVPlugin<TVersityAFDelegate>

- (void) initializeAF:(CDVInvokedUrlCommand*)command;
- (void) terminateAF:(CDVInvokedUrlCommand*)command;

- (void) getDeviceList:(CDVInvokedUrlCommand*)command;

- (void) getVolume:(CDVInvokedUrlCommand*)command;
- (void) setVolume:(CDVInvokedUrlCommand*)command;

@end
