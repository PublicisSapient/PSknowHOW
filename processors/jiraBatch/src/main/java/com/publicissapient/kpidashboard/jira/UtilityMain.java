package com.publicissapient.kpidashboard.jira;

import java.util.Arrays;
import java.util.stream.Collectors;

public class UtilityMain {
	
	 public static void main(String[] args) {
	        String[] stringArrays = new String[]{"apple", "banana", "cherry","dog", "cat"};
	       

	        String result = Arrays.stream(stringArrays)
	                .map(array -> "\"" + String.join("\", \"", array) + "\"")
	                .collect(Collectors.joining(", "));

	        System.out.println(result);
	    }
}
