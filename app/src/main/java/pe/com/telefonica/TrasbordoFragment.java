package pe.com.telefonica.soyuz;

import android.app.Dialog;
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
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.breakTime;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.getArray;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;


 
public class TrasbordoFragment extends Fragment {

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
        return inflater.inflate(R.layout.trasbordo, parent, false);
    }

	/**
	* Implementación de la lógica para realizar el transbordo de boletos de viaje.
	* @param view
	* @param savedInstanceState
	*/
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        /* Inicialización de la base de datos */
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        /* ----------------------------------------- */

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Gson gson = new Gson();

        final Context contextTrasbordo = view.getContext();

        final Dialog selecAsiento_dialog = new Dialog(getActivity());
        selecAsiento_dialog.setContentView(R.layout.seleccionar_asiento_dialog);

        final EditText editText_selecAsiento = selecAsiento_dialog.findViewById(R.id.editText_selecAsiento);
        final Button button_guardarAsiento = selecAsiento_dialog.findViewById(R.id.button_guardarAsiento);

        TextView textView_codEmpresa = view.findViewById(R.id.textView_codEmpresa);
        TextView textView_rumbo = view.findViewById(R.id.textView_rumbo);
        TextView textView_secuencia = view.findViewById(R.id.textView_secuencia);
        TextView textView_fechaProgramacion = view.findViewById(R.id.textView_fechaProgramacion);

        Button btn_leerQR = view.findViewById(R.id.btn_leerQR);
        Button btn_trasbordo = view.findViewById(R.id.btn_trasbordo);

        /* Se obtiene la data del bus que va a realizar el transbordo */
        textView_codEmpresa.setText(sharedPreferences.getString("tras_codigoEmpresa", "NoData"));
        textView_rumbo.setText(sharedPreferences.getString("tras_rumbo", "NoData"));
        textView_secuencia.setText(sharedPreferences.getString("tras_secuencia", "NoData"));
        textView_fechaProgramacion.setText(sharedPreferences.getString("tras_fechaProgramacion", "NoData"));
        /* ----------------------------------------- */

        final String usuarioActual = sharedPreferences.getString("codigoUsuario", "NoData");

        /* Arreglos */
        // lista_asientosVendidosBusEnRuta: arreglo que contiene los boletos vendidos en ruta
        // lista_asientosVendidosBusTrasbordo: arreglo que contiene los número de asiento vendidos
        // lista_trasbordo: arreglo que va almacenar los boletos que se transborden
        final ArrayList<String> lista_asientosVendidosBusEnRuta = getArray(sharedPreferences, gson, "anf_jsonReporteVentaGPS");
        final ArrayList<String> lista_asientosVendidosBusTrasbordo = getArray(sharedPreferences, gson, "insp_jsonReporteVenta");
        final ArrayList<String> lista_trasbordo = getArray(sharedPreferences, gson, "insp_jsonTrasbordo");
        /* ----------------------------------------- */

        /* TODO: CLICK LISTENER DEL BOTÓN QUE LEE EL QR  */
        btn_leerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    /* Se inicializa el scanner del equipo */
                    IDAL idal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = idal.getScanner(EScannerType.REAR);
                    /* ----------------------------------------- */

                    if (iScanner.open()) {
                        iScanner.start(new IScanner.IScanListener() {
                            @Override
                            public void onRead(String codigoQR) {

                                Date date = new Date();

                                final String fechaTrasbordo = new SimpleDateFormat("yyyy-MM-dd").format(date);
                                final String horaTrasbordo = new SimpleDateFormat("hh:mm:ss").format(date);

                                /* Se obtiene la data leída con el scanner */
                                final String[] dataCodigoQR = codigoQR.split("\\|");
                                // dataCodigoQR[2] = serie
                                // dataCodigoQR[3] = correlativo
                                /* ----------------------------------------- */

                                /* Se genera el número del boleto leído */
                                String numCorrelativoBLTLeido = completarCorrelativo(Integer.valueOf(dataCodigoQR[3]));
                                String numBoletoLeido = dataCodigoQR[2] + "-" + numCorrelativoBLTLeido;
                                /* ----------------------------------------- */

                                /* Validación  en caso la lista de asiento vendidos no esté vacía */
                                if (!lista_asientosVendidosBusTrasbordo.get(0).equals("NoData")) {

                                    /* Flag que determina si el boleto está en la lista de trasbordo */
                                    // true: está en la lista
                                    // false: no está en la lista
                                    Boolean flagBoleto = false;
                                    /* ----------------------------------------- */

                                    String[] dataReporteVenta = lista_asientosVendidosBusTrasbordo.get(0).split("/");

                                    /* Se itera en función a los boletos vendidos */
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

                                        /* Se genera el boleto vendido */
                                        String numCorrelativoBLTCompleto = completarCorrelativo(Integer.valueOf(dataAsientosVendidos[2]));
                                        String numBoletoVendido = dataAsientosVendidos[1] + "-" + numCorrelativoBLTCompleto;
                                        /* ----------------------------------------- */

                                        /* Se valida que el boleto no esté agregado a la lista de trasbordo */
                                        if(!lista_trasbordo.isEmpty()){

                                            for(int j = 0; j < lista_trasbordo.size(); j++){

                                                String trama = lista_trasbordo.get(j);
                                                String[] datoTrasbordo = trama.split("/");
                                                // datoTrasbordo[0] = FechaDocumento
                                                // datoTrasbordo[1] = NumeroDocumento
                                                // datoTrasbordo[2] = TipoDocumento
                                                // datoTrasbordo[3] = Asiento(depende si ya hay asiento seleccionado)

                                                /* Validación en caso el boleto leído y el vendido sean iguales */
                                                if(numBoletoLeido.equals(datoTrasbordo[1])){
                                                    flagBoleto = true;
                                                    break;
                                                }
                                            }
                                        }
                                        /* ----------------------------------------- */

                                        /* En caso el boleto no se haya agreago aún a la lista de trasbordo */
                                        if(!flagBoleto){

                                            /* Validación en caso el boleto leído sea igual al boleto vendido */
                                            if (numBoletoLeido.equals(numBoletoVendido) && dataAsientosVendidos[11].equals("VIAJE")) {

                                                String fechaDocumento = fechaTrasbordo + "T" + horaTrasbordo;
                                                String boletoTrasbordo = fechaDocumento + "/" + numBoletoLeido + "/" + dataAsientosVendidos[8];


                                                lista_trasbordo.add(boletoTrasbordo);
                                                String json_trasbordo = gson.toJson(lista_trasbordo);
                                                guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, contextTrasbordo);
                                                break;

                                            }
                                            /* ----------------------------------------- */
                                        }
                                        /* ----------------------------------------- */
                                    }
                                    /* ----------------------------------------- */

                                    /* En caso el boleto no se haya agreago aún a la lista de trasbordo */
                                    if(!flagBoleto){

                                        final ListView listView = view.findViewById(R.id.listView_trasbordo);
                                        final TablaTrasbordoAdapter adapterTrasbordo = new TablaTrasbordoAdapter(lista_trasbordo, getActivity());
                                        listView.setAdapter(adapterTrasbordo);

                                        /* TODO: ITEM CLICK LISTENER EN LA LISTA */
                                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                                                /* TODO: CLICK LISTENER DEL BOTÓN GUARDAR ASIENTO */
                                                button_guardarAsiento.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        /* Flag que determina si el asiento ya esté asignado a otro boleto de transbordo */
                                                        // true: está asignado
                                                        // false: no está asignado
                                                        Boolean flagAsiento = false;
                                                        /* ----------------------------------------- */

                                                        String numAsiento = editText_selecAsiento.getText().toString();
                                                        int numAsientosTotales = sharedPreferences.getInt("anf_numAsientos", 0);

                                                        /* Validación del asiento seleccionado para el boleto que se va a transbordar */
                                                        if(Integer.valueOf(numAsiento) > numAsientosTotales){
                                                            Toast.makeText(getActivity(), "El asiento seleccionado excede el número total de asientos en el bus.", Toast.LENGTH_SHORT).show();

                                                        } else if(numAsiento.equals("")){
                                                            Toast.makeText(getActivity(), "Debe seleccionar un asiento.", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            /* Validación en caso la lista de trasbordo no esté vacía */
                                                            if(!lista_trasbordo.isEmpty()){

                                                                for(int j = 0; j < lista_trasbordo.size(); j++){

                                                                    String trama = lista_trasbordo.get(j);
                                                                    String[] datoTrasbordo = trama.split("/");
                                                                    // datoTrasbordo[0] = FechaDocumento
                                                                    // datoTrasbordo[1] = NumeroDocumento
                                                                    // datoTrasbordo[2] = TipoDocumento
                                                                    // datoTrasbordo[3] = Asiento(depende si ya hay asiento seleccionado)

                                                                    /* Validación en caso el asiento seleccionado ya haya asignado a otro boleto que va a ser transbordado */
                                                                    if(datoTrasbordo.length == 4){

                                                                        if(numAsiento.equals(datoTrasbordo[3])){
                                                                            Toast.makeText(getActivity(), "El asiento ya fue seleccionado.", Toast.LENGTH_SHORT).show();
                                                                            flagAsiento = true;
                                                                            break;
                                                                        }
                                                                    }
                                                                    /* ----------------------------------------- */
                                                                }
                                                                /* ----------------------------------------- */
                                                            }
                                                            /* ----------------------------------------- */

                                                            /* En caso el boleto no se haya agreago aún a la lista de trasbordo */
                                                            if(!flagAsiento){

                                                                /* Flag que determina si el asiento está ocupado en el bus */
                                                                // true: está ocupado
                                                                // false: no está ocupado
                                                                Boolean flag = false;
                                                                /* ----------------------------------------- */

                                                                /* Validación en caso la lista de asientos vendido no esté vacía */
                                                                if (!lista_asientosVendidosBusEnRuta.isEmpty()) {

                                                                    String[] dataReporteVenta = lista_asientosVendidosBusEnRuta.get(0).split("/");

                                                                    /* Se itera en función a los boletos vendidos */
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

                                                                        /* Validación en caso el asiento seleccionado ya esté ocupado en el bus */
                                                                        if (numAsiento.equals(dataAsientosVendidos[0])) {
                                                                            flag = true;
                                                                            Toast.makeText(getActivity(), "El asiento seleccionado se encuentra ocupado.", Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                        }
                                                                        /* ----------------------------------------- */
                                                                    }
                                                                    /* ----------------------------------------- */
                                                                }
                                                                /* ----------------------------------------- */

                                                                /* Validación en caso el asiento no esté ocupado en el bus */
                                                                if (!flag) {

                                                                    String trama = lista_trasbordo.get(position);
                                                                    String[] datoTrasbordo = trama.split("/");
                                                                    // datoTrasbordo[0] = FechaDocumento
                                                                    // datoTrasbordo[1] = NumeroDocumento
                                                                    // datoTrasbordo[2] = TipoDocumento
                                                                    // datoTrasbordo[3] = Asiento(depende si ya hay asiento seleccionado)

                                                                    /* Se asigna o se actualiza el número de asiento de un boleto que va a ser transbordado */
                                                                    if (datoTrasbordo.length > 3) {
                                                                        datoTrasbordo[3] = numAsiento;
                                                                        trama = datoTrasbordo[0] + "/" + datoTrasbordo[1] + "/" + datoTrasbordo[2] + "/" + datoTrasbordo[3];
                                                                        lista_trasbordo.set(position, trama);

                                                                        String json_trasbordo = gson.toJson(lista_trasbordo);
                                                                        guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, contextTrasbordo);

                                                                    } else {
                                                                        trama = trama + "/" + numAsiento;
                                                                        lista_trasbordo.set(position, trama);

                                                                        String json_trasbordo = gson.toJson(lista_trasbordo);
                                                                        guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, contextTrasbordo);
                                                                    }
                                                                    /* ----------------------------------------- */
                                                                }
                                                                /* ----------------------------------------- */
                                                            }
                                                            /* ----------------------------------------- */
                                                        }
                                                        /* ----------------------------------------- */

                                                        adapterTrasbordo.notifyDataSetChanged();
                                                        selecAsiento_dialog.hide();
                                                    }

                                                });
                                                /* ----------------------------------------- */
                                                selecAsiento_dialog.show();
                                            }
                                        });
                                        /* ----------------------------------------- */
                                    }else {
                                        Toast.makeText(getActivity(), "El boleto ya fue agregado.", Toast.LENGTH_SHORT).show();
                                    }
                                    /* ----------------------------------------- */
                                }
                                /* ----------------------------------------- */
                            }

                            @Override
                            public void onFinish() {}

                            @Override
                            public void onCancel() {}
                        });
                    }
                    /* ----------------------------------------- */
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al scanear el boleto.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                }
            }
        });
        /* ----------------------------------------- */

        /* TODO: CLICK LISTENER DEL BOTÓN DE TRASBORDO */
        btn_trasbordo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Queue para el request */
                final RequestQueue queue = Volley.newRequestQueue(getActivity());
                /* ----------------------------------------- */

                /* Se obtiene el JSON que se va a enviar a la Web service */
                final JSONObject jsonObject = generarJSONTrasbordo(sharedPreferences, lista_trasbordo, usuarioActual);
                /* ----------------------------------------- */

                try {
                    /* Se obtiene el JSON generado */
                    JSONArray boletosKeys = (JSONArray) jsonObject.get("Boletos");
                    /* ----------------------------------------- */

                    if (boletosKeys.length() != 0) {

                        /* Ruta de la Web service */
                        String ws_trasbordo = getString(R.string.ws_ruta) + "Transbordo";
                        /* ----------------------------------------- */

                        /* Request que envía los boletos que se van a transbordar */
                        MyJSONArrayRequest jsonArrayRequestTrasbordo = new MyJSONArrayRequest(Request.Method.POST, ws_trasbordo, jsonObject,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        if (response.length() > 0) {

                                            JSONObject info;
                                            try {

                                                info = response.getJSONObject(0);

                                                String respuesta = info.getString("Respuesta");

                                                /* Si la respuesta es "guardado" el boleto se guarda en la BD */
                                                if (respuesta.equals("GUARDADO")) {

                                                    /* Se guarda el boleto en la BD */
                                                    ContentValues cv = new ContentValues();
                                                    cv.put("data_boleto", jsonObject.toString());
                                                    cv.put("estado", "guardado");

                                                    Long id = sqLiteDatabase.insert("TransbordoBoletos", null, cv);
                                                    /* ----------------------------------------- */

                                                    /* Ruta de la Web service */
                                                    String ws_getAsientosVendidos = getString(R.string.ws_ruta) + "ReporteVentaRuta/" + sharedPreferences.getString("anf_codigoEmpresa", "NoData") + "/" +
                                                            sharedPreferences.getString("anf_secuencia", "NoData") + "/" + sharedPreferences.getString("anf_rumbo", "NoData") + "/" +
                                                            sharedPreferences.getString("anf_fechaProgramacion", "NoData");
                                                    /* ----------------------------------------- */

                                                    final ArrayList<String> lista_reporteVenta = new ArrayList<>();
                                                    final ArrayList<String> lista_trasbordo = new ArrayList<>();

                                                    /* Request que obtiene los asientos vendidos y se actualiza en memoria */
                                                    JsonArrayRequest jsonArrayRequestAsientosVendidos = new JsonArrayRequest(Request.Method.GET, ws_getAsientosVendidos, null,
                                                            new Response.Listener<JSONArray>() {
                                                                @Override
                                                                public void onResponse(JSONArray response) {

                                                                    String asientosVend = "";

                                                                    if (response.length() == 0) {

                                                                        asientosVend = "NoData";
                                                                        lista_reporteVenta.add(asientosVend);
                                                                        String json_reporteVenta = gson.toJson(lista_reporteVenta);
                                                                        guardarDataMemoria("insp_jsonReporteVenta", json_reporteVenta, getActivity());

                                                                        String json_trasbordo = gson.toJson(lista_trasbordo);
                                                                        guardarDataMemoria("insp_jsonTrasbordo", json_trasbordo, getActivity());

                                                                        /* Se actualiza la vista de trasbordo */
                                                                        TrasbordoFragment trasbordoFragment = new TrasbordoFragment();
                                                                        FragmentManager fragmentManager = getFragmentManager();
                                                                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                        fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();
                                                                        /* ----------------------------------------- */

                                                                    } else if (response.length() > 0) {
                                                                        try {

                                                                            JSONObject info;

                                                                            /* Arreglos */
                                                                            // lista_reporteVenta: arreglo que contien los número de asientos vendidos
                                                                            // lista_reporteVentaGPS: arreglo que contiene los boletos vendidos
                                                                            ArrayList<String> lista_reporteVenta = new ArrayList<>();
                                                                            ArrayList<String> lista_reporteVentaGPS = new ArrayList<>();
                                                                            /* ----------------------------------------- */

                                                                            /* Se itera en función a la respuesta y se guarda el número de asiento y la data del boleto completo */
                                                                            //TODO: vendido en el arreglo.
                                                                            for (int i = 0; i < response.length(); i++) {

                                                                                info = response.getJSONObject(i);
                                                                                if (info.getString("ServicioEmpresa").equals("VIAJE") && info.getString("LIBERADO").equals("NO")) {

                                                                                    lista_reporteVenta.add(info.getString("NU_ASIE"));

                                                                                    asientosVend += info.getString("NU_ASIE") + "-" + info.getString("NU_DOCU") + "-" +
                                                                                            info.getString("CO_DEST_ORIG") + "-" + info.getString("CO_DEST_FINA") + "-" +
                                                                                            info.getString("CO_CLIE") + "-" + info.getString("IM_TOTA") + "-" +
                                                                                            info.getString("CO_EMPR") + "-" + info.getString("TI_DOCU") + "-" +
                                                                                            info.getString("LIBERADO") + "-" + info.getString("CARGA") + "-" +
                                                                                            info.getString("ServicioEmpresa") + "/";
                                                                                }
                                                                            }
                                                                            /* ----------------------------------------- */

                                                                            /* Se elimina el último "/" de la trama, se agrega al arreglo y se guarda en memoria */
                                                                            asientosVend = asientosVend.substring(0, asientosVend.length() - 1);
                                                                            lista_reporteVentaGPS.add(asientosVend);
                                                                            String json_reporteVentaGPS = gson.toJson(lista_reporteVentaGPS);
                                                                            guardarDataMemoria("anf_jsonReporteVentaGPS", json_reporteVentaGPS, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se actualiza el arrelgo de asientos vendidos en ruta y se guarda en memoria */
                                                                            ArrayList<String> lista_asientosVendidosRuta = new ArrayList<>();
                                                                            String jsonReporteVentaRuta = gson.toJson(lista_asientosVendidosRuta);
                                                                            guardarDataMemoria("anf_jsonReporteVentaRuta", jsonReporteVentaRuta, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se guarda el arreglo de asientos vendidos en memoria */
                                                                            String jsonReporteVenta = gson.toJson(lista_reporteVenta);
                                                                            guardarDataMemoria("anf_jsonReporteVenta", jsonReporteVenta, getActivity());
                                                                            /* ----------------------------------------- */

                                                                            /* Se actualiza la vista de trasbordo */
                                                                            TrasbordoFragment trasbordoFragment = new TrasbordoFragment();
                                                                            FragmentManager fragmentManager = getFragmentManager();
                                                                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                            fragmentTransaction.replace(R.id.fragment_base, trasbordoFragment).commit();
                                                                            /* ----------------------------------------- */

                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    /* ----------------------------------------- */
                                                                }
                                                            }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            error.printStackTrace();
                                                            Toast.makeText(getActivity(), "Error en la ws ReporteVenta.", Toast.LENGTH_LONG).show();
                                                            errorWS(queue, error);
                                                        }
                                                    }) {
                                                        @Override
                                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                                            Map<String, String> headers = new HashMap<>();
                                                            String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                                                            String auth = "Basic "
                                                                    + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                                                            headers.put("Content-Type", "application/json");
                                                            headers.put("Authorization", auth);
                                                            return headers;
                                                        }
                                                    };
                                                    /* ----------------------------------------- */
                                                    queue.add(jsonArrayRequestAsientosVendidos);
                                                    breakTime();
                                                }
                                                /* ----------------------------------------- */
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        /* ----------------------------------------- */
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(getActivity(), "Error en la ws Trasbordo. No se pudo realizar el trasbordo.", Toast.LENGTH_LONG).show();

                                Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();

                                /* Se guarda el boleto en la DB */
                                ContentValues cv = new ContentValues();
                                cv.put("data_boleto", jsonObject.toString());
                                cv.put("estado", "pendiente");

                                Long id = sqLiteDatabase.insert("TransbordoBoletos", null, cv);
                                /* ----------------------------------------- */

                                error.printStackTrace();

                                /* Se inicia el servicio de trasbordo */
                                startTrasbordoService();
                                /* ----------------------------------------- */
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                String credentials = getString(R.string.ws_user) + ":" + getString(R.string.ws_password);
                                String auth = "Basic "
                                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", auth);
                                return headers;
                            }
                            /* ----------------------------------------- */
                        };
                        /* ----------------------------------------- */
                        jsonArrayRequestTrasbordo.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(jsonArrayRequestTrasbordo);
                    } else {
                        Toast.makeText(getActivity(), "No hay boletos para transbordo.", Toast.LENGTH_SHORT).show();
                    }
                    /* ----------------------------------------- */
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al generar el json para trasbordo.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);

                }
            }
        });
        /* ----------------------------------------- */
    }

	/**
	* Inicializa el servicio de transbordo.
	*/
    public void startTrasbordoService() {
        BoletoService.startService(getActivity(), true);
    }

	/**
	* Detiene el servicio de transbordo.
	*/
    public void stopTrasbordoService() {
        BoletoService.startService(getActivity(), false);
    }

	/**
	* Genera un JSON con la información de todos los boletos que van a ser transbordados.
	* @param sharedPreferences
	* @param lista_trasbordo Lista de boletos que serán transbordados
	* @param usuarioActual Usuario que realiza el transbordo.
	* @return Objeto JSON que contiene toda la data de los boletos que van a ser transbordados.
	*/
    public JSONObject generarJSONTrasbordo(SharedPreferences sharedPreferences, ArrayList<String> lista_trasbordo, String usuarioActual) {

        JSONObject trasbordo = new JSONObject();
        JSONArray boletos = new JSONArray();
        JSONObject boletosTrasbordo = new JSONObject();

        for (int i = 0; i < lista_trasbordo.size(); i++) {

            String[] dataBoletosTrasbordo = lista_trasbordo.get(i).split("/");

            try {

                boletosTrasbordo.put("FechaDocumento", dataBoletosTrasbordo[0]);
                boletosTrasbordo.put("Asiento", dataBoletosTrasbordo[3]);
                boletosTrasbordo.put("Empresa", sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
                boletosTrasbordo.put("NumeroDocumento", dataBoletosTrasbordo[1]);
                boletosTrasbordo.put("TipoDocumento", dataBoletosTrasbordo[2]);
                boletosTrasbordo.put("RumboItinerario", sharedPreferences.getString("anf_rumbo", "NoData"));
                boletosTrasbordo.put("FechaViajeItin", sharedPreferences.getString("anf_fechaProgramacion", "NoData"));
                boletosTrasbordo.put("SecuenciaItin", sharedPreferences.getString("anf_secuencia", "NoData"));
                boletosTrasbordo.put("UsuarioRegistro", usuarioActual);

                boletos.put(boletosTrasbordo);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Error al generar el json para trasbordo.", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), ErrorActivity.class);
                startActivity(intent);
            }
        }

        try {
            trasbordo.put("Boletos", boletos);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al generar el json para trasbordo.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

        return trasbordo;
    }

    /**
     * Mapea el error del WS y muestra una vista de error.
     * @param queue Contiene el queue del request.
     * @param error Determina el tipo de error de la Web Service.
     */
    private void errorWS(RequestQueue queue, VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar conectividad del dispositivo.", Toast.LENGTH_LONG).show();

        }else if (error instanceof TimeoutError) {
            Toast.makeText(getActivity(), "Se excedió el tiempo de espera.", Toast.LENGTH_LONG).show();

        } else if (error instanceof AuthFailureError) {
            Toast.makeText(getActivity(), "Error en la autenticación.", Toast.LENGTH_LONG).show();

        } else if (error instanceof ServerError) {
            Toast.makeText(getActivity(), "No se pudo conectar con el servidor. Revisar credenciales e IP del servidor.", Toast.LENGTH_LONG).show();

        } else if (error instanceof NetworkError) {
            Toast.makeText(getActivity(), "No hay conectividad.", Toast.LENGTH_LONG).show();

        }else if (error instanceof ParseError) {
            Toast.makeText(getActivity(), "Se recibe null como respuesta del servidor.", Toast.LENGTH_LONG).show();

        }

        queue.getCache().clear();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });

        Intent intent = new Intent(getActivity(), ErrorActivity.class);
        startActivity(intent);
    }
}
