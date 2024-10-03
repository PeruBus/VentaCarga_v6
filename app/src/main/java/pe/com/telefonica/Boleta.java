package pe.com.telefonica.soyuz;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;


public class Boleta {

    private String CO_EMPR;
    private String pCO_VEHI;

    private String origen;

    private String destino;

    private String tarifa;

    private String dni;

    private String fechaVenta;

    private String horaVenta;

    private String correlativoPasaje;

    private String seriePasaje;

    private String correlativoCarga;

    private String serieCarga;

    private String empresa;

    private String tipoBoleta;

    private String tipoProducto;

    private String cantidad;

    private String nombreAnfitrion;

    private String numAsiento;

    private String nombreCliente;

    private String Prueba;

    private String pRUC;

    private String RazonSocial;

    private String TipoDocuElectronico;

    public void SetDocuElectronico(String DocuElectronico)
    {
        this.TipoDocuElectronico =DocuElectronico;
    }
    public String getDocuElectronico()
    {
        return TipoDocuElectronico;
    }


    public void SetRUC(String RUC)
    {
        this.pRUC = RUC;
    }
    public String GetRUC()
    {
        return pRUC;
    }
    public void SetRazonSocial(String RazonSocial)
    {
        this.RazonSocial = RazonSocial;
    }
    public String GetRazonSocial()
    {
        return RazonSocial;
    }


    public Boleta(String tipoBoleta){
        this.tipoBoleta = tipoBoleta;
    }
	
	public String getPrueba() {
        return Prueba;
    }
    public void SetPrueba(String pPrueba){
        this.Prueba =pPrueba;
    }

    public String getOrigen() {
        return origen;
    }

    public String getEmpesa_imp(){
        return CO_EMPR;
    }
    public void setEmpesa_imp(String co_empr){
        this.CO_EMPR=co_empr;
    }

    public void setCO_VEHI(String co_vehi)
    {
        this.pCO_VEHI = co_vehi;
    }
    public String getCo_VEHI()
    {
        return  pCO_VEHI;
    }
	

    public void setOrigen(String origen) {
        this.origen = origen;
    }


    public String getDestino() {
        return destino;
    }
	

    public void setDestino(String destino) {
        this.destino = destino;
    }


    public String getTarifa() {
        return tarifa;
    }
	

    public void setTarifa(String tarifa) {
        this.tarifa = tarifa;
    }
	

    public String getDNI() {
        return dni;
    }


    public void setDNI(String dni) { this.dni = dni; }


    public String getFechaVenta() {
        return fechaVenta;
    }


    public void setFechaVenta(String fechaVenta) { this.fechaVenta = fechaVenta; }


    public String getHoraVenta() {
        return horaVenta;
    }


    public void setHoraVenta(String horaVenta) { this.horaVenta = horaVenta; }


    public String getCorrelativoPasaje() {
        return correlativoPasaje;
    }


    public void setCorrelativoPasaje(String correlativoPasaje) {
        this.correlativoPasaje = correlativoPasaje;
    }


    public String getSeriePasaje() {
        return seriePasaje;
    }


    public void setSeriePasaje(String seriePasaje) {
        this.seriePasaje = seriePasaje;
    }


    public String getCorrelativoCarga() {
        return correlativoCarga;
    }


    public void setCorrelativoCarga(String correlativoCarga) {
        this.correlativoCarga = correlativoCarga;
    }


    public String getSerieCarga() {
        return serieCarga;
    }


    public void setSerieCarga(String serieCarga) {
        this.serieCarga = serieCarga;
    }


