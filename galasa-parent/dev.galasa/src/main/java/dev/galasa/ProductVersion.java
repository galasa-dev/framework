package dev.galasa;

public class ProductVersion implements Comparable<ProductVersion> {

    private final int version;
    private final int release;
    private final int modification;

    private ProductVersion(int version, int release, int modification) {
        this.version = version;
        this.release = release;
        this.modification = modification;
    }

    public static ProductVersion v(int version) {
        return new ProductVersion(version, 0, 0);
    }

    public ProductVersion r(int release) {
        return new ProductVersion(this.version, release, 0);
    }

    public ProductVersion m(int modification) {
        return new ProductVersion(this.version, this.release, modification);
    }

    @Override
    public String toString() {
        return Integer.toString(this.version) + "." + Integer.toString(this.release) + "."
                + Integer.toString(this.modification);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProductVersion)) {
            return false;
        }

        ProductVersion o = (ProductVersion) other;

        if (this.version != o.version || this.release != o.release || this.modification != o.modification) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(ProductVersion o) {
        int c = this.version - o.version;
        if (c != 0) {
            return c;
        } 
                
        c = this.release - o.release;
        if (c != 0) {
            return c;
        } 
                
        return this.modification - o.modification;
    }

    public boolean isEarlierThan(ProductVersion o) {
        return (compareTo(o) < 0);
    }

    public boolean isLaterThan(ProductVersion o) {
        return (compareTo(o) > 0);
    }

}