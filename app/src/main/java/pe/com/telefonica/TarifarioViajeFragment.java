package pe.com.telefonica.soyuz;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


 
public class TarifarioViajeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tarifario_viaje, parent, false);
    }

	/**
	* Implementación de la lógica para generar la tabla con todas las tarifas de viaje.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final TableLayout table_layoutTarifaViaje = view.findViewById(R.id.table_layoutTarifaViaje);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        /* Arreglos */
        // lista_destinos: arreglo que contiene todos los destinos
        // lista_TarifaViaje: arreglo que contiene todas las tarifas de viaje
        final ArrayList<String> lista_destinos = getArray(sharedPreferences, gson,"json_destinos");
        final ArrayList<String> lista_TarifaViaje = getArray(sharedPreferences, gson,"anf_jsonTarifasViaje");
        /* ----------------------------------------- */

        /* Se itera en función a la lista de tarifas de viaje */
        for(int i = 0; i < lista_TarifaViaje.size(); i++){

            String[] dataTarifaViaje = lista_TarifaViaje.get(i).split("-");
            // dataTarifaViaje[0] = ORIGEN
            // dataTarifaViaje[1] = DESTINO
            // dataTarifaViaje[2] = IMPORTE

            /* Se crea la fila y se configura sus propiedades */
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setId(0);

            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tableRow.setPadding(10, 10, 10, 10);

            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0);
            params.setMargins(0, 0, 100, 0);
            TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0);
            params.setMargins(30, 0, 0, 0);
            /* ----------------------------------------- */

            TextView origen = new TextView(getContext());
            TextView destino = new TextView(getContext());
            TextView tarifa = new TextView(getContext());

            /* Se obtienen los valores de origen, destino y tarifa, y se a cada TextView */
            for(int j = 0; j < lista_destinos.size(); j++){

                String[] dataDestinos = lista_destinos.get(j).split("-");
                // dataDestinos[0] = CO_DEST
                // dataDestinos[1] = DE_DEST

                if(dataTarifaViaje[0].equals(dataDestinos[0])){

                    origen.setText(dataDestinos[1]);
                    origen.setLayoutParams(params);
                    origen.setTextColor(Color.parseColor("#000000"));
                    origen.setGravity(Gravity.CENTER);
                    break;
                }
            }

            for(int j = 0; j < lista_destinos.size(); j++){

                String[] dataDestinos = lista_destinos.get(j).split("-");
                // dataDestinos[0] = CO_DEST
                // dataDestinos[1] = DE_DEST

                if(dataTarifaViaje[1].equals(dataDestinos[0])){

                    destino.setText(dataDestinos[1]);
                    destino.setLayoutParams(params);
                    destino.setTextColor(Color.parseColor("#000000"));
                    destino.setGravity(Gravity.CENTER);
                    break;
                }
            }

            tarifa.setText(dataTarifaViaje[2]);
            tarifa.setLayoutParams(params1);
            tarifa.setTextColor(Color.parseColor("#000000"));
            tarifa.setGravity(Gravity.CENTER);
            /* ----------------------------------------- */

            /* Cada TextView se añade a la fila y la fila se añade a la tabla */
            tableRow.addView(origen);
            tableRow.addView(destino);
            tableRow.addView(tarifa);

            table_layoutTarifaViaje.addView(tableRow);
            /* ----------------------------------------- */
        }
    }

    /**
     * Genera un arreglo partir de un JSON.
     * @param sharedPreferences
     * @param gson
     * @param jsonKey Identificador con el cual se va a buscar un valor en memoria.
     * @return Un arreglo de cadena de caracteres.
     */
    public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey){

        String json = sharedPreferences.getString(jsonKey, "NoData");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<String> lista = new ArrayList<>();

        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }

        return lista;
    }

}
