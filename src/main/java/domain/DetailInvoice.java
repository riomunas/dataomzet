package domain;

import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.Detail;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Delayed;

/**
 * Created by rio on 20/05/19.
 */
public class DetailInvoice {
    String kodeBarang;
    Integer qty;
    BigDecimal price;
    Date tanggal;

    public DetailInvoice(){}

    public DetailInvoice(String colBarang, String colQty, String colPrice, String colTanggal) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        this.tanggal = format.parse(colTanggal);
        this.kodeBarang = colBarang;
        this.qty = Integer.parseInt(colQty);
        this.price = new BigDecimal(colPrice);
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }


    @Override
    public String toString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return
                StringUtils.rightPad(kodeBarang, 10)+
                        StringUtils.leftPad(qty.toString(), 7)+
                        StringUtils.leftPad(price.toString(), 15)+
                        StringUtils.leftPad(format.format(tanggal), 15);
    }

    public static Comparator<DetailInvoice> COMPARE_BY_BARANG_AND_QTY_PRICE = new Comparator<DetailInvoice>() {
        public int compare(DetailInvoice o1, DetailInvoice o2) {
            String x1 = o1.getKodeBarang();
            String x2 = ((DetailInvoice) o2).getKodeBarang();
            int x = x1.compareTo(x2);
            if (x != 0) {
                return x;
            }

            Integer y1 = ((DetailInvoice) o1).getQty();
            Integer y2 = ((DetailInvoice) o2).getQty();
            int y = y1.compareTo(y2);
            if (y != 0) {
                return y;
            }

            BigDecimal z1 = ((DetailInvoice) o1).getPrice();
            BigDecimal z2 = ((DetailInvoice) o2).getPrice();
            int z = z1.compareTo(z2);
            if (z != 0) {
                return z;
            }

            Date a1 = ((DetailInvoice) o1).getTanggal();
            Date a2 = ((DetailInvoice) o2).getTanggal();
            int a = a1.compareTo(a2);
            return a;
        }
    };
}
