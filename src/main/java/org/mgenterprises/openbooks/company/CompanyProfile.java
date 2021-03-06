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

package org.mgenterprises.openbooks.company;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.mgenterprises.openbooks.saving.Saveable;
import org.mgenterprises.openbooks.util.State;

/**
 *
 * @author Manuel Gauto
 */
public class CompanyProfile extends Saveable{
    private String logoBase64;
    private String companyName;
    private String motto;
    private String contactFirst=null;
    private String contactLast=null;
    private String emailAddress=null;
    private String phoneNumber=null;
    private String faxNumber=null;
    private String streetAddress=null;
    private String cityName=null;
    private String state=null;
    private String website=null;

    public CompanyProfile() {
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public String getContactFirst() {
        return contactFirst;
    }

    public void setContactFirst(String contactFirst) {
        this.contactFirst = contactFirst;
    }

    public String getContactLast() {
        return contactLast;
    }

    public void setContactLast(String contactLast) {
        this.contactLast = contactLast;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }
    
    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Transient
    public String getString() {
        return toString().replace("\n", "<br/>");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(companyName);
        sb.append(streetAddress==null?"":"\n"+streetAddress);
        sb.append(cityName==null?"":"\n"+cityName);
        sb.append(state==null?"":"\n"+state);
        sb.append(faxNumber==null?"":"\n"+faxNumber);
        sb.append(phoneNumber==null?"":"\n"+phoneNumber);
        sb.append(emailAddress==null?"":"\n"+emailAddress);
        sb.append(website==null?"":"\n"+website);
        return sb.toString();
    }

    @Override
    public String getSaveableModuleName() {
        return this.getClass().getName();
    }

    @Override
    public String getUniqueId() {
        return companyName;
    }

    @Override
    public void setUniqueId(String id) {
        return;
    }
    
}
