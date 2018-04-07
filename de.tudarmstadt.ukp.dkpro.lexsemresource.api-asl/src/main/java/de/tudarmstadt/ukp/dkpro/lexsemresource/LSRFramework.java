/*******************************************************************************
 * Copyright 2018
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
package de.tudarmstadt.ukp.dkpro.lexsemresource;

import java.io.File;

public class LSRFramework
{
    public static final String ENV_DKPRO_HOME = "DKPRO_HOME";
    public static final String SYS_LSR_WORKSPACE = "dkpro.lsr.workspace";

    private static File workspace;
    
    public static void setWorkspace(File aWorkspace)
    {
        workspace = aWorkspace;
    }
    
    /**
     * Get the workspace directory.
     *
     * @return the workspace directory.
     */
    public static File getWorkspace()
    {
        if (workspace != null) {
            return workspace;
        }
        
        if (System.getProperty(SYS_LSR_WORKSPACE) != null) {
            return new File(System.getProperty(SYS_LSR_WORKSPACE));
        }
        
        if (System.getenv(ENV_DKPRO_HOME) != null) {
            return new File(System.getenv(ENV_DKPRO_HOME));
        }

        throw new IllegalStateException(
                "Environment variable or system property [" + ENV_DKPRO_HOME + "] not set");
    }
}
