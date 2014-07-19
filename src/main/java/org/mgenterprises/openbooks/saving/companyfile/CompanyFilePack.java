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

import com.google.gson.Gson;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Manuel Gauto
 */
public class CompanyFilePack {
    private CompanyPackType companyPackType;
    private File packFile;
    private boolean isPacked = true;
    private File companyFileLocation;
    private File dbFileLocation;
    private File hibernateConfigLocation;
    private Gson gson = new Gson(); 

    public CompanyFilePack(File packFile) {
        this.packFile = packFile;
    }

    public CompanyPackType getCompanyPackType() throws NotYetUnpackedException {
        //Make sure the file has been unpacked
        if(isPacked) throw new NotYetUnpackedException();
        return companyPackType;
    }
    
    public CompanyFile getCompanyFile() throws NotYetUnpackedException, FileNotFoundException {
        //Make sure the file has been unpacked
        if(isPacked) throw new NotYetUnpackedException();
        return parseJson(CompanyFile.class, companyFileLocation);
    }

    public File getDFileBLocation() throws NotYetUnpackedException {
        //Make sure the file has been unpacked
        if(isPacked) throw new NotYetUnpackedException();
        return dbFileLocation;
    }

    public Properties getHibernateProperties() throws NotYetUnpackedException{
        //Make sure the file has been unpacked
        if(isPacked) throw new NotYetUnpackedException();
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(hibernateConfigLocation));
        } catch (IOException ex) {
            Logger.getLogger(CompanyFilePack.class.getName()).log(Level.SEVERE, null, ex);
        }
        return properties;
    }

    public <T> T parseJson(Class type, File location) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(location));
        return (T) gson.fromJson(br, type);
    }

    public void unPack(String unpackDir) throws FileNotFoundException, IOException{
        try {
            unZip(unpackDir);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CompanyFilePack.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(CompanyFilePack.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        
        isPacked = false;
    }
    
    private void unZip(String outputFolder) throws FileNotFoundException, IOException {

        byte[] buffer = new byte[8192];
            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(packFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();

                //Default to client only pack
                if(newFile.getName().contains(".ob")) {
                    //load our company file
                    this.companyFileLocation = newFile;
                } else if(newFile.getName().contains(".obdb")) {
                    //get our db file
                    this.dbFileLocation = newFile;
                } else if(newFile.getName().contains(".obdb.conf")) {
                    //Get hibernate config
                    this.hibernateConfigLocation = newFile;
                }  else if(newFile.getName().equals("MANIFEST")) {
                    //Get type from manifest
                    FileReader fileReader = new FileReader(newFile);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    this.companyPackType = CompanyPackType.valueOf(bufferedReader.readLine());
                }
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
    }
    
    public void pack(CompanyFile companyFile, File dbFile, File hibernateFile, CompanyPackType companyPackType) throws FileNotFoundException, IOException {
        packFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(packFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        
        //Put in the company file
        //Replace spaces with dashes
        ZipEntry ze = new ZipEntry(companyFile.getCompanyProfile().getCompanyName().replace(" ", "-")+".ob");
        zipOutputStream.putNextEntry(ze);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zipOutputStream));
        gson.toJson(companyFile, bw);
        //This next line was a killer, I didn't flush before and the ob file just didn't end up right. This fixed it.
        bw.flush();
        zipOutputStream.closeEntry();
        
        //Put in DB file
        if(dbFile!=null) {
            byte[] buffer = new byte[8192];
            //Set our db name
            //Replace spaces with dashes
            ze = new ZipEntry(companyFile.getCompanyProfile().getCompanyName().replace(" ", "-")+".obdb");
            zipOutputStream.putNextEntry(ze);
            
            FileInputStream in = new FileInputStream(dbFile);
            int len;
            while ((len = in.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
            }
            in.close();
            zipOutputStream.closeEntry();
        }
        
        //Put in hibernate file
        if(hibernateFile!=null) {
            byte[] buffer = new byte[8192];
            //Set hibernate config name
            //Replace spaces with dashes
            ze = new ZipEntry(companyFile.getCompanyProfile().getCompanyName().replace(" ", "-")+".obdb.conf");
            zipOutputStream.putNextEntry(ze);
            
            FileInputStream in = new FileInputStream(hibernateFile);
            int len;
            while ((len = in.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
            }
            in.close();
            zipOutputStream.closeEntry();
        }

        //Manifest time
        //Set manifest name
        ze = new ZipEntry("MANIFEST");
        zipOutputStream.putNextEntry(ze);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zipOutputStream);
        //Write type
        outputStreamWriter.append(companyPackType.toString());
        //Line return
        outputStreamWriter.append("\n");
        outputStreamWriter.flush();
        zipOutputStream.closeEntry();
        //remember close it
        zipOutputStream.close();
    }
}
