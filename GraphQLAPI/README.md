![las2peer](https://rwth-acis.github.io/las2peer/logo/vector/las2peer-logo.svg)

# Continuous Community Analytics

GraphQL API for database visualization wrapped around RESTful API

## Build
Add database credentials for two database *mediabase* and *las2peer* into config.properties file

1. db.dbSchema_*databasename*=*your database schema*
2. db.dbpassword_*databasename*=*database password*
3. db.dbuser_*databasename*=*database user*
4. db.dbType_*databasename*=*type of database*, currently either DB2 or MySQL
5. db.dburl_*databasename*=*database url*, format: database server:port:database name on server

Execute following command in command line

    ant all

After this command the property *restAPIURL* can be changed to URL of corresponding RESTful API.
    
## Deployment
Navigate to mobsos-community-analytics/GraphQLAPI/bin and execute the following

	start_network.bat/sh
	
The URL of the API is shown above the dashed line
    
