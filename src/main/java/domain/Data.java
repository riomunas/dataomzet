package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rio on 21/05/19.
 */
public class Data {
    Invoice penjualanERP;
    Invoice penjualanDOS;

    Pembelian pembelianERP;
    Pembelian pembelianDOS;

    Adjustment adjustmentDOS;
    Adjustment adjustmentERP;

    public Data() {}

    public Data(Pembelian pembelianERP, Pembelian pembelianDOS) {
        this.pembelianERP = pembelianERP;
        this.pembelianDOS = pembelianDOS;
    }

    List dataPembelianERP = new ArrayList();
    List dataPembelianDOS = new ArrayList();

    List dataDetailPembelianERP = new ArrayList();
    List dataDetailPembelianDOS = new ArrayList();

    List dataDetailAdjustmentERP = new ArrayList();
    List dataDetailAdjustmentDOS = new ArrayList();
    List dataDetailAdjustmentNegatifDOS = new ArrayList();

    public List getDataDetailAdjustmentNegatifDOS() {
        return dataDetailAdjustmentNegatifDOS;
    }

    public void setDataDetailAdjustmentNegatifDOS(List dataDetailAdjustmentNegatifDOS) {
        this.dataDetailAdjustmentNegatifDOS = dataDetailAdjustmentNegatifDOS;
    }

    public Adjustment getAdjustmentDOS() {
        return adjustmentDOS;
    }

    public void setAdjustmentDOS(Adjustment adjustmentDOS) {
        this.adjustmentDOS = adjustmentDOS;
    }

    public Adjustment getAdjustmentERP() {
        return adjustmentERP;
    }

    public void setAdjustmentERP(Adjustment adjustmentERP) {
        this.adjustmentERP = adjustmentERP;
    }

    public List getDataDetailAdjustmentERP() {
        return dataDetailAdjustmentERP;
    }

    public void setDataDetailAdjustmentERP(List dataDetailAdjustmentERP) {
        this.dataDetailAdjustmentERP = dataDetailAdjustmentERP;
    }

    public List getDataDetailAdjustmentDOS() {
        return dataDetailAdjustmentDOS;
    }

    public void setDataDetailAdjustmentDOS(List dataDetailAdjustmentDOS) {
        this.dataDetailAdjustmentDOS = dataDetailAdjustmentDOS;
    }

    public List getDataPembelianERP() {
        return dataPembelianERP;
    }

    public void setDataPembelianERP(List dataPembelianERP) {
        this.dataPembelianERP = dataPembelianERP;
    }

    public List getDataPembelianDOS() {
        return dataPembelianDOS;
    }

    public void setDataPembelianDOS(List dataPembelianDOS) {
        this.dataPembelianDOS = dataPembelianDOS;
    }

    public Pembelian getDataPebelianERP() {
        return pembelianERP;
    }

    public Invoice getPenjualanERP() {
        return penjualanERP;
    }

    public void setPenjualanERP(Invoice penjualanERP) {
        this.penjualanERP = penjualanERP;
    }

    public Invoice getPenjualanDOS() {
        return penjualanDOS;
    }

    public void setPenjualanDOS(Invoice penjualanDOS) {
        this.penjualanDOS = penjualanDOS;
    }

    public Pembelian getPembelianERP() {
        return pembelianERP;
    }

    public void setPembelianERP(Pembelian pembelianERP) {
        this.pembelianERP = pembelianERP;
    }

    public Pembelian getPembelianDOS() {
        return pembelianDOS;
    }

    public void setPembelianDOS(Pembelian pembelianDOS) {
        this.pembelianDOS = pembelianDOS;
    }

    public List getDataDetailPembelianERP() {
        return dataDetailPembelianERP;
    }

    public void setDataDetailPembelianERP(List dataDetailPembelianERP) {
        this.dataDetailPembelianERP = dataDetailPembelianERP;
    }

    public List getDataDetailPembelianDOS() {
        return dataDetailPembelianDOS;
    }

    public void setDataDetailPembelianDOS(List dataDetailPembelianDOS) {
        this.dataDetailPembelianDOS = dataDetailPembelianDOS;
    }
}
