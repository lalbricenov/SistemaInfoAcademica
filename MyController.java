import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.control.TableView;


import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;

public class MyController {

    @FXML
    private TableView<Tripulante> tableTripulantes;

    @FXML
    private TableColumn<Tripulante, Integer> colCodigo;

    @FXML
    private TableColumn<Tripulante, String> colNombre;

    @FXML
    private TableColumn<Tripulante, Integer> colReto;

    @FXML
    private TableColumn<Tripulante, Double> colCalificacion;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cmdBuscar;

    @FXML
    private Button cmdInsertar;

    @FXML
    private Button cmdActualizar;

    @FXML
    private Button cmdEliminar;

    @FXML
    private Button cmdCargar;

    @FXML
    private Label lblCodigo;

    @FXML
    private Label lblNombres;

    @FXML
    private Label lblReto;

    @FXML
    private Label lblCalificacion;

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombres;

    @FXML
    private TextField txtReto;

    @FXML
    private TextField txtCalificacion;
    //Lista de datos en TableView
    private ObservableList<Tripulante> datos = FXCollections.observableArrayList();

    private MisionTIC misionTIC2022;

    JDBCConnector jdbc = null;
    
    
    @FXML
    void actualizar(ActionEvent event) {
        Alert alert = null;
        String aviso = null;
        Tripulante p = null;
        if (txtCodigo.getText() != null && txtReto.getText() != null && txtNombres.getText() != null && txtCalificacion.getText() != null && !txtCodigo.getText().trim().isEmpty() && !txtReto.getText().trim().isEmpty()
             && !txtCodigo.getText().trim().isEmpty() && !txtReto.getText().trim().isEmpty()  && !txtNombres.getText().trim().isEmpty() && !txtCalificacion.getText().trim().isEmpty() )
        {
            p = misionTIC2022.buscar(Integer.parseInt(txtCodigo.getText()), Integer.parseInt(txtReto.getText()));
            if(p!=null){
                String nuevoNombre = txtNombres.getText().trim();
                double nuevaCalif = Double.parseDouble(txtReto.getText());
                if (nuevaCalif != p.getCalificacion() || !p.getNombresYApellidos().equals(nuevoNombre))
                {
                    try{
                    p.setNombresYApellidos(nuevoNombre);
                    p.setCalificacion(nuevaCalif);
                    //System.out.printf("Tripulante actualizado con codigo %d, reto %d, nombre %s y calificacion %f\n", p.getCodigo(),p.getReto(), p.getNombresYApellidos(), p.getCalificacion());
                    actualizarBD(p);
                    tableTripulantes.refresh();
                    }
                    catch(Exception e){
                        aviso = "Error al actualizar tripulante: " + e.getMessage();
                        alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                        alert.showAndWait();
                    }
                }
                
            }
            else
            {
                aviso = "El tripulante a modificar no existe.";
                alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                alert.showAndWait();
            }
        }
        else{
            aviso = "Debe ingresar un código y un número de reto, así como el nombre y la calificación.";
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void buscar(ActionEvent event) {
        Alert alert = null;
        String aviso = null;
        Tripulante p = null;
        if (txtCodigo.getText() != null && txtReto.getText() != null && !txtCodigo.getText().trim().isEmpty() && !txtReto.getText().trim().isEmpty())
        {
            //System.out.printf("Buscando tripulante con codigo %d y reto %d\n", Integer.parseInt(txtCodigo.getText()), Integer.parseInt(txtReto.getText()));
            p = misionTIC2022.buscar(Integer.parseInt(txtCodigo.getText()), Integer.parseInt(txtReto.getText()));
            if(p!=null){
                tableTripulantes.getSelectionModel().select(p);
                txtNombres.setText(p.getNombresYApellidos());
                txtCalificacion.setText(Double.toString(p.getCalificacion()));
            }
            else{
                aviso = "No existe un tripulante con el código y número de reto ingresados.";
                alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                alert.showAndWait();
            }
        }
        else{
            aviso = "Debe ingresar un código y un número de reto.";
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void cargar() {
        //System.out.printf("Cargar de BD iniciado");
        String [][] data = null;
        
        data = jdbc.getSelectAsAMatrix("SELECT * FROM tripulante");

        Tripulante p = null;
        misionTIC2022.borrarTodo();
        datos.clear();
        for(int i=0;i<data.length;i++){
            // Tripulante se crea con estas propiedades: int codigo, String nombresYApellidos, int reto, double calificacion
            p = new Tripulante(Integer.parseInt(data[i][0]), data[i][1], Integer.parseInt(data[i][2]), Double.parseDouble(data[i][3]));
            // Se agrega el tripulante al objeto misionTIC
            if(p.esValido())
            {
                misionTIC2022.agregar(p);
                // Luego se agrega a la lista de datos que corresponde a la tabla que se muestra
                datos.add(p);
                //System.out.printf("%d %s %d %f\n", p.getCodigo(), p.getNombresYApellidos(), p.getReto(), p.getCalificacion());
            }
                
        }
        tableTripulantes.setItems(datos);
        tableTripulantes.refresh();
    }

    @FXML
    void eliminar(ActionEvent event) {
        Alert alert = null;
        String aviso = null;
        try{
            Tripulante seleccionado = tableTripulantes.getSelectionModel().getSelectedItem();
            //System.out.printf("A eliminar %s, reto %d\n", seleccionado.getNombresYApellidos(), seleccionado.getReto());
            if(seleccionado == null){
                aviso = "Debe primero hacer click sobre el tripulante a eliminar.";
                alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                alert.showAndWait();
            }
            else
            {
                eliminarBD(seleccionado);
                misionTIC2022.eliminar(seleccionado);
                datos.remove(seleccionado);
                tableTripulantes.setItems(datos);
                tableTripulantes.refresh();
            }
            
        }
        catch(Exception e){
            aviso = "Error al eliminar Tripulante: " + e.getMessage();
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    void insertar(ActionEvent event) {
        Alert alert = null;
        String aviso = null;
        Tripulante p = null;

        try{
            p = new Tripulante(Integer.parseInt(txtCodigo.getText()),
                txtNombres.getText().trim(),
                Integer.parseInt(txtReto.getText()),
                Double.parseDouble(txtCalificacion.getText())
                );
            if(p.esValido())
            {
                // Buscarlo para ver si ya existe
                Tripulante q = misionTIC2022.buscar(p);
                if(q != null)// Si ya existia
                {
                    aviso = "El tripulante a insertar no puede tener el mismo código y reto que otro existente.";
                    alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                    alert.showAndWait();
                }
                else{
                    insertarBD(p);
                    misionTIC2022.agregar(p);            
                    datos.add(p);
                    tableTripulantes.setItems(datos);
                    tableTripulantes.refresh();
                }
                
            }else{
                aviso = "Datos inválidos: el código debe ser mayor a 1, el reto entre 1 y 5, y la nota entre 3 y 5";
                alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
                alert.showAndWait();
            }
            

        }
        catch(Exception e){
            aviso = "Error al agregar Tripulante: " + e.getMessage();
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
        }
    }
    
    @FXML
    void busquedaRapida(InputEvent event) {

        if (txtCodigo.getText() != null && txtReto.getText() != null && !txtCodigo.getText().trim().isEmpty() && !txtReto.getText().trim().isEmpty())
        {
            Tripulante p = null;
            
            p = misionTIC2022.buscar(Integer.parseInt(txtCodigo.getText()), Integer.parseInt(txtReto.getText()));
            if(p!=null){
                tableTripulantes.getSelectionModel().select(p);
            }
            else{
                tableTripulantes.getSelectionModel().clearSelection();
            }
        }
    }
    
    @FXML
    void initialize() {
        
        assert tableTripulantes != null : "fx:id=\"tableTripulantes\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert colCodigo != null : "fx:id=\"colCodgo\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert colNombre != null : "fx:id=\"colNombre\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert colReto != null : "fx:id=\"colReto\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert colCalificacion != null : "fx:id=\"colCalificacion\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert cmdBuscar != null : "fx:id=\"cmdBuscar\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert cmdInsertar != null : "fx:id=\"cmdInsertar\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert cmdActualizar != null : "fx:id=\"cmdActualizar\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert cmdEliminar != null : "fx:id=\"cmdEliminar\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert cmdCargar != null : "fx:id=\"cmdCargar\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert lblCodigo != null : "fx:id=\"lblCodigo\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert lblNombres != null : "fx:id=\"lblNombres\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert lblReto != null : "fx:id=\"lblReto\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert lblCalificacion != null : "fx:id=\"lblCalificacion\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert txtCodigo != null : "fx:id=\"txtCodigo\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert txtNombres != null : "fx:id=\"txtNombres\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert txtReto != null : "fx:id=\"txtReto\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        assert txtCalificacion != null : "fx:id=\"txtCalificacion\" was not injected: check your FXML file 'vistaSIA.fxml'.";
        
        misionTIC2022 = new MisionTIC();

        //tableTripulantes.getColumns().addAll(colCodigo, colNombre, colReto, colCalificacion);
       
        colCodigo.setCellValueFactory(
            new PropertyValueFactory<Tripulante, Integer>("codigo"));
            
        
        colNombre.setCellValueFactory(
            new PropertyValueFactory<Tripulante, String>("nombresYApellidos"));
            
       
        colReto.setCellValueFactory(
            new PropertyValueFactory<Tripulante, Integer>("reto"));
        
       
        colCalificacion.setCellValueFactory(
            new PropertyValueFactory<Tripulante, Double>("calificacion"));
       tableTripulantes.setItems(datos);
        
       conectarBD();   
       cargar();


    }
    
    private void insertarBD(Tripulante p){
        Alert alert = null;
        String aviso = null;

        String sql = null;
        int n = -1;

        sql = String.format(java.util.Locale.US,"INSERT INTO tripulante (codigo, nombresyapellidos, reto, calificacion) VALUES (%d, '%s',%d, %f);", p.getCodigo(), p.getNombresYApellidos(), p.getReto(), p.getCalificacion());
        //System.out.print(sql);

        try{
            n = jdbc.executeInserUpdateOrDelete(sql);
        }
        catch(Exception e){
            aviso = "Error al insertar en BD " + e.getMessage();
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }

    }
    
    private void actualizarBD(Tripulante p){
        Alert alert = null;
        String aviso = null;

        String sql = null;
        int n = -1;

        sql = String.format(java.util.Locale.US,"UPDATE tripulante SET nombresyapellidos='%s', calificacion=%f WHERE codigo=%d and reto=%d", p.getNombresYApellidos(),p.getCalificacion(), p.getCodigo(), p.getReto());
        //System.out.print(sql);

        try{
            n = jdbc.executeInserUpdateOrDelete(sql);
        }
        catch(Exception e){
            aviso = "Error al acualizar fila en BD " + e.getMessage();
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    
    private void eliminarBD(Tripulante p){
        Alert alert = null;
        String aviso = null;

        String sql = null;
        int n = -1;

        sql = String.format(java.util.Locale.US,"DELETE FROM tripulante WHERE codigo=%d and reto=%d", p.getCodigo(), p.getReto());
        //System.out.print(sql);

        try{
            n = jdbc.executeInserUpdateOrDelete(sql);
        }
        catch(Exception e){
            aviso = "Error al insertar en BD " + e.getMessage();
            alert = new Alert(Alert.AlertType.ERROR, aviso, ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    private void conectarBD(){
        jdbc = new JDBCConnector();
        jdbc.setDriver(JDBCConnector.MARIADB_DRIVER);
        jdbc.setUrl(JDBCConnector.MARIADB_URL);
        jdbc.setServer("localhost");
        jdbc.setPort(3306);
        jdbc.setDatabase("test1");
        jdbc.setUser("root");
        jdbc.setPassword("");
        jdbc.setAutoCommit(true);
        //System.out.printf("DB conectada");
    }
}