    public String getEmpresa() {
        return empresa;
    }


    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }


    public String getTipoProducto() {
        return tipoProducto;
    }


    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }


    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }
	

    public String getNombreAnfitrion() {
        return nombreAnfitrion;
    }


    public void setNombreAnfitrion(String nombreAnfitrion) {
        this.nombreAnfitrion = nombreAnfitrion;
    }


    public void setNumAsiento(String numAsiento) {
        this.numAsiento = numAsiento;
    }


    public String getNumAsiento() {
        return numAsiento;
    }


    public String getNombreCliente() {
        return nombreCliente;
    }


    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }


    public String getVoucher() {

        String[] data_empresa = this.getEmpresa().split("-");
        StringBuilder voucher= new StringBuilder();
        if(!this.getPrueba().equals("1"))
        {


            if(this.getEmpesa_imp().equals("01"))
            {
                voucher.append("     "+data_empresa[1]+"\n");
            }else if(this.getEmpesa_imp().equals("02")){
                voucher.append("            "+data_empresa[1]+"\n");
            }
    //        voucher.append("     "+data_empresa[1]+"\n");
            voucher.append(data_empresa[2]+"\n");
            voucher.append("           "+data_empresa[5]+" "+data_empresa[6]+"\n");
            voucher.append("        RUC "+data_empresa[7]+"\n");

            /* Número de serie y correlativo según el tipo de boleto */
            if(tipoBoleta.equals("Carga") || tipoBoleta.equals("Control")){
                voucher.append("         SERIE: "+this.getSerieCarga()+"\n");
                voucher.append("   CORRELATIVO: "+this.getCorrelativoCarga()+"\n");

            }else {
                //voucher.append("         SERIE: "+this.getSeriePasaje()+"\n");
                //voucher.append("   CORRELATIVO: "+this.getCorrelativoPasaje()+"\n");
                voucher.append(" N°COMPROBANTE: "+this.getSeriePasaje()+"-"+this.getCorrelativoPasaje().substring(2,this.getCorrelativoPasaje().length())+"\n");
            }
        }else{
            voucher.append("--------------------------------\n");
            voucher.append("  BOLETO DE PRUEBA CAPACITACION \n");
            //  voucher.append("             (VIAJE)\n");
            // voucher.append("             USUARIO\n");
            voucher.append("--------------------------------\n");
            voucher.append(" N°COMPROBANTE: "+this.getSeriePasaje()+"-"+this.getCorrelativoPasaje().substring(2,this.getCorrelativoPasaje().length())+"\n");
        }

        /* ----------------------------------------- */

        /* Rotulación para el boleto según el tipo de boleto y DNI/RUC */
        if(tipoBoleta.equals("Viaje") && this.getDocuElectronico().equals("BOLETA")){

            voucher.append("--------------------------------\n");
            voucher.append("   BOLETA DE VENTA ELECTRONICA\n");
          //  voucher.append("             (VIAJE)\n");
           // voucher.append("             USUARIO\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Carga") && this.getDocuElectronico().equals("BOLETA")){

            voucher.append("--------------------------------\n");
            voucher.append("   BOLETA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("             USUARIO\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Carga2") && this.getDocuElectronico().equals("BOLETA")){

            voucher.append("--------------------------------\n");
            voucher.append("   BOLETA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("          TRANSPORTISTA\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Control") && this.getDocuElectronico().equals("BOLETA")){

            voucher.append("--------------------------------\n");
            voucher.append("  BOLETA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("             CONTROL\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Viaje") && this.getDocuElectronico().equals("FACTURA")){

            voucher.append("--------------------------------\n");
            voucher.append("  FACTURA DE VENTA ELECTRONICA\n");
            //voucher.append("             (VIAJE)\n");
            //voucher.append("             USUARIO\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Carga") && this.getDocuElectronico().equals("FACTURA")){

            voucher.append("--------------------------------\n");
            voucher.append("  FACTURA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("             USUARIO\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Carga2") && this.getDocuElectronico().equals("FACTURA")){

            voucher.append("--------------------------------\n");
            voucher.append("  FACTURA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("          TRANSPORTISTA\n");
            voucher.append("--------------------------------\n");

        }else if(tipoBoleta.equals("Control") && this.getDocuElectronico().equals("FACTURA")){

            voucher.append("--------------------------------\n");
            voucher.append("  FACTURA DE VENTA ELECTRONICA\n");
            voucher.append("             (CARGA)\n");
            voucher.append("             CONTROL\n");
            voucher.append("--------------------------------\n");

        }
        /* ----------------------------------------- */

        /* Muestra la serie y correlativo del boleto de viaje si el tipo de boleto es carga */
        if(tipoBoleta.equals("Carga") || tipoBoleta.equals("Control")){
            voucher.append("Docu.Asociado: "+this.getSeriePasaje()+"-"+this.getCorrelativoPasaje()+"\n");
        }
        /* ----------------------------------------- */
        if(this.getCo_VEHI() != null)
        {
            if (getCo_VEHI().trim().length()>1 && getCo_VEHI().trim() != "NoData"  )
            {
                voucher.append("VEHICULO:"+this.getCo_VEHI()+"\n");
            }
        }
        voucher.append("FECHA EMISIÓN:"+this.getFechaVenta()+"\n");
        voucher.append("HORA VENTA:   "+this.getHoraVenta()+"\n");
        voucher.append("SENOR(ES):    "+this.getNombreCliente() +"\n");
        /*if (getPrueba().equals("1"))
        {
            voucher.append("BOLETO NO VALIDO PARA VENTA"+"\n");
            voucher.append("BOLETO DE PRUEBA CAPACITACIÓN"+"\n");
        }*/

        //voucher.append("NUM. ASIENTO:  "+this.getNumAsiento()+"\n");
        //voucher.append("FECHA VENTA:   "+this.getFechaVenta()+"\n");
        //voucher.append("HORA VENTA:    "+this.getHoraVenta()+"\n");
        //voucher.append("ORIGEN:        "+this.getOrigen()+"\n");
        //voucher.append("DESTINO:       "+this.getDestino()+"\n");
        //voucher.append("SENORE(ES):    -\n");

        /* Muestra RUC dependiendo de la cantidad de dígitos del documento ingresado */
        if (this.getDocuElectronico().equals("FACTURA")){
            voucher.append("RUC           :"+this.GetRUC()+"\n");
            voucher.append("Razon Social  :"+this.GetRazonSocial()+"\n");
            voucher.append("Docu.Identidad:"+this.getDNI()+"\n");
            //voucher.append("Cliente:     "+this.getNombreCliente() +"\n");
        }else{
            //voucher.append("RUC:           -\n");
        }
        /* ----------------------------------------- */

        //voucher.append("DIRECCION:     -\n");

        /* Muestra DNI dependiendo de la cantidad de dígitos del documento ingresado */
        if (this.getDocuElectronico().equals("BOLETA")){
            voucher.append("Docu.Identidad:         "+this.getDNI()+"\n");
            //voucher.append("Cliente:     "+this.getNombreCliente() +"\n");
        }else{
           // voucher.append("DNI:           -\n");
        }
        /* ----------------------------------------- */
        //String[] Destino = this.getDestino().split("-");
        voucher.append("ORIGEN:        "+this.getOrigen()+"\n");
        voucher.append("DESTINO:       "+this.getDestino()+"\n");
        //voucher.append("DESTINO:       "+this.getDestino()+"\n");

        voucher.append("NUM.ASIENTO:  "+this.getNumAsiento()+"   "+"HORA: "+this.getHoraVenta()+"\n");
        /* Muestra nombre del cliente */
      //  if(this.getNombreCliente().equals("")){
      //      voucher.append("NOMBRE:        -\n");
      //  }else{
      //      voucher.append("NOMBRE:        "+this.getNombreCliente()+"\n");
      //  }
        /* ----------------------------------------- */

       // voucher.append("DIRECCION:     "+data_empresa[5]+" - "+data_empresa[6]+"\n");
        //voucher.append("TELEFONO:      -\n");

        /* Muestra tipo de producto si el tipo de boleto es carga */
        if(tipoBoleta.equals("Carga") || tipoBoleta.equals("Control")){
            voucher.append("PRODUCTO:      "+this.getTipoProducto()+"\n");
        }
        /* ----------------------------------------- */

        voucher.append("--------------------------------\n");
        voucher.append("CANT. DESCRP         IMPORTE S/.\n");
        voucher.append("                                \n");
        voucher.append("--------------------------------\n");

        /* Muestra el monto del boleto y lo alinea según la cantidad de dígitos que tenga el monto */
        if(tipoBoleta.equals("Carga") || tipoBoleta.equals("Control")){
            String value = String.format("%.2f", Float.valueOf(this.getTarifa()));
            if(value.length() > 5){
                voucher.append(this.getCantidad()+".00  CARGA               "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");

            } else if(value.length() > 4){
                voucher.append(this.getCantidad()+".00  CARGA                "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");

            }else{
                voucher.append(this.getCantidad()+".00  CARGA                 "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            }

        }else{
            String value = String.format("%.2f", Float.valueOf(this.getTarifa()));
            if(value.length() > 5){
                voucher.append("1.00  PASAJE             "+String.format("%.2f", Float.valueOf(this.getTarifa()+"\n")));

            } else if(value.length() > 4){
                voucher.append("1.00  PASAJE             "+String.format("%.2f", Float.valueOf(this.getTarifa()+"\n")));

            }else{
                voucher.append("1.00  PASAJE             "+String.format("%.2f", Float.valueOf(this.getTarifa()+"\n")));
            }

        }
        /* ----------------------------------------- */

        voucher.append("    --------------------------------\n");
        //voucher.append("TOTAL DESCUENTO             0.00\n");
        //voucher.append("OP EXONERADAS               0.00\n");
        //voucher.append("OP INAFECTAS                0.00\n");

        String value = String.format("%.2f", Float.valueOf(this.getTarifa()));
        /* Muestra el monto del boleto y lo alinea según la cantidad de dígitos que tenga el monto */
        if(value.length() > 5){
            voucher.append("OP EXONERADAS             "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            voucher.append("OP INAFECTAS              0.00"+"\n");
            voucher.append("OP GRAVADAS               0.00"+"\n");
            voucher.append("OP I.G.V                  0.00"+"\n");
            voucher.append("IMPORTE TOTAL         S/ "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");

            //voucher.append("OP GRAVADAS               "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("OP INAFECTAS              "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("IMPORTE TOTAL             "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");

        } else if(value.length() > 4){
            voucher.append("OP EXONERADAS             "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            voucher.append("OP INAFECTAS              0.00"+"\n");
            voucher.append("OP GRAVADAS               0.00"+"\n");
            voucher.append("OP I.G.V                  0.00"+"\n");
            voucher.append("IMPORTE TOTAL        S/ "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("OP GRAVADAS                "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("OP INAFECTAS               "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("IMPORTE TOTAL              "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");

        }else{
            voucher.append("OP EXONERADAS             "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            voucher.append("OP INAFECTAS              0.00"+"\n");
            voucher.append("OP GRAVADAS               0.00"+"\n");
            voucher.append("OP I.G.V                  0.00"+"\n");
            voucher.append("IMPORTE TOTAL          S/ "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("OP GRAVADAS                 "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("OP INAFECTAS                "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
            //voucher.append("IMPORTE TOTAL               "+String.format("%.2f", Float.valueOf(this.getTarifa()))+"\n");
        }
        /* ----------------------------------------- */

        voucher.append("--------------------------------\n");
        //voucher.append("Cobrado Por: "+this.getNombreAnfitrion()+"\n");
        //voucher.append("Observacion: -\n");

        return voucher.toString();
    }


    public Bitmap getQRBitmap(String QRtext) {

        /* FUENTE:
         / https://stackoverflow.com/questions/28232116/android-using-zxing-generate-qr-code
         */

        BitMatrix result;
        //int WIDTH=500;
        int HEIGHT=600;
        int WIDTH=600;
        try {
            result = new MultiFormatWriter().encode(QRtext,
                    BarcodeFormat.QR_CODE, WIDTH, HEIGHT, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        } catch (WriterException we) {
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);
        bitmap.setPixels(pixels, 0, WIDTH, 0, 0, w, h);

        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 45, 45, 510, 510);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap bitmap3 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(),
                matrix, true);

        return bitmap3;
    }

    public String margenFinal() {
        String[] data_empresa = this.getEmpresa().split("-");
        StringBuilder voucher = new StringBuilder();
        String TipoDocu = "";
        String PaginaWeb="";
        if(this.getPrueba().equals("1"))
        {
            voucher.append("NO VALIDO PARA VIAJAR EN RUTA "+"\n");
            voucher.append("BOLETO NO VALIDO  PARA VIAJAR "+"\n");
            voucher.append("BOLETO DE PRUEBA CAPACITACION"+"\n");
            voucher.append("NO VALIDO PARA VIAJAR EN RUTA "+"\n");
            voucher.append("NO VALIDO PARA VIAJAR EN RUTA "+"\n");
            voucher.append("\n\n\n\n\n");
        }else{
        if(tipoBoleta.equals("Carga") || tipoBoleta.equals("Control")){
            //voucher.append("NU_DOCU: "+this.getSerieCarga()+"-"+this.getCorrelativoCarga()+"\n");
        }else {
            //voucher.append("NU_DOCU: "+this.getSeriePasaje()+"-"+this.getCorrelativoPasaje()+"\n");
            if (this.getSeriePasaje().substring(0,1).equals("B"))
            {
                TipoDocu = "boleta";
            }
            else{
                TipoDocu = "factura";
            }
        }
       /* voucher.append("Representación impresa de la "+"\n");
        voucher.append(TipoDocu+" de Venta electrónica"+"\n");
        voucher.append("Podrá ser consultada en:"+"\n");*/
        if(this.getEmpesa_imp().equals("01"))
        {
            PaginaWeb= "WWW.PERUBUS.COM.PE";
        }else if(this.getEmpesa_imp().equals("02")){
            PaginaWeb= "WWW.SOYUZONLINE.COM.PE";
        }
        voucher.append("Al recibir el presente documento aceptó todos los términos" +"\n");
        voucher.append("y las condiciones del contrato de servicio de");
        voucher.append(" transporte descritas y publicadas en letreros," +"\n");
        voucher.append("banner y paneles ubicados en los Terminales Terrestres y/o" +"\n");
        voucher.append("oficinas de venta y en nuestra página Web :"+PaginaWeb+"\n");
        voucher.append("Autorizado mediante resolución:N°0180050002160" +"\n");
        if(tipoBoleta.equals("Viaje"))
        {
          voucher.append("SOAT Compañia de seguros" +"\n");
          voucher.append("la positiva" +"\n");
        }
        voucher.append("\n\n\n\n\n");

        }
        return voucher.toString();
    }
}
