package pe.com.telefonica.soyuz;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.numeroIntentos;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.timeout;



public class CargaExtraFragment extends Fragment {

    /**
     * Constantes para la interaccion con el APK de DigiFlow.
     */
    public static final String SOLICITA_TED = "ted";
    public static final String PATH_XML = "xml";
    public static final String COLUMN_NAME_XML64 = "xml64";
    public static final String COLUMN_NAME_TED = "ted";
    public static final String COLUMN_NAME_TED64 = "ted64";

    /**
     * Base de datos interna.
     */
    private DatabaseBoletos ventaBlt;
    /**
     * Instancia de SQLiteDatabase.
     */
    private SQLiteDatabase sqLiteDatabase;
    /**
     * Flag para verificar si se encontró el boleto.
     */
    Boolean boletoEncontrado = false;
    /**
     * Constante para la interaccion con el APK de DigiFlow.
     */
    JSONArray getCorrelativo = null;
    /**
     * Instancia para guardar datos en memoria.
     */
    private SharedPreferences sharedPreferences;
    /**
     * Valor de la empresa que esta realizando la venta.
     */
    String empresaSeleccionada = "";
    Button button_imprimirBoletoCarga;
    Gson gson;

    int numCorrelativoCargaSeleccionado = 0;
    String numSerieCargaSeleccionado = "";
    /**
     * Valor del correlativo seleccionado.
     */
    String correlativoSeleccionado = "";
    /**
     * Valor de la serie seleccionada.
     */
    String serieSeleccionado = "";
    /**
     * Valor del documento de identidad del cliente.
     */
    String dni = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        /* Inicialización de la instancia para guardar datos en memoria */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        /* ----------------------------------------- */

