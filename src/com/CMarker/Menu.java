package com.CMarker;

import java.util.Scanner;

/**
 * @author Christian Marker on 12/06/2018 at 23:55.
 */
public class Menu {
	
	private Controller controller;
	private Scanner scanner;
	private boolean flag;
	private String choice;
	
	public Menu() {
		controller = new Controller();
		scanner = new Scanner(System.in);
		flag = true;
		startMenu();
	}
	
	private void startMenu() {
		while (flag) {
			
			System.out.println("############ MENU ############");
			System.out.println("Enter your action:");
			System.out.println("1: Find bridges in your network");
			System.out.println("0: Exit");
			
			choice = scanner.nextLine();
			
			switch (choice) {
				
				case "1":
					controller.findBridgesOnNetwork();
					break;
				case "0":
					flag = false;
					return;
				default:
					startMenu();
				
			}
		}
	}
	
	public void chooseLigthMenu() {
		
		System.out.println("Please enter the index of the light source to change");
		
		
		
		
	}
}
