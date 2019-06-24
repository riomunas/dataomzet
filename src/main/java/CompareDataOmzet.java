import domain.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

//import javax.xml.soap.Detail;

/**
 * Created by rio on 20/05/19.
 */
public class CompareDataOmzet {
    public static final int COL_PRICE = 5;
    public static final int COL_T_COST = 8;

    public static final int ADJ_NOMOR = 1;
    public static final int ADJ_TANGGAL = 3;
    public static final int ADJ_KD_BARANG = 4;
    public static final int ADJ_QTY = 5;
    public static final int ADJ_UNIT_PRICE = 6;
    public static final int ADJ_UNIT_COST = 7;

    public static final int COL_KD_SUPPLIER = 1;
    public static final int COL_NAMA_SUPPLIER = 2;
    public static final int COL_NOMOR = 4;
    public static final int COL_SJ = 3;
    public static final int COL_INV = 4;
    public static final int COL_TANGGAL = 5;
    public static final int COL_KD_BARANG = 6;
    public static final int COL_QTY = 7;
    public static final int COL_UNIT_COST = 9;
    public static final int COL_UNIT_PRICE = 8;
    public static final int COL_T_PRICE = 10;
    private static final int COL_NO_PO = 3;

    boolean isTampilkanSemuaDataPenerimaan;
    String batasToleransi;
    Map dataPenjualan;
    Map dataPembelian;
    Map dataAdjustment;
    Map dataStock;
    Map dataBarang;

    List<DetailAdjustment> dataAdjustmentNegatif = new ArrayList<DetailAdjustment>();

    public CompareDataOmzet(File fileDOS, File fileERP, File filePembelianERP, File fileAdjustmentERP, File fileStockDOS, File fileStockERP,
                            String toleransi, String tampilkanSemuaPenerimaan) throws FileNotFoundException, ParseException {
        this.dataPenjualan = readDataPenjualanFromSource(fileDOS, fileERP);
        this.dataPembelian = readDataPembelianFromSource(fileDOS, filePembelianERP);
        this.dataAdjustment = readDataAdjustmentFromSource(fileDOS, fileAdjustmentERP);
        this.dataStock = readDataStockFromSource(fileStockDOS, fileStockERP);

        this.batasToleransi = toleransi;
        if (tampilkanSemuaPenerimaan != null && tampilkanSemuaPenerimaan.equalsIgnoreCase("Y")) {
            isTampilkanSemuaDataPenerimaan = true;
        }
    }

    public Map getDataAdjustment() {
        return dataAdjustment;
    }

    public void setDataAdjustment(Map dataAdjustment) {
        this.dataAdjustment = dataAdjustment;
    }

    public Map getDataPembelian() {
        return dataPembelian;
    }

    public void setDataPembelian(Map dataPembelian) {
        this.dataPembelian = dataPembelian;
    }

    public Map getDataPenjualan() {
        return dataPenjualan;
    }

    public void setDataPenjualan(Map dataPenjualan) {
        this.dataPenjualan = dataPenjualan;
    }

    private List readDataPembelianDOS(File fileDOS) throws FileNotFoundException, ParseException {
        if (fileDOS == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileDOS);

        BigDecimal a = BigDecimal.ZERO;
        String noPOB4 = "";
        String colNomorB4 = "";
        String nomorTax = "";
        int counter = 0;
        boolean isDataPembelian = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("TOTAL PEMBELIAN")) break;
            if (line.trim().equals("PEMBELIAN")) {
                isDataPembelian = true;
            }
            if (!isDataPembelian) continue;
            if (line.split(";").length < 5) continue;
            String[] dataColumn = line.split(";");
            String noPO = dataColumn[COL_NO_PO].split("/")[0].trim();
            String colKdSupplier = dataColumn[COL_KD_SUPPLIER].trim();
            String colNamaSupplier = dataColumn[COL_NAMA_SUPPLIER].trim();
            String colNomor = dataColumn[COL_NOMOR].trim();
            if (colNomor.length() == 0) {
                if (noPOB4.equals(noPO)) {
                    colNomor = colNomorB4;
                } else {
                    colNomor = StringUtils.leftPad(String.valueOf(counter++), 6, "Z");
                }
            }
            noPOB4 = noPO;
            colNomorB4 = colNomor;
            String colTanggal = dataColumn[COL_TANGGAL].trim();
            String colKdBarang = dataColumn[COL_KD_BARANG].trim();

            if (StringUtils.right(colKdBarang, 2).equalsIgnoreCase("KL")) {
                colKdBarang = StringUtils.left(colKdBarang, 6);
            }

            String colQty = dataColumn[COL_QTY].replaceAll(",", "").trim();
            String colUnitCost = dataColumn[COL_UNIT_COST].replaceAll(",", "").trim();

            final DetailPembelian item = new DetailPembelian(colKdSupplier, colNamaSupplier, colNomor, colKdBarang, colQty, colUnitCost, colTanggal);
            data.add(item);
        }
        scanner.close();
        return data;
    }

    ;

    private List readDataPenjualanDOS(File fileDOS) throws FileNotFoundException, ParseException {
        if (fileDOS == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileDOS);

        BigDecimal a = BigDecimal.ZERO;
        String buffColInvoice = "";
        String nomorTax = "";
        Invoice inv = new Invoice();
        String sjB4 = "";
        String nomorTaxB4 = "";
        Integer counterInvoice = 0;
        List listDetailInvoice = new ArrayList();
        Map<String, DetailInvoice> dataDetailMerge = new HashMap<String, DetailInvoice>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("RETUR PENJUALAN")) break;
            if (line.split(";").length >= 5) {
                String[] dataColumn = line.split(";");
                String colInv = "";
                String colSJ = dataColumn[COL_SJ].trim().split("/")[0].trim();
//                if (dataColumn[COL_INV].trim().length() == 1) {
                Integer panjangNomorTax = StringUtils.join(dataColumn[COL_INV].split("/"), "/").replaceAll(" ", "").length();
                if (panjangNomorTax < 13) {
                    if (panjangNomorTax == 1) {
                        counterInvoice++;
                        colInv = StringUtils.leftPad(String.valueOf(counterInvoice), 5, "Z");
                    } else {
                        if (sjB4.equals(colSJ)) {
                            colInv = nomorTaxB4;
                        } else {
                            colInv = StringUtils.leftPad(String.valueOf(Integer.parseInt(nomorTaxB4) + 1), 5, "0");
                            nomorTaxB4 = colInv;
                        }
                    }
                } else {
                    colInv = dataColumn[COL_INV].trim().split("/")[1].trim();
                    nomorTaxB4 = colInv;
                }

                String colTanggal = dataColumn[COL_TANGGAL].trim();
                String colKdBarang = dataColumn[COL_KD_BARANG].trim();
                String colQty = dataColumn[COL_QTY].replaceAll(",", "").trim();
//                String colTPrice= dataColumn[COL_T_PRICE].replaceAll(",", "").trim();
                String colTPrice = dataColumn[COL_UNIT_PRICE].replaceAll(",", "").trim();

                final DetailInvoice dtlInvoice = new DetailInvoice(colKdBarang, colQty, colTPrice, colTanggal);
                if (!buffColInvoice.equalsIgnoreCase(colInv.split("/")[0])) {
                    //buat invoice baru
                    if (!buffColInvoice.equalsIgnoreCase("")) {
                        Collections.sort(listDetailInvoice, DetailInvoice.COMPARE_BY_BARANG_AND_QTY_PRICE);

                        for (Map.Entry<String, DetailInvoice> entry : dataDetailMerge.entrySet()) {
                            DetailInvoice item = entry.getValue();
                            listDetailInvoice.add(item);
                        }

                        inv.setDetails(listDetailInvoice);
                        data.add(inv);
                    }
                    nomorTax = colInv;

                    buffColInvoice = colInv;
                    inv = new Invoice(StringUtils.right(nomorTax, 5));
                    listDetailInvoice = new ArrayList();
                    dataDetailMerge = new HashMap<String, DetailInvoice>();
                }
//                inv.getDetails().add(dtlInvoice);
                if (dataDetailMerge.get(dtlInvoice.getKodeBarang() + "#" + dtlInvoice.getPrice()) != null) {
                    DetailInvoice mergeDetail = dataDetailMerge.get(dtlInvoice.getKodeBarang() + "#" + dtlInvoice.getPrice());
                    mergeDetail.setQty(mergeDetail.getQty() + dtlInvoice.getQty());
                } else {
                    dataDetailMerge.put(dtlInvoice.getKodeBarang() + "#" + dtlInvoice.getPrice(), dtlInvoice);
                }

//                listDetailInvoice.add(dtlInvoice);
                inv.setDPP(inv.getDPP().add(dtlInvoice.getPrice().multiply(new BigDecimal(dtlInvoice.getQty()))));
                sjB4 = colSJ;
            }
        }
        data.add(inv);
        scanner.close();
        return data;
    }

    ;

    private List readDataPenjualanERP(File fileERP) throws FileNotFoundException, ParseException {
        if (fileERP == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileERP);

        BigDecimal a = BigDecimal.ZERO;
        String buffColInvoice = "";

        String nomorTax = "";
        boolean isDataPembelian = false;
        Invoice inv = new Invoice();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] dataColumn = line.split(";");

            String colKdSupplier = dataColumn[COL_KD_SUPPLIER].trim();
            String colNamaSupplier = dataColumn[COL_NAMA_SUPPLIER].trim();
            String colNomor = dataColumn[COL_NOMOR].trim();
            String colTanggal = dataColumn[COL_TANGGAL].trim();
            String colKdBarang = dataColumn[COL_KD_BARANG].trim();
            String colQty = dataColumn[COL_QTY].replaceAll(",", "").trim();
