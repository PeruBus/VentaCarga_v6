package pe.com.telefonica.soyuz;

import java.math.BigDecimal;
import java.util.ArrayList;

/* FUENTE:
 / https://github.com/monstruosoft/java-utils/blob/master/org/monstruosoft/utils/TareasProgramacion.java
 */
/**
 *
 * Convierte los números a letras.
 *
 * */
public class ConversorNumerosLetras {

	/**
	* Arreglo que contiene las unidades.
	*/
    private static final String UNIDADES[] = {"", "Uno ", "Dos ", "Tres ", "Cuatro ", "Cinco ", "Seis ", "Siete ", "Ocho ", "Nueve "};
	/**
	* Arreglo que contiene las decenas.
	*/
    private static final String DECENAS[]  = {"", "Dieci", "Veinti", "Treinta ", "Cuarenta ", "Cincuenta ", "Sesenta ", "Setenta ", "Ochenta ", "Noventa "};
	/**
	* Arreglo que contiene las centenas.
	*/
    private static final String CENTENAS[] = {"", "Ciento ", "Doscientos ", "Trescientos ", "Cuatrocientos ", "Quinientos ", "Seiscientos ", "Setecientos ", "Ochocientos ", "Novecientos "};

	/**
	* Arreglo que contiene las unidades en romanos.
	*/
    private static final String ROMANOS_UNIDADES[] = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
	/**
	* Arreglo que contiene las decenas en romanos.
	*/
    private static final String ROMANOS_DECENAS[]  = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
	/**
	* Arreglo que contiene las centenas en romanos.
	*/
    private static final String ROMANOS_CENTENAS[] = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
	/**
	* Arreglo que contiene los millares en romanos.
	*/
    private static final String ROMANOS_MILES[]    = {"", "M", "MM", "MMM"};

    /**
     * Convierte el número que recibe como argumento a su representación escrita con letras.
     * @param s Cadena de caracteres que contiene los dígitos de un número.
     * @return  Cadena de caracteres que contiene la representación escrita de
     *          la parte entera del número que se recibió como argumento.
     */
    public static String cantidadConLetra(String s) {
        StringBuilder result = new StringBuilder();
        BigDecimal totalBigDecimal = new BigDecimal(s).setScale(2, BigDecimal.ROUND_DOWN);
        long parteEntera = totalBigDecimal.toBigInteger().longValue();
        int triUnidades      = (int)((parteEntera % 1000));
        int triMiles         = (int)((parteEntera / 1000) % 1000);
        int triMillones      = (int)((parteEntera / 1000000) % 1000);
        int triMilMillones   = (int)((parteEntera / 1000000000) % 1000);

        if (parteEntera == 0) {
            result.append("Cero ");
            return result.toString();
        }

        if (triMilMillones > 0) result.append(triTexto(triMilMillones).toString() + "Mil ");
        if (triMillones > 0)    result.append(triTexto(triMillones).toString());

        if (triMilMillones == 0 && triMillones == 1) result.append("Millón ");
        else if (triMilMillones > 0 || triMillones > 0) result.append("Millones ");

        if (triMiles > 0)       result.append(triTexto(triMiles).toString() + "Mil ");
        if (triUnidades > 0)    result.append(triTexto(triUnidades).toString());

        return result.toString();
    }

    /**
     * Convierte el número que recibe como argumento a su representación escrita con números romanos.
     * @param s Cadena de caracteres que contiene los dígitos de un número. El rango válido para este
     *          argumento es entre 1 y 3999.
     * @return  Cadena de caracteres que contiene la representación escrita con números romanos de
     *          la parte entera del número que se recibió como argumento.
     */
    public static String cantidadNumerosRomanos(String s) {
        StringBuilder result = new StringBuilder();
        BigDecimal totalBigDecimal = new BigDecimal(s).setScale(2, BigDecimal.ROUND_DOWN);
        long parteEntera = totalBigDecimal.toBigInteger().longValue();

        if (parteEntera < 1 || parteEntera > 3999)
            throw new IllegalArgumentException("El número a convertir debe estar entre 1 y 3999.");

        int m = (int)parteEntera / 1000, c = (int)(parteEntera % 1000) / 100, d = (int)(parteEntera % 100) / 10, un = (int)parteEntera % 10;
        result.append(ROMANOS_MILES[m]);
        result.append(ROMANOS_CENTENAS[c]);
        result.append(ROMANOS_DECENAS[d]);
        result.append(ROMANOS_UNIDADES[un]);

        return result.toString();
    }

    /**
     * Genera una lista de enteros que contiene todos los números primos menores a *max*.
     * @param max Entero que define el límite de la búsqueda de números primos.
     * @return Lista de números primos.
     */
    public static ArrayList<Integer> numerosPrimos(int max) {
        ArrayList<Integer> primos = new ArrayList<Integer>();
        primos.add(2);

        for (int i = 3; i < max; i++) {
            boolean es_primo = true;
            double limite = Math.ceil(Math.sqrt(i));
            for (int j = 0; j < primos.size(); j++) {
                if (i % primos.get(j) == 0) {
                    es_primo = false;
                    break;
                }
                if (primos.get(j) > limite) break;
            }
            if (es_primo) primos.add(i);
        }

        return primos;
    }

    /**
     * Convierte una cantidad de tres cifras a su representación escrita.
     * @param n Cantidad a convertir.
     * @return Cadena de caracteres que contiene la representación escrita
     *          del número que se recibió como argumento.
     */
    private static StringBuilder triTexto(int n) {
        StringBuilder result = new StringBuilder();
        int centenas = n / 100;
        int decenas  = (n % 100) / 10;
        int unidades = (n % 10);

        if (n == 100) {
            result.append("Cien ");
            return result;
        }
        else result.append(CENTENAS[centenas]);

        if (decenas == 1 && unidades <= 5) {
            if (unidades == 0) result.append("Diez ");
            else if (unidades == 1) result.append("Once ");
            else if (unidades == 2) result.append("Doce ");
            else if (unidades == 3) result.append("Trece ");
            else if (unidades == 4) result.append("Catorce ");
            else if (unidades == 5) result.append("Quince ");
            return result;
        }
        else if (decenas == 2 && unidades == 0) {
            result.append("Veinte ");
            return result;
        }
        else result.append(DECENAS[decenas]);

        if (decenas > 2 && unidades > 0)
            result.append("y ");

        result.append(UNIDADES[unidades]);

        return result;
    }


}
