import java.sql.DriverManager;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;

/**
 * Clase de ejemplo para explicar conexion a Bases de Datos con JDBC
 * Usa tres formas de conexion:
 * 1) Clasica
 * 2) Pool de Conexiones manual usando la biblioteca de Apache
 * 3) Pool de Conexiones con JNDI para Servidor
 * 
 * Tiene la lista de Driver y URL de varios motores de BD
 * 
 * @author (Milton Jes&uacute;s Vera Contreras - miltonjesusvc@ufps.edu.co) 
 * @version 0.0000000000000003 --> 3*Math.sin(Math.PI-Double.MIN_VALUE) --> :)
 */
public class JDBCConnector{
    public static final String ODBC_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";
    public static final String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    public static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    public static final String ODBC_URL = "jdbc:odbc:$DB";
    public static final String MARIADB_URL = "jdbc:mariadb://$SERVER:$PORT/$DB";//"jdbc:mariadb://localhost:3306/sample";
    public static final String MYSQL_URL = "jdbc:mysql://$SERVER:$PORT/$DB";//"jdbc:mysql://localhost:3306/sample";
    public static final String ORACLE_URL = "jdbc:oracle:thin:@$SERVER:$PORT:$DB";//"jdbc:oracle:thin:@localhost:1521:sample"
    public static final String POSTGRES_URL = "jdbc:postgresql://$HOST:$PORT/$DB";//"jdbc:postgresql://localhost:5432/sample"
    public static final String SQLSERVER_URL = "jdbc:sqlserver://$HOST:$PORT;databaseName=$DB";//"jdbc:sqlserver://localhost;databaseName=sample"
    public static final String SQLITE_URL = "jdbc:sqlite:$DB";//"jdbc:sqlite:sample"

    public static final String JNDI_LOOKUP_NAME_APP = "java:comp/env";
    public static final String JNDI_LOOKUP_NAME_DB = "jdbc/$DB";

    public static final int MANUAL_POOL_CONNECTION = 0;
    public static final int POOL_CONNECTION = 1;
    public static final int CLASSIC_CONNECTION = 2;

    private int typeConnection;

    private String driver;

    private String url;

    private String server;

    private int port;

    private String database;

    private String user;

    private String password;

    private boolean autoCommit;

    private String jndiLookupNameApp;

    private String jndiLookupNameDB;

    private int maxTotal=10;
    private int maxIdle=5;
    private int minIdle=2; 
    private int maxWaitMillis=10000;

    private BasicDataSource basicDataSource;

    private DataSource dataSource;

    public JDBCConnector(){
        typeConnection = MANUAL_POOL_CONNECTION;
    }

    public Connection getConnection(){
        switch(typeConnection){
            case MANUAL_POOL_CONNECTION: return getManualPoolConnection();
            case POOL_CONNECTION: return getServerPoolConnection();
            case CLASSIC_CONNECTION: return getClassicConnection();
            default:return null;
        }
    }

