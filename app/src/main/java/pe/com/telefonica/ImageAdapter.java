package pe.com.telefonica.soyuz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
	/**
	* Contexto del adaptador.
	*/
    private Context mContext;
	/**
	* Numero de asientos.
	*/
    private int num_asientos;
	/**
	* Lista de asientos vendidos durante el transcurso de la ruta.
	*/
    private ArrayList<String> lista_asientos_vendidos_ruta;
	/**
	* Lista de asientos vendidos en agencia.
	*/
    private ArrayList<String> lista_asientos_vendidos;
	/**
	* Lista de la posición de los asientos.
	*/
    private ArrayList<String> listaPosicionAsientos;
	/**
	* Rol del usuario que ha iniciado sesión.
	*/
    private String puesto_usuario;
	/**
	* Número de filas en el bus.
	*/
    private int numFilas;
	/**
	* Número de columnas en el bus.
	*/
    private float numColumna;
	/**
	* Número total y exacto de asientos antes de la última fila.
	*/
    private float resultado;
	/**
	* Número de asientos restantes para completar el número total de asientos según el tipo de bus.
	*/
    private int diferencia;

	/**
	* Constructor que inicializa las variables locales del adaptador con los parámetros de entrada.
	* @param context Contiene el contexto donde se ejecuta esta función.
	* @param num_asientos Número de asientos.
	* @param array_asientos_vendidos_ruta Lista de asientos vendidos en ruta.
	* @param array_asientos_vendidos Lista de asientos vendidos en agencia.
	* @param puesto Rol del usuario que ha iniciado sesión.
	* @param arrayPosicionAsientos Lista de la posición de los asientos.
	* @param numFilasEntero Número de filas en el bus.
	* @param numCol Número de columnas en el bus.
	*/
    public ImageAdapter(Context context, int num_asientos, ArrayList<String> array_asientos_vendidos_ruta , ArrayList<String> array_asientos_vendidos, String puesto, ArrayList<String> arrayPosicionAsientos,
                        int numFilasEntero, float numCol) {

        mContext = context;
        this.num_asientos = num_asientos;
        lista_asientos_vendidos_ruta = array_asientos_vendidos_ruta;
        lista_asientos_vendidos = array_asientos_vendidos;
        listaPosicionAsientos = arrayPosicionAsientos;
        puesto_usuario = puesto;

        numFilas = numFilasEntero;
        numColumna = numCol;

        resultado = numFilas*numColumna;
        diferencia = this.num_asientos - (int) resultado;

    }

	/**
	* Valida que el asiento seleccionado se encuentre disponible.
	* @param position Posicion del asiento que se consulta.
	* @return Valor "true" si el asiento esta dispnible, caso contrario "false".
	*/
    public boolean isEnabled(int position){

        if(puesto_usuario.equals("ANFITRION ESTANDAR") || puesto_usuario.equals("BOLETERO")|| puesto_usuario.equals("ANDROID_VENTAS")|| puesto_usuario.equals("CONDUCTOR ESTANDAR")){

            /* Algortimo para bloquear los asientos vendidos y para bloquear el espacio del asiento en caso de no ser múltiplo de 4 en la última fila */
            if (lista_asientos_vendidos_ruta.contains(listaPosicionAsientos.get(position)) || lista_asientos_vendidos.contains(listaPosicionAsientos.get(position)) ){
                return false;

            }else if((int) resultado - 1 < position){

                if(diferencia == 1){
                    if(position == resultado){
                        return true;
                    }else {
                        return false;
                    }

                } else if(diferencia == 2){
                    if(position < resultado + 1){
                        return true;
                    }else {
                        return false;
                    }

                }else if(diferencia > 2 && diferencia < 4){

                    if(position == num_asientos-1){
                        return false;
                    }else {
                        return true;
                    }

                }
            }
            /* ----------------------------------------- */
        }
        return true;
    }

    public int getCount() {
        return listaPosicionAsientos.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

	/**
	* Asigna la imagen correspondiente al estado del asiento (libre/bloqueado) junto con su número de asiento.
	*/
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        /* Listas */
        // lista_imagenes: lista que contiene las imagenes de los asientos (libre/bloqueado)
        // lista_asientos: lista que contiene el número del asiento
        int[] lista_imagenes = new int[listaPosicionAsientos.size()];
        String[] lista_asientos = new String[listaPosicionAsientos.size()];
        /* ----------------------------------------- */

        /* Se itera en función al número de asientos y se agregan valores a las listas */
        for(int i = 0; i < listaPosicionAsientos.size();i++){

            /* Algoritmo para agregar el número y la imagen del asiento (libre/bloqueado) con la excepción del espacio del asiento faltante en la última fila */
            if (lista_asientos_vendidos_ruta.contains(listaPosicionAsientos.get(i)) || lista_asientos_vendidos.contains(listaPosicionAsientos.get(i)) ){
                lista_imagenes[i] = R.drawable.asiento_vendido;
                lista_asientos[i] = listaPosicionAsientos.get(i);

            }else if((int) resultado - 1 < i){

                if(diferencia == 1){
                    if(i == resultado){
                        lista_imagenes[i] = R.drawable.asiento_libre;
                        lista_asientos[i] = listaPosicionAsientos.get(i);
                    }else {

                    }

                } else if(diferencia == 2){
                    if(i < resultado + 1){
                        lista_imagenes[i] = R.drawable.asiento_libre;
                        lista_asientos[i] = listaPosicionAsientos.get(i);
                    }else {
                    }

                }else if(diferencia > 2 && diferencia < 4){

                    if(i == num_asientos-1){

                    }else {
                        lista_imagenes[i] = R.drawable.asiento_libre;
                        lista_asientos[i] = listaPosicionAsientos.get(i);
                    }
                }
            }else{
                lista_imagenes[i] = R.drawable.asiento_libre;
                lista_asientos[i] = listaPosicionAsientos.get(i);
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */

        /* Se invoca la vista de tabla_inspecciones junto con sus elementos */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.asientos, null);

        } else {
            view = convertView;
        }
        /* ----------------------------------------- */

        TextView textView = view.findViewById(R.id.android_gridview_text);
        ImageView imageView = view.findViewById(R.id.android_gridview_image);

        /* Se asigna un valor a cada elemento */
        textView.setText(lista_asientos[position]);
        imageView.setImageResource(lista_imagenes[position]);
        /* ----------------------------------------- */

        return view;
    }

}
