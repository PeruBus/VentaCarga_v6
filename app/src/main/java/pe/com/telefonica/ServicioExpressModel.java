package pe.com.telefonica.soyuz;

public class ServicioExpressModel {

    public String CO_VEHI = "";
    public String CANT_ASIE="";
    public String HO_SALI="";
    public String FE_PROG = "";
    public String ST_TIPO_SERV="";
    public String CO_DEST_ORIG="";
    public String CO_DEST_FINA="";
    public String NU_SECU="";
    public String ANFITRION="";
    public String CONDUCTOR="";
    public String CO_TIPO_BUSS="";
    public String CO_RUMB="";
    public String CANT_VENT="";
    public String CO_EMPR="";
    public String DE_TIPO_BUS="";
    public String NU_ASIE_DIS="";
    public ServicioExpressModel(String pCO_VEHI, String pCANT_ASIE,String pHO_SALI,String pFE_PROG,String pST_TIPO_SERV,String pCO_DEST_ORIG,String pCO_DEST_FINA,String pNU_SECU,
                                String pANFITRION,String pCONDUCTOR,String pCO_TIPO_BUSS,String pCO_RUMB,String pCANT_VENT,String pCO_EMPR,String pDE_TIPO_BUS,String pNU_ASIE_DIS)
    {
        this.CO_VEHI = pCO_VEHI;
        this.CANT_ASIE = pCANT_ASIE;
        this.HO_SALI = pHO_SALI;
        this.FE_PROG=pFE_PROG;
        this.ST_TIPO_SERV=pST_TIPO_SERV;
        this.CO_DEST_ORIG=pCO_DEST_ORIG;
        this.CO_DEST_FINA=pCO_DEST_FINA;
        this.NU_SECU=pNU_SECU;
        this.ANFITRION=pANFITRION;
        this.CONDUCTOR=pCONDUCTOR;
        this.CO_TIPO_BUSS=pCO_TIPO_BUSS;
        this.CO_RUMB=pCO_RUMB;
        this.CANT_VENT=pCANT_VENT;
        this.CO_EMPR=pCO_EMPR;
        this.DE_TIPO_BUS = pDE_TIPO_BUS;
        this.NU_ASIE_DIS = pNU_ASIE_DIS;
    }
    public String toString()
    {
        return( FE_PROG + "ƒ"+ NU_SECU+"ƒ"+CO_TIPO_BUSS+"ƒ"+CO_RUMB+"ƒ"+ST_TIPO_SERV+"ƒ"+CO_EMPR);
    }
    public String getNU_ASIE_DIS(){return NU_ASIE_DIS; }
    public String getDE_TIPO_BUS(){ return DE_TIPO_BUS;}
    public String getCO_EMPR()
    {
        return CO_EMPR;
    }
    public String getCO_VEHI()
    {
        return CO_VEHI;
    }
    public String getCANT_ASIE()
    {
        return CANT_ASIE;
    }

    public String getANFITRION() {
        return ANFITRION;
    }

    public String getCANT_VENT() {
        return CANT_VENT;
    }

    public String getCO_DEST_FINA() {
        return CO_DEST_FINA;
    }

    public String getCO_DEST_ORIG() {
        return CO_DEST_ORIG;
    }

    public String getCO_RUMB() {
        return CO_RUMB;
    }

    public String getCO_TIPO_BUSS() {
        return CO_TIPO_BUSS;
    }

    public String getCONDUCTOR() {
        return CONDUCTOR;
    }

    public String getFE_PROG() {
        return FE_PROG;
    }

    public String getHO_SALI() {
        return HO_SALI;
    }

    public String getNU_SECU() {
        return NU_SECU;
    }

    public String getST_TIPO_SERV() {
        return ST_TIPO_SERV;
    }

}