//            String colTPrice = dataColumn[COL_T_PRICE].replaceAll(",", "").trim();
            String colTPrice = dataColumn[COL_UNIT_PRICE].replaceAll(",", "").trim();

            final DetailInvoice dtlInvoice = new DetailInvoice(colKdBarang, colQty, colTPrice, colTanggal);
            if (!buffColInvoice.equalsIgnoreCase(colNomor)) {
                //buat invoice baru
                if (!buffColInvoice.equalsIgnoreCase("")) {
                    data.add(inv);
                }

                buffColInvoice = colNomor;
                inv = new Invoice(colNomor);
            }

            inv.getDetails().add(dtlInvoice);
            inv.setDPP(inv.getDPP().add(dtlInvoice.getPrice().multiply(new BigDecimal(dtlInvoice.getQty()))));
        }
        data.add(inv);
        scanner.close();
        return data;
    }

    ;

    private List readDataPembelianERP(File fileERP) throws FileNotFoundException, ParseException {
        if (fileERP == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileERP);

        BigDecimal a = BigDecimal.ZERO;
        String buffColKdBarang = "";
        String nomorTax = "";
        boolean isDataPembelian = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] dataColumn = line.split(";");
            String colKdSupplier = dataColumn[COL_KD_SUPPLIER].trim();
            String colNamaSupplier = dataColumn[COL_NAMA_SUPPLIER].trim();
            String colNomor = dataColumn[COL_NOMOR].trim();
            String colTanggal = dataColumn[COL_TANGGAL].trim();
            String colKdBarang = dataColumn[COL_KD_BARANG].trim();
            String colQty = dataColumn[COL_QTY].replaceAll(",", "").trim();
            String colUnitCost = dataColumn[COL_UNIT_COST].replaceAll(",", "").trim();

            final DetailPembelian item = new DetailPembelian(colKdSupplier, colNamaSupplier, colNomor, colKdBarang, colQty, colUnitCost, colTanggal);
            data.add(item);
        }
        scanner.close();
        return data;
    }

    private void getDataDetailFakturERP(String nomorInvoice) {
        getDataDetailFaktur(nomorInvoice, true);
    }

    private void getDataDetailFakturDOS(String nomorInvoice) {
        getDataDetailFaktur(nomorInvoice, false);
    }

    private void getDataDetailFaktur(String nomorInvoice, Boolean isERP) {
        Data data = (Data) getDataPenjualan().get(nomorInvoice);
        List<DetailInvoice> dataDetail;
        if (isERP) {
            dataDetail = (List<DetailInvoice>) data.getPenjualanERP().getDetails();
        } else {
            dataDetail = (List<DetailInvoice>) data.getPenjualanDOS().getDetails();
        }
        for (DetailInvoice item : dataDetail) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            System.out.println("\tbarang  : " + item.getKodeBarang());
            System.out.println("\ttanggal : " + format.format(item.getTanggal()));
            System.out.println("\tqty     : " + item.getQty());
            System.out.println("\tprice   : " + item.getPrice());
        }
    }

    private Map readDataPembelianFromSource(File fileDOS, File fileERP) throws FileNotFoundException, ParseException {
        Map map = Collections.synchronizedMap(new LinkedHashMap());

        List<DetailPembelian> dataDOS = readDataPembelianDOS(fileDOS);
        List<DetailPembelian> dataERP = readDataPembelianERP(fileERP);

        Collections.sort(dataDOS, DetailPembelian.COMPARE_BY_BARANG_AND_QTY_PRICE);
        Collections.sort(dataERP, DetailPembelian.COMPARE_BY_BARANG_AND_QTY_PRICE);

        System.out.println("~~> dataDetailPembelianDOS : " + dataDOS.size());
        System.out.println("~~> dataDetailPembelianERP : " + dataERP.size());

        for (DetailPembelian item : (List<DetailPembelian>) dataERP) {
            if (map.get(item.getKodeBarang()) != null) {
                ((Data) map.get(item.getKodeBarang())).getDataDetailPembelianERP().add(item);
            } else {
                Data data = new Data();
                data.getDataDetailPembelianERP().add(item);
                map.put(item.getKodeBarang(), data);
            }
        }

        for (DetailPembelian item : (List<DetailPembelian>) dataDOS) {
            if (map.get(item.getKodeBarang()) != null) {
                ((Data) map.get(item.getKodeBarang())).getDataDetailPembelianDOS().add(item);
            } else {
                Data data = new Data();
                data.getDataDetailPembelianDOS().add(item);
                map.put(item.getKodeBarang(), data);
            }
        }

        return map;
    }

    private Map readDataStockFromSource(File fileDOS, File fileERP) throws FileNotFoundException, ParseException {
        Map<String, Map> map = Collections.synchronizedMap(new LinkedHashMap());

        Map<String, Integer> dataDOS = readDataStockDOS(fileDOS);
        Map<String, Integer> dataERP = readDataStockERP(fileERP);

        map.put("DOS", dataDOS);
        map.put("ERP", dataERP);

        return map;
    }

    private Map laporanStock() throws FileNotFoundException, ParseException {
        Map<String, Map> map = this.dataStock;

        Map<String, Integer> dataDOS = map.get("DOS");
        Map<String, Integer> dataERP = map.get("ERP");

        Integer sumQtyDOS = 0;
        Integer sumQtyERP = 0;
        for (Map.Entry<String, Integer> entry : dataDOS.entrySet()) {
            Integer qtyDOS = entry.getValue();
            sumQtyDOS=sumQtyDOS+qtyDOS;

            String kodeBarang = entry.getKey();
            Map dos = new HashMap();
            dos.put("DOS", qtyDOS);
            map.put(kodeBarang, dos);
        }

        for (Map.Entry<String, Integer> entry : dataERP.entrySet()) {
            Integer qtyERP = entry.getValue();
            sumQtyERP=sumQtyERP+qtyERP;

            String kodeBarang = entry.getKey();
            Map erp = new HashMap();
            erp.put("ERP", qtyERP);
            if (map.get(kodeBarang) != null) {
                erp.putAll(map.get(kodeBarang));
                map.put(kodeBarang, erp);
            } else {
                map.put(kodeBarang, erp);
            }
        }

        System.out.println("\nSummary Total Stock : ");
        System.out.println("-------------------------------------------------------------");
        System.out.println("DOS : "+sumQtyDOS+", ERP : "+sumQtyERP+" -> Selisih : "+Math.abs(sumQtyDOS-sumQtyERP));


        System.out.println("\n\nDetail Stock Akhir Yang Tidak Sama: ");
        System.out.println("-------------------------------------------------------------");

        List sortedKeys=new ArrayList(map.keySet());
        Collections.sort(sortedKeys);
        for (Object key : sortedKeys) {
            Map entry =  map.get(key);

            Integer qtyDOS = 0;
            Integer qtyERP = 0;

            if (entry.get("DOS") != null) {
                qtyDOS = (Integer) entry.get("DOS");
            } else {
                qtyDOS = 0;
            }

            if (entry.get("ERP") != null) {
                qtyERP = (Integer) entry.get("ERP");
            } else {
                qtyERP = 0;
            }

            if (!qtyDOS.equals(qtyERP)) {
                if (qtyDOS ==0 && qtyERP == 0) {
                } else {
                    System.out.println(StringUtils.join(getKeys(dataBarang, key), ",")+" / "+key+" "+entry);
                }
            }
        }
        return map;
    }

    public <K, V> Set<K> getKeys(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    private Map readDataStockDOS(File fileDOS) throws FileNotFoundException {
        if (fileDOS == null) return new HashMap();
        Map<String, Integer> data = new HashMap();
        Scanner scanner = new Scanner(fileDOS);

        BigDecimal a = BigDecimal.ZERO;
        String buffColKdBarang = "";
        String nomorTax = "";
        boolean isDataStock = true;
        String kodeBarang = "";
        String lineTerakhir = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String colQty = "";

            if (line.startsWith("Kode Barang :")) {
                kodeBarang = line.split(":")[1].split("Unit")[0].trim();
            }

            if(kodeBarang.length() > 0 && line.startsWith("ÄÄÄÄÄÄÄÄÄÄÄÄÄÄÄ")) {
                if (lineTerakhir.length() >= 109) {
                    colQty = lineTerakhir.substring(97, 109).replaceAll(",","").trim();
                    Integer qty = Integer.parseInt(colQty);
                    if (data.get(kodeBarang) != null) {
                        data.put(kodeBarang, data.get(kodeBarang)+qty);
                    } else {
                        data.put(kodeBarang, qty);
                    }
                }
                break;
            }
            if (kodeBarang.length() > 0 && line.trim().length() == 0) {
                if (lineTerakhir.length() >= 109) {
                    colQty = lineTerakhir.substring(97, 109).replaceAll(",","").trim();
                    Integer qty = Integer.parseInt(colQty);
                    if (data.get(kodeBarang) != null) {
                        data.put(kodeBarang, data.get(kodeBarang)+qty);
                    } else {
                        data.put(kodeBarang, qty);
                    }
                }
            }
            if(kodeBarang.length() > 0 && line.trim().length() != 0) {
                lineTerakhir = line;
            }
        }
        scanner.close();
        return data;
    }

    private Map readDataStockERP(File fileERP) throws FileNotFoundException {
            if (fileERP == null) return new HashMap();

            this.dataBarang = readDataBarang(fileERP);

            Map<String, Integer> data = new HashMap();
            Scanner scanner = new Scanner(fileERP);

            BigDecimal a = BigDecimal.ZERO;
            String buffColKdBarang = "";
            String nomorTax = "";
            boolean isDataStock = true;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("BARANG")) break;
                if (line.contains("-----")) continue;
                if (!line.startsWith("-")) continue;

                String colKodeBarang = line.split(" ")[0].trim();
                String colQty = line.substring(158, 168).replaceAll("\\?","").replaceAll(",","").trim();

                if (dataBarang.get(colKodeBarang) == null) {
                    continue;
                }
                Integer qty = Integer.parseInt(colQty);
                String kodeBarang = dataBarang.get(colKodeBarang).toString();

                if (data.get(kodeBarang) != null) {
                    data.put(kodeBarang, data.get(kodeBarang)+qty);
                } else {
                    data.put(kodeBarang, qty);
                }
            }
            scanner.close();
            return data;
    }

    private Map<String,Object> readDataBarang(File fileERP) throws FileNotFoundException {
        if (fileERP == null) return new HashMap<String, Object>();

        Map<String, Object> data = new HashMap<String, Object>();
        Scanner scanner = new Scanner(fileERP);

        BigDecimal a = BigDecimal.ZERO;
        String buffColKdBarang = "";
        String nomorTax = "";
        boolean isDataBarang = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("BARANG")) {
                isDataBarang = true;
            }
            if (!isDataBarang) continue;
                String[] dataColumn = line.split(";");
                if (dataColumn.length != 2) continue;
                String colKodeERP = dataColumn[0].trim();
                String colKodeDOS = dataColumn[1].trim();
                data.put(colKodeERP, colKodeDOS);
        }
        scanner.close();
        return data;
    }

    private Map readDataAdjustmentFromSource(File fileDOS, File fileERP) throws FileNotFoundException, ParseException {
        Map<String, Data> map = Collections.synchronizedMap(new LinkedHashMap());

        List<DetailAdjustment> dataNegatifDOS = readDataAdjustmentNegatifDOS(fileDOS);
        List<DetailAdjustment> dataDOS = readDataAdjustmentDOS(fileDOS);
        List<DetailAdjustment> dataERP = readDataAdjustmentERP(fileERP);

        Collections.sort(dataNegatifDOS, DetailAdjustment.COMPARE_BY_BARANG_AND_QTY_PRICE);
        Collections.sort(dataDOS, DetailAdjustment.COMPARE_BY_BARANG_AND_QTY_PRICE);
        Collections.sort(dataERP, DetailAdjustment.COMPARE_BY_BARANG_AND_QTY_PRICE);

        System.out.println("~~> dataDetailAdjustmentDOS : " + dataDOS.size());
        System.out.println("~~> dataDetailAdjustmentERP : " + dataERP.size());

        for (DetailAdjustment item : (List<DetailAdjustment>) dataERP) {
            if (map.get(item.getKodeBarang()) != null) {
                ((Data) map.get(item.getKodeBarang())).getDataDetailAdjustmentERP().add(item);
            } else {
                Data data = new Data();
                data.getDataDetailAdjustmentERP().add(item);
                map.put(item.getKodeBarang(), data);
            }
        }

        for (DetailAdjustment item : (List<DetailAdjustment>) dataDOS) {
            if (map.get(item.getKodeBarang()) != null) {
                ((Data) map.get(item.getKodeBarang())).getDataDetailAdjustmentDOS().add(item);
            } else {
                Data data = new Data();
                data.getDataDetailAdjustmentDOS().add(item);
                map.put(item.getKodeBarang(), data);
            }
        }

        for (DetailAdjustment item : (List<DetailAdjustment>) dataNegatifDOS) {
            if (item.getKodeBarang().contains(".")) continue;
            dataAdjustmentNegatif.add(item);
        }

        //buat object adjustment nya
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");


        for (Map.Entry<String, Data> entry : map.entrySet()) {
            Data item = entry.getValue();

            ////DOS
            Map<String, DetailAdjustment> detailDOS = new HashMap();
            for (DetailAdjustment d : (List<DetailAdjustment>) item.getDataDetailAdjustmentDOS()) {
                String tanggal = format.format(d.getTanggal());
                String nomor = d.getNomor();
                Integer qty = d.getQty();
                String key = nomor + "#" + qty + "#" + tanggal;
                detailDOS.put(key, d);
            }

            final Adjustment adjDOS = new Adjustment(entry.getKey());
            for (Map.Entry<String, DetailAdjustment> e : detailDOS.entrySet()) {
                adjDOS.getDetails().add(e.getValue());
            }
            item.setAdjustmentDOS(adjDOS);

            ////ERP
            Map<String, DetailAdjustment> detailERP = new HashMap();
            for (DetailAdjustment d : (List<DetailAdjustment>) item.getDataDetailAdjustmentERP()) {
                String tanggal = format.format(d.getTanggal());
                String nomor = d.getNomor();
                Integer qty = d.getQty();
                String key = nomor + "#" + qty + "#" + tanggal;
                detailERP.put(key, d);
            }

            final Adjustment adjERP = new Adjustment(entry.getKey());
            for (Map.Entry<String, DetailAdjustment> e : detailERP.entrySet()) {
                adjERP.getDetails().add(e.getValue());
            }
            item.setAdjustmentERP(adjERP);

        }

        return map;
    }

    private List<DetailAdjustment> readDataAdjustmentERP(File fileERP) throws FileNotFoundException, ParseException {
        if (fileERP == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileERP);

        BigDecimal a = BigDecimal.ZERO;
        String buffColKdBarang = "";
        String nomorTax = "";
        boolean isDataPembelian = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] dataColumn = line.split(";");

