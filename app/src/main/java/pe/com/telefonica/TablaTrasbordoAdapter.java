package pe.com.telefonica.soyuz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TablaTrasbordoAdapter extends BaseAdapter {

	/**
	* Lista que contiene los boletos que van a ser transbordados.
	*/
    private ArrayList<String> lista_trasbordo;
	/**
	* Contexto del adaptador.
	*/
    private Context mContext;

	/**
	* Constructor del adaptador.
	* @param array_trasbordo Lista que contiene los boletos que van a ser transbordados.
	* @param context Contiene el contexto donde se ejecuta esta función.
	*/
    public TablaTrasbordoAdapter(ArrayList<String>  array_trasbordo, Context context){
        lista_trasbordo = array_trasbordo ;
        mContext = context;
    }

    @Override
    public int getCount() {
        return lista_trasbordo.size();
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
	* Asigna la información de un boleto que va a ser transbordado a una fila.
	*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        /* Listas */
        // lista_boletos: lista que contiene los números de los boletos que se van a trasbordar
        // lista_tipoBoleto: lista que contiene el tipo de cada boleto que se van a trasbordar
        // lista_asiento: lista que contiene el número de los asientos de los boletos que se van a trasbordar
        String[] lista_boletos = new String[lista_trasbordo.size()];
        String[] lista_tipoBoleto = new String[lista_trasbordo.size()];
        String[] lista_asiento = new String[lista_trasbordo.size()];
        /* ----------------------------------------- */

        /* Se itera en función a la lista de trasbordo */
        for (int i = 0; i < lista_trasbordo.size(); i++) {

            String[] dataBoletoTrasbordo = lista_trasbordo.get(i).split("/");
            // dataBoletoTrasbordo[0] = FechaDocumento
            // dataBoletoTrasbordo[1] = NumeroDocumento
            // dataBoletoTrasbordo[2] = TipoDocumento
            // dataBoletoTrasbordo[3] = Asiento

            /* Se agregá el número del boleto, el tipo y el asiento a cada lista */
            lista_boletos[i] = dataBoletoTrasbordo[1];
            lista_tipoBoleto[i] = dataBoletoTrasbordo[2];

            if(dataBoletoTrasbordo.length == 4){
                lista_asiento[i] = dataBoletoTrasbordo[3];
            }else{
                lista_asiento[i] = "";
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */

        /* Se invoca la vista de tabla trasbordo junto con sus elementos */
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.tabla_trasbordo, null);

        } else {
            view = convertView;
        }
        /* ----------------------------------------- */

        TextView textView_trasBoleto = view.findViewById(R.id.textView_trasBoleto);
        TextView textView_trasTipoDoc = view.findViewById(R.id.textView_trasTipoDoc);
        TextView textView_trasSelecAsiento = view.findViewById(R.id.textView_trasSelecAsiento);

        /* Se asigna un valor a cada elemento */
        textView_trasBoleto.setText(lista_boletos[position]);
        textView_trasTipoDoc.setText(lista_tipoBoleto[position]);
        textView_trasSelecAsiento.setText(lista_asiento[position]);
        /* ----------------------------------------- */

        return view;
    }
}
