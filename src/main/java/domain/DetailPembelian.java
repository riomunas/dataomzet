package domain;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * Created by rio on 21/05/19.
 */
public class DetailPembelian extends DetailInvoice  {
    String nomorPenerimaan;
    String namaSupplier;
    String kodeSupplier;

    public DetailPembelian(String kodeSupplier, String namaSupplier, String nomorPenerimaan, String colBarang, String colQty, String colTPrice, String colTanggal) throws ParseException {
        super(colBarang, colQty, colTPrice, colTanggal);
        this.kodeSupplier = kodeSupplier;
        this.namaSupplier = namaSupplier;
        this.nomorPenerimaan = nomorPenerimaan;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    public String getKodeSupplier() {
        return kodeSupplier;
    }

    public void setKodeSupplier(String kodeSupplier) {
        this.kodeSupplier = kodeSupplier;
    }

    public String getNomorPenerimaan() {
        return nomorPenerimaan;
    }

    public void setNomorPenerimaan(String nomorPenerimaan) {
        this.nomorPenerimaan = nomorPenerimaan;
    }

//    @Override
//    public int compareTo(DetailPembelian o) {
//        return Integer.compare(this.qty.intValue(), o.getQty().intValue());
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailPembelian d = (DetailPembelian) o;
        return (kodeBarang+"#"+qty+"#"+price).equals(d.getKodeBarang()+"#"+getQty()+"#"+getPrice());
    }

    public static Comparator<DetailPembelian> COMPARE_BY_BARANG_AND_QTY = new Comparator<DetailPembelian>() {
        public int compare(DetailPembelian o1, DetailPembelian o2) {
            String x1 = o1.getKodeBarang();
            String x2 = ((DetailPembelian) o2).getKodeBarang();
            int sComp = x1.compareTo(x2);

            if (sComp != 0) {
                return sComp;
            }

            Integer y1 = ((DetailPembelian) o1).getQty();
            Integer y2 = ((DetailPembelian) o2).getQty();
            return y1.compareTo(y2);
        }
    };

    public static Comparator<DetailPembelian> COMPARE_BY_BARANG_AND_QTY_PRICE = new Comparator<DetailPembelian>() {
        public int compare(DetailPembelian o1, DetailPembelian o2) {
            String x1 = o1.getKodeBarang();
            String x2 = ((DetailPembelian) o2).getKodeBarang();
            int x = x1.compareTo(x2);
            if (x != 0) {
                return x;
            }

            Integer y1 = ((DetailPembelian) o1).getQty();
            Integer y2 = ((DetailPembelian) o2).getQty();
            int y = y1.compareTo(y2);
            if (y != 0) {
                return y;
            }

            BigDecimal z1 = ((DetailPembelian) o1).getPrice();
            BigDecimal z2 = ((DetailPembelian) o2).getPrice();
            int z = z1.compareTo(z2);
            if (z != 0) {
                return z;
            }

            Date a1 = ((DetailPembelian) o1).getTanggal();
            Date a2 = ((DetailPembelian) o2).getTanggal();
            int a = a1.compareTo(a2);
            return a;
        }
    };
}
