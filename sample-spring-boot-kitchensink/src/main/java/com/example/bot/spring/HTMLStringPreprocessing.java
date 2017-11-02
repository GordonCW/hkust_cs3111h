package com.example.bot.spring;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.jsoup.*;
import org.jsoup.helper.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class HTMLStringPreprocessing {
			

	/**
     * Return the unprocessed contents from one the entire HTML code
     * @param url A String data type
     * this is the url link passed by the user
     * @return A ArrayList<String> data type
     * This is the meaningful content inside the HTML line,
     * which should be passed to getValidContent method
     * before passing into database for query to recommend food 
     */
	
  public static ArrayList<String> readFromUrl(String url) throws IOException {
    InputStream is = new URL(url).openStream();
    ArrayList<String> foodContent = new ArrayList<String>();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
      String temp; // dummy working variable
      boolean isNull = false;
      while (!isNull)
      {
    	  temp = rd.readLine();
    	  	if(temp == null){ isNull = true;}
    	  	else{
    			Document doc = Jsoup.parse(temp);
    			foodContent.add(doc.body().text());
    		}
    		// TODO: else throw no menu exception		  
      }
    } finally {
      is.close();
    }
    
    return foodContent;
  } 
}
