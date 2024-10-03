package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;


public class TablaBusquedaBoletosAdapter extends BaseAdapter {

	/**
	* Trama que contiene los boletos encontrados.
	*/
    private String trama_busquedaBoleto;
	/**
	* Contexto del adaptador.
	*/
    private Context mContext;

	/**
	* Constructor del adaptador.
	* @param tramaBusqueda Trama que contiene los boletos encontrados.
	* @param context Contiene el contexto donde se ejecuta esta función.
	*/
    public TablaBusquedaBoletosAdapter(String tramaBusqueda, Context context){
        trama_busquedaBoleto = tramaBusqueda ;
        mContext = context;
    }

    @Override
    public int getCount() {

        String[] dataReporteBusqueda = trama_busquedaBoleto.split("/");
        return dataReporteBusqueda.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

	/**
	* Asigna la información de un boleto encontrado a una fila.
	*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Gson gson = new Gson();

        /* Arreglo */
        // lista_destinos: arreglo que contiene todos los destinos
        ArrayList<String> lista_destinos = getArray(sharedPreferences,gson,"json_destinos");
        /* ----------------------------------------- */

        /* Se invoca la vista de tabla_busqueda_boleto junto con sus elementos */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tabla_busqueda_boleto, null);

        } else {
            view = convertView;
        }
        /* ----------------------------------------- */

        TextView textView1 = view.findViewById(R.id.textView1);
        TextView textView2 = view.findViewById(R.id.textView2);
        TextView textView3 = view.findViewById(R.id.textView3);
        TextView textView4 = view.findViewById(R.id.textView4);
        TextView textView5 = view.findViewById(R.id.textView5);
        TextView textView6 = view.findViewById(R.id.textView6);

        String[] dataReporteBusqueda = trama_busquedaBoleto.split("/");

        /* Listas */
        // lista_boletos: lista que contiene los números de los boletos encontrados
        // lista_cliente: lista que contiene el DNI/RUC de los boletos encontrados
        // lista_asientos: lista que contiene el número de los asientos de los boletos encontrados
        // lista_origen: lista que contiene el origen de los boletos encontrados
        // lista_destino: lista que contiene el destino de los boleto encontrados
        // lista_carga: lista que contiene la afirmación o negación si el boleto encontrado posee carga
        String[] lista_boletos = new String[lista_destinos.size()];
        String[] lista_cliente = new String[lista_destinos.size()];
        String[] lista_asientos = new String[lista_destinos.size()];
        String[] lista_origen = new String[lista_destinos.size()];
        String[] lista_destino = new String[lista_destinos.size()];
        String[] lista_carga = new String[lista_destinos.size()];
        /* ----------------------------------------- */

        /* Se itera en función a los boletos encontrados */
        for (int i = 0; i < dataReporteBusqueda.length; i++) {

            String[] dataBusquedaBoletos = dataReporteBusqueda[i].split("-");
            // dataBusquedaBoletos[0] = SERIE
            // dataBusquedaBoletos[1] = CORRELATIVO
            // dataBusquedaBoletos[2] = CO_CLIE
            // dataBusquedaBoletos[3] = NUM_ASIENT
            // dataBusquedaBoletos[4] = CO_DEST_ORIG
            // dataBusquedaBoletos[5] = CO_DEST_FINA
            // dataBusquedaBoletos[6] = CARGA

            lista_boletos[i] = dataBusquedaBoletos[0]+"-"+dataBusquedaBoletos[1];
            lista_cliente[i] = dataBusquedaBoletos[2];
            lista_asientos[i] = dataBusquedaBoletos[3];

            for (int j = 0; j < lista_destinos.size(); j++) {
                String[] dataDestinos = lista_destinos.get(j).split("-");

                if (dataBusquedaBoletos[4].equals(dataDestinos[0])) {
                    lista_origen[i] = dataDestinos[0]+"-"+dataDestinos[1];
                }
            }

            for (int j = 0; j < lista_destinos.size(); j++) {
                String[] dataDestinos = lista_destinos.get(j).split("-");

                if (dataBusquedaBoletos[5].equals(dataDestinos[0])) {
                    lista_destino[i] = dataDestinos[0]+"-"+dataDestinos[1];
                }
            }
            lista_carga[i] = dataBusquedaBoletos[6];
        }
        /* ----------------------------------------- */

        /* Se asigna un valor a cada elemento */
        textView1.setText(lista_boletos[position]);
        textView2.setText(lista_cliente[position]);
        textView3.setText(lista_asientos[position]);
        textView4.setText(lista_origen[position]);
        textView5.setText(lista_destino[position]);
        textView6.setText(lista_carga[position]);
        /* ----------------------------------------- */

        return view;
    }
}
