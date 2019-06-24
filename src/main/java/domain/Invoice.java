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
 * Created by rio on 20/05/19.
 */
public class Invoice {
    String nomor;
    String SJ;
    BigDecimal DPP = BigDecimal.ZERO;
    List details = new ArrayList();

    public Invoice(){}

    public Invoice(String nomor) {
        this.nomor = nomor;
    }

    //    public Invoice(String colNomor, String tanggal) throws ParseException {


//    }

    public String getNomor() {
        return nomor;
    }

    public void setNomor(String nomor) {
        this.nomor = nomor;
    }

    public String getSJ() {
        return SJ;
    }

    public void setSJ(String SJ) {
        this.SJ = SJ;
    }

    public BigDecimal getDPP() {
        return DPP;
    }

    public void setDPP(BigDecimal DPP) {
        this.DPP = DPP;
    }

    public List getDetails() {
        return details;
    }

    public void setDetails(List details) {
        this.details = details;
    }

    @Override
    public String toString() {
        String result = "";
        String buffInvoice = "";
        List<DetailInvoice> list = (List<DetailInvoice>) this.getDetails();
        Collections.sort(list, DetailInvoice.COMPARE_BY_BARANG_AND_QTY_PRICE);
        boolean isFirst = true;
        for (DetailInvoice detail : (List<DetailInvoice>) list) {
            String txtNomor = "";
            if (isFirst) {
                txtNomor = nomor;
                isFirst = false;
            } else {
                txtNomor = "";
            }
            txtNomor = StringUtils.rightPad(txtNomor, 15, " ");
            System.out.println(txtNomor+" "+detail);
        }
        return result;
    }
}
