
/**
 * Use los comentarios para explicar el objetivo de este programa Tripulante .
 *
 * @author Ponga aqui su nombre y correo
 * @version Ponga aquí la versión o fecha
 */
public class Tripulante
{
    protected int codigo;
    protected String nombresYApellidos;
    protected int reto;
    protected double calificacion;

    public Tripulante(){
        nombresYApellidos = "NN";
    }

    public Tripulante(int codigo, String nombresYApellidos, int reto, double calificacion){
        this.codigo = codigo;
        this.nombresYApellidos = nombresYApellidos;
        this.reto = reto;
        this.calificacion = calificacion;
    }
    
    public boolean esValido(){
      return this.codigo>0 && this.reto>=1 && this.reto<=5 && this.calificacion>=3 && this.calificacion<=5;
    }

    public boolean equals(Object obj){
      boolean rta = false;
      Tripulante other = null;
      if(obj instanceof Tripulante){
        other = (Tripulante) obj;
        rta = this.codigo == other.codigo && this.reto == other.reto;
      }
      return rta;
    }
    
    public boolean fullyEquals(Object obj){
      boolean rta = false;
      Tripulante other = null;
      if(obj instanceof Tripulante){
        other = (Tripulante) obj;
        rta = this.codigo == other.codigo && this.reto == other.reto && this.nombresYApellidos.equals(other.nombresYApellidos) && this.calificacion == other.calificacion;
      }
      return rta;
    }
    
    public int hashCode(){
      return (this.codigo+"-"+this.reto).hashCode();
    }
    

//Start GetterSetterExtension Source Code

    /**GET Method Propertie codigo*/
    public int getCodigo(){
        return this.codigo;
    }//end method getCodigo

    /**SET Method Propertie codigo*/
    public void setCodigo(int codigo){
        this.codigo = codigo;
    }//end method setCodigo

    /**GET Method Propertie nombresYApellidos*/
    public String getNombresYApellidos(){
        return this.nombresYApellidos;
    }//end method getNombresYApellidos

    /**SET Method Propertie nombresYApellidos*/
    public void setNombresYApellidos(String nombresYApellidos){
        this.nombresYApellidos = nombresYApellidos;
    }//end method setNombresYApellidos

    /**GET Method Propertie reto*/
    public int getReto(){
        return this.reto;
    }//end method getReto

    /**SET Method Propertie reto*/
    public void setReto(int reto){
        this.reto = reto;
    }//end method setReto

    /**GET Method Propertie calificacion*/
    public double getCalificacion(){
        return this.calificacion;
    }//end method getCalificacion

    /**SET Method Propertie calificacion*/
    public void setCalificacion(double calificacion){
        this.calificacion = calificacion;
    }//end method setCalificacion

//End GetterSetterExtension Source Code


}//End class