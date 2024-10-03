package pe.com.telefonica.soyuz;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;


public class TablaBusquedaBusAdapter extends BaseAdapter {

    /**
     * Trama que contiene los boletos encontrados.
     */
    private ArrayList<String> lista_busesEncontrados;
    /**
     * Contexto del adaptador.
     */
    private Context mContext;

    /**
     * Constructor del adaptador.
     * @param listaBusesEncontrados Trama que contiene el boleto encontrado.
     * @param context Contiene el contexto donde se ejecuta esta función.
     */
    public TablaBusquedaBusAdapter(ArrayList<String> listaBusesEncontrados, Context context){
        lista_busesEncontrados = listaBusesEncontrados ;
        mContext = context;
    }

    @Override
    public int getCount() {

        return lista_busesEncontrados.size();
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
        //ArrayList<String> lista_destinos = getArray(sharedPreferences,gson,"json_destinos");
        /* ----------------------------------------- */

        /* Se invoca la vista de tabla_boleto_leido junto con sus elementos */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tabla_busqueda_bus, null);

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

        //String[] dataBusEncontrado = trama_busEncontrado.split("/");

        /* Listas */
        // lista_origen: lista que contiene el origen de los buses encontrados.
        // lista_destino: lista que contiene el destino de los buses encontrados.
        // lista_codBus: lista que contiene los codigos de los buses encontrados.
        // lista_rumbo: lista que contiene el rumbo de los buses encontrados.
        // lista_anfitrion: lista que contiene los anfitriones de los buses encontrados.
        // lista_conductor: lista que contiene los conductores de los buses encontrados.
        String[] lista_codBus = new String[lista_busesEncontrados.size()];
        String[] lista_origen = new String[lista_busesEncontrados.size()];
        String[] lista_destino = new String[lista_busesEncontrados.size()];
        String[] lista_rumbo = new String[lista_busesEncontrados.size()];
        String[] lista_anfitrion = new String[lista_busesEncontrados.size()];
        String[] lista_conductor = new String[lista_busesEncontrados.size()];
        /* ----------------------------------------- */
        Log.d("bus", lista_busesEncontrados.toString());
        /* Se itera en función a los boletos encontrados */
        for (int i = 0; i < lista_busesEncontrados.size(); i++) {

            String[] dataBus = lista_busesEncontrados.get(i).split("/");
            // dataBus[4] = codBus
            // dataBus[6] = origen
            // dataBus[7] = destino
            // dataBus[8] = rumbo
            // dataBus[3] = anfitrion
            // dataBus[1] = conductor

            lista_codBus[i] = dataBus[4];
            lista_origen[i] = dataBus[6];
            lista_destino[i] = dataBus[7];
            lista_rumbo[i] = dataBus[8];
            lista_anfitrion[i] = dataBus[3];
            lista_conductor[i] = dataBus[1];
        }
        /* ----------------------------------------- */

        /* Se asigna un valor a cada elemento */
        textView1.setText(lista_codBus[position]);
        textView2.setText(lista_origen[position]);
        textView3.setText(lista_destino[position]);
        textView4.setText(lista_rumbo[position]);
        textView5.setText(lista_anfitrion[position]);
        textView6.setText(lista_conductor[position]);
        /* ----------------------------------------- */

        return view;
    }
}
