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

package org.mgenterprises.mgmoney.views.panel.accounting;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.mgenterprises.mgmoney.accounting.account.Account;
import org.mgenterprises.mgmoney.accounting.account.AccountManager;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

/**
 *
 * @author Manuel Gauto
 */
public class AccountListPanel extends javax.swing.JPanel {
    private AccountManager accountManager;

    /**
     * Creates new form AccountListPanel
     */
    public AccountListPanel(AccountManager accountManager) {
        this.accountManager = accountManager;
        initComponents();
    }

    private void loadAccounts() {
        SwingWorker accountLoadWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ArrayList<Account> accountsList = accountManager.values();
                
                Account rootAccount = new Account();
                rootAccount.setAccountID(-1);
                rootAccount.setAccountName("Accounts");
                rootAccount.setAccountDescription("");
                rootAccount.setAccountBalance(0);
                DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootAccount);
                DefaultTreeModel defaultTreeModel = new DefaultTreeModel(rootTreeNode);
                
                //Start filling the TreeModel
                for(Account account : accountsList) { 
                    DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(account);
                    int rootChildCount = defaultTreeModel.getChildCount(rootTreeNode);
                    //If it has not parent just add it at the root
                    if(account.getParentAccount()==-1) {
                        defaultTreeModel.insertNodeInto(newTreeNode, rootTreeNode, rootChildCount);
                    }
                    else {
                        boolean hasBeenInsert = false;
                        //Loop through inserted nodes looking for the parent
                        for(int rooti = 0; rooti<rootChildCount; rooti ++) {
                            //Lets get the parent ID
                            int parentID = account.getParentAccount();
                            //Get the node
                            DefaultMutableTreeNode posParentNode = (DefaultMutableTreeNode) defaultTreeModel.getChild(rootAccount, rooti);
                            //Get the associated account
                            Account posParent = (Account) posParentNode.getUserObject();
                            //Lets see if the account id matches that of the parent
                            if(posParent.getAccountID()==parentID) {
                                //Get the childcount for the index
                                int childCount = defaultTreeModel.getChildCount(posParent);
                                //Insert the new node
                                defaultTreeModel.insertNodeInto(newTreeNode, posParentNode, childCount);
                                //Lets not add the parent again
                                hasBeenInsert = true;
                            }
                        }
                        //We didn't find the parent, lets add it
                        if(!hasBeenInsert) {
                            //I hate concurrentmodification
                            ArrayList<Account> temp = new ArrayList(accountsList);
                            //Iterate through loaded accounts
                            for(Account posParent : temp) {
                                int parentID = account.getParentAccount();
                                //Lets see if the account id matches that of the parent
                                if(posParent.getAccountID()==parentID) {
                                    //Create a node
                                    DefaultMutableTreeNode posParentNode = new DefaultMutableTreeNode(posParent);
                                    //Add the child
                                    posParentNode.add(newTreeNode);
                                    //Insert!
                                    defaultTreeModel.insertNodeInto(newTreeNode, rootTreeNode, rootChildCount);
                                }
                            }
                        }
                    }
                } //Done filling the TreeModel
                
                OutlineModel outlineModel = DefaultOutlineModel.createOutlineModel(defaultTreeModel, new AccountRowModel());
                accountsOutline.setRootVisible(false);
                accountsOutline.setRenderDataProvider(new AccountRenderDataProvider());
                accountsOutline.setModel(outlineModel);
                return null;
            }
        };
        accountLoadWorker.execute();
        
    }
    
    private List<String> getColumnNames() {
        ArrayList<String> columnNames = new ArrayList<String>(3);
        columnNames.add("Account Name");
        columnNames.add("Account Description");
        columnNames.add("Account Balance");
        return columnNames;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        accountsOutline = new org.netbeans.swing.outline.Outline();

        setMinimumSize(new java.awt.Dimension(650, 650));
        setPreferredSize(new java.awt.Dimension(650, 650));

        jScrollPane1.setViewportView(accountsOutline);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.swing.outline.Outline accountsOutline;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
