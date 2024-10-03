package pe.com.telefonica.soyuz;


public class PreLiquidacion {

	/**
	* Lista de boletos de pre liquidación.
	*/
    private StringBuilder listaBoletos;
	/**
	* Cantiad de boletos de pre liquidación.
	*/
    private String cantBoletos;
	/**
	* Monto total de pre Liquidación.
	*/
    private String montoTotal;
	/**
	* Nombre del Anfitrión.
	*/
    private String nombreAnfitrion;
    private String co_vehi;
    private String co_rumb;
    private String fe_viaj;

	/**
	* Constructor de la clase.
	* @param listaBoletos
	*/
    public PreLiquidacion(StringBuilder listaBoletos){
        this.listaBoletos = listaBoletos;
    }

	/**
	* @return Cantidad de boletos.
	*/
    public String getCantBoletos() {
        return cantBoletos;
    }

	/**
	* @param cantBoletos Cantidad de boletos.
	*/
    public void setCantBoletos(String cantBoletos) {
        this.cantBoletos = cantBoletos;
    }

	/**
	* @return Monto Total.
	*/
    public String getMontoTotal() {
        return montoTotal;
    }

	/**
	* @param montoTotal Monto Total.
	*/
    public void setMontoTotal(String montoTotal) {
        this.montoTotal = montoTotal;
    }

	/**
	* @return Nombre del Anfitrión.
	*/
    public String getNombreAnfitrion() {
        return nombreAnfitrion;
    }

	/**
	* @param nombreAnfitrion Nombre del anfitrión.
	*/
    public void setNombreAnfitrion(String nombreAnfitrion) {
        this.nombreAnfitrion = nombreAnfitrion;
    }

    public String getCodigoVehiculo() {
        return co_vehi;
    }

    /**
     * @param co_vehi Cantidad de boletos.
     */
    public void setCodigoVehiculo(String co_vehi) {
        this.co_vehi = co_vehi;
    }


    public String getRumbo() {
        return co_rumb;
    }

    /**
     * @param co_rumb Cantidad de boletos.
     */
    public void setRumbo(String co_rumb) {
        this.co_rumb = co_rumb;
    }


    public String getFecha() {
        return fe_viaj;
    }

    /**
     * @param fe_viaj Cantidad de boletos.
     */
    public void setFecha(String fe_viaj) {
        this.fe_viaj = fe_viaj;
    }

	/**
	* Construye el voucher de pre liquidación.
	* @return Voucher de pre liquidación.
	*/
    public String getVoucher() {

        StringBuilder voucher= new StringBuilder();

        voucher.append("--------------------------------\n");
        voucher.append("         PRE-LIQUIDACION\n");
        voucher.append("--------------------------------\n");
        voucher.append("  CONDUCTOR:\n");
        voucher.append("  "+this.getNombreAnfitrion()+"\n");
        voucher.append("  BUS: "+this.getCodigoVehiculo()+"\n");
        voucher.append("  RUMBO: "+this.getRumbo()+"\n");
        voucher.append("  FECHA:"+this.getFecha()+"\n");
        voucher.append("--------------------------------\n");
        voucher.append("    NUM DOC       ORI DST MONTO \n");
        voucher.append("--------------------------------\n");
        voucher.append(this.listaBoletos);
        voucher.append("--------------------------------\n");
        voucher.append("CANTIDAD DE BOLETO: "+this.getCantBoletos()+"\n");
        voucher.append("MONTO TOTAL:        "+this.getMontoTotal()+"\n");

        return voucher.toString();
    }

	/**
	* @return Margen final.
	*/
    public String margenFinal() {
        return "\n\n\n\n\n\n\n\n\n";
    }
}
