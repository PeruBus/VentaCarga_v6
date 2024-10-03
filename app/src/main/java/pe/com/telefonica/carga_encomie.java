package pe.com.telefonica.soyuz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.pax.dal.IDAL;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.completarCorrelativo;
import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;

public class carga_encomie extends Fragment {
    ListView listView;
    private Boolean existe = false;
    private SharedPreferences sharedPreferences;
    private DatabaseBoletos ventaBlt;
    private SQLiteDatabase sqLiteDatabase;


    final ArrayList<String> lista_boletosLeidos = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_carga_encomie, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final Button btn_leerQR = view.findViewById(R.id.btn_leerQR);
        final Button btn_buscarItin_1 = view.findViewById(R.id.btn_buscarItin_1);
        listView = view.findViewById(R.id.listView_boletosLeidos);

        final Spinner spinner_tipoProducto = view.findViewById(R.id.spinner_tipoProducto);

        AgregaBoletos(view);
        btn_leerQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View vista) {

                try {

                    /* Se inicializa el scanner del equipo */
                    final IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
                    IScanner iScanner = dal.getScanner(EScannerType.REAR);
                    /* ----------------------------------------- */

                    if (iScanner.open()) {
                        if(sharedPreferences.getString("Modulo","NoData").equals("ANDROID_CONTROL")||
                                sharedPreferences.getString("Modulo","NoData").equals("ANDROID_INSP"))
                        {
                            iScanner.setContinuousInterval(9000);
                            iScanner.setContinuousTimes(0);
                        }
                        iScanner.start(new IScanner.IScanListener() {

                            @Override
                            public void onRead(String codigoQR) {

                                    ValidaTramaCarga(codigoQR,vista);

                                //                               existe = false;
                            }

                            @Override
                            public void onFinish() {}

                            @Override
                            public void onCancel() {}
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error al scanear el boleto.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), ErrorActivity.class);
                    startActivity(intent);
                }
            }
        });


        btn_buscarItin_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Cambia a la vista de buscar boleto */
                BuscarBoleto BuscaBoletoCliente = new BuscarBoleto();
                //BuscarBoletoFragment buscarBoletoFragment  = new BuscarBoletoFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_base, BuscaBoletoCliente).commit();
                /* ----------------------------------------- */

            }
        });
    }

    boolean ValidaTramaCarga(String Trama,final View view)
    {
        final String[] dataCodigoQR = Trama.split("\\|");
        Log.d("TramaQR",dataCodigoQR.toString());
        String Evento="";
        boolean Respuesta =false;
        Log.d("trama",Trama);
//        for (int i = 0; i < lista_boletosLeidos.size(); i++) {
//            if(lista_boletosLeidos.get(i).contains(completarCorrelativo(Integer.valueOf(dataCodigoQR[3]))) && lista_boletosLeidos.get(i).contains(dataCodigoQR[2])){
//                //existe = true;
//                Toast.makeText(getActivity(),"BOLETO YA FUE AGREGADO", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }
        AgregaBoletoLista(dataCodigoQR[10],dataCodigoQR[2],completarCorrelativo(Integer.valueOf(dataCodigoQR[3])),dataCodigoQR[6],dataCodigoQR[15],dataCodigoQR[12],dataCodigoQR[13],dataCodigoQR[8],view,dataCodigoQR[18],dataCodigoQR[5],"4");

        return Respuesta;
    }

    void AgregaBoletoLista(String codEmpresa,String Serie,String Correlativo,String FE_DOCU,String NombreCliente,String Origen,String Destino,String DOCU_CLIE,View view, String NU_ASIE,String IM_TOTA,String ID_EVENTO)
    {
        lista_boletosLeidos.add(codEmpresa+"ƒ"+Serie+"ƒ"+Correlativo+"ƒ"+"BLT"+"ƒ"+FE_DOCU+"ƒ"+NombreCliente+"ƒ"+Origen+"ƒ"+Destino+"ƒ"+DOCU_CLIE+"ƒ"+NU_ASIE+"ƒ"+IM_TOTA+"ƒ"+ID_EVENTO);
        guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
        TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
        listView.setAdapter(adapterBoletosLeidos);
        Log.d("tamano", lista_boletosLeidos.toString());

    }
    void AgregaBoletos(View view)
    {
        String DataArreglo = sharedPreferences.getString("lista_boletosLeidos","NoData");
        if (DataArreglo.equals("NoData")==false)
        {
            String Arr =  DataArreglo.substring(1,(DataArreglo.length())-1);
            ArrayList<String> Data = new ArrayList<>();
            List<String> items = Arrays.asList(Arr.split("\\,"));

            final int size = items.size();
            for (int i = 0; i < size; i++)
            {
                //object = items.get(i);
                lista_boletosLeidos.add(items.get(i));
                //do something with i
            }
            guardarDataMemoria("lista_boletosLeidos", lista_boletosLeidos.toString(),getActivity());
            listView = view.findViewById(R.id.listView_boletosLeidos);
            TablaBoletosLeidosAdapter adapterBoletosLeidos = new TablaBoletosLeidosAdapter(lista_boletosLeidos, getActivity());
            listView.setAdapter(adapterBoletosLeidos);

            //List<String> list = new ArrayList<String>(Arrays.asList(string.split(" , ")));
        }


    }

}