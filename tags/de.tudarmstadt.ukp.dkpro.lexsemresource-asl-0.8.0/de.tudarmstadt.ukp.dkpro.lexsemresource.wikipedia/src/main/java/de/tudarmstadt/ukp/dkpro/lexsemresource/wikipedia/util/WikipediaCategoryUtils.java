/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
