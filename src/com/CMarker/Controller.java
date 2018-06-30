package com.CMarker;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;

import javax.sound.sampled.*;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static com.philips.lighting.model.PHLight.PHLightType.COLOR_LIGHT;
import static com.philips.lighting.model.PHLight.PHLightType.CT_COLOR_LIGHT;

/**
 * @author Christian Marker on 12/06/2018 at 22:46.
 */
public class Controller {
	
	//TODO make bridge a field!
	private PHBridge connectedBridge;
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
	
	private void listAllLights() {
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
			
			selectedLightMenu((allLights.get(choice - 1)));
			
		} else {
			
			System.out.println("Number is too high");
			listAllLights();
		}
	}
	
	
	private void selectedLightMenu(PHLight light) {
		
		System.out.println("WHAT DO YOU WANT TO DO?");
		System.out.println("1: Change brightness");
		System.out.println("2: Change power state");
		
		if (light.getLightType() == CT_COLOR_LIGHT || light.getLightType() == COLOR_LIGHT) {
			System.out.println("3: Change color of the light");
		}
		
		System.out.println("----------------");
		System.out.println("9: Back");
		
		Scanner scanner = new Scanner(System.in);
		int choice = scanner.nextInt();
		
		switch (choice) {
			
			case 1:
				changeBrightness(light);
				break;
			
			case 2:
				changePowerState(light);
				break;
			
			case 3:
				changeColor(light);
				break;
			
			case 4:
				changeBrightnessBasedOnMicIn(light);
				break;
			
			case 9:
				listAllLights();
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				selectedLightMenu(light);
		}
		
		
	}
	
	private void changeColor(PHLight light) {
		
		System.out.println(light.getLightType());
		
		/*if (light.getLightType().toString().equals("CT_COLOR_LIGHT") || light.getLightType().toString().equals("COLOR_LIGHT")){
			System.out.println("This bulb does not support colors!");
			selectedLightMenu(light,bridge);
			return;
		}*/
		
		Scanner scanner = new Scanner(System.in);
		int r = 0;
		int g = 0;
		int b = 0;
		
		System.out.println("ENTER A CHOICE!");
		System.out.println("1: White");
		System.out.println("2: Blue");
		System.out.println("3: Green");
		System.out.println("4: Yellow");
		System.out.println("5: Red");
		System.out.println("6: Custom");
		System.out.println("7: Random");
		System.out.println("--------------");
		System.out.println("9: Back");
		int choice = scanner.nextInt();
		
		switch (choice) {
			
			case 1:
				//WHITE
				r = 255;
				g = 255;
				b = 255;
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
			
			case 7:
				//RANDOM
				Random random = new Random();
				
				r = random.nextInt(255);
				g = random.nextInt(255);
				b = random.nextInt(255);
				
				break;
			
			case 9:
				//BACK
				selectedLightMenu(light);
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				changeColor(light);
		}
		
		//Setting the colors
		PHLightState lightState = new PHLightState();
		
		float[] xy = PHUtilities.calculateXYFromRGB(r, g, b, PHLight.PHLightColorMode.COLORMODE_XY.getValue());
		lightState.setX(xy[0]);
		lightState.setY(xy[1]);
		
		phHueSDK.getSelectedBridge().updateLightState(light, lightState);
		
		selectedLightMenu(light);
	}
	
	/**
	 * This method checks the last known light state and sets the opposite power state
	 *
	 * @param light light to be changed
	 */
	private void changePowerState(PHLight light) {
		PHLightState lightState = light.getLastKnownLightState();
		
		if (lightState.isOn()) {
			lightState.setOn(false);
			System.out.println("Turned " + light.getName() + " OFF");
		} else {
			lightState.setOn(true);
			System.out.println("Turned " + light.getName() + " ON");
		}
		
		phHueSDK.getSelectedBridge().updateLightState(light, lightState);
		
		selectedLightMenu(light);
	}
	
	
	public void changeBrightness(PHLight light) {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("ENTER A NEW BRIGHTNESS VALUE: ");
		int updatedBrightness = scanner.nextInt();
		
		System.out.println("SETTING " + light.getName() + " TO " + updatedBrightness);
		
		try {
			
			PHLightState lightState = new PHLightState();
			lightState.setBrightness(updatedBrightness);
			phHueSDK.getSelectedBridge().updateLightState(light, lightState);
			
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		selectedLightMenu(light);
		
	}
	
	private void changeBrightnessBasedOnMicIn(PHLight light) {
		
		boolean stopRecording = false;
		int counter = 0;
		int level = 0;
		byte tempBuffer[] = new byte[6000];
		
		try {
			
			AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			
			PHLightState lightState = new PHLightState();
			
			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("This line is not supported");
				return;
			}
			
			TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine((info));
			targetDataLine.open();
			
			System.out.println("Starting to record...");
			targetDataLine.start();
			
			while (counter <= 200) {
				
				if (targetDataLine.read(tempBuffer, 0, tempBuffer.length) > 0) {
					level = calculateRMSLevel(tempBuffer);
					
					lightState.setBrightness(level);
					phHueSDK.getSelectedBridge().updateLightState(light, lightState);
					
					System.out.println(level);
					counter++;
				}
			}
			
			targetDataLine.stop();
			targetDataLine.close();
			System.out.println("Recording stopped");
			
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Algorithm is copyed from https://stackoverflow.com/a/32622121
	 *
	 * @param audioData data segment to analyze
	 * @return procent of input audio level
	 */
	public int calculateRMSLevel(byte[] audioData)
	{
		long lSum = 0;
		for(int i=0; i < audioData.length; i++)
			lSum = lSum + audioData[i];
		
		double dAvg = lSum / audioData.length;
		double sumMeanSquare = 0d;
		
		for(int j=0; j < audioData.length; j++)
			sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);
		
		double averageMeanSquare = sumMeanSquare / audioData.length;
		
		return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
	}
	
	
	private PHSDKListener listener = new PHSDKListener() {
		@Override
		public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
		
		}
		
		@Override
		public void onBridgeConnected(PHBridge phBridge, String s) {
			
			phHueSDK.setSelectedBridge(phBridge);
			phHueSDK.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL);
			
			System.out.println("BRIDGE CONNECTED");
			
			try {
				Thread.sleep(1000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
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
	
	public PHBridge getConnectedBridge() {
		return connectedBridge;
	}
	
	public void setConnectedBridge(PHBridge connectedBridge) {
		this.connectedBridge = connectedBridge;
	}
}
