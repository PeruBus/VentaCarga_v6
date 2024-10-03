package pe.com.telefonica.soyuz;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.dal.IPrinter;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class FuncionesAuxiliares extends Application {

    /**
     * Constante para la interaccion con el APK de DigiFlow.
     */
    private static final String SOLICITA_TED = "ted";
    private static final String PATH_XML = "xml";
    private static final String COLUMN_NAME_XML64 = "xml64";
    private static final String COLUMN_NAME_TED = "ted";
    private static final String COLUMN_NAME_TED64 = "ted64";

    /**
     * Valor del timeout.
     */
    public static final int timeout = 30 * 1000;
    /**
     * Valor del numero de intentos.
     */
    public static final int numeroIntentos = 3;

    /**
	* Genera un arreglo partir de un JSON.
	* @param sharedPreferences
	* @param gson
	* @param jsonKey Identificador con el cual se va a buscar un valor en memoria.
	* @return Un arreglo de cadena de caracteres.
	*/
    static public ArrayList<String> getArray(SharedPreferences sharedPreferences, Gson gson, String jsonKey){

        String json = sharedPreferences.getString(jsonKey, "NoData");
        Type type = new TypeToken<ArrayList<String>>() {}.getType();

        ArrayList<String> lista = new ArrayList<>();

        if (!json.equals("NoData")) {
            lista = gson.fromJson(json, type);
        }

        return lista;
    }

	/**
	* Guarda un valor en memoria con un identificador asignado.
	* @param key Identificador.
	* @param value Valor que se va a guardar en memoria.
	* @param context Contiene el contexto donde se ejecuta esta función.
	*/
    static public void guardarDataMemoria(String key, String value, Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Guarda un valor numérico en memoria con un identificador asignado.
     * @param key Identificador.
     * @param value Valor numérico que se va a guardar en memoria.
     */
    static public void guardarIntegerMemoria(String key, int value, Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(key, value);
        editor.commit();
    }

	/**
	* Completa con ceros el número de correlativo.
	* @param correlativoSeleccionado Número de correlativo.
	* @return El número de correlativo completo.
	*/
    static public String completarCorrelativo(int  correlativoSeleccionado){

        String sec_zeros = "";
        if (Integer.toString(correlativoSeleccionado).length() < 10) {
            int num_zeros = 10 - Integer.toString(correlativoSeleccionado).length();

            for (int i = 0; i < num_zeros; i++) {
                sec_zeros = sec_zeros + "0";
            }
        }
        String numCorrelativoCompleto = sec_zeros + correlativoSeleccionado;

        return numCorrelativoCompleto;

    }

	/**
	* Tiempo de espera para realizar otro request en ms.
	*/
    static public void breakTime(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encripta la trama de venta para generar el código QR.
     * @param trama Trama que contiene la información de la venta.
     * @return Data encriptada generada a partir de la trama de venta.
     */
    static public String[] generarCodigoQR(String trama, Fragment application) {

        /* URI del proveedor del servicio */

        Uri CONTENT_URI = Uri.parse("content://org.pe.dgf.provider").buildUpon().appendPath(PATH_XML).build();
        /* ----------------------------------------- */

        /* Se generar el query para obtener la data encriptada */
        Cursor results = application.getActivity().getContentResolver().query(CONTENT_URI, null, trama, null, SOLICITA_TED);
        /* ----------------------------------------- */

        String xml64 = null;
        String ted = null;
        String ted64 = null;

        /* Se obtiene la data encriptada */
        if (results != null) {
            if (results.moveToNext()) {

                /* Se obtiene el valor de cada columna */
                xml64 = results.getString(results.getColumnIndex(COLUMN_NAME_XML64));
                ted = results.getString(results.getColumnIndex(COLUMN_NAME_TED));//para generar el QR
                ted64 = results.getString(results.getColumnIndex(COLUMN_NAME_TED64));
                /* ----------------------------------------- */
            }
            /* ----------------------------------------- */
        }
        /* ----------------------------------------- */

        String[] valores = {xml64, ted64, ted};

        return valores;
    }

    /**
     * Cambia la region.
     * @param locale
     * @param resources
     * @param context
     */
    // https://stackoverflow.com/questions/4985805/set-locale-programmatically
    public static void setLocale(Locale locale, Resources resources, Context context){
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(locale);
        } else{
            configuration.locale=locale;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration,displayMetrics);
        }
    }

    /**
     * Completa con ceros el código del usuario que ha iniciado sesión.
     * @param codigoUsuarioLogin Código del usuario que ha iniciado sesión.
     * @param editor
     * @return El código de usuario completo.
     */
    public static String getCodigoUsuario(EditText codigoUsuarioLogin, SharedPreferences.Editor editor) {

        String zeros = "";
        if (codigoUsuarioLogin.getText().toString().length() < 11) {
            int num_zeros = 11 - codigoUsuarioLogin.getText().toString().length();

            for (int i = 0; i < num_zeros; i++) {
                zeros = zeros + "0";
            }
        }

        final String codigoUsuario = zeros + codigoUsuarioLogin.getText().toString();

        editor.putString("codigoUsuario", codigoUsuario);
        editor.commit();

        return codigoUsuario;

    }
    public static void logout(Context context){
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list)
    {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
    public static String ValidaRuc(String RUC)
    {
        try{
            if(RUC.length() == 11)
            {
              if(RUC.substring(0,2).equals("10") || RUC.substring(0,2).equals("15")|| RUC.substring(0,2).equals("16")||
                           RUC.substring(0,2).equals("17")|| RUC.substring(0,2).equals("20")
                  ){
                  int SUMA_RUC = Integer.valueOf(RUC.substring(0,1))*5;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(1,2))*4;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(2,3))*3;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(3,4))*2;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(4,5))*7;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(5,6))*6;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(6,7))*5;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(7,8))*4;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(8,9))*3;
                  SUMA_RUC = SUMA_RUC + Integer.valueOf(RUC.substring(9,10))*2;
                  double Resto = SUMA_RUC%11;
                  double Complemento =11-Resto;
                  if(((int) Complemento)>=10)
                  {
                      Complemento = Complemento - 10;
                  }
                  if(Integer.valueOf(RUC.substring(10,11)) ==((int) Complemento))
                  {
                      return "INFINITY_DEV";
                  }
                  else{
                      return "El numero de RUC es incorrecto, verifica y vuelvelo a intentar";
                  }
              }
              else{
                  return "RUC DEBE EMPEZAR CON 10,15,16,20";
              }
            }else{
                return "RUC DEBE CONTENER 11 DIGITOS";
            }
        }catch (Exception ex)
        {
            return  "Error "+ex.getMessage();
        }
    }
    public static boolean ValidaStadoImpresora(Context contexApp, IPrinter printer)
    {
        try{
            printer.init();
            String error = String.valueOf(printer.getStatus());
            if(error.equals("1"))
            {
                Toast.makeText(contexApp,"IMPRESORA OCUPADA", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("2"))
            {
                Toast.makeText(contexApp,"IMPRESORA SIN PAPEL", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("3"))
            {
                Toast.makeText(contexApp,"El formato del error del paquete de datos de impresión", Toast.LENGTH_SHORT).show();
                return false;
            }
            else if(error.equals("4"))
            {
                Toast.makeText(contexApp,"MAL FUNCIONAMIENTO DE LA IMPRESORA", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("8"))
            {
                Toast.makeText(contexApp,"IMPRESORA SOBRE CALOR", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("9"))
            {
                Toast.makeText(contexApp,"EL VOLTAJE DE LA IMPRESORA ES DEMASIADO BAJO", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-16"))
            {
                Toast.makeText(contexApp,"La impresión no está terminada", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-6"))
            {
                Toast.makeText(contexApp,"error de corte de atasco", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-5"))
            {
                Toast.makeText(contexApp,"error de apertura de la cubierta", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-4"))
            {
                Toast.makeText(contexApp,"La impresora no ha instalado la biblioteca de fuentes", Toast.LENGTH_SHORT).show();
                return false;
            }else if(error.equals("-2"))
            {
                Toast.makeText(contexApp,"El paquete de datos es demasiado largo", Toast.LENGTH_SHORT).show();
                return false;
            }
            else{
                return true;
            }
        }catch (Exception ex)
        {
            Toast.makeText(contexApp, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return  false;
        }
    }
    public static void CargaTipoDocumento(final Spinner ViewSpinner,Context ContextoAPP)
    {
        final List<Spinner_model> model = new ArrayList<>();
        Spinner_model tipo_docu = new Spinner_model("1", "SELECCIONAR", "");
        model.add(tipo_docu);
        Spinner_model tipo_docu1 = new Spinner_model("2", "DNI", "");
        model.add(tipo_docu1);
        Spinner_model tipo_docu2 = new Spinner_model("3", "RUC", "");
        model.add(tipo_docu2);
        Spinner_model tipo_docu3 = new Spinner_model("4", "CARNET DE EXTRANJERIA", "");
        model.add(tipo_docu3);
        Spinner_model tipo_docu4 = new Spinner_model("5", "PASAPORTE", "");
        model.add(tipo_docu4);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(ContextoAPP,
                android.R.layout.simple_spinner_item, model);
        ViewSpinner.setAdapter(spinnerArrayAdapter);
    }
    public static boolean ValidacionDocumento(String idTipoDocu, EditText NombreCliente, EditText DocumentoIdentidad, EditText NU_RUC, EditText NO_RASO_SO, Button Button_Validate,Context ContexAPP)
    {
        if(idTipoDocu=="1")
        {
            Button_Validate.setEnabled(true);
            Toast.makeText(ContexAPP, "SELECCIONAR TIPO DOCUMENTO", Toast.LENGTH_SHORT).show();
            return false;
        }else if(idTipoDocu=="2")
        {
            if (DocumentoIdentidad.getText().toString().length() != 8) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingrese un DNI", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (NombreCliente.getText().toString().equals(" ") || NombreCliente.getText().toString().equals("")) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="3")
        {
            if (DocumentoIdentidad.getText().toString().length() != 8) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingrese un DNI", Toast.LENGTH_SHORT).show();
                return false;
            }
            String Respuesta = FuncionesAuxiliares.ValidaRuc(NU_RUC.getText().toString().trim());
            if(!Respuesta.equals("INFINITY_DEV"))
            {
                Toast.makeText(ContexAPP, Respuesta, Toast.LENGTH_SHORT).show();
                Button_Validate.setEnabled(true);
                return false;
            }
            if (NO_RASO_SO.getText().toString().equals(" ") || NO_RASO_SO.getText().toString().equals("")) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingresar Razon Social", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (NombreCliente.getText().toString().equals(" ") || NombreCliente.getText().toString().equals("")) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="4")
        {
            if (DocumentoIdentidad.getText().toString().length() != 12) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingrese Carnet de Extranjeria", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (NombreCliente.getText().toString().equals(" ") || NombreCliente.getText().toString().equals("")) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else if(idTipoDocu=="5")
        {
            if (DocumentoIdentidad.getText().toString().length() != 12) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingrese Pasaporte", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (NombreCliente.getText().toString().equals(" ") || NombreCliente.getText().toString().equals("")) {
                Button_Validate.setEnabled(true);
                Toast.makeText(ContexAPP, "Ingresar Nombre Cliente", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }else {
            return false;
        }
    }
    public static String generarTramaBoleto(SharedPreferences sharedPreferences, String numSerieBLT, String numCorrelativoViajeCompleto, String empresaTrama) {

        numCorrelativoViajeCompleto = numCorrelativoViajeCompleto.substring(2);
        String[] empresaSeleccionada = empresaTrama.split("-");
        String tipoDocumento = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA"))
        {
            tipoDocumento = "01";
        }else{
            tipoDocumento = "03";
        }
        String tipoDocumentoCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            tipoDocumentoCliente = "6";
        } else if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").length() == 8) {
            tipoDocumentoCliente = "1";
        } else {
            tipoDocumentoCliente = "7";
        }
        String documentoCliente = "";

        if (sharedPreferences.getString("guardar_numeroDocumento", "NoData").equals("")) {
            documentoCliente = "-";
        } else {
            documentoCliente = sharedPreferences.getString("guardar_numeroDocumento", "NoData");
        }
        String DocuDeclara  = "";
        String nombreCliente = "";
        if(sharedPreferences.getString("TipoVenta","NoData").equals("FACTURA")) {
            nombreCliente = sharedPreferences.getString("guardar_RAZON_SOCIAL","NoData");
            DocuDeclara = sharedPreferences.getString("guardar_RUC","NoData");
        }else{
            nombreCliente = sharedPreferences.getString("guardar_nombreCliente", "NoData");
            DocuDeclara = sharedPreferences.getString("guardar_numeroDocumento","NoData");
        }
        String direccionCliente = "";
        if (sharedPreferences.getString("guardar_direccionCliente", "NoData").equals("")) {
            direccionCliente = "-";
        } else {
            direccionCliente = sharedPreferences.getString("guardar_direccionCliente", "NoData");
        }
        Date date = new Date();
        String strFechaFormat = "yyyy-MM-dd";
        String strHoraFormat = "hh:mm:ss";
        DateFormat fechaFormat = new SimpleDateFormat(strFechaFormat);
        DateFormat horaFormat = new SimpleDateFormat(strHoraFormat);
        final String fechaVenta = fechaFormat.format(date);
        final String horaVenta = horaFormat.format(date);
        String numeroFloat = String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")));
        String[] dataNumero = numeroFloat.split("\\.");
        String numLetra = ConversorNumerosLetras.cantidadConLetra(dataNumero[0]);
        String precioCadena = numLetra.toUpperCase() + " CON "+dataNumero[1]+"/100 SOLES";
        String tramaBoleto ="A;CODI_EMPR;;" + empresaSeleccionada[0].substring(1) + "\n" +
                "A;TipoDTE;;" + tipoDocumento + "\n" +
                "A;Serie;;" + numSerieBLT + "\n" +
                "A;Correlativo;;" + numCorrelativoViajeCompleto + "\n" +
                "A;FchEmis;;" + fechaVenta + "\n" +
                "A;HoraEmision;;" + horaVenta + "\n" +
                "A;TipoMoneda;;PEN\n" +
                "A;RUTEmis;;" + empresaSeleccionada[7] + "\n" +
                "A;RznSocEmis;;" + empresaSeleccionada[8] + "\n" +
                "A;NomComer;;" + empresaSeleccionada[1] + "\n" +
                "A;ComuEmis;;150115\n" +
                "A;DirEmis;;" + empresaSeleccionada[2] + "\n" +
                "A;UrbanizaEmis;;"+empresaSeleccionada[3]+ "\n" +
                "A;ProviEmis;;"+empresaSeleccionada[4]+ "\n" +
                "A;CodigoLocalAnexo;;0000\n" +
                "A;TipoRutReceptor;;"+tipoDocumentoCliente+"\n" +
                "A;RUTRecep;;" + DocuDeclara + "\n" +
                "A;RznSocRecep;;" + nombreCliente + "\n" +
                "A;DirRecep;;-\n" +
                "A;DirRecepUrbaniza;;NoData"+"\n"+
                "A;DirRecepProvincia;;NoData"+"\n"+
                "A;CodigoAutorizacion;;000000"+"\n"+
                "A;MntNeto;;0.00\n" +
                "A;MntExe;;0.00\n" +
                "A;MntExo;;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "A;MntTotal;;" + String.format("%.2f",Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData"))) +"\n" +
                "A;MntTotalIgv;;0.00\n" +
                "A;TipoOperacion;;0101\n" +
                "B;NroLinDet;1;1\n" +
                "B;QtyItem;1;1\n" +
                "B;UnmdItem;1;NIU\n" +
                "B;VlrCodigo;1;001\n" +
                "B;NmbItem;1;SERV. TRANSP. RUTA\n" +
                "B;CodigoProductoSunat;1;78111802\n" +
                "B;PrcItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;PrcItemSinIgv;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;MontoItem;1;" + String.format("%.2f", Float.valueOf(sharedPreferences.getString("guardar_tarifa", "NoData")))+"\n" +
                "B;IndExe;1;20\n" +
                "B;CodigoTipoIgv;1;9997\n" +
                "B;TasaIgv;1;18\n" +
                "B;ImpuestoIgv;1;0.00\n" +
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
                "E;DescripcionAdicSunat;3;"+sharedPreferences.getString("guardar_nombreCliente", "NoData")+"\n" +
                "E;TipoAdicSunat;4;01\n" +
                "E;NmrLineasDetalle;4;4\n" +
                "E;NmrLineasAdicSunat;4;4\n" +
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
                "E;DescripcionAdicSunat;7;-\n"+
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
                "E;DescripcionAdicSunat;12;WWW.SOYUZONLINE.COM.PE\n"+
                "E;TipoAdicSunat;13;01\n" +
                "E;NmrLineasDetalle;13;13\n" +
                "E;NmrLineasAdicSunat;13;13\n" +
                "E;DescripcionAdicSunat;13;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim() +"\n"+
                "E;TipoAdicSunat;14;01\n" +
                "E;NmrLineasDetalle;14;14\n" +
                "E;NmrLineasAdicSunat;14;14\n" +
                "E;DescripcionAdicSunat;14;"+sharedPreferences.getString("Destino_Texto","NoData").toString().trim() +"\n"+
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
                "E;DescripcionAdicSunat;18;"+numSerieBLT+"/F-VTS-42\n"+
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
                "E;DescripcionAdicSunat;31;www.perubus.com.pe/ Telf: 2052370\n"+
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
                "E;DescripcionAdicSunat;34;"+sharedPreferences.getString("Origen_Texto","NoData").toString().trim()+"  -  "+ sharedPreferences.getString("Destino_Texto","NoData").toString().trim()+"\n"+
                "E;TipoAdicSunat;35;02\n"+
                "E;NmrLineasDetalle;35;1\n"+
                "E;NmrLineasAdicSunat;35;13\n"+
                "E;DescripcionAdicSunat;35;-\n";
        return tramaBoleto;
    }
}
