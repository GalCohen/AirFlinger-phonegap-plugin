package com.urlrnd.plugin;


import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.apache.cordova.api.PluginResult.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tversity.airflinger.sdk.android.AirFlinger;
import com.tversity.airflinger.sdk.android.AirFlingerInternalTypes;
import com.tversity.airflinger.sdk.android.AirFlingerLog;
import com.tversity.airflinger.sdk.android.AndroidUtils;
import com.tversity.airflinger.sdk.android.Player;
import com.tversity.airflinger.sdk.android.Server;
import com.tversity.airflinger.sdk.android.Types;
import com.tversity.airflinger.sdk.android.Types.AlreadyInitializedException;
import com.tversity.airflinger.sdk.android.Types.CommandResult;
import com.tversity.airflinger.sdk.android.Types.DeviceOfflineException;
import com.tversity.airflinger.sdk.android.Types.DeviceStatus;
import com.tversity.airflinger.sdk.android.Types.ICommandCallback;
import com.tversity.airflinger.sdk.android.Types.IEventCallback;
import com.tversity.airflinger.sdk.android.Types.IVolumeLevelCallback;
import com.tversity.airflinger.sdk.android.Types.InvalidCommandException;
import com.tversity.airflinger.sdk.android.Types.InvalidDeviceException;
import com.tversity.airflinger.sdk.android.Types.ServiceDisconnectedException;
import com.tversity.airflinger.sdk.android.Types.UnknownErrorException;


public class AirFlingerPlugin extends CordovaPlugin implements IEventCallback, Types.ILogCallback {
	final int PORT = 8080;
	final int WAIT_TIME = 2;
	final int NUM_TRIES = 3;
	  