//            System.out.println(line);

            String colNomor = dataColumn[ADJ_NOMOR].trim();
            String colTanggal = dataColumn[ADJ_TANGGAL].trim();
            String colKdBarang = dataColumn[ADJ_KD_BARANG].trim();
            String colQty = dataColumn[ADJ_QTY].replaceAll(",", "").trim();
            String colUnitCost = dataColumn[ADJ_UNIT_COST].replaceAll(",", "").trim();

            final DetailAdjustment item = new DetailAdjustment(colKdBarang, colNomor, colQty, colUnitCost, colTanggal);

            data.add(item);
        }
        scanner.close();
        return data;
    }

    private List<DetailAdjustment> readDataAdjustmentNegatifDOS(File fileDOS) throws FileNotFoundException, ParseException {
        if (fileDOS == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileDOS);

        BigDecimal a = BigDecimal.ZERO;
        String noPOB4 = "";
        String colNomorB4 = "";
        String nomorTax = "";
        int counter = 0;
        boolean isDataAdjustment = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("TOTAL ADJ.NGTF")) break;
            if (line.trim().equals("ADJUST NEGATIF")) {
                isDataAdjustment = true;
            }
            if (!isDataAdjustment) continue;
            if (line.split(";").length < 5) continue;
            String[] dataColumn = line.split(";");
            String nomor = dataColumn[ADJ_NOMOR].trim();
            String colTanggal = dataColumn[ADJ_TANGGAL].trim();
            String colKdBarang = dataColumn[ADJ_KD_BARANG].trim();
            String colQty = dataColumn[ADJ_QTY].replaceAll(",", "").trim().replaceAll("-", "").trim();
            String colUnitPrice = dataColumn[ADJ_UNIT_PRICE].replaceAll(",", "").trim();

            final DetailAdjustment item = new DetailAdjustment(colKdBarang, nomor, colQty, colUnitPrice, colTanggal);
            data.add(item);
        }
        scanner.close();
        return data;
    }

    private List<DetailAdjustment> readDataAdjustmentDOS(File fileDOS) throws FileNotFoundException, ParseException {
        if (fileDOS == null) return new ArrayList();
        List data = new ArrayList();
        Scanner scanner = new Scanner(fileDOS);

        BigDecimal a = BigDecimal.ZERO;
        String noPOB4 = "";
        String colNomorB4 = "";
        String nomorTax = "";
        int counter = 0;
        boolean isDataAdjustment = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("TOTAL ADJ")) break;
            if (line.trim().equals("ADJUST POSITIF")) {
                isDataAdjustment = true;
            }
            if (!isDataAdjustment) continue;
            if (line.split(";").length < 5) continue;
            String[] dataColumn = line.split(";");
            String nomor = dataColumn[ADJ_NOMOR].trim();
            String colTanggal = dataColumn[ADJ_TANGGAL].trim();
            String colKdBarang = dataColumn[ADJ_KD_BARANG].trim();
            String colQty = dataColumn[ADJ_QTY].replaceAll(",", "").trim();
            String colUnitCost = dataColumn[ADJ_UNIT_COST].replaceAll(",", "").trim();

            final DetailAdjustment item = new DetailAdjustment(colKdBarang, nomor, colQty, colUnitCost, colTanggal);
            data.add(item);
        }
        scanner.close();
        return data;
    }

    private Map readDataPenjualanFromSource(File fileDOS, File fileERP) throws FileNotFoundException, ParseException {
        List<Invoice> dataDOS = readDataPenjualanDOS(fileDOS);
        List<Invoice> dataERP = readDataPenjualanERP(fileERP);

        System.out.println("~~> dataDOS : " + dataDOS.size());
        System.out.println("~~> dataERP : " + dataERP.size());

        Map map = new HashMap();
        for (Invoice inv : dataDOS) {
            if (map.get(inv.getNomor()) != null) {
                ((Data) map.get(inv.getNomor())).setPenjualanDOS(inv);
            } else {
                Data data = new Data();
                data.setPenjualanDOS(inv);
                map.put(inv.getNomor(), data);
            }
        }

        for (Invoice inv : dataERP) {
            if (map.get(inv.getNomor()) != null) {
                ((Data) map.get(inv.getNomor())).setPenjualanERP(inv);
            } else {
                Data data = new Data();
                data.setPenjualanERP(inv);
                map.put(inv.getNomor(), data);
            }
        }

        return map;
    }

    private void laporanPenjualan() {
        List listERP = new ArrayList();
        List listDOS = new ArrayList();
        List listBoth = new ArrayList();

        Map<String, Data> data = this.getDataPenjualan();
        System.out.println("Jumlah seluruh dataPenjualan : " + data.size());

        System.out.println("\n------------");
        System.out.println(" PENJUALAN  ");
        System.out.println("------------\n");

        //invoice yang hanya ada di dos / erp
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            if (item.getPenjualanDOS() == null) {
                listERP.add(item.getPenjualanERP());
            }

            if (item.getPenjualanERP() == null) {
                listDOS.add(item.getPenjualanDOS());
            }

            if (item.getPenjualanDOS() != null && item.getPenjualanERP() != null) {
                listBoth.add(item);
            }
        }