        /* Se inicializa el servicio para boletero */
        if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
            BoletoService.startService(getActivity(), false);
        }
        /* ----------------------------------------- */

        return inflater.inflate(R.layout.venta_carga_extra, parent, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        gson = new Gson();

        final EditText editText_serie = view.findViewById(R.id.editText_serie);
        final EditText editText_correlativo = view.findViewById(R.id.editText_correlativo);
        final EditText editText_tarifaBase = view.findViewById(R.id.editText_tarifaBase);
        final EditText editText_tarifaCarga = view.findViewById(R.id.editText_tarifaCarga);
        final EditText editText_cantidad = view.findViewById(R.id.editText_cantidad);

        final Spinner spinner_tipoProducto = view.findViewById(R.id.spinner_tipoProducto);

        button_imprimirBoletoCarga = view.findViewById(R.id.button_imprimirBoletoCarga);
        Button button_validar = view.findViewById(R.id.button_validar);

        if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){

            /* Se obtiene el JSON Array de los correlativos */
            try {

                getCorrelativo = new JSONArray(sharedPreferences.getString("bol_getCorrelativo", "NoData"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            /* ----------------------------------------- */
        }

        button_validar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Validación en caso la serie y el correlativo no esten vacíos */
                if (!editText_serie.getText().toString().equals("") && !editText_correlativo.getText().toString().equals("")) {

                    /* Se completa con ceros el correlativo y se genera el número de boleto que se va a buscar */
                    String correlativoCompleto = getNumCorrelativo(Integer.valueOf(editText_correlativo.getText().toString()));
                    String serie = editText_serie.getText().toString().toUpperCase();
                    String documentoBuscar = serie + "-" + correlativoCompleto;
                    /* ----------------------------------------- */

                    /* Inicialización de la base de datos */
                    ventaBlt = new DatabaseBoletos(getActivity());
                    sqLiteDatabase = ventaBlt.getWritableDatabase();
                    /* ----------------------------------------- */

                    /* Se obtienen todos los boletos */
                    Cursor cursor = sqLiteDatabase.query("VentaBoletos", null, null, null, null, null, null);
                    /* ----------------------------------------- */

                    /* Iteración en función a la cantidad filas obtenidas en el query */
                    while (cursor.moveToNext()) {

                        /* Se obtiene el JSON (string) y el tipo (viaje/carga) */
                        String data = cursor.getString(cursor.getColumnIndex("data_boleto"));
                        String tipo = cursor.getString(cursor.getColumnIndex("tipo"));
                        /* ----------------------------------------- */

                        try {

                            /* Se genera un JSON a partir de un string */
                            final JSONObject jsonObject = new JSONObject(data);
                            /* ----------------------------------------- */

                            /* Validación en caso el boleto a buscar sea igual al número de boleto en la BD y que sea de tipo "viaje" */
                            if (tipo.equals("viaje") && documentoBuscar.equals(jsonObject.getString("NumeroDocumento"))) {

                                guardarDataMemoria("boletoSeleccionado", data);
                                guardarDataMemoria("extra_documentoCliente", jsonObject.getString("CodigoCliente"));
                                guardarDataMemoria("extra_empresa", jsonObject.getString("Empresa"));
                                guardarDataMemoria("extra_tipoDocumentoBoleto", jsonObject.getString("tipoDocumento"));
                                guardarDataMemoria("extra_numDocumentoBoleto", jsonObject.getString("NumeroDocumento"));
                                //guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaDocumento"));
                                guardarDataMemoria("extra_fechaProgramacion", jsonObject.getString("FechaViajeItin"));
                                guardarDataMemoria("extra_RUC",jsonObject.getString("RUC"));
                                guardarDataMemoria("extra_RazonSocial",jsonObject.getString("RazonSocial"));
                                guardarDataMemoria("extra_rumbo", jsonObject.getString("RumboItinerario"));
                                guardarDataMemoria("extra_secuencia", jsonObject.getString("SecuenciaItin"));
                                guardarDataMemoria("extra_asiento", jsonObject.getString("Asiento"));
                                guardarDataMemoria("extra_servicio", jsonObject.getString("TipoServicioItin"));
                                guardarDataMemoria("extra_secuencia", jsonObject.getString("SecuenciaItin"));
                                guardarDataMemoria("extra_origen", jsonObject.getString("OrigenBoleto"));
                                guardarDataMemoria("extra_destino", jsonObject.getString("DestinoBoleto"));
                                guardarDataMemoria("extra_nombreCliente", jsonObject.getString("NombreCliente"));
                                guardarDataMemoria("extra_horaViajeItin", jsonObject.getString("horaViajeItin"));
                                dni = jsonObject.getString("CodigoCliente");

                                /* Arreglos el spinner de tipo de producto */
                                // lista_productos: arreglo que contiene la lista de productospara los asientos vendidos en agencia
                                // lista_idProductos: arreglo que contiene los IDs de los productos
                                // lista_nombreProductos: arreglo que contiene el nombre de los productos
                                final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
                                final ArrayList<String> lista_idProductos = new ArrayList<>();
                                final ArrayList<String> lista_nombreProductos = new ArrayList<>();
                                /* ----------------------------------------- */

                                /* Se itera en función a la lista de productos y se agrega data a los arreglos de IDs y nombre */
                                for (int i = 0; i < lista_productos.size(); i++) {
                                    String[] dataProductos = lista_productos.get(i).split("-");
                                    // dataProductos[0] = TI_PROD
                                    // dataProductos[1]= DE_TIPO_PROD

                                    lista_idProductos.add(dataProductos[0]);
                                    lista_nombreProductos.add(dataProductos[1]);

                                }
                                /* ----------------------------------------- */

                                /* Se agrega la lista de nombres de productos al spinner de tipo de producto */
                                ArrayAdapter<String> adapter_spinner = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, lista_nombreProductos);
                                spinner_tipoProducto.setAdapter(adapter_spinner);

                                /* TODO: ITEM CLICK LISTENER PARA EL SPINNER DE TIPO DE PRODUCTO */
                                spinner_tipoProducto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                        /* Se obtine el ID del tipo del producto y se guarda en memoria */
                                        String idProducto = lista_idProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                                        editor.putString("extra_idProducto", idProducto);
                                        editor.commit();
                                        String nombreProducto = lista_nombreProductos.get(spinner_tipoProducto.getSelectedItemPosition());
                                        guardarDataMemoria("guardar_nombreProducto", nombreProducto);
                                        /* ----------------------------------------- */

                                        if(sharedPreferences.getString("Modulo", "nada").equals("CONDUCTOR ESTANDAR")){

                                            /* Arreglo */
                                            // lista_tarifasCargas: arreglo que contiene todas las tarifas de carga
                                            final ArrayList<String> lista_tarifasCarga = getArray(sharedPreferences, gson, "anf_jsonTarifasCarga");
                                            /* ----------------------------------------- */
                                            editText_tarifaBase.setText(String.format("%.2f", Float.valueOf("2.00")));

                                            /* Iteración en función a la lista de tarifas de carga */
                                            /*for (int i = 0; i < lista_tarifasCarga.size(); i++) {
                                                String[] data_tarifaCarga = lista_tarifasCarga.get(i).split("-");
                                                // data_tarifaCarga[0] = PRODUCTO
                                                // data_tarifaCarga[1]= ORIGEN
                                                // data_tarifaCarga[2]= DESTINO
                                                // data_tarifaCarga[3]= IMPORTE

                                                try {
                                                    /* Se muestra el valor de la carga */
                                                 //   if (data_tarifaCarga[0].equals(idProducto) &&
                                                   //         data_tarifaCarga[1].equals(jsonObject.getString("OrigenBoleto")) &&
                                                   //         data_tarifaCarga[2].equals(jsonObject.getString("DestinoBoleto"))) {

                                                   //     editText_tarifaBase.setText(String.format("%.2f", Float.valueOf(data_tarifaCarga[3])));
                                                     //   break;
                                                  //  } else {
                                                   //     editText_tarifaBase.setText("0.00");
                                                   // }
                                                    /* ----------------------------------------- */
                                               // } catch (JSONException e) {
                                               //     e.printStackTrace();
                                              //  }
                                           // }
                                            /* ----------------------------------------- */
                                        } else if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){
                                            editText_tarifaBase.setText("2.00");
                                        }

                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                                /* ----------------------------------------- */
                                editText_tarifaCarga.setEnabled(true);
                                editText_cantidad.setEnabled(true);
                                button_imprimirBoletoCarga.setEnabled(true);
                                boletoEncontrado = true;
                                break;

                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Debe ingresar una serie y correlativo", Toast.LENGTH_SHORT).show();
                }
                /* ----------------------------------------- */

                button_imprimirBoletoCarga.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (editText_tarifaCarga.getText().toString().equals("")) {

                            Toast.makeText(getActivity(), "La tarifa adicional no puede estar vacía.", Toast.LENGTH_SHORT).show();

                        } else if (Float.valueOf(editText_tarifaCarga.getText().toString()) < Float.valueOf(editText_tarifaBase.getText().toString())) {

                            Toast.makeText(getActivity(), "La tarifa adicional no puede ser menor que la tarifa base.", Toast.LENGTH_SHORT).show();

                        }else if (editText_cantidad.getText().toString().equals("")) {

                            Toast.makeText(getActivity(), "El campo cantidad no puede estar vacío", Toast.LENGTH_SHORT).show();

                        }  else if (editText_cantidad.getText().toString().split("\\.").length > 1) {

                            Toast.makeText(getActivity(), "La cantidad debe ser un número.", Toast.LENGTH_SHORT).show();

                        } else if (Integer.valueOf(editText_cantidad.getText().toString()) == 0) {

                            Toast.makeText(getActivity(), "La cantidad no puede ser menor a 1.", Toast.LENGTH_SHORT).show();

                        } else if (Integer.valueOf(editText_cantidad.getText().toString()) > 20) {

                            Toast.makeText(getActivity(), "La cantidad no puede ser mayor a 20.", Toast.LENGTH_SHORT).show();

                        } else {

                            button_imprimirBoletoCarga.setEnabled(false);

                            /* Se obtiene la data del boleto seleccionado */
                            String boletoSeleccionado = sharedPreferences.getString("boletoSeleccionado", "NoData");
                            /* ----------------------------------------- */

                            try {
                                /* Se obtiene el JSON generado */
                                JSONObject jsonObject = new JSONObject(boletoSeleccionado);
                                /* ----------------------------------------- */

                                if(sharedPreferences.getString("Modulo", "nada").equals("CONDUCTOR ESTANDAR")){

                                    /* Obtiene serie y correlativo dependiendo del documento del cliente (DNI/RUC) */
                                    //if (jsonObject.getString("CodigoCliente").length() == 8 && jsonObject.getString("RUC").trim().length()==0) {
                                      if (jsonObject.getString("RUC").trim().length()==0) {
                                        guardarDataMemoria("TipoVenta_carga","BOLETA");
                                        numCorrelativoCargaSeleccionado = Integer.valueOf(sharedPreferences.getString("anf_correlativoBolCarga", "0"));

                                        numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado + 1;

                                        /* Se actualiza correlativo de Carga */
                                        editor.putString("anf_correlativoBolCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                        editor.commit();
                                        /* ----------------------------------------- */

                                        numSerieCargaSeleccionado = sharedPreferences.getString("anf_numSerieBolCarga", "NoData");

                                        editor.putString("extra_tipoDocumentoCarga", sharedPreferences.getString("anf_tipoDocumentoBolCarga", "NoData"));
                                        editor.commit();


                                    } else if (jsonObject.getString("RUC").trim().length() == 11) {
                                          guardarDataMemoria("TipoVenta_carga","FACTURA");
                                        numCorrelativoCargaSeleccionado = Integer.valueOf(sharedPreferences.getString("anf_correlativoFacCarga", "0"));

                                        numCorrelativoCargaSeleccionado = numCorrelativoCargaSeleccionado + 1;

                                        /* Se actualiza correlativo de Carga */
                                        editor.putString("anf_correlativoFacCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                        editor.commit();
                                        /* ----------------------------------------- */

                                        numSerieCargaSeleccionado = sharedPreferences.getString("anf_numSerieFacCarga", "NoData");

                                        editor.putString("extra_tipoDocumentoCarga", sharedPreferences.getString("anf_tipoDocumentoFacCarga", "NoData"));
                                        editor.commit();

                                    }
                                    /* ----------------------------------------- */
                                } else if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){

                                    /* Se obtiene la serie y correlativo dependiendo de la empresa seleccionada y se guarda en memoria */
                                    for (int i = 0; i < getCorrelativo.length(); i++) {

                                        try {

                                            JSONObject info = getCorrelativo.getJSONObject(i);

                                            if(sharedPreferences.getString("extra_documentoCliente","NoData").length() == 8 &&
                                                    sharedPreferences.getString("extra_empresa", "NoData").equals(info.getString("EMPRESA")) &&
                                                    info.getString("DE_GSER").equals("CARGA") &&
                                                    info.getString("TIPO_DOCUMENTO").equals("BOL") && sharedPreferences.getString("extra_RazonSocial","NoData").trim().length()!=11){

                                                serieSeleccionado = info.getString("NUMERO_SERIE");
                                                //carga_correlativoBol = info.getString("ULTIMO_CORRELATIVO");

                                                correlativoSeleccionado = Integer.toString(Integer.valueOf(sharedPreferences.getString("guardar_correlativoCargaBOL"+sharedPreferences.getString("guardar_idEmpresa", ""), "NoData")) + 1);
                                                guardarDataMemoria("extra_tipoDocumentoCarga", info.getString("TIPO_DOCUMENTO"));
                                                guardarDataMemoria("guardar_correlativoCargaBOL"+sharedPreferences.getString("guardar_idEmpresa", ""), correlativoSeleccionado);


                                            } else if (sharedPreferences.getString("extra_RazonSocial","NoData").length() == 11 &&
                                                    sharedPreferences.getString("guardar_idEmpresa", "NoData").equals(info.getString("EMPRESA")) &&
                                                    info.getString("DE_GSER").equals("CARGA") &&
                                                    info.getString("TIPO_DOCUMENTO").equals("FAC")) {

                                                serieSeleccionado = info.getString("NUMERO_SERIE");
                                                //carga_correlativoFac = info.getString("ULTIMO_CORRELATIVO");

                                                correlativoSeleccionado = Integer.toString(Integer.valueOf(sharedPreferences.getString("guardar_correlativoCargaFAC"+sharedPreferences.getString("guardar_idEmpresa", ""), "NoData")) + 1);
                                                guardarDataMemoria("extra_tipoDocumentoCarga", info.getString("TIPO_DOCUMENTO"));
                                                guardarDataMemoria("guardar_correlativoCargaFAC"+sharedPreferences.getString("guardar_idEmpresa", ""), correlativoSeleccionado);

                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                if(sharedPreferences.getString("Modulo", "nada").equals("CONDUCTOR ESTANDAR")){

                                    editor.putString("extra_correlativoCarga", Integer.toString(numCorrelativoCargaSeleccionado));
                                    editor.commit();

                                    editor.putString("extra_serieCarga", numSerieCargaSeleccionado);
                                    editor.commit();


                                    String numCorrelativoCargaCompleto = getNumCorrelativo(numCorrelativoCargaSeleccionado);
                                    editor.putString("extra_correlativoCargaCompleto", numCorrelativoCargaCompleto);
                                    editor.commit();

                                } else if(sharedPreferences.getString("Modulo", "nada").equals("ANDROID_VENTAS")){

                                    guardarDataMemoria("extra_correlativoCarga",correlativoSeleccionado);
                                    guardarDataMemoria("extra_serieCarga", serieSeleccionado);

                                    String numCorrelativoCargaCompleto = getNumCorrelativo(Integer.valueOf(correlativoSeleccionado));
                                    guardarDataMemoria("extra_correlativoCargaCompleto", numCorrelativoCargaCompleto);
                                }


                                /* Arreglo */
                                // lista_empresas: arreglo que contiene la data de ambas empresad
                                final ArrayList<String> lista_empresas = getArray(sharedPreferences, gson, "json_empresas");
                                /* ----------------------------------------- */

                                /* Se obtiene la empresa que genera la venta */
                                String empresaTramaCarga = "";
                                for (int i = 0; i < lista_empresas.size(); i++) {

                                    String[] data = lista_empresas.get(i).split("-");
                                    // data[0] = CODIGO_EMPRESA
                                    // data[1] = EMPRESA
                                    // data[2] = DIRECCION
                                    // data[3] = DEPARTAMENTO
                                    // data[4] = PROVINCIA
                                    // data[5] = RUC

                                    String codigo_empresa = sharedPreferences.getString("extra_empresa", "NoData");

                                    if (codigo_empresa.equals(data[0])) {
                                        empresaTramaCarga = lista_empresas.get(i);
                                        empresaSeleccionada = lista_empresas.get(i);
                                    }
                                }
                                /* ----------------------------------------- */

                                /* Se obtiene el monto total y se guarda en memoria */
                                //float tarifaTotal = Integer.valueOf(editText_cantidad.getText().toString()) * (Float.valueOf(editText_tarifaCarga.getText().toString()));
                                float tarifaTotal = Integer.valueOf(editText_tarifaCarga.getText().toString());
                                editor.putString("extra_tarifaTotal", Float.toString(tarifaTotal));
                                editor.commit();
                                /* ----------------------------------------- */

                                editor.putString("extra_cantidad", editText_cantidad.getText().toString());
                                editor.commit();

                                /* Se genera la trama del boleto de carga y se obtiene la data encriptada */
                                final String trama = generarTramaCarga(sharedPreferences, empresaTramaCarga);
                                //Log.d("trama", trama);
                                String[] dataEncriptada = generarCodigoQR(trama);
                                // dataEncriptada[0] = xml64
                                // dataEncriptada[1] = ted64
                                // dataEncriptada[2] = ted
                                /* ----------------------------------------- */
                                Boolean[] respuesta = guardarCompraCarga(dataEncriptada[0], dataEncriptada[1]);
                                String TedQR="";
                                if(!sharedPreferences.getString("anf_codigoEmpresa", "NoData").equals("NoData"))
                                {
                                    TedQR=  dataEncriptada[2]+"|"+sharedPreferences.getString("anf_codigoEmpresa", "NoData")+"|"
                                            +sharedPreferences.getString("anf_rumbo", "NoData")+"|"
                                            +sharedPreferences.getString("guardar_origen", "NoData")+"|"
                                            +sharedPreferences.getString("guardar_destino", "NoData")+"|"
                                            +sharedPreferences.getString("guardar_numeroDocumento", "NoData")+"|"
                                            +sharedPreferences.getString("guardar_nombreCliente", "NoData")+"|"
                                            +sharedPreferences.getString("anf_secuencia", "NoData")+"|"
                                            +sharedPreferences.getString("anf_fechaProgramacion", "NoData")+"|"
                                            +sharedPreferences.getString("guardar_numAsientoVendido","NoData")+"|"
                                            +"CARGA";
                                }else{
                                    TedQR =  dataEncriptada[2]+"|"+sharedPreferences.getString("extra_empresa", "NoData")+"|"
                                            +sharedPreferences.getString("extra_rumbo", "NoData")+"|"
                                            +sharedPreferences.getString("extra_origen", "NoData")+"|"
                                            +sharedPreferences.getString("extra_destino", "NoData")+"|"
                                            +sharedPreferences.getString("extra_serieCarga", "NoData") + "-" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData")+"|"
                                            +sharedPreferences.getString("Senior", "NoData")+"|"
                                            +sharedPreferences.getString("NU_SECU", "NoData")+"|"
                                            +sharedPreferences.getString("FE_VIAJ", "NoData")+"|0|"
                                            +"CARGA";
                                }

                                /* Arreglo */
                                // lista_productos: arreglo que contiene todos los tipos de productos
                                final ArrayList<String> lista_productos = getArray(sharedPreferences, gson, "json_productos");
                                /* ----------------------------------------- */

                                /* Se obtiene el tipo de producto */
                                String tipoProducto = "";
                                for (int i = 0; i < lista_productos.size(); i++) {
                                    String[] dataProductos = lista_productos.get(i).split("-");
                                    // dataProductos[0] = TI_PROD
                                    // dataProductos[1]= DE_TIPO_PROD

                                    if (dataProductos[0].equals(sharedPreferences.getString("extra_idProducto", "NoData"))) {
                                        tipoProducto = dataProductos[1];
                                        break;
                                    }
                                }/* ----------------------------------------- */

                                /* Impresión del boleto de carga y control */
                                imprimir_boletasCarga(jsonObject, sharedPreferences, TedQR, empresaSeleccionada, tipoProducto, "Carga");
                                //imprimir_boletasCarga(jsonObject, sharedPreferences, dataEncriptada[2], empresaSeleccionada, tipoProducto, "Carga");
                                /* ----------------------------------------- */

                                button_imprimirBoletoCarga.setEnabled(true);

                                /* Actualiza la vista de carga extra */
                                CargaExtraFragment cargaExtraFragment = new CargaExtraFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_base, cargaExtraFragment).commit();
                                /* ----------------------------------------- */

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        /* ----------------------------------------- */
                    }
                });
                /* ----------------------------------------- */
            }
        });
        /* ----------------------------------------- */
    }


    public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey) {

        String json = sharedPreferences.getString(jsonKey, "NoData");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        ArrayList<String> lista = new ArrayList<>();

        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }

        return lista;
    }


    public String getNumCorrelativo(int  numCorrelativoBLT){


        String sec_zeros = "";
        if (Integer.toString(numCorrelativoBLT).length() < 10) {
            int num_zeros = 10 - Integer.toString(numCorrelativoBLT).length();

            for (int i = 0; i < num_zeros; i++) {
                sec_zeros = sec_zeros + "0";
            }
        }
        String numCorrelativoCompleto = sec_zeros + Integer.toString(numCorrelativoBLT);

        return numCorrelativoCompleto;

    }


    public void guardarDataMemoria(String key, String value) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.commit();
    }


    public void imprimir_boletasCarga(JSONObject jsonObject, SharedPreferences sharedPreferences, String ted, String empresaSeleccionada, String tipoProducto, String tipoBoleta){

        Gson gson = new Gson();

        /* Arreglo */
        // lista_destinos: arreglo que contiene todos los desinos
        final ArrayList<String> lista_destinos = getArray(sharedPreferences, gson,"json_destinos"); //trama: CO_DEST - DE_DEST
        /* ----------------------------------------- */

        String origen = "";
        String destino  = "";

        /* Se pasan todos los valores necesarios para generar la estructura de la boleta que se va a imprimir */
        Boleta boleta = new Boleta(tipoBoleta);
        try {

            for(int i = 0; i < lista_destinos.size(); i++){

                String[] dataDestino = lista_destinos.get(i).split("-");

                if(dataDestino[0].equals(jsonObject.getString("OrigenBoleto"))){
                    origen = dataDestino[0]+"-"+dataDestino[1];
                }
            }

            for(int i = 0; i < lista_destinos.size(); i++){

                String[] dataDestino = lista_destinos.get(i).split("-");

                if(dataDestino[0].equals(jsonObject.getString("DestinoBoleto"))){
                    destino = dataDestino[0]+"-"+dataDestino[1];
                }
            }

            boleta.setOrigen(origen);
            boleta.setDestino(destino);
            boleta.setDNI(jsonObject.getString("CodigoCliente"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boleta.setTarifa(sharedPreferences.getString("extra_tarifaTotal", "NoData"));
        boleta.setSerieCarga(sharedPreferences.getString("extra_serieCarga",  "NoData"));
        boleta.setCorrelativoCarga(sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData"));
        boleta.setEmpresa(empresaSeleccionada);
        boleta.setFechaVenta(sharedPreferences.getString("extra_fechaVentaCarga", "NoData"));
        boleta.setHoraVenta(sharedPreferences.getString("extra_horaVentaCarga", "NoData"));
        boleta.setTipoProducto(tipoProducto);
        boleta.setCantidad(sharedPreferences.getString("extra_cantidad", "NoData"));
        boleta.setNombreAnfitrion(sharedPreferences.getString("nombreEmpleado", "NoData"));
        boleta.setNumAsiento(sharedPreferences.getString("extra_asiento", "NoData"));
        String [] dataBoletoViaje = sharedPreferences.getString("extra_numDocumentoBoleto", "NoData").split("-");
        boleta.setSeriePasaje(dataBoletoViaje[0]);
        boleta.setCorrelativoPasaje(dataBoletoViaje[1]);
        boleta.setNombreCliente(sharedPreferences.getString("extra_nombreCliente", ""));
        boleta.setEmpesa_imp(sharedPreferences.getString("extra_empresa", "NoData"));
        boleta.SetPrueba(getString(R.string.ws_ticket));
        boleta.SetRUC(sharedPreferences.getString("extra_RUC","NoData"));
        boleta.SetRazonSocial(sharedPreferences.getString("extra_RazonSocial","NoData"));
        boleta.SetDocuElectronico(sharedPreferences.getString("TipoVenta_carga","NoData"));
        /* ----------------------------------------- */

        try {

            /* Se inicializa la impresora del equipo */
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getContext());
            IPrinter printer = dal.getPrinter();
            printer.init();
            /* ----------------------------------------- */

            /* TEXTO */
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            printer.printStr(boleta.getVoucher(), null);
            /* ----------------------------------------- */

            /* QR */
            printer.printBitmap(boleta.getQRBitmap(ted));
            /* ----------------------------------------- */

            /* Margen final */
            printer.printStr(boleta.margenFinal(), null);
            /* ----------------------------------------- */

            int iRetError = printer.start();

            if (iRetError != 0x00) {
                if (iRetError == 0x02) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String[] generarCodigoQR(String trama) {

        /* URI del proveedor del servicio */
        Uri CONTENT_URI = Uri.parse("content://org.pe.dgf.provider").buildUpon().appendPath(PATH_XML).build();
        /* ----------------------------------------- */

        /* Se generar el query para obtener la data encriptada */
        Cursor results = getActivity().getContentResolver().query(CONTENT_URI, null, trama, null, SOLICITA_TED);
        /* ----------------------------------------- */

        String xml64 = null;
        String ted = null;
        String ted64 = null;

        /* Se obtiene la data encriptada */
        if (results != null) {
            if (results.moveToNext()) {

                /* Se obtiene el valor de cada columna */
                xml64 = results.getString(results.getColumnIndex(COLUMN_NAME_XML64));
                ted = results.getString(results.getColumnIndex(COLUMN_NAME_TED));
                ted64 = results.getString(results.getColumnIndex(COLUMN_NAME_TED64));
                /* ----------------------------------------- */
            }
        }
        /* ----------------------------------------- */

        String[] valores = {xml64, ted64, ted};

        return valores;
    }


    public Boolean[] guardarCompraCarga(String xml64, String ted64) {

        final RequestQueue queue = Volley.newRequestQueue(getContext());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final Boolean[] respuesta = new Boolean[1];
        respuesta[0] = true;

        /* Fecha, hora y día de la venta del boleto */
        Date date = new Date();
        final String fechaVenta = new SimpleDateFormat("yyyy-MM-dd").format(date);
        editor.putString("extra_fechaVentaCarga", fechaVenta);
        editor.commit();

        final String horaVenta = new SimpleDateFormat("hh:mm").format(date);
        editor.putString("extra_horaVentaCarga", horaVenta);
        editor.commit();

        String strDiaFormat = "dd";
        DateFormat diaFormat = new SimpleDateFormat(strDiaFormat);
        String diaSemana = diaFormat.format(date);
        /* ----------------------------------------- */

        /* Se obtiene el JSON generado */
        final JSONObject jsonObject = generarJSONCarga(sharedPreferences, fechaVenta, horaVenta, diaSemana, xml64, ted64);

        ContentValues cv = new ContentValues();
        cv.put("data_boleto", jsonObject.toString());
        cv.put("estado", "pendiente");
        cv.put("tipo", "carga");
        cv.put("liberado", "No");
        cv.put("nu_docu",sharedPreferences.getString("extra_serieCarga", "NoData") + "-" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData"));
        cv.put("ti_docu","BLT");
        //cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
        cv.put("Log_data",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(date));

        if(sharedPreferences.getString("puestoUsuario", "nada").equals("BOLETERO")){
            cv.put("puesto", "boletero");
            sharedPreferences.getString("extra_empresa", "NoData");
        }else{
            cv.put("puesto", "anfitrion");
            //cv.put("co_empr",sharedPreferences.getString("anf_codigoEmpresa", "NoData"));
            sharedPreferences.getString("extra_empresa", "NoData");
        }

        Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);
        //Log.d("json object", jsonObject.toString());
        /* ----------------------------------------- */

        /* Ruta de la Web service */
        String ws_postVenta = getString(R.string.ws_ruta) + "SetBoletoCarga";
        //Log.d("url",ws_postVenta);
        /* ----------------------------------------- */

        /* Request que envía el boleto de carga vendido */
        MyJSONArrayRequest jsonArrayRequestVenta = new MyJSONArrayRequest(Request.Method.POST, ws_postVenta, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {

                            JSONObject info;
                            try {

                                info = response.getJSONObject(0);
                                //Log.d("respuesta",info.toString());

                                /* Se obtiene la respuesta del servidor y en caso de ser "guardado" se guarda el boleto en la BD */
                                if (info.getString("Respuesta").equals("GUARDADO")) {

                                  /*  ContentValues cv = new ContentValues();
                                    cv.put("data_boleto", jsonObject.toString());
                                    cv.put("estado", "guardado");
                                    cv.put("tipo", "carga");
                                    cv.put("liberado", "No");
                                    if(sharedPreferences.getString("puestoUsuario", "NoData").equals("BOLETERO")){
                                        cv.put("puesto", "boletero");
                                    }else {
                                        cv.put("puesto", "anfitrion");
                                    }

                                    Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);*/
                                    /* ----------------------------------------- */

                                    respuesta[0] = true;

                                } else {
                                    Toast.makeText(getActivity(), "El correlativo utilizado ya existe. Por favor, actualizar correlativo.", Toast.LENGTH_SHORT).show();
                                    button_imprimirBoletoCarga.setEnabled(true);
                                }
                                /* ----------------------------------------- */

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                Toast.makeText(getActivity(), "Error en la ws SetBoletoCarga. No se pudo guardar el boleto de carga en la ws.", Toast.LENGTH_LONG).show();

                Toast.makeText(getActivity(), "Se activa modo Offline.", Toast.LENGTH_LONG).show();

                /* Se guarda el boleto de carga con estado "pendiente" en la BD */
               /* ContentValues cv = new ContentValues();
                cv.put("data_boleto", jsonObject.toString());
                cv.put("estado", "pendiente");
                cv.put("tipo", "carga");
                cv.put("liberado", "No");
                if(sharedPreferences.getString("puestoUsuario", "NoData").equals("BOLETERO")){
                    cv.put("puesto", "boletero");
                }else {
                    cv.put("puesto", "anfitrion");
                }

                Long id = sqLiteDatabase.insert("VentaBoletos", null, cv);*/
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
        };
        /* ----------------------------------------- */
        jsonArrayRequestVenta.setRetryPolicy(new DefaultRetryPolicy(timeout, numeroIntentos, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequestVenta);

        return respuesta;
    }


    public JSONObject generarJSONCarga(SharedPreferences sharedPreferences, String fechaVenta, String horaVenta, String diaSemana, String xml64, String ted64) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("CodigoEmpresa", sharedPreferences.getString("extra_empresa", "NoData"));
            if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")){
                jsonObject.put("Unidad", sharedPreferences.getString("guardar_unidad", "NoData"));
                jsonObject.put("Agencia", sharedPreferences.getString("guardar_agencia", "NoData"));

            }else if (sharedPreferences.getString("Modulo", "NoData").equals("CONDUCTOR ESTANDAR")){
                jsonObject.put("Unidad", "");
                jsonObject.put("Agencia", "");
            }

            jsonObject.put("TipoDocuCarga", sharedPreferences.getString("extra_tipoDocumentoCarga", "NoData"));
            jsonObject.put("SerieCorrelativo", sharedPreferences.getString("extra_serieCarga", "NoData") + "-" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData"));
            jsonObject.put("FechaDocumento", fechaVenta);
            jsonObject.put("Rumbo", sharedPreferences.getString("extra_rumbo", "NoData"));
            jsonObject.put("Origen", sharedPreferences.getString("extra_origen", "NoData"));
            jsonObject.put("Destino", sharedPreferences.getString("extra_destino", "NoData"));
            jsonObject.put("NuSecu", sharedPreferences.getString("extra_secuencia", "NoData"));
            jsonObject.put("NumeroDia", diaSemana);
            jsonObject.put("DocumentoIdentidad", sharedPreferences.getString("extra_documentoCliente", "NoData"));
            jsonObject.put("RUC", sharedPreferences.getString("extra_RUC", "NoData"));
            jsonObject.put("NombreCliente", sharedPreferences.getString("extra_nombreCliente", "NoData"));
            jsonObject.put("TipoServicio", sharedPreferences.getString("extra_servicio", "NoData"));
            jsonObject.put("NumeroAsiento", sharedPreferences.getString("extra_asiento", "NoData"));
            jsonObject.put("FechaViajeItinerario", sharedPreferences.getString("extra_fechaProgramacion", "NoData"));
            jsonObject.put("HoraViaje", sharedPreferences.getString("extra_horaViajeItin", "NoData"));
            jsonObject.put("ImporteTotal", sharedPreferences.getString("extra_tarifaTotal", "NoData"));
            jsonObject.put("Observacion", "");
            jsonObject.put("CodigoUsuario", sharedPreferences.getString("codigoUsuario", "NoData"));
            jsonObject.put("NuDocuBoletoViaje", sharedPreferences.getString("extra_numDocumentoBoleto", "NoData"));
            jsonObject.put("TipoDocumentoBoletoViaje", sharedPreferences.getString("extra_tipoDocumentoBoleto", "NoData"));
            jsonObject.put("XML64", xml64);
            jsonObject.put("TED64", ted64);
            jsonObject.put("Correlativo", sharedPreferences.getString("extra_correlativoCarga", "NoData"));
            jsonObject.put("Producto", sharedPreferences.getString("extra_idProducto", "NoData"));
            jsonObject.put("Cantidad", sharedPreferences.getString("extra_cantidad", "NoData"));
            if(sharedPreferences.getString("Modulo", "NoData").equals("ANDROID_VENTAS")){
                jsonObject.put("TipoVenta", "Boletero");
            }else if (sharedPreferences.getString("Modulo", "NoData").equals("CONDUCTOR ESTANDAR")){
                jsonObject.put("TipoVenta", "Anfitrion");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    public String generarTramaCarga(SharedPreferences sharedPreferences, String empresaTrama){

        String[] empresaSeleccionada = empresaTrama.split("-");
        // data[0] = CODIGO_EMPRESA
        // data[1] = EMPRESA
        // data[2] = DIRECCION
        // data[3] = DEPARTAMENTO
        // data[4] = PROVINCIA
        // data[5] = RUC
        // data[6] = RAZON_SOCIAL

        /*String tipoDocumento = "";
        if (sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 11) {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }*/
        String tipoDocumento = "";
        if(sharedPreferences.getString("TipoVenta_carga","NoData").equals("FACTURA")) {
            tipoDocumento = "01";
        }else if (sharedPreferences.getString("TipoVenta_carga", "NoData").equals("BOLETA")) {
            tipoDocumento = "03";
        }

        String normativaSunat = "";
        if(sharedPreferences.getString("TipoVenta_carga","NoData").equals("FACTURA")) {
            normativaSunat = "A;FormaPago;;Contado\n";
        }else if (sharedPreferences.getString("TipoVenta_carga", "NoData").equals("BOLETA")) {
            normativaSunat = "";
        }

        /*String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 11){
            tipoDocumentoCliente = "6";
        }else if(sharedPreferences.getString("extra_documentoCliente", "NoData").length() == 8){
            tipoDocumentoCliente = "1";
        }else{
            tipoDocumentoCliente = "7";
        }*/
        String tipoDocumentoCliente = "";
        String DocuDeclara  = "";
        if(sharedPreferences.getString("TipoVenta_carga","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
            DocuDeclara= sharedPreferences.getString("extra_RUC","NoData");
        } else if (sharedPreferences.getString("TipoVenta_carga", "NoData").equals("BOLETA")) {
            tipoDocumentoCliente = "1";
            DocuDeclara= sharedPreferences.getString("extra_documentoCliente","NoData");
        } else {
            tipoDocumentoCliente = "7";
            DocuDeclara= sharedPreferences.getString("extra_documentoCliente","NoData");
        }



        String documentoCliente = "";
        if(sharedPreferences.getString("extra_documentoCliente", "NoData").equals("")){
            documentoCliente = "-";
        }else{
            documentoCliente = sharedPreferences.getString("extra_documentoCliente", "NoData");
        }

        String nombreCliente = "";
        if(sharedPreferences.getString("extra_nombreCliente", "NoData").equals("")){
            nombreCliente = "-";
        }else{
            nombreCliente = sharedPreferences.getString("extra_nombreCliente", "NoData");
        }

        String direccionCliente = "";
        if(sharedPreferences.getString("extra_direccionCliente", "NoData").equals("NoData")){
            direccionCliente = "-";
        }else{
            direccionCliente = sharedPreferences.getString("extra_direccionCliente", "NoData");
        }

        String codEmpresaTrama = "";
        if(empresaSeleccionada[0].equals("01")){
            codEmpresaTrama = "0180050002083";

        }else if(empresaSeleccionada[0].equals("02")){
            codEmpresaTrama = "0320050000128";

        }else {
            codEmpresaTrama = "-";

        }

        /* Fecha y hora del cuando se genera la trama del boleto de carga */
        Date date = new Date();

        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";

        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);

        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        /* ----------------------------------------- */

        /* Se obtiene el monto neto y el monto de IGV */
        double montoTotal = Float.valueOf(sharedPreferences.getString("extra_tarifaTotal", "NoData"));
        double montoSinIGV = (montoTotal * 100) / 118;
        double montoSinIGVRedondeado = Math.rint(montoSinIGV * 100) / 100;

        double montoIGV = montoTotal - montoSinIGVRedondeado;
        /* ----------------------------------------- */

        /* Convertir monto a letras */
        String numeroFloat = String.format("%.2f",montoTotal);
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        /* ----------------------------------------- */
        String tramaCarga =
                "A;Serie;;" + sharedPreferences.getString("extra_serieCarga", "NoData") + "\n" +
                        "A;Correlativo;;" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData").substring(2) + "\n" +
                        "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                        "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                        "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                        "A;ComuEmis;;150115\n" +
                        "A;CodigoLocalAnexo;;0000\n" +
                        "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                        "A;TipoOperacion;;0101\n" +
                        "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                        "A;RUTRecep;;" + DocuDeclara + "\n" +
                        "A;RznSocRecep;;" + nombreCliente + "\n" +
                        "A;DirRecep;;-\n" +
                        "A;TipoMoneda;;PEN\n" +
                        "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                        "A;MntExe;;0.00\n" +
                        "A;MntExo;;0.00\n" +
                        "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                        "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoRucEmis;;6\n"+
                        "A;UrbanizaEmis;;LIMA\n" +
                        "A;ProviEmis;;LIMA\n" +
                //"A;RUTRecep;;" + documentoCliente + "\n" +
//                "A;DirRecepUrbaniza;;NoData"+"\n"+
//                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+ normativaSunat +
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;"+String.format("%.2f", montoIGV)+"\n" +
                "A2;TasaImpuesto;1;18\n"+
                "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoSinIGV)+"\n"+
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                //"B;CodigoProductoSunat;1;78111802\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;" + montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;IndExe;1;10\n" +
                //"B;CodigoTipoIgv;1;9997\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;" + String.format("%.2f", montoIGV) + "\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;"+sharedPreferences.getString("anf_nombre", "NoData")+"\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                //"E;DescripcionAdicSunat;4;WWW.PERUBUS.COM.PE\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n"+
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n"+
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;"+sharedPreferences.getString("guardar_nombreCliente", "NoData")+"\n" +
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n"+
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;5\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena +"\n"+
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;-\n"+
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;\"SOAT: COMPAÑIA DE SEGUROS LA POSITIVA.\" VENTA NORMAL\n"+
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.PERUBUS.COM.PE\n"+
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("extra_origen","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("extra_destino","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n"+
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n"+
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n"+
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+sharedPreferences.getString("extra_serieCarga", "NoData")+"/F-VTS-42\n"+
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19;-\n"+
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;-\n"+
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;-\n"+
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n"+
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;"+empresaSeleccionada[1]+"\n"+
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;0320050000133\n"+
                "E;TipoAdicSunat;25;01\n"+
                "E;NmrLineasDetalle;25;25\n"+
                "E;NmrLineasAdicSunat;25;25\n"+
                "E;DescripcionAdicSunat;25;S/\n"+
                "E;TipoAdicSunat;26;01\n"+
                "E;NmrLineasDetalle;26;26\n"+
                "E;NmrLineasAdicSunat;26;26\n"+
                "E;DescripcionAdicSunat;26;-\n"+
                "E;TipoAdicSunat;27;01\n"+
                "E;NmrLineasDetalle;27;27\n"+
                "E;NmrLineasAdicSunat;27;27\n"+
                "E;DescripcionAdicSunat;27;-\n"+
                "E;TipoAdicSunat;28;01\n"+
                "E;NmrLineasDetalle;28;28\n"+
                "E;NmrLineasAdicSunat;28;28\n"+
                "E;DescripcionAdicSunat;28;FE. VIAJE: "+fechaVenta+" HORA VIAJE:"+ horaVenta+"\n"+
                "E;TipoAdicSunat;29;01\n"+
                "E;NmrLineasDetalle;29;29\n"+
                "E;NmrLineasAdicSunat;29;29\n"+
                "E;DescripcionAdicSunat;29;ASIENTO:     0         CLASE: ESTANDAR PEGASO\n"+
                "E;TipoAdicSunat;30;01\n"+
                "E;NmrLineasDetalle;30;30\n"+
                "E;NmrLineasAdicSunat;30;30\n"+
                "E;DescripcionAdicSunat;30;-\n"+
                "E;TipoAdicSunat;31;01\n"+
                "E;NmrLineasDetalle;31;31\n"+
                "E;NmrLineasAdicSunat;31;31\n"+
                "E;DescripcionAdicSunat;31;http://www.perubus.com.pe Telf: 2052370\n"+
                "E;TipoAdicSunat;32;01\n"+
                "E;NmrLineasDetalle;32;32\n"+
                "E;NmrLineasAdicSunat;32;32\n"+
                "E;DescripcionAdicSunat;32;NRO\n"+
                "E;TipoAdicSunat;33;02\n"+
                "E;NmrLineasDetalle;33;1\n"+
                "E;NmrLineasAdicSunat;33;11\n"+
                "E;DescripcionAdicSunat;33;0\n"+
                "E;TipoAdicSunat;34;02\n"+
                "E;NmrLineasDetalle;34;1\n"+
                "E;NmrLineasAdicSunat;34;12\n"+
                "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("extra_origen","NoData").toString().trim()+" - "+ sharedPreferences.getString("extra_destino","NoData").toString().trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";
       /* String tramaCarga ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + sharedPreferences.getString("extra_serieCarga", "NoData") + "\n" +
                "A;Correlativo;;" + sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData").substring(2) + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;TipoRucEmis;;6\n"+
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;TipoRutReceptor;;"+tipoDocumento+"\n" +
                "A;RUTRecep;;" + documentoCliente + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;" + direccionCliente + "\n" +
                "A;DirRecepUrbaniza;;NoData"+"\n"+
                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;" + String.format("%.2f", montoIGV) + "\n" +
                "A;MntTotal;;" + String.format("%.2f", montoTotal) + "\n" +
                "A;TipoOperacion;;0101\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                //"B;CodigoProductoSunat;1;78111802\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;" + montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", montoTotal)+"\n" +
                "B;IndExe;1;20\n" +
                //"B;CodigoTipoIgv;1;9997\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;" + String.format("%.2f", montoIGV) + "\n" +
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;ORIGEN\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;DESTINO\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;ASIENTO:     0         CLASE: ESTANDAR RUTERO\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;WWW.PERUBUS.COM.PE\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;0180050002160\n";
       /* String tramaCarga = "A;Serie;;"+sharedPreferences.getString("extra_serieCarga", "NoData")+"\n" +
                "A;Correlativo;;"+sharedPreferences.getString("extra_correlativoCargaCompleto", "NoData").substring(2)+"\n" +
                "A;RznSocEmis;;"+empresaSeleccionada[8]+"\n" +
                "A;CODI_EMPR;;"+empresaSeleccionada[0].substring(1)+"\n" +
                "A;RUTEmis;;"+empresaSeleccionada[7]+"\n" +
                "A;DirEmis;;"+empresaSeleccionada[2] + " - " + empresaSeleccionada[3] + " - " + empresaSeleccionada[4]+"\n" +
                "A;ComuEmis;;150115\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;NomComer;;"+empresaSeleccionada[1]+"\n" +
                "A;TipoDTE;;"+tipoDocumento+"\n" +
                "A;TipoOperacion;;0101\n" +
                "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                "A;RUTRecep;;"+documentoCliente+"\n" +
                "A;RznSocRecep;;"+nombreCliente+"\n" +
                "A;DirRecep;;"+direccionCliente+"\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;MntNeto;;"+montoSinIGVRedondeado+"\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;0.00\n" +
                "A;MntTotalIgv;;"+String.format("%.2f", montoIGV)+"\n" +
                "A;MntTotal;;"+String.format("%.2f", montoTotal)+"\n" +
                "A;FchEmis;;"+fechaVenta+"\n" +
                "A;HoraEmision;;"+horaVenta+"\n" +
                "A2;CodigoImpuesto;1;1000\n" +
                "A2;MontoImpuesto;1;"+ String.format("%.2f", montoIGV)+"\n" +
                "A2;TasaImpuesto;1;18\n" +
                "A2;MontoImpuestoBase;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;"+sharedPreferences.getString("extra_cantidad", "NoData")+"\n" +
                "B;UnmdItem;1;MTQ\n" +
                "B;VlrCodigo;1;"+sharedPreferences.getString("extra_idProducto", "NoData")+"\n" +
                "B;NmbItem;1;"+sharedPreferences.getString("guardar_nombreProducto", "NoData")+"\n" +
                "B;CodigoProductoSunat;1;78101801\n" +
                "B;PrcItem;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;PrcItemSinIgv;1;"+montoSinIGVRedondeado+"\n" +
                "B;MontoItem;1;"+String.format("%.2f", montoTotal)+"\n" +
                "B;IndExe;1;1010\n" +
                "B;CodigoTipoIgv;1;1000\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;"+String.format("%.2f", montoIGV)+"\n"+
                "E;TipoAdicSunat;1;01\n" +
                "E;NmrLineasDetalle;1;1\n" +
                "E;NmrLineasAdicSunat;1;1\n" +
                "E;DescripcionAdicSunat;1;-\n" +
                "E;TipoAdicSunat;2;01\n" +
                "E;NmrLineasDetalle;2;2\n" +
                "E;NmrLineasAdicSunat;2;2\n" +
                "E;DescripcionAdicSunat;2;-\n" +
                "E;TipoAdicSunat;3;01\n" +
                "E;NmrLineasDetalle;3;3\n" +
                "E;NmrLineasAdicSunat;3;3\n" +
                "E;DescripcionAdicSunat;3;-\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
                "E;DescripcionAdicSunat;4;-\n" +
                "E;TipoAdicSunat;5;01\n" +
                "E;NmrLineasDetalle;5;5\n" +
                "E;NmrLineasAdicSunat;5;5\n" +
                "E;DescripcionAdicSunat;5;-\n" +
                "E;TipoAdicSunat;6;01\n" +
                "E;NmrLineasDetalle;6;6\n" +
                "E;NmrLineasAdicSunat;6;6\n" +
                "E;DescripcionAdicSunat;6;-\n" +
                "E;TipoAdicSunat;7;01\n" +
                "E;NmrLineasDetalle;7;7\n" +
                "E;NmrLineasAdicSunat;7;7\n" +
                "E;DescripcionAdicSunat;7;-\n" +
                "E;TipoAdicSunat;8;01\n" +
                "E;NmrLineasDetalle;8;8\n" +
                "E;NmrLineasAdicSunat;8;8\n" +
                "E;DescripcionAdicSunat;8;-\n" +
                "E;TipoAdicSunat;9;01\n" +
                "E;NmrLineasDetalle;9;9\n" +
                "E;NmrLineasAdicSunat;9;9\n" +
                "E;DescripcionAdicSunat;9;"+precioCadena+"\n" +
                "E;TipoAdicSunat;10;01\n" +
                "E;NmrLineasDetalle;10;10\n" +
                "E;NmrLineasAdicSunat;10;10\n" +
                "E;DescripcionAdicSunat;10;F-VTS-43\n" +
                "E;TipoAdicSunat;11;01\n" +
                "E;NmrLineasDetalle;11;11\n" +
                "E;NmrLineasAdicSunat;11;11\n" +
                "E;DescripcionAdicSunat;11;-\n" +
                "E;TipoAdicSunat;12;01\n" +
                "E;NmrLineasDetalle;12;12\n" +
                "E;NmrLineasAdicSunat;12;12\n" +
                "E;DescripcionAdicSunat;12;WWW.SOYUZONLINE.COM.PE\n" +
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;LIMA\n" +
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;ICA\n" +
                "E;TipoAdicSunat;15;01\n" +
                "E;NmrLineasDetalle;15;15\n" +
                "E;NmrLineasAdicSunat;15;15\n" +
                "E;DescripcionAdicSunat;15;-\n" +
                "E;TipoAdicSunat;16;01\n" +
                "E;NmrLineasDetalle;16;16\n" +
                "E;NmrLineasAdicSunat;16;16\n" +
                "E;DescripcionAdicSunat;16;-\n" +
                "E;TipoAdicSunat;17;01\n" +
                "E;NmrLineasDetalle;17;17\n" +
                "E;NmrLineasAdicSunat;17;17\n" +
                "E;DescripcionAdicSunat;17;"+horaVenta+"\n" +
                "E;TipoAdicSunat;18;01\n" +
                "E;NmrLineasDetalle;18;18\n" +
                "E;NmrLineasAdicSunat;18;18\n" +
                "E;DescripcionAdicSunat;18;"+sharedPreferences.getString("extra_serieCarga", "NoData")+"/F-VTS-43\n" +
                "E;TipoAdicSunat;19;01\n" +
                "E;NmrLineasDetalle;19;19\n" +
                "E;NmrLineasAdicSunat;19;19\n" +
                "E;DescripcionAdicSunat;19;Sucursal EN RUTA\n" +
                "E;TipoAdicSunat;20;01\n" +
                "E;NmrLineasDetalle;20;20\n" +
                "E;NmrLineasAdicSunat;20;20\n" +
                "E;DescripcionAdicSunat;20;EN RUTA\n" +
                "E;TipoAdicSunat;21;01\n" +
                "E;NmrLineasDetalle;21;21\n" +
                "E;NmrLineasAdicSunat;21;21\n" +
                "E;DescripcionAdicSunat;21;LA VICTORIA LIMA\n" +
                "E;TipoAdicSunat;22;01\n" +
                "E;NmrLineasDetalle;22;22\n" +
                "E;NmrLineasAdicSunat;22;22\n" +
                "E;DescripcionAdicSunat;22;-\n" +
                "E;TipoAdicSunat;23;01\n" +
                "E;NmrLineasDetalle;23;23\n" +
                "E;NmrLineasAdicSunat;23;23\n" +
                "E;DescripcionAdicSunat;23;SOYUZ S.A\n" +
                "E;TipoAdicSunat;24;01\n" +
                "E;NmrLineasDetalle;24;24\n" +
                "E;NmrLineasAdicSunat;24;24\n" +
                "E;DescripcionAdicSunat;24;"+codEmpresaTrama+"\n" +
                "E;TipoAdicSunat;25;01\n" +
                "E;NmrLineasDetalle;25;25\n" +
                "E;NmrLineasAdicSunat;25;25\n" +
                "E;DescripcionAdicSunat;25;S/\n" +
                "E;TipoAdicSunat;26;01\n" +
                "E;NmrLineasDetalle;26;26\n" +
                "E;NmrLineasAdicSunat;26;26\n" +
                "E;DescripcionAdicSunat;26;-\n" +
                "E;TipoAdicSunat;27;01\n" +
                "E;NmrLineasDetalle;27;27\n" +
                "E;NmrLineasAdicSunat;27;27\n" +
                "E;DescripcionAdicSunat;27;-\n" +
                "E;TipoAdicSunat;28;01\n" +
                "E;NmrLineasDetalle;28;28\n" +
                "E;NmrLineasAdicSunat;28;28\n" +
                "E;DescripcionAdicSunat;28;-\n" +
                "E;TipoAdicSunat;29;01\n" +
                "E;NmrLineasDetalle;29;29\n" +
                "E;NmrLineasAdicSunat;29;29\n" +
                "E;DescripcionAdicSunat;29;-\n" +
                "E;TipoAdicSunat;30;01\n" +
                "E;NmrLineasDetalle;30;30\n" +
                "E;NmrLineasAdicSunat;30;30\n" +
                "E;DescripcionAdicSunat;30;-\n" +
                "E;TipoAdicSunat;31;01\n" +
                "E;NmrLineasDetalle;31;31\n" +
                "E;NmrLineasAdicSunat;31;31\n" +
                "E;DescripcionAdicSunat;31;http://www.soyuzonline.com.pe \n" +
                "E;TipoAdicSunat;32;01\n" +
                "E;NmrLineasDetalle;32;32\n" +
                "E;NmrLineasAdicSunat;32;32\n" +
                "E;DescripcionAdicSunat;32;PESO\n" +
                "E;TipoAdicSunat;33;02\n" +
                "E;NmrLineasDetalle;33;1\n" +
                "E;NmrLineasAdicSunat;33;11\n" +
                "E;DescripcionAdicSunat;33;0.00\n" +
                "E;TipoAdicSunat;34;02\n" +
                "E;NmrLineasDetalle;34;1\n" +
                "E;NmrLineasAdicSunat;34;12\n" +
                "E;DescripcionAdicSunat;34;-\n" +
                "E;TipoAdicSunat;35;02\n" +
                "E;NmrLineasDetalle;35;1\n" +
                "E;NmrLineasAdicSunat;35;13\n" +
                "E;DescripcionAdicSunat;35;-\n";*/

        return tramaCarga;
    }
}