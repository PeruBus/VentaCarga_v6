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


public class TablaAdapter extends BaseAdapter {

	/**
	* Lista de boletos vendidos.
	*/
    private ArrayList<String> lista_ventas;
	/**
	* Contexto del adaptador.
	*/
    private Context mContext;

	/**
	* Constructor del adaptador.
	* @param array_ventas Lista de boletos vendidos.
	* @param context Contiene el contexto donde se ejecuta esta función.
	*/
    public TablaAdapter(ArrayList<String> array_ventas, Context context){
        lista_ventas = array_ventas;
        mContext = context;
    }

    @Override
    public int getCount() {

        String[] dataReporteVenta = lista_ventas.get(0).split("/");
        return dataReporteVenta.length;
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
	* Asigna la información de un boleto vendido a una fila.
	*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Gson gson = new Gson();

        /* Se invoca la vista de tabla_inspecciones junto con sus elementos */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.tabla_inspecciones, null);

        } else {
            view = convertView;
        }
        /* ----------------------------------------- */

        TextView textView1 = view.findViewById(R.id.textView1);
        TextView textView2 = view.findViewById(R.id.textView2);
        TextView textView3 = view.findViewById(R.id.textView3);
        TextView textView4 = view.findViewById(R.id.textView4);

        String[] dataReporteVenta = lista_ventas.get(0).split("/");

        /* Arreglo */
        // lista_destinos: arreglo que contiene todos los destinos
        if(!dataReporteVenta[0].equals("NoData")) {
            ArrayList<String> lista_destinos = getArray(sharedPreferences, gson, "json_destinos");
            /* ----------------------------------------- */

            /* Listas */
            // lista_boletos: lista que contiene todos los boletos vendidos
            // lista_origen: lista que contiene el origen de los boletos vendidos
            // lista_destino: lista que contiene el destino de los boletos vendidos
            // lista_carga: lista que contiene la afirmación o negación si el boleto encontrado posee carga
            String[] lista_boletos = new String[dataReporteVenta.length];
            String[] lista_origen = new String[dataReporteVenta.length];
            String[] lista_destino = new String[dataReporteVenta.length];
            String[] lista_carga = new String[dataReporteVenta.length];
            /* ----------------------------------------- */

            /* Se itera en funcion al reporte de venta */
            for (int i = 0; i < dataReporteVenta.length; i++) {

                String[] dataAsientosVendidos = dataReporteVenta[i].split("-");
                // dataAsientosVendidos[0] = NUM_ASIENT
                // dataAsientosVendidos[1] = SERIE
                // dataAsientosVendidos[2] = CORRELATIVO
                // dataAsientosVendidos[3] = CO_DEST_ORIG
                // dataAsientosVendidos[4] = CO_DEST_FINA
                // dataAsientosVendidos[5] = CO_CLIE
                // dataAsientosVendidos[6] = IM_TOTA
                // dataAsientosVendidos[7] = CO_EMPR
                // dataAsientosVendidos[8] = TI_DOCU
                // dataAsientosVendidos[9] = LIBERADO
                // dataAsientosVendidos[10] = CARGA
                // dataAsientosVendidos[11] = ServicioEmpresa
                //CARGA:
                // dataAsientosVendidos[12] = TI_PROD
                // dataAsientosVendidos[13] = CA_DOCU

                lista_boletos[i] = dataAsientosVendidos[1] + "-" + dataAsientosVendidos[2];

                for (int j = 0; j < lista_destinos.size(); j++) {
                    String[] dataDestinos = lista_destinos.get(j).split("-");

                    if (dataAsientosVendidos[3].equals(dataDestinos[0])) {
                        lista_origen[i] = dataDestinos[0] + "-" + dataDestinos[1];
                    }
                }

                for (int j = 0; j < lista_destinos.size(); j++) {
                    String[] dataDestinos = lista_destinos.get(j).split("-");

                    if (dataAsientosVendidos[4].equals(dataDestinos[0])) {
                        lista_destino[i] = dataDestinos[0] + "-" + dataDestinos[1];
                    }
                }
                lista_carga[i] = dataAsientosVendidos[10];
            }
            /* ----------------------------------------- */

            /* Se asigna un valor a cada elemento */
            textView1.setText(lista_boletos[position]);
            textView2.setText(lista_origen[position]);
            textView3.setText(lista_destino[position]);
            textView4.setText(lista_carga[position]);
            /* ----------------------------------------- */
        }
        return view;
    }
}
