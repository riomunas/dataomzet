package domain;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by rio on 21/05/19.
 */
public class Pembelian {
    String kode;
    String kodeSupplier;
    String namaSupplier;
    Date tanggal;

    BigDecimal total = BigDecimal.ZERO;
    List<DetailPembelian> details = new ArrayList();

    public Integer getTotalQty() {
        Integer sumQty = 0;
        for (DetailPembelian detailPembelian : details) {
            sumQty  = sumQty + detailPembelian.getQty();
        }
        return sumQty ;
    }

    public String getKodeSupplier() {
        return kodeSupplier;
    }

    public void setKodeSupplier(String kodeSupplier) {
        this.kodeSupplier = kodeSupplier;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    public Date getTanggal() {
        return tanggal;
    }

    public void setTanggal(Date tanggal) {
        this.tanggal = tanggal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetailPembelian> getDetails() {
        return details;
    }

    public void setDetails(List<DetailPembelian> details) {
        this.details = details;
    }

    public Pembelian(){}

    public Pembelian(String kode, String kodeSupplier, String namaSupplier, Date tanggal){
        this.tanggal = tanggal;
        this.kodeSupplier = kodeSupplier;
        this.namaSupplier = namaSupplier;
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getStringDetailKodeQty() {
        String result = "";
        for (DetailPembelian detail : (List<DetailPembelian>) this.getDetails()) {
            result = result+"#"+detail.getKodeBarang()+"#"+detail.getQty();
        }
        return  result;
    }

    public String getStringDetailKodeQtyTanggal() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String result = "";
        for (DetailPembelian detail : (List<DetailPembelian>) this.getDetails()) {
            result = result+"#"+detail.getKodeBarang()+"#"+detail.getQty()+"#"+format.format(detail.getTanggal());
        }
        return  result;
    }

    @Override
    public String toString() {
        String result = "";
        String buffNomorPenerimaan = "";
        List<DetailPembelian> list = (List<DetailPembelian>) this.getDetails();
        Collections.sort(list, DetailPembelian.COMPARE_BY_BARANG_AND_QTY);
        String nomorPenerimaan = "";
        boolean isFirst = true;
        for (DetailPembelian detail : (List<DetailPembelian>) list) {
            if (!buffNomorPenerimaan.equals(detail.getNomorPenerimaan())) {
                buffNomorPenerimaan = detail.getNomorPenerimaan();
                nomorPenerimaan = buffNomorPenerimaan+" "+kodeSupplier+" "+StringUtils.left(namaSupplier, 25);
            } else {
                nomorPenerimaan = "";
            }
            nomorPenerimaan = StringUtils.rightPad(nomorPenerimaan, 47);
            if (isFirst) {
                result += nomorPenerimaan+" "+detail;
                isFirst = false;
            } else {
                result += "\n"+nomorPenerimaan+" "+detail;
            }
        }
        return result;
    }
}
