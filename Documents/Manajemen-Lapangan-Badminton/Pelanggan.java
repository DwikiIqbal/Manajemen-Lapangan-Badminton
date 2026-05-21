public abstract class Pelanggan {
    protected String nama;
    protected int jamMulai;
    protected int lamaMain;
    protected String jenisPelanggan;

    public Pelanggan(String nama, int jamMulai, int lamaMain, String jenisPelanggan) {
        this.nama = nama;
        this.jamMulai = jamMulai;
        this.lamaMain = lamaMain;
        this.jenisPelanggan = jenisPelanggan;
    }

    public String getNama() {
        return nama;
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
