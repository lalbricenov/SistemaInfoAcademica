import java.util.List;
import java.util.LinkedList;

/**
 * Use los comentarios para explicar el objetivo de este programa MisionTIC .
 *
 * @author Ponga aqui su nombre y correo
 * @version Ponga aquí la versión o fecha
 */
public class MisionTIC
{
    private int ciclo;
    private List<Tripulante> tripulantes;

    public MisionTIC(){
        tripulantes = new LinkedList<Tripulante>();
    }

    public MisionTIC(int ciclo){
        this();
        this.ciclo = ciclo;
    }

    public boolean agregar(Tripulante tripulante){
        boolean rta = false;
        if(tripulante.esValido() && !tripulantes.contains(tripulante)){
            rta = tripulantes.add(tripulante);
        }
        return rta;
    }

    public Tripulante buscar(Tripulante tripulante){
        Tripulante p  = null;
        int posicion = tripulantes.indexOf(tripulante);
        if(posicion!=-1) p = tripulantes.get(posicion);
        return p;
    }
    
    public Tripulante buscar(int codigo, int reto){
        Tripulante p  = null;
        System.out.println(tripulantes.size());
        for (int i = 0; i < tripulantes.size(); i++)
        {
            System.out.printf("Codigo %d, reto %d\n", tripulantes.get(i).getCodigo(), tripulantes.get(i).getReto());
            if (tripulantes.get(i).getCodigo() == codigo && tripulantes.get(i).getReto() == reto)
            {
                return tripulantes.get(i);
            }
        }
        return p;
    }
    
    public boolean eliminar(Tripulante tripulante){
        int posicion = tripulantes.indexOf(tripulante);
        if(posicion!=-1) tripulantes.remove(posicion);
        return posicion!=-1;
    }
    
    public void borrarTodo(){
      tripulantes.clear();
    }

}//fin class MisionTIC
