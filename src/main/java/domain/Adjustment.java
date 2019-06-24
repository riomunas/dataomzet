package domain;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rio on 13/06/19.
 */
public class Adjustment {
    String kodeBarang;
    List<DetailAdjustment> details = new ArrayList<DetailAdjustment>();

    public Adjustment(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public List<DetailAdjustment> getDetails() {
        return details;
    }

    public void setDetails(List<DetailAdjustment> details) {
        this.details = details;
    }


    @Override
    public String toString() {
        String result = "";
        List<DetailAdjustment> list = this.getDetails();
        Collections.sort(list, DetailAdjustment.COMPARE_BY_BARANG_AND_TANGGAL_AND_QTY);
        boolean isFirst = true;
        for (DetailAdjustment detail : list) {
            String txtNomor = "";
            if (isFirst) {
                txtNomor = kodeBarang;
                isFirst = false;
            } else {
                txtNomor = "";
            }
            txtNomor = StringUtils.rightPad(txtNomor, 10, " ");
            System.out.println(txtNomor+" "+detail);
        }
        return result;
    }
}
