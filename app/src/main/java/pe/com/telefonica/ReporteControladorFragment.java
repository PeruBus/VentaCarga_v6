package pe.com.telefonica.soyuz;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.neptunelite.api.NeptuneLiteUser;

import static pe.com.telefonica.soyuz.FuncionesAuxiliares.guardarDataMemoria;


import java.util.ArrayList;

public class ReporteControladorFragment extends Fragment implements MyRecyclerViewAdapter.ItemClickListener {
    MyRecyclerViewAdapter adapter;
    ArrayList<ReporteControladorTrafico> animalNames = new ArrayList<ReporteControladorTrafico>();
    private RecyclerView recyclerView;
    private SQLiteDatabase sqLiteDatabase;
    private DatabaseBoletos ventaBlt;
    Button button_imprimir_controlador;
    ProgressDialog progressDialog;
    int FlagValidaButton=0;
    private SharedPreferences sharedPreferences;
    //private ReporteAdapter adapter;
    //private ArrayList<ReporteControladorTrafico> planetArrayList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reporte_contralor_trafico, parent, false);
    }


    @Override
    public  void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ventaBlt = new DatabaseBoletos(getActivity());
        sqLiteDatabase = ventaBlt.getWritableDatabase();
        CargaLista();
        button_imprimir_controlador = view.findViewById(R.id.button_imprimir_controlador);


        RecyclerView recyclerView = view.findViewById(R.id.rvAsignacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyRecyclerViewAdapter(getContext(), animalNames);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        button_imprimir_controlador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FlagValidaButton==0)
                {
                    FlagValidaButton=1;
                    /* Validación en caso no haya ningún boleto en pre liquidación */
                    //Log.d("cant_bolAsig",sharedPreferences.getString("Cant_BoletosAsignados","NoData"));
                    //if(sharedPreferences.getString("Cant_BoletosAsignados","NoData") != "0"){
                        //imprimir_preLiquidacion(listaBoletos, sharedPreferences);
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Espere por favor");
                        progressDialog.setMessage("Imprimiendo");
                        progressDialog.setCancelable(false);
                        /* Hilo secundario que ejecuta todos los requests mientras está activado el cuadro de espera */
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    imprimir();
                                    button_imprimir_controlador.setEnabled(true);
                                    FlagValidaButton=0;
                                    progressDialog.dismiss();
                                    //Log.d("PRELIUIDACION_IMPRIME", "IMPRIMIENO");

                                }catch (Exception e)
                                {
                                    //Log.d("error",e.toString());
                                    //button_imprimir.setEnabled(true);
                                    progressDialog.dismiss();
                                }
                            }
                        });
                        /* ----------------------------------------- */
                        thread.start();
                        progressDialog.show();

                   // }else{
                     //   FlagValidaButton=0;
                     //   Toast.makeText(getActivity(), "No hay boletos para imprimir", Toast.LENGTH_SHORT).show();
                    //}
                    /* ----------------------------------------- */
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(getActivity(), "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
    void CargaLista()
    {
        try {
            sqLiteDatabase = ventaBlt.getWritableDatabase();
            final Cursor cursor = sqLiteDatabase.rawQuery("SELECT CO_EMPR,FE_VIAJ,NU_SECU,CO_VEHI,count(1) Cant_Asignacion FROM ASIGNACION group by CO_EMPR,FE_VIAJ,NU_SECU,CO_VEHI",new String[]{});
            guardarDataMemoria("Cant_BoletosAsignados", Integer.toString(cursor.getCount()), getActivity());
            if (cursor.getCount() > 0) {
                while(cursor.moveToNext()){
                    ReporteControladorTrafico planet = new ReporteControladorTrafico(cursor.getString(cursor.getColumnIndex("CO_VEHI")),
                            "","",cursor.getString(cursor.getColumnIndex("FE_VIAJ")),"","",cursor.getString(cursor.getColumnIndex("CO_EMPR")),
                            cursor.getString(cursor.getColumnIndex("Cant_Asignacion")),cursor.getString(cursor.getColumnIndex("NU_SECU")));
                    animalNames.add(planet);
                }
            }

        }catch (Exception ex)
            {

            }
    }
    public String GeneraFormato()
    {

        StringBuilder voucher= new StringBuilder();

        voucher.append("--------------------------------\n");
        voucher.append("         REPORTE ASIGNACION\n");
        voucher.append("--------------------------------\n");
        voucher.append("                                \n");
        for(int i = 0; i < animalNames.size(); i++){
            voucher.append("--------------------------------\n");
            voucher.append("VEHICULO :"+animalNames.get(i).getpCO_VEHI()+"\n");
            voucher.append("EMPRESA  :"+animalNames.get(i).getEmpresa()+"\n");
            voucher.append("CANTIDAD :"+animalNames.get(i).getpcantidad()+"\n");
            voucher.append("FECHA V. :"+animalNames.get(i).getpfechaVenta()+"\n");
            voucher.append("SECUENCIA:"+animalNames.get(i).getpnombreAnfitrion()+"\n");
            voucher.append("--------------------------------\n");
        }
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        voucher.append("\n\n\n\n\n\n\n\n\n");
        return voucher.toString();
    }
    public void imprimir() {


        /* ----------------------------------------- */

        try {
            IDAL dal = NeptuneLiteUser.getInstance().getDal(getActivity());
            IPrinter printer = dal.getPrinter();
            printer.init();
            String[] boletos = GeneraFormato().toString().split("\n");
            for (int i = 0; i < boletos.length; i++) {
                printer.printStr(boletos[i]+"\n", null);

                if (i%100 == 0) {
                    int iRetError = printer.start();
                    if (iRetError != 0x00) {
                        //Log.d("Impresora", "ERROR:"+iRetError);
                    }
                    printer.init();
                }
            }
            printer.printStr(margenFinal(), null);
            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            //printer.printStr(preLiquidacion.margenFinal(), null);
            /* ----------------------------------------- */
            int iRetError = printer.start();
            if (iRetError != 0x00) {
                if (iRetError == 0x02) {
                    //TODO mensaje de falta de papel
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error al imprimir el voucher.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getActivity(), ErrorActivity.class);
            startActivity(intent);
        }

    }
    public String margenFinal() {
        return "\n\n\n\n\n\n\n\n\n";
    }






}