	public boolean execute(String action, JSONArray args,
				final CallbackContext callbackContext) throws JSONException {
		
        if (action.equals("initializeAF")) {
        	
        	System.out.println("initializeAF");
			try {
				try {
		            try {
		            	AirFlinger.getInstance().init(this.cordova.getActivity(), R.drawable.progress_indeterminate_horizontal, PORT, this, Types.EVENT_LISTEN_PLAYER_STATUS_CHANGE | Types.EVENT_LISTEN_PLAYER_VOLUME_CHANGE
		                        | Types.EVENT_LISTEN_SERVER_STATUS_CHANGE, this);
		            }
		            catch (AlreadyInitializedException e) {
		                //If this happens it means that the activity was destroyed without onDestroy() being called first
		                //Another legitimate case is that the activity was sent to the background and then stopped.
		                //AirFlinger remains running, so when onCreate is called again, there is no need to re-initialize
		            	callbackContext.sendPluginResult(new PluginResult(
								PluginResult.Status.OK, "already initated"));
		            }
		            callbackContext.sendPluginResult(new PluginResult(
							PluginResult.Status.OK, "initiated"));
		        }
		        catch (Exception e)
		        {
		        	callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION));
		        }    
				
				return true;
			} catch (Exception e) {
				callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION));
				return false;
			}
			
        } else if (action.equals("terminateAF")) {
        	
        	System.out.println("terminateAF");
        	try {
				AirFlinger.getInstance().terminate();
				callbackContext.sendPluginResult(new PluginResult(Status.OK, "terminated"));
				return true;
			} catch (UnknownErrorException e1) {
				callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION, "AirFlinger was not initialized"));
			}
        	
        } else if (action.equals("getDeviceList")) {
        	System.out.println("getDeviceList");
        	//----
        	AirFlinger airFlinger = AirFlinger.getInstance();
        	if (airFlinger == null) {
        		callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION, "AirFlinger was not initialized"));
        		return false;
        	}
        	
        	boolean isFound = false;
        	int timeToWait = WAIT_TIME;
        	int numTries = NUM_TRIES;
        	Player[] players = null;
        	
        	while (isFound == false) {
        		android.os.SystemClock.sleep(timeToWait*1000);
        		
                System.out.println("waited "+timeToWait);
                
                players = airFlinger.getCurrentPlayers();
                if (players != null) {
                    isFound = true;
                }else{
                    if (numTries > 3) {
                        break;
                    }else{
                        numTries++;
                    }
                }
        	}
        	
        	if (isFound == true){
        		JSONArray devices = new JSONArray(); 
                
        		for (int i = 0; i < players.length; i++) { 
        			devices.put(players[i].getDeviceId());
                    System.out.println("=== device:"+players[i].getDeviceId());
                }
               System.out.println("=== num of devices:"+players.length); 
               System.out.println(devices.toString()); 
                callbackContext.sendPluginResult(new PluginResult(Status.OK, devices.toString()));
            }else{
                callbackContext.sendPluginResult(new PluginResult(Status.ERROR));
            }

        
        } else if (action.equals("getVolume")) {
        	System.out.println("getVolume");
        	
        	String playerString = args.get(0).toString();
     	    System.out.println("player:"+playerString);
        	
      	    AirFlinger airFlinger = AirFlinger.getInstance();
	   	    
	   	    if (airFlinger == null) {
	   	    	 callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION, "AirFlinger was not initialized"));
	   	    	 return false;
	   	    }
	   	    
	   	    Player player = findPlayerByID(playerString);
	   	    
	   	    if (player == null) {
	   	        callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "DeviceIDNotFoundError"));
	   	        return false;
	   	    }
        	        
        	try {
				player.getVolume(null, new IVolumeLevelCallback() {
					@Override
					public void OnReceivedInfo(Object arg0, CommandResult arg1,
							int arg2) {
						callbackContext.sendPluginResult(new PluginResult( Status.OK, arg2));
					}
				});
        	} catch (InvalidDeviceException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "InvalidDeviceException"));
				
			} catch (DeviceOfflineException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "DeviceOfflineException"));
				
			} catch (InvalidCommandException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "InvalidCommandException"));
				
			} catch (ServiceDisconnectedException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "ServiceDisconnectedException"));
				
			} catch (UnknownErrorException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "UnknownErrorException"));
			}

        } else if (action.equals("setVolume")) {
        	System.out.println("setVolume");
        	
        	String playerString = args.get(0).toString();
        	String volumeString = args.get(1).toString(); 
        	int volume = Integer.parseInt(volumeString);
    	    
    	   System.out.println("player:"+playerString);
    	   System.out.println("volume:"+volumeString);
    	    
    	   AirFlinger airFlinger = AirFlinger.getInstance();
    	    
    	    if (airFlinger == null) {
    	    	 callbackContext.sendPluginResult(new PluginResult( Status.INSTANTIATION_EXCEPTION, "AirFlinger was not initialized"));
    	    	 return false;
    	    }
    	    
    	    Player player = findPlayerByID(playerString);
    	    System.out.println("returned to setVolume");
    	    
    	    if (player == null) {
    	        callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "DeviceIDNotFoundError"));
    	        return false;
    	    }
    	    
    	    if (!isPlayerActive(player)){
    	        callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "playerNotActiveError"));
    	        return false;
    	    }
    	    
    	    try {
				player.setVolume(volume, null, new ICommandCallback() {
					@Override
					public void OnCommandPerformed(Object arg0, CommandResult arg1) {
						 callbackContext.sendPluginResult(new PluginResult( Status.OK, "volchanged"));
					}
				});
			} catch (InvalidDeviceException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "InvalidDeviceException"));
				
			} catch (DeviceOfflineException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "DeviceOfflineException"));
				
			} catch (InvalidCommandException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "InvalidCommandException"));
				
			} catch (ServiceDisconnectedException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "ServiceDisconnectedException"));
				
			} catch (UnknownErrorException e) {
				e.printStackTrace();
				callbackContext.sendPluginResult(new PluginResult( Status.ERROR, "UnknownErrorException"));
				
			}
    	    
    	    callbackContext.sendPluginResult(new PluginResult( Status.OK, "volchanged"));
    	    return true;
        }
		return false;
	}

	// ------------------------------------- Private methods -------------------------------

	private boolean isPlayerActive(Player player) { 
	    System.out.println("Made it into isPlayerActive"); 
	    if (player == null){
	        return false;
	    }
	    
	    return((player.getCurrentStatus() != Types.DeviceStatus.OFFLINE ) &&
	    		(player.getCurrentStatus() != Types.DeviceStatus.UNKNOWN));
	}



	private Player findPlayerByID(String deviceID) {
	    System.out.println("made it into _findPlayerByID..ID:"+deviceID);
	    AirFlinger airFlinger = AirFlinger.getInstance();
	    
	    if (airFlinger == null) {
	        return null;
	    }
	    
	    Player[] players = airFlinger.getCurrentPlayers();
	    
	    if (players == null) {
	        return null;
	    }
	   
	    for(int i = 0; i < players.length; i++) {
	    	if (players[i].getDeviceId().equals(deviceID)) {
	            if (players[i] == null){ //should never happen
	                System.out.println("internal error, found the right device but not the right type"+deviceID);
	                return null;
	            }
	            return players[i];
	        }
	    }
	    
	    return null;
	}
	
	
	@Override
	public void OnLogMessage(int arg0, String arg1, String arg2) {
		System.out.println("OnLogMessage");
	}

	
	@Override
	public void OnPlayerStatusChange(Player arg0, DeviceStatus arg1) {
		System.out.println("OnPlayerStatusChange");
		String js = String.format("window.plugins.airFlinger._statusDidChange()", arg1.toString());
		this.webView.addJavascriptInterface(js, js);
	}

	
	@Override
	public void OnPlayerVolumeChange(Player arg0, int arg1) {
		System.out.println("OnPlayerStatusChange");
		String js = String.format("window.plugins.airFlinger._volumeDidChange()", String.valueOf(arg1));
		this.webView.addJavascriptInterface(js, js);
	}

	
	@Override
	public void OnServerStatusChange(Server arg0, DeviceStatus arg1) {
		System.out.println("OnServerStatusChange");
		String js = String.format("window.plugins.airFlinger._statusDidChange()", arg1.toString());
		this.webView.addJavascriptInterface(js, js);
	}
}
