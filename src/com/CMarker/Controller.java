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
	
	Scanner scanner = new Scanner(System.in);
	
	public Controller() {
		this.phHueSDK = PHHueSDK.getInstance();
		findBridgesOnNetwork();
	}
	
	/**
	 * This method searches for bridges connected to your local network
	 */
	public void findBridgesOnNetwork() {
		
		phHueSDK = PHHueSDK.getInstance();
		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		sm.search(true, true);
	}
	
	/**
	 * This method lists up all the lights that are connected to the bridge.
	 */
	private void listAllLights() {
		PHBridge bridge = phHueSDK.getSelectedBridge();
		PHBridgeResourcesCache cache = bridge.getResourceCache();
		
		//Saves list of lights in a List
		List<PHLight> allLights = cache.getAllLights();
		
		for (PHLight light : allLights) {
			System.out.println(light);
		}
		
		//waiting for user input
		System.out.println("Enter the identifier for the light you want to change:");
		int choice = scanner.nextInt();
		
		if (choice <= allLights.size()) {
			
			selectedLightMenu((allLights.get(choice - 1)));
			
		} else {
			
			System.out.println("Number is too high");
			listAllLights();
		}
	}
	
	/**
	 * This is a menu method that gives options for changing values on the selected light
	 * Possible actions: change power state, change brightness, change brightness based on microphone input and change color of the light
	 *
	 * @param light light to be changed
	 */
	private void selectedLightMenu(PHLight light) {
		
		System.out.println("WHAT DO YOU WANT TO DO?");
		System.out.println("1: Change power state");
		System.out.println("2: Change brightness");
		System.out.println("3: Change brightness based on microphone input");
		
		if (light.getLightType() == CT_COLOR_LIGHT || light.getLightType() == COLOR_LIGHT) {
			System.out.println("4: Change color of the light");
		}
		
		System.out.println("----------------");
		System.out.println("9: Back");
		
		int choice = scanner.nextInt();
		
		switch (choice) {
			
			case 1:
				changePowerState(light);
				break;
			
			case 2:
				changeBrightness(light);
				break;
			
			case 3:
				changeBrightnessBasedOnMicIn(light);
				break;
			
			case 4:
				changeColor(light);
				break;
			
			case 9:
				listAllLights();
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				selectedLightMenu(light);
		}
	}
	
	/**
	 * This method changes the color of the selected light
	 * This method gives the user a selection of quick settings to choose between:
	 * Possible actions: White, Blue, Green, Yellow, Red, Custom and Random
	 * <p>
	 * Custom lets the user enter RGB-specific values
	 *
	 * @param light light to be changed
	 */
	private void changeColor(PHLight light) {
		
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
	 * This method checks the last known light state of the selected light and sets the opposite power state
	 *
	 * @param light light to be changed
	 */
	private void changePowerState(PHLight light) {
		
		//gets the last known state of the selected light
		PHLightState lightState = light.getLastKnownLightState();
		
		if (lightState.isOn()) {
			
			//Turn light off
			lightState.setOn(false);
			System.out.println("Turned " + light.getName() + " OFF");
		} else {
			
			//Turn light on
			lightState.setOn(true);
			System.out.println("Turned " + light.getName() + " ON");
		}
		
		phHueSDK.getSelectedBridge().updateLightState(light, lightState);
		
		selectedLightMenu(light);
	}
	
	/**
	 * This method changes the brightness of a selected light
	 * This is a menu method that gives the user the ability to either increase/decrease the brightness og enter a specific value
	 *
	 * @param light light to be changed
	 */
	private void changeBrightness(PHLight light) {
		
		PHLightState lightState = light.getLastKnownLightState();
		
		System.out.println("What do you want to do?");
		System.out.println("1: Increase the brightness");
		System.out.println("2: Decrease the brightness");
		System.out.println("3: Set specific brightness value");
		
		int choice = scanner.nextInt();
		
		switch (choice) {
			case 1:
				
				if (lightState.getBrightness() == 100) {
					System.out.println("Light is already on 100% brightness");
					
				} else {
					
					if (lightState.getBrightness() > 80 && lightState.getBrightness() < 100) {
						lightState.setBrightness(100);
						
					} else {
						lightState.setBrightness(lightState.getBrightness() + 20);
					}
				}
				
				phHueSDK.getSelectedBridge().updateLightState(light, lightState);
				
				break;
			case 2:
				
				if (lightState.getBrightness() == 0) {
					System.out.println("Light is already on 0% brightness");
				} else {
					
					if (lightState.getBrightness() < 20 && lightState.getBrightness() > 0) {
						
						lightState.setBrightness(0);
						
					} else {
						
						lightState.setBrightness(lightState.getBrightness() - 20);
					}
				}
				
				phHueSDK.getSelectedBridge().updateLightState(light, lightState);
				
				
				break;
			case 3:
				
				System.out.println("Enter a new brightness value: ");
				int updatedBrightness = scanner.nextInt();
				
				lightState.setBrightness(updatedBrightness);
				
				break;
			
			default:
				System.out.println("ERROR! Enter a valid number");
				changeBrightness(light);
				break;
		}
		
		phHueSDK.getSelectedBridge().updateLightState(light, lightState);
		selectedLightMenu(light);
	}
	
	/**
	 * This method uses the built in microphone to set the brightness of the given light
	 *
	 * @param light light to be changed
	 */
	private void changeBrightnessBasedOnMicIn(PHLight light) {
		
		boolean stopRecording = false;
		int counter = 0;
		
		byte tempBuffer[] = new byte[6000];
		int sampleRate = 44100;
		int sampleBits = 16;
		int channels = 2;
		int frameSize = 4;
		boolean bigEndian = false;
		
		try {
			
			//create a .wav format
			AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleBits, channels,
					frameSize, sampleRate, bigEndian);
			
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			
			PHLightState lightState = new PHLightState();
			
			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("This line is not supported");
				return;
			}
			
			//Creates a dataLine with the specified format
			TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine((info));
			
			//Opens the created dataLine
			targetDataLine.open();
			
			System.out.println("Starting to record...");
			
			//Starting the recording
			targetDataLine.start();
			
			while (counter <= 500) {
				
				if (targetDataLine.read(tempBuffer, 0, tempBuffer.length) > 0) {
					
					//uses helping method to calculate the average sound level
					int level = calculateRMSLevel(tempBuffer);
					
					//setting light brightness
					lightState.setBrightness(level);
					phHueSDK.getSelectedBridge().updateLightState(light, lightState);
					
					//System.out.println(level);
					counter++;
				}
			}
			
			//stopping the recording
			targetDataLine.stop();
			
			//closing the dataLine
			targetDataLine.close();
			System.out.println("Recording stopped");
			
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		//back to menu
		selectedLightMenu(light);
	}
	
	
	/**
	 * Algorithm is copyed from https://stackoverflow.com/a/32622121
	 *
	 * @param audioData data segment to analyze
	 * @return percent of input audio level
	 */
	public int calculateRMSLevel(byte[] audioData) {
		
		int arrayLength = audioData.length;
		long lSum = 0;
		
		for (byte anAudioData : audioData) {
			lSum += anAudioData;
		}
		
		double dAvg = lSum / arrayLength;
		double sumMeanSquare = 0d;
		
		for (byte anAudioData : audioData) {
			sumMeanSquare += Math.pow(anAudioData - dAvg, 2d);
		}
		
		double averageMeanSquare = sumMeanSquare / arrayLength;
		
		return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
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
