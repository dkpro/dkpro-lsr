package de.tudarmstadt.ukp.dkpro.lexsemresource.wikipedia.util;

public class WikipediaCategoryUtils {
	
    public static String getCaseSensitiveLexeme(String lexeme, boolean isCaseSensitive){
    	if(isCaseSensitive) {
            return genCaseSensitiveString(lexeme);
    	}
    	else {
            return genCaseInsensitiveString(lexeme);        
    	}
    }
    
    private static String genCaseSensitiveString(String lexeme){
    	if(lexeme == null || lexeme.length() == 0) {
            return null;
    	}

    	// TODO TZ: why do we need that condition?
    	// the first letter is low case
    	if(lexeme.substring(0,1).toLowerCase().equals(lexeme.substring(0,1))) {
            return null;
    	}

    	return lexeme;
    }
    
    private static String genCaseInsensitiveString(String lexeme){
        if(lexeme == null || lexeme.length() == 0) {
            return null;        
        }
        
        //make the first letter upper case
    	return lexeme.substring(0,1).toUpperCase() + lexeme.substring(1, lexeme.length());   	
    }
    
}