    public Connection getClassicConnection(){
        Connection connection = null;
        try{
            Class.forName(this.driver);//Registrar el Driver
            connection = DriverManager.getConnection(this.getUrl(),this.user,this.password);//Conectar
            connection.setAutoCommit(autoCommit);//Establecer el autoCommit segun la propiedad
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return connection;
    }

    public Connection getManualPoolConnection(){
        Connection connection = null;

        try{
            if(basicDataSource==null){
                //Crear el Pool de Conexiones 
                basicDataSource = new BasicDataSource();
                basicDataSource.setDriverClassName(this.driver);
                basicDataSource.setUrl(this.getUrl());
                basicDataSource.setUsername(this.user);
                basicDataSource.setPassword(this.password);

                basicDataSource.setMinIdle(this.minIdle);
                basicDataSource.setMaxIdle(this.maxIdle);
                basicDataSource.setMaxTotal(this.maxTotal);
                basicDataSource.setMaxWaitMillis(this.maxWaitMillis);
            }

            connection = basicDataSource.getConnection();
            connection.setAutoCommit(autoCommit);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return connection;
    }

    public Connection getServerPoolConnection(){
        Connection connection = null;
        Context initContext = null;
        Context envContext  = null;
        try{
            if(dataSource==null){
                //Obtener el pool de conexiones del contexto del servidor
                initContext = new InitialContext();
                envContext  = (Context)initContext.lookup(jndiLookupNameApp);
                dataSource = (DataSource)envContext.lookup(jndiLookupNameDB);
            }
            connection = dataSource.getConnection();
            connection.setAutoCommit(autoCommit);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void close(){
        try{
            if(basicDataSource!=null) basicDataSource.close();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public void close(ResultSet resultset){
        try{
            Statement s = resultset.getStatement();
            Connection c = s.getConnection();          
            resultset.close();
            s.close();
            c.close();
        }
        catch(Exception e){
            System.err.println("Error close connection");
        }
    }

    /**
     * Ejecuta sentencias SQL SELECT y regresa el resultado como un ResultSet
     */
    public ResultSet executeSelect(String sql){
        ResultSet resultset = null;
        Statement statement = null;
        Connection connection = null;
        try{
            connection = getConnection();
            statement = connection.createStatement();
            resultset = statement.executeQuery(sql);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return resultset;
    }

    /**
     * Ejecuta INSERT, UPDATE o DELETE
     * Regresa un numero con la cantidad de registros afectados
     */
    public int executeInserUpdateOrDelete(String sql){
        Statement statement = null;
        Connection connection = null;
        int result = -1;
        try{
            connection = getConnection();
            statement = connection.createStatement();
            result =  statement.executeUpdate(sql);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        } 
        return result;
    }

    /**
     * Ejecuta sentencias SQL SELECT y regresa el resultado como un Map
     */
    public List<Map<String, String>> getSelectAsAMap(String sql){
        ResultSet resultset = executeSelect(sql);
        return resultSetToMap(resultset);
    }

    /**
     * Ejecuta sentencias SQL SELECT y regresa el resultado como una Matriz String
     */
    public String[][] getSelectAsAMatrix(String sql){
        ResultSet resultset = executeSelect(sql);
        String [] columns = getColumns(resultset);      
        return resultSetToMatrix(resultset, columns);
    }

    public String [] getColumns(ResultSet resultset){
        String [] columns = null;
        int ncols = 0;
        ResultSetMetaData rsmd = null;
        try{
            rsmd = resultset.getMetaData();
            ncols = rsmd.getColumnCount();
            columns = new String[ncols];
            for (int i = 1; i <= ncols; i++){
                columns[i-1] = rsmd.getColumnLabel(i);  
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return columns;
    }

    /**
     * Toma un ResultSet y lo procesa dejando los datos en un LinkedList de HashMap
     * {{columna, valor}, {columna, valor}, {columna, valor}, {columna, valor}},
     * {{columna, valor}, {columna, valor}, {columna, valor}, {columna, valor}},
     * {{columna, valor}, {columna, valor}, {columna, valor}, {columna, valor}},
     * {{columna, valor}, {columna, valor}, {columna, valor}, {columna, valor}},...
     * 
     */
    public List<Map<String, String>> resultSetToMap(ResultSet resultset){
        List<Map<String, String>> data = new LinkedList<Map<String, String>>();
        HashMap<String, String> tmp = null;

        try{
            ResultSetMetaData rsmd = resultset.getMetaData();
            int ncols = rsmd.getColumnCount();
            while(resultset.next()) {
                tmp = new  HashMap<String, String>();
                for (int i = 1; i <= ncols; i++){
                    String key = rsmd.getColumnLabel(i);  
                    String value = resultset.getString(i);
                    tmp.put(key, value);
                }
                data.add(tmp);
            }
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        finally{
         close(resultset);
        }

        return data;
    }

    /**
     * Toma un ResultSet y lo procesa dejando los datos en una matriz.
     * Asume las columnas del array String
     * Usa resultSetToMap
     * 
     */
    public String[][] resultSetToMatrix(ResultSet resultset, String [] columns){
        String [][] data = null;
        LinkedList<String []> rows = new LinkedList<String[]>();
        LinkedList<String> row = null;

        try{
            while(resultset.next()) {
                row = new LinkedList<String>();
                for (int i = 1; i <= columns.length; i++){
                    String value = resultset.getString(columns[i-1]);
                    row.add(value);
                }
                rows.add(row.toArray(new String[0]));
            }
            data = rows.toArray(new String[0][0]);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        finally{
         close(resultset);
        }

        return data;
    }

    //Start GetterSetterExtension Source Code
    /**GET Method Propertie driver*/
    public String getDriver(){
        return this.driver;
    }//end method getDriver

    /**SET Method Propertie driver*/
    public void setDriver(String driver){
        this.driver = driver;
    }//end method setDriver

    /**GET Method Propertie url*/
    public String getUrl(){
        return this.url.replace("$SERVER", this.server).replace("$PORT", String.valueOf(this.port)).replace("$DB",this.database);
    }//end method getUrl

    /**SET Method Propertie url*/
    public void setUrl(String url){
        this.url = url;
    }//end method setUrl

    /**GET Method Propertie server*/
    public String getServer(){
        return this.server;
    }//end method getServer

    /**SET Method Propertie server*/
    public void setServer(String server){
        this.server = server;
    }//end method setServer

    /**GET Method Propertie port*/
    public int getPort(){
        return this.port;
    }//end method getPort

    /**SET Method Propertie port*/
    public void setPort(int port){
        this.port = port;
    }//end method setPort

    /**GET Method Propertie database*/
    public String getDatabase(){
        return this.database;
    }//end method getPort

    /**SET Method Propertie database*/
    public void setDatabase(String database){
        this.database = database;
    }//end method setPort

    /**GET Method Propertie user*/
    public String getUser(){
        return this.user;
    }//end method getUser

    /**SET Method Propertie user*/
    public void setUser(String user){
        this.user = user;
    }//end method setUser

    /**GET Method Propertie password*/
    public String getPassword(){
        return this.password;
    }//end method getPassword

    /**SET Method Propertie password*/
    public void setPassword(String password){
        this.password = password;
    }//end method setPassword

    /**GET Method Propertie jndiLookupNameApp*/
    public String getJndiLookupNameApp(){
        return this.jndiLookupNameApp;
    }//end method getJndiLookupNameApp

    /**SET Method Propertie jndiLookupNameApp*/
    public void setJndiLookupNameApp(String jndiLookupNameApp){
        this.jndiLookupNameApp = jndiLookupNameApp;
    }//end method setJndiLookupNameApp

    /**GET Method Propertie jndiLookupNameDB*/
    public String getJndiLookupNameDB(){
        return this.jndiLookupNameDB;
    }//end method getJndiLookupNameDB

    /**SET Method Propertie jndiLookupNameDB*/
    public void setJndiLookupNameDB(String jndiLookupNameDB){
        this.jndiLookupNameDB = jndiLookupNameDB;
    }//end method setJndiLookupNameDB

    /**GET Method Propertie maxTotal*/
    public int getMaxTotal(){
        return this.maxTotal;
    }//end method getMaxTotal

    /**SET Method Propertie maxTotal*/
    public void setMaxTotal(int maxTotal){
        this.maxTotal = maxTotal;
    }//end method setMaxTotal

    /**GET Method Propertie maxIdle*/
    public int getMaxIdle(){
        return this.maxIdle;
    }//end method getMaxIdle

    /**SET Method Propertie maxIdle*/
    public void setMaxIdle(int maxIdle){
        this.maxIdle = maxIdle;
    }//end method setMaxIdle

    /**GET Method Propertie minIdle*/
    public int getMinIdle(){
        return this.minIdle;
    }//end method getMinIdle

    /**SET Method Propertie minIdle*/
    public void setMinIdle(int minIdle){
        this.minIdle = minIdle;
    }//end method setMinIdle

    /**GET Method Propertie maxWaitMillis*/
    public int getMaxWaitMillis(){
        return this.maxWaitMillis;
    }//end method getMaxWaitMillis

    /**SET Method Propertie maxWaitMillis*/
    public void setMaxWaitMillis(int maxWaitMillis){
        this.maxWaitMillis = maxWaitMillis;
    }//end method setMaxWaitMillis

    /**GET Method Propertie autoCommit*/
    public boolean isAutoCommit(){
        return this.autoCommit;
    }//end method getAutoCommit

    /**SET Method Propertie autoCommit*/
    public void setAutoCommit(boolean autoCommit){
        this.autoCommit = autoCommit;
    }//end method setAutoCommit

    //End GetterSetterExtension Source Code

}//End class