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

public class TablaManifiestoAdapter extends BaseAdapter {
    private String trama_boletosLeidos;
    /**
     * Trama que contiene los boletos encontrados.
     */
    private ArrayList<String> lista_boletosLeidos;
    /**
     * Contexto del adaptador.
     */
    private Context mContext;

    /**
     * Constructor del adaptador.
     * @param listaBoletosLeidos Trama que contiene el boleto encontrado.
     * @param context Contiene el contexto donde se ejecuta esta función.
     */
    public TablaManifiestoAdapter(ArrayList<String> listaBoletosLeidos, Context context){
        lista_boletosLeidos = listaBoletosLeidos ;
        mContext = context;
    }

    @Override
    public int getCount() {

        //String[] dataReporteBusqueda = trama_boletosLeidos.split("/");
        //return dataReporteBusqueda.length;
        return lista_boletosLeidos.size();
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
            view = inflater.inflate(R.layout.tabla_manifiesto_row, null);

        } else {
            view = convertView;
        }
        /* ----------------------------------------- */

        TextView textView1 = view.findViewById(R.id.textview_NO_CLIE);
        TextView textView2 = view.findViewById(R.id.textview_NU_DOCU);
        TextView textView3 = view.findViewById(R.id.textView_ORIGEN);
        TextView textView4 = view.findViewById(R.id.textView_DESTINO);
        TextView textView5 = view.findViewById(R.id.textView_DOCU_IDENTI);

        //String[] dataBoletosLeidos = trama_boletosLeidos.split("/");

        /* Listas */
        // lista_empresa: lista que contiene la empresa de los boleto leídos.
        // lista_serie: lista que contiene las series de los boletos leídos.
        // lista_correlativo: lista que contiene los correlativos de los boletos leídos.
        // lista_tipoDocumento: lista que contiene el tipo de documento de los boletos leídos.
        // lista_tipoDocumento: lista que contiene la fecha de emision de los boletos leídos.
        String[] lista_empresa = new String[lista_boletosLeidos.size()];
        String[] lista_serie = new String[lista_boletosLeidos.size()];
        String[] lista_correlativo = new String[lista_boletosLeidos.size()];
        String[] lista_tipoDocumento = new String[lista_boletosLeidos.size()];
        String[] lista_emision = new String[lista_boletosLeidos.size()];
        /* ----------------------------------------- */
        Log.d("tamano adapter", Integer.toString(lista_boletosLeidos.size()));
        Log.d("tamano", lista_boletosLeidos.toString());
        /* Se itera en función a los boletos encontrados */
        for (int i = 0; i < lista_boletosLeidos.size(); i++) {

            //String[] dataBoletoLeido = dataBoletosLeidos[i].split("-");
            String[] dataBoletoLeido = lista_boletosLeidos.get(i).split("\\.");
            // dataBoletoLeido[0] = empresa
            // dataBoletoLeido[1] = serie
            // dataBoletoLeido[2] = correlativo
            // dataBoletoLeido[3] = tipoDocumento
            // dataBoletoLeido[4] = emision
            Log.d("data", dataBoletoLeido[1]);

            lista_empresa[i] = dataBoletoLeido[0];
            lista_serie[i] = dataBoletoLeido[1];
            lista_correlativo[i] = dataBoletoLeido[2];
            lista_tipoDocumento[i] = dataBoletoLeido[3];
            lista_emision[i] = dataBoletoLeido[4];
        }
        /* ----------------------------------------- */

        /* Se asigna un valor a cada elemento */
        textView1.setText(lista_empresa[position]);
        textView2.setText(lista_serie[position]);
        textView3.setText(lista_correlativo[position]);
        textView4.setText(lista_tipoDocumento[position]);
        textView5.setText(lista_emision[position]);
        /* ----------------------------------------- */

        return view;
    }
}
