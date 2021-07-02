# oracle-database-syncronizer
DbExport and DbImport projects are written in java language (Java 8) with Spring Boot Framework( v2.1.1). 
Software reads lines in sync_transfer table and creates insert statements from them. 
Writes that inserts to file and send to second site to be imported.  
Blob columns are written into separate files and sent to second site to be imported. Export and import jobs run every minute.
