package domain;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by rio on 13/06/19.
 */
public class DetailAdjustment extends DetailInvoice {
    private String nomor;

    public DetailAdjustment(String colBarang, String colQty, String colPrice, String colTanggal) throws ParseException {
        super(colBarang, colQty, colPrice, colTanggal);
    }

    public DetailAdjustment(String colBarang, String nomor, String colQty, String colPrice, String colTanggal) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.kodeBarang = colBarang;
        this.qty = Integer.parseInt(colQty);
        this.price = new BigDecimal(colPrice);
        this.tanggal = format.parse(colTanggal);
        this.nomor = nomor;
    }

    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return
                StringUtils.rightPad(nomor != null?nomor:"", 20)+
                StringUtils.rightPad(format.format(tanggal), 15)+
                StringUtils.leftPad(qty.toString(), 7)+
                        StringUtils.leftPad(price.toString(), 15);
    }

    public String getNomor() {
        return nomor;
    }

    public void setNomor(String nomor) {
        this.nomor = nomor;
    }

    public static Comparator<DetailAdjustment> COMPARE_BY_BARANG_AND_TANGGAL_AND_QTY = new Comparator<DetailAdjustment>() {
        public int compare(DetailAdjustment o1, DetailAdjustment o2) {
            String x1 = o1.getKodeBarang();
            String x2 = ((DetailAdjustment) o2).getKodeBarang();
            int x = x1.compareTo(x2);
            if (x != 0) {
                return x;
            }

            Date a1 = ((DetailAdjustment) o1).getTanggal();
            Date a2 = ((DetailAdjustment) o2).getTanggal();
            int a = a1.compareTo(a2);
            if (a != 0) {
                return a;
            }

            Integer b1 = o1.getQty();
            Integer b2 = ((DetailAdjustment) o2).getQty();
            int b = b1.compareTo(b2);
            return x;

        }
    };
}
