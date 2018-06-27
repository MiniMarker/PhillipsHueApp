package com.CMarker;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;

import java.util.List;
import java.util.Scanner;

import static com.philips.lighting.model.PHLight.PHLightType;

/**
 * @author Christian Marker on 12/06/2018 at 22:46.
 */
public class Controller {
	
	private PHHueSDK phHueSDK;
	private Controller instance;
	
	public Controller() {
		this.phHueSDK = PHHueSDK.getInstance();
		this.instance = this;
		
		findBridges();
	}
	
	public void findBridges() {
		phHueSDK = PHHueSDK.getInstance();
		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, true);
	}
	
	public void listAllLights() {
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		
		List<PHLight> allLights = cache.getAllLights();
		
		for (PHLight light : allLights) {
			System.out.println(light);
		}
		
		System.out.println("Enter the identifier for the light you want to change:");
		
		Scanner scanner = new Scanner(System.in);
		int choice = scanner.nextInt();
		
		
		if (choice <= allLights.size()) {
			
			menu((allLights.get(choice - 1)), bridge);
			
			//changeBrightness(allLights.get((choice - 1)), bridge);
			
		} else {
			
			System.out.println("Number is too high");
			
		}
	}
	
	
	public void menu(PHLight light, PHBridge bridge) {
		
		System.out.println("WHAT DO YOU WANT TO DO?");
		System.out.println("1: Change brightness");
		System.out.println("2: Change power state");
		System.out.println("3: Change color of the light");
		
		Scanner scanner = new Scanner(System.in);
		int choice = scanner.nextInt();
		
		switch (choice) {
			
			case 1:
				changeBrightness(light, bridge);
				break;
			
				case 2:
				changePowerState(light, bridge);
				break;
			
			case 3:
				changeColor(light, bridge);
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				menu(light,bridge);
		}
		
		
	}
	
	public void changeColor(PHLight light, PHBridge bridge){
		
		Scanner scanner = new Scanner(System.in);
		int r = 0;
		int g = 0;
		int b = 0;
		
		System.out.println("ENTER A CHOICE!");
		System.out.println("1 = WHITE");
		System.out.println("2 = BLUE");
		System.out.println("3 = GREEN");
		System.out.println("4 = YELLOW");
		System.out.println("5 = RED");
		System.out.println("6 = CUSTOM");
		int choice = scanner.nextInt();
		
		switch (choice) {
			
			case 1:
				//WHITE
				r = 0;
				g = 0;
				b = 0;
				break;
			
			case 2:
				//BLUE
				r = 0;
				g = 0;
				b = 255;
				break;
			
			case 3:
				//GREEN
				r = 0;
				g = 255;
				b = 0;
				break;
			
			case 4:
				//YELLOW
				r = 255;
				g = 255;
				b = 0;
				break;
			
			case 5:
				//RED
				r = 255;
				g = 0;
				b = 0;
				break;
			
			case 6:
				//CUSTOM
				System.out.println("Enter value for red:");
				r = scanner.nextInt();
				
				System.out.println("Enter value for green:");
				g = scanner.nextInt();
				
				System.out.println("Enter value for blue:");
				b = scanner.nextInt();
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				changeColor(light,bridge);
		}
		
		
		PHLightState lightState = new PHLightState();
		float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, PHLight.PHLightColorMode.COLORMODE_XY.getValue());
		System.out.println(xy);
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);
		
		bridge.updateLightState(light, lightState);
		
		/*if (!light.getLightType().equals("CT_COLOR_LIGHT")){
			
			System.out.println("Chosen light is of type: " + light.getLightType() + ". this light type is incapable with colors");
			
		} else {
			
		
			
		}*/
		
		listAllLights();
		
	}
	
	public void changePowerState(PHLight light, PHBridge bridge) {
		PHLightState lightState = light.getLastKnownLightState();
		
		if (lightState.isOn()){
			lightState.setOn(false);
		} else {
			lightState.setOn(true);
		}
		
		
		bridge.updateLightState(light, lightState);
		
		listAllLights();
	}
	
	
	public void changeBrightness(PHLight light, PHBridge bridge) {
		
		System.out.println(light);
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("ENTER A NEW BRIGHTNESS VALUE: ");
		int updatedBrightness = scanner.nextInt();
		
		System.out.println("SETTING " + light.getName() + " TO " + updatedBrightness);
		
		try {
			
			PHLightState lightState = new PHLightState();
			lightState.setBrightness(updatedBrightness);
			bridge.updateLightState(light, lightState);
			
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		listAllLights();
		
	}
	
	private PHSDKListener listener = new PHSDKListener() {
		@Override
		public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
			
		}
		
		@Override
		public void onBridgeConnected(PHBridge phBridge, String s) {
			
			phHueSDK.setSelectedBridge(phBridge);
			
			System.out.println("BRIDGE CONNECTED");
			
			phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
			
			listAllLights();
			
		}
		
		@Override
		public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {
			
			System.out.println("AUT REQUIRED: " + phAccessPoint.getBridgeId());
			System.out.println("Please push the pair-button on the bridge you want to connect");
			
			phHueSDK.startPushlinkAuthentication(phAccessPoint);
		}
		
		@Override
		public void onAccessPointsFound(List<PHAccessPoint> list) {
			
			if (list.size() == 0) {
				
				System.out.println("It looks like there is no bridges connected to your network.");
				
			} else {
				
				System.out.println("Found " + list.size() + " bridge(s):");
				System.out.println("Please enter the index of witch bridge to connect");
				
				int count = 0;
				
				for (PHAccessPoint item : list) {
					System.out.println(count + ": " + item.getBridgeId());
					count++;
				}
				
				Scanner scanner = new Scanner(System.in);
				
				int choice = scanner.nextInt();
				
				if (choice <= list.size()) {
					phHueSDK.connect(list.get(choice));
				}
			}
			
		}
		
		@Override
		public void onError(int i, String s) {
			
		}
		
		@Override
		public void onConnectionResumed(PHBridge phBridge) {
			
		}
		
		@Override
		public void onConnectionLost(PHAccessPoint phAccessPoint) {
			
		}
		
		@Override
		public void onParsingErrors(List<PHHueParsingError> list) {
			
		}
	};
	
	public PHSDKListener getListener() {
		return listener;
	}
	
	public void setListener(PHSDKListener listener) {
		this.listener = listener;
	}
}
