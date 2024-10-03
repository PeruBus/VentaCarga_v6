package pe.com.telefonica.soyuz;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseBoletos extends SQLiteOpenHelper {

    /**
     * Version de la base de datos.
     */
    private static final int DATABASE_VERSION = 1;
    /**
     * Nombre de la base de datos.
     */
    private static final String DATABASE_NAME = "soyuz.db";
    /**
     * Nombre de la tabla de venta de boletos.
     */
    private static final String VENTA_BLT_TABLE_NAME = "VentaBoletos";

    //TODO: Constructor de la Base de Datos
    /**
     * Crea la base de datos.
     * @param context Contiene el contexto donde se ejecuta esta función.
     */
    public DatabaseBoletos(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    /**
    * Crea las tablas.
    */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table VentaBoletos (id integer primary key autoincrement, data_boleto text, estado text, tipo text, liberado text, puesto text,nu_docu text,co_empr text,ti_docu text,Log_data text)"
        );

        db.execSQL(
                "create table InspeccionBoletos (id integer primary key autoincrement, data_boleto text)"
        );
        db.execSQL(
                "create table ReporteInspeccion (id integer primary key autoincrement, data_boleto text, estado text)"
        );

        db.execSQL(
                "create table TransbordoBoletos (id integer primary key autoincrement, data_boleto text, estado text)"
        );

        db.execSQL("create table UltimaVenta(id integer primary key autoincrement,CO_EMPR text,TI_DOCU text,NU_DOCU text)");

        db.execSQL(
                "create table Asignacion(id integer primary key autoincrement,CO_EMPR text,TI_DOCU text,NU_DOCU text,NU_SECU text,FE_VIAJ text,HO_VIAJ text,CO_VEHI text)"
        );
        db.execSQL(
                "create table Manifiesto(id integer primary key autoincrement,CO_EMPR text,TI_DOCU text,NU_DOCU text,NU_SECU text,FE_VIAJ text,HO_VIAJ text,CO_VEHI text,NO_CLIE text,CO_DEST_ORIG text,CO_DEST_FINA text,DOCU_IDEN text,NU_ASIE text,IM_TOTA text,TIPO text)"
        );
    }

    /**
     * Actualiza la tabla.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + VENTA_BLT_TABLE_NAME);
        onCreate(db);
    }

}

