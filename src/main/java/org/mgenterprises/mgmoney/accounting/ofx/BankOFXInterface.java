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

package org.mgenterprises.mgmoney.accounting.ofx;

import java.util.Date;
import net.sf.ofx4j.OFXException;
import net.sf.ofx4j.client.AccountStatement;
import net.sf.ofx4j.client.BankAccount;
import net.sf.ofx4j.client.CreditCardAccount;
import net.sf.ofx4j.client.FinancialInstitution;
import net.sf.ofx4j.client.FinancialInstitutionData;
import net.sf.ofx4j.client.FinancialInstitutionProfile;
import net.sf.ofx4j.client.FinancialInstitutionService;
import net.sf.ofx4j.client.impl.FinancialInstitutionServiceImpl;
import net.sf.ofx4j.domain.data.banking.AccountType;
import net.sf.ofx4j.domain.data.banking.BankAccountDetails;

/**
 *
 * @author Manuel Gauto
 */
public class BankOFXInterface {
    private FinancialInstitutionData bankData;
    private FinancialInstitutionProfile profile;
    private BankAccount bankAccount;

    public BankOFXInterface(FinancialInstitutionData bankData) {
        this.bankData = bankData;
    }
    
    public BankAccount getBankAccount(AccountType accountType, String routingNumber, String accountNumber, String username, String password) throws OFXException, UnsupportedAccountTypeException {
        //Temporary check to make sure we only allow what we have implemented
        if(accountType != AccountType.CHECKING || accountType != AccountType.SAVINGS) throw new UnsupportedAccountTypeException();
        
        FinancialInstitutionService service = new FinancialInstitutionServiceImpl();
        FinancialInstitution fi = service.getFinancialInstitution(bankData);

        // read the fi profile (note: not all institutions
        // support this, and you normally don't need it.)
        FinancialInstitutionProfile profile = fi.readProfile();

        //get a reference to a specific bank account at your FI
        BankAccountDetails bankAccountDetails = new BankAccountDetails();

        //routing number to the bank.
        bankAccountDetails.setRoutingNumber(routingNumber);
        //bank account number.
        bankAccountDetails.setAccountNumber(accountNumber);
        //it's a checking account
        bankAccountDetails.setAccountType(accountType);

        bankAccount = fi.loadBankAccount(bankAccountDetails, username, password);
        return bankAccount;
    }
    
    public AccountStatement getAccountStatement(Date startDate, Date endDate) throws OFXException {
        AccountStatement statement = bankAccount.readStatement(startDate, endDate);
        return statement;
    }
    
}
