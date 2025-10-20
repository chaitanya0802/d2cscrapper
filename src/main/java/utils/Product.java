package utils;

import java.util.ArrayList;

public class Product {

    // Data members (13)
    private String productId;          // unique ID (e.g., "boat034324242")
    private String productName;        // Product Name (mandatory)
    private String productUrl;         // Product URL (mandatory)
    private String productImageUrl;    // Product Image URL
    private ArrayList<Integer> categories;     //  list of Category
    private float productPrice;       // Product Price (mandatory)
    private int discount_percent;
    private String productDescription; // Product Description (mandatory)
    private float productRating;         // Product Rating (int)
    private boolean isAvailable;       // Availability (true/false)

    private int store_id;
    private String store_name;
    private String storeUrl;

    // Constructor
    public Product(String productId, String productName, String productUrl,
                   String productImageUrl, ArrayList<Integer> categories,
                   float productPrice, int discount_percent, String productDescription, float productRating,
                   boolean isAvailable, int store_id, String store_name, String storeUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productUrl = productUrl;
        this.productImageUrl = productImageUrl;
        this.categories = categories;
        this.productPrice = productPrice;
        this.discount_percent = discount_percent;
        this.productDescription = productDescription;
        this.productRating = productRating;
        this.isAvailable = isAvailable;
        this.store_id = store_id;
        this.store_name = store_name;
        this.storeUrl = storeUrl;
    }

    // Default constructor
    public Product() {
    }

    @Override
    public String toString() {
        return "productId = " + productId +
                ", \nproductName = " + productName +
                ", \nproductUrl = " + productUrl +
                ", \nproductImageUrl = " + productImageUrl +
                ", \ncategories = " + categories +
                ", \nproductPrice = " + productPrice +
                ", \ndiscount_percent = " + discount_percent +
                ", \nproductDescription = " + productDescription +
                ", \nproductRating = " + productRating +
                ", \nisAvailable = " + isAvailable +
                ", \nstore_id = " + store_id +
                ", \nstore_name = " + store_name +
                ", \nstoreUrl = " + storeUrl;
    }
}
