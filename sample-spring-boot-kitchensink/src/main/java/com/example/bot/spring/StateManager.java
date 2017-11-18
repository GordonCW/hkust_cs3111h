/**
* StateManager.java - A class for managing different states and transitions
*/

package com.example.bot.spring;

import com.rivescript.RiveScript;
import java.io.File;
import java.util.*;

import com.example.bot.spring.DietbotController.DownloadedContent;

public class StateManager {
    // Constant values
    private final int STANDBY_STATE = 0;
    private final int INPUT_MENU_STATE = 3;
    private final int RECOMMEND_STATE = 4;
    // Must first go through InputMenuState before going to RecommendationState,
    // so 4 is not included
//    private final int[] FROM_STANDBY_STATE = {1, 2, 3, 5};

    // Value to keep track current state
    // private static Map<String, Integer> currentState; 
    
    private static final Map<String, State> states; 
    private static RiveScript bot;    

    static
    {
        bot = new RiveScript();
        
        states = new HashMap<String, State>();
        states.put("standby", new StandbyState());
        states.put("collect_user_info", new CollectUserInfoState());    
        states.put("recommend", new RecommendationState());
        states.put("input_menu", new InputMenuState());    
        states.put("provide_info", new ProvideInfoState());  
        states.put("post_eating", new PostEatingState());
        states.put("update_user_info", new UpdateUserInfoState());
    };
    
    /**
     * Default constructor for StateManager
     */
    public StateManager(String path) {
        // Load rive files for Rivescript object
        File resourcesDirectory = new File(path);
        bot.loadDirectory(resourcesDirectory.getAbsolutePath());
        bot.sortReplies();
    }

    public void updateBot(String userId){
        SQLDatabaseEngine sql = new SQLDatabaseEngine();

        try {
            bot.setUservar(userId, "topic", sql.getUserInfo(userId, "state"));
            bot.setUservar(userId, "state", sql.getUserInfo(userId, "topic"));
        }
        catch (Exception e) {
            System.out.println("Database error!");
        }

    }

    /**
     * Get output message after inputting text
     * @param text A String data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, String text, boolean debug) throws Exception {
    	Vector<String> replyText = new Vector<String>(0);
        SQLDatabaseEngine sql = new SQLDatabaseEngine();
        String currentState = null;        
        String currentTopic = null;

        try{
            // If user id is not seen before, record it and set to collect_user_info. 
            try{
                boolean isRegisteredUser = true;
                isRegisteredUser = sql.searchUser(userId);

                if (!isRegisteredUser) {
                    currentState = "collect_user_info";
                    bot.setUservar(userId, "state", "collect_user_info");
                }
                else{
                    // update bot status
                    updateBot(userId);
                    currentState = bot.getUservar(userId, "topic");
                    currentTopic = bot.getUservar(userId, "state");
                }

            } catch (Exception e) {
                replyText.add("Database error!");
                return replyText;
            }
            
        	replyText.add(states.get(currentState).reply(userId, text, bot));
            currentState = bot.getUservar(userId, "state");
            
            if(currentState == "recommend") {            	
            	String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");       	            	          	
            	replyText.add(0, splitString[0]);         	          	
            	replyText.remove(replyText.size() - 1);
            
            	String temp = states.get(currentState).reply(userId, splitString[1], bot);           	
            	replyText.add(temp);
            }
            
        } catch (Exception e) {    // Modify to custom exception TextNotRecognized later
            // Text is not recognized, does not modify current state
        	replyText.clear();
            replyText.add("Your text is not recognized by us!");
        }
        
        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " + bot.getUservar(userId, "state"));
                replyText.add("Current topic is " + bot.getUservar(userId, "topic"));
        	}
        	return replyText;
        }
        throw new Exception("NOT FOUND");
    }

    /**
     * Get output message after inputting image
     * @param jpg A DownloadedContent data type
     * @return A String data type
     */
    public Vector<String> chat(String userId, DownloadedContent jpg, boolean debug) throws Exception {
    	Vector<String> replyText = new Vector<String>(0);
        SQLDatabaseEngine sql = new SQLDatabaseEngine();
    	String currentState = null;        
        String currentTopic = null;

        try{
            try{
                boolean isRegisteredUser = true;
                isRegisteredUser = sql.searchUser(userId);

                if (!isRegisteredUser) {
                    currentState = "collect_user_info";
                    replyText.add("Please finish giving us your personal information before sharing photos!");
                    return replyText;
                }
                else{
                	updateBot(userId);
                    currentState = bot.getUservar(userId, "topic");
                    currentTopic = bot.getUservar(userId, "state");
                    
                    if (currentState == "update_user_info"){
                        replyText.add("Please finish updating your personal information before sharing photos!");
                        return replyText;
                    }
                }
            } catch (Exception e) {
                replyText.add("Database error!");
                return replyText;
            }

            // Pass the image into InputMenuState to check if the image is recognized as menu
            replyText.add(((InputMenuState) states.get(currentState)).replyImage(userId, jpg, bot));
            currentState = bot.getUservar(userId, "state");
            
            if(currentState == "recommend") {               
                String[] splitString = (replyText.lastElement()).split("AAAAAAAAAA");                                       
                replyText.add(0, splitString[0]);                       
                replyText.remove(replyText.size() - 1);
            
                String temp = states.get(currentState).reply(userId, splitString[1], bot);              
                replyText.add(temp);
            }

        } catch (Exception e) {    // Modify to custom exception ImageNotRecognized later
            // Image is not recognized as menu, does not modify current state
        	replyText.clear();
            replyText.add("Your img is not recognized by us!");
        }
        if(replyText.size() > 0) {
            // Just for testing
        	if(debug == true) {
        		replyText.add("Current state is " +  bot.getUservar(userId, "state"));
                replyText.add("Current topic is " +  bot.getUservar(userId, "topic"));
        	}
        	return replyText;
        }
        throw new Exception("NOT FOUND");
    }
    
    /**
     * Get the next state after inputting text
     * @param text A String data type
     * @return A int data type
     */
    public int decodeState(String text) {
        switch(text) {
            case "standby":
                return 0;
            case "collect_user_info":
                return 1;
            case "input_menu":
                return 3;
            case "post_eating":
                return 5;
            case "provide_info":
                return 2;
            default:
                return 4;
        }
    }
    
}