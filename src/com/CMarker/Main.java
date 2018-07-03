package com.CMarker;

import com.philips.lighting.hue.sdk.PHHueSDK;

public class Main {

    public static void main(String[] args) {
    	
        PHHueSDK phHueSDK = PHHueSDK.create();
        
	    Controller controller = new Controller();
	    
	    phHueSDK.getNotificationManager().registerSDKListener(controller.getListener());
    }
}
