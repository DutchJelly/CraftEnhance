package com.dutchjelly.craftenhance.itemcreation;

public enum ParseResult {
	SUCCESS("Successfully edited the item!"), 
	INVALID_FIRST_ARG("The first argument is invalid."), 
	WRONG_TYPE("Expected another type in an argument given."), 
	NO_ARGS("You entered too few arguments."), 
	NULL_ITEM("You have to hold an item to edit it."), 
	MISSING_VALUE("There's an argument missing."), 
	INVALID_ENCHANTMENT("The specified enchantment doesn't exist."),
	NO_NUMBER("Expected a number."), 
	INVALID_ITEMFLAG("The specified itemflag doesn't exist."), 
	NO_PERCENT("Expected a percentage value (0-100).");
	
	private String message;
	
	ParseResult(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
