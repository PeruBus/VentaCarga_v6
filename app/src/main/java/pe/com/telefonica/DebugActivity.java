package pe.com.telefonica.soyuz;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.entity.EScannerType;
import com.pax.neptunelite.api.NeptuneLiteUser;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Printer;


import com.zcs.sdk.SdkResult;
import com.zcs.sdk.*;
import com.zcs.sdk.print.*;
import java.util.ArrayList;


public class DebugActivity extends AppCompatActivity {


    private Context context;

//    private IDAL dal;
//
//    private IPrinter printer;

//    private IScanner iScanner;

    private DriverManager mDriverManager;
    private Printer mPrinter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        context = getApplicationContext();

        try {
//            dal = NeptuneLiteUser.getInstance().getDal(context);
//            printer = dal.getPrinter();
//            iScanner = dal.getScanner(EScannerType.REAR);
//            printer.fontSet(EFontTypeAscii.FONT_16_24, EFontTypeExtCode.FONT_24_24);
            mDriverManager= DriverManager.getInstance();
            mPrinter = mDriverManager.getPrinter();
        } catch (Exception e) {
            Log.e("IMPRESORA", "No se puede inicializar la impresora");
        }

        Button btn_debug_impresion = findViewById(R.id.btn_debug_impresion);

        String test = "PRUEBA IMPRESORA\n";
        final String repetido = new String(new char[400]).replace("\0", test);

        final String[] lineas = repetido.split("\n");

        btn_debug_impresion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int printStatus = mPrinter.getPrinterStatus();
                    if (printStatus == SdkResult.SDK_PRN_STATUS_PAPEROUT) {
                    }
                    else {
                        PrnStrFormat format = new PrnStrFormat();
                        format.setTextSize(30);
                        format.setAli(Layout.Alignment.ALIGN_CENTER);
                        format.setStyle(PrnTextStyle.BOLD);
                        format.setFont(PrnTextFont.CUSTOM);
//                        format.setPath(Environment.getExternalStorageDirectory() +
//                                "/fonts/simsun.ttf");
                        format.setTextSize(25);
                        format.setStyle(PrnTextStyle.NORMAL);
                        format.setAli(Layout.Alignment.ALIGN_NORMAL);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        mPrinter.setPrintAppendString("PRUEBA IMPRESORA", format);
                        printStatus = mPrinter.setPrintStart();
                    }







//                    ValidaImpresion();
//                    printer.init();
//                    printer.printStr("PRUEBA IMPRESORA 1\n", null);
//                    printer.printStr("PRUEBA IMPRESORA 2\n",  null);
//                    printer.printStr("PRUEBA IMPRESORA 3\n",  null);
//                    printer.printStr("PRUEBA IMPRESORA 4\n",  null);
//                    printer.printStr("PRUEBA IMPRESORA 5\n",  null);
//                    printer.printStr("PRUEBA IMPRESORA 6\n",  null);
//                    printer.printStr("PRUEBA IMPRESORA 7\n",  null);
//                    printer.printStr("\n\n\n\n\n\n\n\n", null);
//                    for (int i = 0; i < lineas.length; i++) {
//                       // printer.printStr(lineas[i]+"\n", null);
//                        if(i==10){
//                            break;
//                        }
//                        if (i%100 == 0) {
//                            int iRetError = printer.start();
//                            if (iRetError != 0x00) {
//                                Log.d("Impresora", "ERROR:"+iRetError);
//                            }
//                            Log.d("Impresora", "ERROR:"+iRetError);
//                            printer.init();
//                        }
//                    }
//                    String Serial = Build.SERIAL;
//                    Toast toast = Toast.makeText(getApplicationContext(),Serial , Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.CENTER, 50, 50);
//                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Impresora", "ERROR al imprimir");
                }
            }
        });

        final Button btn_debug_qr = findViewById(R.id.btn_debug_qr);
        btn_debug_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    if (iScanner.open()) {
//                        iScanner.setContinuousTimes(0);
//
//                        iScanner.start(new IScanner.IScanListener() {
//                            @Override
//                            public void onRead(String codigoQR) {
//                                Toast.makeText(getApplicationContext(),"QR:"+codigoQR, Toast.LENGTH_LONG).show();
//                                //btn_debug_qr.performClick();
//                                //iScanner.open();
//                                //iScanner.close();
//                                Log.d("d_leer","pasa despues de leer");
//
//                            }
//                            @Override
//                            public void onFinish() {
//                                Log.d("d_leer","cerrando ");
//                                //btn_debug_qr.performClick();
//                            }
//
//                            @Override
//                            public void onCancel() {
//                                Log.d("d_leer","cancelando");
//
//                            }
//                        });
//                    }
//                    Log.d("EjecutaBoton","false");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //btn_debug_qr.performClick();
            }
        });
    }
//    public void ValidaImpresion()
//    {
//        try{
//            //printer.init();
//            String error = String.valueOf(printer.getStatus());
//            if(error.equals("1"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA OCUPADA", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //        return;
//            }else if(error.equals("2"))
//            {
//                //Toast.makeText(getActivity(),"IMPRESORA SIN PAPEL", Toast.LENGTH_SHORT).show();
//                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA SIN PAPEL", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //    return;
//            }else if(error.equals("3"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"El formato del error del paquete de datos de impresión", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //     return;
//            }
//            else if(error.equals("4"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"MAL FUNCIONAMIENTO DE LA IMPRESORA", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //   return;
//            }else if(error.equals("8"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"IMPRESORA SOBRE CALOR", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //    return;
//            }else if(error.equals("9"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"EL VOLTAJE DE LA IMPRESORA ES DEMASIADO BAJO", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //       return;
//            }else if(error.equals("-16"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"La impresión no está terminada", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //return;
//            }else if(error.equals("-6"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"error de corte de atasco", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //   return;
//            }else if(error.equals("-5"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"error de apertura de la cubierta", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //    return;
//            }else if(error.equals("-4"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"La impresora no ha instalado la biblioteca de fuentes", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //  return;
//            }else if(error.equals("-2"))
//            {
//                Toast toast = Toast.makeText(getApplicationContext(),"El paquete de datos es demasiado largo", Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 50, 50);
//                toast.show();
//                //  return;
//            }
//
//        }catch (Exception ex)
//        {
//            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
}
