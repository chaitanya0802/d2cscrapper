package utils;

public class Product {

    // Data members
    private String productId;          // unique ID (e.g., "boat034324242")
    private String productName;        // Product Name (mandatory)
    private String productUrl;         // Product URL (mandatory)
    private String productImageUrl;    // Product Image URL
    private String mainCategory;     // Main Category
    private String subCategory;        // Sub Category
    private int productPrice;       // Product Price (mandatory)
    private int discount_percent;
    private String productDescription; // Product Description (mandatory)
    private float productRating;         // Product Rating (int)
    private boolean isAvailable;       // Availability (true/false)

    // Constructor
    public Product(String productId, String productName, String productUrl,
                   String productImageUrl, String mainCategory, String subCategory,
                   int productPrice, int discount_percent, String productDescription, float productRating,
                   boolean isAvailable) {
        this.productId = productId;
        this.productName = productName;
        this.productUrl = productUrl;
        this.productImageUrl = productImageUrl;
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.productPrice = productPrice;
        this.discount_percent = discount_percent;
        this.productDescription = productDescription;
        this.productRating = productRating;
        this.isAvailable = isAvailable;
    }

    // Default constructor
    public Product() {
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public String getmainCategory() {
        return mainCategory;
    }

    public void setmainCategory(String mainCategory) {
        this.mainCategory = mainCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public float getProductRating() {
        return productRating;
    }

    public void setProductRating(float productRating) {
        this.productRating = productRating;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "productId = " + productId +
                ", \nproductName = " + productName +
                ", \nproductUrl = " + productUrl +
                ", \nproductImageUrl = " + productImageUrl +
                ", \nmainCategory = " + mainCategory +
                ", \nsubCategory = " + subCategory +
                ", \nproductPrice = " + productPrice +
                ", \ndiscount_percent = " + discount_percent +
                ", \nproductDescription = " + productDescription +
                ", \nproductRating = " + productRating +
                ", \nisAvailable = " + isAvailable;
    }
}
