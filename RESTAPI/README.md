![las2peer](https://rwth-acis.github.io/las2peer/logo/vector/las2peer-logo.svg)

# Continuous Community Analytics

RESTful API and GraphQL for database visualization with accompanying frontend

## Build
Add database credentials for two database *mediabase* and *las2peer* into config.properties file

1. db.dbSchema_*databasename*=*your database schema*
2. db.dbpassword_*databasename*=*database password*
3. db.dbuser_*databasename*=*database user*
4. db.dbType_*databasename*=*type of database*, currently either DB2 or MySQL
5. db.dburl_*databasename*=*database url*, format: database server:port:database name on server


    ant all
    
