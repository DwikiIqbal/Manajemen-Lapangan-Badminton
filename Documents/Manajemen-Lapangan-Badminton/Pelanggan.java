public abstract class Pelanggan {
    protected String nama;
    protected int jamMulai;
    protected int lamaMain;
    protected String hariMain;
    protected String jenisPelanggan;

    public Pelanggan(String nama, String hariMain, int jamMulai, int lamaMain, String jenisPelanggan) {
        this.nama = nama;
        this.hariMain = hariMain; // SET HARI
        this.jamMulai = jamMulai;
        this.lamaMain = lamaMain;
        this.jenisPelanggan = jenisPelanggan;
    }

    public String getNama() {
        return nama;
    }

    public String getHariMain() { 
        return hariMain;
    }

    public int getJamMulai() {
        return jamMulai;
    }

    public int getLamaMain() {
        return lamaMain;
    }

    public int getJamSelesai() {
        return jamMulai + lamaMain;
    }

    public String getJenisPelanggan() {
        return jenisPelanggan;
    }

    public abstract double hitungBiaya();

    public abstract String getStatusTransaksi();
}
