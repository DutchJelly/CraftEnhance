package com.dutchjelly.craftenhance.commandhandling;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface CommandRoute {
	public String[] cmdPath();
	public String perms();
}
