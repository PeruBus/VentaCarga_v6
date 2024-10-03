package pe.com.telefonica.soyuz;


import android.content.ContentValues;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;


public class InspectorFormularioFragment extends Fragment {

	/**
	* Base de datos interna.
	*/
    private DatabaseBoletos ventaBlt;
	/**
	* Instancia de SQLiteDatabase.
	*/
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.formulario_inspeccion, parent, false);
    }

	/**
	* Implementación de la lógica para realizar una inspección de un boleto y lo guarda en la Base de Datos.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /* Inicialización de la base de datos */
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        /* ----------------------------------------- */

        final Context contextFormularioInspeccion = view.getContext();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final ImageView imageView_state = view.findViewById(R.id.imageView_state);

        final TextView textView_origen = view.findViewById(R.id.textView_origen);
        final TextView textView_destino = view.findViewById(R.id.textView_destino);
        final TextView textView_tarifa = view.findViewById(R.id.textView_tarifa);
        final TextView textView_dni = view.findViewById(R.id.textView_dni);
        final TextView textView_carga = view.findViewById(R.id.textView_carga);
        final TextView textView_inspeccionBoleto = view.findViewById(R.id.textView_inspeccionBoleto);

        final EditText editText_asiento = view.findViewById(R.id.editText_asiento);
        final EditText editText_descripcion = view.findViewById(R.id.editText_descripcion);

        final Spinner spinner_asunto = view.findViewById(R.id.spinner_asunto);

        final TextView label_carga = view.findViewById(R.id.label_carga);
        final TextView label_tipoProducto = view.findViewById(R.id.label_tipoProducto);
        final TextView label_cantidad = view.findViewById(R.id.label_cantidad);
        final TextView textView_tipoProducto = view.findViewById(R.id.textView_tipoProducto);
        final TextView textView_cantidad = view.findViewById(R.id.textView_cantidad);

        final Button button_guardarInspeccion = view.findViewById(R.id.button_guardarInspeccion);

        /* Se obtiene la fecha y hora que se realiza la inspección y se guarda en memoria */
        Date date = new Date();
        String fechaInspeccion = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String horaInicio = new SimpleDateFormat("hhmmss").format(date);

        guardarDataMemoria("guardar_fechaInspeccion", fechaInspeccion, contextFormularioInspeccion);
        guardarDataMemoria("guardar_horaInicio", horaInicio, contextFormularioInspeccion);
        /* ----------------------------------------- */

        /* Mensaje en caso el boleto haya sido liberado */
        if(sharedPreferences.getString("insp_liberado", "NoData").equals("SI")){
            Toast.makeText(getActivity(), "El asiento de este boleto ya fue liberado." , Toast.LENGTH_LONG);
        }
        /* ----------------------------------------- */

        /* Se muestra la data del boleto que se está inspeccionando en la pantalla */
        textView_origen.setText(sharedPreferences.getString("insp_origenNombre", "NoData"));
        textView_destino.setText(sharedPreferences.getString("insp_destinoNombre", "NoData"));
        textView_tarifa.setText(sharedPreferences.getString("insp_tarifa", "NoData"));
        textView_dni.setText(sharedPreferences.getString("insp_dni", "NoData"));
        textView_carga.setText(sharedPreferences.getString("insp_carga", "NoData"));
        textView_inspeccionBoleto.setText(sharedPreferences.getString("insp_inspeccionBoleto", "NoData"));
        editText_asiento.setText(sharedPreferences.getString("insp_asiento", "NoData"));
        /* ----------------------------------------- */

        /* Se determina si se va a obtener los eventos de viaje o carga y se bloquean los campos de producto y cantidad según el tipo de servicio */
        String jsonKey = "";
        if (sharedPreferences.getString("insp_servicioEmpresa", "NoData").equals("VIAJE")) {

            jsonKey = "json_eventosViaje";

            label_tipoProducto.setVisibility(View.INVISIBLE);
            label_cantidad.setVisibility(View.INVISIBLE);
            textView_tipoProducto.setVisibility(View.INVISIBLE);
            textView_cantidad.setVisibility(View.INVISIBLE);

        } else if (sharedPreferences.getString("insp_servicioEmpresa", "NoData").equals("CARGA")) {

            jsonKey = "json_eventosCarga";

            label_carga.setVisibility(View.INVISIBLE);
            textView_carga.setVisibility(View.INVISIBLE);
            label_tipoProducto.setVisibility(View.VISIBLE);
            label_cantidad.setVisibility(View.VISIBLE);
            textView_tipoProducto.setVisibility(View.VISIBLE);
            textView_cantidad.setVisibility(View.VISIBLE);

            /* Arreglo */
            // lista_productos: arreglo que contiene todos los tipos de productos
            final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
            /* ----------------------------------------- */

            /* Se muestra el tipo se producto en la pantalla y se guarda en memoria */
            for (int j = 0; j < lista_productos.size(); j++) {

                String[] dataProductos = lista_productos.get(j).split("-");
                // dataProductos[0] = TI_PROD
                // dataProductos[1]= DE_TIPO_PROD

                if (sharedPreferences.getString("insp_tipoProducto", "NoData").equals(dataProductos[0])) {
                    textView_tipoProducto.setText(dataProductos[1]);
                    guardarDataMemoria("guardar_tipoProducto", dataProductos[1], contextFormularioInspeccion);
                }
            }
            /* ----------------------------------------- */

            textView_cantidad.setText(sharedPreferences.getString("insp_cantidad", "NoData"));
        }
        /* ----------------------------------------- */

        /* Arreglos */
        // lista_eventos: arreglo que contiene todos los eventos de viaje o carga
        // lista_idEventos: arreglo que contiene el ID de los eventos
        // lista_nombreEventos: arreglo que contiene el nombre de los eventos
        // lista_semaforoEventos: arreglo que contiene los colores semáforo
        final ArrayList<String> lista_eventos = getArray(sharedPreferences,gson, jsonKey);
        final ArrayList<String> lista_idEventos = new ArrayList<>();
        final ArrayList<String> lista_nombreEventos = new ArrayList<>();
        final ArrayList<String> lista_semaforoEventos = new ArrayList<>();
        /* ----------------------------------------- */

        /* Se itera en función a la lista de eventos */
        for (int j = 0; j < lista_eventos.size(); j++) {

            String[] dataEventosViaje = lista_eventos.get(j).split("-");
            // dataEventosViaje[0] = NU_SECU
            // dataEventosViaje[1] = ID_EVENT
            // dataEventosViaje[2] = DE_EVENT
            // dataEventosViaje[3] = DE_SEMAF2

            lista_idEventos.add(dataEventosViaje[1]);
            lista_nombreEventos.add(dataEventosViaje[2]);
            lista_semaforoEventos.add(dataEventosViaje[3]);
        }
        /* ----------------------------------------- */

        /* Se agrega la lista de nombre de eventos al spinner de asunto */
        ArrayAdapter<String> adapterEventos = new ArrayAdapter<String>(contextFormularioInspeccion, android.R.layout.simple_spinner_item, lista_nombreEventos);
        spinner_asunto.setAdapter(adapterEventos);
        /* ----------------------------------------- */

        /* TODO: ITEM SELECTED LISTENER DEL SPINNER DE ASUNTO */
        spinner_asunto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /* Se obtiene el ID del evento y el color del semáforo correspondiente */
                String idEvento = lista_idEventos.get(position);

                String colorSemaforo = lista_semaforoEventos.get(position);
                guardarDataMemoria("guardar_idEvento", idEvento, contextFormularioInspeccion);
                guardarDataMemoria("guardar_semaforoEvento", colorSemaforo, contextFormularioInspeccion);
                /* ----------------------------------------- */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /* ----------------------------------------- */

        /* En caso el boleto leído ya tenga un color de semáforo asignado, se muestra en la pantalla */
        if(!sharedPreferences.getString("insp_semaforo", "NoData").equals("NoData")){

            if(sharedPreferences.getString("insp_semaforo", "NoData").equals("GREEN")){

                imageView_state.setImageResource(R.drawable.green_state);

            }else if(sharedPreferences.getString("insp_semaforo", "NoData").equals("YELLOW")){

                imageView_state.setImageResource(R.drawable.yellow_state);

            } else if(sharedPreferences.getString("insp_semaforo", "NoData").equals("RED")){

                imageView_state.setImageResource(R.drawable.red_state);

            }
        }
        /* ----------------------------------------- */

        /* Validación en caso el tipo de inspección y el ID del evento no sea vacío */
        if(!sharedPreferences.getString("insp_tipoInspeccion", "NoData").equals("NoData") &&
                !sharedPreferences.getString("insp_idEvento", "NoData").equals("NoData")){

            /* En caso el boleto leído ya tenga un evento asignado, se muestra en pantalla */
            if(sharedPreferences.getString("insp_tipoInspeccion", "NoData").equals("VIAJE")){

                int posicionAsunto = Integer.valueOf(sharedPreferences.getString("insp_idEvento", "NoData")) - 1;
                spinner_asunto.setSelection(posicionAsunto);

            }else if(sharedPreferences.getString("insp_tipoInspeccion", "NoData").equals("CARGA")){

                /* Arreglo */
                // lista_eventosCarga: arreglo que contiene todos los eventos de carga
                ArrayList<String> lista_eventosCarga = getArray(sharedPreferences,gson, "json_eventosCarga");
                /* ----------------------------------------- */

                int posicionAsunto = 0;

                for(int j = 0; j < lista_eventosCarga.size(); j++ ){
                    String[] dataEventosCarga = lista_eventosCarga.get(j).split("-");
                    // dataEventosCarga[1] = ID_EVENT
                    // dataEventosCarga[2] = DE_EVENT
                    // dataEventosCarga[3] = DE_SEMAF2

                    if(sharedPreferences.getString("insp_idEvento", "NoData").equals(dataEventosCarga[1])){
                        posicionAsunto = j;
                        break;
                    }
                }
                spinner_asunto.setSelection(posicionAsunto);
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */

        /* TODO: CLICK LISTENER PARA GUARDAR INSPECCIÓN DEL BOLETO */
        button_guardarInspeccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                button_guardarInspeccion.setEnabled(false);

                /* Validación en caso el asiento no sea vacío */
                if(!editText_asiento.getText().toString().equals("")){

                    guardarDataMemoria("guardar_asiento", editText_asiento.getText().toString(), contextFormularioInspeccion);

                    /* Se obtiene la hora que se inspecciona el boleto */
                    Date date = new Date();
                    String horaFin = new SimpleDateFormat("hhmmss").format(date);
                    /* ----------------------------------------- */

                    /* Se obtiene el JSON generado */
                    JSONObject jsonObject = generarJSONReporte(sharedPreferences, horaFin, editText_descripcion.getText().toString());
                    /* ----------------------------------------- */

                    /* Se guarda el boleto inspeccionado en la BD */
                    ContentValues cv = new ContentValues();
                    cv.put("data_boleto", jsonObject.toString());

                    Long id = sqLiteDatabase.insert("InspeccionBoletos", null, cv);
                    /* ----------------------------------------- */

                    /* Se cambia a la vista de inspeccion de venta */
                    InspeccionVentaFragment inspeccionVentaFragment = new InspeccionVentaFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_base, inspeccionVentaFragment).commitAllowingStateLoss();
                    /* ----------------------------------------- */

                }else{
                    Toast.makeText(getActivity(), " Tiene que ingresar un asiento", Toast.LENGTH_SHORT).show();
                    button_guardarInspeccion.setEnabled(true);
                }

            }
        });
        /* ----------------------------------------- */
    }

	/**
	* Genera un JSON con la información de la inspección realizada.
	* @param sharedPreferences
	* @param horaFin Hora que finalizó la inspección del boleto.
	* @param descripcion Descripción como parte de la inspección realizada.
	*/
    public JSONObject generarJSONReporte(SharedPreferences sharedPreferences, String horaFin, String descripcion) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("IdEvento", sharedPreferences.getString("guardar_idEvento", "NoData"));
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("insp_codigoEmpresa", "NoData"));
            jsonObject.put("TipoDocumento", sharedPreferences.getString("insp_tipoDocumento", "NoData"));
            jsonObject.put("NumeroDocumento", sharedPreferences.getString("insp_numBoleto", "NoData"));
            jsonObject.put("Observacion", descripcion);
            jsonObject.put("InicioInspeccion", sharedPreferences.getString("guardar_horaInicio", "NoData"));
            jsonObject.put("FinInspeccion", horaFin);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para el reporte de inspección.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

        return jsonObject;
    }
}