//        laporanPenjualanKodeBarangYangSelisihQtyNYA(data);

        if (listERP.size() > 0) {
            System.out.println("\nInvoice ERP yang tidak ada di DOS : ");
            System.out.println("----------------------------------------------------------------------");
            System.out.println(StringUtils.rightPad("inv", 24) + StringUtils.leftPad("qty", 5, " ") + StringUtils.leftPad("price", 20));
            System.out.println("----------------------------------------------------------------------");
            for (Invoice invERP : (List<Invoice>) listERP) {
                System.out.println(invERP);
            }
            System.out.println("Total data : " + listERP.size());
            System.out.println("* Coba cek retur untuk invoice invoice ini");
        }

        if (listDOS.size() > 0) {
            System.out.println("\nInvoice DOS yang tidak ada di ERP : ");
            System.out.println("-----------------------------------");
            System.out.println(StringUtils.rightPad("inv", 10) + StringUtils.leftPad("qty", 5, " ") + StringUtils.leftPad("price", 20));
            System.out.println("-----------------------------------");
            for (Invoice invDOS : (List<Invoice>) listDOS) {
                System.out.print(invDOS);
            }
            System.out.println("Total data : " + listDOS.size());
        }

        System.out.println("");
        System.out.println("Data daftar invoice DOS - ERP yang selish nya lebih besar dari " + batasToleransi + " rupiah");
        System.out.println("----------------------------------------------------------------------------------------");

        BigDecimal sumSelisih = BigDecimal.ZERO;
        boolean isErpBigger = false;
        int counter = 0;
        for (Data item : (List<Data>) listBoth) {
            BigDecimal selisih = BigDecimal.ZERO;
            selisih = selisih.add(item.getPenjualanERP().getDPP().subtract(item.getPenjualanDOS().getDPP()));
            if (selisih.compareTo(BigDecimal.ZERO) > 0) {
                isErpBigger = true;
            } else {
                isErpBigger = false;
            }
            selisih = selisih.abs();
            String invoice = StringUtils.right("inv : " + item.getPenjualanERP().getNomor(), 15);
            String dppDOS = StringUtils.leftPad(item.getPenjualanDOS().getDPP().toString(), 22, " ");
            String dppERP = StringUtils.leftPad(item.getPenjualanERP().getDPP().toString(), 22, " ");
            String sSelisih = StringUtils.leftPad(selisih.toString() + (isErpBigger ? "*" : "!"), 22, " ");
            if (selisih.compareTo(new BigDecimal(batasToleransi)) == 1) {
                System.out.println(invoice.concat(dppDOS).concat(dppERP).concat(sSelisih));
                sumSelisih = sumSelisih.add(selisih);
                counter++;
                System.out.println("DOS :");
                System.out.print(item.getPenjualanDOS());
                System.out.println("ERP :");
                System.out.println(item.getPenjualanERP());
            }
        }
        System.out.println("Jumlah data : " + counter);
        System.out.println("\nKeterangan : ");
        System.out.println("* : ERP Bigger [coba cek credit note di erp]");
        System.out.println("! : DOS Bigger [coba cek retur dos nya]");
    }

    public void laporanPembelian() {
        Map<String, Data> data = this.getDataPembelian();
        System.out.println("\nJumlah seluruh kode barang : " + data.size());

        laporanPenerimaanQTY(data);

        laporanPenerimaanKodeBarangYangJomplang(data);

        List dataKodeBarangYangQTYnyaTidakSama = laporanPenerimaanKodeBarangYangSelisihQtyNYA(data);

        laporanPenerimaanKodeBarangYangSelisihHargaNYA(data);

        laporanPenerimaanPertransaksi(data, dataKodeBarangYangQTYnyaTidakSama);
    }

    public void laporanAdjustment() {
        System.out.println("\n------------");
        System.out.println(" ADJUSTMENT  ");
        System.out.println("------------");

        Map<String, Data> data = this.getDataAdjustment();

        List dataKodeBarangYangMissing = laporanAdjustmentKodeBarangYangTidakAdaDiERPDOS(data);

        List dataKodeBarangYangSelisih = laporanAdjustmentKodeBarangYangSelisihQtyNYA(data, dataKodeBarangYangMissing);

        laporanDataKodeBarangYangSelisih(data, dataKodeBarangYangSelisih);

        laporanAdjustmentSelisihQtyHargaNYA(data);

        daftarNomorAdjustmentMutasiYangDihapus(data, dataKodeBarangYangSelisih);
    }

    private void daftarNomorAdjustmentMutasiYangDihapus(Map<String, Data> data, List dataKodeBarangYangSelisih) {
        System.out.println("\nScript Delete nomor Adjustment mutasi ERP :");
        System.out.println("---------------------------------------------------------");
        TreeSet dataNomor = new TreeSet();
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Adjustment adjDOS = entry.getValue().getAdjustmentDOS();
            Adjustment adjERP = entry.getValue().getAdjustmentERP();
            if (!dataKodeBarangYangSelisih.contains(entry.getKey())) {
                for (DetailAdjustment d : adjERP.getDetails()) {
                    if (d.getPrice().compareTo(BigDecimal.ZERO) != 0) continue;
                    dataNomor.add(d.getNomor());
                }
            }
        }
        if (!dataNomor.isEmpty()) {
            String sql = "";
            sql += "DELETE FROM ic_trs_adjustment_stock_hdr WHERE nomor IN ('";
            sql += StringUtils.join(dataNomor.toArray(), "','");
            sql += "')";
            System.out.println(sql);
        }
    }

    private void laporanDataKodeBarangYangSelisih(Map<String, Data> map, List dataKodeBarangYangSelisih) {
        System.out.println("\nDaftar detail Adjustment DOS & ERP yang beda dan bukan hasil mutasi dari erp");
        System.out.println("---------------------------------------------------------");
        for (Map.Entry<String, Data> entry : map.entrySet()) {
            Adjustment adjDOS = entry.getValue().getAdjustmentDOS();
            Adjustment adjERP = entry.getValue().getAdjustmentERP();
            if (dataKodeBarangYangSelisih.contains(entry.getKey())) {
                System.out.println("DOS : ");
                System.out.print(adjDOS);
                System.out.println("ERP : ");
                System.out.print(adjERP);
                System.out.println("---------------------------------------------------------");
            }
        }
    }

    private void laporanAdjustmentSelisihQtyHargaNYA(Map<String, Data> map) {
        System.out.println("\nDaftar Adjustment DOS & ERP yang beda dan bukan hasil mutasi dari erp");
        System.out.println("---------------------------------------------------------");
        //buat object
        boolean isMutasi = false;
        boolean isAdaYangBeda = false;
        List dataKodeBarangYangBeda = new ArrayList();
        for (Map.Entry<String, Data> entry : map.entrySet()) {
            Adjustment adjDOS = entry.getValue().getAdjustmentDOS();
            Adjustment adjERP = entry.getValue().getAdjustmentERP();

            for (DetailAdjustment dERP : adjERP.getDetails()) {
                if (dERP.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                    isMutasi = true;
                    break;
                } else {
                    isMutasi = false;
                }
            }
            if (isMutasi) continue;
            List<Integer> listIndexToByPassDOS = new ArrayList();
            List listDataTidakAdaPasanganERP = new ArrayList();
            for (DetailAdjustment dERP : adjERP.getDetails()) {
                boolean isAdaYangSama = false;
                for (int counter = 0; counter < adjDOS.getDetails().size(); counter++) {
                    DetailAdjustment dDOS = adjDOS.getDetails().get(counter);
                    if (listIndexToByPassDOS.indexOf(counter) >= 0) continue;
                    if (dERP.getTanggal().equals(dDOS.getTanggal())
                            && dERP.getQty().equals(dDOS.getQty())
                            && dERP.getPrice().subtract(dDOS.getPrice()).abs().compareTo(BigDecimal.ONE) < 0) {
                        listIndexToByPassDOS.add(counter);
                        isAdaYangSama = true;
                        break;
                    } else {
                        isAdaYangSama = false;
                    }
                }
                if (!isAdaYangSama) {
                    listDataTidakAdaPasanganERP.add(dERP);
                }
            }

            if (listIndexToByPassDOS.size() != adjDOS.getDetails().size()) {
                isAdaYangBeda = true;
                System.out.println(entry.getKey());
                System.out.println("DOS : ");
                for (Integer i = 0; i < adjDOS.getDetails().size(); i++) {
                    if (listIndexToByPassDOS.contains(i)) continue;
                    System.out.println(adjDOS.getDetails().get(i));
                }
            }

            if (listDataTidakAdaPasanganERP.size() > 0) {
                isAdaYangBeda = true;
                System.out.println("ERP :");
                for (Object d : listDataTidakAdaPasanganERP) {
                    System.out.println(d);
                }
                System.out.println("----------------------------------------------------------");
            }
        }
        if (!isAdaYangBeda) {
            System.out.println("-");
        }
        System.out.println("==============================================================================================");
    }

    private void laporanPenerimaanPertransaksi(Map<String, Data> data, List dataKodeBarangYangQTYnyaTidakSama) {
        Map<String, List> mapDOS = Collections.synchronizedMap(new LinkedHashMap());
        Map<String, List> mapERP = Collections.synchronizedMap(new LinkedHashMap());

        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            //System.out.println(entry.getKey());
            List<DetailPembelian> dataDOS = item.getDataDetailPembelianDOS();
            List<DetailPembelian> dataERP = item.getDataDetailPembelianERP();

            Collections.sort(dataDOS, DetailPembelian.COMPARE_BY_BARANG_AND_QTY_PRICE);
            Collections.sort(dataERP, DetailPembelian.COMPARE_BY_BARANG_AND_QTY_PRICE);

            //buat map penerimaan dos
            for (DetailPembelian detail : (List<DetailPembelian>) dataDOS) {
                //System.out.println("~~> "+ detail);
                if (mapDOS.get(detail.getNomorPenerimaan()) != null) {
                    ((List) mapDOS.get(detail.getNomorPenerimaan())).add(detail);
                } else {
                    List list = new ArrayList();
                    list.add(detail);
                    mapDOS.put(detail.getNomorPenerimaan(), list);
                }
                //System.out.println(mapDOS);
            }

            //buat map penerimaan erp
            for (DetailPembelian detail : (List<DetailPembelian>) dataERP) {
                //System.out.println("~~> qty : "+ detail.getQty());
                if (mapERP.get(detail.getNomorPenerimaan()) != null) {
                    ((List) mapERP.get(detail.getNomorPenerimaan())).add(detail);
                } else {
                    List list = new ArrayList();
                    list.add(detail);
                    mapERP.put(detail.getNomorPenerimaan(), list);
                }
            }
        }


        Map<String, Data> map = Collections.synchronizedMap(new LinkedHashMap());
        //dos
        String kodeSupplier = "";
        String namaSupplier = "";
        Date tanggal = new Date();
        for (Map.Entry<String, List> entry : mapDOS.entrySet()) {
            String nomorPenerimaan = entry.getKey();
            List<DetailPembelian> list = entry.getValue();
            String kode = "";

            for (DetailPembelian detail : list) {
                kode = kode + "#" + detail.getKodeBarang();
                kodeSupplier = detail.getKodeSupplier();
                namaSupplier = detail.getNamaSupplier();
                tanggal = detail.getTanggal();
            }

            //buat object pembelian
            final Pembelian beli = new Pembelian(nomorPenerimaan, kodeSupplier, namaSupplier, tanggal);
            beli.setDetails(list);

            //simpan di map
            if (map.get(kode) != null) {
                ((Data) map.get(kode)).getDataPembelianDOS().add(beli);
            } else {
                Data d = new Data();
                d.getDataPembelianDOS().add(beli);
                map.put(kode, d);
            }
        }
        //erp
        for (Map.Entry<String, List> entry : mapERP.entrySet()) {
            String nomorPenerimaan = entry.getKey();
            List<DetailPembelian> list = entry.getValue();
            String kode = "";

            for (DetailPembelian detail : list) {
                kode = kode + "#" + detail.getKodeBarang();
                kodeSupplier = detail.getKodeSupplier();
                namaSupplier = detail.getNamaSupplier();
                tanggal = detail.getTanggal();
            }

            //buat object penerimaan
            final Pembelian beli = new Pembelian(nomorPenerimaan, kodeSupplier, namaSupplier, tanggal);
            beli.setDetails(list);

            //simpan di map
            if (map.get(kode) != null) {
                ((Data) map.get(kode)).getDataPembelianERP().add(beli);
            } else {
                Data d = new Data();
                d.getDataPembelianERP().add(beli);
                map.put(kode, d);
            }
        }


        List dataMissingERP = new ArrayList();
        List dataMissingDOS = new ArrayList();

        for (Map.Entry<String, Data> entry : map.entrySet()) {
            List<Pembelian> dataPembelianDOS = entry.getValue().getDataPembelianDOS();
            List<Pembelian> dataPembelianERP = entry.getValue().getDataPembelianERP();
            if (dataPembelianDOS.isEmpty()) {
                dataMissingDOS.addAll(dataPembelianERP);
            }
            if (dataPembelianERP.isEmpty()) {
                dataMissingERP.addAll(dataPembelianDOS);
            }
        }

        System.out.println("\nPenerimaan DOS yang tidak ada di erp");
        System.out.println("---------------------------------------------------------");
        for (Pembelian beli : (List<Pembelian>) dataMissingERP) {
            System.out.println(beli);
        }
        System.out.println("\nPenerimaan ERP yang tidak ada di dos");
        System.out.println("---------------------------------------------------------");
        for (Pembelian beli : (List<Pembelian>) dataMissingDOS) {
            System.out.println(beli);
        }

