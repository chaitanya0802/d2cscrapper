package utils;


public class Offer {
    private String offerId;
    private String offerLink;
    private String offerImage;
    private String offerName;
    private int maincategory;     // Category

    private int store_id;
    private String store_name;
    private String storeUrl;

    // Constructor
    public Offer(String offerId, String offerLink, String offerImage, String offerName, 
    int maincategory, int store_id, String store_name, String storeUrl) {
        this.offerId = offerId;
        this.offerLink = offerLink;
        this.offerImage = offerImage;
        this.offerName = offerName;
        this.maincategory = maincategory;
        this.store_id= store_id;
        this.store_name= store_name;
        this.storeUrl= storeUrl;

    }

    // Default constructor
    public Offer() {
    }

    // Getters
    public String getOfferId() {
        return offerId;
    }

    public String getOfferLink() {
        return offerLink;
    }

    public String getOfferImage() {
        return offerImage;
    }

    public String getOfferName() {
        return offerName;
    }

    public Integer getCategory() {
        return maincategory;
    }

    // Setters
    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public void setOfferLink(String offerLink) {
        this.offerLink = offerLink;
    }

    public void setOfferImage(String offerImage) {
        this.offerImage = offerImage;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public void setCategory(Integer maincategory) {
        this.maincategory = maincategory;
    }

    // toString (useful for debugging)
    @Override
    public String toString() {
        return  "offerName=" + offerName + 
                "\nofferId=" + offerId + 
                "\nofferLink=" + offerLink + 
                "\nofferImage=" + offerImage + 
                "\nmaincategory=" + maincategory + 
                "\nstore_id=" + store_id + 
                "\nstore_name=" + store_name + 
                "\nstoreUrl=" + storeUrl ;
                
    }
}
