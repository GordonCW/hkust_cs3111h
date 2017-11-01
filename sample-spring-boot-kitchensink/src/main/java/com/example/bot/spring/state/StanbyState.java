package com.example.bot.spring;

//import java.io.IOException;

public class StanbyState extends State {
    /**
     * Default constructor for StanbyState
     */
	public StanbyState() {
		
	}

    /**
     * Reply a message for input text
     * Inherited from abstract base class
     * @param text A String data type
     * @return A String data type
     */
	public String reply(String text) {
		return "Your text has been well received!"; 
	}
}