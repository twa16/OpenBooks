/*
 * The MIT License
 *
 * Copyright 2014 MG Enterprises Consulting LLC.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.mgenterprises.openbooks.saving.companyfile;

import java.util.HashMap;
import org.mgenterprises.openbooks.company.CompanyProfile;
import org.mgenterprises.openbooks.configuration.ConfigurationManager;
import org.mgenterprises.openbooks.saving.Saveable;

/**
 *
 * @author Manuel Gauto
 */
public class CompanyFile {
    public final long version=1;
    private HashMap<String, Saveable> companyFileComponents = new HashMap<String, Saveable>();
    
    public void updateConfigurationManager(ConfigurationManager configurationManager) {
        this.companyFileComponents.put("configurationManager", configurationManager);
    }
    
    public void updateCompanyProfile(CompanyProfile companyProfile) {
        this.companyFileComponents.put("companyProfile", companyProfile);
    }
    
    public ConfigurationManager getConfigurationManager() {
        Saveable saveable = companyFileComponents.get("configurationManager");
        if(saveable==null) {
            ConfigurationManager configurationManager = new ConfigurationManager();
            this.companyFileComponents.put("configurationManager", configurationManager);
            saveable = configurationManager;
        }
        return (ConfigurationManager) saveable;
    }
    
    public CompanyProfile getCompanyProfile() {
        Saveable saveable = companyFileComponents.get("companyProfile");
        if(saveable==null) {
            CompanyProfile companyProfile = new CompanyProfile();
            this.companyFileComponents.put("companyProfile", companyProfile);
            saveable = companyProfile;
        }
        return (CompanyProfile) saveable;
    }
}