//        laporanPenerimaanXXX(map, dataMissingDOS);

        System.out.println("\nDaftar transaksi Penerimaan DOS & ERP yang qty nya tidak sama");
        System.out.println("---------------------------------------------------------\n");
        boolean isByPass = true;
        for (Map.Entry<String, Data> entry : map.entrySet()) {
            for (String kodeBarangTidakSama : (List<String>) dataKodeBarangYangQTYnyaTidakSama) {
                if (entry.getKey().contains(kodeBarangTidakSama)) {
                    isByPass = false;
                    break;
                } else {
                    isByPass = true;
                }
            }
            if (isByPass) continue;
            List<Pembelian> dataPembelianDOS = entry.getValue().getDataPembelianDOS();
            List<Pembelian> dataPembelianERP = entry.getValue().getDataPembelianERP();

            if (dataPembelianDOS.size() == 0 || dataPembelianERP.size() == 0) continue;
            String sdos = "";
            String serp = "";
            for (Pembelian dos : dataPembelianDOS) {
                sdos = sdos + dos.getStringDetailKodeQty();
            }
            for (Pembelian erp : dataPembelianERP) {
                serp = serp + erp.getStringDetailKodeQty();
            }
            if (sdos.equals(serp)) continue;

            System.out.println(StringUtils.leftPad("", 47) + entry.getKey());
            System.out.println("----------------------------------------------------------------------------------------------");

            String buffNomorPenerimaan = "";
            System.out.println("DOS : ");
            for (Pembelian beli : dataPembelianDOS) {
                System.out.println(beli);
            }
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("ERP : ");
            for (Pembelian beli : dataPembelianERP) {
                System.out.println(beli);
            }
            System.out.println("==============================================================================================");
        }

        System.out.println("\nDaftar transaksi Penerimaan DOS & ERP yang beda tanggal");
        System.out.println("---------------------------------------------------------\n");
        for (Map.Entry<String, Data> entry : map.entrySet()) {
            List<Pembelian> dataPembelianDOS = entry.getValue().getDataPembelianDOS();
            List<Pembelian> dataPembelianERP = entry.getValue().getDataPembelianERP();

            if (dataPembelianDOS.size() == 0 || dataPembelianERP.size() == 0) continue;
            if (dataPembelianDOS.size() != dataPembelianERP.size()) continue;
            String sdos = "";
            String serp = "";
            String sdostgl = "";
            String serptgl = "";

            for (Pembelian beliERP : dataPembelianERP) {
                serp = serp + beliERP.getStringDetailKodeQty();
                serptgl = serptgl + beliERP.getStringDetailKodeQtyTanggal();

                for (Pembelian beliDOS : dataPembelianDOS) {
                    sdos = sdos + beliDOS.getStringDetailKodeQty();
                    sdostgl = sdostgl + beliDOS.getStringDetailKodeQtyTanggal();

                    if (sdos.equals(serp)) {
                        if (sdostgl.equals(serptgl)) {
                            continue;
                        } else {
                            System.out.println(StringUtils.leftPad("", 47) + entry.getKey());
                            System.out.println("----------------------------------------------------------------------------------------------");

                            String buffNomorPenerimaan = "";
                            System.out.println("DOS : ");
                            System.out.println(beliDOS);
                            System.out.println("----------------------------------------------------------------------------------------------");
                            System.out.println("ERP : ");
                            System.out.println(beliERP + "\n");
                        }
                    }
                }
            }
        }
        System.out.println("==============================================================================================");


        if (isTampilkanSemuaDataPenerimaan) {
            System.out.println("\nDaftar transaksi Penerimaan DOS & ERP");
            System.out.println("---------------------------------------------------------\n");
            for (Map.Entry<String, Data> entry : map.entrySet()) {
                System.out.println(StringUtils.leftPad("", 47) + entry.getKey());
                System.out.println("----------------------------------------------------------------------------------------------");
                List<Pembelian> dataPembelianDOS = entry.getValue().getDataPembelianDOS();
                List<Pembelian> dataPembelianERP = entry.getValue().getDataPembelianERP();

                String buffNomorPenerimaan = "";
                System.out.println("DOS : ");
                for (Pembelian beli : dataPembelianDOS) {
                    System.out.println(beli);
                }
                System.out.println("----------------------------------------------------------------------------------------------");
                System.out.println("ERP : ");
                for (Pembelian beli : dataPembelianERP) {
                    System.out.println(beli);
                }
                System.out.println("==============================================================================================");
            }
        }
    }

    private void laporanPenerimaanXXX(Map<String, Data> map, List dataMissingDOS) {

//        int c = 0;
        System.out.println("\nPenerimaan ERP yang tidak sama harga n qty dengan DOS");
        System.out.println("---------------------------------------------------------");
        List missDataDOSQtyHarga = new ArrayList();
        List missDataERPQtyHarga = new ArrayList();
        List dataByPassNomorPenerimaanERP = new ArrayList();
        List dataPenerimaanYangSama = new ArrayList();
        for (Map.Entry<String, Data> entry : map.entrySet()) {
            List<Pembelian> dataPembelianDOS = entry.getValue().getDataPembelianDOS();
            List<Pembelian> dataPembelianERP = entry.getValue().getDataPembelianERP();

            String sERPQtyHarga = "";
            String sDOSQtyHarga = "";
            String sERPQty = "";
            String sDOSQty = "";
            boolean isTidakAdaYangSama = false;
//            System.out.println(entry.getKey()+" -> dos : "+dataPembelianDOS.size()+ ", erp : "+dataPembelianERP.size());
            for (Pembelian beliDOS : dataPembelianDOS) {
                //kalau ada di missingDOS byPass
                //kalau ada di missingERP byPass
                isTidakAdaYangSama = false;
                sDOSQtyHarga = "";
                for (DetailPembelian detail : beliDOS.getDetails()) {
                    sDOSQtyHarga = sDOSQtyHarga + "#" + detail.getKodeBarang() + "#" + detail.getQty() + "#" + detail.getPrice().setScale(0, BigDecimal.ROUND_HALF_DOWN) + "#";
//                    sDOS = sDOS+detail.getKodeSupplier()+"#"+detail.getKodeBarang()+"#"+detail.getQty()+"#"+detail.getPrice()+"#";
                }

//                System.out.println("s-dos : "+beliDOS.getKode()+" "+sDOS);
                for (Pembelian beliERP : dataPembelianERP) {
                    sERPQtyHarga = "";

                    for (DetailPembelian detail : beliERP.getDetails()) {
                        sERPQtyHarga = sERPQtyHarga + "#" + detail.getKodeBarang() + "#" + detail.getQty() + "#" + detail.getPrice().setScale(0, BigDecimal.ROUND_HALF_DOWN) + "#";
//                        sERP = sERP+detail.getKodeSupplier()+"#"+detail.getKodeBarang()+"#"+detail.getQty()+"#"+detail.getPrice()+"#";
                    }

                    if (dataByPassNomorPenerimaanERP.contains(beliERP.getKode())) continue;
//                    System.out.println("s-erp : "+beliERP.getKode()+" "+sERP);

//                    System.out.println(beliERP.getKode());
//                    System.out.println(sDOSQtyHarga);
//                    System.out.println(sERPQtyHarga);
                    if (sDOSQtyHarga.equals(sERPQtyHarga)) {
                        dataByPassNomorPenerimaanERP.add(beliERP.getKode());
                        isTidakAdaYangSama = false;

                        //simpan ke list
                        Data d = new Data(beliDOS, beliERP);
                        Map<String, Data> mapD = Collections.synchronizedMap(new LinkedHashMap());
                        mapD.put(entry.getKey(), d);
                        dataPenerimaanYangSama.add(mapD);
                        break;
                    } else {
                        isTidakAdaYangSama = true;
                    }
                }
                if (isTidakAdaYangSama) {
                    missDataDOSQtyHarga.add(beliDOS);
                }
            }
            //semua data yang tidak da di dataByPassERP masuking ke missing erp
            for (Pembelian beliERP : dataPembelianERP) {
                if (!dataByPassNomorPenerimaanERP.contains(beliERP.getKode())) {
                    missDataERPQtyHarga.add(beliERP);
                }
            }

//            c++;
//            if (c==5) break;
        }
        System.out.println("jumlah data miss qty n harga: " + (missDataDOSQtyHarga.size() + missDataERPQtyHarga.size()));

        System.out.println("DOS : ");
        for (Pembelian beli : (List<Pembelian>) missDataDOSQtyHarga) {
            System.out.println(beli);
        }

        System.out.println("ERP : ");
        boolean isByPasss = false;
        for (Pembelian beli : (List<Pembelian>) missDataERPQtyHarga) {
            for (Pembelian missBeli : (List<Pembelian>) dataMissingDOS) {
                if (beli.getKode().equals(missBeli.getKode())) {
                    isByPasss = true;
                    break;
                } else {
                    isByPasss = false;
                    System.out.println(beli);
                }
            }
//            if (!isByPasss)
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (dataPenerimaanYangSama.size() > 0) {
            System.out.println("\nDaftar transaksi Penerimaan DOS & ERP yang sama tapi beda tanggal");
            System.out.println("---------------------------------------------------------\n");
            for (Map<String, Data> m : (List<Map>) dataPenerimaanYangSama) {
                for (Map.Entry<String, Data> entry : m.entrySet()) {
                    Data d = entry.getValue();
                    if (d.getPembelianDOS().getTanggal().compareTo(d.getPembelianERP().getTanggal()) != 0) {
                        System.out.println("~~> : " + entry.getKey());
                        System.out.println("~~> DOS : " + format.format(d.getPembelianDOS().getTanggal()) + " " + d.getPembelianDOS().getKode());
//                        System.out.println(d.getPembelianDOS());
                        System.out.println("~~> ERP : " + format.format(d.getPembelianERP().getTanggal()) + " " + d.getPembelianERP().getKode());
//                        System.out.println(d.getPembelianERP());
                        System.out.println("---------------------------------------------------------");
                    }
                }
            }
            System.out.println("jumlah data : " + dataPenerimaanYangSama.size());
        }

    }

    private void laporanPenerimaanQTY(Map<String, Data> data) {
        System.out.println("\nLaporan qty dos n erp");
        System.out.println("---------------------------------------------------------");
        Integer sumQtyDOS = 0;
        Integer sumQtyERP = 0;
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            List<DetailPembelian> dataDOS = item.getDataDetailPembelianDOS();
            List<DetailPembelian> dataERP = item.getDataDetailPembelianERP();
            for (DetailPembelian detail : dataDOS) {
                sumQtyDOS = sumQtyDOS + detail.getQty();
            }
            for (DetailPembelian detail : dataERP) {
                sumQtyERP = sumQtyERP + detail.getQty();
            }
        }
        System.out.println("dos : " + sumQtyDOS + ", erp : " + sumQtyERP + " : " + Math.abs(sumQtyDOS - sumQtyERP));
    }

    private void laporanPenerimaanKodeBarangYangSelisihHargaNYA(Map<String, Data> data) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        System.out.println("\nKode DOS yang harga penerimaan nya berbeda antara dos n erp");
