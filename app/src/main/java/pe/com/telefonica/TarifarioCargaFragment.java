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



public class TarifarioCargaFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tarifario_carga, parent, false);
    }

	/**
	* Implementación de la lógica para generar la tabla con todas las tarifas de carga.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final TableLayout table_layoutTarifa = view.findViewById(R.id.table_layoutTarifa);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        /* Arreglos */
        // lista_productos: arreglo que contiene todos los tipos de productos
        // lista_destinos: arreglo que contiene todos los destinos
        // lista_TarifaCarga: arreglo que contiene todas las tarifas de carga
        final ArrayList<String> lista_productos = getArray(sharedPreferences, gson,"json_productos");
        final ArrayList<String> lista_destinos = getArray(sharedPreferences, gson,"json_destinos");
        final ArrayList<String> lista_TarifaCarga = getArray(sharedPreferences, gson,"anf_jsonTarifasCarga");
        /* ----------------------------------------- */

        /* Se itera en función a la lista de tarifas de carga */
        for(int i = 0; i < lista_TarifaCarga.size(); i++){

            String[] dataTarifaCarga = lista_TarifaCarga.get(i).split("-");
            // dataTarifaCarga[0] = PRODUCTO
            // dataTarifaCarga[1] = ORIGEN
            // dataTarifaCarga[2] = DESTINO
            // dataTarifaCarga[3] = IMPORTE

            /* Se crea la fila y se configura sus propiedades */
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setId(0);

            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tableRow.setPadding(5, 5, 5, 5);

            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 0);
            TableRow.LayoutParams params1 = new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT, 0);
            params.setMargins(0, 0, 5, 0);
            /* ----------------------------------------- */

            TextView tipoProducto = new TextView(getContext());
            TextView origen = new TextView(getContext());
            TextView destino = new TextView(getContext());
            TextView tarifa = new TextView(getContext());

            /* Se obtienen los valores de tipo de producto, origen, destino y tarifa, y se a cada TextView */
            for(int j = 0; j < lista_productos.size(); j++){

                String[] dataProductos = lista_productos.get(j).split("-");
                // dataProductos[0] = TI_PROD
                // dataProductos[1]= DE_TIPO_PROD

                if(dataTarifaCarga[0].equals(dataProductos[0])){

                    tipoProducto.setText(dataProductos[1]);
                    tipoProducto.setLayoutParams(params1);
                    tipoProducto.setTextColor(Color.parseColor("#000000"));
                    tipoProducto.setGravity(Gravity.CENTER);
                    break;
                }
            }

            for(int j = 0; j < lista_destinos.size(); j++){

                String[] dataDestinos = lista_destinos.get(j).split("-");
                // dataDestinos[0] = CO_DEST
                // dataDestinos[1] = DE_DEST

                if(dataTarifaCarga[1].equals(dataDestinos[0])){

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

                if(dataTarifaCarga[2].equals(dataDestinos[0])){

                    destino.setText(dataDestinos[1]);
                    destino.setLayoutParams(params);
                    destino.setTextColor(Color.parseColor("#000000"));
                    destino.setGravity(Gravity.CENTER);
                    break;
                }
            }

            tarifa.setText(dataTarifaCarga[3]);
            tarifa.setLayoutParams(params);
            tarifa.setTextColor(Color.parseColor("#000000"));
            tarifa.setGravity(Gravity.CENTER);
            /* ----------------------------------------- */

            /* Cada TextView se añade a la fila y la fila se añade a la tabla */
            tableRow.addView(tipoProducto);
            tableRow.addView(origen);
            tableRow.addView(destino);
            tableRow.addView(tarifa);

            table_layoutTarifa.addView(tableRow);
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
