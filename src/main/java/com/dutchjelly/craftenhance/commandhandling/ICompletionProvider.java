package com.dutchjelly.craftenhance.commandhandling;

import java.util.List;

public interface ICompletionProvider {

    List<String> getCompletions(String[] args);

}