//        System.out.println("-----------------------------------------------------------");
//        for (Map.Entry<String, Data> entry : data.entrySet()) {
//            Data item = entry.getValue();
//            List<DetailPembelian > dataDOS = item.getDataDetailPembelianDOS();
//            List<DetailPembelian > dataERP = item.getDataDetailPembelianERP();
//            Integer sumQtyDOS = 0;
//            Integer sumQtyERP = 0;
//            System.out.println(entry.getKey());
//            System.out.println("DOS");
//            for (DetailPembelian detail : dataDOS) {
//                System.out.println("\t"+detail.getPrice()+"  "+format.format(detail.getTanggal()));
//            }
//            System.out.println("ERP");
//            for (DetailPembelian detail : dataERP) {
//                System.out.println("\t"+detail.getPrice()+"  "+format.format(detail.getTanggal()));
//            }
//        }
    }

    public List laporanAdjustmentKodeBarangYangSelisihQtyNYA(Map<String, Data> data, List dataKodeBarangYangMissing) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List dataKodeBarangYangQTYTidakSama = new ArrayList();

        System.out.println("\nKode DOS yang qty adjustment nya berbeda antara dos n erp");
        System.out.println("---------------------------------------------------------");
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            List<DetailAdjustment> dataDOS = item.getDataDetailAdjustmentDOS();
            List<DetailAdjustment> dataERP = item.getDataDetailAdjustmentERP();
            Integer sumQtyDOS = 0;
            Integer sumQtyERP = 0;

            for (DetailAdjustment detail : dataDOS) {
                sumQtyDOS = sumQtyDOS + detail.getQty();
            }
            for (DetailAdjustment detail : dataERP) {
                sumQtyERP = sumQtyERP + detail.getQty();
            }
            if (!sumQtyDOS.equals(sumQtyERP) && (sumQtyDOS > 0 && sumQtyERP > 0)) {
                Integer selisih = sumQtyERP - sumQtyDOS;
                dataKodeBarangYangQTYTidakSama.add(entry.getKey());
                System.out.println("Kode Barang : " + entry.getKey() + " = [dos : " + sumQtyDOS + ", erp : " + sumQtyERP + "] => " + Math.abs(selisih) + " " + ((selisih > 0) ? "*" : "!"));
            }
        }

        if (!dataKodeBarangYangQTYTidakSama.isEmpty()) {
            System.out.println("* QTY ERP Lebih besar dari pada DOS");
            System.out.println("! QTY DOS Lebih besar dari pada ERP");
        } else {
            System.out.println("-");
        }

        boolean isMutasi = false;
        boolean isAdaYangBeda = false;
        List dataDetailAdjustmentYangSama = new ArrayList();

        System.out.println("\nDaftar Adjustment DOS & ERP hasil mutasi dari erp yang beda:");
        System.out.println("---------------------------------------------------------");
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Adjustment adjDOS = entry.getValue().getAdjustmentDOS();
            Adjustment adjERP = entry.getValue().getAdjustmentERP();

            for (DetailAdjustment dERP : adjERP.getDetails()) {
                if (dERP.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                    isMutasi = true;
                    break;
                } else {
                    isMutasi = false;
                }
            }
            if (!isMutasi) continue;

            if (dataKodeBarangYangMissing.contains(entry.getKey())) continue;

            List<Integer> listIndexToByPassDOS = new ArrayList();
            List listDataTidakAdaPasanganERP = new ArrayList();
            List listDataTidakAdaPasanganDOS = new ArrayList();
            for (DetailAdjustment dERP : adjERP.getDetails()) {

                boolean isAdaYangSama = false;
                for (int counter = 0; counter < adjDOS.getDetails().size(); counter++) {
                    DetailAdjustment dDOS = adjDOS.getDetails().get(counter);
                    if (listIndexToByPassDOS.indexOf(counter) >= 0) continue;
                    if (dERP.getTanggal().equals(dDOS.getTanggal())
                            && dERP.getQty().equals(dDOS.getQty())
                            ) {
                        listIndexToByPassDOS.add(counter);
                        isAdaYangSama = true;
                        break;
                    } else {
                        isAdaYangSama = false;
                    }
                }
                if (!isAdaYangSama) {
                    listDataTidakAdaPasanganERP.add(dERP);
                }
            }

            if (listIndexToByPassDOS.size() != adjDOS.getDetails().size()) {
                isAdaYangBeda = true;
                for (Integer i = 0; i < adjDOS.getDetails().size(); i++) {
                    if (listIndexToByPassDOS.contains(i)) continue;
                    listDataTidakAdaPasanganDOS.add(adjDOS.getDetails().get(i));
                }
            }
            Collections.sort(listDataTidakAdaPasanganDOS, DetailAdjustment.COMPARE_BY_BARANG_AND_QTY_PRICE);
            if (listDataTidakAdaPasanganDOS.size() > 0) {
                System.out.println(entry.getKey());
                System.out.println("DOS : ");
                for (DetailAdjustment d : (List<DetailAdjustment>) listDataTidakAdaPasanganDOS) {
                    System.out.println(d);
                }
            }

            if (listDataTidakAdaPasanganERP.size() > 0) {
                isAdaYangBeda = true;
                System.out.println("ERP :");
                for (Object d : listDataTidakAdaPasanganERP) {
                    System.out.println(d);
                }
                System.out.println("----------------------------------------------------------");
            }


            for (Integer i = 0; i < adjDOS.getDetails().size(); i++) {
                if (listIndexToByPassDOS.contains(i)) {
                    dataDetailAdjustmentYangSama.add(adjDOS.getDetails().get(i));
                }
            }
        }

        String sDetail = "";
        sDetail += "Daftar Adjustment +/- yang di input di ERP\n";
        sDetail += "---------------------------------------------------------\n";
        DecimalFormat df = new DecimalFormat("#,###.####");

        List indexToBypass = new ArrayList();
        for (DetailAdjustment dPositif : (List<DetailAdjustment>) dataDetailAdjustmentYangSama) {
            sDetail += StringUtils.rightPad(dPositif.getNomor(), 7) + ";" +
                    StringUtils.leftPad(format.format(dPositif.getTanggal()), 12) + ";  " +
                    StringUtils.rightPad(dPositif.getKodeBarang(), 20) + ";" +
                    StringUtils.leftPad(df.format(dPositif.getQty()), 11) + ";" +
                    StringUtils.leftPad(df.format(dPositif.getPrice()), 15) + "\n";
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Daftar-Input-Adjustment.txt"));
            writer.write(sDetail);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataKodeBarangYangQTYTidakSama;
    }

    public List laporanPenjualanKodeBarangYangSelisihQtyNYA(Map<String, Data> data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List dataKodeBarangYangQTYTidakSama = new ArrayList();

        System.out.println("\nKode DOS yang qty penjualan nya berbeda antara dos n erp");
        System.out.println("---------------------------------------------------------");
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            List<DetailInvoice> dataDOS = item.getPenjualanDOS().getDetails();
//            List<DetailInvoice> dataERP = item.getPenjualanERP().getDetails();
//            Integer sumQtyDOS = 0;
//            Integer sumQtyERP = 0;
//            for (DetailInvoice detail : dataDOS) {
//                sumQtyDOS = sumQtyDOS + detail.getQty();
//            }
//            for (DetailInvoice detail : dataERP) {
//                sumQtyERP = sumQtyERP + detail.getQty();
//            }
//            if (!sumQtyDOS.equals(sumQtyERP) && (sumQtyDOS > 0 && sumQtyERP > 0)) {
//                Integer selisih = sumQtyERP - sumQtyDOS;
//                dataKodeBarangYangQTYTidakSama.add(entry.getKey());
//                System.out.println("Kode Barang : " + entry.getKey() + " = [dos : " + sumQtyDOS + ", erp : " + sumQtyERP + "] => " + Math.abs(selisih) + " " + ((selisih > 0) ? "*" : "!"));
//            }
        }
        System.out.println("* QTY ERP Lebih besar dari pada DOS");
        System.out.println("! QTY DOS Lebih besar dari pada ERP");

        return dataKodeBarangYangQTYTidakSama;
    }

    public List laporanPenerimaanKodeBarangYangSelisihQtyNYA(Map<String, Data> data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List dataKodeBarangYangQTYTidakSama = new ArrayList();

        System.out.println("\nKode DOS yang qty penerimaan nya berbeda antara dos n erp");
        System.out.println("---------------------------------------------------------");
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            List<DetailPembelian> dataDOS = item.getDataDetailPembelianDOS();
            List<DetailPembelian> dataERP = item.getDataDetailPembelianERP();
            Integer sumQtyDOS = 0;
            Integer sumQtyERP = 0;
            for (DetailPembelian detail : dataDOS) {
                sumQtyDOS = sumQtyDOS + detail.getQty();
            }
            for (DetailPembelian detail : dataERP) {
                sumQtyERP = sumQtyERP + detail.getQty();
            }
            if (!sumQtyDOS.equals(sumQtyERP) && (sumQtyDOS > 0 && sumQtyERP > 0)) {
                Integer selisih = sumQtyERP - sumQtyDOS;
                dataKodeBarangYangQTYTidakSama.add(entry.getKey());
                System.out.println("Kode Barang : " + entry.getKey() + " = [dos : " + sumQtyDOS + ", erp : " + sumQtyERP + "] => " + Math.abs(selisih) + " " + ((selisih > 0) ? "*" : "!"));
            }
        }
        System.out.println("* QTY ERP Lebih besar dari pada DOS");
        System.out.println("! QTY DOS Lebih besar dari pada ERP");

        return dataKodeBarangYangQTYTidakSama;
    }

    public void laporanPenerimaanKodeBarangYangJomplang(Map<String, Data> data) {
        List listERP = new ArrayList();
        List listDOS = new ArrayList();
        //kode barang yang hanya ada di dos / erp
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            if (item.getDataDetailPembelianDOS().isEmpty()) {
                listDOS.add(entry.getKey());
            }

            if (item.getDataDetailPembelianERP().isEmpty()) {
                listERP.add(entry.getKey());
            }
        }

        Collections.sort(listDOS);
        Collections.sort(listERP);

        System.out.println("\n------------");
        System.out.println(" PEMBELIAN  ");
        System.out.println("------------");
        if (listDOS.size() > 0) {
            System.out.println("\nKode Barang yang tidak ada di DOS : ");
            System.out.println("-----------------------------------");
            for (String kodeBarang : (List<String>) listDOS) {
                System.out.println(kodeBarang);
            }
            System.out.println("Jumlah data : " + listDOS.size());
        }

        if (listERP.size() > 0) {
            System.out.println("\nKode Barang yang tidak ada di ERP : ");
            System.out.println("-----------------------------------");
            for (String kodeBarang : (List<String>) listERP) {
                System.out.println(kodeBarang);
            }
            System.out.println("Jumlah data : " + listERP.size());
        }
    }

    public List laporanAdjustmentKodeBarangYangTidakAdaDiERPDOS(Map<String, Data> data) {
        List listKodeBarangYangTidakAda = new ArrayList();
        List listERP = new ArrayList();
        List listDOS = new ArrayList();
        //kode barang yang hanya ada di dos / erp
        for (Map.Entry<String, Data> entry : data.entrySet()) {
            Data item = entry.getValue();
            if (item.getDataDetailAdjustmentDOS().isEmpty()) {
                listDOS.add(entry.getKey());
            }

            if (item.getDataDetailAdjustmentERP().isEmpty()) {
                listERP.add(entry.getKey());
            }
        }

        Collections.sort(listDOS);
        Collections.sort(listERP);

        if (listDOS.size() > 0) {
            System.out.println("\nKode Barang yang tidak ada di DOS : ");
            System.out.println("-----------------------------------");
            for (String kodeBarang : (List<String>) listDOS) {
                System.out.println(kodeBarang);
            }
            System.out.println("Jumlah data : " + listDOS.size());
        }

        if (listERP.size() > 0) {
            System.out.println("\nKode Barang yang tidak ada di ERP : ");
            System.out.println("-----------------------------------");
            for (String kodeBarang : (List<String>) listERP) {
                System.out.println(kodeBarang);
            }
            System.out.println("Jumlah data : " + listERP.size());
        }
        listKodeBarangYangTidakAda.addAll(listDOS);
        listKodeBarangYangTidakAda.addAll(listERP);
        return listKodeBarangYangTidakAda;
    }



    public static void main(String[] args) throws Exception {
        String filePathDOS;
        String filePathPenjualanERP;
        String filePathAdjustmentERP;
        String filePathPembelianERP;
        String filePathStockDOS;
        String filePathStockERP;
        String toleransi;
        String tampilkanSemuaPenerimaan = null;
        if (args == null) throw new Exception("Masukkan path file dos & ERP !!!");
        if (args.length < 2) throw new Exception("Masukkan path file dos & ERP !!!");
        //dos
        if (args[0].contains("/")) {
            filePathDOS = args[0];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathDOS = jarFile.getParent() + File.separator + args[0];
        }
        if (args[0].contains("/")) {
            filePathDOS = args[0];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathDOS = jarFile.getParent() + File.separator + args[0];
        }
        File fileDOS = new File(filePathDOS);

        //penjualan erp
        if (args[1].contains("/")) {
            filePathPenjualanERP = args[1];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathPenjualanERP = jarFile.getParent() + File.separator + args[1];
        }
        if (args[1].contains("/")) {
            filePathPenjualanERP = args[1];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathPenjualanERP = jarFile.getParent() + File.separator + args[1];
        }
        File filePenjualanERP = new File(filePathPenjualanERP);

        //pembelian erp
        if (args[2].contains("/")) {
            filePathPembelianERP = args[2];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathPembelianERP = jarFile.getParent() + File.separator + args[2];
        }
        if (args[2].contains("/")) {
            filePathPembelianERP = args[2];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathPembelianERP = jarFile.getParent() + File.separator + args[2];
        }

        //adjustment +
        if (args[3].contains("/")) {
            filePathAdjustmentERP = args[3];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathAdjustmentERP = jarFile.getParent() + File.separator + args[3];
        }
        if (args[3].contains("/")) {
            filePathAdjustmentERP = args[3];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathAdjustmentERP = jarFile.getParent() + File.separator + args[3];
        }

        if (args[4].contains("/")) {
            filePathStockDOS = args[4];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathStockDOS = jarFile.getParent() + File.separator + args[4];
        }

        if (args[5].contains("/")) {
            filePathStockERP = args[5];
        } else {
            File jarFile = new File(CompareDataOmzet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            filePathStockERP = jarFile.getParent() + File.separator + args[5];
        }

        if (args[6] == null) {
            throw new Exception("Nilai toleransi selisih penjualan belum di masukkan");
        } else {
            toleransi = args[6];
        }

        try {
            if (args[7] == null) {
                System.out.println("Data semua penerimaan tidak di tampilkan");
            } else {
                tampilkanSemuaPenerimaan = args[7];
            }
        } catch (Exception e) {

        }


        File filePembelianERP = new File(filePathPembelianERP);
        File fileAdjustmentERP = new File(filePathAdjustmentERP);
        File fileStockDOS = new File(filePathStockDOS);
        File fileStockERP = new File(filePathStockERP);
        CompareDataOmzet data = new CompareDataOmzet(fileDOS, filePenjualanERP, filePembelianERP, fileAdjustmentERP, fileStockDOS, fileStockERP, toleransi, tampilkanSemuaPenerimaan);
        data.laporanPenjualan();
        data.laporanPembelian();
        data.laporanAdjustment();
        data.laporanStock();
    }
}